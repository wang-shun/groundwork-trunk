package com.groundworkopensource.portal.statusviewer.bean;


import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.impl.FoundationConfiguration;
import org.groundwork.foundation.ws.impl.JasyptUtils;
import org.groundwork.foundation.ws.impl.SysConfigUtils;
import org.groundwork.foundation.ws.impl.WSClientConfiguration;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


/**
 * Created by ArulShanmugam on 8/28/14.
 */

public class SysAccountInfo implements Serializable {

    private static final Logger log = Logger.getLogger(SysAccountInfo.class
            .getName());



    public static final String LDAP_SEC_PRINCIPAL = "ldap-istore:ldap-bind-store[@securityPrincipal]";

    public static final String LDAP_SEC_CREDENTIAL = "ldap-istore:ldap-bind-store[@securityCredential]";

    public static final String TEST_LDAP_CONFIG_FILE = "/usr/local/groundwork/josso-1.8.4/lib/josso-gateway-ldap-stores.xml";

    private static final String JOSSO_GATEWAY_CONFIG = "/usr/local/groundwork/josso-1.8.4/lib/josso-gateway-config.xml";


    public String getApiUserName() {
        return apiUserName;
    }

    public void setApiUserName(String apiUserName) {
        this.apiUserName = filterEmpty(apiUserName);
    }

    public String getLastApiUserName() {
        return lastApiUserName;
    }

    public void setLastApiUserName(String lastApiUserName) {
        this.lastApiUserName = lastApiUserName;
    }

    public String getApiCredentials() {
        return apiCredentials;
    }

    public void setApiCredentials(String apiCredentials) {
        this.apiCredentials = filterEmpty(apiCredentials);
    }

    public String getLastApiCredentials() {
        return lastApiCredentials;
    }

    public void setLastApiCredentials(String lastApiCredentials) {
        this.lastApiCredentials = lastApiCredentials;
    }

    public String getApiReaderUserName() {
        return apiReaderUserName;
    }

    public void setApiReaderUserName(String apiReaderUserName) {
        this.apiReaderUserName = filterEmpty(apiReaderUserName);
    }

    public String getLastApiReaderUserName() {
        return lastApiReaderUserName;
    }

    public void setLastApiReaderUserName(String lastApiReaderUserName) {
        this.lastApiReaderUserName = lastApiReaderUserName;
    }

    public String getApiReaderCredentials() {
        return apiReaderCredentials;
    }

    public void setApiReaderCredentials(String apiReaderCredentials) {
        this.apiReaderCredentials = filterEmpty(apiReaderCredentials);
    }

    public String getLastApiReaderCredentials() {
        return lastApiReaderCredentials;
    }

    public void setLastApiReaderCredentials(String lastApiReaderCredentials) {
        this.lastApiReaderCredentials = lastApiReaderCredentials;
    }


    public String getProxyUserName() {
        return proxyUserName;
    }

    public void setProxyUserName(String proxyUserName) {
        this.proxyUserName = filterEmpty(proxyUserName);
    }

    public String getLastProxyUserName() {
        return lastProxyUserName;
    }

    public void setLastProxyUserName(String lastProxyUserName) {
        this.lastProxyUserName = lastProxyUserName;
    }

    public String getProxyCredentials() {
        return proxyCredentials;
    }

    public void setProxyCredentials(String proxyCredentials) {
        this.proxyCredentials = filterEmpty(proxyCredentials);
    }

    public String getLastProxyCredentials() {
        return lastProxyCredentials;
    }

    public void setLastProxyCredentials(String lastProxyCredentials) {
        this.lastProxyCredentials = lastProxyCredentials;
    }

    public String getLdapUserName() {
        return ldapUserName;
    }

    public void setLdapUserName(String ldapUserName) {
        this.ldapUserName = filterEmpty(ldapUserName);
    }

    public String getLastLdapUserName() {
        return lastLdapUserName;
    }

    public void setLastLdapUserName(String lastLdapUserName) {
        this.lastLdapUserName = lastLdapUserName;
    }

