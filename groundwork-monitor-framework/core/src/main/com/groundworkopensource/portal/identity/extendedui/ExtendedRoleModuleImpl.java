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

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;

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
 * @author <a href="mailto:julien@jboss.org">Julien Viet </a>
 * @author <a href="mailto:theute@jboss.org">Thomas Heute </a>
 * @author Roy Russo : roy at jboss dot org
 * @version $Revision: 5448 $
 * @portal.core
 */
public class ExtendedRoleModuleImpl {

	/** . */
	private static final org.jboss.logging.Logger log = org.jboss.logging.Logger
			.getLogger(ExtendedRoleModuleImpl.class);

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

	public HibernateExtendedRole findRoleByName(String name)
			throws IdentityException {
		if (name != null) {
			try {
				Session session = getSessionFactory().getCurrentSession();
				Criteria criteria = session
						.createCriteria(HibernateExtendedRole.class);
				criteria.add(Restrictions.naturalId().set("name", name));
				criteria.setCacheable(true);
				HibernateExtendedRole role = (HibernateExtendedRole) criteria
						.uniqueResult();
				return role;
			} catch (HibernateException e) {
				String message = "Cannot find role by name " + name;
				log.error(message, e);
				throw new IdentityException(message, e);
			}
		} else {
			throw new IllegalArgumentException("name cannot be null");
		}
	}

	public HibernateExtendedRole createRole(Long roleId, String name,
			boolean isDashboardLinksDisabled, String hgList, String sgList,
			String restrictionType, String defaultHG, String defaultSG,
			boolean isActionsEnabled) throws IdentityException {
		if (roleId instanceof Long) {
			// Session session = null;
			try {
				HibernateExtendedRole role = new HibernateExtendedRole();
				role.setId(roleId);
				role.setName(name);
				role.setDashboardLinksDisabled(new Boolean(
						isDashboardLinksDisabled));
				role.setActionsEnabled(new Boolean(isActionsEnabled));
				role.setRestrictionType(restrictionType);
				// Only add the list if the restriction type is Partial
				if (restrictionType != null
						&& !restrictionType.equals("")
						&& restrictionType
								.equalsIgnoreCase(ExtendedUIRole.RESTRICTION_TYPE_PARTIAL)) {
					if (hgList != null && !hgList.equals("")) {
						role.setHgList(hgList);
						role.setDefaultHostGroup(defaultHG);
					} else {
						role.setHgList(null);
						role.setDefaultHostGroup(null);
					} // end if
					if (sgList != null && !sgList.equals("")) {
						role.setSgList(sgList);
						role.setDefaultServiceGroup(defaultSG);
					} else {
						role.setSgList(null);
						role.setDefaultServiceGroup(null);
					}
				} else {
					role.setHgList(null);
					role.setSgList(null);
					role.setDefaultHostGroup(null);
					role.setDefaultServiceGroup(null);
				}

				Session session = getSessionFactory().getCurrentSession();
				session.save(role);
				return role;
			} catch (HibernateException e) {
				String message = "Cannot create role " + name;
				log.error(message, e);
				throw new IdentityException(message, e);
			}
			/*
			 * finally { session.flush(); session.close(); }
			 */
		} else {
			throw new IllegalArgumentException("name cannot be null");
		}
	}

	public HibernateExtendedRole updateRole(Long roleId, String name,
			boolean isDashboardLinksDisabled, String hgList, String sgList,
			String restrictionType, String defaultHG, String defaultSG,
			boolean isActionsEnabled) throws IdentityException {
		HibernateExtendedRole role = null;
		if (roleId instanceof Long) {
			try {
				role = new HibernateExtendedRole();
				role.setId(roleId);
				role.setName(name);
				role.setDashboardLinksDisabled(isDashboardLinksDisabled);
				role.setActionsEnabled(isActionsEnabled);
				role.setRestrictionType(restrictionType);
				if (restrictionType != null
						&& !restrictionType.equals("")
						&& restrictionType
								.equalsIgnoreCase(ExtendedUIRole.RESTRICTION_TYPE_PARTIAL)) {
					if (hgList != null && !hgList.equals("")) {
						role.setHgList(hgList);
						role.setDefaultHostGroup(defaultHG);
					} else {
						role.setHgList(null);
						role.setDefaultHostGroup(null);
					} // end if
					if (sgList != null && !sgList.equals("")) {
						role.setSgList(sgList);
						role.setDefaultServiceGroup(defaultSG);
					} else {
						role.setSgList(null);
						role.setDefaultServiceGroup(null);
					}
				} else {
					role.setHgList(null);
					role.setSgList(null);
					role.setDefaultHostGroup(null);
					role.setDefaultServiceGroup(null);
				} // end if

				Session session = getSessionFactory().getCurrentSession();
				session.update(role);

			} catch (HibernateException e) {
				String message = "Cannot create role " + name;
				log.error(message, e);
				throw new IdentityException(message, e);
			}
		}
		return role;
	}

	public HibernateExtendedRole updateActionsEnabled(String name,
			boolean isActionsEnabled) throws IdentityException {
		HibernateExtendedRole role = null;

		try {
			role = findRoleByName(name);
			if (role != null) {
				role.setActionsEnabled(isActionsEnabled);
				Session session = getSessionFactory().getCurrentSession();
				session.update(role);
			} // end if

		} catch (HibernateException e) {
			String message = "Cannot create role " + name;
			log.error(message, e);
			throw new IdentityException(message, e);
		}

		return role;
	}

	public void removeRole(Long roleId) throws IdentityException {
		if (roleId instanceof Long) {
			try {
				Session session = getSessionFactory().getCurrentSession();
				HibernateExtendedRole role = (HibernateExtendedRole) session
						.load(HibernateExtendedRole.class, (Long) roleId);
				session.delete(role);
				session.flush();
			} catch (HibernateException e) {
				String message = "Cannot remove role  " + roleId;
				log.error(message, e);
				throw new IdentityException(message, e);
			}
		}
	}

}
