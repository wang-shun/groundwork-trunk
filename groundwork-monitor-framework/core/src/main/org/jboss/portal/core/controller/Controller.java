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
package org.jboss.portal.core.controller;

import org.jboss.portal.common.invocation.InterceptorStackFactory;
import org.jboss.portal.core.controller.command.mapper.CommandFactory;
import org.jboss.portal.core.controller.command.mapper.URLFactory;
import org.jboss.portal.core.controller.command.response.ErrorResponse;
import org.jboss.portal.core.controller.handler.AjaxResponse;
import org.jboss.portal.core.controller.handler.CommandForward;
import org.jboss.portal.core.controller.handler.HTTPResponse;
import org.jboss.portal.core.controller.handler.HandlerResponse;
import org.jboss.portal.core.controller.handler.ResponseForward;
import org.jboss.portal.core.controller.handler.ResponseHandler;
import org.jboss.portal.core.controller.handler.ResponseHandlerException;
import org.jboss.portal.core.model.CustomizationManager;
import org.jboss.portal.core.model.instance.InstanceContainer;
import org.jboss.portal.core.model.portal.PortalObjectContainer;
import org.jboss.portal.core.controller.coordination.CoordinationConfigurator;
import org.jboss.portal.core.controller.coordination.CoordinationManager;
import org.jboss.portal.core.model.portal.content.ContentRendererRegistry;
import org.jboss.portal.core.model.portal.control.page.PageControlPolicy;
import org.jboss.portal.core.impl.model.content.InternalContentProviderRegistry;
import org.jboss.portal.jems.as.system.AbstractJBossService;
import org.jboss.portal.security.spi.auth.PortalAuthorizationManagerFactory;
import org.jboss.portal.server.RequestController;
import org.jboss.portal.server.ServerException;
import org.jboss.portal.server.ServerInvocation;
import org.jboss.portal.theme.PageService;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * @author <a href="mailto:mholzner@novell.com">Martin Holzner</a>
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @author <a href="mailto:boleslaw dot dawidowicz at redhat anotherdot com">Boleslaw Dawidowicz</a>
 * @version $Revision: 11492 $
 */
public class Controller extends AbstractJBossService implements RequestController
{

   /** . */
   protected PageService pageService;

   /** . */
   protected CommandFactory commandFactory;

   /** . */
   protected URLFactory urlFactory;

   /** . */
   protected InterceptorStackFactory stackFactory;

   /** . */
   protected PortalObjectContainer portalObjectContainer;

   /** . */
   protected InstanceContainer instanceContainer;

   /** . */
   protected PortalAuthorizationManagerFactory portalAuthorizationManagerFactory;

   /** . */
   protected CustomizationManager customizationManager;

   /** . */
   protected ContentRendererRegistry contentRendererRegistry;

   /** . */
   protected ResponseHandler responseHandler;

   /** . */
   protected PageControlPolicy pageControlPolicy;

   /** . */
   protected InternalContentProviderRegistry contentProviderRegistry;

   /** . */
   protected CoordinationConfigurator coordinationConfigurator;

   /** . */
   protected CoordinationManager coordinationManager;


   public InternalContentProviderRegistry getContentProviderRegistry()
   {
      return contentProviderRegistry;
   }

   public void setContentProviderRegistry(InternalContentProviderRegistry contentProviderRegistry)
   {
      this.contentProviderRegistry = contentProviderRegistry;
   }

   public ContentRendererRegistry getContentRendererRegistry()
   {
      return contentRendererRegistry;
   }

   public void setContentRendererRegistry(ContentRendererRegistry contentRendererRegistry)
   {
      this.contentRendererRegistry = contentRendererRegistry;
   }

   public CustomizationManager getCustomizationManager()
   {
      return customizationManager;
   }

   public void setCustomizationManager(CustomizationManager customizationManager)
   {
      this.customizationManager = customizationManager;
   }

   public PortalAuthorizationManagerFactory getPortalAuthorizationManagerFactory()
   {
      return portalAuthorizationManagerFactory;
   }

   public void setPortalAuthorizationManagerFactory(PortalAuthorizationManagerFactory portalAuthorizationManagerFactory)
   {
      this.portalAuthorizationManagerFactory = portalAuthorizationManagerFactory;
   }

   public InstanceContainer getInstanceContainer()
   {
      return instanceContainer;
   }

   public void setInstanceContainer(InstanceContainer instanceContainer)
   {
      this.instanceContainer = instanceContainer;
   }

   public PortalObjectContainer getPortalObjectContainer()
   {
      return portalObjectContainer;
   }

   public void setPortalObjectContainer(PortalObjectContainer portalObjectContainer)
   {
      this.portalObjectContainer = portalObjectContainer;
   }

   public URLFactory getURLFactory()
   {
      return urlFactory;
   }

   public void setURLFactory(URLFactory urlFactory)
   {
      this.urlFactory = urlFactory;
   }

   public CommandFactory getCommandFactory()
   {
      return commandFactory;
   }

   public void setCommandFactory(CommandFactory commandFactory)
   {
      this.commandFactory = commandFactory;
   }

