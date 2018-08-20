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

package org.groundwork.foundation.jms.test;

import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.jms.FoundationJMSException;
import org.groundwork.foundation.jms.JMSDestinationInfo;
import org.groundwork.foundation.jms.JMSDestinationWriter;
import org.groundwork.foundation.jms.JMSServer;
import org.groundwork.foundation.jms.JMSServerInfo;
import org.groundwork.foundation.jms.impl.JMSDestinationInfoImpl;
import org.groundwork.foundation.jms.impl.JMSDestinationWriterImpl;
import org.groundwork.foundation.jms.impl.JMSServerImpl;
import org.groundwork.foundation.jms.impl.JMSServerInfoImpl;

/**
 * @author rogerrut
 *
 * Created: Apr 9, 2007
 */
public class JMSTestServer extends Thread 
{
	private static final String MSG_LOG_MESSAGE =
		"<NAGIOS_LOG consolidation='NAGIOSEVENT' MonitorServerName=\"localhost\"" +
		" Host=\"localhost\" Device=\"127.0.0.1\" Severity=\"OK\"" +
		" MonitorStatus=\"UP\" TextMessage=\"JMS Test Message\"" +
		" ReportDate=\"%1$s\" LastInsertDate=\"\"" +
		" SubComponent=\"UNDEFINED\" ErrorType=\"HOST ALERT\" />";	
	
	private static final String MSG_HOST_STATUS = 
		"<HOST_STATUS MonitorServerName=\"localhost\" Host=\"localhost\" Device=\"127.0.0.1\"" +
		" CheckTypeID=\"0\" CurrentNotificationNumber=\"0\" LastCheckTime=\"%1$s\" LastNotificationTime=\"0\"" +
		" LastPluginOutput=\"\" LastStateChange=\"0000-00-00 00:00:00\" MonitorStatus=\"UP\"" +
		" PercentStateChange=\"0.00\" ScheduledDowntimeDepth=\"0\" TimeDown=\"0\" TimeUnreachable=\"0\"" +
		" TimeUp=\"0\" isAcknowledged=\"0\" isChecksEnabled=\"0\" isEventHandlersEnabled=\"0\"" +
		" isFailurePredictionEnabled=\"0\" isFlapDetectionEnabled=\"0\" isHostIsFlapping=\"0\"" +
		" isNotificationsEnabled=\"0\" isPassiveChecksEnabled=\"0\" isProcessPerformanceData=\"0\" />";
	
	private static final String MSG_ADMIN = 
		"<Adapter Session=\"11111\" ApplicationType=\"NAGIOS\" AdapterType=\"SystemAdmin\">" 
       +"<Command Action=\"ADD\">" 
       +"<Entity EntityType=\"HostGroup\">"
       +"<HostGroup HostGroupName=\"Test\" Description=\"A test\">"
       +"<Hosts><Item Type=\"Host\" Value=\"localhost\"/><Item Type=\"Host\" Value=\"Host1\"/><Item Type=\"Host\" Value=\"Host2\"/></Hosts>"
       +"</HostGroup>"
       +"<HostGroup HostGroupName=\"Test2\" Description=\"Another Test\">"
       +"<Hosts><Item Type=\"Host\" Value=\"Host1\"/><Item Type=\"Host\" Value=\"Host3\"/></Hosts>"
       +"</HostGroup>"
       +"</Entity>"
       +"</Command>"
       +"</Adapter>";
	
	private static final String MSG_ADMIN_LOGMSG = 
		"<Adapter Session=\"11111\" ApplicationType=\"NAGIOS\" AdapterType=\"SystemAdmin\"><Command Action=\"MODIFY\"><Entity EntityType=\"LogMessage\"><LogMessage LogMessageID=\"10653\" OperationStatus=\"ACCEPTED\"/><LogMessage LogMessageID=\"12367\" OperationStatus=\"ACCEPTED\"/></Entity></Command></Adapter>";
		/** Use log4j */
	    private static Log log = LogFactory.getLog(JMSTestServer.class);
		
		private String serverName;
		private String queueName;

		private String contextId;

		private int nbOfMessges;		
		
		protected String command;
	    protected String execCommand;
	    
	    private String persistencePath;
		
	    private int serverId;
		
	    private String	adminUser;
	    private String	adminPassword;
	    private int		adminPort;
	    
		private String jndiInitialFactory = JMSDestinationInfo.DEFAULT_JNDI_FACTORY_CLASS;
		private String jndiHost = JMSDestinationInfo.DEFAULT_JNDI_HOST;
		private String jndiServer = JMSDestinationInfo.DEFAULT_JNDI_PORT;
		
		private String jndiFactoryURLPkgs = null;
	    
