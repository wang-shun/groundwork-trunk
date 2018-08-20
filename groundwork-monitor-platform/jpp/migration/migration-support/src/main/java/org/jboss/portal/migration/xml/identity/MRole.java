/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.portal.migration.xml.identity;

import java.util.HashSet;
import java.util.Set;


/**
 * Helper POJO class to keep information about role. It keeps a set of role members names.
 */
public class MRole
{

   private final String name;

   private final String displayName;

   private Set<String> members = new HashSet<String>();

   /**
    *
    * @param name
    * @param displayName
    * @param members
    */
   public MRole(String name, String displayName, Set<String> members)
   {
      this.name = name;
      this.displayName = displayName;
      this.members = members;
   }

   /**
    *
    * @param name
    * @param displayName
    */
   public MRole(String name, String displayName)
   {
      this.name = name;
      this.displayName = displayName;
   }

   /**
    *
    * @return
    */
   public String getName()
   {
      return name;
   }

   /**
    *
    * @return
    */
   public String getDisplayName()
   {
      return displayName;
   }

   /**
    *
    * @return
    */
   public Set<String> getMembers()
   {
      return members;
   }

   /**
    * 
    * @param members
    */
   public void setMembers(Set<String> members)
   {
      this.members = members;
   }
}
