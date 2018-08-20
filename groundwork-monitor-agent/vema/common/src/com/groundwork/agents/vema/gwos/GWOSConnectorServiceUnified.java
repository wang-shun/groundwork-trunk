/*
 * Copyright 2012 GroundWork , Inc. ("GroundWork") 
 * All rights reserved. 
 */
package com.groundwork.agents.vema.gwos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.groundwork.agents.vema.api.GWOSEntity;
import com.groundwork.agents.vema.gwos.GWOSLogMessage;
import com.groundwork.agents.vema.api.VemaConstants;
import com.groundwork.agents.vema.base.VemaBaseHost;
import com.groundwork.agents.vema.base.VemaBaseMetric;
import com.groundwork.agents.vema.base.VemaBaseVM;
import com.groundwork.agents.vema.configuration.MonitorAgentConfigXMLToBean;
import com.groundwork.agents.vema.configuration.VEMAGwosConfiguration;

/**
 * @author rruttimann@gwos.com Created: Jul 18, 2012
 */
public class GWOSConnectorServiceUnified
{
	private static org.apache.log4j.Logger log = Logger.getLogger(GWOSConnectorServiceUnified.class);

	private String	            INITIAL_STATE = "PENDING";
	private String	NOTIFICATIONTYPE_PROBLEM  = "PROBLEM";
	private String	NOTIFICATIONTYPE_RECOVERY = "RECOVERY";

	private String               GwosHostName = null; // "jaun.groundwork.groundworkopensource.com";
	private int                      GwosPort = 0;    // 4913;
	private String       managementServerType = null;
	private String             hypervisorType = null;
	private String            applicationType = null;
	private int                  intervalTime;
	
	private String vemaMonitorProfileFilename = null;
	private String         gwosConfigFilename = null;

	private static String    JSONFORMAT_PARAM = "dataInJSONFormat";

    // --------------------------------------------------------------------
	// REST End point for bulk event generation
    // --------------------------------------------------------------------
	private static String END_POINT_BULK_EVENT_GEN 
        = "/foundation-webapp/restwebservices/eventGeneration/generateBulkEvents";

    // --------------------------------------------------------------------
	// REST end point for performance data post
    // --------------------------------------------------------------------
	private static String END_POINT_PERF_DATA 
        = "/foundation-webapp/restwebservices/performanceData/post";

    // --------------------------------------------------------------------
	// REST end point for NOMA host notify
    // --------------------------------------------------------------------
	private static String END_POINT_NOMA_HOST_NOTIFY 
        = "/foundation-webapp/restwebservices/noma/notifyHost";

    // --------------------------------------------------------------------
	// REST end point for NOMA service notify
    // --------------------------------------------------------------------
	private static String END_POINT_NOMA_SERVICE_NOTIFY 
        = "/foundation-webapp/restwebservices/noma/notifyService";

	private VEMAGwosConfiguration vGwosConfiguration = null;

	public VEMAGwosConfiguration getvGwosConfiguration()
	{
		return vGwosConfiguration;
	}

	public void setvGwosConfiguration( VEMAGwosConfiguration vGwosConfiguration )
	{
		this.vGwosConfiguration = vGwosConfiguration;
	}

	/**
	 * Default constructor. Will connect to localhost and port 4913
	 */
	public GWOSConnectorServiceUnified(String gwosConfigFilename, String vemaMonitorProfileFilename )
	{
		this.gwosConfigFilename         = gwosConfigFilename;
		this.vemaMonitorProfileFilename = vemaMonitorProfileFilename;
	}

	/**
	 * Constructor to define host and port and type for monitoring
	 * 
	 * @param GWOSHost
	 * @param gwosPort
	 * @param managementServerType
	 *            . Valid entries VemaConstants.MGMT_SERVER_VMWARE,
	 *            VemaConstants.MGMT_SERVER_RHEV
	 * @param hypervisorType
	 *            . Valid entries: VemaConstants.HYPERVISOR_VMWARE,
	 *            VemaConstants.HYPERVISOR_RHEV
	 */
	public GWOSConnectorServiceUnified( String GWOSHost, int gwosPort,
			String managmentServerType, String hypervisorType, 
			String applicationType,
            String gwosConfigFilename, String vemaMonitorProfileFilename )
	{
		this.GwosHostName                 = GWOSHost;
		this.GwosPort                     = gwosPort;
		this.managementServerType         = managmentServerType;
		this.hypervisorType               = hypervisorType;
		this.applicationType              = applicationType;
        this.gwosConfigFilename           = gwosConfigFilename;
        this.vemaMonitorProfileFilename   = vemaMonitorProfileFilename;
	}

