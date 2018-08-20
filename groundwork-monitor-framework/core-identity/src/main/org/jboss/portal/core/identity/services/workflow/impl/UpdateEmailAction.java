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

import javax.naming.InitialContext;

import org.jboss.logging.Logger;
import org.jboss.portal.core.identity.services.IdentityConstants;
import org.jboss.portal.core.identity.services.IdentityUserManagementService;
import org.jboss.portal.core.identity.services.workflow.UserContainer;
import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.exe.ExecutionContext;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
@SuppressWarnings("serial")
public class UpdateEmailAction implements ActionHandler
{

   /** The identity management service. */
   private IdentityUserManagementService identityManagementService;
   
   /** The logger. */
   private static Logger log = Logger.getLogger(CreateUserAction.class);

   public void execute(ExecutionContext ectx) throws Exception
   {
      UserContainer uc = (UserContainer) ectx.getContextInstance().getVariable(IdentityConstants.VARIABLE_USER);
      String email = (String) ectx.getContextInstance().getVariable(IdentityConstants.VARIABLE_EMAIL);
      
      this.getIdentityManagementService().updateEmail(uc.getUsername(), email);
   }
   
   private IdentityUserManagementService getIdentityManagementService()
   {
      if (identityManagementService == null)
      {
         try
         {
            identityManagementService = (IdentityUserManagementService) new InitialContext().lookup("java:/portal/IdentityUserManagementService");
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
      }
      return identityManagementService;
   }
}

