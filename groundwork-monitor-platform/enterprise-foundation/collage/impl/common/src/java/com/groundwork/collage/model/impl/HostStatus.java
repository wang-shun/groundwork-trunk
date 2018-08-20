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

import com.groundwork.collage.model.CheckType;
import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.MonitorStatus;
import com.groundwork.collage.model.PropertyType;
import com.groundwork.collage.model.StateType;
import com.groundwork.collage.util.DateTime;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class HostStatus extends PropertyExtensibleAbstract 
	implements Serializable, com.groundwork.collage.model.HostStatus
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
								HostStatus.ENTITY_TYPE_CODE,
								true);		

	private static final PropertyType PROP_APPLICATION_TYPE_ID = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_APPLICATION_TYPE_ID,
								HP_APPLICATION_TYPE_ID, // Description is hibernate property name
								PropertyType.DataType.INTEGER, 
								ApplicationType.ENTITY_TYPE_CODE,
								true);  	
	
	private static final PropertyType PROP_APPLICATION_TYPE_NAME = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_APPLICATION_TYPE_NAME,
								HP_APPLICATION_TYPE_NAME, // Description is hibernate property name
								PropertyType.DataType.STRING, 
								ApplicationType.ENTITY_TYPE_CODE,
								true);  
		
	private static final PropertyType PROP_LAST_CHECK_TIME = 
		new com.groundwork.collage.model.impl.PropertyType(
								EP_LAST_CHECK_TIME,
								HP_LAST_CHECK_TIME, // Description is hibernate property name
								PropertyType.DataType.DATE, 
								null,
								true);	

	private static final PropertyType PROP_MONITOR_STATUS_ID = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_MONITOR_STATUS_ID,
								HP_MONITOR_STATUS_ID, // Description is hibernate property name
								PropertyType.DataType.INTEGER, 
								MonitorStatus.ENTITY_TYPE_CODE,
								true);  	
	
	private static final PropertyType PROP_MONITOR_STATUS_NAME = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_MONITOR_STATUS_NAME,
								HP_MONITOR_STATUS_NAME, // Description is hibernate property name
								PropertyType.DataType.STRING, 
								MonitorStatus.ENTITY_TYPE_CODE,
								true);  

	private static final PropertyType PROP_CHECK_TYPE_ID = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_CHECK_TYPE_ID,
								HP_CHECK_TYPE_ID, // Description is hibernate property name
								PropertyType.DataType.INTEGER, 
								CheckType.ENTITY_TYPE_CODE,
								true);  	
	
	private static final PropertyType PROP_CHECK_TYPE_NAME = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_CHECK_TYPE_NAME,
								HP_CHECK_TYPE_NAME, // Description is hibernate property name
								PropertyType.DataType.STRING, 
								CheckType.ENTITY_TYPE_CODE,
								true);  
	
	private static final PropertyType PROP_HOST_NAME = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_HOST_NAME,
								HP_HOST_NAME, // Description is hibernate property name
								PropertyType.DataType.STRING, 
								Host.ENTITY_TYPE_CODE,
								true);  
	
	private static final PropertyType PROP_STATE_TYPE_ID = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_STATE_TYPE_ID,
								HP_STATE_TYPE_ID, // Description is hibernate property name
								PropertyType.DataType.INTEGER, 
								StateType.ENTITY_TYPE_CODE,
								true);  
		
	private static final PropertyType PROP_STATE_TYPE_NAME = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_STATE_TYPE_NAME,
								HP_STATE_TYPE_NAME, // Description is hibernate property name
								PropertyType.DataType.STRING, 
								StateType.ENTITY_TYPE_CODE,
								true);  
	
	private static final PropertyType PROP_NEXT_CHECK_TIME = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_NEXT_CHECK_TIME,
								HP_NEXT_CHECK_TIME, // Description is hibernate property name
								PropertyType.DataType.DATE, 
								ServiceStatus.ENTITY_TYPE_CODE,
								true);	
	
    /** Built-In property list - For now its static - Once app type properties are supported we may have
     * to have an instance variable **/
	private static List<PropertyType> BUILT_IN_PROPERTIES = null;
	
    /** identifier field */
    private Integer hostStatusId;

    private Host host;
    private MonitorStatus hostMonitorStatus;
    private Date lastCheckTime;
    private CheckType checkType;
    
    private Date nextCheckTime;
    private StateType stateType;

    public Integer getHostStatusId() {
        return this.hostStatusId;
    }

    public void setHostStatusId(Integer hostStatusId) 
    {
        this.hostStatusId = hostStatusId;
        
        // Update property values with host status id
		if (propertyValues == null)
			return;		

		HostStatusPropertyValue propVal = null;
		Iterator it = propertyValues.iterator();
		while (it.hasNext())
		{
			propVal = (HostStatusPropertyValue)it.next();			
			propVal.setHostStatusId(hostStatusId);			
		}             
    }
    
    public String getEntityTypeCode() {
        return com.groundwork.collage.model.HostStatus.ENTITY_TYPE_CODE;
    }
    
	public PropertyValue getPropertyValueInstance (String name, Object value)
	{
		return new HostStatusPropertyValue(hostStatusId, name, value);
	}

    public Host getHost() {
        return this.host;
    }

    public void setHost(Host host) {
        this.host = host;
    }


    /** short-cut for this.getHost().getHostName() */
    public String getHostName() {
        return this.getHost().getHostName();
    }


    public Date getLastCheckTime() {
        return this.lastCheckTime;
    }

    public void setLastCheckTime(Date lastCheckTime) {
        this.lastCheckTime = lastCheckTime;
    }


    protected Integer getMonitorStatusId() 
    {
        if (hostMonitorStatus == null)
        	return null;
        
        return hostMonitorStatus.getMonitorStatusId();
    }

    protected void setMonitorStatusId(Integer monitorStatusId) 
    {    	
    	if (monitorStatusId == null)
    		this.hostMonitorStatus = null;
    	else
    		this.hostMonitorStatus = getMetadata().getMonitorStatusById(monitorStatusId.intValue());
    }    

    public MonitorStatus getHostMonitorStatus() 
    {
    	return this.hostMonitorStatus;
    }

    public void setHostMonitorStatus(MonitorStatus monitorStatus) 
    {
        this.hostMonitorStatus = monitorStatus;
    }
    
    protected Integer getCheckTypeId()
    {
    	if (this.checkType == null)
    		return null;
    	else
    		return this.checkType.getCheckTypeId();
    }

    protected void setCheckTypeId(Integer checkTypeId) 
    {
    	if (checkTypeId == null)
    		this.checkType = null;
    	else
    		this.checkType = getMetadata().getCheckTypeById(checkTypeId.intValue());  
    }
    
    public CheckType getCheckType() 
    {
    	return this.checkType;
    }

    public void setCheckType(CheckType checkType) 
    {
    	this.checkType = checkType;
    }    
    
    protected Integer getStateTypeId()
    {
    	if (this.stateType == null)
    		return null;
    	
        return this.stateType.getStateTypeId();
    }

    protected void setStateTypeId(Integer stateTypeId) 
    {
    	if (stateTypeId != null)    		
    		this.stateType = getMetadata().getStateTypeById(stateTypeId.intValue());
    	else
    		this.stateType = null;  
    }
    
    public StateType getStateType() 
    {
    	return this.stateType;
    }

    public void setStateType(StateType stateType) 
    {
    	this.stateType = stateType;
    }
    
    public Date getNextCheckTime() {
        return this.nextCheckTime;
    }

    public void setNextCheckTime(Date nextCheckTime) {
        this.nextCheckTime = nextCheckTime;
    }
    

    /** 
     * This method overrides the property by the same name in
     * PropertyExtensibleAbstract to make it possible to set the value of one
     * of the named property setters 
     *
     * @throws IllegalArgumentException 
     *
     *  if unable to find PropertyType with the key provided, or if the key
     *  does not corresponding to one of the declared get/sets
     */
    public void setProperty(String key, Object value) throws IllegalArgumentException
    {
    	if (log.isDebugEnabled())
    		log.debug("attempting to set property: '" + key + "' with value " + value.getClass() + " - " + value );
    	
    	// Read only properties
        if (key.equalsIgnoreCase(EP_ID) || key.equalsIgnoreCase(EP_HOST_NAME) ||
        	key.equalsIgnoreCase(EP_APPLICATION_TYPE_NAME))
            return;
        
        if (key.equals(EP_LAST_CHECK_TIME)) 
        {
            if (value instanceof Date)
                this.setLastCheckTime((Date)value);
            else if (value instanceof String)
                this.setLastCheckTime(DateTime.parse((String)value));
        }
        else if (key.equals(EP_MONITOR_STATUS_ID)) 
        {   		
        	if (value instanceof String)
                this.setHostMonitorStatus(getMetadata().getMonitorStatusById(Integer.parseInt((String)value)));
            else if (value instanceof Integer)
                this.setHostMonitorStatus(getMetadata().getMonitorStatusById((Integer)value));
        }
        else if (key.equals(EP_MONITOR_STATUS_NAME)) 
        {   		
            this.setHostMonitorStatus(getMetadata().getMonitorStatusByName((String)value));
        }        
        else if (key.equals(EP_CHECK_TYPE_ID)) 
        {
        	if (value instanceof String)
                this.setCheckType(getMetadata().getCheckTypeById(Integer.parseInt((String)value)));
            else if (value instanceof Integer)
                this.setCheckType(getMetadata().getCheckTypeById((Integer)value));
        }
        else if (key.equals(EP_CHECK_TYPE_NAME)) 
        {
            this.setCheckType(getMetadata().getCheckTypeByName((String)value));     
        } 
        else if (EP_STATE_TYPE_ID.equals(key)) 
        {
            this.setStateType(getMetadata().getStateTypeById((Integer)value));            
        }
        else if (EP_STATE_TYPE_NAME.equals(key)) 
        {
        	this.setStateType(getMetadata().getStateTypeByName((String)value));          
        }
        else if (key.equals(EP_NEXT_CHECK_TIME)) 
        {
            if (value instanceof Date)
                this.setNextCheckTime((Date)value);
            else if (value instanceof String)
                this.setNextCheckTime(DateTime.parse((String)value));
        }
        else {
        	if (log.isDebugEnabled())
                log.debug("Adding Dynamic property [" + key + "] with value ["+value+"]");
            super.setProperty(key, value);
        }
    }

    /** 
     * This method overrides the property by the same name in
     * PropertyExtensibleAbstract to make it possible to set the value of one
     * of the named property getters 
     *
     * @throws IllegalArgumentException 
     *
     *  if unable to find PropertyType with the key provided, or if the key
     *  does not corresponding to one of the declared get/sets
     */
    public Object getProperty(String key) throws IllegalArgumentException
    {
    	if (key.equalsIgnoreCase(EP_ID))
    	{
    		return this.getHostStatusId();
    	} else if (key.equalsIgnoreCase(EP_HOST_NAME)) {
            return this.getHostName();
        } 
        else if (key.equalsIgnoreCase(EP_LAST_CHECK_TIME)) {
            return this.getLastCheckTime();
        }
        else if (key.equalsIgnoreCase(EP_MONITOR_STATUS_ID)) {
            return this.getHostMonitorStatus().getMonitorStatusId();
        }
        else if (key.equalsIgnoreCase(EP_MONITOR_STATUS_NAME)) {
            return this.getHostMonitorStatus().getName();
        }
        else if (key.equalsIgnoreCase(EP_CHECK_TYPE_ID)) {
            return this.getCheckTypeId();
        }
        else if (key.equalsIgnoreCase(EP_APPLICATION_TYPE_NAME)) {
            return this.getApplicationType().getName();
        }    
        else if (key.equalsIgnoreCase(EP_APPLICATION_TYPE_ID)) {
            return this.getApplicationTypeId();
        }   
        else if (key.equals(EP_STATE_TYPE_ID)) {
            return this.getStateTypeId();
        }
        else if (key.equals(EP_STATE_TYPE_NAME)) {
        	StateType stateType = this.getStateType();
        	if (stateType == null)
        		return null;
        	
            return stateType.getName();
        }
        else if (key.equals(EP_NEXT_CHECK_TIME)) {
            return this.getNextCheckTime();
        }
        else {
            return super.getProperty(key);
        }
    }

	public List<PropertyType> getBuiltInProperties()
	{
		if (BUILT_IN_PROPERTIES != null)
			return BUILT_IN_PROPERTIES;
			
		BUILT_IN_PROPERTIES = new ArrayList<PropertyType>(10);
		
		// Add non-volatile properties		
		BUILT_IN_PROPERTIES.add(PROP_ID);
		BUILT_IN_PROPERTIES.add(PROP_LAST_CHECK_TIME);
		BUILT_IN_PROPERTIES.add(PROP_MONITOR_STATUS_ID);
		BUILT_IN_PROPERTIES.add(PROP_MONITOR_STATUS_NAME);
		BUILT_IN_PROPERTIES.add(PROP_CHECK_TYPE_ID);
		BUILT_IN_PROPERTIES.add(PROP_CHECK_TYPE_NAME);
		BUILT_IN_PROPERTIES.add(PROP_HOST_NAME);
		BUILT_IN_PROPERTIES.add(PROP_APPLICATION_TYPE_ID);
		BUILT_IN_PROPERTIES.add(PROP_APPLICATION_TYPE_NAME);	
		BUILT_IN_PROPERTIES.add(PROP_STATE_TYPE_ID);	
		BUILT_IN_PROPERTIES.add(PROP_STATE_TYPE_NAME);	
		BUILT_IN_PROPERTIES.add(PROP_NEXT_CHECK_TIME);
		
		return BUILT_IN_PROPERTIES;
	} 
	
	
	public List<PropertyType> getComponentProperties()
	{
		// Since host status has no "calculated" properties the component properties
		// are the same as the built-in properties
		return getBuiltInProperties();
	}  
	
    public String toString()
    {
        return new ToStringBuilder(this).append("hostStatusId",
                getHostStatusId()).toString();
    }

    /** 
     * two Host Status objects are equal if they have a Host with the same name, 
		 * compared in a case-insensitive way
     */
    public boolean equals(Object other) {
        if ( (this == other ) ) return true;
        if ( !(other instanceof HostStatus) ) return false;
        HostStatus castOther = (HostStatus) other;

        return new EqualsBuilder()
            .append(this.getHostName().toLowerCase(), castOther.getHostName().toLowerCase())
            .isEquals();
    }


    public int hashCode() 
    {
        return new HashCodeBuilder()
            .append(getHostName().toLowerCase())
            .toHashCode();
    }

}
