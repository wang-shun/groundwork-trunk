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

package com.groundwork.feeder.adapter.impl;

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.exception.CollageException;
import com.groundwork.feeder.adapter.AdapterManager;
import com.groundwork.feeder.adapter.FeederBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

/**
 * AdapterManagerImpl
 * @author Roger Ruttimann rruttimann@itgroundwork.com
 * Managmenet class for Adapters needed by the collage feeder framework.
 * The list of adapters to load and initialize are read the from adapter.properties
 * file residing in the same directory as the executing application.
 * 
 * The adapter calsses are defined in Spring assemblies that are packaged with
 * each adapter jar
 * 
 * # Spring assemblies
 *nb.assemblies = 2
 *
 * # Nagios adapters and the name of the Property Bean
 * adapter.assembly1 = META-INF/assembly-adapter-nagios.xml
 * adapter.properties.assembly1 = NagiosAdapterProperties
 *
 * # Sysadmin Beans
 * adapter.assembly2 = META-INF/assembly-adapter-system.xml
 * adapter.properties.assembly2 = SystemAdapterProperties
 *
 * 
 * # Enable if the messages should just be forwarded to the message queue adapter
 * # The queue adapter needs to be configured correctly. If the enable.queue is true
 * # the adapter settings above will be ignored.
 * 
 * enable.queue = false
 * queue.adapter = com.groundwork.feeder.adapter.impl.JoramAdapter
 * 
 */

public class AdapterManagerImpl implements AdapterManager 
{
	// String Constants
	private static final String ADAPTER_BEAN_PREFIX = "adapter.";
	
	/**
	 * Assemblies for Collage Hibernate connection
	 */
	static final String COMMON_ASSEMBLY = "META-INF/common-model-assembly.xml";
	static final String ADMIN_ASSEMBLY = "META-INF/admin-api-assembly.xml";
	static final String BIZ_ASSEMBLY = "META-INF/biz-assembly.xml";
	static final String QUERY_ASSEMBLY = "META-INF/query-api-assembly.xml";
	
	
	/**List of PropertyBeans for the different adapters */
    private Vector<String> assemblyPropertiesBeanID = new Vector<String>(5);
    
    /** Enable logging */
    private Log log = LogFactory.getLog(this.getClass());
    
    /** MessageQueue adapter */
    private FeederBase messageQueue = null;
    
    // Collage framework to communicate with Data Store
    // It will be initialized if the message queue is not enabled.
    private CollageFactory service = null;
    
    // State
    private boolean isAdapterAvailable = false;
    
    // Properties
    private String enableQueue = "false";            
    
    /**
     * Constructor reads the adapter.properties file and
     * initializes the adapters
     *
     */
    public AdapterManagerImpl(CollageFactory factory) {
        super();
        
        this.setup(factory);
    }
    
