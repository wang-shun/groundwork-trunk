/*
 * JOSSO: Java Open Single Sign-On
 *
 * Copyright 2004-2009, Atricore, Inc.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */
package org.josso.gateway.identity.service.store.ldap;

import com.chrylis.codec.base58.Base58Codec;
import com.groundwork.core.security.ldap.LDAPAggregator;
import com.groundwork.core.security.ldap.LDAPConfig;
import com.groundwork.core.security.ldap.LDAPMapper;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasypt.util.text.BasicTextEncryptor;
import org.josso.auth.BaseCredential;
import org.josso.auth.Credential;
import org.josso.auth.CredentialKey;
import org.josso.auth.CredentialProvider;
import org.josso.auth.scheme.AuthenticationScheme;
import org.josso.auth.scheme.PasswordCredential;
import org.josso.auth.scheme.UsernameCredential;
import org.josso.gateway.SSONameValuePair;
import org.josso.gateway.identity.exceptions.NoSuchUserException;
import org.josso.gateway.identity.exceptions.SSOIdentityException;
import org.josso.gateway.identity.service.BaseRole;
import org.josso.gateway.identity.service.BaseRoleImpl;
import org.josso.gateway.identity.service.BaseUser;
import org.josso.gateway.identity.service.BaseUserImpl;
import org.josso.gateway.identity.service.store.AbstractStore;
import org.josso.gateway.identity.service.store.CertificateUserKey;
import org.josso.gateway.identity.service.store.ExtendedIdentityStore;
import org.josso.gateway.identity.service.store.SimpleUserKey;
import org.josso.gateway.identity.service.store.UserKey;
import org.josso.selfservices.ChallengeResponseCredential;

