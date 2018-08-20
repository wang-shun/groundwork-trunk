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
package org.jboss.portal.core.controller.portlet;

import org.jboss.portal.Mode;
import org.jboss.portal.WindowState;
import org.jboss.portal.common.util.MarkupInfo;
import org.jboss.portal.common.util.ParameterMap;
import org.jboss.portal.core.controller.ControllerCommand;
import org.jboss.portal.core.controller.ControllerContext;
import org.jboss.portal.core.model.portal.Portal;
import org.jboss.portal.core.model.portal.Window;
import org.jboss.portal.core.model.portal.command.action.InvokePortletWindowActionCommand;
import org.jboss.portal.core.model.portal.command.action.InvokePortletWindowRenderCommand;
import org.jboss.portal.core.model.portal.command.action.InvokePortletWindowResourceCommand;
import org.jboss.portal.portlet.ActionURL;
import org.jboss.portal.portlet.ContainerURL;
import org.jboss.portal.portlet.ParametersStateString;
import org.jboss.portal.portlet.RenderURL;
import org.jboss.portal.portlet.ResourceURL;
import org.jboss.portal.portlet.StateString;
import org.jboss.portal.portlet.controller.state.PortletPageNavigationalState;
import org.jboss.portal.portlet.impl.spi.AbstractPortletInvocationContext;
import org.jboss.portal.portlet.invocation.ActionInvocation;
import org.jboss.portal.portlet.invocation.PortletInvocation;
import org.jboss.portal.portlet.invocation.RenderInvocation;
import org.jboss.portal.portlet.invocation.ResourceInvocation;
import org.jboss.portal.portlet.spi.PortletInvocationContext;
import org.jboss.portal.portlet.spi.UserContext;
import org.jboss.portal.server.request.URLContext;
import org.jboss.portal.server.request.URLFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 11737 $
 */
public class PortletInvocationFactory
{

   public static InvokePortletCommandFactory createInvokePortletCommandFactory(
           ControllerContext controllerContext,
           Window window,
           PortletPageNavigationalState pageNavigationalState)
   {
      return new InternalInvokePortletCommandFactory(window, controllerContext, pageNavigationalState);
   }

   public static PortletInvocationContext createInvocationContext(ControllerContext controllerContext, InvokePortletCommandFactory cpc)
   {
      MarkupInfo markupInfo = (MarkupInfo)controllerContext.getServerInvocation().getResponse().getContentInfo();
      return new ControllerPortletInvocationContext(cpc, controllerContext, markupInfo);
   }

   public static PortletInvocationContext createInvocationContext(
           ControllerContext controllerContext,
           Window window,
           PortletPageNavigationalState pageNavigationalState)
   {
      InvokePortletCommandFactory ipcf = createInvokePortletCommandFactory(controllerContext, window, pageNavigationalState);
      return createInvocationContext(controllerContext, ipcf);
   }

   public static ActionInvocation createAction(
           ControllerContext controllerContext,
           Mode mode,
           WindowState windowState,
           StateString navigationalState,
           StateString interactionState,
           ParameterMap form,
           Window window,
           Portal portal,
           PortletPageNavigationalState pageNavigationalState)
   {
      PortletContextFactory cf = new PortletContextFactory(controllerContext, portal, window);
      InvokePortletCommandFactory cpc = createInvokePortletCommandFactory(controllerContext, window, pageNavigationalState);
      return createAction(controllerContext, mode, windowState, navigationalState, interactionState, form, cf, cpc);
   }

   public static ActionInvocation createAction(
           ControllerContext controllerContext,
           Mode mode,
           WindowState windowState,
           StateString navigationalState,
           StateString interactionState,
           ParameterMap form,
           PortletContextFactory cf,
           InvokePortletCommandFactory cpc)
   {
      PortletInvocationContext portletInvocationContext = createInvocationContext(controllerContext, cpc);

      //
      ActionInvocation action = new ActionInvocation(portletInvocationContext);

      //
      action.setForm(form);
      action.setMode(mode);
      action.setWindowState(windowState);
      action.setNavigationalState(navigationalState);
      action.setInteractionState(interactionState);

      //
      contextualize(controllerContext, cf, action);

      //
      return action;
   }

