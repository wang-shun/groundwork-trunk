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
package org.jboss.portal.core.identity.services.impl;

import org.jboss.portal.core.controller.ControllerCommand;
import org.jboss.portal.core.controller.ControllerContext;
import org.jboss.portal.core.controller.command.mapper.AbstractCommandFactory;
import org.jboss.portal.core.identity.services.IdentityCommandFactory;
import org.jboss.portal.core.identity.services.IdentityConstants;
import org.jboss.portal.core.identity.services.metadata.CoreIdentityConfigurationException;
import org.jboss.portal.core.identity.services.workflow.ValidateEmailService;
import org.jboss.portal.core.model.instance.command.action.InvokePortletInstanceRenderCommand;
import org.jboss.portal.portlet.ParametersStateString;
import org.jboss.portal.server.ServerInvocation;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class IdentityCommandFactoryService extends AbstractCommandFactory implements IdentityCommandFactory
{
   /** Portlet instance */
   private String instanceId;

   /** jBPM email validation service */
   protected ValidateEmailService validateEmailService = null;

   public void setInstanceId(String instanceId)
   {
      this.instanceId = instanceId;
   }

   public void setValidateEmailService(ValidateEmailService validateEmailService)
   {
      this.validateEmailService = validateEmailService;
   }
   
   public ControllerCommand doMapping(ControllerContext controllerContext, ServerInvocation invocation, String host,
         String contextPath, String requestPath)
   {
    ParametersStateString renderParameters = ParametersStateString.create();
    String operation = IdentityConstants.VALIDATION_FAILED;

      if (requestPath != null && requestPath.length() > 1)
      {

         // Remove starting /
         String rPath = requestPath.trim().substring(1, requestPath.length());
         String[] aRequest = rPath.split("/");
         int aLength = aRequest.length;

         if (aLength == 2)
         {
            String bpmId = aRequest[0];
            String hash = aRequest[1];
            try
            {
               // Validating email
               String status = validateEmailService.validateEmail(bpmId, hash);
               // Passing status to portlet render parameters
               operation = status;
            }
            catch(CoreIdentityConfigurationException e)
            {
               log.error(e);
               operation = IdentityConstants.VALIDATION_ERROR;
            }

         }
      }
      // Perform a render URL on the target window
      
      renderParameters.setValue("operation", operation);
      InvokePortletInstanceRenderCommand ipirc = new InvokePortletInstanceRenderCommand(instanceId, renderParameters);
      return ipirc;
   }

}
