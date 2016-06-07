/*
 * File: StartSecondaryResource.java
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * The contents of this file are subject to the terms and conditions of
 * the Common Development and Distribution License 1.0 (the "License").
 *
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the License by consulting the LICENSE.txt file
 * distributed with this file, or by consulting https://oss.oracle.com/licenses/CDDL
 *
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file LICENSE.txt.
 *
 * MODIFICATIONS:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
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
    @Context
    UriInfo uriInfo;


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
            CoherenceCacheServer server = platform.launch(CoherenceCacheServer.class,
                                                          DisplayName.of("Coherence Demo Server"),
                                                          Console.of(console),
                                                          CacheConfig.of("cache-config.xml"),
                                                          SystemProperty.of("coherence.wka", "127.0.0.1"),
                                                          SystemProperty.of("coherence.ttl", "0"),
                                                          SystemProperty.of("with.http", true),
                                                          SystemProperty.of("http.port",
                                                                            Integer.valueOf(uriInfo.getBaseUri()
                                                                            .getPort() + 1)),
                                                          SystemProperty.of("http.hostname",
                                                                            System.getProperty("http.hostname",
                                                                                               "127.0.0.1")),
                                                          SystemProperty.of("coherence.distribution.2server", "false"),
                                                          Logging.at(0),
                                                          ClusterPort.of(Launcher.SECONDARY_PORT),
                                                          ClusterName.of(secondaryName),
                                                          SystemProperty.of("with.data", "false"),
                                                          SystemProperty.of(Launcher.PRIMARY_CLUSTER_PROPERTY,
                                                                            System.getProperty(Launcher
                                                                                .PRIMARY_CLUSTER_PROPERTY)),
                                                          SystemProperty.of(Launcher.SECONDARY_CLUSTER_PROPERTY,
                                                                            System.getProperty(Launcher
                                                                                .SECONDARY_CLUSTER_PROPERTY)));

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
