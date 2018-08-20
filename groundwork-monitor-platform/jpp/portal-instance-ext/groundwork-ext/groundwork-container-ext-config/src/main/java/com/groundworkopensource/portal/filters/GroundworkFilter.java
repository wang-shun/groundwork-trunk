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
package com.groundworkopensource.portal.filters;

import com.groundworkopensource.portal.extension.rest.ExtendedRoleService;
import com.groundworkopensource.portal.identity.extendedui.ReportHelper;
import com.groundworkopensource.portal.licensing.EnterpriseLicenseValidator;
import com.groundworkopensource.portal.licensing.LicenseManager;
import com.groundworkopensource.portal.licensing.PropertyUtils;
import org.apache.log4j.Logger;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.web.AbstractFilter;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;

import javax.faces.context.FacesContext;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collection;

/**
 * A simple servlet filter to populate extended UI role attributes in the
 * session.
 */

public class GroundworkFilter extends AbstractFilter {

	private static final Logger log = Logger.getLogger(GroundworkFilter.class);

	private static final String LICENSE_VIEW_PATH = "/portal/classic/groundwork-administration/licenseview";
	private final static String FOUNDATION_TOKEN = "FoundationToken";
	protected static final String FOUNDATION_REST_SERVICE = "FoundationRestService";

	/** The Constant Foundation properties path. */
	private static final String FOUNDATION_CONFIG_PATH = "/usr/local/groundwork/config/foundation.properties";

	/**
	 * Do filter
	 */
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		log.debug("Entering GroundworkFilter");
		long startTime = 0;
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		String portalComponentId = httpRequest
				.getParameter("portal:componentId");
		String portalAction = httpRequest.getParameter("portal:action");
		String userName = httpRequest.getRemoteUser();
		log.debug("Login user : " + userName);
		boolean isLicValid = false;
		String requestURI = httpRequest.getRequestURI();
		log.info("URI=" + requestURI);

		// URL Mapper stuff goes here
		if (requestURI != null && requestURI.equals("/portal/classic/status")) {
			String nodeType = (String) httpRequest.getParameter("nodeType");
			String nodeID = (String) httpRequest.getParameter("nodeID");
			String nodeName = (String) httpRequest.getParameter("name");
			String path = (String) httpRequest.getParameter("path");
			httpRequest.setAttribute("com.gwos.sv.nodeType", nodeType);
			httpRequest.setAttribute("com.gwos.sv.nodeID", nodeID);
			httpRequest.setAttribute("com.gwos.sv.nodeName", nodeName);
			httpRequest.setAttribute("com.gwos.sv.path", path);
		} // end if

		// License validation stuff goes here
		if (userName != null
				&& (!(requestURI.endsWith(".js") || requestURI.endsWith(".css")))) {
			Object hasBeenValidated = httpRequest.getSession().getAttribute(
					"hasbeenvalidated");

			/* Check if the session needs to be validated */
			if (hasBeenValidated == null) {

				/* Get the validation object for validation checks */
				EnterpriseLicenseValidator validator = LicenseManager
						.getInstance().getLicenseValidator();
				FacesContext context = FacesContext.getCurrentInstance();
				getServletContext().setAttribute("licenseValidator",validator);

				if (validator != null) {
					if (log.isDebugEnabled()) {
						log.debug("Session not validated. Run validation");
						startTime = System.currentTimeMillis();
					}

					isLicValid = validator.validate();

					/*
					 * License validation checks for the hard limits. If it
					 * fails re-direct to the license page otherwise continue
					 * check for soft limits
					 */
					if (isLicValid) {
						/*
						 * Set the flag that will skip future test when entering
						 * the interceptor
						 */
						httpRequest.getSession().setAttribute(
								"hasbeenvalidated", "true");
					} else {
						if (requestURI != null
								&& !requestURI.equals(LICENSE_VIEW_PATH)) {

							log.info("Redirect to license page...");
							String location = httpResponse
									.encodeRedirectURL(LICENSE_VIEW_PATH);
							httpResponse.sendRedirect(location);
							return;
						} // end if
					}
					String roleList = PropertyUtils.getPropertyFromFilePath(FOUNDATION_CONFIG_PATH,"soft.limit.display.list");

					// GWMON-11052, 9697 and 11985
					// only call validator if user match
					boolean userMatch = isUserMatch(roleList, httpRequest);
					if (userMatch) {
						if (validator.isSoftLimitExceeded()) {
							if (log.isDebugEnabled()) {
								log.debug("Soft Limit exceeded. Set flag.");
							}
							httpRequest.getSession().setAttribute(
									"softlimitexceeded", "true");
							httpRequest.getSession().setAttribute("softlimitmessage", validator.getSoftLimitMessage());
							httpRequest.getSession().setAttribute("softlimitbgcolor", validator.getSoftLimitbgColor());
							httpRequest.getSession().setAttribute("softlimittxtcolor", validator.getSoftLimittxtColor());
						} else {
							if (log.isDebugEnabled()) {
								log.debug("Soft Limit NOT exceeded. Remove flag if existing.");
							}
							httpRequest.getSession().removeAttribute(
									"softlimitexceeded");
						}
					}

					if (log.isDebugEnabled()) {
						log.debug("Validate and soft limit check took ["
								+ (System.currentTimeMillis() - startTime)
								+ "]ms");
					}
				}
				/* De-reference the vaidator instance since is no longer used */
				validator = null;
			}
		}
		/* Clean flag on logout */
		if (isLogoutRequest(httpRequest)) {
			httpRequest.getSession().removeAttribute("hasbeenvalidated");
			isLicValid = false;
			if (log.isDebugEnabled()) {
				log.debug("Cleaned validation flag");
			}
			// @since 7.2.0
			Cookie killMyCookie = new Cookie(FOUNDATION_TOKEN, null);
			killMyCookie.setMaxAge(0);
			killMyCookie.setPath("/");
			httpResponse.addCookie(killMyCookie);
			killMyCookie = new Cookie(FOUNDATION_REST_SERVICE, null);
			killMyCookie.setMaxAge(0);
			killMyCookie.setPath("/");
			httpResponse.addCookie(killMyCookie);
		}
		chain.doFilter(request, response);
	}

	// Return true if logout request is in progress
	private boolean isLogoutRequest(HttpServletRequest req) {
		String portalComponentId = req.getParameter("portal:componentId");
		String portalAction = req.getParameter("portal:action");
		if (("UIPortal".equals(portalComponentId))
				&& ("Logout".equals(portalAction))) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 *
	 * @param roleList from foundation.properties => soft.limit.display.list=GWAdmin,GWOperator
	 * @param httpRequest
	 * @return
	 */
	private boolean isUserMatch(String roleList, HttpServletRequest httpRequest) {
		boolean match = false;

		if (roleList != null) {
			// remove leading and trailing blanks
			String[] roles = roleList.split("\\s*,\\s*");
			for (String role: roles) {
				if (httpRequest.isUserInRole(role)) {
					match = true;
					break;
				}
			}
		}

		return match;
	}

	/**
	 * Override
	 */
	@Override
	protected void afterInit(FilterConfig config) throws ServletException {

	}

	/**
	 * destroy
	 */
	public void destroy() {
	}

}