	/**
	 * Add Hypervisor hosts to GroundWork
	 * 
	 * @param hypervisors
	 * @param serviceList
	 * @return
	 */
	public boolean addHypervisors(List<VemaBaseHost> hypervisors) 
	{
		StringBuffer                   xmlOut = null;
		boolean                   writeStatus = false;
		GWOSService                   service = null;   /* Add Hosts first */
		JSONArray                 logmessages = null; // LogMessage array
        List<GWOSLogMessage>  gwosLogMessages = null;

		for (VemaBaseHost host : hypervisors) 
		{
			xmlOut          = new StringBuffer();    // want a new/cleared one every time for each host
            logmessages     = new JSONArray();
            gwosLogMessages = new ArrayList<GWOSLogMessage>();

            xmlOut.append( GWOSEntity.XML_ADAPTER_OPEN );
            xmlOut.append( GWOSEntity.xmlCmdOpen( GWOSEntity.ACTION_ADD, applicationType ));
			xmlOut.append( host.getXML( GWOSEntity.ACTION_ADD ) );

            if( applicationType == null )   // should probably throw an exception!
            {
                log.debug( "Shouldn't have a NULL applicationType" );
                // -----------------------------------------------------------------
                // this forces a SHORT CIRCUIT of all the rest of the logic.
                // therefore, the rest of the tests of applicationType don't need to 
                // check for null!
                // -----------------------------------------------------------------
                break;
            }

            // -----------------------------------------------------
            // below ... to preserve legacy issue in an efficent way
            // -----------------------------------------------------
            if( applicationType.equals( "VEMA" ) )  // do NOT use VemaConstants.APPLICATIONTYPE_VMWARE here!
            {
                /* Log message for VM */
                logmessages.add(this.createNewLogMessage(
                        host.getHostName(), 
                        "",
                        "PENDING", 
                        "LOW", 
                        "Initial setup"
                        ));
            }
            else // applicationType must != "VEMA", so, we have to use XML instead of JSON
            {
                gwosLogMessages.add( 
                    new GWOSLogMessage( 
                        host.getHostName(),
                        "PENDING",
                        "LOW",
                        null,
                        host.getHostName(),
                        host.getLastUpdate(),
                        "Initial Setup",
                        host.getLastUpdate(),
                        null
                        ));
            }

			/*
			 * Get List of service Should only include services that havw been
			 * marked for monitoring
			 */
			for (String serviceName : host.getMetricPool().keySet()) 
			{
				log.debug("Hypervisor [" + host.getHostName() + "] "
						+ "Add Service [" + serviceName + "] "
						+ "+ LastUpdate: " + host.getLastUpdate());

				service = new GWOSService(
						host.getHostName(), 
						serviceName,
						host.getLastUpdate(), 
						"PENDING", 
						"PENDING"
						);
				xmlOut.append( service.getXML() );

                if( applicationType.equals( "VEMA" ) )  // do NOT use VemaConstants.APPLICATIONTYPE_VMWARE here!
                {
                    logmessages.add(this.createNewLogMessage(
						host.getHostName(),
						serviceName, 
						"PENDING", 
						"LOW", 
						"Initial setup for " + serviceName
						));
                }
                else
                {
                    gwosLogMessages.add( 
                            new GWOSLogMessage( 
                                host.getHostName(),
                                "PENDING",
                                "LOW",
                                null,
                                host.getHostName(),
                                host.getLastUpdate(),
                                "Initial Setup for " + serviceName,
                                host.getLastUpdate(),
                                serviceName
                                ));
                }
			}

            /* Add Hypervisor and all service checks to GWOS */
            xmlOut.append(GWOSEntity.xmlCmdClose());
            xmlOut.append(GWOSEntity.XML_ADAPTER_CLOSE);
        
            log.debug("Add hypervisors XML: " + xmlOut.toString());
            writeStatus = this.writeToGroundWorkServer(xmlOut.toString());

            if( applicationType.equals( "VEMA" ) )
            {
                // ------------------------------------------------------------
                // Wait before sending events for all the Hosts and services.
                // This is to avoid a race condition
                // ------------------------------------------------------------
                try
                {
                    Thread.sleep(5 * 1000);
                }
                catch (InterruptedException ie)
                {
                    log.debug("Add Hypervisor sleep was interrupted. Code: " + ie);
                }

                JSONObject logMessageObject = new JSONObject();
                logMessageObject.put("log-message", logmessages);
                Map<String, String> dataMap = new HashMap<String, String>();
                dataMap.put(JSONFORMAT_PARAM, logMessageObject.toString());
                String responseXML = this.performPost(END_POINT_BULK_EVENT_GEN, dataMap);
                log.debug("Response XML: " + responseXML);
            }
            else
            {
            	writeStatus = sendLogMessages( gwosLogMessages, "Add hypervisors logmessage XML" );
            }
		}

		return writeStatus;
	}

	public boolean deleteHypervisors(List<VemaBaseHost> hypervisors)
    {
		/* Initililaze XML message */
		StringBuffer xmlOut = new StringBuffer( 500 );
		
		xmlOut.append( GWOSEntity.XML_ADAPTER_OPEN );
		xmlOut.append( GWOSEntity.xmlCmdOpen( GWOSEntity.ACTION_DELETE, applicationType ) );
		/*
		 * Iterate of Hypervisors by name and create list of host elements to
		 * delete
		 */
		for( VemaBaseHost host : hypervisors )
		{
			xmlOut.append( host.getXML( GWOSEntity.ACTION_DELETE ) );
		}
		/* Complete XMLL message */
		xmlOut
			.append(GWOSEntity.xmlCmdClose())
			.append(GWOSEntity.XML_ADAPTER_CLOSE);

		/* Send messaage to Groundwork server */
		return this.writeToGroundWorkServer(xmlOut.toString());
	}

