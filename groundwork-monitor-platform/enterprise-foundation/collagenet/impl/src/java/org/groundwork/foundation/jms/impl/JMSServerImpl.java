/*
 * Collage - The ultimate data integration framework.
 *
 * Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")  
 * All rights reserved. This program is free software; you can redistribute it
 * and/or modify it under the terms of the GNU General Public License version 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for 
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 */
/*
 * Collage - The ultimate data integration framework.
 *
 * Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")  
 * All rights reserved. This program is free software; you can redistribute it
 * and/or modify it under the terms of the GNU General Public License version 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for 
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 */

package org.groundwork.foundation.jms.impl;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jms.Queue;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.jms.FoundationJMSException;
import org.groundwork.foundation.jms.JMSDestinationInfo;
import org.groundwork.foundation.jms.JMSServer;
import org.groundwork.foundation.jms.JMSServerInfo;
import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.jms.HornetQJMSClient;
import org.hornetq.core.config.Configuration;
import org.hornetq.core.config.impl.ConfigurationImpl;
import org.hornetq.core.config.impl.FileConfiguration;
import org.hornetq.core.remoting.impl.netty.NettyAcceptorFactory;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.core.remoting.impl.netty.TransportConstants;
import org.hornetq.core.server.HornetQServer;
import org.hornetq.core.server.HornetQServers;
import org.hornetq.core.server.impl.HornetQServerImpl;
import org.hornetq.jms.server.JMSServerManager;
import org.hornetq.jms.server.config.ConnectionFactoryConfiguration;
import org.hornetq.jms.server.config.JMSConfiguration;
import org.hornetq.jms.server.config.impl.ConnectionFactoryConfigurationImpl;
import org.hornetq.jms.server.config.impl.JMSConfigurationImpl;
import org.hornetq.jms.server.impl.JMSServerManagerImpl;
import org.hornetq.spi.core.security.HornetQSecurityManager;
import org.hornetq.spi.core.security.HornetQSecurityManagerImpl;
import org.jnp.server.Main;
import org.jnp.server.NamingBeanImpl;

/**
 * @author rogerrut
 * 
 *         Created: Apr 5, 2007
 */
public class JMSServerImpl implements JMSServer {
	/**
	 * JMS Server instance Instance that maintains a JMS server
	 */

	/** Enable log4j for JMSServer class */
	private Log log = LogFactory.getLog(this.getClass());

	private AtomicBoolean bJMSServerRunning = new AtomicBoolean(false);

	/* Local storage for shared information */
	private String serverContext;
	private int serverId;
	private String serverName;
	private String adminUser;
	private String adminPassword;
	private int adminPort;

	private Main jndiServer = null;
	private NamingBeanImpl naming = null;

