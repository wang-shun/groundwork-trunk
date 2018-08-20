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

package org.josso.gateway.identity.service.store.ldap.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.josso.gateway.SSONameValuePair;
import org.josso.gateway.identity.service.BaseRole;
import org.josso.gateway.identity.service.BaseUser;
import org.josso.gateway.identity.service.store.SimpleUserKey;
import org.josso.gateway.identity.service.store.ldap.LDAPIdentityStore;
import org.josso.gateway.identity.service.store.ldap.WSClientConfiguration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * User: <a href=mailto:ajadzinsky@atricor.org>ajadzinsky</a>
 * Date: Dec 4, 2008
 * Time: 12:55:48 PM
 */
public class LDAPIdentityStoreTest {
    private static final Log logger = LogFactory.getLog( LDAPIdentityStoreTest.class );

    private static LDAPIdentityStore ldap;

    @BeforeClass
    public static void prepareTest () throws Exception {
        logger.debug( "preparing test..." );

        prepareIdentityStore();
    }

    @AfterClass
    public static void tearDownTest () throws Exception {
        logger.debug( "tearing down test..." );
    }

    @Test
    public void testLoadUser () throws Exception {
        final SimpleUserKey uk = new SimpleUserKey( "gw_admin" );
        BaseUser bu = ldap.loadUser( uk );
        assert bu != null : "can not load user " + uk.getId();
        assert bu.getName().equals( uk.getId() ) : "expected user name \"" + uk.getId() + "\" got \"" + bu.getName() + "\"";
        logger.debug( "trying to find description..." );
        for( SSONameValuePair nvp : bu.getProperties()){
            if( nvp.getName().equals( "description" ) )
                assert nvp.getValue().equals( "a d. min" );
        }
    }

    @Test
    public void testRolesByUser() throws Exception {
        final SimpleUserKey uk = new SimpleUserKey( "gw_admin" );
        BaseRole[] brs = ldap.findRolesByUserKey( uk );
        assert brs != null : "expected roles got null";
        assert brs.length > 0 : "expected roles got " + brs.length;
        List<String> roleNames = new ArrayList<String>();
        for (BaseRole br : brs) {
            roleNames.add(br.getName());
        }
        assert roleNames.contains("GWAdmin") : "expected GWAdmin not found";
    }

    private static void prepareIdentityStore() throws Exception {
        String ldapIdentityStoreConfig;
        if (Boolean.parseBoolean(WSClientConfiguration.getProperty(WSClientConfiguration.ENCRYPTION_ENABLED))) {
            ldapIdentityStoreConfig = "META-INF/spring/encrypted-ldap-identity-store.xml";
        } else {
            ldapIdentityStoreConfig = "META-INF/spring/ldap-identity-store.xml";
        }
        ApplicationContext factory = new ClassPathXmlApplicationContext(ldapIdentityStoreConfig);
        ldap = (LDAPIdentityStore)factory.getBean( "josso-ldap-store" );
        assert ldap != null : "could not create LDAPIdentityStore";
    }
}
