package com.groundwork.report.birt.eventhandler;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
import java.util.Properties;
import java.util.logging.Logger;

public class RESTInfo {
	private static RESTInfo _instance = null;
	public static String portal_rest_url = null;
	/** The logger. */
	Logger logger = Logger.getLogger(this.getClass().getName());

	protected RESTInfo() {
		try (InputStream file = new FileInputStream(new File(
				"/usr/local/groundwork/config/status-viewer.properties"))) {
			Properties props = new Properties();
			props.load(file);
			portal_rest_url = props
					.getProperty("portal.extension.resteasy.service.url");
		} catch (Exception e) {
			logger.severe("error" + e);
		}
	}

	public static RESTInfo instance() {
		if (_instance == null) {
			_instance = new RESTInfo();
		}
		return _instance;
	}
}