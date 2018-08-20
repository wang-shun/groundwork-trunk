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

import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.jboss.portal.common.text.FastURLDecoder;
import org.jboss.portal.core.identity.ui.UIRole;
import org.jboss.portal.core.identity.ui.common.IdentityRoleBean;

import com.groundworkopensource.portal.identity.extendedui.ExtendedRoleModuleImpl;
import com.groundworkopensource.portal.identity.extendedui.ExtendedUIRole;
import com.groundworkopensource.portal.identity.extendedui.CommonUtils;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.ws.IWSFacade;
import com.groundworkopensource.portal.common.ws.WebServiceFactory;
import com.groundworkopensource.portal.common.ws.WebServiceFactory.WebServiceType;
import org.jboss.logging.Logger;
import org.groundwork.foundation.ws.model.impl.Category;
import org.groundwork.foundation.ws.model.impl.HostGroup;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public class EditRoleAction
{

   /** .*/
   private String currentRole;

   /** .*/
   private UIRole uiRole;

   /** .*/
   private IdentityRoleBean identityRoleBean;
   
   private ExtendedUIRole extUIRole =null;
   
   private SelectItem[] hgSelectItems = null;
	
	private SelectItem[] sgSelectItems = null;
	
	private SelectItem[] hgSingleSelectItems = null;
	
	private SelectItem[] sgSingleSelectItems = null;
   
   /** . */
   private static final FastURLDecoder decoder = FastURLDecoder.getUTF8Instance();
   
	
	/** The logger. */
	private static final Logger logger = Logger
			.getLogger(EditRoleAction.class);
   
   public EditRoleAction() {

		IWSFacade foundationWSFacade = new WebServiceFactory()
		.getWebServiceInstance(WebServiceType.FOUNDATION_WEBSERVICE);
		HostGroup[] hostGroups = null;
		Category[] serviceGroups = null;
		try {
			hostGroups = foundationWSFacade.getAllHostGroups();
			serviceGroups = foundationWSFacade.getAllServiceGroups();
		} catch (WSDataUnavailableException exc) {
			logger.error(exc.getMessage());
		} catch (GWPortalException exc) {
			logger.error(exc.getMessage());
		} // end try/catch
		hgSelectItems = new SelectItem[hostGroups.length];
		hgSingleSelectItems = new SelectItem[hostGroups.length];
		if (null != hostGroups) {
			for (int i = 0; i < hostGroups.length; i++) {
				SelectItem item = new SelectItem(hostGroups[i].getName(),hostGroups[i].getName(),"",false);
				hgSelectItems[i] = item;
				SelectItem itemSingle = new SelectItem(hostGroups[i].getName(),"","",false);
				hgSingleSelectItems[i] = itemSingle;
			} // end for
		} // end if
		if (serviceGroups != null) {
			sgSelectItems = new SelectItem[serviceGroups.length];
			sgSingleSelectItems = new SelectItem[serviceGroups.length];
			if (null != serviceGroups) {
				for (int i = 0; i < serviceGroups.length; i++) {					
					SelectItem item = new SelectItem(serviceGroups[i].getName(),serviceGroups[i].getName(),"",false);
					sgSelectItems[i] = item;
					SelectItem itemSingle = new SelectItem(serviceGroups[i].getName(),"","",false);
					sgSingleSelectItems[i] = itemSingle;
				} // end for
			} // end if
		} // end if
	
	}

   public UIRole getUiRole()
   {
      return uiRole;
   }

   public void setUiRole(UIRole uiRole)
   {
      this.uiRole = uiRole;
   }
   
   public void setExtUIRole(ExtendedUIRole uiRole)
   {
      this.extUIRole = extUIRole;
   }
   
   public ExtendedUIRole getExtUIRole()
   {
      return extUIRole;
   }

   public IdentityRoleBean getIdentityRoleBean()
   {
      return identityRoleBean;
   }

   public void setIdentityRoleBean(IdentityRoleBean identityRoleBean)
   {
      this.identityRoleBean = identityRoleBean;
   }

   public String editRole()
   {
      FacesContext ctx = FacesContext.getCurrentInstance();
      ExternalContext ectx = ctx.getExternalContext();
      Map params = ectx.getRequestParameterMap();
      this.currentRole = params.get("currentRole") != null ? decoder.encode((String) params.get("currentRole")) : null;
      try
      {
         this.uiRole = identityRoleBean.getUIRole(this.currentRole);
         this.extUIRole = identityRoleBean.getExtendedUIRole(this.currentRole);
         return "editRole";
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return "roleAdmin";
   }

   public String updateRole()
   {
      try
      {
         identityRoleBean.updateRoleDisplayName(this.uiRole.getName(), this.uiRole.getDisplayName());
         ExtendedRoleModuleImpl extRoleImpl = new ExtendedRoleModuleImpl();
         extRoleImpl.updateRole(this.extUIRole.getId(), this.uiRole.getName(), this.extUIRole.isDashboardLinksDisabled(),CommonUtils.convert2HGString(this.extUIRole.getHgList()),CommonUtils.convert2SGString(this.extUIRole.getSgList()),this.extUIRole.getRestrictionType(),this.extUIRole.getDefaultHostGroup(), this.extUIRole.getDefaultServiceGroup(),this.extUIRole.isActionsEnabled());
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return "roleAdmin";
   }
   
	public SelectItem[] getHgSelectItems() {
		return this.hgSelectItems;
	}

	public void setHgSelectItems(SelectItem[] hgSelectItems) {
		this.hgSelectItems = hgSelectItems;
	}
	
	public SelectItem[] getSgSelectItems() {
		return this.sgSelectItems;
	}

	public void setSgSelectItems(SelectItem[] sgSelectItems) {
		this.sgSelectItems = sgSelectItems;
	}
	
	public SelectItem[] getHgSingleSelectItems() {
		return this.hgSingleSelectItems;
	}

	public void setHgSingleSelectItems(SelectItem[] hgSingleSelectItems) {
		this.hgSingleSelectItems = hgSingleSelectItems;
	}
	
	public SelectItem[] getSgSingleSelectItems() {
		return this.sgSingleSelectItems;
	}

	public void setSgSingleSelectItems(SelectItem[] sgSingleSelectItems) {
		this.sgSingleSelectItems = sgSingleSelectItems;
	}

}