	public boolean modifyHypervisors(List<VemaBaseHost> hypervisors)
    {
		StringBuffer xmlOut = null;

		log.debug("Hypervisors List in Modify Hypervisor------- (hyp cnt=" + hypervisors.size() + ")" );

		/* Add Hosts first */
		String      serviceValue = null;
		String      serviceState = null;
		String      serviceExtra = null;
		long        serviceCrit  = 0;
		long        serviceWarn  = 0;
		GWOSService service      = null;
		boolean     writeStatus  = false;

		// NextCheck time setting. Monitoring interval is in minutes and needs
		// to be converted to milli seconds */
		Date nextCheckTime = new Date(System.currentTimeMillis()
				+ (getIntervalTime() * 60 * 1000));
		SimpleDateFormat sdf = new SimpleDateFormat(
				VemaConstants.gwosDateFormat);

		ConcurrentHashMap<String, VemaBaseMetric> serviceList = null;
		// LogMessage array
		log.debug("Create JSON Array");

        List<GWOSLogMessage> gwosLogMessages = new ArrayList<GWOSLogMessage>();
		JSONArray         logmessages = new JSONArray();
		JSONArray  performanceDataArr = new JSONArray();

		log.debug("Iterate over hosts ....");
		/* Iterate over hostlist and create elements with Host status */
		for (VemaBaseHost host : hypervisors)
        {
			xmlOut = new StringBuffer( GWOSEntity.XML_ADAPTER_OPEN );
			xmlOut.append( GWOSEntity.xmlCmdOpen( GWOSEntity.ACTION_MODIFY, applicationType ) );

            if( applicationType == null )
            {
                log.debug( "Another place where appType == null" );
                break;
            }

			log.debug("host in list of hypervisors ---:" + host.getHostName());
			/*
			 * Set next checktime for Hypervisor. Metrics will have to set it as
			 * well.
			 */
			host.setNextCheckTime(sdf.format(nextCheckTime).toString());

			xmlOut.append(host.getXML(GWOSEntity.ACTION_MODIFY));

			/*
			 * Get the Services objects for that Host and create elements for
			 * each of them
			 */
			serviceList = host.getMetricPool();

			log.debug("Hypervisor [" + host.getHostName() + "] Status ["
					+ host.getRunState() + "] has number of metrics: "
					+ serviceList.size());

			for( String serviceName : serviceList.keySet() ) 
			{
				VemaBaseMetric vbm = serviceList.get( serviceName );
				
				serviceValue = vbm.getCurrValue();
				serviceState = vbm.getCurrState();
				serviceExtra = vbm.getCurrStateExtra();
				serviceWarn  = vbm.getThresholdWarning();
				serviceCrit  = vbm.getThresholdCritical();

				log.debug("-------\n" 
                        + "ServiceName:  '" + serviceName + "'\n"
                        + "ServiceState: '" + serviceState  + "'\n"
                        + "ServiceValue: '" + serviceValue  + "'\n"
                        + "ServiceExtra: '" + serviceExtra  + "'\n"
                        + "ServiceWarn:  '" + serviceWarn  + "'\n"
                        + "ServiceCrit:  '" + serviceCrit + "'\n"
                );

				/*
				 * TODO: Set attributes MonitorStatus and LastHardState (last
				 * two arguments)
				 */
				service = new GWOSService(
                        host.getHostName(), 
                        serviceName,
						host.getLastUpdate(), 
                        serviceState, 
                        serviceState);

				String lastPluginOutput = 
						serviceExtra 
						+ ", Status= " + serviceState 
						+ ", " + "(value=" + serviceValue + ") "
						+ "[W/C=" + serviceWarn + "/" + serviceCrit + "] "
						+ host.getLastUpdate();

				service.setLastPluginOutput(lastPluginOutput);
				service.setLastCheckTime(host.getLastUpdate());

				/* Next CheckTime is lastUpdateTime + interval */
				service.setNextCheckTime( sdf.format( nextCheckTime ).toString() );

				/* Performance raw Data */
				service.setPerformanceData(serviceValue);

				if (log.isDebugEnabled())
					log.debug("Service update: " + service.getXML());

				xmlOut.append(service.getXML());

				log.debug("Service List XML generated. Create Event Messages...");
				boolean stateChanged = serviceList.get(serviceName)
						.isStateChange();

				if (stateChanged)
                {
					log.debug("State for Host " + host.getHostName()
							+ " and service " + serviceName + " has changed");
				}

				// Generate Events if there is a service state change...
				log.debug("Create Event Messages...");
				if (stateChanged)
                {
					log.debug("State change. Create event for Hypervisor " 
                        + host.getHostName() 
                        + " And service " 
                        + serviceName
                        );

                    if( applicationType.equals( "VEMA" ) )
                    {
                        logmessages.add(this.createNewLogMessage(
							host.getHostName(), 
                            serviceName, 
                            serviceState,
							"LOW", 
                            lastPluginOutput));
                    }
                    else
                    {
                        gwosLogMessages.add( 
                                new GWOSLogMessage( 
                                    host.getHostName(),
                                    serviceState,
                                    "LOW",
                                    null,
                                    host.getHostName(),
                                    host.getLastUpdate(),
                                    lastPluginOutput,
                                    host.getLastUpdate(),
                                    serviceName
                                    ));
                    }

				}
				
                // ----------------------------------------------------------------------------
				// Send Notification if State has changed and previous message was not PENDING
                // ----------------------------------------------------------------------------
				if (stateChanged 
                &&  serviceList.get(serviceName).getLastState().equalsIgnoreCase(INITIAL_STATE) == false)
                {
                    // ----------------------------------------------------------------------------
					// HostIP address might be null. In this case use the server name
                    // ----------------------------------------------------------------------------
					String hostIpAddress = host.getIpAddress();
					if (hostIpAddress == null )
						hostIpAddress = host.getHostName();
					
					log.debug("Service Notification sent to NoMa for host [" + host.getHostName() + "] Service " + serviceName);
                    this.performNomaServiceNotify(
                        serviceState, 
                        host.getHostName(), 
                        host.getHostGroup(), 
                        "", 
                        serviceName, 
                        lastPluginOutput, 
                        (serviceState.equalsIgnoreCase("OK") 
                            ? NOTIFICATIONTYPE_RECOVERY 
                            : NOTIFICATIONTYPE_PROBLEM ),
                        "", 
                        hostIpAddress, 
                        ""/*shortDateTime*/, 
                        ""/*serviceNotificationId*/, 
                        "", 
                        "Cloud Hub service Notification", 
                        ""
                        );
				}
				log.debug("Create Performance Messages...");
				// Performance Data Posting goes here
			
				if (serviceList.get(serviceName).isGraphed()) 
				{
					JSONObject performanceData = new JSONObject();
					performanceData.put("server-time",
							System.currentTimeMillis() / 1000);
					performanceData.put("server-name",  host.getHostName());
					performanceData.put("service-name", serviceName);
					// There is a 19 char limitation for the DS in the RRD graph
					String label = null;
					if (serviceName.length() > 19)
						label = serviceName.substring(
									serviceName.length() - 19,
									serviceName.length()
									);
					else
						label = serviceName;
					
					performanceData.put("label",    label);
					performanceData.put("value",    serviceValue);
					performanceData.put("warning",  serviceWarn);
					performanceData.put("critical", serviceCrit);
					performanceDataArr.add(performanceData);
				}
			}

			log.debug("Create Event Message for Host...");
			// Generate Events if there is a host change...
			if (host.isStateChange()) 
			{
				log.debug("State for Host " + host.getHostName()	+ " has changed. Create Event");
				
				if (log.isDebugEnabled())
				{
					log.debug("getRunExtra():     " + (host.getRunExtra()     == null 
                        ? "(null)" : host.getRunExtra().toString()));
					log.debug("getRunState():     " + (host.getRunState()     == null 
                        ? "(null)" : host.getRunState().toString()));
					log.debug("getPrevRunState(): " + (host.getPrevRunState() == null 
                        ? "(null)" : host.getPrevRunState().toString()));				
					log.debug("getHostGroup():    " + (host.getHostGroup()    == null 
                        ? "(null)" : host.getHostGroup().toString()));
					log.debug("getIpAddress():    " + (host.getIpAddress()    == null 
                        ? "(null)" : host.getIpAddress().toString()));
				}
			
                if( applicationType.equals( "VEMA" ) )
                {
                    logmessages.add(
                        this.createNewLogMessage(
                            host.getHostName(),
                            "", 
                            host.getRunState(), 
                            "LOW", 
                            host.getRunExtra()
                            ));
                }
                else
                {
                    gwosLogMessages.add( 
                            new GWOSLogMessage( 
                                host.getHostName(),
                                host.getRunState(),
                                "LOW",
                                null,
                                host.getHostName(),
                                null,
                                host.getRunExtra(),
                                null,
                                null
                                ));
                }
				
				/* Send Notification if State has changed and previous message was not PENDING */
				if (host.getPrevRunState().equalsIgnoreCase(INITIAL_STATE) == false)
				{
					/* HostIP address might be null . In this case use the server name */
					String hostIpAddress = host.getIpAddress();
					if (hostIpAddress == null )
						hostIpAddress = host.getHostName();
					
                    this.performNomaHostNotify(
                        host.getRunState(),
                        host.getHostName(),
                        host.getHostGroup(),
                        (host.getRunState().equalsIgnoreCase("UP"))
                            ?  NOTIFICATIONTYPE_RECOVERY
                            :  NOTIFICATIONTYPE_PROBLEM,
                        hostIpAddress,
                        host.getRunExtra(),
                        ""/*shortDateTime*/,
                        ""/*hostNotificationId*/,
                        ""/*notificationAuthOrAlias*/,
                        "Cloud Hub Host Notification",
                        ""/*notificationRecipients*/);
					
					log.debug("Host Notification sent to NoMa for host " + host.getHostName());
				}
			}

			log.debug("End of modifyHypervisors");
			xmlOut.append( GWOSEntity.xmlCmdClose() );
			xmlOut.append( GWOSEntity.XML_ADAPTER_CLOSE );

			log.debug("Modify hypervisors : " + xmlOut.toString());
			writeStatus = this.writeToGroundWorkServer(xmlOut.toString());
		}

		/* Only sent Events on state changes */
		if (logmessages != null && logmessages.size() > 0) 
		{
			JSONObject logMessageObject = new JSONObject();
			logMessageObject.put("log-message", logmessages);
			log.debug("Call EVENT Rest API");
			Map<String, String> dataMap = new HashMap<String, String>();
			dataMap.put(JSONFORMAT_PARAM, logMessageObject.toString());
			String responseXML = this.performPost(END_POINT_BULK_EVENT_GEN, dataMap);
			log.debug(responseXML);
		}
		else if( applicationType.equals( "VEMA" ) )
		{
			log.debug( "No event changes detected Hypervisors and all services" );
		}
        else
        {
        	writeStatus = sendLogMessages( gwosLogMessages, "Modify hypervisors logmessage XML" );
        }

        // TODO:  Is this something which also needs to have an XML alternative?

		log.debug("Write Performance to Rest API...");
		// Now post performance data
		if (performanceDataArr.size() > 0) 
		{
			JSONObject perfDataObject = new JSONObject();
			perfDataObject.put("performance-data", performanceDataArr);
			Map<String, String> dataMap = new HashMap<String, String>();
			dataMap.put(JSONFORMAT_PARAM, perfDataObject.toString());
			String responseXMLPerfData = this.performPost(END_POINT_PERF_DATA, dataMap);
			log.debug(responseXMLPerfData);
		}
		return writeStatus;
	}

