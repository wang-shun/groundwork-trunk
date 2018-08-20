/*
 * 
 * Copyright 2007 GroundWork Open Source, Inc. ("GroundWork") All rights
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
package com.groundworkopensource.portal.common.ws.impl;

import java.util.Properties;

import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.impl.WSCategoryServiceLocator;
import org.groundwork.foundation.ws.impl.WSCommonServiceLocator;
import org.groundwork.foundation.ws.impl.WSEventServiceLocator;
import org.groundwork.foundation.ws.impl.WSHostGroupServiceLocator;
import org.groundwork.foundation.ws.impl.WSHostServiceLocator;
import org.groundwork.foundation.ws.impl.WSRRDServiceLocator;
import org.groundwork.foundation.ws.impl.WSServiceServiceLocator;
import org.groundwork.foundation.ws.impl.WSStatisticsServiceLocator;

import com.groundworkopensource.portal.common.ApplicationType;
import com.groundworkopensource.portal.common.CommonConstants;
import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.common.PropertyUtils;
import com.groundworkopensource.portal.common.ws.Constants;

/**
 * This class is used to locate and bind foundation web service end point. <br>
 * 
 * Important: Please note that application should specify Web Service end point
 * either in application specific properties file or in web.xml file. Key for
 * Web Service URL end point parameter must be - "foundation.webservice.url".
 * 
 * @author rashmi_tambe
 */
public final class WebServiceLocator {

	/**
	 * singleton instance.
	 */
	private static WebServiceLocator locator;

	/**
	 * singleton instance of hostgroup web service.
	 */
	private WSHostGroupServiceLocator hostGroupLocator;

	/**
	 * singleton instance of host web service.
	 */
	private WSHostServiceLocator hostLocator;

	/**
	 * singleton instance of wsevent web service.
	 */
	private WSEventServiceLocator eventServiceLocator;

	/**
	 * singleton instance of wsservice web service.
	 */
	private WSServiceServiceLocator wsserviceLocator;

	/**
	 * singleton instance of statistics web service.
	 */
	private WSStatisticsServiceLocator statisticsLocator;

	/**
	 * singleton instance of Category web service.
	 */
	private WSCategoryServiceLocator categoryServiceLocator;

	/**
	 * singleton instance of common web service.
	 */
	private WSCommonServiceLocator commonServiceLocator;
	/**
	 * singleton instance of RRD web service.
	 */
	private WSRRDServiceLocator rrdLocator;

	/**
	 * Foundation web service URL. E.g.
	 * http://localhost:8080/foundation-webapp/services/
	 */
	private static String foundationURL;

	/**
	 * RESTeasy service URL. E.g. http://localhost:8080/rest/
	 */
	private static String portalExtnRESTeasyURL;

	/**
	 * Logger.
	 */
	private static Logger logger = Logger.getLogger(WebServiceLocator.class
			.getName());

	/**
	 * Static initializer that gets the foundation web service URL value.
	 */
	static {
		try {
			// read application type from web.xml
			String appType = FacesUtils
					.getContextParam(CommonConstants.APPLICATION_TYPE_CONTEXT_PARAM_NAME);
			ApplicationType applicationType = ApplicationType
					.getApplicationType(appType);
			// read foundation URL from application specific properties file
			foundationURL = PropertyUtils.getProperty(applicationType,
					CommonConstants.FOUNDATION_WS_URL_KEY);
			logger.debug("Foundation URL from default properties file : "
					+ foundationURL);
			// if not found, then try to take it from web.xml
			if (null == foundationURL) {
				foundationURL = FacesUtils
						.getContextParam(CommonConstants.FOUNDATION_WS_URL_KEY);
				logger.debug("Foundation URL from web.xml : " + foundationURL);
			}

			// read resteasy URL from application specific properties file
			portalExtnRESTeasyURL = PropertyUtils.getProperty(applicationType,
					CommonConstants.PORTAL_EXTN_RESTEASY_URL_KEY);
			logger.debug("portal extension resteasy URL from default properties file : "
					+ portalExtnRESTeasyURL);
			// if not found, then try to take it from web.xml
			if (null == portalExtnRESTeasyURL) {
				portalExtnRESTeasyURL = FacesUtils
						.getContextParam(CommonConstants.PORTAL_EXTN_RESTEASY_URL_KEY);
				logger.debug("portal extension resteasy URL from web.xml : "
						+ portalExtnRESTeasyURL);
			}
		} catch (Exception e) {
			/*
			 * Note: This exception handling has done temporarily to be able to
			 * run test cases from Build++ and Cruise Control
			 */
			Properties propertices = PropertyUtils
					.loadPropertiesFromFilePath(ApplicationType.STATUS_VIEWER
							.getDefaultPropertiesPath());
			foundationURL = propertices
					.getProperty(CommonConstants.FOUNDATION_WS_URL_KEY);
			logger.debug("Foundation URL for Build++ : " + foundationURL);
			portalExtnRESTeasyURL = propertices
					.getProperty(CommonConstants.PORTAL_EXTN_RESTEASY_URL_KEY);
			logger.debug("portal extension resteasy URL for Build++ : "
					+ portalExtnRESTeasyURL);
		}

		logger.info("Using Foundation Web Service URL " + " ==> "
				+ foundationURL);
		logger.info("portal extension resteasy URL " + " ==> "
				+ portalExtnRESTeasyURL);
	}

