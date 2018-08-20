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
package org.jboss.portal.core.controller.classic;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.jboss.portal.common.util.MarkupInfo;
import org.jboss.portal.core.controller.ControllerCommand;
import org.jboss.portal.core.controller.ControllerContext;
import org.jboss.portal.core.controller.ControllerResponse;
import org.jboss.portal.core.controller.command.response.ErrorResponse;
import org.jboss.portal.core.controller.command.response.RedirectionResponse;
import org.jboss.portal.core.controller.command.response.SecurityErrorResponse;
import org.jboss.portal.core.controller.command.response.SignOutResponse;
import org.jboss.portal.core.controller.command.response.StreamContentResponse;
import org.jboss.portal.core.controller.command.response.UnavailableResourceResponse;
import org.jboss.portal.core.controller.handler.HTTPResponse;
import org.jboss.portal.core.controller.handler.HandlerResponse;
import org.jboss.portal.core.controller.handler.ResponseHandler;
import org.jboss.portal.core.controller.handler.ResponseHandlerException;
import org.jboss.portal.core.model.portal.Portal;
import org.jboss.portal.core.model.portal.PortalObjectContainer;
import org.jboss.portal.core.model.portal.command.view.ViewPageCommand;
import org.jboss.portal.core.theme.PageRendition;
import org.jboss.portal.server.ServerInvocation;
import org.jboss.portal.server.ServerInvocationContext;
import org.jboss.portal.server.ServerURL;
import org.jboss.portal.server.request.URLContext;
import org.jboss.portal.web.ServletContextDispatcher;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 12384 $
 */
public class OtherResponseHandler implements ResponseHandler
{

   /** . */
   // private static final PortalObjectId DEFAULT_PORTAL_PATH = PortalObjectId.parse("/", PortalObjectPath.CANONICAL_FORMAT);

   /** . */
   private static final Logger log = Logger.getLogger(OtherResponseHandler.class);

   public HandlerResponse processCommandResponse(ControllerContext controllerContext, ControllerCommand controllerCommand, ControllerResponse controllerResponse) throws ResponseHandlerException
   {
      final ServerInvocation invocation = controllerContext.getServerInvocation();

      //
      if (controllerResponse instanceof PageRendition)
      {
         final PageRendition rendition = (PageRendition)controllerResponse;

         // Defer execution of rendition to the right place which is in the classic controller send response
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
      else if (controllerResponse instanceof SignOutResponse)
      {
         // Get the optional signout location
         String location = ((SignOutResponse)controllerResponse).getLocation();

         // Indicate that we want a sign out to be done
         invocation.getResponse().setWantSignOut(true);

         //
         if (location == null)
         {
            PortalObjectContainer portalObjectContainer = controllerContext.getController().getPortalObjectContainer();
            Portal portal = (Portal)portalObjectContainer.getContext().getDefaultPortal();
            ViewPageCommand renderCmd = new ViewPageCommand(portal.getId());
            URLContext urlContext = invocation.getServerContext().getURLContext();
            location = controllerContext.renderURL(renderCmd, urlContext.asNonAuthenticated(), null);
         }

         //
         return HTTPResponse.sendRedirect(location);
      }
      else if (controllerResponse instanceof RedirectionResponse)
      {
         // Get the optional signout location
         String location = ((RedirectionResponse)controllerResponse).getLocation();

         //
         if (location == null)
         {
            PortalObjectContainer portalObjectContainer = controllerContext.getController().getPortalObjectContainer();
            Portal portal = (Portal)portalObjectContainer.getContext().getDefaultPortal();
            ViewPageCommand renderCmd = new ViewPageCommand(portal.getId());
            URLContext urlContext = invocation.getServerContext().getURLContext();
            location = controllerContext.renderURL(renderCmd, urlContext, null);
         }

         //
         return HTTPResponse.sendRedirect(location);
      }
      else if (controllerResponse instanceof StreamContentResponse)
      {
         StreamContentResponse scr = (StreamContentResponse)controllerResponse;

         if (scr.getInputStream() != null)
         {
            return HTTPResponse.sendBinary(scr.getContentType(), scr.getLastModified(), scr.getProperties(), scr.getInputStream());
         }
         else
         {
            return HTTPResponse.sendBinary(scr.getContentType(), scr.getLastModified(), scr.getProperties(), scr.getReader());
         }
      }
      else if (controllerResponse instanceof SecurityErrorResponse)
      {
         SecurityErrorResponse ser = (SecurityErrorResponse)controllerResponse;
         URLContext urlContext = controllerContext.getServerInvocation().getServerContext().getURLContext();
         if (ser.getStatus() == SecurityErrorResponse.NOT_AUTHORIZED)
         {
            if (controllerContext.getUser() != null)
            {
               return HTTPResponse.sendForbidden();
            }
            else
            {
               urlContext = URLContext.newInstance(urlContext.isSecure(), true);
               ServerURL serverURL = controllerContext.getController().getURLFactory().doMapping(controllerContext, controllerContext.getServerInvocation(), controllerCommand);
               String url = controllerContext.getServerInvocation().getResponse().renderURL(serverURL, urlContext, null);
               return HTTPResponse.sendRedirect(url);
            }
         }
         else
         {
            urlContext = URLContext.newInstance(true, urlContext.isAuthenticated());
            ServerURL serverURL = controllerContext.getController().getURLFactory().doMapping(controllerContext, controllerContext.getServerInvocation(), controllerCommand);
            String url = controllerContext.getServerInvocation().getResponse().renderURL(serverURL, urlContext, null);
            return HTTPResponse.sendRedirect(url);
         }
      }
      else if (controllerResponse instanceof ErrorResponse)
      {
         ErrorResponse errorResponse = (ErrorResponse)controllerResponse;

         //
         Throwable cause = errorResponse.getCause();

         //
         if (cause != null)
         {
            log.error("An error occured", cause);
         }

         return HTTPResponse.sendError(errorResponse.getMessage());
      }
      else if (controllerResponse instanceof UnavailableResourceResponse)
      {
         // UnavailableResourceResponse unavailable = (UnavailableResourceResponse)controllerResponse;

         //
         return HTTPResponse.sendNotFound();
      }
      else
      {
         return null;
      }
   }
}
