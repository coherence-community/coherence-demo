/*
 * File: Price.java
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

import com.tangosol.io.pof.schema.annotation.PortableType;
import jakarta.persistence.Entity;

import jakarta.persistence.Id;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * An immutable class to represent a price of a given stock in a financial market for an equity (stock).
 *
 * @author Brian Oliver
 * @author Tim Middleton
 */
@Entity
@XmlRootElement(name = "price")
@XmlAccessorType(XmlAccessType.PROPERTY)
@PortableType
public class Price
{
    @SuppressWarnings("unused")
    private static final long serialVersionUID = -2557678549268609664L;

    /**
     * POF index for symbol attribute.
     */
    private static final int SYMBOL = 0;

    /**
     * POF index for price attribute.
     */
    private static final int PRICE = 1;

    /**
     * The symbol (ticker code) of the equity for the {@link Price}.
     */
    @Id
    private String symbol;

    /**
     * The price of the symbol.
     */
    private double price;


    /**
     * Default Constructor (required and used only by {@link PortableObject}).
     */
    public Price()
    {
        // required for Serializable and PortableObject
    }


    /**
     * The standard constructor for a {@link Price}.
     *
     * @param symbol The symbol (ticker code) of the {@link Trade}
     * @param price The current price of the symbol
     */
    public Price(String symbol,
                 double price)
    {
        this.symbol = symbol;
        this.price  = price;
    }


    /**
     * Obtain the symbol (ticker code) of the equity (stock) for the {@link Trade}.
     *
     * @return  the symbol
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
     * Set the price of the Position.
     *
     * @param price the new price.
     */
    public void setPrice(double price)
    {
        this.price = price;
    }
}
