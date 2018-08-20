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

import com.groundwork.collage.model.MonitorServer;
import com.groundwork.collage.model.PropertyType;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/** @author Hibernate CodeGenerator */
public class Device extends PropertyExtensibleAbstract implements Serializable,
        com.groundwork.collage.model.Device 
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
								Device.ENTITY_TYPE_CODE,
								true);

    private static final PropertyType PROP_DISPLAY_NAME = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_DISPLAY_NAME,
								HP_DISPLAY_NAME, // Description is hibernate property name
								PropertyType.DataType.STRING, 
								Device.ENTITY_TYPE_CODE,
								true);
	
    private static final PropertyType PROP_DESCRIPTION = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_DESCRIPTION,
								HP_DESCRIPTION, // Description is hibernate property name
								PropertyType.DataType.STRING, 
								Device.ENTITY_TYPE_CODE,
								true);
	
    private static final PropertyType PROP_IDENTIFICATION = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_IDENTIFICATION,
								HP_IDENTIFICATION, // Description is hibernate property name
								PropertyType.DataType.STRING, 
								Device.ENTITY_TYPE_CODE,
								true);	
	
	/** Filter Only Properties */
    private static final PropertyType PROP_HOST_ID = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_HOST_ID,
								HP_HOST_ID, // Description is hibernate property name
								PropertyType.DataType.INTEGER, 
								Host.ENTITY_TYPE_CODE,
								true);		
	
    private static final PropertyType PROP_HOST_NAME = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_HOST_NAME,
								HP_HOST_NAME, // Description is hibernate property name
								PropertyType.DataType.STRING, 
								Host.ENTITY_TYPE_CODE,
								true);	
	
    private static final PropertyType PROP_MONITOR_SERVER_ID = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_MONITOR_SERVER_ID,
								HP_MONITOR_SERVER_ID, // Description is hibernate property name
								PropertyType.DataType.INTEGER, 
								MonitorServer.ENTITY_TYPE_CODE,
								true);
	
    /** Built-In property list - For now its static - Once app type properties are supported we may have
     * to have an instance variable **/
	private static List<PropertyType> BUILT_IN_PROPERTIES = null;
	
	/* Hibernate component properties */
	private static List<PropertyType> COMPONENT_PROPERTIES = null;
	
    /** identifier field */
    private Integer deviceId;

    /** nullable persistent field */
    private String displayName;

    /** nullable persistent field */
    private String identification;

    /** nullable persistent field */
    private String description;

    /** persistent field */
    private Set hosts;

    /* unidirectional many to many associations */
    private Set deviceParents;
    private Set deviceChildren;

    private Set monitorServers;

    /** full constructor */
    public Device(Integer deviceId, String displayName, String identification,
            String description, Set hosts, Set deviceChildren, Set deviceParents)
    {
        this.deviceId = deviceId;
        this.displayName = displayName;
        this.identification = identification;
        this.description = description;
        this.hosts = hosts;
        this.deviceChildren = deviceChildren;
        this.deviceParents = deviceParents;
    }

    /** default constructor */
    public Device()
    {
    }

    /** minimal constructor */
    public Device(Integer deviceId, Set hosts, Set deviceChildren, Set deviceParents)
    {
        this.deviceId = deviceId;
        this.hosts = hosts;
        this.deviceChildren = deviceChildren;
        this.deviceParents = deviceParents;
    }

    public String getEntityTypeCode() {
        return com.groundwork.collage.model.Device.ENTITY_TYPE_CODE;
    }

	public PropertyValue getPropertyValueInstance (String name, Object value)
	{
        return new EntityPropertyValue(deviceId, getEntityTypeId(), name, value);
	}
	
    public Integer getDeviceId()
    {
        return this.deviceId;
    }

    public void setDeviceId(Integer deviceId)
    {
        this.deviceId = deviceId;
        
        // Update property values with host status id
		if (propertyValues == null)
			return;		

		EntityPropertyValue propVal = null;
		Iterator it = propertyValues.iterator();
		while (it.hasNext())
		{
			propVal = (EntityPropertyValue)it.next();			
			propVal.setObjectId(deviceId);			
		}          
    }

    public Integer getApplicationTypeId()
    {
        return com.groundwork.collage.model.ApplicationType.SYSTEM_APPLICATION_TYPE_ID;
    }

    public void setApplicationTypeId(Integer deviceId)
    {
        log.warn("ApplicationTypeId for Devices is fixed and cannot be changed!");
    }

    public String getDisplayName()
    {
        return this.displayName;
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    public String getIdentification()
    {
        return this.identification;
    }

    public void setIdentification(String identification)
    {
        this.identification = identification;
    }

    public String getDescription()
    {
        return this.description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Set getHosts()
    {
        if (this.hosts == null)
            this.hosts = new HashSet();

        return this.hosts;
    }

    public void setHosts(Set hosts)
    {
        this.hosts = hosts;
    }

    /*
     * @return 
     *  the parent Device with the given Identification, or null if no parent
     *  with that Ident can be found
     */
    public com.groundwork.collage.model.Device getParent(String ident)
    {
        Device device = retrieveFromSet(this.getParents(), ident);

        if (device == null)
            log.warn("Unable to find parent Device '" + ident + "'");
        else if (log.isInfoEnabled()) 
            log.info("found parent Device '" + ident + "' for Device '" + this.getIdentification() + "'");

        return device;
    }
    
 
    public void addParent(com.groundwork.collage.model.Device parentDevice)
    {
        this.getParents().add(parentDevice);
    }
  
    public void removeParent(com.groundwork.collage.model.Device parentDevice)
    {
        this.getParents().remove(parentDevice);
    }

    /* 
     * Removes the parent Devices identified by the array of string
     * identifications
     *
     * @return the number of parents removed
     */
    /*
    public int removeParents(String[] deviceIdent)
    {
        int numRemoved = 0;

        if (deviceIdent != null) {
            for (int i=0; i < deviceIdent.length ; i++) {
                Device device = this.getParent(deviceIdent[i]);
                if (device != null) 
                {
                    this.removeParent(device);
                    numRemoved++;
                    if (log.isInfoEnabled()) log.info("removed parentDevice '" + deviceIdent[i] + "' for Device '" + this.getIdentification() + "'");
                }
            }
        }
        if (log.isInfoEnabled()) log.info("removed " + numRemoved + " parent Devices from Device '" + this.getIdentification() + "'");
        return numRemoved;
    }
    */
    
    /*
     * @return 
     *  the child Device with the given Identification, or null if no child
     *  with that Ident can be found
     */
    public com.groundwork.collage.model.Device getChild(String ident)
    {
        Device device = retrieveFromSet(this.getChildren(), ident);

        if (device == null)
            log.warn("Unable to find child Device '" + ident + "'");
        else if (log.isInfoEnabled()) 
            log.info("found child Device '" + ident + "' for Device '" + this.getIdentification() + "'");

        return device;
    }
    
    public void addChild(com.groundwork.collage.model.Device childDevice)
    {
        this.getChildren().add(childDevice);
    }
    
    public void removeChild(com.groundwork.collage.model.Device childDevice)
    {
        this.getChildren().remove(childDevice);
    }
    
    public Set getParents()
    {
        if (this.deviceParents == null)
            this.deviceParents = new HashSet();

        return this.deviceParents;
    }
    
    public void setParents(Set parents)
    {
        this.deviceParents = parents;
    }
    
    public Set getChildren()
    {
        if (this.deviceChildren == null)
            this.deviceChildren = new HashSet();

        return this.deviceChildren;
    }
    
    public void setChildren(Set children)
    {
        this.deviceChildren = children;;
    }
    
    public String toString()
    {
        return new ToStringBuilder(this)
            .append("deviceStatusId",getDeviceId())
            .append("ident",getIdentification())
            .toString();
    }


    /**
     * @return Returns the monitorServers.
     */
    public Set getMonitorServers() 
    {
        if (this.monitorServers == null)
            this.monitorServers = new HashSet();
        
        return monitorServers;
    }
    /**
     * @param monitorServers The monitorServers to set.
     */
    public void setMonitorServers(Set monitorServers) {
        this.monitorServers = monitorServers;
    }
    
    /**
     * 
     * @param monitorServer
     */
    public boolean addMonitorServer(MonitorServer monitorServer)
    {
    	if (monitorServer == null)
    		throw new IllegalArgumentException("Invalid null MonitorServer parameter.");
    	
    	return this.getMonitorServers().add(monitorServer);
    }
    
    public boolean removeMonitorServer(MonitorServer monitorServer)
    {
    	if (monitorServer == null)
    		throw new IllegalArgumentException("Invalid null MonitorServer parameter.");
    	
    	return this.getMonitorServers().remove(monitorServer);    	
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
            return this.getDeviceId();
        }
        else if (key.equalsIgnoreCase(EP_DISPLAY_NAME)) {
            return this.getDisplayName();
        }
        else if (key.equalsIgnoreCase(EP_DESCRIPTION)) {
            return this.getDescription();
        }
        else if (key.equalsIgnoreCase(EP_IDENTIFICATION)) {
            return getIdentification();
        }   	
        else {
            return super.getProperty(key);
        }
    }   
    
	public List<PropertyType> getBuiltInProperties()
	{
		if (BUILT_IN_PROPERTIES != null)
			return BUILT_IN_PROPERTIES;
			
		BUILT_IN_PROPERTIES = new ArrayList<PropertyType>(5);
		
		// Add non-volatile properties		
		BUILT_IN_PROPERTIES.add(PROP_ID);
		BUILT_IN_PROPERTIES.add(PROP_DISPLAY_NAME);
		BUILT_IN_PROPERTIES.add(PROP_DESCRIPTION);
		BUILT_IN_PROPERTIES.add(PROP_IDENTIFICATION);
		
		return BUILT_IN_PROPERTIES;
	}  
	
	public List<PropertyType> getComponentProperties()
	{
		if (COMPONENT_PROPERTIES != null)
			return COMPONENT_PROPERTIES;
		
		COMPONENT_PROPERTIES = new ArrayList<PropertyType>(10);
		
		// Since device has no "calculated" properties the component properties
		// are the a super-set of the built-in properties
		List<PropertyType> builtInProperties =  getBuiltInProperties();
		
		if (builtInProperties != null)
			COMPONENT_PROPERTIES.addAll(builtInProperties);
		
		/** Add Filter Only Properties **/
		COMPONENT_PROPERTIES.add(PROP_HOST_ID);
		COMPONENT_PROPERTIES.add(PROP_HOST_NAME);
		COMPONENT_PROPERTIES.add(PROP_MONITOR_SERVER_ID);
		
		return COMPONENT_PROPERTIES;
	}  	
	
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) 
    {
        if (obj == null || !(obj instanceof Device))
            return false;
        
        if (this == obj)
        	return true;
        
        Device toCompare = (Device)obj;
        
        return this.getIdentification().equals(toCompare.getIdentification());
    }

    public int hashCode() 
    {
    	return identification.hashCode();
    }

    /** 
     * used to retrieve a child/parent Device from the child/parent Set -
     * instead of this method we could implement the relationship as a Map
     * keyed by identification
     */
    private static Device retrieveFromSet(Set set, String ident)
    {
        for (Iterator i = set.iterator(); i.hasNext() ;)
        {
            Device device = (Device)i.next();
            if (device != null && device.getIdentification().equals(ident)) {
                return device;
            }
        }
        return null;
    }
}
