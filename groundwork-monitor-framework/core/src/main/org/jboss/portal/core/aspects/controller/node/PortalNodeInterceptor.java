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
package org.jboss.portal.core.aspects.controller.node;

import org.jboss.portal.common.invocation.InvocationException;
import org.jboss.portal.core.controller.ControllerCommand;
import org.jboss.portal.core.controller.ControllerInterceptor;
import org.jboss.portal.core.controller.ControllerResponse;
import org.jboss.portal.core.impl.api.PortalRuntimeContextImpl;
import org.jboss.portal.core.impl.api.node.PortalNodeImpl;
import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.Window;
import org.jboss.portal.core.model.portal.command.WindowCommand;
import org.jboss.portal.core.model.portal.command.render.RenderPageCommand;
import org.jboss.portal.security.spi.auth.PortalAuthorizationManager;
import org.jboss.portal.security.spi.auth.PortalAuthorizationManagerFactory;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public class PortalNodeInterceptor extends ControllerInterceptor
{

   /** . */
   private PortalAuthorizationManagerFactory portalAuthorizationManagerFactory;

   public PortalAuthorizationManagerFactory getPortalAuthorizationManagerFactory()
   {
      return portalAuthorizationManagerFactory;
   }

   public void setPortalAuthorizationManagerFactory(PortalAuthorizationManagerFactory portalAuthorizationManagerFactory)
   {
      this.portalAuthorizationManagerFactory = portalAuthorizationManagerFactory;
   }

   public ControllerResponse invoke(ControllerCommand cmd) throws Exception, InvocationException
   {
      // Get the next node
      PortalNodeImpl next = getNode(cmd);

      // Save previous node temporarily
      PortalNodeImpl previous = Navigation.getCurrentNode();

      // Whether or not we inherit from an existing factory
      boolean noFactory = Navigation.getPortalRuntimeContext() == null;

      try
      {
         // Set next node
         Navigation.setCurrentNode(next);

         //
         if (noFactory)
         {
            Navigation.setPortalRuntimeContext(new PortalRuntimeContextImpl(cmd.getControllerContext()));
         }

         // Invoke
         return (ControllerResponse)cmd.invokeNext();
      }
      finally
      {
         // Set previous node back
         Navigation.setCurrentNode(previous);

         //
         if (noFactory)
         {
            Navigation.setPortalRuntimeContext(null);
         }
      }
   }

   private PortalNodeImpl getNode(ControllerCommand cmd)
   {
      PortalAuthorizationManager pam = portalAuthorizationManagerFactory.getManager();
      PortalNodeImpl next = null;

      //
      if (cmd instanceof WindowCommand)
      {
         WindowCommand windowCmd = (WindowCommand)cmd;
         Window window = windowCmd.getWindow();
         next = new PortalNodeImpl(pam, window);
      }
      else if (cmd instanceof RenderPageCommand)
      {
         RenderPageCommand rpCmd = (RenderPageCommand)cmd;
         Page page = rpCmd.getPage();
         next = new PortalNodeImpl(pam, page);
      }

      //
      return next;
   }
}
