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

/**
 * 
 * MonitorStatus
 * 
 * @author <a href="mailto:rruttimann@itgroundwork.com"> Roger Ruttimann </a>
 * @version $Id: MonitorStatus.java 7205 2007-07-05 20:15:48Z rruttimann $
 */
public interface MonitorStatus extends AttributeData
{
    /** the name that identifies this entity in the system: "MONITOR_STATUS" */
    static final String ENTITY_TYPE_CODE = "MONITOR_STATUS";
        
	/** Spring bean interface id */
	static final String INTERFACE_NAME = "com.groundwork.collage.model.MonitorStatus";
	
	/** Hibernate component name that this entity service using */
	static final String COMPONENT_NAME = "com.groundwork.collage.model.impl.MonitorStatus";
	
	/* Built-In (Non-dynamic) Properties - The Properties which have a description (hibernate property)
	 * defined are properties that can be used to query the entity.
	 * We distinquish between "filterable" properties and properties that are returned in 
	 * property maps.
	 */
	static final String HP_ID = "monitorStatusId";
	static final String HP_NAME = "name";
	static final String HP_DESCRIPTION = "description";

	/** Entity Property Constants */
	static final String EP_ID = "MonitorStatusId";
	static final String EP_NAME = "MonitorStatus";
	static final String EP_DESCRIPTION = "Description";
	
    Integer getMonitorStatusId();
}