/*
 * File: MemberInfoResource.java
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

import com.oracle.coherence.demo.invocables.GetMemberInfo;

import com.oracle.coherence.demo.model.MemberInfo;
import com.oracle.coherence.demo.model.Trade;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.DistributedCacheService;
import com.tangosol.net.InvocationService;
import com.tangosol.net.Member;
import com.tangosol.net.NamedCache;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import jakarta.ws.rs.core.Response;

import java.util.Map;
import java.util.Set;

/**
 * A JAX-RS resource providing {@link com.oracle.coherence.demo.model.MemberInfo} for cluster
 * members.
 *
 * @author Brian Oliver
 */
@Path("/member-info")
public class MemberInfoResource {

    /**
     * Return {@link MemberInfo} on each {@link Member} of the cluster.
     *
     * @return {@link MemberInfo} on each {@link Member} of the cluster
     */
    @GET
    public Response getResource() {
        NamedCache<String, Trade> trades = Utilities.getTradesCache();
        InvocationService invocationService = (InvocationService)
                CacheFactory.getService("InvocationService");

        // determine the storage enabled members for the membership query
        Set<Member> storageEnabledMembers =
                ((DistributedCacheService) trades.getCacheService()).getOwnershipEnabledMembers();

        // determine the member information
        //noinspection unchecked
        Map<Member, MemberInfo> results =
                invocationService.query(new GetMemberInfo(trades.getCacheName()),
                        storageEnabledMembers);

        return Response.ok(results.values()).build();
    }
}
