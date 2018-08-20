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
import com.groundwork.collage.exception.CollageException;
import com.groundwork.feeder.adapter.AdapterManager;
import com.groundwork.feeder.adapter.FeederBase;
import com.groundwork.feeder.adapter.impl.AdapterManagerImpl;
import com.groundwork.feeder.adapter.impl.FoundationMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.statistics.StatisticsService;
import org.groundwork.foundation.jms.JMSDestinationInfo;
import org.groundwork.foundation.jms.JMSDestinationWriter;
import org.groundwork.foundation.jms.impl.JMSDestinationInfoImpl;
import org.groundwork.foundation.jms.impl.JMSDestinationWriterImpl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/* JMS packages */

/**
 * ProcessFeederData Class for processing plugin input in XML format. The
 * request are dispatched to the correct adapter for database inserts.
 * 
 * @author <a href="mailto:rruttimann@groundworkopensource.com.com"> Roger Ruttimann</a>
 * @version $Id: ProcessFeederData.java 16952 2009-11-18 01:29:46Z rruttimann $
 */
public class ProcessFeederData {
	// String constants
	private static final String DEFAULT_JNDI_FACTORY_CLASS = "fr.dyade.aaa.jndi2.client.NamingContextFactory";
	private static final String DEFAULT_JNDI_HOST = "localhost";
	private static final String DEFAULT_JNDI_PORT = "16400";
	private static final String DEFAULT_SERVER_CONTEXT = "cf0";
	private static final String DEFAULT_QUEUE = "groundwork";

	/**
	 * Buffer of incomming requests that are serialized into the JMS persistent store. The buffer is necessary
	 * to stream data from the socket into the system without blocking while writing to JMS. It's a synchronized
	 * list that allows access from different threads (read/write)
	 */
	static List<SocketListener> socketListeners = Collections.synchronizedList(new ArrayList<SocketListener>());

	/**
	 * Maintenance queue -- used to shutdown service or reload configuration and
	 * other maintenance tasks
	 */
	public static String MAINTENANCE_QUEUE = "SERVICE-MAINTENANCE";

	/**
	 * Listener port. Default 4913. Can be overwritten by setting it in
	 * service.properties file.
	 */
	public int LISTENER_PORT = 4913;

	/**
	 * Timeout for threads (in ms) to shutdown if no requests are received.
	 * Default is 5 seconds.
	 */
	public static int THREAD_TIMEOUT_IDLE = 5000; // milliseconds

	/**
	 * Number of thread in main listener thread pool - Typically, at minimum
	 * should be number of feeders
	 */
	public static int THREAD_CORE_SIZE = 5;

	/**
	 * Number of request pending before start blocking incoming requests.
	 * Default 700
	 */
	public static int MAX_REQUEST_SIZE = 700; // number of requests

	/**
	 * Wait time in seconds if queue is full before reading more requests.
	 * Default 4 sec
	 */
	public static int THROTTLE_REQUEST_WAIT = 4000; // milliseconds

	/** Input buffer size per read. Default is 32k */
	public static int BLOCK_READ_SIZE = 32696; // 32k

	/** JMS Server Enabled - All incoming message will go through JMS Destination */
	public boolean JMS_FEEDER_ENABLED = false;

	/** JMS / JNDI Server settings */
	public JMSDestinationInfo JMS_QUEUE_INFO = null;
	
	/** Server Socket maintenance interval */
	public static int SERVER_SOCKET_MAINTENANCE_INETRVAL = 8;

	/*
	 * List holding all incoming requests.
	 */
	public static Vector<String> listRequests = new Vector<String>(50);
	
	/* Semaphore allowing connects from external only when process Queue (JMS/memory FIFO) 
	 * is up and running. Until then just block.
	 */
	public static boolean READY_TO_ACCEPT_CALLS = false;

	/* Enable log for log4j */
	private Log log = LogFactory.getLog(this.getClass());

	/**
	 * CollageFactory Spring enabled API using hibernate for data access
	 */
	private CollageFactory service = null;
	private AdapterManager adapterMgr = null;

	/** Running the socket listener and the request processor in threads */
	public FoundationListenerThread backgroundThreads[];

	/* Statistics singleton for keeping tracks of system changes */
	private StatisticsService statisticsService = null;

	/**
	 * Constructor. Initialize Adapter Framework which will be passed to the
	 * listener components such as the PortLIstener or the JMS listener
	 */
	public ProcessFeederData() {
		super();

		this.setup();
	}