import javax.naming.NamingException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * An implementation of an Identity and Credential Store which obtains credential, user and
 * role information from an LDAP server using JNDI, based on the configuration properties.
 * <p/>
 * It allows to set whatever options your LDAP JNDI provider supports your Gateway
 * configuration file.
 * Examples of standard property names are:
 * <ul>
 * <li><code>initialContextFactory = "java.naming.factory.initial"</code>
 * <li><code>securityProtocol = "java.naming.security.protocol"</code>
 * <li><code>providerUrl = "java.naming.provider.url"</code>
 * <li><code>securityAuthentication = "java.naming.security.authentication"</code>
 * </ul>
 * <p/>
 * This store implementation is both an Identity Store and Credential Store.
 * Since in JOSSO the authentication of the user is left to the configured Authentication Scheme,
 * this store implementation cannot delegate user identity assertion by binding to the
 * LDAP server. For that reason it retrieves the required credentials from the directory
 * leaving the authentication procedure to the configured Authentication Scheme.
 * The store must be supplied with the configuratoin parameters so that it can retrieve user
 * identity information.
 * <p/>
 * <p/>
 * Additional component properties include:
 * <ul>
 * <li>securityPrincipal: the DN of the user to be used to bind to the LDAP Server
 * <li>securityCredential: the securityPrincipal password to be used for binding to the
 * LDAP Server.
 * <li>securityAuthentication: the security level to be used with the LDAP Server session.
 * Its value is one of the following strings:
 * "none", "simple", "strong".
 * If not set, "simple" will be used.
 * <li>ldapSearchScope : alows control over LDAP search scope : valid values are ONELEVEL, SUBTREE</li>
 * <li>usersCtxDN : the fixed distinguished name to the context to search for user accounts.
 * <li>principalUidAttributeID: the name of the attribute that contains the user login name.
 * This is used to locate the user.
 * <li>rolesCtxDN : The fixed distinguished name to the context to search for user roles.
 * <li>uidAttributeID: the name of the attribute that, in the object containing the user roles,
 * references role members. The attribute value should be the DN of the user associated with the
 * role. This is used to locate the user roles.
 * <li>roleAttributeID : The name of the attribute that contains the role name
 * <li>roleMatchingMOde : The way JOSSO gets users roles, values UDN (default) and UID.
 * <li>credentialQueryString : The query string to obtain user credentials. It should have the
 * following format : user_attribute_name=credential_attribute_name,...
 * For example :
 * uid=username,userPassword=password
 * <li>userPropertiesQueryString : The query string to obtain user properties. It should have
 * the following format : ldap_attribute_name=user_attribute_name,...
 * For example :
 * mail=mail,cn=description
 * </ul>
 * A sample LDAP Identity Store configuration :
 * <p/>
 * <pre>
 * &lt;sso-identity-store&gt;
 * &lt;class&gt;org.josso.gateway.identity.service.store.ldap.LDAPIdentityStore&lt;/class&gt;
 * &lt;initialContextFactory&gt;com.sun.jndi.ldap.LdapCtxFactory&lt;/initialContextFactory&gt;
 * &lt;providerUrl&gt;ldap://localhost&lt;/providerUrl&gt;
 * &lt;securityPrincipal&gt;cn=Manager\,dc=my-domain\,dc=com&lt;/securityPrincipal&gt;
 * &lt;securityCredential&gt;secret&lt;/securityCredential&gt;
 * &lt;securityAuthentication&gt;simple&lt;/securityAuthentication&gt;
 * &lt;usersCtxDN&gt;ou=People\,dc=my-domain\,dc=com&lt;/usersCtxDN&gt;
 * &lt;principalUidAttributeID&gt;uid&lt;/principalUidAttributeID&gt;
 * &lt;rolesCtxDN&gt;ou=Roles\,dc=my-domain\,dc=com&lt;/rolesCtxDN&gt;
 * &lt;uidAttributeID&gt;uniquemember&lt;/uidAttributeID&gt;
 * &lt;roleMatchingMode&gt;UDN&lt;/roleMatchingMode&gt;
 * &lt;roleAttributeID&gt;cn&lt;/roleAttributeID&gt;
 * &lt;credentialQueryString&gt;uid=username\,userPassword=password&lt;/credentialQueryString&gt;
 * &lt;userPropertiesQueryString&gt;mail=mail\,cn=description&lt;/userPropertiesQueryString&gt;
 * &lt;ldapSearchScope&gt;SUBTREE&lt;/ldapSearchScope&gt;
 * &lt;/sso-identity-store&gt;
 * </pre>
 * <p/>
 * A sample LDAP Credential Store configuration :
 * <p/>
 * <pre>
 * &lt;credential-store&gt;
 * &lt;class&gt;org.josso.gateway.identity.service.store.ldap.LDAPIdentityStore&lt;/class&gt;
 * &lt;initialContextFactory&gt;com.sun.jndi.ldap.LdapCtxFactory&lt;/initialContextFactory&gt;
 * &lt;providerUrl&gt;ldap://localhost&lt;/providerUrl&gt;
 * &lt;securityPrincipal&gt;cn=Manager\,dc=my-domain\,dc=com&lt;/securityPrincipal&gt;
 * &lt;securityCredential&gt;secret&lt;/securityCredential&gt;
 * &lt;securityAuthentication&gt;simple&lt;/securityAuthentication&gt;
 * &lt;usersCtxDN&gt;ou=People\,dc=my-domain\,dc=com&lt;/usersCtxDN&gt;
 * &lt;principalUidAttributeID&gt;uid&lt;/principalUidAttributeID&gt;
 * &lt;rolesCtxDN&gt;ou=Roles\,dc=my-domain\,dc=com&lt;/rolesCtxDN&gt;
 * &lt;uidAttributeID&gt;uniquemember&lt;/uidAttributeID&gt;
 * &lt;roleAttributeID&gt;cn&lt;/roleAttributeID&gt;
 * &lt;credentialQueryString&gt;uid=username\,userPassword=password&lt;/credentialQueryString&gt;
 * &lt;userPropertiesQueryString&gt;mail=mail\,cn=description&lt;/userPropertiesQueryString&gt;
 * &lt;/credential-store&gt;
 * </pre>
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version CVS $Id: LDAPIdentityStore.java 552 2008-04-25 13:38:29Z ajadzinsky $
 *
 * @org.apache.xbean.XBean element="ldap-store"
 */

public class LDAPIdentityStore extends AbstractStore implements ExtendedIdentityStore {

    private static final Log logger = LogFactory.getLog(LDAPIdentityStore.class);

    private static final String FOUNDATION_PROPERTIES_CONFIG = "/usr/local/groundwork/config/foundation.properties";
    private static final String LDAP_MAPPING_PROPERTIES_CONFIG = "/usr/local/groundwork/ldap-mapping-directives.properties";

