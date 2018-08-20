package com.groundwork.agents.vema.vmware.connector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Date;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPFaultException;

import org.apache.log4j.Logger;

import com.groundwork.agents.vema.api.Vema;
import com.groundwork.agents.vema.api.VemaConstants.ConnectionState;
import com.groundwork.agents.vema.api.VemaConstants;
import com.groundwork.agents.vema.base.VemaBaseHost;
import com.groundwork.agents.vema.base.VemaBaseMetric;
import com.groundwork.agents.vema.base.VemaBaseObject;
import com.groundwork.agents.vema.base.VemaBaseObjectTree;
import com.groundwork.agents.vema.base.VemaBaseQuery;
import com.groundwork.agents.vema.base.VemaBaseSynthetic;
import com.groundwork.agents.vema.base.VemaBaseVM;
import com.groundwork.agents.vema.exception.VEMAException;
import com.groundwork.agents.vema.utils.aPad;
import com.groundwork.agents.vema.utils.ParamBox;

import com.vmware.vim25.ArrayOfGuestNicInfo;
import com.vmware.vim25.ArrayOfManagedObjectReference;
import com.vmware.vim25.ArrayOfPerfCounterInfo;
import com.vmware.vim25.DynamicProperty;
import com.vmware.vim25.GuestNicInfo;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ObjectContent;
import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.PerfEntityMetric;
import com.vmware.vim25.PerfEntityMetricBase;
import com.vmware.vim25.PerfMetricId;
import com.vmware.vim25.PerfMetricIntSeries;
import com.vmware.vim25.PerfMetricSeries;
import com.vmware.vim25.PerfQuerySpec;
import com.vmware.vim25.PerfSampleInfo;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertySpec;
import com.vmware.vim25.RetrieveOptions;
import com.vmware.vim25.RetrieveResult;
import com.vmware.vim25.SelectionSpec;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.TraversalSpec;
import com.vmware.vim25.VimPortType;
import com.vmware.vim25.VimService;
import com.vmware.vim25.InvalidPropertyFaultMsg;

// import java.io.BufferedReader;       // were used in examples, may not be now
// import java.io.InputStreamReader;
// import java.io.File;
// import javax.xml.parsers.*;


/**
 * <pre>
 * VemaVMware
 *
 * A utility-application to scan virtual-machine processing cloud
 * APIs ( application-programming-interfaces ), translating the results
 * obtained, on a periodic basis, to the format( s ) that may directly
 * be used by the GroundWorkOpenSystems ( GWOS ) systems management
 * software.
 *
 *
 * The authorization parameters are:
 *
 * accessauth            [req]: establish the authorization node
 * url | system          [req]: url of the web service
 * user[name]            [req]: username for the authentication
 * pass[word]            [req]: password for the authentication
 * vm[name]              [req]: name of the vm to address
 *
 * OPERATING DESIGN:
 *
 * </pre>
 */

public class VemaVMware implements Vema
{
	private String                       vemaUrl;      // 'url' afore; think of as "PM"
	private String                       vemaLogin;    // only be ONE connection-object
	private String                       vemaPassword; // per application.
	private String                       vemaVM;       // virtual machine within URL

    private boolean doHosts              = true;
    private boolean doVMs                = true;
    private boolean doStorageDomains     = false;
    private boolean doNetworks           = false;
    private boolean doResourcePools      = false;
    private boolean doClusters           = false;
    private boolean doDataCenters        = false;

	private static final ManagedObjectReference	SVC_INST_REF	= new ManagedObjectReference();
	private static final String					SVC_INST_NAME	= "ServiceInstance";

	private VimService                   vimService;
	private VimPortType                  vimPort;
	private ServiceContent               serviceContent;

	private ManagedObjectReference       sessionManager;
	private ManagedObjectReference       propertyCollector;
	private ManagedObjectReference       rootFolder;
	private ManagedObjectReference       perfManager;
	private ManagedObjectReference       searchIndex;
	private ManagedObjectReference       viewManager;
	
	private ArrayList<String>            hostFilters;
	private ArrayList<String>            VMFilters;

    private ConnectionState              vemaState       = ConnectionState.NASCENT;
    private int                          vemaRetries     = 0;
    private static final int             MAXRETRIES      = 3;  //internal, arbitrary
    private static final long            RETRYGAP        = 5L * 1000L;  // 5 secs...

    private int                          minSkippedBeforeDefunct = 0;  // settable
    private int                          minSkippedBeforeDelete  = 1;  // settable
    private int                          minTimeBeforeDefunct    = 0 * 60; // seconds
    private int                          minTimeBeforeDelete     = 1 * 60; // seconds

    private int                          everWarned      = 0;  // better than a boolean!
    
    private static org.apache.log4j.Logger log = Logger.getLogger( VemaVMware.class );
    
	public VemaVMware()	// constructor
	{
        vemaPassword = "";              // because an empty password is acceptable
	}
	
    public void setCollectionMode( boolean doHosts, boolean doVMs, boolean doStorageDomains, boolean doNetworks, boolean doResourcePools, boolean doClusters, boolean doDataCenters)
    {
        this.doHosts           = doHosts;
        this.doVMs             = doVMs;
        this.doStorageDomains  = doStorageDomains;
        this.doNetworks        = doNetworks;
        this.doResourcePools   = doResourcePools;
        this.doClusters        = doClusters;
        this.doDataCenters     = doDataCenters;
    }

    public String getStateString()
    {
        return
            vemaState == ConnectionState.NASCENT      ? "Nascent connection (untried)"
        :   vemaState == ConnectionState.CONNECTING   ? "Connecting to service"
        :   vemaState == ConnectionState.CONNECTED    ? "Connected"
        :   vemaState == ConnectionState.FAILED       ? "Connection failed"
        :   vemaState == ConnectionState.DISCONNECTED ? "Disconnected"
        :   vemaState == ConnectionState.TIMEDOUT     ? "Timed out (" + vemaRetries + ")"
        :   /* ... else ... */                          "getStateString() ... unknown state";
    }

    public ConnectionState getConnectionState()
    {
        if( vemaState == ConnectionState.CONNECTED )
        {
            // ----------------------------------------------------------------------
            // so, we'll do a connection TEST
            // ----------------------------------------------------------------------
        	ManagedObjectReference cViewRef   = null;
        	ManagedObjectReference viewMgrRef = serviceContent.getViewManager();
        	ManagedObjectReference propColl   = serviceContent.getPropertyCollector();
            List<String> listContainers = new ArrayList<String>();
            
            try 
            {
                cViewRef = vimPort.createContainerView(
            				viewMgrRef,
            				serviceContent.getRootFolder(),
            				listContainers,
            				true);
                cViewRef = null;  // explicitly toss the object.
            }
            catch ( Exception e )
            {
                log.error( "vimPort.createContainerView(check-connection-state) exception: " + e );
                vemaState = ConnectionState.DISCONNECTED;
            }
        }
    	return vemaState;
    }
    
    public void setDefunctCriteria( int minSkipped, int minTime, int dieSkipped, int dieTime )
    {
    	minSkippedBeforeDefunct = minSkipped;
    	minTimeBeforeDefunct    = minTime;
    	minSkippedBeforeDelete  = dieSkipped;
    	minTimeBeforeDelete     = dieTime;
    }
    
    public void connect( ParamBox parameterBox )
    {
        try {
            String protocol =   parameterBox.get( "vema", "api", "protocol" ) != null
                           &&   parameterBox.get( "vema", "api", "protocol" ).length() > 0 
                           && ! parameterBox.get( "vema", "api", "protocol" ).endsWith( ":" ) 
                           ?    parameterBox.get( "vema", "api", "protocol" ) + ":"
                           :    parameterBox.get( "vema", "api", "protocol" );

            String baseuri  =   parameterBox.get( "vema", "api", "baseuri" ) != null
                           &&   parameterBox.get( "vema", "api", "baseuri" ).length() > 0 
                           && ! parameterBox.get( "vema", "api", "baseuri" ).startsWith( "/" ) 
                           ?    "/" + parameterBox.get( "vema", "api", "baseuri" )
                           :          parameterBox.get( "vema", "api", "baseuri" );

        	String url =
        			""   + protocol +
        			"//" + parameterBox.get( "vema","api","fqhost" ) +
        			":"  + parameterBox.get( "vema","api","port" ) +
        			""   + baseuri ;
        	
            connect(url,
            		parameterBox.get( "vema", "api", "user"       ),
            		parameterBox.get( "vema", "api", "password"   ),
            		""
            		);
        }
        catch ( Exception e )
        {
            log.error( e );
        }
    }