	/**
	 * Private Constructor
	 */
	private WebServiceLocator() {
		// Private Constructor
	}

	/**
	 * return the singleton instance of WebServiceLocator.
	 * 
	 * @return instance of WebServiceLocator
	 */
	public static WebServiceLocator getInstance() {
		if (locator == null) {
			locator = new WebServiceLocator();
		}

		return locator;
	}

	/**
	 * Returns the "host" (wshost) web service locator.
	 * 
	 * @return "host" (wshost) web service locator
	 * @throws ServiceException
	 */
	public WSHostServiceLocator hostLocator() throws ServiceException {
		if (hostLocator == null) {
			hostLocator = new WSHostServiceLocator();
			hostLocator.setEndpointAddress(Constants.FOUNDATION_END_POINT_HOST,
					foundationURL + Constants.FOUNDATION_END_POINT_HOST);
		}

		return hostLocator;
	}

	/**
	 * Returns the "host group" (wshostgroup) web service locator.
	 * 
	 * @return "host group" (wshostgroup) web service locator
	 * @throws ServiceException
	 */
	public WSHostGroupServiceLocator hostGroupLocator() throws ServiceException {
		if (hostGroupLocator == null) {
			hostGroupLocator = new WSHostGroupServiceLocator();
			hostGroupLocator.setEndpointAddress(
					Constants.FOUNDATION_END_POINT_HOST_GROUP, foundationURL
							+ Constants.FOUNDATION_END_POINT_HOST_GROUP);
		}

		return hostGroupLocator;
	}

	/**
	 * Returns the "service" (wsservice) web service locator.
	 * 
	 * @return "service" (wsservice) web service locator
	 * @throws ServiceException
	 */
	public WSServiceServiceLocator serviceLocator() throws ServiceException {
		if (wsserviceLocator == null) {
			wsserviceLocator = new WSServiceServiceLocator();
			wsserviceLocator.setEndpointAddress(
					Constants.FOUNDATION_END_POINT_SERVICE, foundationURL
							+ Constants.FOUNDATION_END_POINT_SERVICE);
		}

		return wsserviceLocator;
	}

	/**
	 * Returns the "statistics" (wsstatistics) web service locator.
	 * 
	 * @return "statistics" (wsstatistics) web service locator
	 * @throws ServiceException
	 */
	public WSStatisticsServiceLocator statisticsLocator()
			throws ServiceException {
		if (statisticsLocator == null) {
			statisticsLocator = new WSStatisticsServiceLocator();
			statisticsLocator.setEndpointAddress(
					Constants.FOUNDATION_END_POINT_STATISTICS, foundationURL
							+ Constants.FOUNDATION_END_POINT_STATISTICS);
		}

		return statisticsLocator;
	}

	/**
	 * Returns the "event" (wsevent) web service locator.
	 * 
	 * @return "event" (wsevent) web service locator.
	 * @throws ServiceException
	 */
	public WSEventServiceLocator eventLocator() throws ServiceException {
		if (eventServiceLocator == null) {
			eventServiceLocator = new WSEventServiceLocator();
			eventServiceLocator.setEndpointAddress(
					Constants.FOUNDATION_END_POINT_EVENT, foundationURL
							+ Constants.FOUNDATION_END_POINT_EVENT);
		}

		return eventServiceLocator;
	}

	/**
	 * Returns the "Service Group" (Category => wscategory) web service locator.
	 * 
	 * @return "Service Group" (Category => wscategory) web service locator.
	 * @throws ServiceException
	 */
	public WSCategoryServiceLocator serviceGroupLocator()
			throws ServiceException {
		if (categoryServiceLocator == null) {
			categoryServiceLocator = new WSCategoryServiceLocator();
			categoryServiceLocator.setEndpointAddress(
					Constants.FOUNDATION_END_POINT_CATEGORY, foundationURL
							+ Constants.FOUNDATION_END_POINT_CATEGORY);
		}

		return categoryServiceLocator;
	}

	/**
	 * Returns the "Commons" (Category => wscommon) web service locator.
	 * 
	 * @return "Commons" (Category => wscommon) web service locator.
	 * @throws ServiceException
	 */
	public WSCommonServiceLocator commonServiceLocator()
			throws ServiceException {

		if (commonServiceLocator == null) {
			commonServiceLocator = new WSCommonServiceLocator();
			commonServiceLocator.setEndpointAddress(
					Constants.FOUNDATION_END_POINT_COMMON, foundationURL
							+ Constants.FOUNDATION_END_POINT_COMMON);
		}
		return commonServiceLocator;

	}

	/**
	 * Returns the "RRD" (Category => wsrrd) web service locator.
	 * 
	 * @return "RRD" (Category => wsrrd) web service locator.
	 * @throws ServiceException
	 */
	public WSRRDServiceLocator rrdServiceLocator() throws ServiceException {

		if (rrdLocator == null) {
			rrdLocator = new WSRRDServiceLocator();
			rrdLocator.setEndpointAddress(Constants.FOUNDATION_END_POINT_RRD,
					foundationURL + Constants.FOUNDATION_END_POINT_RRD);
		}
		return rrdLocator;

	}
	
	/**
	 * Customgroupend point
	 */
	public String portalExtnRESTeasyURL() {
		return portalExtnRESTeasyURL;
	}
}