    /**
     * Valid userPassword schemes according to RFC 2307
     */
    private static final String USERPASSWORD_SCHEME_MD5 = "{md5}";
    private static final String USERPASSWORD_SCHEME_CRYPT = "{crypt}";
    private static final String USERPASSWORD_SCHEME_SHA = "{sha}";

    // ----------------------------------------------------- Instance Variables

    private String _initialContextFactory;
    private String _providerUrl;
    private String _securityAuthentication;
    private String _rolesCtxDN;
    private String _uidAttributeID;
    private String _roleAttributeID;
    private String _roleMatchingMode;
    private String _securityProtocol;
    private String _securityPrincipal;
    private String _securityCredential;
    private String _principalUidAttributeID;
    private String _principalLookupAttributeID;
    private String _userCertificateAtrributeID;
    private String _usersCtxDN;
    private String _credentialQueryString;
    private String _userPropertiesQueryString;
    private String _ldapSearchScope;
    private String _updateableCredentialAttribute;
    private Boolean _useBindCredentials;
    private Boolean _enableStartTls;
    private String _trustStore;
    private String _trustStorePassword;
    private Boolean _isSecurityCredentialDecrypted = false;

    private LDAPAggregator ldapAggregator = null;
    private boolean ldapMappingEnabled;
    private Properties ldapMappingDirectives;
    private LDAPMapper ldapMapper;

    // ----------------------------------------------------- Constructors

    public LDAPIdentityStore() {
    	_userCertificateAtrributeID = "userCertificate";
    	_useBindCredentials = false;
    	_enableStartTls = false;
    }

    // ----------------------------------------------------- IdentityStore Methods

    /**
     * Loads user information and its user attributes from the LDAP server.
     *
     * @param key the userid value to fetch the user in the LDAP server.
     * @return the user instance with the provided userid
     * @throws NoSuchUserException  if the user does not exist
     * @throws SSOIdentityException a fatal exception loading the requested user
     */
    public BaseUser loadUser(UserKey key) throws NoSuchUserException, SSOIdentityException {

        try {
            if (!(key instanceof SimpleUserKey)) {
                throw new SSOIdentityException("Unsupported key type : " + key.getClass().getName());
            }

            String uid = selectUser(((SimpleUserKey) key).getId());

            if (uid == null) {
                throw new NoSuchUserException(key);
            }
            
            BaseUser bu = new BaseUserImpl();
            bu.setName(uid);

            List userProperties = new ArrayList();

            // Optionally find user properties.
            if (getUserPropertiesQueryString() != null) {

                HashMap userPropertiesResultSet = selectUserProperties(((SimpleUserKey) key).getId());

                Iterator i = userPropertiesResultSet.keySet().iterator();
                while (i.hasNext()) {
                    String pName = (String) i.next();
                    String pValue = (String) userPropertiesResultSet.get(pName);

                    SSONameValuePair vp = new SSONameValuePair(pName, pValue);
                    userProperties.add(vp);
                }

            }

            // Store User DN as a SSOUser property.
            String dn = selectUserDN(((SimpleUserKey) key).getId());
            userProperties.add(new SSONameValuePair("josso.user.dn", dn));

            SSONameValuePair[] props = (SSONameValuePair[])
                    userProperties.toArray(new SSONameValuePair[userProperties.size()]);
            bu.setProperties(props);
            return bu;
        } catch (NamingException e) {
            logger.error("NamingException while obtaining user", e);
            throw new SSOIdentityException("Error obtaining user : " + key);
        } catch (IOException e) {
        	logger.error("StartTLS error", e);
            throw new SSOIdentityException("StartTLS error : " + e.getMessage());
		}
    }

    /**
     * Retrieves the roles for the supplied user.
     *
     * @param key the user id of the user for whom role information is to be retrieved.
     * @return the roles associated with the supplied user.
     * @throws SSOIdentityException fatal exception obtaining user roles.
     */
    public BaseRole[] findRolesByUserKey(UserKey key)
            throws SSOIdentityException {

        try {

            if (!(key instanceof SimpleUserKey)) {
                throw new SSOIdentityException("Unsupported key type : " + key.getClass().getName());
            }

            String[] roleNames = selectRolesByUsername(((SimpleUserKey) key).getId());
            List roles = new ArrayList();
            for (int i = 0; i < roleNames.length; i++) {
                String roleName = roleNames[i];
                BaseRole role = new BaseRoleImpl();
                role.setName(roleName);
                roles.add(role);
            }

            return (BaseRole[]) roles.toArray(new BaseRole[roles.size()]);
        } catch (NamingException e) {
            logger.error("NamingException while obtaining roles", e);
            throw new SSOIdentityException("Error obtaining roles for user : " + key);
        } catch (IOException e) {
        	logger.error("StartTLS error", e);
            throw new SSOIdentityException("StartTLS error : " + e.getMessage());
		}
    }
    
