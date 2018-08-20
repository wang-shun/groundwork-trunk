package org.groundwork.foundation.ws.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class WSClientConfiguration implements ConfigurationWatcherNotificationListener {

    private static Log log = LogFactory.getLog(WSClientConfiguration.class);
    private static final String FOUNDATION_PROPERTY_FILE = "/usr/local/groundwork/config/ws_client.properties";

    public static final String WEBSERVICES_USERNAME = "webservices_user";
    public static final String WEBSERVICES_PASSWORD = "webservices_password";
    public static final String WEBSERVICES_READER_USERNAME = "webservices_reader_user";
    public static final String WEBSERVICES_READER_PASSWORD = "webservices_reader_password";
    public static final String WEBSERVICES_ENDPOINT = "status_restservice_url";
    public static final String ENCRYPTION_ENABLED = "credentials.encryption.enabled";
    public static final String FOUNDATION_REST_ENDPOINT = "foundation_rest_url";
    public static final String FOUNDATION_REST_ORIGINS = "foundation_rest_origins";
    public static final String WEBSERVICES_USER_LAST_UPDATE_TIMESTAMP = "webservices.acct.lastupdate.timestamp";
    public static final String WEBSERVICES_USER_READER_LAST_UPDATE_TIMESTAMP = "webservices.reader.acct.lastupdate.timestamp";

    public static final String DEFAULT_WEBSERVICES_USERNAME_VALUE = "RESTAPIACCESS";
    public static final String DEFAULT_WEBSERVICES_READER_USERNAME_VALUE = "RESTAPIACCESSREADER";

    // Public Static Interface
    public synchronized static final String getProperty(String name) {
        if (singleton == null) {
            singleton = new WSClientConfiguration();
            singleton.initConfiguration();
        }
        return singleton.configuration.getProperty(name);
    }

    public static String getFoundationPropertyFileLocation() {
        return FOUNDATION_PROPERTY_FILE;
    }

    private static WSClientConfiguration singleton = null;
    private Properties configuration = null;
    private String watchedFileName = null;

    private WSClientConfiguration() {
    }

    public void notifyChange(Path path) {
        if (watchedFileName != null) {
            if (watchedFileName.equals(path.toString())) {
                if (log.isInfoEnabled()) {
                    log.info("Received Notification of change on WSClientConfiguration file " + path.toString());
                }
                reloadConfiguration();
            }
        }
    }

    private synchronized void initConfiguration() {
        String configFile = reloadConfiguration();
        String filePath = Paths.get(configFile).toString();
        watchedFileName = Paths.get(configFile).getFileName().toString();
        ConfigurationWatcher.registerListener(this, filePath);
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