   public PageService getPageService()
   {
      return pageService;
   }

   public void setPageService(PageService pageService)
   {
      this.pageService = pageService;
   }

   public InterceptorStackFactory getStackFactory()
   {
      return stackFactory;
   }

   public void setStackFactory(InterceptorStackFactory stackFactory)
   {
      this.stackFactory = stackFactory;
   }

   public ResponseHandler getResponseHandler()
   {
      return responseHandler;
   }

   public void setResponseHandler(ResponseHandler responseHandler)
   {
      this.responseHandler = responseHandler;
   }

   public PageControlPolicy getPageControlPolicy()
   {
      return pageControlPolicy;
   }

   public void setPageControlPolicy(PageControlPolicy pageControlPolicy)
   {
      this.pageControlPolicy = pageControlPolicy;
   }

   public CoordinationConfigurator getCoordinationConfigurator()
   {
      return coordinationConfigurator;
   }

   public void setCoordinationConfigurator(CoordinationConfigurator coordinationConfigurator)
   {
      this.coordinationConfigurator = coordinationConfigurator;
   }

   public CoordinationManager getCoordinationManager()
   {
      return coordinationManager;
   }

   public void setCoordinationManager(CoordinationManager coordinationManager)
   {
      this.coordinationManager = coordinationManager;
   }

   public final void handle(ServerInvocation invocation) throws ServerException
   {
      // Create controller context
      ControllerContext controllerContext = new ControllerContext(invocation, this);

      // Invoke the chain that creates the initial command
      ControllerCommand cmd = commandFactory.doMapping(controllerContext, invocation, invocation.getServerContext().getPortalHost(), invocation.getServerContext().getPortalContextPath(), invocation.getServerContext().getPortalRequestPath());

      // Handle that case
      if (cmd == null)
      {
         throw new ServerException("No command was produced by the command factory");
      }

      // Handle the command created
      processCommand(controllerContext, cmd);
   }

   /**
    * Handle a command which means it executes the command and reacts upon the response created by the command.
    *
    * @param controllerContext the controller context
    * @param command           the command
    * @throws org.jboss.portal.server.ServerException
    *
    */
   protected void processCommand(ControllerContext controllerContext, ControllerCommand command) throws ServerException
   {
      ControllerResponse response;

      //
      try
      {
         response = controllerContext.execute(command);
      }
      catch (CommandRedirectionException e)
      {
         processHandlerResponse(
            controllerContext,
            command,
            new CommandForward(e.getRedirection(), null));

         // We are done
         return;
      }
      catch (ControllerException e)
      {
         response = new ErrorResponse(e, true);
      }

      //
      if (response == null)
      {
         response = new ErrorResponse("No response was provided by the invocation of " + command, true);
      }

      //
      processCommandResponse(controllerContext, command, response);
   }

   protected void processCommandResponse(
      ControllerContext controllerContext,
      ControllerCommand command,
      ControllerResponse response) throws ServerException
   {
      // Handle the result
      HandlerResponse handlerResponse;
      try
      {
         handlerResponse = responseHandler.processCommandResponse(controllerContext, command, response);
      }
      catch (ResponseHandlerException e)
      {
         throw new ServerException(e);
      }

      // Might be null if no handling done
      if (handlerResponse == null)
      {
         return;
      }

      //
      processHandlerResponse(controllerContext, command, handlerResponse);
   }

   protected void processHandlerResponse(ControllerContext controllerContext, ControllerCommand command, HandlerResponse handlerResponse) throws ServerException
   {
      // Find out if we can execute in the same server invocation
      if (handlerResponse instanceof CommandForward)
      {
         CommandForward forward = (CommandForward)handlerResponse;
         processCommand(controllerContext, forward.getCommand());
      }
      else if (handlerResponse instanceof ResponseForward)
      {
         ResponseForward forward = (ResponseForward)handlerResponse;
         ControllerResponse response = forward.getResponse();
         processCommandResponse(controllerContext, command, response);
      }
      else if (handlerResponse instanceof HTTPResponse)
      {
         HTTPResponse hr = (HTTPResponse)handlerResponse;
         sendResponse(controllerContext, hr);
      }
      else if (handlerResponse instanceof AjaxResponse)
      {
         AjaxResponse ar = (AjaxResponse)handlerResponse;
         sendResponse(controllerContext, ar);
      }
   }

   /** All http responses in the stack should be handled here. */
   protected void sendResponse(ControllerContext ctx, HTTPResponse resp)
   {
      try
      {
         resp.sendResponse(ctx.getServerInvocation().getServerContext());
      }
      catch (IOException e)
      {
         log.error("Cound not send http response", e);
      }
      catch (ServletException e)
      {
         log.error("Cound not send http response", e);
      }
   }

   /** All http responses in the stack should be handled here. */
   protected void sendResponse(ControllerContext ctx, AjaxResponse resp)
   {
      try
      {
         resp.sendResponse(ctx.getServerInvocation().getServerContext());
      }
      catch (IOException e)
      {
         log.error("Cound not send http response", e);
      }
      catch (ServletException e)
      {
         log.error("Cound not send http response", e);
      }
   }
}