    // ----------------------------------------------------- Extended IdentityStore Methods

    public String loadUsernameByRelayCredential ( ChallengeResponseCredential cred ) throws SSOIdentityException {
        try {
            return this.selectUser( cred.getId(), cred.getResponse() );
        } catch(NamingException e) {
            logger.error("NamingException while obtaining user with relay credential", e);
            throw new SSOIdentityException("Error obtaining user with relay credential: ID[" + cred.getId() + "] = RESPONSE[" + cred.getResponse() + "]");
        } catch (IOException e) {
        	logger.error("StartTLS error", e);
            throw new SSOIdentityException("StartTLS error : " + e.getMessage());
		}
    }

    public void updateAccountPassword ( UserKey key, Credential newPassword ) throws SSOIdentityException {
        try {
            if (!(key instanceof SimpleUserKey)) {
                throw new SSOIdentityException("Unsupported key type : " + key.getClass().getName());
            }

            this.updateCredential( this.selectUserDN( ((SimpleUserKey)key).getId() ), ((BaseCredential)newPassword).getValue().toString() );
            
        } catch (NamingException e) {
            logger.error("NamingException while updating password account", e);
            throw new SSOIdentityException("Error updating password account for user : " + key);
        } catch (IOException e) {
        	logger.error("StartTLS error", e);
            throw new SSOIdentityException("StartTLS error : " + e.getMessage());
		}
    }

    // ----------------------------------------------------- CredentialStore Methods

    /**
     * Loads user credential information for the supplied user from the LDAP server.
     *
     * @param key the user id of the user for whom credential information is to be retrieved.
     * @return the credentials associated with the supplied user.
     * @throws SSOIdentityException fatal exception obtaining user credentials
     */
    public Credential[] loadCredentials(CredentialKey key, CredentialProvider cp) throws SSOIdentityException {

        try {

            if (!(key instanceof CredentialKey)) {
                throw new SSOIdentityException("Unsupported key type : " + key.getClass().getName());
            }

            List credentials = new ArrayList();
            HashMap credentialResultSet = selectCredentials(((SimpleUserKey) key).getId(), cp);

            Iterator i = credentialResultSet.keySet().iterator();
            while (i.hasNext()) {
                String cName = (String) i.next();
                List cValues = (List) credentialResultSet.get(cName);
                
                Iterator valIter = cValues.iterator();
                while (valIter.hasNext()) {
                	Credential c = cp.newCredential(cName, valIter.next());
                	credentials.add(c);
                }
            }

            return (Credential[]) credentials.toArray(new Credential[credentialResultSet.size()]);
        } catch (NamingException e) {
            logger.error("NamingException while obtaining Credentials", e);
            throw new SSOIdentityException("Error obtaining credentials for user : " + key);
        } catch (IOException e) {
        	logger.error("StartTLS error", e);
            throw new SSOIdentityException("StartTLS error : " + e.getMessage());
		}

    }

    /**
     * Loads user UID for the given credential key.
     *
     * @param key the key used to load UID from store.
     * @param cp credential provider
     * @throws SSOIdentityException
     */
    public String loadUID(CredentialKey key, CredentialProvider cp) throws SSOIdentityException {
        try {
        	if (key instanceof CertificateUserKey) {
        		return loadUID(((CertificateUserKey)key).getId(), ((CertificateUserKey)key).getCertificate(), cp);
        	} else if (key instanceof SimpleUserKey) {
        		return ((SimpleUserKey)key).getId();
        	} else {
        		throw new SSOIdentityException("Unsupported key type : " + key.getClass().getName());
        	}
        } catch (NamingException e) {
            logger.error("Failed to locate user", e);
            throw new SSOIdentityException("Failed to locate user for certificate : " + ((CertificateUserKey)key).getCertificate().getSubjectX500Principal().getName());
        } catch (IOException e) {
        	logger.error("StartTLS error", e);
            throw new SSOIdentityException("StartTLS error : " + e.getMessage());
		}
	}

