/*
 * Copyright 2009 GroundWork Open Source, Inc. ("GroundWork") All rights
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
package com.groundworkopensource.portal.migration;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.portal.core.identity.service.IdentityServiceControllerImpl;
import org.jboss.portal.jems.as.system.AbstractJBossService;
import org.jboss.portal.identity.RoleModule;
import org.jboss.portal.identity.Role;
import org.jboss.portal.identity.IdentityContext;
import org.jboss.portal.identity.IdentityException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import java.util.Set;
import com.groundworkopensource.portal.identity.extendedui.ExtendedRoleModuleImpl;
import com.groundworkopensource.portal.identity.extendedui.ExtendedUIRole;
import com.groundworkopensource.portal.identity.extendedui.HibernateExtendedRole;

public class GWOSMigrationService extends AbstractJBossService {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.system.ServiceMBeanSupport#startService()
	 */
	@Override
	protected void startService() throws Exception {
		this.synchExtendedRoleAtts();
		log
				.info("SynchExtendedRoleAtts complete.Stopping GWOSMigrationService service...");
		stopService();
	}

	/**
	 * Syncs gw_ext_role_attributes table with jbp_roles table
	 */
	private void synchExtendedRoleAtts() throws Exception {
		Session session = null;
		Transaction transaction = null;
		try {
			SessionFactory identitySessionFactory = (SessionFactory) new InitialContext()
					.lookup("java:portal/IdentitySessionFactory");
			if (identitySessionFactory != null) {
				session = identitySessionFactory.openSession();
				transaction = session.beginTransaction();
			}
			IdentityServiceControllerImpl identityService = (IdentityServiceControllerImpl) new InitialContext()
					.lookup("java:/portal/IdentityServiceController");
			if (identityService == null) {
				throw new GWOSMigrationException(
						"Cannot access identity service: migration cannot continue");
			} // end if

			IdentityContext identityContext = identityService
					.getIdentityContext();
			RoleModule roleModule = (RoleModule) identityContext
					.getObject(IdentityContext.TYPE_ROLE_MODULE);
			Set<Role> jbossRoles = roleModule.findRoles();
			for (Role jbossRole : jbossRoles) {
				Long roleId = (Long) jbossRole.getId();
				String name = jbossRole.getName();
				ExtendedRoleModuleImpl extRoleImpl = new ExtendedRoleModuleImpl();
				HibernateExtendedRole extRole = extRoleImpl
						.findRoleByName(name);
				if (extRole == null && name.equalsIgnoreCase("msp-sample"))
					extRoleImpl.createRole(roleId, name, false, "Linux Servers", null,ExtendedUIRole.RESTRICTION_TYPE_PARTIAL,"Linux Servers",null,false);
				else if (extRole == null && name.equalsIgnoreCase("ro-dashboard"))
					extRoleImpl.createRole(roleId, name, true, null, null,ExtendedUIRole.RESTRICTION_TYPE_NONE,null,null,false);
				else if (extRole == null && name.equalsIgnoreCase("wsuser"))
					extRoleImpl.createRole(roleId, name, true, null, null,ExtendedUIRole.RESTRICTION_TYPE_PARTIAL,null,null,false);
				else if (extRole == null && (name.equalsIgnoreCase("GWAdmin") || name.equalsIgnoreCase("GWOperator")))
					extRoleImpl.createRole(roleId, name, false, null, null,ExtendedUIRole.RESTRICTION_TYPE_NONE,null,null,true);		
				else if (extRole == null ) 
					extRoleImpl.createRole(roleId, name, false, null, null,ExtendedUIRole.RESTRICTION_TYPE_NONE,null,null,false);				
				if (extRole != null) {
					if (name != null && name.equalsIgnoreCase("GWOperator") || name.equalsIgnoreCase("GWAdmin") )
						extRoleImpl.updateActionsEnabled(name, true);
					else
						extRoleImpl.updateActionsEnabled(name, false);
				} // end if
			} // end for
		} catch (Exception ie) {
			log.error(ie.getMessage());
			if (transaction != null) {
				transaction.rollback();
			} // end if
		} // end try/catch
		finally {
			if (transaction != null && !transaction.wasRolledBack()) {
				transaction.commit();
			} // end if
			if (session != null) {
				session.close();
			} // end if
		}
	}
}
