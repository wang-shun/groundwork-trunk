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
package org.jboss.portal.core.model.portal;

import org.jboss.portal.core.controller.ControllerCommand;
import org.jboss.portal.core.controller.ControllerContext;
import org.jboss.portal.core.controller.command.mapper.AbstractCommandFactory;
import org.jboss.portal.core.controller.command.mapper.CommandFactory;
import org.jboss.portal.core.model.portal.command.view.ViewPageCommand;
import org.jboss.portal.server.ServerInvocation;

/**
 * Return the default command if nothing has been found.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 11673 $
 */
public class DefaultPortalCommandFactory extends AbstractCommandFactory
{

   /** . */
   private CommandFactory nextFactory;

   /** . */
   private PortalObjectContainer container;

   public PortalObjectContainer getContainer()
   {
      return container;
   }

   public void setContainer(PortalObjectContainer container)
   {
      this.container = container;
   }

   public CommandFactory getNextFactory()
   {
      return nextFactory;
   }

   public void setNextFactory(CommandFactory nextFactory)
   {
      this.nextFactory = nextFactory;
   }

   public ControllerCommand doMapping(ControllerContext controllerContext, ServerInvocation invocation, String host, String contextPath, String requestPath)
   {
      ControllerCommand cmd = nextFactory.doMapping(controllerContext, invocation, host, contextPath, requestPath);
      if (cmd == null)
      {
         Context context = container.getContext();
         if (context == null)
         {
            throw new IllegalStateException("Context does not exist");
         }
         Portal portal = context.getDefaultPortal();
         if (portal == null)
         {
            throw new IllegalStateException("Default portal does not exist");
         }
         Page page = portal.getDefaultPage();
         if (page == null)
         {
            throw new IllegalStateException("Default page does not exist");
         }
         PortalObjectId id = page.getId();
         cmd = new ViewPageCommand(id);
      }
      return cmd;
   }
}
