/*
 *  Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork)
 *  All rights reserved. Use is subject to GroundWork commercial license terms.
 */

package com.groundworkopensource.webapp.console;

import java.io.IOException;

import javax.portlet.PortletException;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.filter.FilterChain;
import javax.portlet.filter.RenderFilter;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.portlet.PortletContext;
import java.util.Map;
import java.util.Enumeration;

import org.gatein.pc.portlet.impl.jsr168.api.RenderRequestImpl; 

import org.apache.log4j.Logger;


import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.statusviewer.handler.ReferenceTreeMetaModel;

/**
 * 
 * @author manish_kjain
 * 
 */
public class ConsoleFilter implements RenderFilter {

	private static Logger logger = Logger.getLogger(ConsoleFilter.class
			.getName());

	/**
	 * destroy the filter
	 * 
	 * @see javax.portlet.filter.PortletFilter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub

	}

	/**
	 * Intercepts all JSF/JSP calls. Does a crossContext and get the RTMM from
	 * statusviewer.
	 * @param request
	 * @param response
	 * @param chain
	 * @throws IOException
	 * @throws PortletException
	 */
	public void doFilter(RenderRequest request, RenderResponse response,
			FilterChain chain) throws IOException, PortletException {
		logger.debug("Enter doFilter method");
		try {
			Object userObj = null;

			Object sessionUsername = request.getUserPrincipal().getName();
			if (sessionUsername != null) {
				userObj = sessionUsername;

			} // end if

			if (userObj != null) {
				String user = (String) userObj;
				PortletSession session = request.getPortletSession();
				Object sessionUserObj = session
						.getAttribute(ConsoleConstants.SESSION_LOGIN_USER);
				if (sessionUserObj == null) {
					logger.debug("User not in session.Adding user to session");
					session.setAttribute(ConsoleConstants.SESSION_LOGIN_USER,
							user);
				}
				// Now make a crosscontext and populate RTMM from status-viewer..
				HttpServletRequestWrapper hsrw = null;
				if (request instanceof RenderRequestImpl) { 
					RenderRequestImpl renderRequest = (RenderRequestImpl) request; 
					hsrw = renderRequest.getRealRequest();
				} // end if

				// hsrw cannot be null. It is the basic HTTP Request wrapper
				ServletContext servContext = null;
				if (hsrw != null)
					servContext = hsrw.getSession().getServletContext();
				logger.debug("servletContext" + servContext);
				if (servContext != null) {
					// Cross Context to reference StatusViewer object RTMM
					ServletContext statusViewerContext = servContext
							.getContext("/portal-statusviewer");
					logger.debug("statusviewerContext" + statusViewerContext);
					if (statusViewerContext != null) {
						ReferenceTreeMetaModel rtmm = (ReferenceTreeMetaModel) statusViewerContext
								.getAttribute(ConsoleConstants.MANAGED_BEAN_RTMM);
						if (rtmm == null)
							logger.debug("RTMM not initialized. Usually check-listener.pl should initialize it.If not just visit statusviewer once after the restart!");
						// Now put RTMM in the console application scope
						servContext.setAttribute(
								ConsoleConstants.MANAGED_BEAN_RTMM, rtmm);
					} // end if
				} // end if
				chain.doFilter(request, response);

			} // end if
		} catch (Exception exc) {
			logger.error(exc.getMessage());
			// TODO : Redirect to global error page. Should be done at portal
			// level

		} // end try/catch
	}

	/**
	 * initialize the filter
	 * 
	 * @see javax.portlet.filter.PortletFilter#init(javax.portlet.filter.FilterConfig)
	 */
	public void init(javax.portlet.filter.FilterConfig arg0)
			throws PortletException {
		// TODO Auto-generated method stub

	}

}