   public static RenderInvocation createRender(
           ControllerContext controllerContext,
           Mode mode,
           WindowState windowState,
           StateString navigationalState,
           Window window,
           Portal portal,
           PortletPageNavigationalState pageNavigationalState)
   {
      PortletContextFactory cf = new PortletContextFactory(controllerContext, portal, window);
      InvokePortletCommandFactory cpc = createInvokePortletCommandFactory(controllerContext, window, pageNavigationalState);
      RenderInvocation render = createRender(controllerContext, mode, windowState, navigationalState, cf, cpc);
      render.setPublicNavigationalState(pageNavigationalState.getPortletPublicNavigationalState(window.getName()));
      return render;
   }

   public static RenderInvocation createRender(
           ControllerContext controllerContext,
           Mode mode,
           WindowState windowState,
           StateString navigationalState,
           PortletContextFactory cf,
           InvokePortletCommandFactory cpc)
   {
      PortletInvocationContext portletInvocationContext = createInvocationContext(controllerContext, cpc);

      //
      RenderInvocation render = new RenderInvocation(portletInvocationContext);

      //
      render.setMode(mode);
      render.setWindowState(windowState);
      render.setNavigationalState(navigationalState);

      //
      contextualize(controllerContext, cf, render);

      //
      return render;
   }

   public static void contextualize(
           ControllerContext controllerContext,
           PortletContextFactory cf,
           PortletInvocation invocation)
   {
      invocation.setAttribute("controller_context", controllerContext);

      // Contextualize
      invocation.setSecurityContext(cf.createSecurityContext());
      invocation.setPortalContext(cf.createPortalContext());
      invocation.setWindowContext(cf.createWindowContext());
      invocation.setUserContext(cf.createUserContext());
      invocation.setServerContext(cf.createServerContext());
      invocation.setClientContext(cf.createClientContext());

      //
      if (invocation instanceof ActionInvocation)
      {
         ActionInvocation action = (ActionInvocation)invocation;

         //
         action.setRequestContext(cf.createRequestContext());
      }
      else if (invocation instanceof ResourceInvocation)
      {
         ResourceInvocation resource = (ResourceInvocation)invocation;

         //
         resource.setRequestContext(cf.createRequestContext());
      }
   }

   public static Window getTargetWindow(PortletInvocation action)
   {
      ControllerPortletInvocationContext cpic = (ControllerPortletInvocationContext)action.getContext();
      InternalInvokePortletCommandFactory iipcf = (InternalInvokePortletCommandFactory)cpic.cmdFactory;
      return iipcf.window;
   }

   public static void contextualize(PortletInvocation action)
   {
      ControllerPortletInvocationContext cpic = (ControllerPortletInvocationContext)action.getContext();
      Window window = getTargetWindow(action);
      contextualize(cpic.controllerContext, new PortletContextFactory(cpic.controllerContext, window.getPage().getPortal(), window), action);
   }

   public static class ControllerPortletInvocationContext extends AbstractPortletInvocationContext
   {

      /** . */
      private InvokePortletCommandFactory cmdFactory;

      /** . */
      private ControllerContext controllerContext;

      public ControllerPortletInvocationContext(
              InvokePortletCommandFactory cmdFactory,
              ControllerContext controllerContext,
              MarkupInfo markupInfo)
      {
         super(markupInfo);

         //
         this.cmdFactory = cmdFactory;
         this.controllerContext = controllerContext;
      }

      public HttpServletRequest getClientRequest() throws IllegalStateException
      {
         return controllerContext.getServerInvocation().getServerContext().getClientRequest();
      }

      public HttpServletResponse getClientResponse() throws IllegalStateException
      {
         return controllerContext.getServerInvocation().getServerContext().getClientResponse();
      }