	private JMSServerManager jmsServer = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.groundwork.foundation.jms.JMSServer#addQueue(org.groundwork.foundation
	 * .jms.JMSQueueInfo)
	 */
	public void addQueue(JMSDestinationInfo queueInfo)
			throws FoundationJMSException {
		// JMSserver has to run otherwise configuration will fail
		if (this.bJMSServerRunning.get() == false)
			throw new FoundationJMSException(
					"Cannot add queue to JMS server. Server is not initialized yet. Call initialize first");

		if (queueInfo == null)
			throw new FoundationJMSException(
					"QueueInfo cannot be null for creating new queue.");

		// Lookup if queue already exists
		Queue queueToCreate = null;

		// Use JNDI factory settings if provided
		Hashtable<String, String> htJndiProperties = null;
		if (queueInfo.getContextFactory() != null
				&& queueInfo.getHost() != null && queueInfo.getPort() != null) {
			htJndiProperties = new Hashtable<String, String>(3);
			htJndiProperties.put("java.naming.factory.initial",
					queueInfo.getContextFactory());
			htJndiProperties.put("java.naming.factory.host",
					queueInfo.getHost());
			htJndiProperties.put("java.naming.factory.port",
					queueInfo.getPort());
		}

		InitialContext ictx = null;

		try {

			ictx = new InitialContext(htJndiProperties);
			queueToCreate = (Queue) ictx.lookup(queueInfo.getDestinationName());
		} catch (NamingException ne) {
			log.info("Queue lookup failed create new entry...");
		} finally {
			if (ictx != null) {
				try {
					ictx.close();
				} catch (Exception e) {
				}
				ictx = null;
			}
		}

		// try {
		if (queueToCreate == null) {
			// Create Queue

			queueToCreate = HornetQJMSClient.createQueue(queueInfo
					.getDestinationName());

			// ictx = new javax.naming.InitialContext(htJndiProperties);
			// ictx.bind(queueInfo.getDestinationName(), queueToCreate);

			log.info("JMS Queue created - " + queueInfo.getDestinationName());
		} else {
			log.warn("Queue already exists. Name: "
					+ queueInfo.getDestinationName());
		}

		/*
		 * } catch (NamingException ne) {
		 * log.warn("Queue lookup failed. Message: " + ne); } finally { if (ictx
		 * != null) { try { ictx.close(); } catch (Exception e) { } ictx = null;
		 * } }
		 */
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.groundwork.foundation.jms.JMSServer#addTopic(org.groundwork.foundation
	 * .jms.JMSTopicInfo)
	 */
	public void addTopic(JMSDestinationInfo topicInfo)
			throws FoundationJMSException {
		// JMSserver has to run otherwise configuration will fail
		if (this.bJMSServerRunning.get() == false)
			throw new FoundationJMSException(
					"Cannot add topic to JMS server. Server is not initialized yet. Call initialize first");

		if (topicInfo == null)
			throw new FoundationJMSException(
					"TopicInfo cannot be null for creating new topic.");

		// Lookup if queue already exists
		Topic topicToCreate = null;

		// Use JNDI factory settings if provided
		Hashtable<String, String> htJndiProperties = null;
		if (topicInfo.getContextFactory() != null
				&& topicInfo.getHost() != null && topicInfo.getPort() != null) {
			htJndiProperties = new Hashtable<String, String>(3);
			htJndiProperties.put("java.naming.factory.initial",
					topicInfo.getContextFactory());
			htJndiProperties.put("java.naming.factory.host",
					topicInfo.getHost());
			htJndiProperties.put("java.naming.factory.port",
					topicInfo.getPort());
		}

		InitialContext ictx = null;

		try {

			ictx = new InitialContext(htJndiProperties);
			topicToCreate = (Topic) ictx.lookup(topicInfo.getDestinationName());
		} catch (NamingException ne) {
			log.info("Topic lookup failed create new entry...");
		} finally {
			if (ictx != null) {
				try {
					ictx.close();
				} catch (Exception e) {
				}
				ictx = null;
			}
		}

		// try {
		if (topicToCreate == null) {
			// Create topic

			topicToCreate = HornetQJMSClient.createTopic(topicInfo
					.getDestinationName());

			// ictx = new javax.naming.InitialContext(htJndiProperties);
			// ictx.bind(topicInfo.getDestinationName(), topicToCreate);

			log.info("JMS Topic created - " + topicInfo.getDestinationName());
		} else {
			log.warn("Topic already exists. Name: "
					+ topicInfo.getDestinationName());
		}

		/*
		 * } catch (NamingException ne) {
		 * log.warn("Topic lookup failed. Message: " + ne); } finally { if (ictx
		 * != null) { try { ictx.close(); } catch (Exception e) { } ictx = null;
		 * } }
		 */
	}

	public void initialize(JMSServerInfo serverInfo)
			throws FoundationJMSException {
		try {
			FileConfiguration fc = new FileConfiguration();

			fc.setConfigurationUrl("/home/arul/Downloads/stock_jboss_epp/jboss-epp-5.2/jboss-as/server/production/conf/hornetq-jms.xml");

			fc.start();

			HornetQSecurityManager sm = new HornetQSecurityManagerImpl();

			HornetQServer liveServer = new HornetQServerImpl(fc, sm);

			jmsServer = new JMSServerManagerImpl(liveServer,
					"/home/arul/Downloads/stock_jboss_epp/jboss-epp-5.2/jboss-as/server/production/conf/hornetq-jms.xml");

			jmsServer.setContext(null);

			jmsServer.start();
		} catch (Exception exc) {
			throw new FoundationJMSException(exc.getMessage());
		}

	}

	/**
	 * Initializes the hornetQ
	 * 
	 * @param serverInfo
	 * @throws FoundationJMSException
	 */
	public void initialize2(JMSServerInfo serverInfo)
			throws FoundationJMSException {
		try {

			if (bJMSServerRunning.get() == true) {
				// Already running nothing to do
				log.info("JMS Server already running");
				return;
			}

			// Validate Server Info
			if (serverInfo == null)
				throw new FoundationJMSException(
						"JMS Server Info can't be null. JMS server can' start");

			// Save shared info
			this.serverContext = serverInfo.getServerContext();
			this.serverId = serverInfo.getServerId();

			this.serverName = serverInfo.getServerName();
			this.adminUser = serverInfo.getAdminUser();
			this.adminPassword = serverInfo.getAdminPassword();

			Configuration configuration = new ConfigurationImpl();
			configuration.setPersistenceEnabled(false);
			configuration.setSecurityEnabled(false);
			// Though we don't use clustered mode at this time, we need to
			// suppress the warning message that might panic customers
			// by overriding the default user/password
			configuration.setClustered(false);
			configuration.setClusterUser(this.adminUser);
			configuration.setClusterPassword(this.adminPassword);

			Map<String, Object> connectionParams = new HashMap<String, Object>();
			connectionParams.put(TransportConstants.HOST_PROP_NAME,
					serverInfo.getHost()); // TODO
			connectionParams.put(TransportConstants.PORT_PROP_NAME, 5445); // TODO
			TransportConfiguration acceptorConfig = new TransportConfiguration(
					NettyAcceptorFactory.class.getName());
			HashSet<TransportConfiguration> setTransp = new HashSet<TransportConfiguration>();
			setTransp.add(acceptorConfig);
			configuration.setAcceptorConfigurations(setTransp);

			HornetQServer hornetQServer = HornetQServers
					.newHornetQServer(configuration);
			/*
			 * System.setProperty("java.naming.factory.initial",
			 * serverInfo.getContextFactory());
			 */

			TransportConfiguration connectorConfig = new TransportConfiguration(
					NettyConnectorFactory.class.getName());

			configuration.getConnectorConfigurations().put("connector",
					connectorConfig);

			JMSConfiguration jmsConfig = new JMSConfigurationImpl();
			/*
			 * System.setProperty("java.security.policy", "server.policy"); if
			 * (System.getSecurityManager() == null)
			 * System.setSecurityManager(new RMISecurityManager());
			 */
			Hashtable<String, String> env = new Hashtable<String, String>();
			env.put("java.naming.factory.initial",
					"org.jnp.interfaces.NamingContextFactory");
			env.put("java.naming.provider.url", "jnp://" + serverInfo.getHost()
					+ ":" + serverInfo.getPort());
			/*
			 * env.put("java.naming.factory.url.pkgs",
			 * serverInfo.getJNDIFactoryURLPkgs());
			 */
			Context context = new InitialContext(env);
			// jmsConfig.setContext(context);

			ArrayList<String> connectorNames = new ArrayList<String>();
			connectorNames.add("connector");
			ConnectionFactoryConfiguration cfConfig = new ConnectionFactoryConfigurationImpl(
					serverInfo.getServerContext(), false, connectorNames, "/"
							+ serverInfo.getServerContext());
			jmsConfig.getConnectionFactoryConfigurations().add(cfConfig);

			jmsServer = new JMSServerManagerImpl(
					hornetQServer,
					"/home/arul/Downloads/stock_jboss_epp/jboss-epp-5.2/jboss-as/server/production/conf/hornetq-jms.xml");
			jmsServer.start();
			// Server started
			bJMSServerRunning.set(true);
		} catch (Exception exc) {
			exc.printStackTrace();
		}

	}

	public void restartJMSServer(JMSServerInfo serverInfo)
			throws FoundationJMSException {
		try {
			this.unInitialize();
			this.initialize(serverInfo);
		} catch (FoundationJMSException fje) {
			throw new FoundationJMSException(
					"Restart of JMS Server failed. Error: " + fje);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.groundwork.foundation.jms.JMSServer#removeQueue(org.groundwork.foundation
	 * .jms.JMSDestinationInfo)
	 */
	public void removeQueue(JMSDestinationInfo queueInfo)
			throws FoundationJMSException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.groundwork.foundation.jms.JMSServer#removeTopic(org.groundwork.foundation
	 * .jms.JMSDestinationInfo)
	 */
	public void removeTopic(JMSDestinationInfo topicInfo)
			throws FoundationJMSException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.groundwork.foundation.jms.JMSServer#unInitialize()
	 */
	public void unInitialize() throws FoundationJMSException {
		// Stop JMS server
		if (bJMSServerRunning.get() == true) {
			try {

				// Stop the server
				jmsServer.stop();
				// naming.stop();
				// jndiServer.stop();

				log.info("HornetQ agent stopped.");
			} catch (UnknownHostException uhe) {
				log.error("UnknownHostException. Failed to stop HornetQ Agent. Server ["
						+ this.serverName + "] Error: " + uhe);
			} catch (ConnectException ce) {
				log.error("ConnectException. Failed to stop HornetQ Agent. Server ["
						+ this.serverName + "] Error: " + ce);

			} catch (Exception ce) {
				log.error("Exception. Failed to stop HornetQ Agent. Server ["
						+ this.serverName + "] Error: " + ce);

			}
			// Reset flag
			bJMSServerRunning.set(false);

			log.info("Stopped JMS Server");
		} else {
			log.info("JMS server not running -- nothing to un initialize");
		}

	}
}
