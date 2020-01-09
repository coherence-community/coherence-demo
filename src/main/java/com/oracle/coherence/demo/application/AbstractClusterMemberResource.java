package com.oracle.coherence.demo.application;

import java.util.HashMap;
import java.util.Map;

/**
 * Common functionality for resources that start/stop multiple Coherence members.
 */
public class AbstractClusterMemberResource
{
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
    protected synchronized static int getStableId()
    {
        for (int i = 0, len = Byte.SIZE; i < len; i++)
            {
            if ((s_UsedIds >> i & 1) == 0)
                {
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
    protected synchronized static void associateMemberToStableId(String sMemberId, Integer nStableId)
    {
        MEMBER_TO_STABLE_ID.put(sMemberId, nStableId);
    }

    /**
     * Remove the association between member ID and the stable ID.
     *
     * @param sMemberId  the member ID
     */
    protected synchronized static void releaseMemberToStableIdAssociation(String sMemberId)
    {
        int nId = MEMBER_TO_STABLE_ID.remove(sMemberId);
        releaseId(nId);
    }


    /**
     * Augment the provided role name by appending {@code Even} or {@code Odd} depending
     * on the evenness/oddness of the provided stable ID.
     *
     * @param nStableId  the stable ID
     * @param sRoleName  the role name to augment
     *
     * @return the augmented role name
     */
    protected String augmentRoleName(int nStableId, String sRoleName)
    {
        return sRoleName + ((nStableId & 1) == 1 ? "Odd" : "Even");
    }

    /**
     * Release a stable ID for re-use.
     */
    private static void releaseId(int nId)
    {
        s_UsedIds &= ~(1 << nId);
    }
}
