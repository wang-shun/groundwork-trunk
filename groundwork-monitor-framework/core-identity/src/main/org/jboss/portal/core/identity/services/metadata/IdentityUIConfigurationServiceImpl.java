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
package org.jboss.portal.core.identity.services.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import org.jboss.portal.common.io.IOTools;
import org.jboss.portal.core.identity.services.IdentityConstants;
import org.jboss.portal.identity.IdentityContext;
import org.jboss.portal.identity.IdentityException;
import org.jboss.portal.identity.IdentityServiceController;
import org.jboss.portal.identity.UserProfileModule;
import org.jboss.portal.identity.info.PropertyInfo;
import org.jboss.portal.jems.as.JNDI;
import org.jboss.portal.jems.as.system.AbstractJBossService;
import org.jboss.portal.workflow.service.WorkflowService;
import org.jboss.xb.binding.JBossXBException;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.sunday.unmarshalling.ElementBinding;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBinding;
import org.jboss.xb.binding.sunday.unmarshalling.TermBeforeSetParentCallback;
import org.jboss.xb.binding.sunday.unmarshalling.TypeBinding;
import org.jboss.xb.binding.sunday.unmarshalling.UnmarshallingContext;
import org.jboss.xb.binding.sunday.unmarshalling.XsdBinder;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ProcessDefinition;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class IdentityUIConfigurationServiceImpl extends AbstractJBossService implements IdentityUIConfigurationService
{

   /** XML configuration location */
   private final static String xmlLocation = "conf/identity-ui-configuration.xml";
   
   /** XML schema location */
   private final static String schemaLocation = "conf/schema/identity-ui-configuration.xsd";

   /** Core Identity configration */
   private IdentityUIConfiguration configuration;

   /** jBPM Workflow service */
   private WorkflowService workflowService;

   /** Identity service controller */
   private IdentityServiceController identityServiceController;

   /** Identity user profile */
   private UserProfileModule userProfileModule;

   /** The JNDI binding */
   private JNDI.Binding jndiBinding;

   /** The jndi name */
   private String jndiName = null;

   public void startWorkflow() throws Exception
   {
      try
      {
         // Loading workflow if subscriptionmodes != automatic
         if (this.configuration.enableWorkflow())
         {
            // Throws an CoreIdentityConfigurationException if the workflow service is null
            this.isValidConfiguration();

            String subscriptionMode = this.configuration.getSubscriptionMode();
            String adminSubscriptionMode = this.configuration.getAdminSubscriptionMode();
            String emailValidationProcess = IdentityConstants.jbp_identity_validate_email_process_name;
            if ( subscriptionMode != null
                 && adminSubscriptionMode != null
                 && emailValidationProcess != null)
            {
               // automatically load email validation process
               this.createJBPMContext(emailValidationProcess);

               this.createJBPMContext(subscriptionMode);
               this.createJBPMContext(adminSubscriptionMode);
               log.info("jBPM workflow started ...");
            }
            else
            {
               throw new CoreIdentityConfigurationException("processName must not be null.");
            }
         }
         else
         {
            log.info("not starting jBPM workflow ...");
         }
      }
      catch (Exception e)
      {
         log.error("Error while starting core identity services ...");
         super.stopService();
         throw new CoreIdentityConfigurationException(e);
      }
   }
   
   public void stopWorkflow()
   {
      
   }
   
   
   public void startService() throws Exception
   {
      super.startService();
      // Creating IdentityUIConfiguration
      this.configuration = this.createConfiguration();

      if (this.jndiName != null)
      {
         jndiBinding = new JNDI.Binding(jndiName, this);
         jndiBinding.bind();
      }
      startWorkflow();
   }

   public void stopService() throws Exception
   {
      super.stopService();

      if (jndiBinding != null)
      {
         jndiBinding.unbind();
         jndiBinding = null;
      }
      stopWorkflow();
   }

   public String getJNDIName()
   {
      return this.jndiName;
   }

   public void setJNDIName(String jndiName)
   {
      this.jndiName = jndiName;
   }

   public IdentityUIConfiguration getConfiguration()
   {
      return this.configuration;
   }

   public IdentityServiceController getIdentityServiceController()
   {
      return identityServiceController;
   }

   public void setIdentityServiceController(IdentityServiceController identityServiceController)
   {
      this.identityServiceController = identityServiceController;
   }

   public UserProfileModule getUserProfileModule()
   {
      if (userProfileModule == null)
      {
         try
         {
            this.userProfileModule = (UserProfileModule) identityServiceController.getIdentityContext().getObject(
                  IdentityContext.TYPE_USER_PROFILE_MODULE);
         }
         catch (IdentityException e)
         {
            log.error("failed to load UserProfileModule", e);
         }
      }
      return this.userProfileModule;
   }

   public void setUserProfileModule(UserProfileModule userProfileModule)
   {
      this.userProfileModule = userProfileModule;
   }

   public WorkflowService getWorkflowService() throws CoreIdentityConfigurationException
   {
      if ( workflowService == null )
      {
         throw new CoreIdentityConfigurationException("Workflow service not found. Make sure that the workflow service is deploy.");
      }
      return workflowService;
   }

   public void setWorkflowService(WorkflowService workflowService)
   {
      this.workflowService = workflowService;
   }

   private void createJBPMContext(String processName) throws CoreIdentityConfigurationException
   {
      if (!IdentityConstants.SUBSCRIPTION_MODE_AUTOMATIC.equals(processName))
      {
         String fileName = "conf/processes/" + processName + ".xml";
         JbpmContext jbpmContext = null;
         
         try
         {            
            jbpmContext = this.getWorkflowService().getJbpmConfiguration().createJbpmContext();

            ProcessDefinition processDefinition = jbpmContext.getGraphSession().findLatestProcessDefinition(processName);
            if (processDefinition == null)
            {
               processDefinition = ProcessDefinition.parseXmlResource(fileName);
               jbpmContext.deployProcessDefinition(processDefinition);
               log.debug("deloying process definition: " + processName);
            }
            else
            {
               if (this.configuration.isOverwriteWorkflow())
               {
                  ProcessDefinition fromConfig = ProcessDefinition.parseXmlResource(fileName);
                  jbpmContext.deployProcessDefinition(fromConfig);
                  log.debug("overwriting process definition: " + processName);
               }
            }
         }
         catch(Exception e)
         {
            throw new CoreIdentityConfigurationException("Invalid subscription mode [" + processName + "] ! Please make sure that the file and the process name match - also check the syntax.", e);
         }
         finally
         {
            // Closing JbpmContext
            IOTools.safeClose(jbpmContext);
         }
      }
   }

   private IdentityUIConfiguration createConfiguration() throws CoreIdentityConfigurationException
   {
      try
      {
         SchemaBinding schema = XsdBinder.bind(getURL(schemaLocation).toString());
         schema.setIgnoreUnresolvedFieldOrClass(false);
   
         TermBeforeSetParentCallback componentCallback = new TermBeforeSetParentCallback()
         {
            public Object beforeSetParent(Object o, UnmarshallingContext ctx)
            {
               UIComponentConfiguration uiComponent = null;
               ElementBinding eb = (ElementBinding) ctx.getParticle().getTerm();
               String localPart = eb.getQName().getLocalPart();
               if ("ui-component".equals(localPart))
               {
                  uiComponent = (UIComponentConfiguration) o;
                  try
                  {
                     // Getting property reference
                     PropertyInfo propertyInfo = getUserProfileModule().getProfileInfo().getPropertyInfo(
                           uiComponent.getPropertyRef());
                     uiComponent.setPropertyInfo(propertyInfo);
                  }
                  catch (IdentityException e)
                  {
                     throw new IllegalArgumentException("cannot resolve property: " + uiComponent.getPropertyRef(), e);
                  }
               }
               return uiComponent;
            }
         };
   
         // Adding callback for ui-components
         TypeBinding cb = schema.getType(new javax.xml.namespace.QName("UIComponent"));
         cb.setBeforeSetParentCallback(componentCallback);
   
         TermBeforeSetParentCallback valueCallback = new TermBeforeSetParentCallback()
         {
            public Object beforeSetParent(Object o, UnmarshallingContext ctx)
            {
               
               ElementBinding eb = (ElementBinding) ctx.getParticle().getTerm();
               String localPart = eb.getQName().getLocalPart();
               Map m = (Map) o;
               if ("values".equals(localPart))
               {
                  UIComponentConfiguration uiComponent = (UIComponentConfiguration) ctx.getParentValue();
                  if ( m.containsKey(IdentityConstants.COMPONENT_VALUE_LOCALE) )
                  {
                     uiComponent.setPredefinedMapValues(IdentityConstants.COMPONENT_VALUE_LOCALE);
                     m.remove(IdentityConstants.COMPONENT_VALUE_LOCALE);
                  }
                  else if ( m.containsKey(IdentityConstants.COMPONENT_VALUE_THEME))
                  {
                     uiComponent.setPredefinedMapValues(IdentityConstants.COMPONENT_VALUE_THEME);
                     m.remove(IdentityConstants.COMPONENT_VALUE_THEME);
                  }
                  else if ( m.containsKey(IdentityConstants.COMPONENT_VALUE_TIMEZONE))
                  {
                     uiComponent.setPredefinedMapValues(IdentityConstants.COMPONENT_VALUE_TIMEZONE);
                     m.remove(IdentityConstants.COMPONENT_VALUE_TIMEZONE);
                  }
               }
               return m;
            }
         };
   
         // Adding callback for ui-component values
         TypeBinding vb = schema.getType(new javax.xml.namespace.QName("componentValues"));
         vb.setBeforeSetParentCallback(valueCallback);
   
         // Unmarshalling && creating configuration 
         Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();
         return (IdentityUIConfiguration) unmarshaller.unmarshal(getResource(xmlLocation), schema);
      }
      catch (IOException e)
      {
         throw new CoreIdentityConfigurationException("Could not find configuration file or schema.", e);
      }
      catch (JBossXBException e)
      {
         throw new CoreIdentityConfigurationException("Could not parse configuration file.", e);
      }
      catch (Throwable e)
      {
         throw new CoreIdentityConfigurationException("Could not parse configuration file. If you run JBoss Portal with JBoss AS 4.0.5 or minor please check the jboss portal wiki.");
      }
   }
   
   public boolean isValidConfiguration() throws CoreIdentityConfigurationException
   {
      if (this.configuration.enableWorkflow() )
      {
         this.getWorkflowService();
      }
      return true;
   }

   protected InputStream getResource(String path) throws IOException
   {
      URL url = Thread.currentThread().getContextClassLoader().getResource(path);
      if (url == null)
         throw new IOException();
      return url.openStream();
   }

   private URL getURL(String path) throws IOException
   {
      URL url = Thread.currentThread().getContextClassLoader().getResource(path);
      if (url == null)
         throw new IOException();
      return url;
   }
}
