/*
 * 
 * Copyright 2010 GroundWork Open Source, Inc. ("GroundWork") All rights
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

package com.groundworkopensource.portal.zendesk.portlet;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import javax.crypto.SecretKey;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.PortletSession;
import javax.portlet.GenericPortlet;


import org.apache.log4j.Logger;

import com.groundworkopensource.portal.common.DesEncrypter;
import com.groundworkopensource.portal.zendesk.bean.ZendeskIntegrationBean;

/**
 * This portlet display Zendesk related informations
 * 
 * @author manish_kjain
 * 
 */
public class ZendeskIntegrationPortlet extends GenericPortlet {

	/**
	 * Empty String.
	 */
	public static final String EMPTY_STRING = "";

	/**
	 * logger
	 */
	Logger LOGGER = Logger.getLogger(ZendeskIntegrationPortlet.class.getName());

	/**
	 * ZENDESK_INTEGRATION_PORTLET_TITLE.
	 */
	private static final String ZENDESK_INTEGRATION_PORTLET_TITLE = "zendesk integration";

	private static final String ZENDESK_PROPERTY_FILE = "/usr/local/groundwork/config/zendesk.properties";
	private static final String ZENDESK_URL_PROPERTY = "zendesk.helpdesk.url";
	
	private static final String ZENDESK_URL = "ZenDeskServer";
	private static final String ZENDESK_ADMIN_USER = "ZenDeskUser";
	private static final String ZENDESK_PASS = "ZenDeskPass";
	private static final String ZENDESK_TOKEN = "ZenDeskToken";
	private static final String GWRK_SERVER_NAME = "GroundworkServer";
	
	/**
	 * (non-Javadoc)
	 * 
	 * @see javax.portlet.GenericPortlet#processAction(javax.portlet.ActionRequest,
	 *      javax.portlet.ActionResponse)
	 */
	@Override
	public void processAction(ActionRequest request, ActionResponse response)
			throws PortletException, IOException {
		// get the portlet preferences
		PortletPreferences pref = request.getPreferences();
		// iterate through reqPrefParamMap to get reqParam and prefKey

		// retrieve from request and set into preferences
		String userId = request.getParameter("useridPref");
		String pwd = request.getParameter("pwdPref");
		String url = request.getParameter("zendeskUrlPref");
		
		String token = request.getParameter("zendeskToken");

		pref.setValue("useridPref", (String) userId);
		pref.setValue("pwdPref", (String) pwd);
		pref.setValue("zendeskUrlPref", (String) url);
		pref.setValue("zendeskToken", (String) token);
		
		// store preferences
		pref.store();

		storePrefsInFile(userId, pwd/*encryptedPwd*/, url, token);
		
		// set the portlet mode to VIEW
		response.setPortletMode(PortletMode.VIEW);
	}

	private void storeServerNameInPrefsFile(Object serverName) {
		// Read properties file.
		Properties properties = new Properties();

		try {
			properties.load(new FileInputStream(ZENDESK_PROPERTY_FILE));
		} catch (IOException e) {
			LOGGER
					.error("cannot access zendesk.properties file to write zendesk preferences.");
		}
		Enumeration<Object> keys = properties.keys();
		
		properties.put(GWRK_SERVER_NAME, (String) "http://" + serverName);

		// Write properties file.
		try {
			properties.store(new FileOutputStream(ZENDESK_PROPERTY_FILE), null);
		} catch (IOException e) {
			LOGGER
					.error("error occured while writing zendesk.properties file to disc.");
		}		
	}
	/**
	 * @param userId
	 * @param pwd
	 * @param url
	 */
	private void storePrefsInFile(Object userId, Object pwd, Object url, String token) {
		// save to preference config file zendesk.properties which will be in
		// groundwork/config directory

		// Read properties file.
		Properties properties = new Properties();

		try {
			properties.load(new FileInputStream(ZENDESK_PROPERTY_FILE));
		} catch (IOException e) {
			LOGGER
					.error("cannot access zendesk.properties file to write zendesk preferences.");
		}
		Enumeration<Object> keys = properties.keys();
		
		properties.put(ZENDESK_ADMIN_USER, (String) userId);
		properties.put(ZENDESK_URL, (String) url);
		properties.put(ZENDESK_PASS, (String) pwd);
//		properties.put(ZENDESK_TOKEN, (String) token);

		// Write properties file.
		try {
			properties.store(new FileOutputStream(ZENDESK_PROPERTY_FILE), null);
		} catch (IOException e) {
			LOGGER
					.error("error occured while writing zendesk.properties file to disc.");
		}
	}

