package org.groundwork.foundation.jmx;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.groundwork.collage.CollageFactory;

/**
 * 
 * @author Vong Tran (vong.tran@gmail.com)
 *
 */

public class ConfigurationManager {
	
	private static final String CONFIG_FILE = "/usr/local/groundwork/config/foundation.properties";
	private static final String DEFAULT_JNDI_FACTORY_CLASS = "fr.dyade.aaa.jndi2.client.NamingContextFactory";
	private static final String DEFAULT_JNDI_HOST = "localhost";
	private static final String DEFAULT_JNDI_PORT = "16400";
	private static final String DEFAULT_SERVER_CONTEXT = "cf0";
	private static final String DEFAULT_QUEUE = "groundwork";
	private static final String JMX_QUEUE = "jmx-monitor";
	public static int JMX_LISTENER_PORT = 4950;
	public static String JNDI_FACTORY_CLASS = DEFAULT_JNDI_FACTORY_CLASS;
	public static String JNDI_HOST = DEFAULT_JNDI_HOST;
	public static String JNDI_PORT = DEFAULT_JNDI_PORT;
	public static String SERVER_CONTEXT = DEFAULT_SERVER_CONTEXT;
	public static String G_QUEUE = DEFAULT_QUEUE;
	public static String J_QUEUE = JMX_QUEUE;
	/**
	 * Maintenance queue -- used to shutdown service or reload configuration and
	 * other maintenance tasks
	 */
	public static String MAINTENANCE_QUEUE = "SERVICE-MAINTENANCE";
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

	
	/* Enable log for log4j */
	private Log log = LogFactory.getLog(this.getClass());

	/**
	 * CollageFactory Spring enabled API using hibernate for data access
	 */
	private CollageFactory service = null;
//	private AdapterManager adapterMgr = null;
	
	public ConfigurationManager(){
		Properties configuration = new Properties();
	    
    	try {
			FileInputStream fis = new FileInputStream(CONFIG_FILE);
			configuration.load(fis);
		} catch (Exception e) {
			log.warn("Could not load JMX configuration properties - [" + CONFIG_FILE + "]. Using defaults");
		}
		/* Default listener port */
		String propValue = configuration.getProperty(
				"jmx.listener.port", "4950").trim();
		Integer value = new Integer(propValue);
		ConfigurationManager.JMX_LISTENER_PORT = value.intValue();
		if (log.isInfoEnabled()) {
			log.info("Setting property [default.listener.port] to "
					+ value.toString());
		}
		
		propValue = configuration.getProperty("jndi.factory.initial", DEFAULT_JNDI_FACTORY_CLASS).trim();
		ConfigurationManager.JNDI_FACTORY_CLASS = propValue;
		propValue = configuration.getProperty("jndi.factory.host", DEFAULT_JNDI_HOST).trim();
		ConfigurationManager.JNDI_HOST = propValue;
		propValue = configuration.getProperty("jndi.factory.port", DEFAULT_JNDI_PORT).trim();
		ConfigurationManager.JNDI_PORT = propValue;
		propValue = configuration.getProperty("jms.server.context.id", DEFAULT_SERVER_CONTEXT).trim();
		ConfigurationManager.SERVER_CONTEXT = DEFAULT_SERVER_CONTEXT;
		propValue = configuration.getProperty("jndi.factory.port", DEFAULT_QUEUE).trim();
		ConfigurationManager.G_QUEUE = DEFAULT_QUEUE;
		propValue = configuration.getProperty("jndi.factory.port", JMX_QUEUE).trim();
		ConfigurationManager.J_QUEUE = JMX_QUEUE;
		
		/* Listener thread timeout in seconds */
		propValue = configuration.getProperty("thread.timeout.idle", "5").trim();
		value = new Integer(propValue);
		ConfigurationManager.THREAD_TIMEOUT_IDLE = value.intValue() * 1000; // Convert
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
		ConfigurationManager.THREAD_CORE_SIZE = value.intValue();
		if (log.isInfoEnabled()) {
			log.info("Setting property [thread.executor.core.pool.size] to "
							+ value.toString());
		}

		/* Request buffer size */
		propValue = configuration.getProperty("max.request.size", "700").trim();
		value = new Integer(propValue);
		ConfigurationManager.MAX_REQUEST_SIZE = value.intValue();
		if (log.isInfoEnabled()) {
			log.info("Setting property [max.request.size] to "
					+ value.toString());
		}

		/* Throttle timeout in seconds */
		propValue = configuration.getProperty("throttle.request.wait", "4").trim();
		value = new Integer(propValue);
		ConfigurationManager.THROTTLE_REQUEST_WAIT = value.intValue() * 1000;
		if (log.isInfoEnabled()) {
			log.info("Setting property [throttle.request.wait] to "
					+ value.toString());
		}

		/* Block size to read from Socket default 32k */
		propValue = configuration.getProperty("block.read.size", "32696").trim();
		value = new Integer(propValue);
		ConfigurationManager.BLOCK_READ_SIZE = value.intValue();
		if (log.isInfoEnabled()) {
			log.info("Setting property [block.read.size] to "
					+ value.toString());
		}
				
	}
	

}
