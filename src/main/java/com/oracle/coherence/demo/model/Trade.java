/*
 * File: Trade.java
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

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

import com.tangosol.util.UUID;

import java.io.IOException;

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
public class Trade implements PortableObject
{
    @SuppressWarnings("unused")
    private static final long serialVersionUID = -2557078539268609864L;

    /**
     * POF index for id attribute.
     */
    private static final int ID = 0;

    /**
     * POF index for symbol attribute.
     */
    private static final int SYMBOL = 1;

    /**
     * POF index for amount attribute.
     */
    private static final int AMOUNT = 2;

    /**
     * POF index for price attribute.
     */
    private static final int PRICE = 3;

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
    private int amount;

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
     * @param symbol  symbol (ticker code) of the {@link Trade}
     * @param amount  number of shares (quantity) for the {@link Trade}
     * @param price   price of the shares
     */
    public Trade(String symbol,
                 int    amount,
                 double price)
    {
        this.id     = new UUID().toString();
        this.symbol = symbol;
        this.amount = amount;
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
     * @return the amount
     */
    public int getAmount()
    {
        return amount;
    }


    /**
     * Obtain the original purchase value of the {@link Trade}. (value = price * quantity)
     *
     * @return the value
     */
    public double getPurchaseValue()
    {
        return getAmount() * getPrice();
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


    @Override
    public void readExternal(PofReader reader) throws IOException
    {
        id     = reader.readString(ID);
        symbol = reader.readString(SYMBOL);
        amount = reader.readInt(AMOUNT);
        price  = reader.readDouble(PRICE);
    }


    @Override
    public void writeExternal(PofWriter writer) throws IOException
    {
        writer.writeString(ID, id);
        writer.writeString(SYMBOL, symbol);
        writer.writeInt(AMOUNT, amount);
        writer.writeDouble(PRICE, price);
    }
}
