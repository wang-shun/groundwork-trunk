package org.groundwork.foundation.ws.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class FoundationConfiguration implements ConfigurationWatcherNotificationListener {

    private static Log log = LogFactory.getLog(FoundationConfiguration.class);
    private static final String FOUNDATION_PROPERTY_FILE = "/usr/local/groundwork/config/foundation.properties";

    public static final String JASYPT_MAINKEY = "jasypt.mainkey";
    public static final String PROXY_USER = "portal.proxy.user";

    public static final String PROXY_PASSWORD = "portal.proxy.password";
    public static final String PROXY_USER_LAST_UPDATE_TIMESTAMP = "portal.proxy.acct.lastupdate.timestamp";
    public static final String MAIN_KEY_LAST_UPDATE_TIMESTAMP = "jasypt.mainkey.lastupdate.timestamp";
    public static final String LDAP_LAST_UPDATE_TIMESTAMP = "core.security.ldap.lastupdate.timestamp";

    // Public Static Interface
    public synchronized static final String getProperty(String name) {
        if (singleton == null) {
            singleton = new FoundationConfiguration();
            singleton.initConfiguration();
        }
        return singleton.configuration.getProperty(name);
    }

    /**
     * Force immediate configuration reload
     */
    public synchronized static final void reload() {
        if (singleton != null) {
            if (log.isInfoEnabled()) {
                log.info("Force FoundationConfiguration file reload");
            }
            singleton.reloadConfiguration();
        }
    }

    public static String getFoundationPropertyFileLocation() {
        return FOUNDATION_PROPERTY_FILE;
    }

    private static FoundationConfiguration singleton = null;
    private Properties configuration = null;
    private String watchedFileName = null;

    private FoundationConfiguration() {
    }

    public void notifyChange(Path path) {
        if (watchedFileName != null) {
            if (watchedFileName.equals(path.toString())) {
                if (log.isInfoEnabled()) {
                    log.info("Received Notification of change on FoundationConfiguration file " + path.toString());
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
            configFile = System.getProperty("foundation_configuration", FOUNDATION_PROPERTY_FILE);
            fis = new FileInputStream(configFile);
            configuration.load(fis);
            if (log.isInfoEnabled()) {
                log.info("FoundationConfiguration file loaded");
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
