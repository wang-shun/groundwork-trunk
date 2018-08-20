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

import com.groundwork.collage.model.ApplicationType;
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
import java.util.*;


/** @author Hibernate CodeGenerator */
public class ServiceStatus extends PropertyExtensibleAbstract implements Serializable, com.groundwork.collage.model.ServiceStatus
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
								ServiceStatus.ENTITY_TYPE_CODE,
								true);		

	private static final PropertyType PROP_SERVICE_DESCRIPTION = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_SERVICE_DESCRIPTION,
								HP_SERVICE_DESCRIPTION, // Description is hibernate property name
								PropertyType.DataType.STRING, 
								ServiceStatus.ENTITY_TYPE_CODE,
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
	
	private static final PropertyType PROP_LAST_CHECK_TIME = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_LAST_CHECK_TIME,
								HP_LAST_CHECK_TIME, // Description is hibernate property name
								PropertyType.DataType.DATE, 
								ServiceStatus.ENTITY_TYPE_CODE,
								true);	
	
	private static final PropertyType PROP_NEXT_CHECK_TIME = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_NEXT_CHECK_TIME,
								HP_NEXT_CHECK_TIME, // Description is hibernate property name
								PropertyType.DataType.DATE, 
								ServiceStatus.ENTITY_TYPE_CODE,
								true);	

	private static final PropertyType PROP_LAST_STATE_CHANGE = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_LAST_STATE_CHANGE,
								HP_LAST_STATE_CHANGE, // Description is hibernate property name
								PropertyType.DataType.DATE, 
								ServiceStatus.ENTITY_TYPE_CODE,
								true);		
	
	private static final PropertyType PROP_LAST_HARD_STATE_ID = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_LAST_HARD_STATE_ID,
								HP_LAST_HARD_STATE_ID, // Description is hibernate property name
								PropertyType.DataType.INTEGER, 
								MonitorStatus.ENTITY_TYPE_CODE,
								true);	
	
	private static final PropertyType PROP_LAST_HARD_STATE_NAME = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_LAST_HARD_STATE_NAME,
								HP_LAST_HARD_STATE_NAME, // Description is hibernate property name
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
	
	private static final PropertyType PROP_DOMAIN = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_DOMAIN,
								HP_DOMAIN, // Description is hibernate property name
								PropertyType.DataType.STRING, 
								ServiceStatus.ENTITY_TYPE_CODE,
								true);  
	
	private static final PropertyType PROP_METRIC_TYPE = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_METRIC_TYPE,
								HP_METRIC_TYPE, // Description is hibernate property name
								PropertyType.DataType.STRING, 
								ServiceStatus.ENTITY_TYPE_CODE,
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

    private static final PropertyType PROP_APPLICATION_HOST_NAME =
        new com.groundwork.collage.model.impl.PropertyType(
                                EP_APPLICATION_HOST_NAME,
                                HP_APPLICATION_HOST_NAME, // Description is hibernate property name
                                PropertyType.DataType.STRING,
                                Host.ENTITY_TYPE_CODE,
                                true);

    private static final PropertyType PROP_AGENTID =
        new com.groundwork.collage.model.impl.PropertyType(
                                EP_AGENT_ID,
                                HP_AGENT_ID, // Description is hibernate property name
                                PropertyType.DataType.STRING,
                                Host.ENTITY_TYPE_CODE,
                                true);

    /** Built-In property list - For now its static - Once app type properties are supported we may have
     * to have an instance variable **/
	private static List<PropertyType> BUILT_IN_PROPERTIES = null;
	
    /** identifier field */
    private Integer serviceStatusId;

    /** used by hibernate */
    private ApplicationType applicationType;
	private MonitorStatus monitorStatus;
	private MonitorStatus lastHardState;
	private CheckType checkType;
	private StateType stateType;
    private String serviceDescription;
    private Host host;
    private Date lastCheckTime;
    private Date nextCheckTime;
    private Date lastStateChange;
    private String lastMonitorStatus;
    private String agentId;
    private String applicationHostName;
    private Set comments;


    /**
     * domain and metric type are used for faster indexing
     * application data.
     */
    private String domain; 
    private String metricType;
    
    /** default empty constructor */
    public ServiceStatus() {}


    public String getEntityTypeCode() {
        return com.groundwork.collage.model.ServiceStatus.ENTITY_TYPE_CODE;
    }

	public PropertyValue getPropertyValueInstance (String name, Object value)
	{
		return new ServiceStatusPropertyValue(serviceStatusId, name, value);
	}
	
    public Integer getServiceStatusId()
    {
        return this.serviceStatusId;
    }

    public void setServiceStatusId(Integer serviceStatusId)
    {
        this.serviceStatusId = serviceStatusId;
        
        // Update property values with service status id
		if (propertyValues == null)
			return;		

		ServiceStatusPropertyValue propVal = null;
		Iterator it = propertyValues.iterator();
		while (it.hasNext())
		{
			propVal = (ServiceStatusPropertyValue)it.next();			
			propVal.setServiceStatusId(serviceStatusId);			
		}        
    }

    public String getServiceDescription()
    {
        return this.serviceDescription;
    }

    public void setServiceDescription(String serviceDescription)
    {
        this.serviceDescription = serviceDescription;
    }


    public Host getHost()
    {
        return this.host;
    }

    public void setHost(Host host)
    {
        this.host = host;
    }

    public Set getComments() {
        if (this.comments == null)
            this.comments = new HashSet();

        return this.comments;
    }

    public void setComments(Set comments) {
        this.comments = comments;
    }

    public void addComment(com.groundwork.collage.model.Comment comment) {
        if (comments == null) comments = new HashSet();
        if (comment != null) comments.add(comment);
    }

    public void removeComment(com.groundwork.collage.model.Comment comment) {
        comments.remove(comment);
    }

    public Integer getApplicationTypeId()
    {
    	if (this.applicationType == null)
    		return null;
    	
        return this.applicationType.getApplicationTypeId();
    }

    public void setApplicationTypeId(Integer id) 
    {
    	if (id != null)    		
    		this.applicationType = getMetadata().getApplicationTypeById(id.intValue());
    	else
    		this.applicationType = null;
    }

    public ApplicationType getApplicationType()
    {
        return applicationType;
    }

    public void setApplicationType(ApplicationType applicationType)
    {
        this.applicationType = applicationType;
    }

    public Date getLastCheckTime() {
        return this.lastCheckTime;
    }

    public void setLastCheckTime(Date lastCheckTime) {
        this.lastCheckTime = lastCheckTime;
    }


    public Date getNextCheckTime() {
        return this.nextCheckTime;
    }

    public void setNextCheckTime(Date nextCheckTime) {
        this.nextCheckTime = nextCheckTime;
    }

    protected Integer getMonitorStatusId() 
    {
    	if (this.monitorStatus == null)
    		return null;
    	
        return this.monitorStatus.getMonitorStatusId();
    }

    protected void setMonitorStatusId(Integer monitorStatusId) 
    {
    	if (monitorStatusId != null)    		
    		this.monitorStatus = getMetadata().getMonitorStatusById(monitorStatusId.intValue());
    	else
    		this.monitorStatus = null;    	
    }    

    public MonitorStatus getMonitorStatus() 
    {
        return this.monitorStatus;
    }

    public void setMonitorStatus(MonitorStatus monitorStatus) 
    {
    	this.monitorStatus = monitorStatus;       
    }    

    public Date getLastStateChange() {
        return this.lastStateChange;
    }

    public void setLastStateChange(Date lastStateChange) {
        this.lastStateChange = lastStateChange;
    }

    protected Integer getLastHardStateId() 
    {
    	if (this.lastHardState == null)
    		return null;
    	
        return this.lastHardState.getMonitorStatusId();
    }

    protected void setLastHardStateId(Integer lastHardStateId) 
    {
    	if (lastHardStateId != null)    		
    		this.lastHardState = getMetadata().getMonitorStatusById(lastHardStateId.intValue());
    	else
    		this.lastHardState = null;    
    }
    
    public MonitorStatus getLastHardState()
    {
    	return this.lastHardState; 
    }

    public void setLastHardState(MonitorStatus lastHardState) 
    {
    	this.lastHardState = lastHardState;
    }
    
    protected Integer getCheckTypeId() 
    {
    	if (this.checkType == null)
    		return null;
    	
        return this.checkType.getCheckTypeId();
    }

    protected void setCheckTypeId(Integer checkTypeId) 
    {
    	if (checkTypeId != null)    		
    		this.checkType = getMetadata().getCheckTypeById(checkTypeId.intValue());
    	else
    		this.checkType = null;  
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

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getDomain()
    {
        return this.domain;
    }
    
    public void setDomain(String domain)
    {
        this.domain = domain;
    }
    
    public String getMetricType()
    {
        return this.metricType;
    }
    
    public void setMetricType(String metricType)
    {
        this.metricType = metricType;
    }

    @Override
    public String getApplicationHostName() {
        return applicationHostName;
    }

    @Override
    public void setApplicationHostName(String applicationHostName) {
        this.applicationHostName = applicationHostName;
    }

    /**
     * This method overrides the property by the same name in
     * PropertyExtensibleAbstract to make it possible to set the value of one
     * of the built-in property setters 
     *
     * @throws IllegalArgumentException 
     *
     *  if unable to find PropertyType with the key provided, or if the key
     *  does not corresponding to one of the declared get/sets
     */
    public void setProperty(String key, Object value) throws IllegalArgumentException
    {
        if (key == null || key.length() == 0)
        	throw new IllegalArgumentException("Invalid null / empty property key parameter.");

        if (log.isDebugEnabled())
        	log.debug("attempting to set property: '" + key + "' with value " + value.getClass() + " - " + value );

        // read-only properties
        if (EP_ID.equals(key))
        	return;
		
        if (key.equals(EP_SERVICE_DESCRIPTION))
            this.setServiceDescription((String)value);
        else if (EP_MONITOR_STATUS_ID.equals(key))
        	 this.setMonitorStatus(getMetadata().getMonitorStatusById((Integer)value));
        else if (EP_MONITOR_STATUS_NAME.equals(key))
        	 this.setMonitorStatus(getMetadata().getMonitorStatusByName((String)value));
        else if (key.equals(EP_DOMAIN))
            this.setDomain((String)value);
        else if (key.equals(EP_METRIC_TYPE))
            this.setMetricType((String)value);
        else if (key.equals(EP_LAST_CHECK_TIME)) 
        {
            if (value instanceof Date)
                this.setLastCheckTime((Date)value);
            else if (value instanceof String)
                this.setLastCheckTime(DateTime.parse((String)value));              
        }
        else if (key.equals(EP_NEXT_CHECK_TIME)) 
        {
            if (value instanceof Date)
                this.setNextCheckTime((Date)value);
            else if (value instanceof String)
                this.setNextCheckTime(DateTime.parse((String)value));
        }
        else if (key.equals(EP_LAST_STATE_CHANGE)) 
        {
            if (value instanceof Date)
                this.setLastStateChange((Date)value);
            else if (value instanceof String)
                this.setLastStateChange(DateTime.parse((String)value));
        }
        else if (EP_LAST_HARD_STATE_ID.equals(key)) 
        {
            this.setLastHardState(getMetadata().getMonitorStatusById((Integer)value));
        }
        else if (EP_LAST_HARD_STATE_NAME.equals(key)) 
        {
            this.setLastHardState(getMetadata().getMonitorStatusByName((String)value));
        }        
        else if (EP_CHECK_TYPE_ID.equals(key))
        {
            this.setCheckType(getMetadata().getCheckTypeById((Integer)value));            
        }
        else if (EP_CHECK_TYPE_NAME.equals(key)) 
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
        else if (EP_APPLICATION_TYPE_ID.equals(key)) 
        {
        	this.setApplicationTypeId((Integer)value);          
        }   
        else if (EP_APPLICATION_TYPE_NAME.equals(key)) 
        {
        	this.setApplicationType(getMetadata().getApplicationTypeByName((String)value));          
        }
        else if (EP_APPLICATION_HOST_NAME.equals(key))
        {
            this.setApplicationHostName((String)value);
        }
        else {
            super.setProperty(key, value);
        }       
    }


    /** 
     * This method overrides the property by the same name in
     * PropertyExtensibleAbstract to make it possible to set the value of one
     * of the built-in property getters 
     *
     * @throws IllegalArgumentException 
     *
     *  if unable to find PropertyType with the key provided, or if the key
     *  does not corresponding to one of the declared get/sets
     */
    public Object getProperty(String key) throws IllegalArgumentException
    {
    	if (key == null || key.length() == 0)
    		throw new IllegalArgumentException("Invalid null / empty property key parameter.");
    	 
		if (key.equals(EP_ID))
			return this.getServiceStatusId();
		else if (key.equals(EP_SERVICE_DESCRIPTION)) {
            return this.getServiceDescription();
        }
        else if (key.equals(EP_MONITOR_STATUS_ID)) {        	
            return this.getMonitorStatusId();
        }
        else if (key.equals(EP_MONITOR_STATUS_NAME)) {
        	MonitorStatus status = this.getMonitorStatus();
        	if (status == null) return null;
        	
        	return status.getName();
        }		
        else if (key.equals(EP_LAST_CHECK_TIME)) {
            return this.getLastCheckTime();
        }
        else if (key.equals(EP_NEXT_CHECK_TIME)) {
            return this.getNextCheckTime();
        }
        else if (key.equals(EP_LAST_STATE_CHANGE)) {
            return this.getLastStateChange();
        }
        else if (key.equals(EP_LAST_HARD_STATE_ID)) {
            return this.getLastHardStateId();
        }
        else if (key.equals(EP_LAST_HARD_STATE_NAME)) {
        	MonitorStatus status = this.getLastHardState();
        	if (status == null) return null;
        	
        	return status.getName();
        }
        else if (key.equals(EP_CHECK_TYPE_ID)) {
            return this.getCheckTypeId();
        }
        else if (key.equals(EP_CHECK_TYPE_NAME)) {
        	CheckType checkType = this.getCheckType();
        	if (checkType == null)
        		return null;
        	
            return checkType.getName();
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
        else if (key.equals(EP_DOMAIN)) {
            return this.getDomain();
        }
        else if (key.equals(EP_METRIC_TYPE)) {
            return this.getMetricType();
        }
        else if (key.equals(EP_APPLICATION_TYPE_ID)) {
            return this.getApplicationTypeId();
        }
        else if (key.equals(EP_APPLICATION_TYPE_NAME)) 
        {
        	ApplicationType appType = this.getApplicationType();
        	if (appType == null) return null;
        	
        	return appType.getName();
        }
        else if (key.equals(EP_HOST_ID)) {
            Host host = this.getHost();
            if (host == null) return null;
            
            return host.getHostId();            
        }
        else if (key.equals(EP_HOST_NAME)) {
            Host host = this.getHost();
            if (host == null) return null;
            
            return host.getHostName();     
        }
        else if (key.equals(EP_APPLICATION_HOST_NAME)) {
            return this.getApplicationHostName();
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
		BUILT_IN_PROPERTIES.add(PROP_SERVICE_DESCRIPTION);
		BUILT_IN_PROPERTIES.add(PROP_MONITOR_STATUS_ID);		
		BUILT_IN_PROPERTIES.add(PROP_MONITOR_STATUS_NAME);
		BUILT_IN_PROPERTIES.add(PROP_LAST_CHECK_TIME);
		BUILT_IN_PROPERTIES.add(PROP_NEXT_CHECK_TIME);
		BUILT_IN_PROPERTIES.add(PROP_LAST_STATE_CHANGE);
		BUILT_IN_PROPERTIES.add(PROP_LAST_HARD_STATE_ID);
		BUILT_IN_PROPERTIES.add(PROP_LAST_HARD_STATE_NAME);
		BUILT_IN_PROPERTIES.add(PROP_CHECK_TYPE_ID);
		BUILT_IN_PROPERTIES.add(PROP_CHECK_TYPE_NAME);	
		BUILT_IN_PROPERTIES.add(PROP_STATE_TYPE_ID);	
		BUILT_IN_PROPERTIES.add(PROP_STATE_TYPE_NAME);	
		BUILT_IN_PROPERTIES.add(PROP_DOMAIN);	
		BUILT_IN_PROPERTIES.add(PROP_METRIC_TYPE);	
		BUILT_IN_PROPERTIES.add(PROP_APPLICATION_TYPE_ID);	
		BUILT_IN_PROPERTIES.add(PROP_APPLICATION_TYPE_NAME);	
		BUILT_IN_PROPERTIES.add(PROP_HOST_ID);
		BUILT_IN_PROPERTIES.add(PROP_HOST_NAME);
        BUILT_IN_PROPERTIES.add(PROP_APPLICATION_HOST_NAME);
        BUILT_IN_PROPERTIES.add(PROP_AGENTID);

		return BUILT_IN_PROPERTIES;
	} 
	
	public List<PropertyType> getComponentProperties()
	{
		// Since service status has no "calculated" properties the component properties
		// are the same as the built-in properties
		return getBuiltInProperties();
	}  
	
    public String toString()
    {
        return new ToStringBuilder(this).append("serviceStatusId",
                getServiceStatusId()).toString();
    }

    /** 
     * two ServiceStatus objects are equal if they have the same description, 
     * compared in a case-insensitive way
     */
    public boolean equals(Object other) {
        if ( (this == other ) ) return true;
        if ( !(other instanceof ServiceStatus) ) return false;
        ServiceStatus castOther = (ServiceStatus) other;

        return new EqualsBuilder()
            .append(this.getServiceDescription().toLowerCase(), castOther.getServiceDescription().toLowerCase())
            .isEquals();
    }

    public int hashCode() 
    {
        return new HashCodeBuilder()
            .append(getServiceDescription().toLowerCase())
            .toHashCode();
    }


	public String getLastMonitorStatus() {
		return lastMonitorStatus;
	}


	public void setLastMonitorStatus(String lastMonitorStatus) {
		this.lastMonitorStatus = lastMonitorStatus;
	}
}
