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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * LDAPAggregatorTest
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class LDAPAggregatorTest {

    @BeforeClass
    public static void setupLogger() {
        // setup logging level
        Logger.getRootLogger().setLevel(Level.DEBUG);
    }

    private Map<String,LDAPConfig> configs = new LinkedHashMap<String,LDAPConfig>();
    private LDAPAggregator ldap;
    private Map<String,LDAPConfig> gwAdminConfigs = new LinkedHashMap<String,LDAPConfig>();
    private LDAPAggregator gwAdminLdap;
    private Map<String,LDAPConfig> ldapsConfigs = new LinkedHashMap<String,LDAPConfig>();
    private LDAPAggregator ldaps;

    @Before
    public void setup() {
        // setup LDAP
        LDAPConfig demo = new LDAPConfig("demo", LDAPConfig.AD_SERVER_TYPE);
        demo.setProviderURL("ldap://172.28.113.33:389");
        demo.setSecurityPrincipal("cn=d_ldapauth,cn=Users,dc=demo,dc=com");
        demo.setSecurityCredential("d_ldapauth");
        configs.put("demo", demo);
        LDAPConfig techlab = new LDAPConfig("techlab", LDAPConfig.AD_SERVER_TYPE);
        techlab.setProviderURL("ldap://172.28.111.27:389");
        techlab.setSecurityPrincipal("cn=gw-bind,cn=Users,dc=gwostechlab,dc=com");
        techlab.setSecurityCredential("Fold+tent+leave!");
        techlab.setUsersCtxDN("CN=Users,dc=gwostechlab,dc=com");
        techlab.setRolesCtxDN("OU=GWRoles,DC=gwostechlab,DC=com");
        configs.put("techlab", techlab);
        ldap = new LDAPAggregator(configs, false);

        // setup gw_admin LDAP on demo
        LDAPConfig gwAdminDemo = new LDAPConfig("demo", LDAPConfig.AD_SERVER_TYPE);
        gwAdminDemo.setProviderURL("ldap://172.28.113.33:389");
        gwAdminDemo.setSecurityPrincipal("cn=gw_admin,cn=Users,dc=demo,dc=com");
        gwAdminDemo.setSecurityCredential("gw_admin");
        gwAdminConfigs.put("demo", gwAdminDemo);
        gwAdminLdap = new LDAPAggregator(gwAdminConfigs, false);

        // setup LDAPS
        LDAPConfig ldapsTechlab = new LDAPConfig("ldapstechlab", LDAPConfig.AD_SERVER_TYPE);
        ldapsTechlab.setProviderURL("ldaps://172.28.111.27:636");
        ldapsTechlab.setSecurityPrincipal("cn=gw-bind,cn=Users,dc=gwostechlab,dc=com");
        ldapsTechlab.setSecurityCredential("Fold+tent+leave!");
        ldapsTechlab.setUsersCtxDN("CN=Users,dc=gwostechlab,dc=com");
        ldapsTechlab.setRolesCtxDN("OU=GWRoles,DC=gwostechlab,DC=com");
        File trustStoreFile = new File(System.getProperties().getProperty("testResources")+"/atlas.gwostechlab.com.ks");
        ldapsTechlab.setTrustStore(trustStoreFile.getAbsolutePath());
        ldapsTechlab.setTrustStorePassword("changeit");
        ldapsConfigs.put("ldapstechlab", ldapsTechlab);
        ldaps = new LDAPAggregator(ldapsConfigs, false);
    }

    @After
    public void teardown() {
        // shutdown setups
        ldap.shutdown();
        gwAdminLdap.shutdown();
        ldaps.shutdown();
    }

    @Test
    public void testSetupAndTeardown() {
    }

    @Test
    public void testSecurityCredentialsValidation() {
        // assert successful validation
        assertTrue(ldap.validateSecurityCredentials("demo", "cn=gw_admin,cn=Users,dc=demo,dc=com", "gw_admin"));
        assertTrue(ldap.validateSecurityCredentials("techlab", "cn=gw-admin,cn=Users,dc=gwostechlab,dc=com", "Fold+tent+leave!"));
        assertTrue(ldaps.validateSecurityCredentials("ldapstechlab", "cn=gw-admin,cn=Users,dc=gwostechlab,dc=com", "Fold+tent+leave!"));
        assertTrue(ldap.validateSecurityCredentials("demo", "cn=A\\,AA,cn=Users,dc=demo,dc=com", "AAA"));
        // assert failed validation: wrong password
        assertFalse(ldap.validateSecurityCredentials("techlab", "cn=gw-admin,cn=Users,dc=gwostechlab,dc=com", "not-a-password"));
        // assert failed authentication: not a user
        assertFalse(ldap.validateSecurityCredentials("demo", "cn=nobody,cn=Users,dc=demo,dc=com", "not-a-password"));
        // assert failed validation: not a domain
        assertFalse(ldap.validateSecurityCredentials("linux", "cn=gw_admin,cn=Users,dc=linux,dc=org", "not-a-password"));
    }

    @Test
    public void testAuthentication() {
        // assert successful authentication
        assertTrue(ldap.authenticate("cn=gw_admin,cn=Users,dc=demo,dc=com", "gw_admin", true));
        assertTrue(ldap.authenticate("cn=gw-admin,cn=Users,dc=gwostechlab,dc=com", "Fold+tent+leave!", true));
        assertTrue(ldap.authenticate("cn=A\\,AA,cn=Users,dc=demo,dc=com", "AAA", true));
        // assert cache operation
        assertEquals(0, ldap.getAPICacheStats().get("hits").longValue());
        assertEquals(3, ldap.getAPICacheStats().get("misses").longValue());
        assertEquals(3, ldap.getAPICacheStats().get("puts").longValue());
        assertEquals(3, ldap.getAPICacheStats().get("size").longValue());
        assertTrue(ldap.authenticate("cn=gw-admin,cn=Users,dc=gwostechlab,dc=com", "Fold+tent+leave!", true));
        assertEquals(1, ldap.getAPICacheStats().get("hits").longValue());
        assertEquals(3, ldap.getAPICacheStats().get("misses").longValue());
        assertEquals(3, ldap.getAPICacheStats().get("puts").longValue());
        assertEquals(3, ldap.getAPICacheStats().get("size").longValue());
        // assert failed authentication: wrong password
        assertFalse(ldap.authenticate("cn=gw-admin,cn=Users,dc=gwostechlab,dc=com", "not-a-password", true));
        // assert failed authentication: not a user
        assertFalse(ldap.authenticate("cn=nobody,cn=Users,dc=demo,dc=com", "not-a-password", false));
        // assert failed authentication: not a domain
        assertFalse(ldap.authenticate("cn=gw_admin,cn=Users,dc=linux,dc=org", "not-a-password", false));
    }

    @Test
    public void testBind() {
        // assert successful bind
        assertTrue(ldap.bind("admin", "admin"));
        assertTrue(ldap.bind("gw_admin", "gw_admin"));
        assertTrue(ldap.bind("demo/gw_admin", "gw_admin"));
        assertTrue(ldap.bind("techlab\\gw-admin", "Fold+tent+leave!"));
        assertTrue(ldap.bind("AAA", "AAA"));
        // assert cache operation
        assertEquals(0, ldap.getAPICacheStats().get("hits").longValue());
        assertEquals(5, ldap.getAPICacheStats().get("misses").longValue());
        assertEquals(5, ldap.getAPICacheStats().get("puts").longValue());
        assertEquals(5, ldap.getAPICacheStats().get("size").longValue());
        assertTrue(ldap.bind("admin", "admin"));
        assertEquals(1, ldap.getAPICacheStats().get("hits").longValue());
        assertEquals(5, ldap.getAPICacheStats().get("misses").longValue());
        assertEquals(5, ldap.getAPICacheStats().get("puts").longValue());
        assertEquals(5, ldap.getAPICacheStats().get("size").longValue());
        // assert failed bind: wrong password
        assertFalse(ldap.bind("admin", "password"));
        assertFalse(ldap.bind("demo/gw_admin", "password"));
        assertFalse(ldap.bind("techlab\\gw-admin", "password"));
        // assert failed bind: not a user
        assertFalse(ldap.bind("nobody", "password"));
        assertFalse(ldap.bind("demo/nobody", "password"));
        assertFalse(ldap.bind("techlab\\nobody", "password"));
        // assert failed bind: not a domain
        assertFalse(ldap.bind("linux/admin", "admin"));
    }

    @Test
    public void testSelectUserDN() {
        // assert successful select
        assertEquals("CN=Admin,CN=Users,dc=demo,dc=com", ldap.selectUserDN("admin"));
        assertEquals("CN=gw_admin,CN=Users,dc=demo,dc=com", ldap.selectUserDN("gw_admin"));
        assertEquals("CN=gw_admin,CN=Users,dc=demo,dc=com", ldap.selectUserDN("demo/gw_admin"));
        assertEquals("CN=gw-admin,CN=Users,dc=gwostechlab,dc=com", ldap.selectUserDN("techlab\\gw-admin"));
        assertEquals("CN=A\\,AA,CN=Users,dc=demo,dc=com", ldap.selectUserDN("AAA"));
        // assert cache operation
        assertEquals(0, ldap.getAPICacheStats().get("hits").longValue());
        assertEquals(5, ldap.getAPICacheStats().get("misses").longValue());
        assertEquals(5, ldap.getAPICacheStats().get("puts").longValue());
        assertEquals(5, ldap.getAPICacheStats().get("size").longValue());
        assertEquals("CN=Admin,CN=Users,dc=demo,dc=com", ldap.selectUserDN("admin"));
        assertEquals(1, ldap.getAPICacheStats().get("hits").longValue());
        assertEquals(5, ldap.getAPICacheStats().get("misses").longValue());
        assertEquals(5, ldap.getAPICacheStats().get("puts").longValue());
        assertEquals(5, ldap.getAPICacheStats().get("size").longValue());
        // assert failed select: not a user
        assertNull(ldap.selectUserDN("nobody"));
        assertNull(ldap.selectUserDN("demo/nobody"));
        assertNull(ldap.selectUserDN("techlab\\nobody"));
        // assert failed select: not a domain
        assertNull(ldap.selectUserDN("linux/admin"));
    }

    @Test
    public void testSelectUserProperties() {
        // assert successful select
        assertEquals("admin@demo.com", ldap.selectUserProperties("admin").get("mail"));
        assertEquals("gw_admin@demo.com", ldap.selectUserProperties("gw_admin").get("mail"));
        assertEquals("gw_admin@demo.com", ldap.selectUserProperties("demo/gw_admin").get("mail"));
        assertEquals("gw-admin@gwostechlab.com", ldap.selectUserProperties("techlab\\gw-admin").get("mail"));
        assertEquals("AAA@demo.com", ldap.selectUserProperties("AAA").get("mail"));
        // assert cache operation
        assertEquals(0, ldap.getAPICacheStats().get("hits").longValue());
        assertEquals(5, ldap.getAPICacheStats().get("misses").longValue());
        assertEquals(5, ldap.getAPICacheStats().get("puts").longValue());
        assertEquals(5, ldap.getAPICacheStats().get("size").longValue());
        assertEquals("admin@demo.com", ldap.selectUserProperties("admin").get("mail"));
        assertEquals(1, ldap.getAPICacheStats().get("hits").longValue());
        assertEquals(5, ldap.getAPICacheStats().get("misses").longValue());
        assertEquals(5, ldap.getAPICacheStats().get("puts").longValue());
        assertEquals(5, ldap.getAPICacheStats().get("size").longValue());
        // assert failed select: not a user
        assertTrue(ldap.selectUserProperties("nobody").isEmpty());
        assertTrue(ldap.selectUserProperties("demo/nobody").isEmpty());
        assertTrue(ldap.selectUserProperties("techlab\\nobody").isEmpty());
        // assert failed select: not a domain
        assertTrue(ldap.selectUserProperties("linux/admin").isEmpty());
    }

    @Test
    public void testSelectRolesByUsername() {
        // assert successful select
        assertTrue(Arrays.asList(ldap.selectRolesByUsername("admin")).contains("GWAdmin"));
        assertTrue(Arrays.asList(ldap.selectRolesByUsername("gw_admin")).contains("GWAdmin"));
        assertTrue(Arrays.asList(ldap.selectRolesByUsername("demo/gw_admin")).contains("GWAdmin"));
        assertTrue(Arrays.asList(ldap.selectRolesByUsername("techlab\\gw-admin")).contains("GWAdmin"));
        assertTrue(Arrays.asList(ldap.selectRolesByUsername("AAA")).contains("GWUser"));
        // assert cache operation
        assertEquals(0, ldap.getAPICacheStats().get("hits").longValue());
        assertEquals(5, ldap.getAPICacheStats().get("misses").longValue());
        assertEquals(5, ldap.getAPICacheStats().get("puts").longValue());
        assertEquals(5, ldap.getAPICacheStats().get("size").longValue());
        assertTrue(Arrays.asList(ldap.selectRolesByUsername("admin")).contains("GWAdmin"));
        assertEquals(1, ldap.getAPICacheStats().get("hits").longValue());
        assertEquals(5, ldap.getAPICacheStats().get("misses").longValue());
        assertEquals(5, ldap.getAPICacheStats().get("puts").longValue());
        assertEquals(5, ldap.getAPICacheStats().get("size").longValue());
        // assert failed select: not a user
        assertEquals(0, ldap.selectRolesByUsername("nobody").length);
        assertEquals(0, ldap.selectRolesByUsername("demo/nobody").length);
        assertEquals(0, ldap.selectRolesByUsername("techlab\\nobody").length);
        // assert failed select: not a domain
        assertEquals(0, ldap.selectRolesByUsername("linux/admin").length);
    }

    @Test
    public void testSelectUser() {
        // assert successful select
        assertEquals("admin", ldap.selectUser("admin"));
        assertEquals("gw_admin", ldap.selectUser("gw_admin"));
        assertEquals("demo\\gw_admin", ldap.selectUser("demo/gw_admin"));
        assertEquals("techlab\\gw-admin", ldap.selectUser("techlab\\gw-admin"));
        assertEquals("AAA", ldap.selectUser("AAA"));
        // assert cache operation
        assertEquals(0, ldap.getAPICacheStats().get("hits").longValue());
        assertEquals(5, ldap.getAPICacheStats().get("misses").longValue());
        assertEquals(5, ldap.getAPICacheStats().get("puts").longValue());
        assertEquals(5, ldap.getAPICacheStats().get("size").longValue());
        assertEquals("admin", ldap.selectUser("admin"));
        assertEquals(1, ldap.getAPICacheStats().get("hits").longValue());
        assertEquals(5, ldap.getAPICacheStats().get("misses").longValue());
        assertEquals(5, ldap.getAPICacheStats().get("puts").longValue());
        assertEquals(5, ldap.getAPICacheStats().get("size").longValue());
        // assert failed select: not a user
        assertNull(ldap.selectUser("nobody"));
        assertNull(ldap.selectUser("demo/nobody"));
        assertNull(ldap.selectUser("techlab\\nobody"));
        // assert failed select: not a domain
        assertNull(ldap.selectUser("linux/admin"));
        // assert successful select by attribute
        assertEquals("demo\\gw_admin", ldap.selectUser("sAMAccountName", "demo/gw_admin"));
        assertEquals("techlab\\gw-admin", ldap.selectUser("sAMAccountName", "techlab\\gw-admin"));
        // assert failed select by attribute; attribute does not match
        assertNull(ldap.selectUser("notAnAttribute", "demo/gw_admin"));
    }

    @Test
    public void testSelectCredentials() {
        // assert successful select
        assertTrue(ldap.selectCredentials("admin", LDAPAggregator.STRONG_AUTHENTICATION_SCHEME).get("username").contains("admin"));
        assertTrue(ldap.selectCredentials("gw_admin", LDAPAggregator.STRONG_AUTHENTICATION_SCHEME).get("username").contains("gw_admin"));
        assertTrue(ldap.selectCredentials("demo/gw_admin", null).get("username").contains("gw_admin"));
        assertTrue(ldap.selectCredentials("techlab\\gw-admin", null).get("username").contains("gw-admin"));
        assertTrue(ldap.selectCredentials("AAA", LDAPAggregator.STRONG_AUTHENTICATION_SCHEME).get("username").contains("AAA"));
        // assert cache operation
        assertEquals(0, ldap.getAPICacheStats().get("hits").longValue());
        assertEquals(5, ldap.getAPICacheStats().get("misses").longValue());
        assertEquals(5, ldap.getAPICacheStats().get("puts").longValue());
        assertEquals(5, ldap.getAPICacheStats().get("size").longValue());
        assertTrue(ldap.selectCredentials("admin", LDAPAggregator.STRONG_AUTHENTICATION_SCHEME).get("username").contains("admin"));
        assertEquals(1, ldap.getAPICacheStats().get("hits").longValue());
        assertEquals(5, ldap.getAPICacheStats().get("misses").longValue());
        assertEquals(5, ldap.getAPICacheStats().get("puts").longValue());
        assertEquals(5, ldap.getAPICacheStats().get("size").longValue());
        // assert failed select: not a user
        assertTrue(ldap.selectCredentials("nobody", null).isEmpty());
        assertTrue(ldap.selectCredentials("demo/nobody", null).isEmpty());
        assertTrue(ldap.selectCredentials("techlab\\nobody", null).isEmpty());
        // assert failed select: not a domain
        assertTrue(ldap.selectCredentials("linux/admin", null).isEmpty());
    }

    @Test
    public void testLoadUID() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");

        KeyPair pair = keyGen.generateKeyPair();
        keyGen.initialize(1024, SecureRandom.getInstance("SHA1PRNG", "SUN"));
        X509V3CertificateGenerator v3CertGen = new X509V3CertificateGenerator();
        v3CertGen.setSerialNumber(BigInteger.valueOf(Math.abs(new SecureRandom().nextInt())));
        v3CertGen.setIssuerDN(new X509Principal("CN=test,O=test,L=nowhere,C=USA"));
        v3CertGen.setNotBefore(new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 30));
        v3CertGen.setNotAfter(new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 365*10)));
        v3CertGen.setSubjectDN(new X509Principal("CN=test, OU=None, O=None L=None, C=None"));
        v3CertGen.setPublicKey(pair.getPublic());
        v3CertGen.setSignatureAlgorithm("SHA256WithRSA");

        //X509Certificate testCertificate = v3CertGen.generate(KPair.getPrivate());
        X509Certificate testCertificate = v3CertGen.generate(pair.getPrivate());

        byte[] derEncodedTestCertificate = testCertificate.getEncoded();
        // add test certificate to gw_admin
        assertTrue(gwAdminLdap.setAttribute("demo/gw_admin", LDAPAggregator.AttributeOperation.ADD, "userCertificate", derEncodedTestCertificate));
        try {
            // assert successful load
            assertEquals("gw_admin", ldap.loadUID("gw_admin", testCertificate, LDAPAggregator.STRONG_AUTHENTICATION_SCHEME));
            assertEquals("gw_admin", ldap.loadUID("gw_admin", testCertificate, null));
            // assert cache operation
            assertEquals(0, ldap.getAPICacheStats().get("hits").longValue());
            assertEquals(2, ldap.getAPICacheStats().get("misses").longValue());
            assertEquals(2, ldap.getAPICacheStats().get("puts").longValue());
            assertEquals(2, ldap.getAPICacheStats().get("size").longValue());
            assertEquals("gw_admin", ldap.loadUID("gw_admin", testCertificate, LDAPAggregator.STRONG_AUTHENTICATION_SCHEME));
            assertEquals(1, ldap.getAPICacheStats().get("hits").longValue());
            assertEquals(2, ldap.getAPICacheStats().get("misses").longValue());
            assertEquals(2, ldap.getAPICacheStats().get("puts").longValue());
            assertEquals(2, ldap.getAPICacheStats().get("size").longValue());
            // assert failed load: mismatched certificate
            assertNull(ldap.loadUID("admin", testCertificate, null));
            // assert failed select: not a user
            assertNull(ldap.loadUID("nobody", testCertificate, null));
            assertNull(ldap.loadUID("demo/nobody", testCertificate, null));
            assertNull(ldap.loadUID("techlab\\nobody", testCertificate, null));
            // assert failed select: not a domain
            assertNull(ldap.loadUID("linux/admin", testCertificate, null));
        } finally {
            // remove test certificate to gw_admin
            assertTrue(gwAdminLdap.setAttribute("demo/gw_admin", LDAPAggregator.AttributeOperation.REMOVE, "userCertificate", null));
        }
    }

    @Test
    public void testUpdateCredential() {
        // this cannot work without an SSL protocol connection at least for AD and OpenLDAP
        // see: https://msdn.microsoft.com/en-us/library/cc223249.aspx
        // according to article for AD, will require SSL, fUserPwdSupport, and User-Change-Password/User-Force-Change-Password ACRs
        assertFalse(ldap.updateCredential("demo/gw_admin", "gw_admin"));
    }

    @Test
    public void testAPICache() {
        // setup LDAP with test TTL and reporting intervals
        LDAPAggregator testAPICacheLDAP = new LDAPAggregator(configs, false, 50L, 10L, 25L);
        // assert successful authentication
        assertTrue(testAPICacheLDAP.bind("admin", "admin"));
        // assert cache operation
        assertEquals(0, testAPICacheLDAP.getAPICacheStats().get("hits").longValue());
        assertEquals(1, testAPICacheLDAP.getAPICacheStats().get("misses").longValue());
        assertEquals(1, testAPICacheLDAP.getAPICacheStats().get("puts").longValue());
        assertEquals(0, testAPICacheLDAP.getAPICacheStats().get("reaps").longValue());
        assertEquals(1, testAPICacheLDAP.getAPICacheStats().get("size").longValue());
        // assert successful authentication
        assertTrue(testAPICacheLDAP.bind("admin", "admin"));
        // assert cache operation
        assertEquals(1, testAPICacheLDAP.getAPICacheStats().get("hits").longValue());
        assertEquals(1, testAPICacheLDAP.getAPICacheStats().get("misses").longValue());
        assertEquals(1, testAPICacheLDAP.getAPICacheStats().get("puts").longValue());
        assertEquals(0, testAPICacheLDAP.getAPICacheStats().get("reaps").longValue());
        assertEquals(1, testAPICacheLDAP.getAPICacheStats().get("size").longValue());
        // wait for cache reaper
        try {
            Thread.sleep(60L);
        } catch (InterruptedException ie) {
        }
        // assert cache operation
        assertEquals(1, testAPICacheLDAP.getAPICacheStats().get("hits").longValue());
        assertEquals(1, testAPICacheLDAP.getAPICacheStats().get("misses").longValue());
        assertEquals(1, testAPICacheLDAP.getAPICacheStats().get("puts").longValue());
        assertEquals(1, testAPICacheLDAP.getAPICacheStats().get("reaps").longValue());
        assertEquals(0, testAPICacheLDAP.getAPICacheStats().get("size").longValue());
        // wait for cache reporting
        try {
            Thread.sleep(30L);
        } catch (InterruptedException ie) {
        }
    }

    @Test
    public void testDomainPrefixRequired() {
        // setup LDAP with test TTL and reporting intervals
        LDAPAggregator testDomainReqdLDAP = new LDAPAggregator(configs, true);
        // assert successful authentication
        assertTrue(testDomainReqdLDAP.bind("techlab/gw-admin", "Fold+tent+leave!"));
        assertTrue(testDomainReqdLDAP.bind("demo/admin", "admin"));
        // assert failed: missing domain prefix
        assertFalse(testDomainReqdLDAP.bind("gw-admin", "Fold+tent+leave!"));
        assertFalse(testDomainReqdLDAP.bind("admin", "admin"));
    }
}
