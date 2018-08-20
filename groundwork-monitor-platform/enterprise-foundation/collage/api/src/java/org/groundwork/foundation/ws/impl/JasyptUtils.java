package org.groundwork.foundation.ws.impl;

import com.chrylis.codec.base58.Base58Codec;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasypt.util.text.BasicTextEncryptor;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class JasyptUtils {

    /**
     * Logger.
     */
    private static Log log = LogFactory.getLog(JasyptUtils.class);

    private static final String APPLICATION_REALM = "ApplicationRealm";
    private static final String VALUE_FLAG = "--value=";
    private static final String RANDOM_FLAG = "--random";
    static final int RANDOM_LENGTH = 20;
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final String USAGE = "Usage: [ encrypt | update | updateapi | updateapireader ] [ " + VALUE_FLAG + "{value} | " + RANDOM_FLAG + " ]";

    private static void exitError(String message) {
        System.err.println("ERROR: " + message);
        System.err.println(USAGE);
        System.exit(1);
    }

    public static void main(String[] args) throws Exception {
        if (args == null) exitError("No arguments provided");
        if (args.length != 2) exitError("Incorrect number of arguments are required");

        String value = RANDOM_FLAG.equalsIgnoreCase(args[1]) ? genRandomString(RANDOM_LENGTH) : StringUtils.substringAfter(args[1], VALUE_FLAG);

        if (value.length() == 0) exitError("Invalid value provided");

        switch (args[0]) {
            case "encrypt":
                String encrypted_wsuser_password = JasyptUtils.jasyptEncrypt(value);
                System.out.println(encrypted_wsuser_password);
                break;

            case "update":
                JasyptUtils.updateCredentials();
                System.out.println("All credentials updated successfully");
                break;

            case "updateapi":
                JasyptUtils.updateAPICredentials(value, false);
                System.out.println("API credentials updated successfully");
                break;

            case "updateapireader":
                JasyptUtils.updateAPICredentials(value, true);
                System.out.println("API reader credentials updated successfully");
                break;

            default:
                exitError("Invalid operation");
        }
    }

    /**
     * Update credentials. Java rest clients are using tokensession manager to cache
     * credentials. Using this method will not flush the credentials from cache.
     * Only restarting gwservice will flush it.
     */
    private static void updateCredentials() {
        try {
            updateAPICredentials(jasyptEncrypt(WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_PASSWORD)), false);
            updateAPICredentials(jasyptEncrypt(WSClientConfiguration.getProperty(WSClientConfiguration.WEBSERVICES_READER_PASSWORD)), true);
            SysConfigUtils.getInstance().getFoundationConfiguration().setProperty(FoundationConfiguration.PROXY_PASSWORD, JasyptUtils.jasyptEncrypt(FoundationConfiguration.getProperty(FoundationConfiguration.PROXY_PASSWORD)));
            SysConfigUtils.getInstance().getFoundationConfiguration().save(SysConfigUtils.FOUNDATION_PROPERTIES);
            if (SysConfigUtils.isLDAPEnabled()) {
                // update JOSSO LDAP configuration
                String ldapCredentials = (String) SysConfigUtils.getInstance().getJOSSOLDAPConfiguration().getProperty(SysConfigUtils.LDAP_SEC_CREDENTIAL);
                SysConfigUtils.getInstance().getJOSSOLDAPConfiguration().setProperty(SysConfigUtils.LDAP_SEC_CREDENTIAL, JasyptUtils.jasyptEncrypt(ldapCredentials));
                SysConfigUtils.getInstance().getJOSSOLDAPConfiguration().save(SysConfigUtils.JOSSO_LDAP_PROPERTIES);
                // update LDAP Domains configurations
                for (String domain : SysConfigUtils.getInstance().getLdapDomains()) {
                    String[] ldapDomainCredentials = SysConfigUtils.getInstance().getLdapDomainCredentials(domain);
                    ldapDomainCredentials[1] = JasyptUtils.jasyptEncrypt(ldapDomainCredentials[1]);
                    SysConfigUtils.getInstance().setLdapDomainCredentials(domain, ldapDomainCredentials);
                }
            }
        } catch (Exception exc) {
            log.error(exc.getMessage());
        }

    }

    /**
     * Update API credentials only.
     */
    public static void updateAPICredentials(String password, boolean isReader) throws ConfigurationException {
        PropertiesConfiguration wsClientConfig = SysConfigUtils.getInstance().getWSClientConfiguration();

        // Ensure that the username itself is configured.  Create it if it doesn't exist.
        String usernameProperty = isReader ? WSClientConfiguration.WEBSERVICES_READER_USERNAME : WSClientConfiguration.WEBSERVICES_USERNAME;
        String username = (String) wsClientConfig.getProperty(usernameProperty);
        if (StringUtils.isBlank(username)) {
            username = isReader ? WSClientConfiguration.DEFAULT_WEBSERVICES_READER_USERNAME_VALUE : WSClientConfiguration.DEFAULT_WEBSERVICES_USERNAME_VALUE;
            wsClientConfig.setProperty(usernameProperty, username);
        }

        wsClientConfig.setProperty(isReader ? WSClientConfiguration.WEBSERVICES_READER_PASSWORD : WSClientConfiguration.WEBSERVICES_PASSWORD,
                jasyptEncrypt(password));
        wsClientConfig.setProperty(isReader ? WSClientConfiguration.WEBSERVICES_USER_READER_LAST_UPDATE_TIMESTAMP : WSClientConfiguration.WEBSERVICES_USER_LAST_UPDATE_TIMESTAMP,
                new SimpleDateFormat(SysConfigUtils.DATE_TIME_FORMAT_US).format(Calendar.getInstance().getTime()));
        wsClientConfig.save(SysConfigUtils.WSCLIENT_PROPERTIES);

        // Now first update the application-users.properties. Always use clear text password to generate the hash
        String applicationUsersAPIHash = DigestUtils.md5Hex(username + ":" + APPLICATION_REALM + ":" + password);

        PropertiesConfiguration appUsersProperties = SysConfigUtils.getInstance().getAppUsersProperties();
        appUsersProperties.setProperty(username, applicationUsersAPIHash);
        if (SysConfigUtils.DUAL_APP_USERS_PROPERTIES.isFile()) appUsersProperties.save(SysConfigUtils.DUAL_APP_USERS_PROPERTIES);
        appUsersProperties.save(SysConfigUtils.APP_USERS_PROPERTIES);
    }

    /**
     * Decrypts the jasypt encrypted string.
     *
     * @param encString base58/Flickr encoded encrypted string
     * @return decrypted string
     */
    public static String jasyptDecrypt(String encString) {
        if (JasyptUtils.isEncryptionEnabled()) {
            String jasyptMainKey = FoundationConfiguration.getProperty(FoundationConfiguration.JASYPT_MAINKEY);
            if (jasyptMainKey != null) {
                // decode base58/Flickr encoded encrypted string
                byte [] decodedEncStringBytes = Base58Codec.doDecode(encString);
                // base64 encode encrypted bytes
                String base64EncString = new String(Base64.encodeBase64(decodedEncStringBytes));
                // decrypt base64 encoded string
                BasicTextEncryptor decryptor = new BasicTextEncryptor();
                decryptor.setPassword(JasyptUtils.decryptMainKey(jasyptMainKey));
                encString = decryptor.decrypt(base64EncString);
            }
        }
        return encString;
    }

    /**
     * Helper to decrypt main key
     *
     * @param encMainkey
     * @return
     */
    public static String decryptMainKey(String encMainkey) {
        if (encMainkey != null && encMainkey.length() > 12) {
            String cipher = encMainkey.substring(12);
            return new String(Base64.decodeBase64(cipher.getBytes()));
        }
        return null;
    }

    /**
     * Helper to check if encryption is enabled
     *
     * @return
     */
    public static boolean isEncryptionEnabled() {
        String strEncryptionEnabled = WSClientConfiguration.getProperty(WSClientConfiguration.ENCRYPTION_ENABLED);
        return (strEncryptionEnabled == null || Boolean.parseBoolean(strEncryptionEnabled));
    }

    /**
     * Helper to jasypt encrypt credentials.
     *
     * @param credentials unencrypted credentials
     * @return base58/Flickr encoded encrypted credentials
     */
    public static String jasyptEncrypt(String credentials) {
        return jasyptEncrypt(credentials, null);
    }

    /**
     * Helper to jasypt encrypt credentials.
     *
     * @param credentials unencrypted credentials
     * @param jasyptMainKey jasypt main key override or null
     * @return base58/Flickr encoded encrypted credentials
     */
    public static String jasyptEncrypt(String credentials, String jasyptMainKey) {
        if (jasyptMainKey == null) {
            jasyptMainKey = JasyptUtils.decryptMainKey(FoundationConfiguration.getProperty(FoundationConfiguration.JASYPT_MAINKEY));
        }
        if (JasyptUtils.isEncryptionEnabled() && (jasyptMainKey != null)) {
            // loop until decoded credentials are not negative
            // since a negative leading byte injects padding in
            // base58/Flickr encoded bytes that cannot be removed
            // on decode predictably without encoding length in
            // encoded string: easier just to ensure encrypted
            // bytes are non-negative since encryption is a very rare
            // operation when compared to decryption and performance
            // is not an issue
            int encryptTries = 0;
            byte [] decodedEncCredentialsBytes = null;
            do {
                // limit encryption retries
                if (encryptTries++ > 100) {
                    throw new IllegalStateException("Cannot create non-negative encryption bytes for credentials, please retry.");
                }
                // encrypt credentials
                BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
                textEncryptor.setPassword(jasyptMainKey);
                String encCredentials = textEncryptor.encrypt(credentials);
                // decode base64 encoded encrypted credentials
                decodedEncCredentialsBytes = Base64.decodeBase64(encCredentials);
            } while ((decodedEncCredentialsBytes != null) && (decodedEncCredentialsBytes.length > 0) && (decodedEncCredentialsBytes[0] < 0));
            // base58/Flickr encode non-negative encrypted bytes
            credentials = Base58Codec.doEncode(decodedEncCredentialsBytes);
        }
        return credentials;
    }

    static String genRandomString(int length) {
        return RandomStringUtils.random(length, 0, 0, true, true, null, secureRandom);
    }

}
