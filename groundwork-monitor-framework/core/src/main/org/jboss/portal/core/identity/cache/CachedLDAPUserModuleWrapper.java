/*
* JBoss, a division of Red Hat
* Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/

package org.jboss.portal.core.identity.cache;

import org.jboss.portal.identity.UserModule;
import org.jboss.portal.identity.User;
import org.jboss.portal.identity.IdentityException;
import org.jboss.portal.identity.NoSuchUserException;
import org.jboss.portal.identity.IdentityContext;
import org.jboss.portal.identity.UserProfileModule;
import org.jboss.portal.identity.Role;
import org.jboss.portal.identity.ldap.LDAPUserModule;
import org.jboss.portal.identity.ldap.LDAPUserImpl;
import org.jboss.portal.identity.ldap.LDAPConnectionContext;
import org.jboss.portal.identity.service.IdentityModuleService;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import java.util.Set;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:boleslaw dot dawidowicz at redhat anotherdot com">Boleslaw Dawidowicz</a>
 * @version : 0.1 $
 */
public class CachedLDAPUserModuleWrapper extends LDAPUserModule implements UserModule
{
   private LDAPUserModule userModule;

   private IdentityCacheService cacheService;

   private static final org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(CachedLDAPUserModuleWrapper.class);

   public CachedLDAPUserModuleWrapper(LDAPUserModule userModule, IdentityCacheService cacheService)
   {
      this.userModule = userModule;
      this.cacheService = cacheService;
   }


   public User findUserByUserName(String userName) throws IdentityException, IllegalArgumentException, NoSuchUserException
   {
      if (userName == null)
      {
         throw new IllegalArgumentException("UserName cannot be null");
      }

      User user = cacheService.findUserByUserName(userName);

      if (user != null)
      {
         return user;
      }

      user = userModule.findUserByUserName(userName);

      cacheService.storeUser(user);
      
      return user;
   }

   public User findUserById(Object id) throws IdentityException, IllegalArgumentException, NoSuchUserException
   {
      if (id == null)
      {
         throw new IllegalArgumentException("User id cannot be null");
      }

      User user = cacheService.findUserById(id);

      if (user != null)
      {
         return user;
      }

      user = userModule.findUserById(id);

      cacheService.storeUser(user);

      return user;
   }

   public User findUserById(String id) throws IdentityException, IllegalArgumentException, NoSuchUserException
   {
      return findUserById((Object)id);
   }

   public User createUser(String userName, String password) throws IdentityException, IllegalArgumentException
   {
      return userModule.createUser(userName, password);
   }

   public void removeUser(Object id) throws IdentityException, IllegalArgumentException
   {
      userModule.removeUser(id);

      // Invalidate this user in cache
      User user = cacheService.findUserById(id);
      if (user != null)
      {
         cacheService.invalidateUser(user);
      }
   }

   public Set findUsers(int offset, int limit) throws IdentityException, IllegalArgumentException
   {
      return userModule.findUsers(offset, limit);
   }

   public Set findUsersFilteredByUserName(String filter, int offset, int limit) throws IdentityException, IllegalArgumentException
   {
      return userModule.findUsersFilteredByUserName(filter, offset, limit);
   }

   public int getUserCount() throws IdentityException, IllegalArgumentException
   {
      return userModule.getUserCount();
   }

   public List searchUsers(String filter, Object[] filterArgs) throws NamingException, IdentityException
   {
      return userModule.searchUsers(filter, filterArgs);
   }

   public void updatePassword(LDAPUserImpl ldapu, String password) throws IdentityException
   {
      userModule.updatePassword(ldapu, password);
   }

   public boolean validatePassword(LDAPUserImpl ldapu, String password) throws IdentityException
   {
      return userModule.validatePassword(ldapu, password);
   }

   // Methods of LDAPUserModule - need to delegate for compatibility
   public LDAPUserImpl createUserInstance(Attributes attrs, String dn) throws IdentityException
   {
      return userModule.createUserInstance(attrs, dn);
   }

   public User findUserByDN(String dn) throws IdentityException, IllegalArgumentException, NoSuchUserException
   {
      return userModule.findUserByDN(dn);   
   }

   
}
