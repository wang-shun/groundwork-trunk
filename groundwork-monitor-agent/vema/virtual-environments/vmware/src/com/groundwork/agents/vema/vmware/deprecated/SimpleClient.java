package com.groundwork.agents.vema.vmware.deprecated;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPFaultException;

import org.apache.log4j.Logger;

import com.groundwork.agents.vema.base.VemaBaseHost;
import com.groundwork.agents.vema.base.VemaBaseObject;
import com.groundwork.agents.vema.base.VemaBaseQuery;
import com.groundwork.agents.vema.base.VemaBaseVM;
import com.groundwork.agents.vema.base.VemaBaseHostURI;
import com.groundwork.agents.vema.vmware.connector.VemaVMwareMulti;
import com.groundwork.agents.vema.utils.VemaInformation;
import com.groundwork.agents.vema.utils.aPad;
import com.vmware.vim25.DynamicProperty;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ObjectContent;
import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertySpec;
import com.vmware.vim25.RetrieveOptions;
import com.vmware.vim25.RetrieveResult;
import com.vmware.vim25.SelectionSpec;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.TraversalSpec;
import com.vmware.vim25.VimPortType;
import com.vmware.vim25.VimService;

/**
 * <pre>
 * SimpleClient
 * 
 * This sample lists the inventory contents (managed entities)
 * 
 * <b>Parameters:</b>
 * url          [required] : url of the web service
 * username     [required] : username for the authentication
 * password     [required] : password for the authentication
 * 
 * <b>Command Line:</b>
 * run.bat com.vmware.general.SimpleClient
 * --url [webserviceurl] --username [username] --password [password]
 * </pre>
 */

public class SimpleClient
{
	private static class TrustAllTrustManager implements
			javax.net.ssl.TrustManager, javax.net.ssl.X509TrustManager
	{
		public java.security.cert.X509Certificate[] getAcceptedIssuers()
		{
			return null;
		}

		public boolean isServerTrusted(
				java.security.cert.X509Certificate[] certs)
		{
			return true;
			
		}

		public boolean isClientTrusted(
				java.security.cert.X509Certificate[] certs)
		{
			return true;
		}

		public void checkServerTrusted(
				java.security.cert.X509Certificate[] certs, String authType)
				throws java.security.cert.CertificateException
		{
			return;
		}

		public void checkClientTrusted(
				java.security.cert.X509Certificate[] certs, String authType)
				throws java.security.cert.CertificateException
		{
			return;
		}
	}

	private static final String					SVC_INST_NAME	= "ServiceInstance";
	private static final ManagedObjectReference	SVC_INST_REF	= new ManagedObjectReference();

	private static ManagedObjectReference		propCollectorRef;
	private static ManagedObjectReference		rootRef;
	private static VimService					vimService;
	private static VimPortType					vimPort;
	private static ServiceContent				serviceContent;

	private static ArrayList<VemaBaseHostURI> uriList = new ArrayList<VemaBaseHostURI>();
//	private static String						url;
//	private static String						userName;
//	private static String						password;
//	private static String						vmName;
	private static boolean						help			= false;
	private static boolean						isConnected		= false;

	private static Logger						log				= Logger.getLogger(SimpleClient.class);
	private static boolean						useLogger		= false;
	private static String						sloggerstring	= "";
	
	private static void trustAllHttpsCertificates() throws Exception
	{
		// Create a trust manager that does not validate certificate chains:
		javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
		javax.net.ssl.TrustManager              tm = new TrustAllTrustManager();
		trustAllCerts[0]                           = tm;
		javax.net.ssl.SSLContext                sc = javax.net.ssl.SSLContext.getInstance("SSL");
		javax.net.ssl.SSLSessionContext      sslsc = sc.getServerSessionContext();

		sslsc.setSessionTimeout(0);
		sc.init(null, trustAllCerts, null);

		javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	}

