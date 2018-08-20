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
package org.jboss.portal.core.model.portal.command.view;

import org.jboss.portal.core.controller.ControllerException;
import org.jboss.portal.core.controller.ControllerResponse;
import org.jboss.portal.core.controller.command.info.CommandInfo;
import org.jboss.portal.core.controller.command.info.ViewCommandInfo;
import org.jboss.portal.core.controller.command.response.ErrorResponse;
import org.jboss.portal.core.controller.command.response.UnavailableResourceResponse;
import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.Portal;
import org.jboss.portal.core.model.portal.PortalObjectId;
import org.jboss.portal.core.model.portal.command.ContextCommand;
import org.jboss.portal.core.model.portal.command.response.UpdatePageResponse;
import org.jboss.portal.identity.User;


/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 12041 $
 */
public class ViewContextCommand extends ContextCommand
{

   /** . */
   private static final CommandInfo info = new ViewCommandInfo();

   public ViewContextCommand(PortalObjectId pageId)
   {
      super(pageId);
   }

   public CommandInfo getInfo()
   {
      return info;
   }

   public ControllerResponse execute() throws ControllerException
   {
      if (dashboard)
      {
         User user = context.getUser();
         
         if (user == null)
         {
            return new ErrorResponse("No authenticated user", false);
         }
         
         Portal portal = null;
         if (dashboard) {
             portal = context.getController().getCustomizationManager().getDashboard(user);
         } 

         if (portal != null)
         {
            Page page = portal.getDefaultPage();
            return new UpdatePageResponse(page.getId());
         }
         else
         {
            return new UnavailableResourceResponse("Dashboard for user:" + user.getUserName() + " can't be found" , false);
         }
      }
      else
      {
         Portal portal = root.getDefaultPortal();
         Page page = portal.getDefaultPage();
         return new UpdatePageResponse(page.getId());
      }
   }
}