	/**
	 * @param request
	 * @param response
	 * @throws PortletException
	 * @throws IOException
	 */
	@Override
	protected void doView(RenderRequest request, RenderResponse response)
			throws PortletException, IOException {
		//super.setViewPath("/jsp/zendeskIntegration.jsp");
		String serverNameInURL = null;
		
		try {
    		serverNameInURL = (String)getHttpServletRequest().getSession().getAttribute("serverOfRequestURL");
    	}
    	catch (Exception e)
    	{
    		/*Should not happen in correct setup */
    		System.out.println("Failed getting http request or Attribute serverOfRequestURL is not defined");
    	}
    	
    	if ( serverNameInURL != null && serverNameInURL.length() > 0)
    	{
    		storeServerNameInPrefsFile(serverNameInURL);
    	}
    	
		PortletPreferences pref = request.getPreferences();
		String useridPref = pref.getValue("useridPref", EMPTY_STRING);
		String pwdPref = pref.getValue("pwdPref", EMPTY_STRING);
		String zendeskUrlPref = pref.getValue("zendeskUrlPref", EMPTY_STRING);
		String zendeskToken = pref.getValue("zendeskToken", EMPTY_STRING);
		if (null == useridPref || EMPTY_STRING.equals(useridPref.trim())
				|| null == pwdPref || EMPTY_STRING.equals(pwdPref.trim())
				|| null == zendeskToken || EMPTY_STRING.equals(zendeskToken.trim())
				|| null == zendeskUrlPref
				|| EMPTY_STRING.equals(zendeskUrlPref.trim())) {
			this.doEdit(request, response);
			return;
		}

		// Set the portlet title.
		response.setTitle("Zendesk Integration");		
		/*
		PortletPreferences prefs = request.getPreferences();
		String userId = prefs.getValue("useridPref", EMPTY_STRING);
		String zendeskLoginUrl = prefs.getValue("zendeskUrlPref", EMPTY_STRING);
		String token = prefs.getValue("zendeskToken", EMPTY_STRING);*/
		// Get the zenURL from the bean and set it in the portlet request
		ZendeskIntegrationBean zenBean = new ZendeskIntegrationBean(useridPref,zendeskToken,zendeskUrlPref);
		request.setAttribute("zenURL",zenBean.getZenURL());
		//super.doView(request, response);
		PortletContext ctxt = getPortletContext();
		PortletRequestDispatcher disp = ctxt
				.getRequestDispatcher("/jsp/zendeskIntegration.jsp");
		response.setContentType("text/html");
		disp.include(request, response);
	}

	/**
	 * This method is Responsible for editing preferences of host statistics
	 * portlet
	 * 
	 * @param request
	 * @param response
	 * @throws PortletException
	 * @throws IOException
	 */
	protected void doEdit(RenderRequest request, RenderResponse response)
			throws PortletException, IOException {
		response.setTitle("Edit zendesk integration Preferences");
		PortletPreferences pref = request.getPreferences();
		String useridPref = pref.getValue("useridPref", EMPTY_STRING);
		String pwdPref = pref.getValue("pwdPref", EMPTY_STRING);
		String zendeskUrlPref = pref.getValue("zendeskUrlPref", EMPTY_STRING);
		String zendeskToken = pref.getValue("zendeskToken", EMPTY_STRING);

		if (zendeskUrlPref.length() == 0) {
			zendeskUrlPref = getProperty(ZENDESK_URL_PROPERTY);
		}

		
		request.setAttribute("useridPref", useridPref);
		request.setAttribute("pwdPref", pwdPref);
		request.setAttribute("zendeskUrlPref", zendeskUrlPref);
		request.setAttribute("zendeskToken", zendeskToken);

		PortletContext ctxt = getPortletContext();
		PortletRequestDispatcher disp = ctxt
				.getRequestDispatcher("/jsp/zendeskIntegrationPref.jsp");
		response.setContentType("text/html");
		disp.include(request, response);

	}

	/**
	 * Helper to get properties for this portlet application
	 * 
	 * @param propertyName
	 * @return String property value or empty string if not found
	 */
	private String getProperty(String propertyName) {
		String result = null;
		Properties properties = new Properties();

		try {
			properties.load(new FileInputStream(ZENDESK_PROPERTY_FILE));

			result = properties.getProperty(propertyName);

		} catch (IOException e) {
			LOGGER
					.error("cannot access zendesk.properties file to read zendesk preferences.");
		}

		/* If property is not found result will be null */
		if (result == null)
			return EMPTY_STRING;
		else
			return result;
	}
	
	/* Get the http session to the user */ 
    protected HttpServletRequest getHttpServletRequest() throws PolicyContextException {
    	HttpServletRequest request = (HttpServletRequest) PolicyContext
    	.getContext("javax.servlet.http.HttpServletRequest");
    	return request;
    	}
	}
