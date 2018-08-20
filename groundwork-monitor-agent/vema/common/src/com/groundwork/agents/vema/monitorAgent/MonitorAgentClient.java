package com.groundwork.agents.vema.monitorAgent;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.rpc.ServiceException;

import org.apache.axis.message.SOAPHeaderElement;
import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.api.WSFoundationException;
import org.groundwork.foundation.ws.impl.HostSoapBindingStub;
import org.groundwork.foundation.ws.impl.HostgroupSoapBindingStub;
import org.groundwork.foundation.ws.impl.WSHostGroupServiceLocator;
import org.groundwork.foundation.ws.impl.WSHostServiceLocator;
import org.groundwork.foundation.ws.model.impl.Host;
import org.groundwork.foundation.ws.model.impl.HostGroup;
import org.groundwork.foundation.ws.model.impl.HostQueryType;
import org.groundwork.foundation.ws.model.impl.SortCriteria;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;
import org.itgroundwork.foundation.joxbeans.Metric;
import org.itgroundwork.foundation.joxbeans.VemaMonitoring;

import com.groundwork.agents.vema.utils.ParamBox;
import com.groundwork.agents.vema.api.Vema;
import com.groundwork.agents.vema.api.VemaConstants;
import com.groundwork.agents.vema.base.VemaBaseHost;
import com.groundwork.agents.vema.base.VemaBaseObject;
import com.groundwork.agents.vema.base.VemaBaseQuery;
import com.groundwork.agents.vema.base.VemaBaseTimer;
import com.groundwork.agents.vema.base.VemaBaseVM;
import com.groundwork.agents.vema.base.VemaBaseState;
import com.groundwork.agents.vema.collector.impl.MonitorAgentCollector;
import com.groundwork.agents.vema.configuration.MonitorAgentConfigXMLToBean;
import com.groundwork.agents.vema.configuration.VEMAGwosConfiguration;
import com.groundwork.agents.vema.gwos.GWOSConnectorServiceUnified;
import com.groundwork.agents.vema.gwos.GWOSHostGroup;

/**
 * 
 * This class makes the connection to GWOS and Vema Servers gets the
 * GWOSHostList and VemaHostList and Synchronizes the lists and adds to the GWOS
 * Server.
 * 
 * @author rvardhineedi
 * @author rlynch from 130201 on
 */

public class MonitorAgentClient implements Runnable
{
	private static Logger			log							= Logger.getLogger( MonitorAgentClient.class );

	private GWOSConnectorServiceUnified	gwosService					= null;
	private ParamBox				parambox					= new ParamBox();
	private VEMAGwosConfiguration	vGwosConfig					= null;
	private GWOSHostGroup			mgmthg						= null;
	private Vema					vema						= null;

	private boolean					upAndRunning				= false;

	protected String				hypervisorVema				= null;
	protected String				connectorVema				= null;
	protected String				mgmtServerVema				= null;
	protected String                applicationType             = null;

	protected String				vemaMonitorProfileFilename	= null;
	protected String				gwosConfigFilename			= null;

	// Get the Metrics of Hypervisor and VM
	List<VemaBaseQuery>				hypervisorMetrics			= new ArrayList<VemaBaseQuery>();
	List<VemaBaseQuery>				vmMetrics					= new ArrayList<VemaBaseQuery>();

	private boolean					bFirstTimeSync				= true;
	private boolean					bForceMonitorAfterSync		= false;

	VemaBaseTimer					vemaSyncTimer				= null;
	VemaBaseTimer					vemaMonitorTimer			= null;
	VemaBaseTimer					vemaComaTimer				= null;

	public MonitorAgentClient( Vema vema, VEMAGwosConfiguration vGwosConfig,
			String hypervisorVema, String connectorVema, String mgmtServerVema,
			String applicationType,
			String gwosConfigFilename, String vemaMonitorProfileFilename )
	{
		super();

		this.vema = vema;
		this.vGwosConfig                = vGwosConfig;
		this.connectorVema              = connectorVema;
		this.hypervisorVema             = hypervisorVema;
		this.mgmtServerVema             = mgmtServerVema;
		this.applicationType            = applicationType;
		this.gwosConfigFilename         = gwosConfigFilename;
		this.vemaMonitorProfileFilename = vemaMonitorProfileFilename;

		if( vGwosConfig != null )
		{
			vemaMonitorTimer = new VemaBaseTimer( "vemaMonitor", vGwosConfig.getCheckInterval(), 0 );
			vemaSyncTimer    = new VemaBaseTimer( "vemaSync",    vGwosConfig.getSyncInterval(), 0 );
			vemaComaTimer    = new VemaBaseTimer( "vemaComa",    vGwosConfig.getComaInterval(), 0 );

			logonce( "Monitoring interval set to {"
					+ vGwosConfig.getCheckInterval() + "} Minutes"
					+ "\ngwosConfigFilename         = '" + gwosConfigFilename
					+ "'" + "\nvemaMonitorProfileFilename = '"
					+ vemaMonitorProfileFilename + "'" );
		}
		else
		{
			/* Default 5 min for MONITOR, 2 min for SYNC, 15 min COMA */
			vemaMonitorTimer = new VemaBaseTimer( "vemaMonitor", 5, 0 );
			vemaSyncTimer    = new VemaBaseTimer( "vemaSync",    2, 0 );
			vemaComaTimer    = new VemaBaseTimer( "vemaSync",   15, 0 );

			logonce( "ALERT: \nDefault monitoring interval is 5 Minutes"
					+ "\ngwosConfigFilename         = '" + gwosConfigFilename + "'" 
                    + "\nvemaMonitorProfileFilename = '" + vemaMonitorProfileFilename + "'" );
		}
	}

