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
package org.jboss.portal.test.core.identity.mock;

import java.util.Map;

import org.jboss.portal.identity.IdentityException;
import org.jboss.portal.identity.User;
import org.jboss.portal.identity.UserProfileModule;
import org.jboss.portal.identity.info.ProfileInfo;
import org.jboss.portal.identity.info.PropertyInfo;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class MockUserProfileModule implements UserProfileModule
{

   MockProfileInfo profileInfo = new MockProfileInfo();
   
   /**
    * 
    * Used to set a MockPropertyInfo directly
    * 
    * @param propertyInfo.getName()
    * @param propertyInfo
    */
   public void setPropertyInfo(String id, PropertyInfo propertyInfo)
   {
      this.profileInfo.setPropertyInfo(id, propertyInfo);
   }

   public ProfileInfo getProfileInfo() throws IdentityException
   {
      return profileInfo;
   }
   
   /**
    *  Not implemented Methods
    */
   
   public Map getProperties(User user) throws IdentityException, IllegalArgumentException
   {
      throw new IllegalArgumentException("Mock method not yet implemented");
   }

   public Object getProperty(User user, String propertyName) throws IdentityException, IllegalArgumentException
   {
      throw new IllegalArgumentException("Mock method not yet implemented");
   }

   public void setProperty(User user, String name, Object property) throws IdentityException, IllegalArgumentException
   {
      throw new IllegalArgumentException("Mock method not yet implemented");
   }

}

