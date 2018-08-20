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

package com.groundwork.core.security.ldap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * LDAPAggregator
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class LDAPAggregator {

    public static final String DOMAIN_PREFIX_REQUIRED_PROP = "core.security.ldap.domain_prefix_required";
    public static final String DOMAIN_PREFIX_REQUIRED_DEFAULT = "false";

    public static final String STRONG_AUTHENTICATION_SCHEME = "strong-authentication";

    protected enum AttributeOperation {ADD, REPLACE, REMOVE};

    private static final String DOMAIN_ID_SEPARATOR = "\\";
    private static final String ALTERNATE_DOMAIN_ID_SEPARATOR = "/";

    private static final String USERPASSWORD_SCHEME_MD5 = "{md5}";
    private static final String USERPASSWORD_SCHEME_CRYPT = "{crypt}";
    private static final String USERPASSWORD_SCHEME_SHA = "{sha}";

    private static final String API_CACHE_NULL_NAME = new String("NULL");
    private static final long DEFAULT_API_CACHE_TTL = 60000;
    private static final long DEFAULT_API_REPORTING_INTERVAL = 3600000;

    private static final long SHUTDOWN_WAIT = 1000;

    private static final String MATCHING_FILTER = "(&({0}={1}))";
    private static final String DOUBLE_MATCHING_FILTER = "(&({0}={1})({2}={3}))";

    private static final Log logger = LogFactory.getLog(LDAPAggregator.class);

    private static ThreadLocal<LDAPConfig> ldapConfigTLS = new ThreadLocal<LDAPConfig>();

    private Map<String,LDAPConfig> configs = new LinkedHashMap<String,LDAPConfig>();
    private boolean domainPrefixRequired = false;

    private ConcurrentHashMap<String,APICacheElement> apiCache = new ConcurrentHashMap<String, APICacheElement>();
    private AtomicLong apiCachePuts = new AtomicLong(0);
    private AtomicLong apiCacheHits = new AtomicLong(0);
    private AtomicLong apiCacheMisses = new AtomicLong(0);
    private AtomicLong apiCacheReaps = new AtomicLong(0);

    private AtomicBoolean shutdown = new AtomicBoolean(false);
    private Thread apiCacheReaper = null;
    private Thread apiCacheReporter = null;

    /**
     * Construct LDAP aggregator with endpoint configurations map. Specify
     * configurations using an insertion ordered map to predict lookup order.
     *
     * @param configs domain endpoint LDAP configurations map
     * @param domainPrefixRequired require domain prefix on user/principal names
     */
    public LDAPAggregator(Map<String,LDAPConfig> configs, boolean domainPrefixRequired) {
        this(configs, domainPrefixRequired, DEFAULT_API_CACHE_TTL, DEFAULT_API_CACHE_TTL/5, DEFAULT_API_REPORTING_INTERVAL);
    }

    /**
     * Construct LDAP aggregator with endpoint configurations map and API
     * cache settings. Specify configurations using an insertion ordered map
     * to predict lookup order. Protected since intended for use in unit
     * tests only.
     *
     * @param configs  domain endpoint LDAP configurations map
     * @param domainPrefixRequired require domain prefix on user/principal names
     * @param apiCacheTTL cache element TTL in millis
     * @param apiCacheReapInterval cache reap interval in millis
     * @param apiCacheReportingInterval cache reporting interval in millis
     */
    protected LDAPAggregator(Map<String,LDAPConfig> configs, boolean domainPrefixRequired,
                             final long apiCacheTTL, final long apiCacheReapInterval, final long apiCacheReportingInterval) {
        // ensure configurations are hashed by lowercase domains
        for (Map.Entry<String,LDAPConfig> configEntry: configs.entrySet()) {
            this.configs.put((configEntry.getKey() != null ? configEntry.getKey().toLowerCase(): null), configEntry.getValue());
        }
        this.domainPrefixRequired = domainPrefixRequired;
        // start API cache reaping thread
        apiCacheReaper = new Thread(new Runnable() {
            public void run() {
                // reap API cache on intervals
                while (!shutdown.get()) {
                    // reap
                    try {
                        int reaped = 0;
                        long now = System.currentTimeMillis();
                        for (Iterator<Map.Entry<String,APICacheElement>> cacheEntryIter = apiCache.entrySet().iterator(); cacheEntryIter.hasNext();) {
                            Map.Entry<String,APICacheElement> cacheEntry = cacheEntryIter.next();
                            if (now-cacheEntry.getValue().timestamp > apiCacheTTL) {
                                cacheEntryIter.remove();
                                apiCacheReaps.incrementAndGet();
                                reaped++;
                            }
                        }
                        logger.debug("LDAPAggregatorAPICacheReaper reaped "+reaped+" cache entries");
                    } catch (Exception e) {
                        logger.error("LDAPAggregatorAPICacheReaper unexpected exception: "+e, e);
                    }
                    // wait on interval
                    synchronized (shutdown) {
                        if (!shutdown.get()) {
                            try {
                                shutdown.wait(apiCacheReapInterval);
                            } catch (InterruptedException ie) {
                            }
                        }
                    }
                }
            }
        }, "LDAPAggregatorAPICacheReaper");
        apiCacheReaper.setDaemon(true);
        apiCacheReaper.start();
        // start API cache reporting thread
        apiCacheReporter = new Thread(new Runnable() {
            public void run() {
                // generate API cache stats report on intervals
                while (!shutdown.get()) {
                    // log
                    logger.info("LDAPAggregatorAPICacheReporter stats: "+getAPICacheStats());
                    // wait on interval
                    synchronized (shutdown) {
                        if (!shutdown.get()) {
                            try {
                                shutdown.wait(apiCacheReportingInterval);
                            } catch (InterruptedException ie) {
                            }
                        }
                    }
                }
            }
        }, "LDAPAggregatorAPICacheReporter");
        apiCacheReporter.setDaemon(true);
        apiCacheReporter.start();
        logger.debug("LDAPAggregator instance created and started");
    }

    /**
     * Shutdown background cache threads.
     */
    public void shutdown() {
        // notify shutdown
        synchronized (shutdown) {
            shutdown.set(true);
            shutdown.notifyAll();
        }
        // join background cache threads
        try {
            apiCacheReporter.join(SHUTDOWN_WAIT);
            apiCacheReaper.join(SHUTDOWN_WAIT);
        } catch (InterruptedException ie) {
        }
        logger.debug("LDAPAggregator instance shutdown");
    }

    /**
     * Validate specified LDAP security principal DN and credential against a domain.
     *
     * @param domain domain or null for default domain
     * @param securityPrincipalDN security principal/user DN
     * @param securityCredential security credential/password
     * @return validated
     */
    public boolean validateSecurityCredentials(String domain, String securityPrincipalDN, String securityCredential) {
        // get domain LDAP configuration
        LDAPConfig config = configs.get(domain);
        if (config == null) {
            return false;
        }
        // delegate to domain LDAP configuration
        return authenticate(config, securityPrincipalDN, securityCredential, true) == Boolean.TRUE;
    }

    /**
     * LDAP authenticate specified security principal DN and credential.
     *
     * @param securityPrincipalDN principal/user DN
     * @param securityCredential credential/password
     * @param asSecurityPrincipal authenticate as security principal
     * @return authenticated
     */
    public boolean authenticate(String securityPrincipalDN, String securityCredential, boolean asSecurityPrincipal) {
        // check API cache
        String apiCacheKey = makeAPICacheKey("authenticate", securityPrincipalDN, securityCredential);
        Boolean authenticated = (Boolean)getApiCache(apiCacheKey);
        if (authenticated != null) {
            return authenticated;
        }
        // select domain LDAP configurations
        Collection<LDAPConfig> domainConfigs = selectDomainConfigsFromDN(securityPrincipalDN);
        if (domainConfigs == null) {
            return false;
        }
        // delegate to domain LDAP configurations
        authenticated = null;
        for (LDAPConfig config : domainConfigs) {
            authenticated = authenticate(config, securityPrincipalDN, securityCredential, asSecurityPrincipal);
            if (authenticated != null) {
                break;
            }
        }
        authenticated = (authenticated != null) ? authenticated : Boolean.FALSE;
        // add to API cache
        putApiCache(apiCacheKey, authenticated);
        return authenticated;
    }

    /**
     * Bind specified LDAP credentials looking up DN name for specified username.
     * Accepts user with domain prefix to address a specific LDAP configuration.
     *
     * @param username principal/user with optional domain prefix
     * @param password credential/password
     * @return bound/authenticated
     */
    public boolean bind(String username, String password) {
        // check API cache
        String apiCacheKey = makeAPICacheKey("bind", username, password);
        Boolean bound = (Boolean)getApiCache(apiCacheKey);
        if (bound != null) {
            return bound;
        }
        // select domain LDAP configurations
        Collection<LDAPConfig> domainConfigs = selectDomainConfigs(username);
        if (domainConfigs == null) {
            return false;
        }
        // delegate to domain LDAP configurations
        username = stripDomain(username);
        bound = null;
        for (LDAPConfig config : domainConfigs) {
            bound = bind(config, username, password);
            if (bound != null) {
                break;
            }
        }
        bound = (bound != null) ? bound : Boolean.FALSE;
        // add to API cache
        putApiCache(apiCacheKey, bound);
        return bound;
    }

    /**
     * Lookup LDAP DN name for specified user. Accepts user with domain prefix
     * to address a specific LDAP configuration.
     *
     * @param uid principal/user with optional domain prefix
     * @return principal/user DN name or null
     */
    public String selectUserDN(String uid) {
        // check API cache
        String apiCacheKey = makeAPICacheKey("selectUserDN", uid);
        String dn = (String)getApiCache(apiCacheKey);
        if (dn != null) {
            return (dn != API_CACHE_NULL_NAME) ? dn : null;
        }
        // select domain LDAP configurations
        Collection<LDAPConfig> domainConfigs = selectDomainConfigs(uid);
        if (domainConfigs == null) {
            return null;
        }
        // delegate to domain LDAP configurations
        uid = stripDomain(uid);
        dn = null;
        for (LDAPConfig config : domainConfigs) {
            dn = selectUserDN(config, uid);
            if (dn != null) {
                break;
            }
        }
        // add to API cache
        putApiCache(apiCacheKey, (dn != null ? dn : API_CACHE_NULL_NAME));
        return dn;
    }

    /**
     * Lookup LDAP DN name for specified role. Accepts role with domain prefix
     * to address a specific LDAP configuration.
     *
     * @param roleName role name with optional domain prefix
     * @return role DN name or null
     */
    public String selectRoleDN(String roleName) {
        // check API cache
        String apiCacheKey = makeAPICacheKey("selectRoleDN", roleName);
        String dn = (String)getApiCache(apiCacheKey);
        if (dn != null) {
            return (dn != API_CACHE_NULL_NAME) ? dn : null;
        }
        // select domain LDAP configurations
        Collection<LDAPConfig> domainConfigs = selectDomainConfigs(roleName);
        if (domainConfigs == null) {
            return null;
        }
        // delegate to domain LDAP configurations
        roleName = stripDomain(roleName);
        dn = null;
        for (LDAPConfig config : domainConfigs) {
            dn = selectRoleDN(config, roleName);
            if (dn != null) {
                break;
            }
        }
        // add to API cache
        putApiCache(apiCacheKey, (dn != null ? dn : API_CACHE_NULL_NAME));
        return dn;
    }

    /**
     * Lookup LDAP properties for specified user. Accepts user with domain prefix
     * to address a specific LDAP configuration.
     *
     * @param uid principal/user with optional domain prefix
     * @return user/principal properties or empty map
     */
    public HashMap<String,String> selectUserProperties(String uid) {
        // check API cache
        String apiCacheKey = makeAPICacheKey("selectUserProperties", uid);
        HashMap<String,String> userProperties = (HashMap<String,String>)getApiCache(apiCacheKey);
        if (userProperties != null) {
            return userProperties;
        }
        // select domain LDAP configurations
        Collection<LDAPConfig> domainConfigs = selectDomainConfigs(uid);
        if (domainConfigs == null) {
            return new HashMap<String,String>();
        }
        // delegate to domain LDAP configurations
        uid = stripDomain(uid);
        userProperties = null;
        for (LDAPConfig config : domainConfigs) {
            userProperties = selectUserProperties(config, uid);
            if (userProperties != null) {
                break;
            }
        }
        userProperties = (userProperties != null) ? userProperties : new HashMap<String,String>();
        // add to API cache
        putApiCache(apiCacheKey, userProperties);
        return userProperties;
    }

    /**
     * Lookup LDAP roles for specified user. Accepts user with domain prefix
     * to address a specific LDAP configuration. Return stripped role names.
     *
     * @param username principal/user with optional domain prefix
     * @return user/principal roles or empty array
     */
    public String[] selectRolesByUsername(String username) {
        return selectRolesByUsername(username, false);
    }

    /**
     * Lookup LDAP roles for specified user. Accepts user with domain prefix
     * to address a specific LDAP configuration. Optionally return domain
     * prefix on or stripped roles.
     *
     * @param username principal/user with optional domain prefix
     * @param prefixRoles return roles with domain prefix
     * @return user/principal roles or empty array
     */
    public String[] selectRolesByUsername(String username, boolean prefixRoles) {
        // check API cache
        String apiCacheKey = makeAPICacheKey("selectRolesByUsername", username, Boolean.toString(prefixRoles));
        String[] userRoles = (String[])getApiCache(apiCacheKey);
        if (userRoles != null) {
            return userRoles;
        }
        // select domain LDAP configurations
        Collection<LDAPConfig> domainConfigs = selectDomainConfigs(username);
        if (domainConfigs == null) {
            return new String[0];
        }
        // delegate to domain LDAP configurations
        username = stripDomain(username);
        userRoles = null;
        for (LDAPConfig config : domainConfigs) {
            userRoles = selectRolesByUsername(config, username, prefixRoles);
            if (userRoles != null) {
                break;
            }
        }
        userRoles = (userRoles != null) ? userRoles : new String[0];
        // add to API cache
        putApiCache(apiCacheKey, userRoles);
        return userRoles;
    }

    /**
     * Lookup LDAP roles for specified role. Accepts role with domain prefix
     * to address a specific LDAP configuration. Return stripped role names.
     *
     * @param roleName role name with optional domain prefix
     * @return user/principal roles or empty array
     */
    public String[] selectRolesByRoleName(String roleName) {
        return selectRolesByRoleName(roleName, false);
    }

    /**
     * Lookup LDAP roles for specified role. Accepts role with domain prefix
     * to address a specific LDAP configuration. Optionally return domain
     * prefix on or stripped roles.
     *
     * @param roleName role name with optional domain prefix
     * @param prefixRoles return roles with domain prefix
     * @return user/principal roles or empty array
     */
    public String[] selectRolesByRoleName(String roleName, boolean prefixRoles) {
        // check API cache
        String apiCacheKey = makeAPICacheKey("selectRolesByRoleName", roleName, Boolean.toString(prefixRoles));
        String[] roleRoles = (String[])getApiCache(apiCacheKey);
        if (roleRoles != null) {
            return roleRoles;
        }
        // select domain LDAP configurations
        Collection<LDAPConfig> domainConfigs = selectDomainConfigs(roleName);
        if (domainConfigs == null) {
            return new String[0];
        }
        // delegate to domain LDAP configurations
        roleName = stripDomain(roleName);
        roleRoles = null;
        for (LDAPConfig config : domainConfigs) {
            roleRoles = selectRolesByRoleName(config, roleName, prefixRoles);
            if (roleRoles != null) {
                break;
            }
        }
        roleRoles = (roleRoles != null) ? roleRoles : new String[0];
        // add to API cache
        putApiCache(apiCacheKey, roleRoles);
        return roleRoles;
    }

    /**
     * Lookup LDAP username for specified user. Accepts user with domain prefix
     * to address a specific LDAP configuration.
     *
     * @param uid principal/user with optional domain prefix
     * @return user/principal name or null
     */
    public String selectUser(String uid) {
        // check API cache
        String apiCacheKey = makeAPICacheKey("selectUser", uid);
        String user = (String)getApiCache(apiCacheKey);
        if (user != null) {
            return (user != API_CACHE_NULL_NAME) ? user : null;
        }
        // select domain LDAP configurations
        boolean [] configForDomain = new boolean[1];
        Collection<LDAPConfig> domainConfigs = selectDomainConfigs(uid, configForDomain);
        if (domainConfigs == null) {
            return null;
        }
        // delegate to domain LDAP configurations
        uid = stripDomain(uid);
        user = null;
        for (LDAPConfig config : domainConfigs) {
            user = selectUser(config, config.getPrincipalUidAttributeID(), uid);
            if (user != null) {
                if (configForDomain[0] && config.getDomain() != null) {
                    user = config.getDomain()+DOMAIN_ID_SEPARATOR+user;
                }
                break;
            }
        }
        // add to API cache
        putApiCache(apiCacheKey, (user != null ? user : API_CACHE_NULL_NAME));
        return user;
    }

    /**
     * Lookup LDAP username for specified user using a specified attribute. Accepts
     * user with domain prefix to address a specific LDAP configuration.
     *
     * @param attrId attribute to match against specified user/principal
     * @param attrValue principal/user with optional domain prefix
     * @return user/principal name or null
     */
    public String selectUser(String attrId, String attrValue) {
        // check API cache
        String apiCacheKey = makeAPICacheKey("selectUser", attrId, attrValue);
        String user = (String)getApiCache(apiCacheKey);
        if (user != null) {
            return (user != API_CACHE_NULL_NAME) ? user : null;
        }
        // select domain LDAP configurations
        boolean [] configForDomain = new boolean[1];
        Collection<LDAPConfig> domainConfigs = selectDomainConfigs(attrValue, configForDomain);
        if (domainConfigs == null) {
            return null;
        }
        // delegate to domain LDAP configurations
        attrValue = stripDomain(attrValue);
        user = null;
        for (LDAPConfig config : domainConfigs) {
            user = selectUser(config, attrId, attrValue);
            if (user != null) {
                if (configForDomain[0] && config.getDomain() != null) {
                    user = config.getDomain()+DOMAIN_ID_SEPARATOR+user;
                }
                break;
            }
        }
        // add to API cache
        putApiCache(apiCacheKey, (user != null ? user : API_CACHE_NULL_NAME));
        return user;
    }

    /**
     * Lookup LDAP credentials for specified user. Accepts user with domain prefix
     * to address a specific LDAP configuration.
     *
     * @param uid principal/user with optional domain prefix
     * @param schemeName authentication scheme name
     * @return credentials or empty map
     */
    public HashMap<String,List<Object>> selectCredentials(String uid, String schemeName) {
        // check API cache
        String apiCacheKey = makeAPICacheKey("selectCredentials", uid, schemeName);
        HashMap<String,List<Object>> credentials = (HashMap<String,List<Object>>)getApiCache(apiCacheKey);
        if (credentials != null) {
            return credentials;
        }
        // select domain LDAP configurations
        Collection<LDAPConfig> domainConfigs = selectDomainConfigs(uid);
        if (domainConfigs == null) {
            return new HashMap<String,List<Object>>();
        }
        // delegate to domain LDAP configurations
        uid = stripDomain(uid);
        credentials = null;
        for (LDAPConfig config : domainConfigs) {
            credentials = selectCredentials(config, uid, schemeName);
            if (credentials != null) {
                break;
            }
        }
        credentials = (credentials != null) ? credentials : new HashMap<String,List<Object>>();
        // add to API cache
        putApiCache(apiCacheKey, credentials);
        return credentials;
    }

    /**
     * Load username for specified user and certificate. Accepts user with domain
     * prefix to address a specific LDAP configuration.
     *
     * @param lookupValue principal/user with optional domain prefix
     * @param certificate user certificate
     * @param schemeName authentication scheme name
     * @return loaded user/principal name or null
     */
    public String loadUID(String lookupValue, X509Certificate certificate, String schemeName) {
        // check API cache
        String apiCacheKey = makeAPICacheKey("loadUID", lookupValue, certificate.getSubjectX500Principal().getName(), schemeName);
        String user = (String)getApiCache(apiCacheKey);
        if (user != null) {
            return (user != API_CACHE_NULL_NAME) ? user : null;
        }
        // select domain LDAP configurations
        Collection<LDAPConfig> domainConfigs = selectDomainConfigs(lookupValue);
        if (domainConfigs == null) {
            return null;
        }
        // delegate to domain LDAP configurations
        lookupValue = stripDomain(lookupValue);
        user = null;
        for (LDAPConfig config : domainConfigs) {
            user = loadUID(config, lookupValue, certificate, schemeName);
            if (user != null) {
                break;
            }
        }
        // add to API cache
        putApiCache(apiCacheKey, (user != null ? user : API_CACHE_NULL_NAME));
        return user;
    }

    /**
     * Update security credential for specified principal. Accepts user with domain
     * prefix to address a specific LDAP configuration.
     *
     * @param principal principal/user with optional domain prefix
     * @param credential credential/password
     * @return updated
     */
    public boolean updateCredential(String principal, String credential) {
        // select domain LDAP configurations
        Collection<LDAPConfig> domainConfigs = selectDomainConfigs(principal);
        if (domainConfigs == null) {
            return false;
        }
        // delegate to domain LDAP configurations
        principal = stripDomain(principal);
        Boolean updated = null;
        for (LDAPConfig config : domainConfigs) {
            updated = updateCredential(config, principal, credential);
            if (updated != null) {
                break;
            }
        }
        updated = (updated != null) ? updated : Boolean.FALSE;
        // clear cache on update
        if (updated) {
            apiCache.clear();
        }
        return updated;
    }

    /**
     * Return array of API cache stats with these elements: hits, misses,
     * puts, reaps, and size.
     *
     * @return API cache stats map
     */
    public Map<String,Long> getAPICacheStats() {
        Map<String,Long> apiCacheStats = new LinkedHashMap<String,Long>();
        apiCacheStats.put("puts", apiCachePuts.longValue());
        apiCacheStats.put("hits", apiCacheHits.longValue());
        apiCacheStats.put("misses", apiCacheMisses.longValue());
        apiCacheStats.put("reaps", apiCacheReaps.longValue());
        apiCacheStats.put("size", (long)apiCache.size());
        return apiCacheStats;
    }

    /**
     * Test set attribute for specified principal. Accepts user with domain
     * prefix to address a specific LDAP configuration. Protected since intended
     * for use in unit tests only.
     *
     * @param principal principal/user with optional domain prefix
     * @param op set attribute operation
     * @param attrId attribute name
     * @param attrValue attribute value or null
     * @return updated
     */
    protected boolean setAttribute(String principal, AttributeOperation op, String attrId, Object attrValue) {
        // select domain LDAP configurations
        Collection<LDAPConfig> domainConfigs = selectDomainConfigs(principal);
        if (domainConfigs == null) {
            return false;
        }
        // delegate to domain LDAP configurations
        principal = stripDomain(principal);
        Boolean updated = null;
        for (LDAPConfig config : domainConfigs) {
            updated = setAttribute(config, principal, op, attrId, attrValue);
            if (updated != null) {
                break;
            }
        }
        updated = (updated != null) ? updated : Boolean.FALSE;
        // clear cache on update
        if (updated) {
            apiCache.clear();
        }
        return updated;
    }

    /**
     * Select LDAP configurations based on specified identifier domain prefix.
     *
     * @param identifier principal/user with optional domain prefix
     * @return LDAP configurations to search
     */
    private Collection<LDAPConfig> selectDomainConfigs(String identifier) {
        return selectDomainConfigs(identifier, new boolean[1]);
    }

    /**
     * Select LDAP configurations based on specified identifier domain prefix.
     *
     * @param identifier principal/user with optional domain prefix
     * @param configForDomain returned configuration for domain prefix
     * @return LDAP configurations to search
     */
    private Collection<LDAPConfig> selectDomainConfigs(String identifier, boolean [] configForDomain) {
        // extract lowercase domain from identifier
        String domain = extractDomain(identifier);
        // return all configurations if no domain specified, if
        // there is no default configuration, and domain prefixes
        // are not required
        if (domain == null && !configs.containsKey(domain) && !domainPrefixRequired) {
            return configs.values();
        }
        // lookup configuration for domain or default domain
        LDAPConfig config = configs.get(domain);
        if (config != null) {
            configForDomain[0] = true;
            return Arrays.asList(config);
        }
        return null;
    }

    /**
     * Select LDAP configurations based on specified user DN.
     *
     * @param userDN principal/user DN
     * @return LDAP configurations to search
     */
    private Collection<LDAPConfig> selectDomainConfigsFromDN(String userDN) {
        // use canonicalized DN for comparison
        userDN = canonicalizeDN(userDN);
        // lookup domain configuration based on users context DN matched to specified DN
        for (LDAPConfig config : configs.values()) {
            for (String usersCtxDN : config.getUsersCtxDNs()) {
                if (userDN.endsWith(',' + canonicalizeDN(usersCtxDN))) {
                    return Arrays.asList(config);
                }
            }
        }
        // fallback returning all configurations to search against
        return configs.values();
    }

    /**
     * LDAP authenticate specified security principal DN and credential.
     *
     * @param config LDAP configuration
     * @param securityPrincipalDN principal/user DN
     * @param securityCredential credential/password
     * @param asSecurityPrincipal authenticate as security principal
     * @return authenticated or null
     */
    private static Boolean authenticate(LDAPConfig config, String securityPrincipalDN, String securityCredential, boolean asSecurityPrincipal) {
        // create LDAP context to authenticate and close
        InitialLdapContext ctx = null;
        try {
            // create LDAP context
            ctx = createLdapInitialContext(config, securityPrincipalDN, securityCredential);
            // assert LDAP users search if authenticating as security principal
            if (asSecurityPrincipal) {
                for (String usersCtxDN : config.getUsersCtxDNs()) {
                    ctx.getAttributes(usersCtxDN);
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Authenticate DN " + securityPrincipalDN + " succeeded for domain " + config.getDomain());
            }
            return true;
        } catch (NamingException ne) {
            if (isUserNotFoundAuthenticationException(ne)) {
                // user not found in domain
                if (logger.isDebugEnabled()) {
                    logger.debug("Authenticate DN " + securityPrincipalDN + " not found for domain " + config.getDomain());
                }
                return null;
            }
            if (isAuthenticationException(ne)) {
                // user authentication failed in domain
                logger.debug("Authenticate DN "+securityPrincipalDN+" failed for domain "+config.getDomain());
                return false;
            }
            // authentication failed
            logger.debug("Authenticate DN "+securityPrincipalDN+" failed for domain "+config.getDomain()+": "+ne, ne);
            return false;
        } finally {
            close(ctx);
        }
    }

    /**
     * Bind specified LDAP credentials looking up DN name for specified username.
     *
     * @param config LDAP configuration
     * @param username principal/user name
     * @param password credential/password
     * @return bound/authenticated or null
     */
    private static Boolean bind(LDAPConfig config, String username, String password) {
        // get DN name for username
        String dnName = selectUserDN(config, username);
        if (dnName == null) {
            logger.debug("Bind DN for "+username+" not found for domain "+config.getDomain());
            return null;
        }
        // authenticate DN name and password
        Boolean bound = authenticate(config, dnName, password, false);
        if (bound == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Bind DN " + dnName + " not found for domain " + config.getDomain());
            }
        } else if (bound) {
            if (logger.isDebugEnabled()) {
                logger.debug("Bind DN " + dnName + " succeeded for domain " + config.getDomain());
            }
        } else {
            logger.debug("Bind DN "+dnName+" failed for domain "+config.getDomain());
        }
        return bound;
    }

    /**
     * Lookup LDAP DN name for specified user.
     *
     * @param config LDAP configuration
     * @param uid principal/user name
     * @return principal/user DN name or null
     */
    private static String selectUserDN(LDAPConfig config, String uid) {
        InitialLdapContext ctx = null;
        StartTlsResponse tls = null;
        try {
            // bind LDAP context and start TLS
            ctx = createLdapInitialContext(config);
            if (config.getEnableStartTLS()) {
                tls = startTLS(config, ctx);
            }
            // search LDAP users context
            String principalUidAttrName = config.getPrincipalUidAttributeID();
            for (String usersCtxDN : config.getUsersCtxDNs()) {
                NamingEnumeration answer = ctx.search(usersCtxDN, MATCHING_FILTER, new Object[]{principalUidAttrName, uid}, getSearchControls(config));
                while (answer.hasMore()) {
                    SearchResult result = (SearchResult) answer.next();
                    // return matching user DN
                    Attribute uidAttr = result.getAttributes().get(principalUidAttrName);
                    if (uidAttr != null && uidAttr.get() != null && uidAttr.get().toString() != null) {
                        String dn = result.getName() + "," + usersCtxDN;
                        if (logger.isDebugEnabled()) {
                            logger.debug("SelectUserDN " + uid + " succeeded for domain " + config.getDomain() + " -> " + dn);
                        }
                        return dn;
                    }
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("SelectUserDN " + uid + " not found for domain " + config.getDomain());
            }
        } catch (NamingException ne) {
            logger.debug("SelectUserDN "+uid+" failed for domain "+config.getDomain()+": "+ne, ne);
        } catch (IOException ioe) {
            logger.debug("SelectUserDN "+uid+" failed for domain "+config.getDomain()+": "+ioe, ioe);
        } finally {
            close(tls);
            close(ctx);
        }
        return null;
    }

    /**
     * Lookup LDAP DN name for specified role.
     *
     * @param config LDAP configuration
     * @param roleName role name
     * @return role DN name or null
     */
    private static String selectRoleDN(LDAPConfig config, String roleName) {
        InitialLdapContext ctx = null;
        StartTlsResponse tls = null;
        try {
            // bind LDAP context and start TLS
            ctx = createLdapInitialContext(config);
            if (config.getEnableStartTLS()) {
                tls = startTLS(config, ctx);
            }
            // search LDAP roles context
            String roleAttributeIDName = config.getRoleAttributeID();
            for (String rolesCtxDN : config.getRolesCtxDNs()) {
                NamingEnumeration answer = ctx.search(rolesCtxDN, MATCHING_FILTER, new Object[]{roleAttributeIDName, roleName}, getSearchControls(config));
                while (answer.hasMore()) {
                    SearchResult result = (SearchResult) answer.next();
                    // return matching role DN
                    Attribute uidAttr = result.getAttributes().get(roleAttributeIDName);
                    if (uidAttr != null && uidAttr.get() != null && uidAttr.get().toString() != null) {
                        String dn = result.getName() + "," + rolesCtxDN;
                        if (logger.isDebugEnabled()) {
                            logger.debug("SelectRoleDN " + roleName + " succeeded for domain " + config.getDomain() + " -> " + dn);
                        }
                        return dn;
                    }
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("SelectRoleDN " + roleName + " not found for domain " + config.getDomain());
            }
        } catch (NamingException ne) {
            logger.debug("SelectRoleDN "+roleName+" failed for domain "+config.getDomain()+": "+ne, ne);
        } catch (IOException ioe) {
            logger.debug("SelectRoleDN "+roleName+" failed for domain "+config.getDomain()+": "+ioe, ioe);
        } finally {
            close(tls);
            close(ctx);
        }
        return null;
    }

    /**
     * Lookup LDAP properties for specified user.
     *
     * @param config LDAP configuration
     * @param uid principal/user name
     * @return user/principal properties or null
     */
    private static HashMap<String,String> selectUserProperties(LDAPConfig config, String uid) {
        InitialLdapContext ctx = null;
        StartTlsResponse tls = null;
        try {
            // bind LDAP context and start TLS
            ctx = createLdapInitialContext(config);
            if (config.getEnableStartTLS()) {
                tls = startTLS(config, ctx);
            }
            // setup properties query map
            Map<String,String> userPropertiesQueryMap = parseQueryString(config.getUserPropertiesQueryString());
            // search LDAP users context
            String principalUidAttrName = config.getPrincipalUidAttributeID();
            for (String usersCtxDN : config.getUsersCtxDNs()) {
                NamingEnumeration answer = ctx.search(usersCtxDN, MATCHING_FILTER, new Object[]{principalUidAttrName, uid}, getSearchControls(config));
                if (answer.hasMore()) {
                    HashMap<String,String> userProperties = new HashMap<String,String>();
                    do {
                        SearchResult result = (SearchResult) answer.next();
                        Attributes attrs = result.getAttributes();
                        // return matching mapped user properties
                        for (Map.Entry<String,String> userProperty : userPropertiesQueryMap.entrySet()) {
                            Attribute attr = attrs.get(userProperty.getKey());
                            if (attr != null) {
                                Object attrValue = attr.get();
                                if (attrValue != null && attrValue.toString() != null) {
                                    userProperties.put(userProperty.getValue(), attrValue.toString());
                                }
                            }
                        }
                    } while (answer.hasMore());
                    if (logger.isDebugEnabled()) {
                        logger.debug("SelectUserProperties " + uid + " succeeded for domain " + config.getDomain() + " -> " + userProperties);
                    }
                    return userProperties;
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("SelectUserProperties " + uid + " not found for domain " + config.getDomain());
            }
            return null;
        } catch (NamingException ne) {
            logger.debug("SelectUserProperties "+uid+" failed for domain "+config.getDomain()+": "+ne, ne);
        } catch (IOException ioe) {
            logger.debug("SelectUserProperties "+uid+" failed for domain "+config.getDomain()+": "+ioe, ioe);
        } finally {
            close(tls);
            close(ctx);
        }
        return null;
    }

    /**
     * Lookup LDAP roles for specified user.
     *
     * @param config LDAP configuration
     * @param username principal/user name
     * @param prefixRoles return roles with domain prefix
     * @return user/principal roles or null
     */
    private static String[] selectRolesByUsername(LDAPConfig config, String username, boolean prefixRoles) {
        // select roles by username or DN
        String user = username;
        if (!"UID".equalsIgnoreCase(config.getRoleMatchingMode())) {
            user = selectUserDN(config, username);
            if (user == null) {
                return null;
            }
        }
        // select user roles
        return selectRolesByMember(config, user, prefixRoles);
    }

    /**
     * Lookup LDAP roles for specified role.
     *
     * @param config LDAP configuration
     * @param roleName role name
     * @param prefixRoles return roles with domain prefix
     * @return role roles or null
     */
    private static String[] selectRolesByRoleName(LDAPConfig config, String roleName, boolean prefixRoles) {
        // select roles by role or DN
        String role = roleName;
        if (!"UID".equalsIgnoreCase(config.getRoleMatchingMode())) {
            role = selectRoleDN(config, roleName);
            if (role == null) {
                return null;
            }
        }
        // select role roles
        return selectRolesByMember(config, role, prefixRoles);
    }

    /**
     * Lookup LDAP roles for specified member. Members should be specified
     * with DN or name/id based on role matching mode.
     *
     * @param config LDAP configuration
     * @param member member DN name or id/name
     * @param prefixRoles return roles with domain prefix
     * @return member roles or null
     */
    private static String[] selectRolesByMember(LDAPConfig config, String member, boolean prefixRoles) {
        // select member roles
        InitialLdapContext ctx = null;
        StartTlsResponse tls = null;
        try {
            // bind LDAP context and start TLS
            ctx = createLdapInitialContext(config);
            if (config.getEnableStartTLS()) {
                tls = startTLS(config, ctx);
            }
            // search LDAP roles context
            String uidAttributeID = config.getUidAttributeID();
            String roleAttrName = config.getRoleAttributeID();
            for (String rolesCtxDN : config.getRolesCtxDNs()) {
                NamingEnumeration answer = ctx.search(rolesCtxDN, MATCHING_FILTER, new Object[]{uidAttributeID, member}, getSearchControls(config));
                if (answer.hasMore()) {
                    List<String> userRoles = new ArrayList<String>();
                    do {
                        SearchResult result = (SearchResult) answer.next();
                        Attributes attrs = result.getAttributes();
                        // return role attributes
                        Attribute roles = attrs.get(roleAttrName);
                        if (roles != null) {
                            for (int i = 0, limit = roles.size(); (i < limit); i++) {
                                Object roleValue = roles.get(i);
                                if (roleValue != null && roleValue.toString() != null) {
                                    String role = roleValue.toString();
                                    if (prefixRoles) {
                                        role = config.getDomain()+DOMAIN_ID_SEPARATOR+role;
                                    }
                                    userRoles.add(role);
                                }
                            }
                        }
                    } while (answer.hasMore());
                    if (logger.isDebugEnabled()) {
                        logger.debug("SelectRolesByMember " + member + " succeeded for domain " + config.getDomain() + " -> " + userRoles);
                    }
                    return userRoles.toArray(new String[userRoles.size()]);
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("SelectRolesByMember " + member + " not found for domain " + config.getDomain());
            }
            return null;
        } catch (NamingException ne) {
            logger.debug("SelectRolesByMember "+member+" failed for domain "+config.getDomain()+": "+ne, ne);
        } catch (IOException ioe) {
            logger.debug("SelectRolesByMember "+member+" failed for domain "+config.getDomain()+": "+ioe, ioe);
        } finally {
            close(tls);
            close(ctx);
        }
        return null;
    }

    /**
     * Lookup LDAP username for specified user using a specified attribute.
     *
     * @param config LDAP configuration
     * @param attrId attribute to match against specified user/principal
     * @param attrValue principal/user name
     * @return user/principal name or null
     */
    private static String selectUser(LDAPConfig config, String attrId, String attrValue) {
        InitialLdapContext ctx = null;
        StartTlsResponse tls = null;
        try {
            // bind LDAP context and start TLS
            ctx = createLdapInitialContext(config);
            if (config.getEnableStartTLS()) {
                tls = startTLS(config, ctx);
            }
            // search LDAP users context
            String principalUidAttrName = config.getPrincipalUidAttributeID();
            for (String usersCtxDN : config.getUsersCtxDNs()) {
                NamingEnumeration answer = ctx.search(usersCtxDN, MATCHING_FILTER, new Object[]{attrId, attrValue}, getSearchControls(config));
                while (answer.hasMore()) {
                    SearchResult result = (SearchResult) answer.next();
                    // return matching user
                    Attribute uidAttr = result.getAttributes().get(principalUidAttrName);
                    if (uidAttr != null) {
                        Object uidValue = uidAttr.get();
                        if (uidValue != null && uidValue.toString() != null) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("SelectUser " + attrId + "=" + attrValue + " succeeded for domain " + config.getDomain() + " -> " + uidValue);
                            }
                            return uidValue.toString();
                        }
                    }
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("SelectUser " + attrId + "=" + attrValue + " not found for domain " + config.getDomain());
            }
        } catch (NamingException ne) {
            logger.debug("SelectUser "+attrId+"="+attrValue+" failed for domain "+config.getDomain()+": "+ne, ne);
        } catch (IOException ioe) {
            logger.debug("SelectUser "+attrId+"="+attrValue+" failed for domain "+config.getDomain()+": "+ioe, ioe);
        } finally {
            close(tls);
            close(ctx);
        }
        return null;
    }

    /**
     * Lookup LDAP credentials for specified user.
     *
     * @param config LDAP configuration
     * @param uid principal/user name
     * @param schemeName authentication scheme name
     * @return credentials or null
     */
    private static HashMap<String,List<Object>> selectCredentials(LDAPConfig config, String uid, String schemeName) {
        InitialLdapContext ctx = null;
        StartTlsResponse tls = null;
        try {
            // bind LDAP context and start TLS
            ctx = createLdapInitialContext(config);
            if (config.getEnableStartTLS()) {
                tls = startTLS(config, ctx);
            }
            // setup credentials query map
            Map<String,String> credentialsQueryMap = parseQueryString(config.getCredentialQueryString());
            // search LDAP users context
            String principalLookupAttrName = config.getPrincipalLookupAttributeID();
            if (principalLookupAttrName == null || principalLookupAttrName.length() == 0 ||
                    STRONG_AUTHENTICATION_SCHEME.equalsIgnoreCase(schemeName)) {
                principalLookupAttrName = config.getPrincipalUidAttributeID();
            }
            for (String usersCtxDN : config.getUsersCtxDNs()) {
                NamingEnumeration answer = ctx.search(usersCtxDN, MATCHING_FILTER, new Object[]{principalLookupAttrName, uid}, getSearchControls(config));
                if (answer.hasMore()) {
                    HashMap<String,List<Object>> credentials = new HashMap<String,List<Object>>();
                    do {
                        SearchResult result = (SearchResult) answer.next();
                        Attributes attrs = result.getAttributes();
                        // return matching mapped credentials attributes
                        for (Map.Entry<String, String> credential : credentialsQueryMap.entrySet()) {
                            Attribute attr = attrs.get(credential.getKey());
                            if (attr != null) {
                                for (NamingEnumeration attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
                                    Object attrValue = attrEnum.next();
                                    if (attrValue != null) {
                                        // convert credential attribute to string value if possible
                                        String attrStringValue = null;
                                        if (attrValue.getClass().isArray()) {
                                            try {
                                                attrStringValue = new String((byte[]) attrValue, "UTF-8");
                                            } catch (UnsupportedEncodingException uee) {
                                            }
                                        } else if (attrValue instanceof String) {
                                            attrStringValue = (String) attrValue;
                                        }
                                        if (attrStringValue != null) {
                                            // add to credentials attributes list
                                            List<Object> attrValues = credentials.get(credential.getValue());
                                            if (attrValues == null) {
                                                attrValues = new ArrayList<Object>();
                                                credentials.put(credential.getValue(), attrValues);
                                            }
                                            attrValues.add(attrStringValue != null ? stripUserPasswordSchemes(attrStringValue) : attrValue);
                                        }
                                    }
                                }
                            }
                        }
                    } while (answer.hasMore());
                    if (logger.isDebugEnabled()) {
                        logger.debug("SelectCredentials " + uid + " succeeded for domain " + config.getDomain() + " -> " + credentials);
                    }
                    return credentials;
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("SelectCredentials " + uid + " not found for domain " + config.getDomain());
            }
            return null;
        } catch (NamingException ne) {
            logger.debug("SelectCredentials "+uid+" failed for domain "+config.getDomain()+": "+ne, ne);
        } catch (IOException ioe) {
            logger.debug("SelectCredentials "+uid+" failed for domain "+config.getDomain()+": "+ioe, ioe);
        } finally {
            close(tls);
            close(ctx);
        }
        return null;
    }

    /**
     * Load username for specified user and certificate.
     *
     * @param config LDAP configuration
     * @param lookupValue principal/user name
     * @param certificate user certificate
     * @param schemeName authentication scheme name
     * @return loaded user/principal name or null
     */
    private static String loadUID(LDAPConfig config, String lookupValue, X509Certificate certificate, String schemeName) {
        // validate certificate
        String certificateName = certificate.getSubjectX500Principal().getName();
        byte[] derEncodedCertificate;
        try {
            derEncodedCertificate = certificate.getEncoded();
        } catch (Exception e) {
            throw new RuntimeException("LoadUID "+lookupValue+" with certificate "+certificateName+" unable to DER encode certificate: "+e, e);
        }
        // load user
        InitialLdapContext ctx = null;
        StartTlsResponse tls = null;
        try {
            // bind LDAP context and start TLS
            ctx = createLdapInitialContext(config);
            if (config.getEnableStartTLS()) {
                tls = startTLS(config, ctx);
            }
            // setup credentials query map
            Map<String,String> credentialsQueryMap = parseQueryString(config.getCredentialQueryString());
            // search LDAP users context
            String principalUidAttrName = config.getPrincipalUidAttributeID();
            String principalLookupAttrName = config.getPrincipalLookupAttributeID();
            if (principalLookupAttrName == null || principalLookupAttrName.length() == 0 ||
                    STRONG_AUTHENTICATION_SCHEME.equalsIgnoreCase(schemeName)) {
                principalLookupAttrName = config.getPrincipalUidAttributeID();
            }
            String certificateAttrName = config.getUserCertificateAttributeID();
            for (String usersCtxDN : config.getUsersCtxDNs()) {
                NamingEnumeration answer = ctx.search(usersCtxDN, DOUBLE_MATCHING_FILTER, new Object[] {principalLookupAttrName, lookupValue, certificateAttrName, derEncodedCertificate}, getSearchControls(config));
                while (answer.hasMore()) {
                    SearchResult result = (SearchResult) answer.next();
                    // return loaded user
                    Attribute uidAttr = result.getAttributes().get(principalUidAttrName);
                    if (uidAttr != null) {
                        Object uidValue = uidAttr.get();
                        if (uidValue != null && uidValue.toString() != null) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("LoadUID " + lookupValue + " with certificate " + certificateName + " succeeded for domain " + config.getDomain() + " -> " + uidValue);
                            }
                            return uidValue.toString();
                        }
                    }
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("LoadUID " + lookupValue + " with certificate " + certificateName + " not found for domain " + config.getDomain());
            }
        } catch (NamingException ne) {
            logger.debug("LoadUID "+lookupValue+" with certificate "+certificateName+" failed for domain "+config.getDomain()+": "+ne, ne);
        } catch (IOException ioe) {
            logger.debug("LoadUID "+lookupValue+" with certificate "+certificateName+" failed for domain "+config.getDomain()+": "+ioe, ioe);
        } finally {
            close(tls);
            close(ctx);
        }
        return null;
    }

    /**
     * Update security credential for specified principal.
     *
     * @param config LDAP configuration
     * @param principal principal/user name
     * @param credential credential/password
     * @return updated or null
     */
    private static Boolean updateCredential(LDAPConfig config, String principal, String credential) {
        // get DN name for principal
        String dnName = selectUserDN(config, principal);
        if (dnName == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("UpdateCredential for " + principal + " not found for domain " + config.getDomain());
            }
            return null;
        }
        // update credential
        InitialLdapContext ctx = null;
        try {
            // setup update credential attribute
            Attributes updateAttr = new BasicAttributes();
            updateAttr.put(config.getUpdateableCredentialAttributeID(), credential);
            // bind LDAP context
            ctx = createLdapInitialContext(config);
            // replace credential attribute
            ctx.modifyAttributes(dnName, InitialLdapContext.REPLACE_ATTRIBUTE, updateAttr);
            if (logger.isDebugEnabled()) {
                logger.debug("UpdateCredential for " + principal + " succeeded for domain " + config.getDomain());
            }
            return true;
        } catch (NamingException ne) {
            logger.debug("UpdateCredential for "+principal+" failed for domain "+config.getDomain()+": "+ne, ne);
            return false;
        } finally {
            close(ctx);
        }
    }

    /**
     * Test set attribute for specified principal.
     *
     * @param config LDAP configuration
     * @param principal principal/user name
     * @param op set attribute operation
     * @param attrId attribute name
     * @param attrValue attribute value or null
     * @return updated or null
     */
    private static Boolean setAttribute(LDAPConfig config, String principal, AttributeOperation op, String attrId, Object attrValue) {
        // get DN name for principal
        String dnName = selectUserDN(config, principal);
        if (dnName == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("SetAttribute for " + principal + " not found for domain " + config.getDomain());
            }
            return null;
        }
        // update credential
        InitialLdapContext ctx = null;
        try {
            // setup update attribute
            Attributes updateAttr = new BasicAttributes();
            updateAttr.put(attrId, attrValue);
            // bind LDAP context
            ctx = createLdapInitialContext(config);
            // modify attribute
            int attributeOperation;
            switch (op) {
                case ADD: attributeOperation = InitialLdapContext.ADD_ATTRIBUTE; break;
                case REMOVE: attributeOperation = InitialLdapContext.REMOVE_ATTRIBUTE; break;
                default: attributeOperation = InitialLdapContext.REPLACE_ATTRIBUTE; break;
            }
            ctx.modifyAttributes(dnName, attributeOperation, updateAttr);
            if (logger.isDebugEnabled()) {
                logger.debug("SetAttribute " + attrId + "=" + attrValue + " for " + principal + " succeeded for domain " + config.getDomain());
            }
            return true;
        } catch (NamingException ne) {
            logger.debug("SetAttribute "+attrId+"="+attrValue+" for "+principal+" failed for domain "+config.getDomain()+": "+ne, ne);
            return false;
        } finally {
            close(ctx);
        }
    }

    /**
     * Create LDAP context for configured security principal.
     *
     * @param config LDAP configuration
     * @return LDAP context
     * @throws NamingException
     */
    private static InitialLdapContext createLdapInitialContext(LDAPConfig config) throws NamingException {
        // create initial context using configured security principal and credential
        return createLdapInitialContext(config, config.getSecurityPrincipal(), config.getSecurityCredential());
    }

    /**
     * Create LDAP context for specified credentials.
     *
     * @param config LDAP configuration
     * @param securityPrincipal LDAP principal
     * @param securityCredential LDAP credential
     * @return LDAP context
     * @throws NamingException
     */
    private static InitialLdapContext createLdapInitialContext(LDAPConfig config, String securityPrincipal, String securityCredential) throws NamingException {
        // setup LDAP context env
        Properties env = new Properties();
        env.setProperty(Context.INITIAL_CONTEXT_FACTORY, config.getInitialContextFactory());
        env.setProperty(Context.SECURITY_AUTHENTICATION, config.getSecurityAuthentication());
        env.setProperty(Context.PROVIDER_URL, config.getProviderURL());
        env.setProperty(Context.SECURITY_PROTOCOL, config.getSecurityProtocol());
        env.setProperty(Context.SECURITY_PRINCIPAL, securityPrincipal);
        env.setProperty(Context.SECURITY_CREDENTIALS, securityCredential);
        env.setProperty(Context.REFERRAL, "follow");
        env.setProperty(LDAPSocketFactory.FACTORY_SOCKET_CONTEXT_PARAM, LDAPSocketFactory.class.getName());
        try {
            // set LDAP config TLS
            ldapConfigTLS.set(config);
            // login to LDAP server
            return new InitialLdapContext(env, null);
        } finally {
            // clear LDAP config TLS
            ldapConfigTLS.remove();
        }
    }

    /**
     * Get LDAP configuration TLS. This wrapped in a public method to avoid an
     * IllegalAccessException when read from the InitialLdapContextSSLSocketFactory
     * constructor from potentially another class loader.
     *
     * @return LDAP configuration
     */
    public static LDAPConfig getLDAPConfigTLS() {
        return ldapConfigTLS.get();
    }

    /**
     * Start TLS for LDAP context.
     *
     * @param config LDAP configuration
     * @param ctx LDAP context
     * @return TLS response
     * @throws NamingException
     * @throws IOException
     */
    private static StartTlsResponse startTLS(LDAPConfig config, InitialLdapContext ctx) throws NamingException, IOException {
        StartTlsResponse tls = (StartTlsResponse) ctx.extendedOperation(new StartTlsRequest());
        tls.negotiate(LDAPSocketFactory.createSSLSocketFactory(config));
        return tls;
    }

    /**
     * Get LDAP search controls for LDAP configuration.
     *
     * @param config LDAP configuration
     * @return LDAP search controls
     */
    private static SearchControls getSearchControls(LDAPConfig config) {
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope("SUBTREE".equalsIgnoreCase(config.getLdapSearchScope()) ? SearchControls.SUBTREE_SCOPE : SearchControls.ONELEVEL_SCOPE);
        return searchControls;
    }

    /**
     * Parse LDAP configuration query string into query attributes map.
     *
     * @param queryString query string
     * @return query attributes map
     */
    private static Map<String,String> parseQueryString(String queryString) {
        if (queryString == null || queryString.length() == 0) {
            throw new IllegalArgumentException("Missing query string");
        }
        Map<String,String> attributes = new HashMap<String,String>();
        StringTokenizer tokens = new StringTokenizer(queryString, ",");
        while (tokens.hasMoreTokens()) {
            String attributePair = tokens.nextToken();
            int separatorIndex = attributePair.indexOf('=');
            if (separatorIndex == -1) {
                throw new IllegalArgumentException("Invalid query string attribute pair: "+attributePair);
            }
            attributes.put(attributePair.substring(0, separatorIndex).trim(), attributePair.substring(separatorIndex+1).trim());
        }
        return attributes;
    }

    /**
     * Check NamingException for user not found message.
     *
     * @param ne naming exception
     * @return user not found
     */
    private static boolean isUserNotFoundAuthenticationException(NamingException ne) {
        // test exception detail for OpenLDAP or AD error messages
        String exceptionDetail = ne.toString(true).toLowerCase();
        return (exceptionDetail.contains("code 49") || exceptionDetail.contains("(49)")) &&
                (exceptionDetail.contains("data 525") || exceptionDetail.contains("not found"));
    }

    /**
     * Check NamingException for authentication failure message.
     *
     * @param ne naming exception
     * @return authentication failure
     */
    private static boolean isAuthenticationException(NamingException ne) {
        // test exception detail for OpenLDAP or AD error messages
        String exceptionDetail = ne.toString(true).toLowerCase();
        return exceptionDetail.contains("code 49") || exceptionDetail.contains("(49)");
    }

    /**
     * Strip scheme prefixes from value.
     *
     * @param stringValue value
     * @return stripped value
     */
    private static String stripUserPasswordSchemes(String stringValue) {
        String testStringValue = stringValue.toLowerCase();
        for (String scheme : new String[]{USERPASSWORD_SCHEME_CRYPT, USERPASSWORD_SCHEME_MD5, USERPASSWORD_SCHEME_SHA}){
            if (testStringValue.startsWith(scheme)) {
                return stringValue.substring(scheme.length());
            }
        }
        return stringValue;
    }

    /**
     * Silently close TLS response.
     *
     * @param tls TLS response
     */
    private static void close(StartTlsResponse tls) {
        if (tls != null) {
            // close and release TLS
            try {
                tls.close();
            } catch (IOException ioe) {
            }
        }
    }

    /**
     * Silently close LDAP context.
     *
     * @param ctx LDAP context
     */
    private static void close(InitialLdapContext ctx) {
        if (ctx != null) {
            // close and release LDAP context connection
            try {
                ctx.close();
            } catch (NamingException ne) {
            }
        }
    }

    /**
     * Extract lower case domain from identifier.
     *
     * @param identifier identifier with optional domain prefix
     * @return lower case domain or null
     */
    private static String extractDomain(String identifier) {
        if (identifier == null) {
            return null;
        }
        identifier = identifier.trim();
        if (identifier.length() == 0) {
            return null;
        }
        for (String separator : new String[]{DOMAIN_ID_SEPARATOR, ALTERNATE_DOMAIN_ID_SEPARATOR}) {
            int separatorIndex = identifier.indexOf(separator);
            if (separatorIndex > 0) {
                identifier = identifier.substring(0, separatorIndex).trim().toLowerCase();
                return ((identifier.length() > 0) ? identifier : null);
            }
            if (separatorIndex == 0) {
                return null;
            }
        }
        return null;
    }

    /**
     * Strip domain from identifier.
     *
     * @param identifier identifier with optional domain prefix
     * @return stripped identifier or null
     */
    protected static String stripDomain(String identifier) {
        if (identifier == null) {
            return null;
        }
        identifier = identifier.trim();
        if (identifier.length() == 0) {
            return null;
        }
        for (String separator : new String[]{DOMAIN_ID_SEPARATOR, ALTERNATE_DOMAIN_ID_SEPARATOR}) {
            int separatorIndex = identifier.indexOf(separator);
            if (separatorIndex >= 0) {
                identifier = identifier.substring(separatorIndex+separator.length()).trim();
                return ((identifier.length() > 0) ? identifier : null);
            }
        }
        return identifier;
    }

    /**
     * Canonicalize DN for comparison
     *
     * @param dn DN to canonicalize
     * @return canonical DN
     */
    private static String canonicalizeDN(String dn) {
        return dn.toLowerCase().replace(" ", "");
    }

    /**
     * API Cache element class to hold return values with timestamp.
     */
    private static class APICacheElement {

        public long timestamp;
        public Object value;

        public APICacheElement(Object value) {
            this.timestamp = System.currentTimeMillis();
            this.value = value;
        }
    }

    /**
     * Make API cache key.
     *
     * @param api API method name
     * @param args API method args
     * @return cache key
     */
    private static String makeAPICacheKey(String api, String ... args) {
        StringBuilder key = new StringBuilder(api);
        for (String arg : args) {
            key.append("|");
            key.append(arg);
        }
        return key.toString();
    }

    /**
     * Put value in API cache and record stats.
     *
     * @param key API cache key
     * @param value API cache value
     */
    private void putApiCache(String key, Object value) {
        apiCache.put(key, new APICacheElement(value));
        apiCachePuts.incrementAndGet();
    }

    /**
     * Get value from API cache and record stats. Cache element TTL
     * is enforced by the reaping thread.
     *
     * @param key API cache key
     * @return API cache value or null
     */
    private Object getApiCache(String key) {
        APICacheElement cacheElement = apiCache.get(key);
        if (cacheElement != null) {
            apiCacheHits.incrementAndGet();
            return cacheElement.value;
        } else {
            apiCacheMisses.incrementAndGet();
            return null;
        }
    }
}