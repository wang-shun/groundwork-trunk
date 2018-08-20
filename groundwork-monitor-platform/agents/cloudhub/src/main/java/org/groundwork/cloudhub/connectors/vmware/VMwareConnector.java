package org.groundwork.cloudhub.connectors.vmware;

import com.sun.xml.ws.client.BindingProviderProperties;
import com.vmware.vim25.ArrayOfGuestNicInfo;
import com.vmware.vim25.ArrayOfManagedObjectReference;
import com.vmware.vim25.DynamicProperty;
import com.vmware.vim25.GuestNicInfo;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ObjectContent;
import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertySpec;
import com.vmware.vim25.RetrieveOptions;
import com.vmware.vim25.RetrieveResult;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.TraversalSpec;
import com.vmware.vim25.VimPortType;
import com.vmware.vim25.VimService;
import com.vmware.vim25.VirtualMachineSnapshotInfo;
import com.vmware.vim25.VirtualMachineStorageInfo;
import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.ConnectionState;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.configuration.ConfigurationProvider;
import org.groundwork.cloudhub.configuration.MonitorConnection;
import org.groundwork.cloudhub.configuration.VmwareConnection;
import org.groundwork.cloudhub.connectors.ConnectorConstants;
import org.groundwork.cloudhub.connectors.ConnectorFactory;
import org.groundwork.cloudhub.connectors.ManagementConnector;
import org.groundwork.cloudhub.connectors.MonitoringConnector;
import org.groundwork.cloudhub.connectors.base.BaseConnector;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.InventoryBrowser;
import org.groundwork.cloudhub.inventory.InventoryOptions;
import org.groundwork.cloudhub.inventory.InventoryType;
import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.BaseMetric;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.groundwork.cloudhub.metrics.BaseSynthetic;
import org.groundwork.cloudhub.metrics.BaseVM;
import org.groundwork.cloudhub.metrics.MonitoringState;
import org.groundwork.cloudhub.metrics.SourceType;
import org.groundwork.cloudhub.utils.Conversion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.BindingProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * VMware Connector
 * <p/>
 * Notes on recovering from failed connection from original developer
 * :
 * REMINDER: when many login/authentication errors
 * accumulate, upon actually correcting the password and/or login
 * name, communications with the vSphere/ESXi server will be blocked
 * by the vSphere/ESXi server for approximately 10-15 minutes.
 * This appears to be an undocumented security-enhancement feature
 * to prevent automated password-search by denial-of-service
 * mechanisms.  After the 10-15 minute period, login should
 * happen normally.
 * <p/>
 * KEY TEST: try attaching to [ https:{URL of server}/mob ]
 * you should be prompted for a login name and a password.  IF the
 * hacking-delay has been exceeded (in other words, if the minimum
 * amount of time has elapsed), then your replacement name and
 * password should work.  Otherwise, you'll just get more of these
 * error status messages.]
 */
@Service(VMwareConnector.NAME)
@Scope("prototype")
public class VMwareConnector extends BaseConnector implements MonitoringConnector, ManagementConnector {

    public static final String NAME = "VmWareConnector";

    protected String vmwareURL;      // 'url' afore; think of as "PM"
    protected String vmwareLogin;    // only be ONE connection-object
    protected String vmwarePassword; // per application.

    protected static final String SVC_INST_NAME = "ServiceInstance";

    protected VimService vimService;
    protected VimPortType vimPort;
    protected ServiceContent serviceContent;

    protected ManagedObjectReference sessionManager;
    protected ManagedObjectReference rootFolder;

    protected ConnectionState connectionState = ConnectionState.NASCENT;

    protected static Logger log = Logger.getLogger(VMwareConnector.class);

    @Resource(name = ConnectorFactory.NAME)
    protected ConnectorFactory connectorFactory;

    @Autowired
    protected SnapshotService snapshotService;


    public VMwareConnector()    // constructor
    {
        vmwarePassword = "";              // because an empty password is acceptable
    }

    @Override
    public void connect(MonitorConnection monitorConnection) throws ConnectorException {
        VmwareConnection connection = (VmwareConnection) monitorConnection;
        connect(connection.getUrl(), connection.getUsername(), connection.getPassword(), connection.getServer(), false);
    }

    @Override
    public void testConnection(MonitorConnection monitorConnection) throws ConnectorException {
        VmwareConnection connection = (VmwareConnection) monitorConnection;
        connect(connection.getUrl(), connection.getUsername(), connection.getPassword(), connection.getServer(), true);
    }

    @Override
    public void disconnect() throws ConnectorException {
        if (connectionState == ConnectionState.CONNECTED) {
            try {
                connectionState = ConnectionState.DISCONNECTED;
                vimPort.logout(serviceContent.getSessionManager());
            } catch (Exception e) {
                log.error("Failed to disconnect", e);
            }
        }
    }

    @Override
    public ConnectionState getConnectionState() {
        // may want to consider calling VMwareConnector.getServerTime here
        // However, since getConnectionState is called frequently (every 5 seconds) by the connector monitor thread
        // recommending not to call. Bad connections will be caught during monitoring activities and should be handled
        return connectionState;
    }

    /***
     * Return the current time on the Vmware server
     * Useful for syncing CloudHub with Vmware
     * Also useful for a heartbeat test, since the overhead is approximately 20ms
     *
     * @return full gregorian time stamp of the server
     * @throws Exception
     */
    public String getServerTime() throws Exception {
        ManagedObjectReference ref = new ManagedObjectReference();
        ref.setType(SVC_INST_NAME);
        ref.setValue(SVC_INST_NAME);
        XMLGregorianCalendar calendar = vimPort.currentTime(ref); //this.getServiceInstanceReference());
        return calendar.toString();
    }


    @Override
    public void openConnection(MonitorConnection monitorConnection) throws ConnectorException {
        if (connectionState != ConnectionState.CONNECTED) {
            connect(monitorConnection);
        }
    }

    @Override
    public void closeConnection() throws ConnectorException {
        if (connectionState == ConnectionState.CONNECTED) {
            disconnect();
        }
    }

    private void connect(String url, String login, String pass, String vm, boolean isTest)
            throws ConnectorException {

        if (isTest) {
            try {
                TestContext test = attachTest(url, login, pass);
                test.getVimPortType().logout(test.getServiceContent().getSessionManager());
            } catch (Exception e) {
                throw new ConnectorException(e);
            }
            return;
        }

        vmwareURL = url;
        vmwareLogin = login;
        vmwarePassword = pass;

        try {
            connectionState = ConnectionState.CONNECTING;
            attach();   // will throw exception if doesn't work.
            connectionState = ConnectionState.CONNECTED;
        } catch (Exception e) {
            connectionState = ConnectionState.FAILED;
            throw new ConnectorException(e.getMessage(), e);
        }
    }

    @Override
    public DataCenterInventory gatherInventory() {
        InventoryBrowser inventoryBrowser = new VMwareInventoryBrowser(vimPort, serviceContent);
        InventoryOptions options = new InventoryOptions(collectionMode.isDoHosts(), collectionMode.isDoStorageDomains(),
                collectionMode.isDoNetworks(), collectionMode.isDoResourcePools(),
                collectionMode.isDoTaggedGroups(), collectionMode.getGroupTag());
        DataCenterInventory inventory = inventoryBrowser.gatherInventory(options);
        return inventory;
    }

