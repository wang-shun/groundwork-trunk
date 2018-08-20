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
import org.jboss.portal.core.controller.ControllerContext;
import org.jboss.portal.core.controller.command.mapper.AbstractCommandFactory;
import org.jboss.portal.core.model.instance.command.action.InvokePortletInstanceActionCommand;
import org.jboss.portal.core.model.instance.command.action.InvokePortletInstanceRenderCommand;
import org.jboss.portal.core.portlet.PortletRequestDecoder;
import org.jboss.portal.server.ServerInvocation;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 10545 $
 */
public class InstanceCommandFactory extends AbstractCommandFactory
{

   /** The instance id of the command to create. */
   private String instanceId;

   public String getInstanceId()
   {
      return instanceId;
   }

   public void setInstanceId(String instanceId)
   {
      this.instanceId = instanceId;
   }

   public ControllerCommand doMapping(ControllerContext controllerContext, ServerInvocation invocation, String host, String contextPath, String requestPath)
   {
      PortletRequestDecoder decoder = new PortletRequestDecoder();
      decoder.decode(invocation.getServerContext().getQueryParameterMap(), invocation.getServerContext().getBodyParameterMap());
      switch (decoder.getType())
      {
         case PortletRequestDecoder.NAV_TYPE:
         {
            return createPortletNavCommand(decoder);
         }
         case PortletRequestDecoder.RENDER_TYPE:
         {
            return createPortletRenderCommand(decoder);
         }
         case PortletRequestDecoder.ACTION_TYPE:
         {
            return createPortletActionCommand(decoder);
         }
      }

      //
      return null;
   }

   /**
    *
    */
   public ControllerCommand createPortletActionCommand(PortletRequestDecoder decoder)
   {
      return new InvokePortletInstanceActionCommand(
         instanceId,
         decoder.getNavigationalState(),
         decoder.getInteractionState(),
         decoder.getForm());
   }

   /**
    *
    */
   protected ControllerCommand createPortletRenderCommand(PortletRequestDecoder decoder)
   {
      return new InvokePortletInstanceRenderCommand(instanceId, decoder.getNavigationalState());
   }

   /**
    *
    */
   protected ControllerCommand createPortletNavCommand(PortletRequestDecoder decoder)
   {
      return new InvokePortletInstanceRenderCommand(instanceId, decoder.getNavigationalState());
   }
}
