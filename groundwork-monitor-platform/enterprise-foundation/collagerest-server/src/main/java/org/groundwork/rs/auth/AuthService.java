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

package org.groundwork.rs.auth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.ws.impl.ConfigurationWatcher;
import org.groundwork.foundation.ws.impl.ConfigurationWatcherNotificationListener;
import org.groundwork.foundation.ws.impl.WSClientConfiguration;
import org.groundwork.rs.restwebservices.utils.LoginHelper;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

/**
 * AuthService - manage authorization access tokens.
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class AuthService implements ConfigurationWatcherNotificationListener {

    private static final Log log = LogFactory.getLog(AuthService.class);

    private static final File JOSSO_GATEWAY_CONFIGURATION = new File("/usr/local/groundwork/config/josso-gateway-config.xml");
    private static final File FOUNDATION_CONFIGURATION = new File("/usr/local/groundwork/config/foundation.properties");

    private static final int ACCESS_TOKENS_EXPIRATION_INTERVAL = 120;
    private static final int DEFAULT_ACCESS_TOKENS_EXPIRATION = 28800;

    private static final int DEFAULT_MAX_ACCESS_TOKENS = 500;

    private static volatile AuthService singleton;

    private ExpiringMap<String,AuthAccessInfo> accessTokens;
    private int maxAccessTokens;

    /**
     * Singleton instance access.
     *
     * @return singleton
     */
    public static AuthService getInstance() {
        if (singleton == null) {
            synchronized (LoginHelper.class) {
                if (singleton == null) {
                    singleton = new AuthService();
                }
            }
        }
        return singleton;
    }

    /**
     * Hidden default constructor for singleton.
     */
    private AuthService() {
        configureAccessTokens();
        configure();
        ConfigurationWatcher.registerListener(this, JOSSO_GATEWAY_CONFIGURATION.getAbsolutePath());
        ConfigurationWatcher.registerListener(this, FOUNDATION_CONFIGURATION.getAbsolutePath());
        log.debug("AuthService listening for changes to " + JOSSO_GATEWAY_CONFIGURATION + " and " + FOUNDATION_CONFIGURATION);
    }

    /**
     * Make and cache new access token for specified application.
     *
     * @param appName access token application name
     * @since 7.2.0 @param username authorized user
     * @return new access token
     */
    public String makeAccessToken(String appName, String username) {
        if (appName == null) {
            throw new IllegalArgumentException("Application name required argument");
        }
        // check for max access tokens limit: log and invalidate all access tokens
        // so that server can continue, (all clients will need to reauthenticate and
        // allocate new tokens)
        if (log.isInfoEnabled()) {
            // Log guard this to avoid needlessly counting token usage
            log.info("Attempting to make access token for app '" + appName + "'. Usage by application: " + getAccessTokensByApplication());
        }
        if (accessTokens.size() >= maxAccessTokens) {
            HashMap<String,Integer> accessTokensByApplication = getAccessTokensByApplication();
            accessTokens.clear();
            log.error("Maximum number of access tokens reached, (" + maxAccessTokens + "), by application: " + accessTokensByApplication);
        }

        // @since 7.2.0 - determine user's authorization by simple convention of configured reader username gets read-only authorization,
        // and configured admin username gets full authorization
        String adminUserName = WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_USERNAME);
        boolean isAdmin = (adminUserName != null && adminUserName.equals(username));

        // create and put access token/application name pair
        String accessToken = UUID.randomUUID().toString();
        accessTokens.put(accessToken, new AuthAccessInfo(appName, accessToken, !isAdmin));
        return accessToken;
    }

    /**
     * Check access token for specified application.
     *
     * @param accessToken access token to check
     * @param appName access token application name
     * @return valid flag
     */
    public boolean checkAccessToken(String accessToken, String appName) {
        if (accessToken == null) {
            return false;
        }
        if (appName == null) {
            throw new IllegalArgumentException("Application name required argument");
        }
        // check access token/application name pair
        AuthAccessInfo access = accessTokens.get(accessToken);
        if (access != null) {

            return appName.equals(access.getAppName());
        }
        return false;
    }

    /**
     * Delete access token for specified application.
     *
     * @param accessToken access token to delete
     * @param appName access token application name
     * @return deleted flag
     */
    public boolean deleteAccessToken(String accessToken, String appName) {
        if (accessToken == null) {
            return false;
        }
        if (appName == null) {
            throw new IllegalArgumentException("Application name required argument");
        }
        if (log.isInfoEnabled()) {
            // Log guard this to avoid needlessly counting token usage
            log.info("Attempting to delete access token for app '" + appName + "'. Token's app: " + accessTokens.get(accessToken) + " Usage by application: " + getAccessTokensByApplication());
        }
        // delete access token/application name pair
        AuthAccessInfo access = accessTokens.get(accessToken);
        if (access == null) {
            return false; // not found
        }
        if (!appName.equals(access.getAppName())) {
            return false;
        }
        AuthAccessInfo deletedAccess = accessTokens.remove(accessToken);
        if (deletedAccess != null) {
            return appName.equals(deletedAccess.getAppName());
        }
        return false;
    }

    /**
     * Receive change notification from configuration watcher.
     *
     * @param path file changed
     */
    @Override
    public void notifyChange(Path path) {
        if (path.toString().equals(JOSSO_GATEWAY_CONFIGURATION.getName())) {
            configureAccessTokens();
            log.debug("AuthService reloaded configuration on change to "+JOSSO_GATEWAY_CONFIGURATION);
        } else if (path.toString().equals(FOUNDATION_CONFIGURATION.getName())) {
            configure();
            log.debug("AuthService reloaded configuration on change to "+FOUNDATION_CONFIGURATION);
        }
    }

    /**
     * Return configured max access tokens.
     *
     * @return max access tokens
     */
    protected int getMaxAccessTokens() {
        return maxAccessTokens;
    }

    /**
     * Configure access tokens map with expiration.
     */
    private void configureAccessTokens() {
        // read JOSSO gateway configuration
        int accessTokensExpiration = DEFAULT_ACCESS_TOKENS_EXPIRATION;
        InputStream jossoGatewayConfigurationStream = null;
        try {
            jossoGatewayConfigurationStream = new FileInputStream(JOSSO_GATEWAY_CONFIGURATION);
            Document jossoGatewayConfiguration = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(jossoGatewayConfigurationStream);
            XPathExpression maxInactiveIntervalXPath = XPathFactory.newInstance().newXPath().compile("/beans/session-manager/@maxInactiveInterval");
            String maxInactiveInterval = (String)maxInactiveIntervalXPath.evaluate(jossoGatewayConfiguration, XPathConstants.STRING);
            if ((maxInactiveInterval != null) && (maxInactiveInterval.length() > 0)) {
                accessTokensExpiration = Integer.parseInt(maxInactiveInterval)*60;
            }
         } catch (Exception e) {
            log.error("Cannot read or parse configuration file "+JOSSO_GATEWAY_CONFIGURATION+": "+e);
        } finally {
            if (jossoGatewayConfigurationStream != null) {
                try {
                    jossoGatewayConfigurationStream.close();
                } catch (IOException ioe ) {
                }
            }
        }
        // create or reconfigure access tokens map with expiration
        if (accessTokens == null) {
            accessTokens = new ExpiringMap<String,AuthAccessInfo>(accessTokensExpiration, ACCESS_TOKENS_EXPIRATION_INTERVAL);
        } else {
            accessTokens.setTimeToLive(accessTokensExpiration);
        }
    }

    /**
     * Configure settings.
     */
    private void configure() {
        // load configuration from foundation properties
        if (maxAccessTokens == 0) {
            maxAccessTokens = DEFAULT_MAX_ACCESS_TOKENS;
        }
        FileReader foundationPropertiesReader = null;
        try {
            foundationPropertiesReader = new FileReader(FOUNDATION_CONFIGURATION);
            Properties foundationProperties = new Properties();
            foundationProperties.load(foundationPropertiesReader);
            String sessionMax = foundationProperties.getProperty("collagerest.sessions.max");
            if ((sessionMax != null) && (sessionMax.length() > 0)) {
                maxAccessTokens = Integer.parseInt(sessionMax);
            }
        } catch (IOException ioe) {
            log.error("Cannot read configuration file "+FOUNDATION_CONFIGURATION+": "+ioe);
        } finally {
            if (foundationPropertiesReader != null) {
                try {
                    foundationPropertiesReader.close();
                } catch (IOException ioe) {
                }
            }
        }
    }

    public HashMap<String, Integer> getAccessTokensByApplication() {
        HashMap<String, Integer> accessTokensByApplication = new HashMap<>();
        for (Map.Entry<String, AuthAccessInfo> entry : accessTokens.entrySet()) {
            int count = (accessTokensByApplication.containsKey(entry.getValue()) ? accessTokensByApplication.get(entry.getValue()) + 1 : 1);
            accessTokensByApplication.put(entry.getValue().getAppName(), count);
        }
        return accessTokensByApplication;
    }

    public Map<String, AuthAccessInfo> listAccessTokens() {
        Map<String, AuthAccessInfo> result = new HashMap<>();
        result.putAll(accessTokens);
        return result;
    }

    /**
     * Check access token for specified application.
     *
     * @param accessToken access token to check
     * @param appName appName to check
     * @return valid flag
     */
    public boolean isAdmin(String accessToken, String appName) {
        if (accessToken == null) {
            return false;
        }
        AuthAccessInfo access = accessTokens.get(accessToken);
        if (access != null) {
            return !access.getReadonly() && appName.equals(access.getAppName());
        }
        return false;
    }

}
