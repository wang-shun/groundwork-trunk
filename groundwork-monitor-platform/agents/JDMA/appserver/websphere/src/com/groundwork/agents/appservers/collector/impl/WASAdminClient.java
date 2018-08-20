/*
 * 
 * Copyright 2010 GroundWork Open Source, Inc. ("GroundWork") All rights
 * reserved. This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package com.groundwork.agents.appservers.collector.impl;

import com.groundwork.agents.appservers.utils.JDMALog;
import com.groundwork.agents.appservers.utils.StatUtils;
import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.AdminClientFactory;
import com.ibm.websphere.management.exception.ConnectorException;

import java.util.Properties;

/**
 * Websphere JMX AdminClient Service
 * 
 * @author Arul Shanmugam
 * 
 */
public class WASAdminClient {
	private String hostname = "localhost"; // default
	private String port = "8880"; // default

	private String username;
	private String password;
	private String connector_security_enabled;
	private String connector_soap_config;
	private String ssl_trustStore;
	private String ssl_keyStore;
	private String ssl_trustStorePassword;
	private String ssl_keyStorePassword;

	private Properties properties;
	public static String WEBSPHERE_PROPERTIES = "gwos_websphere.xml";

	private AdminClient adminClient;

//	private static Logger log = Logger.getLogger(WASAdminClient.class);
    private static JDMALog log = new JDMALog();

	public WASAdminClient() {

	}

	public WASAdminClient(Properties prop) {
		hostname = prop.getProperty("hostname");
		port = prop.getProperty("port");
		connector_security_enabled = prop
				.getProperty("connector_security_enabled");
		ssl_trustStore = prop.getProperty("ssl_trustStore");
		ssl_keyStore = prop.getProperty("ssl_keyStore");
		ssl_trustStorePassword = prop.getProperty("ssl_trustStorePassword");
		ssl_keyStorePassword = prop.getProperty("ssl_keyStorePassword");

		if (prop.getProperty("username") != null) {
			username = prop.getProperty("username");
		}

		if (prop.getProperty("password") != null) {
			password = prop.getProperty("password");
		}

	}

	public AdminClient getAdminClient() {
		return adminClient;
	}

	public AdminClient testConnection() throws ConnectorException {
		return connect();
	}

	public AdminClient create() throws ConnectorException {
		loadProperties();
		return connect();

	}

	private AdminClient connect() throws ConnectorException {
        try {
            Properties props = new Properties();
            props.setProperty(AdminClient.CONNECTOR_TYPE,
                    AdminClient.CONNECTOR_TYPE_SOAP);
            props.setProperty(AdminClient.CONNECTOR_HOST, hostname);
            props.setProperty(AdminClient.CONNECTOR_PORT, port);
            props.setProperty(AdminClient.CACHE_DISABLED, "false");

            if (connector_security_enabled == "false") {
                adminClient = AdminClientFactory.createAdminClient(props);
                return adminClient;
            }

            props.setProperty(AdminClient.CONNECTOR_SECURITY_ENABLED,
                    connector_security_enabled);
            props.setProperty(AdminClient.CONNECTOR_AUTO_ACCEPT_SIGNER, "true");
            props.setProperty("javax.net.ssl.trustStore", ssl_trustStore);
            props.setProperty("javax.net.ssl.keyStore", ssl_keyStore);
            props.setProperty("javax.net.ssl.trustStorePassword",
                    ssl_trustStorePassword);
            props.setProperty("javax.net.ssl.keyStorePassword",
                    ssl_keyStorePassword);

            // Use username and password or soap.client.props file
            if (username == null || password == null) {
                props.setProperty(AdminClient.CONNECTOR_SOAP_CONFIG,
                        connector_soap_config);
            } else {
                props.setProperty(AdminClient.USERNAME, username);
                props.setProperty(AdminClient.PASSWORD, password);
            }

            adminClient = AdminClientFactory.createAdminClient(props);
        }
        catch (Exception e) {
            throw new ConnectorException("Failed to connect to WAS JMX server, hostName " + hostname + ", exception: " + e.getMessage(), e);
        }
		return adminClient;
	}

	public void loadProperties() {

		properties = StatUtils
				.readProperties(WASAdminClient.WEBSPHERE_PROPERTIES);
		if (properties != null && properties.size() > 0) {
			connector_security_enabled = properties
					.getProperty("connector_security_enabled");
			ssl_trustStore = properties.getProperty("ssl_trustStore");
			ssl_keyStore = properties.getProperty("ssl_keyStore");
			ssl_trustStorePassword = properties
					.getProperty("ssl_trustStorePassword");
			ssl_keyStorePassword = properties
					.getProperty("ssl_keyStorePassword");

			if (properties.getProperty("username") != null) {
				username = properties.getProperty("username");
			}

			if (properties.getProperty("password") != null) {
				password = properties.getProperty("password");
			}

			if (properties.getProperty("hostname") != null) {
				hostname = properties.getProperty("hostname");
			}

			if (properties.getProperty("port") != null) {
				port = properties.getProperty("port");
			}
		} // end if

	}

}
