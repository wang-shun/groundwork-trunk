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
package org.jboss.portal.core.identity.services.workflow.impl;

import java.util.Locale;

import org.jboss.logging.Logger;
import org.jboss.portal.common.io.IOTools;
import org.jboss.portal.core.identity.services.IdentityConstants;
import org.jboss.portal.core.identity.services.IdentityUserManagementService;
import org.jboss.portal.core.identity.services.metadata.CoreIdentityConfigurationException;
import org.jboss.portal.core.identity.services.metadata.IdentityUIConfigurationService;
import org.jboss.portal.core.identity.services.workflow.UserContainer;
import org.jboss.portal.core.identity.services.workflow.ValidateEmailService;
import org.jboss.portal.identity.IdentityException;
import org.jboss.portal.identity.User;
import org.jboss.portal.jems.as.JNDI;
import org.jboss.portal.jems.as.system.AbstractJBossService;
import org.jboss.portal.workflow.service.WorkflowService;
import org.jbpm.JbpmContext;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class ValidateEmailServiceImpl extends AbstractJBossService implements ValidateEmailService
{

   /** The e-mail validation process name */
   private String processName = IdentityConstants.jbp_identity_validate_email_process_name;

   /** The core-identity configuration service */
   private IdentityUIConfigurationService identityUIConfigurationService;
   
   /** The core-identity user management service */
   private IdentityUserManagementService identityUserManagementService;
   
   /** The workflow service */
   private WorkflowService workflowService = null;
   
   /** The logger */
   private static final Logger log = Logger.getLogger(ValidateEmailService.class);
   
   /** The JNDI binding */
   private JNDI.Binding jndiBinding;

   /** The jndi name */
   private String jndiName = null;

   public void startService() throws Exception
   {
      super.startService();

      if (this.jndiName != null)
      {
         jndiBinding = new JNDI.Binding(jndiName, this);
         jndiBinding.bind();
      }
   }

   public void stopService() throws Exception
   {
      super.stopService();

      if (jndiBinding != null)
      {
         jndiBinding.unbind();
         jndiBinding = null;
      }
   }

   public WorkflowService getWorkflowService() throws CoreIdentityConfigurationException
   {
      if ( workflowService == null)
      {
            workflowService = (WorkflowService) identityUIConfigurationService.getWorkflowService();
      }
      return this.workflowService;
   }

   public void setWorkflowService(WorkflowService workflowService)
   {
      this.workflowService = workflowService;
   }

   public String getJNDIName()
   {
      return this.jndiName;
   }

   public void setJNDIName(String jndiName)
   {
      this.jndiName = jndiName;
   }
   
   public IdentityUIConfigurationService getIdentityUIConfigurationService()
   {
      return identityUIConfigurationService;
   }

   public void setIdentityUIConfigurationService(IdentityUIConfigurationService identityUIConfigurationService)
   {
      this.identityUIConfigurationService = identityUIConfigurationService;
   }

   public IdentityUserManagementService getIdentityUserManagementService()
   {
      return identityUserManagementService;
   }

   public void setIdentityUserManagementService(IdentityUserManagementService identityUserManagementService)
   {
      this.identityUserManagementService = identityUserManagementService;
   }

   public String changeEmail(String url, User user, String email, Locale locale) throws CoreIdentityConfigurationException
   {
      if (! this.identityUIConfigurationService.getConfiguration().enableWorkflow())
      {
         try
         {
            this.getIdentityUserManagementService().updateEmail(user.getUserName(), email);
            return IdentityConstants.REGISTRATION_REGISTERED;
         }
         catch(IdentityException e)
         {
            throw new CoreIdentityConfigurationException("udating the email address failed.", e);
         }
      }
      else
      {
         this.changeEmailWorkflow(url, user, email, locale);
         return IdentityConstants.REGISTRATION_PENDING;
      }
   }
   
   public void changeEmailWorkflow(String url, User user, String email, Locale locale) throws CoreIdentityConfigurationException
   {
      JbpmContext jbpmContext = null;
      ProcessInstance processInstance = null;
      // String registrationHash = this.hashGen(); - generated when sending email
      boolean success = false;
      try
      {
         jbpmContext = this.getWorkflowService().getJbpmConfiguration().createJbpmContext();
         processInstance = jbpmContext.newProcessInstance(this.processName);
         Token token = processInstance.getRootToken();

         processInstance.getContextInstance().setVariable(IdentityConstants.PORTAL_URL, url);
         processInstance.getContextInstance().setVariable(IdentityConstants.VARIABLE_USER, new UserContainer(user));
         processInstance.getContextInstance().setVariable(IdentityConstants.VARIABLE_EMAIL, email);
         processInstance.getContextInstance().setVariable(IdentityConstants.VARIABLE_LOCALE, locale);
         processInstance.getContextInstance().setVariable(IdentityConstants.ACTION, IdentityConstants.ACTION_CHANGE_EMAIL);

         token.signal();
         success = true;
      }
      finally
      {
         if (processInstance != null && success)
         {
            jbpmContext.save(processInstance);
         }
         IOTools.safeClose(jbpmContext);
      }
   }

   public String validateEmail(String id, String registrationHash) throws CoreIdentityConfigurationException
   {
      String success = IdentityConstants.VALIDATION_FAILED;
      if (!this.identityUIConfigurationService.getConfiguration().enableWorkflow())
      {
         return success;
      }
      
      JbpmContext jbpmContext = null;
      try
      {
         long processId = Long.valueOf(id).longValue();
         jbpmContext = this.getWorkflowService().getJbpmConfiguration().createJbpmContext();
         ProcessInstance processInstance = jbpmContext.getProcessInstance(processId);
         
         if (processInstance != null)
         {
            Token token = processInstance.getRootToken();
            if (token != null && token.getNode().getName().equals(IdentityConstants.JBPM_NODE_EMAIL_VALIDATION))
            {
               String hash = (String) processInstance.getContextInstance().getVariable(IdentityConstants.VALIDATION_HASH);
               if (registrationHash.equals(hash) && ! processInstance.hasEnded())
               {
                  token.signal(IdentityConstants.JBPM_TRANSITION_VALIDATED);
                  success = processInstance.getProcessDefinition().getName();
               }
            }
         }
      }
      finally
      {
         IOTools.safeClose(jbpmContext);
      }
      return success;
   }
}

