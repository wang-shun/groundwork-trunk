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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.ListDataModel;

import org.jboss.logging.Logger;
import org.jboss.portal.common.text.FastURLDecoder;
import org.jboss.portal.core.identity.ui.IdentityUIUser;
import org.jboss.portal.core.identity.ui.UIRole;
import org.jboss.portal.core.identity.ui.common.IdentityRoleBean;
import org.jboss.portal.identity.IdentityException;
import org.jboss.portal.identity.Role;
import org.jboss.portal.identity.User;
import com.groundworkopensource.portal.identity.extendedui.ExtendedRoleModuleImpl;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class RoleManagementBean
{
   /** The current role. */
   private String currentRole;

   /** The uiRole */
   private UIRole uiRole;

   /** The initial page. */
   private int page = 1;

   /** The initial result limit. */
   private String limit = "10";

   /** The filtered user name. */
   private String userNameFilter = new String();

   /** The related role list. */
   private ListDataModel roleList;

   /** The identity role bean. */
   private IdentityRoleBean identityRoleBean;
   
   /** The logger. */
   private static final Logger log = Logger.getLogger(RoleManagementBean.class);
   
   /** The decoder. */
   private static final FastURLDecoder decoder = FastURLDecoder.getUTF8Instance();

   public IdentityRoleBean getIdentityRoleBean()
   {
      return identityRoleBean;
   }

   public void setIdentityRoleBean(IdentityRoleBean identityRoleBean)
   {
      this.identityRoleBean = identityRoleBean;
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

   public String getUserNameFilter()
   {
      return userNameFilter;
   }

   public void setUserNameFilter(String userNameFilter)
   {
      this.userNameFilter = userNameFilter;
   }

   public UIRole getUiRole()
   {
      return uiRole;
   }

   public void setUiRole(UIRole uiRole)
   {
      this.uiRole = uiRole;
   }

   public ListDataModel getRoleList()
   {
      List<UIRole> list = new ArrayList<UIRole>();
      Set<Role> set = new HashSet<Role>();
      try
      {
         set = identityRoleBean.getRoleModule().findRoles();
      }
      catch (IdentityException e)
      {
         log.error("Error while performing roleModule.findRoles.", e);
      }
      
      for(Role role : set)
      {
         UIRole uiRole = new UIRole(role);
         list.add(uiRole);
      }
      this.roleList = new ListDataModel(list);
      return this.roleList;
   }

   public String viewRoleMembers()
   {
      FacesContext ctx = FacesContext.getCurrentInstance();
      ExternalContext ectx = ctx.getExternalContext();
      Map params = ectx.getRequestParameterMap();
      String role = params.get("currentRole") != null ? decoder.encode((String) params.get("currentRole")) : null;
      this.currentRole = role != null ? role : this.currentRole;
      try
      {
         this.uiRole = identityRoleBean.getUIRole(this.currentRole);   
      }
      catch ( Exception e)
      {
         log.error("", e);
         ResourceBundle bundle = ResourceBundle.getBundle("conf.bundles.Identity", ctx.getViewRoot().getLocale());
         ctx.addMessage("status", new FacesMessage(bundle.getString("IDENTITY_MANAGEMENT_ERROR_ACTION_ROLE")));
         return "roleAdmin";
      }
      return "viewMembers";
   }

   public ListDataModel getRoleMembers()
   {
      Set<User> members = new HashSet<User>();
      List<IdentityUIUser> roleMembers = new ArrayList<IdentityUIUser>();
      FacesContext ctx = FacesContext.getCurrentInstance();
      
      if(this.currentRole == null || this.currentRole.length() < 1)
         return new ListDataModel(roleMembers);
      
      try
      {
         int intLimit = Integer.valueOf(limit).intValue();
         int offset = page > 0 ? ((page - 1) * intLimit) : 0;
         int limit1 = intLimit + 1;
         members = identityRoleBean.getMembershipModule().findRoleMembers(this.currentRole, offset, limit1, userNameFilter);
      }
      catch (Exception e)
      {
         log.error("", e);
         ResourceBundle bundle = ResourceBundle.getBundle("conf.bundles.Identity", ctx.getViewRoot().getLocale());
         ctx.addMessage("status", new FacesMessage(bundle.getString("IDENTITY_MANAGEMENT_ERROR_ACTION_ROLE")));
      }

      if(members != null)
      {
         for(User user : members)
         {
            IdentityUIUser uiUser = new IdentityUIUser(user.getUserName()); 
            roleMembers.add(uiUser);
         }
      }
      return new ListDataModel(roleMembers);
   }

   public String deleteRole()
   {
      FacesContext ctx = FacesContext.getCurrentInstance();
      ExternalContext ectx = ctx.getExternalContext();
      Map params = ectx.getRequestParameterMap();
      this.currentRole = params.get("currentRole") != null ? decoder.encode((String) params.get("currentRole")) : null;
      if (this.currentRole != null)
      {
         try
         {
            this.uiRole = identityRoleBean.getUIRole(this.currentRole);
            return "deleteRole";
         }
         catch (Exception e)
         {
            log.error("", e);
            ResourceBundle bundle = ResourceBundle.getBundle("conf.bundles.Identity", ctx.getViewRoot().getLocale());
            ctx.addMessage("status", new FacesMessage(bundle.getString("IDENTITY_MANAGEMENT_ERROR_ACTION_ROLE")));
            return "roleAdmin";
         }
         
      }
      return "roleAdmin";
   }

   public String confirmedDelete()
   {
      Role uRole;
      try
      {
         uRole = identityRoleBean.getRoleModule().findRoleByName(this.uiRole.getName());
         this.identityRoleBean.getRoleModule().removeRole(uRole.getId());
         this.roleList = null;
         ExtendedRoleModuleImpl extRoleImpl = new ExtendedRoleModuleImpl();
         extRoleImpl.removeRole((Long)uRole.getId());
      }
      catch (Exception e)
      {
         log.error("", e);
         ResourceBundle bundle = ResourceBundle.getBundle("conf.bundles.Identity", FacesContext.getCurrentInstance().getViewRoot().getLocale());
         FacesContext.getCurrentInstance().addMessage("status", new FacesMessage(bundle.getString("IDENTITY_MANAGEMENT_ERROR_ACTION_ROLE")));
         return "roleAdmin";
      }
      return "roleAdmin";
   }
   
   public String nextPage()
   {
      this.page++;
      return "viewMembers";
   }
   
   public String prevPage()
   {
      this.page--;
      return "viewMembers";
   }
}