	private void setup() {
		double startTime = System.currentTimeMillis();
		double currentTime = startTime;

		// Initialize API framework
		service = CollageFactory.getInstance();
		if (service != null) {
			// Initialize the SpringFramework
			service.initializeSystem();
			currentTime = System.currentTimeMillis();
			log.info("Collage Factory initialized in "
					+ (currentTime - startTime) + " ms");
			startTime = currentTime;

			// Load Adapter Manager
			this.adapterMgr = new AdapterManagerImpl(this.service);

			// Initialize
			if (this.adapterMgr != null) {
				try {
					this.adapterMgr.initializeSystem();
					currentTime = System.currentTimeMillis();
					log.info("Adapter Manager  initialized in "
							+ (currentTime - startTime) + " ms");
					startTime = currentTime;
				} catch (Exception e) {
					log.error("Error. Failed to initialize adapter for processing messages. Error"
									+ e.getMessage());
					this.adapterMgr = null;
				}
			} else {
				log.error("Error. Failed to create adapter for processing messages");
			}
		} else {
			log.error("Failed to create CollageFactory");
		}

		/*
		 * Read properties files and overwrite operation settings
		 */

		Properties configuration = service.getFoundationProperties();
		try {
			/* Default listener port */
			String propValue = configuration.getProperty(
					"default.listener.port", "4913").trim();
			Integer value = new Integer(propValue);
			this.LISTENER_PORT = value.intValue();
			if (log.isInfoEnabled()) {
				log.info("Setting property [default.listener.port] to "
						+ value.toString());
			}

			/* Listener thread timeout in seconds */
			propValue = configuration.getProperty("thread.timeout.idle", "5").trim();
			value = new Integer(propValue);
			ProcessFeederData.THREAD_TIMEOUT_IDLE = value.intValue() * 1000; // Convert
																				// to
																				// milliseconds
			if (log.isInfoEnabled()) {
				log.info("Setting property [thread.timeout.idle] to "
						+ value.toString());
			}

			/* Listener thread pool core size */
			propValue = configuration.getProperty(
					"thread.executor.core.pool.size", "5").trim();
			value = new Integer(propValue);
			ProcessFeederData.THREAD_CORE_SIZE = value.intValue();
			if (log.isInfoEnabled()) {
				log.info("Setting property [thread.executor.core.pool.size] to "
								+ value.toString());
			}

			/* Request buffer size */
			propValue = configuration.getProperty("max.request.size", "500").trim();
			value = new Integer(propValue);
			// The buffer depends on the number of threads that process sockets
			ProcessFeederData.MAX_REQUEST_SIZE = value.intValue()*ProcessFeederData.THREAD_CORE_SIZE;
			if (log.isInfoEnabled()) {
				log.info("Setting property [max.request.size] to "
						+ value.toString());
			}

			/* Throttle timeout in seconds */
			propValue = configuration.getProperty("throttle.request.wait", "1").trim();
			value = new Integer(propValue);
			ProcessFeederData.THROTTLE_REQUEST_WAIT = value.intValue() * 1000;
			if (log.isInfoEnabled()) {
				log.info("Setting property [throttle.request.wait] to "
						+ value.toString());
			}

			/* Block size to read from Socket default 32k */
			propValue = configuration.getProperty("block.read.size", "32696").trim();
			value = new Integer(propValue);
			ProcessFeederData.BLOCK_READ_SIZE = value.intValue();
			if (log.isInfoEnabled()) {
				log.info("Setting property [block.read.size] to "
						+ value.toString());
			}
			
			/* ServerSocket maintenance interval in hours. Default is 8h*/
			propValue = configuration.getProperty("server.socket.maintenance.interval", "8").trim();
			value = new Integer(propValue);
			ProcessFeederData.SERVER_SOCKET_MAINTENANCE_INETRVAL = value.intValue();
			if (log.isInfoEnabled()) {
				log.info("Setting property [server.socket.maintenance.interval] to "
						+ value.toString());
			}

			/* Enable JMS listener thread */
			propValue = configuration
					.getProperty("feeder.jms.enabled", "false").trim();
			Boolean boolVal = new Boolean(propValue);
			JMS_FEEDER_ENABLED = boolVal.booleanValue();
			if (log.isInfoEnabled()) {
				log.info("Setting property [jms.server.enabled] to "
						+ propValue);
			}

			if (JMS_FEEDER_ENABLED == true) {
				JMS_QUEUE_INFO = new JMSDestinationInfoImpl(configuration
						.getProperty("jndi.factory.initial",
								DEFAULT_JNDI_FACTORY_CLASS).trim(),
						configuration.getProperty("jndi.factory.host",
								DEFAULT_JNDI_HOST).trim(), configuration
								.getProperty("jndi.factory.port",
										DEFAULT_JNDI_PORT).trim(),
						configuration.getProperty("jms.server.context.id",
								DEFAULT_SERVER_CONTEXT).trim(), configuration
								.getProperty("feeder.jms.queue.name",
										DEFAULT_QUEUE).trim(),configuration.getProperty("jms.admin.user", JMSDestinationInfo.DEFAULT_JNDI_ADMIN_USER).trim(),configuration.getProperty("jms.admin.password", JMSDestinationInfo.DEFAULT_JNDI_ADMIN_CREDENTIALS).trim());
			}

			boolean statisticsEnabled = Boolean.parseBoolean(configuration.getProperty("statistics.enabled", "true").trim());

			currentTime = System.currentTimeMillis();
			log.info("System Properties read in " + (currentTime - startTime)
					+ " ms");

			if (statisticsEnabled) {
				startTime = currentTime;

				// Statistics module settings
				String hostStatusesCheck = configuration.getProperty(
						"statistics.hoststatus", "DOWN UNREACHABLE PENDING UP").trim();
				String serviceStatusesCheck = configuration.getProperty(
						"statistics.servicestatus",
						"CRITICAL WARNING UNKNOWN OK PENDING").trim();
				String nagiosProperties = configuration.getProperty(
						"statistics.nagios",
						"isNotificationsEnabled isEventHandlersEnabled ScheduledDowntimeDepth isChecksEnabled Acknowledged PassiveChecks")
						.trim();

				// Initialize statistic service so thtat it generates counts for
				// Statuses
				statisticsService = service.getStatisticsService();
				if (statisticsService != null) {
					try {
						// Nagios specific checks
						if (nagiosProperties != null
								&& nagiosProperties.length() < 1)
							nagiosProperties = null; // don't generate

					/*
					 * Intialize all statistics and populate structures
					 */

						// Sucessful for the initial calculation start the
						// background task
						statisticsService.startStatisticsCalculation(
								hostStatusesCheck, serviceStatusesCheck,
								nagiosProperties);

						currentTime = System.currentTimeMillis();
						log.info("Statistics. Start statistics thread  in "
								+ (currentTime - startTime) + " ms");
						startTime = currentTime;

					} catch (CollageException ce) {
						log.error("Error while initializing Foundation Statistics Module. Error:"
								+ ce);
					}
				} else {
					log.warn("Failed to initialize Statistics module.");
				}
			}

		} catch (Exception e) {
			log.warn("WARNING: Could not load service properties or processing failed. Using defaults. Error: "
							+ e);
		}
	}

