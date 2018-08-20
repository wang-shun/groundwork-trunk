/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2009, Red Hat Middleware, LLC, and individual                    *
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

import org.jboss.portal.common.util.Base64;
import org.jboss.portal.common.util.ParameterValidation;
import org.jboss.portal.common.util.Tools;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

/**
 * A path for a portal object.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @author <a href="mailto:theute@jboss.org">Thomas Heute</a>
 * @version $Revision: 13018 $
 */
public class PortalObjectPath implements Comparable, Serializable
{

   /** . */
   private static final String[] EMPTY_STRINGS = new String[0];

   /** This statement must be executed before the previous one otherwise the empty string array will be null. */
   public static final PortalObjectPath ROOT_PATH = new PortalObjectPath();

   /** The composites. */
   private final String[] names;

   /** Inclusive start index. */
   private final int from;

   /** Exclusive stop index. */
   private final int to;

   /** The lazy computed hash code. */
   private Integer hashCode;

   /** The lazy computed to string value for canonical format. */
   private String toStringCanonicalFormat;

   /** The lazy computed to string value for legacy format. */
   private String toStringLegacyFormat;

   /** The lazy created parent that can be useful. */
   private PortalObjectPath parent;

   public PortalObjectPath()
   {
      this.names = EMPTY_STRINGS;
      this.from = 0;
      this.to = 0;
   }

   /**
    * Copy constructor.
    *
    * @param that the id to clone
    * @throws IllegalArgumentException if the argument to clone is null
    */
   public PortalObjectPath(PortalObjectPath that) throws IllegalArgumentException
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(that, "PortalObjectId to be copied");

