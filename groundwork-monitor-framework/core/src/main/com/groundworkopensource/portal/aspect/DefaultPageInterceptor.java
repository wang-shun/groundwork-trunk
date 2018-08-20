/*
 * Copyright 2009 GroundWork Open Source, Inc. ("GroundWork") All rights
 * reserved. This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package com.groundworkopensource.portal.aspect;

import org.apache.log4j.Logger;
import org.jboss.portal.common.invocation.InvocationException;
import org.jboss.portal.core.controller.ControllerCommand;
import org.jboss.portal.core.controller.ControllerContext;
import org.jboss.portal.core.controller.ControllerInterceptor;
import org.jboss.portal.core.controller.ControllerResponse;
import org.jboss.portal.core.controller.command.response.RedirectionResponse;
import org.jboss.portal.core.model.CustomizationManager;
import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.Portal;
import org.jboss.portal.core.model.portal.PortalObject;
import org.jboss.portal.core.model.portal.command.PageCommand;
import org.jboss.portal.core.model.portal.command.view.ViewPageCommand;
import org.jboss.portal.server.request.URLContext;
import org.jboss.portal.server.request.URLFormat;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

import com.groundworkopensource.portal.model.LicenseManager;
import com.groundworkopensource.portal.model.EnterpriseLicenseValidator;

/**
 * Redirects requests to top-level menu items to default subpages. (Hack to get
 * around JBoss Portal's lack of functionality in this area.)
 * 
 * @author Paul Burry
 * @version $Revision: 17115 $
 * @since GWMON 6.0
 */
public class DefaultPageInterceptor extends ControllerInterceptor {
	/**
	 * Logger.
	 */
	private Logger log = Logger.getLogger(DefaultPageInterceptor.class);

	/**
	 * Default dashboard subpage name.
	 */
	private String defaultDashboardPage = "summary";

	/**
	 * Default resources subpage name.
	 */
	private String defaultResourcesPage = "docs";

	/**
	 * Default "My Groundwork" subpage name.
	 */
	private String defaultMyGroundworkPage = "default";

	/**
	 * Portal dashboard customization manager.
	 */
	private CustomizationManager customizationManager;
	
	private String CONSOLE_WAR_PATH = "/usr/local/groundwork/foundation/container/webapps/jboss/jboss-portal.sar/portal-console.war";

	/**
	 * @return the customizationManager
	 */
	public CustomizationManager getCustomizationManager() {
		return customizationManager;
	}

	/**
	 * @param customizationManager
	 *            the customizationManager to set
	 */
	public void setCustomizationManager(
			CustomizationManager customizationManager) {
		this.customizationManager = customizationManager;
	}

	/**
	 * @return the defaultPageName
	 */
	public String getDefaultDashboardPage() {
		return defaultDashboardPage;
	}

	/**
	 * @param defaultPageName
	 *            the defaultPageName to set
	 */
	public void setDefaultDashboardPage(String defaultDashboardPage) {
		this.defaultDashboardPage = defaultDashboardPage;
	}

	/**
	 * @return the defaultResourcesPage
	 */
	public String getDefaultResourcesPage() {
		return defaultResourcesPage;
	}

	/**
	 * @param defaultResourcesPage
	 *            the defaultResourcesPage to set
	 */
	public void setDefaultResourcesPage(String defaultResourcesPage) {
		this.defaultResourcesPage = defaultResourcesPage;
	}

	/**
	 * @return the defaultMyGroundworkPage
	 */
	public String getDefaultMyGroundworkPage() {
		return defaultMyGroundworkPage;
	}