	// get common parameters
	private static void getConnectionParameters(String[] args)
			throws IllegalArgumentException
	{
		int    ai;
		String param;
		String nextparam;
		
		String myUrl      = null;
		String myUsername = null;
		String myPassword = null;
		String myVm       = null;
		String myMulti    = null;

		String es = "";	// 'empty' serves as a flag too.

		for(ai = 0; ai < args.length; ai++)
		{
			param     = args[ai].trim();
			nextparam = ( ai + 1 < args.length ) ? args[ai + 1].trim() : "";

			if (param.equalsIgnoreCase("--help"))
			{
				help = true;
			}
			else if (param.equalsIgnoreCase("--multi") 
					&& !nextparam.startsWith("--")
					&& !nextparam.isEmpty())
			{
				myMulti = nextparam;
				ai++;  // commit to using next parameter
				String parts[] = myMulti.split( "&" );

				if( parts.length == 4 ) // must be 4 parts
					uriList.add( new VemaBaseHostURI(parts[0], parts[1], parts[2], parts[3]) );
				else
					es += "--multi requires {uri}&{user}&{pass}&{vm} format";
			}
			else if (param.equalsIgnoreCase("--url") 
					&& !nextparam.startsWith("--")
					&& !nextparam.isEmpty())
			{
				myUrl = nextparam;
				ai++;  // commit to using next parameter
			}
			else if (param.equalsIgnoreCase("--username")
					&& !nextparam.startsWith("--") 
					&& !nextparam.isEmpty())
			{
				myUsername = nextparam;
				ai++;  // commit to using next parameter
			}
			else if (param.equalsIgnoreCase("--password")
					&& !nextparam.startsWith("--") 
					&& !nextparam.isEmpty())
			{
				myPassword = nextparam;
				ai++;  // commit to using next parameter
			}
			else if (param.equalsIgnoreCase("--vmname")
					&& !nextparam.startsWith("--") 
					&& !nextparam.isEmpty())
			{
				myVm = nextparam;
				ai++;  // commit to using next parameter
			}
			else
			{
				System.out.println("Unexpected parameter: '" + param + "'");
			}
		}
		
		if(     myMulti    == null )
		{
			if( myUrl      == null )	es += " --url {URL}";
			if( myUsername == null )	es += " --username {USERNAME}";
			if( myPassword == null )	es += " --password {PASSWORD}";
		}
		
		if ( !es.isEmpty() )
		{
			throw new IllegalArgumentException("Expected:" + es);
		}
		
		if( uriList.size() == 0 )
		{
			uriList.add( new VemaBaseHostURI( myUrl, myUsername, myPassword, myVm ) );
		}
	}
	

	/**
	 * Establishes session with the virtual center server.
	 * 
	 * @throws Exception
	 *             the exception
	 */
//	private static void connect( int index ) throws Exception
//	{
//		HostnameVerifier hv = new HostnameVerifier()
//		{
//			public boolean verify(String urlHostName, SSLSession session)
//			{
//				return true;
//			}
//		};
//		
//		if( index < 0 || index >= uriList.size() )
//			throw new IllegalArgumentException( "connect( " + index + " ) index outside 0 < index < " + uriList.size() + " range." );
//		
//		trustAllHttpsCertificates();
//		HttpsURLConnection.setDefaultHostnameVerifier(hv);
//
//		SVC_INST_REF.setType(SVC_INST_NAME);
//		SVC_INST_REF.setValue(SVC_INST_NAME);
//
//		vimService  = new VimService();
//		vimPort     = vimService.getVimPort();
//		Map<String, Object> ctxt = ((BindingProvider) vimPort)
//				.getRequestContext();
//
//		ctxt.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, uriList.get( index ).getUri() );
//		ctxt.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
//
//		serviceContent   = vimPort.retrieveServiceContent(SVC_INST_REF);
//		vimPort.login(     serviceContent.getSessionManager(), 
//                           uriList.get( index ).getUser(),	
//                           uriList.get( index ).getPassword(), 
//                           null);
//		
//		isConnected      = true;
//
//		propCollectorRef = serviceContent.getPropertyCollector();
//		rootRef          = serviceContent.getRootFolder();
//	}
//
//	/**
//	 * Disconnects the user session.
//	 * 
//	 * @throws Exception
//	 */
//	private static void disconnect() throws Exception
//	{
//		if (isConnected)
//		{
//			vimPort.logout(serviceContent.getSessionManager());
//		}
//		isConnected = false;
//	}