    @Override
	public boolean userExists(UserKey key) throws SSOIdentityException {
    	if (getUseBindCredentials()) {
    		String uid = null;
			try {
				uid = selectUser(((SimpleUserKey) key).getId());
			} catch (NamingException e) {
	            logger.error("NamingException while obtaining user", e);
	            throw new SSOIdentityException("Error obtaining user : " + key);
	        } catch (IOException e) {
	        	logger.error("StartTLS error", e);
	            throw new SSOIdentityException("StartTLS error : " + e.getMessage());
			}
            if (uid != null) {
                return true;
            } else {
            	return false;
            }
    	}
		return super.userExists(key);
	}
    
    // ----------------------------------------------------- LDAP Primitives

    /**
     * Obtains the roles for the given user.
     *
     * @param username the user name to fetch user data.
     * @return the list of roles to which the user is associated to.
     * @throws NamingException LDAP error obtaining roles fro the given user
     * @throws IOException 
     */
    protected String[] selectRolesByUsername(String username) throws NamingException, IOException {
        // delegate to LDAP Mapper or Aggregator; note: LDAP Aggregator is
        // a virtual identity store and thus will not throw exceptions
        if (getLdapMapper() != null) {
            return getLdapMapper().selectRolesByUsername(username);
        }
        return getLdapAggregator().selectRolesByUsername(username);
    }

    /**
     * Fetches the supplied user DN.
     *
     * @param uid the user id
     * @return the user DN for the supplied uid
     * @throws NamingException LDAP error obtaining user information.
     * @throws IOException 
     */
    protected String selectUserDN(String uid) throws NamingException, IOException {
        // delegate to LDAP Aggregator; note: LDAP Aggregator is a virtual
        // identity store and thus will not throw exceptions
        return getLdapAggregator().selectUserDN(uid);
    }

    /**
     * Fetches the supplied user.
     *
     * @param uid the user id
     * @return the user id for the supplied uid
     * @throws NamingException LDAP error obtaining user information.
     * @throws IOException
     */
    protected String selectUser(String uid) throws NamingException, IOException {
        // delegate to LDAP Aggregator; note: LDAP Aggregator is a virtual
        // identity store and thus will not throw exceptions
        return getLdapAggregator().selectUser(uid);
    }

    /**
     * Fetches the supplied user.
     *
     * @param attrValue the user id
     * @return the user id for the supplied uid
     * @throws NamingException LDAP error obtaining user information.
     * @throws IOException 
     */
    protected String selectUser(String attrId, String attrValue ) throws NamingException, IOException {
        // delegate to LDAP Aggregator; note: LDAP Aggregator is a virtual
        // identity store and thus will not throw exceptions
        return getLdapAggregator().selectUser(attrId, attrValue);
    }

    /**
     * Fetch the Ldap user attributes to be used as credentials.
     *
     * @param uid the user id (or lookup value) for whom credentials are required
     * @return the hash map containing user credentials as name/value pairs
     * @throws NamingException LDAP error obtaining user credentials.
     * @throws IOException 
     */
    protected HashMap selectCredentials(String uid, CredentialProvider cp) throws NamingException, IOException {
        // delegate to LDAP Aggregator; note: LDAP Aggregator is a virtual
        // identity store and thus will not throw exceptions
        String schemeName = (cp instanceof AuthenticationScheme ? ((AuthenticationScheme) cp).getName() : null);
        return getLdapAggregator().selectCredentials(uid, schemeName);
    }

    /**
     * Get user UID attribute for the given certificate.
     *
     * @param lookupValue value used for credentials lookup
     * @param certificate user certificate
     * @param cp credential provider
     * @return user UID
     * @throws NamingException LDAP error obtaining user UID.
     * @throws IOException 
     */
    protected String loadUID(String lookupValue, X509Certificate certificate, CredentialProvider cp) throws NamingException, IOException {
        // delegate to LDAP Aggregator; note: LDAP Aggregator is a virtual
        // identity store and thus will not throw exceptions
        String schemeName = (cp instanceof AuthenticationScheme ? ((AuthenticationScheme) cp).getName() : null);
        return getLdapAggregator().loadUID(lookupValue, certificate, schemeName);
	}
    
