/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2017  GroundWork Open Source Solutions info@groundworkopensource.com

 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.

 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.

 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.groundwork.rs.influxdb;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

class InfluxDBConfiguration {

    private static final Log log = LogFactory.getLog(InfluxDBClient.class);

    private static final String PROPERTIES_FILE = "/usr/local/groundwork/config/influxdb.properties";
    private static final String URL_PROP_NAME = "url";
    private static final String URL_DEFAULT = "http://localhost:8086";
    private static final String DB_NAME_PROP_NAME = "database";
    private static final String DB_NAME_DEFAULT = "groundwork";
    private static final String HOSTGROUPS_ENABLED_PROP_NAME = "hostgroups_enabled";
    private static final String HOSTGROUPS_ENABLED_DEFAULT = "false";
    private static final String HOSTGROUP_CACHE_DURATION_PROP_NAME = "hostgroup_cache_duration";
    private static final String HOSTGROUP_CACHE_DURATION_PROP_NAME_DEFAULT = "600";

    private static URL url;
    private static String dbName;
    private static boolean hostgroupsEnabled;
    private static long hostgroupCacheDuration;

    static {

        Properties influxDBProperties = new Properties();
        InputStream influxDBPropertiesInput = null;

        try {
            influxDBPropertiesInput = new FileInputStream(PROPERTIES_FILE);
            influxDBProperties.load(influxDBPropertiesInput);
        } catch (FileNotFoundException fnfe) {
            if (log.isDebugEnabled()) log.debug("Could not find " + PROPERTIES_FILE + ".  Using default values.");
        } catch (IOException ioe) {
            log.error("Unable to open " + PROPERTIES_FILE + ".  Using default values.");
        } finally {
            try {
                if (influxDBPropertiesInput != null) {
                    influxDBPropertiesInput.close();
                }
            } catch (IOException ioe) {
                log.error("Unable to close " + PROPERTIES_FILE);
            }
        }

        try {
            url = new URL(influxDBProperties.getProperty(URL_PROP_NAME, URL_DEFAULT));
        } catch (MalformedURLException e) {
            log.error("Invalid value for " + URL_PROP_NAME + " in file " + PROPERTIES_FILE);
            throw new RuntimeException(e);
        }
        dbName = influxDBProperties.getProperty(DB_NAME_PROP_NAME, DB_NAME_DEFAULT);
        hostgroupsEnabled = Boolean.parseBoolean(influxDBProperties.getProperty(HOSTGROUPS_ENABLED_PROP_NAME, HOSTGROUPS_ENABLED_DEFAULT));
        hostgroupCacheDuration = Long.parseLong(influxDBProperties.getProperty(HOSTGROUP_CACHE_DURATION_PROP_NAME, HOSTGROUP_CACHE_DURATION_PROP_NAME_DEFAULT));

        if (log.isDebugEnabled()) {
            log.debug(URL_PROP_NAME + "=" + url);
            log.debug(DB_NAME_PROP_NAME + "=" + dbName);
            log.debug(HOSTGROUPS_ENABLED_PROP_NAME + "=" + hostgroupsEnabled);
            log.debug(HOSTGROUP_CACHE_DURATION_PROP_NAME + "=" + hostgroupCacheDuration);
        }
    }

    static URL getURL() {
        return url;
    }

    static String getDBName() {
        return dbName;
    }

    static boolean isHostgroupsEnabled() {
        return hostgroupsEnabled;
    }

    static long getHostgroupCacheDuration() {
        return hostgroupCacheDuration;
    }

}
