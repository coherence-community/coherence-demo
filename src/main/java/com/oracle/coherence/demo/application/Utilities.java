/*
 * File: Utilities.java
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

package com.oracle.coherence.demo.application;

import com.oracle.coherence.demo.model.Price;
import com.oracle.coherence.demo.model.Trade;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

import com.tangosol.net.cache.TypeAssertion;

import com.tangosol.util.InvocableMap;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;

import io.opentracing.tag.Tags;

import io.opentracing.util.GlobalTracer;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Utility functions for the Coherence Demo.
 *
 * @author Brian Oliver
 */
public final class Utilities
{
    /**
    * The number of positions to create.
    */
    private static final int NR_POSITIONS_TO_CREATE = 100000;

    /**
    * The minimum factor for price calculations.
    */
    private static final float MIN_FACTOR = 0.95f;

    /**
    * The maximum factor for price calculations.
    */
    private static final float MAX_FACTOR = 1.06f;

    /**
    * The initial price.
    */
    private static final double INITIAL_PRICE = 20;

    /**
    * The minimum price.
    */
    private static final double MIN_PRICE = 5;

    /**
    * Stock symbols.
    */
    private static final String[] SYMBOLS = {"ORCL", "MSFT", "GOOG", "AAPL", "NFLX", "DELL"};

    /**
     * The path to the VisualVM executable, for JDK9+.
     */
    public static final String VISUALVM = System.getProperty("visualvm.executable", "");

    /**
     * The {@link TypeAssertion} for the trades cache.
     */
    public static final TypeAssertion<String, Trade> TRADE_CACHE_TYPE =
            TypeAssertion.withTypes(String.class, Trade.class);


    /**
     * The {@link TypeAssertion} for the prices cache.
     */
    public static final TypeAssertion<String, Price> PRICE_CACHE_TYPE =
            TypeAssertion.withTypes(String.class, Price.class);


    /**
     * The name of the trades cache.
     */
    public static final String TRADE_CACHE = "Trade";


    /**
     * The name of the prices cache.
     */
    public static final String PRICE_CACHE = "Price";


    // ----- constructors ---------------------------------------------------


    /**
     * Instances not allowed.
     */
    private Utilities()
    {
        throw new IllegalStateException("illegal instantiation");
    }


    // ----- utility methods ------------------------------------------------


    /**
     * Create the required positions.
     *
     * @param args  arguments to main
     */
    public static void main(String[] args)
    {
        createPositions(NR_POSITIONS_TO_CREATE);
    }


    /**
     * Obtain the trades cache.
     *
     * @return the trade {@link NamedCache}
     */
    public static NamedCache<String, Trade> getTradesCache()
    {
        return CacheFactory.getTypedCache(TRADE_CACHE, TRADE_CACHE_TYPE);
    }


    /**
     * Obtain the price cache.
     *
     * @return the price {@link NamedCache}
     */
    public static NamedCache<String, Price> getPricesCache()
    {
        return CacheFactory.getTypedCache(PRICE_CACHE, PRICE_CACHE_TYPE);
    }


    /**
     * Obtain an indicator showing if we are running under the Coherence Operator in
     * Kubernetes.
     *
     * @return an indicator showing if we are running under the Coherence Operator in
     *      Kubernetes
     */
    public static boolean isRunningInKubernetes()
    {
        return System.getenv("KUBERNETES_SERVICE_HOST") != null &&
               System.getenv("KUBERNETES_SERVICE_PORT") != null;
    }

    /**
     * Obtain an indicator showing if we have enabled metrics.
     *
     * @return an indicator showing if we have enabled metrics
     */
    public static boolean isMetricsEnabled()
    {
        Enumeration serviceNames = CacheFactory.ensureCluster().getServiceNames();
        while (serviceNames.hasMoreElements()) {
            if (serviceNames.nextElement().equals("MetricsHttpProxy")) {
                return true;
            }
        }
        return false;
    }



