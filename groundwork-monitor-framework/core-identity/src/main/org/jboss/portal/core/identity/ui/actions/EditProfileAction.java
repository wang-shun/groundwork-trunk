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

import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.portlet.PortletRequest;
import javax.portlet.WindowState;
import javax.portlet.WindowStateException;

import org.jboss.logging.Logger;
import org.jboss.portal.api.node.PortalNode;
import org.jboss.portal.api.node.PortalNodeURL;
import org.jboss.portal.common.text.FastURLDecoder;
import org.jboss.portal.core.aspects.server.UserInterceptor;
import org.jboss.portal.core.identity.services.IdentityConstants;
import org.jboss.portal.core.identity.services.workflow.ValidateEmailService;
import org.jboss.portal.core.identity.ui.IdentityUIUser;
import org.jboss.portal.core.identity.ui.common.IdentityUserBean;
import org.jboss.portal.identity.IdentityException;
import org.jboss.portal.identity.User;
import org.jboss.portal.portlet.aspects.portlet.ContextDispatcherInterceptor;
import org.jboss.portal.portlet.invocation.ActionInvocation;
import org.jboss.portal.server.ServerInvocationContext;
import org.jboss.portlet.JBossActionRequest;
import org.jboss.portlet.JBossActionResponse;
import org.jboss.portlet.JBossRenderRequest;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class EditProfileAction
{
   /** . */
   private String currentUser;

   /** . */
   private String password;

   /** . */
   private String email;
   
   /** . */
   private WindowState windowState;

   /** . */
   private IdentityUIUser uiUser;

   /** . */
   private IdentityUserBean identityUserBean;
   
   /** . */ 
   private ValidateEmailService validateEmailService;
   
   private final static FastURLDecoder decoder = FastURLDecoder.getUTF8Instance();

   /** . */
   private static final Logger log = Logger.getLogger(EditProfileAction.class);

   /** The EditProfileAction constructor */
   public EditProfileAction()
   {
      FacesContext ctx = FacesContext.getCurrentInstance();
      ExternalContext ectx = ctx.getExternalContext();
      this.currentUser = ectx.getRemoteUser();
      this.uiUser = new IdentityUIUser(this.currentUser);
   }

   public String getCurrentUser()
   {
      return currentUser;
   }

   public void setCurrentUser(String currentUser)
   {
      this.currentUser = currentUser;
   }

   public IdentityUIUser getUiUser()
   {
      if (uiUser == null)
      {
         this.uiUser = new IdentityUIUser(this.currentUser);
      }
      return uiUser;
   }

   public void setUiUser(IdentityUIUser uiUser)
   {
      this.uiUser = uiUser;
   }

   public IdentityUserBean getIdentityUserBean()
   {
      return identityUserBean;
   }

   public void setIdentityUserBean(IdentityUserBean identityUserBean)
   {
      this.identityUserBean = identityUserBean;
   }

   public String getPassword()
   {
      return password;
   }

   public void setPassword(String password)
   {
      this.password = password;
   }

   public String getEmail()
   {
      return email;
   }

   public void setEmail(String email)
   {
      this.email = email;
   }

   public ValidateEmailService getValidateEmailService()
   {
      return validateEmailService;
   }

   public void setValidateEmailService(ValidateEmailService validateEmailService)
   {
      this.validateEmailService = validateEmailService;
   }
   
   public String viewStart()
   {
      FacesContext ctx = FacesContext.getCurrentInstance();
      ExternalContext ectx = ctx.getExternalContext();
      JBossActionResponse response = (JBossActionResponse) ectx.getResponse();
      try
      {
         if (windowState == null)
            response.setWindowState(WindowState.NORMAL);
         else 
            response.setWindowState(windowState);
      }
      catch (WindowStateException e)
      {
         log.error("",e);
      }
      return "start";
   }
   
   public String userEditProfile()
   {
      FacesContext ctx = FacesContext.getCurrentInstance();
      ExternalContext ectx = ctx.getExternalContext();
      JBossActionRequest request = (JBossActionRequest) ectx.getRequest();
      this.windowState = request.getWindowState();
      JBossActionResponse response = (JBossActionResponse) ectx.getResponse();
      this.uiUser = new IdentityUIUser(ectx.getRemoteUser());
      try
      {
         response.setWindowState(WindowState.MAXIMIZED);
      }
      catch (WindowStateException e)
      {
         log.error("",e);
      }
      return "editProfile";
   }
   
   public String getCurrentWindowState()
   {
      FacesContext ctx = FacesContext.getCurrentInstance();
      ExternalContext ectx = ctx.getExternalContext();
      
      JBossRenderRequest request = (JBossRenderRequest) ectx.getRequest();
      return request.getWindowState().toString();
   }

   public String adminEditProfile()
   {
      FacesContext ctx = FacesContext.getCurrentInstance();
      ExternalContext ectx = ctx.getExternalContext();
      Map params = ectx.getRequestParameterMap();
      this.currentUser = params.get("currentUser") != null ? decoder.encode((String) params.get("currentUser")) : null;
      this.windowState = null;
      if (this.currentUser == null)
      {
         this.currentUser = ectx.getRemoteUser();
      }
      this.uiUser = new IdentityUIUser(this.currentUser);
      return "editProfile";
   }

   public String updateProfile()
   {
      FacesContext ctx = FacesContext.getCurrentInstance();
      try
      {        
         User user = identityUserBean.findUserByUserName(this.currentUser);
         // Adding dynamically set properties
         identityUserBean.updateProfile(user, uiUser.getAttribute().getProfileAttributes());
      }
      catch (RuntimeException e)
      {
         ResourceBundle bundle = ResourceBundle.getBundle("conf.bundles.Identity", ctx.getViewRoot().getLocale());
         FacesContext.getCurrentInstance().addMessage("status", new FacesMessage(bundle.getString("IDENTITY_EDIT_PROFILE_ERROR")));
         log.error("",e);
      }
      catch (IdentityException e)
      {
         ResourceBundle bundle = ResourceBundle.getBundle("conf.bundles.Identity", ctx.getViewRoot().getLocale());
         FacesContext.getCurrentInstance().addMessage("status", new FacesMessage(bundle.getString("IDENTITY_EDIT_PROFILE_ERROR")));
         log.error("",e);
      }
  
      // Removing user properties caching so that the changes are immediately available
      ActionInvocation rInvocation = (ActionInvocation)((PortletRequest)ctx.getExternalContext().getRequest()).getAttribute(ContextDispatcherInterceptor.REQ_ATT_COMPONENT_INVOCATION);
      rInvocation.setAttribute(UserInterceptor.PROFILE_KEY, null);
      rInvocation.setAttribute(UserInterceptor.USER_KEY, null);
      
      // JSR crap is also keeping a reference to a previous locale...
      // Let's fix that for this portlet only
      Object propertyValue = uiUser.getAttribute().getValue("locale").getObject();
      if (propertyValue  != null)
      {
         FacesContext.getCurrentInstance().getViewRoot().setLocale(new Locale(propertyValue.toString()));
      }
      
      this.resetWindowState(ctx);
      return "start";
   }
   
   public String changePassword()
   {
      FacesContext ctx = FacesContext.getCurrentInstance();
      ResourceBundle bundle = ResourceBundle.getBundle("conf.bundles.Identity", ctx.getViewRoot().getLocale());
      if (this.password != null)
      {
         try
         {
            // Update password
            this.identityUserBean.updatePassword(this.currentUser, this.password);
            ctx.addMessage("status", new FacesMessage(bundle.getString("IDENTITY_EDIT_CHANGE_PASSWOR_STATUS")));
         }
         catch (IdentityException e)
         {
            log.error("error while updating password", e);
            ctx.addMessage("status", new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("IDENTITY_EDIT_CHANGE_PASSWORD_ERROR"), ""));
         }
      }
      else
      {
         ctx.addMessage("status", new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("IDENTITY_EDIT_CHANGE_PASSWORD_ERROR"), ""));         
      }
      
      this.resetWindowState(ctx);
      return "status";
   }
   
   public String adminChangePassword()
   {
      FacesContext ctx = FacesContext.getCurrentInstance();
      ExternalContext ectx = ctx.getExternalContext();
      Map params = ectx.getRequestParameterMap();
      this.currentUser = params.get("currentUser") != null ? decoder.encode((String) params.get("currentUser")) : null;
      
     return "adminChangePassword";
   }

   public String changeEmail()
   {
      FacesContext ctx = FacesContext.getCurrentInstance();
      ResourceBundle bundle = ResourceBundle.getBundle("conf.bundles.Identity", ctx.getViewRoot().getLocale());
      if (this.email != null)
      {
         try
         {
            User user = this.identityUserBean.findUserByUserName(this.currentUser);
            
            // TODO status page
            Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
            String portalURL = this.getPortalURL();
            String validationStatus = this.validateEmailService.changeEmail(portalURL, user, email, locale);
            if (IdentityConstants.REGISTRATION_PENDING.equals(validationStatus))
            {
               ctx.addMessage("status", new FacesMessage(bundle.getString("IDENTITY_EDIT_CHANGE_EMAIL_STATUS_PENDING")));
            }
            else
            {
               ctx.addMessage("status", new FacesMessage(bundle.getString("IDENTITY_EDIT_CHANGE_EMAIL_STATUS_CHANGED")));
            }
         }
         catch (Exception e)
         {
            log.error("error while changing email", e);
            ctx.addMessage("status", new FacesMessage(bundle.getString("IDENTITY_EDIT_CHANGE_EMAIL_ERROR")));
         }
      }
      this.resetWindowState(ctx);
      return "status";
   }
   
   /**
    * Reset the window state (MAXIMIZED, NORMAL), which is for editing profile
    * 
    * @param ctx the faces context
    */
   private void resetWindowState(FacesContext ctx)
   {
      if ( windowState != null)
      {
         try
         {
           JBossActionResponse response = (JBossActionResponse) ctx.getExternalContext().getResponse();
           response.setWindowState(windowState);
         }
         catch(WindowStateException e)
         {
            log.error("", e);
         }
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
