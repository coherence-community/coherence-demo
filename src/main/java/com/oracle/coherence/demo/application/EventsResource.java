/*
 * File: EventsResource.java
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

package com.oracle.coherence.demo.application;

import com.oracle.coherence.demo.model.Price;

import com.tangosol.net.NamedCache;

import com.tangosol.util.listener.SimpleMapListener;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseBroadcaster;
import jakarta.ws.rs.sse.SseEventSink;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.Date;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

/**
 * A JAX-RS resource providing SSE events.
 *
 * @author Tim Middleton
 */
@Path("/events")
@ApplicationScoped
public class EventsResource {

    private NamedCache<String, Price> prices;

    @Context
    private Sse sse;

    private SseBroadcaster broadcaster;

    /**
     * Default constructor for EventsResource.
     */
    public EventsResource() {
    }

    @PostConstruct
    void createBroadcaster() {
        this.broadcaster = sse.newBroadcaster();
        this.prices = Utilities.getPricesCache();

        prices.addMapListener(new SimpleMapListener<String, Price>()
                .addUpdateHandler(e->broadcaster.broadcast(createEvent("priceUpdate",
                        e.getNewValue().getSymbol(), e.getOldValue().getPrice(), e.getNewValue().getPrice()))));
    }

    private OutboundSseEvent createEvent(String name, String symbol, double oldPrice, double newPrice) {
        return sse.newEventBuilder()
                  .name(name)
                  .data(Price.class, new PriceUpdate(symbol, oldPrice, newPrice))
                  .mediaType(APPLICATION_JSON_TYPE)
                  .build();
    }

    /**
     * Registers an event listener for the specified {@link SseEventSink}.
     *
     * @param eventSink  provided {@link SseEventSink}
     */
    @GET
    @Path("subscribe")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void registerEventListener(@Context SseEventSink eventSink) {
        broadcaster.register(eventSink);
        eventSink.send(sse.newEvent("begin", new Date().toString()));
    }

    /**
     * Represents a price update to be sent back via web sockets.
     */
    @XmlRootElement(name = "price")
    @XmlAccessorType(XmlAccessType.PROPERTY)
    public static class PriceUpdate {
        private final String symbol;
        private final double oldPrice;
        private final double newPrice;

        /**
         * Constructs a new PriceUpdate instance with the specified symbol and price information.
         *
         * @param symbol    the stock symbol associated with the price update
         * @param oldPrice  the previous price of the stock
         * @param newPrice  the current price of the stock
         */
        public PriceUpdate(String symbol, double oldPrice, double newPrice) {
            this.symbol = symbol;
            this.oldPrice = oldPrice;
            this.newPrice = newPrice;
        }

        /**
         * Returns the stock symbol associated with this price update.
         *
         * @return the stock symbol
         */
        public String getSymbol() {
            return symbol;
        }

        /**
         * Returns the previous price of the stock.
         *
         * @return the old price
         */
        public double getOldPrice() {
            return oldPrice;
        }

        /**
         * Returns the current price of the stock.
         *
         * @return the new price
         */
        public double getNewPrice() {
            return newPrice;
        }
    }
}
