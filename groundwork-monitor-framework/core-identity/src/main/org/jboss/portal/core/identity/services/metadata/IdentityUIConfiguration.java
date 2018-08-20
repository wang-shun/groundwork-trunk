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

import java.util.List;
import java.util.Map;

import org.jboss.portal.core.identity.services.IdentityConstants;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class IdentityUIConfiguration
{
   /** UserPortlet subscription mode */
   private String subscriptionMode;
   
   /** UserManagementPortlet subscription mode */
   private String adminSubscriptionMode = "automatic";
   
   /** Overwrite existing workflow */
   private boolean overwriteWorkflow = false;
   
   /** E-Mail domain */
   private String emailDomain;
   
   /** E-Mail from */
   private String emailFrom;
   
   /** List of default roles */
   private List<String> defaultRoles;
   
   /** Characters used for generating a random password */
   private String passwordGenerationCharacters;
   
   /** Map of available UI-Components */
   private Map<String, UIComponentConfiguration> uiComponents;

   public String getSubscriptionMode()
   {
      return subscriptionMode;
   }

   public void setSubscriptionMode(String subscriptionMode)
   {
      this.subscriptionMode = subscriptionMode;
   }

   public String getAdminSubscriptionMode()
   {
      return adminSubscriptionMode;
   }

   public void setAdminSubscriptionMode(String adminSubscriptionMode)
   {
      this.adminSubscriptionMode = adminSubscriptionMode;
   }

   public boolean isOverwriteWorkflow()
   {
      return overwriteWorkflow;
   }

   public void setOverwriteWorkflow(boolean overwriteWorkflow)
   {
      this.overwriteWorkflow = overwriteWorkflow;
   }

   public String getEmailDomain()
   {
      return emailDomain;
   }

   public void setEmailDomain(String emailDomain)
   {
      this.emailDomain = emailDomain;
   }

   public String getEmailFrom()
   {
      return emailFrom;
   }

   public void setEmailFrom(String emailFrom)
   {
      this.emailFrom = emailFrom;
   }

   public List<String> getDefaultRoles()
   {
      return defaultRoles;
   }

   public void setDefaultRoles(List<String> defaultRoles)
   {
      this.defaultRoles = defaultRoles;
   }

   public String getPasswordGenerationCharacters()
   {
      return passwordGenerationCharacters;
   }

   public void setPasswordGenerationCharacters(String passwordGenerationCharacters)
   {
      this.passwordGenerationCharacters = passwordGenerationCharacters;
   }

   public Map<String, UIComponentConfiguration> getUiComponents()
   {
      return uiComponents;
   }

   public void setUiComponents(Map<String, UIComponentConfiguration> uiComponents)
   {
      this.uiComponents = uiComponents;
   }
   
   public boolean enableWorkflow()
   {
      // If subscriptionModes != automatic == true
      if (this.subscriptionMode.equals(IdentityConstants.SUBSCRIPTION_MODE_AUTOMATIC)
            && this.adminSubscriptionMode.equals(IdentityConstants.SUBSCRIPTION_MODE_AUTOMATIC))
      {
         return false;
      }
      else 
      {
         return true;
      }
   }
}

