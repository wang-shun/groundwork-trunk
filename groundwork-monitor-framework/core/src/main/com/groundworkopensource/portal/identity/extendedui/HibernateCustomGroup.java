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

import java.sql.Timestamp;
import java.util.Collection;


public class HibernateCustomGroup implements java.io.Serializable{
	
	/** groupId - Unique Identifier for the custom group     **/
	private long groupId;
	
	/** groupName - Unique name for the custom group     **/
	private String groupName = null;
	
	/** entityTypeId - Unique Identifier for the entityType(Hostgroup or Servicegroup)     **/
	private HibernateEntityType entityType;
	
	/** parentId -  Identifier for the parent (applies to custom groups)     **/
	  /* unidirectional many to many associations */
  private Collection<com.groundworkopensource.portal.identity.extendedui.HibernateCustomGroup> parents;
      
  /* unidirectional one to many associations */
  private Collection<com.groundworkopensource.portal.identity.extendedui.HibernateCustomGroupElement> elements;
	
	/** groupState - GroupState whether save or publish     **/
	private String groupState = null;
	
	/** createdBy - Author of the custom group     **/
	private String createdBy = null;
	
	/** createdTimeStamp- Author of the custom group     **/
	private Timestamp createdTimeStamp = null;
	
	/** lastModifiedTimeStamp - Author of the custom group     **/
	private Timestamp lastModifiedTimeStamp = null;

	public long getGroupId() {
		return groupId;
	}

	public void setGroupId(long groupId) {
		this.groupId = groupId;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public HibernateEntityType getEntityType() {
		return entityType;
	}

	public void setEntityType(HibernateEntityType entityType) {
		this.entityType = entityType;
	}

	
	public String getGroupState() {
		return groupState;
	}

	public void setGroupState(String groupState) {
		this.groupState = groupState;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Timestamp getCreatedTimeStamp() {
		return createdTimeStamp;
	}

	public void setCreatedTimeStamp(Timestamp createdTimeStamp) {
		this.createdTimeStamp = createdTimeStamp;
	}

	public Timestamp getLastModifiedTimeStamp() {
		return lastModifiedTimeStamp;
	}

	public void setLastModifiedTimeStamp(Timestamp lastModifiedTimeStamp) {
		this.lastModifiedTimeStamp = lastModifiedTimeStamp;
	}
	
	public Collection<com.groundworkopensource.portal.identity.extendedui.HibernateCustomGroup> getParents() {
		return parents;
	}

	public void setParents(
			Collection<com.groundworkopensource.portal.identity.extendedui.HibernateCustomGroup> parents) {
		this.parents = parents;
	}

	public Collection<com.groundworkopensource.portal.identity.extendedui.HibernateCustomGroupElement> getElements() {
		return elements;
	}

	public void setElements(
			Collection<com.groundworkopensource.portal.identity.extendedui.HibernateCustomGroupElement> elements) {
		this.elements = elements;
	}
}