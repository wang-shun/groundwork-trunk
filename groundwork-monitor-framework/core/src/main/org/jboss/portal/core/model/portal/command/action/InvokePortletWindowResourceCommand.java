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
package org.jboss.portal.core.model.portal.command.action;

import org.jboss.portal.core.model.portal.PortalObjectId;
import org.jboss.portal.core.model.portal.command.response.UpdatePageResponse;
import org.jboss.portal.core.model.portal.navstate.PageNavigationalState;
import org.jboss.portal.core.navstate.NavigationalStateContext;
import org.jboss.portal.core.controller.ControllerCommand;
import org.jboss.portal.core.controller.ControllerException;
import org.jboss.portal.core.controller.ControllerResponse;
import org.jboss.portal.core.controller.command.info.CommandInfo;
import org.jboss.portal.core.controller.command.info.ActionCommandInfo;
import org.jboss.portal.core.controller.portlet.ControllerPageNavigationalState;
import org.jboss.portal.core.controller.portlet.ControllerPortletControllerContext;
import org.jboss.portal.core.controller.portlet.ControllerResponseFactory;
import org.jboss.portal.common.util.ParameterMap;
import org.jboss.portal.portlet.controller.PortletController;
import org.jboss.portal.portlet.controller.request.PortletResourceRequest;
import org.jboss.portal.portlet.controller.request.ContainerRequest;
import org.jboss.portal.portlet.controller.response.PageUpdateResponse;
import org.jboss.portal.portlet.controller.response.PortletResponse;
import org.jboss.portal.portlet.controller.response.ResourceResponse;
import org.jboss.portal.portlet.controller.state.PortletPageNavigationalState;
import org.jboss.portal.portlet.controller.state.PortletWindowNavigationalState;
import org.jboss.portal.portlet.cache.CacheLevel;
import org.jboss.portal.portlet.PortletInvokerException;
import org.jboss.portal.portlet.StateString;
import org.jboss.portal.portlet.info.PortletInfo;

/**
 * @author <a href="mailto:julien@jboss-portal.org">Julien Viet</a>
 * @version $Revision: 630 $
 */
public class InvokePortletWindowResourceCommand extends InvokeWindowCommand
{

   /** . */
   private static final CommandInfo info = new ActionCommandInfo(true);

   /** . */
   private final CacheLevel cacheability;

   /** . */
   private final String resourceId;

   /** . */
   private final StateString resourceState;

   /** . */
   private final ParameterMap resourceForm;

   public InvokePortletWindowResourceCommand(
      PortalObjectId windowId,
      CacheLevel cacheability,
      String resourceId,
      StateString resourceState,
      ParameterMap resourceForm) throws IllegalArgumentException
   {
      super(windowId);

      //
      if (cacheability == null)
      {
         throw new IllegalArgumentException("No null cache level accepted");
      }

      //
      this.cacheability = cacheability;
      this.resourceId = resourceId;
      this.resourceState = resourceState;
      this.resourceForm = resourceForm;
   }

   public CacheLevel getCacheability()
   {
      return cacheability;
   }

   public String getResourceId()
   {
      return resourceId;
   }

   public StateString getResourceState()
   {
      return resourceState;
   }

   public CommandInfo getInfo()
   {
      return info;
   }

   protected ContainerRequest createPortletRequest(PortletInfo portletInfo, PortletPageNavigationalState pageNS, PortletWindowNavigationalState windowNS)
   {
      PortletResourceRequest.Scope scope;

      // todo
      // added because of a bug in portlet controller that will throw NPE if the portlet
      // does not provide window NS during resource serving.
      if (windowNS == null)
      {
         windowNS = new PortletWindowNavigationalState();
      }

      //
      switch (cacheability)
      {
         case FULL:
            scope = new PortletResourceRequest.FullScope();
            break;
         case PORTLET:
            scope = new PortletResourceRequest.PortletScope(windowNS);
            break;
         case PAGE:
            scope = new PortletResourceRequest.PageScope(windowNS, pageNS);
            break;
         default:
            throw new AssertionError();
      }

      return new PortletResourceRequest(
         window.getName(),
         resourceId,
         resourceState,
         resourceForm,
         scope);
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
         ResourceResponse resourceResponse = (ResourceResponse)cr;
         
         // Populate the parameters
         NavigationalStateContext ctx = (NavigationalStateContext)cpcc.getControllerContext().getAttributeResolver(ControllerCommand.NAVIGATIONAL_STATE_SCOPE);
         
         PageNavigationalState pns = ctx.getPageNavigationalState(page.getId().toString());

         //
         return ControllerResponseFactory.createActionResponse(targetId, resourceResponse.getResponse(), portletInfo, pns);
      }
      catch (PortletInvokerException e)
      {
         return ControllerResponseFactory.createResponse(e);
      }
   }

}
