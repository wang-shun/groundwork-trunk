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
package org.jboss.portal.core.model.instance.command.action;

import org.jboss.portal.Mode;
import org.jboss.portal.WindowState;
import org.jboss.portal.common.util.ParameterMap;
import org.jboss.portal.core.controller.ControllerException;
import org.jboss.portal.core.controller.ControllerResponse;
import org.jboss.portal.core.controller.command.info.CommandInfo;
import org.jboss.portal.core.controller.portlet.ControllerResponseFactory;
import org.jboss.portal.core.controller.portlet.InvokePortletCommandFactory;
import org.jboss.portal.core.controller.portlet.PortletContextFactory;
import org.jboss.portal.core.controller.portlet.PortletInvocationFactory;
import org.jboss.portal.core.model.instance.InvokePortletInstanceCommandFactory;
import org.jboss.portal.core.model.instance.command.PortletInstanceCommand;
import org.jboss.portal.portlet.PortletInvokerException;
import org.jboss.portal.portlet.StateString;
import org.jboss.portal.portlet.invocation.ActionInvocation;
import org.jboss.portal.portlet.invocation.response.PortletInvocationResponse;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 10090 $
 */
public class InvokePortletInstanceActionCommand extends PortletInstanceCommand
{

   /** The interaction state. */
   private StateString interactionState;

   /** The nform. */
   private ParameterMap form;

   public InvokePortletInstanceActionCommand(
      String instanceId,
      StateString navigationalState,
      StateString interactionState,
      ParameterMap form)
   {
      super(instanceId, navigationalState);

      //
      this.interactionState = interactionState;
      this.form = form;
   }

   public StateString getInteractionState()
   {
      return interactionState;
   }

   public ParameterMap getForm()
   {
      return form;
   }

   public CommandInfo getInfo()
   {
      return null;
   }

   public ControllerResponse execute() throws ControllerException
   {
      try
      {
         PortletContextFactory pcf1 = new PortletContextFactory(context);
         InvokePortletCommandFactory pcf2 = new InvokePortletInstanceCommandFactory(instanceId);

         //
         ActionInvocation action = PortletInvocationFactory.createAction(
            context,
            Mode.VIEW,
            WindowState.MAXIMIZED,
            navigationalState,
            interactionState,
            form,
            pcf1,
            pcf2);

         //
         PortletInvocationResponse response = instance.invoke(action);

         //
         return ControllerResponseFactory.createActionResponse(instanceId, response);
      }
      catch (PortletInvokerException e)
      {
         return ControllerResponseFactory.createResponse(e);
      }
   }
}
