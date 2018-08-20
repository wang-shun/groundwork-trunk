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
package org.jboss.portal.core.model.portal;

import java.io.Serializable;

/**
 * A composite id for a portal object in the scope of its container.
 * <p/>
 * <ul> <li>The empty string maps to the empty namespace and the empty path</li> </ul>
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public class PortalObjectId implements Comparable, Serializable
{

   /** The namespace. */
   private final String namespace;

   /** The path. */
   private final PortalObjectPath path;

   /** The cached hash code. */
   private Integer hashCode;

   /** The lazy computed to string value for canonical format. */
   private String toStringCanonicalFormat;

   /** The lazy computed to string value for legacy format. */
   private String toStringLegacyFormat;
   public static final char NAMESPACE_SEPARATOR = ':';

   /**
    * Build a new portal object id.
    *
    * @param namespace the namespace value
    * @param path      the path object
    * @throws IllegalArgumentException if any argument is null
    */
   public PortalObjectId(String namespace, PortalObjectPath path) throws IllegalArgumentException
   {
      if (namespace == null)
      {
         throw new IllegalArgumentException();
      }
      if (path == null)
      {
         throw new IllegalArgumentException();
      }
      this.namespace = namespace;
      this.path = path;
   }

   /**
    * Returns the portal object namespace.
    *
    * @return the namespace
    */
   public String getNamespace()
   {
      return namespace;
   }

   /**
    * Returns the portal object path.
    *
    * @return the path
    */
   public PortalObjectPath getPath()
   {
      return path;
   }

   public int hashCode()
   {
      if (hashCode == null)
      {
         hashCode = new Integer(namespace.hashCode() + path.hashCode());
      }
      return hashCode.intValue();
   }

   public boolean equals(Object obj)
   {
      if (obj == this)
      {
         return true;
      }
      if (obj instanceof PortalObjectId)
      {
         PortalObjectId that = (PortalObjectId)obj;
         return namespace.equals(that.namespace) && path.equals(that.path);
      }
      return false;
   }

   /**
    * Parse a portal object id given its string representation.
    *
    * @param idValue the id string value
    * @param format  the format
    * @return the PortalObjectId
    * @throws IllegalArgumentException if any argument is null or not well formed
    */
   public static PortalObjectId parse(String idValue, PortalObjectPath.Format format) throws IllegalArgumentException
   {
      if (idValue == null)
      {
         throw new IllegalArgumentException("No null id value accepted");
      }
      int pos = idValue.indexOf(NAMESPACE_SEPARATOR);

      //
      if (pos == -1)
      {
         return parse("", idValue, format);
      }
      else
      {
         String namespace = idValue.substring(0, pos);
         String pathValue = idValue.substring(pos + 1);
         return parse(namespace, pathValue, format);
      }
   }

   /**
    * Parse a portal object id given the namespace and the path string representation.
    *
    * @param namespace the namespace value
    * @param pathValue the path value
    * @param format    the path format
    * @return the PortalObjectId
    * @throws IllegalArgumentException if any argument is null or not well formed
    */
   public static PortalObjectId parse(String namespace, String pathValue, PortalObjectPath.Format format) throws IllegalArgumentException
   {
      return new PortalObjectId(namespace, PortalObjectPath.parse(pathValue, format));
   }

   /**
    * Returns the canonical representation.
    *
    * @return the string value
    */
   public String toString()
   {
      return toString(PortalObjectPath.CANONICAL_FORMAT);
   }

   /**
    * Returns the portal object id string value.
    *
    * @param format the path format
    * @return a formated portal object id value
    * @throws IllegalArgumentException if the format argument is null
    */
   public String toString(PortalObjectPath.Format format) throws IllegalArgumentException
   {
      if (format == null)
      {
         throw new IllegalArgumentException("No null format accepted");
      }

      //
      if (format == PortalObjectPath.LEGACY_FORMAT)
      {
         if (toStringLegacyFormat == null)
         {
            toStringLegacyFormat = toString(namespace, path, format);
         }
         return toStringLegacyFormat;
      }
      else if (format == PortalObjectPath.CANONICAL_FORMAT)
      {
         if (toStringCanonicalFormat == null)
         {
            toStringCanonicalFormat = toString(namespace, path, format);
         }
         return toStringCanonicalFormat;
      }
      else
      {
         return toString(namespace, path, format);
      }
   }

   /**
    * Format a portal object id values to a string value.
    *
    * @param namespace the id value
    * @param path      the id path
    * @param format    the desired format
    * @return the formated value
    * @throws IllegalArgumentException if any argument is null or not well formed
    */
   public static String toString(String namespace, PortalObjectPath path, PortalObjectPath.Format format) throws IllegalArgumentException
   {
      if (namespace == null)
      {
         throw new IllegalArgumentException("No null namespace accepted");
      }
      if (path == null)
      {
         throw new IllegalArgumentException("No null path accepted");
      }
      if (namespace.length() > 0)
      {
         return namespace + NAMESPACE_SEPARATOR + path.toString(format);
      }
      else
      {
         return path.toString(format);
      }
   }

   public int compareTo(Object o)
   {
      PortalObjectId that = (PortalObjectId)o;
      int order = namespace.compareTo(that.namespace);
      return order != 0 ? order : path.compareTo(that.path);
   }
}
