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
package org.jboss.portal.core.model.content;

import java.io.Serializable;

/**
 * Type safe string for notion of content type.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public final class ContentType implements Serializable
{

   /** . */
   public static final ContentType UNKNOWN = new ContentType("unknown");

   /** . */
   public static final ContentType CMS = new ContentType("cms");

   /** . */
   public static final ContentType PORTLET = new ContentType("portlet");

   /** . */
   private final String value;

   private ContentType(String value)
   {
      if (value == null)
      {
         throw new IllegalArgumentException();
      }
      this.value = value;
   }

   public int hashCode()
   {
      return value.hashCode();
   }

   public boolean equals(Object obj)
   {
      if (obj == this)
      {
         return true;
      }
      if (obj instanceof ContentType)
      {
         ContentType that = (ContentType)obj;
         return value.equals(that.value);
      }
      return false;
   }

   public String toString()
   {
      return value;
   }

   /**
    * Factory method to create objects.
    *
    * @param value the wrapped value
    * @return the corresponding content type
    * @throws IllegalArgumentException if the string argument is null
    */
   public static ContentType create(String value) throws IllegalArgumentException
   {
      if ("portlet".equals(value))
      {
         return PORTLET;
      }
      else if ("cms".equals(value))
      {
         return CMS;
      }
      else if ("unknown".equals(value))
      {
         return UNKNOWN;
      }
      else if (value == null)
      {
         throw new IllegalArgumentException("No null value for content type accepted");
      }
      return new ContentType(value);
   }
}
