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

package org.groundwork.rs.client;

import org.junit.Ignore;
import org.junit.Test;

/**
 * LDAPAuthClientTest
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class LDAPAuthClientTest extends AbstractClientTest {

    @Ignore
    @Test
    public void testValidateCredentials() throws Exception {
        // allocate LDAP authentication client
        if (serverDown) return;
        LDAPAuthClient client = new LDAPAuthClient(getDeploymentURL());

        // assert valid credentials
        assert client.validateCredentials("demo", "cn=gw_admin,cn=Users,dc=demo,dc=com", "gw_admin");

        // assert invalid credentials
        assert !client.validateCredentials("demo", "cn=gw_admin,cn=Users,dc=demo,dc=com", "not-a-password");
        assert !client.validateCredentials("demo", "cn=nobody,cn=Users,dc=demo,dc=com", "not-a-password");
        assert !client.validateCredentials("demo", "cn=gw_admin,cn=Users,dc=linux,dc=org", "not-a-password");
    }
}