    @Override
    public MonitoringState collectMetrics(MonitoringState priorResults,
                                          List<BaseQuery> hostQueries, List<BaseQuery> vmQueries, List<BaseQuery> customQueries)
            throws ConnectorException {

        boolean deleteDefunctMembers = true;
        boolean onlyRequestedMetrics = true;
        boolean crushMetricsIfDown = true;

        ConcurrentHashMap<String, BaseHost> hostPool = new ConcurrentHashMap<String, BaseHost>();
        ConcurrentHashMap<String, VMwareVM> vmPool = new ConcurrentHashMap<String, VMwareVM>();
        ConcurrentHashMap<String, BaseQuery> hostQueryPool = new ConcurrentHashMap<String, BaseQuery>();
        ConcurrentHashMap<String, BaseQuery> vmQueryPool = new ConcurrentHashMap<String, BaseQuery>();

        // debug with timer
        long startTime = System.currentTimeMillis();
        StringBuffer timeStamp = new StringBuffer(100);
        if (log.isDebugEnabled()) {
            timeStamp.append("VMware obj("
                    + Double.toString((System.currentTimeMillis() - startTime) / 1000.0) + ") ");
        }

        // CLOUDHUB-296: custom names
        Map<String, String> customHostNames = new ConcurrentHashMap<>();
        Map<String, String> customVmNames = new ConcurrentHashMap<>();
        for (BaseQuery query : hostQueries) {
            customHostNames.put(query.getQuery(), (query.getCustomName() == null) ? "" : query.getCustomName());
        }
        for (BaseQuery query : vmQueries) {
            customVmNames.put(query.getQuery(), (query.getCustomName() == null) ? "" : query.getCustomName());
        }

        ConfigurationProvider provider = connectorFactory.getConfigurationProvider(VirtualSystem.VMWARE);
        if (priorResults == null)    // safety check BEFORE any return()
            priorResults = new MonitoringState();

        if (connectionState != ConnectionState.CONNECTED) {
            log.error("collectMetrics(): not connected");
            return priorResults;
        }
        ManagedObjectReference viewMgrRef = serviceContent.getViewManager();

        List<String> listContainers = new ArrayList<String>();
        listContainers.add("VirtualMachine");
        listContainers.add("HostSystem");

        // resources requiring destroy
        ManagedObjectReference containerView = null;
        ManagedObjectReference propertyCollector = null;

        try {
            containerView = vimPort.createContainerView(
                    viewMgrRef,
                    serviceContent.getRootFolder(),
                    listContainers,
                    true);

            TraversalSpec tSpec = new TraversalSpec();
            tSpec.setName("traverseEntities");
            tSpec.setType("ContainerView");
            tSpec.setPath("view");
            tSpec.setSkip(false);

            TraversalSpec tSpecVMN = new TraversalSpec();
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
            ObjectSpec oSpec = new ObjectSpec();
            oSpec.setObj(containerView);
            oSpec.setSkip(true);
            oSpec.getSelectSet().add(tSpec);
           oSpec.getSelectSet().add(tSpecVMN);
           oSpec.getSelectSet().add(tSpecVMRP);

            PropertySpec pSpecVM = new PropertySpec();
            pSpecVM.setType("VirtualMachine");

            VMwareVM dummyVM = new VMwareVM("dummy");

            for (BaseQuery accessor : dummyVM.getDefaultMetricList()) {
                vmQueryPool.put(accessor.getQuery(), accessor);
                if (!accessor.getQuery().startsWith(ConnectorConstants.SYNTHETIC_PREFIX)
                        && !accessor.getQuery().startsWith("perfcounter")
                        && !accessor.getQuery().endsWith(".scaled"))
                    pSpecVM.getPathSet().add(accessor.getQuery());
            }

            for (BaseQuery accessor : dummyVM.getDefaultConfigList()) {
                vmQueryPool.put(accessor.getQuery(), accessor);
                if (!accessor.getQuery().startsWith(ConnectorConstants.SYNTHETIC_PREFIX)
                        && !accessor.getQuery().startsWith("perfcounter")
                        && !accessor.getQuery().endsWith(".scaled"))
                    pSpecVM.getPathSet().add(accessor.getQuery());
            }

            for (BaseQuery accessor : dummyVM.getDefaultSyntheticList()) {
                vmQueryPool.put(accessor.getQuery(), accessor);
                if (!accessor.getQuery().startsWith(ConnectorConstants.SYNTHETIC_PREFIX)
                        && !accessor.getQuery().startsWith("perfcounter")
                        && !accessor.getQuery().endsWith(".scaled"))
                    pSpecVM.getPathSet().add(accessor.getQuery());
            }

            // this is LAST in order to OVERRIDE the above defaults
            for (BaseQuery accessor : vmQueries) {
                if (accessor.getSourceType() == SourceType.diagnostics || accessor.getSourceType() == SourceType.compute) {
                    vmQueryPool.put(accessor.getQuery(), accessor);
                    if (!accessor.getQuery().startsWith(ConnectorConstants.SYNTHETIC_PREFIX)
                        && !accessor.getQuery().startsWith(SnapshotService.SNAPSHOTS_PREFIX)
                        && !accessor.getQuery().startsWith("perfcounter")
                        && !accessor.getQuery().endsWith(".scaled")) {
                        pSpecVM.getPathSet().add(accessor.getQuery());
                    }
                }
            }

            // CLOUDHUB-333: add support for snapshots
            snapshotService.configurePropertySpec(vmQueryPool, pSpecVM);

            PropertySpec pSpecHost = new PropertySpec();
            pSpecHost.setType("HostSystem");

            VMwareHost dummyHost = new VMwareHost("dummy");

            for (BaseQuery accessor : dummyHost.getDefaultMetricList()) {
                hostQueryPool.put(accessor.getQuery(), accessor);
                if (!accessor.getQuery().startsWith(ConnectorConstants.SYNTHETIC_PREFIX)
                        && !accessor.getQuery().startsWith("perfcounter")
                        && !accessor.getQuery().endsWith(".scaled"))
                    pSpecHost.getPathSet().add(accessor.getQuery());
            }

            for (BaseQuery accessor : dummyHost.getDefaultConfigList()) {
                hostQueryPool.put(accessor.getQuery(), accessor);
                if (!accessor.getQuery().startsWith(ConnectorConstants.SYNTHETIC_PREFIX)
                        && !accessor.getQuery().startsWith("perfcounter")
                        && !accessor.getQuery().endsWith(".scaled"))
                    pSpecHost.getPathSet().add(accessor.getQuery());
            }

            for (BaseQuery accessor : dummyHost.getDefaultSyntheticList()) {
                hostQueryPool.put(accessor.getQuery(), accessor);
                if (!accessor.getQuery().startsWith(ConnectorConstants.SYNTHETIC_PREFIX)
                        && !accessor.getQuery().startsWith("perfcounter")
                        && !accessor.getQuery().endsWith(".scaled"))
                    pSpecHost.getPathSet().add(accessor.getQuery());
            }

            // This is LAST in the sequence to ensure OVERRIDE capability.
            for (BaseQuery accessor : hostQueries) {
                if (accessor.getSourceType() == SourceType.diagnostics || accessor.getSourceType() == SourceType.compute) {
                    hostQueryPool.put(accessor.getQuery(), accessor);
                    if (!accessor.getQuery().startsWith(ConnectorConstants.SYNTHETIC_PREFIX)
                        && !accessor.getQuery().startsWith("perfcounter")
                        && !accessor.getQuery().endsWith(".scaled")) {
                        pSpecHost.getPathSet().add(accessor.getQuery());
                    }
                }
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
            propertyCollector = vimPort.createPropertyCollector(serviceContent.getPropertyCollector());

            try {
                boolean firstround = true;
                RetrieveResult props = null;
                int i = 0;

                while (true) {
                    if (firstround) {
                        props = vimPort.retrievePropertiesEx(propertyCollector, fSpecList, ro);
                    }
                    else if (props.getToken() != null) {
                        props = vimPort.continueRetrievePropertiesEx(propertyCollector, props.getToken());
                    }
                    else {
                        break;
                    }
                    firstround = false;
                    if (props != null)
                        ocList.addAll(props.getObjects());
                }
            } catch (InvalidPropertyFaultMsg e) {
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
                                + ConnectorConstants.CONFIG_FILE_PATH
                                + ConnectorConstants.CONFIG_FILE_EXTN
                                + "' file\n"
                                + "-----------------------------------------------------------------------------" + "\n"
                );
            } catch (Exception e) {
                String message = "vimPort.retrievePropertiesEx(.2.) error";
                log.error(message + e.toString());
                log.error("localized msg: '" + e.getLocalizedMessage() + "', msg: '" + e.getMessage() + "', cause='" + e.getCause() + "'");
                throw new ConnectorException(e);
            }

            if (log.isDebugEnabled()) {
                timeStamp.append("vmcall("
                        + Double.toString((System.currentTimeMillis() - startTime) / 1000.0) + ") ");
            }

            // ----------------------------------------------
            // Let's look for VM objects first.
            // (because they link INTO the host objects)
            // ----------------------------------------------
            for (ObjectContent oc : ocList) {
                String name = null;
                String path = null;
                Boolean postcast = true;
                String ocType = oc.getObj().getType();
                String ocValue = oc.getObj().getValue();

                if (!ocType.equals("VirtualMachine"))
                    // skip everything except VirtualMachine, and get the next one
                    continue;

                //log.debug( "0a: " + ocValue + " = " + ocType );

                List<DynamicProperty> dpList = oc.getPropSet();
                if (dpList == null)
                    continue;

                // -----------------------------------------------
                // PASS1: make receiver objects, on finding "name"
                // -----------------------------------------------
                VMwareVM vmObj = null;    // VM object

                for (DynamicProperty dp : dpList) {
                    path = dp.getName();
                    name = dp.getVal().toString();

                    if (path.equals("name")) {
                        vmPool.put(ocValue, vmObj = new VMwareVM(name));
                        break;  // end of search-through-for-NAME
                    }
                }

                if (vmObj == null) {
                    log.error("ERROR: virtual machine object == NULL");
                    continue;  // if its NULL, its bad, so for now skip it.
                }

                // -----------------------------------------------
                // PASS2: fill up the object
                // -----------------------------------------------
                for (DynamicProperty dp : dpList) {
                    path = dp.getName();
                    name = dp.getVal().toString();
                    postcast = true;

                    if (path.equals("guest.net")) {
                        if (dp.getVal() instanceof ArrayOfGuestNicInfo) {
                            List<GuestNicInfo> gniList =
                                    ((ArrayOfGuestNicInfo) dp.getVal()).getGuestNicInfo();

                            for (GuestNicInfo gni : gniList) {
                                //log.debug( "3: " + path + " = " + gni.getMacAddress());
                                if (vmObj != null) {
                                    BaseQuery vbq = vmQueryPool.get(path);
                                    BaseMetric vbm = new BaseMetric(
                                            path,
                                            vbq.getWarning(),
                                            vbq.getCritical(),
                                            vbq.isGraphed(),
                                            vbq.isMonitored(),
                                            customVmNames.get(path)
                                    );
                                    if (vbq.isTraced())
                                        vbm.setTrace();
                                    vbm.setValue(gni.getMacAddress());
                                    vmObj.setMacAddress(gni.getMacAddress());
                                    vmObj.putConfig(path, vbm);

                                    postcast = false; // block end code
                                }
                                break;  // only do FIRST item, explicitly!
                            }
                        }
                    } else if (path.equals("summary.runtime.host")) {
                        if (dp.getVal() instanceof ManagedObjectReference) {
                            ManagedObjectReference mor = (ManagedObjectReference) dp.getVal();
                            //log.debug( "4: " + path + " = " + mor.getValue() + " / " + mor.getType());

                            vmObj.setHypervisor(mor.getValue());
                            name = mor.getValue();
                        }
                    } else if (path.equals("guest.ipAddress")) {
                        vmObj.setIpAddress(name);
                    } else if (path.equals("guest.guestState")) {
                        vmObj.setGuestState(name);
                    } else if (path.equals("summary.runtime.bootTime")) {
                        vmObj.setBootDate(name, ConnectorConstants.VMWARE_DATE_FORMAT, ConnectorConstants.VMWARE_DATE_FORMAT2);
                    } else if (path.equals("summary.quickStats.uptimeSeconds")
                            || path.equals("summary.quickStats.uptime")
                            ) {
                        vmObj.setLastUpdate(name);
                    } else if (path.equals(SnapshotService.DP_TYPE_SNAPSHOT)) {
                        // CLOUDHUB-333: gather snapshot information
                        VirtualMachineSnapshotInfo vmsi = (VirtualMachineSnapshotInfo) dp.getVal();
                        SnapshotService.SnapshotInfo vmSnapshot = snapshotService.calculateSnapshots(vmObj.getVMName(), vmsi);
                        snapshotService.updateSnapshotMetrics(vmQueryPool, customVmNames, vmSnapshot, vmObj);
                        postcast = false;
                    } else if (path.equals(SnapshotService.DP_TYPE_SNAPSHOT_STORAGE)) {
                        // CLOUDHUB-333: gather snapshot storage info
                        VirtualMachineStorageInfo storageInfo = (VirtualMachineStorageInfo) dp.getVal();
                        SnapshotService.SnapshotStorageInfo vmSnapshot = snapshotService.calculateSnapshotStorage(vmObj.getVMName(), storageInfo);
                        snapshotService.updateSnapshotStorageMetrics(vmQueryPool, customVmNames, vmSnapshot, vmObj);
                        postcast = false;
                    } else {
                        // empty: ON PURPOSE.  Keep it that way (or use it)
                        //        but don't remove it!
                    }

                    if (postcast) {
                        BaseQuery vbq = vmQueryPool.get(path);
                        BaseMetric vbm = new BaseMetric(
                                path,
                                vbq.getWarning(),
                                vbq.getCritical(),
                                vbq.isGraphed(),
                                vbq.isMonitored(),
                                customVmNames.get(path)
                        );
                        if (vbq.isTraced())
                            vbm.setTrace();
                        vbm.setValue(name);

                        if (path.startsWith("summary.quickStats")
                                || path.startsWith("summary.runtime")
                                || path.startsWith("summary.storage")
                            //|| path.startsWith("perfcounter")
                                ) {
                            vmObj.putMetric(path, vbm);
                        } else {
                            vmObj.putConfig(path, vbm);
                        }
                    }
                }

                // now ALL metrics have been collected an assigned.
                // this is the per-vm place where supplimental
                // statistics are computed.  Scaled.
                for (String query : vmQueryPool.keySet())  // SCALED value adjustments here
                {
                    if (!query.endsWith(".scaled"))
                        continue;  // move along

                    BaseQuery vbq = vmQueryPool.get(query);
                    BaseMetric vbm = new BaseMetric(
                            query,
                            vbq.getWarning(),
                            vbq.getCritical(),
                            vbq.isGraphed(),
                            vbq.isMonitored(),
                            customVmNames.get(query)
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

                    vbm.setValue(result);

                    vmObj.putMetric(query, vbm);
                }

                for (String query : vmQueryPool.keySet()) {
                    if (!query.startsWith("syn.vm."))
                        continue;  // not one of ours.

                    BaseSynthetic vbs;
                    BaseQuery vbq = vmQueryPool.get(query);
                    BaseMetric vbm = new BaseMetric(
                            query,
                            vbq.getWarning(),
                            vbq.getCritical(),
                            vbq.isGraphed(),
                            vbq.isMonitored(),
                            customVmNames.get(query)
                    );

                    String result = "uncomputed";
                    if ((vbs = dummyVM.getSynthetic(query)) != null) {
                        String value1 = vmObj.getValueByKey(vbs.getLookup1());
                        String value2 = vmObj.getValueByKey(vbs.getLookup2());

                        result = String.valueOf(vbs.compute(value1, value2)) + "%";
                    }
                    vbm.setValue(result);

                    if (vbq.isTraced())
                        vbm.setTrace();

                    vmObj.putMetric(query, vbm);
                }

                // Do NOT move this above the foregoing vmObj filler-code.  The
                // MonitorState can only be computed once all the above is done.
                vmObj.setRunState(vmObj.getMonitorState());
            }
            //log.debug("debug 5");

            // ----------------------------------------------
            // Now, the HOST objects, since VM objects done
            // ----------------------------------------------
            for (ObjectContent oc : ocList) {
                String name = null;
                String path = null;
                Boolean postcast = true;

                String ocType = oc.getObj().getType();
                String ocValue = oc.getObj().getValue();

                if (!ocType.equals("HostSystem"))
                    // if NOT HostSystem, just skip
                    continue;

                //log.debug( "0b: " + ocValue + " = " + ocType );

                List<DynamicProperty> dpList = oc.getPropSet();
                if (dpList == null)
                    continue;

                // -----------------------------------------------
                // PASS1: make receiver objects, on finding "name"
                // -----------------------------------------------
                VMwareHost hostObj = null;    // VM object

                for (DynamicProperty dp : dpList) {
                    path = dp.getName();
                    name = dp.getVal().toString();

                    if (path.equals("name")) {
                        hostPool.put(ocValue, hostObj = new VMwareHost(name));
                        hostObj.setSystemName(ocValue);
                        break;  // end of search-through-for-NAME
                    }
                }

                if (hostObj == null) {
                    log.error("ERROR: host object == NULL");
                    continue;  // break out, object must have name
                }

                // -----------------------------------------------
                // PASS2: fill up the object
                // -----------------------------------------------
                Double cpuMhz = null;
                Double numCpuCores = null;

                for (DynamicProperty dp : dpList) {
                    path = dp.getName();
                    name = dp.getVal().toString();
                    postcast = true;

                    if (path.equals("vm")) {
                        if (dp.getVal() instanceof ArrayOfManagedObjectReference) {
                            List<ManagedObjectReference> morList =
                                    ((ArrayOfManagedObjectReference) dp.getVal()).getManagedObjectReference();

                            for (ManagedObjectReference vmmor : morList) {
                                VMwareVM v = vmPool.get(vmmor.getValue());
                                hostObj.putVM(vmmor.getValue(), v);
                        /*    log.debug(
                                    "2b: " + path +
                                    " = " + vmmor.getValue() +
                                    " / " +
                                    ( v == null ? "null" : v.getVMName()) );*/
                                postcast = false;
                            }
                        }
                    } else if (path.equals("hardware.network.ipAddress")) // not supported
                    {
                        hostObj.setIpAddress(name);
                    } else if (path.equals("hardware.network.macAddress")) // not supported
                    {
                        hostObj.setMacAddress(name);
                    } else if (path.equals("summary.hardware.cpuMhz")) {
                        cpuMhz = Double.parseDouble(name);
                    } else if (path.equals("summary.hardware.numCpuCores")) {
                        numCpuCores = Double.parseDouble(name);
                    } else if (path.equals("summary.runtime.bootTime")) {
                        //hostObj.setBootDate(name, ConnectorConstants.VMWARE_DATE_FORMAT, ConnectorConstants.VMWARE_DATE_FORMAT2);
                        XMLGregorianCalendar xmlCalendar = (XMLGregorianCalendar)dp.getVal();
                        hostObj.setBootDate(xmlCalendar.toGregorianCalendar());
                    } else if (path.equals("summary.quickStats.uptimeSeconds")
                            || path.equals("summary.quickStats.uptime")
                            ) {
                        hostObj.setLastUpdate(name);
                    } else if (path.equals("summary.hardware.model")) {
                        hostObj.setDescription(name);
                    } else {
                        // empty: ON PURPOSE.  Keep it that way (or use it)
                        //        but don't remove it!
                    }

                    if (postcast) {
                        BaseQuery vbq = hostQueryPool.get(path);
                        BaseMetric vbm = new BaseMetric(
                                path,
                                vbq.getWarning(),
                                vbq.getCritical(),
                                vbq.isGraphed(),
                                vbq.isMonitored(),
                                customHostNames.get(path)
                        );
                        if (vbq.isTraced())
                            vbm.setTrace();
                        vbm.setValue(name);

                        if (path.startsWith("summary.quickStats")
                                || path.startsWith("summary.runtime")
                                || path.startsWith("summary.storage")
                            //|| path.startsWith("perfcounter")
                                ) {
                            hostObj.putMetric(path, vbm);
                        } else {
                            hostObj.putConfig(path, vbm);
                        }
                    }
                }

                // now ALL metrics have been collected and assigned.
                // this is the per-hypervisor place where supplimental
                // statistics are computed.  Scaled.
                for (String query : hostQueryPool.keySet())  // SCALED value adjustments here
                {
                    if (!query.endsWith(".scaled"))
                        continue;  // move along

                    BaseQuery vbq = hostQueryPool.get(query);
                    BaseMetric vbm = new BaseMetric(
                            query,
                            vbq.getWarning(),
                            vbq.getCritical(),
                            vbq.isGraphed(),
                            vbq.isMonitored(),
                            customHostNames.get(query)
                    );

                    String result = "uncomputed";
                    if (query.equals("summary.hardware.cpuMhz.scaled")) {
                        double scaled = 1.0;
                        scaled *= (cpuMhz == null) ? 1.0 : cpuMhz;
                        scaled *= (numCpuCores == null) ? 1.0 : numCpuCores;

                        result = Double.toString(scaled);

                        if (numCpuCores != null && numCpuCores > 1.01) { // .01 added for floating point compare
                            if (log.isDebugEnabled())
                                log.debug(String.format("Scaled %.1f MHz (x %.0f) = %.1f", cpuMhz, numCpuCores, scaled));
                        }
                    }
                    vbm.setValue(result);

                    if (vbq.isTraced())
                        vbm.setTrace();

                    hostObj.putMetric(query, vbm);
                }
                // it is important to do "synthetics" after "scaled" objects,
                // since one of the main uses of scaled values is the computation
                // of more accurate relative values.

                for (String query : hostQueryPool.keySet()) {
                    if (!query.startsWith("syn.host."))
                        continue;  // move along, not one of ours.

                    BaseSynthetic vbs;
                    BaseQuery vbq = hostQueryPool.get(query);
                    BaseMetric vbm = new BaseMetric(
                            query,
                            vbq.getWarning(),
                            vbq.getCritical(),
                            vbq.isGraphed(),
                            vbq.isMonitored(),
                            customHostNames.get(query)
                    );
                    String result = "uncomputed";
                    if ((vbs = dummyHost.getSynthetic(query)) != null) {
                        String value1 = hostObj.getValueByKey(vbs.getLookup1());
                        String value2 = hostObj.getValueByKey(vbs.getLookup2());

                        result = String.valueOf(vbs.compute(value1, value2)) + "%";
                    }
                    vbm.setValue(result);

                    if (vbq.isTraced())
                        vbm.setTrace();

                    hostObj.putMetric(query, vbm);
                }

                // Do NOT move this above the foregoing hostObj filler-code.  The
                // MonitorState can only be computed once all the above is done.
                hostObj.setRunState(hostObj.getMonitorState());
            }

            if (log.isDebugEnabled()) {
                timeStamp.append("premerge("
                        + Double.toString((System.currentTimeMillis() - startTime) / 1000.0) + ") ");
            }

            // while not specifically warned against, the use of a separate
            // list here ensures that the keys can be CHANGED, and that the
            // loop will terminate.  (Otherwise, infinite loop a possibility)

            List<String> hostkeylist =
                    new ArrayList<String>(hostPool.keySet());

            for (String hostkey : hostkeylist) {
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

                for (String vmkey : vmkeylist) {
                    hostPool.get(hostkey).getVM(vmkey).setHypervisor(hostname);
                    BaseVM vmo = hostPool.get(hostkey).getVM(vmkey);
                    String vmname = vmo.getVMName();
                    // -----------------------------------------------------------
                    // DEFINITELY - don't want to do the following (although
                    // it would be real nice!) as it causes concurrancy violations
                    // in the runtime package.
                    // -----------------------------------------------------------
                    hostPool.get(hostkey).renameVM(vmkey, vmname);
                }
                BaseHost hosto = hostPool.get(hostkey);
                // -----------------------------------------------------------
                // Same issue with concurrency.
                hostPool.remove(hostkey);
                hostPool.put(hostname, hosto);
            }

            mergeMonitoringResults(priorResults.hosts(), hostPool, deleteDefunctMembers);
            hostPool = null;   // explicitly kill off objects!

        } catch (Exception e) {
            throw new ConnectorException("Failed to retrieve metrics, error: " + e.getMessage(), e);
        } finally {
            VMwareConnector.destroyCollectorAndView(vimPort, containerView, propertyCollector);
        }

        if (collectionMode.isDoStorageDomains()) {
            Map<String, VmWareStorage> storageMap = gatherStorageMetrics(hostQueries);
            Map<String, VmWareStorage> storageVms = new ConcurrentHashMap<String, VmWareStorage>();
            for (String storageName : storageMap.keySet()) {
                VmWareStorage storage = storageMap.get(storageName);
                String prefixedName = provider.getPrefix(ConfigurationProvider.PrefixType.VmStorage) + storageName;
                storage.setHostName(prefixedName);
                storageVms.put(prefixedName, storage);
            }
            mergeMonitoringResults(priorResults.hosts(), storageVms, deleteDefunctMembers);
            crushMergeMetrics(priorResults, storageVms);
        }
        if (collectionMode.isDoNetworks()) {
            Map<String, VmWareNetwork> networkMap = gatherNetworkMetrics(hostQueries);
            Map<String, VmWareNetwork> networkVms = new ConcurrentHashMap<String, VmWareNetwork>();
            for (String networkName : networkMap.keySet()) {
                VmWareNetwork network = networkMap.get(networkName);
                String prefixedName = provider.getPrefix(ConfigurationProvider.PrefixType.VmNetwork) + networkName;
                network.setHostName(prefixedName);
                networkVms.put(prefixedName, network);
            }
            mergeMonitoringResults(priorResults.hosts(), networkVms, deleteDefunctMembers);
            crushMergeMetrics(priorResults, networkVms);
        }

        if (onlyRequestedMetrics || crushMetricsIfDown)  // a STRIP OUT operation!
        {
            crushDownMetrics(priorResults.hosts());
        }
        if (log.isDebugEnabled()) {
            timeStamp.append("end("
                + Double.toString((System.currentTimeMillis() - startTime) / 1000.0) + ") ");
            log.debug("collectMetrics(" + this.vmwareURL + ") timestamps: [" + timeStamp.toString() + "]");
        }

        return priorResults;   // which must never be null by this point in code.
    }

    private void crushMergeMetrics(MonitoringState priorResults, Map<String, ? extends VMwareHost> newObjects) {
        List<String> hostDeletes = new ArrayList<String>();
        for (Map.Entry<String, BaseHost> entry : priorResults.hosts().entrySet()) {
            BaseHost host = entry.getValue();
            VMwareHost newHost = newObjects.get(entry.getKey());
            if (newHost instanceof VmWareStorage || newHost instanceof VmWareNetwork) {
                for (BaseMetric metric : host.getMetricPool().values()) {
                    if (null == newHost.getMetricPool().get(metric.getQuerySpec())) {
                        hostDeletes.add(metric.getQuerySpec());
                    }
                }
                for (String query : hostDeletes) {
                    host.getMetricPool().remove(query);
                }
            }
        }

    }

    private Map<String, VmWareStorage> gatherStorageMetrics(List<BaseQuery> hostQueries) throws ConnectorException {
        InventoryType inventoryType = InventoryType.Datastore;
        Map<String, VmWareStorage> storageMap = new ConcurrentHashMap<String, VmWareStorage>();
        Map<String, BaseQuery> vmQueryMap = new HashMap<>();
        for (BaseQuery q : hostQueries) {
            vmQueryMap.put(q.getQuery(), q);
        }
        // resources requiring destroy
        ManagedObjectReference containerView = null;
        ManagedObjectReference propertyCollector = null;
        try {
            ConcurrentHashMap<String, BaseQuery> queryPool = new ConcurrentHashMap<String, BaseQuery>();
            ManagedObjectReference viewManager = serviceContent.getViewManager();
            containerView = vimPort.createContainerView(viewManager, rootFolder, Arrays.asList(inventoryType.name()), true);

            // Create Property Spec
            PropertySpec propertySpec = new PropertySpec();
            propertySpec.setAll(Boolean.FALSE);
            propertySpec.setType(inventoryType.name());
            VmWareStorage temp = new VmWareStorage("temp");
            for (BaseQuery accessor : temp.getDefaultMetricList()) {
                queryPool.put(accessor.getQuery(), accessor);
                if (!accessor.getQuery().startsWith(ConnectorConstants.SYNTHETIC_PREFIX)
                        && !accessor.getQuery().startsWith("perfcounter")
                        && !accessor.getQuery().endsWith(".scaled"))
                    propertySpec.getPathSet().add(accessor.getQuery());
            }
            for (BaseQuery accessor : temp.getDefaultConfigList()) {
                queryPool.put(accessor.getQuery(), accessor);
                if (!accessor.getQuery().startsWith(ConnectorConstants.SYNTHETIC_PREFIX)
                        && !accessor.getQuery().startsWith("perfcounter")
                        && !accessor.getQuery().endsWith(".scaled"))
                    propertySpec.getPathSet().add(accessor.getQuery());
            }
            for (BaseQuery accessor : temp.getDefaultSyntheticList()) {
                queryPool.put(accessor.getQuery(), accessor);
                if (!accessor.getQuery().startsWith(ConnectorConstants.SYNTHETIC_PREFIX)
                        && !accessor.getQuery().startsWith("perfcounter")
                        && !accessor.getQuery().endsWith(".scaled"))
                    propertySpec.getPathSet().add(accessor.getQuery());
            }
            // this is LAST in order to OVERRIDE the above defaults
            for (BaseQuery accessor : hostQueries) {
                if (accessor.getSourceType() == SourceType.storage) {
                    queryPool.put(accessor.getQuery(), accessor);
                    if (!accessor.getQuery().startsWith(ConnectorConstants.SYNTHETIC_PREFIX)
                            && !accessor.getQuery().startsWith("perfcounter")
                            && !accessor.getQuery().endsWith(".scaled"))
                        propertySpec.getPathSet().add(accessor.getQuery());
                }
            }

            TraversalSpec ts = new TraversalSpec();
            ts.setName("view");
            ts.setPath("view");
            ts.setSkip(false);
            ts.setType("ContainerView");

            // Now create Object Spec
            ObjectSpec objectSpec = new ObjectSpec();
            objectSpec.setObj(containerView);
            objectSpec.setSkip(Boolean.TRUE);
            objectSpec.getSelectSet().add(ts);

            PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
            propertyFilterSpec.getPropSet().add(propertySpec);
            propertyFilterSpec.getObjectSet().add(objectSpec);
            List<PropertyFilterSpec> propertyFilterSpecs = new ArrayList<PropertyFilterSpec>();
            propertyFilterSpecs.add(propertyFilterSpec);

            propertyCollector = vimPort.createPropertyCollector(serviceContent.getPropertyCollector());
            List<ObjectContent> objectContents = vimPort.retrieveProperties(propertyCollector, propertyFilterSpecs);
            if (objectContents != null) {
                for (ObjectContent oc : objectContents) {
                    ManagedObjectReference mr = oc.getObj();
                    List<DynamicProperty> dps = oc.getPropSet();
                    if (dps == null) {
                        log.error("no properties found for vmStorage");
                        continue;
                    }
                    // extract storage by name and accessible status
                    VmWareStorage vmStorage = null;
                    boolean accessible = true;
                    for (DynamicProperty dp : dps) {
                        // storage by name
                        if (dp.getName().equals("name")) {
                            vmStorage = new VmWareStorage(dp.getVal().toString());
                        }
                        // metrics accessible status, (e.g. not stale)
                        if (dp.getName().equals("summary.accessible")) {
                            if (dp.getVal() instanceof Boolean) {
                                accessible = (Boolean) dp.getVal();
                            } else {
                                accessible = Boolean.parseBoolean(dp.getVal().toString());
                            }
                        }
                    }
                    if (vmStorage == null) {
                        log.error("no name property found for vmStorage");
                        continue;
                    }
                    String url = "";
                    String type = "";
                    for (DynamicProperty dp : dps) {
                        String path = dp.getName();
                        String value = dp.getVal().toString();
                        if (path.equals("summary.url"))
                            url = value;
                        if (path.equals("summary.type"))
                            type = value;
                        BaseQuery vbq = queryPool.get(path);
                        BaseQuery cq = vmQueryMap.get(path);
                        String customName = (cq == null) ? "" : cq.getCustomName();
                        BaseMetric vbm = new BaseMetric(
                                path,
                                vbq.getWarning(),
                                vbq.getCritical(),
                                vbq.isGraphed(),
                                vbq.isMonitored(),
                                customName
                        );
                        if (vbq.isTraced())
                            vbm.setTrace();

                        if (path.equals("summary.capacity") ||
                                path.equals("summary.uncommitted") ||
                                path.equals("summary.freeSpace")) {
                            value = Conversion.byte2MB(value);
                        }
                        // return unknown status if not accessible
                        vbm.setValue(accessible ? value : null);

                        if (vmStorage.isMetric(path))
                            vmStorage.putMetric(path, vbm);
                        else
                            vmStorage.putConfig(path, vbm);
                    }
                    // inject summary.uncommitted if not returned as property
                    if (!vmStorage.getMetricPool().containsKey("summary.uncommitted") &&
                            vmStorage.getMetricPool().containsKey("summary.capacity") &&
                            vmStorage.getMetricPool().containsKey("summary.freeSpace")) {
                        // create summary.uncommitted metric
                        BaseQuery vbq = queryPool.get("summary.uncommitted");
                        BaseQuery cq = vmQueryMap.get("summary.uncommitted");
                        String customName = (cq == null) ? "" : cq.getCustomName();
                        BaseMetric vbm = new BaseMetric(
                                "summary.uncommitted",
                                vbq.getWarning(),
                                vbq.getCritical(),
                                vbq.isGraphed(),
                                vbq.isMonitored(),
                                customName
                        );
                        // return unknown status if not accessible
                        vbm.setValue(accessible ? "0" : null);
                        // add summary.uncommitted metric
                        vmStorage.putMetric("summary.uncommitted", vbm);
                    }
                    vmStorage.setDescription(type);
                    vmStorage.setRunExtra(url);
                    computeScaled(queryPool, vmStorage, vmQueryMap);
                    computerSynthetics(queryPool, vmStorage, vmQueryMap);

                    vmStorage.setRunState(vmStorage.getMonitorStateByStatus());
                    storageMap.put(vmStorage.getHostName(), vmStorage);
                }
            }
            crushMetrics(storageMap, vmQueryMap);
        } catch (Exception e) {
            throw new ConnectorException("Failed to retrieve storage metrics for " + inventoryType.name(), e);
        } finally {
            VMwareConnector.destroyCollectorAndView(vimPort, containerView, propertyCollector);
        }
        return storageMap;
    }

    private Map<String, VmWareNetwork> gatherNetworkMetrics(List<BaseQuery> hostQueries) {
        InventoryType inventoryType = InventoryType.Network;
        Map<String, VmWareNetwork> networkMap = new ConcurrentHashMap<String, VmWareNetwork>();
        Map<String, BaseQuery> vmQueryMap = new HashMap<>();
        for (BaseQuery q : hostQueries) {
            vmQueryMap.put(q.getQuery(), q);
        }
        // resources requiring destroy
        ManagedObjectReference containerView = null;
        ManagedObjectReference propertyCollector = null;
        try {
            ConcurrentHashMap<String, BaseQuery> queryPool = new ConcurrentHashMap<String, BaseQuery>();
            ManagedObjectReference viewManager = serviceContent.getViewManager();
            containerView = vimPort.createContainerView(viewManager, rootFolder, Arrays.asList(inventoryType.name()), true);

            // Create Property Spec
            PropertySpec propertySpec = new PropertySpec();
            propertySpec.setAll(Boolean.FALSE);
            propertySpec.setType(inventoryType.name());
            VmWareNetwork temp = new VmWareNetwork("temp");
            for (BaseQuery accessor : temp.getDefaultMetricList()) {
                queryPool.put(accessor.getQuery(), accessor);
                if (!accessor.getQuery().startsWith(ConnectorConstants.SYNTHETIC_PREFIX)
                        && !accessor.getQuery().startsWith("perfcounter")
                        && !accessor.getQuery().endsWith(".scaled"))
                    propertySpec.getPathSet().add(accessor.getQuery());
            }
            for (BaseQuery accessor : temp.getDefaultConfigList()) {
                queryPool.put(accessor.getQuery(), accessor);
                if (!accessor.getQuery().startsWith(ConnectorConstants.SYNTHETIC_PREFIX)
                        && !accessor.getQuery().startsWith("perfcounter")
                        && !accessor.getQuery().endsWith(".scaled"))
                    propertySpec.getPathSet().add(accessor.getQuery());
            }
            for (BaseQuery accessor : temp.getDefaultSyntheticList()) {
                queryPool.put(accessor.getQuery(), accessor);
                if (!accessor.getQuery().startsWith(ConnectorConstants.SYNTHETIC_PREFIX)
                        && !accessor.getQuery().startsWith("perfcounter")
                        && !accessor.getQuery().endsWith(".scaled"))
                    propertySpec.getPathSet().add(accessor.getQuery());
            }
            // this is LAST in order to OVERRIDE the above defaults
            for (BaseQuery accessor : hostQueries) {
                if (accessor.getSourceType() == SourceType.network) {
                    queryPool.put(accessor.getQuery(), accessor);
                    if (!accessor.getQuery().startsWith(ConnectorConstants.SYNTHETIC_PREFIX)
                            && !accessor.getQuery().startsWith("perfcounter")
                            && !accessor.getQuery().endsWith(".scaled"))
                        propertySpec.getPathSet().add(accessor.getQuery());
                }
            }

            TraversalSpec ts = new TraversalSpec();
            ts.setName("view");
            ts.setPath("view");
            ts.setSkip(false);
            ts.setType("ContainerView");

            // Now create Object Spec
            ObjectSpec objectSpec = new ObjectSpec();
            objectSpec.setObj(containerView);
            objectSpec.setSkip(Boolean.TRUE);
            objectSpec.getSelectSet().add(ts);

            PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
            propertyFilterSpec.getPropSet().add(propertySpec);
            propertyFilterSpec.getObjectSet().add(objectSpec);
            List<PropertyFilterSpec> propertyFilterSpecs = new ArrayList<PropertyFilterSpec>();
            propertyFilterSpecs.add(propertyFilterSpec);

            propertyCollector = vimPort.createPropertyCollector(serviceContent.getPropertyCollector());
            List<ObjectContent> objectContents = vimPort.retrieveProperties(propertyCollector, propertyFilterSpecs);
            if (objectContents != null) {
                for (ObjectContent oc : objectContents) {
                    ManagedObjectReference mr = oc.getObj();
                    List<DynamicProperty> dps = oc.getPropSet();
                    if (dps == null) {
                        log.error("no properties found for vmNetwork");
                        continue;
                    }
                    VmWareNetwork vmNetwork = null;
                    for (DynamicProperty dp : dps) {
                        if (dp.getName().equals("name")) {
                            vmNetwork = new VmWareNetwork(dp.getVal().toString());
                            break;
                        }
                    }
                    if (vmNetwork == null) {
                        log.error("no name property found for vmNetwork");
                        continue;
                    }
                    String ipPool = null;
                    String accessible = null;
                    for (DynamicProperty dp : dps) {
                        boolean forceMetric = false;
                        String path = dp.getName();
                        String value = dp.getVal().toString();
                        if (path.equals("summary.ipPoolName")) {
                            ipPool = value;
                        }
                        else if (path.equals("summary.accessible")) {
                            accessible = value;
                            if (value == null || !value.toLowerCase().equals("true")) {
                                value = "0";
                            }
                            else {
                                value = "1";
                            }
                            forceMetric = true;
                        }
                        BaseQuery vbq = queryPool.get(path);
                        BaseQuery cq = vmQueryMap.get(path);
                        String customName = (cq == null) ? "" : cq.getCustomName();
                        BaseMetric vbm = new BaseMetric(
                                path,
                                vbq.getWarning(),
                                vbq.getCritical(),
                                vbq.isGraphed(),
                                vbq.isMonitored(),
                                customName
                        );
                        if (vbq.isTraced())
                            vbm.setTrace();
                        vbm.setValue(value);

                        if (forceMetric || vmNetwork.isMetric(path))
                            vmNetwork.putMetric(path, vbm);
                        else
                            vmNetwork.putConfig(path, vbm);
                    }
                    vmNetwork.setDescription(mr.getValue());
                    if (accessible == null) {
                        accessible = "";
                    }
                    if (ipPool == null) {
                        ipPool = "";
                    }
                    accessible = (accessible.equalsIgnoreCase("false") || accessible.equals("")) ? "not accessible" : "accessible";
                    ipPool = (ipPool.equals("")) ? "no pools configured" : ipPool;
                    vmNetwork.setRunExtra(accessible + " - " + ipPool);
                    computeScaled(queryPool, vmNetwork, vmQueryMap);
                    computerSynthetics(queryPool, vmNetwork, vmQueryMap);
                    vmNetwork.setRunState(vmNetwork.getMonitorStateByStatus());
                    networkMap.put(vmNetwork.getHostName(), vmNetwork);
                }
            }
            //crushDownMetrics(networkMap);
        } catch (Exception e) {
            throw new ConnectorException("Failed to retrieve network metrics for " + inventoryType.name(), e);
        } finally {
            VMwareConnector.destroyCollectorAndView(vimPort, containerView, propertyCollector);
        }
        return networkMap;
    }


    protected void mergeMonitoringResults(Map<String, BaseHost> baseHosts, Map<String, ? extends BaseHost> newHosts, boolean autoDelete) {
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
        for (BaseHost hosto : newHosts.values()) {
            hosto.incSkipped();
            for (BaseVM vmo : hosto.getVMPool().values())
                vmo.incSkipped();
        }

        int hostsAdded = 0;
        int vmsAdded = 0;
        // now try to merge in the newstuff...
        for (String host : newHosts.keySet()) {
            // begin by ensuring target object exists, for hosts.
            if (!baseHosts.containsKey(host)) {
                hostsAdded++;
                VMwareHost newHost = new VMwareHost(host);
                BaseHost tHost = newHosts.get(host);
                newHost.setSystemName(tHost.getSystemName());
                baseHosts.put(host, newHost);
            }

            // and target object (virtual machines) exists, too.
            for (String vm : newHosts.get(host).getVMPool().keySet()) {
                if (!baseHosts.get(host).getVMPool().containsKey(vm)) {
                    vmsAdded++;
                    baseHosts.get(host).getVMPool().put(vm, new VMwareVM(vm));
                }
            }
            // now merge them
            baseHosts.get(host).mergeInNew(newHosts.get(host));
        }

        // DELETION OF ORPHANED OBJECTS HERE  // This code deletes 'em.
        List<String> hostDeletes = new ArrayList<String>();
        int vmDeleteCount = 0;
        if (autoDelete) {
            for (String host : baseHosts.keySet()) {
                BaseHost newHost = newHosts.get(host);
                BaseHost baseHost = baseHosts.get(host);
                if (newHost != null) {
                    if (!newHosts.containsKey(host)) {
                        hostDeletes.add(host);
                        continue;
                    }
                    Map<String, BaseVM> newVmPool = newHost.getVMPool();
                    Map<String, BaseVM> baseVmPool = baseHost.getVMPool();
                    if (newVmPool != null && baseVmPool != null) {
                        List<String> vmDeletes = new ArrayList<String>();
                        for (String vm : baseVmPool.keySet()) {
                            if (!newVmPool.containsKey(vm)) {
                                vmDeletes.add(vm);
                            }
                        }
                        for (String vm : vmDeletes) {
                            baseVmPool.remove(vm);
                            if (log.isDebugEnabled())
                                log.debug("Removing VM " + vm + " from monitored list for host " + host);
                            vmDeleteCount++;
                        }
                    }
                }
            }
            for (String host : hostDeletes) {
                baseHosts.remove(host);
                if (log.isDebugEnabled())
                    log.debug("Removing host " + host + " from monitored list.");
            }
        }
        if (log.isInfoEnabled())
            log.info("mergeMonitoringResults: "
                    + " Hosts Added: " + hostsAdded
                    + ", VMs Added: " + vmsAdded
                    + ", Hosts Deleted: " + hostDeletes.size()
                    + ", VMs Deleted: " + vmDeleteCount);

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
        sslsc.setSessionTimeout(0); // seconds
        sc.init(null, trustAllCerts, null);
        HttpsURLConnection.setDefaultSSLSocketFactory(
                sc.getSocketFactory());
    }

    /**
     * Establishes session with the virtual center server.
     *
     * @throws Exception the exception
     */
    public void attach() throws Exception {
        HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String urlHostName, SSLSession session) {
                if (log.isDebugEnabled())
                    log.debug("urlHostName = '" + urlHostName + "'");
                return true;
            }
        };

