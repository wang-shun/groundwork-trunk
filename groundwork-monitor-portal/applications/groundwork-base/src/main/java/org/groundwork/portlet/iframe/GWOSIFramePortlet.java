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

import org.jboss.portlet.iframe.IFramePortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.model.CommonUtils;
import com.groundworkopensource.portal.common.PropertyUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.servlet.http.HttpServletRequest;

import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Roger Ruttimann (rruttimann@gwos.com) GroundWOrk Open Source Inc.
 * 
 */
public class GWOSIFramePortlet extends IFramePortlet {

	/* INIT Parameter defining the URL */
	private static final String PARAM_URL = "URL";

	/* INIT Param to activate link to open URL in sepaarate Window */
	private static final String PARAM_WINDOW = "LINK";

	/* INIT Param to include UID to URL */
	private static final String PARAM_UID = "ATTACH_UID";

	/* INIT Parameter defining the URL */
	private static final String PARAM_ALLOW_REMOTE_URL = "allow_remote_url";

	protected static final String defaultHeight = "2000px";
	protected static final String defaultWidth = "100%";
	protected static final String defaultNonIFrameMessage = "Your browser does not support iframes";
	private static final String JPP_CONFIG = "/usr/local/groundwork/jpp/standalone/configuration/gatein/configuration.properties";

	/* Host name to be used to replace localhost */
	protected String hostName;

	/* URL for IFrame set default to GroundWork */
	protected String urlToRender = "http://www.groundworkopensource.com";

	/* Keep track of configured URL since it might change doing doView */
	protected String configuredURL = urlToRender;

	/* By default don't show link. Can be overwritten bi unit param */
	protected String isOpenInWindow = "false";

	/* By default user ID is not attached to the URL as query string */
	protected boolean isAttachUID = false;

	/*
	 * Default protocol for IFrame is http but it can be changed with a property
	 * setting
	 */
	private String protocolUsed = "http";

	private static final String protocolHTTPS = "https";

	private static final String PROTOCOL_PROPERTY = "secure.access.enabled";
	/**
	 * Properties file
	 */
	private static final String PROPERTIES_FILE_PATH = "/usr/local/groundwork/config/status-viewer.properties";

	/**
	 * Logging
	 */
	private static Log logger = LogFactory.getLog(GWOSIFramePortlet.class);

	/*
	 * By default remote url is not allowed due the javascript limitation for
	 * the jQuery to calculate the height
	 */
	protected String allowRemoteURL = "false";

