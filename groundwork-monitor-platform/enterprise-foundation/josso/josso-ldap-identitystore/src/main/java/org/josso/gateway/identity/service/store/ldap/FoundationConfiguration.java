package org.josso.gateway.identity.service.store.ldap;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class FoundationConfiguration {
	
	private static final Log log = LogFactory.getLog(FoundationConfiguration.class);

	private static final String FOUNDATION_PROPERTY_FILE = "/usr/local/groundwork/config/foundation.properties";

	public static final String JASYPT_MAINKEY = "jasypt.mainkey";


	// Public Static Interface
	public static final String getProperty(String name) {
		if (singleton == null) {
			singleton = new FoundationConfiguration();
			singleton.reloadConfiguration();
		}
		return singleton.configuration.getProperty(name);
	}

	public static String getFoundationPropertyFileLocation() {
		return FOUNDATION_PROPERTY_FILE;
	}

	private static FoundationConfiguration singleton = null;
	private Properties configuration = null;

	private FoundationConfiguration() {
	}

	 /**
     * Helper to decrypt main key
     *
     * @param encstr
     * @return
     */
    public static String decryptMainKey(String encstr) {
        if (encstr.length() > 12) {
            String cipher = encstr.substring(12);
            return new String(Base64.decodeBase64(cipher.getBytes()));
        }
        return null;
    }

    /**
     * Forced configuration reload.
     */
    public static void reload() {
        if (singleton != null) {
            singleton.reloadConfiguration();
        }
    }
	
	private synchronized String reloadConfiguration() {
        configuration = new Properties();
        FileInputStream fis = null;
        String configFile = null;
        try {
            configFile = System.getProperty("foundation-configuration", FOUNDATION_PROPERTY_FILE);
            fis = new FileInputStream(configFile);
            configuration.load(fis);
            if (log.isInfoEnabled()) {
                log.info("FoundationConfiguration file loaded");
            }

        } catch (IOException e) {
            log.error(e);
        }
        finally {
            if (fis != null) {
                try {
                    fis.close();
                }
                catch (Exception e) {
                    log.error(e);
                }
            }
        }
        return configFile;
    }

}