	private void connect( String url, String login, String pass, String vm )
        throws VEMAException
	{
		String              es = "";
		StringBuffer timeStamp = new StringBuffer(100);
		long         startTime = System.currentTimeMillis();
		
		if(url   == null || url.isEmpty())    es += "{url} needed\n";
		else                                  vemaUrl = url;
		
		if(login == null || login.isEmpty())  es += "{login} needed\n";
		else                                  vemaLogin = login;
		
		if(pass  == null /* empty is OK */ )  es += "{password} needed\n";
		else                                  vemaPassword = pass;
		
		// if(vm    == null || vm.isEmpty())     es += "{virtual machine} needed\n";
		// else                                  vemaVM = vm;

		if( getConnectionState() == ConnectionState.CONNECTED )
			return;
		
        if(!(vemaState == ConnectionState.NASCENT
        ||   vemaState == ConnectionState.DISCONNECTED
        ||   vemaState == ConnectionState.TIMEDOUT
        ) )
        {
            es += vemaState + ": incorrect state for connect()\n";
        }

		if( !es.isEmpty() )
			throw new VEMAException( es );

		String lastException = null;
		for( vemaRetries = 0; vemaRetries < MAXRETRIES; vemaRetries++ )
		{
	        try
	        {
	            vemaState = ConnectionState.CONNECTING;
	            attach();   // will throw exception if doesn't work.
	            vemaState = ConnectionState.CONNECTED;
	            lastException = null;
	            break;
	        }
			catch ( Exception e )
			{
	            vemaState = ConnectionState.TIMEDOUT;
	            lastException = new String(e.getMessage());
	            try { Thread.sleep( RETRYGAP ); } catch ( Exception ee ) {}
			}
		}
		
		if( lastException != null )
			 log.error( "Vema Connect - last exception (" + lastException + ")");
		else log.debug( "Vema connect(" + url + ", " + login + ", " + pass + ", null)" );
		
		timeStamp.append( url + ": connect(" 
				+ Double.toString( (System.currentTimeMillis() - startTime) / 1000.0 ) + ") ");
        // log.info( "Vema timestamp: " + timeStamp.toString());

        if( vemaState != ConnectionState.CONNECTED )
        {
			throw new VEMAException( 
                "Failed to Attach: Exceeded max retries "
                + "(exception = '" + lastException + "' "
                + "login = '"  + login + "' "
                + "pass = '"   + pass  + "' "
                + "url = '"    + url   + "' "
                + ") \n"
                + 
                ( everWarned++ > 0 ? "" : (
                "----------------------------------------------------------------------\n" +
                "- REMINDER: when many login/authentication errors                     \n" + 
                "- accumulate, upon actually correcting the password and/or login      \n" + 
                "- name, communications with the vSphere/ESXi server will be blocked   \n" + 
                "- by the vSphere/ESXi server for approximately 10-15 minutes.         \n" + 
                "- This appears to be an undocumented security-enhancement feature     \n" + 
                "- to prevent automated password-search by denial-of-service           \n" + 
                "- mechanisms.  After the 10-15 minute period, login should            \n" + 
                "- happen normally.                                                    \n" + 
                "-                                                                     \n" + 
                "- KEY TEST: try attaching to [ https://{URL of server}/mob ];         \n" + 
                "- you should be prompted for a login name and a password.  IF the     \n" + 
                "- hacking-delay has been exceeded (in other words, if the minimum     \n" + 
                "- amount of time has elapsed), then your replacement name and         \n" + 
                "- password should work.  Otherwise, you'll just get more of these     \n" + 
                "- error status messages.]                                             \n" +
                "----------------------------------------------------------------------\n"
                ))
            );
        }
	}

    public String formatGetListHost( ConcurrentHashMap<String, VemaBaseHost> glh )
    {
        StringBuilder s = new StringBuilder();

        s.append( "\n" );
        s.append( "-------------------------------------------\n" );
        s.append( "           getListHost object:\n" );
        s.append( "-------------------------------------------\n" );
        for( String key : glh.keySet() )
        {
            s.append( "- - - - - - - - - - - - - - - - - - - - - - \n" );
            s.append( String.format( "%-40s: (getListHost formatted output)\n%s", key, glh.get( key ).formatSelf() ));
        }
        s.append( "-------------------------------------------\n" );

        return s.toString();
    }


    public void getInventoryTreeTool() throws Exception
    {
    	//log.debug( "serviceContent.getViewManager()\n..." + 
    //	"type: '" + serviceContent.getViewManager().getType() + "'\n..." + 
    	//"value: [" + serviceContent.getViewManager().getValue() + "]" );

 /*   	log.debug( "serviceContent.getRootFolder().getType() [.getValue()]" + 
    	serviceContent.getRootFolder().getType() + " [" +
    	serviceContent.getRootFolder().getValue() + "]" );
    	
    	log.debug( "serviceContent.getDynamicType() = " + 
    	serviceContent.getDynamicType() + "" );

    	log.debug( "serviceContent.getPropertyCollector() = " + 
    	serviceContent.getPropertyCollector().getType()
    	);
    	
    	log.debug( "serviceContent.getAbout().getApiType() = " +
    	serviceContent.getAbout().getApiType()
    	);
    	
    	log.debug( "serviceContent.getAbout().getApiVersion() = " +
    	serviceContent.getAbout().getApiVersion()
    	);
    	
    	log.debug( "serviceContent.getAbout().getBuild() = " +
    	serviceContent.getAbout().getBuild()
    	);
    	
    	log.debug( "serviceContent.getAbout().getFullName() = " +
    	serviceContent.getAbout().getFullName()
    	);
    	
    	log.debug( "serviceContent.getAbout().getLicenseProductName() = " +
    	serviceContent.getAbout().getLicenseProductName()
    	);
    	
    	log.debug( "serviceContent.getAbout().getLicenseProductVersion() = " +
    	serviceContent.getAbout().getLicenseProductVersion()
    	);
    	
    	log.debug( "serviceContent.getAbout().getName() = " +
    	serviceContent.getAbout().getName()
    	);
    	
    	log.debug( "serviceContent.getAbout().getOsType() = " +
    	serviceContent.getAbout().getOsType()
    	);
    	
    	log.debug( "serviceContent.getAbout().getVendor() = " +
    	serviceContent.getAbout().getVendor()
    	);
    	
    	log.debug( "serviceContent.getAbout().getVersion() = " +
    	serviceContent.getAbout().getVersion()
    	);
    	
    	log.debug( "serviceContent.getAbout().getDynamicProperty() = " +
    	serviceContent.getAbout().getDynamicProperty()
    	);
    	*/
    	ManagedObjectReference mor0 = null;
  
    	mor0 = serviceContent.getRootFolder();
        //log.debug( "serviceContent.getRootFolder().getType() = '" + mor0.getType() + "'");

        ManagedObjectReference searchIndex = serviceContent.getSearchIndex();
        //log.debug( "serviceContent.getSearchIndex().getType() = '" + searchIndex.getType() + "'");
    }
    
    public ConcurrentHashMap<String, VemaBaseObject> getHostAndVM( ConcurrentHashMap< String, VemaBaseHost> hostTree )
    {
    	    ConcurrentHashMap<String, VemaBaseObject> result = 
    	new ConcurrentHashMap<String, VemaBaseObject>();
    	
    	if( hostTree == null )
    		return result;  // the empty list.
    	
    	for( String hostkey : hostTree.keySet() )
    	{
    		VemaBaseHost   hosto = hostTree.get( hostkey );
    		String      hostname = hosto.getHostName();
    		String hostGroupName = hosto.getHostGroup();
    		
    		result.put(hostname, 
    				new VemaBaseObject( 
    						hostname, 
    						VemaBaseObject.VemaObjectEnum.HOST, 
    						hosto, 
    						null ));

    		for( String vmkey : hosto.getVMPool().keySet() )
    		{
    			VemaBaseVM vmo = hosto.getVM( vmkey );
    			String  vmname = vmo.getVMName();
    			result.put(vmname, 
    					new VemaBaseObject( 
    							vmname, 
    							VemaBaseObject.VemaObjectEnum.VM, 
    							null, 
    							vmo ));
    		}
    	}
    	// return result.size() == 0 ? null : result;  // using NULL as hard flag
    	return result;
    }
    
    /**
     * print out the vema-object internal variables that are
     * of significant importance in debugging.  
     * 
     * @return
     */
    public String getInternals()
    {
    	StringBuffer output = new StringBuffer( 5000 );
    	aPad a = new aPad();

    	output.append(a.Pad("]", "Vema URL   [ " + vemaUrl + " ]\n"));
        output.append(a.Pad("]", "Vema Login [ " + vemaLogin + " ]\n"));
        output.append(a.Pad("]", "Vema Passwd[ " + "*******" + " ]\n"));
        output.append(a.Pad("]", "Vema vmSeed[ " + vemaVM + " ]\n"));
        output.append(a.Pad("]", "Vema STATE [ " + vemaState + " ]\n"));
        
        if( hostFilters != null )
        	for( String filter : hostFilters )
        		output.append(a.Pad("]", "host Filter[ " + filter + " ]\n"));
        
        if( VMFilters != null )
        	for( String filter : VMFilters )
        		output.append(a.Pad("]", "VM   Filter[ " + filter + " ]\n"));

    	return output.toString();
    }

    public ConcurrentHashMap<String, VemaBaseHost> getListHost( 
    		ConcurrentHashMap<String, VemaBaseHost> priorVBH, 
    		List<VemaBaseQuery>hostQueries, 
    		List<VemaBaseQuery>vmQueries )
    {
    	return getListHost( priorVBH, hostQueries, vmQueries, true, true, true );
    }
    
