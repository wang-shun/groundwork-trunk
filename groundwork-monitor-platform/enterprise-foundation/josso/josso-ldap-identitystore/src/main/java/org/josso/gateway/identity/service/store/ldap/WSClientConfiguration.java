package org.josso.gateway.identity.service.store.ldap;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class WSClientConfiguration {
	
	private static final Log log = LogFactory.getLog(WSClientConfiguration.class);

	private static final String FOUNDATION_PROPERTY_FILE = "/usr/local/groundwork/config/ws_client.properties";

	public static final String WEBSERVICES_USERNAME = "webservices_user";
	public static final String WEBSERVICES_PASSWORD = "webservices_password";
	public static final String WEBSERVICES_ENDPOINT = "status_restservice_url";
	public static final String JASYPT_MAINKEY = "jasypt.mainkey";
	public static final String ENCRYPTION_ENABLED = "credentials.encryption.enabled";

	// Public Static Interface
	public static final String getProperty(String name) {
		if (singleton == null) {
			singleton = new WSClientConfiguration();
			singleton.reloadConfiguration();
		}
		return singleton.configuration.getProperty(name);
	}

	public static String getFoundationPropertyFileLocation() {
		return FOUNDATION_PROPERTY_FILE;
	}

	private static WSClientConfiguration singleton = null;
	private Properties configuration = null;

	private WSClientConfiguration() {
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
	
	private synchronized String reloadConfiguration() {
        configuration = new Properties();
        FileInputStream fis = null;
        String configFile = null;
        try {
            configFile = System.getProperty("configuration", FOUNDATION_PROPERTY_FILE);
            fis = new FileInputStream(configFile);
            configuration.load(fis);
            if (log.isInfoEnabled()) {
                log.info("WSClientConfiguration file loaded");
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
