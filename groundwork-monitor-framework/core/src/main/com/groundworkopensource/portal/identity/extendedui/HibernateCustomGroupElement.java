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



public class HibernateCustomGroupElement implements java.io.Serializable{
	
	/** elementId - Unique Identifier for the customgroup elements    **/
	private long elementId;
	
	/** groupId - Unique Identifier for the custom group     **/
	private HibernateCustomGroup group;
	
	/** entityTypeId - Unique Identifier for the entityType(Hostgroup or Servicegroup)     **/
	private HibernateEntityType entityType;
	
	
	public long getElementId() {
		return elementId;
	}

	public void setElementId(long elementId) {
		this.elementId = elementId;
	}

	public HibernateCustomGroup getGroup() {
		return group;
	}

	public void setGroup(HibernateCustomGroup group) {
		this.group = group;
	}
	
	public HibernateEntityType getEntityType() {
		return entityType;
	}

	public void setEntityType(HibernateEntityType entityType) {
		this.entityType = entityType;
	}

}