    /**
     * Obtain the Coherence cluster version.
     *
     * @return the Coherence cluster version
     */
    public static String getCoherenceVersion()
    {
        return CacheFactory.VERSION.replaceFirst(" .*$", "")
                                   .replaceFirst("[.-]SNAPSHOT.*$", "")
                                   .replaceAll("-", ".");
    }


    /**
     * Obtain an indicator showing if federation is configured in K8s.
     *
     * @return an indicator showing if federation is configured in K8s.
     */
    public static boolean isFederationConfiguredInK8s()
    {
        return isRunningInKubernetes() &&
               System.getProperty("primary.cluster") != null &&
               System.getProperty("secondary.cluster") != null &&
               System.getProperty("primary.cluster.host") != null &&
               System.getProperty("secondary.cluster.host") != null;
    }


    /**
     * Obtain the Coherence cluster version as an integer.
     *
     * @return the Coherence cluster version as an integer
     */
    public static int getCoherenceVersionAsInt()
    {
        return Integer.parseInt(getCoherenceVersion().replaceAll("\\.", ""));
    }


    /**
     * Add indexes to the caches to improve query performance.
     */
    public static void addIndexes()
    {
        NamedCache<String, Trade> tradesCache = getTradesCache();
        Tracer                    tracer      = GlobalTracer.get();
        Span                      span        = tracer.buildSpan("Utilities.AddIndexes")
                .withTag(Tags.COMPONENT, "demo")
                .withTag(Tags.SPAN_KIND, Tags.SPAN_KIND_SERVER).start();

        System.out.print("Adding Indexes...");
        try (Scope ignored = tracer.activateSpan(span))
        {
            tradesCache.addIndex(Trade::getSymbol, true, null);
            spanLog(span, "Created trade symbol index");
            tradesCache.addIndex(Trade::getPurchaseValue, false, null);
            spanLog(span, "Created trade purchase value index");
            tradesCache.addIndex(Trade::getAmount, false, null);
            spanLog(span, "Created trade amount index");
        }
        finally
        {
            span.finish();
        }
        System.out.println(" Done");
    }


    /**
     * Remove indexes to the caches.
     */
    public static void removeIndexes()
    {
        NamedCache<String, Trade> tradesCache = getTradesCache();
        Tracer                    tracer      = GlobalTracer.get();
        Span                      span        = tracer.buildSpan("Utilities.RemoveIndexes")
                .withTag(Tags.COMPONENT, "demo")
                .withTag(Tags.SPAN_KIND, Tags.SPAN_KIND_SERVER).start();

        System.out.print("Removing Indexes...");
        try (Scope ignored = tracer.activateSpan(span))
        {
            tradesCache.removeIndex(Trade::getSymbol);
            spanLog(span, "Removed trade symbol index");
            tradesCache.removeIndex(Trade::getPurchaseValue);
            spanLog(span, "Removed trade purchase value index");
            tradesCache.removeIndex(Trade::getAmount);
            spanLog(span, "Removed trade amount index");
        }
        finally
        {
            span.finish();
        }

        System.out.println(" Done");
    }


    /**
     * Populate initial prices for symbols. Make the current price for all
     * symbols to be $40 to make it fair and un-biased.
     */
    public static void populatePrices()
    {
        NamedCache<String, Price> pricesCaches = getPricesCache();
        Tracer                    tracer       = GlobalTracer.get();
        Span                      span         = tracer.buildSpan("Utilities.PopulatePrices")
                .withTag(Tags.COMPONENT, "demo")
                .withTag(Tags.SPAN_KIND, Tags.SPAN_KIND_SERVER)
                .withTag("symbol.count", SYMBOLS.length).start();

        try (Scope ignored = tracer.activateSpan(span))
        {
            for (String symbol : SYMBOLS)
            {
                Price price = new Price(symbol, INITIAL_PRICE);
                pricesCaches.put(price.getSymbol(), price);
            }
        }
        finally
        {
            span.finish();
        }
    }


    /**
     * Create {@value NR_POSITIONS_TO_CREATE} in the cache.
     */
    public static void createPositions()
    {
        createPositions(NR_POSITIONS_TO_CREATE);
    }


