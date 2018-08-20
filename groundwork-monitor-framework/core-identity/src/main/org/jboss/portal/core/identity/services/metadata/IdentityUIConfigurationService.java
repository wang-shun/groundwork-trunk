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

import org.jboss.portal.workflow.service.WorkflowService;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public interface IdentityUIConfigurationService
{

   /** 
    * @return IdentityUIConfiguration 
    */
   IdentityUIConfiguration getConfiguration();
   
   /**
    * @return the workflow service
    * @throws CoreIdentityConfigurationException
    */
   WorkflowService getWorkflowService() throws CoreIdentityConfigurationException;
   
   /**
    * @param the workflow service
    */
   void setWorkflowService(WorkflowService workflowService);
   
   /**
    * @return true if the configuration is valid
    * @throws CoreIdentityConfigurationException
    */
   boolean isValidConfiguration() throws CoreIdentityConfigurationException;
   
   /**
    * start the workflow service
    * 
    * @throws Exception
    */
   void startWorkflow() throws Exception;
   
   /**
    * stop the workflow service
    * 
    * @throws Exception
    */
   void stopWorkflow() throws Exception;
}

