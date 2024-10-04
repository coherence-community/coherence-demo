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

import java.util.Date;

import com.oracle.coherence.demo.model.Price;

import com.tangosol.net.NamedCache;

import com.tangosol.util.listener.SimpleMapListener;

import javax.annotation.PostConstruct;

import javax.enterprise.context.ApplicationScoped;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSink;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

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

    @PostConstruct
    void createBroadcaster() {
        this.broadcaster = sse.newBroadcaster();
        this.prices = Utilities.getPricesCache();

        prices.addMapListener(new SimpleMapListener<String, Price>()
                .addUpdateHandler(e->broadcaster.broadcast(createEvent("priceUpdate", e.getNewValue()))));
    }

    private OutboundSseEvent createEvent(String name, Price price) {
        return sse.newEventBuilder()
                  .name(name)
                  .data(Price.class, price)
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
}