	public boolean addVirtualMachines(List<VemaBaseVM> listOfVM)
	{
		StringBuffer                    xmlOut = null;
		boolean                    writeStatus = false;
		GWOSService                    service = null;
		JSONArray                  logmessages = null;
        List<GWOSLogMessage>   gwosLogMessages = null;

		/* Add VM as Host objects first first */
		for (VemaBaseVM vm : listOfVM)
        {
			xmlOut = new StringBuffer(GWOSEntity.XML_ADAPTER_OPEN );
			xmlOut.append( GWOSEntity.xmlCmdOpen( GWOSEntity.ACTION_ADD, applicationType ));
			xmlOut.append( vm.getXML(GWOSEntity.ACTION_ADD) );

            if( applicationType == null )
            {
                break;
                // short circuit so that subsequent tests need not worry about null
            }

			log.debug("Add VM instance [" + vm + "]");

			logmessages     = new JSONArray();
            gwosLogMessages = new ArrayList<GWOSLogMessage>();

			/* Log message for VM */
            if( applicationType.equals( "VEMA" ) )
            {
                logmessages.add(this.createNewLogMessage(
                    vm.getVMName(), 
                    "",
					"PENDING", 
                    "LOW", 
                    "Initial setup"
                    ));
            }
            else
            {
                gwosLogMessages.add( 
                        new GWOSLogMessage( 
                            vm.getVMName(),
                            "PENDING",
                            "LOW",
                            null,
                            null,
                            null,
                            "Initial Setup",
                            null,
                            null
                            ));
            }

			/*
			 * List of services for VM's. Call should only return metrics marked
			 * for monitoring
			 */
			for (String serviceName : vm.getMetricPool().keySet())
            {
				log.debug("VM Guest [" + vm.getVMName() + "] Add Service ["
						+ serviceName + "] + LastUpdate: " + vm.getLastUpdate());
				service = new GWOSService(vm.getVMName(), serviceName,
						vm.getLastUpdate(), "PENDING", "PENDING");
				xmlOut.append(service.getXML());
                if( applicationType.equals( "VEMA" ) )
                {
                    logmessages.add(this.createNewLogMessage(
                        vm.getVMName(),
						serviceName, 
                        "PENDING", 
                        "LOW", 
                        "Initial setup for " + serviceName
                        ));
                }
                else
                {
                    gwosLogMessages.add( 
                            new GWOSLogMessage( 
                                vm.getVMName(),
                                "PENDING",
                                "LOW",
                                null,
                                vm.getMacAddress(),
                                null,
                                "Initial Setup for " + serviceName,
                                null,
                                serviceName
                                ));
                }
			}

			/* Add VM and it's services to GWOS */
			xmlOut.append( GWOSEntity.xmlCmdClose() );
			xmlOut.append( GWOSEntity.XML_ADAPTER_CLOSE);

			log.debug("Add Virtual machines : " + xmlOut.toString());
			writeStatus = this.writeToGroundWorkServer(xmlOut.toString());
			
			// Wait before sending events for all the Hosts and services. This is to avoid a race condition
            if( applicationType.equals( "VEMA" ) )
            {
                try
                {
                    Thread.sleep(200); // 0.2 seconds
                }
                catch (InterruptedException ie)
                {
                    log.error("Add Virtual machines sleep was interrupted. Error: " + ie);
                }

                JSONObject logMessageObject = new JSONObject();
                logMessageObject.put("log-message", logmessages);

                Map<String, String> dataMap = new HashMap<String, String>();
                dataMap.put(JSONFORMAT_PARAM, logMessageObject.toString());
                String responseXML = this.performPost(END_POINT_BULK_EVENT_GEN, dataMap);
                log.debug(responseXML);
            }
            else
            {
            	writeStatus = sendLogMessages( gwosLogMessages, "Add VMs logmessage XML" );
            }
		}

		return writeStatus;
	}

	public boolean deleteVirtualMachines( List<VemaBaseVM> listOfVM )
	{
		boolean status = true;

		return status;
	}

