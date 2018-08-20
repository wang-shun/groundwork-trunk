/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

/*
 * Extended JBoss IFrame portlet with additional functionality and enhancements such as:
 * 1) Define URL as part of the portlet Init param
 * 2) Modify URL if url includes localhost or 127.0.0.* with the real hostname
 * 3) INIT param LINK {true|false} to turn on link open in new Window. Default false 
 * 4) Added new INIT param ATTACH_UID default false that enables the user name to be attached to the url example: gwuid=admin
 * 
 * Author: Roger Ruttimann (rruttimann@gwos.com) GroundWOrk Open Source Inc.
 */

package org.groundwork.portlet.iframe;

import java.util.Properties;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.PrintWriter;
import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletMode;
import javax.portlet.PortletSecurityException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import javax.portlet.PortletConfig;
import javax.portlet.PortletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.groundworkopensource.portal.common.FacesUtils;

/**
 * @authorArul Shanmugam (ashanmugam@gwos.com) GroundWOrk Open Source Inc.
 * 
 */
public class NMSPortlet extends GWOSIFramePortlet {
	/* INIT Parameter defining the PropFile */
	private static final String PARAM_PROP_FILE = "propFile";

	/* INIT Parameter defining the PropFile */
	private static final String PARAM_PROP_PREFIX = "propertyPrefix";

	/**
	 * Logging
	 */
	private static Log logger = LogFactory.getLog(NMSPortlet.class);

	private int numberofInstances = 1;

	private HashMap<String, String> hostUrlMap = null;

	private static final String SELECTED_SERVER_INSTANCE = "selectedServerInstance";

	/**
	 * Default constructor
	 */
	public NMSPortlet() {

	}

	/**
	 * Init phase of the portlet. Using it to read INIT params defined in the
	 * portlet.xml
	 * 
	 * @param config
	 * @throws PortletException
	 */
	public void init(PortletConfig config) throws PortletException {
		super.init(config);
		hostUrlMap = new HashMap<String, String>();
		Properties prop = GWOSIFramePortlet.loadPropertiesFromFilePath(config
				.getInitParameter(PARAM_PROP_FILE));
		if (!prop.isEmpty()) {
			String instanceStr = prop
					.getProperty("number."
							+ config.getInitParameter(PARAM_PROP_PREFIX)
							+ ".instances");
			if (instanceStr != null) {
				numberofInstances = Integer.parseInt(instanceStr);
				for (int i = 0; i < numberofInstances; i++) {
					String host = prop.getProperty(config
							.getInitParameter(PARAM_PROP_PREFIX)
							+ "."
							+ (i + 1) + ".host");
					String protocol = prop.getProperty(config
							.getInitParameter(PARAM_PROP_PREFIX)
							+ "."
							+ (i + 1) + ".protocol");
					String port = prop.getProperty(config
							.getInitParameter(PARAM_PROP_PREFIX)
							+ "."
							+ (i + 1) + ".port");
					if (port == null || port.equals(""))
						port = "80"; // if no port specified use 80
					String url = protocol
							+ "://"
							+ host.trim()
							+ ":"
							+ port.trim()
							+ prop.getProperty(config
									.getInitParameter(PARAM_PROP_PREFIX)
									+ "."
									+ (i + 1) + ".uri");
					hostUrlMap.put(host.trim(), url.trim());
					if (i == 0)
						this.configuredURL = url.trim();
				} // end for
			} else {
				String host = prop.getProperty(config
						.getInitParameter(PARAM_PROP_PREFIX) + ".host");
				String protocol = prop.getProperty(config
						.getInitParameter(PARAM_PROP_PREFIX) + ".protocol");
				String port = prop.getProperty(config
						.getInitParameter(PARAM_PROP_PREFIX) + ".port");
				if (port == null || port.equals(""))
					port = "80"; // if no port specified use 80
				String url = protocol
						+ "://"
						+ host.trim()
						+ ":"
						+ port.trim()
						+ prop.getProperty(config
								.getInitParameter(PARAM_PROP_PREFIX) + ".uri");
				this.configuredURL = url.trim();
				hostUrlMap.put(host.trim(), url.trim());
			} // end if
		} // end if
	}

