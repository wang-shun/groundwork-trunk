package com.groundwork.core.security;

import com.groundwork.core.security.ldap.LDAPAggregator;
import com.groundwork.core.security.ldap.LDAPConfig;
import com.groundwork.core.security.ldap.LDAPMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.groundwork.foundation.ws.impl.ConfigurationWatcher;
import org.groundwork.foundation.ws.impl.ConfigurationWatcherNotificationListener;
import org.groundwork.foundation.ws.impl.FoundationConfiguration;
import org.groundwork.foundation.ws.impl.JasyptUtils;
import org.josso.gateway.identity.service.store.ldap.LDAPIdentityStore;
import org.springframework.context.ApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/*
 * LDAP JOSSO helper to find out if LDAP is enabled, authenticate, get userDN etc
 * Author - Arul Shanmugam
 * Since GWM 7.0
 */
public class LDAPHelper implements ConfigurationWatcherNotificationListener {

	private static Log log = LogFactory.getLog(LDAPHelper.class);

	private boolean LDAP = false;

	private static final String JOSSO_LDAP_CONFIG = "/usr/local/groundwork/config/josso-gateway-ldap-stores.xml";
	
	private static final String JOSSO_GATEWAY_CONFIG = "/usr/local/groundwork/config/josso-gateway-config.xml";

    private static final String FOUNDATION_PROPERTIES_CONFIG = "/usr/local/groundwork/config/foundation.properties";

	private static final String LDAP_MAPPING_PROPERTIES_CONFIG = "/usr/local/groundwork/ldap-mapping-directives.properties";

	private static LDAPHelper helper = null;

	private LDAPIdentityStore ldap = null;

	private LDAPAggregator ldapAggregator = null;

    private boolean ldapMappingEnabled = false;

    private Properties ldapMappingDirectives = null;

    private LDAPMapper ldapMapper = null;

	private String foundationPropertiesWatchedFileName = null;

	private String ldapMappingPropertiesWatchedFileName = null;

