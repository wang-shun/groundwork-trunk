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

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.jboss.portal.core.aspects.controller.PageCustomizerInterceptor;
import org.jboss.portal.core.aspects.controller.node.Navigation;
import org.jboss.portal.core.controller.Controller;
import org.jboss.portal.core.controller.ControllerCommand;
import org.jboss.portal.core.controller.ControllerContext;
import org.jboss.portal.core.controller.ControllerRequestDispatcher;
import org.jboss.portal.core.controller.command.SignOutCommand;
import org.jboss.portal.core.impl.api.node.PortalNodeImpl;
import org.jboss.portal.core.model.portal.Page;
import org.jboss.portal.core.model.portal.Portal;
import org.jboss.portal.core.model.portal.PortalObject;
import org.jboss.portal.core.model.portal.PortalObjectId;
import org.jboss.portal.core.model.portal.PortalObjectPath;
import org.jboss.portal.core.model.portal.command.PageCommand;
import org.jboss.portal.core.model.portal.command.view.ViewContextCommand;
import org.jboss.portal.identity.User;
import org.jboss.portal.security.spi.auth.PortalAuthorizationManager;
import org.jboss.portal.server.config.ServerConfig;
import org.jboss.portal.server.request.URLContext;
import org.jboss.portal.server.request.URLFormat;
import org.jboss.portal.api.node.PortalNode;

/**
 * A Groundwork Monitor-specific replacement for the JBoss Portal
 * PageCustomizerInterceptor.
 * 
 * @author Paul Burry
 * @version $Id$
 * @since GWMON 6.0 Removed references to the StatusViewer since 6.1.1
 */
