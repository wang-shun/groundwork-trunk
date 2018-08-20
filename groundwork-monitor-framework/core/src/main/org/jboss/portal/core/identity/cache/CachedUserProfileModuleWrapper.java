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

import org.jboss.portal.identity.UserProfileModule;
import org.jboss.portal.identity.User;
import org.jboss.portal.identity.IdentityException;
import org.jboss.portal.identity.info.ProfileInfo;
import org.jboss.logging.Logger;

import java.util.Map;

/**
 * @author <a href="mailto:boleslaw dot dawidowicz at redhat anotherdot com">Boleslaw Dawidowicz</a>
 * @version : 0.1 $
 */
public class CachedUserProfileModuleWrapper implements UserProfileModule
{

   private static final Logger log = Logger.getLogger(CachedUserProfileModuleWrapper.class);

   private UserProfileModule userProfileModule;

   private IdentityCacheService cacheService;

   public CachedUserProfileModuleWrapper(UserProfileModule userProfileModule, IdentityCacheService identityCacheService)
   {
      this.userProfileModule = userProfileModule;
      this.cacheService = identityCacheService;
   }

   public Object getProperty(User user, String propertyName) throws IdentityException, IllegalArgumentException
   {
      // Just grab the whole profile and check if this property is there

      Map profile = this.getProperties(user);

      if (profile != null && profile.containsKey(propertyName))
      {
         
         return profile.get(propertyName);
      }

      // else delegate to the wrapped implementation

      return userProfileModule.getProperty(user, propertyName);

   }

   public void setProperty(User user, String name, Object property) throws IdentityException, IllegalArgumentException
   {
      userProfileModule.setProperty(user, name, property);
      cacheService.invalidateProfile(user);

   }

   public Map getProperties(User user) throws IdentityException, IllegalArgumentException
   {
      Map profile = cacheService.findUserProfileById(user.getId());

      if (profile != null)
      {
         return profile;
      }

      profile = userProfileModule.getProperties(user);
      cacheService.storeProfile(user, profile);
      return profile;
   }

   public ProfileInfo getProfileInfo() throws IdentityException
   {
      return userProfileModule.getProfileInfo();
   }
}
