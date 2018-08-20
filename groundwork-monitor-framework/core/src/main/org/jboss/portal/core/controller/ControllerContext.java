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

import org.jboss.portal.common.invocation.AbstractInvocationContext;
import org.jboss.portal.common.invocation.InterceptorStack;
import org.jboss.portal.common.invocation.InvocationException;
import org.jboss.portal.common.servlet.BufferingRequestWrapper;
import org.jboss.portal.common.servlet.BufferingResponseWrapper;
import org.jboss.portal.core.aspects.server.UserInterceptor;
import org.jboss.portal.core.model.portal.navstate.PortalObjectNavigationalStateContext;
import org.jboss.portal.identity.User;
import org.jboss.portal.server.ServerInvocation;
import org.jboss.portal.server.ServerInvocationContext;
import org.jboss.portal.server.ServerURL;
import org.jboss.portal.server.request.URLContext;
import org.jboss.portal.server.request.URLFormat;
import org.jboss.portal.web.ServletContainer;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * The context of the controller.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 11068 $
 */
public class ControllerContext extends AbstractInvocationContext
{

   /** . */
   public static final int CLASSIC_TYPE = 0;

   /** . */
   public static final int AJAX_TYPE = 1;

   /** The server invocation. */
   private final ServerInvocation serverInvocation;

   /** The controller. */
   private final Controller controller;

   /** . */
   private final int type;

   /** The depth of the call stack. */
   private int depth;

   public ControllerContext(ServerInvocation serverInvocation, Controller controller)
   {
      if (serverInvocation == null)
      {
         throw new IllegalArgumentException();
      }
      if (controller == null)
      {
         throw new IllegalArgumentException();
      }

      //
      this.serverInvocation = serverInvocation;
      this.controller = controller;
      this.depth = 0;

      //
      HttpServletRequest req = serverInvocation.getServerContext().getClientRequest();
      String value = req.getHeader("ajax");
      if ("true".equalsIgnoreCase(value))
      {
         type = AJAX_TYPE;
      }
      else
      {
         type = CLASSIC_TYPE;
      }

      //
      addResolver(ControllerCommand.REQUEST_SCOPE, serverInvocation.getContext());
      addResolver(ControllerCommand.SESSION_SCOPE, serverInvocation.getContext());
      addResolver(ControllerCommand.NAVIGATIONAL_STATE_SCOPE, new PortalObjectNavigationalStateContext(serverInvocation.getContext().getAttributeResolver(ControllerCommand.PRINCIPAL_SCOPE)));
      addResolver(ControllerCommand.PRINCIPAL_SCOPE, serverInvocation.getContext());
   }

   public ServletContainer getServletContainer()
   {
      return serverInvocation.getRequest().getServer().getServletContainer();
   }

   public ControllerResponse execute(ControllerCommand command) throws ControllerException, InvocationException
   {
      if (command == null)
      {
         throw new IllegalArgumentException();
      }

      // Contextualize
      command.createContext(this);

      //
      int oldDepth = depth;

      //
      try
      {
         depth++;

         // Execute
         InterceptorStack commandStack = controller.getStackFactory().getInterceptorStack();

         //
         return (ControllerResponse)command.invoke(commandStack);
      }
      catch (Exception e)
      {
         ControllerCommand.rethrow(e);
         throw new Error("Should not happen");
      }
      finally
      {
         //
         depth = oldDepth;

         // Call destroy
         command.destroyContext();
      }
   }

   public int getType()
   {
      return type;
   }

   public int getDepth()
   {
      return depth;
   }

   /**
    * Render the command as an URL or return null if it is not possible.
    *
    * @param cmd        the command to render
    * @param urlContext the url context
    * @param urlFormat  the url format
    * @return the URL as a string or null
    */
   public String renderURL(ControllerCommand cmd, URLContext urlContext, URLFormat urlFormat)
   {
      ServerURL serverURL = controller.getURLFactory().doMapping(this, serverInvocation, cmd);

      //
      if (serverURL == null)
      {
         return null;
      }
      else
      {
         return serverInvocation.getResponse().renderURL(serverURL, urlContext, urlFormat);
      }
   }

   public ServerInvocation getServerInvocation()
   {
      return serverInvocation;
   }

   public Controller getController()
   {
      return controller;
   }

   public User getUser()
   {
      return (User)getAttribute(ServerInvocation.PRINCIPAL_SCOPE, UserInterceptor.USER_KEY);
   }

   public Map<String, String> getUserProfile()
   {
      return (Map<String, String>)getAttribute(ServerInvocation.PRINCIPAL_SCOPE, UserInterceptor.PROFILE_KEY);
   }

   public ControllerRequestDispatcher getRequestDispatcher(String contextPath, String path)
   {
      ServerInvocationContext serverContext = serverInvocation.getServerContext();
      ServletContext servletContext = serverContext.getClientRequest().getSession().getServletContext().getContext(contextPath);
      RequestDispatcher rd = servletContext.getRequestDispatcher(path);

      //
      if (rd != null)
      {
         BufferingRequestWrapper bufferReq = new BufferingRequestWrapper(
            serverContext.getClientRequest(),
            contextPath,
            serverInvocation.getRequest().getLocales());
         BufferingResponseWrapper bufferResp = new BufferingResponseWrapper(serverContext.getClientResponse());
         return new ControllerRequestDispatcher(rd, bufferReq, bufferResp);
      }

      //
      return null;
   }
}
