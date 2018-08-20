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
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Date;
import java.sql.Timestamp;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.hibernate.Transaction;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jboss.portal.identity.IdentityException;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Restrictions;

/**
 * @author Arul Shanmugam
 */
public class CustomGroupModuleImpl implements CustomGroupModule {

	/** . */
	private static final Logger log = Logger
			.getLogger(CustomGroupModuleImpl.class);

	/**
	 * Get Session factory instance
	 * 
	 * @return SessionFactory
	 * @throws IOException
	 */
	public SessionFactory getSessionFactory() {
		try {
			return (SessionFactory) new InitialContext()
					.lookup("java:/portal/IdentitySessionFactory");
		} catch (NamingException badDatabaseName) {
			log.error("Error in obtaining Session");
			// throw (new IOException());
		}
		return null;
	}

	/**
	 * Finds the customgroup by ID
	 */
	public HibernateCustomGroup findCustomGroupById(Long groupId)
			throws IdentityException {
		Session session = null;
		if (groupId > 0) {
			try {
				session = getSessionFactory().openSession();
				Criteria criteria = session
						.createCriteria(HibernateCustomGroup.class);
				criteria.add(Restrictions.naturalId().set("groupId", groupId));
				criteria.setCacheable(true);
				HibernateCustomGroup group = (HibernateCustomGroup) criteria
						.uniqueResult();
				return group;
			} catch (HibernateException e) {
				String message = "Cannot find group by id " + groupId;
				log.error(message, e);
				throw new IdentityException(message, e);
			} finally {
				if (session != null) {
					session.flush();
					session.close();
				}
			}
		} else {
			throw new IllegalArgumentException("groupId cannot be null or zero");
		} // end if

	}

	/**
	 * Gets all the custom groups
	 */
	public List<HibernateCustomGroup> findCustomGroups()
			throws IdentityException {
		Session session = null;
		try {
			session = getSessionFactory().openSession();
			Criteria criteria = session
					.createCriteria(HibernateCustomGroup.class);
			Query query = session.createQuery("from HibernateCustomGroup");

			List<HibernateCustomGroup> groups = query.list();
			return groups;
		} catch (HibernateException e) {
			String message = "Unable to find custom groups";
			log.error(message, e);
			throw new IdentityException(message, e);
		} finally {
			if (session != null) {
				session.flush();
				session.close();
			}
		}
	}

	/**
	 * Gets custom groups by name
	 */
	public List<HibernateCustomGroup> findCustomGroupByName(String groupName)
			throws IdentityException {
		Session session = null;
		try {
			session = getSessionFactory().openSession();
			Criteria criteria = session.createCriteria(
					HibernateCustomGroup.class).add(
					Restrictions.like("groupName", groupName));
			Query query = session.createQuery("from HibernateCustomGroup");

			List<HibernateCustomGroup> groups = criteria.list();
			return groups;
		} catch (HibernateException e) {
			String message = "Unable to find custom groups";
			log.error(message, e);
			throw new IdentityException(message, e);
		} finally {
			if (session != null) {
				session.flush();
				session.close();
			}
		}
	}

	/**
	 * Get the entity types
	 */
	public List<HibernateEntityType> findEntityTypes() throws IdentityException {
		Session session = null;
		try {
			session = getSessionFactory().openSession();
			Criteria criteria = session
					.createCriteria(HibernateEntityType.class);
			Query query = session.createQuery("from HibernateEntityType");

			List<HibernateEntityType> entityTypes = query.list();
			return entityTypes;
		} catch (HibernateException e) {
			String message = "Unable to find custom groups";
			log.error(message, e);
			throw new IdentityException(message, e);
		} finally {
			if (session != null) {
				session.flush();
				session.close();
			}
		}
	}

