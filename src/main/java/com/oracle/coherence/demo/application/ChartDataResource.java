/*
 * File: ChartDataResource.java
 *
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates.
 *
 * You may not use this file except in compliance with the Universal Permissive
 * License (UPL), Version 1.0 (the "License.")
 *
 * You may obtain a copy of the License at https://opensource.org/licenses/UPL.
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and limitations
 * under the License.
 */

package com.oracle.coherence.demo.application;

import com.oracle.coherence.demo.invocables.GetMemberInfo;

import com.oracle.coherence.demo.model.ChartData;
import com.oracle.coherence.demo.model.MemberInfo;
import com.oracle.coherence.demo.model.Price;
import com.oracle.coherence.demo.model.Trade;

import com.oracle.bedrock.util.StopWatch;

import com.oracle.coherence.demo.model.TradeSummary;
import com.oracle.coherence.demo.model.TradeSummaryAggregator;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.DistributedCacheService;
import com.tangosol.net.InvocationService;
import com.tangosol.net.Member;
import com.tangosol.net.NamedCache;

import com.tangosol.util.aggregator.GroupAggregator;
import com.tangosol.util.aggregator.ReducerAggregator;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

import jakarta.ws.rs.core.Response;

import java.util.Map;
import java.util.Set;

import java.util.concurrent.TimeUnit;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.APPLICATION_XML;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;

/**
 * A JAX-RS resource providing raw data for application charting. If the PathParam updatePrices
 * is set to true then before we carry out the aggregations, we make an
 * update to the prices. This is set to true when Real-Time Price Updates is
 * checked within the web application.
 *
 * @author Brian Oliver
 */
@Path("/chart-data")
@SuppressWarnings("rawTypes")
public class ChartDataResource
{
    /**
     * Obtain the chart data as JSON, optionally updating the prices.
     *
     * @param updatePrices  flag indicating if prices should be updated when obtaining the chart data
     *
     * @return JSON chart data for stock prices
     */
    @GET
    @Path("{updatePrices}")
    @Produces({APPLICATION_JSON, APPLICATION_XML, TEXT_PLAIN})
    @SuppressWarnings("unchecked")
    public Response getChartData(@PathParam("updatePrices") boolean updatePrices) {
        // we're going to query the positions cache
        NamedCache<String, Trade> trades    = Utilities.getTradesCache();
        int                       cacheSize = trades.size();

        // we measure the time our aggregations take
        StopWatch stopWatch = new StopWatch();

        // update prices outside the timer, so we don't affect the overall stopwatch time
        if (updatePrices && cacheSize > 0)
        {
            Utilities.updatePrices();
        }

        stopWatch.start();

        Map<String, TradeSummary> mapTradesBySymbol = trades.aggregate(GroupAggregator.createInstance(Trade::getSymbol,
                                                                                              new TradeSummaryAggregator()));
        stopWatch.stop();
        
        Map<String, Double> symbolPrice = Utilities.getPricesCache().aggregate(new ReducerAggregator<>(Price::getPrice));

        InvocationService invocationService = (InvocationService) CacheFactory.getService("InvocationService");

        // determine the storage enabled members for the membership query
        Set<Member> storageEnabledMembers =
            ((DistributedCacheService) trades.getCacheService()).getOwnershipEnabledMembers();

        // determine the member information
        Map<Member, MemberInfo> memberInfoMap =
                invocationService.query(new GetMemberInfo(trades.getCacheName()), storageEnabledMembers);

        // establish the chart data
        ChartData data = new ChartData(CacheFactory.getCluster().getTimeMillis(),
                                       mapTradesBySymbol,
                                       symbolPrice,
                                       memberInfoMap.values(),
                                       stopWatch.getElapsedTimeIn(TimeUnit.MILLISECONDS));

        return Response.ok(data).build();
    }
}
