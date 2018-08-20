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
package org.jboss.portal.core.controller.command.response;

import org.jboss.portal.common.util.MultiValuedPropertyMap;
import org.jboss.portal.core.controller.ControllerResponse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 12384 $
 */
public class StreamContentResponse extends ControllerResponse
{

   /** . */
   private final String contentType;
   
   /**
    * The time the content was last modified, measured in milliseconds since
    * the epoch (00:00:00 GMT, January 1, 1970). If the value is not greater than zero then
    * the last modified field does not represent a valid last modified date and it is left
    * up to the streamer to interpret it.
    */
   private final long lastModified;

   /** . */
   private final InputStream inputStream;
   
   /** . */
   private MultiValuedPropertyMap<String> properties;

   /** . */
   private final Reader reader;

   public StreamContentResponse(String contentType, long lastModified, InputStream inputStream)
   {
      if (contentType == null)
      {
         throw new IllegalArgumentException();
      }
      if (inputStream == null)
      {
         throw new IllegalArgumentException();
      }

      //
      this.contentType = contentType;
      this.lastModified = lastModified;
      this.inputStream = inputStream;
      this.reader = null;
   }

   public StreamContentResponse(String contentType, long lastModified, Reader reader)
   {
      if (contentType == null)
      {
         throw new IllegalArgumentException();
      }
      if (reader == null)
      {
         throw new IllegalArgumentException();
      }

      //
      this.contentType = contentType;
      this.lastModified = lastModified;
      this.inputStream = null;
      this.reader = reader;
   }

   public StreamContentResponse(String contentType,
		   MultiValuedPropertyMap<String> properties,
		   ByteArrayInputStream inputStream) {

	   this(contentType, -1, inputStream);
	   this.properties = properties;
   }

   public StreamContentResponse(String contentType,
		   MultiValuedPropertyMap<String> properties,
		   Reader reader) {
	   this(contentType, -1, reader);
	   this.properties = properties;
   }
   

   public String getContentType()
   {
      return contentType;
   }
   
   public long getLastModified()
   {
      return lastModified;
   }

   public InputStream getInputStream()
   {
      return inputStream;
   }

   public Reader getReader()
   {
      return reader;
   }
   
   public MultiValuedPropertyMap<String> getProperties()
   {
	   return properties;
   }
}