        trustAllHttpsCertificates();
        HttpsURLConnection.setDefaultHostnameVerifier(hv);

        ManagedObjectReference serviceInstanceRef = new ManagedObjectReference();
        serviceInstanceRef.setType(SVC_INST_NAME);   // SVC_INST_NAME
        serviceInstanceRef.setValue(SVC_INST_NAME);  // is "ServiceInstance"

        vimService = new VimService();
        vimPort = vimService.getVimPort();
        Map<String, Object> ctxt = ((BindingProvider) vimPort).getRequestContext();

        ctxt.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, vmwareURL);
        ctxt.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);      // keeps session open
        ctxt.put(BindingProviderProperties.REQUEST_TIMEOUT, 30000); // Timeout in millis
        ctxt.put(BindingProviderProperties.CONNECT_TIMEOUT, 60000); // Timeout in millis

        serviceContent = vimPort.retrieveServiceContent(serviceInstanceRef);
        sessionManager = serviceContent.getSessionManager();

        try {
            vimPort.login(sessionManager, vmwareLogin, vmwarePassword, null);
        } catch (Exception e) {
            log.error(String.format(
                    "vimPort.login( url=%s, login=%s ) - couldn't connect.  Exception '%s'\n",
                    vmwareURL, vmwareLogin, e.toString()));
            throw e;
        }

        rootFolder = serviceContent.getRootFolder();
        //propertyCollector = serviceContent.getPropertyCollector();
        //perfManager = serviceContent.getPerfManager();
    }

    private TestContext attachTest(String vmwareURL, String vmwareLogin, String vmwarePassword) throws Exception {
        HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String urlHostName, SSLSession session) {
                if (log.isDebugEnabled())
                    log.debug("urlHostName = '" + urlHostName + "'");
                return true;
            }
        };

        trustAllHttpsCertificates();
        HttpsURLConnection.setDefaultHostnameVerifier(hv);

        ManagedObjectReference svcRef = new ManagedObjectReference();
        svcRef.setType(SVC_INST_NAME);   // SVC_INST_NAME
        svcRef.setValue(SVC_INST_NAME);  // is "ServiceInstance"

        VimService vimTestService = new VimService();
        VimPortType vimTestPort = vimTestService.getVimPort();
        Map<String, Object> ctxt = ((BindingProvider) vimTestPort).getRequestContext();

        ctxt.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, vmwareURL);
        ctxt.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);      // keeps session open
        ctxt.put(BindingProviderProperties.REQUEST_TIMEOUT, 30000); // Timeout in millis
        ctxt.put(BindingProviderProperties.CONNECT_TIMEOUT, 60000); // Timeout in millis

        ServiceContent serviceTestContent = vimTestPort.retrieveServiceContent(svcRef);
        ManagedObjectReference sessionTestManager = serviceTestContent.getSessionManager();

        try {
            vimTestPort.login(sessionTestManager, vmwareLogin, vmwarePassword, null);
        } catch (Exception e) {
            log.error(String.format(
                    "vimPort.login( url=%s, login=%s ) - couldn't connect.  Exception '%s'\n",
                    vmwareURL, vmwareLogin, e.toString()));
            throw e;
        }
        return new TestContext(vimTestPort, serviceTestContent);
    }

    private void computeScaled(Map<String, BaseQuery> queryPool, VMwareHost vmObj, Map<String, BaseQuery> vmQueryMap) {
        for (String query : queryPool.keySet())  // SCALED value adjustments here
        {
            if (!query.endsWith(".scaled"))
                continue;  // move along

            BaseQuery vbq = queryPool.get(query);
            BaseQuery cq = vmQueryMap.get(query);
            String customName = (cq == null) ? "" : cq.getCustomName();
            BaseMetric vbm = new BaseMetric(
                    query,
                    vbq.getWarning(),
                    vbq.getCritical(),
                    vbq.isGraphed(),
                    vbq.isMonitored(),
                    customName
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

            vbm.setValue(result);
            vmObj.putMetric(query, vbm);
        }
    }

    private void computerSynthetics(Map<String, BaseQuery> queryPool, VMwareHost vmObj, Map<String, BaseQuery> vmQueryMap) {
        for (String query : queryPool.keySet()) {
            if (!query.startsWith(ConnectorConstants.SYNTHETIC_PREFIX))
                continue;  // not one of ours.
            BaseSynthetic vbs;
            BaseQuery vbq = queryPool.get(query);
            BaseQuery cq = vmQueryMap.get(query);
            String customName = (cq == null) ? "" : cq.getCustomName();
            BaseMetric vbm = new BaseMetric(
                    query,
                    vbq.getWarning(),
                    vbq.getCritical(),
                    vbq.isGraphed(),
                    vbq.isMonitored(),
                    customName
            );
            String result = "uncomputed";
            if ((vbs = vmObj.getSynthetic(query)) != null) {
                String value1 = vmObj.getValueByKey(vbs.getLookup1());
                String value2 = vmObj.getValueByKey(vbs.getLookup2());
                result = String.valueOf(vbs.compute(value1, value2)) + "%";
            }
            vbm.setValue(result);

            if (vbq.isTraced())
                vbm.setTrace();

            vmObj.putMetric(query, vbm);
        }
    }

    // ----------------------------------------------------------------------
    // Clear out the metrics that the upper code doesn't want to monitor
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
    private void crushDownMetrics(Map<String, ? extends BaseHost> hostMap) {
        for (BaseHost host : hostMap.values()) {
            boolean crushHostMetrics = false;
            if (host.getMergeCount() > 1 &&
                    (host.getRunState().equals(VMwareHost.UNSCHEDULED_DOWN)
                            || host.getRunState().contains("SUSPEND"))) {
                crushHostMetrics = true;
            }
            for (String metricName : host.getMetricPool().keySet())
                if (crushHostMetrics || !host.getMetric(metricName).isMonitored())
                    host.getMetricPool().remove(metricName);

            for (String configName : host.getConfigPool().keySet())
                if (crushHostMetrics || !host.getConfig(configName).isMonitored())
                    host.getConfigPool().remove(configName);

            for (BaseVM vmo : host.getVMPool().values()) {
                boolean crushVMMetrics = false;

                if ((vmo.getMergeCount() > 1) &&
                        (vmo.getRunState().equals(VMwareHost.UNSCHEDULED_DOWN)
                                || vmo.getRunState().contains("SUSPEND"))) {
                    crushVMMetrics = true;
                }

                for (String metricName : vmo.getMetricPool().keySet())
                    if (crushVMMetrics || !vmo.getMetric(metricName).isMonitored())
                        vmo.getMetricPool().remove(metricName);

                for (String configName : vmo.getConfigPool().keySet())
                    if (crushVMMetrics || !vmo.getConfig(configName).isMonitored())
                        vmo.getConfigPool().remove(configName);
            }
        }
    }

    private class TestContext {

        private VimPortType vimPortType;
        private ServiceContent serviceContent;

        TestContext(final VimPortType vimPort, final ServiceContent serviceContext) {
            this.vimPortType = vimPort;
            this.serviceContent = serviceContext;
        }

        public VimPortType getVimPortType() {
            return vimPortType;
        }

        public ServiceContent getServiceContent() {
            return serviceContent;
        }
    }

    public static void destroyCollectorAndView(VimPortType vimPort,
                                                  ManagedObjectReference containerView,
                                                  ManagedObjectReference propertyCollector) {
        if (propertyCollector != null) {
            try {
                vimPort.destroyPropertyCollector(propertyCollector);
            } catch (Exception e) {
                log.error("Failed to destroy inventory managed property collector: " + e.getMessage(), e);
            }
        }
        if (containerView != null) {
            try {
                vimPort.destroyView(containerView);
            } catch (Exception e) {
                log.error("Failed to destroy inventory managed continer view: " + e.getMessage(), e);
            }
        }

    }

}