	/**
	 * Creates a new CustomGroup
	 */
	public HibernateCustomGroup createCustomGroup(String groupName,
			Byte entityTypeId, Collection<Long> parents, String groupState,
			String createdBy, Collection<Long> children)
			throws IdentityException {
		Session session = null;
		HibernateCustomGroup group = new HibernateCustomGroup();
		try {
			group.setGroupName(groupName);
			HibernateEntityType entityType = new HibernateEntityType();
			entityType.setEntityTypeId(entityTypeId);
			group.setEntityType(entityType);

			group.setGroupState(groupState);
			group.setCreatedBy(createdBy);
			Date today = new Date();
			Timestamp currentTimestamp = new Timestamp(today.getTime());
			group.setCreatedTimeStamp(currentTimestamp);
			session = getSessionFactory().openSession();

			Set<HibernateCustomGroup> hibParents = new HashSet<HibernateCustomGroup>();
			for (Long parentId : parents) {
				HibernateCustomGroup parent = findCustomGroupById(parentId);
				hibParents.add(parent);
			}

			Set<HibernateCustomGroupElement> hibElements = new HashSet<HibernateCustomGroupElement>();
			for (Long childId : children) {
				HibernateCustomGroupElement child = new HibernateCustomGroupElement();
				child.setGroup(group);
				child.setElementId(childId);
				child.setEntityType(entityType);
				hibElements.add(child);
				if (entityTypeId == 3) {
					HibernateCustomGroup associatedGroup = findCustomGroupById(childId);
					Set<HibernateCustomGroup> tempParents = new HashSet<HibernateCustomGroup>();
					if (associatedGroup.getParents() != null) {
						associatedGroup.getParents().add(group);
					} else {
						tempParents.add(group);
						associatedGroup.setParents(tempParents);
					} // end if
					session.update(associatedGroup);
				} // end if
			} // end for
			group.setElements(hibElements);
			group.setParents(hibParents);
			session.save(group);
		} catch (HibernateException e) {
			String message = "Unable to create custom groups";
			log.error(message, e);
			throw new IdentityException(message, e);
		} finally {
			if (session != null) {
				session.flush();
				session.close();
			}
		}
		return group;
	}

	/**
	 * Updates the custom group
	 */
	public HibernateCustomGroup updateCustomGroup(String groupName,
			Byte entityTypeId, Collection<Long> parents, String groupState,
			String createdBy, Collection<Long> children)
			throws IdentityException {
		log.debug("Enter updateCustomGroup");
		Session session = null;
		Transaction tx = null;
		HibernateCustomGroup group = null;
		List<HibernateCustomGroup> customGroups = findCustomGroupByName(groupName);
		if (customGroups != null) {
			group = customGroups.get(0); // customgroup name
											// is unique
			try {
				session = getSessionFactory().openSession();
				tx = session.beginTransaction();
				group.setGroupName(groupName);
				HibernateEntityType entityType = new HibernateEntityType();
				entityType.setEntityTypeId(entityTypeId);
				group.setEntityType(entityType);
				group.setGroupState(groupState);
				group.setCreatedBy(createdBy);
				Date today = new Date();
				Timestamp currentTimestamp = new Timestamp(today.getTime());
				group.setLastModifiedTimeStamp(currentTimestamp);
				log.debug("Before updating children");
				

				log.debug("Before removing children");
				// first remove all children
				Collection<HibernateCustomGroupElement> currentElements = group
						.getElements();
				Collection<HibernateCustomGroupElement> markForDelete = new ArrayList<HibernateCustomGroupElement>();

				// Compare UI object and hibernate object an mark for delete
				Collection<Long> tempChildren = new ArrayList(children);
				for (HibernateCustomGroupElement element : currentElements) {
					boolean matchFound = false;
					for (Long childId : children) {
						if (element.getElementId() == childId) {
							matchFound = true;
							tempChildren.remove(childId);
							break;
						} // end if
					} // end for
					if (!matchFound) {
						markForDelete.add(element);
					}
				} // end for

				for (HibernateCustomGroupElement element : markForDelete) {
					currentElements.remove(element);
					Query deleteQuery = session
							.createSQLQuery("delete from gw_customgroup_element "
									+ "where group_id= ? and element_id= ? and entitytype_id=?");
					deleteQuery.setLong(0, element.getGroup().getGroupId());
					deleteQuery.setLong(1, element.getElementId());
					deleteQuery.setLong(2, element.getEntityType().getEntityTypeId());
					int deleted = deleteQuery.executeUpdate();
				} // end if

				Set<HibernateCustomGroup> hibParents = new HashSet<HibernateCustomGroup>();
				for (Long parentId : parents) {
					HibernateCustomGroup parent = findCustomGroupById(parentId);
					hibParents.add(parent);
				}

				// Now add the new objects
				Set<HibernateCustomGroupElement> hibElements = new HashSet<HibernateCustomGroupElement>();
				for (Long childId : tempChildren) {
					HibernateCustomGroupElement child = new HibernateCustomGroupElement();
					child.setGroup(group);
					child.setElementId(childId);
					child.setEntityType(entityType);
					hibElements.add(child);
					if (entityTypeId == 3) {
						log.debug("This is a Custom group...");
						HibernateCustomGroup associatedGroup = findCustomGroupById(childId);
						Set<HibernateCustomGroup> tempParents = new HashSet<HibernateCustomGroup>();
						if (associatedGroup.getParents() != null) {
							associatedGroup.getParents().add(group);
						} else {
							tempParents.add(group);
							associatedGroup.setParents(tempParents);
						} // end if
						session.update(associatedGroup);
					} // end if
				} // end for

				log.debug("Before adding children");
				group.getElements().addAll(hibElements);
				session.update(group);
				tx.commit();
			} catch (HibernateException e) {
				if (tx != null)
					tx.rollback();
				String message = "Unable to update custom groups";
				log.error(message, e);
				throw new IdentityException(message, e);
			} finally {
				if (session != null) {
					session.flush();
					session.close();
				}
			}
		}
		log.debug("Exit updateCustomGroup");
		return group;
	}
	
