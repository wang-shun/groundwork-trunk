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
package org.jboss.portal.core.identity.services.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.portal.core.identity.services.IdentityConstants;
import org.jboss.portal.core.identity.services.IdentityUserManagementService;
import org.jboss.portal.core.identity.services.metadata.CoreIdentityConfigurationException;
import org.jboss.portal.core.identity.services.metadata.IdentityUIConfigurationService;
import org.jboss.portal.identity.IdentityContext;
import org.jboss.portal.identity.IdentityException;
import org.jboss.portal.identity.IdentityServiceController;
import org.jboss.portal.identity.MembershipModule;
import org.jboss.portal.identity.Role;
import org.jboss.portal.identity.RoleModule;
import org.jboss.portal.identity.User;
import org.jboss.portal.identity.UserModule;
import org.jboss.portal.identity.UserProfileModule;
import org.jboss.portal.jems.as.JNDI;
import org.jboss.portal.jems.as.system.AbstractJBossService;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class IdentityUserManagementServiceImpl extends AbstractJBossService implements IdentityUserManagementService
{
   /** The identity user module */
   private UserModule userModule;

   /** The identity role module */
   private RoleModule roleModule;

   /** The identity user profile module */
   private UserProfileModule userProfileModule;

   /** The identity membership module */
   private MembershipModule membershipModule;

   /** The identity service controller */
   private IdentityServiceController identityServiceController;

   /** The core-identity configuration service */
   private IdentityUIConfigurationService identityUIConfigurationService;

   /** The JNDI binding */
   private JNDI.Binding jndiBinding;

   /** The jndi name */
   private String jndiName = null;

   public void startService() throws Exception
   {
      super.startService();

      try
      {
         // Loading required modules
         userModule = (UserModule) identityServiceController.getIdentityContext().getObject(
               IdentityContext.TYPE_USER_MODULE);
         roleModule = (RoleModule) identityServiceController.getIdentityContext().getObject(
               IdentityContext.TYPE_ROLE_MODULE);
         userProfileModule = (UserProfileModule) identityServiceController.getIdentityContext().getObject(
               IdentityContext.TYPE_USER_PROFILE_MODULE);
         membershipModule = (MembershipModule) identityServiceController.getIdentityContext().getObject(
               IdentityContext.TYPE_MEMBERSHIP_MODULE);
      }
      catch (IdentityException e)
      {
         super.stopService();
         throw new CoreIdentityConfigurationException(e);
      }

      if (this.jndiName != null)
      {
         jndiBinding = new JNDI.Binding(jndiName, this);
         jndiBinding.bind();
      }
   }

   public void stopService() throws Exception
   {
      super.stopService();

      if (jndiBinding != null)
      {
         jndiBinding.unbind();
         jndiBinding = null;
      }
   }

   public String getJNDIName()
   {
      return this.jndiName;
   }

   public void setJNDIName(String jndiName)
   {
      this.jndiName = jndiName;
   }

   public IdentityServiceController getIdentityServiceController()
   {
      return identityServiceController;
   }

   public void setIdentityServiceController(IdentityServiceController identityServiceController)
   {
      this.identityServiceController = identityServiceController;
   }

   public UserModule getUserModule()
   {
      return userModule;
   }

   public void setUserModule(UserModule userModule)
   {
      this.userModule = userModule;
   }

   public RoleModule getRoleModule()
   {
      return roleModule;
   }

   public void setRoleModule(RoleModule roleModule)
   {
      this.roleModule = roleModule;
   }

   public UserProfileModule getUserProfileModule()
   {
      return userProfileModule;
   }

   public void setUserProfileModule(UserProfileModule userProfileModule)
   {
      this.userProfileModule = userProfileModule;
   }

   public MembershipModule getMembershipModule()
   {
      return membershipModule;
   }

   public void setMembershipModule(MembershipModule membershipModule)
   {
      this.membershipModule = membershipModule;
   }

   public IdentityUIConfigurationService getIdentityUIConfigurationService()
   {
      return identityUIConfigurationService;
   }

   public void setIdentityUIConfigurationService(IdentityUIConfigurationService identityUIConfigurationService)
   {
      this.identityUIConfigurationService = identityUIConfigurationService;
   }

   public void createUser(String username, String password, Map<String, Object> profileMap, List<String> roles) throws IdentityException
   {

      if (username == null)
         throw new IllegalArgumentException("Username may not be null.");
      if (password == null)
         throw new IllegalArgumentException("Password may not be null.");
      if (profileMap == null)
         throw new IllegalArgumentException("profileMap may not be null.");

      
      User user = this.getUserModule().createUser(username, password);
      Set<Role> roleSet = this.checkRoles(roles);

      // Enable the user
      profileMap.put(User.INFO_USER_ENABLED, Boolean.TRUE);

      for(String key : profileMap.keySet())
      {
         Object value = profileMap.get(key);
         this.getUserProfileModule().setProperty(user, key, value);
      }

      this.getMembershipModule().assignRoles(user, roleSet);
   }

   public String getCurrentEmail(String username) throws IdentityException
   {
      if(username == null)
         throw new IllegalArgumentException("username may not be null.");
      
      User user = this.getUserModule().findUserByUserName(username);
      return (String) this.getUserProfileModule().getProperty(user, User.INFO_USER_EMAIL_REAL);
   }

   public void updateEmail(String username, String email) throws IdentityException
   {
      if(username == null)
         throw new IllegalArgumentException("username may not be null.");
      if(email == null)
         throw new IllegalArgumentException("email may not be null.");

      User user = this.getUserModule().findUserByUserName(username);
      this.getUserProfileModule().setProperty(user, User.INFO_USER_EMAIL_REAL, email);
   }

   private Set<Role> checkRoles(List<String> roles) throws IllegalArgumentException, IdentityException
   {
      
      Set<Role> roleSet = new HashSet<Role>();
      // Set default roles if required
      if (roles == null || (roles != null && roles.size() == 0))
      {
         roles = this.identityUIConfigurationService.getConfiguration().getDefaultRoles();
      }
      // If roles are still not available 
      if (roles == null || (roles != null && roles.size() == 0))
      {
         roles = new ArrayList<String>();
         roles.add(IdentityConstants.DEFAULT_ROLE);
         log.error("no default roles spezified - please check your configuration");
      }

      if (roles != null && roles.size() > 0)
      { // Checking existing roles

         for(String roleName : roles)
         {
            Role role = this.getRoleModule().findRoleByName(roleName);

            if (role == null)
            {
               // Create new role ?
            }
            else
            {
               roleSet.add(role);
            }
         }
      }
      return roleSet;
   }

}
