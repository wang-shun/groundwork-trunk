/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2006, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.jboss.portal.core.aspects.server;

import org.jboss.logging.Logger;
import org.jboss.portal.common.invocation.AttributeResolver;
import org.jboss.portal.common.invocation.InvocationException;
import org.jboss.portal.common.p3p.P3PConstants;
import org.jboss.portal.identity.CachedUserImpl;
import org.jboss.portal.identity.NoSuchUserException;
import org.jboss.portal.identity.User;
import org.jboss.portal.identity.Role;
import org.jboss.portal.identity.UserModule;
import org.jboss.portal.identity.UserProfileModule;
import org.jboss.portal.identity.MembershipModule;
import org.jboss.portal.server.ServerInterceptor;
import org.jboss.portal.server.ServerInvocation;
import org.jboss.portal.identity.IdentityException;
import com.groundworkopensource.portal.model.CustomGroup;
import com.groundworkopensource.portal.model.CustomGroupElement;
import com.groundworkopensource.portal.model.EntityType;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.text.SimpleDateFormat;

import com.groundworkopensource.portal.identity.extendedui.CommonUtils;
import com.groundworkopensource.portal.identity.extendedui.ExtendedRoleModuleImpl;
import com.groundworkopensource.portal.identity.extendedui.ExtendedUIRole;
import com.groundworkopensource.portal.identity.extendedui.HibernateExtendedRole;
import com.groundworkopensource.portal.identity.extendedui.CustomGroupModule;
import com.groundworkopensource.portal.identity.extendedui.CustomGroupModuleImpl;
import com.groundworkopensource.portal.identity.extendedui.HibernateCustomGroup;
import com.groundworkopensource.portal.identity.extendedui.HibernateCustomGroupElement;
import com.groundworkopensource.portal.identity.extendedui.HibernateCustomGroupCollection;

/**
 * The interceptor is responsible for managing the user identity lifecycle based
 * on the principal name returned by the
 * <code>HttpServletRequest.getUserPrincipal()</code> method.
 * <p/>
 * 
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 12465 $
 */
public class UserInterceptor extends ServerInterceptor {

	/** . */
	public static final String PROFILE_KEY = "profile";

	/** . */
	public static final String USER_KEY = "user";

	/** . */
	public static final String EXTENDED_ROLE_ATT = "com.gwos.portal.ext_role_atts";
	
	/** . */
	public static final String GWOS_CUSTOM_GROUP_ATT = "com.gwos.portal.custom_groups";

	/** Our logger. */
	private static final Logger log = Logger.getLogger(UserInterceptor.class);

	/** User. */
	protected UserModule userModule = null;

	/** UserProfile */
	protected UserProfileModule userProfileModule = null;

	protected MembershipModule membershipModule;

	/** . */
	protected boolean cacheUser = true;
	
	private static final String DATE_FORMAT = "MM/dd/yyyy hh:mm:ss a";

	public UserModule getUserModule() {
		if (userModule == null) {
			try {
				userModule = (UserModule) new InitialContext()
						.lookup("java:portal/UserModule");
			} catch (NamingException e) {
				log.error("could not obtain User Module: ", e);
			}
		}
		return userModule;
	}

	public UserProfileModule getUserProfileModule() {
		if (userProfileModule == null) {
			try {
				userProfileModule = (UserProfileModule) new InitialContext()
						.lookup("java:portal/UserProfileModule");
			} catch (NamingException e) {
				log.error("could not obtain UserProfileModule: ", e);
			}
		}
		return userProfileModule;
	}

	private MembershipModule getMembershipModule() {
		if (membershipModule == null) {
			try {
				membershipModule = (MembershipModule) new InitialContext()
						.lookup("java:portal/MembershipModule");
			} catch (NamingException e) {
				log.error("could not obtain MembershipModule: ", e);
			}
		}
		return membershipModule;
	}

	public void setUserModule(UserModule userModule) {
		this.userModule = userModule;
	}

