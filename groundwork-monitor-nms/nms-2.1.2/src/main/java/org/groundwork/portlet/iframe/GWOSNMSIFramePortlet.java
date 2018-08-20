/*
 * JBoss, Home of Professional Open Source Copyright 2009, JBoss Inc., and
 * individual contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of individual
 * contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

/*
 * Extended JBoss IFrame portlet with additional functionality and enhancements
 * such as: 1) Define URL as part of the portlet Init param 2) Modify URL if url
 * includes localhost or 127.0.0. with the real hostname 3) INIT param LINK
 * {true|false} to turn on link open in new Window. Default false 4) Added new
 * INIT param ATTACH_UID default false that enables the user name to be attached
 * to the url example: gwuid=admin
 * 
 * Author: Roger Ruttimann (rruttimann@gwos.com) GroundWOrk Open Source Inc.
 */

package org.groundwork.portlet.iframe;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Properties;

import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.portlet.iframe.JBossIFramePortlet;

/**
 * @author Roger Ruttimann (rruttimann@gwos.com) GroundWOrk Open Source Inc.
 * 
 */
public class GWOSNMSIFramePortlet extends JBossIFramePortlet {

	/* INIT Parameter defining the URL */
	private static final String PARAM_URL = "URL";

	/* INIT Param to activate link to open URL in sepaarate Window */
	private static final String PARAM_WINDOW = "LINK";

	/* INIT Param to include UID to URL */
	private static final String PARAM_UID = "ATTACH_UID";

	private static final String defaultHeight = "2000px";
	private static final String defaultWidth = "100%";
	private static final String defaultNonIFrameMessage = "Your browser does not support iframes";

	/* Host name to be used to replace localhost */
	private String hostName;

	/* URL for IFrame set default to GroundWork */
	private String urlToRender = "http://www.groundworkopensource.com";

	/* Keep track of configured URL since it might change doing doView */
	private String configuredURL = urlToRender;

	/* By default don't show link. Can be overwritten bi unit param */
	private String isOpenInWindow = "false";

	/* By default user ID is not attached to the URL as query string */
	private boolean isAttachUID = false;

	/* INIT Parameter defining the NMS_TYPE */
	private static final String NMS_TYPE = "NMS_TYPE";
	private String nmsTypeValue = "";

	/* Falg that indicates if the feature has been configured */
	private boolean bNMSConfigured = true;
	
	   /* Default protocol for IFrame is http but it can be changed with a property setting */
    private String protocolUsed = "http";
    
    private static final String protocolHTTPS = "https";
    private static final int portHTTPS = 444;
    private boolean bIsSecurePortEnabled = false;
    
    private static final String PROTOCOL_PROPERTY = "secure.access.enabled";
    /**
	 * Properties file
	 */
	private static final String PROPERTIES_FILE_PATH = "/usr/local/groundwork/config/status-viewer.properties";


	/**
	 * ENTERPRISE_PROPERTIES_PATH
	 */
	private static final String ENTERPRISE_PROPERTIES_PATH = "/usr/local/groundwork/enterprise/config/enterprise.properties";
	private static final String ENTERPRISE_PROPERTIES_ALTERNATE_PATH = "/usr/local/groundwork/config/enterprise.properties";
	/**
	 * enterprise Properties
	 */
	private static Properties enterpriseProperties;

	// NMS Default
	private static final String NMS_PORT_DEFAULT = "nms.httpd.httpd_main.port";
	private static final String NMS_HOST_DEFAULT = "nms.httpd.httpd_main.host";

	// Nedi
	/**
	 * NMS Nedi type.
	 */
	private static final String NMS_NEDI = "nedi";
	private static final String NEDI_PORT = "nms.nedi.nedi_main.port";
	private static final String NEDI_HOST = "nms.nedi.nedi_main.host";
	private static final String NEDI_COOKIE_NAME = "nedi_auth_tkt";

