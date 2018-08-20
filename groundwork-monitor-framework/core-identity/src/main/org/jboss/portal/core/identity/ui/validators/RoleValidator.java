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

import org.jboss.portal.identity.IdentityException;
import org.jboss.portal.identity.Role;
import org.jboss.portal.identity.RoleModule;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */

public class RoleValidator implements Validator
{
   /** The role module */
   private RoleModule roleModule;

   /** The logger */
   private static final org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(RoleValidator.class);

   public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException
   {
      String rolename = (String) value;
      ResourceBundle bundle = ResourceBundle.getBundle("conf.bundles.Identity", context.getViewRoot().getLocale());
      PortletContext portletContext = (PortletContext) context.getExternalContext().getContext();
      roleModule = (RoleModule) portletContext.getAttribute("RoleModule");

      try
      {
         @SuppressWarnings("unused")
         Role u = roleModule.findRoleByName(rolename);
         throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
               bundle.getString("IDENTITY_VALIDATION_ERROR_ROLENAME_TAKEN"),
               bundle.getString("IDENTITY_VALIDATION_ERROR_ROLENAME_TAKEN")));
      }
      catch (IllegalArgumentException e)
      {
         throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
               bundle.getString("IDENTITY_VALIDATION_ERROR_ROLENAME_ERROR"),
               bundle.getString("IDENTITY_VALIDATION_ERROR_ROLENAME_ERROR")));
      }
      catch (IdentityException e)
      {
         // TODO: Throw a catchable error
         if (e.getMessage().contains("No such role"))
         {
            // ignore
         }
         else
         {
            log.error("Error validation rolename", e);
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
               bundle.getString("IDENTITY_VALIDATION_ERROR_ROLENAME_ERROR"),
               bundle.getString("IDENTITY_VALIDATION_ERROR_ROLENAME_ERROR")));
         }
      }
   }
}
