package org.groundwork.cloudhub.connectors;

import com.sun.xml.ws.client.BindingProviderProperties;
import com.vmware.vim25.*;
import org.apache.log4j.Logger;
import org.groundwork.cloudhub.ServerConfigurator;
import org.groundwork.cloudhub.connectors.helpers.GetMOREF;
import org.groundwork.cloudhub.connectors.vmware.SnapshotService;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.cloudhub.inventory.InventoryType;
import org.junit.Test;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPFaultException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Use this class to discover properties prior to developing code, tests
 */
public class VMwareInternalsTest {

    private static Logger log = Logger.getLogger(VMwareInternalsTest.class);

    private VimService vimService;
    private VimPortType vimPort;
    private ServiceContent serviceContent;
    private ManagedObjectReference sessionManager;
    private ManagedObjectReference propertyCollector;
    private ManagedObjectReference rootFolder;
    protected GetMOREF getMOREFs;

    @Test
    public void storageTest() throws Exception {
        System.out.println("Storage Test...");
        connect("https://" + ServerConfigurator.VERMONT_VMWARE_SERVER + "/sdk",
                ServerConfigurator.VERMONT_VMWARE_USERNAME,
                ServerConfigurator.VERMONT_VMWARE_PASSWORD);
        retrieveStorageMetrics(rootFolder);
        disconnect();
    }

    @Test
    public void networkTest() throws Exception {
        System.out.println("Network Test...");
        connect("https://" + ServerConfigurator.VERMONT_VMWARE_SERVER + "/sdk",
                ServerConfigurator.VERMONT_VMWARE_USERNAME,
                ServerConfigurator.VERMONT_VMWARE_PASSWORD);
        retrieveNetworkMetrics(rootFolder);
        disconnect();
    }

    @Test
    public void hypervisorTest() throws Exception {
        System.out.println("Hypervisor Test...");
        connect("https://" + ServerConfigurator.VERMONT_VMWARE_SERVER  + "/sdk",
                ServerConfigurator.VERMONT_VMWARE_USERNAME,
                ServerConfigurator.VERMONT_VMWARE_PASSWORD);
        retrieveHypervisorMetrics(rootFolder);
        disconnect();
    }

    @Test
    public void vmTest() throws Exception {
        System.out.println("VM Test...");
        connect("https://" + ServerConfigurator.VERMONT_VMWARE_SERVER  + "/sdk",
                ServerConfigurator.VERMONT_VMWARE_USERNAME,
                ServerConfigurator.VERMONT_VMWARE_PASSWORD);
        retrieveVmStandardMetrics(rootFolder);
        disconnect();
    }

