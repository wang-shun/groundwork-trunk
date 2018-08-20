/*
 * Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork) All rights
 * reserved. This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * dname.pl - This is a utility script to clean up the foundation database for
 * device display names and identification fields that were inconsistently fed
 * into the database. This can cause some issues in the display for the event
 * console, especially when upgrading an older database. Use in consultation
 * with GroundWork Support!
 */

package com.groundworkopensource.portal.statusviewer.bean.networkservice;

import java.util.Enumeration;
import java.util.Properties;

/**
 * NetworkServiceConfig containing utility methods for returning properties read
 * from network-service.properties file.
 */
public class NetworkServiceConfig {

    /** properties from network-service.properties */
    private static Properties properties;

    /**
     * Loads properties from network-service.properties
     */
    public static void load() {
        String nsConfigFile = System
                .getenv("BITROCK_NETWORK_SERVICE_PLUGIN_CONFIG_FILE");

        if (nsConfigFile == null) {
            nsConfigFile = "network-service.properties";
        }

        Properties networkServiceProperties = NetworkServiceHelpers
                .readPropertiesFromFile(nsConfigFile);

        // load default properties
        loadDefaults();
        if (networkServiceProperties != null) {
            for (Enumeration<Object> propertyKeys = networkServiceProperties
                    .keys(); propertyKeys.hasMoreElements();) {
                String key = (String) propertyKeys.nextElement();
                properties.setProperty(key, networkServiceProperties
                        .getProperty(key));
            }
        }
    }

    /**
     * load default properties.
     */
    private static void loadDefaults() {
        properties.setProperty("ns.active", "false");

        properties.setProperty("ns.internal_css", "false");

        properties.setProperty("ns.files.info", "");
        properties.setProperty("ns.files.agent_config", "agent.conf");

        properties.setProperty("ns.msg.main_header", "Notifications");
        properties.setProperty("ns.msg.info_header", "Network Service Info");
        properties.setProperty("ns.msg.ns_inactive",
                "Network Service component is not activated");
        properties.setProperty("ns.msg.no_notifications",
                "no notifications found");
        properties.setProperty("ns.msg.db_connection_problems",
                "database connection problems");
        properties.setProperty("ns.msg.errors.agent_config_reading",
                "Error: Network Service Agent reading problems.");
        properties.setProperty("ns.msg.errors.info_reading",
                "Error: Network Service Info file reading problems.");

        // ################################
        properties.setProperty("ns.db.config_file", "ns.db.properties");
        // ################################
        properties.setProperty("ns.db.connection_driver_class_key",
                "ns.db.connection_driver_class");
        properties.setProperty("ns.db.connection_driver_key",
                "ns.db.connection_driver");
        properties.setProperty("ns.db.url_key", "ns.db.url");
        properties.setProperty("ns.db.username_key", "ns.db.username");
        properties.setProperty("ns.db.password_key", "ns.db.password");
        properties.setProperty("ns.db.database_key", "ns.db.database");
        properties.setProperty("ns.db.dbhost_key", "ns.db.dbhost");
        // ##### optional
        properties.setProperty("ns.db.port_key", "ns.db.port");

        properties.setProperty("ns.db.connection_driver_class",
                "com.mysql.jdbc.Driver");
        properties.setProperty("ns.db.connection_driver", "mysql");
        properties.setProperty("ns.db.username", "bitrock");
        properties.setProperty("ns.db.password", "");
        properties.setProperty("ns.db.database", "bitrock");
        properties.setProperty("ns.db.dbhost", "localhost");
    }

    /**
     * Gets the value for the passed key.
     * 
     * @param key
     *            the key
     * 
     * @return value for the passed key
     */
    public static String get(String key) {
        return properties.getProperty(key);
    }

    /**
     * Checks if network service is activated.
     * 
     * @return ns.active property value
     */
    public static Boolean isActivated() {
        return Boolean.parseBoolean(properties.getProperty("ns.active"));
    }

    /**
     * Checks if need to use Internal CSS.
     * 
     * @return ns.internal_css property value
     */
    public static Boolean useInternalCss() {
        return Boolean.parseBoolean(properties.getProperty("ns.internal_css"));
    }

    /**
     * Returns Properties.
     * 
     * @return Properties
     */
    public static Properties getProperties() {
        if (null == properties) {
            properties = new Properties();
            load();
        }
        return properties;
    }

    /**
     * Protected Constructor - Rationale: Instantiating utility classes does not
     * make sense. Hence the constructors should either be private or (if you
     * want to allow sub-classing) protected. <br>
     * 
     * Refer to "HideUtilityClassConstructor" section in
     * http://checkstyle.sourceforge.net/config_design.html.
     */
    protected NetworkServiceConfig() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }
}