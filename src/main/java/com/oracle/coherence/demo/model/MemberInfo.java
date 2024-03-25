/*
 * File: MemberInfo.java
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

package com.oracle.coherence.demo.model;

import com.tangosol.internal.tracing.TracingHelper;

import com.tangosol.io.pof.PortableObject;

import com.tangosol.io.pof.schema.annotation.PortableType;
import com.tangosol.net.Member;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * An immutable class to represent the information about a single cluster member.
 *
 * @author Brian Oliver
 */
@XmlRootElement(name = "member-info")
@XmlAccessorType(XmlAccessType.PROPERTY)
@PortableType
public class MemberInfo
{
    @SuppressWarnings("unused")
    private static final long serialVersionUID = -2555078539266609164L;

    /**
     * The unique identifier for the {@link MemberInfo}.
     */
    private int id;

    /**
     * The maximum memory from the {@link Runtime}.
     */
    private long maxMemory;

    /**
     * The free memory from the {@link Runtime}.
     */
    private long freeMemory;

    /**
     * The total memory from the {@link Runtime}.
     */
    private long totalMemory;

    /**
     * The total number of entries managed by the member.
     */
    private int entryCount;

    /**
     * Flag, when {@code} true, indicates tracing is enabled on this member.
     */
    private boolean tracingEnabled;

    /**
     * The role name for this member.
     */
    private String roleName;


    /**
     * Default Constructor (required and used only by {@link PortableObject}).
     */
    @SuppressWarnings("unused")
    public MemberInfo()
    {
        // required for Serializable and PortableObject
    }


    /**
     * Construct for a {@link MemberInfo} based on a {@link Member}
     * and the {@link Runtime} of the said {@link Member}.
     *
     * @param member      the {@link Member}
     * @param runtime     the {@link Runtime}
     * @param entryCount  the number of entries
     */
    public MemberInfo(Member  member,
                      Runtime runtime,
                      int     entryCount)
    {
        this.id             = member.getId();
        this.maxMemory      = runtime.maxMemory();
        this.totalMemory    = runtime.totalMemory();
        this.freeMemory     = runtime.freeMemory();
        this.entryCount     = entryCount;
        this.tracingEnabled = TracingHelper.isEnabled();
        this.roleName       = member.getRoleName();
    }


    /**
     * Obtain the unique identifier for the {@link MemberInfo}.
     *
     * @return the identifier
     */
    public int getId()
    {
        return id;
    }


    /**
     * Obtain the maximum memory that can be used for the member.
     *
     * @return the maximum memory
     */
    @SuppressWarnings("unused")
    public long getMaxMemory()
    {
        return maxMemory;
    }


    /**
     * Obtain the total memory that is used for the member.
     *
     * @return the total memory
     */
    @SuppressWarnings("unused")
    public long getTotalMemory()
    {
        return totalMemory;
    }


    /**
     * Obtain the free memory that is available to the member.
     *
     * @return the free memory
     */
    @SuppressWarnings("unused")
    public long getFreeMemory()
    {
        return freeMemory;
    }


    /**
     * Obtain the number of entries stored for the member.
     *
     * @return the entry count
     */
    @SuppressWarnings("unused")
    public int getEntryCount()
    {
        return entryCount;
    }

    /**
     * Returns {@code true} if tracing is enabled on this member.
     *
     * @return {@code true} if tracing is enabled on this member
     */
    @SuppressWarnings("unused")
    public boolean isTracingEnabled()
        {
        return tracingEnabled;
        }

    /**
     * Return this member's role name.
     *
     * @return this member's role name
     */
    @SuppressWarnings("unused")
     public String getRoleName()
         {
         return roleName;
         }
}
