package com.groundwork.agents.vema.vmware.deprecated;

// import com.vmware.vim25.*;
import javax.xml.ws.soap.SOAPFaultException;

import org.apache.log4j.Logger;

import com.groundwork.agents.vema.vmware.connector.VemaVMware;

// import javax.net.ssl.HostnameVerifier;
// import javax.net.ssl.HttpsURLConnection;
// import javax.net.ssl.SSLSession;
// import java.io.BufferedReader;
// import java.io.InputStreamReader;
// import java.io.File;
// import javax.xml.parsers.*;


/**
 * <pre>
 * VMscantest
 * 
 * A utility-application to scan virtual-machine processing cloud
 * APIs (application-programming-interfaces), translating the results 
 * obtained, on a periodic basis, to the format(s) that may directly
 * be used by the GroundWorkOpenSystems (GWOS) systems management 
 * software.
 * 
 * <b>Bootstrap Parameters:</b>
 * --configfile {PATH}   [req]: configuration file
 * --accessauth {AUTH}   [req]: authentication string for VM resource
 * 
 * NOTE: While {AUTH} may be contained in the {configfile}, better 
 * adherence to strong security measures suggests that ephemeral 
 * command-line data is preferred over file-containing data.  
 * 
 * NOTE: the format of {AUTH} is command-line friendly mod-XML as in: 
 * 
 *   --accessauth {accessauth}
 *                {user}bob{/user}
 *                {pass}TheMonkey{/pass}
 *                {url}//plants.groundwork.com{/url}
 *                {vmname}bananatree{/vmname}
 *                {/accessauth}
 * 
 * Where this would be on the command line without imbedded 
 * carriage-returns. The curly-braces correspond to XML's angle-brackets.
 * 
 * The authorization parameters are:
 * 
 * accessauth            [req]: establish the authorization node
 * url | system          [req]: url of the web service
 * user[name]            [req]: username for the authentication
 * pass[word]            [req]: password for the authentication
 * vm[name]              [req]: name of the vm to address
 * 
 * </pre>
 */

public class VemaScantest
{
	private static String						url;            // basic URL to access [IP resolver]
	private static String						username;       // and authentication name
	private static String						password;       // ... plus password
	private static String						vm_name;        // vm-name within url
	private static boolean						help = false;   // flag leading to help-printout
    private static org.apache.log4j.Logger      log = Logger.getLogger(VemaVMware.class);

    public VemaScantest()
    {
    }

