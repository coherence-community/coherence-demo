/*
 * File: MemberInfo.java
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

package com.oracle.coherence.demo.model;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

import com.tangosol.net.Member;

import java.io.IOException;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * An immutable class to represent the information about a single cluster member
 *
 * @author Brian Oliver
 */
@XmlRootElement(name = "member-info")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class MemberInfo implements PortableObject
{
    private static final long serialVersionUID = -2555078539266609164L;

    /**
     * POF index for id attribute.
     */
    private static final int ID = 0;

    /**
     * POF index for maxMemory attribute.
     */
    private static final int MAX_MEMORY = 1;

    /**
     * POF index for freeMemory attribute.
     */
    private static final int FREE_MEMORY = 2;

    /**
     * POF index for totalMemory attribute.
     */
    private static final int TOTAL_MEMORY = 3;

    /**
     * POF index for entryCount attribute.
     */
    private static final int ENTRY_COUNT = 4;

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
     * Default Constructor (required and used only by {@link PortableObject}).
     */
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
        this.id          = member.getId();
        this.maxMemory   = runtime.maxMemory();
        this.totalMemory = runtime.totalMemory();
        this.freeMemory  = runtime.freeMemory();
        this.entryCount  = entryCount;
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
     *  
     * Obtain the maximum memory that can be used for the member.
     *
     * @return the maximum memory
     */
    public long getMaxMemory()
    {
        return maxMemory;
    }


    /**
     *  
     * Obtain the total memory that is used for the member.
     *
     * @return the total memory
     */
    public long getTotalMemory()
    {
        return totalMemory;
    }


    /**
     *  
     * Obtain the free memory that is available to the member.
     *
     * @return the free memory
     */
    public long getFreeMemory()
    {
        return freeMemory;
    }


    /**
     *  
     * Obtain the number of entries storged for the member.
     *
     * @return the entry count
     */
    public int getEntryCount()
    {
        return entryCount;
    }


    @Override
    public void readExternal(PofReader reader) throws IOException
    {
        id          = reader.readInt(ID);
        maxMemory   = reader.readLong(MAX_MEMORY);
        freeMemory  = reader.readLong(FREE_MEMORY);
        totalMemory = reader.readLong(TOTAL_MEMORY);
        entryCount  = reader.readInt(ENTRY_COUNT);
    }


    @Override
    public void writeExternal(PofWriter writer) throws IOException
    {
        writer.writeInt(ID, id);
        writer.writeLong(MAX_MEMORY, maxMemory);
        writer.writeLong(FREE_MEMORY, freeMemory);
        writer.writeLong(TOTAL_MEMORY, totalMemory);
        writer.writeInt(ENTRY_COUNT, entryCount);
    }
}