	/*
	 * public MonitorAgentClient() { super();
	 * 
	 * vemaMonitorTimer = new VemaBaseTimer( "vemaMonitor", 5, 0 );
	 * vemaSyncTimer = new VemaBaseTimer( "vemaSync", 2, 0 ); vemaComaTimer =
	 * new VemaBaseTimer( "vemaComa", 15, 0 );
	 * 
	 * log.info( "ALERT> Default monitoring interval is 5 Minutes" ); }
	 */
	/**
	 * This method returns the protocol type from config
	 * 
	 * @return
	 */

	public boolean isRunning()
	{
		return this.upAndRunning;
	}

	public boolean setIsRunning( boolean bValue )
	{
		if( bValue == false )
			bFirstTimeSync = true;	// restore to 'new object' state.
		
		return this.upAndRunning = bValue;
	}

	public String getGwosProtocolString()
	{
		return vGwosConfig.isGwosSSLEnabled() ? "https://" : "http://";
	}

	public String getVirtualEnvProtocolString()
	{
		return vGwosConfig.isVirtualEnvSSLEnabled() ? "https://" : "http://";
	}

	/**
	 * Make a Call to GWOS Webservice
	 * 
	 * @param vema
	 * @throws WSFoundationException
	 * @throws RemoteException
	 * @throws ServiceException
	 */

	public WSHostServiceLocator hostLocatorCall( VEMAGwosConfiguration vGwosConfig )
	{
		WSHostServiceLocator hostLocator = new WSHostServiceLocator();
		log.debug( "wsHost Address---: '" + hostLocator.gethostAddress() + "'" );

		try
		{

			String endPointURL = 
                      getGwosProtocolString()
					+ vGwosConfig.getGwosServer() 
                    + vGwosConfig.getWsEndpoint()
					+ "/" 
                    + vGwosConfig.getWsHostName();

			log.debug( "Endpoint URL: '" + vGwosConfig.getWsHostName() + "-" );

			hostLocator.setEndpointAddress( vGwosConfig.getWsHostName(), endPointURL );

			log.debug( "Connection to GWOS Webservice is established" );

		}
		catch( Exception exc )
		{
			log.error( "Exception while calling gwos wshost Service   " + exc.getMessage() );
		}

		return hostLocator;
	}

	/**
	 * This method connectod to the gwos wshost webservice..
	 * 
	 * @param vGwosConfig
	 * @return
	 */

	public WSHostGroupServiceLocator hostgroupLocatorCall( VEMAGwosConfiguration vGwosConfig )
	{
		WSHostGroupServiceLocator hostGroupLocator = new WSHostGroupServiceLocator();

		try
		{
			log.debug( "Build the Endpoint URL from vGwosConfig object: '" + vGwosConfig.getWsHostGroupName() + "'" );
			log.debug( "Endpoint URL: '" + vGwosConfig.getWsHostGroupName() + "'" );

			String endPointURL = 
                      getGwosProtocolString()
					+ vGwosConfig.getGwosServer() 
                    + vGwosConfig.getWsEndpoint()
					+ "/" 
                    + vGwosConfig.getWsHostGroupName();

			hostGroupLocator.setEndpointAddress( vGwosConfig.getWsHostGroupName(), endPointURL );

			log.debug( "Connection to GWOS Host Group Webservice is established" );

		}
		catch( Exception exc )
		{
			log.error( "Exception while calling gwos hostGroup webservice   " + exc.getMessage() );
		}

		return hostGroupLocator;
	}

	/**
	 * Call GWOS Service and the list of hosts from GWOS WebService
	 * 
	 * 
	 * @param vema
	 * @throws WSFoundationException
	 * @throws RemoteException
	 * @throws ServiceException
	 */

	private List<String> getGwosService( VEMAGwosConfiguration vGwosConfig )
			throws ServiceException, WSFoundationException, RemoteException
	{
		SortCriteria sc = new SortCriteria( "", "ALL" );

		HostSoapBindingStub wshost = (HostSoapBindingStub) hostLocatorCall( vGwosConfig ).gethost();
		wshost.setHeader( this.buildAuthentication() );

		List<String> hostNames = new ArrayList<String>();

		WSFoundationCollection ws2 = wshost.getHosts( 
            HostQueryType.ALL, 
            "",
            this.applicationType, 
            -1, 
            -1, 
            sc 
            );

		Host[] hosts = ws2.getHost();
		hosts   = (hosts == null) 
                ? new Host[0] 
                : hosts;

		if( hosts != null )
			for( Host host : hosts )
			{
                StringBuffer s = new StringBuffer( 1000 );

                s.append( "Host Name       = '" + host.getName() + "'\n" );
                s.append( "Host ID         = '" + host.getHostID() + "'\n" );
                s.append( "Host Check Type = '" + host.getCheckType() + "'\n" );
                s.append( "Host Device     = '" + host.getDevice() + "'\n" );
                s.append( "Host Groups     # '" + ( host.getHostGroups() == null ? 0 : host.getHostGroups().length ) + "'\n" );
                int i = 0;
                if( host.getHostGroups() != null )  // never happens!  They're never set with the call.
                    for( HostGroup hg : host.getHostGroups() )
                    {
                        s.append( "HG " + i + " name        = '" + hg.getName() + "'\n" );
                        s.append( "HG " + i + " alias       = '" + hg.getAlias() + "'\n" );
                        s.append( "HG " + i + " application = '" + hg.getApplicationName() + "'\n" );
                        s.append( "HG " + i + " description = '" + hg.getDescription() + "'\n" );
                    }

                log.debug( "\n" +  s.toString() );
				
				hostNames.add( host.getName() );
			}

		log.debug( "Host name returned from webservice: " + hostNames.size() );

		return hostNames;
	}

