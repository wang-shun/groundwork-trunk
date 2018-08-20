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
package org.jboss.portal.core.theme;

import org.jboss.portal.Mode;
import org.jboss.portal.WindowState;
import org.jboss.portal.core.controller.ControllerContext;
import org.jboss.portal.core.model.portal.PortalObjectPath;
import org.jboss.portal.core.model.portal.Window;
import org.jboss.portal.core.model.portal.command.action.InvokePortletWindowRenderCommand;
import org.jboss.portal.core.model.portal.command.response.MarkupResponse;
import org.jboss.portal.core.model.portal.content.WindowRendition;
import org.jboss.portal.server.ServerInvocationContext;
import org.jboss.portal.server.request.URLContext;
import org.jboss.portal.server.request.URLFormat;
import org.jboss.portal.theme.ThemeConstants;
import org.jboss.portal.theme.page.WindowContext;
import org.jboss.portal.theme.page.WindowResult;
import org.jboss.portal.theme.render.renderer.ActionRendererContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 13018 $
 */
public class WindowContextFactory
{

   /** . */
   private final ControllerContext context;

   public WindowContextFactory(ControllerContext context)
   {
      this.context = context;
   }

   public WindowContext createWindowContext(Window window, WindowRendition context)
   {
      Map actionMap = new HashMap();
      addModeActions(window, actionMap, context.getMode(), context.getSupportedModes());
      addWindowStateActions(window, actionMap, context.getWindowState(), context.getSupportedWindowStates());

      //
      MarkupResponse markup = (MarkupResponse)context.getControllerResponse();

      //
      String region = window.getDeclaredProperty(ThemeConstants.PORTAL_PROP_REGION);
      String order = window.getDeclaredProperty(ThemeConstants.PORTAL_PROP_ORDER);

      //
      WindowResult windowResult = new WindowResult(
         markup.getTitle(),
         markup.getContent(),
         actionMap,
         context.getProperties(),
         markup.getHeaderContent(),
         context.getWindowState(),
         context.getMode());

      //
      return new WindowContext(
         window.getId().toString(PortalObjectPath.SAFEST_FORMAT),
         region,
         order,
         windowResult);
   }

   /**
    * Create the action URLs for the allowed window states of the rendered portlet window and add them to the provided
    * actionMap.
    */
   protected final void addWindowStateActions(Window window, Map actionMap, WindowState currentWindowState, List supportedWindowStates)
   {
      List windowStates = new ArrayList(supportedWindowStates.size());
      for (Iterator j = supportedWindowStates.iterator(); j.hasNext();)
      {
         WindowState windowState = (WindowState)j.next();
         String url = createUpdateNavigationalStateURL(window, null, windowState);
         boolean disabled = windowState.equals(currentWindowState);
         WindowResult.Action action = new WindowResult.Action(windowState.toString(), "window_state", url, !disabled);
         windowStates.add(action);
      }
      actionMap.put(ActionRendererContext.WINDOWSTATES_KEY, windowStates);
   }

   /**
    * Create the action URLs for the allowed portlet modes of the rendered portlet window and add them to the provided
    * actionMap.
    */
   protected final void addModeActions(Window window, Map actionMap, Mode currentMode, List supportedModes)
   {
      List modes = new ArrayList(supportedModes.size());
      for (Iterator j = supportedModes.iterator(); j.hasNext();)
      {
         Mode mode = (Mode)j.next();
         String url = createUpdateNavigationalStateURL(window, mode, null);
         boolean disabled = mode.equals(currentMode);
         WindowResult.Action action = new WindowResult.Action(mode.toString(), "mode", url, !disabled);
         modes.add(action);
      }
      actionMap.put(ActionRendererContext.MODES_KEY, modes);
   }

   protected final String createUpdateNavigationalStateURL(Window window, Mode mode, WindowState windowState)
   {
      InvokePortletWindowRenderCommand cmd = new InvokePortletWindowRenderCommand(window.getId(), mode, windowState);
      ServerInvocationContext serverContext = context.getServerInvocation().getServerContext();
      boolean secure = serverContext.getURLContext().isSecure();
      boolean authenticated = serverContext.getURLContext().isAuthenticated();
      URLContext urlContext = URLContext.newInstance(secure, authenticated);
      return context.renderURL(cmd, urlContext, URLFormat.newInstance(true, true));
   }
}
