package org.groundwork.foundation.jmx;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.statistics.StatisticsService;
import org.groundwork.foundation.jms.JMSDestinationInfo;
import org.groundwork.foundation.jms.JMSDestinationWriter;
import org.groundwork.foundation.jms.impl.JMSDestinationInfoImpl;
import org.groundwork.foundation.jms.impl.JMSDestinationWriterImpl;

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.exception.CollageException;

public class IncommingMessage extends IncommingMessageListenerThread{
	// String constants
//	private static final String DEFAULT_JNDI_FACTORY_CLASS = "fr.dyade.aaa.jndi2.client.NamingContextFactory";
//	private static final String DEFAULT_JNDI_HOST = "localhost";
//	private static final String DEFAULT_JNDI_PORT = "16400";
//	private static final String DEFAULT_SERVER_CONTEXT = "cf0";
//	private static final String DEFAULT_QUEUE = "groundwork";
//	private static final String JMX_QUEUE = "jmx-monitor";
	
	private ConnectionFactory	cnxF = null;
    private Queue				dest = null;
    private Connection			cnx = null;
//	/**
//	 * Listener port. Default 4913. Can be overwritten by setting it in
//	 * service.properties file.
//	 */
//	public int LISTENER_PORT = 4913;
//	
//	public int JMX_LISTENER_PORT = 4950;
	
	/*
	 * List holding all incoming requests.
	 */
	public static Vector<String> listRequests = new Vector<String>(50);

	/** JMS / JNDI Server settings */
	public JMSDestinationInfo JMX_QUEUE_INFO = null;
	public JMSDestinationInfo JMS_QUEUE_INFO = null;
	/** Running the socket listener and the request processor in threads */
	public IncommingMessageListenerThread backgroundThreads[];

	/* Enable log for log4j */
	private Log log = LogFactory.getLog(this.getClass());
	processThread p;
	public IncommingMessage(){
		super();
		this.setup();
	}
	
	private void setup() {
		ConfigurationManager cm = new ConfigurationManager();
	}

	/**
	 * Cleanup of system
	 * 
	 */
	public void unInitializeSystem() {
		try {
			// Stop all threads
			backgroundThreads[0].unInitialize();
		} catch (Exception e) {
			log.error("Uninitialize of ImcommingMessage or Topic server throw an exception. Error: "
							+ e);
		}
	}

	public void unInitialize() {
		try {
			// Stop all threads
			backgroundThreads[0].unInitialize();
			backgroundThreads[1].unInitialize();
			backgroundThreads[2].unInitialize();
		} catch (Exception e) {
			log.error("Uninitialize of ImcommingMessage or Topic server throw an exception. Error: "
							+ e);
		}
	}