    public String getLdapCredentials() {
        return ldapCredentials;
    }

    public void setLdapCredentials(String ldapCredentials) {
        this.ldapCredentials = filterEmpty(ldapCredentials);
    }

    public String getLastLdapCredentials() {
        return lastLdapCredentials;
    }

    public void setLastLdapCredentials(String lastLdapCredentials) {
        this.lastLdapCredentials = lastLdapCredentials;
    }

    private String apiUserName = null;
    private String lastApiUserName = null;

    private String apiCredentials = null;
    private String lastApiCredentials = null;

    private String apiReaderUserName = null;
    private String lastApiReaderUserName = null;

    private String apiReaderCredentials = null;
    private String lastApiReaderCredentials = null;

    public String getEncAPICredentials() {
        return encAPICredentials;
    }

    public void setEncAPICredentials(String encAPICredentials) {
        this.encAPICredentials = encAPICredentials;
    }

    private String encAPICredentials = null;


    public String getEncAPIReaderCredentials() {
        return encAPIReaderCredentials;
    }

    public void setEncAPIReaderCredentials(String encAPIReaderCredentials) {
        this.encAPIReaderCredentials = encAPIReaderCredentials;
    }

    private String encAPIReaderCredentials = null;

    private String proxyUserName = null;
    private String lastProxyUserName = null;

    private String proxyCredentials = null;
    private String lastProxyCredentials = null;

    private String ldapUserName = null;
    private String lastLdapUserName = null;

    private String ldapCredentials = null;
    private String lastLdapCredentials = null;

    public String getRemoteAPIUserName() {
        return remoteAPIUserName;
    }

    public void setRemoteAPIUserName(String remoteAPIUserName) {
        this.remoteAPIUserName = filterEmpty(remoteAPIUserName);
    }

    public String getLastRemoteAPIUserName() {
        return lastRemoteAPIUserName;
    }

    public void setLastRemoteAPIUserName(String lastRemoteAPIUserName) {
        this.lastRemoteAPIUserName = lastRemoteAPIUserName;
    }

    public String getRemoteAPICredentials() {
        return remoteAPICredentials;
    }

    public void setRemoteAPICredentials(String remoteAPICredentials) {
        this.remoteAPICredentials = filterEmpty(remoteAPICredentials);
    }

    public String getLastRemoteAPICredentials() {
        return lastRemoteAPICredentials;
    }

    public void setLastRemoteAPICredentials(String lastRemoteAPICredentials) {
        this.lastRemoteAPICredentials = lastRemoteAPICredentials;
    }

    private String remoteAPIUserName = null;
    private String lastRemoteAPIUserName = null;

    private String remoteAPICredentials = null;
    private String lastRemoteAPICredentials = null;

    public boolean isLdapEnabled() {
        return ldapEnabled;
    }

    public void setLdapEnabled(boolean ldapEnabled) {
        this.ldapEnabled = ldapEnabled;
    }

    private boolean ldapEnabled = false;

    public String getMainCredentials() {
        return mainCredentials;
    }

    public void setMainCredentials(String mainCredentials) {
        this.mainCredentials = filterEmpty(mainCredentials);
    }

    public String getLastMainCredentials() {
        return lastMainCredentials;
    }

    public void setLastMainCredentials(String lastMainCredentials) {
        this.lastMainCredentials = lastMainCredentials;
    }

    private String mainCredentials = null;
    private String lastMainCredentials = null;

    public String getTestButtonLabel() {
        return testButtonLabel;
    }

    public void setTestButtonLabel(String testButtonLabel) {
        this.testButtonLabel = testButtonLabel;
    }

    private String testButtonLabel = "Test";

    public boolean isTestButtonDisabled() {
        return testButtonDisabled;
    }

    public void setTestButtonDisabled(boolean testButtonDisabled) {
        this.testButtonDisabled = testButtonDisabled;
    }

    private boolean testButtonDisabled = false;

    public Date getLastUpdateAPICredentials() {
        return lastUpdateAPICredentials;
    }