	private static void sloggerflush()
	{
		if( !sloggerstring.isEmpty() )
        {
			if( useLogger )
				log.info(sloggerstring);
			else 
				System.out.println(sloggerstring);
        }
		
		sloggerstring = "";
	}
	
	private static void slogger(Boolean flush, String message)
	{
		sloggerstring = sloggerstring + message;
		if( flush )
			sloggerflush();
	}
	
	/**
	 * Uses the new RetrievePropertiesEx method to emulate the now deprecated
	 * RetrieveProperties method
	 * 
	 * @param listpfs
	 * @return list of object content
	 * @throws Exception
	 */
	private static List<ObjectContent> retrievePropertiesAllObjects(
			List<PropertyFilterSpec> listpfs) throws Exception
	{
		RetrieveOptions propObjectRetrieveOpts = new RetrieveOptions();
		List<ObjectContent>     listobjcontent = new ArrayList<ObjectContent>();

		try
		{
			String token = null;
			RetrieveResult results = vimPort.retrievePropertiesEx(
					propCollectorRef, listpfs, propObjectRetrieveOpts);

			if (results              != null      // seed root level 
			&&  results.getObjects() != null      // which is breadth-filled
			&& !results.getObjects().isEmpty())   // below.
			{
				listobjcontent.addAll(results.getObjects());
			}
			
			if (results != null && results.getToken() != null)
			{
				token = results.getToken();
				slogger(false, "TRACE 3: " + results.toString());
				slogger(true, "   token[" + (token != null ? token : "") + "]");
			}
			while (token != null && !token.isEmpty())
			{
				results = vimPort.continueRetrievePropertiesEx(
						propCollectorRef,
						token);
				slogger(true, "TRACE 4: " + results.toString());
				token = null;
				if (results != null)
				{
					token = results.getToken();
					slogger(true, "   token[" + (token != null ? token : "") + "]");
					if (results.getObjects() != null
					&& !results.getObjects().isEmpty())
					{
						listobjcontent.addAll(results.getObjects());
					}
				}
			}
		}
		catch (SOAPFaultException sfe)
		{
			printSoapFaultException(sfe);
		}
		catch (Exception e)
		{
			log.error(e.toString() + " : Failed getting contents");
			e.printStackTrace();
		}

		return listobjcontent;
	}

    private static class TraversalSpecPill
    {
        private TraversalSpec ts = null;
        private SelectionSpec ss = null;

        TraversalSpecPill (String name, String type, String path, boolean skip, String pool )
        {
            ts = new TraversalSpec();
            ts.setName( name );
            ts.setType( type );
            ts.setPath( path );
            ts.setSkip( skip );

            ss = new SelectionSpec();
            ss.setName( pool );

            ts.getSelectSet().add(ss);
        }

        public TraversalSpec getTS() { return ts; }
        public SelectionSpec getSS() { return ss; }
    }
    
