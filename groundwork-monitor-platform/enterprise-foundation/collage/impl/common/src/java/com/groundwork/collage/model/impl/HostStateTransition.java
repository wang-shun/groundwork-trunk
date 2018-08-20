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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.groundwork.collage.exception.CollageException;
import com.groundwork.collage.model.MonitorStatus;
import com.groundwork.collage.model.PropertyType;

public class HostStateTransition extends StateTransition 
{
	private static final long serialVersionUID = 1;
	
    /** Built-In property list - For now its static - Once app type properties are supported we may have
     * to have an instance variable **/
	private static List<PropertyType> BUILT_IN_PROPERTIES = null;
	
	/* Hibernate component properties */
	private static List<PropertyType> COMPONENT_PROPERTIES = null;
	
    /** the name that identifies this entity in the system: "STATE_TRANSITIONS" */
    public static final String ENTITY_TYPE_CODE = "HOST_STATE_TRANSITIONS";
    
	/* Built-In (Non-dynamic) Properties - The Properties which have a description (hibernate property)
	 * defined are properties that can be used to query the entity.
	 * We distinguish between "filterable" properties and properties that are returned in 
	 * property maps.
	 */	
       
	public static final PropertyType PROP_HOST_NAME = 
		new com.groundwork.collage.model.impl.PropertyType(				
								"Host",
								"Host",
								PropertyType.DataType.STRING, 
								null,
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
	
	private static final PropertyType PROP_FROM_STATE_ID = 
		new com.groundwork.collage.model.impl.PropertyType(				
								"FromStateId",
								"FromStateId",
								PropertyType.DataType.INTEGER, 
								null,
								true);	
	
	private static final PropertyType PROP_FROM_STATE = 
		new com.groundwork.collage.model.impl.PropertyType(				
								"FromState",
								"FromState",
								PropertyType.DataType.STRING, 
								null,
								true);	
	
	private static final PropertyType PROP_FROM_TRANSITION_DATE = 
		new com.groundwork.collage.model.impl.PropertyType(				
								"FromTransitionDate",
								"FromTransitionDate",
								PropertyType.DataType.DATE, 
								null,
								true);		
	
	private static final PropertyType PROP_TO_STATE_ID = 
		new com.groundwork.collage.model.impl.PropertyType(				
								"ToStateId",
								"ToStateId",
								PropertyType.DataType.INTEGER, 
								null,
								true);	
	
	private static final PropertyType PROP_TO_STATE = 
		new com.groundwork.collage.model.impl.PropertyType(				
								"ToState",
								"ToState",
								PropertyType.DataType.STRING, 
								null,
								true);	
	
	private static final PropertyType PROP_TO_TRANSITION_DATE = 
		new com.groundwork.collage.model.impl.PropertyType(				
								"ToTransitionDate",
								"ToTransitionDate",
								PropertyType.DataType.DATE, 
								null,
								true);	

	private static final PropertyType PROP_END_TRANSITION_DATE = 
		new com.groundwork.collage.model.impl.PropertyType(				
								"EndTransitionDate",
								"EndTransitionDate",
								PropertyType.DataType.DATE, 
								null,
								true);	
	
	private static final PropertyType PROP_STATE_DURATION = 
		new com.groundwork.collage.model.impl.PropertyType(				
								"StateDuration",
								"StateDuration",
								PropertyType.DataType.LONG, 
								null,
								true);	
	
	/**
	 * Default Constructor
	 */
	public HostStateTransition ()
	{		
	}
	
	/**
	 * Constructor
	 * @param statProperty
	 */
	public HostStateTransition (String hostName, 
							MonitorStatus fromStatus, 
							Date fromDate, 
							MonitorStatus toStatus, 
							Date toDate)
	{
		this.hostName = hostName;
		this.fromStatus = fromStatus;
		this.fromTransitionDate = fromDate;
		this.toStatus = toStatus;
		this.toTransitionDate = toDate;		
	}	
	
	public HostStateTransition (String hostName, 
			MonitorStatus fromStatus, 
			Date fromDate, 
			MonitorStatus toStatus, 
			Date toDate,
			Date endTransitionDate)
	{
		this.hostName = hostName;
		this.fromStatus = fromStatus;
		this.fromTransitionDate = fromDate;
		this.toStatus = toStatus;
		this.toTransitionDate = toDate;		
		this.endTransitionDate = endTransitionDate;		
	}		
	
	public List<PropertyType> getBuiltInProperties() 
	{
		if (BUILT_IN_PROPERTIES != null)
			return BUILT_IN_PROPERTIES;
			
		BUILT_IN_PROPERTIES = new ArrayList<PropertyType>(10);
		
		// Add non-volatile properties
		BUILT_IN_PROPERTIES.add(PROP_HOST_NAME);
		BUILT_IN_PROPERTIES.add(PROP_FROM_STATE_ID);
		BUILT_IN_PROPERTIES.add(PROP_FROM_STATE);
		BUILT_IN_PROPERTIES.add(PROP_FROM_TRANSITION_DATE);		
		BUILT_IN_PROPERTIES.add(PROP_TO_STATE_ID);
		BUILT_IN_PROPERTIES.add(PROP_TO_STATE);
		BUILT_IN_PROPERTIES.add(PROP_TO_TRANSITION_DATE);
		BUILT_IN_PROPERTIES.add(PROP_END_TRANSITION_DATE);
		BUILT_IN_PROPERTIES.add(PROP_STATE_DURATION);
		
		return BUILT_IN_PROPERTIES;
	}
	
	public List<PropertyType> getComponentProperties() 
	{
		if (COMPONENT_PROPERTIES != null)
			return COMPONENT_PROPERTIES;
		
		COMPONENT_PROPERTIES = new ArrayList<PropertyType>(10);		
		
		/** Add Filter Only Properties **/
		COMPONENT_PROPERTIES.add(PROP_HOST_NAME);
		COMPONENT_PROPERTIES.add(PROP_START_DATE);
		COMPONENT_PROPERTIES.add(PROP_END_DATE);
		
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
    
		if (propertyName.equalsIgnoreCase(PROP_HOST_NAME.getName()))
    	{
    		return this.hostName;
    	}	
		else if (propertyName.equalsIgnoreCase(PROP_FROM_STATE_ID.getName()))
    	{
    		if (this.fromStatus == null)
    			return null;
    		
    		return this.fromStatus.getMonitorStatusId();
    	}		
		else if (propertyName.equalsIgnoreCase(PROP_FROM_STATE.getName()))
    	{
    		if (this.fromStatus == null)
    			return null;
    		
    		return this.fromStatus.getName();
    	}
    	else if (propertyName.equalsIgnoreCase(PROP_FROM_TRANSITION_DATE.getName()))
    	{
    		return this.fromTransitionDate;
    	}
    	else if (propertyName.equalsIgnoreCase(PROP_TO_STATE_ID.getName()))
    	{
    		if (this.toStatus == null)
    			return null;
    		
    		return this.toStatus.getMonitorStatusId();
    	} 		
    	else if (propertyName.equalsIgnoreCase(PROP_TO_STATE.getName()))
    	{
    		if (this.toStatus == null)
    			return null;
    		
    		return this.toStatus.getName();
    	}    	
    	else if (propertyName.equalsIgnoreCase(PROP_TO_TRANSITION_DATE.getName()))
    	{
    		return this.toTransitionDate;
    	}   
    	else if (propertyName.equalsIgnoreCase(PROP_END_TRANSITION_DATE.getName()))
    	{
    		return this.endTransitionDate;
    	} 		
    	else if (propertyName.equalsIgnoreCase(PROP_STATE_DURATION.getName()))
    	{
    		// NOTE:  We call method to apply logic when no end transition date is set
    		return getDurationInState();
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
    
    	if (propertyName.equalsIgnoreCase(PROP_HOST_NAME.getName()))
    	{
    		return PROP_HOST_NAME;
    	}  
    	else if (propertyName.equalsIgnoreCase(PROP_FROM_STATE_ID.getName()))
    	{
    		return PROP_FROM_STATE_ID;
    	}    	
    	else if (propertyName.equalsIgnoreCase(PROP_FROM_STATE.getName()))
    	{
    		return PROP_FROM_STATE;
    	}
    	else if (propertyName.equalsIgnoreCase(PROP_FROM_TRANSITION_DATE.getName()))
    	{
    		return PROP_FROM_TRANSITION_DATE;
    	}
    	else if (propertyName.equalsIgnoreCase(PROP_TO_STATE_ID.getName()))
    	{
    		return PROP_TO_STATE_ID;
    	}       	
    	else if (propertyName.equalsIgnoreCase(PROP_TO_STATE.getName()))
    	{
    		return PROP_TO_STATE;
    	}     	
    	else if (propertyName.equalsIgnoreCase(PROP_TO_TRANSITION_DATE.getName()))
    	{
    		return PROP_TO_TRANSITION_DATE;
    	} 
    	else if (propertyName.equalsIgnoreCase(PROP_END_TRANSITION_DATE.getName()))
    	{
    		return PROP_END_TRANSITION_DATE;
    	}     	
    	else if (propertyName.equalsIgnoreCase(PROP_STATE_DURATION.getName()))
    	{
    		return PROP_STATE_DURATION;
    	} 
    	else 
    	{
    		throw new CollageException("Unknown property - " + propertyName);
    	}	
	}

	public boolean hasPropertyType(String propertyName) 
	{
    	if (propertyName.equalsIgnoreCase(PROP_HOST_NAME.getName()))
    	{
    		return true;
    	}
    	else if (propertyName.equalsIgnoreCase(PROP_FROM_STATE_ID.getName()))
    	{
    		return true;
    	}    	
    	else if (propertyName.equalsIgnoreCase(PROP_FROM_STATE.getName()))
    	{
    		return true;
    	}
    	else if (propertyName.equalsIgnoreCase(PROP_FROM_TRANSITION_DATE.getName()))
    	{
    		return true;
    	}
    	else if (propertyName.equalsIgnoreCase(PROP_TO_STATE_ID.getName()))
    	{
    		return true;
    	}     	
    	else if (propertyName.equalsIgnoreCase(PROP_TO_STATE.getName()))
    	{
    		return true;
    	} 
    	else if (propertyName.equalsIgnoreCase(PROP_TO_TRANSITION_DATE.getName()))
    	{
    		return true;
    	}    
    	else if (propertyName.equalsIgnoreCase(PROP_END_TRANSITION_DATE.getName()))
    	{
    		return true;
    	}      	
    	else if (propertyName.equalsIgnoreCase(PROP_STATE_DURATION.getName()))
    	{
    		return true;
    	}      	
    	else 
    	{
    		return false;
    	}	
	}
}
