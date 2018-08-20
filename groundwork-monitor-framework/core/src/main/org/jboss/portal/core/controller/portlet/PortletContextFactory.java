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
import org.jboss.portal.common.util.CollectionBuilder;
import org.jboss.portal.core.controller.ControllerContext;
import org.jboss.portal.core.model.portal.Portal;
import org.jboss.portal.core.model.portal.Window;
import org.jboss.portal.portlet.impl.spi.AbstractClientContext;
import org.jboss.portal.portlet.impl.spi.AbstractRequestContext;
import org.jboss.portal.portlet.impl.spi.AbstractSecurityContext;
import org.jboss.portal.portlet.impl.spi.AbstractServerContext;
import org.jboss.portal.portlet.spi.ClientContext;
import org.jboss.portal.portlet.spi.PortalContext;
import org.jboss.portal.portlet.spi.RequestContext;
import org.jboss.portal.portlet.spi.SecurityContext;
import org.jboss.portal.portlet.spi.ServerContext;
import org.jboss.portal.portlet.spi.UserContext;
import org.jboss.portal.portlet.spi.WindowContext;
import org.jboss.portal.server.PortalConstants;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 10307 $
 */
public class PortletContextFactory
{

   /** . */
   private PortalContext portalContext;

   /** . */
   private RequestContext requestContext;

   /** . */
   private SecurityContext securityContext;

   /** . */
   private UserContext userContext;

   /** . */
   private WindowContext windowContext;
   
   /**. */
   private ServerContext serverContext;

   /** . */
   private AbstractClientContext clientContext;
   

   public PortletContextFactory(ControllerContext controllerContext, Portal portal, Window window)
   {
      this.requestContext = new AbstractRequestContext(controllerContext.getServerInvocation().getServerContext().getClientRequest());
      this.securityContext = new AbstractSecurityContext(controllerContext.getServerInvocation().getServerContext().getClientRequest());
      this.userContext = new ControllerUserContext(controllerContext);
      this.portalContext = new org.jboss.portal.core.model.portal.portlet.PortalContextImpl(portal);
      this.windowContext = new org.jboss.portal.core.model.portal.portlet.WindowContextImpl(window);
      this.serverContext =  new AbstractServerContext(controllerContext.getServerInvocation().getServerContext().getClientRequest(), controllerContext.getServerInvocation().getServerContext().getClientResponse());
      this.clientContext = new AbstractClientContext(controllerContext.getServerInvocation().getServerContext().getClientRequest());
   }

   public PortletContextFactory(ControllerContext controllerContext)
   {
      this.requestContext = new AbstractRequestContext(controllerContext.getServerInvocation().getServerContext().getClientRequest());
      this.securityContext = new AbstractSecurityContext(controllerContext.getServerInvocation().getServerContext().getClientRequest());
      this.userContext = new ControllerUserContext(controllerContext);
      this.portalContext = portalContextImpl;
      this.windowContext = new WindowContextImpl("abc"); // Well ????
      this.serverContext =  new AbstractServerContext(controllerContext.getServerInvocation().getServerContext().getClientRequest(), controllerContext.getServerInvocation().getServerContext().getClientResponse());
      this.clientContext = new AbstractClientContext(controllerContext.getServerInvocation().getServerContext().getClientRequest());
   }

   public PortalContext createPortalContext()
   {
      return portalContext;
   }

   public RequestContext createRequestContext()
   {
      return requestContext;
   }

   public SecurityContext createSecurityContext()
   {
      return securityContext;
   }

   public UserContext createUserContext()
   {
      return userContext;
   }

   public WindowContext createWindowContext()
   {
      return windowContext;
   }
   
   public ServerContext createServerContext()
   {
      return serverContext;
   }

   public ClientContext createClientContext()
   {
      return clientContext;
   }

   /** . */
   private static final PortalContextImpl portalContextImpl = new PortalContextImpl();

   private static class PortalContextImpl implements PortalContext
   {

      /** . */
      private final Set<WindowState> windowStates;

      /** . */
      private final Set<Mode> modes;

      /** . */
      private final Map props;

      public PortalContextImpl()
      {
         windowStates = Collections.unmodifiableSet((Set)CollectionBuilder.hashSet().add(WindowState.MAXIMIZED).add(WindowState.MINIMIZED).add(WindowState.NORMAL).get());
         modes = Collections.unmodifiableSet((Set)CollectionBuilder.hashSet().add(Mode.EDIT).add(Mode.HELP).add(Mode.VIEW).get());
         props = Collections.EMPTY_MAP;
      }

      public String getInfo()
      {
         return PortalConstants.VERSION.toString();
      }

      public Set<WindowState> getWindowStates()
      {
         return windowStates;
      }

      public Set<Mode> getModes()
      {
         return modes;
      }

      public Map getProperties()
      {
         return props;
      }
   }

   private static class WindowContextImpl implements WindowContext
   {

      /** . */
      private String id;

      public WindowContextImpl(String id)
      {
         this.id = id;
      }

      public String getId()
      {
         return id;
      }
   }
}
