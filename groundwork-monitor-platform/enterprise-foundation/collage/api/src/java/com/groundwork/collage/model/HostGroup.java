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
 * HostGroup
 * 
 * @author <a href="mailto:dtaylor@itgroundwork.com">David Sean Taylor</a>
 * @version $Id: HostGroup.java 14739 2009-03-18 04:37:06Z rruttimann $
 */

public interface HostGroup extends PropertyExtensible
{
	static final String ENTITY_TYPE_CODE = "HOSTGROUP";
    
	/** Spring bean interface id */
	static final String INTERFACE_NAME = "com.groundwork.collage.model.HostGroup";
	
	/** Hibernate component name that this entity service using */
	static final String COMPONENT_NAME = "com.groundwork.collage.model.impl.HostGroup";
	
	/** Hibernate Property Constants */
	static final String HP_ID = "hostGroupId"; 
	static final String HP_NAME = "name";
	static final String HP_DESCRIPTION = "description";
	static final String HP_APPLICATION_TYPE_ID = "applicationType.applicationTypeId";
	static final String HP_APPLICATION_TYPE_NAME = "applicationType.name";
	static final String HP_ALIAS = "alias";
	static final String HP_AGENT_ID = "agentId";

	/** Entity Property Constants */
	static final String EP_ID = "HostGroupId";
	static final String EP_NAME = "HostGroup";
	static final String EP_DESCRIPTION = "Description";
	static final String EP_APPLICATION_TYPE_ID = "ApplicationTypeId";
	static final String EP_APPLICATION_TYPE_NAME = "ApplicationType";
	static final String EP_SERVICE_COUNT = "ServiceCount";
	static final String EP_HOST_COUNT = "HostCount";
	static final String EP_ALIAS = "Alias";
	static final String EP_AGENT_ID = "AgentId";

    Integer getHostGroupId();
    
    Integer getApplicationTypeId();
    
    ApplicationType getApplicationType();

    String getName();

    void setName(String name);

    String getDescription();

    void setDescription(String description);

    void addHost(Host host);
    
    void removeHost(Host host);
        
    Set getHosts();
    
    String getAlias();

    void setAlias(String alias);
        
    void setApplicationType(ApplicationType applicationType);

    String getAgentId();

    void setAgentId(String agentId);
}