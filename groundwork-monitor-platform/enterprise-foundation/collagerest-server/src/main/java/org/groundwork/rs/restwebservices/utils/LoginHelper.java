/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2014  GroundWork Open Source Solutions info@groundworkopensource.com

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

package org.groundwork.rs.restwebservices.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.ws.impl.ConfigurationWatcher;
import org.groundwork.foundation.ws.impl.ConfigurationWatcherNotificationListener;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Properties;

/**
 * LoginHelper - validate credentials for rest and soap calls.
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class LoginHelper implements ConfigurationWatcherNotificationListener {

	private static final Log log = LogFactory.getLog(LoginHelper.class);

    private static final String APPLICATION_REALM = "ApplicationRealm";

    private static final File APPLICATION_USERS_CONFIGURATION =
            new File("/usr/local/groundwork/foundation/container/jpp/standalone/configuration/application-users.properties");
    private static final File DUAL_APPLICATION_USERS_CONFIGURATION =
            new File("/usr/local/groundwork/foundation/container/jpp2/standalone/configuration/application-users.properties");

    private static volatile LoginHelper singleton;

    /**
     * Singleton instance access.
     *
     * @return singleton
     */
    public static LoginHelper getInstance() {
        if (singleton == null) {
            synchronized (LoginHelper.class) {
                if (singleton == null) {
                    singleton = new LoginHelper();
                }
            }
        }
        return singleton;
    }

    /**
     * Login using application-users.properties authentication.
     *
     * @param username application user username
     * @param password application user UUID
     * @return success flag
     */
    public static boolean login(String username, String password) {
        return getInstance().authenticate(username, password);
    }

    /**
     * Login using application-users.properties authentication.
     *
     * @param authorization base-64 encoded basic auth string
     * @return success flag
     */
    public static boolean login(String authorization) {
        return getInstance().authenticate(authorization);
    }

    /** application-users.properties file */
    private volatile File applicationUsersPropertiesFile;

    /** application-users.properties configuration */
    private volatile Properties applicationUsersProperties = new Properties();

    /**
     * Hidden default constructor for singleton.
     */
    private LoginHelper() {
        this.applicationUsersPropertiesFile = (DUAL_APPLICATION_USERS_CONFIGURATION.isFile() ?
                DUAL_APPLICATION_USERS_CONFIGURATION : APPLICATION_USERS_CONFIGURATION);
        loadProperties();
        ConfigurationWatcher.registerListener(this, this.applicationUsersPropertiesFile.getAbsolutePath());
        log.debug("LoginHelper listening for changes to " + this.applicationUsersPropertiesFile);
    }

    /**
     * Authenticate credentials against application-users.properties.
     *
     * @param authorization base-64 encoded basic auth string
     * @return authenticated flag
     */
    public boolean authenticate(String authorization) {
        if (StringUtils.isEmpty(authorization) || !StringUtils.startsWith(authorization,"Basic ")) {
            return false;
        }
        String base64Credentials = StringUtils.substringAfter(authorization,"Basic ");
        String credentials = new String(Base64.decodeBase64(base64Credentials), StandardCharsets.UTF_8);
        String[] values = StringUtils.split(credentials, ":", 2);
        return authenticate(values[0], values[1]);
    }

	/**
	 * Authenticate credentials against application-users.properties.
	 * 
	 * @param username application user username
	 * @param password application user UUID
	 * @return authenticated flag
	 */
	public boolean authenticate(String username, String password) {
		if ((username == null) || (password == null)) {
            return false;
        }
        // check username:realm:password MD5 HEX hash in application-users.properties
        String applicationUsersHash = DigestUtils.md5Hex(username+":"+APPLICATION_REALM+":"+password);
        return applicationUsersHash.equalsIgnoreCase(applicationUsersProperties.getProperty(username));
	}

    /**
     * Receive change notification from configuration watcher.
     *
     * @param path file changed
     */
    @Override
    public void notifyChange(Path path) {
        if (path.toString().equals(applicationUsersPropertiesFile.getName())) {
            loadProperties();
            log.debug("LoginHelper reloaded configuration on change to " + applicationUsersPropertiesFile);
        }
    }

    /**
     * Load application-users.properties file.
     */
    private void loadProperties() {
        Properties properties = new Properties();
        FileReader propertiesReader = null;
        try {
            propertiesReader = new FileReader(applicationUsersPropertiesFile);
            properties.load(propertiesReader);
        } catch (IOException ioe) {
            log.error("Cannot read configuration file "+applicationUsersPropertiesFile+": "+ioe, ioe);
        } finally {
            if (propertiesReader != null) {
                try {
                    propertiesReader.close();
                } catch (IOException ioe) {
                }
            }
        }
        applicationUsersProperties = properties;
    }
}
