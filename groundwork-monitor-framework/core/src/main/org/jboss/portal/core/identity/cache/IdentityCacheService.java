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

import org.jboss.portal.identity.Role;
import org.jboss.portal.identity.User;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:boleslaw dot dawidowicz at redhat anotherdot com">Boleslaw Dawidowicz</a>
 * @version : 0.1 $
 */
public class IdentityCacheService
{
   private static final org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(IdentityCacheService.class);

   public final static String JNDI_NAME = "java:portal/IdentityCacheService";

   protected ThreadLocal<Map<String, User>> userNameCache = new ThreadLocal<Map<String, User>>();

   protected ThreadLocal<Map<Object, User>> userIdCache = new ThreadLocal<Map<Object, User>>();

   protected ThreadLocal<Map<Object, Map>> profileCache = new ThreadLocal<Map<Object, Map>>();

   protected ThreadLocal<Map<String, Role>> roleNameCache = new ThreadLocal<Map<String, Role>>();

   protected ThreadLocal<Map<Object, Role>> roleIdCache = new ThreadLocal<Map<Object, Role>>();


   public void cleanup()
   {
      userNameCache.set(null);
      userIdCache.set(null);
      profileCache.set(null);
      roleNameCache.set(null);
      roleIdCache.set(null);

      log.debug("Identity cache invalidated");
   }

   private Map<String, User> getUserNameCache()
   {
      if (userNameCache.get() == null)
      {
         userNameCache.set(new HashMap<String, User>());
      }
      return userNameCache.get();
   }

   private Map<Object, User> getUserIdCache()
   {
      if (userIdCache.get() == null)
      {
         userIdCache.set(new HashMap<Object, User>());
      }
      return userIdCache.get();
   }

   private Map<Object, Map> getProfileCache()
   {
      if (profileCache.get() == null)
      {
         profileCache.set(new HashMap<Object, Map>());
      }
      return profileCache.get();
   }

   private Map<String, Role> getRoleNameCache()
   {
      if (roleNameCache.get() == null)
      {
         roleNameCache.set(new HashMap<String, Role>());
      }
      return roleNameCache.get();
   }

   private Map<Object, Role> getRoleIdCache()
   {
      if (roleIdCache.get() == null)
      {
         roleIdCache.set(new HashMap<Object, Role>());
      }
      return roleIdCache.get();
   }

   public void storeUser(User user)
   {
      // We want to be transparent so just ignore null argument
      if (user != null)
      {
         getUserIdCache().put(user.getId(), user);
         getUserNameCache().put(user.getUserName(), user);

         if (log.isDebugEnabled())
         {
            log.debug("User cached for id=" + user.getId() + "; username=" + user.getUserName());
         }
      }
   }

   public void invalidateUser(User user)
   {
      // We want to be transparent so just ignore null argument
      if (user != null)
      {
         getUserIdCache().put(user.getId(), null);
         getUserNameCache().put(user.getUserName(), null);

         if (log.isDebugEnabled())
         {
            log.debug("User invalidated in cache for id=" + user.getId() + "; username=" + user.getUserName());
         }
      }
   }

   public void storeProfile(User user, Map profile)
   {
      // We want to be transparent so just ignore null argument
      if (user != null && profile != null)
      {
         getProfileCache().put(user.getId(), profile);

         if (log.isDebugEnabled())
         {
            log.debug("User profile cached for id=" + user.getId());
         }
      }
   }


   public void invalidateProfile(User user)
   {
      // We want to be transparent so just ignore null argument
      if (user != null)
      {
         getProfileCache().put(user.getId(), null);

         if (log.isDebugEnabled())
         {
            log.debug("User profile invalidated in cache for id=" + user.getId());
         }
      }
   }

   public void storeRole(Role role)
   {
      // We want to be transparent so just ignore null argument
      if (role != null)
      {
         getRoleIdCache().put(role.getId(), role);
         getRoleNameCache().put(role.getName(), role);

         if (log.isDebugEnabled())
         {
            log.debug("Role cached for id=" + role.getId() + "; name=" + role.getName());
         }
      }
   }

   public void invalidateRole(Role role)
   {
      // We want to be transparent so just ignore null argument
      if (role != null)
      {
         getRoleIdCache().put(role.getId(), null);
         getRoleNameCache().put(role.getName(), null);

         if (log.isDebugEnabled())
         {
            log.debug("Role invalidated in cache for id=" + role.getId() + "; name=" + role.getName());
         }
      }
   }

   public User findUserByUserName(String userName)
   {
      User user = getUserNameCache().get(userName);

      if (user != null && log.isDebugEnabled())
      {
         log.debug("User retrieved from cache for username=" + user.getUserName());
      }

      return user;
   }

   public User findUserById(Object id)
   {
      User user = getUserIdCache().get(id);

      if (user != null && log.isDebugEnabled())
      {
         log.debug("User retrieved from cache for id=" + user.getId());
      }

      return user;
   }

   public Map findUserProfileById(Object id)
   {
      Map profile = getProfileCache().get(id);

      if (profile != null && log.isDebugEnabled())
      {
         log.debug("User profile retrieved from cache for user id=" + id);
      }

      return profile;
   }

   public Role findRoleByName(String roleName)
   {
      Role role = getRoleNameCache().get(roleName);

      if (role != null && log.isDebugEnabled())
      {
         log.debug("Role retrieved from cache for name=" + role.getName());
      }

      return role;
   }

   public Role findRoleById(Object id)
   {
      Role role = getRoleIdCache().get(id);

      if (role != null && log.isDebugEnabled())
      {
         log.debug("Role retrieved from cache for id=" + role.getId());
      }

      return role;
   }


}
