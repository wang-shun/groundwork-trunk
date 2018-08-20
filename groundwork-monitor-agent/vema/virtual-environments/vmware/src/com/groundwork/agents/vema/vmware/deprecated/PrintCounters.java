/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
// package com.vmware.performance;
package com.groundwork.agents.vema.vmware.deprecated;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPFaultException;

import com.vmware.vim25.ArrayOfPerfCounterInfo;
import com.vmware.vim25.DynamicProperty;
import com.vmware.vim25.ElementDescription;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ObjectContent;
import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.PerfMetricId;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertySpec;
import com.vmware.vim25.RetrieveOptions;
import com.vmware.vim25.RetrieveResult;
import com.vmware.vim25.SelectionSpec;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.TraversalSpec;
import com.vmware.vim25.VimPortType;
import com.vmware.vim25.VimService;

import org.apache.log4j.Logger;

/**
 * <pre>
 * PrintCounters
 * 
 * This sample writes available VM, Hostsystem or ResourcePool
 * perf counters into the file specified
 * 
 * <b>Parameters:</b>
 * url          [required] : url of the web service
 * username     [required] : username for the authentication
 * password     [required] : password for the authentication
 * entitytype   [required] : Managed entity
 *                          [HostSystem|VirtualMachine|ResourcePool]
 * entityname   [required] : name of the managed entity
 * filename     [required] : Full path of filename to write to
 * 
 * <b>Command Line:</b>
 * Save counters available for a host
 * run.bat com.vmware.performance.PrintCounters
 * --url https://myHost.com/sdk
 * --username [user]  --password [password] --entitytype HostSystem
 * --entityname myHost.com --filename myHostCounters
 * </pre>
 */

public class PrintCounters
{
	private static org.apache.log4j.Logger	log	= Logger.getLogger(PrintCounters.class);

