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
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;
import java.util.Hashtable;

/**
 * WebSphereJMXAgent - WebSphere JMX agent implementation
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class WebSphereJMXAgent extends JMXAgent {

    private static final String JMX_CONNECTION_URL = "service:jmx:iiop://localhost:%d/jndi/JMXConnector";
    private static final int JMX_CONNECTION_PORT = 2809;
    private static final String JNDI_NAMING_PROVIDER_URL = "corbaloc:iiop:localhost:%d/WsnAdminNameService";
    private static final String JNDI_NAMING_FACTORY_CLASS = "com.ibm.websphere.naming.WsnInitialContextFactory";

    @Override
    protected MBeanServerConnection createMBeanServerConnection(JMXAgentConfiguration configuration) throws Exception {
        // make JMX connection to local MBean server
        /*
          Note: secure/SSL access may require these untested settings:

          System.setProperty(Context.INITIAL_CONTEXT_FACTORY, JNDI_NAMING_FACTORY_CLASS);
          System.setProperty("com.ibm.CORBA.ConfigURL", "file:C:\\IBM\\WebSphere\\AppServer\\profiles\\AppSrv01\\properties\\sas.client.props");
          System.setProperty("com.ibm.SSL.ConfigURL", "file:C:\\IBM\\WebSphere\\AppServer\\profiles\\AppSrv01\\properties\\ssl.client.props");

          These would require gathering more configuration info. The initial context factory
          setting here should probably be avoided if possible since it is specified in the
          JMX connector factory connect properties.
        */
        int port = ((configuration.getPort() != null) ? configuration.getPort() : JMX_CONNECTION_PORT);
        JMXServiceURL url = new JMXServiceURL(String.format(JMX_CONNECTION_URL, port));
        Hashtable properties = new Hashtable();
        properties.put(Context.PROVIDER_URL, String.format(JNDI_NAMING_PROVIDER_URL, port));
        properties.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_NAMING_FACTORY_CLASS);
        if ((configuration.getUsername() != null) && (configuration.getPassword() != null)) {
            properties.put(Context.SECURITY_PRINCIPAL, configuration.getUsername());
            properties.put(Context.SECURITY_CREDENTIALS, configuration.getPassword());
        }
        JMXConnector connector = JMXConnectorFactory.connect(url, properties);
        return connector.getMBeanServerConnection();
    }

    @Override
    protected String jmxDataNamePrefix(ObjectName managementBeanName) {
        // compute prefix from management bean name properties
        StringBuilder prefix = new StringBuilder("was");
        prefix.append(NAME_DELIMITER);
        prefix.append(managementBeanName.getKeyProperty("cell"));
        prefix.append(NAME_DELIMITER);
        prefix.append(managementBeanName.getKeyProperty("node"));
        prefix.append(NAME_DELIMITER);
        prefix.append(managementBeanName.getKeyProperty("process"));
        prefix.append(NAME_DELIMITER);
        prefix.append(managementBeanName.getKeyProperty("type"));
        prefix.append(NAME_DELIMITER);
        prefix.append(managementBeanName.getKeyProperty("name"));
        return prefix.toString();
    }

    @Override
    protected boolean enableMergePlatformMBeans() {
        // merge platform MBeans since these are not exported by local MBean server
        return true;
    }
}