	protected void invoke(ServerInvocation invocation) throws Exception,
			InvocationException {
		boolean trace = log.isTraceEnabled();
		HttpServletRequest req = invocation.getServerContext()
				.getClientRequest();

		// Get scope
		AttributeResolver principalScopeResolver = invocation.getContext()
				.getAttributeResolver(ServerInvocation.PRINCIPAL_SCOPE);

		// Get the id
		Principal userPrincipal = req.getUserPrincipal();

		// The user and its profile
		User user = null;
		Map<String, String> profile = null;

		// Fetch user if we can
		if (userPrincipal != null) {
			String userName = userPrincipal.getName();

			//
			try {
				if (trace) {
					log.trace("About to fetch user=" + userName);
				}

				// Try to obtain cached user
				user = (User) principalScopeResolver.getAttribute(USER_KEY);

				//
				if (user == null) {
					// Fetch user info
					user = getUserModule().findUserByUserName(userName);

					// Set Last login date
					getUserProfileModule().setProperty(user,
							User.INFO_USER_LAST_LOGIN_DATE,
							"" + new Date().getTime());

					// Set login id
					getUserProfileModule().setProperty(user,
							P3PConstants.INFO_USER_LOGIN_ID, userName);

					// Get profile
					profile = getUserProfileModule().getProperties(user);

					// Build detached pojo
					user = new CachedUserImpl(user.getId(), user.getUserName());

					// Cache
					invocation.getContext().setAttribute(
							ServerInvocation.PRINCIPAL_SCOPE, USER_KEY, user);

					// Get a detached object
					profile = new HashMap(profile);

					// Cache
					invocation.getContext().setAttribute(
							ServerInvocation.PRINCIPAL_SCOPE, PROFILE_KEY,
							profile);
					// load the membership module

					getMembershipModule();
					List<ExtendedUIRole> extUIRoles = populateExtendedUIRoles(user);
					req.getSession()
							.setAttribute(EXTENDED_ROLE_ATT, extUIRoles);
					List<CustomGroup> customGroups = populateCustomGroups();
					req.getSession()
							.setAttribute(GWOS_CUSTOM_GROUP_ATT, customGroups);
					
				}

				//
				if (trace) {
					log.trace("Found user=" + userName);
				}
			} catch (NoSuchUserException e) {
				if (trace) {
					log.trace("User not found " + userName + " for principal "
							+ userName + ", will use no user instead");
				}
			} catch (Exception e) {
				log.error("Cannot retrieve user=" + userName, e);
				throw new InvocationException("Cannot fetch user=" + userName,
						e);
			}
		}

		try {
			// Continue the invocation
			invocation.invokeNext();
		} finally {
			if (!cacheUser) {
				principalScopeResolver.setAttribute(USER_KEY, null);
				principalScopeResolver.setAttribute(PROFILE_KEY, null);
			}
		}
	}

	/**
	 * Populates the extendedUIRoles
	 * 
	 * @return
	 */
	private List<ExtendedUIRole> populateExtendedUIRoles(User user) {
		List<ExtendedUIRole> list = new ArrayList<ExtendedUIRole>();
		try {
			Set<Role> roleSet = this.membershipModule.getRoles(user);
			for (Role role : roleSet) {
				String roleName = role.getName();
				if (roleName != null) {
					ExtendedRoleModuleImpl extRoleModule = new ExtendedRoleModuleImpl();

					HibernateExtendedRole hibRole = extRoleModule
							.findRoleByName(roleName);
					if (hibRole != null) {
						ExtendedUIRole uiRole = new ExtendedUIRole();
						uiRole.setId(hibRole.getId());
						uiRole.setRoleName(roleName);
						uiRole.setDashboardLinksDisabled(hibRole
								.isDashboardLinksDisabled());
						uiRole.setHgList(CommonUtils.convert2HGList(hibRole
								.getHgList()));
						uiRole.setSgList(CommonUtils.convert2HGList(hibRole
								.getSgList()));
						uiRole.setDefaultHostGroup(hibRole
								.getDefaultHostGroup());
						uiRole.setDefaultServiceGroup(hibRole
								.getDefaultServiceGroup());
						uiRole.setRestrictionType(hibRole.getRestrictionType());
						uiRole.setActionsEnabled(hibRole.isActionsEnabled());
						list.add(uiRole);
					} // end if
				} // end if
			} // end for
		} catch (IdentityException ie) {
			log.error(ie.getMessage());
		} // end try/catch
		return list;
	}

