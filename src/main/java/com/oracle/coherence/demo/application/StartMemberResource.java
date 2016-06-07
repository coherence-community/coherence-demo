/*
 * File: StartMemberResource.java
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
import com.oracle.bedrock.runtime.LocalPlatform;
import com.oracle.bedrock.runtime.coherence.CoherenceCacheServer;
import com.oracle.bedrock.runtime.coherence.options.CacheConfig;
import com.oracle.bedrock.runtime.coherence.options.ClusterName;
import com.oracle.bedrock.runtime.coherence.options.ClusterPort;
import com.oracle.bedrock.runtime.coherence.options.Logging;
import com.oracle.bedrock.runtime.coherence.options.RoleName;
import com.oracle.bedrock.runtime.java.options.SystemProperty;
import com.oracle.bedrock.runtime.options.DisplayName;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.Cluster;
import com.tangosol.net.Member;
import com.tangosol.util.ResourceRegistry;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import static com.oracle.bedrock.deferred.DeferredHelper.eventually;
import static com.oracle.bedrock.deferred.DeferredHelper.invoking;
import static com.oracle.bedrock.predicate.Predicates.greaterThan;

/**
 * A JAX-RS resource providing the ability to start a new cluster {@link Member}s for the current
 * cluster, either primary or secondary.
 *
 * @author Brian Oliver
 */
@Path("/start-member")
public class StartMemberResource
{
    @GET
    public Response createMember()
    {
        Cluster cluster = CacheFactory.getCluster();

        if (cluster.getMemberSet().size() < 5)
        {
            // we'll use the local platform to create the new member
            LocalPlatform platform = LocalPlatform.get();

            try
            {
                // start the new cache server
                CoherenceCacheServer server = platform.launch(CoherenceCacheServer.class,
                                                              DisplayName.of("Coherence Demo Server"),
                                                              CacheConfig.of("cache-config.xml"),
                                                              SystemProperty.of("coherence.wka", "127.0.0.1"),
                                                              SystemProperty.of("coherence.ttl", "0"),
                                                              SystemProperty.of("with.http", false),
                                                              Logging.at(0),
                                                              RoleName.of("CoherenceDemoServer"),
                                                              ClusterPort.of(cluster.getDependencies().getGroupPort()),
                                                              ClusterName.of(cluster.getClusterName()),
                                                              SystemProperty.of(Launcher.PRIMARY_CLUSTER_PROPERTY,
                                                                                System.getProperty(Launcher
                                                                                    .PRIMARY_CLUSTER_PROPERTY)),
                                                              SystemProperty.of(Launcher.SECONDARY_CLUSTER_PROPERTY,
                                                                                System.getProperty(Launcher
                                                                                    .SECONDARY_CLUSTER_PROPERTY)));

                // wait for the new cache server to join the cluster
                DeferredHelper.ensure(eventually(invoking(server).getClusterSize()), greaterThan(1));

                // determine the member-id of the new server
                String memberId = Integer.toString(server.getLocalMemberId());

                // save the new server in the local resource registry (so later we can control it/shut it down)
                ResourceRegistry registry = CacheFactory.getConfigurableCacheFactory().getResourceRegistry();

                registry.registerResource(CoherenceCacheServer.class, memberId, server);

                // return the member-id
                return Response.ok(memberId).build();
            }
            catch (Exception e)
            {
                return Response.noContent().build();
            }
        }
        else
        {
            return Response.noContent().build();
        }
    }
}