    private void retrieveStorageMetrics(ManagedObjectReference folder) {
        InventoryType inventoryType = InventoryType.Datastore;
        try {
            ManagedObjectReference viewManager = serviceContent.getViewManager();
            ManagedObjectReference containerView = vimPort.createContainerView(viewManager, folder,
                    Arrays.asList(inventoryType.name()), true);

            // Create Property Spec
            PropertySpec propertySpec = new PropertySpec();
            // FIRST RUN: setAll to TRUE to discover all properties
            // namePropertySpec.setAll(Boolean.TRUE);
            propertySpec.setAll(Boolean.TRUE);
            propertySpec.setType(inventoryType.name());
//            propertySpec.getPathSet().add("name");
//            propertySpec.getPathSet().add("summary.capacity");
//            propertySpec.getPathSet().add("summary.freeSpace");
//            propertySpec.getPathSet().add("summary.url");
//            propertySpec.getPathSet().add("summary.type");
//            propertySpec.getPathSet().add("summary.uncommitted");
//            propertySpec.getPathSet().add("overallStatus");

            TraversalSpec ts = new TraversalSpec();
            ts.setName("view");
            ts.setPath("view");
            ts.setSkip(false);
            ts.setType("ContainerView");

            ObjectSpec objectSpec = new ObjectSpec();
            objectSpec.setObj(containerView);
            objectSpec.setSkip(Boolean.TRUE);
            objectSpec.getSelectSet().add(ts);

            PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
            propertyFilterSpec.getPropSet().add(propertySpec);
            propertyFilterSpec.getObjectSet().add(objectSpec);

            List<PropertyFilterSpec> propertyFilterSpecs = new ArrayList<PropertyFilterSpec>();
            propertyFilterSpecs.add(propertyFilterSpec);

            List<ObjectContent> objectContents = vimPort.retrieveProperties(serviceContent.getPropertyCollector(), propertyFilterSpecs);

            if (objectContents != null) {
                for (ObjectContent oc : objectContents) {
                    ManagedObjectReference mr = oc.getObj();
                    List<DynamicProperty> dps = oc.getPropSet();
                    System.out.println("entityType: " + oc.getObj().getType());
                    if (dps != null) {
                        for (DynamicProperty dp : dps) {
                            if (dp.getName().equals("name")) {
                                System.out.println("Name = " + dp.getVal());
                            } else if (dp.getName().equals("info")) {
                                if (dp.getVal() instanceof NasDatastoreInfo) {
                                    NasDatastoreInfo info = (NasDatastoreInfo) dp.getVal();
                                    info.getTimestamp();
                                }
                            } else if (dp.getName().equals("iormConfiguration")) {
                                if (dp.getVal() instanceof NasDatastoreInfo) {
                                    StorageIORMInfo iorm = (StorageIORMInfo) dp.getVal();
                                }
                            } else if (dp.getName().equals("summary")) {
                                if (dp.getVal() instanceof DatastoreSummary) {
                                    DatastoreSummary summary = (DatastoreSummary) dp.getVal();
                                    System.out.format("cap: %d, fs: %d, mm: %s, unc: %d, url: %s, typ: %s, %b\n",
                                            summary.getCapacity(),
                                            summary.getFreeSpace(),
                                            summary.getMaintenanceMode(),
                                            summary.getUncommitted(),
                                            summary.getUrl(),
                                            summary.getType(),
                                            summary.isAccessible());
//                                    List<DynamicProperty> properties = summary.getDynamicProperty();
//                                    for (DynamicProperty p : properties) {
//                                        System.out.println("\t\tDP" + p.getName() + "-" + p.getVal());
//                                    }
                                }
                            }
                            System.out.println("\tdp = " + dp.getName() + " : " + dp.getVal());
//                            } else if (dp.getName().equals("vm")) {
//                                if (dp.getVal() instanceof ArrayOfManagedObjectReference) {
//                                    vmRefList = ((ArrayOfManagedObjectReference) dp.getVal()).getManagedObjectReference();
//                                }
//                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to retrieve managed objects for " + inventoryType.name(), e);
        }

    }

    private void retrieveNetworkMetrics(ManagedObjectReference folder) {
        InventoryType inventoryType = InventoryType.Network;
        try {
            ManagedObjectReference viewManager = serviceContent.getViewManager();
            ManagedObjectReference containerView = vimPort.createContainerView(viewManager, folder,
                    Arrays.asList(inventoryType.name()), true);

            PropertySpec propertySpec = new PropertySpec();
            // FIRST RUN: setAll to TRUE to discover all properties
            // namePropertySpec.setAll(Boolean.TRUE);
            propertySpec.setAll(Boolean.TRUE);
            propertySpec.setType(inventoryType.name());
//            propertySpec.getPathSet().add("name");
//            propertySpec.getPathSet().add("overallStatus");
//            propertySpec.getPathSet().add("summary.accessible");
//            propertySpec.getPathSet().add("summary.ipPoolName");

            TraversalSpec ts = new TraversalSpec();
            ts.setName("view");
            ts.setPath("view");
            ts.setSkip(false);
            ts.setType("ContainerView");

            ObjectSpec objectSpec = new ObjectSpec();
            objectSpec.setObj(containerView);
            objectSpec.setSkip(Boolean.TRUE);
            objectSpec.getSelectSet().add(ts);

            PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
            propertyFilterSpec.getPropSet().add(propertySpec);
            propertyFilterSpec.getObjectSet().add(objectSpec);

            List<PropertyFilterSpec> propertyFilterSpecs = new ArrayList<PropertyFilterSpec>();
            propertyFilterSpecs.add(propertyFilterSpec);

            List<ObjectContent> objectContents = vimPort.retrieveProperties(serviceContent.getPropertyCollector(), propertyFilterSpecs);
            if (objectContents != null) {
                for (ObjectContent oc : objectContents) {
                    List<DynamicProperty> dps = oc.getPropSet();
                    System.out.println("entityType: " + oc.getObj().getType());
                    if (dps != null) {
                        for (DynamicProperty dp : dps) {
                            if (dp.getName().equals("name")) {
                                System.out.println("Name = " + dp.getVal());
                            } else if (dp.getName().equals("summary")) {
                                if (dp.getVal() instanceof NetworkSummary) {
                                    NetworkSummary summary = (NetworkSummary) dp.getVal();
                                    System.out.format("acc: %b, name: %s, ippool: %s\n",
                                            summary.isAccessible(),
                                            summary.getName(),
                                            //summary.getDynamicType(),
                                            summary.getIpPoolName());
                                    ManagedObjectReference network = summary.getNetwork();
                                    System.out.println("net - " + network.getValue() + " - " + network.getType());
//                                    List<DynamicProperty> properties = summary.getDynamicProperty(); summary.ge
//                                    for (DynamicProperty p : properties) {
//                                        System.out.println("\t\tDP" + p.getName() + "-" + p.getVal());
//                                    }

                                }
                            } else if (dp.getName().equals("value")) {
                                if (dp.getVal() instanceof ArrayOfCustomFieldValue) {
                                    ArrayOfCustomFieldValue customFieldValue = (ArrayOfCustomFieldValue) dp.getVal();
                                    List<CustomFieldValue> values = customFieldValue.getCustomFieldValue();
                                    for (CustomFieldValue value : values) {
                                        System.out.println("cfv: " + value.getKey());
                                    }
                                }
                            } else
                                System.out.println("\tdp = " + dp.getName() + " : " + dp.getVal());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to retrieve managed objects for " + inventoryType.name(), e);
        }
    }

    private void retrieveHypervisorMetrics(ManagedObjectReference folder) {
        try {
            ManagedObjectReference viewManager = serviceContent.getViewManager();
            ManagedObjectReference containerView = vimPort.createContainerView(viewManager, folder,
                    Arrays.asList("HostSystem"), true);

            // Create Property Spec
            PropertySpec propertySpec = new PropertySpec();
            propertySpec.setAll(Boolean.TRUE); // return all properties
            propertySpec.setType("HostSystem");
            TraversalSpec ts = new TraversalSpec();
            ts.setName("view");
            ts.setPath("view");
            ts.setSkip(false);
            ts.setType("ContainerView");

            ObjectSpec objectSpec = new ObjectSpec();
            objectSpec.setObj(containerView);
            objectSpec.setSkip(Boolean.TRUE);
            objectSpec.getSelectSet().add(ts);

            PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
            propertyFilterSpec.getPropSet().add(propertySpec);
            propertyFilterSpec.getObjectSet().add(objectSpec);

            List<PropertyFilterSpec> propertyFilterSpecs = new ArrayList<PropertyFilterSpec>();
            propertyFilterSpecs.add(propertyFilterSpec);

            List<ObjectContent> objectContents = vimPort.retrieveProperties(serviceContent.getPropertyCollector(), propertyFilterSpecs);
            if (objectContents != null) {
                for (ObjectContent oc : objectContents) {
                    ManagedObjectReference mr = oc.getObj();
                    List<DynamicProperty> dps = oc.getPropSet();
                    System.out.println("== entityType: " + oc.getObj().getType());
                    if (dps != null) {
                        for (DynamicProperty dp : dps) {
                            System.out.println("  -- dp: " + dp.getName() + " : " + dp.getVal());
                            if (dp.getVal() instanceof HostConfigInfo) {
                                HostConfigInfo configInfo = (HostConfigInfo) dp.getVal();
                                System.out.println("*** IPMI = " + configInfo.getIpmi());
                            } else if (dp.getVal() instanceof HostHardwareInfo) {
                                HostHardwareInfo hardwareInfo = (HostHardwareInfo) dp.getVal();
                                HostCpuPowerManagementInfo powerInfo = hardwareInfo.getCpuPowerManagementInfo();
                                System.out.println("hardinfo = " + hardwareInfo.getSystemInfo().getModel());
                            } else if (dp.getVal() instanceof HostRuntimeInfo) {
                                HostRuntimeInfo info = (HostRuntimeInfo) dp.getVal();
                                System.out.println("RUNTIME POWER = " + info.getPowerState());
                            }
                            else if (dp.getVal() instanceof HostListSummary) {
                                HostListSummary summary = (HostListSummary) dp.getVal();
                                HostListSummaryQuickStats quick = summary.getQuickStats();
                                System.out.println("Quick stats: " + quick);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to retrieve managed objects for " + "HostSystem", e);
        }

    }


    private void connect(String url, String login, String password) throws Exception {
        HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String urlHostName, SSLSession session) {
                log.debug("urlHostName = '" + urlHostName + "'");
                return true;
            }
        };

        ManagedObjectReference serviceInstanceRef = new ManagedObjectReference();

        trustAllHttpsCertificates();
        HttpsURLConnection.setDefaultHostnameVerifier(hv);

        serviceInstanceRef.setType("ServiceInstance");
        serviceInstanceRef.setValue("ServiceInstance");

        vimService = new VimService();
        vimPort = vimService.getVimPort();
        Map<String, Object> ctxt = ((BindingProvider) vimPort).getRequestContext();

        ctxt.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);
        ctxt.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);      // keeps session open
        ctxt.put(BindingProviderProperties.REQUEST_TIMEOUT, 30000); // Timeout in millis
        ctxt.put(BindingProviderProperties.CONNECT_TIMEOUT, 60000); // Timeout in millis

        serviceContent = vimPort.retrieveServiceContent(serviceInstanceRef);
        sessionManager = serviceContent.getSessionManager();

        try {
            vimPort.login(sessionManager, login, password, null);
        } catch (Exception e) {
            log.debug(String.format(
                    "vimPort.login( url=%s, login=%s, pass=%-3.3s****, null ) - couldn't connect.  Exception '%s'\n",
                    url, login, password, e.toString()));
            throw e;
        }

        propertyCollector = serviceContent.getPropertyCollector();
        rootFolder = serviceContent.getRootFolder();
    }

    private static class TrustAllTrustManager implements
            javax.net.ssl.TrustManager,
            javax.net.ssl.X509TrustManager {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public boolean isServerTrusted(
                java.security.cert.X509Certificate[] certs) {
            return true;
        }

        public boolean isClientTrusted(
                java.security.cert.X509Certificate[] certs) {
            return true;
        }

        public void checkServerTrusted(
                java.security.cert.X509Certificate[] certs,
                String authType)
                throws java.security.cert.CertificateException {
            return;
        }

        public void checkClientTrusted(
                java.security.cert.X509Certificate[] certs,
                String authType)
                throws java.security.cert.CertificateException {
            return;
        }
    }

    private static void trustAllHttpsCertificates() throws Exception {
        // Create a trust manager that does not validate certificate chains:
        javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
        javax.net.ssl.TrustManager tm = new TrustAllTrustManager();
        trustAllCerts[0] = tm;
        javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");
        javax.net.ssl.SSLSessionContext sslsc = sc.getServerSessionContext();
        sslsc.setSessionTimeout(0);
        sc.init(null, trustAllCerts, null);
        HttpsURLConnection.setDefaultSSLSocketFactory(
                sc.getSocketFactory());
    }

    public void disconnect() throws ConnectorException {
        try {
            //VMwareConnector.destroyCollectorAndView(vimPort, containerView, propertyCollector);

            vimPort.logout(serviceContent.getSessionManager());
        } catch (Exception e) {
            log.error("Failed to disconnect", e);
        }
    }

    //@Test
    // Average 8 seconds initial, 3.5 seconds 2..5
    public void connectTest() throws Exception {
        System.out.println("Connection Test...");
        for (int ix = 0; ix < 5; ix++) {
            long start = System.currentTimeMillis();
            connect("https://" + ServerConfigurator.VERMONT_VMWARE_SERVER,
                    ServerConfigurator.VERMONT_VMWARE_USERNAME,
                    ServerConfigurator.VERMONT_VMWARE_PASSWORD);
            System.out.println("Connection time ms: " + (System.currentTimeMillis() - start));
            disconnect();
            System.out.println("Connection time + disconnect ms: " + (System.currentTimeMillis() - start));
        }
    }

    @Test
    public void connectionStateTest() throws Exception {
        System.out.println("Connection State Test...");
        connect("https://" + ServerConfigurator.VERMONT_VMWARE_SERVER,
                ServerConfigurator.VERMONT_VMWARE_USERNAME,
                ServerConfigurator.VERMONT_VMWARE_PASSWORD);
        for (int ix = 0; ix < 5; ix++) {
            long start = System.currentTimeMillis();
            String now = getConnectionStateSimple();
            System.out.println("Connection State time + disconnect ms: " + (System.currentTimeMillis() - start) + ", " + now);
        }
        disconnect();
    }

    /***
     * Note: this was the major cause to the VMWare memory leak. As of 2016/08/22,
     *       its been removed from the VMwareConnector,getConnector code base
     * See: GWMON-12669
     * The VMwareConnector.getConnectionState method was not freeing the resource with a vimPort.destroyView
     * Note that getConnectionState averages about 60 ms, and with destroyView 110 ms per call
     * This is a poor way to check a connnection state as it is called with every heartbeat of the monitor thread,
     * which is by default every 5 seconds. According to VMWare, the VMWare management server received over 57000 calls
     * that were not freed over a several day test
     */
    public void getConnectionState() {
        ManagedObjectReference cViewRef = null;
        ManagedObjectReference viewMgrRef = serviceContent.getViewManager();
        List<String> listContainers = new ArrayList<String>();


        try {
            cViewRef = vimPort.createContainerView(
                    viewMgrRef,
                    serviceContent.getRootFolder(),
                    listContainers,
                    true);
        } catch (Exception e) {
            log.error("vimPort.createContainerView(check-connection-state) exception: ", e);
        } finally {
            try {
                if (cViewRef != null) {
                    vimPort.destroyView(cViewRef);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This would be a more appropriate way to test the connection
     * Response times are in the range of 20 - 30ms
     *
     * @return
     * @throws Exception
     */
    public String getConnectionStateSimple() throws Exception {
        ManagedObjectReference ref = new ManagedObjectReference();
        ref.setType("ServiceInstance");
        ref.setValue("ServiceInstance");
        XMLGregorianCalendar calendar = vimPort.currentTime(ref); //this.getServiceInstanceReference());
        return calendar.toString();
    }


    private void retrieveSnapshot(VimPortType portType, ManagedObjectReference vm) throws Exception {


        PropertySpec propertySpec = new PropertySpec();
        propertySpec.setType(vm.getType());
        propertySpec.setAll(Boolean.TRUE); // return all properties
        //propertySpec.getPathSet().add("snapshot");
        PropertyFilterSpec fSpec = new PropertyFilterSpec();
        fSpec.getPropSet().add(propertySpec);
        ObjectSpec oSpec = new ObjectSpec();
        oSpec.setObj(vm);
        oSpec.setSkip(true);
        fSpec.getObjectSet().add(oSpec);
        List<PropertyFilterSpec> fSpecList = new ArrayList<PropertyFilterSpec>();
        fSpecList.add(fSpec);

        RetrieveResult result = vimPort.retrievePropertiesEx(serviceContent.getPropertyCollector(), fSpecList, new RetrieveOptions());

        if (result == null) {
            return;
        }
        List<ObjectContent> oCont = result.getObjects();

        final HashMap<String, Object> retVal = new HashMap<String, Object>();
        if (oCont != null) {
            for (ObjectContent oc : oCont) {
                List<DynamicProperty> dps = oc.getPropSet();
                for (DynamicProperty dp : dps) {
                    retVal.put(dp.getName(), dp.getVal());
                }
            }
        }

        VirtualMachineSnapshotInfo snapInfo = (VirtualMachineSnapshotInfo) retVal.get("snapshot");
        System.out.println("snapshot = " + snapInfo);
    }


    //pSpecVM.getPathSet().add("snapshot");
    private void retrieveVmStandardMetrics(ManagedObjectReference folder) {
        try {
//            JexlEngine jexl = new JexlBuilder().create();
//            JexlExpression e = jexl.createExpression("(vm.hardwareInfo.cpus + vm.hardwareInfo.memoryMB) / 2");
//            JexlContext context = new MapContext();
            SnapshotService snapshots = new SnapshotService();
            Map<String, SnapshotService.SnapshotInfo> snapshotResults = new HashMap();

            ManagedObjectReference viewManager = serviceContent.getViewManager();
            ManagedObjectReference containerView = vimPort.createContainerView(viewManager, folder,
                    Arrays.asList("VirtualMachine"), true);
            propertyCollector = vimPort.createPropertyCollector(serviceContent.getPropertyCollector());

            // Create Property Spec
            PropertySpec propertySpec = new PropertySpec();
            propertySpec.setAll(Boolean.TRUE); // return all properties
            propertySpec.setType("VirtualMachine");
            TraversalSpec ts = new TraversalSpec();
            ts.setName("view");
            ts.setPath("view");
            ts.setSkip(false);
            ts.setType("ContainerView");

            ObjectSpec objectSpec = new ObjectSpec();
            objectSpec.setObj(containerView);
            objectSpec.setSkip(Boolean.TRUE);
            objectSpec.getSelectSet().add(ts);

            PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
            propertyFilterSpec.getPropSet().add(propertySpec);
            propertyFilterSpec.getObjectSet().add(objectSpec);

            List<PropertyFilterSpec> propertyFilterSpecs = new ArrayList<PropertyFilterSpec>();
            propertyFilterSpecs.add(propertyFilterSpec);

            //List<ObjectContent> objectContents = vimPort.retrieveProperties(serviceContent.getPropertyCollector(), propertyFilterSpecs);
            RetrieveResult results = vimPort.retrievePropertiesEx(serviceContent.getPropertyCollector(), propertyFilterSpecs, new RetrieveOptions());
            System.out.println("-- objectCounts.size = " + results.getObjects().size());
            boolean testALL = false;
            if (testALL) {
                RetrieveOptions ro = new RetrieveOptions();
                boolean firstround = true;
                RetrieveResult props = null;
                int i = 0;
                List<ObjectContent> objectContents = new ArrayList<ObjectContent>();

                while (true) {
                    if (firstround) {
                        props = vimPort.retrievePropertiesEx(propertyCollector, propertyFilterSpecs, ro);
                    } else if (props.getToken() != null) {
                        props = vimPort.continueRetrievePropertiesEx(propertyCollector, props.getToken());
                    } else {
                        break;
                    }
                    firstround = false;
                    if (props != null)
                        objectContents.addAll(props.getObjects());
                }
                System.out.println("-- objectCounts.size = " + objectContents.size());
            }


            if (results.getObjects() != null) {
                for (ObjectContent oc : results.getObjects()) {
                    String name = "";
                    ManagedObjectReference mr = oc.getObj();


                    //retrieveSnapshot(vimPort, mr);
                    List<DynamicProperty> dps = oc.getPropSet();
                    for (DynamicProperty dp : dps) {
                        if (dp.getName().equals("name")) {
                            name = (String)dp.getVal();
                        }
                    }
                    //System.out.println("== entityType: " + oc.getObj().getType());
                    if (dps != null) {
                        for (DynamicProperty dp : dps) {
                            if (dp.getName().equals("name")) {
                                name = (String)dp.getVal();
                            }

//                            System.out.println("  -- dp: " + dp.getName() + " : " + dp.getVal());

//                            if (dp.getName().equals("rootSnapshot")) {
//                                ArrayOfManagedObjectReference array = (ArrayOfManagedObjectReference)dp.getVal();
//                                if (array != null) {
//                                    for (ManagedObjectReference mor : array.getManagedObjectReference()) {
//                                        System.out.println("more = " + mor);
//                                    }
//                                }
//                            }
                            if (dp.getName().equals("snapshot")) {
                                VirtualMachineSnapshotInfo vmsi = (VirtualMachineSnapshotInfo) dp.getVal();
                                SnapshotService.SnapshotInfo vmSnapshot = snapshots.calculateSnapshots(name, vmsi);
                                snapshotResults.put(name, vmSnapshot);
                            }
                            if (dp.getVal() instanceof VirtualMachineStorageInfo) {
                                //System.out.println("--dpname " + dp.getName());
                                VirtualMachineStorageInfo storageInfo = (VirtualMachineStorageInfo) dp.getVal();
                                if (storageInfo != null) {
                                    List<VirtualMachineUsageOnDatastore> s = storageInfo.getPerDatastoreUsage();
                                    for (VirtualMachineUsageOnDatastore vmu : s) {
                                        //System.out.println("*** store " + name + ": " + vmu.getCommitted() + ", " + vmu.getUncommitted());
                                    }
                                }
                            }
//                            else if (dp.getVal() instanceof VirtualMachineFileLayout) {
//                                VirtualMachineFileLayout layout = (VirtualMachineFileLayout) dp.getVal();
//                                if (layout != null) {
//                                    System.out.println("*** layout = " + layout.getSnapshot());
//                                }
//                            }
                            if (dp.getVal() instanceof VirtualMachineFileLayoutEx) {
                                Map<Integer, VirtualMachineFileLayoutExFileInfo> map = new HashMap<>();
                                VirtualMachineFileLayoutEx ext = (VirtualMachineFileLayoutEx) dp.getVal();
                                for (VirtualMachineFileLayoutExFileInfo exFile : ext.getFile()) {
                                    if (exFile.getType().equals("snapshotData")) {
                                        map.put(exFile.getKey(), exFile);
                                    }
                                }
                                //System.out.println("timestamp: " + ext.getTimestamp());
                                if (ext != null) {
                                    List<VirtualMachineFileLayoutExSnapshotLayout> snapshotList = ext.getSnapshot();
                                    for (VirtualMachineFileLayoutExSnapshotLayout snapshot : snapshotList) {
                                        VirtualMachineFileLayoutExFileInfo exFile = map.get(snapshot.getDataKey());
                                        if (exFile != null) {
                                            System.out.println("-- snapshot : " + name + ": " + exFile.getSize());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            for (Map.Entry<String, SnapshotService.SnapshotInfo> entry : snapshotResults.entrySet()) {
                System.out.println("-- traversal info: " + entry.getKey() + ": " + entry.getValue().getCount() + ", " + entry.getValue().getOldest());
                if (entry.getValue().getRootCount() > 0) {
                    System.out.println("### ROOT " + entry.getValue().getRootCount());
                }
            }

        } catch (Exception e) {
            log.error("Failed to retrieve managed objects for " + "HostSystem", e);
        }
    }


    @Test
    public void perfManagerTest() throws Exception {
        System.out.println("VM Test...");
        //connect("https://" + "vcsa/sdk", // ServerConfigurator.VERMONT_VMWARE_SERVER,
        connect("https://" + ServerConfigurator.VERMONT_VMWARE_SERVER,
                ServerConfigurator.VERMONT_VMWARE_USERNAME,
                ServerConfigurator.VERMONT_VMWARE_PASSWORD);

        propertyCollector = serviceContent.getPropertyCollector();
        ManagedObjectReference perfManager = serviceContent.getPerfManager();
        this.getMOREFs = new GetMOREF(this.vimPort, this.serviceContent);
        assert perfManager != null;
        Map<String, ManagedObjectReference> vms = getMOREFs.inContainerByType(serviceContent
                .getRootFolder(), "VirtualMachine");
        ManagedObjectReference vmmor = vms.get(0);
        List<PerfCounterInfo> cInfo = getPerfCounters(perfManager);
        List<PerfCounterInfo> vmCpuCounters = new ArrayList<PerfCounterInfo>();
        for (int i = 0; i < cInfo.size(); ++i) {
            if ("cpu".equalsIgnoreCase(cInfo.get(i).getGroupInfo().getKey())) {
                vmCpuCounters.add(cInfo.get(i));
            }
            PerfCounterInfo pci = cInfo.get(i);
            System.out.println("perfCounter: " + pci.getNameInfo().getKey() + " : " + pci.getNameInfo().getLabel() + ", " + pci.getRollupType().value());
            System.out.println("   group: " + pci.getGroupInfo().getKey() + ", " + pci.getStatsType().value());
        }

        disconnect();
    }

    List<PerfCounterInfo> getPerfCounters(ManagedObjectReference perfManager) {
        List<PerfCounterInfo> pciArr = null;

        try {
            // Create Property Spec
            PropertySpec propertySpec = new PropertySpec();
            propertySpec.setAll(Boolean.FALSE);
            propertySpec.getPathSet().add("perfCounter");
            propertySpec.setType("PerformanceManager");
            List<PropertySpec> propertySpecs = new ArrayList<PropertySpec>();
            propertySpecs.add(propertySpec);

            // Now create Object Spec
            ObjectSpec objectSpec = new ObjectSpec();
            objectSpec.setObj(perfManager);
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

            List<PropertyFilterSpec> listpfs =
                    new ArrayList<PropertyFilterSpec>(1);
            listpfs.add(propertyFilterSpec);
            List<ObjectContent> listobjcont =
                    retrievePropertiesAllObjects(listpfs);

            if (listobjcont != null) {
                for (ObjectContent oc : listobjcont) {
                    List<DynamicProperty> dps = oc.getPropSet();
                    if (dps != null) {
                        for (DynamicProperty dp : dps) {
                            List<PerfCounterInfo> pcinfolist =
                                    ((ArrayOfPerfCounterInfo) dp.getVal())
                                            .getPerfCounterInfo();
                            pciArr = pcinfolist;
                        }
                    }
                }
            }
        } catch (SOAPFaultException sfe) {
            sfe.printStackTrace();
            //printSoapFaultException(sfe);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pciArr;
    }

    List<ObjectContent> retrievePropertiesAllObjects(
            List<PropertyFilterSpec> listpfs) {

        RetrieveOptions propObjectRetrieveOpts = new RetrieveOptions();

        List<ObjectContent> listobjcontent = new ArrayList<ObjectContent>();

        try {
            RetrieveResult rslts =
                    vimPort.retrievePropertiesEx(propertyCollector, listpfs,
                            propObjectRetrieveOpts);
            if (rslts != null && rslts.getObjects() != null
                    && !rslts.getObjects().isEmpty()) {
                listobjcontent.addAll(rslts.getObjects());
            }
            String token = null;
            if (rslts != null && rslts.getToken() != null) {
                token = rslts.getToken();
            }
            while (token != null && !token.isEmpty()) {
                rslts =
                        vimPort.continueRetrievePropertiesEx(propertyCollector, token);
                token = null;
                if (rslts != null) {
                    token = rslts.getToken();
                    if (rslts.getObjects() != null && !rslts.getObjects().isEmpty()) {
                        listobjcontent.addAll(rslts.getObjects());
                    }
                }
            }
        } catch (SOAPFaultException sfe) {
            //printSoapFaultException(sfe);
            sfe.printStackTrace();
        } catch (Exception e) {
            System.out.println(" : Failed Getting Contents");
            e.printStackTrace();
        }

        return listobjcontent;
    }

    private void connect2(String url, String login, String password) throws Exception {
        HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String urlHostName, SSLSession session) {
                log.debug("urlHostName = '" + urlHostName + "'");
                return true;
            }
        };

        ManagedObjectReference serviceInstanceRef = new ManagedObjectReference();

        trustAllHttpsCertificates();
        HttpsURLConnection.setDefaultHostnameVerifier(hv);

        serviceInstanceRef.setType("ServiceInstance");
        serviceInstanceRef.setValue("ServiceInstance");

        vimService = new VimService();
        vimPort = vimService.getVimPort();
        Map<String, Object> ctxt = ((BindingProvider) vimPort).getRequestContext();

        ctxt.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);
        ctxt.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);      // keeps session open

        serviceContent = vimPort.retrieveServiceContent(serviceInstanceRef);
        sessionManager = serviceContent.getSessionManager();

        try {
            vimPort.login(sessionManager, login, password, null);
        } catch (Exception e) {
            log.debug(String.format(
                    "vimPort.login( url=%s, login=%s, pass=%-3.3s****, null ) - couldn't connect.  Exception '%s'\n",
                    url, login, password, e.toString()));
            throw e;
        }

        propertyCollector = serviceContent.getPropertyCollector();
        rootFolder = serviceContent.getRootFolder();
    }

}