    public void setLastUpdateAPICredentials(Date lastUpdateAPICredentials) {
        this.lastUpdateAPICredentials = lastUpdateAPICredentials;
    }

    public Date getLastUpdateAPIReaderCredentials() {
        return lastUpdateAPIReaderCredentials;
    }

    public void setLastUpdateAPIReaderCredentials(Date lastUpdateAPIReaderCredentials) {
        this.lastUpdateAPIReaderCredentials = lastUpdateAPIReaderCredentials;
    }

    public Date getLastUpdateProxyCredentials() {
        return lastUpdateProxyCredentials;
    }

    public void setLastUpdateProxyCredentials(Date lastUpdateProxyCredentials) {
        this.lastUpdateProxyCredentials = lastUpdateProxyCredentials;
    }

    public Date getLastUpdateLDAPCredentials() {
        return lastUpdateLDAPCredentials;
    }

    public void setLastUpdateLDAPCredentials(Date lastUpdateLDAPCredentials) {
        this.lastUpdateLDAPCredentials = lastUpdateLDAPCredentials;
    }

    public Date getLastUpdateMainCredentials() {
        return lastUpdateMainCredentials;
    }

    public void setLastUpdateMainCredentials(Date lastUpdateMainCredentials) {
        this.lastUpdateMainCredentials = lastUpdateMainCredentials;
    }

    private Date lastUpdateMainCredentials = null;

    private Date lastUpdateAPICredentials = null;

    private Date lastUpdateAPIReaderCredentials = null;

    private Date lastUpdateProxyCredentials = null;

    private Date lastUpdateLDAPCredentials = null;

    public String getToolCredentials() {
        return toolCredentials;
    }

    public void setToolCredentials(String toolCredentials) {
        this.toolCredentials = toolCredentials;
    }

    public String getToolEncCredentials() {
        return toolEncCredentials;
    }

    public void setToolEncCredentials(String toolEncCredentials) {
        this.toolEncCredentials = toolEncCredentials;
    }

    private String toolCredentials = null;

    private String toolEncCredentials = null;

    /**
     * Returns ordered LDAP domains credentials loaded from foundation.properties.
     *
     * @return list of LDAP domain credentials
     */
    public List<DomainCredentials> getLdapDomainCredentialsList() {
        return ldapDomainCredentialsList;
    }

    /**
     * Return date LDAP domains credentials last updated.
     *
     * @return date last updated
     */
    public Date getLastUpdateLdapDomainsCredentials() {
        return lastUpdateLdapDomainsCredentials;
    }

    private List<DomainCredentials> ldapDomainCredentialsList = new ArrayList<DomainCredentials>();

    private List<DomainCredentials> lastLdapDomainCredentialsList = new ArrayList<DomainCredentials>();

    private Date lastUpdateLdapDomainsCredentials = null;