    public ConcurrentHashMap<String, VemaBaseHost> getListHost( 
    		ConcurrentHashMap<String, VemaBaseHost> priorVBH, 
    		List<VemaBaseQuery>                     hostQueries, 
    		List<VemaBaseQuery>                     vmQueries, 
    		boolean                                 onlyRequestedMetrics,
    		boolean                                 crushMetricsIfDown,
    		boolean                                 deleteDefunctMembers )
    {
        ConcurrentHashMap<String, VemaBaseHost>   hostPool = new ConcurrentHashMap<String, VemaBaseHost>();
        ConcurrentHashMap<String, VemaVMwareVM>     vmPool = new ConcurrentHashMap<String, VemaVMwareVM>();
        ConcurrentHashMap<String, VemaBaseQuery> queryPool = new ConcurrentHashMap<String, VemaBaseQuery>();
        
        long startTime = System.currentTimeMillis();  // capture "now"
        StringBuffer timeStamp = new StringBuffer(100);
        
        if( priorVBH == null )	// safety check BEFORE any return()
        	priorVBH = new ConcurrentHashMap<String, VemaBaseHost>();

        if( vemaState != ConnectionState.CONNECTED )
        {
            log.error( "getListHost(): not connected" );
            return priorVBH;
        }
    	ManagedObjectReference viewMgrRef = serviceContent.getViewManager();
    	ManagedObjectReference propColl   = serviceContent.getPropertyCollector();
    	
        List<String> listContainers = new ArrayList<String>();
        listContainers.add("VirtualMachine");  // OK, creates output
        listContainers.add("HostSystem");      // creates no more output
//      listContainers.add("ResourcePool");    // creates MORE output
//      listContainers.add("Network");         // creates yet more output
//0     listContainers.add("DataStore");       // causes SOAP error.
//1     listContainers.add("DataCenter");      // causes SOAP error.
//      listContainers.add("Folder");          // creates no more output
//      listContainers.add("ComputeResource"); // creates no more output

        ManagedObjectReference cViewRef = null;
        
        try 
        {
            cViewRef = vimPort.createContainerView(
        				viewMgrRef,
        				serviceContent.getRootFolder(),
        				listContainers,
        				true);
        }
        catch ( Exception e )
        {
            log.error( "vimPort.createContainerView(...) exception: " + e );
        }

        timeStamp.append( "VMware obj(" 
				+ Double.toString( (System.currentTimeMillis() - startTime) / 1000.0 ) + ") ");

        TraversalSpec 
        tSpec = new TraversalSpec();
        tSpec.setName("traverseEntities");
        tSpec.setType("ContainerView");
        tSpec.setPath("view");
        tSpec.setSkip(false);
        
        TraversalSpec 
        tSpecVMN = new TraversalSpec();
        tSpecVMN.setType("VirtualMachine");
        tSpecVMN.setPath("network");
        tSpecVMN.setSkip(false);

        tSpec.getSelectSet().add(tSpecVMN);
        
        TraversalSpec 
        tSpecVMRP = new TraversalSpec();
        tSpecVMRP.setType("VirtualMachine");
        tSpecVMRP.setPath("resourcePool");
        tSpecVMRP.setSkip(false);

        tSpec.getSelectSet().add(tSpecVMRP);
        
        // create an object spec to define the beginning of the traversal;
        ObjectSpec 
        oSpec = new ObjectSpec();
        oSpec.setObj(cViewRef);
        oSpec.setSkip(true);
        oSpec.getSelectSet().add(tSpec);
        oSpec.getSelectSet().add(tSpecVMN);
        oSpec.getSelectSet().add(tSpecVMRP);

        PropertySpec pSpecVM = new PropertySpec();
		pSpecVM.setType("VirtualMachine");
		
		VemaVMwareVM dummyVM = new VemaVMwareVM("dummy");

        for( VemaBaseQuery accessor : dummyVM.getDefaultMetricList() )
        {
            queryPool.put( accessor.getQuery(), accessor);
            if(! accessor.getQuery().startsWith("syn.")
            && ! accessor.getQuery().startsWith("perfcounter")
            && ! accessor.getQuery().endsWith( ".scaled" ))
            	pSpecVM.getPathSet().add( accessor.getQuery() );
        }
        
        for( VemaBaseQuery accessor : dummyVM.getDefaultConfigList() )
        {
            queryPool.put( accessor.getQuery(), accessor);
            if(! accessor.getQuery().startsWith("syn.")
            && ! accessor.getQuery().startsWith("perfcounter")
            && ! accessor.getQuery().endsWith( ".scaled" ))
            	pSpecVM.getPathSet().add( accessor.getQuery() );
        }
        
        for( VemaBaseQuery accessor : dummyVM.getDefaultSyntheticList() )
        {
            queryPool.put( accessor.getQuery(), accessor);
            if(! accessor.getQuery().startsWith("syn.")
            && ! accessor.getQuery().startsWith("perfcounter")
            && ! accessor.getQuery().endsWith( ".scaled" ))
            	pSpecVM.getPathSet().add( accessor.getQuery() );
        }
        
        // this is LAST in order to OVERRIDE the above defaults
        for( VemaBaseQuery accessor : vmQueries )
        {
        	queryPool.put( accessor.getQuery(), accessor);
            if(! accessor.getQuery().startsWith("syn.")
            && ! accessor.getQuery().startsWith("perfcounter")
            && ! accessor.getQuery().endsWith( ".scaled" ))
            	pSpecVM.getPathSet().add( accessor.getQuery() );
        }
        
        //log.debug("debug 2");

        PropertySpec pSpecHost = new PropertySpec();
        pSpecHost.setType("HostSystem");
        
        VemaVMwareHost dummyHost = new VemaVMwareHost("dummy");
        
        for( VemaBaseQuery accessor : dummyHost.getDefaultMetricList() )
        {
            queryPool.put( accessor.getQuery(), accessor);
            if(! accessor.getQuery().startsWith("syn.")
            && ! accessor.getQuery().startsWith("perfcounter")
            && ! accessor.getQuery().endsWith( ".scaled" ))
            	pSpecHost.getPathSet().add( accessor.getQuery() );
        }
        
        for( VemaBaseQuery accessor : dummyHost.getDefaultConfigList() )
        {
            queryPool.put( accessor.getQuery(), accessor);
            if(! accessor.getQuery().startsWith("syn.")
            && ! accessor.getQuery().startsWith("perfcounter")
            && ! accessor.getQuery().endsWith( ".scaled" ))
            	pSpecHost.getPathSet().add( accessor.getQuery() );
        }
        
        for( VemaBaseQuery accessor : dummyHost.getDefaultSyntheticList() )
        {
            queryPool.put( accessor.getQuery(), accessor);
            if(! accessor.getQuery().startsWith("syn.")
            && ! accessor.getQuery().startsWith("perfcounter")
            && ! accessor.getQuery().endsWith( ".scaled" ))
            	pSpecHost.getPathSet().add( accessor.getQuery() );
        }
        
        // This is LAST in the sequence to ensure OVERRIDE capability.
        for( VemaBaseQuery accessor : hostQueries )
        {
            queryPool.put( accessor.getQuery(), accessor);
            if(! accessor.getQuery().startsWith("syn.")
            && ! accessor.getQuery().startsWith("perfcounter")
            && ! accessor.getQuery().endsWith( ".scaled" ))
            	pSpecHost.getPathSet().add( accessor.getQuery() );
        }

        PropertyFilterSpec pfsHost = new PropertyFilterSpec();
    	pfsHost.getObjectSet().add(oSpec);
    	pfsHost.getPropSet().add(pSpecHost);

    	PropertyFilterSpec pfsVM = new PropertyFilterSpec();
    	pfsVM.getObjectSet().add(oSpec);
    	pfsVM.getPropSet().add(pSpecVM);
    	
    	List<PropertyFilterSpec> fSpecList = new ArrayList<PropertyFilterSpec>();
    	fSpecList.add(pfsHost);
    	fSpecList.add(pfsVM);
    	
    	RetrieveOptions ro = new RetrieveOptions();
        ArrayList<ObjectContent> ocList = new ArrayList<ObjectContent>();
        //log.debug("debug 3");
        //ro.setMaxObjects( 100 );

        try
        {
            boolean firstround = true;
            RetrieveResult props = null;
            int i = 0;

            //log.debug("retrieveVIM25 benchmark START");
            while( true )
            {
                if(firstround) 
                    props = vimPort.retrievePropertiesEx(propColl, fSpecList, ro);
                else if( props.getToken() != null )
                    props = vimPort.continueRetrievePropertiesEx( propColl, props.getToken() );
                else
                    break;
                
                //log.debug("retrieveVIM25 benchmark iteration #" + (++i));

                firstround = false;

                if( props != null )
                    ocList.addAll( props.getObjects() );
            }
        }
        catch( InvalidPropertyFaultMsg e )
        {
            log.error( 
            		"\n" 
            		+ "retrievePropertiesEx() error ='" + e + "'" + "\n"
            		+ "Localized Message            ='" + e.getLocalizedMessage() + "'" + "\n"
            		+ "Message                      ='" + e.getMessage() + "'" + "\n"
            		+ "Cause                        ='" + e.getCause() + "'" + "\n"
            		+ "FaultInfo                    ='" + e.getFaultInfo() + "'" + "\n"
            		+ "FaultInfo.Name               ='" + e.getFaultInfo().getName() + "'" + "\n"
            		+ "FaultInfo.Cause              ='" + e.getFaultInfo().getFaultCause() + "'" + "\n"
            		+ "-----------------------------------------------------------------------------" + "\n"
            		+ "Disable '" + e.getFaultInfo().getName() + "' (to not graphed/monitored) in the CloudHub config" + "\n"
            		+ "... or remove from the '"
                    + VemaConstants.CONFIG_FILE_PATH
                    + VemaConstants.VMWARE_PROFILE_FILE
                    + VemaConstants.CONFIG_FILE_EXTN
                    + "' file\n"
            		+ "-----------------------------------------------------------------------------" + "\n"
            		);
        }
        catch( Exception e )
        {
            log.error( "vimPort.retrievePropertiesEx(.2.) error: " + e );
            log.error( "localized msg: '" + e.getLocalizedMessage() + "', msg: '" + e.getMessage() + "', cause='" + e.getCause() + "'");
        }
        //log.debug("debug 4");

        timeStamp.append( "vmcall(" 
				+ Double.toString( (System.currentTimeMillis() - startTime) / 1000.0 ) + ") ");

        // ----------------------------------------------
        // Let's look for VM objects first.
        // (because they link INTO the host objects)
        // ----------------------------------------------
        for( ObjectContent oc : ocList )
        {
            String  name     = null;
            String  path     = null;
            Boolean postcast = true;
            String  ocType   = oc.getObj().getType();
            String  ocValue  = oc.getObj().getValue();

            if( ! ocType.equals( "VirtualMachine" ) )
                // skip everything except VirtualMachine, and get the next one
                continue;

            //log.debug( "0a: " + ocValue + " = " + ocType );
            
            List<DynamicProperty> dpList = oc.getPropSet();
            if( dpList == null )
                continue;
            
            // -----------------------------------------------
            // PASS1: make receiver objects, on finding "name"
            // -----------------------------------------------
            VemaVMwareVM  vmObj = null;    // VM object

            for( DynamicProperty dp : dpList )
            {
                path = dp.getName();
                name = dp.getVal().toString();

                if( path.equals("name"))
                {
                    vmPool.put( ocValue, vmObj = new VemaVMwareVM( name ) );
                    break;  // end of search-through-for-NAME
                }
            }

            if( vmObj == null )
            {
                // TODO: this is an error state.  There MUST be a name
                log.error( "ERROR: virtual machine object == NULL" );
                continue;  // if its NULL, its bad, so for now skip it.
            }

            // -----------------------------------------------
            // PASS2: fill up the object
            // -----------------------------------------------
            for( DynamicProperty dp : dpList )
            {
                path     = dp.getName();
                name     = dp.getVal().toString();
                postcast = true;
                
                if( path.equals( "guest.net" ) )
                {
                    if( dp.getVal() instanceof ArrayOfGuestNicInfo )
                    {
                        List<GuestNicInfo> gniList = 
                    	  ((ArrayOfGuestNicInfo)dp.getVal()).getGuestNicInfo();

                        for( GuestNicInfo gni : gniList )
                        {
                            //log.debug( "3: " + path + " = " + gni.getMacAddress());
                            if( vmObj != null )
                            {
                            	VemaBaseQuery  vbq = queryPool.get(path);
                            	VemaBaseMetric vbm = new VemaBaseMetric(
                            			path,
                            			vbq.getWarning(),
                            			vbq.getCritical(),
                            			vbq.isGraphed(),
                            			vbq.isMonitored()
                            			);
                            	if(vbq.isTraced())
                            		vbm.setTrace();
                            	vbm.setValue(gni.getMacAddress());
                                vmObj.setMacAddress( gni.getMacAddress() );
                                vmObj.putConfig( path, vbm );
                                	
                                postcast = false; // block end code
                            }
                            break;  // only do FIRST item, explicitly!
                        }
                    }
                }
                else if( path.equals( "summary.runtime.host" ) )
                {
                    if( dp.getVal() instanceof ManagedObjectReference )
                    {
                    	ManagedObjectReference mor = (ManagedObjectReference)dp.getVal();
                        //log.debug( "4: " + path + " = " + mor.getValue() + " / " + mor.getType());

                        vmObj.setHypervisor( mor.getValue() );
                        name = mor.getValue();
                    }
                }
                else if( path.equals( "guest.ipAddress" ) )
                {
                    vmObj.setIpAddress( name );
                }
                else if( path.equals( "guest.guestState" ) )
                {
                    vmObj.setGuestState( name );
                }
                else if( path.equals("summary.runtime.bootTime"))
                {
                	vmObj.setBootDate( name, VemaConstants.vmWareDateFormat );
                }
                else if( path.equals("summary.quickStats.uptimeSeconds")
                	 ||  path.equals("summary.quickStats.uptime") 
               	     )
                {
                	vmObj.setLastUpdate( name );
                }
                else
                {
                	// empty: ON PURPOSE.  Keep it that way (or use it)
                	//        but don't remove it!
                }
                
                if( postcast )
                {
                	VemaBaseQuery  vbq = queryPool.get(path);
                	VemaBaseMetric vbm = new VemaBaseMetric(
                			path,
                			vbq.getWarning(),
                			vbq.getCritical(),
                			vbq.isGraphed(),
                			vbq.isMonitored()
                			);
                	if(vbq.isTraced())
                		vbm.setTrace();
                	vbm.setValue(name);
                	
                	if( path.startsWith( "summary.quickStats" )
                    ||  path.startsWith( "summary.runtime" )
                    ||  path.startsWith( "perfcounter" )
                    )
                	{
                		vmObj.putMetric( path, vbm );
	                }
	                else
	                {
	                    vmObj.putConfig( path, vbm );
	                }
                    //log.debug( "5: path(" + path + ") = name(" + name + ")" );
                }
                else
                {
                    //log.debug( "6: path(" + path + ") = name(" + name + ")" );
                }
            }
            
            // now ALL metrics have been collected an assigned.
            // this is the per-vm place where supplimental 
            // statistics are computed.  Scaled. 
            for( String query : queryPool.keySet() )  // SCALED value adjustments here
            {
            	if( !query.endsWith( ".scaled" ))
            		continue;  // move along

            	VemaBaseQuery     vbq = queryPool.get(query);
            	VemaBaseMetric    vbm = new VemaBaseMetric(
            			query,
            			vbq.getWarning(),
            			vbq.getCritical(),
            			vbq.isGraphed(),
            			vbq.isMonitored()
            			);

            	String result = "uncomputed";
//----------------------------------------------------------------------
//- WHEN vm's get scaled/computed values,they go here...               -
//----------------------------------------------------------------------
//            	if( query.equals( "summary.hardware.cpuMhz.scaled"  ) )
//            	{
//            		double scaled = 1.0;
//            		scaled *= (cpuMhz      == null) ? 1.0 : cpuMhz;
//            		scaled *= (numCpuCores == null) ? 1.0 : numCpuCores;
//            		result = Double.toString( scaled );
//            	}

            	vbm.setValue( result );

            	vmObj.putMetric( query, vbm );
            }

            for( String query : queryPool.keySet() )
            {
            	if( !query.startsWith("syn.vm."))
            		continue;  // not one of ours.

            	VemaBaseSynthetic vbs;
            	VemaBaseQuery     vbq = queryPool.get(query);
            	VemaBaseMetric    vbm = new VemaBaseMetric(
            			query,
            			vbq.getWarning(),
            			vbq.getCritical(),
            			vbq.isGraphed(),
            			vbq.isMonitored()
            			);

            	String result = "uncomputed";
            	if( ( vbs = dummyVM.getSynthetic(query)) != null )
            	{
                	String value1 = vmObj.getValueByKey(vbs.getLookup1());
                	String value2 = vmObj.getValueByKey(vbs.getLookup2());
                	
                	result = String.valueOf( vbs.compute(value1, value2) ) + "%";
            	} 
                vbm.setValue( result );

            	if(vbq.isTraced())
            		vbm.setTrace();

            	vmObj.putMetric( query, vbm );
            }

            // Do NOT move this above the foregoing vmObj filler-code.  The 
            // MonitorState can only be computed once all the above is done.
            vmObj.setRunState( vmObj.getMonitorState() );
    	}
        //log.debug("debug 5");

        // ----------------------------------------------
        // Now, the HOST objects, since VM objects done
        // ----------------------------------------------
        for( ObjectContent oc : ocList )
        {
            String name      = null;
            String path      = null;
            Boolean postcast = true;

            String ocType    = oc.getObj().getType();
            String ocValue   = oc.getObj().getValue();

            if( ! ocType.equals( "HostSystem" ) )
                // if NOT HostSystem, just skip
                continue;

            //log.debug( "0b: " + ocValue + " = " + ocType );
            
            List<DynamicProperty> dpList = oc.getPropSet();
            if( dpList == null )
                continue;
            
            // -----------------------------------------------
            // PASS1: make receiver objects, on finding "name"
            // -----------------------------------------------
            VemaVMwareHost hostObj = null;    // VM object

            for( DynamicProperty dp : dpList )
            {
                path = dp.getName();
                name = dp.getVal().toString();

                if( path.equals( "name" ))
                {
                    hostPool.put( ocValue, hostObj = new VemaVMwareHost( name ) );

                    break;  // end of search-through-for-NAME
                }
            }

            if( hostObj == null )
            {
                // TODO: this is an error state.  There MUST be a name
                log.error( "ERROR: host object == NULL" );
                continue;  // break out, object must have name
            }

            // -----------------------------------------------
            // PASS2: fill up the object
            // -----------------------------------------------
            Double cpuMhz      = null;
            Double numCpuCores = null;
            
            for( DynamicProperty dp : dpList )
            {
                path     = dp.getName();
                name     = dp.getVal().toString();
                postcast = true;

                if( path.equals( "vm" ) )
                {
                    if( dp.getVal() instanceof ArrayOfManagedObjectReference )
                    {
                        List<ManagedObjectReference> morList = 
                    	  ((ArrayOfManagedObjectReference)dp.getVal()).getManagedObjectReference();

                        for( ManagedObjectReference vmmor : morList )
                        {
                            VemaVMwareVM v = vmPool.get( vmmor.getValue() );
                            hostObj.putVM( vmmor.getValue(), v );
                        /*    log.debug( 
                                    "2b: " + path + 
                                    " = " + vmmor.getValue() + 
                                    " / " + 
                                    ( v == null ? "null" : v.getVMName()) );*/
                            postcast = false;
                        }
                    }
                }
                else if( path.equals( "hardware.network.ipAddress" ) ) // not supported
                {
                    hostObj.setIpAddress( name );
                }
                else if( path.equals( "hardware.network.macAddress" ) ) // not supported
                {
                    hostObj.setMacAddress( name );
                }
                else if ( path.equals( "summary.hardware.cpuMhz" ))
            	{
                	cpuMhz = Double.parseDouble( name );
            	}
                else if ( path.equals( "summary.hardware.numCpuCores"))
                {
                	numCpuCores = Double.parseDouble( name );
                }
                else if( path.equals("summary.runtime.bootTime"))
                {
                	hostObj.setBootDate( name, VemaConstants.vmWareDateFormat );
                }
                else if( path.equals("summary.quickStats.uptimeSeconds")
                	 ||  path.equals("summary.quickStats.uptime") 
                	 )
                {
                	hostObj.setLastUpdate( name );
                }
                else if( path.equals("summary.hardware.model"))
                {
                	hostObj.setDescription( name );
                }
                else
                {
                	// empty: ON PURPOSE.  Keep it that way (or use it)
                	//        but don't remove it!
                }

                if( postcast )
                {
                	VemaBaseQuery  vbq = queryPool.get(path);
                	VemaBaseMetric vbm = new VemaBaseMetric(
                			path,
                			vbq.getWarning(),
                			vbq.getCritical(),
                			vbq.isGraphed(),
                			vbq.isMonitored()
                			);
                	if(vbq.isTraced())
                		vbm.setTrace();
                	vbm.setValue(name);
                	
                	if( path.startsWith( "summary.quickStats" )
                    ||  path.startsWith( "summary.runtime" )
                    ||  path.startsWith( "perfcounter" )
                    )
                	{
                		hostObj.putMetric( path, vbm );
	                }
	                else
	                {
	                    hostObj.putConfig( path, vbm );
	                }
                	//log.debug( "8: " + path + " = " + name );
                }
                else
                {
                	//log.debug( "9: " + path + " = " + name );
                }
            }

            // now ALL metrics have been collected and assigned.
            // this is the per-hypervisor place where supplimental 
            // statistics are computed.  Scaled. 
            for( String query : queryPool.keySet() )  // SCALED value adjustments here
            {
            	if( !query.endsWith( ".scaled" ))
            		continue;  // move along

            	VemaBaseQuery     vbq = queryPool.get(query);
            	VemaBaseMetric    vbm = new VemaBaseMetric(
            			query,
            			vbq.getWarning(),
            			vbq.getCritical(),
            			vbq.isGraphed(),
            			vbq.isMonitored()
            			);

            	String result = "uncomputed";
            	if( query.equals( "summary.hardware.cpuMhz.scaled"  ) )
            	{
            		double 
            		scaled  = 1.0;
            		scaled *= (cpuMhz      == null) ? 1.0 : cpuMhz;
            		scaled *= (numCpuCores == null) ? 1.0 : numCpuCores;

            		result = Double.toString( scaled );

                    if( numCpuCores != null && numCpuCores > 1.01 )  // .01 added for floating point compare
                        log.debug( String.format( "Scaled %.1f MHz (x %.0f) = %.1f", cpuMhz, numCpuCores, scaled ));
            	}
            	vbm.setValue( result );

            	if(vbq.isTraced())
            		vbm.setTrace();

            	hostObj.putMetric( query, vbm );
            }
            // it is important to do "synthetics" after "scaled" objects,
            // since one of the main uses of scaled values is the computation
            // of more accurate relative values.
            
            for( String query : queryPool.keySet() )
            {
            	if( !query.startsWith("syn.host."))
            		continue;  // move along, not one of ours.

            	VemaBaseSynthetic vbs;
            	VemaBaseQuery     vbq = queryPool.get(query);
            	VemaBaseMetric    vbm = new VemaBaseMetric(
            			query,
            			vbq.getWarning(),
            			vbq.getCritical(),
            			vbq.isGraphed(),
            			vbq.isMonitored()
            			);
            	String result = "uncomputed";
            	if( ( vbs = dummyHost.getSynthetic(query)) != null )
            	{
                	String value1 = hostObj.getValueByKey(vbs.getLookup1());
                	String value2 = hostObj.getValueByKey(vbs.getLookup2());
                	
                	result = String.valueOf(vbs.compute(value1, value2)) + "%";
            	} 
            	vbm.setValue( result );

            	if(vbq.isTraced())
            		vbm.setTrace();

            	hostObj.putMetric( query, vbm );
            }

            // Do NOT move this above the foregoing hostObj filler-code.  The 
            // MonitorState can only be computed once all the above is done.
            hostObj.setRunState( hostObj.getMonitorState() );
    	}
        //log.debug("debug 6");
        
        timeStamp.append( "premerge(" 
				+ Double.toString( (System.currentTimeMillis() - startTime) / 1000.0 ) + ") ");

		// while not specifically warned against, the use of a separate
        // list here ensures that the keys can be CHANGED, and that the 
        // loop will terminate.  (Otherwise, infinite loop a possibility)
        
        List<String> hostkeylist = 
            new ArrayList<String>(hostPool.keySet());

        for( String hostkey : hostkeylist )
        {
            // ---------------------------------------------------------
        	// A little tidying up of the host-name references in each
        	// of the virtual machine objects.  (They are first 'host-1234'
        	// format, the internal VMware naming convention.  This converts
        	// them into 'common name' format.
            // ---------------------------------------------------------
        	String hostname = hostPool.get(hostkey).getHostName();
        
        	// same termination deal, as above note.
        	List<String> vmkeylist = 
                new ArrayList<String>(hostPool.get(hostkey).getVMPool().keySet());
        	
        	for( String vmkey : vmkeylist )
        	{
        		hostPool.get(hostkey).getVM(vmkey).setHypervisor(hostname);
        		VemaBaseVM vmo = hostPool.get(hostkey).getVM(vmkey);
        		String vmname = vmo.getVMName();
        		// -----------------------------------------------------------
        		// DEFINITELY - don't want to do the following (although
        		// it would be real nice!) as it causes concurrancy violations
        		// in the runtime package.
        		// -----------------------------------------------------------
        		hostPool.get(hostkey).renameVM( vmkey, vmname );
        	}
        	VemaBaseHost hosto = hostPool.get(hostkey);
    		// -----------------------------------------------------------
        	// Same issue with concurrency.
        	hostPool.remove(hostkey);
        	hostPool.put(hostname, hosto);
        }
        //log.debug("debug 7");

        hListMerge( priorVBH, hostPool, deleteDefunctMembers );
        hostPool = null;   // explicitly kill off objects!
        //log.debug("debug 8");
        
        // ----------------------------------------------------------------------
        // now clear out the metrics that the upper code doesn't want to monitor
        // ----------------------------------------------------------------------
        // The special "object.getMergeCount() > 1" case below INITIALLY suppresses
        // the metric-crushing logic.  Thus, on new HOSTs and VMs, when they first
        // appear in the tree, their metrics will be passed along, whatever their 
        // values.  (VIM25 values).  After that, on the 2nd and subsequent merge, 
        // the metrics will be suppressed IF the getRunState is either DOWN or SUSPEND
        // ... which lightens traffic substantially, and reduces server-side load as well.
        //
        // NOTE: this is "> 1" (instead of >= 1) because these tests are done POST-merging
        //       where merging increments the merge counter.  Thus the brand new objects
        //       will have a merge-count of exactly 1, here.
        // ----------------------------------------------------------------------
        
        if( onlyRequestedMetrics || crushMetricsIfDown )  // a STRIP OUT operation!
        {
        	for( VemaBaseHost hosto : priorVBH.values() )
        	{
        		boolean crushHostMetrics = false;
        		
        		if( ( crushMetricsIfDown       ) &&
        		    ( hosto.getMergeCount() > 1) &&  // VERY useful special case (see above)
        		   (  hosto.getRunState().contains( "DOWN" )
        		   || hosto.getRunState().contains( "SUSPEND" )))
        			crushHostMetrics = true;

        		for( String metricName : hosto.getMetricPool().keySet() )
        			if( crushHostMetrics || !hosto.getMetric( metricName ).isMonitored() )
        				hosto.getMetricPool().remove(metricName);
        		
        		for( String configName : hosto.getConfigPool().keySet() )
        			if( crushHostMetrics || !hosto.getConfig( configName ).isMonitored() )
        				hosto.getConfigPool().remove(configName);

        		for( VemaBaseVM vmo : hosto.getVMPool().values() )
        		{
            		boolean crushVMMetrics = false;
            		
            		if( ( crushMetricsIfDown     ) &&
               		    ( vmo.getMergeCount() > 1) &&  // VERY useful special case (see above)
            		   (  vmo.getRunState().contains( "DOWN" )
            		   || vmo.getRunState().contains( "SUSPEND" )))
            			crushVMMetrics = true;

            		for( String metricName : vmo.getMetricPool().keySet() )
            			if( crushVMMetrics || !vmo.getMetric( metricName ).isMonitored() )
            				vmo.getMetricPool().remove(metricName);

        			for( String configName : vmo.getConfigPool().keySet() )
            			if( crushVMMetrics || !vmo.getConfig( configName ).isMonitored() )
            				vmo.getConfigPool().remove(configName);
            	}
        	}
        }
        timeStamp.append( "end(" 
				+ Double.toString( (System.currentTimeMillis() - startTime) / 1000.0 ) + ") ");

		log.debug( "getListHost(" + this.vemaUrl + ") timestamps: [" + timeStamp.toString() + "]");
        
        // return priorVBH.size() == 0 ? null : priorVBH;   // using NULL as flag
		return priorVBH;   // which must never be null by this point in code.
    }

