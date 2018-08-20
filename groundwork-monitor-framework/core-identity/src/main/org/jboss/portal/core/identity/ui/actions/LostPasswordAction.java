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
package org.jboss.portal.core.identity.ui.actions;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.jboss.logging.Logger;
import org.jboss.portal.common.text.FastURLDecoder;
import org.jboss.portal.core.identity.services.IdentityConstants;
import org.jboss.portal.core.identity.services.IdentityMailService;
import org.jboss.portal.core.identity.ui.IdentityUIUser;
import org.jboss.portal.core.identity.ui.common.IdentityUserBean;
import org.jboss.portal.identity.NoSuchUserException;
import org.jboss.portal.identity.User;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class LostPasswordAction
{
   /** The user name. */
   private String username;

   /** The users email address. */
   private String email;

   /** Characters used for generating the password. */
   private String passwordCharacters;

   /** The identity user bean. */
   private IdentityUserBean identityUserBean;

   /** The identity mail service. */
   private IdentityMailService identityMailService;

   /** The logger. */
   private static final Logger log = Logger.getLogger(LostPasswordAction.class);
   
   /** The decoder. */
   private static final FastURLDecoder decoder = FastURLDecoder.getUTF8Instance();

   public String getUsername()
   {
      return username;
   }

   public void setUsername(String username)
   {
      this.username = username;
   }

   public String getEmail()
   {
      return email;
   }

   public void setEmail(String email)
   {
      this.email = email;
   }

   public IdentityUserBean getIdentityUserBean()
   {
      return identityUserBean;
   }

   public void setIdentityUserBean(IdentityUserBean identityUserBean)
   {
      this.identityUserBean = identityUserBean;
   }

   public IdentityMailService getIdentityMailService()
   {
      return identityMailService;
   }

   public void setIdentityMailService(IdentityMailService identityMailService)
   {
      this.identityMailService = identityMailService;
   }

   public String getPasswordCharacters()
   {
      return passwordCharacters;
   }

   public void setPasswordCharacters(String passwordCharacters)
   {
      this.passwordCharacters = passwordCharacters;
   }

   public String doomed()
   {
      User user = null;
      ResourceBundle bundle = ResourceBundle.getBundle("conf.bundles.Identity", FacesContext.getCurrentInstance().getViewRoot()
            .getLocale());

      if (username != null && username.trim().length() > 0)
      {
         try
         {
            user = identityUserBean.findUserByUserName(username);
         }
         catch (NoSuchUserException e)
         {
            FacesContext.getCurrentInstance().addMessage(null,
                  new FacesMessage(bundle.getString("IDENTITY_LOST_PASSWORD_STATUS_404")));
            return "status";
         }
         catch (Exception e)
         {
            log.error("", e);
            FacesContext.getCurrentInstance().addMessage(null,
                  new FacesMessage(bundle.getString("IDENTITY_LOST_PASSWORD_ERROR")));
            return "status";
         }
      }
      else
      {
         FacesContext.getCurrentInstance().addMessage(null,
               new FacesMessage(bundle.getString("IDENTITY_LOST_PASSWORD_STATUS_404")));
         return "status";
      }

      if (user != null)
      {
         try
         {
            String newPassword = this.genPassword(8);

            IdentityUIUser uiUser = new IdentityUIUser(user.getUserName());
            
            Map<String, String> mailMap = new HashMap<String, String>();
            mailMap.put(IdentityConstants.EMAIL_TO, (String) uiUser.getAttribute().getValue("email").getObject());
            mailMap.put("username", user.getUserName());
            mailMap.put("password", newPassword);

            // Update password
            identityUserBean.updatePassword(user.getUserName(), newPassword);
            // Sending email
            Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
            identityMailService.sendMail("lostPassword", mailMap, locale);
         }
         catch (Exception e)
         {
            log.error("", e);
            FacesContext.getCurrentInstance().addMessage(null,
                  new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        bundle.getString("IDENTITY_LOST_PASSWORD_ERROR"),
                        bundle.getString("IDENTITY_LOST_PASSWORD_ERROR")));
            return "status";
         }
      }
      FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(bundle.getString("IDENTITY_LOST_PASSWORD_STATUS_SUCCESSFUL")));
      return "status";
   }
   
   public String adminResetPassword()
   {
      FacesContext ctx = FacesContext.getCurrentInstance();
      ExternalContext ectx = ctx.getExternalContext();
      Map params = ectx.getRequestParameterMap();
      this.username =  params.get("currentUser") != null ? decoder.encode((String) params.get("currentUser")) : null;
      return "resetPassword";
   }

   private String genPassword(int length) throws NoSuchAlgorithmException
   {
      StringBuffer buffer = new StringBuffer();
      char[] characterMap = passwordCharacters.toCharArray();
      SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");

      for (int i = 0; i <= length; i++)
      {
         byte[] bytes = new byte[512];
         secureRandom.nextBytes(bytes);
         double number = secureRandom.nextDouble();
         int b = ((int) (number * characterMap.length));
         buffer.append(characterMap[b]);
      }

      return buffer.toString();
   }
}