	/**
	 * Helper to build authentication
	 * 
	 * @return
	 */
	private SOAPHeaderElement buildAuthentication()
	{
		String endPoint = 
            getGwosProtocolString() 
            + vGwosConfig.getGwosServer()
            + vGwosConfig.getWsEndpoint() 
            + "/" 
            + "wshost";

		// Add Authentication header
		SOAPHeaderElement authentication = new SOAPHeaderElement( endPoint,
				HostSoapBindingStub.TAG_HEADER_AUTHENTICATION );

		SOAPHeaderElement user = new SOAPHeaderElement( endPoint,
				HostSoapBindingStub.TAG_HEADER_USER, vGwosConfig.getWsUser() );

		SOAPHeaderElement password = new SOAPHeaderElement( endPoint,
				HostSoapBindingStub.TAG_HEADER_SECRET,
				vGwosConfig.getWsPassword() );

		authentication.setPrefix( HostSoapBindingStub.HEADER_PREFIX );
		try
		{
			authentication.addChild( user );
			authentication.addChild( password );
		}
		catch( Exception exc )
		{
			log.error( exc.getMessage() );
		}
		return authentication;
	}

	/**
	 * Get GWOSHostGroupNames
	 * 
	 * @param vGwosConfig
	 * @return
	 * @throws ServiceException
	 * @throws WSFoundationException
	 * @throws RemoteException
	 */

	public List<String> getGwosHostGroupNameService(
			VEMAGwosConfiguration vGwosConfig ) throws ServiceException,
			WSFoundationException, RemoteException
	{
		HostgroupSoapBindingStub wsHostGroup = 
            (HostgroupSoapBindingStub) hostgroupLocatorCall( vGwosConfig ).getwshostgroup();
		wsHostGroup.setHeader( this.buildAuthentication() );
		List<String> hostGroupNames = new ArrayList<String>();
		List<String> headers =
			( vGwosConfig.getVirtualEnvType().equalsIgnoreCase( VemaConstants.CONNECTOR_VMWARE )
			? VemaConstants.PREFIXLIST_VMWARE 
			: vGwosConfig.getVirtualEnvType().equalsIgnoreCase( VemaConstants.CONNECTOR_RHEV )
			? VemaConstants.PREFIXLIST_RHEV
			: null );

		if( headers == null ) // something deeply wrong
		{
			log.error( "gwosConfig.getVirtualEnvType() = '" + 
					vGwosConfig.getVirtualEnvType() + "'" );
			return hostGroupNames;
		}
		WSFoundationCollection wsHostGroupResponse = wsHostGroup
				.getHostGroupsByString( "ALL", "", this.applicationType, "false", "-1", "-1", "", "" );

		HostGroup[] hostGroupCollection = wsHostGroupResponse.getHostGroup();

		StringBuffer sb = new StringBuffer( 100 );
		sb.append( "getGwosHostGroupNameService():\n" );
		if( hostGroupCollection != null )
			for( HostGroup hgc : hostGroupCollection )
			{
				sb.append( "Host group name: '" + hgc.getName() + "' " );
				for( String header : headers )
				{
					if( hgc.getName().startsWith( header ) )
					{
						sb.append( "added" );
						hostGroupNames.add( hgc.getName() );
						break;  // shortcircuit
					}
				}
				sb.append( "\n" );
			}
		log.debug( sb.toString() );

		return hostGroupNames;
	}

	/**
	 * This method gets the Host List from GWOS Host Webservice and get the List
	 * of Hosts from the Vema API. The GWOS HostList and the VemaHostList are
	 * synched to find the unique hosts in both the lists and add the
	 * hypervisors and VM's to GWOS Service. The Host Groups are created for
	 * each Host and the VMs for each Host group are added to the hostGroup
	 * 
	 * 
	 * 
	 * @param vema
	 * @param vGwosConfig
	 * @throws WSFoundationException
	 * @throws RemoteException
	 * @throws ServiceException
	 */

    // -------------------------------------------------
    // 130502.rlynch -- design thoughts --
    // -------------------------------------------------
    // this SYNC needs to be rewritten as:
    //
    // gwosHostList = get list
    // vemaHostList = get list
    //
    // gwosAddHostList = List.getadds( gwosHostList, vemaHostList );
    // gwosDelHostList = List.getdels( gwosHostList, vemaHostList );
    // gwosModHostList = List.getmods( gwosHostList, vemaHostList );
    //
    // then take action.   
    //
    // Same for VMS:
    // gwosVMList = get list
    // vemaVMList = get list
    //
    // gwosAddVMList = List.getadds( gwosHostList, vemaHostList );
    // gwosDelVMList = List.getdels( gwosHostList, vemaHostList );
    // gwosModVMList = List.getmods( gwosHostList, vemaHostList );
    //
    // then take action.
    //