	public boolean modifyVirtualMachines(List<VemaBaseVM> listOfVM)
	{
		StringBuffer xmlOut = null;
		boolean writeStatus = false;

		log.debug("listOfVM List in Modify VMs-----------" + listOfVM.size());
		log.debug("XML OUT VM-----" + xmlOut);

		/* Add Hosts first */
		String               serviceValue    = null;
		String               serviceState    = null;
		String               serviceExtra    = null;
		long                 serviceWarn     = 0;
		long                 serviceCrit     = 0;
		GWOSService          service         = null;
        List<GWOSLogMessage> gwosLogMessages = null;

		// NextCheck time setting. Monitoring interval is in minutes and needs
		// to be converted to milli seconds */
		Date nextCheckTime = new Date(System.currentTimeMillis()
				+ (getIntervalTime() * 60 * 1000));
		SimpleDateFormat sdf = new SimpleDateFormat( VemaConstants.gwosDateFormat );

		// LogMessage array
		JSONArray        logmessages = new JSONArray();
		JSONArray performanceDataArr = new JSONArray();
		gwosLogMessages              = new ArrayList<GWOSLogMessage>();

		/* Iterate over hostlist and create elements with Host status */
		for (VemaBaseVM vm : listOfVM)
        {
			xmlOut = new StringBuffer( );
			xmlOut.append( GWOSEntity.XML_ADAPTER_OPEN );
			xmlOut.append( GWOSEntity.xmlCmdOpen( GWOSEntity.ACTION_MODIFY, applicationType ));

			log.debug("vms in list of VMs ---:" + vm.getVMName());

			vm.setNextCheckTime(sdf.format(nextCheckTime).toString());

			xmlOut.append(vm.getXML(GWOSEntity.ACTION_MODIFY));

			/*
			 * Get the Services objects for that Host and create elements for
			 * each of them
			 */

			log.debug("VM (guest) [" + vm.getVMName() + "] Status ["
					+ vm.getRunState() + "] has number of metrics: "
					+ vm.getMetricPool().size());

			for (String serviceName : vm.getMetricPool().keySet()) 
			{
				VemaBaseMetric vbm = vm.getMetric( serviceName );
				
				serviceValue = vbm.getCurrValue();
				serviceState = vbm.getCurrState();
				serviceExtra = vbm.getCurrStateExtra();
				serviceWarn  = vbm.getThresholdWarning();
				serviceCrit  = vbm.getThresholdCritical();

				log.debug("VM serviceState---- " + serviceState);

				/*
				 * TODO: Set attributes MonitorStatus and LastHardState (last
				 * two arguments)
				 */
				service = new GWOSService(
                        vm.getVMName(), 
                        serviceName,
						vm.getLastUpdate(), 
                        serviceState, 
                        serviceState 
                        );

				String lastPluginOutput = serviceExtra + ", Status= "
						+ serviceState + ", " + "(value=" + serviceValue + ") "
						+ "[W/C=" + serviceWarn + "/" + serviceCrit + "] "
						+ vm.getLastUpdate();
				service.setLastPluginOutput(lastPluginOutput);
				service.setLastCheckTime(vm.getLastUpdate());

				/* Next check time */
				service.setNextCheckTime(sdf.format(nextCheckTime).toString());

				/* Performance raw Data */
				service.setPerformanceData(serviceValue);

				if (log.isDebugEnabled())
					log.debug("Service update: " + service.getXML());

				xmlOut.append(service.getXML());
				boolean stateChanged = vm.getMetric(serviceName)
						.isStateChange();

				log.debug("XML creation complete for service " + serviceName);

				if (stateChanged)
				{
					log.debug( "State for Host " + vm.getVMName()
							+ " and service " + serviceName + " has changed" );

					if( applicationType.equals( "VEMA" ) )
					{
						logmessages.add( 
								this.createNewLogMessage( 
										vm.getVMName(),
										serviceName, 
										serviceState, 
										"LOW",
										lastPluginOutput ) );
					}
					else
					{
	                    gwosLogMessages.add( 
	                            new GWOSLogMessage( 
	                                vm.getVMName(),
	                                serviceState,
	                                "LOW",
	                                null,
	                                vm.getMacAddress(),
	                                null,
	                                lastPluginOutput,
	                                null,
	                                serviceName
	                                ));	
					}
				}
				
				/*
				 * Send Notification if State has changed and previous message
				 * was not PENDING
				 */
				if (stateChanged
				&&  vm.getMetric( serviceName ).getLastState()
								.equalsIgnoreCase( INITIAL_STATE ) == false)
				{
					/* HostIP address might be null . In this case use the server name */
					String hostIpAddress = vm.getIpAddress();
					if (hostIpAddress == null )
						hostIpAddress = vm.getVMName();
					
                    this.performNomaServiceNotify( 
                        serviceState,
                        vm.getVMName(),
                        vm.getHostGroup(),
                        "",
                        serviceName,
                        lastPluginOutput,
                        (serviceState.equalsIgnoreCase( "OK" ))
                            ? NOTIFICATIONTYPE_RECOVERY
                            : NOTIFICATIONTYPE_PROBLEM, 
                        "",
                        hostIpAddress,
                        ""/* shortDateTime */,
                        ""/* serviceNotificationId */,
                        "",
                        "Cloud Hub service Notification",
                        "" 
                        );

					log.debug( "Service Notification sent to NoMa for host ["
							+ vm.getVMName() + "] Service " + serviceName );
				}

				log.debug("JSON for event complete");
				// Performance Data Posting goes here

				if (vm.getMetric( serviceName ).isGraphed())
				{
					JSONObject performanceData = new JSONObject();
					performanceData.put( "server-time",
							System.currentTimeMillis() / 1000 );
					performanceData.put( "server-name", vm.getVMName() );
					performanceData.put( "service-name", serviceName );
					// There is a 19 char limitation for the DS in the RRD graph
					String label = null;
					if (serviceName.length() > 19)
						label = serviceName
								.substring( serviceName.length() - 19,
										serviceName.length() );
					else
						label = serviceName;

					performanceData.put( "label",    label );
					performanceData.put( "value",    serviceValue );
					performanceData.put( "warning",  serviceWarn );
					performanceData.put( "critical", serviceCrit );

					performanceDataArr.add( performanceData );
				}
			}
			log.debug("JSON for Performance data complete");

			/* VM and all services output */
			xmlOut.append( GWOSEntity.xmlCmdClose() );
            xmlOut.append( GWOSEntity.XML_ADAPTER_CLOSE );

			log.debug("VM Status update: " + xmlOut.toString());
			writeStatus = this.writeToGroundWorkServer( xmlOut.toString() );

			// Generate Events if there is a virtual machine change...
			if (vm.isStateChange())
			{
				log.debug( "State for VM " + vm.getVMName() + " has changed" );
				logmessages.add( this.createNewLogMessage( vm.getVMName(), "",
						vm.getRunState(), "LOW", vm.getRunExtra() ) );

				/*
				 * Send Notification if State has changed and previous message
				 * was not PENDING
				 */
				if (vm.getPrevRunState().equalsIgnoreCase( INITIAL_STATE ) == false)
				{
					/* HostIP address might be null . In this case use the server name */
					String hostIpAddress = vm.getIpAddress();
					if (hostIpAddress == null )
						hostIpAddress = vm.getVMName();
					
                    this.performNomaHostNotify(
                        vm.getRunState(),
                        vm.getVMName(),
                        vm.getHostGroup(),
                        (vm.getRunState().equalsIgnoreCase( "UP" )) 
                            ? NOTIFICATIONTYPE_RECOVERY 
                            : NOTIFICATIONTYPE_PROBLEM,
                        hostIpAddress,
                        vm.getRunExtra(),
                        ""/* shortDateTime */,
                        ""/* hostNotificationId */,
                        ""/* notificationAuthOrAlias */,
                        "Cloud Hub Host Notification",
                        ""/* notificationRecipients */);

					log.debug( "Host Notification sent to NoMa for host " + vm.getVMName() );
				}
			}
		}

		/* Only sent Events on state changes */
		if (logmessages != null && logmessages.size() > 0)
		{
			JSONObject logMessageObject = new JSONObject();
			logMessageObject.put( "log-message", logmessages );

			log.debug( "Call EVENT Rest API" );
			Map<String, String> dataMap = new HashMap<String, String>();
			dataMap.put( JSONFORMAT_PARAM, logMessageObject.toString() );
			String responseXML = this.performPost( END_POINT_BULK_EVENT_GEN, dataMap );
			log.debug( responseXML );
		}
		else if( applicationType.equals( "VEMA" ))
		{
			log.debug( "No event changes detected VM Guest and all services" );
		}
		else
		{
        	writeStatus = sendLogMessages( gwosLogMessages, "Add Modify VMs logmessage XML" );
		}

		// Now post performance data
		if (performanceDataArr.size() > 0)
		{
			JSONObject perfDataObject = new JSONObject();
			perfDataObject.put( "performance-data", performanceDataArr );

			Map<String, String> dataMap = new HashMap<String, String>();
			dataMap.put( JSONFORMAT_PARAM, perfDataObject.toString() );
			String responseXMLPerfData = this.performPost( END_POINT_PERF_DATA,
					dataMap );
			log.debug( responseXMLPerfData );
		}
		/*
		 * boolean status = true;
		 * 
		 * return status;
		 */
		return writeStatus;
	}