	/**
	 * @param defaultMyGroundworkPage
	 *            the defaultMyGroundworkPage to set
	 */
	public void setDefaultMyGroundworkPage(String defaultMyGroundworkPage) {
		this.defaultMyGroundworkPage = defaultMyGroundworkPage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jboss.portal.core.controller.ControllerInterceptor#invoke(org.jboss
	 * .portal.core.controller.ControllerCommand)
	 */
	@Override
	public ControllerResponse invoke(ControllerCommand cmd) throws Exception,
			InvocationException {
		ControllerResponse response = null;
		ControllerContext controllerContext = cmd.getControllerContext();
		boolean isLicValid = false;
		long startTime = 0;
		
		HttpServletRequest	request 		= controllerContext.getServerInvocation().getServerContext().getClientRequest();
		Object 				hasBeenValidated= request.getSession().getAttribute("hasbeenvalidated");
		
		/*Check if the session needs to be validated */
		if (hasBeenValidated == null) {
			
			/* Get the validation object for validation checks */
			EnterpriseLicenseValidator validator = LicenseManager.getInstance().getLicenseValidator();
			
			if (validator != null) {
				
				/* Validation only available in enterprise */
				// Check if console war file exists.If exists then it is enterprise
				File consoleWAR = new File(CONSOLE_WAR_PATH);
				//if (consoleWAR.exists()) {
		
					if (log.isDebugEnabled() ) {
						log.debug("Session not validated. Run validation");
						startTime = System.currentTimeMillis();
					}
					
					isLicValid = validator.validate();
					
					/* License validation checks for the hard limits. If it fails re-direct to the license page
					 * otherwise continue check for soft limits */
					if (isLicValid) {
						/* Set the flag that will skip future test when entering the interceptor */
						request.getSession().setAttribute("hasbeenvalidated", "true");	
					}
					else
					{
						if (cmd instanceof PageCommand) {
							Page page = ((PageCommand) cmd).getPage();
							String pageName = page.getName();
							if (!pageName.equals("licenseview")) {
								request.getSession().setAttribute("softlimitexceeded",
										"true");
								request.getSession().setAttribute("hardlimitexceeded",
								"true");
								response = new RedirectionResponse(
										"/portal/auth/portal/groundwork-monitor/admin/licenseview");
								return response;
							}
						}
					}
					
					if (validator.isSoftLimitExceeded()) {
						if (log.isDebugEnabled() ) {
							log.debug("Soft Limit exceeded. Set flag.");
						}
						request.getSession().setAttribute("softlimitexceeded", "true");
					} else {
						if (log.isDebugEnabled() ) {
							log.debug("Soft Limit NOT exceeded. Remove flag if existing.");
						}
						request.getSession().removeAttribute("softlimitexceeded");
					}
					
					if (log.isDebugEnabled()) {
						log.debug("Validate and soft limit check took ["+ (System.currentTimeMillis() - startTime) +"]ms");
					}
					/* Clean flag on logout */
					if (	request.getRequestURI().indexOf("/portal/portal") >= 0
						|| 	request.getRequestURI().indexOf("auth/signout") >= 0   )
					{
						request.getSession().removeAttribute("hasbeenvalidated");
						isLicValid = false;
						
						if (log.isDebugEnabled() ) {
							log.debug("Cleaned validation flag");
						}	
					}
				//}
			}
			/* De-reference the vaidator instance since is no longer used */
			validator = null;		
		}
		else
		{
			isLicValid = true;
		}
			
		if (cmd instanceof PageCommand) {
			Page page = ((PageCommand) cmd).getPage();
			log.debug("Page: " + page.getId());
			PortalObject parent = page.getParent();
			if (parent instanceof Portal
					&& "groundwork-monitor".equals(parent.getName())) {
				Page redirectPage = null;
				String pageName = page.getName();
				if ("dashboard".equals(pageName)) {
					redirectPage = page.getPage(defaultDashboardPage);
				} else if ("resources".equals(pageName)) {
					redirectPage = page.getPage(defaultResourcesPage);
				} else if ("mygroundwork".equals(pageName)) {
					Portal myGroundwork = customizationManager.getDashboard(cmd
							.getControllerContext().getUser());
					redirectPage = myGroundwork
							.getPage(defaultMyGroundworkPage);
				}
				
				
				if (redirectPage != null) {

					ViewPageCommand viewPageCommand = new ViewPageCommand(
							redirectPage.getId());
					URLContext urlContext = controllerContext
							.getServerInvocation().getServerContext()
							.getURLContext();
					String redirectURL = controllerContext.renderURL(
							viewPageCommand, urlContext, URLFormat.newInstance(
									true, true));
					
					response = new RedirectionResponse(redirectURL);
				}
			}
		}

		if (response == null) {
			
			response = (ControllerResponse) cmd.invokeNext();
		}

		return response;
	}

}