    /**
     * Obtain the properties for the user associated with the given uid using the
     * configured user properties query string.
     *
     * @param uid the user id of the user for whom its user properties are required.
     * @return the hash map containing user properties as name/value pairs.
     * @throws NamingException LDAP error obtaining user properties.
     * @throws IOException 
     */
    protected HashMap selectUserProperties(String uid) throws NamingException, IOException {
        // delegate to LDAP Aggregator; note: LDAP Aggregator is a virtual
        // identity store and thus will not throw exceptions
        return getLdapAggregator().selectUserProperties(uid);
    }

    /**
     * Update credential to the user specified.
     *
     * @param principal principal/user
     * @param credential credential/password
     * @return updated
     * @throws NamingException
     * @throws IOException
     */
    protected boolean updateCredential(String principal, String credential) throws NamingException, IOException {
        // delegate to LDAP Aggregator; note: LDAP Aggregator is a virtual
        // identity store and thus will not throw exceptions
        return getLdapAggregator().updateCredential(principal, credential);
    }

    /**
     * Allocate/get LDAP Aggregator instance.
     *
     * @return LDAP Aggregator
     */
    protected LDAPAggregator getLdapAggregator() {
        // lazily instantiate LDAP Aggregator and Mapper after this is allocated from JOSSO configuration
        if (ldapAggregator != null) {
            return ldapAggregator;
        }
        // load and return LDAP Aggregator and Mapper
        reloadLdapAggregator();
        reloadLdapMappingDirectives();
        // start foundation.properties and ldap-mapping-directives.properties file watcher thread
        Thread fileWatcher = new Thread(new Runnable() {
            public void run() {
                // watch properties last modified time
                File foundationPropertiesFile = new File(FOUNDATION_PROPERTIES_CONFIG);
                long foundationPropertiesLastModified = foundationPropertiesFile.lastModified();
                File ldapMappingPropertiesFile = new File(LDAP_MAPPING_PROPERTIES_CONFIG);
                long ldapMappingPropertiesLastModified = ldapMappingPropertiesFile.lastModified();
                while (true) {
                    try {
                        Thread.sleep(2500);
                    } catch (InterruptedException ie) {
                    }
                    if (foundationPropertiesFile.lastModified() > foundationPropertiesLastModified) {
                        // reload LDAP Aggregator on foundation.properties change and continue
                        reloadLdapAggregator();
                        foundationPropertiesLastModified = foundationPropertiesFile.lastModified();
                    }
                    if (ldapMappingPropertiesFile.lastModified() > ldapMappingPropertiesLastModified) {
                        // reload LDAP Mapper on ldap-mapping-directives.properties change and continue
                        reloadLdapMappingDirectives();
                        ldapMappingPropertiesLastModified = ldapMappingPropertiesFile.lastModified();
                    }
                }

            }
        }, "LDAPIdentityStoreFileWatcher");
        fileWatcher.setDaemon(true);
        fileWatcher.start();
        return ldapAggregator;
    }

    /**
     * Allocate/get LDAP Mapper instance.
     *
     * @return LDAP Mapper
     */
    protected LDAPMapper getLdapMapper() {
        // lazily instantiate LDAP Aggregator and Mapper
        getLdapAggregator();
        // return LDAP Mapper
        return ldapMapper;
    }

    /**
     * Utility to decrypt security credentials if enabled.
     *
     * @param securityCredential encrypted credential/password
     * @return decrypted credential/password
     */
    private String jasyptDecrypt(String securityCredential) {
        String strEncryptionEnabled = WSClientConfiguration.getProperty(WSClientConfiguration.ENCRYPTION_ENABLED);
        boolean encryptionEnabled = (strEncryptionEnabled == null) || Boolean.parseBoolean(strEncryptionEnabled);
        if (encryptionEnabled) {
            String mainKey = FoundationConfiguration.getProperty(FoundationConfiguration.JASYPT_MAINKEY);
            if (mainKey != null) {
                mainKey = WSClientConfiguration.decryptMainKey(mainKey);
                // decode base58/Flickr encoded encrypted string
                byte [] decodedSecurityCredentialBytes = Base58Codec.doDecode(securityCredential);
                // base64 encode encrypted bytes
                String base64SecurityCredential = new String(Base64.encodeBase64(decodedSecurityCredentialBytes));
                // decrypt base64 encoded string
                BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
                textEncryptor.setPassword(mainKey);
                securityCredential = textEncryptor.decrypt(base64SecurityCredential);
            }
        }
        return securityCredential;
    }

