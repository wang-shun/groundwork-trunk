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
package org.jboss.portal.core.impl.api.navstate;

import org.jboss.portal.Mode;
import org.jboss.portal.WindowState;
import org.jboss.portal.api.navstate.NavigationalStateContext;
import org.jboss.portal.api.node.PortalNode;
import org.jboss.portal.common.invocation.AttributeResolver;
import org.jboss.portal.core.impl.api.node.PortalNodeImpl;
import org.jboss.portal.core.model.portal.navstate.WindowNavigationalState;
import org.jboss.portal.core.navstate.NavigationalStateKey;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public class NavigationalStateContextImpl implements NavigationalStateContext
{

   /** . */
   private AttributeResolver navigationalStateResolver;

   public NavigationalStateContextImpl(AttributeResolver navigationalStateResolver)
   {
      this.navigationalStateResolver = navigationalStateResolver;
   }

   private WindowNavigationalState getWNS(PortalNode window, boolean create)
   {
      PortalNodeImpl pon = (PortalNodeImpl)window;
      NavigationalStateKey key = new NavigationalStateKey(WindowNavigationalState.class, pon.getObjectId());
      WindowNavigationalState wns = (WindowNavigationalState)navigationalStateResolver.getAttribute(key);
      if (wns == null && create)
      {
         wns = WindowNavigationalState.create();
         navigationalStateResolver.setAttribute(key, wns);
      }
      return wns;
   }

   public WindowState getWindowState(PortalNode window) throws IllegalArgumentException
   {
      if (window == null)
      {
         throw new IllegalArgumentException("No null window can be provided");
      }

      //
      WindowNavigationalState wns = getWNS(window, false);
      if (wns != null)
      {
         return wns.getWindowState();
      }
      else
      {
         return null;
      }
   }

   public void setWindowState(PortalNode window, WindowState windowState) throws IllegalArgumentException
   {
      if (window == null)
      {
         throw new IllegalArgumentException("No null window can be provided");
      }
      if (windowState == null)
      {
         throw new IllegalArgumentException();
      }

      //
      PortalNodeImpl pon = (PortalNodeImpl)window;
      WindowNavigationalState.setWindowState(navigationalStateResolver, pon.getObjectId(), windowState);
   }

   public Mode getMode(PortalNode window) throws IllegalArgumentException
   {
      if (window == null)
      {
         throw new IllegalArgumentException("No null window can be provided");
      }

      //
      WindowNavigationalState wns = getWNS(window, false);
      if (wns != null)
      {
         return wns.getMode();
      }
      else
      {
         return null;
      }
   }

   public void setMode(PortalNode window, Mode mode) throws IllegalArgumentException
   {
      if (window == null)
      {
         throw new IllegalArgumentException("No null window can be provided");
      }
      if (mode == null)
      {
         throw new IllegalArgumentException();
      }

      //
      PortalNodeImpl pon = (PortalNodeImpl)window;

      //
      WindowNavigationalState.setMode(navigationalStateResolver, pon.getObjectId(), mode);
   }
}
