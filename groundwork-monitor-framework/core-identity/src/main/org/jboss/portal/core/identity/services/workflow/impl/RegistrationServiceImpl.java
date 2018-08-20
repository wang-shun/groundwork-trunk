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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jboss.logging.Logger;
import org.jboss.portal.common.io.IOTools;
import org.jboss.portal.core.identity.services.IdentityConstants;
import org.jboss.portal.core.identity.services.IdentityUserManagementService;
import org.jboss.portal.core.identity.services.metadata.CoreIdentityConfigurationException;
import org.jboss.portal.core.identity.services.metadata.IdentityUIConfiguration;
import org.jboss.portal.core.identity.services.metadata.IdentityUIConfigurationService;
import org.jboss.portal.core.identity.services.workflow.RegistrationService;
import org.jboss.portal.core.identity.services.workflow.UserContainer;
import org.jboss.portal.identity.IdentityException;
import org.jboss.portal.identity.User;
import org.jboss.portal.jems.as.JNDI;
import org.jboss.portal.jems.as.system.AbstractJBossService;
import org.jboss.portal.workflow.service.WorkflowService;
import org.jbpm.JbpmContext;
import org.jbpm.db.GraphSession;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class RegistrationServiceImpl extends AbstractJBossService implements RegistrationService
{

   /** The UserPortlet subscription mode */
   private String subscriptionMode = null;

   /** The UserManagementPortlet subscription mode */
   private String adminSubscriptionMode = null;

   /** The core-identity configuration service */
   private IdentityUIConfigurationService identityUIConfigurationService;

   /** The core-identity user management service */
   private IdentityUserManagementService identityUserManagementService;

   /** The jBPM workflow service */
   private WorkflowService workflowService = null;

   /** The logger */
   private static final Logger log = Logger.getLogger(RegistrationServiceImpl.class);

   /** . */
   private JNDI.Binding jndiBinding;

   /** . */
   private String jndiName = null;

   public void startService() throws Exception
   {
      super.startService();

      // Getting subscription modes
      IdentityUIConfiguration cf = this.identityUIConfigurationService.getConfiguration();
      this.subscriptionMode = cf.getSubscriptionMode();
      this.adminSubscriptionMode = cf.getAdminSubscriptionMode();

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
      if (this.workflowService == null)
      {
         this.workflowService = identityUIConfigurationService.getWorkflowService();
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

   public void setIdentityUIConfigurationService(IdentityUIConfigurationService identityUIConfigurationService)
   {
      this.identityUIConfigurationService = identityUIConfigurationService;
   }

   public IdentityUIConfigurationService getIdentityUIConfigurationService()
   {
      return identityUIConfigurationService;
   }

   public IdentityUserManagementService getIdentityUserManagementService()
   {
      return identityUserManagementService;
   }

   public void setIdentityUserManagementService(IdentityUserManagementService identityManagementService)
   {
      this.identityUserManagementService = identityManagementService;
   }

   public String registerUser(String url, String username, String password, Map<String, Object> profileMap, List<String> roles, Locale locale, boolean adminFlag)
      throws CoreIdentityConfigurationException
   {

      if (adminFlag)
      {
         if (adminSubscriptionMode == null
               || IdentityConstants.SUBSCRIPTION_MODE_AUTOMATIC.equals(adminSubscriptionMode))
         {
            // Admin - automatic subscription
            try
            {
               this.getIdentityUserManagementService().createUser(username, password, profileMap, roles);
               return IdentityConstants.REGISTRATION_REGISTERED;
            }
            catch(IdentityException e)
            {
               throw new CoreIdentityConfigurationException("registration failed", e);
            }
         }
         else
         {
            // Admin - jBPM subscription
            UserContainer user = new UserContainer(username, password, profileMap, roles);
            this.registerUserWorkflow(url, user, locale, this.adminSubscriptionMode);
            return IdentityConstants.REGISTRATION_PENDING;
         }
      }
      else
      {
         if (subscriptionMode == null || IdentityConstants.SUBSCRIPTION_MODE_AUTOMATIC.equals(subscriptionMode))
         {
            // User - automatic subscription
            try
            {
               this.getIdentityUserManagementService().createUser(username, password, profileMap, roles);
               return IdentityConstants.REGISTRATION_REGISTERED;
            }
            catch(IdentityException e)
            {
               throw new CoreIdentityConfigurationException("registration failed", e);
            }
         }
         else
         {
            // User - jBPM subscription
            UserContainer user = new UserContainer(username, password, profileMap, roles);
            this.registerUserWorkflow(url, user, locale, this.subscriptionMode);
            return IdentityConstants.REGISTRATION_PENDING;
         }
      }
   }

   private void registerUserWorkflow(String url, UserContainer user, Locale locale, String processName) throws CoreIdentityConfigurationException
   {
      JbpmContext jbpmContext = null;
      ProcessInstance processInstance = null;
      boolean success = false;

      try
      {
         jbpmContext = this.getWorkflowService().getJbpmConfiguration().createJbpmContext();
         processInstance = jbpmContext.newProcessInstance(processName);
         Token token = processInstance.getRootToken();

         // Setting required attributes for the workflow
         processInstance.getContextInstance().setVariable(IdentityConstants.PORTAL_URL, url);
         processInstance.getContextInstance().setVariable(IdentityConstants.VARIABLE_USER, user);
         processInstance.getContextInstance().setVariable(IdentityConstants.VARIABLE_EMAIL,
               user.getProfileMap().get(User.INFO_USER_EMAIL_REAL));
         processInstance.getContextInstance().setVariable(IdentityConstants.VARIABLE_LOCALE, locale);
         processInstance.getContextInstance().setVariable(IdentityConstants.ACTION,
               IdentityConstants.ACTION_REGISTER_USER);

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

   public String approve(String id, boolean approve) throws CoreIdentityConfigurationException
   {
      // return registered if workflow is disabled
      if (!this.identityUIConfigurationService.getConfiguration().enableWorkflow())
      {
         return IdentityConstants.REGISTRATION_REGISTERED;
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
            if (token.getNode().getName().equals(IdentityConstants.JBPM_NODE_APPROVAL))
            {
               if (approve)
               {
                  token.signal(IdentityConstants.JBPM_TRANSITION_APPROVED);
               }
               else
               {
                  token.signal(IdentityConstants.JBPM_TRANSITION_REJECTED);
               }
            }
         }
      }
      finally
      {
         IOTools.safeClose(jbpmContext);
      }
      return IdentityConstants.REGISTRATION_PENDING;
   }

   public int getPendingCount() throws CoreIdentityConfigurationException
   {
      int count = 0;
      JbpmContext jbpmContext = null;
      try
      {
         List userProcessInstances = this.getProcessInstances(jbpmContext, this.subscriptionMode);
         if (userProcessInstances != null && userProcessInstances.size() > 0)
         {
            Iterator i = userProcessInstances.iterator();
            while (i.hasNext())
            {
               ProcessInstance instance = (ProcessInstance) i.next();
               Node node = instance.getRootToken().getNode();
               if (IdentityConstants.JBPM_NODE_APPROVAL.equals(node.getName()))
                  count = !instance.hasEnded() ? count + 1 : count;
            }
         }
         if (! this.adminSubscriptionMode.equals(this.subscriptionMode)
               && ! this.adminSubscriptionMode.equals(IdentityConstants.SUBSCRIPTION_MODE_AUTOMATIC))
         {
            List adminProcessInstances = this.getProcessInstances(jbpmContext, this.adminSubscriptionMode);
            if (adminProcessInstances != null && adminProcessInstances.size() > 0)
            {
               Iterator i = adminProcessInstances.iterator();
               while (i.hasNext())
               {
                  ProcessInstance instance = (ProcessInstance) i.next();
                  Node node = instance.getRootToken().getNode();
                  if (IdentityConstants.JBPM_NODE_APPROVAL.equals(node.getName()))
                     count = !instance.hasEnded() ? count + 1 : count;
               }
            }
         }
      }
      finally
      {
         IOTools.safeClose(jbpmContext);
      }
      return count;
   }

   public List<UserContainer> getPendingUsers(String nodeName) throws CoreIdentityConfigurationException
   {
      List<UserContainer> queue = new ArrayList<UserContainer>();
      JbpmContext jbpmContext = null;
      try
      {
         List userProcessInstances = this.getProcessInstances(jbpmContext, this.subscriptionMode);
         if (userProcessInstances != null && userProcessInstances.size() > 0)
         {
            queue.addAll(this.getPendingUser(userProcessInstances, nodeName));
         }

         if (! this.adminSubscriptionMode.equals(this.subscriptionMode)
               && ! this.adminSubscriptionMode.equals(IdentityConstants.SUBSCRIPTION_MODE_AUTOMATIC))
         {
            List adminProcessInstances = this.getProcessInstances(jbpmContext, this.adminSubscriptionMode);
            if (adminProcessInstances != null && adminProcessInstances.size() > 0)
            {
               queue.addAll(this.getPendingUser(adminProcessInstances, nodeName));
            }
         }
      }
      finally
      {
         IOTools.safeClose(jbpmContext);
      }
      return queue;
   }

   private List<UserContainer> getPendingUser(List processInstances, String nodeName)
   {
      List<UserContainer> queue = new ArrayList<UserContainer>();
      if (processInstances != null)
      {
         Iterator i = processInstances.iterator();
         while (i.hasNext())
         {
            ProcessInstance instance = (ProcessInstance) i.next();
            if (!instance.hasEnded())
            {
               Node node = instance.getRootToken().getNode();
               Object obj = instance.getContextInstance().getVariable(IdentityConstants.VARIABLE_USER);

               // Filter by node
               if (nodeName != null && nodeName.equals(node.getName()))
               {
                  if (obj instanceof UserContainer)
                  {
                     // Filling pending user List 
                     UserContainer user = (UserContainer) obj;
                     user.setProcessId(String.valueOf(instance.getId()));
                     user.setCurrentNode(node.getName());
                     queue.add(user);
                  }
               }
               else if (nodeName == null)
               {
                  if (obj instanceof UserContainer)
                  {
                     // Filling pending user List 
                     UserContainer user = (UserContainer) obj;
                     user.setProcessId(String.valueOf(instance.getId()));
                     user.setCurrentNode(node.getName());
                     queue.add(user);
                  }
               }
            }
         }
      }
      return queue;
   }

   public boolean checkUsername(String username) throws CoreIdentityConfigurationException
   {
      JbpmContext jbpmContext = null;
      try
      {
         List userProcessInstances = this.getProcessInstances(jbpmContext, this.subscriptionMode);
         if (userProcessInstances != null && userProcessInstances.size() > 0)
         {
            if (this.checkUsername(userProcessInstances, username))
               return true;
         }
         
         if (! this.adminSubscriptionMode.equals(this.subscriptionMode)
               && ! this.adminSubscriptionMode.equals(IdentityConstants.SUBSCRIPTION_MODE_AUTOMATIC))
         {
            List adminProcessInstances = this.getProcessInstances(jbpmContext, this.adminSubscriptionMode);
            if (adminProcessInstances != null && adminProcessInstances.size() > 0)
            {
               if (this.checkUsername(adminProcessInstances, username))
                  return true;
            }
         }
      }
      finally
      {
         IOTools.safeClose(jbpmContext);
      }
      return false;
   }

   private boolean checkUsername(List processInstances, String username)
   {
      boolean usernameTaken = false;
      if (processInstances != null)
      {
         Iterator i = processInstances.iterator();
         while (i.hasNext())
         {
            ProcessInstance instance = (ProcessInstance) i.next();
            if (!instance.hasEnded())
            {
               Object obj = instance.getContextInstance().getVariable(IdentityConstants.VARIABLE_USER);
               if (obj instanceof UserContainer)
               {
                  UserContainer user = (UserContainer) obj;
                  if (username != null && username.equals(user.getUsername()))
                  {
                     usernameTaken = true;
                  }
               }
            }
         }
      }
      return usernameTaken;
   }

   private List getProcessInstances(JbpmContext jbpmContext, String processName) throws CoreIdentityConfigurationException
   {
      List processInstances = new ArrayList();
      if (!IdentityConstants.SUBSCRIPTION_MODE_AUTOMATIC.equals(processName))
      {
         if (jbpmContext == null)
         {
            jbpmContext = this.getWorkflowService().getJbpmConfiguration().createJbpmContext();
         }
         GraphSession graphSession = jbpmContext.getGraphSession();
         ProcessDefinition processDefinition = graphSession.findLatestProcessDefinition(processName);
         if ( processDefinition != null )
         {
            processInstances = graphSession.findProcessInstances(processDefinition.getId());
         }
         else
         {
            throw new CoreIdentityConfigurationException("Could not find process definition for process name: "+ processName);
         }
      }
      return processInstances;
   }

}