    // ----------------------------------------------------- Utils

    /**
     * Gets the username from the received credentials.
     *
     * @param credentials
     */
    protected String getUsername(Set credentials) {
        UsernameCredential c = getUsernameCredential(credentials);
        if (c == null)
            return null;
        return (String) c.getValue();
    }
    
    /**
     * Gets the credential that represents a Username.
     */
    protected UsernameCredential getUsernameCredential(Set credentials) {
    	Iterator i = credentials.iterator();
        while (i.hasNext()) {
            Credential credential = (Credential) i.next();
            if (credential instanceof UsernameCredential) {
                return (UsernameCredential) credential;
            }
        }
        return null;
    }
    
    /**
     * Gets the password from the recevied credentials.
     *
     * @param credentials
     */
    protected String getPassword(Set credentials) {
        PasswordCredential p = getPasswordCredential(credentials);
        if (p == null)
            return null;
        return (String) p.getValue();
    }
    
    /**
     * Gets the credential that represents a password.
     *
     * @param credentials
     */
    protected PasswordCredential getPasswordCredential(Set credentials) {
    	Iterator i = credentials.iterator();
        while (i.hasNext()) {
            Credential credential = (Credential) i.next();
            if (credential instanceof PasswordCredential) {
                return (PasswordCredential) credential;
            }
        }
        return null;
    }

    // ----------------------------------------------------- LDAP Aggregator

    /**
     * Reload LDAP Aggregator if foundation.properties changes. Note: JOSSO
     * configurations are fixed as loaded at startup because this JOSSO bean
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
                config.setSecurityCredential(jasyptDecrypt(config.getSecurityCredential()));
            }
        } else {
            // fallback to JOSSO configuration
            String serverType = (getPrincipalUidAttributeID().equals("sAMAccountName") ?
                    LDAPConfig.AD_SERVER_TYPE : LDAPConfig.OPENLDAP_SERVER_TYPE);
            LDAPConfig jossoLDAPConfig = new LDAPConfig(null, serverType);
            jossoLDAPConfig.setCredentialQueryString(getCredentialQueryString());
            jossoLDAPConfig.setEnableStartTLS(getEnableStartTls());
            jossoLDAPConfig.setInitialContextFactory(getInitialContextFactory());
            jossoLDAPConfig.setLdapSearchScope(getLdapSearchScope());
            jossoLDAPConfig.setPrincipalLookupAttributeID(getPrincipalLookupAttributeID());
            jossoLDAPConfig.setPrincipalUidAttributeID(getPrincipalUidAttributeID());
            jossoLDAPConfig.setProviderURL(getProviderUrl());
            jossoLDAPConfig.setRoleAttributeID(getRoleAttributeID());
            jossoLDAPConfig.setRoleMatchingMode(getRoleMatchingMode());
            jossoLDAPConfig.setRolesCtxDN(getRolesCtxDN());
            jossoLDAPConfig.setSecurityAuthentication(getSecurityAuthentication());
            jossoLDAPConfig.setSecurityCredential(getSecurityCredential());
            jossoLDAPConfig.setSecurityPrincipal(getSecurityPrincipal());
            jossoLDAPConfig.setSecurityProtocol(getSecurityProtocol());
            jossoLDAPConfig.setTrustStore(getTrustStore());
            jossoLDAPConfig.setTrustStorePassword(getTrustStorePassword());
            jossoLDAPConfig.setUidAttributeID(getUidAttributeID());
            jossoLDAPConfig.setUpdateableCredentialAttributeID(getUpdateableCredentialAttribute());
            jossoLDAPConfig.setUserCertificateAttributeID(getUserCertificateAtrributeID());
            jossoLDAPConfig.setUserPropertiesQueryString(getUserPropertiesQueryString());
            jossoLDAPConfig.setUsersCtxDN(getUsersCtxDN());
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
            logger.error("LDAPMapper: cannot load configuration from "+file.getAbsolutePath()+": "+ioe, ioe);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ioe) {
                }
            }
        }
    }

    /**
     * Configuration Properties
     */

    // ----------------------------------------------------- Configuration Properties
    public void setInitialContextFactory(String initialContextFactory) {
        _initialContextFactory = initialContextFactory;
    }

    public String getInitialContextFactory() {
        return _initialContextFactory;
    }

    public void setProviderUrl(String providerUrl) {
        _providerUrl = providerUrl;
    }

