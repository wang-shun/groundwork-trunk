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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.portlet.ActionRequest;
import javax.portlet.filter.PortletRequestWrapper;

import org.jboss.portal.api.node.PortalNode;
import org.jboss.portal.core.aspects.controller.BackwardCompatibilityInterceptor;
import org.jboss.portal.core.aspects.controller.node.Navigation;
import org.jboss.portal.core.controller.ControllerContext;
import org.jboss.portal.core.controller.portlet.ControllerUserContext;
import org.jboss.portal.identity.User;
import org.jboss.portal.portlet.spi.UserContext;
import org.jboss.portlet.util.Parameters;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 11737 $
 */
public class JBossActionRequest
   extends PortletRequestWrapper implements ActionRequest
{

   /** The parameters. */
   private Parameters blah;
   
   private ActionRequest portletRequest;

   /** . */
   private ControllerContext controllerContext;

   private UserContext userContext;

   public JBossActionRequest(ActionRequest portletRequest)
   {
      super(portletRequest);
      this.portletRequest = portletRequest;
      this.controllerContext = BackwardCompatibilityInterceptor.controllerContextTL.get();
      this.blah = null;
      this.userContext = org.jboss.portal.core.aspects.portlet.BackwardCompatibilityInterceptor.userContextTL.get();
   }

   public ControllerContext getControllerContext()
   {
      if (controllerContext == null)
      {
         throw new IllegalStateException("No controller context");
      }
      return controllerContext;
   }

   public PortalNode getPortalNode()
   {
      return Navigation.getCurrentNode();
   }

   /** Returns the current authenticated user or null if the user is not authenticated */
   public User getUser()
   {
      if (userContext instanceof ControllerUserContext)
      {
         return ((ControllerUserContext)userContext).getUser();
      }
      else
      {
         return null;
      }
   }

   public Parameters getParameters()
   {
      if (blah == null)
      {
         blah = new Parameters(getParameterMap());
      }
      return blah;
   }

   public String getCharacterEncoding()
   {
      return portletRequest.getCharacterEncoding();
   }

   public int getContentLength()
   {
      return portletRequest.getContentLength();
   }

   public String getContentType()
   {
      return portletRequest.getContentType();
   }

   public String getMethod()
   {
      return portletRequest.getMethod();
   }

   public InputStream getPortletInputStream() throws IOException
   {
      return portletRequest.getPortletInputStream();
   }

   public BufferedReader getReader() throws UnsupportedEncodingException, IOException
   {
      return portletRequest.getReader();
   }

   public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException
   {
      portletRequest.setCharacterEncoding(arg0);
   }

}
