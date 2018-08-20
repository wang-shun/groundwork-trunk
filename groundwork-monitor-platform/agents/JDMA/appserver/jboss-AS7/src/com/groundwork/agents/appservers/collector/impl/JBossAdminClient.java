package com.groundwork.agents.appservers.collector.impl;

import com.groundwork.agents.appservers.utils.StatUtils;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class JBossAdminClient {

	public static String JBOSS_PROPERTIES = "gwos_jbossas7.xml";

    public static MBeanServerConnection createMBeanServerConnection()
            throws Exception {
        Properties properties = StatUtils.readProperties(JBossAdminClient.JBOSS_PROPERTIES);
        String hostName = "localhost"; // since it is remote agent, it always localhost
        int port = Integer.parseInt(properties.getProperty("port"));
        JMXServiceURL url = new JMXServiceURL("service:jmx:remoting-jmx://"
                + hostName + ":" + port);
        JMXConnector connector = JMXConnectorFactory.connect(url, getCredentials(properties));
        return connector.getMBeanServerConnection();
    }

    public static MBeanServerConnection createMBeanServerConnection(
            Properties prop) throws Exception {
        String hostName = "localhost";// since it is remote agent, it always localhost
        int port = Integer.parseInt(prop.getProperty("port"));
        JMXServiceURL url = new JMXServiceURL("service:jmx:remoting-jmx://"
                + hostName + ":" + port);
        JMXConnector connector = JMXConnectorFactory.connect(url, getCredentials(prop));
        return connector.getMBeanServerConnection();
    }

    private static Map getCredentials(Properties properties) {
        HashMap environment = new HashMap();
        String username = properties.getProperty("jmx_username", null);
        String password = properties.getProperty("jmx_password", "");
        if (username != null && username.trim().length() > 0)  {
            String[] credentials = new String[]
                    {
                            username, password
                    };
            environment.put(JMXConnector.CREDENTIALS, credentials);
        }
        return environment;
    }
}
