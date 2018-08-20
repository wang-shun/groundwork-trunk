/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2009  GroundWork Open Source Solutions info@groundworkopensource.com

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
package org.groundwork.foundation.bs.events;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.groundwork.foundation.bs.BusinessServiceImpl;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.jms.JMSDestinationInfo;
import org.groundwork.foundation.jms.JMSDestinationWriter;
import org.groundwork.foundation.jms.impl.JMSDestinationInfoImpl;
import org.groundwork.foundation.jms.impl.JMSDestinationWriterImpl;

import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The EventService is responsible for publishing events to the configured JMS topic.
 *
 */
public class EntityPublisherImpl extends BusinessServiceImpl implements EntityPublisher  
{	

	
	private ConcurrentHashMap<String, String> distinctEntityMap = null;

	/**
	 * Logger.
	 */
	private static final Logger logger = Logger
			.getLogger(EntityPublisherImpl.class.getName());
	
	public ConcurrentHashMap<String, String> getDistinctEntityMap() {
		return distinctEntityMap;
	}

	public void setDistinctEntityMap(
			ConcurrentHashMap<String, String> distinctEntityMap) {
		this.distinctEntityMap = distinctEntityMap;
	}
	
	private Vector<String> _entities = new Vector<String>(100);
	
	private Properties _configuration = null;
	private PublishThread _publishThread = null;

	private final ArrayList<EntityPublisherListener> _listeners = new ArrayList<>();
	
	/* Enable Log4j */
	Log log = LogFactory.getLog(this.getClass());
	
	public EntityPublisherImpl (Properties configuration)
	{
		distinctEntityMap = new ConcurrentHashMap<String, String>();
		_configuration = configuration;
	}
	
	public void initialize() throws BusinessServiceException 
	{
		// Create and start publish thread
		_publishThread = new PublishThread(_configuration);
		_publishThread.start();
	}

	@Override
	public void uninitialize() throws BusinessServiceException
	{
		if (_publishThread != null)
		{
			// Stop thread
			_publishThread.uninitialize();
			_publishThread = null;
		}
	}
	
	public void publishEntity(String data) 
	{

		
		_entities.add(data);
	}	
	
	private class PublishThread extends Thread
	{
		// Configuration property constants
	
		private static final String PROP_SERVER_CONTEXT = "jms.server.context.id";
				
		private static final String PROP_JNDI_INITIAL_FACTORY = "jndi.factory.initial";
		private static final String PROP_JNDI_HOST = "jndi.factory.host";
		private static final String PROP_JNDI_PORT = "jndi.factory.port";
						
		// Default Values
		private static final String DEFAULT_TOPIC_NAME = "/topic/foundation_entities";
		private static final String DEFAULT_SERVER_CONTEXT = "jms/RemoteConnectionFactory";
	
		private static final int DEFAULT_BATCH_INTERVAL = 5000;
		
		// JNDI Configuration
		private String _jndiInitialFactory = null;
		private String _jndiHost = null;
		private String _jndiServer = null;
		
		private String _topicName = DEFAULT_TOPIC_NAME;
		private String _serverContext = DEFAULT_SERVER_CONTEXT;
		
		private int _batchInterval = DEFAULT_BATCH_INTERVAL;  // In milliseconds
		
		private String _jndiAdminUser = null;
		
		private String _jndiAdminCredentials = null;
		
		private boolean _isRunning = false;
		
		private PublishThread (Properties configuration)
		{		
			super();
			
			if (configuration == null)
			{
				log.warn("PublishThread - No configuration defined.  Using defaults.");
			}
			else {
				
				_jndiInitialFactory = configuration.getProperty(PROP_JNDI_INITIAL_FACTORY, JMSDestinationInfo.DEFAULT_JNDI_FACTORY_CLASS);
				_jndiHost = configuration.getProperty(PROP_JNDI_HOST, JMSDestinationInfo.DEFAULT_JNDI_HOST);
				_jndiServer = configuration.getProperty(PROP_JNDI_PORT, JMSDestinationInfo.DEFAULT_JNDI_PORT);
				
				_topicName = DEFAULT_TOPIC_NAME;
				_serverContext = configuration.getProperty(PROP_SERVER_CONTEXT, DEFAULT_SERVER_CONTEXT);
				_jndiAdminUser = configuration.getProperty("jms.admin.user", JMSDestinationInfo.DEFAULT_JNDI_ADMIN_USER).trim();
				_jndiAdminCredentials = configuration.getProperty("jms.admin.password", JMSDestinationInfo.DEFAULT_JNDI_ADMIN_CREDENTIALS).trim();
			}
		}
		
		public void run() 
		{
			boolean bInitialized = false;
			_isRunning = true;
			
			// Create JMS Writer
			JMSDestinationInfo topicInfo = new JMSDestinationInfoImpl(_jndiInitialFactory, 
																	  _jndiHost, 
																	  _jndiServer, 
																	  _serverContext, 
																	  _topicName,_jndiAdminUser,_jndiAdminCredentials);
			
			// Try to initialize - we continue to try until we are successful
			JMSDestinationWriter jmsWriter = new JMSDestinationWriterImpl();
			while (bInitialized == false)
			{
				try { 
					jmsWriter.initialize(topicInfo);
					bInitialized = true;					
					log.warn("Successfully initialized EntityPublisherService JMSDestination Writer.");
				}
				catch (Exception e)
				{
					log.warn("Unable to initialize JMSDestinationWriter - Will retry.", e);
					// Wait before we try to re-initialize.  JMS Server may not be started yet.
					try {
                        Thread.sleep(5000);
                        jmsWriter.unInitialize();
					}
					catch (Exception ex)
					{
						log.error("Exception occurred trying to sleep.", ex);
					}
				}
			}
			
			int numEvents = 0;
			
			while (_isRunning == true)
			{				
				numEvents = _entities.size();
				String newMessage = null;
				StringBuilder sb = new StringBuilder(32);
                try {
                    for (int i = 0; i < numEvents; i++) {
                        logger.debug(_entities);
                        newMessage = _entities.get(i);
                        sb.append(newMessage);
                        if (logger.isDebugEnabled()) {
                            logger.info("Publishing Entity message : " + sb.toString());
                        }
                        // Send individual events
                        TextMessage message = jmsWriter.writeDestination(sb.toString());
						synchronized (_listeners) {
							for (EntityPublisherListener listener : _listeners) {
								listener.eventTextMessage(message);
							}
						}
                        sb.setLength(0);
                    }
                    _entities.removeAllElements();
                    // Commit all new messages
                    jmsWriter.commit();
                } catch (Exception e) {
                    log.error("Unable to write or commit to jms topic [" + _topicName + "]", e);
                    jmsWriter.reInitialize(topicInfo);
                }
                // Sleep batch interval
				synchronized (this) {
					if (_isRunning) {
						try {
							wait(_batchInterval);
						} catch (InterruptedException ie) {
						}
					}
				}
            }
			// Cleanup
			if (jmsWriter != null) {
				jmsWriter.unInitialize();
				jmsWriter = null;
			}
		}
		
		public synchronized void uninitialize() {
			_isRunning = false;
			notifyAll();
		}	
	}

	@Override
	public void addEntityPublisherListener(EntityPublisherListener entityPublisherListener) {
		synchronized (_listeners) {
			_listeners.add(entityPublisherListener);
		}
	}

	@Override
	public void removeEntityPublisherListener(EntityPublisherListener entityPublisherListener) {
		synchronized (_listeners) {
			_listeners.remove(entityPublisherListener);
		}
	}
}
