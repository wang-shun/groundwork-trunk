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

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Locale;

import javax.naming.InitialContext;

import org.jboss.logging.Logger;
import org.jboss.portal.common.util.Tools;
import org.jboss.portal.core.identity.services.IdentityConstants;
import org.jboss.portal.core.identity.services.IdentityMailService;
import org.jboss.portal.core.identity.services.workflow.UserContainer;
import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.exe.ExecutionContext;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
@SuppressWarnings("serial")
public class SendValidationMailAction implements ActionHandler
{

   /** The identity mail service. */
   private IdentityMailService identityMailService;

   /** The logger. */
   private static Logger log = Logger.getLogger(SendValidationMailAction.class);

   public void execute(ExecutionContext ectx) throws Exception
   {
      HashMap emailMap = new HashMap();
      String hash = generateRandomHash();

      String action = (String) ectx.getContextInstance().getVariable(IdentityConstants.ACTION);
      String email = (String) ectx.getContextInstance().getVariable(IdentityConstants.VARIABLE_EMAIL);
      Locale locale = (Locale) ectx.getContextInstance().getVariable(IdentityConstants.VARIABLE_LOCALE);

      if (locale == null)
      {
         locale = new Locale(IdentityConstants.DEFAULT_LOCALE);
      }

      UserContainer uc = (UserContainer) ectx.getContextInstance().getVariable(IdentityConstants.VARIABLE_USER);

      // Register new user
      if (IdentityConstants.ACTION_REGISTER_USER.equals(action))
      {
         emailMap.put(IdentityConstants.EMAIL_TO, email);
         emailMap.put("username", uc.getUsername());
         emailMap.put("password", uc.getPassword());
      }
      // Change email request (send an email to the NEW email address)
      else if (IdentityConstants.ACTION_CHANGE_EMAIL.equals(action))
      {
         emailMap.put(IdentityConstants.EMAIL_TO, email);
      }
      else
      {
         throw new RuntimeException("no actuin defined for SendValidationMail: " + action);
      }

      ectx.getContextInstance().setVariable(IdentityConstants.VALIDATION_HASH, hash);
      
      // Generating the validation URL
      String portalURL = (String) ectx.getContextInstance().getVariable(IdentityConstants.PORTAL_URL);
      String activationLink = portalURL + "/" + IdentityConstants.VALIDATE_EMAIL + "/"
            + ectx.getProcessInstance().getId() + "/" + hash;
      emailMap.put("activationLink", activationLink);

      this.getIdentityMailService().sendMail(action, emailMap, locale);
   }

   // generating a random hash
   private static String generateRandomHash()
   {
      try
      {
         SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");

         byte[] bytes = new byte[512];
         secureRandom.nextBytes(bytes);

         double rand = secureRandom.nextDouble();
         long time = System.currentTimeMillis();
         String salt = IdentityConstants.HASH_SALT;

         StringBuffer buffer = new StringBuffer();
         buffer.append(rand);
         buffer.append(salt);
         buffer.append(time);

         return Tools.md5AsHexString(buffer.toString());
      }
      catch (NoSuchAlgorithmException e)
      {
         log.error("No Such Algorithm exists " + e);
      }
      return null;
   }

   private IdentityMailService getIdentityMailService()
   {
      if (identityMailService == null)
      {
         try
         {
            this.identityMailService = (IdentityMailService) new InitialContext()
                  .lookup("java:/portal/IdentityMailService");
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
      }
      return identityMailService;
   }

}
