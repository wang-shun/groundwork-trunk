/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2015  GroundWork Open Source Solutions info@groundworkopensource.com

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

package org.groundwork.rs.opentsdb;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * OpenTSDBConfiguration
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class OpenTSDBConfiguration {

    private static final String OPENTSDB_PROPERTIES_FILE = "/usr/local/groundwork/config/opentsdb.properties";
    private static final String OPENTSDB_HOST_PROP_NAME = "com.groundwork.feeder.service.opentsdb.host";
    private static final String OPENTSDB_HOST_DEFAULT = "localhost";
    private static final String OPENTSDB_PORT_PROP_NAME = "com.groundwork.feeder.service.opentsdb.port";
    private static final String OPENTSDB_PORT_DEFAULT = "4242";
    private static final String OPENTSDB_HOSTGROUPS_ENABLED_PROP_NAME = "com.groundwork.feeder.service.opentsdb.hostgroups.enabled";
    private static final String OPENTSDB_HOSTGROUPS_ENABLED_DEFAULT = "false";
    private static final String OPENTSDB_CACHE_TTL_PROP_NAME = "com.groundwork.feeder.service.opentsdb.cache.ttl";
    private static final String OPENTSDB_CACHE_TTL_DEFAULT = "300";

    private static String openTSDBHost;
    private static int openTSDBPort;
    private static boolean openTSDBHostGroupsEnabled;
    private static int openTSDBCacheTTL;

    static {
        // load OpenTSDB configuration
        Properties openTSDBProperties = new Properties();
        InputStream openTSDBPropertiesInput = null;
        try {
            openTSDBPropertiesInput = new FileInputStream(OPENTSDB_PROPERTIES_FILE);
            openTSDBProperties.load(openTSDBPropertiesInput);
        } catch (IOException ioe) {
        } finally {
            try {
                if (openTSDBPropertiesInput != null) {
                    openTSDBPropertiesInput.close();
                }
            } catch (IOException ioe) {
            }
        }
        openTSDBHost = openTSDBProperties.getProperty(OPENTSDB_HOST_PROP_NAME, OPENTSDB_HOST_DEFAULT);
        openTSDBPort = Integer.parseInt(openTSDBProperties.getProperty(OPENTSDB_PORT_PROP_NAME, OPENTSDB_PORT_DEFAULT));
        openTSDBHostGroupsEnabled = Boolean.parseBoolean(openTSDBProperties.getProperty(OPENTSDB_HOSTGROUPS_ENABLED_PROP_NAME, OPENTSDB_HOSTGROUPS_ENABLED_DEFAULT));
        openTSDBCacheTTL = Integer.parseInt(openTSDBProperties.getProperty(OPENTSDB_CACHE_TTL_PROP_NAME, OPENTSDB_CACHE_TTL_DEFAULT));
    }

    public static String getOpenTSDBHost() {
        return openTSDBHost;
    }

    public static int getOpenTSDBPort() {
        return openTSDBPort;
    }

    public static boolean isOpenTSDBHostGroupsEnabled() {
        return openTSDBHostGroupsEnabled;
    }

    public static int getOpenTSDBCacheTTL() {
        return openTSDBCacheTTL;
    }
}