	// Cacti
	/**
	 * NMS Cacti type.
	 */
	private static final String NMS_CACTI = "cacti";
	private static final String CACTI_PORT = "nms.cacti.cacti_main.port";
	private static final String CACTI_HOST = "nms.cacti.cacti_main.host";
	private static final String CACTI_COOKIE_NAME = "cacti_auth_tkt";

	// Ntop
	/**
	 * NMS Ntop type.
	 */
	private static final String NMS_NTOP = "ntop";
	private static final String NTOP_PORT = "nms.ntop.ntop_main.port";
	private static final String NTOP_HOST = "nms.ntop.ntop_main.host";
	private static final String NTOP_COOKIE_NAME = "ntop_auth_tkt";

	// Weathermap
	/**
	 * NMS Weathermap type.
	 */
	private static final String NMS_WATHERMAP = "weathermap";
	private static final String WATHERMAP_PORT = "nms.weathermap.weathermap_main.port";
	private static final String WATHERMAP_HOST = "nms.weathermap.weathermap_main.host";
	private static final String WEATHERMAP_COOKIE_NAME = "weathermap_auth_tkt";
	/**
	 * WATHERMAP END URL
	 */
	private static final String WATHERMAP_END_URL = "cacti/plugins/weathermap/editor.php";

	// Common
	private static final String EMPTY = "";
	private static final String HTTP_COLON = "http://";
	private static final String COLON = ":";
	private static final String SLASH = "/";

	/**
	 * Logging
	 */
	private static Log logger = LogFactory.getLog(GWOSNMSIFramePortlet.class);

	/**
	 * Default constructor
	 */
	public GWOSNMSIFramePortlet() {

	}

	/**
	 * Init phase of the portlet. Using it to read INIT params defined in the
	 * portlet.xml
	 * 
	 * @param config
	 * @throws PortletException
	 */
	@Override
	public void init(PortletConfig config) throws PortletException {
		super.init(config);

		// Get the INIT PARAMETERS for this portlet. If the values are missing
		// throw an exception
		nmsTypeValue = config.getInitParameter(NMS_TYPE);
		isOpenInWindow = config.getInitParameter(PARAM_WINDOW);

		// set the configured URL for NMS as per the NMS-Type
		// load the enterprise properties file
		enterpriseProperties = loadPropertiesFromFilePath(ENTERPRISE_PROPERTIES_PATH);
		if (enterpriseProperties != null) {
			/* Read the values and set the environment */
			setURLAsPerNMSType(nmsTypeValue);
		}
		else
		{
			// Try alternate path before giving up
			enterpriseProperties = loadPropertiesFromFilePath(ENTERPRISE_PROPERTIES_ALTERNATE_PATH);
			
			if (null == enterpriseProperties || enterpriseProperties.isEmpty()) {
				bNMSConfigured = false;
			}
			else
			{
				/* Read the values and set the environment */
				setURLAsPerNMSType(nmsTypeValue);
			}
		}

		/* Set the default value if it is not defined */
		if (this.isOpenInWindow == null || this.isOpenInWindow.length() == 0)
			this.isOpenInWindow = "false";

		String attachUID = config.getInitParameter(PARAM_UID);
		/* Set the default value if it is not defined */
		if (attachUID != null && (attachUID.compareToIgnoreCase("true") == 0))
			this.isAttachUID = true;
		
		/* Read the properties if found */
		
		if (nmsTypeValue.equalsIgnoreCase(NMS_NTOP)) {
			logger.info("NTOP is configured for HTTP only");
		}
		else
		{
		    Properties propertyFile = loadPropertiesFromFilePath(PROPERTIES_FILE_PATH);
			if (propertyFile != null) {
				String isEnabled = propertyFile.getProperty(PROTOCOL_PROPERTY,"false");
				if (isEnabled.compareToIgnoreCase("true") == 0) {
					this.protocolUsed = protocolHTTPS;
					this.bIsSecurePortEnabled = true;
				}
			}
		}

	}

