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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.groundwork.collage.model.ActionParameter;
import com.groundwork.collage.model.ActionProperty;
import com.groundwork.collage.model.ActionType;

public class Action implements com.groundwork.collage.model.Action 
{
	private Integer actionId = null;
	private String name = null;
	private String description = null;
	private ActionType actionType = null;

	private List actionProperties = null;
	private List actionParameters = null;
	private List applicationTypes = null;
	
	public Integer getActionId() 
	{
		return actionId;
	}
	
	public void setActionId(Integer actionId) {
		this.actionId = actionId;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public ActionType getActionType() {
		return actionType;
	}
	
	public void setActionType(ActionType actionType) {
		this.actionType = actionType;
	}
	
	public List getActionProperties() 
	{
		if (actionProperties == null)
			return new ArrayList();
		
		return actionProperties;
	}
	
	public ActionProperty getActionProperty(String name)
	{
		if (name == null || name.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty action property name parameter.");
		
		if (this.actionProperties == null || this.actionProperties.size() == 0)
			return null;
		
		ActionProperty actionProperty = null;
		Iterator<ActionProperty> it = this.actionProperties.iterator();
		while (it.hasNext())
		{
			actionProperty = it.next();
			
			if (actionProperty.getName().equalsIgnoreCase(name))
				return actionProperty;				
		}
		
		return null;
	}
	
	public void setActionProperties(List actionProperties) {
		this.actionProperties = actionProperties;
	}
	
	public List getActionParameters() 
	{
		if (actionParameters == null)
			return new ArrayList();
		
		return actionParameters;
	}
	
	public ActionParameter getActionParameter(String name)
	{
		if (name == null || name.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty action parameter name parameter.");
		
		if (this.actionParameters == null || this.actionParameters.size() == 0)
			return null;
		
		ActionParameter actionParameter = null;
		Iterator<ActionParameter> it = this.actionParameters.iterator();
		while (it.hasNext())
		{
			actionParameter = it.next();
			
			if (actionParameter.getName().equalsIgnoreCase(name))
				return actionParameter;				
		}
		
		return null;
	}
	
	public void setActionParameters(List actionParameters) {
		this.actionParameters = actionParameters;
	}
	
    /**
     * @return applications that this action is related
     */
    public List getApplicationTypes()
    {
    	if (this.applicationTypes == null)
    		this.applicationTypes = new ArrayList();
    	
        return this.applicationTypes;        
    }

    /**
     * @param appTypes The application types to set.
     */
    public void setApplicationTypes(List appTypes) {
        this.applicationTypes = appTypes;
    }
    
    public String toString()
    {
        return new ToStringBuilder(this)
                .append("actionId", getActionId()).toString();
    }

    /** 
     * two Action objects are equal if they have the same name, 
     * compared in a case-insensitive way
     */
    public boolean equals(Object other) {
        if ( (this == other ) ) return true;
        if ( !(other instanceof Action) ) return false;
        Action castOther = (Action) other;

        return new EqualsBuilder()
            .append(this.getName().toLowerCase(), castOther.getName().toLowerCase())
            .isEquals();
    }

    public int hashCode() 
    {
        return new HashCodeBuilder()
            .append(getName().toLowerCase())
            .toHashCode();
    }	
}

