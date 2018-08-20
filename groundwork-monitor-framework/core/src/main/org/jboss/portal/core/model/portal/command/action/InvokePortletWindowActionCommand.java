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
import org.jboss.portal.common.util.ParameterMap;
import org.jboss.portal.core.controller.ControllerCommand;
import org.jboss.portal.core.controller.ControllerException;
import org.jboss.portal.core.controller.ControllerResponse;
import org.jboss.portal.core.controller.NoSuchResourceException;
import org.jboss.portal.core.controller.command.info.ActionCommandInfo;
import org.jboss.portal.core.controller.command.info.CommandInfo;
import org.jboss.portal.core.controller.portlet.ControllerPageNavigationalState;
import org.jboss.portal.core.controller.portlet.ControllerPortletControllerContext;
import org.jboss.portal.core.controller.portlet.ControllerResponseFactory;
import org.jboss.portal.core.model.content.Content;
import org.jboss.portal.core.model.instance.Instance;
import org.jboss.portal.core.model.portal.PortalObjectId;
import org.jboss.portal.core.model.portal.Window;
import org.jboss.portal.core.model.portal.command.response.UpdatePageResponse;
import org.jboss.portal.core.model.portal.navstate.PageNavigationalState;
import org.jboss.portal.core.navstate.NavigationalStateContext;
import org.jboss.portal.identity.User;
import org.jboss.portal.portlet.PortletInvokerException;
import org.jboss.portal.portlet.StateString;
import org.jboss.portal.portlet.info.PortletInfo;
import org.jboss.portal.portlet.controller.PortletController;
import org.jboss.portal.portlet.controller.request.PortletActionRequest;
import org.jboss.portal.portlet.controller.request.ContainerRequest;
import org.jboss.portal.portlet.controller.response.PageUpdateResponse;
import org.jboss.portal.portlet.controller.response.PortletResponse;
import org.jboss.portal.portlet.controller.response.ResourceResponse;
import org.jboss.portal.portlet.controller.state.PortletPageNavigationalState;
import org.jboss.portal.portlet.controller.state.PortletWindowNavigationalState;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 12352 $
 */
public class InvokePortletWindowActionCommand extends InvokePortletWindowCommand
{
   /** . */
   private static final CommandInfo info = new ActionCommandInfo(false);

   /** . */
   private StateString interactionState;

   /** . */
   private ParameterMap formParameters;

   /** The instance. */
   protected Instance instance;

   public InvokePortletWindowActionCommand(
      PortalObjectId windowId,
      Mode mode,
      WindowState windowState,
      StateString navigationalState,
      StateString interactionState,
      ParameterMap formParameters)
      throws IllegalArgumentException
   {
      super(windowId, mode, windowState, navigationalState);

      //
      this.interactionState = interactionState;
      this.formParameters = formParameters;
   }

   public StateString getInteractionState()
   {
      return interactionState;
   }

   public void setInteractionState(StateString interactionState)
   {
      this.interactionState = interactionState;
   }

   public ParameterMap getFormParameters()
   {
      return formParameters;
   }

   public void setFormParameters(ParameterMap formParameters)
   {
      this.formParameters = formParameters;
   }

   public CommandInfo getInfo()
   {
      return info;
   }

   public void acquireResources() throws NoSuchResourceException
   {
      super.acquireResources();
      instance = getInstance(window);

      // No instance means we can't continue
      if (instance == null)
      {
         String ref = null;
         Content content = window.getContent();
         if (content != null)
         {
            ref = content.getURI();
         }
         if (ref == null)
         {
            ref = window.getId().toString();
         }
         throw new NoSuchResourceException(ref);
      }
   }

   private Instance getInstance(Window window)
   {
      // We need the user id
      User user = getControllerContext().getUser();

      // Get instance
      return context.getController().getCustomizationManager().getInstance(window, user);
   }

   protected ContainerRequest createPortletRequest(PortletInfo portletInfo, PortletPageNavigationalState pageNS, PortletWindowNavigationalState windowNS)
   {
      return new PortletActionRequest(
         window.getName(),
         interactionState,
         formParameters,
         windowNS,
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

         PortletResponse portletResponse = (PortletResponse)cr;

         if (cr instanceof PageUpdateResponse)
         {
        	 //
        	 PageUpdateResponse pageUpdate = (PageUpdateResponse)cr;

        	 //
        	 ControllerPageNavigationalState pageNavigationalState = (ControllerPageNavigationalState)pageUpdate.getPageNavigationalState();

        	 // Flush all NS
        	 pageNavigationalState.flushUpdates();
         }
         // Populate the parameters
         NavigationalStateContext ctx = (NavigationalStateContext)cpcc.getControllerContext().getAttributeResolver(ControllerCommand.NAVIGATIONAL_STATE_SCOPE);
         
         PageNavigationalState pns = ctx.getPageNavigationalState(page.getId().toString());

         //
         return ControllerResponseFactory.createActionResponse(targetId, portletResponse.getResponse(), portletInfo, pns);
      }
      catch (PortletInvokerException e)
      {
         return ControllerResponseFactory.createResponse(e);
      }
   }

}