	/**
	 * Default constructor
	 */
	public GWOSIFramePortlet() {

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

		// Get the INIT PARAMETERS for this portlet. If the values are missing
		// throw an exception
		configuredURL = config.getInitParameter(PARAM_URL);
		isOpenInWindow = config.getInitParameter(PARAM_WINDOW);

		// Set the allowremoteURL variable from the portlet conf
		if (config.getInitParameter(PARAM_ALLOW_REMOTE_URL) != null)
			allowRemoteURL = config.getInitParameter(PARAM_ALLOW_REMOTE_URL);

		/* Set the default value if it is not defined */
		if (this.isOpenInWindow == null || this.isOpenInWindow.length() == 0)
			this.isOpenInWindow = "false";

		String attachUID = config.getInitParameter(PARAM_UID);
		/* Set the default value if it is not defined */
		if (attachUID != null && (attachUID.compareToIgnoreCase("true") == 0))
			this.isAttachUID = true;

		/* Read the properties if found */
		Properties propertyFile = loadPropertiesFromFilePath(PROPERTIES_FILE_PATH);
		if (propertyFile != null) {
			String isEnabled = propertyFile.getProperty(PROTOCOL_PROPERTY,
					"false");
			if (isEnabled.compareToIgnoreCase("true") == 0)
				this.protocolUsed = protocolHTTPS;
		}

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

				/*
				 * Validate URL to make sure that we are the correct URL when
				 * the IFrame was configured for localhost
				 */
				this.urlToRender = (this.configuredURL.startsWith("/") ? this.configuredURL
						: validateURL(this.configuredURL, serverNameInURL));
				setRenderAttributes(request);
				response.setContentType("text/html");
				PortletRequestDispatcher prd = getPortletContext()
						.getRequestDispatcher("/jsp/iframe.jsp");
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

	public void doEdit(RenderRequest request, RenderResponse response)
			throws IOException, PortletException {
		setRenderAttributes(request);
		response.setContentType("text/html");
		response.setTitle("Edit");
		PortletRequestDispatcher prd = getPortletContext()
				.getRequestDispatcher("/jsp/edit.jsp");
		prd.include(request, response);
	}

	protected void setRenderAttributes(RenderRequest request) {
		try {
			PortletPreferences prefs = FacesUtils.getAllPreferences(request,
					true);

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
				}
			}

			request.setAttribute("iframeurl",
					prefs.getValue("iframeurl", updURL.toString()));
			request.setAttribute("iframeheight",
					prefs.getValue("iframeheight", this.defaultHeight));
			request.setAttribute("iframewidth",
					prefs.getValue("iframewidth", this.defaultWidth));
			request.setAttribute("iframemessage", prefs.getValue(
					"iframemessage", this.defaultNonIFrameMessage));

			request.setAttribute("openinwindow", this.isOpenInWindow);
			if (allowRemoteURL != null)
				request.setAttribute("allow_remote_url", allowRemoteURL);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected String validateURL(String userURL, String serverName)
			throws PortletException {
		String validatedURL = null;
		URL incomingURL = null;

		try {
			incomingURL = new URL(userURL);

		} catch (MalformedURLException me) {
			throw new PortletException(
					"GWOSIFramePortlet is incorrectly configured. Init parameter "
							+ PARAM_URL + " is malformed: " + userURL);
		}

		try {
			String URLHost = incomingURL.getHost();
			if ((URLHost.compareToIgnoreCase("localhost") == 0)
					|| (URLHost.contains("127.0.0") == true)) {

				/* First try to use the value passed in the session */
				if (serverName != null && serverName.length() > 0) {
					hostName = serverName;
				} else {
					// First try to get the FQDN from the JPP configuration.properties with
					// a fallback to getting it from localhost hostname. Getting hostname from the 
					// OS doesnt help especially in case of amazon EC2 which has public-hostname and local-hostname
					String portalURL = PropertyUtils.loadPropertiesFromFilePath(GWOSIFramePortlet.JPP_CONFIG).getProperty("gatein.sso.portal.url");
					if (portalURL != null) {
						hostName = portalURL.substring(portalURL.lastIndexOf("/") + 1);
						try {
							getHttpServletRequest().getSession().setAttribute("serverOfRequestURL",hostName);
						}
						catch (Exception e) {
						/* Should not happen in correct setup */
							System.err
									.println("Failed to set hostname in serverOfRequestURL");
						}
					}
					else {
						hostName = InetAddress.getLocalHost().getHostName();
					}
				}
				/*
				 * Fix the URL if the API returns localhost.locadomain for the
				 * hostname
				 */
				if (hostName != null
						&& hostName.indexOf("localhost.localdomain") != -1)
					hostName = "localhost";

				// String protocol = incomingURL.getProtocol();
				String path = incomingURL.getPath();
				String query = incomingURL.getQuery();

				StringBuilder newURL = new StringBuilder(protocolUsed)
						.append("://").append(this.hostName).append(path);
				if (query != null)
					newURL.append("?").append(query);

				validatedURL = newURL.toString();
			} else {
				validatedURL = userURL;
			}

			// Return validated URL
			return validatedURL;

		} catch (UnknownHostException he) {
			throw new PortletException(
					"GWOSIFramePortlet cannot determine host name.");
		}
	}

	@Override
	protected String getTitle(RenderRequest request) {
		try {
			PortletPreferences prefs = FacesUtils.getAllPreferences(request,
					true);
			return (prefs == null ? super.getTitle(request) : prefs.getValue(
					"title", super.getTitle(request)));
		} catch (Exception e) {
			return super.getTitle(request);
		}
	}

	/* Get the http session to the user */
	protected HttpServletRequest getHttpServletRequest()
			throws PolicyContextException {
		HttpServletRequest request = (HttpServletRequest) PolicyContext
				.getContext("javax.servlet.http.HttpServletRequest");
		return request;
	}

	/**
	 * Loads properties from file path.
	 * 
	 * @param filePath
	 * @return Properties
	 */
	public static Properties loadPropertiesFromFilePath(String filePath) {
		FileInputStream defaultFS = null;
		Properties defaultProps = new Properties();
		try {
			defaultFS = new FileInputStream(filePath);
			defaultProps.load(defaultFS);
		} catch (Exception e) {
			logger.info("Unable to find properties file [" + filePath
					+ "]. Using default");
		} finally {
			try {
				if (defaultFS != null) {
					defaultFS.close();
				}
			} catch (IOException ioe) {
				logger.warn("Unable to close the input stream for properties file ["
						+ filePath + "]. Exception is - " + ioe.getMessage());
			}
		}
		return defaultProps;
	}
}