	/**
	 * Cleanup of system
	 * 
	 */
	public void unInitializeSystem() {
		try {
			if (this.adapterMgr != null)
				this.adapterMgr.unInitializeSystem();

			/* Stop Statistics gathering */
			if (this.statisticsService != null)
				this.statisticsService.stopStatisticsCalculation();

			// Stop all threads
			/** Stopping listening socket, read buffer into XML Parser class, extract all messages and put into xmlRequest vector
			 * for each socket.
			 */
			log.info("Stopping main listener thread. All messages in memory will be forwarded to the persistence store.");
			try
			{
				backgroundThreads[0].unInitialize();
			}
			catch(Exception e){log.info("ProcessFeederData: error during unintialization of listenerMainThread"+e);}
			
			/*
			 * Sleep for 2 seconds to ensure that main listener thread has time to clean up
			 * TBD: use a semaphore that indicates cleanup done
			 */

			try {
				Thread.sleep(2000);
			} catch (InterruptedException ie)
			{
				log.info("Interrupted while waiting for main listener thread to clean-up. Continue.");
			}
			

			/** 
			 * Reading messages from xml request vector and write it to JMS queue
			 */
			log.info("Stopping message processing threads.");
			backgroundThreads[1].unInitialize();
			
		} catch (Exception e) {
			log.error("Uninitialize of AdapteManager or Topic server throw an exception. Error: "
							+ e);
		}
	}

	public void logConsoleMessage(String xmlMsg) {
		if (xmlMsg == null || xmlMsg.length() == 0)
			throw new IllegalArgumentException(
					"Invalid null / empty message parameter.");

		try {
			FeederBase collageAdapter = 
				(FeederBase)this.service.getAPIObject("adapter.collage_log");
			
			collageAdapter.process(this.service, new FoundationMessage(xmlMsg));
		} catch (Exception e) {
			log.error("Exception calling into CollageLog Adapter. Error:" + e);
		}
	}

