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

/**
 * Created by ArulShanmugam on 1/6/15.
 */
public class LDAPMappingDirectiveConfig implements ConfigurationWatcherNotificationListener {
    private static Log log = LogFactory.getLog(LDAPMappingDirectiveConfig.class);
    private static final String LDAP_MAPPING_DIR_PROPERTY_FILE = "/usr/local/groundwork/config/ldap-mapping-directives.properties";

    public static final String ROOT_GROUP="root_group";
    public static final String ADMIN_GROUP="admin_group";
    public static final String OPERATOR_GROUP="operator_group";
    public static final String USER_GROUP="user_group";


    // Public Static Interface
    public static final String getProperty(String name) {
        if (singleton == null) {
            singleton = new LDAPMappingDirectiveConfig();
            singleton.initConfiguration();
        }
        return singleton.configuration.getProperty(name);
    }

    public static String getLDAPMappingPropertyFileLocation() {
        return LDAP_MAPPING_DIR_PROPERTY_FILE;
    }

    private static LDAPMappingDirectiveConfig singleton = null;
    private Properties configuration = null;
    private String watchedFileName = null;

    private LDAPMappingDirectiveConfig() {
    }

    public void notifyChange(Path path) {
        if (watchedFileName != null) {
            if (watchedFileName.equals(path.toString())) {
                if (log.isInfoEnabled()) {
                    log.info("Received Notification of change on LDAPMappingDirectiveConfig file " + path.toString());
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
            configFile = System.getProperty("ldap_map_dir_configuration", LDAP_MAPPING_DIR_PROPERTY_FILE);
            fis = new FileInputStream(configFile);
            configuration.load(fis);
            if (log.isInfoEnabled()) {
                log.info("LDAPMappingDirectiveConfig file loaded");
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