    private void hListMerge( ConcurrentHashMap<String, VemaBaseHost> baseList, ConcurrentHashMap<String, VemaBaseHost> newList, boolean autoDelete )
    {
    	// NOTE: this "merges new into base" conceptually.
    	
    	// FIRST... clear out all the values. 
    	//                ------------------------------------------------------------
    	// 120910.rlynch: the PROBLEM with this chunk of code is that it establishes
    	//                null values in the metrics ... which doesn't sound bad, but
    	//                messes up the STATES for transition detection.  Don't do it!
    	//                ------------------------------------------------------------
//    	for( String host : baseList.keySet() )
//    	{
//    		VemaBaseHost hostObj = baseList.get(host);
//    		for( String hostMetric : hostObj.getMetricPool().keySet())
//    			hostObj.getMetric(hostMetric).setValue(null);
//
//    		for( String hostConfig : hostObj.getConfigPool().keySet())
//    			hostObj.getConfig(hostConfig).setValue(null);
//
//    		for( String vm : hostObj.getVMPool().keySet() )
//    		{
//    			VemaBaseVM vmObj = hostObj.getVM( vm );
//    			for( String vmMetric : vmObj.getMetricPool().keySet() )
//        			vmObj.getMetric(vmMetric).setValue(null);
//
//        		for( String vmConfig : vmObj.getConfigPool().keySet())
//        			vmObj.getConfig(vmConfig).setValue(null);
//    		}
//    	}
    	
    	// set up SKIP counters... to DETECT dropped/moved hosts & vms.
    	for( VemaBaseHost hosto : newList.values() )
    	{
    		hosto.incSkipped();
    		for( VemaBaseVM vmo : hosto.getVMPool().values() )
    			vmo.incSkipped();
    	}
    	
    	int added = 0;
    	// now try to merge in the newstuff...
    	for( String host : newList.keySet() )
    	{
    		// begin by ensuring target object exists, for hosts.
    		if(!baseList.containsKey(host))
    		{
    			added++;
    			baseList.put( host, new VemaVMwareHost(host) );
    		}

    		// and target object (virtual machines) exists, too.
    		for( String vm : newList.get(host).getVMPool().keySet() )
    		{
    			if(!baseList.get(host).getVMPool().containsKey(vm))
    			{
    				added++;
    				baseList.get(host).getVMPool().put( vm, new VemaVMwareVM(vm));
    			}
    		}
    		// now merge them
    		baseList.get(host).mergeInNew( newList.get(host) );
    	}
    	
    	// DELETION OF ORPHANED OBJECTS HERE  // This code deletes 'em.
		int deleted     = 0;
		int vmcounter   = 0;
		int hostcounter = 0;
		
    	for( String host : baseList.keySet() )
    	{
    		hostcounter++;
    		for( String vm : baseList.get( host ).getVMPool().keySet() )
    		{
    			vmcounter++;
    			if( autoDelete
    			&&  baseList.get( host ).getVM( vm ).isStale( minSkippedBeforeDelete, minTimeBeforeDelete ))
    			{
    				baseList.get( host ).getVMPool().remove( vm );
    				deleted++;
    				log.debug( "'" + vm + "'... orphaned VM deleted (min#:" + minSkippedBeforeDelete + " minT:" + minTimeBeforeDelete + ")" );
    			}
    		}
    		if( autoDelete
    		&&  baseList.get( host ).isStale( minSkippedBeforeDelete, minTimeBeforeDelete ))
    		{
    			baseList.remove( host );
    			deleted++;
				log.debug( "'" + host + "'... orphaned HOST deleted (min#:" + minSkippedBeforeDelete + " minT:" + minTimeBeforeDelete + ")" );
    		}
    	}
    	log.info( "Hypervisors: (" + hostcounter 
    			+ "),  VMs: ("       + vmcounter 
    			+ "),  Added: ("     + added 
    			+ "),  Deleted: ("   + deleted 
    			+ ")" );
    }
    
