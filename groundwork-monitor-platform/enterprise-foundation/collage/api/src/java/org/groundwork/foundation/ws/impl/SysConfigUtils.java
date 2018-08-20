package org.groundwork.foundation.ws.impl;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ArulShanmugam on 9/3/14.
 */
public class SysConfigUtils {

    private static Log log = LogFactory.getLog(SysConfigUtils.class);

    /**
     * common date format used in various config files.
     */
    public static String DATE_TIME_FORMAT_US = "MM/dd/yyyy HH:mm:ss a";

    /**
     * ws_client.properties file where webservices userinfo is stored.
     */
    public static final String WSCLIENT_PROPERTIES = "/usr/local/groundwork/config/ws_client.properties";

    /**
     * foundation.properties file where proxy userinfo is stored.
     */
    public static final String FOUNDATION_PROPERTIES = "/usr/local/groundwork/config/foundation.properties";

    /**
     * josso-gateway-ldap-stores.xml file where LDAP info is stored.
     */
    public static final String JOSSO_LDAP_PROPERTIES = "/usr/local/groundwork/josso-1.8.4/lib/josso-gateway-ldap-stores.xml";

    /**
     * ws_client.properties file where webservices userinfo is stored.
     */
    public static final String GATIN_CONFIG_PROPERTIES = "/usr/local/groundwork/config/configuration.properties";

    public static final String LDAP_SEC_CREDENTIAL = "ldap-istore:ldap-bind-store[@securityCredential]";

    public static final String JOSSO_GATEWAY_CONFIG = "/usr/local/groundwork/josso-1.8.4/lib/josso-gateway-config.xml";

    private static SysConfigUtils instance = null;

    private PropertiesConfiguration wsclientProperties = null;

    private PropertiesConfiguration foundationProperties = null;

    private PropertiesConfiguration gateinProperties = null;

    public PropertiesConfiguration getAppUsersProperties() {
        return appUsersProperties;
    }

    private PropertiesConfiguration appUsersProperties = null;

    private XMLConfiguration jossoLDAPConfig = null;

    private static final String LDAP_PROP_PREFIX = "core.security.ldap.config.";
    private static final String LDAP_SECURITY_PRINCIPAL_PROP = LDAP_PROP_PREFIX+"security_principal";
    private static final String LDAP_SECURITY_CREDENTIAL_PROP = LDAP_PROP_PREFIX+"security_credential";

    private Map<String,String[]> ldapDomainCredentials = new LinkedHashMap<String,String[]>();

    /**
     * application-users.properties file where api account info.
     */
    public static final File APP_USERS_PROPERTIES =
            new File("/usr/local/groundwork/foundation/container/jpp/standalone/configuration/application-users.properties");
    public static final File DUAL_APP_USERS_PROPERTIES =
            new File("/usr/local/groundwork/foundation/container/jpp2/standalone/configuration/application-users.properties");

    private SysConfigUtils() throws ConfigurationException {
        this.loadFoundationProperties();
        this.loadWSClientProperties();
        this.loadGateinConfigurationProperties();
        this.loadAppUsersConfigurationProperties();
        jossoLDAPConfig = new XMLConfiguration(JOSSO_LDAP_PROPERTIES);
    }

    public static synchronized SysConfigUtils getInstance() throws ConfigurationException {
        if (instance == null)
            instance = new SysConfigUtils();
        return instance;
    }

    /**
     * Helper to load WSClient properties file
     */
    private void loadWSClientProperties() throws ConfigurationException {
        wsclientProperties = new PropertiesConfiguration();
        wsclientProperties.setReloadingStrategy(new FileChangedReloadingStrategy());

        try (FileInputStream fis = new FileInputStream(WSCLIENT_PROPERTIES)) {
            wsclientProperties.load(fis);
        } catch (IOException e) {
            throw new ConfigurationException(e);
        }
    }

    /**
     * Helper to load WSClient properties file
     */
    private void loadGateinConfigurationProperties() throws ConfigurationException {
        gateinProperties = new PropertiesConfiguration();
        gateinProperties.setReloadingStrategy(new FileChangedReloadingStrategy());

        try (FileInputStream fis = new FileInputStream(GATIN_CONFIG_PROPERTIES)) {
            gateinProperties.load(fis);
        } catch (IOException e) {
            throw new ConfigurationException(e);
        }
    }

    /**
     * Helper to load app users properties file
     */
    private void loadAppUsersConfigurationProperties() throws ConfigurationException {
        appUsersProperties = new PropertiesConfiguration();
        appUsersProperties.setReloadingStrategy(new FileChangedReloadingStrategy());

        File appUsersPropertiesFile = (DUAL_APP_USERS_PROPERTIES.isFile() ?
                DUAL_APP_USERS_PROPERTIES : APP_USERS_PROPERTIES);

        try (FileInputStream fis = new FileInputStream(appUsersPropertiesFile)) {
            appUsersProperties.load(fis);
        } catch (IOException e) {
            throw new ConfigurationException(e);
        }
    }


