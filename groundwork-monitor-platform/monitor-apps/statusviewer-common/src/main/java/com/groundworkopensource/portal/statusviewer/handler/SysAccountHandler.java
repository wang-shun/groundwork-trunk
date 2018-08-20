package com.groundworkopensource.portal.statusviewer.handler;

import com.groundwork.collage.util.TLSV12ClientConfiguration;
import com.groundworkopensource.portal.statusviewer.bean.SysAccountInfo;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.impl.FoundationConfiguration;
import org.groundwork.foundation.ws.impl.JasyptUtils;
import org.groundwork.foundation.ws.impl.SysConfigUtils;
import org.groundwork.foundation.ws.impl.WSClientConfiguration;
import org.groundwork.rs.client.AuthClient;
import org.groundwork.rs.client.LDAPAuthClient;

import javax.el.ELContext;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by ArulShanmugam on 9/6/14.
 */
public class SysAccountHandler {

    private static final Logger log = Logger.getLogger(SysAccountHandler.class
            .getName());

    private static Random rand = new Random((new Date()).getTime());

    static CharsetEncoder asciiEncoder =
            Charset.forName("US-ASCII").newEncoder(); // or "ISO-8859-1" for ISO Latin 1

    public static boolean isPureAscii(String v) {
        return asciiEncoder.canEncode(v);
    }

    private static final String APPLICATION_REALM = "ApplicationRealm";

    /**
     * Encrypt the mainpassword
     *
     * @param str
     * @return
     */
    public String encryptMainKey(String str) {
        byte[] salt = new byte[8];
        rand.nextBytes(salt);
        return new String(org.jasypt.contrib.org.apache.commons.codec_1_3.binary.Base64.encodeBase64(salt)) + new String(org.jasypt.contrib.org.apache.commons.codec_1_3.binary.Base64.encodeBase64(str.getBytes()));
    }


    public SysAccountInfo getSysAcctInfo() {
        Object bean = null;
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc != null) {
            ELContext elContext = fc.getELContext();
            bean = elContext.getELResolver().getValue(elContext, null, "sysAccountInfo");
        }

