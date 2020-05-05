/*
 * File: StartSecondaryResource.java
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

import com.oracle.bedrock.deferred.DeferredHelper;

import com.oracle.bedrock.runtime.ApplicationConsole;
import com.oracle.bedrock.runtime.LocalPlatform;

import com.oracle.bedrock.runtime.coherence.CoherenceCacheServer;

import com.oracle.bedrock.runtime.coherence.options.CacheConfig;
import com.oracle.bedrock.runtime.coherence.options.ClusterName;
import com.oracle.bedrock.runtime.coherence.options.ClusterPort;
import com.oracle.bedrock.runtime.coherence.options.Logging;

import com.oracle.bedrock.runtime.console.NullApplicationConsole;
import com.oracle.bedrock.runtime.console.SystemApplicationConsole;

import com.oracle.bedrock.runtime.java.options.SystemProperty;

import com.oracle.bedrock.runtime.options.Console;
import com.oracle.bedrock.runtime.options.DisplayName;

import com.tangosol.net.CacheFactory;

import com.tangosol.util.ResourceRegistry;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static com.oracle.bedrock.deferred.DeferredHelper.eventually;
import static com.oracle.bedrock.deferred.DeferredHelper.invoking;
import static com.oracle.bedrock.predicate.Predicates.greaterThan;

/**
 * A JAX-RS resource providing the ability to start a new cluster as a secondary cluster
 * for Federation. The Federation configuration is to make the members active-active.
 *
 * @author Tim Middleton
 */

@Path("/start-secondary")
public class StartSecondaryResource
{
    /**
     * JAX-RS injection of UriInfo.
     */
    @Context
    private UriInfo uriInfo;


    /**
     * Starts the secondary cluster for demonstrating cluster replication.
     *
     * @return {@link Response#ok}
     */
    @GET
    public Response startCluster()
    {
        CacheFactory.getCluster();

        String secondaryName = System.getProperty(Launcher.SECONDARY_CLUSTER_PROPERTY);

        // we'll use the local platform to create the new member
        LocalPlatform platform = LocalPlatform.get();

        // we don't care about console output from the new member unless we set
        // -Dsecondary.verbose=something
        ApplicationConsole console = System.getProperty("secondary.verbose") == null
                                     ? new NullApplicationConsole() : new SystemApplicationConsole();

        try
        {
            // start the new cache server
            CoherenceCacheServer server =
                    platform.launch(CoherenceCacheServer.class,
                                    DisplayName.of("Coherence Demo Server"),
                                    Console.of(console),
                                    CacheConfig.of("cache-config.xml"),
                                    SystemProperty.of("coherence.wka", "127.0.0.1"),
                                    SystemProperty.of("coherence.ttl", "0"),
                                    SystemProperty.of("with.http", true),
                                    SystemProperty.of("http.port", uriInfo.getBaseUri().getPort() + 1),
                                    SystemProperty.of("http.hostname", System.getProperty("http.hostname",
                                                                                          "127.0.0.1")),
                                    SystemProperty.of("coherence.distribution.2server", "false"),
                                    SystemProperty.of("coherence.tracing.ratio", 1.0),
                                    SystemProperty.of(Launcher.JAEGER_SERVICE_NAME_PROPERTY,
                                                      "Coherence Demo (" + secondaryName + ')'),
                                    SystemProperty.of(Launcher.JAEGER_ENDPOINT_PROPERTY,
                                                      System.getProperty(Launcher.JAEGER_ENDPOINT_PROPERTY,
                                                                         Launcher.DEFAULT_JAEGER_ENDPOINT)),
                                    Logging.at(0),
                                    ClusterPort.of(Launcher.SECONDARY_PORT),
                                    ClusterName.of(secondaryName),
                                    SystemProperty.of("with.data", "false"),
                                    SystemProperty.of(Launcher.PRIMARY_CLUSTER_PROPERTY,
                                                      System.getProperty(Launcher.PRIMARY_CLUSTER_PROPERTY)),
                                    SystemProperty.of(Launcher.SECONDARY_CLUSTER_PROPERTY,
                                                      System.getProperty(Launcher.SECONDARY_CLUSTER_PROPERTY)));

            // wait for the new secondary cluster to start
            DeferredHelper.ensure(eventually(invoking(server).getClusterSize()), greaterThan(0));

            // save the new server in the local resource registry (so later we can control it/shut it down)
            ResourceRegistry registry = CacheFactory.getConfigurableCacheFactory().getResourceRegistry();

            registry.registerResource(CoherenceCacheServer.class, "secondary", server);

            // return the member-id
            return Response.ok("secondary").build();
        }
        catch (Exception e)
        {
            return Response.noContent().build();
        }
    }
}
