/*
 * Copyright (C) 2004-2015  GroundWork Open Source Solutions info@groundworkopensource.com

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

package org.groundwork.cloudhub.jmx;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * JBossAS7JMXAgent - JBoss Application Server 7 JMX agent implementation
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class JBossAS7JMXAgent extends JMXAgent {

    private static final String JMX_CONNECTION_URL = "service:jmx:remoting-jmx://localhost:%d";
    private static final int JMX_CONNECTION_PORT = 4447;

    private final static Set<String> IGNORE_INTEGER_ATTRIBUTE_NAMES = new HashSet<String>(Arrays.asList("debug",
            "RolesCount", "UserCount", "locks held"));

    @Override
    protected MBeanServerConnection createMBeanServerConnection(JMXAgentConfiguration configuration) throws Exception {
        // make JMX connection to local MBean server
        int port = ((configuration.getPort() != null) ? configuration.getPort() : JMX_CONNECTION_PORT);
        JMXServiceURL url = new JMXServiceURL(String.format(JMX_CONNECTION_URL, port));
        JMXConnector connector = JMXConnectorFactory.connect(url);
        return connector.getMBeanServerConnection();
    }

    @Override
    protected Set<String> ignoreIntegerAttributeNames() {
        return IGNORE_INTEGER_ATTRIBUTE_NAMES;
    }
}