	public ConcurrentHashMap<String, VemaBaseHost> syncMonitorAgentData(
			Vema vema, 
            VEMAGwosConfiguration vGwosConfig,
			ConcurrentHashMap<String, VemaBaseHost> hosts,
			ConcurrentHashMap<String, VemaBaseObject> hostsandvms )
			throws WSFoundationException, RemoteException, ServiceException
	{
		log.debug( "Inside SyncMonitorAgentData Method" );

		// Call GWOS getHost Webservice
		List<String> gwosHostList = (List<String>) getGwosService( vGwosConfig );
		// gwosHostList.clear(); // for TESTING purposes ONLY, for testing the "NEW LIST" case

		log.debug( "Total hosts returned from the GWOS Host Webservice = '" + gwosHostList.size() + "'" );
        for( String hostorvm : gwosHostList )
        {
            log.debug( "gwosHostList element: '" + hostorvm + "'" );
        }

		gwosService.setvGwosConfiguration( vGwosConfig );

		// Create List Objects to store the resultHostList and resultVMList
		// after Synching.
		List<VemaBaseHost>  comparedHostList = new ArrayList<VemaBaseHost>();
		List<VemaBaseVM>      comparedVMList = new ArrayList<VemaBaseVM>();

		List<String>  ListOfHypervisorsToAdd = new ArrayList<String>();
		List<String>       ListOfHypervisors = new ArrayList<String>();
		List<String>                  vmList = new ArrayList<String>();
		List<String>          hostsOrVmsList = new ArrayList<String>();
		List<String> prefixlessHostgroupList = new ArrayList<String>();

		VemaBaseHost            vemaBaseHost = null;
		VemaBaseVM                vemaBaseVM = null;

		VemaBaseObject        vemaBaseObject = null;
		VemaBaseObject      hgVemaBaseObject = null;

		GWOSHostGroup    hypervisorHostGroup = null;
		GWOSHostGroup             hostGroups = null;
		GWOSHostGroup       deleteHostGroups = null;

		// Compare gwosHostList and vemaHostList
		log.debug( "Compare [" 
                + gwosHostList.size()
				+ "] Hosts from gwos Host Webservice  and ["
				+ hostsandvms.size() 
                + "] HostsandVms from CloudHub (VSystem=" 
				+ vGwosConfig.getVirtualEnvType() 
				+ ")" );

		for( String hostOrVm : hostsandvms.keySet() )
		{
			vemaBaseObject = hostsandvms.get( hostOrVm );

			log.debug( "Host or VM Name ( " + hostOrVm + " ) ---TYPE--- ( " + vemaBaseObject.getType() + " )" );

			if( !(gwosHostList.contains( hostOrVm )) )
			{
				log.debug( "Host: '" + hostOrVm
						+ "' TYPE >>> '" + vemaBaseObject.getType()
						+ "' Enum Type Return; '"
						+ VemaBaseObject.VemaObjectEnum.HOST + "'" );

				if( vemaBaseObject.getType() == VemaBaseObject.VemaObjectEnum.HOST )
				{
					vemaBaseObject = hostsandvms.get( hostOrVm ); // redundant!
					vemaBaseHost   = vemaBaseObject.getHost();

					log.info( "Hypervisor '" + vemaBaseHost.getHostName()
							+ "' in CloudHub but not in GWOS. Will be added" );

					comparedHostList.add( vemaBaseHost );

					log.debug( "Temp Host List size: [" + comparedHostList.size() + "]" );
				}
				else if (vemaBaseObject.getType() == VemaBaseObject.VemaObjectEnum.VM)
				{
					vemaBaseObject = hostsandvms.get( hostOrVm );
					vemaBaseVM     = vemaBaseObject.getVM();

					log.info( "Virtual Machine '" + vemaBaseVM.getVMName()
							+ "' in CloudHub but not in GWOS. Will be added" );

					comparedVMList.add( vemaBaseVM );

					log.debug( "Filtered VM NAME: '" + vemaBaseVM.getVMName()
							+ "', TempList VM size: [" + comparedVMList.size()
							+ "]" );
				}
				else
				{
					// should NEVER get here.
					log.error( "Unknown host/vm type: '" + vemaBaseObject.getType() + "'" );
				}
			}
			/* Create a stringlist of all Hypervisers and Virtual Machines in VM */
			hostsOrVmsList.add( hostOrVm );
		}

		if (comparedHostList.size() > 0)  // i.e. if there are new hosts to tell GWOS about
		{
			log.info( "Adding [" + comparedHostList.size() + "]" + " Hypervisors to the gwosService" );

			gwosService.addHypervisors( comparedHostList );
		}

		if (comparedVMList.size() > 0)  // i.e. if there are new VMs to tell GWOS about
		{
			log.info( "Adding [" + comparedVMList.size() + "]" + " Virtual Machines to the gwosService" );

			gwosService.addVirtualMachines( comparedVMList );
		}
		// End Compare gwosHostList and vemaHostList

		// Delete Hypervisors
		// TBD: Rename method because it deletes VM's and Hypervisors
		deleteHypervisors( gwosHostList, hostsOrVmsList );

		// call GWOS getHotGroup Webservice.
		List<String> gwosHostGroupList = (List<String>) getGwosHostGroupNameService( vGwosConfig );

		log.debug( "Total Number of HostGroups from GwosHostGroup Service: '" + gwosHostGroupList.size() + "'" );

		/* Returns a list of Hostgroup names without the prefix */
		prefixlessHostgroupList = stripHostGroupList( gwosHostGroupList );

		// Clear lists before it gets used
		ListOfHypervisors.clear();
		ListOfHypervisorsToAdd.clear();

		for( String hostKey : hosts.keySet() )
		{
			if (!hostKey.equalsIgnoreCase( VemaConstants.HOSTLESS_VMS ))
				ListOfHypervisors.add( hostKey );

			hgVemaBaseObject = hostsandvms.get( hostKey );

			if (!prefixlessHostgroupList.contains( hgVemaBaseObject.getHost().getHostName() ))
			{
				log.debug( hgVemaBaseObject.getHost().getHostName() + " not in prefixlessHostGroups: " + prefixlessHostgroupList.toString() );
				hostGroups = new GWOSHostGroup( 
						gwosService.getHostGroupName(
								VemaConstants.ENTITY_HYPERVISOR, 
								hgVemaBaseObject.getHost().getHostName() ), 
						hypervisorVema,
						connectorVema,
						applicationType );

				ListOfHypervisorsToAdd.add( hgVemaBaseObject.getHost().getHostName() );

				gwosService.addHostgroup( hostGroups );
			}
		}

		log.debug( "Host Groups are added to GWOS Server #["
				+ ListOfHypervisorsToAdd.size() + "]" );

		// add mamagement server to hostgroup
		String hostGroupName = gwosService.getHostGroupName(
				VemaConstants.ENTITY_MGMT_SERVER,
				vGwosConfig.getVirtualEnvServer() );

		mgmthg = new GWOSHostGroup( hostGroupName, mgmtServerVema, connectorVema, applicationType );

		if (!(gwosHostGroupList.contains( hostGroupName )))
		{
			log.debug( "ADDING ManagementServer '" + hostGroupName + "' to HostGroup" );
			gwosService.addHostgroup( mgmthg );
		}
		log.debug( "Modifying MgmtServer '" + hostGroupName
				+ "' to HostGroup with HypervisorList of '"
				+ ListOfHypervisors.size() + "' elements" );

		gwosService.modifyHostgroup( mgmthg, ListOfHypervisors );

		// Delete hostGroupList not present in hypervisorList
		log.debug( "Check which HostGroups no longer present in GWOS system (for deletion)" );

		for( String gwosHostGroupName : prefixlessHostgroupList )
		{
			if (!(hostsOrVmsList.contains( gwosHostGroupName )))
			{
				/* Need to determine if it's a management or hypervisor */
				if (gwosHostGroupList.contains( 
                        gwosService.getHostGroupName(
                            VemaConstants.ENTITY_HYPERVISOR, gwosHostGroupName ) ))
				{
					log.debug( "Delete this HostGroup: '" + gwosHostGroupName + "'" );

					deleteHostGroups = new GWOSHostGroup(
							gwosService.getHostGroupName(
									VemaConstants.ENTITY_HYPERVISOR,
									gwosHostGroupName ), 
                            hypervisorVema,
							connectorVema,
							applicationType
							);

					gwosService.deleteHostgroup( deleteHostGroups );
				}
				else if (!gwosHostGroupList.contains( 
                    gwosService.getHostGroupName( 
                        VemaConstants.ENTITY_MGMT_SERVER,
                        vGwosConfig.getVirtualEnvServer() ) ))
				{
					/* It's not the management server so it should be removed */
					log.debug( "Delete special HostGroup: '" + gwosHostGroupName + "'" );

					deleteHostGroups = new GWOSHostGroup(
							gwosService.getHostGroupName(
									VemaConstants.ENTITY_MGMT_SERVER,
									gwosHostGroupName ), 
                            hypervisorVema,
							connectorVema,
							applicationType
							);

					gwosService.deleteHostgroup( deleteHostGroups );
				}
			}
		}

		// ModifyHostGroup for Hypervisor List
		// Iterate through the vemaHostList and get the VM's for each Host.
		VemaBaseHost vbHost = null;
		VemaBaseVM   vmName = null;

		for( VemaBaseHost hypervisorHostList : hosts.values() )
		{
			vbHost = hosts.get( hypervisorHostList.getHostName() );

			for( String vms : vbHost.getVMPool().keySet() )
			{
				vmName = vbHost.getVM( vms );
				if (vmName != null)
					vmList.add( vmName.getVMName() );
				else
					log.error( "Alert: vmname is (null)" );
			}

			// Create the HostGroups for VMs
			hypervisorHostGroup = new GWOSHostGroup(
					gwosService.getHostGroupName(
							VemaConstants.ENTITY_HYPERVISOR,
							hypervisorHostList.getHostName() ), 
                    hypervisorVema,
					connectorVema,
					applicationType
					);

			// -----------------------------------------------------------------------
			// This code Proves that all VMs end up in the right Hostgroups.
			// Now... as to why the from-GWOS hostgroups don't get trimmed of the 
			// vms that have been moved (or deactivated)... that's another question
			// -----------------------------------------------------------------------
			StringBuffer sb = new StringBuffer( 1000 );
			sb.append( "\nHypervisorHostGroup: '" + hypervisorHostGroup.getHostGroupName() + "'\n" );
			for( String vmHandle : vmList )
			{
				sb.append( "modifylist: '" + vmHandle + "'\n" );
			}
			log.debug( sb.toString() );
			
			// Modify the HostGroup with VM's
			gwosService.modifyHostgroup( hypervisorHostGroup, vmList );

			vmList.clear();
		}
		return hosts;
	}

