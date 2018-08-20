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


import java.util.List;
import java.util.Collection;


import org.jboss.portal.identity.IdentityException;


/**
 * @author  Arul Shanmugam
 */
public interface CustomGroupModule {

	public HibernateCustomGroup findCustomGroupById(Long groupId)
			throws IdentityException;
	
	public List<HibernateCustomGroup> findCustomGroups()
			throws IdentityException;
	
	public List<HibernateEntityType> findEntityTypes()
			throws IdentityException ;

	public HibernateCustomGroup createCustomGroup(String groupName,
			Byte entityTypeId, Collection<Long> parents,
			String groupState, String createdBy, Collection<Long> children) throws IdentityException;

	public HibernateCustomGroup updateCustomGroup(String groupName,
			Byte entityTypeId, Collection<Long> parents, String groupState,
			String createdBy, Collection<Long> children) throws IdentityException;

	public void removeCustomGroup(Long groupId) throws IdentityException;
	
	public void removeOrphanedChildren(Long elementId, Byte entityTypeId) throws IdentityException;

}
