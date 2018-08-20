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
package org.jboss.portlet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

import javax.portlet.CacheControl;
import javax.portlet.PortletMode;
import javax.portlet.PortletURL;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceURL;
import javax.portlet.filter.PortletResponseWrapper;

import org.jboss.portal.api.PortalRuntimeContext;
import org.jboss.portal.api.node.PortalNode;
import org.jboss.portal.api.node.PortalNodeURL;
import org.jboss.portal.core.aspects.controller.node.Navigation;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 10309 $
 */
public class JBossRenderResponse extends PortletResponseWrapper implements RenderResponse
{
   private RenderResponse portletResponse;

   public JBossRenderResponse(RenderResponse portletResponse)
   {
      super(portletResponse);
      this.portletResponse = portletResponse;
   }         
            
   public PortalNodeURL createActionURL(PortalNode node) throws IllegalArgumentException, IllegalStateException
   {
      PortalRuntimeContext context = Navigation.getPortalRuntimeContext();

      //
      if (context == null)
      {
         throw new IllegalStateException("Not in a controller context");
      }

      //
      return node.createURL(context);
   }

   public PortalNodeURL createRenderURL(PortalNode node) throws IllegalArgumentException, IllegalStateException
   {
      PortalRuntimeContext context = Navigation.getPortalRuntimeContext();

      //
      if (context == null)
      {
         throw new IllegalStateException("Not in a controller context");
      }

      //
      return node.createURL(context);
   }
   
   public void setContentType(String arg0)
   {
      portletResponse.setContentType(arg0);
   }

   public void setNextPossiblePortletModes(Collection<PortletMode> arg0)
   {
      portletResponse.setNextPossiblePortletModes(arg0);
   }

   public void setTitle(String arg0)
   {
      portletResponse.setTitle(arg0);
   }

   public PortletURL createActionURL()
   {
      return portletResponse.createActionURL();
   }

   public PortletURL createRenderURL()
   {
      return portletResponse.createRenderURL();
   }

   public ResourceURL createResourceURL()
   {
      return portletResponse.createResourceURL();
   }

   public void flushBuffer() throws IOException
   {
      portletResponse.flushBuffer();
   }

   public int getBufferSize()
   {
      return portletResponse.getBufferSize();
   }

   public CacheControl getCacheControl()
   {
      return portletResponse.getCacheControl();
   }

   public String getCharacterEncoding()
   {
      return portletResponse.getCharacterEncoding();
   }

   public String getContentType()
   {
      return portletResponse.getContentType();
   }

   public Locale getLocale()
   {
      return portletResponse.getLocale();
   }

   public OutputStream getPortletOutputStream() throws IOException
   {
      return portletResponse.getPortletOutputStream();
   }

   public PrintWriter getWriter() throws IOException
   {
      return portletResponse.getWriter();
   }

   public boolean isCommitted()
   {
      return portletResponse.isCommitted();
   }

   public void reset()
   {
      portletResponse.reset();
   }

   public void resetBuffer()
   {
      portletResponse.resetBuffer();
   }

   public void setBufferSize(int arg0)
   {
      portletResponse.setBufferSize(arg0);
   }
}
