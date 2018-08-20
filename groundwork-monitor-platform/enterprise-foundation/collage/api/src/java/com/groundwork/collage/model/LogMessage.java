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

package com.groundwork.collage.model;

import java.util.Date;

/**
 * LogMessage
 * 
 * @author <a href="mailto:dtaylor@itgroundwork.com">David Sean Taylor</a>
 * @author <a href="mailto:philippe.paravicini@eCommerceStudio.com">Philippe Paravicini</a>
 * @version $Id: LogMessage.java 17917 2010-08-25 23:00:48Z ashanmugam $
 */

public interface LogMessage extends PropertyExtensible
{
    /** the name that identifies this entity in the system: "LOG_MESSAGE" */
    static final String ENTITY_TYPE_CODE = "LOG_MESSAGE";
        
	/** Spring bean interface id */
	static final String INTERFACE_NAME = "com.groundwork.collage.model.LogMessage";
	
	/** Hibernate component name that this entity service using */
	static final String COMPONENT_NAME = "com.groundwork.collage.model.impl.LogMessage";
	
	/* Built-In (Non-dynamic) Properties - The Properties which have a description (hibernate property)
	 * defined are properties that can be used to query the entity.
	 * We distinquish between "filterable" properties and properties that are returned in 
	 * property maps.
	 */
	static final String HP_ID = "logMessageId";
	static final String HP_TEXT_MESSAGE = "textMessage";
	static final String HP_MSG_COUNT = "msgCount";
	static final String HP_FIRST_INSERT_DATE = "firstInsertDate";
	static final String HP_LAST_INSERT_DATE = "lastInsertDate";
	static final String HP_REPORT_DATE = "reportDate";
	static final String HP_APPLICATION_TYPE_ID = "applicationType.applicationTypeId";
	static final String HP_APPLICATION_TYPE_NAME = "applicationType.name";
	static final String HP_MONITOR_STATUS_ID = "monitorStatus.monitorStatusId";
	static final String HP_MONITOR_STATUS_NAME = "monitorStatus.name";
	static final String HP_APP_SEVERITY_ID = "applicationSeverityId.severityId";	
	static final String HP_APP_SEVERITY_NAME = "applicationSeverity.name";
	static final String HP_COMPONENT_ID = "component.componentId";
	static final String HP_COMPONENT_NAME = "component.name";
	static final String HP_PRIORITY_ID = "priority.priorityId";
	static final String HP_PRIORITY_NAME = "priority.name";
	static final String HP_TYPE_RULE_ID = "typeRule.typeRuleId"; 
	static final String HP_TYPE_RULE_NAME = "typeRule.name";
	static final String HP_SEVERITY_ID = "severity.severityId";
	static final String HP_SEVERITY_NAME = "severity.name";
	static final String HP_STATE_CHANGED = "stateChanged";
	static final String HP_DEVICE_ID = "device.deviceId";
	static final String HP_DEVICE_IDENTIFICATION = "device.identification";
	static final String HP_DEVICE_DISPLAY_NAME = "device.displayName";
	static final String HP_HOST_STATUS_ID = "hostStatus.hostStatusId";
	static final String HP_HOST_STATUS_LAST_CHECK_TIME = "hostStatus.lastCheckTime";
	static final String HP_SERVICE_STATUS_ID = "serviceStatus.serviceStatusId";
	static final String HP_SERVICE_STATUS_DESCRIPTION = "serviceStatus.serviceDescription";
	static final String HP_OPERATION_STATUS_ID = "operationStatus.operationStatusId";
	static final String HP_OPERATION_STATUS_NAME = "operationStatus.name";

	/** Filter Only Properties - These properties are only available to filter on and not query */	
	static final String HP_HOST_GROUP_NAME = "hostStatus.host.hostGroups.name";
	static final String HP_HOST_GROUP_ID = "hostStatus.host.hostGroups.hostGroupId";
	static final String HP_HOST_NAME = "device.hosts.hostName";
	static final String HP_CONSOLIDATIONHASH = "consolidationHash";
	static final String HP_STATELESSHASH = "statelessHash";
	static final String HP_STATETRANSITIONHASH = "stateTransitionHash";

