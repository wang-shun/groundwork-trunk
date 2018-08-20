/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.groundwork.rs.dto;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "cache")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoCacheState
{
    @XmlAttribute
    private String name;
    @XmlAttribute
    private float averageGetTime;
    @XmlAttribute
    private long cacheHits;
    @XmlAttribute
    private long cacheMisses;
    @XmlAttribute
    private long diskStoreSize;
    @XmlAttribute
    private long evictionCount;
    @XmlAttribute
    private long inMemoryHits;
    @XmlAttribute
    private long inMemorySize;
    @XmlAttribute
    private long memoryStoreSize;
    @XmlAttribute
    private long objectCount;
    @XmlAttribute
    private long size;
    @XmlAttribute
    private long onDiskHits;
    @XmlAttribute
    private long maxElementsInMemory;
    @XmlAttribute
    private long maxElementsOnDisk;
    @XmlAttribute
    private long timeToIdle;
    @XmlAttribute
    private long timeToLive;
    
    public DtoCacheState(String name)
    {
        this.name = name;
    }

    public DtoCacheState() {}

    public String getCacheName()
    {
        return name;
    }
    
    public float getAverageGetTime()
    {
        return averageGetTime;
    }

    public long getCacheHits()
    {
        return cacheHits;
    }

    public long getCacheMisses()
    {
        return cacheMisses;
    }

    public long getDiskStoreSize()
    {
        return diskStoreSize;
    }

    public long getEvictionCount()
    {
        return evictionCount;
    }

    public long getInMemoryHits()
    {
        return inMemoryHits;
    }

    public long getInMemorySize()
    {
        return inMemorySize;
    }

    public long getMemoryStoreSize()
    {
        return memoryStoreSize;
    }

    public long getObjectCount()
    {
        return objectCount;
    }
    
    public long getSize()
    {
        return size;
    }

    public long getOnDiskHits()
    {
        return onDiskHits;
    }
    
    
    public long getMaxElementsInMemory()
    {
        return maxElementsInMemory;
    }

    public long getMaxElementsOnDisk()
    {
        return maxElementsOnDisk;
    }
    
    public long getTimeToIdle()
    {
        return timeToIdle;
    }

    public long getTimeToLive()
    {
        return timeToLive;
    }        
    
    public void setAverageGetTime(float averageGetTime)
    {
        this.averageGetTime = averageGetTime;
    }

    
    public void setCacheHits(long cacheHits)
    {
        this.cacheHits = cacheHits;
    }

    
    public void setCacheMisses(long cacheMisses)
    {
        this.cacheMisses = cacheMisses;
    }

    
    public void setDiskStoreSize(long diskStoreSize)
    {
        this.diskStoreSize = diskStoreSize;
    }

    
    public void setEvictionCount(long evictionCount)
    {
        this.evictionCount = evictionCount;
    }

    
    public void setInMemoryHits(long inMemoryHits)
    {
        this.inMemoryHits = inMemoryHits;
    }

    
    public void setInMemorySize(long inMemorySize)
    {
        this.inMemorySize = inMemorySize;
    }

    
    public void setMemoryStoreSize(long memoryStoreSize)
    {
        this.memoryStoreSize = memoryStoreSize;
    }

    
    public void setObjectCount(long objectCount)
    {
        this.objectCount = objectCount;
    }

    
    public void setSize(long size)
    {
        this.size = size;
    }

    
    public void setOnDiskHits(long onDiskHits)
    {
        this.onDiskHits = onDiskHits;
    }
 
    
    public void setMaxElementsInMemory(long maxElementsInMemory)
    {
        this.maxElementsInMemory = maxElementsInMemory;
    }

    
    public void setMaxElementsOnDisk(long maxElementsOnDisk)
    {
        this.maxElementsOnDisk = maxElementsOnDisk;
    }

    
    public void setTimeToIdle(long timeToIdle)
    {
        this.timeToIdle = timeToIdle;
    }

    public void setTimeToLive(long timeToLive)
    {
        this.timeToLive = timeToLive;
    }
    
}