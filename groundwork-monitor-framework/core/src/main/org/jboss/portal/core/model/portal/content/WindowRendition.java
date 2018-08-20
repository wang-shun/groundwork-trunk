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
package org.jboss.portal.core.model.portal.content;

import org.jboss.portal.Mode;
import org.jboss.portal.WindowState;
import org.jboss.portal.core.controller.ControllerResponse;

import java.util.List;
import java.util.Map;

/**
 * Aggregates window specific data and a controller response.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 10985 $
 */
public class WindowRendition
{

   /** The window properties. */
   private final Map<String, String> properties;

   /** The window state. */
   private final WindowState windowState;

   /** The window mode. */
   private final Mode mode;

   /** The supported window states. */
   private List supportedWindowStates;

   /** The supported modes. */
   private List supportedModes;

   /** The controller response. */
   private ControllerResponse controllerResponse;

   public WindowRendition(Map properties, WindowState windowState, Mode mode, List supportedWindowStates, List supportedModes, ControllerResponse controllerResponse)
   {
      if (controllerResponse == null)
      {
         throw new IllegalArgumentException("Null controller response not accepted");
      }

      //
      this.properties = properties;
      this.windowState = windowState;
      this.mode = mode;
      this.supportedWindowStates = supportedWindowStates;
      this.supportedModes = supportedModes;
      this.controllerResponse = controllerResponse;
   }

   public Map<String, String> getProperties()
   {
      return properties;
   }

   public WindowState getWindowState()
   {
      return windowState;
   }

   public Mode getMode()
   {
      return mode;
   }

   public List getSupportedWindowStates()
   {
      return supportedWindowStates;
   }

   public void setSupportedWindowStates(List supportedWindowStates)
   {
      this.supportedWindowStates = supportedWindowStates;
   }

   public List getSupportedModes()
   {
      return supportedModes;
   }

   public void setSupportedModes(List supportedModes)
   {
      this.supportedModes = supportedModes;
   }

   public ControllerResponse getControllerResponse()
   {
      return controllerResponse;
   }

   public void setControllerResponse(ControllerResponse controllerResponse)
   {
      this.controllerResponse = controllerResponse;
   }
}
