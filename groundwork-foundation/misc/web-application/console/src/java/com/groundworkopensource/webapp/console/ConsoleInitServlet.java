/*
 * Copyright 2008 GroundWork Open Source, Inc. ("GroundWork")  
 * All rights reserved. Use is subject to GroundWork commercial license terms.
 */

package com.groundworkopensource.webapp.console;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.digester.Digester;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * This servlet just initializes Log4j based on a propery file. The file may be
 * specified in the init parameters as "log4j.properties". Per default
 * "/usr/local/groundwork/config/log4j.properties" are checked.
 * 
 * @version $Revision: 5.2 $
 */
public class ConsoleInitServlet extends HttpServlet {
	private static Logger logger = Logger.getLogger(ConsoleInitServlet.class
			.getName());
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void init() throws ServletException {

		String log4jfile = getInitParameter("log4j-init-file");
		if (log4jfile != null) {
			 PropertyConfigurator.configure(log4jfile);
		}

		FileInputStream defaultFS = null;

		FileInputStream fallbackFS = null;

		try {
			logger
					.debug("Trying to find console.properties in /usr/local/groundwork/config/...-");
			Properties defaultProps = new Properties();
			Properties fallbackProps = new Properties();
			defaultFS = new FileInputStream(ConsoleConstants.CONSOLE_PROP_PATH);
			fallbackFS = new FileInputStream(getServletContext().getRealPath(
					ConsoleConstants.CONSOLE_PROP_FALLBACK_PATH));

			defaultProps.load(defaultFS);
			fallbackProps.load(fallbackFS);

			if (validateKeys(defaultProps, fallbackProps)) {

				logger
						.info("Found console.properties in /usr/local/groundwork/config/...-");
				getServletContext().setAttribute(
						ConsoleConstants.CONSOLE_PROPS, defaultProps);
			} else {
				this.loadFromWAR();
				logger
						.warn("One or more properties missing in /usr/local/groundwork/config/console.properties.Using the backup file from WAR...");
			}

		}

		// catch exception in case properties file does not exist

		catch (Exception e) {
			logger
					.warn("console.properties not found in /usr/local/groundwork/config/.Using the backup file...-"
							+ e.getMessage());
			// If the value is null or empty, then get it from the backup file.
			this.loadFromWAR();

		} finally {
			try {
				if (defaultFS != null)
					defaultFS.close();
				if (fallbackFS != null)
					fallbackFS.close();
				this.loadPublicFilters();
				//LocaleConfigurator.configure(getServletContext());
			} catch (IOException ioe) {
				logger.error("Unable to close the input stream-"
						+ ioe.getMessage());
				ioe.printStackTrace();
			} // end if
		} // end try/catch/finally

	}

	/**
	 * Loads the property file from the WAR file
	 */
	private void loadFromWAR() {
		Locale locale = new Locale("en");
		ResourceBundle bundle = ResourceBundle.getBundle("console", locale);
		Enumeration<String> bundleKeys = bundle.getKeys();
		Properties fallbackProps = new Properties();
		while (bundleKeys.hasMoreElements()) {
			String key = bundleKeys.nextElement();
			String value = bundle.getString(key);
			fallbackProps.put(key, value);
		} // end while
		getServletContext().setAttribute(ConsoleConstants.CONSOLE_PROPS,
				fallbackProps);
	}

	/**
	 * Validates the if all properties in the fallback props are available in the default config properties.
	 * @param defaultProp
	 * @param fallbackProp
	 * @return
	 */
	private boolean validateKeys(Properties defaultProp, Properties fallbackProp) {
		boolean returnValue = true;
		Enumeration<Object> em = fallbackProp.keys();
		while (em.hasMoreElements()) {
			String str = (String) em.nextElement();
			if (!defaultProp.containsKey(str)) {
				returnValue = false;
				break;
			} // end if
		} // end while
		return returnValue;
	}

	private void loadPublicFilters() {
		Digester digester = null;
		try {
			digester = new Digester();
			digester.setValidating(false);

			digester.addObjectCreate("PublicFilters",
					PublicFiltersConfigBean.class);

			digester.addObjectCreate("PublicFilters/Filter",
					FilterConfigBean.class);
			digester.addBeanPropertySetter("PublicFilters/Filter/Name", "name");
			digester.addBeanPropertySetter("PublicFilters/Filter/Label",
					"label");
			digester.addBeanPropertySetter("PublicFilters/Filter/AppType",
					"appType");
			digester.addBeanPropertySetter("PublicFilters/Filter/HostGroup",
			"hostGroup");
			digester.addBeanPropertySetter(
					"PublicFilters/Filter/MonitorStatus", "monitorStatus");
			digester.addBeanPropertySetter("PublicFilters/Filter/Severity",
					"severity");
			digester.addBeanPropertySetter("PublicFilters/Filter/OpStatus",
					"opStatus");

			digester.addObjectCreate("PublicFilters/Filter/Fetch",
					FetchConfigBean.class);
			digester.addBeanPropertySetter("PublicFilters/Filter/Fetch/Size",
					"size");
			digester.addBeanPropertySetter("PublicFilters/Filter/Fetch/Order",
					"order");
			digester.addSetNext("PublicFilters/Filter/Fetch", "setFetch");
			digester.addObjectCreate("PublicFilters/Filter/Time",
					TimeConfigBean.class);
			digester.addBeanPropertySetter("PublicFilters/Filter/Time/Unit",
					"unit");
			digester.addBeanPropertySetter(
					"PublicFilters/Filter/Time/Measurement", "measurement");
			digester.addSetNext("PublicFilters/Filter/Time", "setTime");
			digester.addSetNext("PublicFilters/Filter", "addFilterConfigs");
			logger.info("Loading console-admin-config.xml from default location started..");
			File inputFile = new File(ConsoleConstants.CONSOLE_ADMIN_CONFIG_PATH);
			PublicFiltersConfigBean publicFiltersConfigBean = (PublicFiltersConfigBean) digester
					.parse(inputFile);
			getServletContext().setAttribute(
					ConsoleConstants.CONSOLE_ADMIN_CONFIG_PROP,
					publicFiltersConfigBean);
		} catch (Exception exc) {
			logger
					.error("Error loading console-admin-config.xml from default location " + exc.getMessage()
							+ ConsoleConstants.CONSOLE_ADMIN_CONFIG_PATH);
			logger.info("Loading console-admin-config.xml from WAR file started..");
			try {
				File inputFile = new File(getServletContext().getRealPath(
						ConsoleConstants.CONSOLE_ADMIN_FALLBACK_CONFIG_PATH));
				PublicFiltersConfigBean publicFiltersConfigBean = (PublicFiltersConfigBean) digester
						.parse(inputFile);
				getServletContext().setAttribute(
						ConsoleConstants.CONSOLE_ADMIN_CONFIG_PROP,
						publicFiltersConfigBean);
			} catch (Exception cexc) {
				logger.error("Error loading console admin config from WAR "
						+ cexc.getMessage());
			} // end try/catch
		} // end try/catch
	}
}