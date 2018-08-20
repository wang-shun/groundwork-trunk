package com.groundwork.core.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.ws.impl.ConfigurationWatcher;
import org.groundwork.foundation.ws.impl.ConfigurationWatcherNotificationListener;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class GateinConfiguration implements ConfigurationWatcherNotificationListener {

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
            singleton.initConfiguration();
        }
        return singleton.configuration.getProperty(name);
    }
    public static String getGateinPropertyFileLocation() {
        return GATEIN_PROPERTY_FILE;
    }

    private static GateinConfiguration singleton = null;
    private Properties configuration = null;
    private String watchedFileName = null;

    private GateinConfiguration() {
    }

    public void notifyChange(Path path) {
        if (watchedFileName != null) {
            if (watchedFileName.equals(path.toString())) {
                if (log.isInfoEnabled()) {
                    log.info("Received Notification of change on GateinConfiguration file " + path.toString());
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
            configFile = System.getProperty("configuration", GATEIN_PROPERTY_FILE);
            fis = new FileInputStream(configFile);
            configuration.load(fis);
            if (log.isInfoEnabled()) {
                log.info("GateinConfiguration file loaded");
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

        // update/check super user portal configuration
        try {
            if (GateinConfigurationUtils.updateUserACLSuperUserInitParam()) {
                if (log.isInfoEnabled()) {
                    log.info("Portal super user configuration updated");
                }
            }
        } catch (Exception e) {
            log.error("Update/check for portal super user failed: "+e, e);
        }

        return configFile;
    }


}
