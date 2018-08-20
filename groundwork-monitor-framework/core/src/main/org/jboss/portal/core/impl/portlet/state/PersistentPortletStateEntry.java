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
package org.jboss.portal.core.impl.portlet.state;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 12318 $
 */
public class PersistentPortletStateEntry implements Serializable
{

   /** The primary key. */
   private Long key;

   /** The name. */
   private String name;

   /** The access mode. */
   private boolean readOnly;

   /** The preference type. */
   private int type;

   /** The preference values. */
   private String[] strings;
   
   private List<String> value;

   // This flag is used because the value is made up of strings and type
   // and it is not possible to update the value when strings or type
   // is updated, so when strings of type is modified with set dirty
   private boolean dirty;

   public PersistentPortletStateEntry()
   {
      this.key = null;
      this.name = null;
      this.readOnly = false;
      this.type = 0;
      this.strings = null;
      this.value = null;
      this.dirty = false;
   }

   public PersistentPortletStateEntry(String name, List<String> value)
   {
      if (value == null)
      {
         throw new IllegalArgumentException();
      }
      this.key = null;
      this.name = name;
      this.readOnly = false;
      this.type = 1;
      this.strings = value.toArray(new String[value.size()]);
      this.value = value;
      this.dirty = false;
   }

   public Long getKey()
   {
      return key;
   }

   public void setKey(Long key)
   {
      this.key = key;
   }

   /**
    *
    */
   public String getName()
   {
      return name;
   }

   /** Called by hibernate. */
   public void setName(String name)
   {
      this.name = name;
   }

   /**
    *
    */
   public int getType()
   {
      return type;
   }

   /** Called by hibernate. */
   public void setType(int type)
   {
      this.type = type;
      this.dirty = true;
   }

   public boolean isReadOnly()
   {
      return readOnly;
   }

   public void setReadOnly(boolean readOnly)
   {
      this.readOnly = readOnly;
   }

   /**
    *
    */
   public String[] getStrings()
   {
      return strings;
   }

   /** Called by hibernate. */
   public void setStrings(String[] strings)
   {
      this.strings = strings;
      this.dirty = true;
   }

   public List<String> getValue()
   {
      if (dirty)
      {
         value = new ArrayList<String>();
         for (int i=0; i<strings.length; i++)
         {
            value.add(strings[i]);
         }
         dirty = false;
      }
      return value;
   }

   /** Provide a default impl. */
   public String toString()
   {
      StringBuffer buffer = new StringBuffer("[").append(name).append(",");
      if (strings == null)
      {
         buffer.append("null,");
      }
      else
      {
         buffer.append("(");
         for (int i = 0; i < strings.length; i++)
         {
            String s = strings[i];
            buffer.append(i > 0 ? "," : "").append(s);
         }
         buffer.append("),");
      }
      buffer.append("]");
      return buffer.toString();
   }
}
