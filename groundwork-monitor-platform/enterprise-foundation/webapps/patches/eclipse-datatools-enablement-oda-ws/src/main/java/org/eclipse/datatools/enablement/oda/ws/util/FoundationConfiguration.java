package org.eclipse.datatools.enablement.oda.ws.util;

import org.apache.commons.codec.binary.Base64;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class FoundationConfiguration {
	
	private static final String FOUNDATION_PROPERTY_FILE = "/usr/local/groundwork/config/foundation.properties";

	public static final String JASYPT_MAINKEY = "jasypt.mainkey";


	// Public Static Interface
	public static final String getProperty(String name) throws IOException {
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
	
	private synchronized String reloadConfiguration() throws IOException {
        configuration = new Properties();
        FileInputStream fis = null;
        String configFile = null;
        try {
            configFile = System.getProperty("foundation-configuration", FOUNDATION_PROPERTY_FILE);
            fis = new FileInputStream(configFile);
            configuration.load(fis);
        }
        finally {
            if (fis != null) {
                try {
                    fis.close();
                }
                catch (Exception e) {
                }
            }
        }
        return configFile;
    }

}