	private static void getConnectionParameters(String[] args)
			throws IllegalArgumentException
	{
		int ai;
		String param;
		String nextp;
		String es = "";	// 'empty' serves as a flag too.
        String ws = ""; // 'warning' string... not an error per-se

		for(ai = 0; ai < args.length; ai++)
		{
			param = args[ai].trim();
			nextp = ( ai + 1 < args.length ) ? args[++ai].trim() : "";

			if (param.equalsIgnoreCase("--help"))
				help = true;

			else if (param.equalsIgnoreCase("--url") 
					&& !nextp.startsWith("--")
					&& !nextp.isEmpty())
				url = nextp;
			
			else if (param.equalsIgnoreCase("--username")
					&& !nextp.startsWith("--") 
					&& !nextp.isEmpty())
				username = nextp;
			
			else if (param.equalsIgnoreCase("--password")
					&& !nextp.startsWith("--") 
					&& !nextp.isEmpty())
				password = nextp;

			else if (param.equalsIgnoreCase("--vmname") 
					&& !nextp.startsWith("--") 
					&& !nextp.isEmpty())
				vm_name = nextp;
			
			else
                ws += " Unknown param[" + param + "]";
		}
		
		if (url       == null)	es += " --url {URL}";
		if (username  == null)	es += " --username {USERNAME}";
		if (password  == null)	es += " --password {PASSWORD}";
		if (vm_name   == null)  es += " --vmname {VirtualMachineName}";

        if ( !ws.isEmpty() )
            log.info( "WARNING - " + ws );

		if ( !es.isEmpty() )
			throw new IllegalArgumentException("Expected:" + es);
	}

/*	private static void doRealTime() throws Exception
	{
		ManagedObjectReference vmmor = getVmByVMname(virtualmachinename);

		if (vmmor != null)
		{
			List<PerfCounterInfo> cInfo         = getPerfCounters();
			List<PerfCounterInfo> vmCpuCounters = new ArrayList<PerfCounterInfo>();
			for (int i = 0; i < cInfo.size(); ++i)
			{
				if ("cpu".equalsIgnoreCase(cInfo.get(i).getGroupInfo().getKey()))
				{
					vmCpuCounters.add(cInfo.get(i));
				}
			}
			Map<Integer, PerfCounterInfo> counters = new ConcurrentHashMap<Integer, PerfCounterInfo>();
			while (true)
			{
				int i = 0;
	
				for (Iterator<PerfCounterInfo> it = vmCpuCounters.iterator(); it
						.hasNext();)
				{
					PerfCounterInfo pcInfo = (PerfCounterInfo) it.next();
					log.info(++i + " - "
							+ pcInfo.getNameInfo().getSummary());
				}
				log.info("Please select a counter from"
						+ " the above list" + "\nEnter 0 to end: ");
//				BufferedReader reader =
//						new BufferedReader(new InputStreamReader(System.in));
//				i = Integer.parseInt(reader.readLine());
				i = 17;	// 120503.rlynch:.

				setIndex(i-1);	// 120504.rlynch: fixes "off by 1" bug.
				
				if (i > vmCpuCounters.size())
				{
					log.info("*** Value out of range!");
				}
				else
				{
					--i;	// zero [end] becomes -1...
					if (i < 0)
					{
						return;
					}
					PerfCounterInfo pcInfo = (PerfCounterInfo) vmCpuCounters.get(i);
					counters.put(new Integer(pcInfo.getKey()), pcInfo);
					break;
				}
			}
			List<PerfMetricId> listpermeid = vimPort.queryAvailablePerfMetric(
					perfManager, vmmor, null, null, new Integer(20));
			ArrayList<PerfMetricId> mMetrics = new ArrayList<PerfMetricId>();
			if (listpermeid != null)
			{
				if (counters.containsKey(new Integer(listpermeid.get(index)
							.getCounterId())))
				{
					mMetrics.add(listpermeid.get(index));
				}
			}
			monitorPerformance(perfManager, vmmor, mMetrics, counters);
		}
		else
		{
			log.info("Virtual Machine " + vm_name
					+ " not found");
		}
	}
*/
	/**
	 * 
	 * @param pmRef
	 * @param vmRef
	 * @param mMetrics
	 * @param counters
	 * @throws Exception
	 */
/*	private static void monitorPerformance(ManagedObjectReference pmRef,
			ManagedObjectReference vmRef,
			ArrayList<PerfMetricId> mMetrics,
			Map counters)
			throws Exception
	{
		PerfQuerySpec qSpec = new PerfQuerySpec();
		
		qSpec.setEntity(vmRef);
		qSpec.setMaxSample(new Integer(10));
		qSpec.getMetricId().addAll(mMetrics);
		qSpec.setIntervalId(new Integer(20));

		List<PerfQuerySpec> qSpecs = new ArrayList<PerfQuerySpec>();
		qSpecs.add(qSpec);
		while (true)
		{
			List<PerfEntityMetricBase> listpemb = vimPort.queryPerf(pmRef,
					qSpecs);
			List<PerfEntityMetricBase> pValues = listpemb;
			if (pValues != null)
			{
				displayValues(pValues, counters);
			}
			log.info("[NOT 3] Sleeping 10 seconds...");
			break;
//			Thread.sleep(10 * 1000);
		}
	}*/

	private static void printSoapFaultException(SOAPFaultException sfe)
	{
		log.info("SOAP Fault -");
		if (sfe.getFault().hasDetail())
			log.info(sfe.getFault().getDetail().getFirstChild().getLocalName());

		if (sfe.getFault().getFaultString() != null)
			log.info("\n Message: " + sfe.getFault().getFaultString());
	}

	private static void printUsage()
	{
		log.info("This sample displays performance measurements from " +
						"the current time at the console.");
		log.info("\nParameters:");
		log.info("url        [required] : url of the web serVemvice.");
		log.info("username   [required] : username for the authentication");
		log.info("password   [required] : password for the authentication");
		log.info("vmname     [required] : name of the vm");
		log.info("\nCommand:");
		log.info("run.bat com.vmware.performance.VITop");
		log.info("--url [webservice url]  --username [user] --password [password]");
		log.info("--vmname [name of the vm]");
	}

	public static void TestVMscantest(String[] args)
	{
        VemaVMware vema = new VemaVMware();

		try
		{
			getConnectionParameters( args );
			if (help)
			{
				printUsage();
				return;
			}
			throw new Exception();
//			TODO: this needs to become connect(parambox)
//			vema.connect( url, username, password, vm_name );
//          vema.doTopTest();
//			vema.doRealTime();
		}
		catch (IllegalArgumentException e)
		{
			log.info(e.getMessage());
			printUsage();
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
				vema.disconnect();
			}
			catch (SOAPFaultException sfe)
			{
				printSoapFaultException(sfe);
			}
			catch (Exception e)
			{
				log.info("Failed to disconnect - " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
}
