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
 * Device
 * 
 * @author <a href="mailto:dtaylor@itgroundwork.com">David Sean Taylor</a>
 * @version $Id: Device.java 7205 2007-07-05 20:15:48Z rruttimann $
 */

public interface Device extends PropertyExtensible
{
    /** the name that identifies this entity in the system: "DEVICE" */
    static final String ENTITY_TYPE_CODE = "DEVICE";
    
	/** Spring bean interface id */
	static final String INTERFACE_NAME = "com.groundwork.collage.model.Device";
	
	/** Hibernate component name that this entity service using */
	static final String COMPONENT_NAME = "com.groundwork.collage.model.impl.Device";
	
	/** Hibernate Property Constants */
	static final String HP_ID = "deviceId";
	static final String HP_DISPLAY_NAME = "displayName";	
	static final String HP_DESCRIPTION = "description";
	static final String HP_IDENTIFICATION = "identification";
	
	/** Filter Only Hibernate Properties */
	static final String HP_HOST_ID = "hosts.hostId";	
	static final String HP_HOST_NAME = "hosts.hostName";
	static final String HP_MONITOR_SERVER_ID = "monitorServers.monitorServerId";
		
	/** Entity Property Name Constants */
	static final String EP_ID = "DeviceId";
	static final String EP_DISPLAY_NAME = "DisplayName";
	static final String EP_DESCRIPTION = "Description";
	static final String EP_IDENTIFICATION = "Device";	
	static final String EP_HOST_ID = "HostId";
	static final String EP_HOST_NAME = "HostName";
	static final String EP_MONITOR_SERVER_ID = "MonitorServerId";
	
    Integer getDeviceId();

    String getDisplayName();

    void setDisplayName(String displayName);

    String getIdentification();

    void setIdentification(String identification);

    String getDescription();

    void setDescription(String description);

    Set getHosts();

    Set getParents();

    /**
     * @return the parent Device with the given Identification, or null if no parent
     *   with that Ident can be found
     */
    Device getParent(String ident);
    
    void addParent(Device parentDevice);
    
    void removeParent(Device parentDevice);

    Set getChildren();

    /**
     * @return the child Device with the given Identification, or null if no child
     *   with that Ident can be found
     */
    Device getChild(String ident);

		void addChild(Device childDevice);
    
    void removeChild(Device childDevice);

    Set getMonitorServers();
    
    boolean addMonitorServer(MonitorServer monitorServer);   
    
    boolean removeMonitorServer(MonitorServer monitorServer);
 }
