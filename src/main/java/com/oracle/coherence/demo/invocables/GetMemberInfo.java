/*
 * File: GetMemberInfo.java
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

package com.oracle.coherence.demo.invocables;

import com.oracle.coherence.demo.application.Utilities;

import com.oracle.coherence.demo.model.MemberInfo;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

import com.tangosol.net.AbstractInvocable;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.CacheService;
import com.tangosol.net.ExtensibleConfigurableCacheFactory;
import com.tangosol.net.Member;
import com.tangosol.net.NamedCache;

import java.io.IOException;

import static com.oracle.coherence.demo.application.Utilities.TRADE_CACHE;

/**
 * An {@link com.tangosol.net.Invocable} to acquire cluster member information.
 *
 * @author Brian Oliver
 */
public class GetMemberInfo extends AbstractInvocable implements PortableObject
{
    /**
     * POF index for cacheName attribute.
     */
    private static final int CACHE_NAME = 0;

    /**
     * The cache name to get information about.
     */
    private String cacheName;


    /**
     * Constructs a {@link GetMemberInfo} (for serialization).
     */
    @SuppressWarnings("unused")
    public GetMemberInfo()
    {
    }


    /**
     * Constructs a {@link GetMemberInfo} for a specified cache.
     *
     * @param cacheName  name of the cache to get information for.
     */
    public GetMemberInfo(String cacheName)
    {
        super();
        this.cacheName = cacheName;
    }


    @SuppressWarnings("rawtypes")
    @Override
    public void run()
    {
        // calculate number of entries for the specified named cache
        int entryCount = 0;
        NamedCache namedCache = TRADE_CACHE.equals(cacheName)
                                    ? Utilities.getTradesCache()
                                    : Utilities.getPricesCache();

        if (namedCache != null)
        {
            CacheService cacheService = namedCache.getCacheService();

            if (cacheService.getBackingMapManager() instanceof ExtensibleConfigurableCacheFactory.Manager)
            {
                ExtensibleConfigurableCacheFactory.Manager backingMapManager =
                    (ExtensibleConfigurableCacheFactory.Manager) cacheService.getBackingMapManager();

                entryCount = backingMapManager.getBackingMap(cacheName).size();
            }
        }

        // we use the runtime to determine the current jvm memory state
        Runtime runtime = Runtime.getRuntime();

        // we need the member to send back membership information
        Member member = CacheFactory.getCluster().getLocalMember();

        // construct the MemberInfo for the result
        MemberInfo memberInfo = new MemberInfo(member, runtime, entryCount);

        setResult(memberInfo);
    }


    @Override
    public void readExternal(PofReader reader) throws IOException
    {
        cacheName = reader.readString(CACHE_NAME);
    }


    @Override
    public void writeExternal(PofWriter writer) throws IOException
    {
        writer.writeString(CACHE_NAME, cacheName);
    }
}