	/**
	 * processThread Distributes request to the correct adapters
	 * 
	 * @author <a href="mailto:rruttimann@itgroundwork.com"> Roger Ruttimann
	 *         </a>
	 */
	class processThread extends IncommingMessageListenerThread{
		private JMSDestinationWriter jmsProducer = null;
		private boolean isSending = true;
		private boolean isReading = true;
		private boolean isJMSProducerInitialized = false;
		private boolean isJMSConsumerInitialized = false;
	    private MsgProducer producer;
		private static final long MIN_RETRY_TIME = 3000; // 30 seconds
		private static final long MAX_RETRY_TIME = 36000000; // 1 Hour
		private static final long RETRY_TIMEOUT = 3000; // 30 seconds
		private IncommingMessageProcessing imp;
		public processThread(){
			super();
		}
		public void run(){
			MessageConsumer consumer = null;
			Session session = null;
			Message msg = null;
			
			while (isReading)
			{
				// Try to connect to JMS server and initialize
				while (isJMSConsumerInitialized == false)
				{							
					isJMSConsumerInitialized = initializeJMSConsumer();
					
					if (isJMSConsumerInitialized == false)
					{
						// Try again in RETRY_TIMEOUT seconds
						try {
							Thread.sleep(RETRY_TIMEOUT);
						}
						catch (Exception ex)
						{
							log.error(ex);
						}						
					}
				}
				
				try {
					// Create a session for each message b/c each message is atomic
					// Message will be acknowledged after processing has been completed.					
					session = cnx.createSession(false, Session.CLIENT_ACKNOWLEDGE);
					consumer = session.createConsumer(dest);								
					msg = consumer.receive();
				}
				catch (Exception e)
				{
					// Reset 
					msg = null;
					isJMSConsumerInitialized = false; // Re-initialize connection

					log.error("IM - Unable to read JMX JMS Queue.  JMS Server may be down.  Will re-try connection.", e);
				}
				finally {			
					try {
						if (consumer != null)
							consumer.close();
					}
					catch (Exception e)
					{
						log.error("IM - Error closing consumer.", e);								
					}
				}
				
				if (msg == null)
					continue;
						
				// Currently, only supporting TextMessage
				if ((msg instanceof TextMessage) == false)
				{
					log.error("IM - Currently only supporting text messages.");
					
					try {
						if (msg != null)
						{
							log.error("IM - Invalid message type - " + msg.getJMSType());
							
							msg.acknowledge(); // Make sure message is off the queue							
						}
						
						if (session != null)
							session.close();
					}
					catch (Exception e)
					{
						log.error(e);
					}
					
					continue;
				}		
				
				// Add message to incoming queue
				try 
				{			
					if (log.isInfoEnabled())
					{
						log.info("IM - Incoming JMS Message about to be processed - [" 
								+ ((TextMessage)msg).getText()
								+ "]");
					}
//					System.out.println("IM - Incoming JMS Message about to be processed - [" 
//							+ ((TextMessage)msg).getText()
//							+ "]");
					
					String xmlMessage = ((TextMessage)msg).getText();
					XMLProcessing.addHosts(xmlMessage);
					XMLProcessing.addServices(xmlMessage);
					String fMessage = XMLProcessing.filterMessage(xmlMessage);
					producer = new MsgProducer(fMessage);
					producer.start();
					producer.join();
					producer.unInitialize();
					
					//Thread.sleep(60);
				}
				catch (Exception e)
				{
					try {
						if (msg != null)
						{
							msg.acknowledge(); // Make sure message is off the queue							
						}
						
						if (session != null)
							session.close();
					}
					catch (Exception ex)
					{
						log.error(ex);
					}
					
					log.error("IM - Error placing message on queue message.", e);
				}						
			}
			
		}
		
		public void unInitialize(){
			isReading = false;
			isSending = false;
			
			log.warn("Shutdown listener in IncommingMessageListener");
		}
		

