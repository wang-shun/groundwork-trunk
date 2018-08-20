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

import java.util.List;

public interface Action 
{
	/** Spring bean interface id */
	public static final String INTERFACE_NAME = "com.groundwork.collage.model.Action";
	
	/** Hibernate component name that this entity service is using */
	public static final String COMPONENT_NAME = "com.groundwork.collage.model.impl.Action";
	
	 /** Hibernate property constants */
    public static final String HP_NAME = "name";
    public static final String HP_APPLICATION_TYPE_NAME = "applicationTypes.name";
    
	public Integer getActionId();
	public void setActionId(Integer actionId);
	
	public String getName();	
	public void setName(String name);
	
	public String getDescription();	
	public void setDescription(String description);
	
	public ActionType getActionType();	
	public void setActionType(ActionType actionType);
	
	public List getActionProperties();	
	public void setActionProperties(List actionProperties);
	public ActionProperty getActionProperty(String name);
	
	public List getActionParameters();	
	public void setActionParameters(List actionParameters);
	public ActionParameter getActionParameter(String name);
	
	public List getApplicationTypes();	
	public void setApplicationTypes(List appTypes);
}
