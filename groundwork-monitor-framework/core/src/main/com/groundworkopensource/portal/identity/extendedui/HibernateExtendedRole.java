/*
 * 
 * Copyright 2007 GroundWork Open Source, Inc. ("GroundWork") All rights
 * reserved. This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package com.groundworkopensource.portal.identity.extendedui;


/**
 * @author Arul Shanmugam
 * @version $Revision$
 */
public class HibernateExtendedRole  {
	
	private Long id;
	
	private String name = null;

	/** The Dashboardlinks disable/enable */
	private Boolean dashboardLinksDisabled = false;
	
	/** HGList to be shown */
	private String hgList = null;
	
	private String sgList = null;	
	
	private String defaultHostGroup = null;
	
	private String defaultServiceGroup = null;
	
	private String restrictionType = null;
	
	private Boolean actionsEnabled = false;
	

	public HibernateExtendedRole() {		
	}
	
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean isDashboardLinksDisabled() {
		return this.dashboardLinksDisabled;
	}

	public void setDashboardLinksDisabled(Boolean dashboardLinksDisabled) {
		this.dashboardLinksDisabled = dashboardLinksDisabled;
	}
	
	public String getHgList() {
		return this.hgList;
	}

	public void setHgList(String hgList) {
		this.hgList = hgList;
	}
	
	public String getSgList() {
		return this.sgList;
	}

	public void setSgList(String sgList) {
		this.sgList = sgList;
	}
	
	
	
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDefaultHostGroup() {
		return defaultHostGroup;
	}

	public void setDefaultHostGroup(String defaultHostGroup) {
		this.defaultHostGroup = defaultHostGroup;
	}

	public String getDefaultServiceGroup() {
		return defaultServiceGroup;
	}

	public void setDefaultServiceGroup(String defaultServiceGroup) {
		this.defaultServiceGroup = defaultServiceGroup;
	}

	public String getRestrictionType() {
		return restrictionType;
	}

	public void setRestrictionType(String restrictionType) {
		this.restrictionType = restrictionType;
	}
	
	public Boolean isActionsEnabled() {
		return this.actionsEnabled;
	}

	public void setActionsEnabled(Boolean actionsEnabled) {
		this.actionsEnabled = actionsEnabled;
	}
}
