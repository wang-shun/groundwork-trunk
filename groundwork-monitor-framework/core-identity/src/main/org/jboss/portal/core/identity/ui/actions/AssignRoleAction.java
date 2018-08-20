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
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.jboss.portal.common.text.FastURLDecoder;
import org.jboss.portal.core.identity.ui.IdentityUIUser;
import org.jboss.portal.core.identity.ui.common.IdentityRoleBean;
import org.jboss.portal.core.identity.ui.common.IdentityUserBean;
import org.jboss.portal.identity.User;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class AssignRoleAction
{

   /** . */
   private String currentUser;

   /** .*/
   private List roles = new ArrayList();

   /** . */
   private IdentityUIUser uiUser;

   /** . */
   private IdentityUserBean identityUserBean;

   /** .*/
   private IdentityRoleBean identityRoleBean;
   
   /** . */
   private final static FastURLDecoder decoder = FastURLDecoder.getUTF8Instance();

   public IdentityUIUser getUiUser()
   {
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

   public IdentityRoleBean getIdentityRoleBean()
   {
      return identityRoleBean;
   }

   public void setIdentityRoleBean(IdentityRoleBean identityRoleBean)
   {
      this.identityRoleBean = identityRoleBean;
   }

   public List getRoles()
   {
      return roles;
   }

   public void setRoles(List roles)
   {
      this.roles = roles;
   }

   public String assignRoles()
   {
      FacesContext ctx = FacesContext.getCurrentInstance();
      ExternalContext ectx = ctx.getExternalContext();
      Map params = ectx.getRequestParameterMap();
      this.currentUser = params.get("currentUser") != null ? decoder.encode((String) params.get("currentUser")) : null;
      if (this.currentUser != null)
      {
         this.uiUser = new IdentityUIUser(this.currentUser);
         try
         {
            User user = identityUserBean.findUserByUserName(this.currentUser);
            this.roles = identityRoleBean.getUserRoles(user);
            return "assignRoles";
         }
         catch (Exception e)
         {
            e.printStackTrace();
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Problem while fetching user.", "Problem while fetching user."));
         }
         return "userAdmin";
      }
      else
      {
         return "userAdmin";
      }
   }

   public String updateRoles()
   {
      if (this.currentUser != null && this.roles != null)
      {
         try
         {
            User user = identityUserBean.findUserByUserName(this.currentUser);
            identityRoleBean.updateRoles(user, this.roles);
         }
         catch (Exception e)
         {
            e.printStackTrace();
            // FacesMessage
         }
      }
      return "userAdmin";
   }
}
