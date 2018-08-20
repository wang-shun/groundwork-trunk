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

import org.jboss.portal.common.i18n.LocalizedString;
import org.jboss.portal.identity.info.PropertyInfo;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class MockPropertyInfo implements PropertyInfo
{
   /** The name */
   private String name;

   /** The type - default a String */
   private String type = "java.lang.String";

   /** The accessMode - default read-write */
   private String accessMode = "read-write";

   /** The usage - default optional */
   private String usage = "optional";

   /** The displayName - not used */
   private LocalizedString displayName;

   /** The description - not used */
   private LocalizedString description;

   /** The mappingDBType - not used */
   private String mappingDBType;

   /** The mappingDBValue - not used */
   private String mappingDBValue;

   /** The mappingLDAPValue - not used */
   private String mappingLDAPValue;

   /** The mappedLDAP - not used */
   private boolean mappedLDAP;

   /** The mappedDB - not used */
   private boolean mappedDB;
   
   public MockPropertyInfo(String name)
   {
      this.name = name;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getType()
   {
      return type;
   }

   public void setType(String type)
   {
      this.type = type;
   }

   public String getAccessMode()
   {
      return accessMode;
   }

   public void setAccessMode(String accessMode)
   {
      this.accessMode = accessMode;
   }

   public String getUsage()
   {
      return usage;
   }

   public void setUsage(String usage)
   {
      this.usage = usage;
   }

   public LocalizedString getDisplayName()
   {
      return displayName;
   }

   public LocalizedString getDescription()
   {
      return description;
   }

   public String getMappingDBType()
   {
      return mappingDBType;
   }

   public String getMappingDBValue()
   {
      return mappingDBValue;
   }

   public String getMappingLDAPValue()
   {
      return mappingLDAPValue;
   }

   public boolean isMappedLDAP()
   {
      return mappedLDAP;
   }

   public boolean isMappedDB()
   {
      return mappedDB;
   }

}
