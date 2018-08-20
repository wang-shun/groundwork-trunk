package org.groundwork.cloudhub.connectors.vmware2;

import com.vmware.vim25.ArrayOfGuestNicInfo;
import com.vmware.vim25.GuestNicInfo;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.VirtualMachineSnapshotInfo;
import com.vmware.vim25.VirtualMachineStorageInfo;
import org.groundwork.cloudhub.connectors.vmware.SnapshotService;
import org.groundwork.cloudhub.connectors.vmware.VMwareVM;
import org.groundwork.cloudhub.metrics.BaseMetric;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.groundwork.cloudhub.synthetics.Synthetics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

@Component
public class VirtualMachineConverter extends VmWareBaseConverter {

    @Autowired
    protected SnapshotService snapshotService;
    @Autowired
    protected MetricsUtils metricsUtils;
    @Autowired
    protected Synthetics synthetics;

    public VMwareVM convert(MetricCollectorInstance instance, Map<String, BaseQuery> queryPool,  Map<String, String> customNames) {
        String name = (String) instance.getProperty(PROP_NAME);
        VMwareVM vm = new VMwareVM(name);
        vm.setSystemName(instance.getName());
        // get the macAddress config
        Object property = instance.getProperty(PROP_GUEST_NETWORK);
        if (property instanceof ArrayOfGuestNicInfo) {
            List<GuestNicInfo> gniList = ((ArrayOfGuestNicInfo) property).getGuestNicInfo();
            if (gniList.size() > 0) {
                GuestNicInfo gni = gniList.get(0);
                if (vm != null) {
                    BaseQuery vbq = queryPool.get(PROP_GUEST_NETWORK);
                    BaseMetric vbm = metricsUtils.createMetricFromQuery(PROP_GUEST_NETWORK, vbq, customNames.get(name));
                    if (vbq.isTraced())
                        vbm.setTrace();
                    vbm.setValue(gni.getMacAddress());
                    vm.setMacAddress(gni.getMacAddress());
                    vm.putConfig(name, vbm);
                }
            }
        }
        // hypervisor name
        property = instance.getProperty(PROP_RUNTIME_HOST);
        if (property instanceof ManagedObjectReference) {
            ManagedObjectReference mor = (ManagedObjectReference) property;
            vm.setHypervisor(mor.getValue());
        }
        // Custom VM Fields
        String ipAddress = (String) instance.getProperty(PROP_IP_ADDRESS);
        if (ipAddress != null) {
            vm.setIpAddress(ipAddress);
        }
        String guestState = (String) instance.getProperty(PROP_GUEST_STATE);
        if (guestState != null) {
            vm.setGuestState(guestState);
        }
        Object bootTime = instance.getProperty(PROP_BOOTTIME);
        if (bootTime != null && bootTime instanceof Calendar) {
            Calendar date = (Calendar) bootTime;
            vm.setBootDate(date);
        }
        Integer upTime = (Integer) instance.getProperty(PROP_UPTIME);
        if (upTime != null) {
            vm.setLastUpdate(upTime.toString());
        }
        // Snapshots - CLOUDHUB-333: gather snapshot information
        VirtualMachineSnapshotInfo vmsi = (VirtualMachineSnapshotInfo)instance.getProperty(SnapshotService.DP_TYPE_SNAPSHOT);
        if (vmsi != null) {
            SnapshotService.SnapshotInfo vmSnapshot = snapshotService.calculateSnapshots(vm.getVMName(), vmsi);
            snapshotService.updateSnapshotMetrics(queryPool, customNames, vmSnapshot, vm);
        }
        VirtualMachineStorageInfo storageInfo = (VirtualMachineStorageInfo) instance.getProperty(SnapshotService.DP_TYPE_SNAPSHOT_STORAGE);
        if (storageInfo != null) {
            SnapshotService.SnapshotStorageInfo vmSnapshot = snapshotService.calculateSnapshotStorage(vm.getVMName(), storageInfo);
            snapshotService.updateSnapshotStorageMetrics(queryPool, customNames, vmSnapshot, vm);
        }

        // required properties that don't always report
        Integer cpu = (Integer)instance.getProperty("summary.runtime.maxCpuUsage");
        if (cpu == null) {
            instance.getProperties().put("summary.runtime.maxCpuUsage", 0);
        }
        Integer mem = (Integer)instance.getProperty("summary.config.memorySizeMB");
        if (mem == null) {
            instance.getProperties().put("summary.config.memorySizeMB", 0);
        }
        // Convert all properties to GW metrics
        metricsUtils.processMetrics(vm, instance, queryPool, customNames);

        // Process Synthetics
        //synthetics.evaluate(new SyntheticContext(instance.getProperties()));
        metricsUtils.processSynthetics(instance, vm, null, queryPool, customNames);

        vm.setRunningState(vm.getMonitorState());
        return vm;
    }

}
