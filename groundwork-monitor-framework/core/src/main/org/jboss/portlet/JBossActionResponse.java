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
import java.io.Serializable;
import java.util.Map;

import javax.portlet.ActionResponse;
import javax.portlet.PortletMode;
import javax.portlet.PortletModeException;
import javax.portlet.WindowState;
import javax.portlet.WindowStateException;
import javax.portlet.filter.PortletResponseWrapper;
import javax.xml.namespace.QName;

import org.jboss.portal.api.PortalRuntimeContext;
import org.jboss.portal.api.node.PortalNode;
import org.jboss.portal.api.node.PortalNodeURL;
import org.jboss.portal.core.CoreConstants;
import org.jboss.portal.core.aspects.controller.node.Navigation;
import org.jboss.portal.core.controller.portlet.CoreEventControllerContext;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 11745 $
 */
public class JBossActionResponse  extends PortletResponseWrapper implements ActionResponse
{
   private ActionResponse portletResponse;

   public JBossActionResponse(ActionResponse portletResponse)
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

   public void sendRedirect(String arg0) throws IOException
   {
      portletResponse.sendRedirect(arg0);
   }

   public void sendRedirect(String arg0, String arg1) throws IOException
   {
      portletResponse.sendRedirect(arg0, arg1);
   }

   public PortletMode getPortletMode()
   {
      return portletResponse.getPortletMode();
   }

   public Map<String, String[]> getRenderParameterMap()
   {
      return portletResponse.getRenderParameterMap();
   }

   public WindowState getWindowState()
   {
      return portletResponse.getWindowState();
   }

   public void removePublicRenderParameter(String arg0)
   {
      portletResponse.removePublicRenderParameter(arg0);
   }

   public void setEvent(QName arg0, Serializable arg1)
   {
      portletResponse.setEvent(arg0, arg1);
   }

   public void setEvent(String arg0, Serializable arg1)
   {
      portletResponse.setEvent(arg0, arg1);
   }

   public void setPortletMode(PortletMode arg0) throws PortletModeException
   {
      portletResponse.setPortletMode(arg0);
   }

   public void setRenderParameter(String arg0, String arg1)
   {
      portletResponse.setRenderParameter(arg0, arg1);
   }

   public void setRenderParameter(String arg0, String[] arg1)
   {
      portletResponse.setRenderParameter(arg0, arg1);
   }

   public void setRenderParameters(Map<String, String[]> arg0)
   {
      portletResponse.setRenderParameters(arg0);
   }

   public void setWindowState(WindowState arg0) throws WindowStateException
   {
      portletResponse.setWindowState(arg0);
   }

   /**
    * Perform a programmatic sign out.
    *
    * @deprecated Use the QName("urn:jboss:portal", "signOut") event instead
    * @throws IllegalStateException if programmatic signout cannot be done
    */
   public void signOut() throws IllegalStateException
   {      
      setEvent(CoreConstants.JBOSS_PORTAL_SIGN_OUT, null);
   }
   
   /**
    * Perform a programmatic sign out and navigate to the URL specified by the location parameter.
    *
    * @deprecated Use the QName("urn:jboss:portal", "signOut") event instead (Pass the redirection URL as payload)
    * @param location URL to redirect to after signout; can be null to indicate default behavior.
    * @throws IllegalStateException    if programmatic signout cannot be done
    * @throws IllegalArgumentException if the location is not valid
    */
   public void signOut(String location) throws IllegalStateException
   {
      setEvent(CoreConstants.JBOSS_PORTAL_SIGN_OUT, location);

   }

}
