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
package org.jboss.portal.core.identity.ui.common;

import org.jboss.logging.Logger;
import org.jboss.portal.core.identity.services.metadata.CoreIdentityConfigurationException;
import org.jboss.portal.core.identity.services.metadata.IdentityUIConfiguration;
import org.jboss.portal.core.identity.services.metadata.IdentityUIConfigurationService;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class ConfigurationBean
{
   
   /** The identityUIConfiguration */
   private IdentityUIConfiguration configuration;
   
   /** The identityUIConfigurationService */
   private IdentityUIConfigurationService identityUIConfigurationService;
   
   /** the logger */
   private static final Logger log = Logger.getLogger(ConfigurationBean.class);
   
   public IdentityUIConfiguration getConfiguration()
   {
      return configuration;
   }
   
   public IdentityUIConfigurationService getIdentityUIConfigurationService()
   {
      return identityUIConfigurationService;
   }
   
   public void setIdentityUIConfigurationService(IdentityUIConfigurationService identityUIConfigurationService)
   {
      this.identityUIConfigurationService = identityUIConfigurationService;
      this.configuration = identityUIConfigurationService.getConfiguration();
   }
   
   public boolean isValidConfiguration()
   {
      if(this.identityUIConfigurationService == null)
      {
         log.error("IdentityUIConfigurationService not found.");
         return false;
      }
      
      try
      {
         this.identityUIConfigurationService.isValidConfiguration();
      }
      catch(CoreIdentityConfigurationException e)
      {
         log.error("", e);
         return false;
      }
      return true;
   }
}

