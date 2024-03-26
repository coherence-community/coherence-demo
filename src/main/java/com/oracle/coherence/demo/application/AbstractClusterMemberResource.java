/*
 * File: AbstractClusterMemberResource.java
 *
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates.
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

import java.util.HashMap;
import java.util.Map;

/**
 * Common functionality for resources that start/stop multiple Coherence members.
 */
public class AbstractClusterMemberResource {

    /**
     * Stable internal ID set for odd/even tracking.
     */
    private static byte s_UsedIds = 0x0;

    /**
     * Mapping of Member IDs to local stable ID.
     */
    private static final Map<String, Integer> MEMBER_TO_STABLE_ID = new HashMap<>();

    /**
     * Get a local stable ID for members started by REST.
     *
     * @return the next available ID
     */
    protected synchronized static int getStableId() {
        for (int i = 0, len = Byte.SIZE; i < len; i++) {
            if ((s_UsedIds >> i & 1) == 0) {
                s_UsedIds |= 1 << i;
                return i;
            }
        }
        return -1;
    }

    /**
     * Associate the member ID to the stable ID.
     *
     * @param sMemberId  the member ID
     * @param nStableId  the stable ID
     */
    protected synchronized static void associateMemberToStableId(String sMemberId, Integer nStableId) {
        MEMBER_TO_STABLE_ID.put(sMemberId, nStableId);
    }

    /**
     * Remove the association between member ID and the stable ID.
     *
     * @param sMemberId  the member ID
     */
    protected synchronized static void releaseMemberToStableIdAssociation(String sMemberId) {
        int nId = MEMBER_TO_STABLE_ID.remove(sMemberId);
        releaseId(nId);
    }

    /**
     * Augment the provided role name by appending {@code Even} or {@code Odd} depending
     * on the evenness/oddness of the provided stable ID.
     *
     * @param nStableId  the stable ID
     *
     * @return the augmented role name
     */
    protected String createRoleName(int nStableId) {
        return "CoherenceDemoServer" + ((nStableId & 1) == 1 ? "Odd" : "Even");
    }

    /**
     * Release a stable ID for re-use.
     */
    private static void releaseId(int nId) {
        s_UsedIds &= ~(1 << nId);
    }
}
