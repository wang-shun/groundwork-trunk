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
package org.jboss.portal.core.model.portal.command.action;

import org.jboss.portal.Mode;
import org.jboss.portal.WindowState;
import org.jboss.portal.core.controller.ControllerException;
import org.jboss.portal.core.controller.ControllerResponse;
import org.jboss.portal.core.controller.command.info.CommandInfo;
import org.jboss.portal.core.controller.command.info.ActionCommandInfo;
import org.jboss.portal.core.controller.portlet.ControllerPageNavigationalState;
import org.jboss.portal.core.controller.portlet.ControllerPortletControllerContext;
import org.jboss.portal.core.controller.portlet.ControllerResponseFactory;
import org.jboss.portal.core.model.portal.PortalObjectId;
import org.jboss.portal.core.model.portal.command.response.UpdatePageResponse;
import org.jboss.portal.portlet.PortletInvokerException;
import org.jboss.portal.portlet.StateString;
import org.jboss.portal.portlet.ParametersStateString;
import org.jboss.portal.portlet.info.PortletInfo;
import org.jboss.portal.portlet.info.ParameterInfo;
import org.jboss.portal.portlet.controller.PortletController;
import org.jboss.portal.portlet.controller.request.PortletRenderRequest;
import org.jboss.portal.portlet.controller.request.ContainerRequest;
import org.jboss.portal.portlet.controller.response.PageUpdateResponse;
import org.jboss.portal.portlet.controller.response.PortletResponse;
import org.jboss.portal.portlet.controller.response.ResourceResponse;
import org.jboss.portal.portlet.controller.state.PortletPageNavigationalState;
import org.jboss.portal.portlet.controller.state.PortletWindowNavigationalState;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

/**
 * Simply update the navigational state of the window. No invocation to the underlying is done.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 12314 $
 */
public class InvokePortletWindowRenderCommand extends InvokePortletWindowCommand
{

   /** . */
   private static final String[] REMOVED_PARAMETER = new String[0];

   /** . */
   private static final CommandInfo info = new ActionCommandInfo(true);

   public InvokePortletWindowRenderCommand(
      PortalObjectId windowId,
      Mode mode,
      WindowState windowState,
      StateString navigationalState)
      throws IllegalArgumentException
   {
      super(windowId, mode, windowState, navigationalState);
   }

   public InvokePortletWindowRenderCommand(
      PortalObjectId windowId,
      Mode mode,
      WindowState windowState)
      throws IllegalArgumentException
   {
      super(windowId, mode, windowState, null);
   }

   public CommandInfo getInfo()
   {
      return info;
   }

   protected ContainerRequest createPortletRequest(
      PortletInfo portletInfo, PortletPageNavigationalState pageNS,
      PortletWindowNavigationalState windowNS)
   {
      Mode newMode = null;
      WindowState newWindowState = null;
      StateString newNavigationalState = null;

      //
      if (windowNS != null)
      {
         newMode = windowNS.getMode();
         newWindowState = windowNS.getWindowState();
         newNavigationalState = windowNS.getPortletNavigationalState();
      }

      //
      if (mode != null)
      {
         newMode = mode;
      }
      if (windowState != null)
      {
         newWindowState = windowState;
      }
      if (navigationalState != null)
      {
         newNavigationalState = navigationalState;
      }

      // Compute the public navigational state changes
      Map<String, String[]> publicChanges = Collections.emptyMap();

      //
      if (navigationalState instanceof ParametersStateString)
      {
         ParametersStateString navigationalParameters = (ParametersStateString)navigationalState;

         //
         Map<String, String[]> parameters = navigationalParameters.getParameters();

         //
         for (ParameterInfo parameterInfo : portletInfo.getNavigation().getPublicParameters())
         {
            String key = parameterInfo.getId();

            //
            String[] values = parameters.remove(key);

            //
            if (values == null)
            {
               values = REMOVED_PARAMETER;
            }

            // Lazy create
            if (publicChanges.isEmpty())
            {
               publicChanges = new HashMap<String, String[]>();
            }

            //
            publicChanges.put(key, values);
         }
      }

      //
      return new PortletRenderRequest(
         window.getName(),
         new PortletWindowNavigationalState(newNavigationalState, newMode, newWindowState),
         publicChanges,
         pageNS
      );
   }
   
   public ControllerResponse execute() throws ControllerException
   {
      try
      {
         ControllerPortletControllerContext cpcc = new ControllerPortletControllerContext(
            context,
            page
         );

         //
         PortletPageNavigationalState pageNS = cpcc.getStateControllerContext().createPortletPageNavigationalState(false);

         //
         PortletWindowNavigationalState windowNS = pageNS.getPortletWindowNavigationalState(window.getName());

         //
         PortletInfo portletInfo = cpcc.getPortletInfo(window.getName());

         //
         ContainerRequest containerRequest = createPortletRequest(portletInfo, pageNS, windowNS);

         //
         PortletController controller = new PortletController();

         //
         org.jboss.portal.portlet.controller.response.ControllerResponse cr = controller.process(cpcc, containerRequest);

         //
         PageUpdateResponse pageUpdate = (PageUpdateResponse)cr;

         //
         ControllerPageNavigationalState pageNavigationalState = (ControllerPageNavigationalState)pageUpdate.getPageNavigationalState();

         // Flush all NS
         pageNavigationalState.flushUpdates();

         //
         return new UpdatePageResponse(page.getId());

      }
      catch (PortletInvokerException e)
      {
         return ControllerResponseFactory.createResponse(e);
      }
   }

}