    /**
     * Helper to load Foundation properties file
     */
    private void loadFoundationProperties() throws ConfigurationException {
        foundationProperties = new PropertiesConfiguration();
        foundationProperties.setReloadingStrategy(new FileChangedReloadingStrategy());

        // load foundation properties
        try (FileInputStream fis = new FileInputStream(FOUNDATION_PROPERTIES)) {
            foundationProperties.load(fis);
        } catch (IOException e) {
            throw new ConfigurationException(e);
        }

        // load LDAP domain credentials in order
        List<String> domains = new ArrayList<String>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FOUNDATION_PROPERTIES))) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                line = line.trim();
                if (line.startsWith(LDAP_PROP_PREFIX) && line.contains("=")) {
                    String propName = line.substring(0, line.indexOf('=')).trim();
                    int lastSeparatorIndex = propName.lastIndexOf('.');
                    if (lastSeparatorIndex > LDAP_PROP_PREFIX.length()) {
                        String domain = propName.substring(LDAP_PROP_PREFIX.length(), lastSeparatorIndex);
                        if (!domains.contains(domain)) {
                            domains.add(domain);
                        }
                    } else if (lastSeparatorIndex == LDAP_PROP_PREFIX.length()-1) {
                        if (!domains.contains(null)) {
                            domains.add(null);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new ConfigurationException(e);
        }

        for (String domain : domains) {
            String principal = join(foundationProperties.getStringArray(domainPropName(domain, LDAP_SECURITY_PRINCIPAL_PROP)), ",");
            String credential = foundationProperties.getString(domainPropName(domain, LDAP_SECURITY_CREDENTIAL_PROP));
            if (principal != null && credential != null) {
                ldapDomainCredentials.put(domain, new String[]{principal, credential});
            }
        }
    }

    /**
     * Gets wsclient properties.
     *
     * @return
     * @throws Exception
     */
    public PropertiesConfiguration getWSClientConfiguration() {
        return wsclientProperties;
    }

    /**
     * Gets foundation properties.
     *
     * @return
     * @throws Exception
     */
    public PropertiesConfiguration getFoundationConfiguration() {
        return foundationProperties;
    }

    /**
     * Helper to get the xml properties configuration.
     *
     * @return
     * @throws Exception
     */
    public XMLConfiguration getJOSSOLDAPConfiguration() throws Exception {
        return jossoLDAPConfig;
    }

    /**
     * Gets Gatein configuration properties.
     *
     * @return
     * @throws Exception
     */
    public PropertiesConfiguration getGateinConfiguration() {
        return gateinProperties;
    }

    /**
     * Returns ordered LDAP domains loaded from foundation.properties.
     *
     * @return list of LDAP domain names or null for the default domain
     */
    public List<String> getLdapDomains() {
        return new ArrayList(ldapDomainCredentials.keySet());
    }

    /**
     * Returns LDAP domain credentials loaded from foundation.properties.
     *
     * @param domain LDAP domain name or null
     * @return string array containing LDAP principal and credential for domain
     */
    public String[] getLdapDomainCredentials(String domain) {
        return ldapDomainCredentials.get(domain);
    }

    /**
     * Updates LDAP domain credentials and foundation.properties.
     *
     * @param domain LDAP domain name or null
     * @param credentials string array containing LDAP principal and credential for domain
     * @throws Exception on foundation.properties save exception
     */
    public void setLdapDomainCredentials(String domain, String[] credentials) throws Exception {
        if (!ldapDomainCredentials.containsKey(domain)) {
            throw new UnsupportedOperationException("Cannot create LDAP domains");
        }
        ldapDomainCredentials.put(domain, credentials);
        foundationProperties.setProperty(domainPropName(domain, LDAP_SECURITY_PRINCIPAL_PROP), credentials[0]);
        foundationProperties.setProperty(domainPropName(domain, LDAP_SECURITY_CREDENTIAL_PROP), credentials[1]);
        foundationProperties.save(SysConfigUtils.FOUNDATION_PROPERTIES);
    }

    /**
     * Checks is LDAP is configured
     */
    public static boolean isLDAPEnabled() {
        boolean ldapEnabled = false;
        try {
            File fXmlFile = new File(SysConfigUtils.JOSSO_GATEWAY_CONFIG);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("s:import");

            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    String component = eElement.getAttribute("resource");
                    log.debug("elem name==>" + component);
                    if (component.equals("josso-gateway-ldap-stores.xml")) {
                        ldapEnabled = true;
                        break;
                    }
                }
            }
        } catch (Exception exc) {
            log.error(exc.getMessage());
        }
        return ldapEnabled;
    }

    /**
     * Forms LDAP domain specific property name.
     *
     * @param domain domain name
     * @param propName base property name
     * @return property name
     */
    private static String domainPropName(String domain, String propName) {
        if (domain != null) {
            int lastSeparatorIndex = propName.lastIndexOf('.');
            return propName.substring(0, lastSeparatorIndex)+"."+domain+propName.substring(lastSeparatorIndex);
        } else {
            return propName;
        }
    }

    /**
     * Basic utility to join a string array. Implementing here to avoid 3rd party
     * module dependency.
     *
     * @param stringArray input string array
     * @param delim delimiter string
     * @return joined string
     */
    public static String join(String[] stringArray, String delim) {
        if (stringArray == null || stringArray.length == 0) {
            return null;
        }
        if (stringArray.length == 1) {
            return stringArray[0];
        }
        StringBuilder joinedString = new StringBuilder();
        for (String string: stringArray) {
            if (joinedString.length() > 0) {
                joinedString.append(delim);
            }
            joinedString.append(string);
        }
        return joinedString.toString();
    }
}
