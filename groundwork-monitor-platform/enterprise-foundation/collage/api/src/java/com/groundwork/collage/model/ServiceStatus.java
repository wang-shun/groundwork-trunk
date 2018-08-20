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
import java.util.Set;

/**
 * Represents the status of a Service (for example, an http server) on a
 * logical Host (for example, www.some-domain.com)
 * 
 * @author <a href="mailto:rruttimann@itgroundwork.com"> Roger Ruttimann </a>
 * @author <a href="mailto:philippe.paravicini@eCommerceStudio.com">Philippe Paravicini</a>
 *
 * @version $Id: ServiceStatus.java 16379 2009-08-27 03:15:23Z ashanmugam $
 */
public interface ServiceStatus extends PropertyExtensible
{
	/** the name that identifies this entity in the system: "SERVICE_STATUS" */
	static final String ENTITY_TYPE_CODE = "SERVICE_STATUS";
	
	/** Spring bean interface id */
	static final String INTERFACE_NAME = "com.groundwork.collage.model.ServiceStatus";
	
	/** Hibernate component name that this entity service using */
	static final String COMPONENT_NAME = "com.groundwork.collage.model.impl.ServiceStatus";
	
	/** Hibernate Property Constants */
	static final String HP_ID = "serviceStatusId";
	static final String HP_SERVICE_DESCRIPTION = "serviceDescription";
	static final String HP_MONITOR_STATUS_ID = "monitorStatus.monitorStatusId";
	static final String HP_MONITOR_STATUS_NAME = "monitorStatus.name";
	static final String HP_LAST_CHECK_TIME = "lastCheckTime";	
	static final String HP_NEXT_CHECK_TIME = "nextCheckTime";
	static final String HP_LAST_STATE_CHANGE = "lastStateChange";
	static final String HP_LAST_HARD_STATE_ID = "lastHardState.monitorStatusId";
	static final String HP_LAST_HARD_STATE_NAME = "lastHardState.name";
	static final String HP_CHECK_TYPE_ID = "checkType.checkTypeId";
	static final String HP_CHECK_TYPE_NAME = "checkType.name";
	static final String HP_STATE_TYPE_ID = "stateType.stateTypeId";
	static final String HP_STATE_TYPE_NAME = "stateType.name";
	static final String HP_DOMAIN = "domain";
	static final String HP_METRIC_TYPE = "metricType";
	static final String HP_APPLICATION_TYPE_ID = "applicationType.applicationTypeId";
	static final String HP_APPLICATION_TYPE_NAME = "applicationType.name";
	static final String HP_HOST_ID = "host.hostId";
	static final String HP_HOST_NAME = "host.hostName";
    static final String HP_APPLICATION_HOST_NAME = "applicationHostName";
	static final String HP_AGENT_ID = "agentId";

	/** Entity Property Constants */
	static final String EP_ID = "ServiceStatusId";
	static final String EP_SERVICE_DESCRIPTION = "ServiceDescription";
	static final String EP_MONITOR_STATUS_ID = "MonitorStatusId";
	static final String EP_MONITOR_STATUS_NAME = "MonitorStatus";
	static final String EP_LAST_CHECK_TIME = "LastCheckTime";
	static final String EP_NEXT_CHECK_TIME = "NextCheckTime";
	static final String EP_LAST_STATE_CHANGE = "LastStateChange";
	static final String EP_LAST_HARD_STATE_ID = "LastHardStateId";
	static final String EP_LAST_HARD_STATE_NAME = "LastHardState";
	static final String EP_CHECK_TYPE_ID = "CheckTypeId";
	static final String EP_CHECK_TYPE_NAME = "CheckType";
	static final String EP_STATE_TYPE_ID = "StateTypeId";
	static final String EP_STATE_TYPE_NAME = "StateType";
	static final String EP_DOMAIN = "Domain";
	static final String EP_METRIC_TYPE = "MetricType";
	static final String EP_APPLICATION_TYPE_ID = "ApplicationTypeId";
	static final String EP_APPLICATION_TYPE_NAME = "ApplicationType";
	static final String EP_HOST_ID = "HostId";
	static final String EP_HOST_NAME = "HostName";
    static final String EP_APPLICATION_HOST_NAME = "ApplicationHostName";
	static final String EP_AGENT_ID = "AgentId";

    /** Transient Property Constants */
    static final String TP_IS_MONITORED = "isMonitored";
    static final String TP_IS_GRAPHED = "isGraphed";

	Integer getServiceStatusId();

	String getServiceDescription();
	void setServiceDescription(String serviceDescription);

	Host getHost();
	void setHost(Host host);

	Set getComments();

	void removeComment(Comment comment);

	void addComment(Comment comment);

	MonitorStatus getMonitorStatus();
	void setMonitorStatus(MonitorStatus monitorStatus);
	
	Date getLastCheckTime();
	void setLastCheckTime(Date lastCheckTime);

	Date getNextCheckTime();
	void setNextCheckTime(Date nextCheckTime);

	Date getLastStateChange();
	void setLastStateChange(Date lastStateChange);

	StateType getStateType();
	void setStateType(StateType stateType);

	MonitorStatus getLastHardState();
	void setLastHardState(MonitorStatus monitorStatus);

	CheckType getCheckType();
	void setCheckType(CheckType checkType);

    String getAgentId();
    public void setAgentId(String agentId);

    String getDomain();
	void setDomain(String domain);
	
	String getMetricType();
	void setMetricType(String metricType);
	
	// No mapping required for this field as it is used by the AOP
	String getLastMonitorStatus();
	void setLastMonitorStatus(String lastMonitorStatus);

    String getApplicationHostName();
    void setApplicationHostName(String applicationHostName);
}