	/**
	 * Hostgroup helper function for adding an empty HostGroup
	 * 
	 * @param hostGroup
	 * @return
	 */
	public boolean addHostgroup( GWOSHostGroup hostGroup )
	{
		log.debug( "HOST GROUP XML" + hostGroup.getXMLAddHostgroup() );

		return this.writeToGroundWorkServer( hostGroup.getXMLAddHostgroup() );
	}

	/**
	 * Hostgroup helper function for modifying the content of the hostgroup
	 * 
	 * @param hostGroup
	 * @param hostList
	 * @return
	 */
	public boolean modifyHostgroup( GWOSHostGroup hostGroup,
			List<String> hostList )
	{
		log.debug( "Modify HostGroup '" 
                + hostGroup.getHostGroupName()
				+ "' VMList has [" 
                + hostList.size() 
                + "] VMlist '"
				+ hostList.toString()
                + "'"
                );

		return this.writeToGroundWorkServer( hostGroup
				.getXMLModifyHostgroup( hostList ) );
	}

	/**
	 * Delete given Hostgroup from GroundWork server
	 * 
	 * @param hostGroup
	 * @return
	 */
	public boolean deleteHostgroup( GWOSHostGroup hostGroup )
	{
		return this.writeToGroundWorkServer( hostGroup.getXMLDeleteHostgroup() );
	}

	/**
	 * Return the Hostgroup name that will be used in GroundWork to display the
	 * Hostgroup depending on the entity (Management Server, Hypervisor) and
	 * connector (VMware or RHev)
	 * 
	 * @param entityScope
	 *            . Valid values: VemaConstants.ENTITY_MGMT_SERVER,
	 *            VemaConstants.HYPERVISOR_VMWARE
	 * @param hostgroupName
	 *            Base name used in the Virtual environment
	 * @return hostgroup name with the prefix that should be used to create the
	 *         Hostgroup in GroundWork Monitor
	 */
	public String getHostGroupName( String entityScope, String hostgroupName )
	{
        String theResult = null;
		if (entityScope.compareTo( VemaConstants.ENTITY_MGMT_SERVER ) == 0)
		{
			if (this.managementServerType
					.compareTo( VemaConstants.MGMT_SERVER_VMWARE ) == 0)
				theResult = VemaConstants.PREFIX_VMWARE_MGMT_SERVER + hostgroupName;

			else if (this.managementServerType
					.compareTo( VemaConstants.MGMT_SERVER_RHEV ) == 0)
				theResult = VemaConstants.PREFIX_RHEV_MGMT_SERVER + hostgroupName;

			else
			{
				log.error( "Unknown managementServerType: '" + this.managementServerType + "'" );
				theResult = hostgroupName;
			}
		}
		else if (entityScope.compareTo( VemaConstants.ENTITY_HYPERVISOR ) == 0)
		{
			if (this.hypervisorType.compareTo( VemaConstants.HYPERVISOR_VMWARE ) == 0)
				theResult = VemaConstants.PREFIX_VMWARE_HYPERVISOR + hostgroupName;

			else if (this.hypervisorType.compareTo( VemaConstants.HYPERVISOR_RHEV ) == 0)
				theResult = VemaConstants.PREFIX_RHEV_HYPERVISOR + hostgroupName;

			else
			{
				log.error( "Unknown hypervisorType: '" + this.hypervisorType + "'" );
				theResult = hostgroupName;
			}
		}
		else
		{
			log.error( "ALERT: entityScope = '" + entityScope + "'" );
			theResult = hostgroupName;
		}
        // log.debug( "theResult = '" + theResult + "'" ); // OBSERVATION:  correct result returned for RHEV!

        return theResult;
	}

	/**
	 * Helper functions
	 */