	public void doView(RenderRequest request, RenderResponse response) {
		PrintWriter writer = null;
		try {
			Object roleObj = FacesUtils.getExtendedRoles();
			if (roleObj == null) {
				writer = response.getWriter();
				writer.write(FacesUtils.NO_EXTENDED_ROLES_ERROR);
			} else {
				/*
				 * Jboss portal doesn't make it easy to get access to a HTTP
				 * session
				 */
				String serverNameInURL = null;

				try {
					serverNameInURL = (String) getHttpServletRequest()
							.getSession().getAttribute("serverOfRequestURL");
				} catch (Exception e) {
					/* Should not happen in correct setup */
					System.out
							.println("Failed getting http request or Attribute serverOfRequestURL is not defined");
				}

				String viewId = "/jsp/iframe.jsp";
				if (numberofInstances > 1) {
					if (request.getPortletSession().getAttribute(
							SELECTED_SERVER_INSTANCE) == null) {
						request.setAttribute("serverList",
								new ArrayList<String>(hostUrlMap.keySet()));
						viewId = "/jsp/selectInstance.jsp";
					} else {
						String host = (String) request.getPortletSession()
								.getAttribute(SELECTED_SERVER_INSTANCE);
						this.configuredURL = hostUrlMap.get(host);
						/*
						 * Validate URL to make sure that we are the correct URL
						 * when the IFrame was configured for localhost
						 */
						if (this.configuredURL != null) {
							this.urlToRender = (this.configuredURL
									.startsWith("/") ? this.configuredURL
									: validateURL(this.configuredURL,
											serverNameInURL));
						}
						setRenderAttributes(request);
					}
				} else {
					viewId = "/jsp/iframe.jsp";
					/*
					 * Validate URL to make sure that we are the correct URL
					 * when the IFrame was configured for localhost
					 */
					if (this.configuredURL != null) {
						this.urlToRender = (this.configuredURL.startsWith("/") ? this.configuredURL
								: validateURL(this.configuredURL,
										serverNameInURL));
					}
					setRenderAttributes(request);
				} // end if

				response.setContentType("text/html");
				PortletRequestDispatcher prd = getPortletContext()
						.getRequestDispatcher(viewId);
				prd.include(request, response);
			}
		} catch (Exception e) {
			try {
				writer = response.getWriter();
				writer.write(FacesUtils.NO_EXTENDED_ROLES_ERROR);
			} catch (Exception exc) {
				logger.error(exc.getMessage());
			}
			e.printStackTrace();
		} finally {
			try {
				if (writer != null)
					writer.close();
			} catch (Exception exc) {
				logger.error(exc.getMessage());
			}
		}
	}

	public void processAction(ActionRequest request, ActionResponse response)
			throws PortletException, PortletSecurityException, IOException {
		String selectedServer = (String) request
				.getParameter(SELECTED_SERVER_INSTANCE);
		request.getPortletSession().setAttribute(SELECTED_SERVER_INSTANCE,
				selectedServer);
		response.setPortletMode(PortletMode.VIEW);
	}

	protected void setRenderAttributes(RenderRequest request) {
		try {

			StringBuffer updURL = new StringBuffer(this.urlToRender);
			/*
			 * Check if the IFRame URL needs to have the GroundWork UID attached
			 * to the URL
			 */
			if (this.isAttachUID == true) {
				String uid = request.getUserPrincipal().getName();
				if (this.urlToRender.indexOf('?') == -1) {
					updURL.append("?gwuid=").append(uid);
				} else {
					updURL.append("&gwuid=").append(uid);
				} // end if
			}
			request.setAttribute("iframeurl", updURL);
			request.setAttribute("iframeheight", this.defaultHeight);
			request.setAttribute("iframewidth", this.defaultWidth);
			if (allowRemoteURL != null)
				request.setAttribute("allow_remote_url", allowRemoteURL);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}