public class GroundworkPortalCustomizerInterceptor extends
		PageCustomizerInterceptor {
	// TODO: Refactor this class so it no longer extends
	// PageCustomizerInterceptor

	private static final String MAIN_DASHBOARD_PAGE = "mygroundwork";

	/**
	 * The name of the dashboard configuration page.
	 */
	private static final String DASHBOARD_CONFIG_PAGE = "default";
	
	private static final String SUPPORTED_3RD_LEVEL_MENUS_PROP = "supported.3rd.level.menus";

	/**
	 * Overridden from PageCustomizerInterceptor in order to fit custom
	 * requirements for the GWMON Portal. Main tab navigation always comes from
	 * the default portal ("groundwork-monitor"), but second-level navigation
	 * may come from either the dashboard or statusview portals, if the user is
	 * on one of them.
	 * 
	 * @see org.jboss.portal.core.aspects.controller.PageCustomizerInterceptor#injectTabbedNav(org.jboss.portal.core.model.portal.command.PageCommand)
	 */
	@Override
	public String injectTabbedNav(PageCommand rpc) {
		String markup = null;

		// HACK: Since calling super.injectTabbedNav() does not appear to work
		// due to issues with the request dispatcher, this code is copied
		// directly from the PageCustomizerInterceptor.injectTabbedNav() method.
		ControllerContext controllerCtx = rpc.getControllerContext();
		ControllerRequestDispatcher rd = controllerCtx.getRequestDispatcher(
				getTargetContextPath(), getTabsPath());

		if (rd != null) {
			Controller controller = controllerCtx.getController();
			User user = controllerCtx.getUser();
			Page page = rpc.getPage();
			PortalAuthorizationManager pam = getPortalAuthorizationManagerFactory()
					.getManager();
			PortalNodeImpl node = new PortalNodeImpl(pam, page);

			rd.setAttribute("org.jboss.portal.api.PORTAL_NODE", node);
			rd.setAttribute("org.jboss.portal.api.PORTAL_RUNTIME_CONTEXT",
					Navigation.getPortalRuntimeContext());

			Portal pageRoot = page.getPortal();

			Portal dashboard = controller.getCustomizationManager()
					.getDashboard(user);
			Portal defaultPortal = getPortalObjectContainer().getContext()
					.getDefaultPortal();
			PortalNodeImpl defaultPortalNode = new PortalNodeImpl(pam,
					defaultPortal);

			Collection<PortalNode> mainPages = defaultPortalNode.getChildren();
			Collection<PortalNode> subPages = null;
			PortalNode currentMainPage = null;
			PortalNode currentSubPage = null;

			Collection<PortalNode> level3Pages = null;
			PortalNode currentLevel3Page = null;
			// If we're on a dashboard or status viewer page, show the
			// "dashboard" or "status" page as the current main page
			if (dashboard != null && (dashboard.equals(pageRoot)
					|| (defaultPortal.equals(pageRoot) && page.getName()
							.equals(MAIN_DASHBOARD_PAGE)))) {
				currentMainPage = defaultPortalNode
						.getChild(MAIN_DASHBOARD_PAGE);
				currentSubPage = node;
				subPages = new PortalNodeImpl(pam, dashboard).getChildren();
			} else {
				// Determine if the current page is a main page or a subpage
				PortalObject parent = page.getParent();
				if (parent != null
						&& parent.getType() == PortalObject.TYPE_PAGE) {
					currentMainPage = new PortalNodeImpl(pam, parent);
					if ( is3rdLevelMenuSupported(page,node.getParent().getName()))	{
							currentSubPage = node.getParent();
							level3Pages = node.getParent().getChildren();
							subPages = node.getParent().getParent().getChildren();
					}
					else 	{
						currentSubPage = node;
						level3Pages = node.getChildren();
						subPages = node.getParent().getChildren();
					} // end if
				} else {
					currentMainPage = node;
					currentSubPage = null;
					subPages = currentMainPage.getChildren();
				}
				
			}

			rd.setAttribute("com.groundworkopensource.portal.MAIN_PAGES",
					mainPages);
			rd.setAttribute("com.groundworkopensource.portal.SUB_PAGES",
					subPages);
			rd.setAttribute(
					"com.groundworkopensource.portal.CURRENT_MAIN_PAGE",
					currentMainPage);
			rd.setAttribute("com.groundworkopensource.portal.CURRENT_SUB_PAGE",
					currentSubPage);

			rd.setAttribute("com.groundworkopensource.portal.LEVEL3_PAGES",
					level3Pages);
			rd.setAttribute(
					"com.groundworkopensource.portal.CURRENT_LEVEL3_PAGE",
					currentLevel3Page);

			rd.include();
			markup = rd.getMarkup();
		}

		return markup;
	}
	
	/**
	 * Helper method
	 * @param page
	 * @param secondLevelMenuName
	 * @return
	 */
	private boolean is3rdLevelMenuSupported(Page page, String secondLevelMenuName)
	{
		boolean result = false;
		String supported3rdLevelMenus = page.getProperty(SUPPORTED_3RD_LEVEL_MENUS_PROP);
		if (supported3rdLevelMenus != null && !"".equalsIgnoreCase(supported3rdLevelMenus)) {
			StringTokenizer stkn = new StringTokenizer(supported3rdLevelMenus,",");
			while (stkn.hasMoreTokens())
			{
				String menuName = stkn.nextToken();
				if (menuName != null && !menuName.equalsIgnoreCase("") && menuName.equalsIgnoreCase(secondLevelMenuName)) {
					result=true;
					break;
				} // end if
			} // end while
		} // end if
		return result;
	}
	

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.jboss.portal.core.aspects.controller.PageCustomizerInterceptor#injectDashboardNav(org.jboss.portal.core.controller.ControllerCommand)
	 */
	@Override
	public String injectDashboardNav(ControllerCommand cc) {
		// HACK: Since calling super.injectDashboardNav() does not appear to
		// work due to issues with the request dispatcher, some of this code is
		// copied directly from the
		// PageCustomizerInterceptor.injectDashboardNav() method.
		String markup = null;
		ControllerContext controllerCtx = cc.getControllerContext();
		ControllerRequestDispatcher rd = controllerCtx.getRequestDispatcher(
				getTargetContextPath(), getHeaderPath());

		//
		if (rd != null) {
			ServerConfig config = getConfig();

			// Get user
			Controller controller = controllerCtx.getController();
			User user = controllerCtx.getUser();
			rd.setAttribute("org.jboss.portal.header.USER", user);

			Map<String, String> userProfile = controllerCtx.getUserProfile();
			if (userProfile != null) {
				rd.setAttribute("org.jboss.portal.header.USER_PROFILE",
						userProfile);
			}

			Principal principal = controllerCtx.getServerInvocation()
					.getServerContext().getClientRequest().getUserPrincipal();
			rd.setAttribute("org.jboss.portal.header.PRINCIPAL", principal);

			if (principal == null) {
				String loginNamespace = getLoginNamespace();

				if (loginNamespace == null) {
					loginNamespace = getConfig().getProperty(
							"core.login.namespace");
				}

				String securedLogin = config.getProperty("core.login.secured");
				boolean wantSecure = (securedLogin != null && "true"
						.equals(securedLogin.toLowerCase()));

				ViewContextCommand vcc = new ViewContextCommand(
						new PortalObjectId(loginNamespace,
								new PortalObjectPath()));

				rd.setAttribute("org.jboss.portal.header.LOGIN_URL", renderURL(
						vcc, controllerCtx, Boolean.TRUE, wantSecure));
			}

			if (user != null) {
				Portal dashboard = controller.getCustomizationManager()
						.getDashboard(user);

				if (dashboard != null) {
					rd.setAttribute(
							"org.jboss.portal.header.EDIT_DASHBOARD_URL",
							new PortalNodeImpl(
									getPortalAuthorizationManagerFactory()
											.getManager(), dashboard
											.getPage(DASHBOARD_CONFIG_PAGE))
									.createURL(Navigation
											.getPortalRuntimeContext()));
				}

				rd.setAttribute("org.jboss.portal.header.SIGN_OUT_URL",
						renderURL(new SignOutCommand(), controllerCtx,
								Boolean.FALSE, null));
			}

			//   
			rd.include();
			markup = rd.getMarkup();
		}

		//
		return markup;
	}

	/**
	 * @param command
	 * @param controllerCtx
	 * @param wantAuthenticated
	 * @param wantSecure
	 * @return renderURL
	 */
	private String renderURL(ControllerCommand command,
			ControllerContext controllerCtx, Boolean wantAuthenticated,
			Boolean wantSecure) {
		URLContext urlContext = controllerCtx.getServerInvocation()
				.getServerContext().getURLContext();
		return controllerCtx.renderURL(command, urlContext, URLFormat
				.newInstance(wantAuthenticated, true));
	}
}
