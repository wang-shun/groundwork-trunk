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

import org.jboss.portal.identity.Role;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class UIRole
{

   /** The role id */
   private Object id;

   /** The role name */
   private String name;

   /** The role display name */
   private String displayName;
   
   public UIRole()
   {
      this.id = null;
      this.name = null;
      this.displayName = null;
   }
   
   public UIRole(Role role)
   {
      this.id = role.getId();
      this.name = role.getName();
      this.displayName = role.getDisplayName();
   }

   public String getDisplayName()
   {
      return this.displayName;
   }

   public Object getId()
   {
      return this.id;
   }

   public String getName()
   {
      return this.name;
   }

   public void setDisplayName(String displayName)
   {
      this.displayName = displayName;
   }

   public void setName(String name)
   {
      this.name = name;
   }
   
   /**
    * Used for GET parameters - role name does never contain a escape char
    * 
    * @return double escaped role
    */
   public String getEscapedName()
   {
      return name.replace("\\", "\\\\");
   }
}