	/**
	 * getXMLGroupServices
	 * 
	 * @param serviceList
	 * @return
	 */
	public String getXMLGroupServices( List<GWOSService> serviceList )
	{
		StringBuffer xmlOut = new StringBuffer();
		
		xmlOut.append( GWOSEntity.XML_ADAPTER_OPEN );
		{
			xmlOut.append( GWOSEntity.xmlCmdOpen( GWOSEntity.ACTION_MODIFY, applicationType ));
	
			for( GWOSService serviceObj : serviceList )
				if (serviceObj != null)
					xmlOut.append( serviceObj.getXML() );
	
			xmlOut.append( GWOSEntity.xmlCmdClose() );
		}
		xmlOut.append( GWOSEntity.XML_ADAPTER_CLOSE );
		
		return xmlOut.toString();
	}

	/**
	 * getXMLGroupHosts
	 * 
	 * @param hostList
	 * @return
	 */
	public String getXMLGroupHosts( List<VemaBaseHost> hostList, String action )
	{
		StringBuffer xmlOut = new StringBuffer();
		
		xmlOut.append( GWOSEntity.XML_ADAPTER_OPEN );
		{
			xmlOut.append( GWOSEntity.xmlCmdOpen( GWOSEntity.ACTION_MODIFY, applicationType ));

			for( VemaBaseHost hostObj : hostList )
				if (hostObj != null)
					xmlOut.append( hostObj.getXML( action ) );

			xmlOut.append( GWOSEntity.xmlCmdClose() );
		}
		xmlOut.append( GWOSEntity.XML_ADAPTER_CLOSE );
		return xmlOut.toString();
	}

	/**
	 * writeToGroundWorkServer
	 * 
	 * @param message
	 */
	private boolean writeToGroundWorkServer( String message )
	{
		// ObjectOutputStream out = null;
		Socket   requestSocket = null;
		BufferedWriter      wr = null;
		boolean         result = false;

        if(log.isDebugEnabled())
            log.debug( 
				"Write to GroundWork server [" 
				+ this.GwosHostName 
				+ "] port [" 
				+ this.GwosPort 
				+ "] \n" 
				+ message 
				+ "\n");

		// Create connection and write message
		try
		{
			requestSocket = new Socket( this.GwosHostName, this.GwosPort );
			log.debug( "REQUEST SOCKET OBJECT" );
			wr = new BufferedWriter( 
                    new OutputStreamWriter( requestSocket.getOutputStream() ) );

			wr.write( message );
			wr.flush();

			/* Success */
			result = true;
		}
		catch( Exception e )
		{
			result = false;
			log.error( "Exception while writing to GroundWork server. Message to be send: "
					+ message + " Exception: " + e );
		}
		finally
		{
			try
			{
				wr.close();
				requestSocket.close();
				log.debug( "SOCKET IS CLOSED: " + requestSocket.isClosed() );
			}
			catch( IOException ioException )
			{
				result = false;
				log.error( "Exception while closing socket. Error :"
						+ ioException );
			}
		}
		return result;
	}

	/**
	 * get monitor interval time in minutes
	 * 
	 * @return interval time in minutes
	 */
	public int getIntervalTime()
	{
		return intervalTime;
	}

	/**
	 * Set monitor interval time in minutes
	 * 
	 * @param intervalTime
	 *            in minutes
	 */
	public void setIntervalTime( int intervalTime )
	{
		this.intervalTime = intervalTime;
	}
	
	private boolean sendLogMessages( List<GWOSLogMessage> list, String message )
	{
        if( list == null || list.size() == 0 )
            return true;

        StringBuffer xmlOut = new StringBuffer();
        xmlOut.append( GWOSEntity.XML_ADAPTER_OPEN );
        {
            xmlOut.append( GWOSEntity.xmlCmdOpen( GWOSEntity.ACTION_ADD, applicationType ));
            {
                for( GWOSLogMessage msg : list )
                    xmlOut.append( msg.getXML() );
            }
            xmlOut.append(GWOSEntity.xmlCmdClose());
        }
        xmlOut.append(GWOSEntity.XML_ADAPTER_CLOSE);
        
        String xml = xmlOut.toString();
    
        if( log.isDebugEnabled() )
            log.debug( "sendLogMessage( ..., '" + message + "'): xml ='" + xml + "'\n"); // for efficiency, using a temp String

        return this.writeToGroundWorkServer( xml );
	}

	/**
	 * Perform post
	 * 
	 * @param userName
	 * @param password
	 * @param appServerName
	 * @param mbeanAtts
	 * @param gwServerName
	 * @return
	 */
	private String performPost( String endPoint, Map<String, String> postDataMap )
	{
		VEMAGwosConfiguration gwosConfig = MonitorAgentConfigXMLToBean
				.gwosConfigXMLToBean( gwosConfigFilename );
		String                  userName = gwosConfig.getWsUser();
		String                  password = gwosConfig.getWsPassword();
		String              gwServerName = gwosConfig.getGwosServer();
		String                  protocol = gwosConfig.isGwosSSLEnabled() ? "https" : "http";
		String                  response = null;

		if (postDataMap != null && postDataMap.size() > 0)
		{
			DataOutputStream out = null;
			try
			{
				// connect
				URI uri = new URI( 
						protocol, 
						null, 
						"//" + gwServerName + endPoint, 
						null, 
						null 
						);

				URL url = uri.toURL();
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				logonce( url.toString() );

				// initialize the connection
				connection.setDoOutput( true );
				connection.setDoInput( true );
				connection.setRequestMethod( "POST" );
				connection.setUseCaches( false );

				connection.setRequestProperty( "Content-type", "application/x-www-form-urlencoded" );
				connection.setRequestProperty( "Connection", "Keep-Alive" );

				out = new DataOutputStream( connection.getOutputStream() );
				
				StringBuilder postData = new StringBuilder();
				postData.append( "username=" + userName );
				postData.append( "&password=" + password );

				for( Map.Entry<String, String> entry : postDataMap.entrySet() )
				{
					postData.append( 
							"&" + entry.getKey() 
							+ "="
							+ URLEncoder.encode( entry.getValue(), "UTF-8" ) );
				} // end for
				if (log.isDebugEnabled()) // for efficiency, don't call unless in mode.
					log.debug( URLDecoder.decode( postData.toString(), "UTF-8" ) );
				
				out.writeBytes( postData.toString() );
				out.flush();
				out.close();

				BufferedReader inStream = new BufferedReader(
						new InputStreamReader( connection.getInputStream() ) );

				StringBuffer sb = new StringBuffer();
				String str = null;
				while( (str = inStream.readLine()) != null )
					sb.append( str );
				response = sb.toString();
				
				connection.disconnect();
			}
			catch( Exception e )
			{
				log.error( "Got Exception: " + e );
				response = "<code>6</code><message>" + e.getMessage()
						+ " or Invalid GroundWork Server Name</message>";
			}
		}
		else
		{
			log.error( "Invalid post data or nothing to post to rest api" );
		} // end if

		return response;
	}

