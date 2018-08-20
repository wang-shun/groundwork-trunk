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
import java.util.*;

import com.groundworkopensource.portal.model.ExtendedUIRolePermission;
import com.groundworkopensource.portal.model.ExtendedUIRolePermissionList;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.database.HibernateService;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet </a>
 * @author <a href="mailto:theute@jboss.org">Thomas Heute </a>
 * @author Roy Russo : roy at jboss dot org
 * @version $Revision: 5448 $
 * @portal.core
 */
public class ExtendedRoleModuleImpl {

	/** . */
	private static final Logger log = Logger
			.getLogger(ExtendedRoleModuleImpl.class);

	public static final String RESTRICTION_TYPE_NONE = "N";
	public static final String RESTRICTION_TYPE_PARTIAL = "P";

	/**
	 * Get Session factory instance
	 * 
	 * @return SessionFactory
	 * @throws IOException
	 */
	public SessionFactory getSessionFactory() {
		PortalContainer manager = PortalContainer.getInstance();
		HibernateService service_ = (HibernateService) manager
				.getComponentInstanceOfType(HibernateService.class);
		SessionFactory sessionFactory = service_.getSessionFactory();
		return sessionFactory;
	}

	public HibernateExtendedRole findRoleByName(String name)
			throws GroundworkContainerExtensionException {
		Session session = null;
		if (name != null) {
			try {
				session = getSessionFactory().openSession();
				session.beginTransaction();
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
				throw new GroundworkContainerExtensionException(message, e);
			} finally {
				session.getTransaction().commit();
				session.close();
			}
		} else {
			throw new IllegalArgumentException("name cannot be null");
		}

	}

	public HibernateExtendedRole createRole(String name,
			boolean isDashboardLinksDisabled, String hgList, String sgList,
			String restrictionType, String defaultHG, String defaultSG,
			boolean isActionsEnabled, ExtendedUIRolePermissionList permissions)
			throws GroundworkContainerExtensionException {
		Session session = null;
		if (name != null) {

			try {
				HibernateExtendedRole role = new HibernateExtendedRole();
				role.setName(name);
				role.setDashboardLinksDisabled(new Boolean(
						isDashboardLinksDisabled));
				role.setActionsEnabled(new Boolean(isActionsEnabled));
				role.setRestrictionType(restrictionType);
				// Only add the list if the restriction type is Partial
				if (restrictionType != null
						&& !restrictionType.equals("")
						&& restrictionType
								.equalsIgnoreCase(RESTRICTION_TYPE_PARTIAL)) {
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
                Set<HibernateExtendedRolePermission> permCol = this.convert2HibObject(permissions,role);
                role.setRolePermissions(permCol);
				session = getSessionFactory().openSession();
				session.beginTransaction();
				session.save(role);
				return role;
			} catch (HibernateException e) {
				String message = "Cannot create role " + name;
				log.error(message, e);
				throw new GroundworkContainerExtensionException(message, e);
			} finally {
				session.getTransaction().commit();
				session.close();
			}
			/*
			 * finally { session.flush(); session.close(); }
			 */
		} else {
			throw new IllegalArgumentException("name cannot be null");
		}
	}

    private Set<HibernateExtendedRolePermission> convert2HibObject(ExtendedUIRolePermissionList permissions, HibernateExtendedRole role) throws GroundworkContainerExtensionException{
        Set<HibernateExtendedRolePermission> hibPermissions = new HashSet<>();

        if (permissions != null) {
            Collection<ExtendedUIRolePermission> permCol = permissions.getRolePermissions();
            for (ExtendedUIRolePermission permission : permCol) {
                String resource = permission.getResource();
                String action = permission.getAction();
                HibernateExtendedRolePermission hibernateExtendedRolePermission = new HibernateExtendedRolePermission();
                HibernateResource hibernateResource = this.findResourceByName(resource);
                HibernatePermission hibernatePermission = this.findPermissionByAction(action);
                if (hibernateResource != null && hibernatePermission != null) {
                    hibernateExtendedRolePermission.setResource(hibernateResource);
                    hibernateExtendedRolePermission.setPermission(hibernatePermission);
                    hibernateExtendedRolePermission.setRole(role);
                    hibPermissions.add(hibernateExtendedRolePermission);
                }
            }
        }
        return hibPermissions;
    }

	public HibernateExtendedRole updateRole(Long roleId, String name,
			boolean isDashboardLinksDisabled, String hgList, String sgList,
			String restrictionType, String defaultHG, String defaultSG,
			boolean isActionsEnabled, ExtendedUIRolePermissionList permissions)
			throws GroundworkContainerExtensionException {
		HibernateExtendedRole role = null;
		Session session = null;
		if (name != null) {
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
								.equalsIgnoreCase(RESTRICTION_TYPE_PARTIAL)) {
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
                role.setRolePermissions(this.convert2HibObject(permissions,role));
				session = getSessionFactory().openSession();
				session.beginTransaction();
				session.update(role);

			} catch (HibernateException e) {
				String message = "Cannot create role " + name;
				log.error(message, e);
				throw new GroundworkContainerExtensionException(message, e);
			} finally {
				session.getTransaction().commit();
				session.close();
			}
		}
		return role;
	}