	/**
	 * This method Deletes the list of Hypervisors from gwos server
	 * 
	 */
	private void deleteHypervisors( List<String> gwosHostList, List<String> hostsOrVmsList )
	{
		VemaBaseHost deleteHosts = null;
		List<VemaBaseHost> deleteHostList = new ArrayList<VemaBaseHost>();

		for( String gwosHostName : gwosHostList )
        {
			if (!(hostsOrVmsList.contains( gwosHostName )))
			{
				deleteHosts = new VemaBaseHost( gwosHostName );
				deleteHostList.add( deleteHosts );
			}
        }

		if (deleteHostList.size() > 0)
			log.debug( "Delete  [" + deleteHostList.size() + "]"
					+ " Hypervisors and VM's from the GroundWork system" );

		if (deleteHostList.size() > 0)
			gwosService.deleteHypervisors( deleteHostList );
	}

	/***
	 * This method seperates the prefix from the hostGroupNames in the
	 * gwosHostGroupList
	 * 
	 * @param gwosHostGroupList
	 * @return
	 */
	public List<String> stripHostGroupList( List<String> gwosHostGroupList )
	{
		List<String> strippedNames = new ArrayList<String>();
		log.debug( "prefixless input list: " + gwosHostGroupList.toString() );
		for( String gwosHGName : gwosHostGroupList )
		{
			StringTokenizer st = new StringTokenizer( gwosHGName, ":" );

			// JUST the first splitting of {tag}:{hostname}
			String hgType = st.hasMoreTokens() ? st.nextToken() : null;
			String hgName = st.hasMoreTokens() ? st.nextToken() : null;

			if (hgName != null)
				strippedNames.add( hgName );
			else
				log.error( "hgName detokenizer returned null (pat=':') in gwosHGName='" + gwosHGName + "'" );
		}
		return strippedNames;
	}