    @PostConstruct
    public void init() {
        try {
            this.apiUserName = (String) SysConfigUtils.getInstance().getWSClientConfiguration().getProperty(WSClientConfiguration.WEBSERVICES_USERNAME);
            this.apiReaderUserName = (String) SysConfigUtils.getInstance().getWSClientConfiguration().getProperty(WSClientConfiguration.WEBSERVICES_READER_USERNAME);
            if (JasyptUtils.isEncryptionEnabled()) {
                this.encAPICredentials = (String) SysConfigUtils.getInstance().getWSClientConfiguration().getProperty(WSClientConfiguration.WEBSERVICES_PASSWORD);
                this.apiCredentials = JasyptUtils.jasyptDecrypt(this.encAPICredentials);
                this.encAPIReaderCredentials = (String) SysConfigUtils.getInstance().getWSClientConfiguration().getProperty(WSClientConfiguration.WEBSERVICES_READER_PASSWORD);
                this.apiReaderCredentials = JasyptUtils.jasyptDecrypt(this.encAPIReaderCredentials);
            } else {
                this.apiCredentials = (String) SysConfigUtils.getInstance().getWSClientConfiguration().getProperty(WSClientConfiguration.WEBSERVICES_PASSWORD);
                this.apiReaderCredentials = (String) SysConfigUtils.getInstance().getWSClientConfiguration().getProperty(WSClientConfiguration.WEBSERVICES_READER_PASSWORD);
            }

            this.remoteAPIUserName = "REMOTEAPIACCESS";

            SimpleDateFormat formatter = new SimpleDateFormat(SysConfigUtils.DATE_TIME_FORMAT_US);
            String lastupdateAPICredentialsProperty = (String) SysConfigUtils.getInstance().getWSClientConfiguration().getProperty(WSClientConfiguration.WEBSERVICES_USER_LAST_UPDATE_TIMESTAMP);
            if (lastupdateAPICredentialsProperty != null) {
                try {
                    this.lastUpdateAPICredentials = formatter.parse(lastupdateAPICredentialsProperty);
                } catch (ParseException pe) {
                    log.error(pe.getMessage());
                }
            }
            String lastupdateAPIReaderCredentialsProperty = (String) SysConfigUtils.getInstance().getWSClientConfiguration().getProperty(WSClientConfiguration.WEBSERVICES_USER_READER_LAST_UPDATE_TIMESTAMP);
            if (lastupdateAPIReaderCredentialsProperty != null) {
                try {
                    this.lastUpdateAPIReaderCredentials = formatter.parse(lastupdateAPIReaderCredentialsProperty);
                } catch (ParseException pe) {
                    log.error(pe.getMessage());
                }
            }
            this.proxyUserName = (String) SysConfigUtils.getInstance().getFoundationConfiguration().getProperty(FoundationConfiguration.PROXY_USER);
            if (JasyptUtils.isEncryptionEnabled())
                this.proxyCredentials = JasyptUtils.jasyptDecrypt((String) SysConfigUtils.getInstance().getFoundationConfiguration().getProperty(FoundationConfiguration.PROXY_PASSWORD));
            else
                this.proxyCredentials = (String) SysConfigUtils.getInstance().getFoundationConfiguration().getProperty(FoundationConfiguration.PROXY_PASSWORD);

            String lastupdateProxyCredentialsProperty = FoundationConfiguration.getProperty(FoundationConfiguration.PROXY_USER_LAST_UPDATE_TIMESTAMP);
            if (lastupdateProxyCredentialsProperty != null) {
                try {
                    this.lastUpdateProxyCredentials = formatter.parse(lastupdateProxyCredentialsProperty);
                } catch (ParseException pe) {
                    log.error(pe.getMessage());
                }
            }

            this.mainCredentials = JasyptUtils.decryptMainKey(FoundationConfiguration.getProperty(FoundationConfiguration.JASYPT_MAINKEY));
            String lastupdateMainCredentialsProperty = FoundationConfiguration.getProperty(FoundationConfiguration.MAIN_KEY_LAST_UPDATE_TIMESTAMP);
            if (lastupdateMainCredentialsProperty != null) {
                try {
                    this.lastUpdateMainCredentials = formatter.parse(lastupdateMainCredentialsProperty);
                } catch (ParseException pe) {
                    log.error(pe.getMessage());
                }
            }

            if (SysConfigUtils.isLDAPEnabled()) {
                this.ldapEnabled = true;
                try {
                    Object ldapPrincipalObj = SysConfigUtils.getInstance().getJOSSOLDAPConfiguration().getProperty(LDAP_SEC_PRINCIPAL);
                    // Apache parser automatically converts to Arraylist of it sees comma separated items. For ex securityPrincipal="cn=Administrator,dc=demo,dc=com"
                    if (ldapPrincipalObj instanceof ArrayList) {
                        this.ldapUserName = StringUtils.join(((ArrayList<String>) ldapPrincipalObj).iterator(), ",");
                    }
                    if (ldapPrincipalObj instanceof String) {
                        this.ldapUserName = (String) ldapPrincipalObj;
                    }
                    if (JasyptUtils.isEncryptionEnabled())
                        this.ldapCredentials = JasyptUtils.jasyptDecrypt((String) SysConfigUtils.getInstance().getJOSSOLDAPConfiguration().getProperty(LDAP_SEC_CREDENTIAL));
                    else
                        this.ldapCredentials = (String) SysConfigUtils.getInstance().getJOSSOLDAPConfiguration().getProperty(LDAP_SEC_CREDENTIAL);

                    File ldapConfigFile = new File(SysAccountInfo.TEST_LDAP_CONFIG_FILE);
                    this.lastUpdateLDAPCredentials = new Date(ldapConfigFile.lastModified());

                    // load LDAP domain credentials and last updated
                    this.ldapDomainCredentialsList.clear();
                    List<String> ldapDomains = SysConfigUtils.getInstance().getLdapDomains();
                    if (!ldapDomains.isEmpty()) {
                        for (String ldapDomain : ldapDomains) {
                            String[] ldapCredentials = Arrays.copyOf(SysConfigUtils.getInstance().getLdapDomainCredentials(ldapDomain), 2);
                            if (JasyptUtils.isEncryptionEnabled()) {
                                ldapCredentials[1] = JasyptUtils.jasyptDecrypt(ldapCredentials[1]);
                            }
                            this.ldapDomainCredentialsList.add(new DomainCredentials(ldapDomain, ldapCredentials[0], ldapCredentials[1]));
                        }

                        String lastupdateLdapDomainsCredentialsProperty = FoundationConfiguration.getProperty(FoundationConfiguration.LDAP_LAST_UPDATE_TIMESTAMP);
                        if (lastupdateLdapDomainsCredentialsProperty != null) {
                            try {
                                this.lastUpdateLdapDomainsCredentials = formatter.parse(lastupdateLdapDomainsCredentialsProperty);
                            } catch (ParseException pe) {
                                log.error(pe.getMessage());
                            }
                        }
                    }
                } catch (Exception exc) {
                    log.error(exc.getMessage());
                }
            }
        } catch (ConfigurationException e) {
            log.error(e);
        }
        setLastAll();
    }

