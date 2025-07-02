/*
 * File: TradeSummary.java
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
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * A class which represents summary information for trades.
 */
@XmlRootElement(name = "summary")
@XmlAccessorType(XmlAccessType.PROPERTY)
@PortableType(id = 1005)
public class TradeSummary {

    /**
     * The total number of shares acquired for the {@link Trade}.
     */
    private long quantity;

    /**
     * The number of trades.
     */
    private int count;

    /**
     * The purchase value.
     */
    private double purchaseValue;


    /**
     * Constructs a {@link TradeSummary}.
     */
    public TradeSummary() {
    }

    /**
     * Constructs a TradeSummary object.
     *
     * @param quantity  total number of shares acquired for the {@link Trade}
     * @param count   number of trades
     * @param purchaseValue total purchase valuation
     */
    public TradeSummary(long quantity, int count, double purchaseValue) {
        this.quantity = quantity;
        this.count = count;
        this.purchaseValue = purchaseValue;
    }

    /**
     * Add the quantity and purchase value to the summary.
     *
     * @param quantity  total number of shares acquired for the {@link Trade}
     * @param purchaseValue total purchase valuation
     */
    public void add(long quantity, double purchaseValue) {
        this.quantity += quantity;
        this.count++;
        this.purchaseValue += purchaseValue;
    }

    /**
     * Combine the given {@link TradeSummary} with this one.
     *
     * @param tradeSummary {@link TradeSummary} to combine
     */
    public void combine(TradeSummary tradeSummary) {
        this.quantity += tradeSummary.quantity;
        this.count += tradeSummary.count;
        this.purchaseValue += tradeSummary.purchaseValue;
    }

    /**
     * Obtain the total number of shares acquired for the {@link Trade}.
     *
     * @return the total number of shares acquired for the {@link Trade}
     */
    public long getQuantity() {
        return quantity;
    }

    /**
     * Set the total number of shares acquired for the {@link Trade}.
     *
     * @param quantity the total number of shares acquired for the {@link Trade}
     */
    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    /**
     * Obtain the total number of trades.
     *
     * @return the total number of trades
     */
    public int getCount() {
        return count;
    }

    /**
     * Set the total number of trades.
     * 
     * @param count the total number of trades
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * Obtain the total purchase value.
     *
     * @return the total purchase value
     */
    public double getPurchaseValue() {
        return purchaseValue;
    }

    /**
     * Set the total purchase value.
     * 
     * @param purchaseValue the total purchase value
     */
    public void setPurchaseValue(double purchaseValue) {
        this.purchaseValue = purchaseValue;
    }
}
