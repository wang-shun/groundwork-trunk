package org.groundwork.cloudhub.connectors.vmware;

import com.vmware.vim25.ArrayOfPerfCounterInfo;
import com.vmware.vim25.DynamicProperty;
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
import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.ConnectionState;

import javax.xml.ws.soap.SOAPFaultException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by dtaylor on 8/22/16.
 * This class is not used. It was all intermixed in the VMWareConnector implementation, so I pulled it out
 * and saved it off to another file outside of the VMWareConnector
 * I believe the initial intent was to make use of the new PerformanceMonitor APIs
 */
public class RealTimePrototype {

    private static Logger log = Logger.getLogger(RealTimePrototype.class);

    private VimService vimService;
    private VimPortType vimPort;
    private ServiceContent serviceContent;
    private ManagedObjectReference perfManager;
    private ManagedObjectReference rootFolder;
    private ManagedObjectReference propertyCollector;
    private ConnectionState connectionState = ConnectionState.NASCENT;

    public static void main(String[] args) throws Exception {
        RealTimePrototype realTime = new RealTimePrototype();
        realTime.doRealTime("bernina.groundwork.groundworkopensource.com");
    }

    public void doRealTime(String vemaVM) throws Exception
	{
		ManagedObjectReference vmmor = getVmByVMname( vemaVM );
        int selectedChoice;

		if ( vmmor != null )
		{
			List<PerfCounterInfo> cInfo = getPerfCounters();
			List<PerfCounterInfo> vmCpuCounters = new ArrayList<PerfCounterInfo>();
			Map<Integer, PerfCounterInfo> counters =
					new ConcurrentHashMap<Integer, PerfCounterInfo>();

			int ct = 0;
			int lo = 6;
			int hi = 8;
			for ( int i = 0; i < cInfo.size(); ++i )
			{
                PerfCounterInfo pci = cInfo.get(i);

				  String key1 = pci.getGroupInfo().getKey();
                String key2 = pci.getNameInfo().getKey();
                String key3 = pci.getStatsType().toString();
                String key4 = pci.getUnitInfo().getKey();
                //String key5 = pci.getDynamicType();
                String key6 = pci.getRollupType().toString();
                String key7 = pci.getNameInfo().getSummary();
                String msg  = "";

                if ( !"net".equalsIgnoreCase( key1 ) ||  ( ct++ < lo || ct > hi ) )
                {
                	msg += "* ";
                    vmCpuCounters.add( pci );
                    counters.put(new Integer(pci.getKey()), pci); // testing "add all that match"
                }
                else {
                    msg += "(" + ct + ")...";
                }

                log.info( msg +
                        "key[" + i + "]> "
                        + key1 + " / "
                        + key2 + " / "
                        + key3 + " / "
                        + key4 + " / "
                  //      + key5 + " / "
                        + key6 + " / "
                        + key7 );
			}

			while ( true )
			{
				int i = 0;

                // print possible choices (using i)
				for ( i = 0; i < vmCpuCounters.size(); i++ )
				{
					log.info( (i+1) + " - " + vmCpuCounters.get(i).getNameInfo().getSummary() );
				}
				log.info( "Please select a counter from the above list"
                        + "\nEnter 0 to end: " );
//				BufferedReader reader =
//						new BufferedReader( new InputStreamReader( System.in ));
//				i = Integer.parseInt( reader.readLine()) - 1;
				i = 17;
				i = 10;

				selectedChoice = --i;   // decrementing 'i' important for right choice

				if ( selectedChoice >= vmCpuCounters.size() )
				{
					log.info( "*** Value chosen too high! ***" );
				}
				else
				{
					if ( selectedChoice < 0 )
						return;

					PerfCounterInfo pcInfo = ( PerfCounterInfo ) vmCpuCounters
							.get( selectedChoice );
//					counters.put( new Integer( pcInfo.getKey()), pcInfo );
					break;
				}
			}
			List<PerfMetricId> listpermeid = vimPort.queryAvailablePerfMetric(
					perfManager, vmmor, null, null, new Integer( 20 ) );
			ArrayList<PerfMetricId> mMetrics = new ArrayList<PerfMetricId>();

			if ( listpermeid != null )
			{
				if ( counters.containsKey(
                    new Integer( listpermeid.get( selectedChoice )
                        .getCounterId() )))
				{
					mMetrics.add( listpermeid.get( selectedChoice ) );
                    log.info( "Adding listpermeid: " + selectedChoice );
				}
			}
			monitorPerformance( perfManager, vmmor, mMetrics, counters );
		}
		else
		{
			log.info( "doRealTime(): Virtual Machine " + vemaVM + " not found" );
		}
	}

