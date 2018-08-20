package org.groundwork.cloudhub.connectors.vmwarevi;

import com.doublecloud.vim25.GuestNicInfo;
import com.doublecloud.vim25.ManagedObjectReference;
import com.doublecloud.vim25.VirtualMachineSnapshotInfo;
import com.doublecloud.vim25.VirtualMachineStorageInfo;
import com.doublecloud.vim25.mo.InventoryNavigator;
import com.doublecloud.vim25.mo.ManagedEntity;
import com.doublecloud.vim25.mo.util.PropertyCollectorUtil;
import org.apache.log4j.Logger;
import org.groundwork.agents.utils.CollectorTimer;
import org.groundwork.cloudhub.metrics.DefaultMetricProvider;
import org.groundwork.cloudhub.connectors.vmware.VMwareVM;
import org.groundwork.cloudhub.connectors.vmware2.MetricsUtils;
import org.groundwork.cloudhub.metrics.BaseMetric;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.groundwork.cloudhub.metrics.BaseSynthetic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Collects metrics for VMware Virtual machines
 */
@Component
public class VirtualMachineMetricCollector {

    private static Logger log = Logger.getLogger(VirtualMachineMetricCollector.class);

    @Autowired
    private PropertyListManager propertyListManager;
    @Autowired
    private VISnapshotService snapshotService;
    @Autowired
    protected MetricsUtils metricsUtils;

    private DefaultMetricProvider defaults = new VMwareVM("__default__");

    /**
     * Collects metrics for all virtual machines in this data center. The metric list is constrained by the vmQueries list
     *
     * @param vmQueries the active set of metric names to be retrieved
     * @param navigator
     * @return a set of Cloudhub VMs, with each VM holding current set of metrics
     * @throws Exception main collectMetrics end point handles all exceptions
     */
    public Map<String, VMwareVM> collectMetrics(List<BaseQuery> vmQueries, InventoryNavigator navigator, CollectorTimer timer) throws Exception {

        if (log.isDebugEnabled()) timer.start("VM-metrics");

        Map<String, BaseQuery> vmQueryPool = new ConcurrentHashMap<String, BaseQuery>();
        Map<String, VMwareVM> vmPool = new ConcurrentHashMap<String, VMwareVM>();

        // CLOUDHUB-296: custom names
        Map<String, String> customVmNames = new ConcurrentHashMap<>();
        for (BaseQuery query : vmQueries) {
            customVmNames.put(query.getQuery(), (query.getCustomName() == null) ? "" : query.getCustomName());
        }

        if (log.isDebugEnabled()) timer.start("VM-inventory");
        ManagedEntity[] managedEntities = navigator.searchManagedEntities(InventoryType.VirtualMachine.name());
        Set<String> propertySet = propertyListManager.createPropertyNames(vmQueryPool, vmQueries, defaults);
        if (log.isDebugEnabled()) timer.stopStart("VM-inventory", "VM-properties") ;

        // CLOUDHUB-333: add support for snapshots
        vmQueryPool = snapshotService.configurePropertyNames(vmQueryPool, propertySet);

        String[] propertyNames = propertySet.toArray(new String[0]);
        Hashtable[] properties = PropertyCollectorUtil.retrieveProperties(managedEntities, InventoryType.VirtualMachine.name(), propertyNames);
        if (log.isDebugEnabled()) {
            timer.stopStart("VM-properties", "VM-collect");
        }
        for (int ix = 0; ix < managedEntities.length; ix++) {
            String morName = managedEntities[ix].getMOR().getVal();
            VMwareVM vm = createVM(properties[ix], morName);
            // snapshots
            VirtualMachineSnapshotInfo vmsi = (VirtualMachineSnapshotInfo) properties[ix].get(VISnapshotService.DP_TYPE_SNAPSHOT);
            if (vmsi != null) {
                VISnapshotService.SnapshotInfo vmSnapshot = snapshotService.calculateSnapshots(vm.getVMName(), vmsi);
                snapshotService.updateSnapshotMetrics(vmQueryPool, customVmNames, vmSnapshot, vm);
            }
            VirtualMachineStorageInfo storageInfo = (VirtualMachineStorageInfo) properties[ix].get(VISnapshotService.DP_TYPE_SNAPSHOT_STORAGE);
            if (storageInfo != null) {
                VISnapshotService.SnapshotStorageInfo vmSnapshot = snapshotService.calculateSnapshotStorage(vm.getVMName(), storageInfo);
                snapshotService.updateSnapshotStorageMetrics(vmQueryPool, customVmNames, vmSnapshot, vm);
            }
            for (int iy = 0; iy < propertyNames.length; iy++) {
                String path = propertyNames[iy];
                Object value = properties[ix].get(path);
                if (value == null) {
                    continue;
                }
                BaseQuery vbq = vmQueryPool.get(path);
                if (vbq == null) {
                    continue;
                }
                BaseMetric vbm = metricsUtils.createMetricFromQuery(path, vbq, customVmNames.get(path));
                vbm.setValue(value.toString());
                if (defaults.isMetricMonitored(vbq)) {
                    vm.putMetric(path, vbm);
                } else {
                    vm.putConfig(path, vbm);
                }
            }

            for (String query : vmQueryPool.keySet()) {
                if (!query.startsWith("syn.vm."))
                    continue;  // not one of ours.

                BaseSynthetic vbs;
                BaseQuery vbq = vmQueryPool.get(query);
                BaseMetric vbm = metricsUtils.createMetricFromQuery(query, vbq, customVmNames.get(query));
                String result = "uncomputed";
                if ((vbs = defaults.getSynthetic(query)) != null) {
                    String value1 = vm.getValueByKey(vbs.getLookup1());
                    String value2 = vm.getValueByKey(vbs.getLookup2());

                    result = String.valueOf(vbs.compute(value1, value2)) + "%";
                }
                vbm.setValue(result);
                vm.putMetric(query, vbm);
            }

            vm.setRunState(vm.getMonitorState());
            vmPool.put(morName, vm);
        }
        if (log.isDebugEnabled()) {
            timer.stop("VM-properties");
            timer.stop("VM-metrics");
        }
        return vmPool;
    }

