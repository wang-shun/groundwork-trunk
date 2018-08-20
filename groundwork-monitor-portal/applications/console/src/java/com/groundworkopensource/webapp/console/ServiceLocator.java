/*
 *  Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork)
 *  All rights reserved. Use is subject to GroundWork commercial license terms.
 */

package com.groundworkopensource.webapp.console;

import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.impl.WSCategoryServiceLocator;
import org.groundwork.foundation.ws.impl.WSCommonServiceLocator;
import org.groundwork.foundation.ws.impl.WSHostGroupServiceLocator;
import org.groundwork.foundation.ws.impl.WSHostServiceLocator;
import org.groundwork.foundation.ws.impl.WSServiceServiceLocator;

/**
 * ServiceLocator class is used to locate the web service locators.
 * @author ashanmugam
 *
 */
public class ServiceLocator {

	private static ServiceLocator locator;
	private static WSCommonServiceLocator commonLocator;
	private static WSHostGroupServiceLocator hostGroupLocator;
	private static WSCategoryServiceLocator categoryLocator;
	private static WSHostServiceLocator hostLocator;
	private static WSServiceServiceLocator serviceLocator;

	public static Logger logger = Logger.getLogger(ServiceLocator.class
			.getName());

	private ServiceLocator() {

	}

	public static ServiceLocator getInstance() {
		if (locator == null) {
			locator = new ServiceLocator();
		}
		return locator;
	}

	public static WSCommonServiceLocator commonLocator() {
		if (commonLocator == null) {
			commonLocator = new WSCommonServiceLocator();
			try {
				commonLocator
						.setEndpointAddress(
								ConsoleConstants.FOUNDATION_END_POINT_PORT_NAME,
								PropertyUtils
										.getProperty(ConsoleConstants.PROP_WS_URL)
										+ ConsoleConstants.FOUNDATION_END_POINT_PORT_NAME);
			} catch (Exception exc) {
				logger.error(exc.getMessage());

			} // end try/catch
		}
		return commonLocator;
	}

	public static WSHostGroupServiceLocator hostGroupLocator() {
		if (hostGroupLocator == null) {
			hostGroupLocator = new WSHostGroupServiceLocator();
			try {
				hostGroupLocator
						.setEndpointAddress(
								ConsoleConstants.FOUNDATION_END_POINT_HOST_GROUP,
								PropertyUtils
										.getProperty(ConsoleConstants.PROP_WS_URL)
										+ ConsoleConstants.FOUNDATION_END_POINT_HOST_GROUP);
			} catch (Exception exc) {
				logger.error(exc.getMessage());

			} // end try/catch
		}
		return hostGroupLocator;
	}
	
	public static WSCategoryServiceLocator categoryLocator() {
		if (categoryLocator == null) {
			categoryLocator = new WSCategoryServiceLocator();
			try {
				categoryLocator
						.setEndpointAddress(
								ConsoleConstants.FOUNDATION_END_POINT_CATEGORY,
								PropertyUtils
										.getProperty(ConsoleConstants.PROP_WS_URL)
										+ ConsoleConstants.FOUNDATION_END_POINT_CATEGORY);
			} catch (Exception exc) {
				logger.error(exc.getMessage());

			} // end try/catch
		}
		return categoryLocator;
	}

	public static WSHostServiceLocator hostLocator() {
		if (hostLocator == null) {
			hostLocator = new WSHostServiceLocator();
			try {
				hostLocator
						.setEndpointAddress(
								ConsoleConstants.FOUNDATION_END_POINT_HOST,
								PropertyUtils
										.getProperty(ConsoleConstants.PROP_WS_URL)
										+ ConsoleConstants.FOUNDATION_END_POINT_HOST);
			} catch (Exception exc) {
				logger.error(exc.getMessage());

			} // end try/catch
		}
		return hostLocator;
	}
	
	public static WSServiceServiceLocator serviceLocator() {
		if (serviceLocator == null) {
			serviceLocator  = new WSServiceServiceLocator();
			try {
				serviceLocator 
						.setEndpointAddress(
								ConsoleConstants.FOUNDATION_END_POINT_SERVICE,
								PropertyUtils
										.getProperty(ConsoleConstants.PROP_WS_URL)
										+ ConsoleConstants.FOUNDATION_END_POINT_SERVICE);
			} catch (Exception exc) {
				logger.error(exc.getMessage());

			} // end try/catch
		}
		return serviceLocator ;
	}
}
