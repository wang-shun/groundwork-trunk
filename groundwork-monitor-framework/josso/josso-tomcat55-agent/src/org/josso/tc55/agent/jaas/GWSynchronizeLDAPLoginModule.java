package org.josso.tc55.agent.jaas;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;

import javax.naming.InitialContext;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.ArrayUtils;
import org.jboss.portal.common.transaction.Transactions;
import org.jboss.portal.identity.IdentityException;
import org.jboss.portal.identity.MembershipModule;
import org.jboss.portal.identity.Role;
import org.jboss.portal.identity.RoleModule;
import org.jboss.portal.identity.User;
import org.jboss.portal.identity.UserModule;
import org.jboss.portal.identity.UserProfileModule;
import org.josso.gateway.identity.SSORole;

public abstract class GWSynchronizeLDAPLoginModule {

	private static final Log logger = LogFactory
			.getLog(GWSynchronizeLDAPLoginModule.class);
	protected String additionalRole;
	protected String defaultAssignedRole;
	protected String synchronizeIdentity;
	protected String synchronizeRoles;
	protected String userModuleJNDIName;
	protected String roleModuleJNDIName;
	protected String membershipModuleJNDIName;
	protected String userProfileModuleJNDIName;
	protected String preserveRoles;
	protected boolean isLDAP = true;

	private UserModule userModule;
	private RoleModule roleModule;
	private MembershipModule membershipModule;
	private UserProfileModule userProfileModule;
	public static final String AUTHENTICATED_ROLE = "Authenticated";

	public void initialize(Subject subject, CallbackHandler callbackHandler,
			Map sharedState, Map options) {

		// Get data
		userModuleJNDIName = (String) options.get("userModuleJNDIName");
		roleModuleJNDIName = (String) options.get("roleModuleJNDIName");
		membershipModuleJNDIName = (String) options
				.get("membershipModuleJNDIName");
		userProfileModuleJNDIName = (String) options
				.get("userProfileModuleJNDIName");
		additionalRole = (String) options.get("additionalRole");
		synchronizeIdentity = (String) options.get("synchronizeIdentity");
		synchronizeRoles = (String) options.get("synchronizeRoles");
		defaultAssignedRole = (String) options.get("defaultAssignedRole");
		preserveRoles = (String) options.get("preserveRoles");

		if (userModuleJNDIName == null || roleModuleJNDIName == null
				|| membershipModuleJNDIName == null
				|| userProfileModuleJNDIName == null
				|| synchronizeIdentity == null)
			isLDAP = false;
	}

	protected UserModule getUserModule() throws Exception {
		if (userModule == null) {
			userModule = (UserModule) new InitialContext()
					.lookup(userModuleJNDIName);
		}
		if (userModule == null) {
			throw new IdentityException(
					"Cannot obtain UserModule using JNDI name:"
							+ userModuleJNDIName);
		}

		return userModule;
	}

	protected RoleModule getRoleModule() throws Exception {

		if (roleModule == null) {
			roleModule = (RoleModule) new InitialContext()
					.lookup(roleModuleJNDIName);
		}
		if (roleModule == null) {
			throw new IdentityException(
					"Cannot obtain RoleModule using JNDI name:"
							+ roleModuleJNDIName);
		}
		return roleModule;
	}

	protected MembershipModule getMembershipModule() throws Exception {

		if (membershipModule == null) {
			membershipModule = (MembershipModule) new InitialContext()
					.lookup(membershipModuleJNDIName);
		}
		if (membershipModule == null) {
			throw new IdentityException(
					"Cannot obtain MembershipModule using JNDI name:"
							+ membershipModuleJNDIName);
		}
		return membershipModule;
	}

	protected UserProfileModule getUserProfileModule() throws Exception {

		if (userProfileModule == null) {
			userProfileModule = (UserProfileModule) new InitialContext()
					.lookup(userProfileModuleJNDIName);
		}
		if (userProfileModule == null) {
			throw new IdentityException(
					"Cannot obtain UserProfileModule using JNDI name:"
							+ userProfileModuleJNDIName);
		}
		return userProfileModule;
	}