    public void resetApi() {
        apiUserName = lastApiUserName;
        apiCredentials = lastApiCredentials;
    }

    public void resetApiReader() {
        apiReaderUserName = lastApiReaderUserName;
        apiReaderCredentials = lastApiReaderCredentials;
    }

    public void resetProxy() {
        proxyUserName = lastProxyUserName;
        proxyCredentials = lastProxyCredentials;
    }

    public void resetLdap() {
        ldapUserName = lastLdapUserName;
        ldapCredentials = lastLdapCredentials;
        copyLdapDomainCredentials(lastLdapDomainCredentialsList, ldapDomainCredentialsList);
    }

    public void resetRemote() {
        remoteAPIUserName = lastRemoteAPIUserName;
        remoteAPICredentials = lastRemoteAPICredentials;
    }

    public void resetMain() {
        mainCredentials = lastMainCredentials;
    }

    public void resetAll() {
        resetApi();
        resetApiReader();
        resetProxy();
        resetLdap();
        resetRemote();
        resetMain();
    }

    public void setLastApi() {
        lastApiUserName = apiUserName;
        lastApiCredentials = apiCredentials;
    }

    public void setLastApiReader() {
        lastApiReaderUserName = apiReaderUserName;
        lastApiReaderCredentials = apiReaderCredentials;
    }

    public void setLastProxy() {
        lastProxyUserName = proxyUserName;
        lastProxyCredentials = proxyCredentials;
    }

    public void setLastLdap() {
        lastLdapUserName = ldapUserName;
        lastLdapCredentials = ldapCredentials;
        copyLdapDomainCredentials(ldapDomainCredentialsList, lastLdapDomainCredentialsList);
    }

    public void setLastRemote() {
        lastRemoteAPIUserName = remoteAPIUserName;
        lastRemoteAPICredentials = remoteAPICredentials;
    }