    public ArrayList<VemaBaseObject> getDefunctList( ConcurrentHashMap<String, VemaBaseHost> hostTree )
    {
    	ArrayList<VemaBaseObject> out = new ArrayList<VemaBaseObject>();
    	
    	for( String host : hostTree.keySet() )
    	{
    		for( String vm : hostTree.get( host ).getVMPool().keySet() )
            {
    			if( hostTree.get( host ).getVM( vm ).isStale( minSkippedBeforeDefunct, minTimeBeforeDefunct ))
    			{
    				out.add( new VemaBaseObject( 
    						vm, 
    						VemaBaseObject.VemaObjectEnum.VM, 
    						null, 
    						hostTree.get( host ).getVM(vm)));
    				log.debug( vm + "... orphaned VM" );
    			}
            }

    		if( hostTree.get( host ).isStale( minSkippedBeforeDefunct, minTimeBeforeDefunct ))
    		{
    			out.add( new VemaBaseObject( 
    					host, 
    					VemaBaseObject.VemaObjectEnum.HOST, 
    					hostTree.get( host ), 
    					null));
    			
    			log.debug( host + "... orphaned HOST" );
    		}
    	}
    	return out;
    }

    public ArrayList<String> getListVM( String targetHost )
    {
        ArrayList<String> returnValues = new ArrayList<String>();

        if( vemaState != ConnectionState.CONNECTED )
        {
    		log.error( "getListVM(): not connected" );
            return null;
        }
    	ManagedObjectReference viewMgrRef = serviceContent.getViewManager();
    	ManagedObjectReference propColl   = serviceContent.getPropertyCollector();
    	
        List<String> listContainers = new ArrayList<String>();
        listContainers.add("VirtualMachine");  // OK, creates output
//      listContainers.add("ResourcePool");    // creates MORE output
//      listContainers.add("Network");         // creates yet more output
//0     listContainers.add("DataStore");       // causes SOAP error.
//1     listContainers.add("DataCenter");      // causes SOAP error.
//      listContainers.add("Folder");          // creates no more output
//      listContainers.add("ComputeResource"); // creates no more output
//      listContainers.add("HostSystem");      // creates no more output

        ManagedObjectReference cViewRef = null;
        
        try 
        {
            cViewRef = vimPort.createContainerView(
        				viewMgrRef,
        				serviceContent.getRootFolder(),
        				listContainers,
        				true);
        }
        catch ( Exception e )
        {
            log.error( "vimPort.createContainerView(...) exception: " + e );
        }

        TraversalSpec 
        tSpec = new TraversalSpec();
        tSpec.setName("traverseEntities");
        tSpec.setPath("view");
        tSpec.setSkip(false);
        tSpec.setType("ContainerView");
        
        TraversalSpec 
        tSpecVMN = new TraversalSpec();
        tSpecVMN.setType("VirtualMachine");
        tSpecVMN.setPath("network");
        tSpecVMN.setSkip(false);

        tSpec.getSelectSet().add(tSpecVMN);
        
        TraversalSpec 
        tSpecVMRP = new TraversalSpec();
        tSpecVMRP.setType("VirtualMachine");
        tSpecVMRP.setPath("resourcePool");
        tSpecVMRP.setSkip(false);

        tSpec.getSelectSet().add(tSpecVMRP);
        
        // create an object spec to define the beginning of the traversal;
        ObjectSpec 
        oSpec = new ObjectSpec();
        oSpec.setObj(cViewRef);
        oSpec.setSkip(true);
        oSpec.getSelectSet().add(tSpec);
        oSpec.getSelectSet().add(tSpecVMN);
        oSpec.getSelectSet().add(tSpecVMRP);

        PropertySpec pSpec = new PropertySpec();
  		pSpec.setType("VirtualMachine");
        pSpec.getPathSet().add("name");
        
        PropertyFilterSpec fSpec = new PropertyFilterSpec();
    	fSpec.getObjectSet().add(oSpec);
    	fSpec.getPropSet().add(pSpec);
    	
    	List<PropertyFilterSpec> fSpecList = new ArrayList<PropertyFilterSpec>();
    	fSpecList.add(fSpec);
    	
    	RetrieveOptions ro = new RetrieveOptions();
        ArrayList<ObjectContent> ocList = new ArrayList<ObjectContent>();
        
        try
        {
            boolean firstround = true;      // these are temporary
            RetrieveResult props = null;

            while( true )
            {
                //                ---------------------------------------------------
                // 120620.rlynch: so... the [.retrievePropertiesEx] method only
                //                returns 100 results at a time.  This code works
                //                around that to gather ALL objects properties
                //                Also, setting [ro.setMaxObjects()] doesn't appear
                //                to change the 100-limit behavior at all.  Dumb.
                //                The method [props.getToken()] is NOT documented,
                //                but given the rest of the vmware schema, was 
                //                adduced without surprise.
                //                ---------------------------------------------------
                if(firstround) 
                    props = vimPort.retrievePropertiesEx(propColl, fSpecList, ro);
                else if( props.getToken() != null )
                    props = vimPort.continueRetrievePropertiesEx(
                            propColl, props.getToken() );
                else
                    break;

                firstround = false;

                if( props != null )
                    ocList.addAll( props.getObjects() );
            }
        }
        catch( Exception e )
        {
            log.error( "vimPort2.retrievePropertiesEx(...) error: " + e );
        }

        for( ObjectContent oc : ocList )
        {
            List<DynamicProperty> dpList = oc.getPropSet();

            if( dpList == null )
                continue;
            
            for( DynamicProperty dp : dpList )
            {
                if( dp.getName().equals("name") )
                    returnValues.add( (String) dp.getVal() );
            }
    	}

//        return returnValues.size() == 0 ? null : returnValues;  // using NULL as flag
        return returnValues;
    }

