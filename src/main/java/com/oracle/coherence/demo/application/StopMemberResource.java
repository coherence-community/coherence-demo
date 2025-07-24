/*
 * File: StopMemberResource.java
 *
 * Copyright (c) 2015, 2025 Oracle and/or its affiliates.
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

import com.oracle.bedrock.runtime.coherence.CoherenceCacheServer;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.Member;

import com.tangosol.util.ResourceRegistry;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import jakarta.ws.rs.core.Response;

/**
 * A JAX-RS resource providing the ability to stop cluster {@link Member}s.
 *
 * @author Brian Oliver
 */
@Path("/stop-member")
public class StopMemberResource
        extends AbstractClusterMemberResource {

    /**
     * Default constructor for StopMemberResource.
     */
    public StopMemberResource() {
    }

    /**
     * Stops the specified cluster member.
     *
     * @param memberId the member ID
     * @return an empty response
     */
    @GET
    @Path("{memberId}")
    public Response stopMember(@PathParam("memberId") String memberId) {
        // use the resource registry to locate the CoherenceCacheServer to stop
        ResourceRegistry registry = CacheFactory.getConfigurableCacheFactory().getResourceRegistry();

        CoherenceCacheServer server = registry.getResource(CoherenceCacheServer.class, memberId);

        if (memberId != null) {
            releaseMemberToStableIdAssociation(memberId);

            if (server != null) {
                registry.unregisterResource(CoherenceCacheServer.class, memberId);

                server.close();
            }
        }

        return Response.noContent().build();
    }
}
