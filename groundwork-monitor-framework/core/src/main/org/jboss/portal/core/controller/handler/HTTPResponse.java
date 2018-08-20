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
package org.jboss.portal.core.controller.handler;

import org.jboss.portal.common.io.IOTools;
import org.jboss.portal.common.util.MultiValuedPropertyMap;
import org.jboss.portal.server.ServerInvocationContext;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

/**
 * Response that sends a response to the http layer.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 12384 $
 */
public abstract class HTTPResponse extends HandlerResponse
{

   public abstract void sendResponse(ServerInvocationContext ctx) throws IOException, ServletException;

   public static HTTPResponse sendRedirect(final String redirect)
   {
      return new HTTPResponse()
      {
         public void sendResponse(ServerInvocationContext ctx) throws IOException
         {
            HttpServletResponse resp = ctx.getClientResponse();
            resp.sendRedirect(redirect);
         }
      };
   }

   public static HTTPResponse sendBinary(final String contentType, final long lastModified, final MultiValuedPropertyMap<String> properties, final InputStream in)
   {
      return new HTTPResponse()
      {
         public void sendResponse(ServerInvocationContext ctx) throws IOException
         {
            HttpServletResponse resp = ctx.getClientResponse();

            //
            resp.setContentType(contentType);

            //
            if (lastModified > 0)
            {
               resp.addDateHeader("Last-Modified", lastModified);
            }
            
            if (properties != null)
            {
            	for (String key: properties.keySet())
            	{
            		if (properties.getValue(key) != null)
            		{
                		resp.addHeader(key, properties.getValue(key));
            		}
            	}
            }

            //
            ServletOutputStream sout = null;
            try
            {
               sout = resp.getOutputStream();
               IOTools.copy(in, sout);
            }
            finally
            {
               IOTools.safeClose(in);
               IOTools.safeClose(sout);
            }
         }
      };
   }

   public static HTTPResponse sendBinary(final String contentType, final long lastModified, final MultiValuedPropertyMap<String> properties, final Reader reader)
   {
      return new HTTPResponse()
      {
         public void sendResponse(ServerInvocationContext ctx) throws IOException
         {
            HttpServletResponse resp = ctx.getClientResponse();

            //
            resp.setContentType(contentType);

            //
            if (lastModified > 0)
            {
               resp.addDateHeader("Last-Modified", lastModified);
            }

            if (properties != null)
            {
            	for (String key: properties.keySet())
            	{
            		if (properties.getValue(key) != null)
            		{
                		resp.addHeader(key, properties.getValue(key));
            		}
            	}
            }

            //
            Writer writer = null;
            try
            {
               writer = resp.getWriter();
               IOTools.copy(reader, writer);
            }
            finally
            {
               IOTools.safeClose(reader);
               IOTools.safeClose(writer);
            }
         }
      };
   }

   public static HTTPResponse sendForbidden()
   {
      return sendStatus(HttpServletResponse.SC_FORBIDDEN, null);
   }

   public static HTTPResponse sendNotFound()
   {
      return sendStatus(HttpServletResponse.SC_NOT_FOUND, null);
   }

   public static HTTPResponse sendError()
   {
      return sendStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null);
   }

   public static HTTPResponse sendForbidden(String message)
   {
      return sendStatus(HttpServletResponse.SC_FORBIDDEN, message);
   }

   public static HTTPResponse sendNotFound(String message)
   {
      return sendStatus(HttpServletResponse.SC_NOT_FOUND, message);
   }

   public static HTTPResponse sendError(String message)
   {
      return sendStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
   }

   private static HTTPResponse sendStatus(final int statusCode, final String message)
   {
      return new HTTPResponse()
      {
         public void sendResponse(ServerInvocationContext ctx) throws IOException
         {
            HttpServletResponse resp = ctx.getClientResponse();
            if (message == null)
            {
               resp.sendError(statusCode);
            }
            else
            {
               resp.sendError(statusCode, message);
            }
         }
      };
   }
}
