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

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * LDAPConfig
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class LDAPConfig {

    private static final Log logger = LogFactory.getLog(LDAPConfig.class);

    public static final String AD_SERVER_TYPE = "AD";
    public static final String OPENLDAP_SERVER_TYPE = "OpenLDAP";

    public static final String PROP_PREFIX = "core.security.ldap.config.";

    // configuration properties
    public static final String CREDENTIAL_QUERY_STRING_PROP = PROP_PREFIX+"credential_query_string";
    public static final String CREDENTIAL_QUERY_STRING_AD_DEFAULT = "sAMAccountName=username,unicodePwd=password";
    public static final String CREDENTIAL_QUERY_STRING_OPENLDAP_DEFAULT = "uid=username,userPassword=password";
    public static final String ENABLE_START_TLS_PROP = PROP_PREFIX+"enable_start_tls";
    public static final Boolean ENABLE_START_TLS_DEFAULT = Boolean.FALSE;
    public static final String INITIAL_CONTEXT_FACTORY_PROP = PROP_PREFIX+"initial_context_factory";
    public static final String INITIAL_CONTEXT_FACTORY_DEFAULT = "com.sun.jndi.ldap.LdapCtxFactory";
    public static final String LDAP_SEARCH_SCOPE_PROP = PROP_PREFIX+"ldap_search_scope";
    public static final String LDAP_SEARCH_SCOPE_DEFAULT = "SUBTREE";
    public static final String PRINCIPAL_LOOKUP_ATTRIBUTE_ID_PROP = PROP_PREFIX+"principle_lookup_attribute_id";
    public static final String PRINCIPAL_LOOKUP_ATTRIBUTE_ID_DEFAULT = null;
    public static final String PRINCIPAL_UID_ATTRIBUTE_ID_PROP = PROP_PREFIX+"principal_uid_attribute_id";
    public static final String PRINCIPAL_UID_ATTRIBUTE_ID_AD_DEFAULT = "sAMAccountName";
    public static final String PRINCIPAL_UID_ATTRIBUTE_ID_OPENLDAP_DEFAULT = "uid";
    public static final String PROVIDER_URL_PROP = PROP_PREFIX+"provider_url";
    public static final String PROVIDER_URL_DEFAULT = null;
    public static final String ROLE_ATTRIBUTE_ID_PROP = PROP_PREFIX+"role_attribute_id";
    public static final String ROLE_ATTRIBUTE_ID_AD_DEFAULT = "sAMAccountName";
    public static final String ROLE_ATTRIBUTE_ID_OPENLDAP_DEFAULT = "cn";
    public static final String ROLE_MATCHING_MODE_PROP = PROP_PREFIX+"role_matching_mode";
    public static final String ROLE_MATCHING_MODE_DEFAULT = "UDN";
    public static final String ROLES_CTX_DN_PROP = PROP_PREFIX+"roles_ctx_dn";
    public static final String ROLES_CTX_DN_AD_DEFAULT = "OU=GWRoles,dc=demo,dc=com";
    public static final String ROLES_CTX_DN_OPENLDAP_DEFAULT = "ou=Groups,dc=demo,dc=com";
    public static final String SECURITY_AUTHENTICATION_PROP = PROP_PREFIX+"security_authentication";
    public static final String SECURITY_AUTHENTICATION_DEFAULT = "simple";
    public static final String SECURITY_CREDENTIAL_PROP = PROP_PREFIX+"security_credential";
    public static final String SECURITY_CREDENTIAL_DEFAULT = "";
    public static final String SECURITY_PRINCIPAL_PROP = PROP_PREFIX+"security_principal";
    public static final String SECURITY_PRINCIPAL_AD_DEFAULT = "cn=Administrator,cn=Users,dc=demo,dc=com";
    public static final String SECURITY_PRINCIPAL_OPENLDAP_DEFAULT = "cn=Administrator,dc=demo,dc=com";
    public static final String SECURITY_PROTOCOL_PROP = PROP_PREFIX+"security_protocol";
    public static final String SECURITY_PROTOCOL_DEFAULT = "";
    public static final String SERVER_TYPE_PROP = PROP_PREFIX+"server_type";
    public static final String SERVER_TYPE_DEFAULT = AD_SERVER_TYPE;
    public static final String TRUST_STORE_PROP = PROP_PREFIX+"trust_store";
    public static final String TRUST_STORE_DEFAULT = null;
    public static final String TRUST_STORE_PASSWORD_PROP = PROP_PREFIX+"trust_store_password";
    public static final String TRUST_STORE_PASSWORD_DEFAULT = null;
    public static final String UID_ATTRIBUTE_ID_PROP = PROP_PREFIX+"uid_attribute_id";
    public static final String UID_ATTRIBUTE_ID_DEFAULT = "member";
    public static final String UPDATABLE_CREDENTIAL_ATTRIBUTE_ID_PROP = PROP_PREFIX+"updatable_credential_attribute_id";
    public static final String UPDATABLE_CREDENTIAL_ATTRIBUTE_ID_DEFAULT = "userPassword";
    public static final String USER_CERTIFICATE_ATTRIBUTE_ID_PROP = PROP_PREFIX+"user_certificate_attribute_id";
    public static final String USER_CERTIFICATE_ATTRIBUTE_ID_DEFAULT = "userCertificate";
    public static final String USER_PROPERTIES_QUERY_STRING_PROP = PROP_PREFIX+"user_properties_query_string";
    public static final String USER_PROPERTIES_QUERY_STRING_AD_DEFAULT = "givenName=firstname,sn=lastname,userPrincipalName=mail";
    public static final String USER_PROPERTIES_QUERY_STRING_OPENLDAP_DEFAULT = "givenName=firstname,sn=lastname,mail=mail";
    public static final String USERS_CTX_DN_PROP = PROP_PREFIX+"users_ctx_dn";
    public static final String USERS_CTX_DN_AD_DEFAULT = "CN=Users,dc=demo,dc=com";
    public static final String USERS_CTX_DN_OPENLDAP_DEFAULT = "ou=Users,dc=demo,dc=com";

    /**
     * Load LDAP configuration properties from file.
     *
     * @param file file to load properties from
     * @param properties loaded properties
     * @return LDAP configuration
     */
    public static Map<String,LDAPConfig> loadConfigsFromFile(File file, Properties properties) {
        // load configurations in order from file
        Map<String,LDAPConfig> configs = new LinkedHashMap<String,LDAPConfig>();
        FileInputStream input = null;
        try {
            // load properties
            input = new FileInputStream(file);
            properties.load(input);
            // extract domains in order from properties file, (domains appear in
            // property paths after property prefix but before property name and
            // no domain in the property implies the default domain).
            List<String> domains = new ArrayList<String>();
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(file));
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    line = line.trim();
                    if (line.startsWith(PROP_PREFIX) && line.contains("=")) {
                        String propName = line.substring(0, line.indexOf('=')).trim();
                        int lastSeparatorIndex = propName.lastIndexOf('.');
                        if (lastSeparatorIndex > PROP_PREFIX.length()) {
                            String domain = propName.substring(PROP_PREFIX.length(), lastSeparatorIndex);
                            if (!domains.contains(domain)) {
                                domains.add(domain);
                            }
                        } else if (lastSeparatorIndex == PROP_PREFIX.length()-1) {
                            if (!domains.contains(null)) {
                                domains.add(null);
                            }
                        }
                    }
                }
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
            // load domains in order
            for (String domain : domains) {
                configs.put(domain, new LDAPConfig(domain, properties));
            }
        } catch (IOException ioe) {
            logger.error("LDAPConfig: cannot load configurations from "+file.getAbsolutePath()+": "+ioe, ioe);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ioe) {
                }
            }
        }
        return configs;
    }

    private static final Pattern DN_LIST_ELEMENT_DELIMITER_SPLIT = Pattern.compile("\\s*[|]\\s*");

    // configuration domain
    private String domain;
    private String serverType;

    // LDAP configuration
    private String credentialQueryString;
    private Boolean enableStartTLS;
    private String initialContextFactory;
    private String ldapSearchScope;
    private String principalLookupAttributeID;
    private String principalUidAttributeID;
    private String providerURL;
    private String roleAttributeID;
    private String roleMatchingMode;
    private String rolesCtxDN;
    private String securityAuthentication;
    private String securityCredential;
    private String securityPrincipal;
    private String securityProtocol;
    private String trustStore;
    private String trustStorePassword;
    private String uidAttributeID;
    private String updateableCredentialAttributeID;
    private String userCertificateAttributeID;
    private String userPropertiesQueryString;
    private String usersCtxDN;

    /**
     * Construct domain LDAP configuration.
     *
     * @param domain domain prefix/name or null for the default
     * @param serverType endpoint server type or null
     */
    public LDAPConfig(String domain, String serverType) {
        this.domain = cleanDomain(domain);
        this.serverType = (serverType != null ? serverType : SERVER_TYPE_DEFAULT);
        // set defaults
        boolean serverTypeIsAD = AD_SERVER_TYPE.equalsIgnoreCase(this.serverType);
        this.credentialQueryString = (serverTypeIsAD ? CREDENTIAL_QUERY_STRING_AD_DEFAULT : CREDENTIAL_QUERY_STRING_OPENLDAP_DEFAULT);
        this.enableStartTLS = ENABLE_START_TLS_DEFAULT;
        this.initialContextFactory = INITIAL_CONTEXT_FACTORY_DEFAULT;
        this.ldapSearchScope = LDAP_SEARCH_SCOPE_DEFAULT;
        this.principalLookupAttributeID = PRINCIPAL_LOOKUP_ATTRIBUTE_ID_DEFAULT;
        this.principalUidAttributeID = (serverTypeIsAD ? PRINCIPAL_UID_ATTRIBUTE_ID_AD_DEFAULT : PRINCIPAL_UID_ATTRIBUTE_ID_OPENLDAP_DEFAULT);
        this.providerURL = PROVIDER_URL_DEFAULT;
        this.roleAttributeID = (serverTypeIsAD ? ROLE_ATTRIBUTE_ID_AD_DEFAULT : ROLE_ATTRIBUTE_ID_OPENLDAP_DEFAULT);
        this.roleMatchingMode = ROLE_MATCHING_MODE_DEFAULT;
        this.rolesCtxDN = (serverTypeIsAD ? ROLES_CTX_DN_AD_DEFAULT : ROLES_CTX_DN_OPENLDAP_DEFAULT);
        this.securityAuthentication = SECURITY_AUTHENTICATION_DEFAULT;
        this.securityCredential = SECURITY_CREDENTIAL_DEFAULT;
        this.securityPrincipal = (serverTypeIsAD ? SECURITY_PRINCIPAL_AD_DEFAULT : SECURITY_PRINCIPAL_OPENLDAP_DEFAULT);
        this.securityProtocol = SECURITY_PROTOCOL_DEFAULT;
        this.trustStore = TRUST_STORE_DEFAULT;
        this.trustStorePassword = TRUST_STORE_PASSWORD_DEFAULT;
        this.uidAttributeID = UID_ATTRIBUTE_ID_DEFAULT;
        this.updateableCredentialAttributeID = UPDATABLE_CREDENTIAL_ATTRIBUTE_ID_DEFAULT;
        this.userCertificateAttributeID = USER_CERTIFICATE_ATTRIBUTE_ID_DEFAULT;
        this.userPropertiesQueryString = (serverTypeIsAD ? USER_PROPERTIES_QUERY_STRING_AD_DEFAULT : USER_PROPERTIES_QUERY_STRING_OPENLDAP_DEFAULT);
        this.usersCtxDN = (serverTypeIsAD ? USERS_CTX_DN_AD_DEFAULT : USERS_CTX_DN_OPENLDAP_DEFAULT);
    }

    /**
     * Construct domain LDAP configuration with properties.
     *
     * @param domain domain prefix/name or null for the default
     */
    public LDAPConfig(String domain, Properties properties) {
        this(domain, properties.getProperty(domainPropName(cleanDomain(domain), SERVER_TYPE_PROP)));
        // override defaults with properties
        this.credentialQueryString = properties.getProperty(domainPropName(this.domain, CREDENTIAL_QUERY_STRING_PROP), this.credentialQueryString);
        this.enableStartTLS = Boolean.valueOf(properties.getProperty(domainPropName(this.domain, ENABLE_START_TLS_PROP), this.enableStartTLS.toString()));
        this.initialContextFactory = properties.getProperty(domainPropName(this.domain, INITIAL_CONTEXT_FACTORY_PROP), this.initialContextFactory);
        this.ldapSearchScope = properties.getProperty(domainPropName(this.domain, LDAP_SEARCH_SCOPE_PROP), this.ldapSearchScope);
        this.principalLookupAttributeID = properties.getProperty(domainPropName(this.domain, PRINCIPAL_LOOKUP_ATTRIBUTE_ID_PROP), this.principalLookupAttributeID);
        this.principalUidAttributeID = properties.getProperty(domainPropName(this.domain, PRINCIPAL_UID_ATTRIBUTE_ID_PROP), this.principalUidAttributeID);
        this.providerURL = properties.getProperty(domainPropName(this.domain, PROVIDER_URL_PROP), this.providerURL);
        this.roleAttributeID = properties.getProperty(domainPropName(this.domain, ROLE_ATTRIBUTE_ID_PROP), this.roleAttributeID);
        this.roleMatchingMode = properties.getProperty(domainPropName(this.domain, ROLE_MATCHING_MODE_PROP), this.roleMatchingMode);
        this.rolesCtxDN = properties.getProperty(domainPropName(this.domain, ROLES_CTX_DN_PROP), this.rolesCtxDN);
        this.securityAuthentication = properties.getProperty(domainPropName(this.domain, SECURITY_AUTHENTICATION_PROP), this.securityAuthentication);
        this.securityCredential = properties.getProperty(domainPropName(this.domain, SECURITY_CREDENTIAL_PROP), this.securityCredential);
        this.securityPrincipal = properties.getProperty(domainPropName(this.domain, SECURITY_PRINCIPAL_PROP), this.securityPrincipal);
        this.securityProtocol = properties.getProperty(domainPropName(this.domain, SECURITY_PROTOCOL_PROP), this.securityProtocol);
        this.trustStore = properties.getProperty(domainPropName(this.domain, TRUST_STORE_PROP), this.trustStore);
        this.trustStorePassword = properties.getProperty(domainPropName(this.domain, TRUST_STORE_PASSWORD_PROP), this.trustStorePassword);
        this.uidAttributeID = properties.getProperty(domainPropName(this.domain, UID_ATTRIBUTE_ID_PROP), this.uidAttributeID);
        this.updateableCredentialAttributeID = properties.getProperty(domainPropName(this.domain, UPDATABLE_CREDENTIAL_ATTRIBUTE_ID_PROP), this.updateableCredentialAttributeID);
        this.userCertificateAttributeID = properties.getProperty(domainPropName(this.domain, USER_CERTIFICATE_ATTRIBUTE_ID_PROP), this.userCertificateAttributeID);
        this.userPropertiesQueryString = properties.getProperty(domainPropName(this.domain, USER_PROPERTIES_QUERY_STRING_PROP), this.userPropertiesQueryString);
        this.usersCtxDN = properties.getProperty(domainPropName(this.domain, USERS_CTX_DN_PROP), this.usersCtxDN);
    }

    public String getDomain() {
        return domain;
    }

    public String getCredentialQueryString() {
        return credentialQueryString;
    }

    public void setCredentialQueryString(String credentialQueryString) {
        if (credentialQueryString != null) {
            this.credentialQueryString = credentialQueryString;
        }
    }

    public Boolean getEnableStartTLS() {
        return enableStartTLS;
    }

    public void setEnableStartTLS(Boolean enableStartTLS) {
        if (enableStartTLS != null) {
            this.enableStartTLS = enableStartTLS;
        }
    }

    public String getInitialContextFactory() {
        return initialContextFactory;
    }

    public void setInitialContextFactory(String initialContextFactory) {
        if (initialContextFactory != null) {
            this.initialContextFactory = initialContextFactory;
        }
    }

    public String getLdapSearchScope() {
        return ldapSearchScope;
    }

    public void setLdapSearchScope(String ldapSearchScope) {
        if (ldapSearchScope != null) {
            this.ldapSearchScope = ldapSearchScope;
        }
    }

    public String getPrincipalLookupAttributeID() {
        return principalLookupAttributeID;
    }

    public void setPrincipalLookupAttributeID(String principalLookupAttributeID) {
        this.principalLookupAttributeID = principalLookupAttributeID;
    }

    public String getPrincipalUidAttributeID() {
        return principalUidAttributeID;
    }

    public void setPrincipalUidAttributeID(String principalUidAttributeID) {
        if (principalUidAttributeID != null) {
            this.principalUidAttributeID = principalUidAttributeID;
        }
    }

    public String getProviderURL() {
        if (providerURL == null || providerURL.equals("")) {
            // default provider URL to localhost
            if ("ssl".equalsIgnoreCase(securityProtocol)) {
                providerURL = "ldaps://localhost:636";
            } else {
                providerURL = "ldap://localhost:389";
            }
        }
        return providerURL;
    }

    public void setProviderURL(String providerURL) {
        if (providerURL != null) {
            this.providerURL = providerURL;
        }
    }

    public String getRoleAttributeID() {
        return roleAttributeID;
    }

    public void setRoleAttributeID(String roleAttributeID) {
        if (roleAttributeID != null) {
            this.roleAttributeID = roleAttributeID;
        }
    }

    public String getRoleMatchingMode() {
        return roleMatchingMode;
    }

    public void setRoleMatchingMode(String roleMatchingMode) {
        if (roleMatchingMode != null) {
            this.roleMatchingMode = roleMatchingMode;
        }
    }

    public String getRolesCtxDN() {
        return rolesCtxDN;
    }

    public String[] getRolesCtxDNs() {
        return ctxDNs(rolesCtxDN);
    }

    public void setRolesCtxDN(String rolesCtxDN) {
        if (rolesCtxDN != null) {
            this.rolesCtxDN = rolesCtxDN;
        }
    }

    public String getSecurityAuthentication() {
        return securityAuthentication;
    }

    public void setSecurityAuthentication(String securityAuthentication) {
        if (securityAuthentication != null) {
            this.securityAuthentication = securityAuthentication;
        }
    }

    public String getSecurityCredential() {
        return securityCredential;
    }

    public void setSecurityCredential(String securityCredential) {
        if (securityCredential != null) {
            this.securityCredential = securityCredential;
        }
    }

    public String getSecurityPrincipal() {
        return securityPrincipal;
    }

    public void setSecurityPrincipal(String securityPrincipal) {
        if (securityPrincipal != null) {
            this.securityPrincipal = securityPrincipal;
        }
    }

    public String getSecurityProtocol() {
        return securityProtocol;
    }

    public void setSecurityProtocol(String securityProtocol) {
        if (securityProtocol != null) {
            this.securityProtocol = securityProtocol;
        }
    }

    public String getTrustStore() {
        return trustStore;
    }

    public void setTrustStore(String trustStore) {
        this.trustStore = trustStore;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    public String getUidAttributeID() {
        return uidAttributeID;
    }

    public void setUidAttributeID(String uidAttributeID) {
        if (uidAttributeID != null) {
            this.uidAttributeID = uidAttributeID;
        }
    }

    public String getUpdateableCredentialAttributeID() {
        return updateableCredentialAttributeID;
    }

    public void setUpdateableCredentialAttributeID(String updateableCredentialAttributeID) {
        if (updateableCredentialAttributeID != null) {
            this.updateableCredentialAttributeID = updateableCredentialAttributeID;
        }
    }

    public String getUserCertificateAttributeID() {
        return userCertificateAttributeID;
    }

    public void setUserCertificateAttributeID(String userCertificateAttributeID) {
        if (userCertificateAttributeID != null) {
            this.userCertificateAttributeID = userCertificateAttributeID;
        }
    }

    public String getUserPropertiesQueryString() {
        return userPropertiesQueryString;
    }

    public void setUserPropertiesQueryString(String userPropertiesQueryString) {
        if (userPropertiesQueryString != null) {
            this.userPropertiesQueryString = userPropertiesQueryString;
        }
    }

    public String getUsersCtxDN() {
        return usersCtxDN;
    }

    public String[] getUsersCtxDNs() {
        return ctxDNs(usersCtxDN);
    }

    public void setUsersCtxDN(String usersCtxDN) {
        if (usersCtxDN != null) {
            this.usersCtxDN = usersCtxDN;
        }
    }

    private static String cleanDomain(String domain) {
        if (domain != null) {
            domain = domain.trim();
            if (domain.length() == 0) {
                return null;
            }
        }
        return domain;
    }

    private static String domainPropName(String domain, String propName) {
        if (domain != null) {
            int lastSeparatorIndex = propName.lastIndexOf('.');
            return propName.substring(0, lastSeparatorIndex)+"."+domain+propName.substring(lastSeparatorIndex);
        } else {
            return propName;
        }
    }

    private static String[] ctxDNs(String ctxDN) {
        if (ctxDN != null) {
            return DN_LIST_ELEMENT_DELIMITER_SPLIT.split(ctxDN);
        } else {
            return null;
        }
    }
}
