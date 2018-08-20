/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2008, Red Hat Middleware, LLC, and individual                    *
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

package org.jboss.portal.core.metadata.portlet;

import org.jboss.portal.common.io.UndeclaredIOException;
import org.jboss.portal.common.io.WriterCharWriter;
import org.jboss.portal.common.text.AbstractCharEncoder;
import org.jboss.portal.common.text.CharEncoder;
import org.jboss.portal.common.text.CharWriter;
import org.jboss.portal.common.text.EncodingException;
import org.jboss.portal.common.text.FastURLEncoder;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * A markup attribute.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 7228 $
 */
public class MarkupAttribute
{

   /** . */
   private final String name;

   /** . */
   private final String value;

   /** . */
   final Type type;

   /**
    * @param name  the attribute name
    * @param value the attribute value
    * @param type  the attribute type
    */
   public MarkupAttribute(String name, String value, Type type)
   {
      if (name == null)
      {
         throw new IllegalArgumentException("No null name accepted");
      }
      if (value == null)
      {
         throw new IllegalArgumentException("No null value accepted");
      }
      if (type == null)
      {
         throw new IllegalArgumentException("No null type accepted");
      }
      this.name = name;
      this.value = value;
      this.type = type;
   }

   @Override
   public String toString()
   {
      return toString("");
   }

   public String getName()
   {
      return name;
   }

   public String getValue()
   {
      return value;
   }

   public String getEncodedValue()
   {
      StringWriter tmp = new StringWriter(32);
      encodeValueTo(null, tmp);
      return tmp.toString();
   }

   public String getEncodedValue(String contextPath)
   {
      StringWriter writer = new StringWriter(64);
      encodeValueTo(contextPath, writer);
      return writer.toString();
   }

   private void encodeValueTo(String contextPath, Writer writer)
   {
      if (contextPath != null && type == Type.URI && value.startsWith("/"))
      {
         Type.URI.encode(contextPath, writer);
      }

      type.encode(getValue(), writer);
   }

   public Type getType()
   {
      return type;
   }

   public void write(String urlPrefix, Writer writer) throws UndeclaredIOException
   {
      if (urlPrefix == null)
      {
         throw new IllegalArgumentException("No context path provided");
      }
      if (writer == null)
      {
         throw new IllegalArgumentException("No writer provided");
      }
      try
      {
         writer.write(name);
         writer.write("=\"");

         encodeValueTo(urlPrefix, writer);
         writer.write('"');
      }
      catch (IOException e)
      {
         throw new UndeclaredIOException(e);
      }
   }

   public String toString(String contextPath)
   {
      StringWriter buffer = new StringWriter(128);
      write(contextPath, buffer);
      return buffer.toString();
   }

   /** The type of the attribute value. */
   public abstract static class Type
   {

      /**
       * Encode the string in the proper format according to the type.
       *
       * @param string the string to encode
       * @return the encoded string
       */
      public abstract void encode(String string, Writer writer) throws UndeclaredIOException;

      private static class CDATAType extends Type
      {
         public void encode(String string, Writer writer)
         {
            try
            {
               writer.write(string);
            }
            catch (IOException e)
            {
               throw new UndeclaredIOException(e);
            }
         }
      }

      /**
       * CDATA is a sequence of characters from the document character set and may include character entities. User
       * agents should interpret attribute values as follows: <ul> <li>Replace character entities with characters,</li>
       * <li>Ignore line feeds,</li> <li>Replace each carriage return or tab with a single space.</li> </ul>
       */
      public static final Type CDATA = new CDATAType();

      /**
       * NAME tokens must begin with a letter ([A-Za-z]) and may be followed by any number of letters, digits ([0-9]),
       * hyphens ("-"), underscores ("_"), colons (":"), and periods (".").
       */
      public static final Type NAME = new CDATAType();

      /** %ContentType required : CDATA -- media type, as per [RFC2045]. */
      public static final Type CONTENT_TYPE = new CDATAType();

      private static final char[] SLASH_ARRAY = "/".toCharArray();

      /** %URI : CDATA -- a Uniform Resource Identifier, see [URI]. */
      public static final Type URI = new CDATAType()
      {
         {
            // Patches the encoder to let '/' not being encoded
            encoder = new AbstractCharEncoder()
            {
               protected void safeEncode(char c, CharWriter writer) throws EncodingException
               {
                  if (c == '/')
                  {
                     writer.append(SLASH_ARRAY);
                  }
                  else
                  {
                     FastURLEncoder.getUTF8Instance().encode(c, writer);
                  }
               }

               @Override
               protected void safeEncode(char[] chars, int off, int len, CharWriter writer) throws EncodingException
               {
                  //
                  int to = off + len;

                  // Perform lookup char by char
                  for (int current = off; current < to; current++)
                  {
                     if (chars[current] == '/')
                     {
                         writer.append(SLASH_ARRAY);
                     }
                     else
                     {
                        writer.append(chars[current]);
                     }
                  }
               }
            };
         }

         /** Our encoder for URI. */
         final CharEncoder encoder;

         public void encode(String string, Writer writer)
         {
            encoder.encode(string, new WriterCharWriter(writer));
         }
      };

      /** %LinkTypes : CDATA -- space-separated list of link types. */
      public static final Type LINK_TYPES = new CDATAType();

      /** %Text : CDATA : CDATA. */
      public static final Type TEXT = new CDATAType();

      /** %MediaDesc : CDATA -- single or comma-separated list of media descriptors. */
      public static final Type MEDIA_DESC = new CDATAType();

      /** %URI : CDATA -- a Uniform Resource Identifier, see [URI]. */
      public static final Type HREF = new CDATAType();
   }
}
