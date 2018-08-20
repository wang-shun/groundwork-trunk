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

import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Hashtable;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.management.MBeanServer;

import com.groundwork.agents.appservers.utils.StatUtils;

/**
 * Weblogic JMX AdminClient Service
 * 
 * @author Arul Shanmugam
 * 
 */
public class WLSAdminClient {

	// private ResourceBundle resBundle;
	public static String WEBLOGIC_PROPERTIES = "gwos_weblogic.xml";

	public static MBeanServerConnection createMBeanServer() throws Exception {
		Properties properties = StatUtils
		.readProperties(WLSAdminClient.WEBLOGIC_PROPERTIES);
		return WLSAdminClient.createMBeanServer(properties);
	}
	
	public static MBeanServerConnection createMBeanServer(Properties properties) throws Exception {
		String hostName = properties.getProperty("hostname");
		String protocol = properties.getProperty("protocol");
		Integer portInteger = Integer.valueOf(properties.getProperty("port"));
		int port = portInteger.intValue();
		String jndiroot = "/jndi/";
		JMXServiceURL serviceURL = new JMXServiceURL(protocol, hostName,
				port, jndiroot + "weblogic.management.mbeanservers.runtime");
		Hashtable h = new Hashtable();
		h.put(Context.SECURITY_PRINCIPAL, properties.getProperty("username"));
		h.put(Context.SECURITY_CREDENTIALS, properties.getProperty("password"));
		h.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES,
				"weblogic.management.remote");
		h.put("jmx.remote.x.request.waiting.timeout", new Long(10000));
		JMXConnector connector = JMXConnectorFactory.connect(serviceURL, h);
		return connector.getMBeanServerConnection();
	}

}
