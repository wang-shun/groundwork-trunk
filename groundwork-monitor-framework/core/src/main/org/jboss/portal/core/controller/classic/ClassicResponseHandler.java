/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2009, Red Hat Middleware, LLC, and individual                    *
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

import org.jboss.portal.core.controller.ControllerCommand;
import org.jboss.portal.core.controller.ControllerContext;
import org.jboss.portal.core.controller.ControllerResponse;
import org.jboss.portal.core.controller.command.info.ActionCommandInfo;
import org.jboss.portal.core.controller.command.info.CommandInfo;
import org.jboss.portal.core.controller.handler.AbstractResponseHandler;
import org.jboss.portal.core.controller.handler.CommandForward;
import org.jboss.portal.core.controller.handler.HTTPResponse;
import org.jboss.portal.core.controller.handler.HandlerResponse;
import org.jboss.portal.core.controller.handler.ResponseHandler;
import org.jboss.portal.core.controller.handler.ResponseHandlerException;
import org.jboss.portal.core.controller.portlet.PortletResponseHandler;
import org.jboss.portal.core.model.instance.PortletInstanceResponseHandler;
import org.jboss.portal.core.model.portal.PortalObjectResponseHandler;
import org.jboss.portal.server.request.URLContext;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 12809 $
 */
public class ClassicResponseHandler extends AbstractResponseHandler
{

   public HandlerResponse processCommandResponse(
      ControllerContext controllerContext,
      ControllerCommand controllerCommand,
      ControllerResponse commandResponse) throws ResponseHandlerException
   {
      HandlerResponse handlerResponse = processHandlers(controllerContext, controllerCommand, commandResponse);

      // Handle redirection here
      if (handlerResponse instanceof CommandForward)
      {
         CommandForward forward = (CommandForward)handlerResponse;
         URLContext urlContext = controllerContext.getServerInvocation().getServerContext().getURLContext();
         if (requiresRedirect(controllerCommand, urlContext, forward))
         {
            String url = controllerContext.renderURL(forward.getCommand(), forward.getURLContext(), null);
            return HTTPResponse.sendRedirect(url);
         }
      }

      //
      return handlerResponse;
   }

   private HandlerResponse processHandlers(
      ControllerContext controllerContext,
      ControllerCommand command,
      ControllerResponse commandResponse) throws ResponseHandlerException
   {
      for (ResponseHandler handler : handlers)
      {
         HandlerResponse handlerResponse = handler.processCommandResponse(controllerContext, command, commandResponse);
         if (handlerResponse != null)
         {
            return handlerResponse;
         }
      }

      //
      return null;
   }

   // Unhardcode this
   private ResponseHandler[] handlers = new ResponseHandler[]
      {
         new PortletInstanceResponseHandler(),
         new PortalObjectResponseHandler(),
         new PortletResponseHandler(),
         new OtherResponseHandler()
      };

   /**
    * Return true if the execution of the next command requires a redirect.
    *
    * @param currentCmd    the current command which has been executed
    * @param currentURLCtx the request URL context
    * @param forward       the forward
    * @return
    */
   public boolean requiresRedirect(
      ControllerCommand currentCmd,
      URLContext currentURLCtx,
      CommandForward forward)
   {
      CommandInfo currentCmdInfo = currentCmd.getInfo();
      if (currentCmdInfo instanceof ActionCommandInfo && !((ActionCommandInfo)currentCmdInfo).isIdempotent())
      {
         return true;
      }
      else
      {
         URLContext nextURLCtx = forward.getURLContext();
         boolean currentAuthenticated = currentURLCtx.isAuthenticated();
         if (nextURLCtx != null && currentAuthenticated != nextURLCtx.isAuthenticated())
         {
            return true;
         }
         else
         {
            boolean currentSecure = currentURLCtx.isSecure();
            if (nextURLCtx != null && nextURLCtx.isSecure() && !currentSecure)
            {
               return true;
            }
         }
      }
      return false;
   }

}