      public String renderURL(ContainerURL containerURL, org.jboss.portal.portlet.URLFormat urlFormat)
      {
         ControllerCommand cmd = null;

         //
         if (containerURL instanceof ActionURL)
         {
            cmd = cmdFactory.createInvokeActionCommand((ActionURL)containerURL);
         }
         else if (containerURL instanceof RenderURL)
         {
            cmd = cmdFactory.createInvokeRenderCommand((RenderURL)containerURL);
         }
         else if (containerURL instanceof ResourceURL)
         {
            cmd = cmdFactory.createInvokeResourceCommand((ResourceURL)containerURL);
         }

         //
         if (cmd == null)
         {
            throw new IllegalArgumentException("No container url such as " + containerURL + " can be rendered by the core");
         }

         //
         boolean secure = controllerContext.getServerInvocation().getServerContext().getURLContext().isSecure();
         if (urlFormat.getWantSecure() != null)
         {
            secure = urlFormat.getWantSecure();
         }

         //
         boolean authenticated = controllerContext.getServerInvocation().getServerContext().getURLContext().isAuthenticated();
         if (urlFormat.getWantAuthenticated() != null)
         {
            authenticated = urlFormat.getWantAuthenticated();
         }

         //
         boolean relative = true;
         if (urlFormat.getWantRelative() != null)
         {
            relative = urlFormat.getWantRelative();
         }

         //
         URLContext info = URLContext.newInstance(secure, authenticated);
         return controllerContext.renderURL(cmd, info, URLFormat.newInstance(relative, true));
      }
   }

   private static class InternalInvokePortletCommandFactory implements InvokePortletCommandFactory
   {

      /** . */
      Window window;

      /** . */
      ControllerContext controllerContext;

      /** The existing public navigational state of the portlet. */
      Map<String, String[]> publicNavigationalState;

      public InternalInvokePortletCommandFactory(
              Window window,
              ControllerContext controllerContext,
              PortletPageNavigationalState pageNavigationalState)
      {
         this.controllerContext = controllerContext;
         this.window = window;
         this.publicNavigationalState = pageNavigationalState.getPortletPublicNavigationalState(window.getName());
      }

      public ControllerCommand createInvokeActionCommand(ActionURL actionURL)
      {
         return new InvokePortletWindowActionCommand(
                 window.getId(),
                 actionURL.getMode(),
                 actionURL.getWindowState(),
                 actionURL.getNavigationalState(),
                 actionURL.getInteractionState(),
                 null);
      }

      public ControllerCommand createInvokeRenderCommand(RenderURL renderURL)
      {
         StateString navigationalState = renderURL.getNavigationalState();

         //
         if (navigationalState instanceof ParametersStateString)
         {
            ParametersStateString navigationalParameters = (ParametersStateString)navigationalState;

            //
            Map<String, String[]> parameters = navigationalParameters.getParameters();

            // We add the changes desired by the portlet
            Map<String, String[]> publicChanges = renderURL.getPublicNavigationalStateChanges();
            if (publicChanges != null && publicChanges.size() > 0)
            {
               for (Map.Entry<String, String[]> entry : publicChanges.entrySet())
               {
                  if (entry.getValue().length > 0)
                  {
                     parameters.put(entry.getKey(), entry.getValue());
                  }
               }
            }

            // Complete with previous public portion of navigational state
            if (publicNavigationalState != null && publicNavigationalState.size() > 0)
            {
               for (Map.Entry<String, String[]> entry : publicNavigationalState.entrySet())
               {
                  String name = entry.getKey();

                  //
                  if (parameters.containsKey(name))
                  {
                     continue;
                  }

                  //
                  if (publicChanges != null)
                  {
                     String[] value = publicChanges.get(name);

                     //
                     if (value != null && value.length == 0)
                     {
                        continue;
                     }
                  }

                  //
                  parameters.put(name, entry.getValue());
               }
            }

            //
            navigationalState = ParametersStateString.create(parameters);
         }

         //
         return new InvokePortletWindowRenderCommand(
                 window.getId(),
                 renderURL.getMode(),
                 renderURL.getWindowState(),
                 navigationalState);
      }

      public ControllerCommand createInvokeResourceCommand(ResourceURL resourceURL)
      {
         return new InvokePortletWindowResourceCommand(
                 window.getId(),
                 resourceURL.getCacheability(),
                 resourceURL.getResourceId(),
                 resourceURL.getResourceState(),
                 null
         );
      }
   }
}