    private static void getAndPrintInventoryList() throws Exception
    {
    	ManagedObjectReference viewMgrRef = serviceContent.getViewManager();
    	ManagedObjectReference propColl   = serviceContent.getPropertyCollector();
    	
        List<String> vmList = new ArrayList<String>();
//      vmList.add("VirtualMachine");  // OK, creates output
//      vmList.add("ResourcePool");    // creates MORE output
//      vmList.add("Network");         // creates yet more output
//0     vmList.add("DataStore");       // causes SOAP error.
//1     vmList.add("DataCenter");      // causes SOAP error.
//      vmList.add("Folder");          // creates no more output
//      vmList.add("ComputeResource"); // creates no more output
        vmList.add("HostSystem");      // creates no more output
        
        ManagedObjectReference cViewRef = 
        		vimPort.createContainerView(
        				viewMgrRef,
        				serviceContent.getRootFolder(),
        				vmList,
        				true);

        TraversalSpec tSpec = new TraversalSpec();
        tSpec.setName("traverseEntities");
        tSpec.setPath("view");
        tSpec.setSkip(false);
        tSpec.setType("ContainerView");
        
        TraversalSpec tSpecVMN = new TraversalSpec();
        tSpecVMN.setType("VirtualMachine");
        tSpecVMN.setPath("network");
        tSpecVMN.setSkip(false);
        tSpec.getSelectSet().add(tSpecVMN);
        
        TraversalSpec tSpecVMRP = new TraversalSpec();
        tSpecVMRP.setType("VirtualMachine");
        tSpecVMRP.setPath("resourcePool");
        tSpecVMRP.setSkip(false);
        tSpec.getSelectSet().add(tSpecVMRP);
        
        // create an object spec to define the beginning of the traversal;
        ObjectSpec oSpec = new ObjectSpec();
        oSpec.setObj(cViewRef);
        oSpec.setSkip(true);
        oSpec.getSelectSet().add(tSpec);
        oSpec.getSelectSet().add(tSpecVMN);
        oSpec.getSelectSet().add(tSpecVMRP);

        PropertySpec pSpec = new PropertySpec();
//		pSpec.setType("VirtualMachine");
        pSpec.setType("HostSystem");
        pSpec.getPathSet().add("name");
        
//      PropertySpec pSpecNS = new PropertySpec();
//      pSpecNS.setType("Network");
//      pSpecNS.getPathSet().add("summary.accessible");
        
//      PropertySpec pSpecRPR = new PropertySpec();
//      pSpecRPR.setType("ResourcePool");
//      pSpecRPR.getPathSet().add("runtime.cpu.maxUsage");
//      pSpecRPR.getPathSet().add("runtime.memory.maxUsage");
//      pSpecRPR.getPathSet().add("runtime.overallStatus");

        PropertyFilterSpec fSpec = new PropertyFilterSpec();
    	fSpec.getObjectSet().add(oSpec);
//    	fSpec.getPropSet().add(pSpecRPR);
//    	fSpec.getPropSet().add(pSpecNS);
    	fSpec.getPropSet().add(pSpec);
    	
    	List<PropertyFilterSpec> fSpecList = new ArrayList<PropertyFilterSpec>();
    	fSpecList.add(fSpec);
    	
    	RetrieveOptions ro = new RetrieveOptions();
//    	ro.setMaxObjects(1000); // this did nothing to improve things.
    	RetrieveResult props = vimPort.retrievePropertiesEx(propColl, fSpecList, ro);

    	if(props != null )
    	{
    		for( ObjectContent oc : props.getObjects())
    		{
    			String value = null;
    			String path  = null;
    			
    			List<DynamicProperty> dps = oc.getPropSet();

    			if( dps == null )
    				continue;
    			
    			for( DynamicProperty dp : dps )
    			{
    				path = dp.getName();

    				if( path.equals("name"))
    				{
    					value = (String) dp.getVal();
    				}
       				else if ( 
       					path.equals("summary.accessible")
    				||  path.equals("runtime.cpu.maxUsage")
    				||  path.equals("runtime.memory.maxUsage")
    				||  path.equals("runtime.overallStatus")
    				)
    				{
    					value = String.valueOf( dp.getVal() );
    				}
    				else
    				{
    					value = String.valueOf( dp.getVal() ) + " (unknown path)";
    				}

    				sloggerflush();
	    			slogger(false, path + "=" + value);
    			}
    		}
    		sloggerflush();
    	}
    }