    public String getProviderUrl() {
        return _providerUrl;
    }

    public void setSecurityAuthentication(String securityAuthentication) {
        _securityAuthentication = securityAuthentication;
    }

    public String getSecurityAuthentication() {
        return _securityAuthentication;
    }

    public void setSecurityProtocol(String securityProtocol) {
        _securityProtocol = securityProtocol;
    }

    public String getSecurityProtocol() {
        return _securityProtocol;
    }

    public void setSecurityPrincipal(String securityPrincipal) {
        _securityPrincipal = securityPrincipal;
    }

    public String getSecurityPrincipal() {
        return _securityPrincipal;
    }

    public void setSecurityCredential(String securityCredential) {
        _securityCredential = securityCredential;
    }

    public String getSecurityCredential() {
    	if (!_isSecurityCredentialDecrypted) {
            _securityCredential = jasyptDecrypt(_securityCredential);
            _isSecurityCredentialDecrypted = true;
    	}
        return _securityCredential;
    }

    public String getLdapSearchScope() {
        return _ldapSearchScope;
    }

    public void setLdapSearchScope(String ldapSearchScope) {
        _ldapSearchScope = ldapSearchScope;
    }

    public void setUsersCtxDN(String usersCtxDN) {
        _usersCtxDN = usersCtxDN;
    }

    public String getUsersCtxDN() {
        return _usersCtxDN;
    }

    public void setRolesCtxDN(String rolesCtxDN) {
        _rolesCtxDN = rolesCtxDN;
    }

    public String getRolesCtxDN() {
        return _rolesCtxDN;
    }

    public void setPrincipalUidAttributeID(String principalUidAttributeID) {
        _principalUidAttributeID = principalUidAttributeID;
    }

    public String getPrincipalUidAttributeID() {
        return _principalUidAttributeID;
    }

    public void setUidAttributeID(String uidAttributeID) {
        _uidAttributeID = uidAttributeID;
    }

    public void setPrincipalLookupAttributeID(String principalLookupAttributeID) {
    	_principalLookupAttributeID = principalLookupAttributeID;
    }

    public String getPrincipalLookupAttributeID() {
        return _principalLookupAttributeID;
    }
    
    public void setUserCertificateAtrributeID(String userCertificateAtrributeID) {
    	_userCertificateAtrributeID = userCertificateAtrributeID;
    }

    public String getUserCertificateAtrributeID() {
        return _userCertificateAtrributeID;
    }
    
    public String getRoleMatchingMode() {
        return _roleMatchingMode;
    }

    public void setRoleMatchingMode(String roleMatchingMode) {
        this._roleMatchingMode = roleMatchingMode;
    }


    public String getUidAttributeID() {
        return _uidAttributeID;
    }

    public void setRoleAttributeID(String roleAttributeID) {
        _roleAttributeID = roleAttributeID;
    }

    public String getRoleAttributeID() {
        return _roleAttributeID;
    }

    public void setCredentialQueryString(String credentialQueryString) {
        _credentialQueryString = credentialQueryString;
    }

    public String getCredentialQueryString() {
        return _credentialQueryString;
    }

    public void setUserPropertiesQueryString(String userPropertiesQueryString) {
        _userPropertiesQueryString = userPropertiesQueryString;
    }

    public String getUserPropertiesQueryString() {
        return _userPropertiesQueryString;
    }

    public String getUpdateableCredentialAttribute () {
        return _updateableCredentialAttribute;
    }

    public void setUpdateableCredentialAttribute ( String updateableCredentialAttribute ) {
        this._updateableCredentialAttribute = updateableCredentialAttribute;
    }

	public Boolean getUseBindCredentials() {
		return _useBindCredentials;
	}

	public void setUseBindCredentials(Boolean useBindCredentials) {
		_useBindCredentials = useBindCredentials;
	}

	public Boolean getEnableStartTls() {
		return _enableStartTls;
	}

	public void setEnableStartTls(Boolean enableStartTls) {
		_enableStartTls = enableStartTls;
	}

	public String getTrustStore() {
		return _trustStore;
	}

	public void setTrustStore(String trustStore) {
		_trustStore = trustStore;
	}

	public String getTrustStorePassword() {
		return _trustStorePassword;
	}

	public void setTrustStorePassword(String trustStorePassword) {
		_trustStorePassword = trustStorePassword;
	}
}
