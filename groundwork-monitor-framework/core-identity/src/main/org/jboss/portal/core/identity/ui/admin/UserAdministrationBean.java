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
package org.jboss.portal.core.identity.ui.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import org.jboss.logging.Logger;
import org.jboss.portal.common.text.FastURLDecoder;
import org.jboss.portal.core.identity.services.IdentityConstants;
import org.jboss.portal.core.identity.services.metadata.CoreIdentityConfigurationException;
import org.jboss.portal.core.identity.services.workflow.RegistrationService;
import org.jboss.portal.core.identity.services.workflow.UserContainer;
import org.jboss.portal.core.identity.ui.IdentityUIUser;
import org.jboss.portal.core.identity.ui.common.IdentityRoleBean;
import org.jboss.portal.core.identity.ui.common.IdentityUserBean;
import org.jboss.portal.identity.User;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class UserAdministrationBean
{

   /** The current user. */
   private String currentUser;

   /** A list of users. */
   private ListDataModel userList;

   /** The page. */
   private int page = 1;

   /** The result limit. */
   private String limit = "10";

   /** The user count. */
   private int userCount = 0;

   /** The roles */
   private List<String> roles = new ArrayList<String>();

   /** The node which should be displayed. */
   private String displayNode = IdentityConstants.JBPM_NODE_APPROVAL;

   /** The subscription mode. */
   private String subscriptionMode;

   /** The admin subscription mode. */
   private String adminSubscriptionMode;

   /** The pending users. */
   private List<UserContainer> pendingUsers = null;

   /** The pending list selected for approval or reject. */
   private List<UserContainer> pendingActionList = null;

   /** The pending user action. */
   private String pendingUserAction = null;

   /** The uiUser. */
   private IdentityUIUser uiUser;

   /** The search string */
   private String searchString = "";

   /** The identity user bean. */
   private IdentityUserBean identityUserBean;

   /** The idenetity role bean. */
   private IdentityRoleBean identityRoleBean;

   /** The registration service. */
   private RegistrationService registrationService;

   /** The logger. */
   private static final Logger log = Logger.getLogger(UserAdministrationBean.class);

   /** The decoder. */
   private static final FastURLDecoder decoder = FastURLDecoder.getUTF8Instance();

   private boolean isNewSearch=true;

   public UserAdministrationBean()
   {
      // this.userList = new ListDataModel();
   }

   public DataModel getUserList()
   {
      if ( this.userList == null)
      {
         this.searchUsers();
      }
      return userList;
   }

   public String getSearchString()
   {
      return searchString;
   }

   public void setSearchString(String searchString)
   {
      this.searchString = searchString;
   }

   public int getPage()
   {
      return page;
   }

   public void setPage(int page)
   {
      this.page = page;
   }

   public String getLimit()
   {
      return limit;
   }

   public void setLimit(String limit)
   {
      this.limit = limit;
   }

   public List<String> getRoles()
   {
      return roles;
   }

   public void setRoles(List<String> roles)
   {
      this.roles = roles;
   }

   public IdentityUIUser getUiUser()
   {
      return uiUser;
   }

   public void setUiUser(IdentityUIUser uiUser)
   {
      this.uiUser = uiUser;
   }

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

   public IdentityUserBean getIdentityUserBean()
   {
      return identityUserBean;
   }

   public String getPendingUserAction()
   {
      return pendingUserAction;
   }

   public List<UserContainer> getPendingActionList()
   {
      return pendingActionList;
   }

   public void setIdentityUserBean(IdentityUserBean identityUserBean)
   {
      this.identityUserBean = identityUserBean;
   }

   public IdentityRoleBean getIdentityRoleBean()
   {
      return identityRoleBean;
   }

   public void setIdentityRoleBean(IdentityRoleBean identityRoleBean)
   {
      this.identityRoleBean = identityRoleBean;
   }

   public RegistrationService getRegistrationService()
   {
      return registrationService;
   }

   public void setRegistrationService(RegistrationService registrationService)
   {
      this.registrationService = registrationService;
   }

   public String searchUsers()
   {
      if (this.searchString != null)
      {
         try
         {

            int initLimit = Integer.valueOf(limit).intValue();
            int offset = 0;
            if(!isNewSearch) 
            {
               offset = page > 0 ? ((page - 1) * initLimit) : 0;
               isNewSearch = true;
            }  

            else
            {
               page = 1;
            }


            int limit1 = initLimit + 1;
            this.userList = new ListDataModel(identityUserBean.findUsersFilteredByUserName(searchString, offset, limit1));
         }
         catch (Exception e)
         {
            log.error("",e);
            ResourceBundle bundle = ResourceBundle.getBundle("conf.bundles.Identity", FacesContext.getCurrentInstance().getViewRoot().getLocale());
            FacesContext.getCurrentInstance().addMessage("status", new FacesMessage(bundle.getString("IDENTITY_MANAGEMENT_ERROR_ACTION_USER")));
         }
      }
      return "searchUsers";
   }

   public String enableUser()
   {
      FacesContext ctx = FacesContext.getCurrentInstance();
      ExternalContext ectx = ctx.getExternalContext();
      Map params = ectx.getRequestParameterMap();
      String action = (String) params.get("enableAction");
      ResourceBundle bundle = ResourceBundle.getBundle("conf.bundles.Identity", ctx.getViewRoot().getLocale());
      this.currentUser = params.get("currentUser") != null ? decoder.encode((String) params.get("currentUser")) : null;

      if (this.currentUser != null && action != null)
      {
         Map<String, Object> profileMap = new HashMap<String, Object>();
         User user;

         try
         {
            user = identityUserBean.findUserByUserName(this.currentUser);

            if (action.equals("enable"))
            {
               profileMap.put("enabled", Boolean.TRUE);
            }
            else if (action.equals("disable"))
            {
               profileMap.put("enabled", Boolean.FALSE);
            }
            // Enabling user
            identityUserBean.updateProfile(user, profileMap);
         }
         catch (Exception e)
         {
            log.error("unable to get user: " + this.currentUser, e);
            ctx.addMessage("status", new FacesMessage(bundle.getString("IDENTITY_MANAGEMENT_ERROR_ACTION_USER")));
            return "userAdmin";
         }
         // Updating search
         if (action.equals("enable"))
         {
            ctx.addMessage("status", new FacesMessage(bundle.getString("IDENTITY_MANAGEMENT_ENABLE_USER_ENABLED")));
         }
         else if (action.equals("disable"))
         {
            ctx.addMessage("status", new FacesMessage(bundle.getString("IDENTITY_MANAGEMENT_DISABLE_USER_DISABLED")));
         }		 
         // Update userList
         return this.searchUsers();
      }
      return "searchUsers";
   }

   public int getPendingCount()
   {
      try
      {
         return this.registrationService.getPendingCount();
      }
      catch (CoreIdentityConfigurationException e)
      {
         log.error("", e);
         return -1;
      }
   }

   public int getUserCount()
   {
      if (userCount == 0)
      {
         try
         {
            userCount = this.identityUserBean.getUserModule().getUserCount();
         }
         catch (Exception e)
         {
            log.error("", e);
            ResourceBundle bundle = ResourceBundle.getBundle("conf.bundles.Identity", FacesContext.getCurrentInstance().getViewRoot().getLocale());
            FacesContext.getCurrentInstance().addMessage("status", new FacesMessage(bundle.getString("IDENTITY_MANAGEMENT_ERROR_ACTION_USER")));
         }
      }
      return userCount;
   }

   public List<UserContainer> getPendingUsers()
   {
      try
      {
         if ( this.registrationService != null && this.pendingUsers == null)
            this.pendingUsers = this.registrationService.getPendingUsers(displayNode);
      }
      catch (CoreIdentityConfigurationException e)
      {
         log.error("", e);
      }
      return pendingUsers;
   }

   public String deleteUser()
   {
      FacesContext ctx = FacesContext.getCurrentInstance();
      ExternalContext ectx = ctx.getExternalContext();
      Map params = ectx.getRequestParameterMap();
      this.currentUser = params.get("currentUser") != null ? decoder.encode((String) params.get("currentUser")) : null;

      if (this.currentUser != null)
      {
         this.uiUser = new IdentityUIUser(this.currentUser);
         return "deleteUser";
      }
      return "userAdmin";
   }

   public String confirmedDelete()
   {
      ResourceBundle bundle = ResourceBundle.getBundle("conf.bundles.Identity", FacesContext.getCurrentInstance().getViewRoot().getLocale());
      try
      {
         User user = identityUserBean.findUserByUserName(this.uiUser.getUsername());
         identityUserBean.getUserModule().removeUser(user.getId());
         FacesContext.getCurrentInstance().addMessage("status", new FacesMessage(bundle.getString("IDENTITY_MANAGEMENT_USER_DELETED")));
         // Update the userList
         this.searchUsers();
         return "userAdmin";
      }
      catch (Exception e)
      {
         log.error("unable delete user.", e);
         FacesContext.getCurrentInstance().addMessage("status", new FacesMessage(bundle.getString("IDENTITY_MANAGEMENT_ERROR_ACTION_USER")));
      }
      return "userAdmin";
   }

   public String nextPage()
   {
      isNewSearch = false; 
      this.page++;
      this.searchUsers();
      return "searchUsers";
   }

   public String prevPage()
   {
      isNewSearch = false; 
      this.page--;
      this.searchUsers();
      return "searchUsers";
   }

   public String approveRegistration()
   {
      ExternalContext ectx = FacesContext.getCurrentInstance().getExternalContext();
      Map params = ectx.getRequestParameterMap();
      String processId = (String) params.get("processId");
      String action = (String) params.get("action");
      try
      {
         if (processId != null && action != null)
         {
            if (action.equals("approve"))
            {
               this.registrationService.approve(processId, true);   
            }
            else if (action.equals("reject"))
            {
               this.registrationService.approve(processId, false);
            }
         }
      }
      catch (CoreIdentityConfigurationException e)
      {
         log.error("", e);
         return "userAdmin";
      }
      this.pendingUsers = null;
      this.getPendingUsers();
      if (this.pendingUsers.size() > 0)
         return "pendingUsers";
      else
         return "userAdmin";
   }

   public String approveList()
   {
      this.pendingUserAction = "approve";
      this.pendingActionList = new ArrayList<UserContainer>();
      for(UserContainer user : pendingUsers)
      {
         if ( user.isSelected() )
         {
            this.pendingActionList.add(user);
         }
      }
      if (this.pendingActionList.size() >0)
      {
         return "confirmPendingAction";
      }
      return "pendingUsers";
   }

   public String rejectList()
   {
      this.pendingUserAction = "reject";
      this.pendingActionList = new ArrayList<UserContainer>();
      for(UserContainer user : pendingUsers)
      {
         if ( user.isSelected() )
         {
            this.pendingActionList.add(user);
         }
      }
      if (this.pendingActionList.size() > 0)
      {
         return "confirmPendingAction";
      }
      return "pendingUsers";
   }

   public String confirmPendingAction()
   {
      try
      {
         for(UserContainer user : pendingActionList)
         {
            if (this.pendingUserAction.equals("approve"))
            {
               this.registrationService.approve(user.getProcessId(), true);   
            }
            else if (this.pendingUserAction.equals("reject"))
            {
               this.registrationService.approve(user.getProcessId(), false);
            }
         }
      }
      catch(CoreIdentityConfigurationException e)
      {
         log.error("", e);
      }
      this.pendingActionList = null;
      this.pendingUsers = null;
      return "success";
   }
}
