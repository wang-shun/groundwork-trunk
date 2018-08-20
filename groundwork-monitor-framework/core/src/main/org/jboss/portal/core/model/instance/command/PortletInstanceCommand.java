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
package org.jboss.portal.core.model.instance.command;

import org.jboss.portal.core.controller.ControllerCommand;
import org.jboss.portal.core.controller.NoSuchResourceException;
import org.jboss.portal.core.model.instance.Instance;
import org.jboss.portal.core.model.instance.InstanceContainer;
import org.jboss.portal.portlet.StateString;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8786 $
 */
public abstract class PortletInstanceCommand extends ControllerCommand
{

   /** The instance id. */
   protected String instanceId;

   /** The navigational state. */
   protected StateString navigationalState;

   /** The instance displayed. */
   protected Instance instance;

   protected PortletInstanceCommand(String instanceId, StateString navigationalState)
   {
      if (instanceId == null)
      {
         throw new IllegalArgumentException();
      }

      //
      this.instanceId = instanceId;
      this.navigationalState = navigationalState;

   }

   public String getInstanceId()
   {
      return instanceId;
   }


   public void acquireResources() throws NoSuchResourceException
   {
      InstanceContainer container = context.getController().getInstanceContainer();

      //
      instance = container.getDefinition(instanceId);

      //
      if (instance == null)
      {
         throw new NoSuchResourceException("Configurator portlet instance " + instanceId + " not found");
      }
   }

   public StateString getNavigationalState()
   {
      return navigationalState;
   }
}