		public JMSTestServer(Properties configuration) {
			/* Admin user info */
			adminUser = configuration.getProperty("admin.user", "root").trim();
			adminPassword = configuration.getProperty("admin.password", "root").trim();
			
			/* Admin Port Nb */
			String portNb = configuration.getProperty("admin.port", "16011").trim();
			Integer intPortNb = new Integer(portNb);
			adminPort = intPortNb.intValue(); 
			
			serverName = configuration.getProperty("server.name", "localhost").trim();
			queueName = configuration.getProperty("queue", "groundwork").trim();
			contextId = configuration.getProperty("context", "cf0").trim();
			String messageBlock = configuration.getProperty("message-block", "10").trim();
			
			String serverID = configuration.getProperty("serverid", "0").trim();
			persistencePath = configuration.getProperty("persistencePath", "./s0").trim();
			
			Integer value = new Integer(serverID);
			this.serverId = value.intValue();
				
			value = new Integer(messageBlock);

			this.nbOfMessges = value.intValue();
			this.jndiFactoryURLPkgs = configuration.getProperty("jndi.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces").trim();
		}

		public void run() {
			
			// Create structures for Server and Queue information
			JMSServerInfo serverInfo	= new JMSServerInfoImpl(jndiInitialFactory, 
					 								jndiHost, 
					 								jndiServer,
					 								this.serverName, 
					 								this.contextId, 
					 								this.serverId, 
					 								this.persistencePath, 
					 								this.adminUser, 
					 								this.adminPassword, 
					 								this.adminPort,jndiFactoryURLPkgs);
			
			JMSDestinationInfo queueInfo = new JMSDestinationInfoImpl(jndiInitialFactory, 
																			 jndiHost, 
																			 jndiServer,
																			 this.contextId, 
																			 this.queueName,JMSDestinationInfo.DEFAULT_JNDI_ADMIN_USER, JMSDestinationInfo.DEFAULT_JNDI_ADMIN_CREDENTIALS);
			
			JMSServer jmsServer = null;
			JMSDestinationWriter jmsWriter = null;
			try
			{
				// Create a server
				jmsServer = new JMSServerImpl();
				jmsServer.initialize(serverInfo);
				
				log.info("Joram JMS started");
				
				jmsServer.addQueue(queueInfo);
				
				log.info("Added Queue to JMS server");
				
				jmsWriter = new JMSDestinationWriterImpl();
				jmsWriter.initialize(queueInfo);
					
			} 
			catch (FoundationJMSException fje)
			{
				log.error("Error running test script. Error: " + fje);
			}
						
			// Wait for input
			while (true)
			{
				try {
					char c = (char)System.in.read();
					
					// Exit on 
					if (c == 'c')
						break;
					
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String now = dateFormat.format(new Date());
					String msg = null;
					
					switch (c)
					{
						// Write Event Message
						case 'e':								
							msg = String.format(MSG_LOG_MESSAGE, now);
							jmsWriter.writeDestination(msg);
							jmsWriter.commit();
							break;	
						// Write Host Msg
						case 'h':
							msg = String.format(MSG_HOST_STATUS, now);
							jmsWriter.writeDestination(msg);
							jmsWriter.commit();
							break;
						// Bulk messages
						case 'b':
						{
							msg = String.format(MSG_HOST_STATUS, now);
							long start = System.currentTimeMillis();
							for (int i = 0; i < 1000; i++)
							{
								jmsWriter.writeDestination(msg);								
							}
							jmsWriter.commit();
							log.info("Time to Write 1000 messages (ms): " +
										(System.currentTimeMillis() - start));
						}
							break;
						// Bulk messages - 5000 messages
						case 'f':
						{
							msg = String.format(MSG_HOST_STATUS, now);
							long start = System.currentTimeMillis();
							for (int i = 0; i < 5000; i++)
							{
								jmsWriter.writeDestination(msg);								
							}
							jmsWriter.commit();
							log.info("Time to Write 5000 messages (ms): " +
										(System.currentTimeMillis() - start));
						}
							break;		
							// Bulk messages - 10000 messages
						case 't':
						{
							msg = String.format(MSG_HOST_STATUS, now);
							long start = System.currentTimeMillis();
							for (int i = 0; i < 10000; i++)
							{
								jmsWriter.writeDestination(msg);								
							}
							jmsWriter.commit();
							log.info("Time to Write 10000 messages (ms): " +
										(System.currentTimeMillis() - start));
						}
							break;								
						case 'a':
						{
							msg = String.format(MSG_ADMIN, now);
							jmsWriter.writeDestination(msg);
							jmsWriter.commit();
						}
						case 'l':
						{
							msg = String.format(MSG_ADMIN_LOGMSG, now);
							jmsWriter.writeDestination(msg);
							jmsWriter.commit();
						}
						default:
							continue;
					}		
				}
				catch (Exception e)
				{
					log.error(e);
				}
				
			}
			
			jmsWriter.unInitialize();
			jmsServer.unInitialize();
		}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String configFile = System.getProperty("configuration",
		"./consumer.properties");
			Properties configuration = new Properties();
			try {
				FileInputStream fis = new FileInputStream(configFile);
				configuration.load(fis);
			} catch (Exception e) {
				
				log.warn("WARNING: Could not load Consumer properties. Using defaults");
			}
			// TODO: log4j
			//System.out.println("Properies loaded..");
			JMSTestServer jmsTestserver = new JMSTestServer(configuration);
			jmsTestserver.run();
			
	}
}
