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
package org.jboss.portal.core.identity.ui.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.model.SelectItem;

import org.jboss.portal.identity.IdentityException;
import org.jboss.portal.identity.MembershipModule;
import org.jboss.portal.identity.NoSuchUserException;
import org.jboss.portal.identity.Role;
import org.jboss.portal.identity.RoleModule;
import org.jboss.portal.identity.User;
import org.jboss.portal.core.identity.ui.UIRole;

import com.groundworkopensource.portal.identity.extendedui.CommonUtils;
import com.groundworkopensource.portal.identity.extendedui.ExtendedRoleModuleImpl;
import com.groundworkopensource.portal.identity.extendedui.HibernateExtendedRole;
import com.groundworkopensource.portal.identity.extendedui.ExtendedUIRole;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class IdentityRoleBean
{
   /** The identity role module */
   private RoleModule roleModule;

   /** The identity memebershipModule */
   private MembershipModule membershipModule;

   public RoleModule getRoleModule()
   {
      return roleModule;
   }

   public void setRoleModule(RoleModule roleModule)
   {
      this.roleModule = roleModule;
   }

   public MembershipModule getMembershipModule()
   {
      return membershipModule;
   }

   public void setMembershipModule(MembershipModule membershipModule)
   {
      this.membershipModule = membershipModule;
   }

   public void assignRoles(User user, List<String> roles) throws IllegalArgumentException, IdentityException
   {
      Set<Role> roleSet = this.checkRoles(roles);
      this.membershipModule.assignRoles(user, roleSet);
   }
   
   private Set<Role> checkRoles(List<String> roles) throws IllegalArgumentException, IdentityException
   {
      Set<Role> roleSet = new HashSet<Role>();
      if (roles != null && roles.size() > 0)
      { // Checking existing roles

         for(String roleName : roles)
         {
            Role role = roleModule.findRoleByName(roleName);

            if (role == null)
            {
               // Create new role ?
            }
            else
            {
               roleSet.add(role);
            }
         }
      }
      return roleSet;
   }

   public void updateRoleDisplayName(String roleName, String roleDisplayName) throws IllegalArgumentException,
         IdentityException
   {
      Role cRole = roleModule.findRoleByName(roleName);
      cRole.setDisplayName(roleDisplayName);
   }

   public void updateRoles(User user, List<String> roles) throws IllegalArgumentException, NoSuchUserException,
         IdentityException
   {
      Set<Role> roleSet = this.checkRoles(roles);
      this.membershipModule.assignRoles(user, roleSet);
   }

   public UIRole getUIRole(String roleName) throws IllegalArgumentException, IdentityException
   {
	   UIRole uiRole = new UIRole();
	   Role role = this.roleModule.findRoleByName(roleName);
      uiRole.setDisplayName(role.getDisplayName());
      uiRole.setName(role.getName());
      return uiRole;
   }
   
   public ExtendedUIRole getExtendedUIRole(String roleName) throws IllegalArgumentException, IdentityException
   {
	  ExtendedUIRole uiRole = new ExtendedUIRole();
	  ExtendedRoleModuleImpl extRoleImpl = new ExtendedRoleModuleImpl();
	  HibernateExtendedRole role = extRoleImpl.findRoleByName(roleName);
	  if (role != null)  {
		  uiRole.setId(role.getId());
	      uiRole.setDashboardLinksDisabled(role.isDashboardLinksDisabled().booleanValue());
	      uiRole.setActionsEnabled(role.isActionsEnabled().booleanValue());
	      String restrictionType = role.getRestrictionType();
	      uiRole.setRestrictionType(restrictionType);
	      uiRole.setDefaultHostGroup(role.getDefaultHostGroup());
	      uiRole.setDefaultServiceGroup(role.getDefaultServiceGroup());
	      if (restrictionType!= null &&  restrictionType.equalsIgnoreCase(ExtendedUIRole.RESTRICTION_TYPE_PARTIAL))  {
	    	  uiRole.setHgList(CommonUtils.convert2HGList(role.getHgList()));
		      uiRole.setSgList(CommonUtils.convert2SGList(role.getSgList()));     
	      }
	      else {
	    	  uiRole.setHgList(null);
		      uiRole.setSgList(null);     
	      }	
	  } // end if
      return uiRole;
   }
   
   public List<SelectItem> getRoleSelectItems()
   {
      List<SelectItem> list = new ArrayList<SelectItem>();
      Set<Role> set = new HashSet<Role>();
      try
      {
         set = roleModule.findRoles();
      }
      catch (IdentityException e)
      {
         // FIXME
         e.printStackTrace();
      }

      for(Role role : set)
      {
         list.add(new SelectItem(role.getName(), role.getDisplayName()));
      }
      return list;
   }

   public List<String> getUserRoles(User user) throws IllegalArgumentException, NoSuchUserException, IdentityException
   {
      List<String> list = new ArrayList<String>();
      Set<Role> roleSet = this.membershipModule.getRoles(user);
      
      for(Role role : roleSet)
      {
         list.add(role.getName());
      }
      return list;
   }

}
