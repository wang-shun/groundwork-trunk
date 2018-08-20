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

import com.groundwork.collage.model.PropertyType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** @author Hibernate CodeGenerator */
public class MonitorServer extends PropertyExtensibleAbstract implements Serializable, com.groundwork.collage.model.MonitorServer
{
	private static final long serialVersionUID = 1;
	
	/* Built-In (Non-dynamic) Properties - The Properties which have a description (hibernate property)
	 * defined are properties that can be used to query the entity.
	 * We distinquish between "filterable" properties and properties that are returned in 
	 * property maps.
	 */	
	private static final PropertyType PROP_ID = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_ID,
								HP_ID, // Description is hibernate property name
								PropertyType.DataType.INTEGER, 
								MonitorServer.ENTITY_TYPE_CODE,
								true);		
	
	private static final PropertyType PROP_NAME = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_NAME,
								HP_NAME, // Description is hibernate property name
								PropertyType.DataType.STRING, 
								MonitorServer.ENTITY_TYPE_CODE,
								true);	

	private static final PropertyType PROP_IP = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_IP,
								HP_IP, // Description is hibernate property name
								PropertyType.DataType.STRING, 
								MonitorServer.ENTITY_TYPE_CODE,
								true);	
	
	private static final PropertyType PROP_DESCRIPTION = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_DESCRIPTION,
								HP_DESCRIPTION, // Description is hibernate property name
								PropertyType.DataType.STRING, 
								MonitorServer.ENTITY_TYPE_CODE,
								true);	
		
    /** Built-In property list - For now its static - Once app type properties are supported we may have
     * to have an instance variable **/
	private static List<PropertyType> BUILT_IN_PROPERTIES = null;

	//private Log log = LogFactory.getLog(this.getClass());

    /** identifier field */
    private Integer monitorServerId;

    /** persistent field */
    private String monitorServerName;

    /** persistent field */
    private String ip;

    /** nullable persistent field */
    private String description;
      
    /* Association table MonitorList
     * devices is a list of devices accositaed with this Monitor Server 
     */
    private Set devices;

    /** full constructor */
    public MonitorServer(Integer monitorServerId, String monitorServerName,
            String ip, String description, Set devices)
    {
        this.monitorServerId = monitorServerId;
        this.monitorServerName = monitorServerName;
        this.ip = ip;
        this.description = description;
        this.devices = devices;
    }

    /** default constructor */
    public MonitorServer()
    {
        
    }

    /** minimal constructor */
    public MonitorServer(Integer monitorServerId, String monitorServerName,
            String ip, Set devices)
    {
        this.monitorServerId = monitorServerId;
        this.monitorServerName = monitorServerName;
        this.ip = ip;
        this.devices = devices;
     }

    public Integer getMonitorServerId()
    {
        return this.monitorServerId;
    }

    public void setMonitorServerId(Integer monitorServerId)
    {
        this.monitorServerId = monitorServerId;
    }

    public String getMonitorServerName()
    {
        return this.monitorServerName;
    }

    public void setMonitorServerName(String monitorServerName)
    {
        this.monitorServerName = monitorServerName;
    }

    public String getIp()
    {
        return this.ip;
    }

    public void setIp(String ip)
    {
        this.ip = ip;
    }

    public String getDescription()
    {
        return this.description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }


    public Set getDevices()
    {
        if (this.devices == null)
            this.devices = new HashSet();

        return this.devices;
    }

    /** @param devices The devices to set.  */
    public void setDevices(Set devices) {
        this.devices = devices;
    }
       
    public String toString()
    {
        return new ToStringBuilder(this).append("monitorServerId",
                getMonitorServerId()).toString();
    }

    /** 
     * two MonitorServer objects are equal if they have the same name, 
     * compared in a case-insensitive way
     */
    public boolean equals(Object other) {
        if ( (this == other ) ) return true;
        if ( !(other instanceof MonitorServer) ) return false;
        MonitorServer castOther = (MonitorServer) other;

        return new EqualsBuilder()
            .append(this.getMonitorServerName().toLowerCase(), castOther.getMonitorServerName().toLowerCase())
            .isEquals();
    }


    public int hashCode() 
    {
        return new HashCodeBuilder()
            .append(getMonitorServerName().toLowerCase())
            .toHashCode();
    }
    
    /** 
     * This method overrides the property by the same name in
     * PropertyExtensibleAbstract to make it possible to get the value of one
     * of the named property getters 
     *
     * @throws IllegalArgumentException 
     *
     *  if unable to find PropertyType with the key provided, or if the key
     *  does not corresponding to one of the declared get/sets
     */
    public Object getProperty(String key) throws IllegalArgumentException
    {
    	if (key == null || key.length() == 0)
    	{
    		throw new IllegalArgumentException("Invalid null / empty property key.");
    	}
    	
        if (key.equalsIgnoreCase(EP_ID)) {
            return this.getMonitorServerId();
        }
        else if (key.equalsIgnoreCase(EP_NAME)) {
            return this.getMonitorServerName();
        }
        else if (key.equalsIgnoreCase(EP_ID)) {
            return this.getIp();
        }        
        else if (key.equalsIgnoreCase(EP_DESCRIPTION)) {
            return this.getDescription();
        } 	
        else {
            return super.getProperty(key);
        }
    }   
    
	/* (non-Javadoc)
	 * @see com.groundwork.collage.model.impl.PropertyExtensibleAbstract#getBuiltInProperties()
	 */
	@Override
	public List<PropertyType> getBuiltInProperties()
	{
		if (BUILT_IN_PROPERTIES != null)
			return BUILT_IN_PROPERTIES;
			
		BUILT_IN_PROPERTIES = new ArrayList<PropertyType>(10);
		
		// Add non-volatile properties		
		BUILT_IN_PROPERTIES.add(PROP_ID);
		BUILT_IN_PROPERTIES.add(PROP_NAME);
		BUILT_IN_PROPERTIES.add(PROP_IP);
		BUILT_IN_PROPERTIES.add(PROP_DESCRIPTION);		
		
		return BUILT_IN_PROPERTIES;
	}

	/* (non-Javadoc)
	 * @see com.groundwork.collage.model.impl.PropertyExtensibleAbstract#getEntityTypeCode()
	 */
	@Override
	public String getEntityTypeCode()
	{
		return ENTITY_TYPE_CODE;
	}

	/* (non-Javadoc)
	 * @see com.groundwork.collage.model.impl.PropertyExtensibleAbstract#getPropertyValueInstance(java.lang.String, java.lang.Object)
	 */
	@Override
	public PropertyValue getPropertyValueInstance(String name, Object value)
	{
		return new EntityPropertyValue(monitorServerId, getEntityTypeId(), name, value);
	}

	/* (non-Javadoc)
	 * @see com.groundwork.collage.model.PropertyExtensible#getComponentProperties()
	 */
	public List<PropertyType> getComponentProperties()
	{
		// Filterable properties are the same as the built-in properties
		return getBuiltInProperties();
	}    
}