	private static class TrustAllTrustManager implements
			javax.net.ssl.TrustManager,
			javax.net.ssl.X509TrustManager
	{
		public java.security.cert.X509Certificate[] getAcceptedIssuers()
		{
			return null;
		}

		public boolean isServerTrusted(
				java.security.cert.X509Certificate[] certs )
		{
			return true;
		}

		public boolean isClientTrusted(
				java.security.cert.X509Certificate[] certs )
		{
			return true;
		}

		public void checkServerTrusted(
				java.security.cert.X509Certificate[] certs,
				String authType )
				throws java.security.cert.CertificateException
		{
			return;
		}

		public void checkClientTrusted(
				java.security.cert.X509Certificate[] certs,
				String authType )
				throws java.security.cert.CertificateException
		{
			return;
		}
	}

	private static void trustAllHttpsCertificates() throws Exception
	{
		// Create a trust manager that does not validate certificate chains:
		javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
		javax.net.ssl.TrustManager              tm = new TrustAllTrustManager();
		trustAllCerts[0]                           = tm;
		javax.net.ssl.SSLContext                sc = javax.net.ssl.SSLContext.getInstance( "SSL" );
		javax.net.ssl.SSLSessionContext      sslsc = sc.getServerSessionContext();
		sslsc.setSessionTimeout( 0 );
		sc.init( null, trustAllCerts, null );
		javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(
				sc.getSocketFactory());
	}

