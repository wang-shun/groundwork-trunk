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

import org.jboss.portal.identity.ldap.LDAPRoleModule;
import org.jboss.portal.identity.ldap.LDAPRoleImpl;
import org.jboss.portal.identity.RoleModule;
import org.jboss.portal.identity.Role;
import org.jboss.portal.identity.IdentityException;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import java.util.Set;
import java.util.List;
import java.util.HashSet;

/**
 * @author <a href="mailto:boleslaw dot dawidowicz at redhat anotherdot com">Boleslaw Dawidowicz</a>
 * @version : 0.1 $
 */
public class CachedLDAPRoleModuleWrapper extends LDAPRoleModule implements RoleModule
{
   private LDAPRoleModule ldapRoleModule;

   private IdentityCacheService cacheService;

   public CachedLDAPRoleModuleWrapper(LDAPRoleModule ldapRoleModule, IdentityCacheService cacheService)
   {
      this.ldapRoleModule = ldapRoleModule;
      this.cacheService = cacheService;
   }

   public Role findRoleByName(String name) throws IdentityException, IllegalArgumentException
   {
      Role role = cacheService.findRoleByName(name);

      if (role != null)
      {
         return role;
      }

      return ldapRoleModule.findRoleByName(name);
   }

   public Set findRolesByNames(String[] names) throws IdentityException, IllegalArgumentException
   {

      //Check if all roles needed are in cache. If not just delegate to the wrapped module
      Set roles = new HashSet();

      for (String name : names)
      {
         Role role = cacheService.findRoleByName(name);
         if (role != null)
         {
            roles.add(role);
         }
         else
         {
            roles = ldapRoleModule.findRolesByNames(names);
            break;
         }
      }

      return roles;
   }

   public Role findRoleById(Object id) throws IdentityException, IllegalArgumentException
   {
      Role role = cacheService.findRoleById(id);

      if (role != null)
      {
         return role;
      }

      return ldapRoleModule.findRoleById(id);
   }

   public Role findRoleById(String id) throws IdentityException, IllegalArgumentException
   {
      return this.findRoleById((Object)id);
   }

   public Role createRole(String name, String displayName) throws IdentityException, IllegalArgumentException
   {
      Role role = ldapRoleModule.createRole(name, displayName);

      cacheService.storeRole(role);

      return role;
   }

   public void removeRole(Object id) throws IdentityException, IllegalArgumentException
   {
      ldapRoleModule.removeRole(id);

      // Invalidate this role in cache
      Role role = cacheService.findRoleById(id);
      if (role != null)
      {
         cacheService.invalidateRole(role);
      }
   }

   public int getRolesCount() throws IdentityException
   {
      return ldapRoleModule.getRolesCount();
   }

   public Set findRoles() throws IdentityException
   {
      return ldapRoleModule.findRoles();
   }

   public List searchRoles(String filter, Object[] filterArgs) throws NamingException, IdentityException
   {
      return ldapRoleModule.searchRoles(filter, filterArgs);
   }

   // Methods of LDAPRoleModule - need to delegate for compatibility

   public void updateDisplayName(LDAPRoleImpl ldapr, String name) throws IdentityException
   {
      ldapRoleModule.updateDisplayName(ldapr, name);

      cacheService.invalidateRole(ldapr);
   }

   public LDAPRoleImpl createRoleInstance(Attributes attrs, String dn) throws IdentityException
   {
      return ldapRoleModule.createRoleInstance(attrs, dn);
   }

   public Role findRoleByDN(String dn) throws IdentityException, IllegalArgumentException
   {
      return ldapRoleModule.findRoleByDN(dn);
   }


}
