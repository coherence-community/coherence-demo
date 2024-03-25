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
@PortableType
public class TradeSummary {

    private long frequency;
    private int  count;
    private double purchaseValue;

    public TradeSummary() {
    }
    
    public TradeSummary(long frequency, int count, double originalValuation) {
        this.frequency = frequency;
        this.count = count;
        this.purchaseValue = originalValuation;
    }

    public void add(long amount, double purchaseValue) {
        this.frequency += amount;
        this.count++;
        this.purchaseValue += purchaseValue;
    }

    public void combine(TradeSummary tradeSummary) {
        this.frequency += tradeSummary.frequency;
        this.count += tradeSummary.count;
        this.purchaseValue += tradeSummary.purchaseValue;
    }

    public long getFrequency() {
        return frequency;
    }

    public void setFrequency(long frequency) {
        this.frequency = frequency;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public double getPurchaseValue() {
        return purchaseValue;
    }

    public void setPurchaseValue(double purchaseValue) {
        this.purchaseValue = purchaseValue;
    }
}