	/**
	 * Populates the CustomGroups
	 * 
	 * @return
	 */
	public static List<CustomGroup> populateCustomGroups() {
		List<CustomGroup> customGroups = null;
		try {
			customGroups = new ArrayList<CustomGroup>();
			CustomGroupModule dao = new CustomGroupModuleImpl();
			List<HibernateCustomGroup> hibCustomGroups = dao.findCustomGroups();
			for (HibernateCustomGroup hibCustomGroup : hibCustomGroups) {
				// Populate entityType
				EntityType uiType = new EntityType();
				uiType.setEntityTypeId(hibCustomGroup.getEntityType()
						.getEntityTypeId());
				uiType.setEntityType(hibCustomGroup.getEntityType()
						.getEntityType());

				// Populate customgroup here
				CustomGroup uiCustomGroup = new CustomGroup();
				uiCustomGroup.setGroupId(hibCustomGroup.getGroupId());
				uiCustomGroup.setEntityType(uiType);
				uiCustomGroup.setGroupName(hibCustomGroup.getGroupName());

				// Now populate children
				Collection<HibernateCustomGroupElement> hibCustomGroupElements = hibCustomGroup
						.getElements();
				List<CustomGroupElement> uiElements = new ArrayList<CustomGroupElement>();
				for (HibernateCustomGroupElement hibCustomGroupElement : hibCustomGroupElements) {
					CustomGroupElement uiElement = new CustomGroupElement();
					uiElement
							.setElementId(hibCustomGroupElement.getElementId());
					uiElements.add(uiElement);
				}

				uiCustomGroup.setElements(uiElements);

				// Now populate parents
				Collection<HibernateCustomGroup> hibParents = hibCustomGroup
						.getParents();
				List<CustomGroup> uiParents = new ArrayList<CustomGroup>();
				for (HibernateCustomGroup hibParent : hibParents) {
					CustomGroup uiParent = new CustomGroup();
					List<CustomGroup> uiParents_level_2 = new ArrayList<CustomGroup>();
					for (HibernateCustomGroup hibParent_level_2 : hibParent
							.getParents()) {
						CustomGroup uiParent_level_2 = new CustomGroup();
						uiParent_level_2.setGroupName(hibParent_level_2
								.getGroupName());
						uiParents_level_2.add(uiParent_level_2);
					}
					uiParent.setParents(uiParents_level_2);
					uiParent.setGroupName(hibParent.getGroupName());
					uiParent.setGroupId(hibParent.getGroupId());
					uiParents.add(uiParent);
				}

				uiCustomGroup.setParents(uiParents);
				uiCustomGroup.setCreatedBy(hibCustomGroup.getCreatedBy());
				uiCustomGroup.setGroupState(hibCustomGroup.getGroupState());

				String createdTimeStamp = new SimpleDateFormat(DATE_FORMAT)
						.format(hibCustomGroup.getCreatedTimeStamp());

				uiCustomGroup.setCreatedTimeStamp(createdTimeStamp);

				if (hibCustomGroup.getLastModifiedTimeStamp() != null) {
					String lastModifiedTimeStamp = new SimpleDateFormat(
							DATE_FORMAT).format(hibCustomGroup
							.getLastModifiedTimeStamp());
					uiCustomGroup
							.setLastModifiedTimeStamp(lastModifiedTimeStamp);
				}
				customGroups.add(uiCustomGroup);
			}
		} catch (IdentityException ie) {
			log.error(ie.getMessage());
		} // end try/catch
		return customGroups;
	}

	/**
	 * Initialize all drop downs and DAO
	 */
	private void init() {
		try {

		} catch (Exception exc) {
			log.error(exc.getMessage());
		}

	}

	public boolean isCacheUser() {
		return cacheUser;
	}

	public void setCacheUser(boolean cacheUser) {
		this.cacheUser = cacheUser;
	}

}