	private static class TrustAllTrustManager implements
			javax.net.ssl.TrustManager,
			javax.net.ssl.X509TrustManager
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
				java.security.cert.X509Certificate[] certs,
				String authType)
				throws java.security.cert.CertificateException
		{
			return;
		}

		public void checkClientTrusted(
				java.security.cert.X509Certificate[] certs,
				String authType)
				throws java.security.cert.CertificateException
		{
			return;
		}
	}

	private static final ManagedObjectReference	SVC_INST_REF	= new ManagedObjectReference();
	private static final String					SVC_INST_NAME	= "ServiceInstance";

	private static ManagedObjectReference		propCollectorRef;
	private static ManagedObjectReference		rootRef;
	private static VimService					vimService;
	private static VimPortType					vimPort;
	private static ServiceContent				serviceContent;

	private static String						url;
	private static String						userName;
	private static String						password;
	private static boolean						help			= false;
	private static ManagedObjectReference		perfManager;
	private static String						filename;
	private static String						entityname;
	private static String						entitytype;
	private static Boolean						isConnected		= false;

	private static void trustAllHttpsCertificates()
			throws Exception
	{
		// Create a trust manager that does not validate certificate chains:
		javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
		javax.net.ssl.TrustManager tm = new TrustAllTrustManager();
		trustAllCerts[0] = tm;
		javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext
				.getInstance("SSL");
		javax.net.ssl.SSLSessionContext sslsc = sc.getServerSessionContext();
		sslsc.setSessionTimeout(0);
		sc.init(null, trustAllCerts, null);
		javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc
				.getSocketFactory());
	}

	// get common parameters
	private static void getParameters(String[] args) throws IllegalArgumentException
	{
		int ai;
		String param;
		String nextparam;
		for(ai = 0; ai < args.length; ai++)
		{
			param = args[ai].trim();
			nextparam = (ai + 1 < args.length) ? args[++ai].trim() : "";
			
			if (param.equalsIgnoreCase("--help"))
			{
				help = true;
				break;
			}
			else if (param.equalsIgnoreCase("--url") 
					&& !nextparam.startsWith("--") 
					&& !nextparam.isEmpty())
				url = nextparam;

			else if (param.equalsIgnoreCase("--username")
					&& !nextparam.startsWith("--") 
					&& !nextparam.isEmpty())
				userName = nextparam;

			else if (param.equalsIgnoreCase("--password")
					&& !nextparam.startsWith("--") 
					&& !nextparam.isEmpty())
				password = nextparam;

			else if (param.equalsIgnoreCase("--entitytype") 
					&& !nextparam.startsWith("--")
					&& !nextparam.isEmpty())
				entitytype = nextparam;

			else if (param.equalsIgnoreCase("--entityname")
					&& !nextparam.startsWith("--") 
					&& !nextparam.isEmpty())
				entityname = nextparam;

			else if (param.equalsIgnoreCase("--filename")
					&& !nextparam.startsWith("--") 
					&& !nextparam.isEmpty())
				filename = nextparam;

			else
			{
				// do nothing - because there's another parse routine
				// that's looking for DIFFERENT sections of the command
				// line to parse.  [normally, one would report an error
				// in command-line parameters.]
			}
		}
		String es = "";	// 'empty' serves as a flag too.

		if( url        == null )	es += " --url {URL}";
		if( userName   == null )	es += " --username {USERNAME}";
		if( password   == null )	es += " --password {PASSWORD}";
		if( entitytype == null )	es += " --entitytype {TYPE}";
		if( entityname == null )	es += " --entityname {NAME}";
		if( filename   == null )	es += " --filename {PATH+FILE}";
		
		if ( !es.isEmpty() )
		{
			throw new IllegalArgumentException("Expected:" + es);
		}
	}

	/**
	 * Establishes session with the virtual center server.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	private static void connect()
			throws Exception
	{

		HostnameVerifier hv = new HostnameVerifier()
		{
			public boolean verify(String urlHostName, SSLSession session)
			{
				return true;
			}
		};

		trustAllHttpsCertificates();
		HttpsURLConnection.setDefaultHostnameVerifier(hv);

		SVC_INST_REF.setType(SVC_INST_NAME);
		SVC_INST_REF.setValue(SVC_INST_NAME);

		vimService = new VimService();
		vimPort = vimService.getVimPort();
		Map<String, Object> ctxt =
				((BindingProvider) vimPort).getRequestContext();

		ctxt.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);
		ctxt.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);

        try 
        {
            serviceContent = vimPort.retrieveServiceContent(SVC_INST_REF);
        } 
        catch ( Exception e )
        {
            log.info( "10a: Exception - '" + e + "'" );
        }
		vimPort.login(serviceContent.getSessionManager(),
				userName,
				password, null);
		isConnected = true;

		propCollectorRef = serviceContent.getPropertyCollector();
		rootRef = serviceContent.getRootFolder();
		perfManager = serviceContent.getPerfManager();
	}

	/**
	 * Disconnects the user session.
	 * 
	 * @throws Exception
	 */
	private static void disconnect()
			throws Exception
	{
		if (isConnected)
		{
			vimPort.logout(serviceContent.getSessionManager());
		}
		isConnected = false;
	}

	/**
	 * Uses the new RetrievePropertiesEx method to emulate the now deprecated
	 * RetrieveProperties method.
	 * 
	 * @param listpfs
	 * @return list of object content
	 * @throws Exception
	 */
	private static List<ObjectContent> retrievePropertiesAllObjects(
			List<PropertyFilterSpec> listpfs)
			throws Exception
	{

		RetrieveOptions propObjectRetrieveOpts = new RetrieveOptions();

		List<ObjectContent> listobjcontent = new ArrayList<ObjectContent>();

		try
		{
			RetrieveResult rslts =
					vimPort.retrievePropertiesEx(propCollectorRef,
							listpfs,
							propObjectRetrieveOpts);
			if (rslts != null && rslts.getObjects() != null &&
					!rslts.getObjects().isEmpty())
			{
				listobjcontent.addAll(rslts.getObjects());
			}
			String token = null;
			if (rslts != null && rslts.getToken() != null)
			{
				token = rslts.getToken();
			}
			while (token != null && !token.isEmpty())
			{
				rslts = vimPort.continueRetrievePropertiesEx(propCollectorRef,
						token);
				token = null;
				if (rslts != null)
				{
					token = rslts.getToken();
					if (rslts.getObjects() != null
							&& !rslts.getObjects().isEmpty())
					{
						listobjcontent.addAll(rslts.getObjects());
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
			System.out.println(" : Failed Getting Contents");
			e.printStackTrace();
		}

		return listobjcontent;
	}

	private static void printCounters()
			throws Exception
	{
		String entityType = entitytype; // kind of superfluous

		if (entityType.equalsIgnoreCase("HostSystem"))
			printEntityCounters("HostSystem");
		
		else if (entityType.equalsIgnoreCase("VirtualMachine"))
			printEntityCounters("VirtualMachine");
		
		else if (entityType.equals("ResourcePool"))
			printEntityCounters("ResourcePool");
		
		else
		{
			System.out.println("Entity Argument must be [HostSystem|VirtualMachine|ResourcePool]");
		}
	}

	/**
	 * This method initializes all the performance counters available on the
	 * system it is connected to. The performance counters are stored in the
	 * hashmap counters with group.counter.rolluptype being the key and id being
	 * the value.
	 */
	private static List<PerfCounterInfo> getPerfCounters()
	{
		List<PerfCounterInfo> pciArr = new ArrayList<PerfCounterInfo>();
		try
		{
			// Create Property Spec
			PropertySpec propertySpec = new PropertySpec();
			propertySpec.setAll(Boolean.FALSE);
			propertySpec.getPathSet().add("perfCounter");
			propertySpec.setType("PerformanceManager");
			List<PropertySpec> propertySpecList = new ArrayList<PropertySpec>();
			propertySpecList.add(propertySpec);

			// Now create Object Spec
			ObjectSpec objectSpec = new ObjectSpec();
			objectSpec.setObj(perfManager);
			List<ObjectSpec> objectSpecList = new ArrayList<ObjectSpec>();
			objectSpecList.add(objectSpec);

			// Create PropertyFilterSpec using the PropertySpec and ObjectPec
			// created above.
			PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
			propertyFilterSpec.getPropSet().add(propertySpec);
			propertyFilterSpec.getObjectSet().add(objectSpec);
			List<PropertyFilterSpec> propertyFilterSpecList =
					new ArrayList<PropertyFilterSpec>();
			propertyFilterSpecList.add(propertyFilterSpec);
			List<PropertyFilterSpec> listpfs = new ArrayList<PropertyFilterSpec>();
			listpfs.add(propertyFilterSpec);
			List<ObjectContent> listobjcont = retrievePropertiesAllObjects(listpfs);

			if (listobjcont != null)
			{
				for (ObjectContent oc : listobjcont)
				{
					List<DynamicProperty> dps = oc.getPropSet();
					if (dps != null)
					{
						for (DynamicProperty dp : dps)
						{
							List<PerfCounterInfo> pcinfolist =
									((ArrayOfPerfCounterInfo) dp.getVal())
											.getPerfCounterInfo();
							pciArr.addAll(pcinfolist);
						}
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
			e.printStackTrace();
		}
		if( pciArr.size() > 0 )
			return pciArr;
		else
			return null;
	}
	
	private static void printEntityCounters(String entityType)
			throws Exception
	{
		ManagedObjectReference mor = getEntityByName(entityname, entityType);

		List<PerfCounterInfo> cInfo = getPerfCounters();

		if (mor != null)
		{
			Set<?> ids = getPerfIdsAvailable(perfManager, mor);
			PrintWriter out = new PrintWriter(new BufferedWriter(
					new FileWriter(filename)));
			out.println("<perf-counters>");
			if (cInfo != null)
			{
				for (int c = 0; c < cInfo.size(); ++c)
				{
					PerfCounterInfo pci = cInfo.get(c);
					Integer id = new Integer(pci.getKey());
			
					if (ids.contains(id))
					{
						out.print("  <perf-counter key=\"");
						out.print(id);
						out.print("\" ");

						out.print("rollupType=\"");
						out.print(pci.getRollupType());
						out.print("\" ");

						out.print("statsType=\"");
						out.print(pci.getStatsType());
						out.println("\">");

						printElementDescription(out, "groupInfo", pci.getGroupInfo());
						printElementDescription(out, "nameInfo",  pci.getNameInfo());
						printElementDescription(out, "unitInfo",  pci.getUnitInfo());

						out.println("   <entity type=\"" + entityType + "\"/>");
						out.println("   <getlevel>" + pci.getLevel() + "</getlevel>");
						out.println("   <getperdevicelevel>" + pci.getPerDeviceLevel() + "</getperdevicelevel>");

						List<Integer> listint = pci.getAssociatedCounterId();
System.out.println( "pci.gAssCID.size = " + listint.size() + " pci.value = " + pci.getAssociatedCounterId());

						for (int i = 0; i < listint.size(); i++)
						{
							out.println("    <associatedCounter>" + listint.get(i) + "</associatedCounter>");
						}
						out.println("  </perf-counter>");
					}
				}
			}
			out.println("</perf-counters>");
			out.flush();
			out.close();
			System.out.println("Check " + filename + " for Print Counters");
		}
		else
		{
			System.out.println(entityType + " " + entityname + " not found.");
		}
	}

	private static void printElementDescription(PrintWriter out,
			String name,
			ElementDescription ed)
	{
		out.print("   <" + name + "-key>");
		out.print(ed.getKey());
		out.println("</" + name + "-key>");

		out.print("   <" + name + "-label>");
		out.print(ed.getLabel());
		out.println("</" + name + "-label>");

		out.print("   <" + name + "-summary>");
		out.print(ed.getSummary());
		out.println("</" + name + "-summary>");
	}

	private static Set<Integer> getPerfIdsAvailable(
			ManagedObjectReference perfMoRef,
			ManagedObjectReference entityMoRef)
			throws Exception
	{
		Set<Integer> ret = new HashSet<Integer>();
		if (entityMoRef != null)
		{
			List<PerfMetricId> listpermids =
					vimPort.queryAvailablePerfMetric(perfMoRef,
							entityMoRef, null, null, new Integer(300));

			if (listpermids != null)
			{
				for (int i = 0; i < listpermids.size(); ++i)
				{
					ret.add(new Integer(listpermids.get(i).getCounterId()));
				}
			}
		}
		return ret;
	}

	/**
	 * 
	 * @return An array of SelectionSpec covering all the entities that provide
	 *         performance statistics. The entities that provide performance
	 *         statistics are VM, Host, Resource pool, Cluster Compute Resource
	 *         and Datastore.
	 */
	private static SelectionSpec[] buildFullTraversal()
	{

		// Terminal traversal specs

		// RP -> VM
		TraversalSpec rpToVm = new TraversalSpec();
		rpToVm.setName("rpToVm");
		rpToVm.setType("ResourcePool");
		rpToVm.setPath("vm");
		rpToVm.setSkip(Boolean.FALSE);

		// vApp -> VM
		TraversalSpec vAppToVM = new TraversalSpec();
		vAppToVM.setName("vAppToVM");
		vAppToVM.setType("VirtualApp");
		vAppToVM.setPath("vm");

		// HostSystem -> VM
		TraversalSpec hToVm = new TraversalSpec();
		hToVm.setType("HostSystem");
		hToVm.setPath("vm");
		hToVm.setName("HToVm");
		hToVm.setSkip(Boolean.FALSE);

		// DC -> DS
		TraversalSpec dcToDs = new TraversalSpec();
		dcToDs.setType("Datacenter");
		dcToDs.setPath("datastore");
		dcToDs.setName("dcToDs");
		dcToDs.setSkip(Boolean.FALSE);

		// For RP -> RP recursion
		SelectionSpec rpToRpSpec = new SelectionSpec();
		rpToRpSpec.setName("rpToRp");

		// Recurse through all ResourcePools
		TraversalSpec rpToRp = new TraversalSpec();
		rpToRp.setType("ResourcePool");
		rpToRp.setPath("resourcePool");
		rpToRp.setSkip(Boolean.FALSE);
		rpToRp.setName("rpToRp");
		SelectionSpec[] sspecs = new SelectionSpec[] { rpToRpSpec };
		rpToRp.getSelectSet().addAll(Arrays.asList(sspecs));

		TraversalSpec crToRp = new TraversalSpec();
		crToRp.setType("ComputeResource");
		crToRp.setPath("resourcePool");
		crToRp.setSkip(Boolean.FALSE);
		crToRp.setName("crToRp");
		SelectionSpec[] sspecarrayrptorprtptovm = new SelectionSpec[] { rpToRp };
		crToRp.getSelectSet().addAll(Arrays.asList(sspecarrayrptorprtptovm));

		TraversalSpec crToH = new TraversalSpec();
		crToH.setSkip(Boolean.FALSE);
		crToH.setType("ComputeResource");
		crToH.setPath("host");
		crToH.setName("crToH");
		crToH.getSelectSet().add(hToVm);

		// For Folder -> Folder recursion
		SelectionSpec sspecvfolders = new SelectionSpec();
		sspecvfolders.setName("VisitFolders");

		TraversalSpec dcToHf = new TraversalSpec();
		dcToHf.setSkip(Boolean.FALSE);
		dcToHf.setType("Datacenter");
		dcToHf.setPath("hostFolder");
		dcToHf.setName("dcToHf");
		dcToHf.getSelectSet().add(sspecvfolders);

		TraversalSpec vAppToRp = new TraversalSpec();
		vAppToRp.setName("vAppToRp");
		vAppToRp.setType("VirtualApp");
		vAppToRp.setPath("resourcePool");
		SelectionSpec[] vAppToVMSS = new SelectionSpec[] { rpToRpSpec };
		vAppToRp.getSelectSet().addAll(Arrays.asList(vAppToVMSS));

		TraversalSpec dcToVmf = new TraversalSpec();
		dcToVmf.setType("Datacenter");
		dcToVmf.setSkip(Boolean.FALSE);
		dcToVmf.setPath("vmFolder");
		dcToVmf.setName("dcToVmf");
		dcToVmf.getSelectSet().add(sspecvfolders);

		TraversalSpec visitFolders = new TraversalSpec();
		visitFolders.setType("Folder");
		visitFolders.setPath("childEntity");
		visitFolders.setSkip(Boolean.FALSE);
		visitFolders.setName("VisitFolders");
		List<SelectionSpec> sspecarrvf = new ArrayList<SelectionSpec>();
		sspecarrvf.add(crToRp);
		sspecarrvf.add(crToH);
		sspecarrvf.add(dcToVmf);
		sspecarrvf.add(dcToHf);
		sspecarrvf.add(vAppToRp);
		sspecarrvf.add(vAppToVM);
		sspecarrvf.add(dcToDs);
		sspecarrvf.add(rpToVm);
		sspecarrvf.add(sspecvfolders);
		visitFolders.getSelectSet().addAll(sspecarrvf);

		return new SelectionSpec[] { visitFolders };
	}

	/**
	 * Getting the MOREF of the entity.
	 */
	private static ManagedObjectReference getEntityByName(String entityName,
			String entityType)
	{
		ManagedObjectReference retVal = null;
		try
		{
			// Create Property Spec
			PropertySpec propertySpec = new PropertySpec();
			propertySpec.setAll(Boolean.FALSE);
			propertySpec.setType(entityType);
			propertySpec.getPathSet().add("name");
			List<PropertySpec> propertySpecs = new ArrayList<PropertySpec>();
			propertySpecs.add(propertySpec);

			// Now create Object Spec
			ObjectSpec objectSpec = new ObjectSpec();
			objectSpec.setObj(rootRef);
			objectSpec.setSkip(Boolean.TRUE);
			objectSpec.getSelectSet().addAll(
					Arrays.asList(buildFullTraversal()));
			List<ObjectSpec> objectSpecs = new ArrayList<ObjectSpec>();
			objectSpecs.add(objectSpec);

			// Create PropertyFilterSpec using the PropertySpec and ObjectPec
			// created above.
			PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
			propertyFilterSpec.getPropSet().add(propertySpec);
			propertyFilterSpec.getObjectSet().add(objectSpec);

			List<PropertyFilterSpec> propertyFilterSpecs =
					new ArrayList<PropertyFilterSpec>();
			propertyFilterSpecs.add(propertyFilterSpec);

			List<PropertyFilterSpec> listpfs = new ArrayList<PropertyFilterSpec>(
					1);
			listpfs.add(propertyFilterSpec);
			List<ObjectContent> listobjcont = retrievePropertiesAllObjects(listpfs);
			if (listobjcont != null)
			{
				for (ObjectContent oc : listobjcont)
				{
					if (getEntityName(oc.getObj(), entityType).equals(
							entityName))
					{
						retVal = oc.getObj();
						break;
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
			e.printStackTrace();
		}
		return retVal;
	}

	private static String getEntityName(ManagedObjectReference obj,
			String entityType)
	{
		String retVal = null;
		try
		{
			// Create Property Spec
			PropertySpec propertySpec = new PropertySpec();
			propertySpec.setAll(Boolean.FALSE);
			propertySpec.getPathSet().add("name");
			propertySpec.setType(entityType);
			List<PropertySpec> propertySpecs = new ArrayList<PropertySpec>();
			propertySpecs.add(propertySpec);

			// Now create Object Spec
			ObjectSpec objectSpec = new ObjectSpec();
			objectSpec.setObj(obj);
			List<ObjectSpec> objectSpecs = new ArrayList<ObjectSpec>();
			objectSpecs.add(objectSpec);

			// Create PropertyFilterSpec using the PropertySpec and ObjectPec
			// created above.
			PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
			propertyFilterSpec.getPropSet().add(propertySpec);
			propertyFilterSpec.getObjectSet().add(objectSpec);

			List<PropertyFilterSpec> propertyFilterSpecs =
					new ArrayList<PropertyFilterSpec>();
			propertyFilterSpecs.add(propertyFilterSpec);

			List<PropertyFilterSpec> listfps = new ArrayList<PropertyFilterSpec>(
					1);
			listfps.add(propertyFilterSpec);
			List<ObjectContent> listobjcont = retrievePropertiesAllObjects(listfps);
			if (listobjcont != null)
			{
				for (ObjectContent oc : listobjcont)
				{
					List<DynamicProperty> dps = oc.getPropSet();
					if (dps != null)
					{
						for (DynamicProperty dp : dps)
						{
							retVal = (String) dp.getVal();
							return retVal;   // one heck of a short-circuit exit!
						}
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
			e.printStackTrace();
		}
		return retVal;
	}

	private static void printSoapFaultException(SOAPFaultException sfe)
	{
		System.out.println("SOAP Fault -");
		if (sfe.getFault().hasDetail())
		{
			System.out.println(sfe.getFault().getDetail().getFirstChild()
					.getLocalName());
		}
		if (sfe.getFault().getFaultString() != null)
		{
			System.out
					.println("\n Message: " + sfe.getFault().getFaultString());
		}
	}

	private static void printUsage()
	{
    System.out.println("This sample writes available VM, Hostsystem or ResourcePool");
    System.out.println("perf counters into the file specified.");
    System.out.println("");
    System.out.println("Parameters:");
    System.out.println("----------------------------------------------------------------------");
    System.out.println("url          [required] : url of the web service.");
    System.out.println("username     [required] : username for the authentication");
    System.out.println("password     [required] : password for the authentication");
    System.out.println("entitytype   [required] : Managed entity");
    System.out.println("                          [HostSystem|VirtualMachine|ResourcePool]");
    System.out.println("entityname   [required] : name of the managed entity");
    System.out.println("filename     [required] : Full path of filename to write to");
    System.out.println("----------------------------------------------------------------------");
    System.out.println("Command:");
    System.out.println("");
    System.out.println("Save counters available for a host");
    System.out.println("run.bat com.vmware.performance.PrintCounters");
    System.out.println("--url https://myHost.com/sdk");
    System.out.println("--username [user]  --password [password] --entitytype HostSystem");
    System.out.println("--entityname myHost.com --filename myHostCounters");
	}

	public void TestPrintCounters(String[] args)
	{
        log.info( "pre-paramaters" );
		try
		{
			getParameters(args);
			if (help)
			{
				printUsage();
                log.info( "ifhelp" );
				return;
			}
            log.info( "postparameters" );
			connect();
            log.info( "postconnect" );
			printCounters();
            log.info( "post printCounters" );
		}
		catch (IllegalArgumentException e)
		{
			System.out.println(e.getMessage());
			printUsage();
            log.info( "Boing" );
		}
		catch (SOAPFaultException sfe)
		{
			printSoapFaultException(sfe);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
                log.info( "before disconnect" );
				disconnect();
                log.info( "after  disconnect" );
			}
			catch (SOAPFaultException sfe)
			{
				printSoapFaultException(sfe);
			}
			catch (Exception e)
			{
				System.out.println("Failed to disconnect - " + e.getMessage());
				e.printStackTrace();
			}
		}
        log.info( "heading out" );
	}
}