	/**
	 * Computes Configured URL as per the NMS Type (as per the NMS Portlet -
	 * nedi / cacti / weathermap / ntop)
	 * 
	 * @param nmsType
	 */
	private void setURLAsPerNMSType(String nmsType) {
		if (nmsType.equalsIgnoreCase(NMS_NEDI)) {

			String host = enterpriseProperties.getProperty(NEDI_HOST);

			/* No entry in enterprise.properties */
			if (null == host || host.equals(EMPTY)) {
				this.bNMSConfigured = false;
			} else {
				String port = enterpriseProperties.getProperty(NEDI_PORT);
				// If not defined use default port
				if (null == port || port.equals(EMPTY)) {
					port = enterpriseProperties.getProperty(NMS_PORT_DEFAULT);

				}

				configuredURL = new StringBuilder(HTTP_COLON).append(host)
						.append(COLON).append(port).append(SLASH).append(
								NMS_NEDI).toString();
			}
		} else if (nmsType.equalsIgnoreCase(NMS_CACTI)) {
			String host = enterpriseProperties.getProperty(CACTI_HOST);
			if (null == host || host.equals(EMPTY)) {
				this.bNMSConfigured = false;
			} else {
				String port = enterpriseProperties.getProperty(CACTI_PORT);
				if (null == port || port.equals(EMPTY)) {
					port = enterpriseProperties.getProperty(NMS_PORT_DEFAULT);
				}
				configuredURL = new StringBuilder(HTTP_COLON).append(host)
						.append(COLON).append(port).append(SLASH).append(
								NMS_CACTI).toString();
			}

		} else if (nmsType.equalsIgnoreCase(NMS_NTOP)) {
			String host = enterpriseProperties.getProperty(NTOP_HOST);
			if (null == host || host.equals(EMPTY)) {
				this.bNMSConfigured = false;
			} else {
				String port = enterpriseProperties.getProperty(NTOP_PORT);
				if (null == port || port.equals(EMPTY)) {
					port = enterpriseProperties.getProperty(NMS_PORT_DEFAULT);
				}
				configuredURL = new StringBuilder(HTTP_COLON).append(host)
						.append(COLON).append(port).toString();
			}

		} else if (nmsType.equalsIgnoreCase(NMS_WATHERMAP)) {
			// Weathermap URL example -
			// http://boudry:81/cacti/plugins/weathermap/editor.php
			String host = enterpriseProperties.getProperty(WATHERMAP_HOST);
			if (null == host || host.equals(EMPTY)) {
				this.bNMSConfigured = false;
			} else {
				String port = enterpriseProperties.getProperty(WATHERMAP_PORT);
				if (null == port || port.equals(EMPTY)) {
					port = enterpriseProperties.getProperty(NMS_PORT_DEFAULT);
				}
				configuredURL = new StringBuilder(HTTP_COLON).append(host)
						.append(COLON).append(port).append(SLASH).append(
								WATHERMAP_END_URL).toString();
			}
		}

		// logger.error(Configured URL : " + configuredURL);
	}