    /**
     * @param pmRef
     * @param vmRef
     * @param mMetrics
     * @param counters
     * @throws Exception
     */
    private void monitorPerformance(
            ManagedObjectReference pmRef,
            ManagedObjectReference vmRef,
            ArrayList<PerfMetricId> mMetrics,
            Map<Integer, PerfCounterInfo> counters) throws Exception {
        PerfQuerySpec qSpec = new PerfQuerySpec();

        qSpec.setEntity(vmRef);
        qSpec.setMaxSample(new Integer(10)); // gather 10 samples per
        qSpec.getMetricId().addAll(mMetrics);
        qSpec.setIntervalId(new Integer(20)); // in a 20 sec window

        List<PerfQuerySpec> qSpecList = new ArrayList<PerfQuerySpec>();
        qSpecList.add(qSpec);

        while (true) {
            List<PerfEntityMetricBase>
                    listpemb = vimPort.queryPerf(pmRef, qSpecList);

            List<PerfEntityMetricBase>
                    pValues = listpemb;

            if (pValues != null)
                displayValues(pValues, counters);

            //		log.debug( "[Breakpoint with no iterations] ..." );
            break;
//			Thread.sleep( 10 * 1000 );   // milliseconds
        }
    }

    /**
     * Get the MOR of the Virtual Machine by its name.
     *
     * @param vmName The name of the Virtual Machine
     * @return The Managed Object reference for this VM
     */
    private ManagedObjectReference getVmByVMname(String vmName) {
        ManagedObjectReference retVal = null;

        try {
            TraversalSpec tSpec = getVMTraversalSpec();

            // Create Property Spec
            PropertySpec propertySpec = new PropertySpec();
            propertySpec.setAll(false);
            propertySpec.getPathSet().add("name");
            propertySpec.setType("VirtualMachine");

            // Now create Object Spec
            ObjectSpec
                    objectSpec = new ObjectSpec();
            objectSpec.setObj(rootFolder);
            objectSpec.setSkip(true);
            objectSpec.getSelectSet().add(tSpec);

            // Create PropertyFilterSpec using the PropertySpec and ObjectPec
            // created above.
            PropertyFilterSpec
                    propertyFilterSpec = new PropertyFilterSpec();
            propertyFilterSpec.getPropSet().add(propertySpec);
            propertyFilterSpec.getObjectSet().add(objectSpec);

            List<PropertyFilterSpec>
                    propertyFilterSpecList = new ArrayList<PropertyFilterSpec>(1);
            propertyFilterSpecList.add(propertyFilterSpec);
            List<ObjectContent> objectContentList = retrievePropertiesAllObjects(propertyFilterSpecList);

            if (objectContentList != null) {
                for (ObjectContent oc : objectContentList) {
                    ManagedObjectReference manObRef = oc.getObj();
                    String vmnm = null;
                    List<DynamicProperty> dpList = oc.getPropSet();

                    if (dpList != null) {
                        // 120605.rlynch: I'm not clear why 'loop thru all' needed.
                        // 120612.rlynch: I'd have thought there'd be a test here
                        //                followed by a BREAK.
                        //
                        for (DynamicProperty dp : dpList) {
                            vmnm = (String) dp.getVal();
                        }
                    }
                    if (vmnm != null && vmnm.equals(vmName)) {
                        retVal = manObRef;
                        break;
                    }
                }
            }
        } catch (SOAPFaultException sfe) {
            printSoapFaultException(sfe);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retVal;
    }

    /**
     * This method initializes all the performance counters available on the
     * system it is connected to. The performance counters are stored in the
     * hashmap counters with group.counter.rolluptype being the key and id being
     * the value.
     */
    private List<PerfCounterInfo> getPerfCounters() {
        List<PerfCounterInfo> pciList = new ArrayList<PerfCounterInfo>();

        try {
            // Create Property Spec
            PropertySpec
                    propertySpec = new PropertySpec();
            propertySpec.setAll(false);
            propertySpec.getPathSet().add("perfCounter");
            propertySpec.setType("PerformanceManager");

            // Create Object Spec for perfManager
            ObjectSpec
                    objectSpec = new ObjectSpec();
            objectSpec.setObj(perfManager);

            // Create PropertyFilterSpec using the PropertySpec and ObjectPec
            // created above.
            PropertyFilterSpec
                    propertyFilterSpec = new PropertyFilterSpec();
            propertyFilterSpec.getPropSet().add(propertySpec);
            propertyFilterSpec.getObjectSet().add(objectSpec);

            List<PropertyFilterSpec>
                    propertyFilterSpecList = new ArrayList<PropertyFilterSpec>();
            propertyFilterSpecList.add(propertyFilterSpec);

            List<ObjectContent>
                    objectContentList = retrievePropertiesAllObjects(propertyFilterSpecList);

            if (objectContentList != null) {
                for (ObjectContent oc : objectContentList) {
                    List<DynamicProperty> dpList = oc.getPropSet();
                    if (dpList != null) {
                        for (DynamicProperty dp : dpList) {
                            List<PerfCounterInfo> pcinfolist
                                    = ((ArrayOfPerfCounterInfo) dp
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
                            pciList.addAll(pcinfolist);
                        }
                    }
                }
            }
        } catch (SOAPFaultException sfe) {
            printSoapFaultException(sfe);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pciList;
    }

    /**
     * @return TraversalSpec specification to get to the VirtualMachine managed
     * object.
     */
    private static TraversalSpec getVMTraversalSpec() {
        // Create a traversal spec that starts from the 'root' objects
        // and traverses the inventory tree to get to the VirtualMachines.
        // Build the traversal specs bottoms up

        // Traversal to get to the VM in a VApp
        TraversalSpec
                vAppToVM = new TraversalSpec();
        vAppToVM.setName("vAppToVM");
        vAppToVM.setType("VirtualApp");
        vAppToVM.setPath("vm");

        // Traversal spec for VApp to VApp
        TraversalSpec
                vAppToVApp = new TraversalSpec();
        vAppToVApp.setName("vAppToVApp");
        vAppToVApp.setType("VirtualApp");
        vAppToVApp.setPath("resourcePool");

        // SelectionSpec for VApp to VApp recursion
        SelectionSpec
                vAppRecursion = new SelectionSpec();
        vAppRecursion.setName("vAppToVApp");

        // SelectionSpec to get to a VM in the VApp
        SelectionSpec
                vmInVApp = new SelectionSpec();
        vmInVApp.setName("vAppToVM");

        // SelectionSpec for both VApp to VApp and VApp to VM
        List<SelectionSpec>
                vAppToVMSS = new ArrayList<SelectionSpec>();
        vAppToVMSS.add(vAppRecursion);
        vAppToVMSS.add(vmInVApp);
        vAppToVApp.getSelectSet().addAll(vAppToVMSS);

        // This SelectionSpec is used for recursion for Folder recursion
        SelectionSpec
                sSpec = new SelectionSpec();
        sSpec.setName("VisitFolders");

        // Traversal to get to the vmFolder from DataCenter
        TraversalSpec
                dataCenterToVMFolder = new TraversalSpec();
        dataCenterToVMFolder.setName("DataCenterToVMFolder");
        dataCenterToVMFolder.setType("Datacenter");
        dataCenterToVMFolder.setPath("vmFolder");
        dataCenterToVMFolder.setSkip(false);
        dataCenterToVMFolder.getSelectSet().add(sSpec);

        // TraversalSpec to get to the DataCenter from rootFolder
        TraversalSpec
                traversalSpec = new TraversalSpec();
        traversalSpec.setName("VisitFolders");
        traversalSpec.setType("Folder");
        traversalSpec.setPath("childEntity");
        traversalSpec.setSkip(false);

        List<SelectionSpec>
                sSpecArr = new ArrayList<SelectionSpec>();
        sSpecArr.add(sSpec);
        sSpecArr.add(dataCenterToVMFolder);
        sSpecArr.add(vAppToVM);
        sSpecArr.add(vAppToVApp);

        traversalSpec.getSelectSet().addAll(sSpecArr);

        return traversalSpec;
    }

    private static void displayValues(
            List<PerfEntityMetricBase> values,
            Map<Integer, PerfCounterInfo> counters)    // 'counters' as 'selections'
    {
        for (int i = 0; i < values.size(); ++i) {
            List<PerfMetricSeries> listpems =
                    ((PerfEntityMetric) values.get(i)).getValue();

            List<PerfSampleInfo> listinfo =
                    ((PerfEntityMetric) values.get(i)).getSampleInfo();

			/*log.debug( "Sample time range: "
                    + listinfo.get( 0 ).getTimestamp().toString()
					+ " - "
					+ listinfo.get( listinfo.size() - 1 )
                        .getTimestamp().toString() );*/

            //log.debug( "listpems.size = '" + listpems.size() + "'");
            for (int vi = 0; vi < listpems.size(); ++vi) {
                StringBuffer s = new StringBuffer(250); // init capacity
                PerfMetricSeries pems = listpems.get(vi);
                int counterId = pems.getId().getCounterId();
                PerfCounterInfo pci = (PerfCounterInfo) counters.get(counterId);

                if (pci != null) {
                    s.append(pci.getNameInfo().getSummary());
                    s.append(":");
                }

                if (pems instanceof PerfMetricIntSeries) {
                    for (Long k : ((PerfMetricIntSeries) pems).getValue()) {
                        s.append(k);
                        s.append(" ");
                    }
                    //log.debug( s );
                }
                //			else log.debug( "PerfCounter[" + vi + "] not instance of PerfMetricIntSeries");
            }
        }
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
            List<PropertyFilterSpec> propertyFilterSpecList)
            throws Exception {
        RetrieveOptions retrieveOptions = new RetrieveOptions();

        List<ObjectContent> objectContentList = new ArrayList<ObjectContent>();

        try {
            RetrieveResult results = vimPort.retrievePropertiesEx(
                    propertyCollector,
                    propertyFilterSpecList,
                    retrieveOptions);

            if (results != null
                    && results.getObjects() != null
                    && !results.getObjects().isEmpty()) {
                objectContentList.addAll(results.getObjects());
            }

            String token = null;

            if (results != null)
                token = results.getToken();

            while (token != null && !token.isEmpty()) {
                results = vimPort.continueRetrievePropertiesEx(
                        propertyCollector, token);
                token = null;

                // 120612.rlynch: apparently, its implemented as a poor man's linked list.

                if (results != null) {
                    token = results.getToken();    // to follow the chain along...

                    if (results.getObjects() != null
                            && !results.getObjects().isEmpty()) {
                        objectContentList.addAll(results.getObjects());
                    }
                }
            }
        } catch (SOAPFaultException sfe) {
            printSoapFaultException(sfe);
        } catch (Exception e) {
            log.error("Failed Getting Contents ", e);
        }

        return objectContentList;  // then throw them back.
    }


    private static void printSoapFaultException(SOAPFaultException sfe) {
        //log.debug( "SOAP Fault -" );
        if (sfe.getFault().hasDetail()) {
            if (log.isDebugEnabled())
                log.debug(sfe.getFault().getDetail().getFirstChild().getLocalName());
        }

        if (sfe.getFault().getFaultString() != null) {
            if (log.isDebugEnabled())
                log.debug("\n Message: " + sfe.getFault().getFaultString());
        }
    }

    /**
     * This method is no longer used after first refactoring of original work
     * @deprecated
     * @param targetHost
     * @return
     */
    public ArrayList<String> getListVM(String targetHost) {
        ArrayList<String> returnValues = new ArrayList<String>();

        if (connectionState != ConnectionState.CONNECTED) {
            log.error("getListVM(): not connected");
            return null;
        }
        ManagedObjectReference viewMgrRef = serviceContent.getViewManager();
        ManagedObjectReference propColl = serviceContent.getPropertyCollector();

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

        try {
            cViewRef = vimPort.createContainerView(
                    viewMgrRef,
                    serviceContent.getRootFolder(),
                    listContainers,
                    true);
        } catch (Exception e) {
            log.error("vimPort.createContainerView(...) exception: ", e);
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

        try {
            boolean firstround = true;      // these are temporary
            RetrieveResult props = null;

            while (true) {
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
                if (firstround)
                    props = vimPort.retrievePropertiesEx(propColl, fSpecList, ro);
                else if (props.getToken() != null)
                    props = vimPort.continueRetrievePropertiesEx(
                            propColl, props.getToken());
                else
                    break;

                firstround = false;

                if (props != null)
                    ocList.addAll(props.getObjects());
            }
        } catch (Exception e) {
            log.error("vimPort2.retrievePropertiesEx(...) error: ", e);
        }

        for (ObjectContent oc : ocList) {
            List<DynamicProperty> dpList = oc.getPropSet();

            if (dpList == null)
                continue;

            for (DynamicProperty dp : dpList) {
                if (dp.getName().equals("name"))
                    returnValues.add((String) dp.getVal());
            }
        }

        if (cViewRef != null) {
            try {
                vimPort.destroyView(cViewRef);
            }
            catch (Exception e) {
                log.error("Failed to free container view: " + e.getMessage(), e);
            }
        }
//        return returnValues.size() == 0 ? null : returnValues;  // using NULL as flag
        return returnValues;
    }


}