	public HibernateExtendedRole updateActionsEnabled(String name,
			boolean isActionsEnabled)
			throws GroundworkContainerExtensionException {
		HibernateExtendedRole role = null;
		Session session = null;
		try {
			role = findRoleByName(name);
			if (role != null) {
				role.setActionsEnabled(isActionsEnabled);
				session = getSessionFactory().openSession();
				session.beginTransaction();
				session.update(role);
			} // end if

		} catch (HibernateException e) {
			String message = "Cannot create role " + name;
			log.error(message, e);
			throw new GroundworkContainerExtensionException(message, e);
		} finally {
			session.getTransaction().commit();
			session.close();
		}

		return role;
	}

	public void removeRole(Long roleId)
			throws GroundworkContainerExtensionException {
		Session session = null;
		if (roleId instanceof Long) {
			try {
				session = getSessionFactory().openSession();
				session.beginTransaction();
				HibernateExtendedRole role = (HibernateExtendedRole) session
						.load(HibernateExtendedRole.class, (Long) roleId);
				session.delete(role);
				session.flush();
			} catch (HibernateException e) {
				String message = "Cannot remove role  " + roleId;
				log.error(message, e);
				throw new GroundworkContainerExtensionException(message, e);
			} finally {
				session.getTransaction().commit();
				session.close();
			}
		}
	}

	public Long getMaxRoleID() {
		Session session = getSessionFactory().openSession();
		Long result = 0L;
		try {
			Query query = session
					.createSQLQuery("select max(jbp_rid) from gw_ext_role_attributes");

			List list = query.list();

			Object output = list.get(0);
			if (output != null) {
				result = ((java.math.BigInteger) output).longValue();
			} else {
				result = 0L;
			}

		} catch (HibernateException he) {
			log.error("Error while getting maxnodeid");

		} finally {
			session.flush();
			session.close();
		}
		return result;
	}

    /**
     * Get the Resource by name
     */
    public HibernateResource findResourceByName(String name) throws GroundworkContainerExtensionException {
        Session session = null;
        try {
            session = getSessionFactory().openSession();
            Criteria criteria = session
                    .createCriteria(HibernateEntityType.class);
            Query query = session.createQuery("from com.groundworkopensource.portal.identity.extendedui.HibernateResource R WHERE R.name = :name");
            query.setParameter("name",name);
            List<HibernateResource> resources = query.list();
            return resources.get(0);
        } catch (HibernateException e) {
            String message = "Unable to find resource";
            log.error(message, e);
            throw new GroundworkContainerExtensionException(message, e);
        } finally {
            if (session != null) {
                session.flush();
                session.close();
            }
        }
    }

    /**
     * Gets all the resources
     */
    public List<HibernateResource> getResources() throws GroundworkContainerExtensionException {
        Session session = null;
        try {
            session = getSessionFactory().openSession();
            Criteria criteria = session
                    .createCriteria(HibernateEntityType.class);
            Query query = session.createQuery("from com.groundworkopensource.portal.identity.extendedui.HibernateResource");

            List<HibernateResource> resources = query.list();
            return resources;
        } catch (HibernateException e) {
            String message = "Unable to find resource";
            log.error(message, e);
            throw new GroundworkContainerExtensionException(message, e);
        } finally {
            if (session != null) {
                session.flush();
                session.close();
            }
        }
    }

    /**
     * Get the Permission by action
     */
    public HibernatePermission findPermissionByAction(String action) throws GroundworkContainerExtensionException {
        Session session = null;
        try {
            session = getSessionFactory().openSession();
            Criteria criteria = session
                    .createCriteria(HibernateEntityType.class);
            Query query = session.createQuery("from com.groundworkopensource.portal.identity.extendedui.HibernatePermission P WHERE P.action = :action");
            query.setParameter("action",action);
            List<HibernatePermission> resources = query.list();
            return resources.get(0);
        } catch (HibernateException e) {
            String message = "Unable to find resource";
            log.error(message, e);
            throw new GroundworkContainerExtensionException(message, e);
        } finally {
            if (session != null) {
                session.flush();
                session.close();
            }
        }
    }

}
