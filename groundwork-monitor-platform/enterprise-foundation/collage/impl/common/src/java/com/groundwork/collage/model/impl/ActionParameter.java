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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.groundwork.collage.model.Action;

public class ActionParameter implements com.groundwork.collage.model.ActionParameter 
{
	private Integer actionParameterId = null;
	private String name = null;
	private String value = null;
	private Action action = null;	
		
    public Integer getActionParameterId() {
		return actionParameterId;
	}

	public void setActionParameterId(Integer actionParameterId) {
		this.actionParameterId = actionParameterId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public String toString()
    {
        return new ToStringBuilder(this)
                .append("actionParameterId", getActionParameterId()).toString();
    }

    /** 
     * two ActionParameter objects are equal if they have the same action and name, 
     * compared in a case-insensitive way
     */
    public boolean equals(Object other) {
        if ( (this == other ) ) return true;
        if ( !(other instanceof Action) ) return false;
        ActionParameter castOther = (ActionParameter) other;

        return new EqualsBuilder()
            .append(this.getName().toLowerCase(), castOther.getName().toLowerCase())
            .append(this.getAction().getActionId(), castOther.getAction().getActionId())
            .isEquals();
    }

    public int hashCode() 
    {
        return new HashCodeBuilder()
            .append(getName().toLowerCase())
            .append(this.getAction().getActionId())
            .toHashCode();
    }		

}