    /**
     * Create a VM object and handle all special cases, set all hard-wired properties on that VM
     *
     * @param properties the dynamic properties queried for a given VM
     * @return a new Cloudhub VM
     */
    protected VMwareVM createVM(Hashtable properties, String morName) {

        // create VM from name
        String name = (String) properties.get(PropertyListManager.PROP_NAME);
        VMwareVM vm = new VMwareVM(name);
        vm.setSystemName(morName);

        // network mac address
        Object networkProbe = properties.get(PropertyListManager.PROP_GUEST_NETWORK);
        if (networkProbe instanceof GuestNicInfo[]) { // BUG: VI driver is returning a weird Object with no members here
            GuestNicInfo[] nics = (GuestNicInfo[]) properties.get(PropertyListManager.PROP_GUEST_NETWORK);
            if (nics != null) {
                if (nics != null && nics.length > 0) {
                    vm.setMacAddress(nics[0].getMacAddress());
                }
            }
        }
        // VM's Host
        ManagedObjectReference mor = (ManagedObjectReference) properties.get(PropertyListManager.PROP_RUNTIME_HOST);
        if (mor != null) {
            vm.setHypervisor(mor.getVal());
        }
        // IP Address
        String ipAddress = (String) properties.get(PropertyListManager.PROP_IP_ADDRESS);
        if (ipAddress != null) {
            vm.setIpAddress(ipAddress);
        }
        // Guest state
        String guestState = (String) properties.get(PropertyListManager.PROP_GUEST_STATE);
        if (guestState != null) {
            vm.setGuestState(guestState);
        }
        // Boot Time
        Object bootTime = properties.get(PropertyListManager.PROP_BOOTTIME);
        if (bootTime != null && bootTime instanceof Calendar) {
            Calendar date = (Calendar) bootTime;
            vm.setBootDate(date);
        }
        // Up Time
        Integer upTime = (Integer) properties.get(PropertyListManager.PROP_UPTIME);
        if (upTime != null) {
            vm.setLastUpdate(upTime.toString());
        }
        return vm;
    }

}