		public boolean initializeJMSConsumer(){
			Hashtable<String, String> htJndiProperties = new Hashtable<String, String>(3);
			htJndiProperties.put("java.naming.factory.initial", ConfigurationManager.JNDI_FACTORY_CLASS);
			htJndiProperties.put("java.naming.factory.host", ConfigurationManager.JNDI_HOST);
			htJndiProperties.put("java.naming.factory.port", ConfigurationManager.JNDI_PORT);
			
			Context ictx = null;
			
			try 
			{
				ictx = new InitialContext(htJndiProperties);
		        cnxF = (ConnectionFactory) ictx.lookup(ConfigurationManager.SERVER_CONTEXT);
		        dest = (Queue) ictx.lookup(ConfigurationManager.J_QUEUE);
		        ictx.close();
		        
		        cnx = cnxF.createConnection();
		        log.warn("IM - Successful connect to JMX message queue. Wait for input ...");
		        //System.out.println("IM - Successful connect to JMX message queue. Wait for input ...");
		        cnx.start();			
			}
			catch (Exception e)
			{
				log.error("Error initializing connection to JMS server when reading message from jmx-monitor queue.", e);
				return false;
			}
			return true;
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
	class listenerMainThread extends IncommingMessageListenerThread implements
			RejectedExecutionHandler {
		private int listenerPort = 0;
		private ServerSocket listeningSocket = null;

		// Any child thread can override this setting which means that
		// the whole service needs to be shutdown
		public boolean serviceIsListening = true;

		// List of child threads
		private List<SocketListener> socketListeners = new ArrayList<SocketListener>();

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
			synchronized (this) {
				this.serviceIsListening = false;
				log.info("Shutdown ListenerMainThread thread in IncommingMessage.");
			}
		}

		public void removeListener(SocketListener listener) {
			if (log.isInfoEnabled())
				log.info("Listener.Remove child listener.");

			synchronized (this) {
				this.socketListeners.remove(listener);
			}
		}

		public void run() {
			System.out.println("JMX Configuration Listener 1.1 started. Waiting for input...\n");

			if (log.isInfoEnabled())
				log.info("JMX Configuration Listener 1.1 started. Waiting for input...\n");

			// Calculate request threshold in case Foundation get unundated with
			// messages
			int numRequestThreshold = (int) ((double) ConfigurationManager.MAX_REQUEST_SIZE * 0.10);

			// Try to open a server socket on listenerPort (constructor)
			try {
				listeningSocket = new ServerSocket(listenerPort);

				/* Set the blocking time (during accept) to 1 second */
				listeningSocket.setSoTimeout(1000);

			} catch (IOException e) {
				log.error("JMX Configuration Listener. Failed to create Listener: " + e);
				return;
			}

			// Create a socket object from the ServerSocket to listen and accept
			// connections.

			// For each incoming request create a new listener thread. If the
			// connection disconnects
			// the thread will exit.
			Socket serviceSocket = null;

			// Thread Pool
			ThreadPoolExecutor executor = new ThreadPoolExecutor(
					ConfigurationManager.THREAD_CORE_SIZE, ConfigurationManager.THREAD_CORE_SIZE * 2, 5,
					TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(
							ConfigurationManager.THREAD_CORE_SIZE, true), this);

			SocketListener listener = null;
			while (this.serviceIsListening == true) {
				// We block until the request vector is at 10% of the
				// MAX_REQUEST_SIZE
				if ((IncommingMessage.listRequests.size() > ConfigurationManager.MAX_REQUEST_SIZE)) {
					// Log Message to Console
					String xmlMsg = "<COLLAGE_LOG consolidation='SYSTEM' TextMessage='Warning:  Max requests ["
							+ ConfigurationManager.MAX_REQUEST_SIZE
							+ "] threshold reached.  Foundation is throttling socket listener.' "
							+ "MonitorStatus='WARNING' Severity='WARNING' />";
//					logConsoleMessage(xmlMsg);

					if (log.isInfoEnabled())
						log.info("Request stack full("
								+ IncommingMessage.listRequests.size()
								+ ") -- waiting for queue to reach size of ("
								+ numRequestThreshold + ")");

					while ((IncommingMessage.listRequests.size() > 0)
							&& (IncommingMessage.listRequests.size() > numRequestThreshold)) {
						try {
							Thread.sleep(ConfigurationManager.THROTTLE_REQUEST_WAIT);
						} catch (Exception e) {
							log.error(e);
						}
					}

					if (log.isInfoEnabled())
						log.info("Start accepting again after request stack was full.");
				}

				try {
					serviceSocket = listeningSocket.accept();

					listener = new SocketListener(serviceSocket, this);
					executor.execute(listener);
					socketListeners.add(listener);
				} catch (SocketTimeoutException se) {
					log.debug("No connection during 1 second timeout.");
				} catch (IOException e) {
					log.error("Exception while creating/listening to a socket. Error: "
									+ e);
				}
			}

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
	 * startProcessing()
	 * 
	 * Creates and starts the processing and listener threads.
	 * 
	 */
	public void run(){
		try{
			backgroundThreads = new IncommingMessageListenerThread[3];
			
			
			backgroundThreads[0] = new processThread();
			backgroundThreads[0].start();
			backgroundThreads[1] = new listenerMainThread(ConfigurationManager.JMX_LISTENER_PORT);
			backgroundThreads[1].start();
			backgroundThreads[2] = new QueryMBEAN();
			backgroundThreads[2].start();
			backgroundThreads[2].join();

		}catch(Exception e){
			log.error(e);
		}
	}

}
