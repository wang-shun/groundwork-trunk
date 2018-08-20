package com.groundwork.dashboard;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.ws.impl.ConfigurationWatcher;
import org.groundwork.foundation.ws.impl.ConfigurationWatcherNotificationListener;
import org.groundwork.foundation.ws.impl.JasyptUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class NocConfiguration implements ConfigurationWatcherNotificationListener {

    private static Log log = LogFactory.getLog(NocConfiguration.class);
    private static final String NOC_CONFIGURATION = "/usr/local/groundwork/config/noc.properties";

    public static final String NOC_DOWNTIME_USERNAME = "noc.downtime.username";
    public static final String NOC_DOWNTIME_PASSWORD = "noc.downtime.password";
    public static final String NOC_DOWNTIME_ROLE = "noc.downtime.role";
    public static final String NOC_DOWNTIME_URL_OVERRIDE = "noc.downtime.url.override";
    public static final String NOC_DOWNTIME_ENABLE = "noc.downtime.enable";
    public static final String NOC_DOWNTIME_CACHE_SECONDS = "noc.downtime.cache.seconds";
    public static final String NOC_METRICS_ENABLE = "noc.metrics.enable";
    public static final String NOC_AVAILABILITY_ENABLE = "noc.availability.enable";
    public static final String NOC_DOWNTIME_UNSCHEDULED_ENABLE = "noc.downtime.unscheduled.enable";

    private static NocConfiguration singleton = null;
    private Properties configuration = null;
    private String watchedFileName = null;

    private NocConfiguration() {
    }

    // Public Static Interface
    public synchronized static final String getProperty(String name) {
        if (singleton == null) {
            singleton = new NocConfiguration();
            singleton.initConfiguration();
        }
        return singleton.configuration.getProperty(name);
    }

    public synchronized static final String getEncryptedProperty(String name) {
        if (singleton == null) {
            singleton = new NocConfiguration();
            singleton.initConfiguration();
        }
        String encrypted = singleton.configuration.getProperty(name);
        return JasyptUtils.jasyptDecrypt(encrypted);
    }

    public synchronized static final Boolean getBooleanProperty(String name) {
        String prop = getProperty(name);
        if (prop == null) {
            return false;
        }
        return Boolean.parseBoolean(prop);
    }

    public synchronized static final Integer getIntegerProperty(String name) {
        String prop = getProperty(name);
        if (prop == null) {
            return -1;
        }
        return Integer.parseInt(prop);
    }

    public void notifyChange(Path path) {
        if (watchedFileName != null) {
            if (watchedFileName.equals(path.toString())) {
                if (log.isInfoEnabled()) {
                    log.info("Received Notification of change on NOC Configuration file " + path.toString());
                }
                reloadConfiguration();
            }
        }
    }

    public static String getPropertyFileLocation() {
        return NOC_CONFIGURATION;
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
            configFile = System.getProperty("configuration", NOC_CONFIGURATION);
            fis = new FileInputStream(configFile);
            configuration.load(fis);
            if (log.isInfoEnabled()) {
                log.info("NOC file loaded");
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
