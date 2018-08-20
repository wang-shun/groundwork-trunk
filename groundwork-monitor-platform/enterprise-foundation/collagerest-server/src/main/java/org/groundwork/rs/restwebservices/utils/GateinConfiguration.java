package org.groundwork.rs.restwebservices.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class GateinConfiguration {

	private static Log log = LogFactory.getLog(GateinConfiguration.class);
	private static final String GATEIN_PROPERTY_FILE = "/usr/local/groundwork/config/gatein.properties";

	public static final String JPP_HOST = "host";
	public static final String JPP_PORT = "port";
	public static final String JPP_PROTOCOL = "protocol";
	public static final String JPP_CONTEXT = "context";

	// Public Static Interface
	public synchronized static final String getProperty(String name) {
		if (singleton == null) {
			singleton = new GateinConfiguration();
		}
		return singleton.configuration.getProperty(name);
	}

	public static String getGateinPropertyFileLocation() {
		return GATEIN_PROPERTY_FILE;
	}

	private static GateinConfiguration singleton = null;
	private Properties configuration = null;

	private GateinConfiguration() {
		loadConfiguration();
	}

	private String loadConfiguration() {
		configuration = new Properties();
		FileInputStream fis = null;
		String configFile = null;
		try {
			configFile = System.getProperty("configuration",
					GATEIN_PROPERTY_FILE);
			fis = new FileInputStream(configFile);
			configuration.load(fis);
			if (log.isInfoEnabled()) {
				log.info("GateinConfiguration file loaded");
			}

		} catch (IOException e) {
			log.error(e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (Exception e) {
					log.error(e);
				}
			}
		}
		return configFile;
	}
}
