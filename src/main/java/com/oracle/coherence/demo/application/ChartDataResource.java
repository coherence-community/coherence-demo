/*
 * File: ChartDataResource.java
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates.
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
import com.oracle.coherence.demo.model.Trade;

import com.oracle.bedrock.util.StopWatch;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.DistributedCacheService;
import com.tangosol.net.InvocationService;
import com.tangosol.net.Member;
import com.tangosol.net.NamedCache;
import com.tangosol.util.UUID;
import com.tangosol.util.aggregator.Count;
import com.tangosol.util.aggregator.DoubleSum;
import com.tangosol.util.aggregator.GroupAggregator;
import com.tangosol.util.aggregator.LongSum;
import com.tangosol.util.filter.PresentFilter;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

/**
 * A JAX-RS resource providing raw data for application charting. If the PathParam updatePrices
 * is set to true then before we carry out the aggregations, we make an
 * update to the prices. This is set to true when Real-Time Price Updates is
 * checked in the web application.
 *
 * @author Brian Oliver
 */
@Path("/chart-data")
public class ChartDataResource
{
    @GET
    @Path("{updatePrices}")
    @Produces({APPLICATION_JSON, APPLICATION_XML, TEXT_PLAIN})
    @SuppressWarnings("unchecked")
    public Response getChartData(@PathParam("updatePrices") boolean updatePrices)
    {
        // we're going to query the positions cache
        NamedCache<UUID, Trade> trades    = Utilities.getTradesCache();

        int                     cacheSize = trades.size();

        // we measure the time our aggregations take
        StopWatch stopWatch = new StopWatch();

        // update prices outside the timer so we don't affect the overall stopwatch time
        if (updatePrices && cacheSize > 0)
        {
            Utilities.updatePrices();
        }

        stopWatch.start();

        // determine the frequency of each of the symbols
        Map<String, Long> symbolFrequency = (Map<String, Long>) trades.aggregate(PresentFilter.INSTANCE,
                                                                                 GroupAggregator.createInstance(Trade::getSymbol,
                                                                                     new LongSum<>(Trade::getAmount)));

        // determine the number of positions with the symbol
        Map<String, Integer> symbolCount = (Map<String, Integer>) trades.aggregate(PresentFilter.INSTANCE,
                                                                                   GroupAggregator.createInstance(Trade::getSymbol,
                                                                                       new Count<>()));

        // get the current prices for the symbols using Map default methods which access the cache
        Map<String, Double> symbolPrice = new HashMap<>();

        Utilities.getPricesCache().forEach((key, value) -> symbolPrice.put(key, value.getPrice()));

        // determine the original valuation of the positions
        double originalValuation = cacheSize == 0 ? 0 : trades.aggregate(PresentFilter.INSTANCE,
                                                                         new DoubleSum<>(Trade::getPurchaseValue));

        stopWatch.stop();

        InvocationService invocationService = (InvocationService) CacheFactory.getService("InvocationService");

        // determine the storage enabled members for the membership query
        Set<Member> storageEnabledMembers =
            ((DistributedCacheService) trades.getCacheService()).getOwnershipEnabledMembers();

        // determine the member information
        Map<Member, MemberInfo> memberInfoMap =
            (Map<Member, MemberInfo>) invocationService.query(new GetMemberInfo(trades.getCacheName()),
                                                              storageEnabledMembers);

        // establish the chart data
        ChartData data = new ChartData(CacheFactory.getCluster().getTimeMillis(),
                                       cacheSize,
                                       symbolFrequency.keySet(),
                                       symbolFrequency,
                                       originalValuation,
                                       memberInfoMap.values(),
                                       stopWatch.getElapsedTimeIn(TimeUnit.MILLISECONDS),
                                       symbolPrice,
                                       symbolCount);

        return Response.ok(data).build();
    }
}
