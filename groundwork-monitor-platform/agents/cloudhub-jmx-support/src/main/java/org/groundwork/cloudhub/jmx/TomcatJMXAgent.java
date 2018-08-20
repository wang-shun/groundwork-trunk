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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * TomcatJMXAgent - Tomcat JMX agent implementation
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class TomcatJMXAgent extends JMXAgent {

    private static final String JMX_CONNECTION_URL = "service:jmx:rmi:///jndi/rmi://localhost:%d/jmxrmi";
    private static final int JMX_CONNECTION_PORT = 9012;

    private final static Set<String> INCLUDE_COMPOSITE_ATTRIBUTE_TYPE_NAMES = new HashSet<String>();
    private final static Map<String,Set<String>> INCLUDE_COMPOSITE_ATTRIBUTE_TYPE_KEYS = new HashMap<String,Set<String>>();
    static {
        INCLUDE_COMPOSITE_ATTRIBUTE_TYPE_NAMES.add(MEMORY_USAGE_PLATFORM_ATTRIBUTE_TYPE);
        INCLUDE_COMPOSITE_ATTRIBUTE_TYPE_KEYS.put(MEMORY_USAGE_PLATFORM_ATTRIBUTE_TYPE,
                new HashSet<String>(Arrays.asList(MEMORY_USAGE_PLATFORM_ATTRIBUTE_KEYS)));
    }

    @Override
    protected MBeanServerConnection createMBeanServerConnection(JMXAgentConfiguration configuration) throws Exception {
        // make JMX connection to local MBean server
        int port = ((configuration.getPort() != null) ? configuration.getPort() : JMX_CONNECTION_PORT);
        JMXServiceURL url = new JMXServiceURL(String.format(JMX_CONNECTION_URL, port));
        JMXConnector connector = JMXConnectorFactory.connect(url);
        return connector.getMBeanServerConnection();
    }

    @Override
    protected Set<String> includeCompositeAttributeTypeNames() {
        return INCLUDE_COMPOSITE_ATTRIBUTE_TYPE_NAMES;
    }

    @Override
    protected Map<String,Set<String>> includeCompositeAttributeTypeKeys() {
        return INCLUDE_COMPOSITE_ATTRIBUTE_TYPE_KEYS;
    }
}
