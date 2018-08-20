package com.groundwork.agents.appservers.collector.impl;

import java.util.Properties;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.groundwork.agents.appservers.utils.StatUtils;

public class TomcatAdminClient {

	public static String TOMCAT_PROPERTIES = "gwos_tomcat.xml";

	public static MBeanServerConnection createMBeanServerConnection()
			throws Exception {
		Properties properties = StatUtils
				.readProperties(TomcatAdminClient.TOMCAT_PROPERTIES);
		String hostName = properties.getProperty("hostname");
		int port = Integer.parseInt(properties.getProperty("port"));
		JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://"
				+ hostName + ":" + port + "/jmxrmi");
		JMXConnector connector = JMXConnectorFactory.connect(url);
		return connector.getMBeanServerConnection();
	}

	public static MBeanServerConnection createMBeanServerConnection(
			Properties prop) throws Exception {		
		String hostName = prop.getProperty("hostname");
		int port = Integer.parseInt(prop.getProperty("port"));
		JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://"
				+ hostName + ":" + port + "/jmxrmi");
		JMXConnector connector = JMXConnectorFactory.connect(url);
		return connector.getMBeanServerConnection();
	}

}
