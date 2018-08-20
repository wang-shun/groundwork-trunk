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
package org.jboss.portal.core.model.instance;

import org.jboss.portal.core.controller.ControllerCommand;
import org.jboss.portal.core.controller.portlet.InvokePortletCommandFactory;
import org.jboss.portal.core.model.instance.command.action.InvokePortletInstanceActionCommand;
import org.jboss.portal.core.model.instance.command.action.InvokePortletInstanceRenderCommand;
import org.jboss.portal.portlet.ActionURL;
import org.jboss.portal.portlet.RenderURL;
import org.jboss.portal.portlet.ResourceURL;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 11108 $
 */
public class InvokePortletInstanceCommandFactory implements InvokePortletCommandFactory
{

   /** . */
   private String instanceId;

   public InvokePortletInstanceCommandFactory(String instanceId)
   {
      this.instanceId = instanceId;
   }

   public ControllerCommand createInvokeActionCommand(ActionURL portletURL)
   {
      return new InvokePortletInstanceActionCommand(
         instanceId,
         null,
         portletURL.getInteractionState(),
         null);
   }

   public ControllerCommand createInvokeRenderCommand(RenderURL portletURL)
   {
      return new InvokePortletInstanceRenderCommand(
         instanceId,
         portletURL.getNavigationalState());
   }

   /**
    * We don't implement (yet?) for instances as it is rather internal and not needed yet.
    */
   public ControllerCommand createInvokeResourceCommand(ResourceURL resourceURL)
   {
      return null;
   }
}
