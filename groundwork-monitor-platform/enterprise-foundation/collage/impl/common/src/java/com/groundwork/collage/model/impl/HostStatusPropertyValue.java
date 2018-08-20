/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com

 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.

 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.

 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.groundwork.collage.model.impl;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;

public class HostStatusPropertyValue extends PropertyValue implements Serializable, com.groundwork.collage.model.HostStatusPropertyValue
{

    private static final long serialVersionUID = 1;

    /** identifier field */
    private Integer hostStatusId;    
             
    /** default constructor */
    public HostStatusPropertyValue()
    {
    }

	public HostStatusPropertyValue(Integer hostStatusId, String name, Object value)
	{
		super(name, value);
		
		this.hostStatusId = hostStatusId;
	}		
    
    public Integer getHostStatusId ()
    {
    	return hostStatusId;
    }
    
    public void setHostStatusId (Integer hostStatusId)
    {
    	this.hostStatusId = hostStatusId;
    }   
    
    /** 
     * two property value objects are equal if they have the same name, 
     * compared in a case-insensitive way and related to the same owner
     */
    public boolean equals(Object other) {
        if ( (this == other ) ) return true;
        if ( !(other instanceof ServiceStatus) ) return false;
        HostStatusPropertyValue castOther = (HostStatusPropertyValue) other;

        return new EqualsBuilder()
            .append(this.getHostStatusId(), castOther.getHostStatusId())
            .append(this.getName().toLowerCase(), castOther.getName().toLowerCase())
            .isEquals();
    }

    public int hashCode() 
    {
        String name = getName();
        return new HashCodeBuilder()
            .append(getHostStatusId())
            .append((name == null) ? "" : name.toLowerCase())
            .toHashCode();
    }      
}