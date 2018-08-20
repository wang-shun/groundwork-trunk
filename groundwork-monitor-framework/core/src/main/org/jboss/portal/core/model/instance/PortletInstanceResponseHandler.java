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
package org.jboss.portal.core.model.instance;

import org.jboss.portal.common.util.MarkupInfo;
import org.jboss.portal.core.controller.ControllerCommand;
import org.jboss.portal.core.controller.ControllerContext;
import org.jboss.portal.core.controller.ControllerException;
import org.jboss.portal.core.controller.ControllerResponse;
import org.jboss.portal.core.controller.handler.HTTPResponse;
import org.jboss.portal.core.controller.handler.HandlerResponse;
import org.jboss.portal.core.controller.handler.ResponseHandler;
import org.jboss.portal.core.controller.handler.ResponseHandlerException;
import org.jboss.portal.core.model.instance.command.render.RenderPortletInstanceCommand;
import org.jboss.portal.core.model.instance.command.response.UpdatePortletInstanceResponse;
import org.jboss.portal.core.theme.PageRendition;
import org.jboss.portal.server.ServerInvocation;
import org.jboss.portal.server.ServerInvocationContext;
import org.jboss.portal.web.ServletContextDispatcher;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public class PortletInstanceResponseHandler implements ResponseHandler
{
   public HandlerResponse processCommandResponse(ControllerContext controllerContext, ControllerCommand codmmand, ControllerResponse controllerResponse) throws ResponseHandlerException
   {
      if (controllerResponse instanceof UpdatePortletInstanceResponse)
      {
         try
         {
            UpdatePortletInstanceResponse upir = (UpdatePortletInstanceResponse)controllerResponse;
            RenderPortletInstanceCommand render = new RenderPortletInstanceCommand(upir.getInstanceId(), upir.getNavigationalState());
            final PageRendition rendition = (PageRendition)controllerContext.execute(render);
            final ServerInvocation invocation = controllerContext.getServerInvocation();
            return new HTTPResponse()
            {
               public void sendResponse(ServerInvocationContext ctx) throws IOException, ServletException
               {
                  ServletContextDispatcher dispatcher = new ServletContextDispatcher(invocation.getServerContext().getClientRequest(), invocation.getServerContext().getClientResponse(), invocation.getRequest().getServer().getServletContainer());
                  MarkupInfo markupInfo = (MarkupInfo)invocation.getResponse().getContentInfo();
                  rendition.render(markupInfo, dispatcher);
               }
            };
         }
         catch (ControllerException e)
         {
            e.printStackTrace();
         }
      }
      return null;
   }
}
