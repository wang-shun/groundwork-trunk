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
package org.jboss.portal.core.impl.api.node;

import org.jboss.portal.api.PortalRuntimeContext;
import org.jboss.portal.api.node.PortalNode;
import org.jboss.portal.api.node.event.PortalNodeEvent;
import org.jboss.portal.api.node.event.PortalNodeEventContext;
import org.jboss.portal.api.node.event.PortalNodeEventListener;
import org.jboss.portal.core.event.PortalEventListenerRegistry;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 12031 $
 */
public class PortalNodeEventContextImpl implements PortalNodeEventContext
{

   /** . */
   PortalEventListenerRegistry registry;

   /** . */
   private PortalNodeEvent event;

   /** . */
   private PortalNodeImpl node;

   /** . */
   private PortalRuntimeContext prc;

   public PortalNodeEventContextImpl(PortalEventListenerRegistry registry, PortalNodeImpl node, PortalNodeEvent event, PortalRuntimeContext prc)
   {
      this.registry = registry;
      this.node = node;
      this.event = event;
      this.prc = prc;
   }

   public PortalRuntimeContext getPortalRuntimeContext()
   {
      return prc;
   }

   public PortalNodeEvent dispatch()
   {
      PortalNodeEventListener listener = null;
      String listenerId = node.object.getListener();
      if (listenerId != null && listenerId.length() > 0)
      {
         Object tmp = registry.getListener(listenerId);
         if (tmp != null && tmp instanceof PortalNodeEventListener)
         {
            listener = (PortalNodeEventListener)tmp;
         }
      }

      // Dispatch
      PortalNodeEvent nextEvent = null;
      PortalNode parent = node.getParent();
      if (parent != null)
      {
         PortalNodeImpl current = node;
         try
         {
            node = (PortalNodeImpl)parent;
            if (listener != null)
            {
               nextEvent = listener.onEvent(this, event);
            }
            else
            {
               nextEvent = dispatch();
            }
         }
         finally
         {
            node = current;
         }
      }

      //
      return nextEvent;
   }

   public PortalNode getNode()
   {
      return node;
   }
}