    private static void getAndPrintInventoryContents() throws Exception
    {
    	log.info("0:");
        TraversalSpecPill resourcePoolTraversalSpec = 
            new TraversalSpecPill( 
                "resourcePoolTraversalSpec",
                "ResourcePool",
                "resourcePool",
                false,
                "resourcePoolTraversalSpec" );
        
        TraversalSpecPill computeResourceRpTraversalSpec = 
            new TraversalSpecPill(
                "computeResourceRpTraversalSpec",
                "ComputeResource",
                "resourcePool",
                false,
                "resourcePoolTraversalSpec");

        TraversalSpecPill computeResourceHostTraversalSpec = 
            new TraversalSpecPill(
                "computeResourceHostTraversalSpec",
                "ComputeResource",
                "host",
                false,
                "resourcePoolTraversalSpec");

        TraversalSpecPill datacenterHostTraversalSpec = 
            new TraversalSpecPill(
                "datacenterHostTraversalSpec",
                "Datacenter",
                "hostFolder",
                false,
                "folderTraversalSpec" );

        TraversalSpecPill datacenterVmTraversalSpec = 
            new TraversalSpecPill(
                "datacenterVmTraversalSpec",
                "Datacenter",
                "vmFolder",
                false,
                "folderTraversalSpec" );

        // TraversalSpec folderTraversalSpec = 
        TraversalSpecPill folderTraversalSpec = 
            new TraversalSpecPill(
                "folderTraversalSpec",
                "Folder",
                "childEntity",
                false,
                "folderTraversalSpec" );
log.info("A:");
        /*
        TraversalSpec resourcePoolTraversalSpec = new TraversalSpec();
        resourcePoolTraversalSpec.setName("resourcePoolTraversalSpec");
        resourcePoolTraversalSpec.setType("ResourcePool");
        resourcePoolTraversalSpec.setPath("resourcePool");
        resourcePoolTraversalSpec.setSkip(Boolean.FALSE);
        SelectionSpec rpts = new SelectionSpec();
        rpts.setName("resourcePoolTraversalSpec");
        resourcePoolTraversalSpec.getSelectSet().add(rpts);
        
        TraversalSpec computeResourceRpTraversalSpec = new TraversalSpec();
        computeResourceRpTraversalSpec
                .setName("computeResourceRpTraversalSpec");
        computeResourceRpTraversalSpec.setType("ComputeResource");
        computeResourceRpTraversalSpec.setPath("resourcePool");
        computeResourceRpTraversalSpec.setSkip(Boolean.FALSE);
        SelectionSpec rptss = new SelectionSpec();
        rptss.setName("resourcePoolTraversalSpec");
        computeResourceRpTraversalSpec.getSelectSet().add(rptss);

        TraversalSpec computeResourceHostTraversalSpec = new TraversalSpec();
        computeResourceHostTraversalSpec
                .setName("computeResourceHostTraversalSpec");
        computeResourceHostTraversalSpec.setType("ComputeResource");
        computeResourceHostTraversalSpec.setPath("host");
        computeResourceHostTraversalSpec.setSkip(Boolean.FALSE);

        TraversalSpec datacenterHostTraversalSpec = new TraversalSpec();
        datacenterHostTraversalSpec.setName("datacenterHostTraversalSpec");
        datacenterHostTraversalSpec.setType("Datacenter");
        datacenterHostTraversalSpec.setPath("hostFolder");
        datacenterHostTraversalSpec.setSkip(Boolean.FALSE);
        SelectionSpec ftspec = new SelectionSpec();
        ftspec.setName("folderTraversalSpec");
        datacenterHostTraversalSpec.getSelectSet().add(ftspec);

        TraversalSpec datacenterVmTraversalSpec = new TraversalSpec();
        datacenterVmTraversalSpec.setName("datacenterVmTraversalSpec");
        datacenterVmTraversalSpec.setType("Datacenter");
        datacenterVmTraversalSpec.setPath("vmFolder");
        datacenterVmTraversalSpec.setSkip(Boolean.FALSE);
        SelectionSpec ftspecs = new SelectionSpec();
        ftspecs.setName("folderTraversalSpec");
        datacenterVmTraversalSpec.getSelectSet().add(ftspecs);

        TraversalSpec folderTraversalSpec = new TraversalSpec();
        folderTraversalSpec.setName("folderTraversalSpec");
        folderTraversalSpec.setType("Folder");
        folderTraversalSpec.setPath("childEntity");
        folderTraversalSpec.setSkip(Boolean.FALSE);
        SelectionSpec ftrspec = new SelectionSpec();
        ftrspec.setName("folderTraversalSpec");
        */

        /* This setting up of a List of objects may seem a bit wasteful
         * but it shows the general case, and frankly isn't a substantial
         * overhead in any case.  
         */
        List<SelectionSpec> ssarray = new ArrayList<SelectionSpec>();

        ssarray.add( folderTraversalSpec             .getSS() );
        ssarray.add( datacenterHostTraversalSpec     .getTS() );
        ssarray.add( datacenterVmTraversalSpec       .getTS() );
        ssarray.add( computeResourceRpTraversalSpec  .getTS() );
        ssarray.add( computeResourceHostTraversalSpec.getTS() );
        ssarray.add( resourcePoolTraversalSpec       .getTS() );

        folderTraversalSpec.getTS().getSelectSet().addAll(ssarray);

        PropertySpec props = new PropertySpec();
        props.setAll(Boolean.FALSE);
        props.getPathSet().add("name");
        props.setType("ManagedEntity");
        
        /* 120501.rlynch: however, this List setup is basically wasteful.  The
         * reason I'm leaving it is because it perhaps can be extended in the
         * future to include more PropertySpec[s]. 
         */
        List<PropertySpec> propspecary = new ArrayList<PropertySpec>();
        propspecary.add(props);

        PropertyFilterSpec spec = new PropertyFilterSpec();
        spec.getPropSet().addAll(propspecary);

        ObjectSpec os = new ObjectSpec();
        os.setObj(rootRef);
        os.setSkip(Boolean.FALSE);
        os.getSelectSet().add(folderTraversalSpec.getTS());
        spec.getObjectSet().add(os);
        
        List<PropertyFilterSpec> listpfs = new ArrayList<PropertyFilterSpec>(1);
        listpfs.add(spec);
        List<ObjectContent>  listobjcont = retrievePropertiesAllObjects(listpfs);
        log.info("b:");

        // If we get contents back. print them out.
        if (listobjcont != null)
        {
            ObjectContent           oc = null;
            ManagedObjectReference mor = null;
            DynamicProperty         pc = null;
            log.info("c:");

            for (int oci = 0; oci < listobjcont.size(); oci++)
            {
                oc  = listobjcont.get(oci);
                mor = oc.getObj();
                log.info("d:");

                List<DynamicProperty> listdp = oc.getPropSet();
                sloggerflush();
                slogger(false, "oci[" + oci + "]");
                slogger(false, ".OT[ " + mor.getType()  + " ]"); // Object Type
                slogger(false, ".RV[ " + mor.getValue() + " ]"); // Referenced Value
                
                if (listdp == null)
                    continue;

                for (int pci = 0; pci < listdp.size(); pci++)
                {
                    pc = listdp.get(pci);
                    slogger(false, "[" + pci + "]:");
                    slogger(false, ".PN[ " + pc.getName() + " ]");

                    if (!pc.getVal().getClass().isArray())
                    {
                        slogger(false, ".PV[ " + pc.getVal() + " ]");
                        continue; // short circuit
                    }

                    List<Object> ipcArray = new ArrayList<Object>();
                    ipcArray.add(pc.getVal());
                    slogger(false, ".V[ " + pc.getVal() + " ]");
                    
                    for (int ii = 0; ii < ipcArray.size(); ii++)
                    {
                        Object oval = ipcArray.get(ii);
                        if (oval.getClass()
                        		.getName()
                                .indexOf("ManagedObjectReference") >= 0)
                        {
                            ManagedObjectReference imor = (ManagedObjectReference) oval;

                            slogger(false, ".iOT[ " + imor.getType() + " ]");
                            slogger(false, ".iOR[ " + imor.getValue() + " ]");
                        }
                        else
                        {
                            slogger(false, ".iPV[ " + oval + " ]");
                        }
                    }
                }
            }
        }
        else
        {
            log.info("No Managed Entities retrieved!");
        }
        sloggerflush();
    }