	/**
	 * Thread classes for running the task in the background
	 * 
	 * listenerThread Reading the socket and distribute the messages into
	 * distribution lists
	 * 
	 * @author <a href="mailto:rruttimann@itgroundwork.com"> Roger Ruttimann</a>
	 */
	class listenerMainThread extends FoundationListenerThread implements
			RejectedExecutionHandler {
		private int listenerPort = 0;
		private ServerSocket listeningSocket = null;
		private SocketAddress localSocketAddress = null;
		/* keep track of how long server socket was running */
		private long timeServerSocketStarted = 0;
		// ===============================================================================
		// cpora -- make the executor global so that we can shutdown the executing threads
		// from a different procedure.
		// ===============================================================================
		private ThreadPoolExecutor executor = null;

		/* Flag indicating any catastrophic error in Socket communication */
		private boolean hasSocketErrorOccured = false;

		// Any child thread can override this setting which means that
		// the whole service needs to be shutdown
		public boolean serviceIsListening = true;



		public listenerMainThread() {
			super();
		}

		public listenerMainThread(int portNumber) {
			super();

			if (portNumber > 1023)
				this.listenerPort = portNumber;
		}

		/** Shutdown system * */
		public void unInitialize() {
			// ==========================================================================
			// cpora -- initially serviceIsListening = false was done during synchronization.
			// But this caused a problem, because the other threads (the excluded ones) 
			// were not able to see the change of serviceIsListening, which caused errors. 
			// ===========================================================================
			this.serviceIsListening = false;
			//===============================
		}

		public void removeListener(SocketListener listener) {
			if (log.isInfoEnabled())
				log.info("Listener.Remove child listener.ThreadID [" + listener.getThreadId() + "]");

			synchronized (this) {
				socketListeners.remove(listener);
			}
			if (log.isInfoEnabled())
				log.info("Size of socket List after removal [ " + socketListeners.size() + "] Thread ID of socket thread: " + listener.getThreadId() );
		}
		
		public void setFlagErrorInSocket()
		{
			this.hasSocketErrorOccured = true;
		}
		
		public boolean renewServerSocket()
		{
			synchronized(this)
			{
				// Reset flag indicating error in underlying socket code
				this.hasSocketErrorOccured = false;
				
				// Closing existing server socket and issue a new one
				if (listeningSocket != null)
				{
					try {
						listeningSocket.close();
					}
					catch(IOException ioe)
					{
						log.error("Exception in closing Server socket. Error: " +ioe);
					}
				}
				try
				{
					// Recreate socket
					if (this.localSocketAddress != null )
					{
						listeningSocket = new ServerSocket();
						listeningSocket.setReuseAddress(true);
						listeningSocket.setSoTimeout(1000);
						listeningSocket.bind(this.localSocketAddress);
						log.debug("Maintenance action. Re-issued a new server socket setting SO_REUSEADDR.");
						// reset time
						this.timeServerSocketStarted = System.currentTimeMillis();
					}
					else
					{
						listeningSocket = new ServerSocket(listenerPort);
						/* Set the blocking time (during accept) to 1 second */
						listeningSocket.setSoTimeout(1000);
					
						log.debug("Maintenance action. Re-issued a new server socket.");
						
						// reset time
						this.timeServerSocketStarted = System.currentTimeMillis();
						// Save Socket address so that it can be re-used in the future
						this.localSocketAddress = listeningSocket.getLocalSocketAddress();
					}
					
					// success
					return true;
				
				} 
				catch (IOException ioe)
				{
					log.error("Failed to create new server socket. No longer listening. Error: " + ioe);
					return false;
				}
			}
		}

		public void run() {

			/* Wait until processing threads signal success */
			int numAttempts = 0;
			int RETRY_TIME = 2000; /* 2 seconds */
			int MAX_WAITING_TIME = 120000; /*2 minutes */
			
			long retryTime = RETRY_TIME * numAttempts;
			while (READY_TO_ACCEPT_CALLS == false)
			{
				if (retryTime > MAX_WAITING_TIME)
				{
					// Give up fatal error...
					log.error("After two minutes backend processes not ready to receive messages. Attempt restart...");
					return;
				}
				
				try{
					log.info("Listener not ready. Wait for signal from backend....");
					sleep(RETRY_TIME);
				}catch (InterruptedException ie)
				{
					log.info("Interrupted while waiting for backend update on availability. Continue.");
				}
				
				numAttempts++;
				retryTime = RETRY_TIME * numAttempts;
			}
			
			if (log.isInfoEnabled())
				log.info("Collage Listener 2.0 started. Waiting for input...");

			// Calculate request threshold in case Foundation get unundated with
			// messages
			int numRequestThreshold = (int) ((double) ProcessFeederData.MAX_REQUEST_SIZE * 0.10);

			// Try to open a server socket on listenerPort (constructor)
			try {
				
				listeningSocket = new ServerSocket(listenerPort);
				this.localSocketAddress = listeningSocket.getLocalSocketAddress();

				/* Set the blocking time (during accept) to 1 second */
				listeningSocket.setSoTimeout(1000);
				
				/* log time Server Socket was started */
				this.timeServerSocketStarted = System.currentTimeMillis();

			} catch (IOException e) {
				log.error("Listener. Failed to create Listener: " + e);
				return;
			}

			// Create a socket object from the ServerSocket to listen and accept
			// connections.

			// For each incoming request create a new listener thread. If the
			// connection disconnects
			// the thread will exit.
			Socket serviceSocket = null;
			// =========================================================================
			// cpora -- commented out, and changed executor to be a global in this class
			// ThreadPoolExecutor executor = new ThreadPoolExecutor(
			// =========================================================================
			executor = new ThreadPoolExecutor(
					THREAD_CORE_SIZE, THREAD_CORE_SIZE * 2, 5,
					TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(
							THREAD_CORE_SIZE, true), this);

			SocketListener listener = null;
			while (this.serviceIsListening == true) {
				// We block until the request vector is at 10% of the
				// MAX_REQUEST_SIZE
				if ((ProcessFeederData.listRequests.size() > ProcessFeederData.MAX_REQUEST_SIZE)) {
					// Log Message to Console
					String xmlMsg = "<COLLAGE_LOG consolidation='SYSTEM' TextMessage='Warning:  Max requests ["
							+ ProcessFeederData.MAX_REQUEST_SIZE
							+ "] threshold reached.  Foundation is throttling socket listener.' "
							+ "MonitorStatus='WARNING' Severity='WARNING' />";
					logConsoleMessage(xmlMsg);

					if (log.isInfoEnabled())
						log.info("Request stack full("
								+ ProcessFeederData.listRequests.size()
								+ ") -- waiting for queue to reach size of ("
								+ numRequestThreshold + ")");
						// Requests are coming in in high rate. Give back end some time to process messages currently in the queue
						try {
							Thread.sleep(ProcessFeederData.THROTTLE_REQUEST_WAIT);
						} catch (Exception e) {
							log.error(e);
						}

					if (log.isInfoEnabled())
						log.info("Start accepting again after request stack was full.");
				}
				
				/* Check if any of the underlying socket signaled a catastrophic failure that might require
				 * the server socket to restart.
				 * If a thread reading data over a TCP socket receives a fatal exception it sets the hasSocketErrorOccured flag and cleans up the thread.
				 *  If that flag is set and all threads removed attempt to restart the server socket. If that fails stop the system and attempt a restart  
				 */ 
				if ( ( ((System.currentTimeMillis() - this.timeServerSocketStarted) > (SERVER_SOCKET_MAINTENANCE_INETRVAL* 3600000)) || this.hasSocketErrorOccured == true) && socketListeners.isEmpty() )
				{
					log.info("Entering ServerSocket maintenace. Service will re-create server listener socket");
					int nSocketRetryCount = 0;
					while (this.renewServerSocket() == false && nSocketRetryCount < 3)
					{
						nSocketRetryCount++;
						
						log.error("Restarting Server Socket failed. Retry in 20 seconds..");
						try {
							Thread.sleep(20000);
						} catch (Exception e) {
							log.error(e);
						}
					}
					
					/* Server Socket restart failed. Asked user to restart GWSERVICES */
					if (nSocketRetryCount == 3)
					{
						// Error renewing server socket -- attempt a restart
						this.serviceIsListening = false;
						log.error("Server Listener Socket could not be initialized. Retry failed. Restart Foundation required.");	
						
					}		
				}
				
				if (this.serviceIsListening == true) {

					try {
						/* Put up to (3 times the executor queue size) of sockets into the waiting queue */
						if (socketListeners.size()  < THREAD_CORE_SIZE*3)
						{
							serviceSocket = listeningSocket.accept();
		
							listener = new SocketListener(serviceSocket, this);
							executor.execute(listener);
							socketListeners.add(listener);
						}
						else
						{
							log.info("Socket Listener Threadpool full. Size [" + socketListeners.size() +"]. Delay accept socket.");
							try {
								Thread.sleep(THROTTLE_REQUEST_WAIT);
							} catch (Exception e) {
								log.error(e);
							}
						}
					} catch (SocketTimeoutException se) {
						log.debug("No connection during 1 second timeout.");
					} catch (IOException e) {
						log.error("Exception while creating/listening to a socket. Error: "
										+ e);
					}
				}
			}
			synchronized (this) {
				// ================================
				// cpora -- see above ^. 
				// this.serviceIsListening = false;
				// ================================
				log.info("Shutdown ListenerMainThread thread.");
				
				// Cleanup all listeners
				Iterator<SocketListener> it = socketListeners.iterator();

				// Shutdown each listener
				while (it.hasNext()) {
					// ==============================================================
					// cpora -- iterate through all the socketListeners to force them
					// to shutdown and unitialize.
					SocketListener sktListener = it.next();
					sktListener.shutdown ();
					/* TBD: SHUTDOWN calls un-initialize no need for another one */
					//sktListener.uninitialize();
					// ==============================================================
				}
				socketListeners.clear();
				
				try {
					// cpora -- then force the shutdown of all the threads in the 
					// ThreadPoolExecutor.
					executor.shutdown();
					executor.awaitTermination(30, TimeUnit.SECONDS);
					// ==========================================================
				} catch (Exception e) {
					log.error("Listener Cleanup. Failed shutting down main listening thread executor", e);
				}
				if ((listeningSocket != null) && (!listeningSocket.isClosed())) {
					try {
						listeningSocket.close();
					} catch (IOException e) {
						log.error("Unable to close listener socket on port: " + listenerPort);
					}
				}
			}
/*
			// Cleanup all listeners
			Iterator<SocketListener> it = socketListeners.iterator();

			// Shutdown each listener
			while (it.hasNext()) {
				it.next().shutdown();
			}

			socketListeners.clear();

			try {
				executor.shutdown();
				executor.awaitTermination(30, TimeUnit.SECONDS);
			} catch (Exception e) {
				log.error("Listener Cleanup. Failed shutting down main listening thread executor", e);
			}
*/
		}

		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
			log.warn("Message Executor [ listenerMainThread ] Cannot execute task b/c all threads are active and the queue is full.");

			try {

				// Note: This exception call stalls all execution - This should
				// not occur because we throttle incoming
				// requests and the executer queue size is typically the same
				// size as the number of incoming requests we allow
				Thread.sleep(20);

				// Try to execute the message again
				// Note: We could get into an endless loop if the task cannot be
				// executed.
				executor.execute(r);
			} catch (Exception e) {
				// Report an error an continue
				log.error("Collage Adapter. Exception while re-executing request Error: "
								+ e);

				throw new RejectedExecutionException(e);
			}
		}
	}

	/**
	 * processThread Distributes request to the correct adapters
	 * 
	 * @author <a href="mailto:rruttimann@itgroundwork.com"> Roger Ruttimann
	 *         </a>
	 */
	class processThread extends FoundationListenerThread {
		private FoundationDispatcher foundationDispatcher = null;
		private FoundationMessageQueue _foundationMessageQueue = null;
		private JMSDestinationInfo queueInfo = null;
		private JMSDestinationWriter jmsWriter = null;

		private boolean isListening = true;
		private boolean isInitialized = false;

		private Properties configuration = null;
		private AdapterManager adapterMgr = null;

		private static final long MIN_RETRY_TIME = 2000; // 2 seconds
		private static final long MAX_RETRY_TIME = 60000; // 1 minutes
		
		/* Enable log for log4j */
		private Log log = LogFactory.getLog(this.getClass());

		public processThread(Properties configuration, AdapterManager adapterMgr) {
			super();

			if (adapterMgr == null)
				throw new IllegalArgumentException(
						"Invalid null AdapterManager parameter.");

			_foundationMessageQueue = new FoundationMessageQueue(configuration,
					adapterMgr);
		}

		public processThread(JMSDestinationInfo queueInfo, Properties configuration,
				AdapterManager adapterMgr) {
			super();

			if (queueInfo == null)
				throw new IllegalArgumentException(
						"Invalid null queueInfo parameter.");

			if (configuration == null)
				throw new IllegalArgumentException(
						"Invalid null configuration parameter.");

			if (adapterMgr == null)
				throw new IllegalArgumentException(
						"Invalid null AdapterManager parameter.");

			this.queueInfo = queueInfo;
			jmsWriter = new JMSDestinationWriterImpl();

			// We keep this information in the case the JMS server is not
			// available at which time
			// we go directly to the FoundationMessageQueue
			this.configuration = configuration;
			this.adapterMgr = adapterMgr;
		}

		/** Shutdown system * */
		public void unInitialize() {
			this.isListening = false;
			
			log.info("Stop -- Foundation dispatcher...");
			// Un-initialize Foundation Dispatcher
			if (this.foundationDispatcher != null)
				foundationDispatcher.unInitialize();
			
			if (log.isInfoEnabled())
			{
				log.info("processThread::unInitialize -- writing requests to JMS Queue start");
			}
	
			// TBD: Put all xmlRequest into JMS queue

			int numRequests = listRequests.size();
			// ========================================
			// cpora -- changed to a thread safer method to 
			// check if the vector listRequests reaches
			// its limits.
			// for (int i = 0; i < numRequests; i++) {
			// ========================================
			while (!listRequests.isEmpty()) {
				
				String xmlRequest = listRequests.remove(0);
				try {
					if (isInitialized == true) {
						// Write message to JMS Destination
						jmsWriter.writeDestination(xmlRequest);
						jmsWriter.commit();
					}
				} catch (Exception e) {
						log.error("unInitialize(): Exception while posting request to JMS Queue ["
										+ xmlRequest + "] Error: " + e);
				}
			}
			
			if (log.isInfoEnabled())
			{
				log.info("processThread::unInitialize -- writing requests to JMS Queue done");
			}

			this.isInitialized = false;
			// Un-initialize JMS Writer
			if (jmsWriter != null)
				jmsWriter.unInitialize();
			// Un-initialize Foundation Dispatcher
			if (this.foundationDispatcher != null)
				foundationDispatcher.unInitialize();
			// Un-initialize message queue
			if (_foundationMessageQueue != null)
				_foundationMessageQueue.unInitialize();

			log.info("Shutdown processThread thread.");
		}

		public void run() {

			if (_foundationMessageQueue == null)
				runJMS();
			else
				runSocket();
		}

		/**
		 * Write message to JMS Destination
		 * 
		 */
		private void runJMS() {

			log.info("JMS Persistent store is enabled.  All messages will be forwarded to the JMS queue configured in foundation.properties");

			long lastAttempt = 0;
			int numAttempts = 0;

			while (isListening == true) {
			// Initialize JMS connection - Retry
				long retryTime = MIN_RETRY_TIME * numAttempts;

				// Try to reconnect to JMS Server if retry time has elapsed.
				// We increase time to wait
				// between retries as the number attempts increase setting
				// and constraining the time to
				// MAX_RETRY_TIME
				
				while (retryTime < MAX_RETRY_TIME && isInitialized == false){
					retryTime = MIN_RETRY_TIME * numAttempts;
					if (retryTime > MAX_RETRY_TIME)
						retryTime = MAX_RETRY_TIME;
				
					/* If the JMS queue is configured to be the default message storage make sure
					 * that a connection is setup and it never goes to the FIFO
					 * If the JMS is not available after MAX_RETRY_TIME go to the FIFO but post a warning to the system console.
					 */
					try {
						jmsWriter.initialize(queueInfo);
						isInitialized = true;
						
						// Signal back that backend is ready to receive messages
						READY_TO_ACCEPT_CALLS = true;
						
						log.info("All incoming messages will be forwarded to the JMS queue.");

						// Reset number of tries since we have successfully
						// initialized.
						numAttempts = 0;

						// Since we are now initialized and post to JMS, we
						// don't go directly to the FoundationMessageQueue
						// Note: we allow existing messages to finish
						if (_foundationMessageQueue != null) {
							_foundationMessageQueue.unInitialize();
							_foundationMessageQueue = null;	
						}

						// Setup Foundation Message Dispatcher to listen on
						// JMS Destination
						if (this.foundationDispatcher == null) {
							this.foundationDispatcher = new FoundationDispatcher(
									this.queueInfo, 
									this.configuration,
									this.adapterMgr);

							this.foundationDispatcher.start();
							log.info("Dispatcher will read messages from the JMS queue.");
							
							/* Post Message to console so that a user knows that the JMS is up and running */
							String successJMSinit = "<GENERICLOG consolidation='SYSTEM' ApplicationType='SYSTEM' MonitorServerName='localhost' Device='127.0.0.1' Severity='OK' MonitorStatus='OK' TextMessage='JMS Queue is initialized and all incoming messages are routed through the persistence store.' />";
							jmsWriter.writeDestination(successJMSinit);
							jmsWriter.commit(); 
														
						}
					} catch (Exception e) {
						isInitialized = false;
						lastAttempt = System.currentTimeMillis();
						numAttempts++;

						log.error("Failed to connect to JMS queue in ProcessThread - Number of attempts: "
										+ numAttempts + " (" + e.getMessage() + ")");
						
						try{
							sleep(MIN_RETRY_TIME);
						}catch (InterruptedException ie)
						{
							log.info("Interrupted while waiting for retry on JMS connect. Continue.");
						}
					}
				}

				try {
					// Since we are unable to connect to the JMS server we go
					// directly
					// to the foundation message queue. We will try to connect
					// to JMS after
					// processing existing messages.
					if (isInitialized == false
							&& _foundationMessageQueue == null) {
						_foundationMessageQueue = new FoundationMessageQueue(
								configuration, adapterMgr);

						// Start foundation queue
						_foundationMessageQueue.start();
						
						/* Post warning to console and log file */
						String errorJMSinit = "<GENERICLOG ApplicationType='SYSTEM' MonitorServerName='localhost' Device='127.0.0.1' Severity='WARNING' MonitorStatus='WARNING' TextMessage='JMS Queue could not be initialized. Processing incoming messages in memory and not persisting them for recovery' />";
						_foundationMessageQueue.processMessage(new FoundationMessage(errorJMSinit));
						log.warn("JMS Queue could not be initialized. Processing incoming messages in memory.");
						
						// Signal back that backend is ready to receive messages
						READY_TO_ACCEPT_CALLS = true;
					}

					/*
					 * Check if there are any messages and forward it to the
					 * adapter package
					 */
					int numRequests = listRequests.size();
					int requestBundle =0;
					String xmlRequest = null;
					for (int i = 0; i < numRequests; i++) {
						requestBundle++;
						
						xmlRequest = listRequests.remove(0);
						long startTime =0;
						try {
							if (isInitialized == true) {
								// Write message to JMS Destination
								startTime = System.currentTimeMillis();
								jmsWriter.writeDestination(xmlRequest);
								// Bundle the JMS messages before doing a commit
								if(requestBundle >= 10 || numRequests -i < 10) {
									jmsWriter.commit();
									if (log.isInfoEnabled())
										log.info("Time to commit ["+ requestBundle +"] msg to JMS[" + (System.currentTimeMillis()- startTime) +"]" );
									requestBundle=0;
								}
							} else {
								// Dispatch request to foundation message queue
								// for coordination and execution
								_foundationMessageQueue.processMessage(new FoundationMessage(
												xmlRequest));
							}
						} catch (Exception e) {
							// Report an error an continue
							if (isInitialized == true) {
								log.error("ProcessThread: Exception while posting request to JMS Queue ["
												+ xmlRequest + "] Error: " + e);

								// Add request back to the beginning of the
								// queue
								listRequests.add(0, xmlRequest);

								isInitialized = false; // Reset initialized and
														// try to re-initialize

								// Stop Foundation Message Dispatcher listening
								// on JMS Destination
								if (this.foundationDispatcher != null) {
									this.foundationDispatcher.unInitialize();
									this.foundationDispatcher = null;
								}
							} else {
								log.error("Collage Adapter. Exception while processing request ["
												+ xmlRequest + "] Error: " + e);
							}

							// Break so we try to re-initialize and Foundation
							// Message queue will be created, if necessary
							break;
						}
					}

					// Wait a second before redoing the processing
					Thread.sleep(20);
				} catch (Exception /* InterruptedException */e) {
					e.printStackTrace();
				}
			}
		}

		/**
		 * Process message directly without writing to JMS Destination
		 * 
		 */
		private void runSocket() {

			log.info("JMS Persistent store is not enabled.  Property: feeder.jms.enabled in Foundation.properties is set to false. Keeping the messages in memory FIFO");
			
			// Start foundation queue
			_foundationMessageQueue.start();
			
			// Signal back that backend is ready to receive messages
			READY_TO_ACCEPT_CALLS = true;

			while (isListening == true) {
				try {
					/*
					 * Check if there are any messages and forward it to the
					 * adapter package
					 */
					int numRequests = listRequests.size();
					for (int i = 0; i < numRequests; i++) {
						String xmlRequest = listRequests.remove(0);

						try {
							// Dispatch request to foundation message queue for
							// coordination and execution
							_foundationMessageQueue.processMessage(
									new FoundationMessage(xmlRequest));
						} catch (Exception e) {
							// Report an error an continue
							log.error("Collage Adapter. Exception while processing request ["
											+ xmlRequest + "] Error: " + e);
						}
					}

					// Wait a second before redoing the processing
					Thread.sleep(20);
				} catch (Exception /* InterruptedException */e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * startProcessing()
	 * 
	 * Creates and starts the processing and listener threads.
	 * 
	 */
	public boolean startProcessing() {
		/*
		 * The adapter framework should be up and running.
		 */
		if (this.adapterMgr != null
				&& this.adapterMgr.getIsAdapterLoaded() == true) {
			backgroundThreads = new FoundationListenerThread[2];

			backgroundThreads[0] = new listenerMainThread(this.LISTENER_PORT);
			backgroundThreads[0].start();

			if (JMS_FEEDER_ENABLED) {
				backgroundThreads[1] = new processThread(JMS_QUEUE_INFO,
						this.service.getFoundationProperties(), this.adapterMgr);
				backgroundThreads[1].start();
			} else {
				backgroundThreads[1] = new processThread(this.service.getFoundationProperties(), 
														 this.adapterMgr);
				backgroundThreads[1].start();
			}

			// Success
			return true;
		} else {
			// Failed
			log.error("Adapters not loaded. System can't accept messages. Shutdown system.");
			return false;
		}
	}
}