	/**
	 * Establishes session with the virtual center server.
	 *
	 * @throws Exception
	 *             the exception
	 */
	public void attach() throws Exception       // this was connect() from the VIM25 example
	{
		HostnameVerifier hv = new HostnameVerifier()
		{
			public boolean verify( String urlHostName, SSLSession session )
			{
                log.debug( "urlHostName = '" + urlHostName + "'" );
				return true;
			}
		};

		trustAllHttpsCertificates();
		HttpsURLConnection.setDefaultHostnameVerifier( hv );

		SVC_INST_REF.setType( SVC_INST_NAME );   // SVC_INST_NAME
		SVC_INST_REF.setValue( SVC_INST_NAME );  // is "ServiceInstance"

		vimService               = new VimService();
		vimPort                  = vimService.getVimPort();
		Map<String, Object> ctxt = (( BindingProvider ) vimPort ).getRequestContext();

		ctxt.put( BindingProvider.ENDPOINT_ADDRESS_PROPERTY, vemaUrl );
		ctxt.put( BindingProvider.SESSION_MAINTAIN_PROPERTY, true );      // keeps session open

		serviceContent           = vimPort.retrieveServiceContent( SVC_INST_REF );
		sessionManager           = serviceContent.getSessionManager();

		try
		{
			vimPort.login( sessionManager, vemaLogin, vemaPassword, null );
		}
		catch( Exception e )
		{
			log.debug( String.format( 
					"vimPort.login( url=%s, login=%s, pass=%-3.3s****, null ) - couldn't connect.  Exception '%s'\n", 
					vemaUrl, vemaLogin, vemaPassword, e.toString()));
			throw e;
		}

		propertyCollector        = serviceContent.getPropertyCollector();
		rootFolder               = serviceContent.getRootFolder();
		perfManager              = serviceContent.getPerfManager();
		searchIndex              = serviceContent.getSearchIndex();
	}

	/**
	 * Disconnects the user session.
	 *
	 * @throws Exception
	 */
	public void disconnect() throws VEMAException
	{
		if ( vemaState == ConnectionState.CONNECTED )
			try
            {
                vemaState = ConnectionState.DISCONNECTED;
			}
            catch (Exception e)
            {
				/* Logging an error */
			}
	}

    public VemaBaseObjectTree collect( VemaBaseObjectTree previous)
    {
        return previous; // TODO PLACE HOLDER - this is where the actual "action" will go.
    }

	/**
	 * Uses the new RetrievePropertiesEx method to emulate the now deprecated
	 * RetrieveProperties method.
	 *
	 * @param propertyFilterSpecList
	 * @return list of object content
	 * @throws Exception
	 */
	private List<ObjectContent> retrievePropertiesAllObjects(
			List<PropertyFilterSpec> propertyFilterSpecList )
			throws Exception
	{
		RetrieveOptions retrieveOptions = new RetrieveOptions();

		List<ObjectContent> objectContentList = new ArrayList<ObjectContent>();

		try
		{
			RetrieveResult results = vimPort.retrievePropertiesEx(
					propertyCollector,
					propertyFilterSpecList,
					retrieveOptions );
			
			if (results              != null
			&&  results.getObjects() != null
			&& !results.getObjects().isEmpty() )
			{
				objectContentList.addAll( results.getObjects() );
			}
			
			String token = null;
			
			if ( results != null )
				token = results.getToken();

			while ( token != null && !token.isEmpty() )
			{
				results = vimPort.continueRetrievePropertiesEx( 
						propertyCollector, token );
				token = null;

                // 120612.rlynch: apparently, its implemented as a poor man's linked list. 

				if ( results != null )
				{
					token = results.getToken();    // to follow the chain along...

					if ( results.getObjects() != null
					&&  !results.getObjects().isEmpty() )
					{
						objectContentList.addAll( results.getObjects() );
					}
				}
			}
		}
		catch ( SOAPFaultException sfe )
		{
			printSoapFaultException( sfe );
		}
		catch ( Exception e )
		{
			//log.debug( e + " : Failed Getting Contents" );
		}

		return objectContentList;  // then throw them back. 
	}

