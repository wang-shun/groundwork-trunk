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
import com.groundwork.collage.model.Component;
import com.groundwork.collage.model.Device;
import com.groundwork.collage.model.HostStatus;
import com.groundwork.collage.model.MonitorStatus;
import com.groundwork.collage.model.OperationStatus;
import com.groundwork.collage.model.Priority;
import com.groundwork.collage.model.PropertyType;
import com.groundwork.collage.model.ServiceStatus;
import com.groundwork.collage.model.Severity;
import com.groundwork.collage.model.TypeRule;
import com.groundwork.collage.util.DateTime;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class LogMessage extends PropertyExtensibleAbstract implements Serializable, com.groundwork.collage.model.LogMessage
{
	private static final long serialVersionUID = 1;
	
	/* Built-In (Non-dynamic) Properties - The Properties which have a description (hibernate property)
	 * defined are properties that can be used to query the entity.
	 * We distinguish between "filterable" properties and properties that are returned in 
	 * property maps.
	 */	
	private static final PropertyType PROP_ID = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_ID,
								HP_ID, // Description is hibernate property name
								PropertyType.DataType.INTEGER, 
								LogMessage.ENTITY_TYPE_CODE,
								true);		
	
	private static final PropertyType PROP_TEXT_MESSAGE = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_TEXT_MESSAGE,
								HP_TEXT_MESSAGE, // Description is hibernate property name
								PropertyType.DataType.STRING, 
								LogMessage.ENTITY_TYPE_CODE,
								true);	

	private static final PropertyType PROP_MSG_COUNT = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_MSG_COUNT,
								HP_MSG_COUNT, // Description is hibernate property name
								PropertyType.DataType.INTEGER, 
								LogMessage.ENTITY_TYPE_CODE,
								true);	
	
	private static final PropertyType PROP_FIRST_INSERT_DATE = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_FIRST_INSERT_DATE,
								HP_FIRST_INSERT_DATE, // Description is hibernate property name
								PropertyType.DataType.DATE, 
								LogMessage.ENTITY_TYPE_CODE,
								true);	

	private static final PropertyType PROP_LAST_INSERT_DATE = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_LAST_INSERT_DATE,
								HP_LAST_INSERT_DATE, // Description is hibernate property name
								PropertyType.DataType.DATE, 
								LogMessage.ENTITY_TYPE_CODE,
								true);		
	
	private static final PropertyType PROP_REPORT_DATE = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_REPORT_DATE,
								HP_REPORT_DATE, // Description is hibernate property name
								PropertyType.DataType.DATE, 
								LogMessage.ENTITY_TYPE_CODE,
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

	private static final PropertyType PROP_APP_SEVERITY_ID = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_APP_SEVERITY_ID,
								HP_APP_SEVERITY_ID, // Description is hibernate property name
								PropertyType.DataType.INTEGER, 
								Severity.ENTITY_TYPE_CODE,
								true);  	
	
	private static final PropertyType PROP_APP_SEVERITY_NAME = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_APP_SEVERITY_NAME,
								HP_APP_SEVERITY_NAME, // Description is hibernate property name
								PropertyType.DataType.STRING, 
								Severity.ENTITY_TYPE_CODE,
								true);  

	private static final PropertyType PROP_COMPONENT_ID = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_COMPONENT_ID,
								HP_COMPONENT_ID, // Description is hibernate property name
								PropertyType.DataType.INTEGER, 
								Component.ENTITY_TYPE_CODE,
								true);  	
	
	private static final PropertyType PROP_COMPONENT_NAME = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_COMPONENT_NAME,
								HP_COMPONENT_NAME, // Description is hibernate property name
								PropertyType.DataType.STRING, 
								Component.ENTITY_TYPE_CODE,
								true);  
	
	private static final PropertyType PROP_PRIORITY_ID = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_PRIORITY_ID,
								HP_PRIORITY_ID, // Description is hibernate property name
								PropertyType.DataType.INTEGER, 
								Priority.ENTITY_TYPE_CODE,
								true);  	
	
	private static final PropertyType PROP_PRIORITY_NAME = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_PRIORITY_NAME,
								HP_PRIORITY_NAME, // Description is hibernate property name
								PropertyType.DataType.STRING, 
								Priority.ENTITY_TYPE_CODE,
								true);  
	
	private static final PropertyType PROP_TYPE_RULE_ID = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_TYPE_RULE_ID,
								HP_TYPE_RULE_ID, // Description is hibernate property name
								PropertyType.DataType.INTEGER, 
								TypeRule.ENTITY_TYPE_CODE,
								true);  	
	
	private static final PropertyType PROP_TYPE_RULE_NAME = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_TYPE_RULE_NAME,
								HP_TYPE_RULE_NAME, // Description is hibernate property name
								PropertyType.DataType.STRING, 
								TypeRule.ENTITY_TYPE_CODE,
								true);  	
	
	private static final PropertyType PROP_SEVERITY_ID = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_SEVERITY_ID,
								HP_SEVERITY_ID, // Description is hibernate property name
								PropertyType.DataType.INTEGER, 
								Severity.ENTITY_TYPE_CODE,
								true);  	
	
	private static final PropertyType PROP_SEVERITY_NAME = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_SEVERITY_NAME,
								HP_SEVERITY_NAME, // Description is hibernate property name
								PropertyType.DataType.STRING, 
								Severity.ENTITY_TYPE_CODE,
								true);  

	private static final PropertyType PROP_STATE_CHANGED = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_STATE_CHANGED,
								HP_STATE_CHANGED, // Description is hibernate property name
								PropertyType.DataType.BOOLEAN, 
								null,
								true); 
	
	private static final PropertyType PROP_DEVICE_ID = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_DEVICE_ID,
								HP_DEVICE_ID, // Description is hibernate property name
								PropertyType.DataType.INTEGER, 
								Device.ENTITY_TYPE_CODE,
								true);  	
	
	private static final PropertyType PROP_DEVICE_IDENTIFICATION = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_DEVICE_IDENTIFICATION,
								HP_DEVICE_IDENTIFICATION, // Description is hibernate property name
								PropertyType.DataType.STRING, 
								Device.ENTITY_TYPE_CODE,
								true);  
	
	private static final PropertyType PROP_DEVICE_DISPLAY_NAME = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_DEVICE_DISPLAY_NAME,
								HP_DEVICE_DISPLAY_NAME, // Description is hibernate property name
								PropertyType.DataType.STRING, 
								Device.ENTITY_TYPE_CODE,
								true);  	
	
	private static final PropertyType PROP_HOST_STATUS_ID = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_HOST_STATUS_ID,
								HP_HOST_STATUS_ID, // Description is hibernate property name
								PropertyType.DataType.INTEGER, 
								null,
								true);  	
	
	private static final PropertyType PROP_HOST_STATUS_LAST_CHECK_TIME = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_HOST_STATUS_LAST_CHECK_TIME,
								HP_HOST_STATUS_LAST_CHECK_TIME, // Description is hibernate property name
								PropertyType.DataType.DATE, 
								null,
								true);  
	
	private static final PropertyType PROP_SERVICE_STATUS_ID = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_SERVICE_STATUS_ID,
								HP_SERVICE_STATUS_ID, // Description is hibernate property name
								PropertyType.DataType.INTEGER, 
								ServiceStatus.ENTITY_TYPE_CODE,
								true);  	
		
	private static final PropertyType PROP_SERVICE_STATUS_DESCRIPTION = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_SERVICE_STATUS_DESCRIPTION,
								HP_SERVICE_STATUS_DESCRIPTION, // Description is hibernate property name
								PropertyType.DataType.STRING, 
								ServiceStatus.ENTITY_TYPE_CODE,
								true);  
	
	private static final PropertyType PROP_OPERATION_STATUS_ID = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_OPERATION_STATUS_ID,
								HP_OPERATION_STATUS_ID, // Description is hibernate property name
								PropertyType.DataType.INTEGER, 
								OperationStatus.ENTITY_TYPE_CODE,
								true);  	

	private static final PropertyType PROP_OPERATION_STATUS_NAME = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_OPERATION_STATUS_NAME,
								HP_OPERATION_STATUS_NAME, // Description is hibernate property name
								PropertyType.DataType.STRING, 
								OperationStatus.ENTITY_TYPE_CODE,
								true);  

	/** Filter Only Properties - These properties are only available to filter on and not query */	
	private static final PropertyType PROP_HOST_GROUP_NAME = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_HOST_GROUP_NAME,
								HP_HOST_GROUP_NAME, // Description is hibernate property name
								PropertyType.DataType.STRING, 
								HostGroup.ENTITY_TYPE_CODE,
								true);  	
	
	private static final PropertyType PROP_HOST_GROUP_ID = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_HOST_GROUP_ID,
								HP_HOST_GROUP_ID, // Description is hibernate property name
								PropertyType.DataType.INTEGER, 
								HostGroup.ENTITY_TYPE_CODE,
								true);  	
	
	private static final PropertyType PROP_HOST_NAME = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_HOST_NAME,
								HP_HOST_NAME, // Description is hibernate property name
								PropertyType.DataType.STRING, 
								Host.ENTITY_TYPE_CODE,
								true);  
	
	private static final PropertyType PROP_CONSOLIDATIONHASH = 
		new com.groundwork.collage.model.impl.PropertyType(				
								EP_CONSOLIDATIONHASH,
								HP_CONSOLIDATIONHASH, // Description is hibernate property name
								PropertyType.DataType.INTEGER, 
								null,
								true);
		
    /** Built-In property list - For now its static - Once app type properties are supported we may have
     * to have an instance variable **/
	private static List<PropertyType> BUILT_IN_PROPERTIES = null;
	
	/* Hibernate component properties */
	private static List<PropertyType> COMPONENT_PROPERTIES = null;
	
    private Integer logMessageId;

    private Device        device;
    private HostStatus    hostStatus;
    private ServiceStatus serviceStatus;
    private OperationStatus operationStatus;
    
    private MonitorStatus	monitorStatus;
    private Severity		severity;
    private Severity 		applicationSeverity;
    private Component 		component;
    private Priority 		priority;
    private TypeRule 		typeRule;
    
    private Set     logPerformanceDatas;    

    private String  textMessage;
    private Date    firstInsertDate;
    private Date    lastInsertDate;
    private Date    reportDate;

    private Integer msgCount = new Integer(1);

    /* New fields in Foundation 1.5 */
    private Integer consolidationHash	= new Integer(0);
    private Integer statelessHash	= new Integer(0);
    private boolean stateChanged		= false;
    private Integer stateTransitionHash = null;


    /** plain-vanilla empty default constructor */
    public LogMessage() { }

    public String getEntityTypeCode() {
        return com.groundwork.collage.model.LogMessage.ENTITY_TYPE_CODE;
    }

	public PropertyValue getPropertyValueInstance (String name, Object value)
	{
		return new LogMessagePropertyValue(logMessageId, name, value);
	}
	
    public Integer getLogMessageId() {
        return this.logMessageId;
    }

    public void setLogMessageId(Integer logMessageId) 
    {
        this.logMessageId = logMessageId;
        
		if (propertyValues == null)
			return;		

		LogMessagePropertyValue propVal = null;
		Iterator it = propertyValues.iterator();
		while (it.hasNext())
		{
			propVal = (LogMessagePropertyValue)it.next();			
			propVal.setLogMessageId(logMessageId);			
		}		   
    }

    public Device getDevice() {
        return this.device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public HostStatus getHostStatus() {
        return hostStatus;
    }
    
    public void setHostStatus(HostStatus hostStatus) {
        this.hostStatus = hostStatus;
    }
    

    public ServiceStatus getServiceStatus() {
        return this.serviceStatus;
    }

    public void setServiceStatus(ServiceStatus serviceStatus) {
        this.serviceStatus = serviceStatus;
    }


/*    protected Integer getMonitorStatusId() {
        return this.monitorStatusId;
    }

    protected void setMonitorStatusId(Integer monitorStatusId) {
        this.monitorStatusId = monitorStatusId;
    }
 */  
    public MonitorStatus getMonitorStatus() {
        return this.monitorStatus;
    }

    public void setMonitorStatus(MonitorStatus monitorStatus) {
        this.monitorStatus = monitorStatus;
    }
    
    public String getTextMessage() {
        return this.textMessage;
    }

    public void setTextMessage(String textMessage) {
        this.textMessage = textMessage;
    }

    public Integer getMsgCount() {
        return this.msgCount;
    }

    public void setMsgCount(Integer msgCount) {
        this.msgCount = msgCount;
    }

    public Date getFirstInsertDate() {
        return this.firstInsertDate;
    }

    public void setFirstInsertDate(Date firstInsertDate) {
        this.firstInsertDate = firstInsertDate;
    }


    public Date getLastInsertDate() {
        return this.lastInsertDate;
    }

    public void setLastInsertDate(Date lastInsertDate) {
        this.lastInsertDate = lastInsertDate;
    }


    public Date getReportDate() {
        return this.reportDate;
    }

    public void setReportDate(Date reportDate) {
        this.reportDate = reportDate;
    }


    public Set getLogPerformanceDatas() {
        return this.logPerformanceDatas;
    }

    public void setLogPerformanceDatas(Set logPerformanceDatas) {
        this.logPerformanceDatas = logPerformanceDatas;
    }
    
/*    protected Integer getSeverityId() {
        return this.severityId;
    }

    protected void setSeverityId(Integer severityId) {
        this.severityId = severityId;
    }
*/
    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }    

    protected Integer getApplicationSeverityId() 
    {
    	if (this.applicationSeverity == null)
    		return null;
    	
        return this.applicationSeverity.getSeverityId();
    }

    protected void setApplicationSeverityId(Integer severityId) 
    {
    	if (severityId == null)
    		this.applicationSeverity = null;
    	else    		
    		this.applicationSeverity = getMetadata().getSeverityById(severityId.intValue());
    }
    
    public Severity getApplicationSeverity() 
    {
        return this.applicationSeverity;
    }

    public void setApplicationSeverity(Severity severity) 
    {
        this.applicationSeverity = severity;
    }
    
    protected Integer getPriorityId() 
    {
    	if (this.priority == null)
    		return null;
    	
        return this.priority.getPriorityId();
    }

    protected void setPriorityId(Integer priorityId) 
    {
    	if (priorityId == null)
    		this.priority = null;
    	else    		
    		this.priority = getMetadata().getPriorityById(priorityId.intValue());
    }

    public Priority getPriority() 
    {
        return this.priority;
    }

    public void setPriority(Priority priority) 
    {
        this.priority = priority;
    }
    
    protected Integer getTypeRuleId() 
    {
    	if (this.typeRule == null)
    		return null;
    	
        return this.typeRule.getTypeRuleId();
    }

    protected void setTypeRuleId(Integer typeRuleId) 
    {
    	if (typeRuleId == null)
    		this.typeRule = null;
    	else    		
    		this.typeRule = getMetadata().getTypeRuleById(typeRuleId.intValue());
    }

    public TypeRule getTypeRule() 
    {
        return this.typeRule;
    }

    public void setTypeRule(TypeRule typeRule) 
    {
        this.typeRule = typeRule;
    }
    
    protected Integer getComponentId() 
    {
    	if (this.component == null)
    		return null;
    	
        return this.component.getComponentId();
    }

    protected void setComponentId(Integer componentId) 
    {
    	if (componentId == null)
    		this.component = null;
    	else    		
    		this.component = getMetadata().getComponentById(componentId.intValue());
    }

    public Component getComponent() 
    {
        return this.component;
    }

    public void setComponent(Component component) 
    {
        this.component = component;
    }
    
    protected Integer getOperationStatusId() 
    {
    	if (operationStatus == null)
    		return null;
    	
        return this.operationStatus.getID();
    }

    public OperationStatus getOperationStatus() {
        return operationStatus;
    }

    public void setOperationStatus(OperationStatus operationStatus) {
        this.operationStatus = operationStatus;
    }
    
    /* New in Foundation 1.5 to speed up consolidation */
    public Integer getConsolidationHash(){
    	return this.consolidationHash;
    }
    
    public void setConsolidationHash(Integer newHashValue)
    {
    	this.consolidationHash =newHashValue;
    }
    
    public Integer getStatelessHash(){
    	return this.statelessHash;
    }
    
    public void setStatelessHash(Integer newHashValue)
    {
    	this.statelessHash =newHashValue;
    }
    
    public boolean getStateChanged()
    {
    	return this.stateChanged;
    }
    
	public void setStateChanged(boolean isStateChanged)
	{
		this.stateChanged = isStateChanged;
	}

    public Integer getStateTransitionHash(){
    	return this.stateTransitionHash;
    }
    
    public void setStateTransitionHash(Integer hashValue)
    {
    	this.stateTransitionHash = hashValue;
    }
    
    /** 
     * This method overrides the property by the same name in
     * PropertyExtensibleAbstract to make it possible to get the value of one
     * of the built-in property getters
     *
     * @throws IllegalArgumentException 
     *
     * if unable to find PropertyType with the key provided, or if the key
     * does not corresponding to one of the declared get/sets
     */
    public Object getProperty(String key) throws IllegalArgumentException
    {
    	if (key == null || key.length() == 0)
    		throw new IllegalArgumentException("Invalid null / empty key parameter.");
    
    	if (key.equalsIgnoreCase(EP_ID))
    	{
    		return this.getLogMessageId();
    	} else if (key.equals(EP_TEXT_MESSAGE)) {
            return this.getTextMessage();
        }
        else if (key.equals(EP_MSG_COUNT)) {
            return this.getMsgCount();
        }
        else if (key.equals(EP_FIRST_INSERT_DATE)) {
            return this.getFirstInsertDate();
        }
        else if (key.equals(EP_LAST_INSERT_DATE)) {
            return this.getLastInsertDate();
        }
        else if (key.equals(EP_REPORT_DATE)) {
            return this.getReportDate();
        }
        else if (key.equals(EP_APP_SEVERITY_ID)) 
        {
            return this.getApplicationSeverityId();
        }
        else if (key.equals(EP_APP_SEVERITY_NAME)) 
        {
        	Severity severity = this.getApplicationSeverity();
            return severity.getName();
        }		
        else if (key.equals(EP_COMPONENT_ID)) {
            return this.getComponentId();
        }
        else if (key.equals(EP_COMPONENT_NAME)) 
        {
        	Component component = this.getComponent();
        	if (component == null)
        		return null;
        	
        	return component.getName();
        }		
        else if (key.equals(EP_PRIORITY_ID)) {
            return this.getPriorityId();
        }
        else if (key.equals(EP_PRIORITY_NAME)) {
        	Priority priority = this.getPriority();
        	if (priority == null)
        		return null;
        	
        	return priority.getName();
        }    	
        else if (key.equals(EP_OPERATION_STATUS_ID)) {
            return this.getOperationStatusId();
        }
        else if (key.equals(EP_OPERATION_STATUS_NAME)) {
        	OperationStatus opStatus = this.getOperationStatus();
        	if (opStatus == null)
        		return null;
        	
        	return opStatus.getName();
        }    	
        else if (key.equals(EP_TYPE_RULE_ID)) {
            return this.getTypeRuleId();
        }
        else if (key.equals(EP_TYPE_RULE_NAME)) {
        	TypeRule typeRule = this.getTypeRule();
        	if (typeRule == null)
        		return null;
        	
        	return typeRule.getName();
        }    	
        else if (key.equals(EP_STATE_CHANGED)) {
            return this.getStateChanged();
        }
        else if (key.equals(EP_MONITOR_STATUS_ID)) {
        	MonitorStatus status = this.getMonitorStatus();
        	if (status == null)
        		return null;
        	
        	return status.getID();
        }
        else if (key.equals(EP_MONITOR_STATUS_NAME)) {
        	MonitorStatus status = this.getMonitorStatus();
        	if (status == null)
        		return null;
        	
        	return status.getName();
        }
        else if (key.equals(EP_SEVERITY_ID)) {
        	Severity severity = this.getSeverity();
        	if (severity == null)
        		return null;
        	
        	return severity.getID();
        }
        else if (key.equals(EP_SEVERITY_NAME)) {
        	Severity severity = this.getSeverity();
        	if (severity == null)
        		return null;
        	
        	return severity.getName();
        }
        else if (key.equals(EP_APPLICATION_TYPE_ID)) {
            return this.getApplicationTypeId();
        }   
        else if (key.equals(EP_APPLICATION_TYPE_NAME)) {
        	ApplicationType appType = this.getApplicationType();
        	if (appType == null)
        		return null;
        	
        	return appType.getName();
        }   
        else if (key.equals(EP_DEVICE_ID)) {
        	Device device = this.getDevice();
        	if (device == null)
        		return null;
        	
        	return device.getDeviceId();
        }   
        else if (key.equals(EP_DEVICE_IDENTIFICATION)) {
        	Device device = this.getDevice();
        	if (device == null)
        		return null;
        	
        	return device.getIdentification();
        }   
        else if (key.equals(EP_DEVICE_DISPLAY_NAME)) {
        	Device device = this.getDevice();
        	if (device == null)
        		return null;
        	
        	return device.getDisplayName();
        }       	
        else if (key.equals(EP_HOST_STATUS_ID)) {
        	HostStatus hostStatus = this.getHostStatus();
        	if (hostStatus == null)
        		return null;
        	
        	return hostStatus.getHostStatusId();
        }    
        else if (key.equals(EP_HOST_STATUS_LAST_CHECK_TIME)) {
        	HostStatus hostStatus = this.getHostStatus();
        	if (hostStatus == null)
        		return null;
        	
        	return hostStatus.getLastCheckTime();
        }   
        else if (key.equals(EP_SERVICE_STATUS_ID)) {
        	ServiceStatus status = this.getServiceStatus();
        	if (status == null)
        		return null;
        	
        	return status.getServiceStatusId();
        }   
        else if (key.equals(EP_SERVICE_STATUS_DESCRIPTION)) {
        	ServiceStatus status = this.getServiceStatus();
        	if (status == null)
        		return null;
        	
        	return status.getServiceDescription();
        }     	
        else if (key.equals(EP_HOST_NAME)) {
        	HostStatus hostStatus = this.getHostStatus();
        	if (hostStatus == null)
        		return null;
        	
        	return hostStatus.getHostName();
        }      	
        else {   	
            return super.getDynamicProperty(key);
        }
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
    		throw new IllegalArgumentException("Invalid null / empty key parameter.");
    	
        log.debug("attempting to set property: '" + key + "' with value " +
                ((value != null) ? value.getClass() + " - " + value : value));
        
    	// Read only properties
        if (key.equalsIgnoreCase(EP_ID))
            return;
        
    	if (key.equalsIgnoreCase(EP_TEXT_MESSAGE)) {
            this.setTextMessage((String)value);
        }
        else if (key.equalsIgnoreCase(EP_MSG_COUNT)) {
        	this.setMsgCount((Integer)value);
        }
        else if (key.equalsIgnoreCase(EP_FIRST_INSERT_DATE)) {
            if (value instanceof Date)
                this.setFirstInsertDate((Date)value);
            else if (value instanceof String)
                this.setFirstInsertDate(DateTime.parse((String)value));
        }
        else if (key.equalsIgnoreCase(EP_LAST_INSERT_DATE))
        {
            if (value instanceof Date)
                this.setLastInsertDate((Date)value);
            else if (value instanceof String)
                this.setLastInsertDate(DateTime.parse((String)value));
        }
        else if (key.equalsIgnoreCase(EP_REPORT_DATE)) {
            if (value instanceof Date)
                this.setReportDate((Date)value);
            else if (value instanceof String)
                this.setReportDate(DateTime.parse((String)value));
        }
        else if (key.equalsIgnoreCase(EP_APP_SEVERITY_ID)) 
        {
            this.setApplicationSeverityId((Integer)value);
        }
        else if (key.equalsIgnoreCase(EP_APP_SEVERITY_NAME)) 
        {
            this.setApplicationSeverity(getMetadata().getSeverityByName((String)value));
        }		
        else if (key.equalsIgnoreCase(EP_COMPONENT_ID)) 
        {
            this.setComponentId((Integer)value);
        }
        else if (key.equalsIgnoreCase(EP_COMPONENT_NAME)) 
        {
        	this.setComponent(getMetadata().getComponentByName((String)value));
        }		
        else if (key.equalsIgnoreCase(EP_PRIORITY_ID)) {
        	this.setPriorityId((Integer)value);
        }
        else if (key.equalsIgnoreCase(EP_PRIORITY_NAME)) {
        	this.setPriority(getMetadata().getPriorityByName((String)value));
        }    	
        else if (key.equalsIgnoreCase(EP_OPERATION_STATUS_ID)) {
            this.setOperationStatus(getMetadata().getOperationStatusById((Integer)value));
        }
        else if (key.equalsIgnoreCase(EP_OPERATION_STATUS_NAME)) {
        	this.setOperationStatus(getMetadata().getOperationStatusByName((String)value));        
        }    	
        else if (key.equalsIgnoreCase(EP_TYPE_RULE_ID)) {
            this.setTypeRuleId((Integer)value);
        }
        else if (key.equalsIgnoreCase(EP_TYPE_RULE_NAME)) {
        	this.setTypeRule(getMetadata().getTypeRuleByName(((String)value))); 
        }    	
        else if (key.equalsIgnoreCase(EP_STATE_CHANGED)) {
           this.setStateChanged(new Boolean((String)value));
        }
        else if (key.equalsIgnoreCase(EP_MONITOR_STATUS_ID)) 
        {
        	this.setMonitorStatus(getMetadata().getMonitorStatusById((Integer)value));
        }
        else if (key.equalsIgnoreCase(EP_MONITOR_STATUS_NAME)) {
        	this.setMonitorStatus(getMetadata().getMonitorStatusByName((String)value));
        }
        else if (key.equalsIgnoreCase(EP_SEVERITY_ID)) {
        	this.setSeverity(getMetadata().getSeverityById((Integer)value));
        }
        else if (key.equalsIgnoreCase(EP_SEVERITY_NAME)) {
        	this.setSeverity(getMetadata().getSeverityByName((String)value));
        }
        else if (key.equalsIgnoreCase(EP_APPLICATION_TYPE_ID)) {
            this.setApplicationType(getMetadata().getApplicationTypeById((Integer)value));
        }   
        else if (key.equalsIgnoreCase(EP_APPLICATION_TYPE_NAME)) 
        {
        	 this.setApplicationType(getMetadata().getApplicationTypeByName((String)value));
        }
        else if (key.equalsIgnoreCase(EP_HOST_STATUS_ID)) {
            this.setHostStatus((HostStatus)value);
        }
        else if (key.equalsIgnoreCase(EP_SERVICE_STATUS_ID)) {
            this.setServiceStatus((ServiceStatus)value);
        }
        else {
        	if (log.isInfoEnabled())
        		log.info("LogMessage. Set property [" + key + "] value [" + value + "]");
        		
            super.setDynamicProperty(key, value);
        }
    }

	public List<PropertyType> getBuiltInProperties()
	{
		if (BUILT_IN_PROPERTIES != null)
			return BUILT_IN_PROPERTIES;
			
		BUILT_IN_PROPERTIES = new ArrayList<PropertyType>(10);
		
		// Add non-volatile properties		
		BUILT_IN_PROPERTIES.add(PROP_ID);
		BUILT_IN_PROPERTIES.add(PROP_TEXT_MESSAGE);
		BUILT_IN_PROPERTIES.add(PROP_MSG_COUNT);		
		BUILT_IN_PROPERTIES.add(PROP_REPORT_DATE);
		BUILT_IN_PROPERTIES.add(PROP_FIRST_INSERT_DATE);
		BUILT_IN_PROPERTIES.add(PROP_LAST_INSERT_DATE);
		BUILT_IN_PROPERTIES.add(PROP_COMPONENT_ID);
		BUILT_IN_PROPERTIES.add(PROP_COMPONENT_NAME);
		BUILT_IN_PROPERTIES.add(PROP_PRIORITY_ID);
		BUILT_IN_PROPERTIES.add(PROP_PRIORITY_NAME);
		BUILT_IN_PROPERTIES.add(PROP_TYPE_RULE_ID);	
		BUILT_IN_PROPERTIES.add(PROP_TYPE_RULE_NAME);	
		BUILT_IN_PROPERTIES.add(PROP_STATE_CHANGED);	
		BUILT_IN_PROPERTIES.add(PROP_MONITOR_STATUS_ID);	
		BUILT_IN_PROPERTIES.add(PROP_MONITOR_STATUS_NAME);	
		BUILT_IN_PROPERTIES.add(PROP_SEVERITY_ID);	
		BUILT_IN_PROPERTIES.add(PROP_SEVERITY_NAME);	
		BUILT_IN_PROPERTIES.add(PROP_APPLICATION_TYPE_ID);	
		BUILT_IN_PROPERTIES.add(PROP_APPLICATION_TYPE_NAME);	
		BUILT_IN_PROPERTIES.add(PROP_DEVICE_ID);	
		BUILT_IN_PROPERTIES.add(PROP_DEVICE_IDENTIFICATION);	
		BUILT_IN_PROPERTIES.add(PROP_DEVICE_DISPLAY_NAME);		
		BUILT_IN_PROPERTIES.add(PROP_HOST_STATUS_ID);	
		BUILT_IN_PROPERTIES.add(PROP_HOST_STATUS_LAST_CHECK_TIME);			
		BUILT_IN_PROPERTIES.add(PROP_SERVICE_STATUS_ID);
		BUILT_IN_PROPERTIES.add(PROP_SERVICE_STATUS_DESCRIPTION);	
		BUILT_IN_PROPERTIES.add(PROP_OPERATION_STATUS_ID);
		BUILT_IN_PROPERTIES.add(PROP_OPERATION_STATUS_NAME);
		BUILT_IN_PROPERTIES.add(PROP_APP_SEVERITY_ID);
		BUILT_IN_PROPERTIES.add(PROP_APP_SEVERITY_NAME);
		
		return BUILT_IN_PROPERTIES;
	} 
	
	public List<PropertyType> getComponentProperties()
	{
		if (COMPONENT_PROPERTIES != null)
			return COMPONENT_PROPERTIES;
		
		COMPONENT_PROPERTIES = new ArrayList<PropertyType>(10);
		
		// Since log message has no "calculated" properties the component properties
		// are the a super-set of the built-in properties
		List<PropertyType> builtInProperties =  getBuiltInProperties();
		
		if (builtInProperties != null)
			COMPONENT_PROPERTIES.addAll(builtInProperties);
		
		/** Add Filter Only Properties **/
		COMPONENT_PROPERTIES.add(PROP_HOST_GROUP_NAME);
		COMPONENT_PROPERTIES.add(PROP_HOST_GROUP_ID);
		COMPONENT_PROPERTIES.add(PROP_HOST_NAME);
		COMPONENT_PROPERTIES.add(PROP_CONSOLIDATIONHASH);
		
		return COMPONENT_PROPERTIES;
	}  	

    public String toString()
    {
        return new ToStringBuilder(this).append("logMessageId",
                getLogMessageId()).toString();
    }    
}
