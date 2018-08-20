/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com

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
import org.groundwork.foundation.bs.BusinessServiceImpl;
import org.groundwork.foundation.bs.ServiceNotify;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.jms.JMSDestinationInfo;
import org.groundwork.foundation.jms.JMSDestinationWriter;
import org.groundwork.foundation.jms.impl.JMSDestinationInfoImpl;
import org.groundwork.foundation.jms.impl.JMSDestinationWriterImpl;

import java.util.Properties;
import java.util.Vector;

/**
 * The EventService is responsible for publishing events to the configured JMS topic.
 *
 */
public class EventServiceImpl extends BusinessServiceImpl implements EventService  
{	
	private static final String BEG_EVENT = "<EVENT>";
	private static final String END_EVENT = "</EVENT>";
	
	private Vector<ServiceNotify> _events = new Vector<ServiceNotify>(100);
	
	private Properties _configuration = null;
	private PublishThread _publishThread = null;
	
	/* Enable Log4j */
	Log log = LogFactory.getLog(this.getClass());
	
	public EventServiceImpl (Properties configuration)
	{
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
	
	public void publishEvent(ServiceNotify notify) 
	{
		if (notify == null)
			throw new IllegalArgumentException("Invalid null ServiceNotify parameter.");
		
		// Validate notify
		Integer entityId = (Integer)notify.getAttribute(NOTIFY_ATTR_ENTITY_ID);
		if (entityId == null)
			throw new IllegalArgumentException("Invalid ServiceNotify parameter - Missing [" + NOTIFY_ATTR_ENTITY_ID + "] attribute.");
		
		_events.add(notify);
	}	
	
	private class PublishThread extends Thread
	{
		// Configuration property constants
		private static final String PROP_TOPIC_NAME = "fes.topic.name";
		private static final String PROP_MAX_BATCH_SIZE = "fes.batch.size";
		private static final String PROP_MAX_BATCH_INTERVAL = "fes.batch.interval";
		private static final String PROP_MAX_CHECK_INTERVAL = "fes.batch.interval";
		private static final String PROP_SERVER_CONTEXT = "jms.server.context.id";
		private static final String PROP_BATCH_CONCATENATE = "fes.batch.concatenate";
		
		private static final String PROP_JNDI_INITIAL_FACTORY = "jndi.factory.initial";
		private static final String PROP_JNDI_HOST = "jndi.factory.host";
		private static final String PROP_JNDI_PORT = "jndi.factory.port";
						
		// Default Values
		private static final String DEFAULT_TOPIC_NAME = "/topic/foundation_events";
        private static final String DEFAULT_SERVER_CONTEXT = "jms/RemoteConnectionFactory";
		private static final int DEFAULT_BATCH_SIZE = -1;
		private static final int DEFAULT_BATCH_INTERVAL = 5000;
		private static final int MINIMUM_BATCH_INTERVAL = 200;
		private static final boolean DEFAULT_BATCH_CONCATENATE = true;
		
		// JNDI Configuration
		private String _jndiInitialFactory = null;
		private String _jndiHost = null;
		private String _jndiServer = null;
		private String _jndiAdminUser = null;
		
		private String _jndiAdminCredentials = null;
		
		private String _topicName = DEFAULT_TOPIC_NAME;
		private String _serverContext = DEFAULT_SERVER_CONTEXT;
		
		private int _batchSize = DEFAULT_BATCH_SIZE; // -1 indicates ignore size
		private int _batchInterval = DEFAULT_BATCH_INTERVAL;  // In milliseconds
		private int _checkInterval = DEFAULT_BATCH_INTERVAL;  // In milliseconds
		private boolean _batchConcatenate = DEFAULT_BATCH_CONCATENATE;
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
				
				_topicName = configuration.getProperty(PROP_TOPIC_NAME, DEFAULT_TOPIC_NAME);
				_serverContext = configuration.getProperty(PROP_SERVER_CONTEXT, DEFAULT_SERVER_CONTEXT);
				_jndiAdminUser = configuration.getProperty("jms.admin.user", JMSDestinationInfo.DEFAULT_JNDI_ADMIN_USER).trim();
				_jndiAdminCredentials = configuration.getProperty("jms.admin.password", JMSDestinationInfo.DEFAULT_JNDI_ADMIN_CREDENTIALS).trim();
				
				String val = configuration.getProperty(PROP_MAX_BATCH_SIZE, null);
				if (val != null && val.length() > 0)
				{
					try {
						_batchSize = Integer.parseInt(val);
					} catch (Exception e)
					{
						log.warn("Invalid configuration setting for property, " + PROP_MAX_BATCH_SIZE 
								+ ".  Defaulting value to " + DEFAULT_BATCH_SIZE);
					}					
				}
				
				val = configuration.getProperty(PROP_MAX_BATCH_INTERVAL, null);
				if (val != null && val.length() > 0)
				{
					try {
						_batchInterval = Integer.parseInt(val);
						
						// Constrain to minimum sleep value
						if (_batchInterval < MINIMUM_BATCH_INTERVAL)
						{
							_batchInterval = MINIMUM_BATCH_INTERVAL;
							
							log.warn("Invalid configuration setting for property, " + PROP_MAX_BATCH_INTERVAL 
									+ ".  Value must be at least " + MINIMUM_BATCH_INTERVAL);
						}
					} catch (Exception e)
					{
						log.warn("Invalid configuration setting for property, " + PROP_MAX_BATCH_INTERVAL 
								+ ".  Defaulting value to " + DEFAULT_BATCH_INTERVAL);
					}					
				}	
				
				val = configuration.getProperty(PROP_MAX_CHECK_INTERVAL, null);
				if (val != null && val.length() > 0)
				{
					try {
						_checkInterval = Integer.parseInt(val);
						
						// Constrain to minimum sleep value
						if (_checkInterval < MINIMUM_BATCH_INTERVAL)
						{
							_checkInterval = MINIMUM_BATCH_INTERVAL;
							
							log.warn("Invalid configuration setting for property, " + PROP_MAX_CHECK_INTERVAL 
									+ ".  Value must be at least " + MINIMUM_BATCH_INTERVAL);
						}
					} catch (Exception e)
					{
						log.warn("Invalid configuration setting for property, " + PROP_MAX_CHECK_INTERVAL 
								+ ".  Defaulting value to " + DEFAULT_BATCH_INTERVAL);
					}					
				}	
				
				val = configuration.getProperty(PROP_BATCH_CONCATENATE, null);
				if (val != null && val.length() > 0)
				{
					try {
						_batchConcatenate = Boolean.parseBoolean(val);
						
					} catch (Exception e)
					{
						log.warn("Invalid configuration setting for property, " + PROP_BATCH_CONCATENATE 
								+ ".  Defaulting value to " + DEFAULT_BATCH_CONCATENATE);
					}					
				}					
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
					log.warn("Successfully initialized EventService JMSDestination Writer.");
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
			long lastPublishTime = 0;
			while (_isRunning == true)
			{				
				numEvents = _events.size();
				long timeElapsedSinceLastPublish =  System.currentTimeMillis()-lastPublishTime ;
				log.info(" Number of events :" + numEvents + ", Time elapsed since last publish: "  + timeElapsedSinceLastPublish + " ms");				
				if (numEvents > 0 && ((_batchSize <= 0) || (numEvents >= _batchSize) || (timeElapsedSinceLastPublish >= _batchInterval)))
				{
					log.info("Starting to publish events...");				
					ServiceNotify notify = null;		
                    try {
                        // Single message with multiple messages
                        if (_batchConcatenate == true)
                        {
                            StringBuilder sb = new StringBuilder(32 * numEvents);
                            for (int i = 0; i < numEvents; i++)
                            {
                                 notify = _events.remove(0);
                                 sb.append(BEG_EVENT);
                                 sb.append(notify.toString());
                                 sb.append(END_EVENT);
                            }
                            jmsWriter.writeDestination(sb.toString());
                        } // Individual messages
                        else {
                            StringBuilder sb = new StringBuilder(32);
                            for (int i = 0; i < numEvents; i++)
                            {
                                 notify = _events.remove(0);
                                 sb.append(BEG_EVENT);
                                 sb.append(notify.toString());
                                 sb.append(END_EVENT);

                                 // Send individual events
                                 jmsWriter.writeDestination(sb.toString());
                                 sb.setLength(0);
                            }
                        }
						jmsWriter.commit();
					}
					catch (Exception e)
					{
						log.error("Unable to write or commit to jms topic [" + _topicName + "]", e);
                        jmsWriter.reInitialize(topicInfo);
 					}
					lastPublishTime = System.currentTimeMillis();
				}
			
				// Sleep batch interval
				synchronized (this) {
					if (_isRunning) {
						try {
							wait(_checkInterval);
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
}
