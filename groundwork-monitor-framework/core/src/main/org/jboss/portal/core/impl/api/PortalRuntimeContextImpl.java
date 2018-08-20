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
package org.jboss.portal.core.impl.api;

import org.jboss.portal.api.PortalRuntimeContext;
import org.jboss.portal.api.navstate.NavigationalStateContext;
import org.jboss.portal.api.session.PortalSession;
import org.jboss.portal.core.controller.ControllerCommand;
import org.jboss.portal.core.controller.ControllerContext;
import org.jboss.portal.core.impl.api.navstate.NavigationalStateContextImpl;
import org.jboss.portal.core.impl.api.node.PortalNodeURLFactory;
import org.jboss.portal.core.impl.api.session.PortalSessionImpl;
import org.jboss.portal.identity.User;

import javax.servlet.http.HttpSession;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public class PortalRuntimeContextImpl implements PortalRuntimeContext
{

   /** . */
   private NavigationalStateContextImpl navigationalStateContext;

   /** . */
   private PortalNodeURLFactory urlFactory;

   /** . */
   private PortalSession session;

   /** . */
   private String userId;

   public PortalRuntimeContextImpl(HttpSession session)
   {
      this.session = new PortalSessionImpl(session);
   }

   public PortalRuntimeContextImpl(HttpSession session, String userId)
   {
      this.session = new PortalSessionImpl(session);
      this.userId = userId;
   }

   public PortalRuntimeContextImpl(ControllerContext controllerContext)
   {
      navigationalStateContext = new NavigationalStateContextImpl(controllerContext.getAttributeResolver(ControllerCommand.NAVIGATIONAL_STATE_SCOPE));
      session = new PortalSessionImpl(controllerContext.getServerInvocation().getServerContext().getClientRequest().getSession());
      urlFactory = new PortalNodeURLFactory(controllerContext);

      //
      User user = controllerContext.getUser();
      if (user != null)
      {
         userId = user.getId().toString();
      }
   }

   public String getUserId()
   {
      return userId;
   }

   public PortalSession getSession()
   {
      return session;
   }

   public NavigationalStateContext getNavigationalStateContext()
   {
      return navigationalStateContext;
   }

   public PortalNodeURLFactory getURLFactory()
   {
      return urlFactory;
   }
}
