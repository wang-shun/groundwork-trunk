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
package org.jboss.portal.core.model.portal.command.render;

import org.jboss.portal.core.controller.ControllerContext;
import org.jboss.portal.core.controller.ControllerException;
import org.jboss.portal.core.controller.ControllerResponse;
import org.jboss.portal.core.controller.NoSuchResourceException;
import org.jboss.portal.core.controller.command.info.CommandInfo;
import org.jboss.portal.core.controller.command.info.ViewCommandInfo;
import org.jboss.portal.core.controller.portlet.ControllerPageNavigationalState;
import org.jboss.portal.core.controller.portlet.PortletInvocationFactory;
import org.jboss.portal.core.model.content.ContentType;
import org.jboss.portal.core.model.portal.PortalObjectId;
import org.jboss.portal.core.model.portal.Window;
import org.jboss.portal.core.model.portal.command.WindowCommand;
import org.jboss.portal.core.model.portal.content.ContentRenderer;
import org.jboss.portal.core.model.portal.content.ContentRendererContext;
import org.jboss.portal.core.model.portal.content.ContentRendererRegistry;
import org.jboss.portal.core.model.portal.content.WindowRendition;
import org.jboss.portal.core.model.portal.control.page.PageControlContext;
import org.jboss.portal.identity.User;
import org.jboss.portal.portlet.controller.state.PortletWindowNavigationalState;
import org.jboss.portal.portlet.invocation.RenderInvocation;

import java.util.Map;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 11068 $
 */
public class RenderWindowCommand extends WindowCommand implements ContentRendererContext
{

   /** . */
   private static final CommandInfo info = new ViewCommandInfo();

   /** . */
   private WindowRendition rendition;

   /** . */
   private ControllerPageNavigationalState pageNavigationalState;

   public RenderWindowCommand(ControllerPageNavigationalState pageNavigationalState, PortalObjectId windowId) throws IllegalArgumentException
   {
      super(windowId);

      //
      this.pageNavigationalState = pageNavigationalState;
   }

   public CommandInfo getInfo()
   {
      return info;
   }

   /** Hack the command system. */
   public WindowRendition render(ControllerContext ctx) throws ControllerException
   {
      ctx.execute(this);

      //
      return rendition;
   }

   public ControllerResponse execute() throws ControllerException
   {
      // Find the appropriate content renderer
      ContentRendererRegistry registry = context.getController().getContentRendererRegistry();
      ContentType contentType = window.getContentType();
      ContentRenderer renderer = registry.getRenderer(contentType);

      //
      if (renderer == null)
      {
         // Return the appropriate result
      }
      else
      {
         rendition = renderer.renderWindow(this);

         // Apply policy behavior here
         if (rendition != null)
         {
            PageControlContext wcc = new PageControlContext(context, targetId, rendition);
            context.getController().getPageControlPolicy().doControl(wcc);
         }
      }

      //
      return null;
   }

   public Map<String, String[]> getPublicNavigationalState()
   {
      return pageNavigationalState.getPortletPublicNavigationalState(window.getName());
   }

   public PortletWindowNavigationalState getPortletNavigationalState()
   {
      return pageNavigationalState.getPortletWindowNavigationalState(window.getName());
   }

   public User getUser()
   {
      return context.getUser();
   }

   public RenderInvocation createRenderInvocation(PortletWindowNavigationalState navigationalState)
   {
      return PortletInvocationFactory.createRender(
         context,
         navigationalState.getMode(),
         navigationalState.getWindowState(),
         navigationalState.getPortletNavigationalState(),
         window,
         portal,
         pageNavigationalState);
   }

   @Override
   public void acquireResources() throws NoSuchResourceException
   {
      super.acquireResources();
/*
      // check that the portlet associated with the window is deployed by delegating to the navigational state which
      // in turn delegates to ControllerPortletControllerContext which knows the windows which are available
      String windowName = window.getName();
      Window realWindow = pageNavigationalState.getWindow(windowName);
      if(realWindow == null)
      {
         log.debug("Resource associated with window '" + windowName + "' could not be found!");
         throw new NoSuchResourceException(windowName);
      }
      */
   }
   
}
