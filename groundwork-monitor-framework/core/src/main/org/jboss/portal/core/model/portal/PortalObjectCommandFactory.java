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
import org.jboss.portal.common.util.ParameterMap;
import org.jboss.portal.core.controller.ControllerCommand;
import org.jboss.portal.core.controller.ControllerContext;
import org.jboss.portal.core.controller.command.mapper.AbstractCommandFactory;
import org.jboss.portal.core.model.portal.command.action.ImportPageToDashboardCommand;
import org.jboss.portal.core.model.portal.command.action.InvokePortletWindowActionCommand;
import org.jboss.portal.core.model.portal.command.action.InvokePortletWindowRenderCommand;
import org.jboss.portal.core.model.portal.command.action.InvokePortletWindowResourceCommand;
import org.jboss.portal.core.model.portal.command.mapping.PortalObjectPathMapper;
import org.jboss.portal.core.model.portal.command.view.ViewContextCommand;
import org.jboss.portal.core.model.portal.command.view.ViewPageCommand;
import org.jboss.portal.core.model.portal.command.view.ViewPortalCommand;
import org.jboss.portal.core.model.portal.navstate.WindowNavigationalState;
import org.jboss.portal.core.navstate.NavigationalStateKey;
import org.jboss.portal.core.portlet.PortletRequestDecoder;
import org.jboss.portal.server.ServerInvocation;

/**
 * This command mapper is used to map portal objects living in a container to <code>org.jboss.portal.core.command.RenderPageCommand</code>
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 11549 $
 */
public class PortalObjectCommandFactory extends AbstractCommandFactory
{

   /** . */
   private PortalObjectPathMapper mapper;

   public PortalObjectCommandFactory()
   {
   }

   public PortalObjectPathMapper getMapper()
   {
      return mapper;
   }

   public void setMapper(PortalObjectPathMapper mapper)
   {
      this.mapper = mapper;
   }

   public ControllerCommand doMapping(ControllerContext controllerContext, ServerInvocation invocation, String host, String contextPath, String requestPath)
   {
      if (requestPath == null)
      {
         return null;
      }

      //
      Object target = mapper.getTarget(controllerContext, requestPath);
      ParameterMap queryParams = invocation.getServerContext().getQueryParameterMap();

      //
      ControllerCommand cmd;
      if (target instanceof Window)
      {
         Window window = (Window)target;

         //
         PortletRequestDecoder decoder = new PortletRequestDecoder();
         decoder.decode(queryParams, invocation.getServerContext().getBodyParameterMap());

         //
         if (decoder.getType() == PortletRequestDecoder.RESOURCE_TYPE)
         {
            cmd = new InvokePortletWindowResourceCommand(
               window.getId(),
               decoder.getCacheability(),
               decoder.getResourceId(),
               decoder.getResourceState(),
               decoder.getForm());
         }
         else
         {
            // Get the window navigational state
            NavigationalStateKey nsKey = new NavigationalStateKey(WindowNavigationalState.class, window.getId());
            WindowNavigationalState windowNavState = (WindowNavigationalState)controllerContext.getAttribute(ControllerCommand.NAVIGATIONAL_STATE_SCOPE, nsKey);
            if (windowNavState == null)
            {
               windowNavState = WindowNavigationalState.create();
               controllerContext.setAttribute(ControllerCommand.NAVIGATIONAL_STATE_SCOPE, nsKey, windowNavState);
            }

            //
            WindowState windowState = decoder.getWindowState();
            if (windowState == null)
            {
               windowState = windowNavState.getWindowState();
            }

            //
            Mode mode = decoder.getMode();
            if (mode == null)
            {
               mode = windowNavState.getMode();
            }

            //
            switch (decoder.getType())
            {
               case PortletRequestDecoder.NAV_TYPE:
                  cmd = new InvokePortletWindowRenderCommand(window.getId(), mode, windowState);
                  break;
               case PortletRequestDecoder.ACTION_TYPE:
                  cmd = new InvokePortletWindowActionCommand(window.getId(), mode, windowState, decoder.getNavigationalState(), decoder.getInteractionState(), decoder.getForm());
                  break;
               case PortletRequestDecoder.RENDER_TYPE:
                  cmd = new InvokePortletWindowRenderCommand(window.getId(), mode, windowState, decoder.getNavigationalState());
                  break;
               default:
                  throw new AssertionError();
            }
         }
      }
      else
      {
         if (target instanceof Context)
         {
            Context context = (Context)target;
            return new ViewContextCommand(context.getId());
         }
         else if (target instanceof Portal)
         {
            Portal portal = (Portal)target;
            return new ViewPortalCommand(portal.getId());
         }
         else
         {
            Page page = (Page)target;
            PortalObjectId id = page.getId();

            //
            String action = queryParams.getValue("action");

            //
            if ("import".equals(action))
            {
               cmd = new ImportPageToDashboardCommand(id);
            }
            else
            {
               cmd = new ViewPageCommand(id, queryParams);
            }
         }
      }

      //
      return cmd;
   }
}
