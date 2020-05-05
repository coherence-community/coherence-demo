/*
 * File: ChartData.java
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

package com.oracle.coherence.demo.model;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

import java.io.IOException;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * An immutable class to represent the chart data for a cluster.
 *
 * @author Brian Oliver
 */
@XmlRootElement(name = "chart-data")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class ChartData implements PortableObject
{
    @SuppressWarnings("unused")
    private static final long serialVersionUID = -4557078639768809864L;

    /**
     * POF index for instant attribute access.
     */
    private static final int INSTANT = 0;

    /**
     * POF index for aggregationDuration attribute.
     */
    private static final int AGGREGATION_DURATION = 1;

    /**
     * POF index for positionCount attribute.
     */
    private static final int POSITION_COUNT = 2;

    /**
     * POF index for symbols attribute.
     */
    private static final int SYMBOLS = 3;

    /**
     * POF index for symbolFrequency attribute.
     */
    private static final int SYMBOL_FREQUENCY = 4;

    /**
     * POF index for symbolPrice attribute.
     */
    private static final int SYMBOL_PRICE = 5;

    /**
     * POF index for symbolCount attribute.
     */
    private static final int SYMBOL_COUNT = 6;

    /**
     * POF index for originalValuation attribute.
     */
    private static final int ORIGINAL_VALUATION = 7;

    /**
     * POF index for memberInfo attribute.
     */
    private static final int MEMBER_INFO = 8;

    /**
     * The instant in time the {@link ChartData} was created.
     */
    private long instant;

    /**
     * The duration in milliseconds for the aggregations to complete.
     */
    private long aggregationDuration;

    /**
     * The number of positions (entries in the positions cache).
     */
    private int positionCount;

    /**
     * The symbols.
     */
    private Set<String> symbols;

    /**
     * The frequency of each symbol.
     */
    private Map<String, Long> symbolFrequency;

    /**
     * The current price of each symbol.
     */
    private Map<String, Double> symbolPrice;

    /**
     * The count of positions for each symbol.
     */
    private Map<String, Integer> symbolCount;

    /**
     * The valuation of the portfolio at purchase time.
     */
    private double originalValuation;

    /**
     * The {@link MemberInfo} for the cluster members.
     */
    private List<MemberInfo> memberInfo;


    /**
     * Default Constructor (required and used only by {@link PortableObject}).
     */
    @SuppressWarnings("unused")
    public ChartData()
    {
        // required for Serializable and PortableObject
    }


    /**
     * Construct a {@link ChartData}.
     *
     * @param instant             time at which the sample was taken
     * @param positionCount       number of positions present
     * @param symbols             symbols that are present
     * @param symbolFrequency     frequency of each of the symbols
     * @param originalValuation   original valuation before price changes
     * @param memberInfo          {@link MemberInfo} containing detailed information
     * @param aggregationDuration time to calculate aggregations
     * @param symbolPrice         symbol prices
     * @param symbolCount         count of symbols
     */
    public ChartData(long                   instant,
                     int                    positionCount,
                     Collection<String>     symbols,
                     Map<String, Long>      symbolFrequency,
                     double                 originalValuation,
                     Collection<MemberInfo> memberInfo,
                     long                   aggregationDuration,
                     Map<String, Double>    symbolPrice,
                     Map<String, Integer>   symbolCount)
    {
        this.instant             = instant;
        this.positionCount       = positionCount;
        this.symbols             = new TreeSet<>(symbols);
        this.symbolFrequency     = new HashMap<>(symbolFrequency);
        this.originalValuation   = originalValuation;
        this.memberInfo          = new Vector<>(memberInfo);
        this.aggregationDuration = aggregationDuration;
        this.symbolPrice         = symbolPrice;
        this.symbolCount         = symbolCount;
    }


    /**
     * Obtain the time the ChartData was created.
     *
     * @return the time
     */
    @SuppressWarnings("unused")
    public long getInstant()
    {
        return instant;
    }


    /**
     * Obtain the number of positions that were found.
     *
     * @return the number of positions
     */
    @SuppressWarnings("unused")
    public int getPositionCount()
    {
        return positionCount;
    }


    /**
     * Obtain the list of symbols that were found.
     *
     * @return the symbols
     */
    public Set<String> getSymbols()
    {
        return symbols;
    }


    /**
     * Obtain a {@link Map} representing the symbol frequency.
     *
     * @return the symbol frequency
     */
    @SuppressWarnings("unused")
    public Map<String, Long> getSymbolFrequency()
    {
        return symbolFrequency;
    }


    /**
     * Obtain a {@link Map} representing the symbol price.
     *
     * @return the symbol price
     */
    @SuppressWarnings("unused")
    public Map<String, Double> getSymbolPrice()
    {
        return symbolPrice;
    }


    /**
     * Obtain a {@link Map} representing the symbol count.
     *
     * @return symbol count
     */
    @SuppressWarnings("unused")
    public Map<String, Integer> getSymbolCount()
    {
        return symbolCount;
    }


    /**
     * Obtain the original valuation across all stocks.
     *
     * @return the original valuation
     */
    @SuppressWarnings("unused")
    public double getOriginalValuation()
    {
        return originalValuation;
    }


    /**
     * Obtain the list of {@link MemberInfo} for this execution.
     *
     * @return the list of {@link MemberInfo}
     */
    @SuppressWarnings("unused")
    public List<MemberInfo> getMemberInfo()
    {
        return memberInfo;
    }


    /**
     * Obtain the duration of the aggregations.
     *
     * @return the duration
     */
    @SuppressWarnings("unused")
    public long getAggregationDuration()
    {
        return aggregationDuration;
    }


    @Override
    public void readExternal(PofReader reader) throws IOException
    {
        instant             = reader.readLong(INSTANT);
        aggregationDuration = reader.readLong(AGGREGATION_DURATION);
        positionCount       = reader.readInt(POSITION_COUNT);
        symbols             = reader.readCollection(SYMBOLS, new TreeSet<>());
        symbolFrequency     = reader.readMap(SYMBOL_FREQUENCY, new HashMap<>());
        symbolPrice         = reader.readMap(SYMBOL_PRICE, new HashMap<>());
        symbolCount         = reader.readMap(SYMBOL_COUNT, new HashMap<>());
        originalValuation   = reader.readDouble(ORIGINAL_VALUATION);
        memberInfo          = reader.readCollection(MEMBER_INFO, new Vector<>());
    }


    @Override
    public void writeExternal(PofWriter writer) throws IOException
    {
        writer.writeLong(INSTANT, instant);
        writer.writeLong(AGGREGATION_DURATION, aggregationDuration);
        writer.writeInt(POSITION_COUNT, positionCount);
        writer.writeCollection(SYMBOLS, symbols);
        writer.writeMap(SYMBOL_FREQUENCY, symbolFrequency);
        writer.writeMap(SYMBOL_PRICE, symbolPrice);
        writer.writeMap(SYMBOL_COUNT, symbolCount);
        writer.writeDouble(ORIGINAL_VALUATION, originalValuation);
        writer.writeCollection(MEMBER_INFO, memberInfo);
    }
}
