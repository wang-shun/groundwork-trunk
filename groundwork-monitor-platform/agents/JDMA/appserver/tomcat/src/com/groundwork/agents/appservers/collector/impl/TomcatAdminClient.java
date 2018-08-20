package com.groundwork.agents.appservers.collector.impl;

import com.groundwork.agents.appservers.utils.StatUtils;
import org.apache.log4j.Logger;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class TomcatAdminClient {

	private static org.apache.log4j.Logger log = Logger.getLogger(TomcatAdminClient.class);

	public static String TOMCAT_PROPERTIES = "gwos_tomcat.xml";

	JMXConnector connector = null;

	public TomcatAdminClient() {
	}

	public MBeanServerConnection createMBeanServerConnection()
			throws Exception {
		Properties properties = StatUtils.readProperties(TomcatAdminClient.TOMCAT_PROPERTIES);
		String hostName = "localhost"; // since it is remote agent, it always localhost
		int port = Integer.parseInt(properties.getProperty("port"));
		JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://"
				+ hostName + ":" + port + "/jmxrmi");
		connector = JMXConnectorFactory.connect(url, getCredentials(properties));
		return connector.getMBeanServerConnection();
	}

	public MBeanServerConnection createMBeanServerConnection(
			Properties prop) throws Exception {

        String hostName = "localhost";// since it is remote agent, it always localhost
		int port = Integer.parseInt(prop.getProperty("port"));

		JMXServiceURL url = new JMXServiceURL(
				"service:jmx:rmi:///jndi/rmi://"
						+ hostName
						+ ":" + port + "/jmxrmi");
		JMXConnector connector = JMXConnectorFactory.connect(url, getCredentials(prop));
		return connector.getMBeanServerConnection();
	}

	public void shutdown() {
		if (connector != null) {
			try {
				connector.close();
				connector = null;
			}
			catch (Exception e) {
				log.error("Failed to close JMX connection", e);
			}
		}
	}

	private Map getCredentials(Properties properties) {
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