	@Override
	public void doView(RenderRequest request, RenderResponse response) {
		try {
			if (bNMSConfigured == false) {
				/* Display default Page */
				response.setContentType("text/html");
				PortletRequestDispatcher prd = getPortletContext()
						.getRequestDispatcher("/jsp/default.jsp");
				prd.include(request, response);			
				}else {
				try {

					/* Set the Cookie */
					if (nmsTypeValue.compareToIgnoreCase(NMS_CACTI) == 0) {
						response.addProperty(new Cookie(CACTI_COOKIE_NAME,
								SLASH + NMS_CACTI));
					} else if (nmsTypeValue.compareToIgnoreCase(NMS_NEDI) == 0) {
						response.addProperty(new Cookie(NEDI_COOKIE_NAME, SLASH
								+ NMS_NEDI));
					} else if (nmsTypeValue.compareToIgnoreCase(NMS_NTOP) == 0) {
						response.addProperty(new Cookie(NTOP_COOKIE_NAME, SLASH
								+ NMS_NTOP));
					} else if (nmsTypeValue.compareToIgnoreCase(NMS_WATHERMAP) == 0) {
						response.addProperty(new Cookie(WEATHERMAP_COOKIE_NAME,
								SLASH + NMS_WATHERMAP));
					}
				} catch (Exception e) {
					logger.error("Failed setting cookie in http response");
				}

				/*
				 * Jboss portal doesn't make it easy to get access to a HTTP
				 * session
				 */
				String serverNameInURL = null;
				try {
					serverNameInURL = (String) getHttpServletRequest()
							.getSession().getAttribute("serverOfRequestURL");
				} catch (PolicyContextException pce) {
					/* Should not happen in correct setup */
					logger
							.error("Failed getting http request or Attribute serverOfRequestURL is not defined");
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
			e.printStackTrace();
		}
	}

	@Override
	public void doEdit(RenderRequest request, RenderResponse response)
			throws IOException, PortletException {
		setRenderAttributes(request);
		response.setContentType("text/html");
		response.setTitle("Edit");
		PortletRequestDispatcher prd = getPortletContext()
				.getRequestDispatcher("/jsp/edit.jsp");
		prd.include(request, response);
	}

	private void setRenderAttributes(RenderRequest request) {
		PortletPreferences prefs = request.getPreferences();

		StringBuffer updURL = new StringBuffer(this.urlToRender);

		/*
		 * Check if the IFRame URL needs to have the GroundWork UID attached to
		 * the URL
		 */
		if (this.isAttachUID == true) {
			String uid = request.getUserPrincipal().getName();

			if (this.urlToRender.indexOf('?') == -1) {
				updURL.append("?gwuid=").append(uid);
			} else {
				updURL.append("&gwuid=").append(uid);
			}
		}

		request.setAttribute("iframeurl", prefs.getValue("iframeurl", updURL
				.toString()));
		request.setAttribute("iframeheight", prefs.getValue("iframeheight",
				defaultHeight));
		request.setAttribute("iframewidth", prefs.getValue("iframewidth",
				defaultWidth));
		request.setAttribute("iframemessage", prefs.getValue("iframemessage",
				defaultNonIFrameMessage));

		request.setAttribute("openinwindow", this.isOpenInWindow);
	}

	private String validateURL(String userURL, String serverName)
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
					// get the local hostname just in case the URL is incomplete
					// or
					// points to localhost
					hostName = InetAddress.getLocalHost().getHostName();
				}
				/*
				 * Fix the URL if the API returns localhost.locadomain for the
				 * hostname
				 */
				if (hostName != null
						&& hostName.indexOf("localhost.localdomain") != -1)
					hostName = "localhost";
			}
			else
			{
				hostName = URLHost;
			}

			//String protocol = incomingURL.getProtocol();
			int port = incomingURL.getPort();
			
			String path = incomingURL.getPath();
			String query = incomingURL.getQuery();

			StringBuilder newURL = new StringBuilder(protocolUsed)
					.append("://").append(this.hostName).append(":").append(port).append(path);
			if (query != null)
				newURL.append("?").append(query);

			validatedURL = newURL.toString();
			

			// Return validated URL
			return validatedURL;

		} catch (UnknownHostException he) {
			throw new PortletException(
					"GWOSIFramePortlet cannot determine host name.");
		}
	}

	@Override
	protected String getTitle(RenderRequest request) {
		PortletPreferences prefs = request.getPreferences();
		return (prefs == null ? super.getTitle(request) : prefs.getValue(
				"title", super.getTitle(request)));
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
			logger.error("Unable to find properties file [" + filePath + "]");
		} finally {
			try {
				if (defaultFS != null) {
					defaultFS.close();
				}
			} catch (IOException ioe) {
				logger
						.error("Unable to close the input stream for properties file ["
								+ filePath
								+ "]. Exception is - "
								+ ioe.getMessage());
			}
		}
		return defaultProps;
	}

}
