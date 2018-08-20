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
package org.jboss.portal.core.identity.ui;

import org.jboss.portal.common.text.FastURLEncoder;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class IdentityUIUser
{
   
   /** The username */
   private String username;

   /** The password */
   private String password;
   
   /** The dynamic user attributes */
   private DynamicUserAttribute attribute;

   public IdentityUIUser()
   {
      this.attribute = new DynamicUserAttribute();
   }
   
   public IdentityUIUser(String username)
   {
      this.username = username;
      this.attribute = new DynamicUserAttribute(this.username);
   }
   
   public String getUsername()
   {
      return username;  
   }
   
   public void setUsername(String username)
   {
      this.username = username;
   }

   public String getPassword()
   {
      return password;
   }

   public void setPassword(String password)
   {
      this.password = password;
   }

   public DynamicUserAttribute getAttribute()
   {
      return attribute;
   }
   
   public String getUTF8Username()
   {
      return FastURLEncoder.getUTF8Instance().encode(username);
   }
}
