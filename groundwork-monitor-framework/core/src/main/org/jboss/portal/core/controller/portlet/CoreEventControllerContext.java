/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2008, Red Hat Middleware, LLC, and individual                    *
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

import java.util.LinkedList;
import java.util.Map;

import org.jboss.portal.portlet.controller.event.EventControllerContext;
import org.jboss.portal.portlet.controller.event.EventPhaseContext;
import org.jboss.portal.portlet.controller.event.PortletWindowEvent;
import org.jboss.portal.portlet.invocation.response.PortletInvocationResponse;
import org.jboss.portal.core.CoreConstants;
import org.jboss.portal.core.controller.coordination.CoordinationManager;
import org.jboss.portal.core.model.portal.Window;
import org.apache.log4j.Logger;

/**
 * @author <a href="mailto:julien@jboss-portal.org">Julien Viet</a>
 * @author <a href="mailto:boleslaw dot dawidowicz at redhat anotherdot com">Boleslaw Dawidowicz</a>
 * @version $Revision: 630 $
 */
public class CoreEventControllerContext implements EventControllerContext
{

   /** . */
   private final Logger log = Logger.getLogger(CoreEventControllerContext.class);

   /** . */
   private final ControllerPortletControllerContext portletControllerContext;
   
   /** Events to be consumed by the portal, unused at the moment */
   private LinkedList<PortletWindowEvent> toConsumeEvents;


   public CoreEventControllerContext(ControllerPortletControllerContext portletControllerContext)
   {
      this.portletControllerContext = portletControllerContext;
      this.toConsumeEvents = new LinkedList<PortletWindowEvent>();
   }

   public void eventProduced(EventPhaseContext context, PortletWindowEvent producedEvent, PortletWindowEvent sourceEvent)
   {
      try
      {
         CoordinationManager coordinationManager =
            portletControllerContext.getControllerContext().getController().getCoordinationManager();

         Map<Window, PortletWindowEvent> windows = coordinationManager.getEventWindows(producedEvent, portletControllerContext);

         for (PortletWindowEvent event : windows.values())
         {
            context.queueEvent(event);
         }

         // Portal events unused at the moment
         if (CoreConstants.JBOSS_PORTAL_NAMESPACE.equals(producedEvent.getName().getNamespaceURI()))
         {
            toConsumeEvents.addLast(producedEvent);
         }
      }
      catch (Exception e)
      {
         log.error("An error occured when an event was routed", e);

         //
         context.interrupt();
      }
   }

   public void eventConsumed(EventPhaseContext context, PortletWindowEvent consumedEvent, PortletInvocationResponse consumerResponse)
   {
   }

   public void eventFailed(EventPhaseContext context, PortletWindowEvent failedEvent, Throwable throwable)
   {
      log.error("Could not deliver event " + failedEvent.getName() + " to portlet window " + failedEvent.getWindowId(), throwable);
   }

   public void eventDiscarded(EventPhaseContext context, PortletWindowEvent discardedEvent, int cause)
   {
   }
}