      //
      this.names = that.names;
      this.hashCode = that.hashCode;
      this.from = that.from;
      this.to = that.to;
   }

   /**
    * Build an id directly from its composing names.
    *
    * @param names the id names
    * @throws IllegalArgumentException if any argument is null or not well formed
    */
   public PortalObjectPath(String[] names) throws IllegalArgumentException
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(names, "composing names");

      //
      this.names = names;
      this.from = 0;
      this.to = names.length;
   }

   /**
    * Build an id directly from its composing names.
    *
    * @param names the id names
    * @throws IllegalArgumentException if any argument is null or not well formed
    */
   private PortalObjectPath(String[] names, int from, int to) throws IllegalArgumentException
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(names, "composing names");

      //
      this.names = names;
      this.from = from;
      this.to = to;
   }

   /**
    * Return the parent or null if this is the root id.
    *
    * @return the parent
    */
   public PortalObjectPath getParent()
   {
      if (parent == null && from < to)
      {
         parent = new PortalObjectPath(names, from, to - 1);
      }
      return parent;
   }

   public PortalObjectPath getChild(String name)
   {
      int length = to - from;
      String[] childNames = new String[length + 1];
      System.arraycopy(this.names, from, childNames, 0, length);
      childNames[length] = name;
      return new PortalObjectPath(childNames);
   }

   /**
    * Build an id by parsing a string representation.
    *
    * @param value  the string representation
    * @param format the string format
    * @throws IllegalArgumentException if any argument is null or not well formed
    */
   public PortalObjectPath(String value, PortalObjectPath.Format format) throws IllegalArgumentException
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(format, "Format");

      //
      this.names = format.parse(value);
      this.from = 0;
      this.to = names.length;
   }

   public int getLength()
   {
      return to - from;
   }

   public String getName(int index)
   {
      if (index < from)
      {
         throw new IllegalArgumentException();
      }
      if (index >= to)
      {
         throw new IllegalArgumentException();
      }
      return names[index - from];
   }

   public String getLastComponentName()
   {
      return names[names.length - 1];
   }

   public boolean equals(Object obj)
   {
      if (obj == this)
      {
         return true;
      }
      if (obj instanceof PortalObjectPath)
      {
         PortalObjectPath that = (PortalObjectPath)obj;
         String[] thisNames = this.names;
         String[] thatNames = that.names;

         //
         //
         int thisLen = this.to - this.from;
         int thatLen = that.to - that.from;
         if (thisLen != thatLen)
         {
            return false;
         }

         //
         int thisIdx = this.from;
         int thatIdx = that.from;
         while (thisLen-- > 0)
         {
            if (!thisNames[thisIdx++].equals(thatNames[thatIdx++]))
            {
               return false;
            }
         }

         //
         return true;
      }
      return false;
   }

   public int hashCode()
   {
      if (hashCode == null)
      {
         int value = 0;
         for (int i = to - 1; i >= from; i--)
         {
            value = value * 41 + names[i].hashCode();
         }
         hashCode = new Integer(value);
      }
      return hashCode.intValue();
   }

   /** Lexicographical order based implementation on the names atoms. */
   public int compareTo(Object o)
   {
      PortalObjectPath that = (PortalObjectPath)o;
      int index = 0;
      while (index < this.names.length && index < that.names.length)
      {
         String thisName = this.names[index];
         String thatName = that.names[index];
         int order = thisName.compareTo(thatName);
         if (order != 0)
         {
            return order;
         }
         index++;
      }
      return that.names.length - this.names.length;
   }

   /**
    * Return an iterator over the different names.
    *
    * @return the iterator over the names
    */
   public Iterator names()
   {
      return Tools.iterator(names, from, to);
   }

   /**
    * Returns the canonical representation.
    *
    * @return the string value
    */
   public String toString()
   {
      return toString(CANONICAL_FORMAT);
   }

   /**
    * Returns a string representation using a specified format
    *
    * @param format the output format
    * @return the string value
    */
   public String toString(PortalObjectPath.Format format)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(format, "Format");

      //
      if (format == LEGACY_FORMAT)
      {
         if (toStringLegacyFormat == null)
         {
            toStringLegacyFormat = LEGACY_FORMAT.toString(names, from, to);
         }
         return toStringLegacyFormat;
      }
      else if (format == CANONICAL_FORMAT)
      {
         if (toStringCanonicalFormat == null)
         {
            toStringCanonicalFormat = CANONICAL_FORMAT.toString(names, from, to);
         }
         return toStringCanonicalFormat;
      }

      //
      return format.toString(names, from, to);
   }

   public static PortalObjectPath parse(String value, PortalObjectPath.Format format)
   {
      return new PortalObjectPath(value, format);
   }

   /** The format of a string representation of an id. */
   public abstract static class Format
   {
      /**
       * @param value
       * @return
       */
      public abstract String[] parse(String value) throws IllegalArgumentException;

      /**
       * @param names
       * @return
       */
      public abstract String toString(String[] names, int from, int to) throws IllegalArgumentException;

      /**
       * @param id
       * @return
       */
      public final String toString(PortalObjectPath id) throws IllegalArgumentException
      {
         ParameterValidation.throwIllegalArgExceptionIfNull(id, "PortalObjectId");

         //
         return toString(id.names, id.from, id.to);
      }
   }

   /** Canonical format, smth like /a/b/c. */
   public static final PortalObjectPath.Format CANONICAL_FORMAT = new CanonicalFormat();

   public static final class CanonicalFormat extends Format
   {
      public static final char PATH_SEPARATOR = '/';

      public String[] parse(String value)
      {
         ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(value, "value", "Format.parse(value)");
         if (value.charAt(0) != PATH_SEPARATOR)
         {
            throw new IllegalArgumentException("Not a canonical value " + value);
         }
         if (value.length() == 1)
         {
            return new String[0];
         }

         //
         int length = 1;
         int previous = 1;
         while (true)
         {
            int next = value.indexOf(PATH_SEPARATOR, previous);
            if (next == -1)
            {
               break;
            }
            if (next > 0 && value.charAt(next - 1) != '\\')
            {
               length++;
            }
            previous = next + 1;
         }

         //
         String[] names = new String[length];
         length = 0;
         previous = 1;
         int next = 1;
         while (true)
         {
            next = value.indexOf(PATH_SEPARATOR, next);
            if (next == -1)
            {
               break;
            }
            if (next > 0 && value.charAt(next - 1) != '\\')
            {
               String name = value.substring(previous, next);
               name = decodeName(name);
               names[length++] = name;
               previous = next + 1;
            }
            next++;
         }
         String name = value.substring(previous);
         name = decodeName(name);
         names[length] = name;

         //
         return names;
      }

      public String toString(String[] names, int from, int to)
      {
         ParameterValidation.throwIllegalArgExceptionIfNull(names, "name string array");
         if (from == to)
         {
            return "" + PATH_SEPARATOR;
         }
         else
         {
            StringBuffer tmp = new StringBuffer(names.length * 10);
            for (int i = from; i < to; i++)
            {
               String name = names[i];
               if (name == null)
               {
                  throw new IllegalArgumentException("No null name expected in the name string array");
               }
               name = encodeName(name);
               tmp.append(PATH_SEPARATOR).append(name);
            }
            return tmp.toString();
         }
      }

      protected String decodeName(String name)
      {
         return name.replaceAll("\\/", "/");
      }

      protected String encodeName(String name)
      {
         return name.replaceAll("/", "\\/");
      }

   }

   ;

   /** The internal format when it is persisted, smth like a.b.c . */
   public static final PortalObjectPath.Format LEGACY_FORMAT = new PortalObjectPath.LegacyFormat();

   public static class LegacyFormat extends PortalObjectPath.Format
   {

      /** . */
      private final String[] EMPTY_STRING_ARRAY = new String[0];

      public String[] parse(String value)
      {
         ParameterValidation.throwIllegalArgExceptionIfNull(value, "value");

         //
         if (value.length() == 0)
         {
            return EMPTY_STRING_ARRAY;
         }

         // Count the number of names
         int length = 1;
         for (int next = value.indexOf('.'); next != -1; next = value.indexOf('.', next + 1))
         {
            if (next > 0 && value.charAt(next - 1) != '\\')
            {
               length++;
            }
         }

         //
         String[] names = new String[length];
         length = 0;
         int previous = 0;
         for (int next = value.indexOf('.'); next != -1; next = value.indexOf('.', next + 1))
         {
            if (next > 0 && value.charAt(next - 1) != '\\')
            {
               String name = value.substring(previous, next);
               name = decodeName(name);
               names[length++] = name;
               previous = next + 1;
            }
         }
         String name = value.substring(previous);
         name = decodeName(name);
         names[length] = name;

         //
         return names;
      }

      public String toString(String[] names, int from, int to)
      {
         ParameterValidation.throwIllegalArgExceptionIfNull(names, "name string array");

         //
         if (from == to)
         {
            return "";
         }

         //
         StringBuffer buffer = new StringBuffer((to - from) * 8);
         for (int i = from; i < to; i++)
         {
            if (i > 0)
            {
               buffer.append('.');
            }
            String name = names[i];
            if (name == null)
            {
               throw new IllegalArgumentException("No null name expected in the name string array");
            }
            name = encodeName(name);
            buffer.append(name);
         }
         return buffer.toString();
      }

      protected String decodeName(String name)
      {
         return name.replaceAll("\\\\\\.", ".");
      }

      protected String encodeName(String name)
      {
         return name.replaceAll("\\.", "\\\\\\.");
      }

   }

   public static final PortalObjectPath.Format LEGACY_BASE64_FORMAT = new PortalObjectPath.LegacyFormat()
   {

      protected String decodeName(String name)
      {
         try
         {
            byte[] bytes = Base64.decode(name);
            return new String(bytes, "UTF-8");
         }
         catch (UnsupportedEncodingException e)
         {
            throw new Error(e);
         }
      }

      protected String encodeName(String name)
      {
         try
         {
            byte[] bytes = name.getBytes("UTF-8");
            name = Base64.encodeBytes(bytes, Base64.EncodingOption.NOCARRIAGERETURN);
            return name;
         }
         catch (UnsupportedEncodingException e)
         {
            throw new Error(e);
         }
      }
   };

   /**
    * Should only use a-z0-9_
    */
   public static final Format SAFEST_FORMAT = new PortalObjectPath.LegacyFormat()
   {
      
      private final String EQUALS = "_e";
      private final String SLASH = "_s";
      private final String DOT = "_d";
      private final String PLUS = "_p";
      
      @Override
      public String[] parse(String value)
      {
         String uncoded = value.replace(EQUALS, "=");
         uncoded = uncoded.replace(SLASH, "/");
         uncoded = uncoded.replace(DOT, ".");
         uncoded = uncoded.replace(PLUS, "+");
         return LEGACY_BASE64_FORMAT.parse(uncoded);
      }
      
      @Override
      public String toString(String[] names, int from, int to)
      {
         String encoded = LEGACY_BASE64_FORMAT.toString(names, from, to);
         encoded = encoded.replace("=", EQUALS);
         encoded = encoded.replace("/", SLASH);
         encoded = encoded.replace(".", DOT);
         encoded = encoded.replace("+", PLUS);
         return encoded;
      }
   };

}