	public void monitorData( ConcurrentHashMap<String, VemaBaseHost> hostsList,
			VEMAGwosConfiguration vGwosConfig )
	{
		String hostGroupName = null;
		String mgmtServer = gwosService.getHostGroupName(
				VemaConstants.ENTITY_MGMT_SERVER,
				vGwosConfig.getVirtualEnvServer() );

		log.debug( "Monitor Process Started" );

		List<VemaBaseHost> listOfHypervisors = new ArrayList<VemaBaseHost>();
		List<VemaBaseVM> listOfVM = new ArrayList<VemaBaseVM>();

		log.debug( "Host List Size: "
				+ (hostsList == null ? "null.size() = 0" : hostsList.size()) );

		if (hostsList == null)
			return; // there's nothing to do - hostsList is null.

		for( VemaBaseHost host : hostsList.values() )
		{
			log.debug( "Hypervisor to update: '" + host.getHostName() + "'" );

			host.setHostGroup( mgmtServer );
			listOfHypervisors.add( host );

			hostGroupName = gwosService.getHostGroupName(
					VemaConstants.ENTITY_HYPERVISOR, host.getHostName() );

			for( VemaBaseVM vm : host.getVMPool().values() )
			{
				vm.setHostGroup( hostGroupName );
				listOfVM.add( vm );
			}
		}
		log.debug( "Call Modify Hypervisors" );
		gwosService.modifyHypervisors( listOfHypervisors );

		log.debug( "Call Modify VirtualMachines" );
		gwosService.modifyVirtualMachines( listOfVM );

		log.debug( "Total number of Hypervisors that will be updated: '" + listOfHypervisors.size() + "'" );
		log.debug( "Total number of VM's        that will be updated: '" + listOfVM.size() + "'" );
		log.debug( "Monitor Process Ended" );
	}

	/**
	 * This is a Run method used to call the syncMonitorAgentData method to sync
	 * the GWOSHostList and VemaHostList every 60sec.
	 * 
	 */

