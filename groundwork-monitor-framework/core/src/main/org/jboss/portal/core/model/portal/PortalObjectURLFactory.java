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

import org.jboss.portal.Mode;
import org.jboss.portal.WindowState;
import org.jboss.portal.core.controller.ControllerCommand;
import org.jboss.portal.core.controller.ControllerContext;
import org.jboss.portal.core.controller.command.mapper.URLFactoryDelegate;
import org.jboss.portal.core.model.portal.command.PortalObjectCommand;
import org.jboss.portal.core.model.portal.command.action.ImportPageToDashboardCommand;
import org.jboss.portal.core.model.portal.command.action.InvokePortletWindowActionCommand;
import org.jboss.portal.core.model.portal.command.action.InvokeWindowCommand;
import org.jboss.portal.core.model.portal.command.action.InvokePortletWindowCommand;
import org.jboss.portal.core.model.portal.command.action.InvokePortletWindowResourceCommand;
import org.jboss.portal.core.model.portal.command.mapping.PortalObjectPathMapper;
import org.jboss.portal.core.model.portal.command.view.ViewPageCommand;
import org.jboss.portal.core.model.portal.command.view.ViewPortalCommand;
import org.jboss.portal.core.portlet.PortletRequestEncoder;
import org.jboss.portal.portlet.StateString;
import org.jboss.portal.portlet.cache.CacheLevel;
import org.jboss.portal.server.AbstractServerURL;
import org.jboss.portal.server.ServerInvocation;
import org.jboss.portal.server.ServerURL;

import java.util.Map;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 11549 $
 */
public class PortalObjectURLFactory extends URLFactoryDelegate
{

   /** . */
   private String path;

   /** . */
   private String namespace;

   /** . */
   private PortalObjectPathMapper mapper;

   /** . */
   private String effectiveNamespace;

   public String getPath()
   {
      return path;
   }

   public void setPath(String path)
   {
      this.path = path;
   }

   public String getNamespace()
   {
      return namespace;
   }

   public void setNamespace(String namespace)
   {
      this.namespace = namespace;
   }

   public PortalObjectPathMapper getMapper()
   {
      return mapper;
   }

   public void setMapper(PortalObjectPathMapper mapper)
   {
      this.mapper = mapper;
   }


   protected void startService() throws Exception
   {
      super.startService();

      //
      effectiveNamespace = namespace == null ? "" : namespace;
   }

   public ServerURL doMapping(ControllerContext controllerContext, ServerInvocation invocation, ControllerCommand cmd)
   {
      if (cmd == null)
      {
         throw new IllegalArgumentException("No null command accepted");
      }

      //
      if (cmd instanceof PortalObjectCommand)
      {
         PortalObjectCommand poc = (PortalObjectCommand)cmd;

         //
         PortalObjectId targetId = poc.getTargetId();

         //
         String targetNamespace = targetId.getNamespace();

         //
         if (effectiveNamespace.equals(targetNamespace))
         {
            // Get base URL
            ServerURL url = getBaseURL(targetId);

            // Customize further more
            if (cmd instanceof ViewPageCommand)
            {
               ViewPageCommand vpCmd = (ViewPageCommand)cmd;
               Map<String, String[]> parameters = vpCmd.getParameters();
               url.getParameterMap().putAll(parameters);
            }
            else if (cmd instanceof ViewPortalCommand)
            {
               // Nothing for now, we let the statement just to show that it handles page rendering commands
            }
            else if (cmd instanceof InvokeWindowCommand)
            {
               InvokeWindowCommand iwCmd = (InvokeWindowCommand)cmd;

               //
               PortletRequestEncoder encoder = new PortletRequestEncoder(url.getParameterMap());

               //
               if (cmd instanceof InvokePortletWindowCommand)
               {
                  InvokePortletWindowCommand ipwCmd = (InvokePortletWindowCommand)iwCmd;

                  //
                  Mode mode = ipwCmd.getMode();
                  WindowState windowState = ipwCmd.getWindowState();
                  StateString navigationalState = ((InvokePortletWindowCommand)iwCmd).getNavigationalState();

                  //
                  if (iwCmd instanceof InvokePortletWindowActionCommand)
                  {
                     StateString interactionState = ((InvokePortletWindowActionCommand)iwCmd).getInteractionState();

                     //
                     encoder.encodeAction(navigationalState, interactionState, mode, windowState);
                  }
                  else
                  {
                     encoder.encodeRender(navigationalState, mode, windowState);
                  }
               }
               else
               {
                  InvokePortletWindowResourceCommand ipwrCmd = (InvokePortletWindowResourceCommand)iwCmd;

                  //
                  StateString resourceState = ipwrCmd.getResourceState();
                  String resourceId = ipwrCmd.getResourceId();
                  CacheLevel cacheability = ipwrCmd.getCacheability();

                  //
                  encoder.encodeResource(cacheability, resourceId, resourceState);
               }
            }
            else if (cmd instanceof ImportPageToDashboardCommand)
            {
               url.setParameterValue("action", "import");
            }

            //
            return url;
         }
      }

      //
      return null;
   }

   private AbstractServerURL getBaseURL(PortalObjectId id)
   {
      //
      StringBuffer buffer = new StringBuffer();

      //
      if (path != null && path.length() > 0)
      {
         buffer.append(path);
      }

      //
      mapper.appendPath(buffer, id);

      //
      AbstractServerURL asu = new AbstractServerURL();
      asu.setPortalRequestPath(buffer.toString());

      //
      return asu;
   }
}