    private static void printSoapFaultException(SOAPFaultException sfe)
    {
        System.out.println("SOAP Fault -");
        if (sfe.getFault().hasDetail())
        {
            log.warn(sfe.getFault()
            		.getDetail()
            		.getFirstChild()
                    .getLocalName());
        }
        if (sfe.getFault().getFaultString() != null)
        {
            log.warn("\n Message: " + sfe.getFault().getFaultString());
        }
    }

    private static void printUsage(String message)
    {
        System.out.println("---------------------------------------------------------");
        System.out.println(message);
        System.out.println("---------------------------------------------------------");
        System.out.println("This sample lists inventory contents (managed entities)");
        System.out.println("");
        System.out.println("Parameters:");
        System.out.println("url          [required] : url of the web service");
        System.out.println("username     [required] : username for the authentication");
        System.out.println("password     [required] : password for the authentication");
        System.out.println("");
        System.out.println("Command: (originally... 120501.rlynch modified for Catalina)");
        System.out.println("run.bat com.vmware.general.SimpleClient "
                        + "--url {webserviceurl} --username {username} --password {password} "
//						+ "[--ignorecerts {parameter}]"
                        );
    }

    private static void testVMwareListHost( VemaVMwareMulti vema )
    {
    	log.info("Starting...");
    	String s = null;
    	
    	List<VemaBaseQuery> hostMetrics = new ArrayList<VemaBaseQuery>();
    	List<VemaBaseQuery> vmMetrics = new ArrayList<VemaBaseQuery>();  	// TODO - change to VemaBaseMetric[]

    	hostMetrics.add(new VemaBaseQuery("summary.quickStats.overallCpuUsage", 25, 50, false, true, false /*!!!*/));
    	vmMetrics.add(  new VemaBaseQuery("summary.quickStats.guestMemoryUsage", 250, 2500, false, true ));
    	vmMetrics.add(  new VemaBaseQuery("syn.vm.cpu.cpuToMax.used", 50, 75, false, true ));
    	
    	ConcurrentHashMap<String, VemaBaseHost>  hosts = null;

    	/* do it once, should all be pending */  
    	hosts = vema.getListHost(hostMetrics, vmMetrics );
        log.info( (s = VemaInformation.listHostInformationToString(null, hosts, true, false, true, false, false, false, true)) == null ? "VemaInformation is null" : s );
        try { Thread.sleep(20 * 1000); } catch ( Exception e ){ }
        
        /* do again... pending => OK    */       
        hosts = vema.getListHost(hostMetrics, vmMetrics );
        log.info( (s = VemaInformation.listHostInformationToString(null, hosts, false, false, true, false, false, false, true)) == null ? "VemaInformation is null" : s );
        try { Thread.sleep(20 * 1000); } catch ( Exception e ){ }

        /* and 'is state chg' s/b false */       
        hosts = vema.getListHost(hostMetrics, vmMetrics );
        log.info( (s = VemaInformation.listHostInformationToString(null, hosts, false, false, true, false, false, false, true)) == null ? "VemaInformation is null" : s );

        if( hosts == null )
        {
            log.info( "No HOSTS returned by Vema.getListHost()" );
            return;
        }
    }
    private static void testRhevListHost( VemaVMwareMulti vema )
    {
    	log.info("Starting...");
    	String s = null;
    	
    	List<VemaBaseQuery> hostMetrics = new ArrayList<VemaBaseQuery>();
    	List<VemaBaseQuery> vmMetrics = new ArrayList<VemaBaseQuery>();  	// TODO - change to VemaBaseMetric[]

    	hostMetrics.add(new VemaBaseQuery("summary.quickStats.overallCpuUsage", 25, 50, false, true, false /*!!!*/));
    	vmMetrics.add(  new VemaBaseQuery("summary.quickStats.guestMemoryUsage", 250, 2500, false, true ));
    	vmMetrics.add(  new VemaBaseQuery("syn.vm.cpu.cpuToMax.used", 50, 75, false, true ));
    	
    	ConcurrentHashMap<String, VemaBaseHost>  hosts = null;

    	/* do it once, should all be pending */  
    	hosts = vema.getListHost(hostMetrics, vmMetrics );
        log.info( (s = VemaInformation.listHostInformationToString(null, hosts, true, false, true, false, false, false, true)) == null ? "VemaInformation is null" : s );
        try { Thread.sleep(20 * 1000); } catch ( Exception e ){ }
        
        /* do again... pending => OK    */       
        hosts = vema.getListHost(hostMetrics, vmMetrics );
        log.info( (s = VemaInformation.listHostInformationToString(null, hosts, false, false, true, false, false, false, true)) == null ? "VemaInformation is null" : s );
        try { Thread.sleep(20 * 1000); } catch ( Exception e ){ }

        /* and 'is state chg' s/b false */       
        hosts = vema.getListHost(hostMetrics, vmMetrics );
        log.info( (s = VemaInformation.listHostInformationToString(null, hosts, false, false, true, false, false, false, true)) == null ? "VemaInformation is null" : s );

        if( hosts == null )
        {
            log.info( "No HOSTS returned by VemaRhev.getListHost()" );
            return;
        }
    }

//    private static void testListVM( VemaRhev vema )
//    {
//        List<String> VMs = vema.getListVM( null );
//
//        if( VMs == null )
//        {
//            log.info( "No HOSTS returned by VemaRhev.getListVM()" );
//            return;
//        }
//
//        int i = 0;
//        for( String vm : VMs )
//        {
//            log.info( "VM[ " + i + "]: \"" + vm + "\"" );
//            i++;
//        }
//        return;
//    }
//    
//    private static void testInventoryTreeTool( VemaRhev vema )
//    {
//    	try
//    	{ 
//    		vema.getInventoryTreeTool( );
//    	}
//    	catch ( Exception e )
//    	{
//    		slogger(true, "getInventoryTreeTool() returned '" + e + "'");
//    	}
//    	finally
//    	{
//    	}
//
//    	return;
//    }
	
