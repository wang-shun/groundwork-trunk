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
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

/**
 * JBossJMXAgent - JBoss JMX agent implementation
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class JBossJMXAgent extends JMXAgent {

    private static final String JNDI_NAMING_PROVIDER_URL = "jnp://localhost:%d";
    private static final int JNDI_NAMING_PROVIDER_PORT = 1099;
    private static final String JNDI_NAMING_FACTORY_CLASS = "org.jnp.interfaces.NamingContextFactory";
    private static final String JNDI_NAMING_FACTORY_URL_PACKAGES = "org.jboss.naming:org.jnp.interfaces";
    private static final String JMX_AGENT_JNDI_NAME = "jmx/invoker/RMIAdaptor";

    private final static Set<String> IGNORE_INTEGER_ATTRIBUTE_NAMES = new HashSet<String>(Arrays.asList("debug",
            "RolesCount", "UserCount"));

    @Override
    protected MBeanServerConnection createMBeanServerConnection(JMXAgentConfiguration configuration) throws Exception {
        // get JMX connection to local MBean server from JNDI
        Hashtable properties = new Hashtable();
        int port = ((configuration.getPort() != null) ? configuration.getPort() : JNDI_NAMING_PROVIDER_PORT);
        properties.put(Context.PROVIDER_URL, String.format(JNDI_NAMING_PROVIDER_URL, port));
        properties.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_NAMING_FACTORY_CLASS);
        properties.put(Context.URL_PKG_PREFIXES, JNDI_NAMING_FACTORY_URL_PACKAGES);
        InitialContext ctx = new InitialContext(properties);
        try {
            return (MBeanServerConnection)ctx.lookup(JMX_AGENT_JNDI_NAME);
        } finally {
            ctx.close();
        }
    }

    @Override
    protected Set<String> ignoreIntegerAttributeNames() {
        return IGNORE_INTEGER_ATTRIBUTE_NAMES;
    }
}