    private LDAPHelper() {

		try {
			// load static JOSSO gateway configuration
			File fXmlFile = new File(JOSSO_GATEWAY_CONFIG);
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
						LDAP = true;
						break;
					}
				}
			}
			if (LDAP) {
				// load static JOSSO LDAP configuration
				ApplicationContext factory = new ClassPathXmlApplicationContext(
						"file://" + JOSSO_LDAP_CONFIG);
				ldap = (LDAPIdentityStore) factory
						.getBean("josso-identity-store");

                // load LDAP Aggregator
				reloadLdapAggregator();

				// setup foundation.properties file watcher
				Path foundationPropertiesFilePath = Paths.get(FOUNDATION_PROPERTIES_CONFIG);
				foundationPropertiesWatchedFileName = foundationPropertiesFilePath.getFileName().toString();
				ConfigurationWatcher.registerListener(this, foundationPropertiesFilePath.toString());

				// load LDAP mapping directives
				reloadLdapMappingDirectives();

				// setup ldap-mapping-directives.properties file watcher
				Path ldapMappingDirectivesPropertiesFilePath = Paths.get(LDAP_MAPPING_PROPERTIES_CONFIG);
				this.ldapMappingPropertiesWatchedFileName = ldapMappingDirectivesPropertiesFilePath.getFileName().toString();
				ConfigurationWatcher.registerListener(this, ldapMappingDirectivesPropertiesFilePath.toString());
			}
		} catch (Exception exc) {
			log.error("LDAPHelper unexpected configuration error: "+exc, exc);
		}
	}

	/*
	 * Gets the LDAP Helper instance
	 */
	public static final synchronized LDAPHelper getInstance() {

		if (helper == null) {
			helper = new LDAPHelper();
		}
		return helper;
	}

	/**
	 * Returns if LDAP/AD is enabled.
	 * 
	 * @return
	 */
	public boolean isLDAP() {
		return LDAP;
	}

	/**
	 * Validate security credentials against an LDAP domain.
	 *
	 * @param domain security domain or null for the default domain
	 * @param securityPrincipalDN security principal/username DN
	 * @param securityCredential security credential/password
	 * @return validated
	 */
	public boolean validateSecurityCredentials(String domain, String securityPrincipalDN, String securityCredential) {
		// delegate to LDAP aggregator
		return ldapAggregator.validateSecurityCredentials(domain, securityPrincipalDN, securityCredential);
	}

	/**
	 * Authenticate user against LDAP.
	 *
	 * @param securityPrincipalDN principal/username DN
	 * @param securityCredential credential/password
     * @return authenticated
     */
	public boolean authenticate(String securityPrincipalDN, String securityCredential) {
        // delegate to LDAP aggregator
		return ldapAggregator.authenticate(securityPrincipalDN, securityCredential, false);
	}

	/**
	 * Fetch user DN from LDAP.
	 *
	 * @param uid principal/username
	 * @return user DN
     */
	public String selectUserDN(String uid) {
        // delegate to LDAP aggregator
        return ldapAggregator.selectUserDN(uid);
	}

	/**
	 * Get user properties from LDAP.
	 *
	 * @param uid principal/username
	 * @return user properties or empty map
     */
	public HashMap<String,String> selectUserProperties(String uid) {
        // delegate to LDAP aggregator
        return ldapAggregator.selectUserProperties(uid);
	}

	/**
	 * Get user roles from LDAP.
	 *
	 * @param username principal/username
	 * @return roles or empty array
     */
	public String[] selectRolesByUsername(String username) {
        // delegate to LDAP mapper if enabled or aggregator
		if (ldapMapper != null) {
			return ldapMapper.selectRolesByUsername(username);
		}
        return ldapAggregator.selectRolesByUsername(username);
	}

	@Override
	public void notifyChange(Path path) {
		// reload LDAP Aggregator on foundation.properties change
		if (foundationPropertiesWatchedFileName != null && foundationPropertiesWatchedFileName.equals(path.toString())) {
			reloadLdapAggregator();
		}
		// reload LDAP Mapper on ldap-mapping-directives.properties change
		if (ldapMappingPropertiesWatchedFileName != null && ldapMappingPropertiesWatchedFileName.equals(path.toString())) {
			reloadLdapMappingDirectives();
		}
	}

	/**
	 * Reload LDAP Aggregator if foundation.properties changes. Note: JOSSO
	 * configurations are fixed as loaded at startup because JOSSO itself
	 * will not reconfigure itself dynamically.
	 */
	private synchronized void reloadLdapAggregator() {
		// load LDAP aggregator configuration
		Properties properties = new Properties();
		Map<String, LDAPConfig> configs = LDAPConfig.loadConfigsFromFile(new File(FOUNDATION_PROPERTIES_CONFIG), properties);
		if (!configs.isEmpty()) {
			// decrypt security credentials loaded from configuration
			FoundationConfiguration.reload();
			for (LDAPConfig config : configs.values()) {
				config.setSecurityCredential(JasyptUtils.jasyptDecrypt(config.getSecurityCredential()));
			}
		} else {
			// fallback to JOSSO configuration
			String serverType = (ldap.getPrincipalUidAttributeID().equals("sAMAccountName") ?
					LDAPConfig.AD_SERVER_TYPE : LDAPConfig.OPENLDAP_SERVER_TYPE);
			LDAPConfig jossoLDAPConfig = new LDAPConfig(null, serverType);
			jossoLDAPConfig.setCredentialQueryString(ldap.getCredentialQueryString());
			jossoLDAPConfig.setEnableStartTLS(ldap.getEnableStartTls());
			jossoLDAPConfig.setInitialContextFactory(ldap.getInitialContextFactory());
			jossoLDAPConfig.setLdapSearchScope(ldap.getLdapSearchScope());
			jossoLDAPConfig.setPrincipalLookupAttributeID(ldap.getPrincipalLookupAttributeID());
			jossoLDAPConfig.setPrincipalUidAttributeID(ldap.getPrincipalUidAttributeID());
			jossoLDAPConfig.setProviderURL(ldap.getProviderUrl());
			jossoLDAPConfig.setRoleAttributeID(ldap.getRoleAttributeID());
			jossoLDAPConfig.setRoleMatchingMode(ldap.getRoleMatchingMode());
			jossoLDAPConfig.setRolesCtxDN(ldap.getRolesCtxDN());
			jossoLDAPConfig.setSecurityAuthentication(ldap.getSecurityAuthentication());
			jossoLDAPConfig.setSecurityCredential(getSecurityCredential());
			jossoLDAPConfig.setSecurityPrincipal(ldap.getSecurityPrincipal());
			jossoLDAPConfig.setSecurityProtocol(ldap.getSecurityProtocol());
			jossoLDAPConfig.setTrustStore(ldap.getTrustStore());
			jossoLDAPConfig.setTrustStorePassword(ldap.getTrustStorePassword());
			jossoLDAPConfig.setUidAttributeID(ldap.getUidAttributeID());
			jossoLDAPConfig.setUpdateableCredentialAttributeID(ldap.getUpdateableCredentialAttribute());
			jossoLDAPConfig.setUserCertificateAttributeID(ldap.getUserCertificateAtrributeID());
			jossoLDAPConfig.setUserPropertiesQueryString(ldap.getUserPropertiesQueryString());
			jossoLDAPConfig.setUsersCtxDN(ldap.getUsersCtxDN());
			configs.put(null, jossoLDAPConfig);
		}
		// create LDAP aggregator
		boolean domainPrefixRequired = Boolean.parseBoolean(properties.getProperty(
				LDAPAggregator.DOMAIN_PREFIX_REQUIRED_PROP, LDAPAggregator.DOMAIN_PREFIX_REQUIRED_DEFAULT));
		LDAPAggregator shutdownLdapAggregator = ldapAggregator;
		ldapAggregator = new LDAPAggregator(configs, domainPrefixRequired);
		// (re)create LDAP mapper if mapping enabled
		ldapMappingEnabled = Boolean.parseBoolean(properties.getProperty(
				LDAPMapper.LDAP_MAPPING_ENABLED_PROP, LDAPMapper.LDAP_MAPPING_ENABLED_DEFAULT));
		if (ldapMappingEnabled && ldapMappingDirectives != null) {
			ldapMapper = new LDAPMapper(ldapMappingDirectives, ldapAggregator);
		} else {
			ldapMapper = null;
		}
		// shutdown replaced LDAP aggregator
		if (shutdownLdapAggregator != null) {
			shutdownLdapAggregator.shutdown();
		}
	}

	/**
	 * Helper to get the securitycredential out of the josso-gateway-ldap-config
	 * file since getSecurityCrendential method is protected in the
	 * LDAPIdentityStore
	 * 
	 * @return
	 */
	private static String getSecurityCredential() {
		String securityCredential = null;
		try {
			File fXmlFile = new File(JOSSO_LDAP_CONFIG);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
			NodeList nList = doc
					.getElementsByTagName("ldap-istore:ldap-bind-store");
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					securityCredential = eElement
							.getAttribute("securityCredential");
				}
			}

		} catch (Exception exc) {
			exc.printStackTrace();
		}
        securityCredential = JasyptUtils.jasyptDecrypt(securityCredential);
		return securityCredential;
	}

	/**
	 * Reload LDAP mapping if ldap-mapping-directives.properties changes.
	 */
    private synchronized void reloadLdapMappingDirectives() {
        File file = new File(LDAP_MAPPING_PROPERTIES_CONFIG);
        FileInputStream input = null;
        try {
            // load properties
            input = new FileInputStream(file);
            Properties properties = new Properties();
            properties.load(input);
            ldapMappingDirectives = properties;
            // (re)create LDAP mapper if mapping enabled
            if (ldapMappingEnabled && ldapAggregator != null) {
                ldapMapper = new LDAPMapper(ldapMappingDirectives, ldapAggregator);
            } else {
                ldapMapper = null;
            }
        } catch (IOException ioe) {
            log.error("LDAPMapper: cannot load configuration from "+file.getAbsolutePath()+": "+ioe, ioe);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ioe) {
                }
            }
        }
    }
}
