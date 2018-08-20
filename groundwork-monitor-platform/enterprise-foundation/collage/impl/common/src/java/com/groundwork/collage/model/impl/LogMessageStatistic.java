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

public class LogMessageStatistic extends StatisticEntity implements Serializable, PropertyExtensible
{
	private static final long serialVersionUID = 1;
		
    /** Built-In property list - For now its static - Once app type properties are supported we may have
     * to have an instance variable **/
	private static List<PropertyType> BUILT_IN_PROPERTIES = null;
	
	/* Hibernate component properties */
	private static List<PropertyType> COMPONENT_PROPERTIES = null;
	
    /** the name that identifies this entity in the system: "LOG_MESSAGE_STATISTICS" */
    public static final String ENTITY_TYPE_CODE = "LOG_MESSAGE_STATISTICS";
        
	/* Built-In (Non-dynamic) Properties - The Properties which have a description (hibernate property)
	 * defined are properties that can be used to query the entity.
	 * We distinguish between "filterable" properties and properties that are returned in 
	 * property maps.
	 */	
	public static final PropertyType PROP_NAME = 
		new com.groundwork.collage.model.impl.PropertyType(				
								"Name",
								"Name",
								PropertyType.DataType.STRING, 
								null,
								true);		
	
	public static final PropertyType PROP_COUNT = 
		new com.groundwork.collage.model.impl.PropertyType(				
								"Count",
								"Count",
								PropertyType.DataType.LONG, 
								null,
								true);	
	
	/** Filterable Properties **/
	public static final PropertyType PROP_STATISTIC_TYPE =
		new com.groundwork.collage.model.impl.PropertyType(				
				"StatisticType",
				"StatisticType",
				PropertyType.DataType.STRING, 
				null,
				true);  
	
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
	
	public static final PropertyType PROP_START_DATE = 
		new com.groundwork.collage.model.impl.PropertyType(				
								"StartDate",
								"StartDate",
								PropertyType.DataType.DATE, 
								null,
								true);  
	
	public static final PropertyType PROP_END_DATE = 
		new com.groundwork.collage.model.impl.PropertyType(				
								"EndDate",
								"EndDate",
								PropertyType.DataType.DATE, 
								null,
								true);  	
	
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
	public LogMessageStatistic ()
	{		
	}
	
	/**
	 * Constructor
	 * @param statProperty
	 */
	public LogMessageStatistic (StatisticProperty statProperty)
	{
		if (statProperty == null)
			throw new IllegalArgumentException("Invalid null StatisticProperty parameter.");
		
		this.name = statProperty.getName();
		this.count = statProperty.getCount();
	}
	
	public List<PropertyType> getBuiltInProperties() 
	{
		if (BUILT_IN_PROPERTIES != null)
			return BUILT_IN_PROPERTIES;
			
		BUILT_IN_PROPERTIES = new ArrayList<PropertyType>(10);
		
		// Add non-volatile properties		
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
		COMPONENT_PROPERTIES.add(PROP_START_DATE);
		COMPONENT_PROPERTIES.add(PROP_END_DATE);
		COMPONENT_PROPERTIES.add(PROP_STATISTIC_TYPE);
		
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
    	else 
    	{
    		return false;
    	}	
	}
}