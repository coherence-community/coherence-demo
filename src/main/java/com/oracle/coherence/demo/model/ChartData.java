/*
 * File: ChartData.java
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

package com.oracle.coherence.demo.model;

import com.tangosol.io.pof.PortableObject;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.tangosol.io.pof.schema.annotation.PortableType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * An immutable class to represent the chart data for a cluster.
 *
 * @author Brian Oliver
 */
@XmlRootElement(name = "chart-data")
@XmlAccessorType(XmlAccessType.PROPERTY)
@PortableType
public class ChartData {
    @SuppressWarnings("unused")
    private static final long serialVersionUID = -4557078639768809864L;

    /**
     * The instant in time the {@link ChartData} was created.
     */
    private long instant;

    /**
     * The duration in milliseconds for the aggregations to complete.
     */
    private long aggregationDuration;

    /**
     * The {@link MemberInfo} for the cluster members.
     */
    private List<MemberInfo> memberInfo;

    /**
     * Trade summary.
     */
    private Map<String, TradeSummary> tradeSummary;

    /**
     * Current price;
     */
    private Map<String, Double> currentPrice;

    /**
     * Default Constructor (required and used only by {@link PortableObject}).
     */
    @SuppressWarnings("unused")
    public ChartData() {
        // required for Serializable and PortableObject
    }

    /**
     * Construct a {@link ChartData}.
     *
     * @param instant             time at which the sample was taken
     * @param mapTradeSummary     summary of trades
     * @param currentPrice        current price
     * @param memberInfo          {@link MemberInfo} containing detailed information
     * @param aggregationDuration time to calculate aggregations
     */
    public ChartData(long instant,
                     Map<String, TradeSummary> mapTradeSummary,
                     Map<String, Double> currentPrice,
                     Collection<MemberInfo> memberInfo,
                     long aggregationDuration) {
        this.instant = instant;
        this.memberInfo = new Vector<>(memberInfo);
        this.aggregationDuration = aggregationDuration;
        this.currentPrice = currentPrice;
        this.tradeSummary = mapTradeSummary;
    }


    /**
     * Obtain the time the ChartData was created.
     *
     * @return the time
     */
    @SuppressWarnings("unused")
    public long getInstant() {
        return instant;
    }


    /**
     * Obtain a {@link Map} representing the trade summary.
     *
     * @return symbol count
     */
    @SuppressWarnings("unused")
    public Map<String, TradeSummary> getTradeSummary() {
        return tradeSummary;
    }

    /**
     * Obtain a {@link Map} representing the current stock price.
     *
     * @return symbol count
     */
    public Map<String, Double> getCurrentPrice() {
        return currentPrice;
    }

    /**
     * Obtain the list of {@link MemberInfo} for this execution.
     *
     * @return the list of {@link MemberInfo}
     */
    @SuppressWarnings("unused")
    public List<MemberInfo> getMemberInfo() {
        return memberInfo;
    }

    /**
     * Obtain the duration of the aggregations.
     *
     * @return the duration
     */
    @SuppressWarnings("unused")
    public long getAggregationDuration() {
        return aggregationDuration;
    }
}