	/**
	 * This method initializes all the performance counters available on the
	 * system it is connected to. The performance counters are stored in the
	 * hashmap counters with group.counter.rolluptype being the key and id being
	 * the value.
	 */
	private  List<PerfCounterInfo> getPerfCounters()
	{
		List<PerfCounterInfo> pciList = new ArrayList<PerfCounterInfo>();

		try
		{
			// Create Property Spec
			PropertySpec
            propertySpec = new PropertySpec();
			propertySpec.setAll( false );
			propertySpec.getPathSet().add( "perfCounter" );
			propertySpec.setType( "PerformanceManager" );

			// Create Object Spec for perfManager
			ObjectSpec
            objectSpec = new ObjectSpec();
			objectSpec.setObj( perfManager );

			// Create PropertyFilterSpec using the PropertySpec and ObjectPec
			// created above.
			PropertyFilterSpec
            propertyFilterSpec = new PropertyFilterSpec();
			propertyFilterSpec.getPropSet().add( propertySpec );
			propertyFilterSpec.getObjectSet().add( objectSpec );

			List<PropertyFilterSpec>
            propertyFilterSpecList = new ArrayList<PropertyFilterSpec>();
			propertyFilterSpecList.add( propertyFilterSpec );

			List<ObjectContent>
            objectContentList = retrievePropertiesAllObjects( propertyFilterSpecList );

			if ( objectContentList != null )
			{
				for ( ObjectContent oc : objectContentList )
				{
					List<DynamicProperty> dpList = oc.getPropSet();
					if ( dpList != null )
					{
						for ( DynamicProperty dp : dpList )
						{
							List<PerfCounterInfo> pcinfolist
                                = (( ArrayOfPerfCounterInfo ) dp
									.getVal()).getPerfCounterInfo();
// ------------------------------------------------------------------------------------
// 120522: to [rlynch] this seems kind of bogus... search through all the properties?
// find one that seems to be a list, then assign it?  What if there are multiple lists?
// ------------------------------------------------------------------------------------
// if( pcinfolist == null ) log.info( "pcinfolist returns 'null'" );
// else                     log.info( "there are " + pcinfolist.size() + " members" );
// ------------------------------------------------------------------------------------
// found pcinfolist.size() == 462 ... in the debug file.
// ------------------------------------------------------------------------------------
// AND there appears to only be ONE thing in the list, so... now it seems that this
// is just a curiously convoluted artifact to get the first ( and only ) element.
// ------------------------------------------------------------------------------------
//							pciList = pcinfolist;
// ------------------------------------------------------------------------------------
// 120612.rlynch: but I'm changing it to get the whole array.  Must make it right.
// ------------------------------------------------------------------------------------
                            pciList.addAll( pcinfolist );
						}
					}
				}
			}
		}
		catch ( SOAPFaultException sfe )
		{
			printSoapFaultException( sfe );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
		return pciList;
	}

	/**
	 * @return TraversalSpec specification to get to the VirtualMachine managed
	 *         object.
	 */
	private static TraversalSpec getVMTraversalSpec()
	{
		// Create a traversal spec that starts from the 'root' objects
		// and traverses the inventory tree to get to the VirtualMachines.
		// Build the traversal specs bottoms up

		// Traversal to get to the VM in a VApp
		TraversalSpec
        vAppToVM = new TraversalSpec();
		vAppToVM.setName( "vAppToVM" );
		vAppToVM.setType( "VirtualApp" );
		vAppToVM.setPath( "vm" );

		// Traversal spec for VApp to VApp
		TraversalSpec
        vAppToVApp = new TraversalSpec();
		vAppToVApp.setName( "vAppToVApp" );
		vAppToVApp.setType( "VirtualApp" );
		vAppToVApp.setPath( "resourcePool" );

		// SelectionSpec for VApp to VApp recursion
		SelectionSpec
        vAppRecursion = new SelectionSpec();
		vAppRecursion.setName( "vAppToVApp" );

		// SelectionSpec to get to a VM in the VApp
		SelectionSpec
        vmInVApp = new SelectionSpec();
		vmInVApp.setName( "vAppToVM" );

		// SelectionSpec for both VApp to VApp and VApp to VM
		List<SelectionSpec>
        vAppToVMSS = new ArrayList<SelectionSpec>();
		vAppToVMSS.add( vAppRecursion );
		vAppToVMSS.add( vmInVApp );
		vAppToVApp.getSelectSet().addAll( vAppToVMSS );

		// This SelectionSpec is used for recursion for Folder recursion
		SelectionSpec
        sSpec = new SelectionSpec();
		sSpec.setName( "VisitFolders" );

		// Traversal to get to the vmFolder from DataCenter
		TraversalSpec
        dataCenterToVMFolder = new TraversalSpec();
		dataCenterToVMFolder.setName( "DataCenterToVMFolder" );
		dataCenterToVMFolder.setType( "Datacenter" );
		dataCenterToVMFolder.setPath( "vmFolder" );
		dataCenterToVMFolder.setSkip( false );
		dataCenterToVMFolder.getSelectSet().add( sSpec );

		// TraversalSpec to get to the DataCenter from rootFolder
		TraversalSpec
        traversalSpec = new TraversalSpec();
		traversalSpec.setName( "VisitFolders" );
		traversalSpec.setType( "Folder" );
		traversalSpec.setPath( "childEntity" );
		traversalSpec.setSkip( false );

		List<SelectionSpec>
        sSpecArr = new ArrayList<SelectionSpec>();
		sSpecArr.add( sSpec );
		sSpecArr.add( dataCenterToVMFolder );
		sSpecArr.add( vAppToVM );
		sSpecArr.add( vAppToVApp );

		traversalSpec.getSelectSet().addAll( sSpecArr );

		return traversalSpec;
	}

	/**
	 * Get the MOR of the Virtual Machine by its name.
	 *
	 * @param vmName
	 *            The name of the Virtual Machine
	 * @return The Managed Object reference for this VM
	 */
	private  ManagedObjectReference getVmByVMname( String vmName )
	{
		ManagedObjectReference retVal = null;

		try
		{
			TraversalSpec tSpec = getVMTraversalSpec();
			
			// Create Property Spec
			PropertySpec propertySpec = new PropertySpec();
			propertySpec.setAll( false );
			propertySpec.getPathSet().add( "name" );
			propertySpec.setType( "VirtualMachine" );

			// Now create Object Spec
			ObjectSpec
            objectSpec = new ObjectSpec();
			objectSpec.setObj( rootFolder );
			objectSpec.setSkip( true );
			objectSpec.getSelectSet().add( tSpec );

			// Create PropertyFilterSpec using the PropertySpec and ObjectPec
			// created above.
			PropertyFilterSpec
            propertyFilterSpec = new PropertyFilterSpec();
			propertyFilterSpec.getPropSet().add( propertySpec );
			propertyFilterSpec.getObjectSet().add( objectSpec );

			List<PropertyFilterSpec>
            propertyFilterSpecList = new ArrayList<PropertyFilterSpec>( 1 );
			propertyFilterSpecList.add( propertyFilterSpec );
			List<ObjectContent> objectContentList = retrievePropertiesAllObjects( propertyFilterSpecList );

			if ( objectContentList != null )
			{
				for ( ObjectContent oc : objectContentList )
				{
					ManagedObjectReference manObRef = oc.getObj();
					String                     vmnm = null;
					List<DynamicProperty>       dpList = oc.getPropSet();

					if ( dpList != null )
					{
                        // 120605.rlynch: I'm not clear why 'loop thru all' needed.
                        // 120612.rlynch: I'd have thought there'd be a test here
                        //                followed by a BREAK.
                        //
						for ( DynamicProperty dp : dpList )
						{
							vmnm = ( String ) dp.getVal();
						}
					}
					if ( vmnm != null && vmnm.equals( vmName ) )
					{
						retVal = manObRef;
						break;
					}
				}
			}
		}
		catch ( SOAPFaultException sfe )
		{
			printSoapFaultException( sfe );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
		return retVal;
	}

//	public static void doRealTime() throws Exception
//	{
//		ManagedObjectReference vmmor = getVmByVMname( vemaVM );
//        int                    selectedchoice;
//
//		if ( vmmor != null )
//		{
//			List<PerfCounterInfo> cInfo = getPerfCounters();
//			List<PerfCounterInfo> vmCpuCounters = new ArrayList<PerfCounterInfo>();
//			Map<Integer, PerfCounterInfo> counters =
//					new ConcurrentHashMap<Integer, PerfCounterInfo>();
//			
//			int ct = 0;
//			int lo = 6;
//			int hi = 8;
//			for ( int i = 0; i < cInfo.size(); ++i )
//			{
//                PerfCounterInfo pci = cInfo.get(i);
//
//				  String key1 = pci.getGroupInfo().getKey();
//                String key2 = pci.getNameInfo().getKey();
//                String key3 = pci.getStatsType().toString();
//                String key4 = pci.getUnitInfo().getKey();
//                String key5 = pci.getDynamicType();
//                String key6 = pci.getRollupType().toString();
//                String key7 = pci.getNameInfo().getSummary();
//                String msg  = "";
//
//                if ( !"net".equalsIgnoreCase( key1 )
//                ||  ( ct++ < lo || ct > hi )  // tricky programming. It works.
// )
//                {
//                	msg += "* ";
//                    vmCpuCounters.add( pci );
//counters.put(new Integer(pci.getKey()), pci); // testing "add all that match"
//                }
//                else
//                    msg += "(" + ct + ")...";
//
//                log.info( msg +
//                        "key[" + i + "]> "
//                        + key1 + " / "
//                        + key2 + " / "
//                        + key3 + " / "
//                        + key4 + " / "
//                        + key5 + " / "
//                        + key6 + " / "
//                        + key7 );
//			}
//
//			while ( true )
//			{
//				int i = 0;
//	
//                // print possible choices (using i)
//				for ( i = 0; i < vmCpuCounters.size(); i++ )
//				{
//					log.info( (i+1) + " - " + vmCpuCounters.get(i).getNameInfo().getSummary() );
//				}
//				log.info( "Please select a counter from the above list"
//                        + "\nEnter 0 to end: " );
////				BufferedReader reader =
////						new BufferedReader( new InputStreamReader( System.in ));
////				i = Integer.parseInt( reader.readLine()) - 1;
//				i = 17;
//				i = 10;
//
//				selectedchoice = --i;   // decrementing 'i' important for right choice
//				
//				if ( selectedchoice >= vmCpuCounters.size() )
//				{
//					log.info( "*** Value chosen too high! ***" );
//				}
//				else
//				{
//					if ( selectedchoice < 0 )
//						return;
//
//					PerfCounterInfo pcInfo = ( PerfCounterInfo ) vmCpuCounters
//							.get( selectedchoice );
////					counters.put( new Integer( pcInfo.getKey()), pcInfo );
//					break;
//				}
//			}
//			List<PerfMetricId> listpermeid = vimPort.queryAvailablePerfMetric(
//					perfManager, vmmor, null, null, new Integer( 20 ) );
//			ArrayList<PerfMetricId> mMetrics = new ArrayList<PerfMetricId>();
//
//			if ( listpermeid != null )
//			{
//				if ( counters.containsKey(
//                    new Integer( listpermeid.get( selectedchoice )
//                        .getCounterId() )))
//				{
//					mMetrics.add( listpermeid.get( selectedchoice ) );
//                    log.info( "Adding listpermeid: " + selectedchoice );
//				}
//			}
//			monitorPerformance( perfManager, vmmor, mMetrics, counters );
//		}
//		else
//		{
//			log.info( "doRealTime(): Virtual Machine " + vemaVM + " not found" );
//		}
//	}

	
	private static void displayValues(
            List<PerfEntityMetricBase> values,
			Map<Integer, PerfCounterInfo> counters )	// 'counters' as 'selections'
	{
		for ( int i = 0; i < values.size(); ++i )
		{
			List<PerfMetricSeries> listpems =
                (( PerfEntityMetric ) values.get( i )).getValue();

			List<PerfSampleInfo> listinfo =
                (( PerfEntityMetric ) values.get( i )).getSampleInfo();

			/*log.debug( "Sample time range: "
					+ listinfo.get( 0 ).getTimestamp().toString()
					+ " - "
					+ listinfo.get( listinfo.size() - 1 )
                        .getTimestamp().toString() );*/

			//log.debug( "listpems.size = '" + listpems.size() + "'");
			for ( int vi = 0; vi < listpems.size(); ++vi )
			{
				StringBuffer        s = new StringBuffer(250); // init capacity
				PerfMetricSeries pems = listpems.get(vi);
				int         counterId = pems.getId().getCounterId();
				PerfCounterInfo   pci = ( PerfCounterInfo ) counters.get(counterId);

				if ( pci != null )
				{
	                  s.append( pci.getNameInfo().getSummary() );
	                  s.append( ":" );
				}

				if ( pems instanceof PerfMetricIntSeries )
				{
					for ( Long k : ((PerfMetricIntSeries)pems).getValue() )
					{
                        s.append( k );
                        s.append( " " );
					}
					//log.debug( s );
				}
	//			else log.debug( "PerfCounter[" + vi + "] not instance of PerfMetricIntSeries");
			}
		}
	}


	/**
	 *
	 * @param pmRef
	 * @param vmRef
	 * @param mMetrics
	 * @param counters
	 * @throws Exception
	 */
	private  void monitorPerformance(
            ManagedObjectReference  pmRef,
			ManagedObjectReference  vmRef,
			ArrayList<PerfMetricId> mMetrics,
			Map<Integer, PerfCounterInfo> counters ) throws Exception
	{
		PerfQuerySpec qSpec = new PerfQuerySpec();

		qSpec.setEntity( vmRef );
		qSpec.setMaxSample( new Integer( 10 ) ); // gather 10 samples per
		qSpec.getMetricId().addAll( mMetrics );
		qSpec.setIntervalId( new Integer( 20 ) ); // in a 20 sec window

		List<PerfQuerySpec> qSpecList = new ArrayList<PerfQuerySpec>();
		qSpecList.add( qSpec );
		
		while ( true )
		{
			List<PerfEntityMetricBase> 
				listpemb = vimPort.queryPerf( pmRef, qSpecList );
			
			List<PerfEntityMetricBase> 
				pValues  = listpemb;

			if ( pValues != null )
				displayValues( pValues, counters );

	//		log.debug( "[Breakpoint with no iterations] ..." );
			break;
//			Thread.sleep( 10 * 1000 );   // milliseconds
		}
	}

	private static void printSoapFaultException( SOAPFaultException sfe )
	{
		//log.debug( "SOAP Fault -" );
		if ( sfe.getFault().hasDetail() )
			log.debug( sfe.getFault().getDetail().getFirstChild().getLocalName() );

		if ( sfe.getFault().getFaultString() != null )
			log.debug( "\n Message: " + sfe.getFault().getFaultString() );
	}
}