	protected boolean isSynchronizeIdentity() {
		if (synchronizeIdentity != null
				&& synchronizeIdentity.equalsIgnoreCase("false")) {
			return Boolean.FALSE.booleanValue();
		}
		return Boolean.TRUE.booleanValue();
	}

	protected boolean isSynchronizeRoles() {
		if (synchronizeRoles != null
				&& synchronizeRoles.equalsIgnoreCase("false")) {
			return Boolean.FALSE.booleanValue();
		}
		return Boolean.TRUE.booleanValue();
	}

	protected boolean isPreserveRoles() {
		if (preserveRoles != null && preserveRoles.equalsIgnoreCase("true")) {
			return Boolean.TRUE.booleanValue();
		}
		return Boolean.FALSE.booleanValue();
	}

	protected abstract SSORole[] getRoleSets() throws LoginException;

	protected void performSynchronization(final String name,
			final String password) throws Exception {
		Group[] tempGroup = getRoleSets();
		if (tempGroup.length > 0) {
			logger
					.debug("Removing the Authenticated role during synch process..");
			final ArrayList<Group> group = new ArrayList<Group>();
			for (int i = 0; i < tempGroup.length; i++) {
				if (!tempGroup[i].getName().equals(
						GWSynchronizeLDAPLoginModule.AUTHENTICATED_ROLE)) {
					group.add(tempGroup[i]);
				} // end if
			} // endif

			logger.debug("$$Synchronizing user: " + name);

			if (logger.isDebugEnabled()) {
				for (Group group1 : group) {
					logger.debug("$$Role Group: " + group1.getName());
					Enumeration xx = group1.members();
					while (xx.hasMoreElements()) {
						Principal o = (Principal) xx.nextElement();
						logger.debug("$$Principal in group: " + o.getName()
								+ "; " + o.toString());

					}
				}
			}
			try {
				TransactionManager tm = (TransactionManager) new InitialContext()
						.lookup("java:/TransactionManager");
				Transactions.requiresNew(tm, new Transactions.Runnable() {
					public Object run() throws Exception {
						try {

							User user = null;
							// check if user exist
							try {
								user = getUserModule().findUserByUserName(name);
							} catch (Exception e) {
								// nothing as user can simply not exist
							}

							// if not try to synchronize it
							if (user == null) {
								user = getUserModule().createUser(name,
										password);
								getUserProfileModule().setProperty(user,
										User.INFO_USER_ENABLED, Boolean.TRUE);

							}

							Set rolesToAssign = new HashSet();

							// now check and try synchronize all the roles
							if (isSynchronizeRoles()) {
								//
								for (Group group1: group) {
									String roleName = group1.getName();
									// check if such role is present

									Role role = null;
									try {
										role = getRoleModule().findRoleByName(
												roleName);
									} catch (Exception e) {
										//
									}

									if (role == null) {
										try {
											role = getRoleModule().createRole(
													roleName, roleName);
										} catch (Throwable e) {
											logger.warn(
													"Error when trying to synchronize role: "
															+ roleName, e);
											continue;
										}
									}
									rolesToAssign.add(role);

								}

							}

							if (defaultAssignedRole != null) {
								try {
									logger
											.debug("DefaultAssigned role is defined so adding it..");
									rolesToAssign
											.add(getRoleModule()
													.findRoleByName(
															defaultAssignedRole));
								} catch (Exception e) {
									//
									logger.warn(
											"Cannot find defaultAssignedRole: "
													+ defaultAssignedRole, e);
								}
							}

							if (rolesToAssign.size() > 0) {
								// If we don't want to overwrite roles
								// assignemts
								// already present in identity store
								if (isPreserveRoles() || !isSynchronizeRoles()) {
									Set presentRoles = getMembershipModule()
											.getRoles(user);
									if (presentRoles != null) {
										rolesToAssign.addAll(presentRoles);
									}
								}
								getMembershipModule().assignRoles(user,
										rolesToAssign);
							}

							return null;

						} catch (Exception e) {
							throw new LoginException(e.toString());
						}
					}
				});
			} catch (Exception e) {
				Throwable cause = e.getCause();
				throw new LoginException(cause.toString());
			}
		} // end if
	}

}
