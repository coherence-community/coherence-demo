/*
 * File: TradeSummaryAggregator.java
 *
 * Copyright (c) 2015, 2024 Oracle and/or its affiliates.
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

import com.tangosol.io.pof.schema.annotation.PortableType;
import com.tangosol.util.InvocableMap;

/**
 * An aggregator to efficiently summarise trade information across all trades.
 */
@PortableType(id = 1006)
public class TradeSummaryAggregator
        implements InvocableMap.StreamingAggregator<String, Trade, TradeSummary, TradeSummary> {

    /**
     * The trade summary.
     */
    private transient TradeSummary tradeSummary;

    /**
     * Construct a {@link TradeSummaryAggregator}.
     */
    public TradeSummaryAggregator() {
        super();
        this.tradeSummary = new TradeSummary();
    }

    @Override
    public InvocableMap.StreamingAggregator<String, Trade, TradeSummary, TradeSummary> supply() {
        return new TradeSummaryAggregator();
    }

    @Override
    public boolean accumulate(InvocableMap.Entry<? extends String, ? extends Trade> entry) {
        tradeSummary.add(entry.extract(Trade::getQuantity), entry.extract(Trade::getPurchaseValue));
        return true;
    }

    @Override
    public boolean combine(TradeSummary tradeSummary) {
        this.tradeSummary.combine(tradeSummary);
        return true;
    }

    @Override
    public TradeSummary getPartialResult() {
        return tradeSummary;
    }

    @Override
    public TradeSummary finalizeResult() {
        return tradeSummary;
    }

    @Override
    public int characteristics() {
        return PARALLEL | PRESENT_ONLY;
    }
}