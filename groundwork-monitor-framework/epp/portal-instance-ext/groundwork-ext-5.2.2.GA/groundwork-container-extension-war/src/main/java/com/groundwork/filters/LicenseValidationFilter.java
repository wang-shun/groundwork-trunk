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
package com.groundwork.filters;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.Principal;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.groundwork.portal.security.LicenseUtils;

/**
 * A simple servlet filter to attempt validation of the GroundWork license.  This is for every request and is
 * stored in the session if it is valid.  The validation will attempt to check the file system first.  If no
 * valid license can be found in the WAR, then the request will be redirected to a license page where the 
 * user will be able to enter a valid license.
 * 
 * Also, there must be a valid user logged into the session.  
 */
public class LicenseValidationFilter implements Filter {
	// TODO:  Change to not use the 'root logger'
	private static final Logger log = Logger.getRootLogger();

	private static final String HAS_BEEN_VALIDATED = "hasbeenvalidated";

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
		throws IOException, ServletException {

		log.info("Entering LicenseValidationFilter");

		final HttpServletRequest httpServletRequest = (HttpServletRequest) request;

		// Configured in the 'config.properties' and initialized in the 'init()' method below.
		final String prop_url_license = System.getProperty("url.license");

		final boolean hasBeenValidated = Boolean.parseBoolean( "" + httpServletRequest.getSession().getAttribute( HAS_BEEN_VALIDATED ) );

		final Principal userPrincipal = httpServletRequest.getUserPrincipal();
		
		// Check if the session needs to be validated
		if ( userPrincipal != null && !hasBeenValidated && !httpServletRequest.getRequestURI().contains( prop_url_license ) ) {
			// Check to see if the file system has a valid license
			final boolean hasValidLicense = LicenseUtils.validateWebArchive();

			// If there was a license found and it is valid, simply place the flag 
			// into the session and move on..
			if (hasValidLicense) {
				httpServletRequest.getSession().setAttribute(HAS_BEEN_VALIDATED, "true");

				chain.doFilter(request, response);
			} else {
				log.warn("No valid license found.  Redirecting to license page: " + prop_url_license);

				// This is simply test code to tell the 'license.jsp' where to redirect after it has 
				// validated the license attribute in the session.
				final String frompage = httpServletRequest.getRequestURL().toString();
				httpServletRequest.setAttribute("frompage", frompage );
				
				// Redirect to the license validation page. 
				// NOTE: 'prop_url_license' is assigned in the 'init()' method.
				final String redirectPath = "/" + prop_url_license;
				  
				request.getRequestDispatcher(redirectPath).include(request, response);
			}
		} 
		// Check to see if there is a user logged in.  If there isn't a user logged in then we don't have to validate the license.
		// Only continue with validation if it hasn't been validated yet and there is a user principal 
		else if ( userPrincipal == null ) {
			chain.doFilter(request, response);
		} 
		// Else, default to open.. which may not be the best approach..
		// If there is a user and a 
		else {
			chain.doFilter(request, response);
		}
	}

	public void init(javax.servlet.FilterConfig chain) throws ServletException {
		log.info("Servlet Init");

		// TODO: Put this someplace else that will get loaded only once on
		// startup. There must be a
		// class that gets bootstrapped somewhere.
		try {
			final InputStream inputStream = getClass().getResourceAsStream("/config.properties");
			System.getProperties().load(new InputStreamReader(inputStream));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void destroy() {
	}

}
