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
import org.junit.Test;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * LDAPAggregatorTest
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class LDAPConfigTest {

    private static final Log logger = LogFactory.getLog(LDAPConfigTest.class);

    @Test
    public void testLoadConfigFromFile() {
        // get test configuration properties file
        File file = new File(System.getProperties().getProperty("testResources")+"/LDAPConfigTest.properties");
        assert file.isFile();
        // load configuration from test configuration properties
        Properties properties = new Properties();
        Map<String,LDAPConfig> configs = LDAPConfig.loadConfigsFromFile(file, properties);
        // assert configuration loaded
        assert Boolean.parseBoolean(properties.getProperty(LDAPAggregator.DOMAIN_PREFIX_REQUIRED_PROP, LDAPAggregator.DOMAIN_PREFIX_REQUIRED_DEFAULT));
        assert configs.size() == 2;
        Iterator<String> ordererdKetsIter = configs.keySet().iterator();
        assert ordererdKetsIter.next() == null;
        assert ordererdKetsIter.next().equals("demo");
        assert configs.get(null).getProviderURL().equals("ldap://172.28.111.27:389");
        assert configs.get(null).getSecurityPrincipal().equals("cn=gw-bind,cn=Users,dc=gwostechlab,dc=com");
        assert configs.get("demo").getProviderURL().equals("ldap://172.28.113.33:389");
        assert configs.get("demo").getSecurityPrincipal().equals("cn=d_ldapauth,cn=Users,dc=demo,dc=com");
    }
}
