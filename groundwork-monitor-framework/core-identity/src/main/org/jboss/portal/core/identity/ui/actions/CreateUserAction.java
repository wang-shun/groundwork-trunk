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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.jboss.logging.Logger;
import org.jboss.portal.api.node.PortalNode;
import org.jboss.portal.api.node.PortalNodeURL;
import org.jboss.portal.core.identity.services.IdentityConstants;
import org.jboss.portal.core.identity.services.metadata.CoreIdentityConfigurationException;
import org.jboss.portal.core.identity.services.workflow.RegistrationService;
import org.jboss.portal.core.identity.ui.IdentityUIUser;
import org.jboss.portal.core.identity.ui.common.IdentityUserBean;
import org.jboss.portal.core.identity.ui.common.MetaDataServiceBean;
import org.jboss.portal.identity.User;
import org.jboss.portal.server.ServerInvocationContext;
import org.jboss.portlet.JBossActionRequest;
import org.jboss.portlet.JBossActionResponse;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class CreateUserAction
{

   /** . */
   private List roles = new ArrayList();
   
   /** . */
   private List defaultRoles = null;

   /** . */
   private IdentityUIUser uiUser = new IdentityUIUser();

   /** . */
   private RegistrationService registrationService;

   /** . */
   private IdentityUserBean identityUserBean;

   /** . */
   private MetaDataServiceBean metaDataService;

   /** . */
   private static final Logger log = Logger.getLogger(CreateUserAction.class);

   public IdentityUIUser getUiUser()
   {
      return uiUser;
   }

   public void setUiUser(IdentityUIUser uiUser)
   {
      this.uiUser = uiUser;
   }

   public List getRoles()
   {
      return roles;
   }

   public void setRoles(List roles)
   {
      this.roles = roles;
   }

   public void setDefaultRoles(List roles)
   {
      this.roles = roles;
      this.defaultRoles = roles;
   }

   public IdentityUserBean getIdentityUserBean()
   {
      return identityUserBean;
   }

   public void setIdentityUserBean(IdentityUserBean identityUserBean)
   {
      this.identityUserBean = identityUserBean;
   }

   public RegistrationService getRegistrationService()
   {
      return registrationService;
   }

   public void setRegistrationService(RegistrationService registrationService)
   {
      this.registrationService = registrationService;
   }

   public MetaDataServiceBean getMetaDataService()
   {
      return metaDataService;
   }

   public void setMetaDataService(MetaDataServiceBean metaDataService)
   {
      this.metaDataService = metaDataService;
   }
   
   public String cancelRegistration()
   {
      this.uiUser = new IdentityUIUser();
      this.roles = defaultRoles;
      return "start";
   }

   public void register(ActionEvent ev)
   {
      String registrationStatus = "failed";
      FacesContext ctx = FacesContext.getCurrentInstance();
      String adminSubscription = (String) ev.getComponent().getId();
      ResourceBundle bundle = ResourceBundle.getBundle("conf.bundles.Identity", ctx.getViewRoot().getLocale());
      if (uiUser.getUsername() != null && uiUser.getPassword() != null)
      {
         try
         {
            Class registrationDateClass = uiUser.getAttribute().getType("registrationdate");

            if (registrationDateClass.equals(Date.class))
            {
               uiUser.getAttribute().setValue("registrationdate", new Date());
            }
            else if (registrationDateClass.equals(String.class))
            {
               uiUser.getAttribute().setValue("registrationdate", (new Date()).toString());
            }
            else
            {
               log.warn(User.INFO_USER_REGISTRATION_DATE + " property is mapped in not supported type: "
                     + registrationDateClass.toString());
            }

            // Variables for RegisterService
            String wUsername = uiUser.getUsername();
            String wPassword = uiUser.getPassword();
            List wRoles = roles;
            Map wProfileMap = this.identityUserBean.getProfileMap(uiUser.getAttribute().getProfileAttributes());
            Locale wLocale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
            String wURL = this.getPortalURL();


            if (adminSubscription != null && adminSubscription.equals("admin"))
            {
               registrationStatus = this.registrationService.registerUser(wURL, wUsername, wPassword, wProfileMap, wRoles,
                     wLocale, true);
            }
            else
            {
               registrationStatus = this.registrationService.registerUser(wURL, wUsername, wPassword, wProfileMap, wRoles,
                     wLocale, false);
            }

         }
         catch(CoreIdentityConfigurationException e)
         {
            registrationStatus = IdentityConstants.REGISTRATION_FAILED;
            log.error("", e);
         }
      }

      // cleaning up the user
      this.uiUser = new IdentityUIUser();
      
      if (IdentityConstants.REGISTRATION_PENDING.equals(registrationStatus) && !adminSubscription.equals("admin"))
      {
         FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(bundle.getString("IDENTITY_REGISTER_PENDING_TITLE")));
      }
      else if (IdentityConstants.REGISTRATION_REGISTERED.equals(registrationStatus) && !adminSubscription.equals("admin"))
      {
         FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(bundle.getString("IDENTITY_REGISTER_SUCCESS_TITLE")));  
      }
      else if (adminSubscription.equals("admin") && ! IdentityConstants.REGISTRATION_FAILED.equals(registrationStatus))
      {
         FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(bundle.getString("IDENTITY_MANAGEMENT_CREATE_USER_CREATED")));
      }
      else
      {
         FacesContext.getCurrentInstance().addMessage("status", new FacesMessage(FacesMessage.SEVERITY_ERROR,
               bundle.getString("IDENTITY_VALIDATION_ERROR_REGISTRATION"),
               bundle.getString("IDENTITY_VALIDATION_ERROR_REGISTRATION")));
      }
   }

   private String getPortalURL()
   {
      ExternalContext ectx = FacesContext.getCurrentInstance().getExternalContext();
      JBossActionRequest request = (JBossActionRequest) ectx.getRequest();
      JBossActionResponse response = (JBossActionResponse) ectx.getResponse();
      ServerInvocationContext invocationContext = request.getControllerContext().getServerInvocation().getServerContext();
      PortalNode n = request.getPortalNode();
      PortalNodeURL url = response.createRenderURL(n);
      url.setRelative(false);
      String portalURL = url.toString();
      String a = invocationContext.getPortalContextPath();
      int contextPathIndex = portalURL.indexOf(a);
      String cleanPortalURL = portalURL.substring(0, contextPathIndex);
      cleanPortalURL = cleanPortalURL + a;
      return cleanPortalURL;
   }
}
