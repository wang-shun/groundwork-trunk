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
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * LDAPMapperTest
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class LDAPMapperTest {

    private static final Log logger = LogFactory.getLog(LDAPMapperTest.class);

    @BeforeClass
    public static void setupLogger() {
        // setup logging level
        Logger.getRootLogger().setLevel(Level.DEBUG);
    }

    private Map<String,LDAPConfig> configs = new LinkedHashMap<String,LDAPConfig>();
    private LDAPAggregator ldapAggregator;
    private LDAPMapper ldapMapper;

    @Before
    public void setup() {
        // setup LDAP
        LDAPConfig local = new LDAPConfig("local", LDAPConfig.OPENLDAP_SERVER_TYPE);
        local.setProviderURL("ldap://10.0.0.31:10389");
        local.setSecurityPrincipal("cn=bind,ou=users,dc=corp,dc=localdomain");
        local.setSecurityCredential("bind");
        local.setUsersCtxDN("ou=users,dc=corp,dc=localdomain|ou=more\\20users,dc=corp,dc=localdomain");
        local.setRolesCtxDN("ou=groups,dc=corp,dc=localdomain | ou=gw\\20roles,dc=corp,dc=localdomain");
        configs.put("local", local);
        ldapAggregator = new LDAPAggregator(configs, false);
        Properties ldapMappingDirectives = new Properties();
        ldapMappingDirectives.setProperty("GWAdmin", LDAPMapper.GROUNDWORK_7_ADMIN_ROLE);
        ldapMappingDirectives.setProperty("GWUser", "MappedGWUser");
        ldapMappingDirectives.setProperty("MappedGWUser", "");
        ldapMapper = new LDAPMapper(ldapMappingDirectives, ldapAggregator);
    }

    @After
    public void teardown() {
        // shutdown setups
        ldapAggregator.shutdown();
    }

    @Test
    public void testSetupAndTeardown() {
    }

    @Test
    public void testLdapAggregator() {
        // assert LDAP setup
        assertEquals("cn=admin,ou=users,dc=corp,dc=localdomain", ldapAggregator.selectUserDN("local\\admin"));
        assertTrue(ldapAggregator.validateSecurityCredentials("local", "cn=admin,ou=users,dc=corp,dc=localdomain", "admin"));
        assertEquals("cn=Admin,ou=groups,dc=corp,dc=localdomain", ldapAggregator.selectRoleDN("local\\Admin"));
        Set<String> adminUserRoles = new HashSet<String>(Arrays.asList(ldapAggregator.selectRolesByUsername("local\\admin")));
        assertEquals(1, adminUserRoles.size());
        assertTrue(adminUserRoles.contains("Admin"));
        assertEquals("cn=GWAdmin,ou=gw\\20roles,dc=corp,dc=localdomain", ldapAggregator.selectRoleDN("local\\GWAdmin"));
        Set<String> adminRoleRoles = new HashSet<String>(Arrays.asList(ldapAggregator.selectRolesByRoleName("local\\Admin")));
        assertEquals(1, adminRoleRoles.size());
        assertTrue(adminRoleRoles.contains("GWAdmin"));
        Set<String> gwAdminRoleRoles = new HashSet<String>(Arrays.asList(ldapAggregator.selectRolesByRoleName("local\\GWAdmin")));
        assertTrue(gwAdminRoleRoles.isEmpty());
        assertEquals("cn=user,ou=users,dc=corp,dc=localdomain", ldapAggregator.selectUserDN("local\\user"));
        assertTrue(ldapAggregator.validateSecurityCredentials("local", "cn=user,ou=users,dc=corp,dc=localdomain", "user"));
        assertEquals("cn=User,ou=groups,dc=corp,dc=localdomain", ldapAggregator.selectRoleDN("local\\User"));
        Set<String> userUserRoles = new HashSet<String>(Arrays.asList(ldapAggregator.selectRolesByUsername("local\\user")));
        assertEquals(1, userUserRoles.size());
        assertTrue(userUserRoles.contains("User"));
        assertEquals("cn=GWUser,ou=gw\\20roles,dc=corp,dc=localdomain", ldapAggregator.selectRoleDN("local\\GWUser"));
        Set<String> userRoleRoles = new HashSet<String>(Arrays.asList(ldapAggregator.selectRolesByRoleName("local\\User")));
        assertEquals(1, userRoleRoles.size());
        assertTrue(userRoleRoles.contains("GWUser"));
        Set<String> gwUserRoleRoles = new HashSet<String>(Arrays.asList(ldapAggregator.selectRolesByRoleName("local\\GWUser")));
        assertTrue(gwUserRoleRoles.isEmpty());
        assertEquals("cn=user2,ou=more\\20users,dc=corp,dc=localdomain", ldapAggregator.selectUserDN("local\\user2"));
        assertTrue(ldapAggregator.validateSecurityCredentials("local", "cn=user2,ou=more\\20users,dc=corp,dc=localdomain", "user2"));
        Set<String> user2UserRoles = new HashSet<String>(Arrays.asList(ldapAggregator.selectRolesByUsername("local\\user2")));
        assertEquals(1, user2UserRoles.size());
        assertTrue(user2UserRoles.contains("GWUser"));
    }

    @Test
    public void testLDAPMapper() {
        Set<String> adminUserRoles = new HashSet<String>(Arrays.asList(ldapMapper.selectRolesByUsername("local\\admin")));
        assertEquals(1, adminUserRoles.size());
        assertTrue(adminUserRoles.contains("GWAdmin"));
        Set<String> userUserRoles = new HashSet<String>(Arrays.asList(ldapMapper.selectRolesByUsername("local\\user")));
        assertEquals(1, userUserRoles.size());
        assertTrue(userUserRoles.contains("MappedGWUser"));
        Set<String> user2UserRoles = new HashSet<String>(Arrays.asList(ldapMapper.selectRolesByUsername("local\\user2")));
        assertEquals(1, user2UserRoles.size());
        assertTrue(user2UserRoles.contains("MappedGWUser"));
    }
}
