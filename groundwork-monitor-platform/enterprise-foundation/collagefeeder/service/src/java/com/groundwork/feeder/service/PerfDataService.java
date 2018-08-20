/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2008  GroundWork Open Source Solutions info@groundworkopensource.com

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
package com.groundwork.feeder.service;

import com.groundwork.collage.CollageFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.jms.FoundationJMSException;
import org.groundwork.foundation.jms.JMSDestinationInfo;
import org.groundwork.foundation.jms.JMSDestinationReader;
import org.groundwork.foundation.jms.impl.JMSDestinationInfoImpl;
import org.groundwork.foundation.jms.impl.JMSDestinationReaderImpl;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.SECONDS;

/* JMS packages */

/**
 * PerfDataService Class for processing perf data from the JMS Queue and write
 * it to the file system
 * 
 * @author <a href="mailto:ashanmugam@groundworkopensource.com.com">Arul
 *         Shanmugam</a>
 * @version $Id: PerfDataService.java 2012-08-88 01:29:46Z ashanmugam $
 */
public class PerfDataService {
	// String constants
	private static final String DEFAULT_JNDI_FACTORY_CLASS = "fr.dyade.aaa.jndi2.client.NamingContextFactory";
	private static final String DEFAULT_JNDI_HOST = "localhost";
	private static final String DEFAULT_JNDI_PORT = "16400";
    private static final String DEFAULT_SERVER_CONTEXT = "jms/RemoteConnectionFactory";
	private static final String DEFAULT_QUEUE = "groundwork";
    private static final String DEFAULT_WRITERS = "com.groundwork.feeder.service.RRDPerfDataWriter";
	private int runInterval = 30; // 30 secs

	/** JMS / JNDI Server settings */
	private JMSDestinationInfo jmsQueueInfo = null;
    private JMSDestinationReader jmsReader = null;

	/* Enable log for log4j */
	private Log log = LogFactory.getLog(this.getClass());

	/**
	 * CollageFactory Spring enabled API using hibernate for data access
	 */
	private CollageFactory service = null;


	// One thread is good enough for now(VEMA)
	private final ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(1);

	private final List<PerfDataWriter> writers = new ArrayList<PerfDataWriter>();

	/**
	 * Constructor for the service. Initialize all stuff here
     *
     * @param queueProperty JMS queue name property name
     * @param runInterval JMS queue consumer interval
     * @param writersProperty perf data writers property
     */
	public PerfDataService(String queueProperty, int runInterval, String writersProperty) {

		service = CollageFactory.getInstance();
		/*
		 * Read properties files and overwrite operation settings
		 */

		Properties configuration = service.getFoundationProperties();
		/* Default perf data queue name */
		String perfDataQueue = configuration.getProperty(
				queueProperty, DEFAULT_QUEUE).trim();
        /* Default perf data writers */
        String perfDataWriters = configuration.getProperty(
                writersProperty, DEFAULT_WRITERS).trim();
		try {

			jmsQueueInfo = new JMSDestinationInfoImpl(configuration
					.getProperty("jndi.factory.initial",
							DEFAULT_JNDI_FACTORY_CLASS).trim(),
					configuration.getProperty("jndi.factory.host",
							DEFAULT_JNDI_HOST).trim(),
					configuration.getProperty("jndi.factory.port",
							DEFAULT_JNDI_PORT).trim(), configuration
							.getProperty("jms.server.context.id",
									DEFAULT_SERVER_CONTEXT).trim(),
					perfDataQueue,configuration.getProperty("jms.admin.user", JMSDestinationInfo.DEFAULT_JNDI_ADMIN_USER).trim(),configuration.getProperty("jms.admin.password", JMSDestinationInfo.DEFAULT_JNDI_ADMIN_CREDENTIALS).trim());

			if ((perfDataWriters != null) && !perfDataWriters.isEmpty()) {
                String [] perfDataWriterClassNames = perfDataWriters.split("\\s*,\\s*");
                for (String perfDataWriterClassName : perfDataWriterClassNames) {
                    try {
                        writers.add((PerfDataWriter) Class.forName(perfDataWriterClassName).newInstance());
                    } catch (Exception e) {
                        log.error("Could not create PerfDataWriter "+perfDataWriterClassName+" instance: " + e);
                    }
                }
            }


		} catch (Exception e) {
			log.warn("WARNING: Could not load service properties or processing failed. Using defaults. Error: "
					+ e);
		}
		
		this.runInterval = runInterval;

	}

	/**
	 * Starts the data processing.
	 */
	public void start() {
		final Runnable dataProcessor = new Runnable() {
			public void run() {
				log.info("PerfDataWriter started...");
				double startTime = System.currentTimeMillis();
				double currentTime = startTime;

				try {
					// Use client acknowledge mechanism here
                    if (jmsReader == null) {
                        log.info("Initializing perf queue reader..."  );
                        try {
                            jmsReader = new JMSDestinationReaderImpl();
                            jmsReader.initialize(jmsQueueInfo, true,
                                    Session.CLIENT_ACKNOWLEDGE, null);
                        }
                        catch (Exception e) {
                            log.error("Failed to initialize JMS Reader ", e);
                            if (jmsReader != null)
                                jmsReader.unInitialize();
                            jmsReader = null;
                        }
                    }
                    if (jmsReader != null) {
                        log.info("Reading messages from the perf_data queue...");
                        // Read all messages here
                        Map<String, List<String>> messages = new HashMap<>();
                        while (true) {
                            Message msg = jmsReader.readMsgNoWait();
                            if (msg instanceof TextMessage) {
                                String str = ((TextMessage) msg).getText();
                                String appType = msg.getStringProperty("appType");
								if (appType != null) {
									List<String> messageList = messages.get(appType);
									if (messageList == null) {
										messageList = new LinkedList<String>();
										messages.put(appType, messageList);
									}
									messageList.add(str);
								}
								else {
									log.error("Received message with no app type: " + ((str == null) ? "(no message)" : str));
								}
                                msg.acknowledge();
                            } else
                                break;
                        } // end while

                        // Write to the file if there any messages
                        if (messages.size() > 0) {
                            for (Map.Entry entry : messages.entrySet()) {
                                List<String> messageList = (List<String>)entry.getValue();
								String appType = (String)entry.getKey();
                                for (PerfDataWriter writer : writers) {
                                    try {
                                        writer.writeMessages(messageList, appType);
                                    } catch (Exception e) {
                                        log.error("Error writing perf data messages for "+writer.getClass().getName()+": " + e);
                                    }
                                }
                            }
                        } // end if
                    }
				} catch (FoundationJMSException fje) {
					log.error("Error initializing/processing reader: " + fje);
				} catch (JMSException fje) {
					log.error("Error processing perf data from queue: " + fje);
				} finally {
					if (jmsReader != null) {
						jmsReader.commit();
					} // end if
				}

				currentTime = System.currentTimeMillis();
				log.info("PerfDataWriter completed in  " + (currentTime - startTime) + " ms");
				startTime = currentTime;
			}
		};
		// Initial delay is 1 secs and runs every 30 secs
		final ScheduledFuture<?> dataHandle = scheduler.scheduleAtFixedRate(
				dataProcessor, 1, this.runInterval, SECONDS);

	}

	/**
	 * Shuts down the processing gracefully.
	 */
	public void shutdown() {
		scheduler.shutdown();
	}

}
