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

import java.util.Set;

/**
 * Host
 * 
 * @author <a href="mailto:dtaylor@itgroundwork.com">David Sean Taylor </a>
 * @version $Id: Host.java 16379 2009-08-27 03:15:23Z ashanmugam $
 */

public interface Host extends PropertyExtensible
{
	static final String ENTITY_TYPE_CODE = "HOST";
	
	/** Spring bean interface id */
	static final String INTERFACE_NAME = "com.groundwork.collage.model.Host";	
	
	/** Hibernate component name that this entity service using */
	static final String COMPONENT_NAME = "com.groundwork.collage.model.impl.Host";
	
	/* Built-In (Non-dynamic) Properties - The Properties which have a description (hibernate property)
	 * defined are properties that can be used to query the entity.
	 * We distinquish between "filterable" properties and properties that are returned in 
	 * property maps.
	 */
	static final String HP_ID = "hostId";
	static final String HP_NAME = "hostName";
	static final String HP_DESCRIPTION = "description";
	static final String HP_DEVICE_ID = "device.deviceId";
    static final String HP_AGENT_ID = "agentId";
	static final String HP_DEVICE_IDENTIFICATION = "device.identification";
	static final String HP_APPLICATION_TYPE_ID = "applicationType.applicationTypeId";
	static final String HP_APPLICATION_TYPE_NAME = "applicationType.name";
	static final String HP_LAST_CHECK_TIME = "hostStatus.lastCheckTime";
	static final String HP_MONITOR_STATUS_ID = "hostStatus.monitorStatus.monitorStatusId";
	static final String HP_MONITOR_STATUS_NAME = "hostStatus.monitorStatus.name";
    
	/** Filter-Only Properties */
	static final String HP_HOSTGROUP_ID = "hostGroups.hostGroupId";
	static final String HP_HOSTGROUP_NAME = "hostGroups.name";
	static final String HP_MONITORSERVER_ID = "device.monitorServers.monitorServerId";
	static final String HP_MONITORSERVER_NAME = "device.monitorServers.monitorServerName";	
	static final String HP_SERVICE_ID = "serviceStatuses.serviceStatusId";
	static final String HP_SERVICE_NAME = "serviceStatuses.serviceDescription";

	/** Entity Property Constants */
	static final String EP_ID = "HostId";
	static final String EP_NAME = "Host";
	static final String EP_DESCRIPTION = "Description";
	static final String EP_DEVICE_ID = "DeviceId";
	static final String EP_DEVICE_IDENTIFICATION = "Device";
	static final String EP_APPLICATION_TYPE_ID = "ApplicationTypeId";
	static final String EP_APPLICATION_TYPE_NAME = "ApplicationType";
	static final String EP_LAST_CHECK_TIME = "LastCheckTime";
	static final String EP_MONITOR_STATUS_ID = "MonitorStatusId";
	static final String EP_MONITOR_STATUS_NAME = "MonitorStatus";
	static final String EP_SERVICE_COUNT = "ServiceCount";
	static final String EP_HOSTGROUP_ID = "HostGroupId";
	static final String EP_HOSTGROUP_NAME = "HostGroup";
	static final String EP_MONITORSERVER_ID = "MonitorServerId";
	static final String EP_MONITORSERVER_NAME = "MonitorServer";
	static final String EP_SERVICE_ID = "ServiceId";
	static final String EP_SERVICE_NAME = "ServiceDescription";
    static final String EP_AGENT_ID = "AgentId";

    Integer getHostId();

    String getHostName();
    
    void setHostName(String hostName);
    
    String getDescription();
    
    void setDescription(String description);

    Device getDevice();

    void setDevice(Device device);

    Set getHostGroups();

    HostStatus getHostStatus();

    void setHostStatus(HostStatus status);

    Set getServiceStatuses();

    ServiceStatus getServiceStatus(String description);

    Set getComments();

    void removeComment(Comment comment);

    void addComment(Comment comment);

    Integer getApplicationTypeId();

    ApplicationType getApplicationType();    
    
    void setApplicationType(ApplicationType applicationType);
    
    String getLastMonitorStatus();
	void setLastMonitorStatus(String lastMonitorStatus);

    String getAgentId();
    void setAgentId(String agentId);

}
