/*
 * File: Utilities.java
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

import com.oracle.coherence.demo.model.Price;
import com.oracle.coherence.demo.model.Trade;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.TypeAssertion;
import com.tangosol.util.InvocableMap;
import com.tangosol.util.UUID;

import java.util.HashMap;
import java.util.Random;

/**
 * Utility functions for the Coherence Demo.
 *
 * @author Brian Oliver
 */
public class Utilities
{
    private final static int      NR_POSITIONS_TO_CREATE = 100000;
    private final static float    MIN_FACTOR             = 0.95f;
    private final static float    MAX_FACTOR             = 1.06f;
    private final static double   INITIAL_PRICE          = 20;
    private final static double   MIN_PRICE              = 5;
    private final static String[] SYMBOLS                = {"ORCL", "MSFT", "GOOG", "AAPL", "YHOO", "EMC"};

    /**
     * The {@link TypeAssertion} for the trades cache.
     */
    public static final TypeAssertion TRADE_CACHE_TYPE = TypeAssertion.withTypes(UUID.class, Trade.class);


    /**
     * The {@link TypeAssertion} for the prices cache.
     */
    public static final TypeAssertion PRICE_CACHE_TYPE = TypeAssertion.withTypes(String.class, Price.class);


    /**
     * The name of the trades cache.
     */
    public static final String TRADE_CACHE = "trades";


    /**
     * The name of the prices cache.
     */
    public static final String PRICE_CACHE = "prices";


    /**
     * Create the required positions.
     *
     * @param args arguments to main
     */
    @SuppressWarnings("unchecked")
    public static void main(String[] args)
    {
        createPositions(NR_POSITIONS_TO_CREATE);
    }


    /**
     * Obtain the trades cache.
     *
     * @return the trade {@link NamedCache}
     */
    public static NamedCache<UUID, Trade> getTradesCache()
    {
        return CacheFactory.getTypedCache(TRADE_CACHE, TRADE_CACHE_TYPE);
    }


    /**
     * Obtain the price cache
     *
     * @return the price {@link NamedCache}
     */
    public static NamedCache<String, Price> getPricesCache()
    {
        return CacheFactory.getTypedCache(PRICE_CACHE, PRICE_CACHE_TYPE);
    }


    /**
     * Add indexes to the caches to improve query performance.
     */
    public static void addIndexes()
    {
        NamedCache<UUID, Trade> tradesCache = getTradesCache();

        System.out.print("Adding Indexes...");
        tradesCache.addIndex(Trade::getSymbol, true, null);
        tradesCache.addIndex(Trade::getPurchaseValue, false, null);
        tradesCache.addIndex(Trade::getAmount, false, null);
        System.out.println(" Done");
    }


    /**
     * Remove indexes to the caches.
     */
    public static void removeIndexes()
    {
        NamedCache<UUID, Trade> tradesCache = getTradesCache();

        System.out.print("Removing Indexes...");
        tradesCache.removeIndex(Trade::getSymbol);
        tradesCache.removeIndex(Trade::getPurchaseValue);
        tradesCache.removeIndex(Trade::getAmount);
        System.out.println(" Done");
    }


    /**
     * populate initial prices for symbols. Make the current price for all
     * symbols to be $40 to make it fair and un-biased.
     */
    public static void populatePrices()
    {
        NamedCache<String, Price> pricesCaches = getPricesCache();

        for (int i = 0; i < SYMBOLS.length; i++)
        {
            Price price = new Price(SYMBOLS[i], INITIAL_PRICE);

            pricesCaches.put(price.getSymbol(), price);
        }
    }


    /**
     * Create NR_POSITIONS_TO_CREATE in the cache.
     */
    public static void createPositions()
    {
        createPositions(NR_POSITIONS_TO_CREATE);
    }


    /**
     * Create "count" positions in the cache at the current price.
     *
     * @param count the number of entries to add
     */
    public static void createPositions(int count)
    {
        System.out.printf("Creating %d Positions...\n", count);

        NamedCache<UUID, Trade>   tradesCache = getTradesCache();
        NamedCache<String, Price> priceCache  = getPricesCache();

        if (priceCache.size() != 5)
        {
            populatePrices();
        }

        Random               random = new Random();
        HashMap<UUID, Trade> trades = new HashMap<>();

        for (int i = 0; i < count; i++)
        {
            // create a random position
            String symbol = SYMBOLS[random.nextInt(SYMBOLS.length)];
            int    amount = random.nextInt(1000) + 1;
            double price  = priceCache.get(symbol).getPrice();

            Trade  trade  = new Trade(symbol, amount, price);

            trades.put(trade.getId(), trade);
        }

        tradesCache.putAll(trades);

        System.out.printf("Creation Complete! (Cache contains %d positions)\n", tradesCache.size());
    }


    /**
     * Update a single random stock symbol price on each call.
     */
    public static void updatePrices()
    {
        NamedCache<String, Price> priceCache = getPricesCache();
        Random                    random     = new Random();

        // choose random symbol to modify
        String symbol = SYMBOLS[random.nextInt(SYMBOLS.length)];

        // invoke using static method to ensure all arguments are captured
        priceCache.invoke(symbol, updateStockPrice(random.nextFloat()));
    }


    /**
     * An entry processor to update the price of a symbol.
     *
     * @param randomValue   a random float to generate the price
     *
     * @return a {@link InvocableMap.EntryProcessor} to carry out the processing
     */
    protected static InvocableMap.EntryProcessor<String, Price, Void> updateStockPrice(float randomValue)
    {
        return entry -> {
                   if (entry.isPresent())
                   {
                       Price  price    = entry.getValue();
                       float  factor   = (randomValue * (MAX_FACTOR - MIN_FACTOR) + MIN_FACTOR);
                       double newPrice = price.getPrice() * factor;

                       // when setting the price, if the value < MIN_PRICE, then make it MIN_PRICE
                       price.setPrice(newPrice <= MIN_PRICE ? MIN_PRICE : newPrice);
                       entry.setValue(price);
                   }

                   return null;
               };
    }
}
