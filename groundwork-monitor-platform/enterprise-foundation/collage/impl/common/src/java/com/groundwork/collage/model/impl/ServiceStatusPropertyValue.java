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

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class ServiceStatusPropertyValue extends PropertyValue implements Serializable, com.groundwork.collage.model.ServiceStatusPropertyValue
{
    private static final long serialVersionUID = 1;

    /** identifier field */
    private Integer serviceStatusId;
                 
    /** default constructor */
    public ServiceStatusPropertyValue()
    {
    }

	public ServiceStatusPropertyValue(Integer serviceStatusId, String name, Object value)
	{
		super(name, value);
		
		this.serviceStatusId = serviceStatusId;
	}		
    
    public Integer getServiceStatusId ()
    {
    	return serviceStatusId;
    }
    
    public void setServiceStatusId (Integer serviceStatusId)
    {
    	this.serviceStatusId = serviceStatusId;
    } 
    
    /** 
     * two property value objects are equal if they have the same name, 
     * compared in a case-insensitive way and related to the same owner
     */
    public boolean equals(Object other) {
        if ( (this == other ) ) return true;
        if ( !(other instanceof ServiceStatus) ) return false;
        ServiceStatusPropertyValue castOther = (ServiceStatusPropertyValue) other;

        return new EqualsBuilder()
            .append(this.getServiceStatusId(), castOther.getServiceStatusId())
            .append(this.getName().toLowerCase(), castOther.getName().toLowerCase())
            .isEquals();
    }

    public int hashCode() 
    {
        return new HashCodeBuilder()
            .append(getServiceStatusId())
            .append(getName().toLowerCase())
            .toHashCode();
    }    
}