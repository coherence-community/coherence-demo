/*
 * File: Trade.java
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


import com.tangosol.io.pof.PortableObject;

import com.tangosol.io.pof.schema.annotation.PortableType;
import com.tangosol.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * An immutable class to represent a single position in a financial market for an equity (stock).
 *
 * @author Brian Oliver
 */
@Entity
@XmlRootElement(name = "trade")
@XmlAccessorType(XmlAccessType.PROPERTY)
@PortableType(id = 1004)
public class Trade
{
    @SuppressWarnings("unused")
    private static final long serialVersionUID = -2557078539268609864L;

    /**
     * The unique identifier for this trade.
     */
    @Id
    private String id;

    /**
     * The symbol (ticker code) of the equity for the {@link Trade}.
     */
    private String symbol;

    /**
     * The number of shares for the {@link Trade}.
     */
    private int quantity;

    /**
     * The price at which the shares in the {@link Trade} were acquired.
     */
    private double price;


    /**
     * Default Constructor (required and used only by {@link PortableObject}).
     */
    public Trade()
    {
        // required for Serializable and PortableObject
    }


    /**
     * The standard constructor for a {@link Trade}.
     *
     * @param symbol    symbol (ticker code) of the {@link Trade}
     * @param quantity  number of shares (quantity) for the {@link Trade}
     * @param price     price of the shares
     */
    public Trade(String symbol,
                 int    quantity,
                 double price)
    {
        this.id     = new UUID().toString();
        this.symbol = symbol;
        this.quantity = quantity;
        this.price  = price;
    }


    /**
     * Obtain the unique identifier for the {@link Trade}.
     *
     * @return the identifier
     */
    public String getId()
    {
        return id;
    }


    /**
     * Obtain the symbol (ticker code) of the equity (stock) for the {@link Trade}.
     *
     * @return the symbol
     */
    public String getSymbol()
    {
        return symbol;
    }

    /**
     * Obtain the value at which the shares were acquired for the {@link Trade}.
     *
     * @return the price
     */
    public double getPrice()
    {
        return price;
    }


    /**
     * Obtain the number of shares acquired for the {@link Trade}.
     *
     * @return the quantity
     */
    public int getQuantity()
    {
        return quantity;
    }


    /**
     * Obtain the original purchase value of the {@link Trade}. (value = price * quantity)
     *
     * @return the value
     */
    public double getPurchaseValue()
    {
        return getQuantity() * getPrice();
    }


    /**
     * Set the price of the Position.
     *
     * @param price the new price.
     */
    public void setPrice(double price)
    {
        this.price = price;
    }

    /**
     * Split the stock.
     *
     * @param factor factor to use for split
     */
    public void split(int factor)
    {
        quantity *= factor;
        price /= factor;
    }


    /**
     * Set the number of shares acquired for the {@link Trade}.
     *
     * @param quantity the new quantity.
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
