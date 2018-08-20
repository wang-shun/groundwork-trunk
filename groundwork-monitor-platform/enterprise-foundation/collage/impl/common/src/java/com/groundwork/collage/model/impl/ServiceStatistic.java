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
import java.util.ArrayList;
import java.util.List;

import com.groundwork.collage.exception.CollageException;
import com.groundwork.collage.model.ApplicationType;
import com.groundwork.collage.model.PropertyExtensible;
import com.groundwork.collage.model.PropertyType;

public class ServiceStatistic extends StatisticEntity implements Serializable, PropertyExtensible
{
	private static final long serialVersionUID = 1;
		
    /** Built-In property list - For now its static - Once app type properties are supported we may have
     * to have an instance variable **/
	private static List<PropertyType> BUILT_IN_PROPERTIES = null;
	
	/* Hibernate component properties */
	private static List<PropertyType> COMPONENT_PROPERTIES = null;
	
    /** the name that identifies this entity in the system: "SERVICE_STATISTICS" */
    public static final String ENTITY_TYPE_CODE = "SERVICE_STATISTICS";
    
	/* Built-In (Non-dynamic) Properties - The Properties which have a description (hibernate property)
	 * defined are properties that can be used to query the entity.
	 * We distinguish between "filterable" properties and properties that are returned in 
	 * property maps.
	 */	
    
    /** Note Owner Name may be host group name or host name depending on the query being performed **/
	private static final PropertyType PROP_OWNER_NAME = 
		new com.groundwork.collage.model.impl.PropertyType(				
								"OwnerName",
								"OwnerName",
								PropertyType.DataType.STRING, 
								null,
								true);	
	
	private static final PropertyType PROP_NAME = 
		new com.groundwork.collage.model.impl.PropertyType(				
								"Name",
								"Name",
								PropertyType.DataType.STRING, 
								null,
								true);		
	
	private static final PropertyType PROP_COUNT = 
		new com.groundwork.collage.model.impl.PropertyType(				
								"Count",
								"Count",
								PropertyType.DataType.LONG, 
								null,
								true);	
	
	/** Filterable Properties **/
	public static final PropertyType PROP_APPLICATION_TYPE_NAME = 
		new com.groundwork.collage.model.impl.PropertyType(				
								"ApplicationType",
								"ApplicationType",
								PropertyType.DataType.STRING, 
								ApplicationType.ENTITY_TYPE_CODE,
								true);  
	
	public static final PropertyType PROP_HOST_GROUP_NAME = 
		new com.groundwork.collage.model.impl.PropertyType(				
								"HostGroupName",
								"HostGroupName",
								PropertyType.DataType.STRING, 
								HostGroup.ENTITY_TYPE_CODE,
								true);  	
	
	public static final PropertyType PROP_HOST_NAME = 
		new com.groundwork.collage.model.impl.PropertyType(				
								"HostName",
								"HostName",
								PropertyType.DataType.STRING, 
								Host.ENTITY_TYPE_CODE,
								true);  	

	/* HostGroup Name - if it is a Total for all then Name will be "ALL" */
	private String hostGroupName = null;
	
	/**
	 * Statistic Name (e.g. UP, DOWN, etc)
	 */
	private String name = null;
	
	/**
	 * Statistic Value
	 */
	private long count = 0;
	
	/**
	 * Default Constructor
	 */
	public ServiceStatistic ()
	{		
	}
	
	/**
	 * Constructor
	 * @param statProperty
	 */
	public ServiceStatistic (String hostGroupName, StatisticProperty statProperty)
	{
		if (statProperty == null)
			throw new IllegalArgumentException("Invalid null StatisticProperty parameter.");
		
		this.hostGroupName = hostGroupName;		
		this.name = statProperty.getName();
		this.count = statProperty.getCount();
	}	
	
	public List<PropertyType> getBuiltInProperties() 
	{
		if (BUILT_IN_PROPERTIES != null)
			return BUILT_IN_PROPERTIES;
			
		BUILT_IN_PROPERTIES = new ArrayList<PropertyType>(10);
		
		// Add non-volatile properties
		BUILT_IN_PROPERTIES.add(PROP_OWNER_NAME);
		BUILT_IN_PROPERTIES.add(PROP_NAME);
		BUILT_IN_PROPERTIES.add(PROP_COUNT);
		
		return BUILT_IN_PROPERTIES;
	}
	
	public List<PropertyType> getComponentProperties() 
	{
		if (COMPONENT_PROPERTIES != null)
			return COMPONENT_PROPERTIES;
		
		COMPONENT_PROPERTIES = new ArrayList<PropertyType>(10);		
		
		/** Add Filter Only Properties **/
		COMPONENT_PROPERTIES.add(PROP_APPLICATION_TYPE_NAME);
		COMPONENT_PROPERTIES.add(PROP_HOST_GROUP_NAME);
		COMPONENT_PROPERTIES.add(PROP_HOST_NAME);
		
		return COMPONENT_PROPERTIES;		
	}
	
	public String getEntityTypeCode() 
	{
		return ENTITY_TYPE_CODE;
	}	

	public Object getProperty(String propertyName) 
	{
		if (propertyName == null || propertyName.length() == 0)
    		throw new IllegalArgumentException("Invalid null / empty propertyName parameter.");
    
    	if (propertyName.equalsIgnoreCase(PROP_NAME.getName()))
    	{
    		return this.name;
    	}
    	else if (propertyName.equalsIgnoreCase(PROP_COUNT.getName()))
    	{
    		return this.count;
    	}
    	else if (propertyName.equalsIgnoreCase(PROP_OWNER_NAME.getName()))
    	{
    		return this.hostGroupName;
    	}    	
    	else 
    	{
    		return null;
    	}	    		
   }

	public PropertyType getPropertyType(String propertyName) 
	{
		if (propertyName == null || propertyName.length() == 0)
    		throw new IllegalArgumentException("Invalid null / empty propertyName parameter.");
    
    	if (propertyName.equalsIgnoreCase(PROP_NAME.getName()))
    	{
    		return PROP_NAME;
    	}
    	else if (propertyName.equalsIgnoreCase(PROP_COUNT.getName()))
    	{
    		return PROP_COUNT;
    	}
    	else if (propertyName.equalsIgnoreCase(PROP_OWNER_NAME.getName()))
    	{
    		return PROP_OWNER_NAME;
    	}     	
    	else 
    	{
    		throw new CollageException("Unknown property - " + propertyName);
    	}	
	}

	public boolean hasPropertyType(String propertyName) 
	{
    	if (propertyName.equalsIgnoreCase(PROP_NAME.getName()))
    	{
    		return true;
    	}
    	else if (propertyName.equalsIgnoreCase(PROP_COUNT.getName()))
    	{
    		return true;
    	}
    	else if (propertyName.equalsIgnoreCase(PROP_OWNER_NAME.getName()))
    	{
    		return true;
    	}    	
    	else 
    	{
    		return false;
    	}	
	}
}
