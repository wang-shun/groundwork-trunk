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

package org.jboss.portal.core.controller.coordination;

import org.jboss.portal.core.controller.portlet.ControllerPortletControllerContext;
import org.jboss.portal.core.model.portal.PageContainer;
import org.jboss.portal.core.model.portal.Window;
import org.jboss.portal.portlet.controller.event.PortletWindowEvent;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Map;

/**
 * Interface defining operations for explicit event wiring management
 *
 * @author <a href="mailto:boleslaw dot dawidowicz at redhat anotherdot com">Boleslaw Dawidowicz</a>
 * @version : 0.1 $
 */
public interface CoordinationManager
{

   // Event Discovery

   /**
    * @param event
    * @param context
    * @return all windows that given event should be delivered to with current wirings and configuration
    */
   Map<Window, PortletWindowEvent> getEventWindows(PortletWindowEvent event, ControllerPortletControllerContext context) throws IllegalCoordinationException;


   /**
    * Resolves wiring implicit mode. If there is no strategy defined for this page container method will browse parent
    * object to resolve inherited modes. If no mode is set this method will return default one
    *
    * @param page
    * @return
    */
   Boolean resolveEventWiringImplicitModeEnabled(PageContainer page);

   /**
    * Resolves binding implicit mode. If there is no mode defined for this page container method will browse parent
    * object to resolve inherited modes. If no mode is set this method will return default one
    *
    * @param pageContainer the page container
    * @return
    */
   Boolean resolveParameterBindingImplicitModeEnabled(PageContainer pageContainer);

   /**
    * Returns the list of bindings for a given window and a given name. The collection is an aggregation of parameter
    * bindings and alias bindings.
    *
    * @param window the target window
    * @param name   the target name
    * @return all binding names with a given window/name mapping
    */
   Collection<String> getBindingNames(Window window, QName name);
}