	/** Entity Property Constants */
	static final String EP_ID = "LogMessageId";
	static final String EP_TEXT_MESSAGE = "TextMessage";
	static final String EP_MSG_COUNT = "MessageCount";
	static final String EP_FIRST_INSERT_DATE = "FirstInsertDate";
	static final String EP_LAST_INSERT_DATE = "LastInsertDate";
	static final String EP_REPORT_DATE = "ReportDate";
	static final String EP_APPLICATION_TYPE_ID = "ApplicationTypeId";
	static final String EP_APPLICATION_TYPE_NAME = "ApplicationType";
	static final String EP_MONITOR_STATUS_ID = "MonitorStatusId";
	static final String EP_MONITOR_STATUS_NAME = "MonitorStatus";
	static final String EP_APP_SEVERITY_ID = "ApplicationSeverityId";	
	static final String EP_APP_SEVERITY_NAME = "ApplicationSeverity";
	static final String EP_COMPONENT_ID = "ComponentId";
	static final String EP_COMPONENT_NAME = "Component";
	static final String EP_PRIORITY_ID = "PriorityId";
	static final String EP_PRIORITY_NAME = "Priority";
	static final String EP_TYPE_RULE_ID = "TypeRuleId";
	static final String EP_TYPE_RULE_NAME = "TypeRule";
	static final String EP_SEVERITY_ID = "SeverityId";	
	static final String EP_SEVERITY_NAME = "Severity";
	static final String EP_STATE_CHANGED = "StateChanged";
	static final String EP_DEVICE_ID = "DeviceId";
	static final String EP_DEVICE_IDENTIFICATION = "Device";
	static final String EP_DEVICE_DISPLAY_NAME = "DeviceDisplayName";
	static final String EP_HOST_STATUS_ID = "HostStatusId";
	static final String EP_HOST_STATUS_LAST_CHECK_TIME = "LastCheckTime";
	static final String EP_SERVICE_STATUS_ID = "ServiceStatusId";	
	static final String EP_SERVICE_STATUS_DESCRIPTION = "ServiceDescription";
	static final String EP_OPERATION_STATUS_ID = "OperationStatusId";
	static final String EP_OPERATION_STATUS_NAME = "OperationStatus";
	static final String EP_HOST_GROUP_NAME = "HostGroup";
	static final String EP_HOST_GROUP_ID = "HostGroupId";
	static final String EP_HOST_NAME = "Host";
	static final String EP_CONSOLIDATIONHASH = "ConsolidationHash";

    /** 
     * key which needs to be defined if consolidation needs to be enabled
     * for the incoming message. If not defined create a new logMessage otherwise
     * apply the criteria defined for the value defined for the Consolidation.
     * We expect the consolidation value when the
     * values of a LogMessage are represented as a map: "consolidation"
     */
    public static final String KEY_CONSOLIDATION = "consolidation";
    
    /**
     * key which needs to be defined in order to consolidate on monitor server name
     */
    public static final String KEY_MONITOR_SERVER = "MonitorServer";
    
    /**
     * Event pre process switch
     */
    public static final String EVENT_PRE_PROCESS_SWITCH= "event.pre.process.enabled";
    /**
     * Event pre process opstatus
     */
    public static final String EVENT_PRE_PROCESS_DEST_STATE= "event.pre.process.opstatus";

	/**
	 * If configured, this represents the number of hours before an event is not fetched by default unless
	 * a specific time window is provided
	 */
	String EVENT_MAX_QUERY_AGE_HOURS = "event.maxQueryAgeHours";

    Integer getLogMessageId();

    Device getDevice();
    void setDevice(Device device);

    HostStatus getHostStatus();
    void setHostStatus(HostStatus hostStatus);

    ServiceStatus getServiceStatus();
    void setServiceStatus(ServiceStatus serviceStatus);

    MonitorStatus getMonitorStatus();
    void setMonitorStatus(MonitorStatus monitorStatus);

    String getTextMessage();
    void setTextMessage(String textMessage);

    Integer getMsgCount();
    void setMsgCount(Integer msgCount);

    Date getFirstInsertDate();
    void setFirstInsertDate(Date firstInsertDate);

    Date getLastInsertDate();
    void setLastInsertDate(Date lastInsertDate);

    Date getReportDate();
    void setReportDate(Date reportDate);

    Severity getSeverity();
    void setSeverity(Severity severity);

    Severity getApplicationSeverity();
    void setApplicationSeverity(Severity applicationSeverity);

    Component getComponent();
    void setComponent(Component component);

    Priority getPriority();
    void setPriority(Priority priority);

    OperationStatus getOperationStatus();
    void setOperationStatus(OperationStatus operationStatus);

    TypeRule getTypeRule();
    void setTypeRule(TypeRule typeRule);
    
    /* New in Foundation 1.5 to speed up consolidation */
    Integer getConsolidationHash();
    void setConsolidationHash(Integer newHashValue);
    
    Integer getStatelessHash();
    void setStatelessHash(Integer newHashValue);
    
    boolean getStateChanged();
	void setStateChanged(boolean isStateChanged);

	Integer getStateTransitionHash();
	void setStateTransitionHash(Integer hashValue);
	
    /*
    String getApplicationName();
    void setApplicationName(String applicationName);

    String getApplicationCode();
    void setApplicationCode(String applicationCode);

    String getLoggerName();
    void setLoggerName(String loggerName);

    String getErrorType();
    void setErrorType(String errorType);

    String getSubComponent();
    void setSubComponent(String subComponent);
    */


}