	/**
	 * Removes orphaned children (especially when the hostgroup or service group is delete in monarch
	 */
	public void removeOrphanedChildren(Long elementId, Byte entityTypeId) throws IdentityException {
		Session session = null;
		Transaction tx = null;
		try {
			session = getSessionFactory().openSession();
			tx = session.beginTransaction();
			Query deleteQuery = session
					.createSQLQuery("delete from gw_customgroup_element "
							+ "where element_id= ? and entitytype_id= ?");
			deleteQuery.setLong(0, elementId);
			deleteQuery.setLong(1, entityTypeId);
			int deleted = deleteQuery.executeUpdate();
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			String message = "Unable to delete orphaned children due to "
					+ e.getMessage();
			log.error(message, e);
			throw new IdentityException(message, e);
		} finally {
			if (session != null) {
				session.flush();
				session.close();
			}
		}
	}

	

	/**
	 * Removes the custom group
	 */
	public void removeCustomGroup(Long groupId) throws IdentityException {
		Session session = null;
		Transaction tx = null;
		try {
			session = getSessionFactory().openSession();
			tx = session.beginTransaction();
			Query deleteQuery = session
					.createSQLQuery("delete from gw_customgroup_collection "
							+ "where group_id= ?");
			deleteQuery.setLong(0, groupId);
			int deleted = deleteQuery.executeUpdate();

			deleteQuery = session
					.createSQLQuery("delete from gw_customgroup_element "
							+ "where group_id= ?");
			deleteQuery.setLong(0, groupId);
			deleted = deleteQuery.executeUpdate();
			// Delete it from parent perspective as well
			deleteQuery = session
					.createSQLQuery("delete from gw_customgroup_element "
							+ "where element_id= ? and entitytype_id=3");
			deleteQuery.setLong(0, groupId);
			deleted = deleteQuery.executeUpdate();

			deleteQuery = session.createSQLQuery("delete from gw_customgroup "
					+ "where group_id= ?");
			deleteQuery.setLong(0, groupId);
			deleted = deleteQuery.executeUpdate();
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			String message = "Unable to delete custom groups due to "
					+ e.getMessage();
			log.error(message, e);
			throw new IdentityException(message, e);
		} finally {
			if (session != null) {
				session.flush();
				session.close();
			}
		}
	}

}
