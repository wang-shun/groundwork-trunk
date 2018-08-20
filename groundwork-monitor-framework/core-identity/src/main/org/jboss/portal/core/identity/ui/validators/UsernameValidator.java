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
package org.jboss.portal.core.identity.ui.validators;

import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.portlet.PortletContext;

import org.jboss.portal.core.identity.services.metadata.CoreIdentityConfigurationException;
import org.jboss.portal.core.identity.services.workflow.RegistrationService;
import org.jboss.portal.identity.IdentityException;
import org.jboss.portal.identity.NoSuchUserException;
import org.jboss.portal.identity.User;
import org.jboss.portal.identity.UserModule;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */

public class UsernameValidator implements Validator
{
   /** The username regex */
   // private static final String NICKNAME_VALIDATION = "^[a-zA-Z]([a-zA-Z0-9]+(([\\.\\-\\_]?[a-zA-Z0-9]+)+)?)";
   
   /** The user module */
   private UserModule userModule;

   /** The registration service */
   private RegistrationService registrationService;

   /** The logger */
   private static final org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(UsernameValidator.class);

   public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException
   {
      String username = (String) value;
      ResourceBundle bundle = ResourceBundle.getBundle("conf.bundles.Identity", context.getViewRoot().getLocale());
      PortletContext portletContext = (PortletContext) context.getExternalContext().getContext();
      userModule = (UserModule) portletContext.getAttribute("UserModule");
      registrationService = (RegistrationService) portletContext.getAttribute("RegistrationService");

      // if (username.length() >= 5 && (Pattern.matches(NICKNAME_VALIDATION, username)))
      try
      {
         // checking jBPM context
         if (registrationService.checkUsername(username))
         {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                  bundle.getString("IDENTITY_VALIDATION_ERROR_USERNAME_TAKEN"),
                  bundle.getString("IDENTITY_VALIDATION_ERROR_USERNAME_TAKEN")));
         }

         User u = userModule.findUserByUserName(username);
         // User found so this nickname is already taken
         throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
               bundle.getString("IDENTITY_VALIDATION_ERROR_USERNAME_TAKEN"),
               bundle.getString("IDENTITY_VALIDATION_ERROR_USERNAME_TAKEN")));
      }
      catch (NoSuchUserException e)
      {
         // No user found - proceed
      }
      catch (IllegalArgumentException e)
      {
         throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
               bundle.getString("IDENTITY_VALIDATION_ERROR_USERNAME_ERROR"),
               bundle.getString("IDENTITY_VALIDATION_ERROR_USERNAME_ERROR")));
      }
      catch (IdentityException e)
      {
         log.error("Error validation username", e);
         throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
               bundle.getString("IDENTITY_VALIDATION_ERROR_USERNAME_ERROR"),
               bundle.getString("IDENTITY_VALIDATION_ERROR_USERNAME_ERROR")));
      }
      catch (CoreIdentityConfigurationException e)
      {
         log.error("Error validation username", e);
         throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
               bundle.getString("IDENTITY_VALIDATION_ERROR_USERNAME_ERROR"),
               bundle.getString("IDENTITY_VALIDATION_ERROR_USERNAME_ERROR")));
      }
   }
}