	public synchronized void run()
	{
		log.warn( "Monitoring thread ( re- ) started. This should be an infrequent event" );

		log.debug( "If New Configuration Created or Existing Configuration Modified ?   "
				+ VemaBaseState.isGWOSConfigurationUpdated() );

		ConcurrentHashMap<String, VemaBaseHost> vemaHosts = null;
		ConcurrentHashMap<String, VemaBaseHost> virtualMonitoredHosts = null;
		ConcurrentHashMap<String, VemaBaseObject> hostsandvms = null;

		/* initialize starting condition */
		setIsRunning( true );

		long startTime = 0;
		while( isRunning() ) // "while forever" is intentional!
		{
			if (vemaComaTimer.secondsToGo() == 0)
			{
				log.error( "CloudHub is unresponsive ( no activity for more than "
						+ vemaComaTimer.secondsPeriod() / 60 + " minute(s)" );
				
				vemaComaTimer.reset();

				setIsRunning( false );

				if (gwosService != null)
					gwosService.sendEventMessage( 
							null, 
							null, 
							null, 
							"HIGH",
							"CloudHub unresponsive, will restart", 
							null 
							);
				else
					log.error( "EVENT message not sent - no GWOS service yet!" );

				return;
			}

			try
			{
				long remainder = System.currentTimeMillis() % 1000L;

				Thread.sleep( 9 * 1000 + remainder ); // always wait 10 seconds
														// at least.
			}
			catch( InterruptedException ie )
			{
				log.error( "run sleep was interrupted. Error: " + ie );
				setIsRunning( false );
				return;
			}

			if (VemaBaseState.isSuspendMonitorAgentCollector())
			{
				log.warn( "Monitoring suspended from the UI." );
				continue; // short circuit to "do nothing, but loop forever"
			}
			/*
			 * Check if CloudHub has been configured or if the configuration has
			 * changed
			 */
			if (VemaBaseState.isGWOSConfigurationUpdated()
			|| (vGwosConfig == null))
			{
				log.debug( "Reading the configuration file: '" + gwosConfigFilename + "'" );
				this.vGwosConfig = MonitorAgentConfigXMLToBean
						.gwosConfigXMLToBean( gwosConfigFilename );

				// Configuration can be null and therefore no action until a
				// config file is available */
				if (this.vGwosConfig != null)
				{
					vema.disconnect();
					gwosService = new GWOSConnectorServiceUnified(
							vGwosConfig.getGwosServer(),
							Integer.parseInt( vGwosConfig.getGwosPort() ),
							this.mgmtServerVema, // VemaConstants.MGMT_SERVER_VMWARE,
							this.hypervisorVema, // VemaConstants.HYPERVISOR_VMWARE
							this.applicationType,	// "VEMA" or "CHRHEV" or ...
							this.gwosConfigFilename,
							this.vemaMonitorProfileFilename );

					/*
					 * Adjust monitoring interval with latest value from
					 * configuration file
					 */
					this.vemaMonitorTimer = new VemaBaseTimer( "vemaMonitor", vGwosConfig.getCheckInterval(), 0 );
					logonce( "Monitoring interval set to '" + vGwosConfig.getCheckInterval() + "' Minutes" );

					this.vemaSyncTimer = new VemaBaseTimer( "vemaSync", vGwosConfig.getSyncInterval(), 0 );
					logonce( "Syncing interval set to '" + vGwosConfig.getSyncInterval() + "' Minutes" );

					this.vemaComaTimer = new VemaBaseTimer( "vemaComa", vGwosConfig.getComaInterval(), 0 );
					logonce( "COMA-detection interval set to '" + vGwosConfig.getComaInterval() + "' Minutes" );

					/*
					 * Set interval in GWOSConnectorServiceUnified. It's used to
					 * calculate next check time
					 */
					gwosService.setIntervalTime( vGwosConfig.getCheckInterval() );

					// Call Vema Configuration Bean to get Vema Config
					// Information and Metrics
					VemaMonitoring vemaConfig = 
                            MonitorAgentConfigXMLToBean.vemaXMLToBean( vemaMonitorProfileFilename );

					// Iterate through the metrics and set it to VemaBaseQuery
					// 121212.rlynch: made the ADDING conditional on
					// monitoring/graphing
					hypervisorMetrics.clear();
					for( Metric hypervisorMetricsFromXML : vemaConfig.getHypervisor().getMetric() )
					{
						if( hypervisorMetricsFromXML.isMonitored()
                        ||  hypervisorMetricsFromXML.isGraphed() )
							hypervisorMetrics.add( new VemaBaseQuery(
											(String)  hypervisorMetricsFromXML.getName(),
											(int)     hypervisorMetricsFromXML.getWarningThreshold(),
											(int)     hypervisorMetricsFromXML.getCriticalThreshold(),
											(boolean) hypervisorMetricsFromXML.isGraphed(),
											(boolean) hypervisorMetricsFromXML.isMonitored() ) );
					}
					vmMetrics.clear();
					for( Metric vmMetricXML : vemaConfig.getVm().getMetric() )
					{
						if( vmMetricXML.isMonitored() 
                        ||  vmMetricXML.isGraphed() )
							vmMetrics.add( new VemaBaseQuery(
									(String)  vmMetricXML.getName(),
									(int)     vmMetricXML.getWarningThreshold(),
									(int)     vmMetricXML.getCriticalThreshold(),
									(boolean) vmMetricXML.isGraphed(),
									(boolean) vmMetricXML.isMonitored() ) );
					}
					VemaBaseState.setGWOSConfigurationUpdated( false );
				}
			}

			/* We have a valid configuration to run the monitoring */
			if( this.vGwosConfig != null )
			{
				log.info( "Heartbeat of CloudHub Agent" );
				log.debug( "\n" 
                        + "vemaSync                             = '" + vemaSyncTimer.secondsToGo() + "'\n"
						+ "vemaComa                             = '" + vemaComaTimer.secondsToGo() + "'\n"
						+ "vemaMonitor                          = '" + vemaMonitorTimer.secondsToGo() + "'\n"
						+ "vGwosConfig Object inside RUN Method = '" + vGwosConfig.getVirtualEnvServer() + "'\n"
						+ "Get Connection State                 = '" + vema.getConnectionState() + "'\n" + ""
                        );

				parambox.put( "vema", "api", "fqhost",     vGwosConfig.getVirtualEnvServer() != null 
                                                         ? vGwosConfig.getVirtualEnvServer() : "" ); 
                                                    // "eng-rhev-m-1.groundwork.groundworkopensource.com"

				parambox.put( "vema", "api", "user",       vGwosConfig.getVirtualEnvUser() != null 
                                                         ? vGwosConfig.getVirtualEnvUser() : "" ); 
                                                    // "admin" );

				parambox.put( "vema", "api", "password",   vGwosConfig.getVirtualEnvPassword() != null 
                                                         ? vGwosConfig.getVirtualEnvPassword() : "" ); 
                                                    // "#m3t30r1t3"

				parambox.put( "vema", "api", "realm",      vGwosConfig.getVirtualEnvRealm() != null 
                                                         ? vGwosConfig.getVirtualEnvRealm() : "" ); 
                                                    // "internal" );

				parambox.put( "vema", "api", "port",       vGwosConfig.getVirtualEnvPort() != null 
                                                         ? vGwosConfig.getVirtualEnvPort() : "443" ); 
                                                    // "443" );

				parambox.put( "vema", "api", "protocol",   vGwosConfig.getVirtualEnvProtocol() != null 
                                                         ? vGwosConfig.getVirtualEnvProtocol() : "https" ); 
                                                    // "https"

				parambox.put( "vema", "api", "baseuri",    vGwosConfig.getVirtualEnvURI() != null 
                                                         ? vGwosConfig.getVirtualEnvURI() : "" ); 
                                                    // "/api" );

				parambox.put( "vema", "api", "certsfile",  vGwosConfig.getCertificateStore() != null 
                                                         ? vGwosConfig.getCertificateStore() : "" ); 
                                                    // "/usr/java/latest/jre/lib/security/cacerts"

				parambox.put( "vema", "api", "certspass",  vGwosConfig.getCertificatePassword() != null 
                                                         ? vGwosConfig.getCertificatePassword() : "" ); 
                                                    // "changeit"

				parambox.put( "vema", "api", "sslenabled", vGwosConfig.isVirtualEnvSSLEnabled() 
                                                         ? "true" : "false" );

				parambox.put( "vema", "api", "type", "rhev" );
				
				if( log.isDebugEnabled() )
					log.debug( parambox.formatSelf() );

				// Make vema Connection...
				if( vema.getConnectionState() != VemaConstants.ConnectionState.CONNECTED ) 
				{
					try
					{
						vema.connect( parambox );
						/*
						 * vema.connect( getVirtualEnvProtocolString() +
						 * vGwosConfig.getVirtualEnvServer() + "/" +
						 * vGwosConfig.getVirtualEnvURI(),
						 * vGwosConfig.getVirtualEnvUser(),
						 * vGwosConfig.getVirtualEnvPassword(),
						 * vGwosConfig.getVirtualEnvType() );
						 */
					}
					catch( Exception exc )
					{
						log.error( "Connection of CloudHub to Cloud failed: " + exc.getMessage() );
						setIsRunning( false );
						return;
					}
				}

				try
				{
					// ---------------------------------------------------------
					// SYNC process
					// ---------------------------------------------------------
					// finds hypervisors and vms.  compares to hypervisors and 
					// vms coming "back" from GroundWork.  Creates the update
					// instructions to cause hostgroup changes in Groundwork. 
					//
					vemaHosts = null;  // so it can count as a 'boolean' too.
					
					if( vemaSyncTimer.isReadyAndReset() ) // checks AND resets if true...
					{
						logonce( "Gathering Hosts+VMs using getListHost()" );

                        // IMPORTANT
                        // BIG - this is where the data is gotten
						vemaHosts = vema.getListHost( null, hypervisorMetrics, vmMetrics );

						if( vemaHosts == null )
						{
							log.error( "Virtualization API returned no Hypervisors" );
							setIsRunning( false );
							return;
						}
						else
						{
							log.debug( "Number of hosts discovered by CloudHub: '"
									+ vemaHosts.size() + "'"
									/* + "\n" + vema.formatGetListHost( vemaHosts ) */
									);
						}

						hostsandvms = vema.getHostAndVM( vemaHosts );

						/* Start time for monitoring */
						startTime = System.currentTimeMillis();
						syncMonitorAgentData( vema, vGwosConfig, vemaHosts, hostsandvms );

						log.debug( "Time to execute sync operation ["
								+ (System.currentTimeMillis() - startTime)
								+ "] ms  (hosts & VMs: " + hostsandvms.size() + ")");

						/*
						 * Make sure that monitor is started after the 2nd sync
						 * to update any PENDING state
						 */
						if( this.bForceMonitorAfterSync )
						{
							logonce( "Triggering monitoring in 2nd sync to update PENDING metrics" );
							vemaMonitorTimer.resetAndTrigger();
							this.bForceMonitorAfterSync = false;
						}

						/*
						 * First sync completed. Set flag to force monitor after
						 * the next sync operation
						 */
						if( this.bFirstTimeSync )
						{
							this.bFirstTimeSync = false;
							this.bForceMonitorAfterSync = true;
						}
						/* Reset timer to current time */
						vemaSyncTimer.reset();
						vemaComaTimer.reset();
					}
					/* Ready for Monitoring */
					if( vemaMonitorTimer.isReadyAndReset() )
					{
						startTime = System.currentTimeMillis();
						logonce( "Start the Monitor Process" );
						virtualMonitoredHosts = vema.getListHost(
								virtualMonitoredHosts, hypervisorMetrics, vmMetrics );

						/* Update metrics for configured metrics */
						monitorData( virtualMonitoredHosts, vGwosConfig );
						logonce( "Time to execute monitor operation ["
								+ (System.currentTimeMillis() - startTime)
								+ "] ms" );

						/* Monitoring done reset time */
						vemaMonitorTimer.reset();
						vemaComaTimer.reset();
					}
					else
					{
						log.debug( 
								"CloudHub Monitor sec2go(" 
								+ vemaMonitorTimer.secondsToGo() 
								+ ")"
								);
					}
				}
				catch( WSFoundationException e )
				{
					log.error( "Foundation exception in run method: " + e
							+ "\n" + e.getStackTrace()[0] + "\n"
							+ e.getStackTrace()[1] + "\n"
							+ e.getStackTrace()[2] + "\n"
							+ e.getStackTrace()[3] + "\n"
							+ e.getStackTrace()[4] + "\n"
							+ e.getStackTrace()[5] + "\n"
							+ e.getStackTrace()[6] + "\n"
							+ e.getStackTrace()[7] );
					setIsRunning( false );
				}
				catch( RemoteException e )
				{
					log.error( "Remote exception in run method: " + e + "\n"
							+ e.getStackTrace()[0] + "\n"
							+ e.getStackTrace()[1] + "\n"
							+ e.getStackTrace()[2] + "\n"
							+ e.getStackTrace()[3] + "\n"
							+ e.getStackTrace()[4] + "\n"
							+ e.getStackTrace()[5] + "\n"
							+ e.getStackTrace()[6] + "\n"
							+ e.getStackTrace()[7] );
					setIsRunning( false );
				}
				catch( ServiceException e )
				{
					log.error( "Service exception in run method: " + e + "\n"
							+ e.getStackTrace()[0] + "\n"
							+ e.getStackTrace()[1] + "\n"
							+ e.getStackTrace()[2] + "\n"
							+ e.getStackTrace()[3] + "\n"
							+ e.getStackTrace()[4] + "\n"
							+ e.getStackTrace()[5] + "\n"
							+ e.getStackTrace()[6] + "\n"
							+ e.getStackTrace()[7] );
					setIsRunning( false );
				}
				catch( Exception e )
				{
					log.error( "General exception in run method: " + e + "\n"
							+ e.getStackTrace()[0] + "\n"
							+ e.getStackTrace()[1] + "\n"
							+ e.getStackTrace()[2] + "\n"
							+ e.getStackTrace()[3] + "\n"
							+ e.getStackTrace()[4] + "\n"
							+ e.getStackTrace()[5] + "\n"
							+ e.getStackTrace()[6] + "\n"
							+ e.getStackTrace()[7] );
					setIsRunning( false );
				}
			}
		}
	}

	private ConcurrentHashMap<String, Boolean>	everSeen	= new ConcurrentHashMap<String, Boolean>();

	private void logonce( String message )
	{
		if( everSeen.get( message ) == null )
		{
			everSeen.put( message, true );
			log.info( message );
		}
		else
		{
			log.info( message );
		}
	}
}