	/**
	 * Helper to create logmessage
	 * 
	 * @param host
	 * @param serviceDescription
	 * @param monitorStatus
	 * @param severity
	 * @param message
	 * @return
	 */
	private JSONObject createNewLogMessage(String host, String serviceDescription, 
			String monitorStatus, String severity, String message) 
	{
		JSONObject logmessage = new JSONObject();

		logmessage.put( "host",                host );  // "Host" in xml
		logmessage.put( "service-description", serviceDescription );  // "ServiceDescription" in xml
		logmessage.put( "type",                applicationType );  // "ApplicationType", but in <command ... section>
		logmessage.put( "status",              monitorStatus );  // "MonitorStatus" in xml
		logmessage.put( "severity",            severity ); // "Severity"  in xml
		logmessage.put( "message",             message );  // "TextMessage" in xml

		return logmessage;
	}
	
	/**
	 * Helper to create logmessage
	 * 
	 * @param host
	 * @param service
	 * @param monitorStatus
	 * @param severity
	 * @param message
	 * @param type
	 * @return
	 */
	public final void sendEventMessage( String host, String service,
			String monitorStatus, String severity, String message, String type )
	{
		JSONArray logmessages = new JSONArray();
		JSONObject logmessage = new JSONObject();
		
		if( host == null )  // must have a host!
			return;
		
		if( applicationType.equals( "VEMA" ) )
		{
			logmessage.put("host",                   host );
			logmessage.put("service-description", service == null ? ""             : service );
			logmessage.put("type",                   type == null ? "SYSTEM"       : type);
			logmessage.put("status",        monitorStatus == null ? "UNKNOWN"      : monitorStatus );
			logmessage.put("severity",           severity == null ? "HIGH"         : severity );
			logmessage.put("message",             message == null ? "(no message)" : message);

			logmessages.add( logmessage );
			
			JSONObject logMessageObject = new JSONObject();
			logMessageObject.put("log-message", logmessages);
			Map<String, String> dataMap = new HashMap<String, String>();
			dataMap.put(JSONFORMAT_PARAM, logMessageObject.toString());
			String responseXML = this.performPost(END_POINT_BULK_EVENT_GEN,	dataMap);
			log.debug("LogEvent response XML: " + responseXML);
		}
		else
		{
			GWOSLogMessage glm = new GWOSLogMessage(
					host,
					monitorStatus == null ? "UNKNOWN"      : monitorStatus,
					severity      == null ? "HIGH"         : severity,
					null,
					host,
                    this.nowTime(),
					message       == null ? "(no message)" : message,
                    this.nowTime(),
					type          == null ? "SYSTEM"       : type
					);
			List<GWOSLogMessage> list = new ArrayList<GWOSLogMessage>();
			list.add( glm );
			sendLogMessages( list, "Sending event" );
		}
	}

	/**
	 * Helper to create new NOMA Host Notify
	 * 
	 * @param host
	 * @param serviceDescription
	 * @param monitorStatus
	 * @param severity
	 * @param message
	 * @return
	 */
	private void performNomaHostNotify(String hostState,
			String hostName, String hostGroupNames, String notificationType,
			String hostAddress, String hostOutput, String shortDateTime,
			String hostNotificationId, String notificationAuthOrAlias,
			String notificationComment, String notificationRecipients) 
	{
		Map<String,String> hostNotify = new HashMap<String,String>();

		hostNotify.put("hoststate",               hostState);
		hostNotify.put("hostname",                hostName);
		hostNotify.put("hostgroupnames",          hostGroupNames);
		hostNotify.put("notificationtype",        notificationType);
		hostNotify.put("hostaddress",             hostAddress);
		hostNotify.put("hostoutput",              hostOutput);
		hostNotify.put("shortdatetime",           shortDateTime);
		hostNotify.put("hostnotificationid",      hostNotificationId);
		hostNotify.put("notificationauthoralias", notificationAuthOrAlias);
		hostNotify.put("notificationcomment",     notificationComment);
		hostNotify.put("notificationrecipients",  notificationRecipients);

		String responseXMLNotifications = this.performPost(END_POINT_NOMA_HOST_NOTIFY, hostNotify);
		log.debug( "ResponseXMLNotifications response: '" + responseXMLNotifications + "'");
	}

	/**
	 * Helper to create new NOMA Service Notify
	 * 
	 * @param host
	 * @param serviceDescription
	 * @param monitorStatus
	 * @param severity
	 * @param message
	 * @return
	 */
	private void performNomaServiceNotify(String serviceState,
			String hostName, String hostGroupNames, String serviceGroupNames,
			String serviceDescription, String serviceOutput,
			String notificationType, String hostAlias, String hostAddress,
			String shortDateTime, String serviceNotificationId,
			String notificationAuthOrAlias, String notificationComment,
			String notificationRecipients) 
	{
		Map<String,String> serviceNotify = new HashMap<String,String>();

		serviceNotify.put("servicestate",            serviceState);
		serviceNotify.put("hostname",                hostName);
		serviceNotify.put("hostgroupnames",          hostGroupNames);
		serviceNotify.put("servicegroupnames",       serviceGroupNames);
		serviceNotify.put("servicedescription",      serviceDescription);
		serviceNotify.put("serviceoutput",           serviceOutput);
		serviceNotify.put("notificationtype",        notificationType);
		serviceNotify.put("hostalias",               hostAlias);
		serviceNotify.put("hostaddress",             hostAddress);
		serviceNotify.put("shortdatetime",           shortDateTime);
		serviceNotify.put("servicenotificationid",   serviceNotificationId);
		serviceNotify.put("notificationauthoralias", notificationAuthOrAlias);
		serviceNotify.put("notificationcomment",     notificationComment);
		serviceNotify.put("notificationrecipients",  notificationRecipients);

		String responseXMLNotifications = this.performPost(END_POINT_NOMA_SERVICE_NOTIFY, serviceNotify);
		log.debug(responseXMLNotifications);
    }

    private ConcurrentHashMap< String, Boolean >everSeen = new ConcurrentHashMap<String, Boolean>();
    private void logonce( String message )
    {
        if(everSeen.get( message ) == null)
        {
            everSeen.put( message, true );
            log.info( message );
        }
    }

    private String nowTime()
    {
        Date now = new Date( System.currentTimeMillis() );
        SimpleDateFormat sdf = new SimpleDateFormat( VemaConstants.gwosDateFormat );

        return sdf.format( now ).toString();
    }
}