    /*
     * Initialize system at startup
     */
    private void setup(CollageFactory factory)
    {   
        this.service = factory;
        
        // Load Common Assembly - It is used to load foundation properties
        service.loadSpringAssembly(COMMON_ASSEMBLY);
        
        // Read the properties file
        Properties configuration = this.service.getFoundationProperties();
        
        // Check first if all the messages should be send to a message queue
        this.enableQueue = configuration.getProperty("enable.queue", "false").trim();
        
        Class classAdapter = null;
        
        if (this.enableQueue.compareToIgnoreCase("true") == 0)
        {
        	// Load Message Queue adapter
        	String messageQueueAdapter = configuration.getProperty("queue.adapter", "com.groundwork.feeder.adapter.impl.JoramAdapter").trim();
        	try
        	{
        		classAdapter = Class.forName(messageQueueAdapter);
        		
        		// Add it to the supported class list
        		this.messageQueue =  (FeederBase)classAdapter.newInstance();
        		this.isAdapterAvailable = true;
        		
        		if (this.messageQueue != null)
        		{
        			log.info("MessageQueue configurred as default adapter. All requests will be passed to this adapter");
        		}
        		
        	}
        	catch (Exception e)
        	{
        		log.error("Error: Failed to load class for Message queue [" + messageQueueAdapter+ "]. Error" +e );
        		return;
        	}
        	
        }
        else
        {
        	// Read and initialize adapters listed in the properties file
         	String numberOfAssemblies = configuration.getProperty("nb.assemblies", "0").trim();
        	
        	Integer value = new Integer(numberOfAssemblies);
    		int nbOfAssemblies = value.intValue();
    		
    		if ( nbOfAssemblies <= 0)
    		{
    			// No adapters defined
    			log.error("Error: nb.assemblies property is not set or set to 0. Make sure the value is set correctly in adapter.properties file.");
    			return;
    		}
    		
    		/* Load assemblies */
    		for (int ii=1; ii <= nbOfAssemblies; ii++)
    		{
            	/*
            	 * Read assemblies and let Spring handle the bean creation
            	 * instead of creating the classes directly
            	 */
    			String assemblyName = "adapter.assembly" + Integer.toString(ii);
    			String assemblyFile = configuration.getProperty(assemblyName, "").trim();
    			log.info("Assembly Name for adapter.assembly" + ii + " is " + assemblyFile);
    			

    			// Load the adapter bean
    			try
    			{
    				service.loadSpringAssembly(assemblyFile);
    				
    				/*
        			 * Load name of Properties Bean for this package. The property bean contains
        			 * the name of the adapter classes that have an initialize method.
        			 */
        			String assemblyPropertyBean = "adapter.properties.assembly" + Integer.toString(ii);
        			String assemblyPropertyBeanID = configuration.getProperty(assemblyPropertyBean, "").trim();
        			
        			if (assemblyPropertyBeanID != null && assemblyPropertyBeanID.length() > 0)
        			{
        				if (log.isInfoEnabled())
        					log.info("PropertyBean for adapter assembly  [" + assemblyName + "] defined as [" + assemblyPropertyBeanID + "]");
        			
        				this.assemblyPropertiesBeanID.add(assemblyPropertyBeanID);
        				
        				// Adapters are available -- ready to accept messages
        				this.isAdapterAvailable = true;
        			}
    			}
    			catch (Exception e)
    			{
    				log.error("Error: Failed to load assembly [" + assemblyFile + "] identified by property [" + assemblyName +"]. Error " + e );
    			}
    		} 
    		
    		/* If we have adapters loaded make sure that the Collage Hibernate assemblies
    		 * are loaded. If we use the message queue the assemblies won't be loaded.
    		 */
    		if (this.isAdapterAvailable == true)
    		{    			
    			service.loadSpringAssembly(ADMIN_ASSEMBLY);
                service.loadSpringAssembly(BIZ_ASSEMBLY);
    		}
    		else
    		{
    			log.warn("Adapter Manager: No adapters available. Collage Spring assemblies not loaded!");
    		}
        }
    }


