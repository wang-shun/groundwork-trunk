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
 * HostStatus externalizes the state properties of a Host, and has a one-to-one
 * dependent relationship with a Host: 
 * <ul>
 *   <li>a Host may exist with a null HostStatus</li>
 *   <li>a HostStatus may not exist without a Host</li>
 *   <li>a Host deletion cascades to the HostStatus</li>
 *   <li>a Host and its HostStatus share the same numeric identifier</li>
 * </ul>
 *
 * @author <a href="mailto:dtaylor@itgroundwork.com">David Sean Taylor</a>
 * @author <a href="mailto:philippe.paravicini@eCommerceStudio.com">Philippe Paravicini</a>
 *
 * @version $Id: HostStatus.java 14640 2009-03-03 00:02:43Z ashanmugam $
 */

public interface HostStatus extends PropertyExtensible
{
    /** the name that identifies this entity in the system: "HOST_STATUS" */
    static final String ENTITY_TYPE_CODE = "HOST_STATUS";    
	static final String INTERFACE_NAME = "com.groundwork.collage.model.HostStatus";
	static final String COMPONENT_NAME = "com.groundwork.collage.model.impl.HostStatus";
	
	/* Built-In (Non-dynamic) Properties - The Properties which have a description (hibernate property)
	 * defined are properties that can be used to query the entity.
	 * We distinguish between "filterable" properties and properties that are returned in 
	 * property maps.
	 */
	static final String HP_ID = "hostStatusId";
	static final String HP_APPLICATION_TYPE_ID = "applicationType.applicationTypeId";
	static final String HP_APPLICATION_TYPE_NAME = "applicationType.name";
	static final String HP_LAST_CHECK_TIME = "lastCheckTime";
	static final String HP_MONITOR_STATUS_ID = "hostMonitorStatus.monitorStatusId";
	static final String HP_MONITOR_STATUS_NAME = "hostMonitorStatus.name";
	static final String HP_CHECK_TYPE_ID = "checkType.checkTypeId";
	static final String HP_CHECK_TYPE_NAME = "checkType.name";
	static final String HP_HOST_NAME = "host.hostName";
	static final String HP_STATE_TYPE_ID = "stateType.stateTypeId";
	static final String HP_STATE_TYPE_NAME = "stateType.name";
	static final String HP_NEXT_CHECK_TIME = "nextCheckTime";
	
	/** Entity Property Constants */
	static final String EP_ID = "HostStatusId";
	static final String EP_APPLICATION_TYPE_ID = "ApplicationTypeId";
	static final String EP_APPLICATION_TYPE_NAME = "ApplicationType";
	static final String EP_LAST_CHECK_TIME = "LastCheckTime";
	static final String EP_MONITOR_STATUS_ID = "MonitorStatusId";
	static final String EP_MONITOR_STATUS_NAME = "MonitorStatus";
	static final String EP_CHECK_TYPE_ID = "CheckTypeId";
	static final String EP_CHECK_TYPE_NAME = "CheckType";
	static final String EP_HOST_NAME = "Host";
	static final String EP_STATE_TYPE_ID = "StateTypeId";
	static final String EP_STATE_TYPE_NAME = "StateType";
	static final String EP_NEXT_CHECK_TIME = "NextCheckTime";

    
	/** 
	 * unique numeric identifier for this HostStatus, note that this will be
	 * the same identifier as for the parent Host 
	 */
    Integer getHostStatusId();

		/** the parent Host */
    Host getHost();
    void setHost(Host host);

    /** shortcut for this.getHost().getHostName() */
    String getHostName();

		/** the status of the Monitor monitoring this Host */
    MonitorStatus getHostMonitorStatus();
    void setHostMonitorStatus(MonitorStatus monitorStatus);
    
    /** Check Type status */
    CheckType getCheckType();
    void setCheckType(CheckType checkType);
    
		/** the last time at which the Host was checked by the Monitor */
    Date getLastCheckTime();
    void setLastCheckTime(Date lastCheckTime);
    
	/** the next time at which the Host will be checked by the Monitor */
    Date getNextCheckTime();
    void setNextCheckTime(Date nextCheckTime);
    
    StateType getStateType();
    void setStateType(StateType stateType);
    
    
}