    /**
     * Create "count" positions in the cache at the current price.
     *
     * @param count  the number of entries to add
     */
    public static void createPositions(int count)
    {
        System.out.printf("Creating %d Positions...\n", count);

        NamedCache<String, Trade> tradesCache = getTradesCache();
        NamedCache<String, Price> priceCache  = getPricesCache();
        Tracer                    tracer      = GlobalTracer.get();
        Span                      span        = tracer.buildSpan("Utilities.CreatePositions")
                .withTag(Tags.COMPONENT, "demo")
                .withTag(Tags.SPAN_KIND, Tags.SPAN_KIND_SERVER)
                .withTag("symbol.count", SYMBOLS.length).start();

        try (Scope ignored = tracer.activateSpan(span))
        {
            Map<String,     Price> localPrices = new HashMap<>(priceCache.getAll(priceCache.keySet()));
            HashMap<String, Trade> trades      = new HashMap<>();
            Random                 random      = ThreadLocalRandom.current();

            for (int i = 0; i < count; i++)
            {
                // create a random position
                String symbol = SYMBOLS[random.nextInt(SYMBOLS.length)];
                int    amount = random.nextInt(1000) + 1;
                double price  = localPrices.get(symbol).getPrice();

                Trade trade = new Trade(symbol, amount, price);

                trades.put(trade.getId(), trade);

                // batch the putAll's at 10000
                if (i % 10000 == 0)
                {
                    spanLog(span, "Flushed 10000 trades to cache");
                    System.out.println("Flushing 10000 trades from HashMap to Coherence cache...");
                    tradesCache.putAll(trades);
                    trades.clear();
                }
            }

            // insert any remaining trades not previously flushed
            if (!trades.isEmpty())
            {
                tradesCache.putAll(trades);
            }
        }
        finally
        {
            span.finish();
        }

        System.out.printf("Creation Complete! (Cache contains %d positions)\n", tradesCache.size());
    }


    /**
     * Update a single random stock symbol price on each call.
     */
    public static void updatePrices()
    {
        NamedCache<String, Price> priceCache = getPricesCache();
        Random                    random     = ThreadLocalRandom.current();

        // choose random symbol to modify
        String symbol = SYMBOLS[random.nextInt(SYMBOLS.length)];
        Tracer tracer = GlobalTracer.get();
        Span   span   = tracer.buildSpan("Utilities.UpdatePrices")
                .withTag(Tags.COMPONENT, "demo")
                .withTag(Tags.SPAN_KIND, Tags.SPAN_KIND_SERVER)
                .withTag("update.symbol", symbol).start();

        try (Scope ignored = tracer.activateSpan(span))
        {
            // invoke using static method to ensure all arguments are captured
            priceCache.invoke(symbol, updateStockPrice(random.nextFloat()));
        }
        finally
        {
            span.finish();
        }
    }


    /**
     * Invokes {@link Span#log(String)} if {@code span} is not {@code null}.
     *
     * @param span     the target {@link Span}
     * @param message  the message to log
     */
    public static void spanLog(Span span, String message)
    {
        if (span != null)
        {
            span.log(message);
        }
    }


    /**
     * An entry processor to update the price of a symbol.
     *
     * @param randomValue  a random float to generate the price
     *
     * @return a {@link InvocableMap.EntryProcessor} to carry out the processing
     */
    protected static InvocableMap.EntryProcessor<String, Price, Void> updateStockPrice(float randomValue)
    {
        return entry ->
        {
            if (entry.isPresent())
            {
                Price  price    = entry.getValue();
                float  factor   = (randomValue * (MAX_FACTOR - MIN_FACTOR) + MIN_FACTOR);
                double newPrice = price.getPrice() * factor;

                // when setting the price, if the value < MIN_PRICE, then make it MIN_PRICE
                price.setPrice(Math.max(newPrice, MIN_PRICE));
                entry.setValue(price);
            }

            return null;
        };
    }
}