	public void process(String adapterName, FoundationMessage message) throws CollageException
	{		
		// Any adapters available to process or did initialization fall
		if (isAdapterAvailable == false)
		{
			log.error("No adapter loaded. Can't process messages. Please make sure that your system is initialized properly and the adapter.properties file is correct.");
			throw new CollageException("No adapters loaded. Can't process messages. Please check adapter.properties.");
		}
	
		if (message == null)
		{
			throw new IllegalArgumentException("Invalid null / empty FoundationMessage");			
		}
		
		/*
		 * Check if the message queue was initialized and forward all messages to the message queue
		 */
		if (this.messageQueue != null)
		{
			try
			{
				this.messageQueue.process(this.service, message);
			}
			catch(Exception e)
			{
				log.error("Exception calling into MessageQueue adapter. Error: " + e);
			}
			
			return;
		}
		
		if (adapterName == null || adapterName.length() == 0)
		{
			throw new IllegalArgumentException("Invalid null / empty adapter name parameter.");
		}
		
		/*
		 * Message queue not enabled. Pass the message to the specific adapter by doing a lookup
		 * in the spring assemblies.
		 */
		
		String beanName = ADAPTER_BEAN_PREFIX + adapterName.toLowerCase();		
		
		if (log.isDebugEnabled())
			log.debug("Requested adapter [" + beanName + "]");
		
		FeederBase adapter = null;
		try
		{
			 adapter = (FeederBase)this.service.getAPIObject(beanName);
		}
		catch(Exception nbd)
		{
			// Adatper is not supported -- Log a message in the EventLog
        	log.warn("Requested adapter Bean [" + beanName + "] is not supported. Make sure that an adapter for this type exists");
            
            String xmlMsg = "<COLLAGE_LOG TextMessage='Requested adapter Bean " +beanName + " is not supported by this version. Please ask your system admin to update the system so that you can feed data into the Collage system' />" ;
            try
            {
                adapter = (FeederBase)this.service.getAPIObject("adapter.collage_log");
            	adapter.process(this.service, new FoundationMessage(xmlMsg));
            }
	    	catch (Exception e)
	    	{
	    		log.error("Exception calling into CollageLog Adapter. Error:" + e);
	    	}
	    	
	    	return;
		}
		
		// Found an adapter. Forwarding the request...
		if (log.isInfoEnabled())
			log.info("Found adapter bean for message type [" + beanName + "]");
		
		try
        {		
			// Forward message to adapter
        	adapter.process(this.service, message );        	
        }
        catch (Exception ce)
        {
        	log.error("Exception Processing Message: " + message, ce);
        	
        	// Error -- report it into the Log Message table
        	String xmlMsg = "<COLLAGE_LOG TextMessage=\"Error in Adapter [" + adapterName + "] Error: " + ce + "\" />" ;
        	            
        	try
        	{
        		FeederBase collageAdapter = (FeederBase)this.service.getAPIObject("adapter.collage_log");
        		collageAdapter.process(this.service, new FoundationMessage(xmlMsg));
        	}
        	catch (Exception e)
        	{
        		log.error("Exception calling into CollageLog Adapter. Error:" + e);
        	}
        	
        	throw new CollageException(ce);
        }
	}

	public void initializeSystem() {
		//	Initialize the System components
		
		if (this.messageQueue != null )
		{
			try
			{
				this.messageQueue.initialize();
				log.info("Initialized MessageQueue adapter from AdapterManager");
			}
			catch(Exception e)
			{
				log.error("Failed to initialized MessageQueue adapter from AdapterManager. Error: " + e);
			}
			
			return;
		}
		else
		{
			// Initialize every adapter defined in the PropertyBean
			Enumeration<String> enumPropertyBeans = this.assemblyPropertiesBeanID.elements();
			while (enumPropertyBeans.hasMoreElements())
			{
				String name = (String)enumPropertyBeans.nextElement();
				
				if (name != null && name.length() >0)
				{
					Properties props = (Properties)this.service.getAPIObject(name);
					if (props != null)
					{
						if (log.isInfoEnabled())
							log.info("Found PropertyBean [" + name +"]. Start inspecting properties.");
						
						Enumeration<Object> propEnum = props.elements();
						while (propEnum.hasMoreElements())
						{
							String adapterName = (String)propEnum.nextElement();
							
							if (log.isInfoEnabled())
								log.info("Found Property [" + adapterName + "]");
							
							try 
							{
								FeederBase adapter = (FeederBase)this.service.getAPIObject(adapterName);
								
								if (adapter != null)
								{
									if (log.isInfoEnabled())
										log.info("Initializing adapter ["+adapterName+"] defined by PropertyBean [" + name +"]");
									
									adapter.initialize();
								}
								else
								{
									log.warn("Adapter [" + adapterName + "] referenced in the PropertyBean ["+ name +"] is not defined in spring assembly");
								}
							}
							catch(Exception e)
							{
								log.error("Exception while initializing bean ["+ adapterName + "] Error:" +e);
							}
						}
					}
				}
			}
		}
	}

	public void unInitializeSystem() {
		service.unloadSpringAssembly();
	}
	
	/*
	 *  (non-Javadoc)
	 * @see com.groundwork.feeder.adapter.AdapterManager#getIsAdapterLoaded()
	 */
	public boolean getIsAdapterLoaded()
	{
		return this.isAdapterAvailable;
	}

}
