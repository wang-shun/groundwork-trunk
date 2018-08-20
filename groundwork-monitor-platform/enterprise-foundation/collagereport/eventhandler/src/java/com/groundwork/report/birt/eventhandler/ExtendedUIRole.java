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
package com.groundwork.report.birt.eventhandler;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author Arul Shanmugam
 * @version $Revision$
 */
@XmlRootElement(name = "ExtendedRole")
public class ExtendedUIRole implements Serializable {

	public static final String RESTRICTION_TYPE_NONE = "N";
	public static final String RESTRICTION_TYPE_PARTIAL = "P";
	
	/** The role id */
	private Long id;

	/** The Dashboardlinks disable/enable */
	private boolean dashboardLinksDisabled = false;

	/** HGList to be shown */
	private String hgList=null;

	private   String sgList = null;
	
	private String restrictionType = "N"; // Default to no restriction
	
	private String defaultHostGroup = null;
	
	private String defaultServiceGroup = null;
	
	private boolean actionsEnabled = false;
	
	private String roleName = null;

	public ExtendedUIRole() {
	}
	
	@XmlElement
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	@XmlElement
	public boolean isDashboardLinksDisabled() {
		return this.dashboardLinksDisabled;
	}

	public void setDashboardLinksDisabled(boolean dashboardLinksDisabled) {
		this.dashboardLinksDisabled = dashboardLinksDisabled;
	}

	@XmlElement
	public   String  getHgList() {
		return this.hgList;
	}

	public void setHgList( String hgList) {
		this.hgList = hgList;
	}

	@XmlElement
	public  String getSgList() {
		return this.sgList;
	}
	
	public void setSgList( String sgList) {
		this.sgList = sgList;
	}

	@XmlElement
	public String getRestrictionType() {
		return restrictionType;
	}

	public void setRestrictionType(String restrictionType) {
		this.restrictionType = restrictionType;
	}

	@XmlElement
	public String getDefaultHostGroup() {
		return defaultHostGroup;
	}

	public void setDefaultHostGroup(String defaultHostGroup) {
		this.defaultHostGroup = defaultHostGroup;
	}

	@XmlElement
	public String getDefaultServiceGroup() {
		return defaultServiceGroup;
	}

	public void setDefaultServiceGroup(String defaultServiceGroup) {
		this.defaultServiceGroup = defaultServiceGroup;
	}
	
	@XmlElement
	public boolean isActionsEnabled() {
		return this.actionsEnabled;
	}

	public void setActionsEnabled(boolean actionsEnabled) {
		this.actionsEnabled = actionsEnabled;
	}
	
	@XmlElement
	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	
}