        return (SysAccountInfo) bean;
    }

    /**
     * Diabled check for sysaccounts test.
     *
     * @return disabled
     */
    public boolean isTestSysAccountsDisabled() {
        return getSysAcctInfo().isChangedApi() || getSysAcctInfo().isChangedApiReader() || getSysAcctInfo().isChangedProxy() ||
                getSysAcctInfo().isChangedLdap() || getSysAcctInfo().isChangedRemote() ||
                getSysAcctInfo().isChangedMain();
    }

    /**
     * Action to test sysaccounts
     */
    public boolean testSysAccounts() {
        // reset all accounts to last updated
        getSysAcctInfo().resetAll();
        // test all accounts
        log.debug("Testing SysAccounts");
        this.testStart();
        boolean soapResults = testSOAP(getSysAcctInfo().getApiUserName(), getSysAcctInfo().getApiCredentials());
        boolean restResults = testREST(getSysAcctInfo().getApiUserName(), getSysAcctInfo().getEncAPICredentials());
        boolean soapReaderResults = testSOAP(getSysAcctInfo().getApiReaderUserName(), getSysAcctInfo().getApiReaderCredentials());
        boolean restReaderResults = testREST(getSysAcctInfo().getApiReaderUserName(), getSysAcctInfo().getEncAPIReaderCredentials());
        boolean cactiResults = testCacti(getSysAcctInfo().getProxyUserName(), getSysAcctInfo().getProxyCredentials());
        boolean ldapResults = true;
        if (getSysAcctInfo().isLdapEnabled()) {
            ldapResults = testLDAPAccountCredentials();
        }
        this.testEnd();
        return (soapResults && restResults && soapReaderResults && restReaderResults && cactiResults && ldapResults);
    }

    /**
     * Diabled check for API account test.
     *
     * @return disabled
     */
    public boolean isTestAPIAccountDisabled() {
        return getSysAcctInfo().isChangedApi() || getSysAcctInfo().isChangedMain();
    }

    /**
     * Diabled check for API Reader account test.
     *
     * @return disabled
     */
    public boolean isTestAPIReaderAccountDisabled() {
        return getSysAcctInfo().isChangedApiReader() || getSysAcctInfo().isChangedMain();
    }

    public void testAPIAccount(ActionEvent event) {
        // reset accounts to last updated
        getSysAcctInfo().resetApi();
        getSysAcctInfo().resetMain();
        // test api account
        testSOAP(getSysAcctInfo().getApiUserName(), getSysAcctInfo().getApiCredentials());
        testREST(getSysAcctInfo().getApiUserName(), getSysAcctInfo().getEncAPICredentials());
    }

    public void testAPIReaderAccount(ActionEvent event) {
        // reset accounts to last updated
        getSysAcctInfo().resetApiReader();
        getSysAcctInfo().resetMain();
        // test api reader account
        testSOAP(getSysAcctInfo().getApiReaderUserName(), getSysAcctInfo().getApiReaderCredentials());
        testREST(getSysAcctInfo().getApiReaderUserName(), getSysAcctInfo().getEncAPIReaderCredentials());
    }

    /**
     * Diabled check for proxy account test.
     *
     * @return disabled
     */
    public boolean isTestProxyAccountDisabled() {
        return getSysAcctInfo().isChangedProxy() || getSysAcctInfo().isChangedMain();
    }

    public void testProxyAccount(ActionEvent event) {
        // reset accounts to last updated
        getSysAcctInfo().resetProxy();
        getSysAcctInfo().resetMain();
        // test proxy account
        testCacti(getSysAcctInfo().getProxyUserName(), getSysAcctInfo().getProxyCredentials());
    }

    /**
     * Diabled check for LDAP account test.
     *
     * @return disabled
     */
    public boolean isTestLDAPAccountDisabled() {
        return getSysAcctInfo().isChangedLdap() || getSysAcctInfo().isChangedMain();
    }

    public void testLDAPAccount(ActionEvent event) {
        // reset accounts to last updated
        getSysAcctInfo().resetLdap();
        getSysAcctInfo().resetMain();
        // test ldap accounts
        if (getSysAcctInfo().isLdapEnabled()) {
            testLDAPAccountCredentials();
        }
    }


    /**
     * Adds faces message
     *
     * @param severity
     * @param summary
     * @param messageDetail
     */
    private void addMessage(FacesMessage.Severity severity, String summary, String messageDetail) {
        FacesMessage message = new FacesMessage(
                severity,
                summary,
                messageDetail);
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, message);
    }

    /**
     * Disabled check for mainkey update.
     *
     * @return disabled
     */
    public boolean isUpdateMainKeyDisabled() {
        return !getSysAcctInfo().isChangedMain();
    }

    /**
     * Updates the mainkey only
     *
     * @param event
     */
    public void updateMainKey(ActionEvent event) {
        // reset accounts to last updated
        getSysAcctInfo().resetApi();
        getSysAcctInfo().resetApiReader();
        getSysAcctInfo().resetProxy();
        getSysAcctInfo().resetLdap();
        getSysAcctInfo().resetRemote();
        // update only if main account changed
        if (!getSysAcctInfo().isChangedMain()) {
            return;
        }
        // update main account: at this point mainKey is always decrypted text
        String mainCredentials = getSysAcctInfo().getMainCredentials();
        if (SysAccountHandler.isPureAscii(mainCredentials)) {
            try {
                SysConfigUtils.getInstance().getFoundationConfiguration().setProperty(FoundationConfiguration.JASYPT_MAINKEY, encryptMainKey(mainCredentials));
                SysConfigUtils.getInstance().getFoundationConfiguration().setProperty(FoundationConfiguration.MAIN_KEY_LAST_UPDATE_TIMESTAMP, new SimpleDateFormat(SysConfigUtils.DATE_TIME_FORMAT_US).format(Calendar.getInstance().getTime()));
                // save foundation configuration and force immediate reload
                SysConfigUtils.getInstance().getFoundationConfiguration().save(SysConfigUtils.FOUNDATION_PROPERTIES);
                FoundationConfiguration.reload();
                getSysAcctInfo().setLastMain();
                this.updateSysAccounts();
                this.getSysAcctInfo().init();
                if (this.testSysAccounts()) {
                    addMessage(FacesMessage.SEVERITY_INFO, "Confirmation : ", "Master password updated successfully!");
                } else {
                    throw new ConfigurationException("ERROR updating system accounts");
                }
            } catch (ConfigurationException exc) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Master password update FAILED!", exc.getMessage());
                log.error(exc.getMessage());
            }
        }
        else {
            addMessage(FacesMessage.SEVERITY_ERROR, "Master password update FAILED!", "NON-ASCII characters are not allowed!");
        }
    }

    /**
     * Disabled check for API account update.
     *
     * @return disabled
     */
    public boolean isUpdateAPIAccountDisabled() {
        return !getSysAcctInfo().isChangedApi();
    }

    /**
     * Disabled check for API reader account update.
     *
     * @return disabled
     */
    public boolean isUpdateAPIReaderAccountDisabled() {
        return !getSysAcctInfo().isChangedApiReader();
    }


    /**
     * Updates API account only
     *
     * @param event
     */
    public void updateAPIAccount(ActionEvent event) {
        if (event != null) {
            // reset accounts to last updated
            getSysAcctInfo().resetMain();
            // update only if api account changed
            if (!getSysAcctInfo().isChangedApi()) return;
        }
        // update api account
        try {
            SysConfigUtils.getInstance().getWSClientConfiguration().setProperty(WSClientConfiguration.WEBSERVICES_USERNAME, getSysAcctInfo().getApiUserName());
            JasyptUtils.updateAPICredentials(getSysAcctInfo().getApiCredentials(), false);
            getSysAcctInfo().setLastApi();
            addMessage(FacesMessage.SEVERITY_INFO, "Confirmation : ","API account updated successfully!");
            // Now remove the credentials from the tokensessionmanager so the rest client can read the new credentials from the ws_client.properties.
            removeCredentialsFromSession();
        } catch (ConfigurationException exc) {
            addMessage(FacesMessage.SEVERITY_ERROR, "API account update FAILED!", exc.getMessage());
            log.error(exc.getMessage());
        }
        if (event != null) this.getSysAcctInfo().init();
    }

    /**
     * Updates API Reader account only
     *
     * @param event
     */
    public void updateAPIReaderAccount(ActionEvent event) {
        if (event != null) {
            // reset accounts to last updated
            getSysAcctInfo().resetMain();
            // update only if api Reader account changed
            if (!getSysAcctInfo().isChangedApiReader()) return;
        }
        // update api reader account
        try {
            SysConfigUtils.getInstance().getWSClientConfiguration().setProperty(WSClientConfiguration.WEBSERVICES_READER_USERNAME, getSysAcctInfo().getApiReaderUserName());
            JasyptUtils.updateAPICredentials(getSysAcctInfo().getApiReaderCredentials(), true);
            getSysAcctInfo().setLastApiReader();
            addMessage(FacesMessage.SEVERITY_INFO, "Confirmation : ","API Reader account updated successfully!");
            // Now remove the credentials from the tokensessionmanager so the rest client can read the new credentials from the ws_client.properties.
            removeCredentialsFromSession();
        } catch (ConfigurationException exc) {
            addMessage(FacesMessage.SEVERITY_ERROR, "API Reader account update FAILED!", exc.getMessage());
            log.error(exc.getMessage());
        }
        if (event != null) this.getSysAcctInfo().init();
    }

    private void removeCredentialsFromSession() throws ConfigurationException {
        // Now remove the credentials from the tokensessionmanager so the rest client can read the new credentials from the
        String authURL = (String)SysConfigUtils.getInstance().getWSClientConfiguration().getProperty(WSClientConfiguration.FOUNDATION_REST_ENDPOINT);
        String statusRESTURL = (String)SysConfigUtils.getInstance().getWSClientConfiguration().getProperty(WSClientConfiguration.WEBSERVICES_ENDPOINT);
        String[] arr = statusRESTURL.split("status-restservice");
        String portalRESTURL = arr[0] + "rest";
        AuthClient authClient = new AuthClient(authURL);
        authClient.removeCredentialsFromTokenSession(authURL);
        authClient.removeCredentialsFromTokenSession(portalRESTURL);
    }

    /**
     * Disabled check for remote API account update.
     *
     * @return disabled
     */
    public boolean isUpdateRemoteAPIAccountDisabled() {
        return !getSysAcctInfo().isChangedRemote();
    }

    /**
     * Updates remote API account only
     *
     * @param event
     */
    public void updateRemoteAPIAccount(ActionEvent event) {
        // reset accounts to last updated
        getSysAcctInfo().resetMain();
        // update only if remote account changed
        if (!getSysAcctInfo().isChangedRemote()) {
            return;
        }
        // update remote account
        String remoteAPICredentials = getSysAcctInfo().getRemoteAPICredentials();
        try {
            remoteAPICredentials = this.encryptCredentials(remoteAPICredentials, false);
            String applicationUsersRemoteAPIHash = DigestUtils.md5Hex(getSysAcctInfo().getRemoteAPIUserName() + ":" + APPLICATION_REALM + ":" + remoteAPICredentials);
            SysConfigUtils.getInstance().getAppUsersProperties().setProperty(getSysAcctInfo().getRemoteAPIUserName(),applicationUsersRemoteAPIHash);
            PropertiesConfiguration appUsersProperties = SysConfigUtils.getInstance().getAppUsersProperties();
            if (SysConfigUtils.DUAL_APP_USERS_PROPERTIES.isFile()) {
                appUsersProperties.save(SysConfigUtils.DUAL_APP_USERS_PROPERTIES);
            }
            appUsersProperties.save(SysConfigUtils.APP_USERS_PROPERTIES);
            getSysAcctInfo().setLastRemote();
            addMessage(FacesMessage.SEVERITY_INFO, "Confirmation : ","Remote API account updated successfully!");
        } catch (ConfigurationException exc) {
            addMessage(FacesMessage.SEVERITY_ERROR, "API account update FAILED!", exc.getMessage());
            log.error(exc.getMessage());
        }
    }

    /**
     * Encrypt a password
     * @param event
     */
    public void encryptToolPassword(ActionEvent event) throws ConfigurationException {
        String toolCredentials = getSysAcctInfo().getToolCredentials();
        if (toolCredentials != null && !toolCredentials.equalsIgnoreCase(""))
            getSysAcctInfo().setToolEncCredentials(this.encryptCredentials(toolCredentials, false));

    }

    /**
     * Disabled check for proxy account update.
     *
     * @return disabled
     */
    public boolean isUpdateProxyAccountDisabled() {
        return !getSysAcctInfo().isChangedProxy();
    }

    /**
     * Updates the proxy account information only
     *
     * @param event
     */
    public void updateProxyAccount(ActionEvent event) {
        if (event != null) {
            // reset accounts to last updated
            getSysAcctInfo().resetMain();
            // update only if proxy account changed
            if (!getSysAcctInfo().isChangedProxy()) {
                return;
            }
        }
        // update proxy account
        boolean testResult = true;
        if (event != null) {
            testResult = testCacti(getSysAcctInfo().getProxyUserName(), getSysAcctInfo().getProxyCredentials());
        }
        if (testResult) {
            String proxyCredentials = getSysAcctInfo().getProxyCredentials();
            try {
                SysConfigUtils.getInstance().getFoundationConfiguration().setProperty(FoundationConfiguration.PROXY_USER, getSysAcctInfo().getProxyUserName());
                proxyCredentials = this.encryptCredentials(proxyCredentials,event != null ? false: true);
                SysConfigUtils.getInstance().getFoundationConfiguration().setProperty(FoundationConfiguration.PROXY_PASSWORD, proxyCredentials);
                SysConfigUtils.getInstance().getFoundationConfiguration().setProperty(FoundationConfiguration.PROXY_USER_LAST_UPDATE_TIMESTAMP, new SimpleDateFormat(SysConfigUtils.DATE_TIME_FORMAT_US).format(Calendar.getInstance().getTime()));
                // save foundation configuration and force immediate reload
                SysConfigUtils.getInstance().getFoundationConfiguration().save(SysConfigUtils.FOUNDATION_PROPERTIES);
                FoundationConfiguration.reload();
                getSysAcctInfo().setLastProxy();
                addMessage(FacesMessage.SEVERITY_INFO, "Confirmation : ","Proxy account updated successfully!");
            } catch (ConfigurationException exc) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Proxy account update FAILED!", exc.getMessage());
                log.error(exc.getMessage());
            }
        } // end if
        if (event != null)
            this.getSysAcctInfo().init();
    }


    /**
     * Disabled check for LDAP account update.
     *
     * @return disabled
     */
    public boolean isUpdateLDAPAccountDisabled() {
        return !getSysAcctInfo().isChangedLdap();
    }

    /**
     * Updates the LDAP configuration only
     *
     * @param event
     */
    public void updateLDAPAccount(ActionEvent event) {
        if (event != null) {
            // reset accounts to last updated
            getSysAcctInfo().resetMain();
            // update only if ldap account changed
            if (!getSysAcctInfo().isChangedLdap()) {
                return;
            }
        }
        if (getSysAcctInfo().isLdapEnabled()) {
            // test ldap accounts
            boolean testResult = true;
            if (event != null) {
                testResult = testLDAPAccountCredentials();
            }
            // update ldap accounts
            if (testResult) {
                boolean error = false;
                List<SysAccountInfo.DomainCredentials> ldapDomains = getSysAcctInfo().getLdapDomainCredentialsList();
                if (ldapDomains.isEmpty()) {
                    // update JOSSO LDAP configuration
                    String ldapCredentials = getSysAcctInfo().getLdapCredentials();
                    try {
                        SysConfigUtils.getInstance().getJOSSOLDAPConfiguration().setProperty(SysAccountInfo.LDAP_SEC_PRINCIPAL, getSysAcctInfo().getLdapUserName());
                        ldapCredentials = this.encryptCredentials(ldapCredentials, event != null ? false : true);
                        SysConfigUtils.getInstance().getJOSSOLDAPConfiguration().setProperty(SysAccountInfo.LDAP_SEC_CREDENTIAL, ldapCredentials);
                        SysConfigUtils.getInstance().getJOSSOLDAPConfiguration().save(SysConfigUtils.JOSSO_LDAP_PROPERTIES);
                        addMessage(FacesMessage.SEVERITY_INFO, "Confirmation : ", "LDAP account updated successfully!");
                    } catch (Exception exc) {
                        error = true;
                        addMessage(FacesMessage.SEVERITY_ERROR, "LDAP account update FAILED!", exc.getMessage());
                        log.error(exc.getMessage());
                    }
                } else {
                    // update LDAP domains configurations
                    for (SysAccountInfo.DomainCredentials ldapDomain : ldapDomains) {
                        String[] ldapCredentials = new String[]{ldapDomain.getPrincipal(), ldapDomain.getCredential()};
                        try {
                            ldapCredentials[1] = this.encryptCredentials(ldapCredentials[1], event != null ? false : true);
                            SysConfigUtils.getInstance().setLdapDomainCredentials(ldapDomain.getDomain(), ldapCredentials);
                            SysConfigUtils.getInstance().getFoundationConfiguration().setProperty(FoundationConfiguration.LDAP_LAST_UPDATE_TIMESTAMP, new SimpleDateFormat(SysConfigUtils.DATE_TIME_FORMAT_US).format(Calendar.getInstance().getTime()));
                            SysConfigUtils.getInstance().getFoundationConfiguration().save(SysConfigUtils.FOUNDATION_PROPERTIES);
                            addMessage(FacesMessage.SEVERITY_INFO, "Confirmation : ", "LDAP " + ldapDomain.getDomain() + " account updated successfully!");
                        } catch (Exception exc) {
                            error = true;
                            addMessage(FacesMessage.SEVERITY_ERROR, "LDAP " + ldapDomain.getDomain() + " account update FAILED!", exc.getMessage());
                            log.error(exc.getMessage());
                        }
                    }
                }
                if (!error) {
                    if (!ldapDomains.isEmpty()) {
                        FoundationConfiguration.reload();
                    }
                    getSysAcctInfo().setLastLdap();
                }
            }
        }
        if (event != null)
            this.getSysAcctInfo().init();
    }

    /**
     * Test LDAP account credentials.
     *
     * @return result
     */
    private boolean testLDAPAccountCredentials() {
        boolean testResult = true;
        List<SysAccountInfo.DomainCredentials> ldapDomains = getSysAcctInfo().getLdapDomainCredentialsList();
        if (ldapDomains.isEmpty()) {
            // test JOSSO LDAP configuration
            testResult = testLDAP(null, getSysAcctInfo().getLdapUserName(), getSysAcctInfo().getLdapCredentials());
        } else {
            // test LDAP domains configurations
            for (SysAccountInfo.DomainCredentials ldapDomain : ldapDomains) {
                testResult = testLDAP(ldapDomain.getDomain(), ldapDomain.getPrincipal(), ldapDomain.getCredential()) && testResult;
            }
        }
        return testResult;
    }

    /**
     * Helper to encrypt credentials
     *
     * @param credentials
     * @return
     */
    private String encryptCredentials(String credentials, boolean useFormValue) throws ConfigurationException  {
        String jasyptMainKey = null;
        if (useFormValue) {
            // encrypted form value set in update main key
            jasyptMainKey = (String) SysConfigUtils.getInstance().getFoundationConfiguration().getProperty(FoundationConfiguration.JASYPT_MAINKEY);
            jasyptMainKey = JasyptUtils.decryptMainKey(jasyptMainKey);
        }
        return JasyptUtils.jasyptEncrypt(credentials, jasyptMainKey);
    }

    /**
     * Action to update system accounts
     *
     * @return
     */
    private void updateSysAccounts() {
        log.debug("Updating SysAccounts");
        this.updateAPIAccount(null);
        this.updateAPIReaderAccount(null);
        this.updateProxyAccount(null);
        this.updateLDAPAccount(null);
        this.getSysAcctInfo().init();
    }

    /**
     * Helper to test SOAP
     *
     * @return
     */
    private boolean testSOAP(String user, String password) {
        boolean results;
        DefaultHttpClient client = null;
        String testMessage = "SOAP API account testing (user=" + user + "): ";
        try {
            client = new DefaultHttpClient();
            TLSV12ClientConfiguration.configure(client);
            String testSoapURL = SysConfigUtils.getInstance().getGateinConfiguration().getProperty("gatein.sso.portal.url") + "/foundation-webapp/services/wshost?wsdl";
            HttpGet httpGet = new HttpGet(testSoapURL);
            String encoding = Base64.encodeBase64String((user + ":" + password).getBytes("ASCII"));
            httpGet.setHeader("Authorization", "Basic " + encoding.replaceAll("[\n\r]", ""));
            httpGet.setHeader("SOAPAction", "soapaction");
            httpGet.setHeader("Accept", "text/html,application/xhtml;q=0.9,*/*;q=0.8");

            HttpResponse httpResponse = client.execute(httpGet);
            int respCode = httpResponse.getStatusLine().getStatusCode();
            String respMessage = httpResponse.getStatusLine().getReasonPhrase();
            HttpEntity entity = httpResponse.getEntity();
            EntityUtils.consume(entity);
            if (respCode != 200) {
                addMessage(FacesMessage.SEVERITY_ERROR, testMessage, " FAILED! ==> " + respCode + " " + respMessage);
                results = false;
            } else {
                addMessage(FacesMessage.SEVERITY_INFO, testMessage, " SUCCESS! ==> " + respCode + " " + respMessage);
                results = true;
            }
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, testMessage, " FAILED! ==> " + e.getMessage());
            results = false;
        } finally {
            client.getConnectionManager().shutdown();
        }
        return results;
    }

    /**
     * Helper to test REST
     *
     * @return
     */
    private boolean testREST(String user, String password) {
        String appName = "testapp";
        String testMessage = "REST API account testing (user=" + user + "): ";

        String authURL;
        try {
            authURL = (String) SysConfigUtils.getInstance().getWSClientConfiguration().getProperty(WSClientConfiguration.FOUNDATION_REST_ENDPOINT);
        } catch (ConfigurationException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, testMessage, " ERROR! ==> unable to read wsclient configuration file");
            return false;
        }

        AuthClient authClient = new AuthClient(authURL);
        AuthClient.Response response = authClient.login(user, password, appName);
        if (!response.success()) {
            addMessage(FacesMessage.SEVERITY_ERROR, testMessage, " FAILED! ==> " + response.getStatus().toString());
            return false;
        }
        String token = response.getToken();
        if (!Boolean.parseBoolean(authClient.isTokenValid(appName, token))) {
            addMessage(FacesMessage.SEVERITY_ERROR, testMessage, " FAILED! ==> " + response.getStatus().toString());
            return false;
        }
        authClient.logout(appName, token);
        addMessage(FacesMessage.SEVERITY_INFO, testMessage, " SUCCESS! ==> " + response.getStatus().toString());
        return true;
    }

    /**
     * Access cacti
     */
    private boolean testCacti(String user, String password) {
        InputStream is = null;
        BufferedReader br = null;
        DefaultHttpClient httpclient = null;
        boolean results = false;
        try {

            httpclient = new DefaultHttpClient();
            TLSV12ClientConfiguration.configure(httpclient);

            String testLoginURL = SysConfigUtils.getInstance().getGateinConfiguration().getProperty("gatein.sso.portal.url") + "/josso/signon/usernamePasswordLogin.do";
            HttpGet httpget = new HttpGet(testLoginURL);

            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                entity.consumeContent();
            }

            HttpPost httpost = new HttpPost(testLoginURL);

            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("josso_username", user));
            nvps.add(new BasicNameValuePair("josso_password", password));
            nvps.add(new BasicNameValuePair("josso_cmd", "login"));

            httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

            response = httpclient.execute(httpost);
            entity = response.getEntity();

            int responseCode = 401;
            String responseMessage = response.getStatusLine().getReasonPhrase();


            if (entity != null) {
                String authVerificationString = "You have been successfully authenticated";
                is = entity.getContent();
                br = new BufferedReader(new InputStreamReader(is));
                String str = "";
                while ((str = br.readLine()) != null) {
                    if (str.contains(authVerificationString)) {
                        responseCode = 200;

                    }
                }
            }

            if (responseCode != 200) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Proxy account testing(Cacti) : ", " FAILED! ==> " + HttpStatus.SC_UNAUTHORIZED + " Unauthorized");
                return false;
            }
            responseCode = 401;

            HttpGet httget = new HttpGet(SysConfigUtils.getInstance().getGateinConfiguration().getProperty("gatein.sso.portal.url") + "/nms-cacti/index.php?gwuid=admin");
            HttpResponse response2 = httpclient.execute(httget);
            entity = response2.getEntity();
            responseCode = response2.getStatusLine().getStatusCode();
            responseMessage = response2.getStatusLine().getReasonPhrase();


            if (entity != null) {
                String cactiVerificationString = "cacti_backdrop.gif";
                is = entity.getContent();
                br = new BufferedReader(new InputStreamReader(is));
                String str = "";
                while ((str = br.readLine()) != null) {
                    if (str.contains(cactiVerificationString)) {
                        responseCode = 200;
                    }
                }
            }
            if (responseCode == 200) {
                addMessage(FacesMessage.SEVERITY_INFO, "Proxy account testing(Cacti) : ", " SUCCESS! ==> " + HttpStatus.SC_OK + " OK");
                results = true;
            } else {
                addMessage(FacesMessage.SEVERITY_ERROR, "Proxy account testing(Cacti) : ", " FAILED! ==> " + "HTTP/1.1  " + responseCode + " " + responseMessage);
                results = false;
            }
        } catch (Exception exc) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Proxy account testing(Cacti) : ", " FAILED! ==> " + exc.getMessage());
            results = false;
        } finally {
            try {
                if (is != null)
                    is.close();
                if (br != null)
                    br.close();
            } catch (Exception exc) {
                log.error(exc.getMessage());
            }
            httpclient.getConnectionManager().shutdown();
        }
        return results;
    }

    /**
     * Helper to test LDAP. Verifies that LDAP can be accessed using
     * bind credentials and that search base can be queried.
     *
     * @param domain LDAP domain or null for the default domain
     * @param bindUserDN LDAP access credentials user
     * @param bindCredentials LDAP access credentials password
     * @return test result
     */
    private boolean testLDAP(String domain, String bindUserDN, String bindCredentials) {
        boolean results = false;
        String messageFormattedDomain = (domain != null ? domain + " " : "");
        String testingBindMessage = "LDAP " + messageFormattedDomain + "account testing(Bind):";
        try {
            // lookup REST client deployment configuration
            String deploymentUrl = WSClientConfiguration.getProperty(WSClientConfiguration.FOUNDATION_REST_ENDPOINT);
            // get LDAP authentication REST client
            LDAPAuthClient ldapAuthClient = new LDAPAuthClient(deploymentUrl);
            // validate security credentials against domain
            if (ldapAuthClient.validateCredentials(domain, bindUserDN, bindCredentials)) {
                addMessage(FacesMessage.SEVERITY_INFO, testingBindMessage, " SUCCESS!");
                results = true;
            } else {
                addMessage(FacesMessage.SEVERITY_ERROR, testingBindMessage, " FAILED! ==> " +
                        "Cannot bind to LDAP/AD " + messageFormattedDomain + "using " + bindUserDN + " with credentials");
                results = false;
            }
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, testingBindMessage, " FAILED! ==> " +
                    "Unexpected LDAP/AD " + messageFormattedDomain + "binding error using " + bindUserDN + ": " + e.getMessage());
            results = false;
        }
        return results;
    }

    private void testStart() {
        this.getSysAcctInfo().setTestButtonDisabled(true);
        this.getSysAcctInfo().setTestButtonLabel("Please wait..");
    }

    private void testEnd() {
        this.getSysAcctInfo().setTestButtonDisabled(false);
        this.getSysAcctInfo().setTestButtonLabel("Test");
    }
}