    public static void TestSimpleClient(String[] args)
    {
    	getConnectionParameters( args );
        if (help)
        {
            printUsage("TRACE 1");
            return;
        }
        VemaVMwareMulti vmulti = new VemaVMwareMulti( uriList );
        
    	
    	log.info("Begin of TestSimpleClient().connect()" );
        try
        {
        	int pausing = 180; // seconds.  Set to ZERO to turn off.
        	
        	vmulti.connect();
            log.info("TestSimpleClient: past connection to vema()...");
            if( pausing != 0 )
            {
            	log.info( "TestSimpleClient - pausing for " + pausing + " sec." );
            	Thread.sleep( pausing * 1000L );
            }
            testVMwareListHost( vmulti );
            log.info("TestSimpleClient: past 'testListHost()'");
            sloggerflush();
        }
        catch (IllegalArgumentException e)
        {
            System.out.println("IllegalArgument" + e.getMessage());
            printUsage("TRACE 2");
        }
        catch (SOAPFaultException sfe)
        {
            printSoapFaultException(sfe);
        }
        catch (Exception e)
        {
            log.error("General Exception" + e);
            e.printStackTrace();
        }
        finally
        {
            try
            {
            	vmulti.disconnect();
            }
            catch (SOAPFaultException sfe)
            {
                printSoapFaultException(sfe);
            }
            catch (Exception e)
            {
                System.out.println("Failed to disconnect - " + e.getMessage());
                log.error(         "Failed to disconnect - " + e.getMessage());
                
                e.printStackTrace();
            }
        }
    }
}
