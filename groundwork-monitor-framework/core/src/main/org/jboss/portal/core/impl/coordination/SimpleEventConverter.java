/*
* JBoss, a division of Red Hat
* Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/

package org.jboss.portal.core.impl.coordination;

import org.jboss.portal.core.controller.coordination.EventConverter;
import org.jboss.portal.core.model.portal.Window;
import org.jboss.portal.portlet.controller.event.PortletWindowEvent;
import org.jboss.portal.portlet.impl.info.ContainerTypeInfo;
import org.jboss.portal.portlet.info.EventInfo;

import javax.xml.namespace.QName;
import java.io.Serializable;

/**
 * @author <a href="mailto:boleslaw dot dawidowicz at redhat anotherdot com">Boleslaw Dawidowicz</a>
 * @version : 0.1 $
 */
public class SimpleEventConverter implements EventConverter
{

   public PortletWindowEvent convertEvent(PortletWindowEvent sourceEvent, EventInfo destEventInfo, Window window)
   {

      QName destName = destEventInfo.getName();
      String windowId = window.getName();
      Serializable payload = sourceEvent.getPayload();


      // Source and destination payload types

      String sourcePayloadType = sourceEvent.getPayload().getClass().getName();
      String destPayloadType = ((ContainerTypeInfo)destEventInfo.getType()).getType().getName();


      // NOTE: rules below can be merged but I leave it like this to have more clear logic to follow

      // Same payload types

      if (destPayloadType != null && sourcePayloadType.equals(destPayloadType))
      {
         return new PortletWindowEvent(destName, payload, windowId);
      }

      // source payload == null -> null

      else if (sourceEvent.getPayload() == null)
      {
         return new PortletWindowEvent(destName, null, windowId);
      }

      // destination have no type

      else if (destPayloadType == null || destPayloadType.equals(""))
      {
         return new PortletWindowEvent(destName, null, windowId);
      }

      // DEFAULT - just pass null value
      return new PortletWindowEvent(destName, null, windowId);


   }


}
