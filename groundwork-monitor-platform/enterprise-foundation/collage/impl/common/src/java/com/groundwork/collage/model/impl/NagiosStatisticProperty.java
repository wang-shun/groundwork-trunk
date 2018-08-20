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

public class NagiosStatisticProperty implements Serializable, Cloneable
{
	private java.lang.String propertyName;

    private long hostStatisticEnabled;

    private long hostStatisticDisabled;

    private long serviceStatisticEnabled;

    private long serviceStatisticDisabled;

    public NagiosStatisticProperty() {
    }

    public NagiosStatisticProperty(
           java.lang.String propertyName,
           long hostStatisticEnabled,
           long hostStatisticDisabled,
           long serviceStatisticEnabled,
           long serviceStatisticDisabled) {
           this.propertyName = propertyName;
           this.hostStatisticEnabled = hostStatisticEnabled;
           this.hostStatisticDisabled = hostStatisticDisabled;
           this.serviceStatisticEnabled = serviceStatisticEnabled;
           this.serviceStatisticDisabled = serviceStatisticDisabled;
    }


    /**
     * Gets the propertyName value for this NagiosStatisticProperty.
     * 
     * @return propertyName
     */
    public java.lang.String getPropertyName() {
        return propertyName;
    }


    /**
     * Sets the propertyName value for this NagiosStatisticProperty.
     * 
     * @param propertyName
     */
    public void setPropertyName(java.lang.String propertyName) {
        this.propertyName = propertyName;
    }


    /**
     * Gets the hostStatisticEnabled value for this NagiosStatisticProperty.
     * 
     * @return hostStatisticEnabled
     */
    public long getHostStatisticEnabled() {
        return hostStatisticEnabled;
    }


    /**
     * Sets the hostStatisticEnabled value for this NagiosStatisticProperty.
     * 
     * @param hostStatisticEnabled
     */
    public void setHostStatisticEnabled(long hostStatisticEnabled) {
        this.hostStatisticEnabled = hostStatisticEnabled;
    }


    /**
     * Gets the hostStatisticDisabled value for this NagiosStatisticProperty.
     * 
     * @return hostStatisticDisabled
     */
    public long getHostStatisticDisabled() {
        return hostStatisticDisabled;
    }


    /**
     * Sets the hostStatisticDisabled value for this NagiosStatisticProperty.
     * 
     * @param hostStatisticDisabled
     */
    public void setHostStatisticDisabled(long hostStatisticDisabled) {
        this.hostStatisticDisabled = hostStatisticDisabled;
    }


    /**
     * Gets the serviceStatisticEnabled value for this NagiosStatisticProperty.
     * 
     * @return serviceStatisticEnabled
     */
    public long getServiceStatisticEnabled() {
        return serviceStatisticEnabled;
    }


    /**
     * Sets the serviceStatisticEnabled value for this NagiosStatisticProperty.
     * 
     * @param serviceStatisticEnabled
     */
    public void setServiceStatisticEnabled(long serviceStatisticEnabled) {
        this.serviceStatisticEnabled = serviceStatisticEnabled;
    }


    /**
     * Gets the serviceStatisticDisabled value for this NagiosStatisticProperty.
     * 
     * @return serviceStatisticDisabled
     */
    public long getServiceStatisticDisabled() {
        return serviceStatisticDisabled;
    }


    /**
     * Sets the serviceStatisticDisabled value for this NagiosStatisticProperty.
     * 
     * @param serviceStatisticDisabled
     */
    public void setServiceStatisticDisabled(long serviceStatisticDisabled) {
        this.serviceStatisticDisabled = serviceStatisticDisabled;
    }
    
	public String toString ()
	{
		StringBuilder sb = new StringBuilder(64);
		
		sb.append("Property Name: ");
		sb.append(propertyName);
		sb.append(", Host Enabled=");
		sb.append(hostStatisticEnabled);
		sb.append(", Host Disabled=");
		sb.append(hostStatisticDisabled);
		sb.append(", Service Enabled=");
		sb.append(serviceStatisticEnabled);
		sb.append(", Service Disabled=");
		sb.append(serviceStatisticDisabled);
	    
		return sb.toString();
	}    
	
	public Object clone()
	{
    	return new NagiosStatisticProperty(propertyName, 
    									   hostStatisticEnabled, 
    									   hostStatisticDisabled, 
    									   serviceStatisticEnabled, 
    									   serviceStatisticDisabled);
	}
}