    public void setLastMain() {
        lastMainCredentials = mainCredentials;
    }

    public void setLastAll() {
        setLastApi();
        setLastApiReader();
        setLastProxy();
        setLastLdap();
        setLastRemote();
        setLastMain();
    }

    public boolean isChangedApi() {
        return (isChanged(apiUserName, lastApiUserName) || isChanged(apiCredentials, lastApiCredentials));
    }

    public boolean isChangedApiReader() {
        return (isChanged(apiReaderUserName, lastApiReaderUserName) || isChanged(apiReaderCredentials, lastApiReaderCredentials));
    }

    public boolean isChangedProxy() {
        return (isChanged(proxyUserName, lastProxyUserName) || isChanged(proxyCredentials, lastProxyCredentials));
    }

    public boolean isChangedLdap() {
        return (isChanged(ldapUserName, lastLdapUserName) || isChanged(ldapCredentials, lastLdapCredentials) ||
                !equalsLdapDomainCredentials(ldapDomainCredentialsList, lastLdapDomainCredentialsList));
    }

    public boolean isChangedRemote() {
        return (isChanged(remoteAPIUserName, lastRemoteAPIUserName) || isChanged(remoteAPICredentials, lastRemoteAPICredentials));
    }

    public boolean isChangedMain() {
        return isChanged(mainCredentials, lastMainCredentials);
    }

    private static boolean isChanged(String string, String lastString) {
        return (((string == null) && (lastString != null)) || ((string != null) && !string.equals(lastString)));
    }

    private static String filterEmpty(String in) {
        return (((in != null) && !in.isEmpty()) ? in : null);
    }

    /**
     * Copy LDAP domain credentials.
     *
     * @param credentialsFrom LDAP domain credentials
     * @param credentialsTo LDAP domain credentials
     */
    private static void copyLdapDomainCredentials(List<DomainCredentials> credentialsFrom, List<DomainCredentials> credentialsTo) {
        credentialsTo.clear();
        for (DomainCredentials credentials : credentialsFrom) {
            credentialsTo.add(new DomainCredentials(credentials));
        }
    }

    /**
     * Compare copied LDAP domain credentials.
     *
     * @param credentials0 LDAP domain credentials
     * @param credentials1 LDAP domain credentials
     * @return equals
     */
    private static boolean equalsLdapDomainCredentials(List<DomainCredentials> credentials0, List<DomainCredentials> credentials1) {
        if (credentials0.size() != credentials1.size()) {
            return false;
        }
        for (Iterator<DomainCredentials> credentialsIter0 = credentials0.iterator(), credentialsIter1 = credentials1.iterator(); (credentialsIter0.hasNext() && credentialsIter1.hasNext());) {
            if (!credentialsIter0.next().equals(credentialsIter1.next())) {
                return false;
            }
        }
        return true;
    }

    public static class DomainCredentials {
        private String domain;
        private String principal;
        private String credential;

        public DomainCredentials(String domain, String principal, String credential) {
            if (principal == null || credential == null) {
                throw new IllegalArgumentException("domain principal or credential missing");
            }
            this.domain = domain;
            this.principal = principal;
            this.credential = credential;
        }

        public DomainCredentials(DomainCredentials credentials) {
            this(credentials.domain, credentials.principal, credentials.credential);
        }

        public boolean equals(Object other) {
            if (!(other instanceof DomainCredentials)) {
                return false;
            }
            DomainCredentials domainCredentialsOther = (DomainCredentials) other;
            return (((this.domain == null && domainCredentialsOther.domain == null) ||
                    (this.domain != null && this.domain.equals(domainCredentialsOther.domain))) &&
                    this.principal.equals(domainCredentialsOther.principal) &&
                    this.credential.equals(domainCredentialsOther.credential));
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public String getPrincipal() {
            return principal;
        }

        public void setPrincipal(String principal) {
            this.principal = principal;
        }

        public String getCredential() {
            return credential;
        }

        public void setCredential(String credential) {
            this.credential = credential;
        }
    }
}
