package org.groundwork.cloudhub.connectors.vmware;

import com.vmware.vim25.PropertySpec;
import com.vmware.vim25.VirtualMachineSnapshotInfo;
import com.vmware.vim25.VirtualMachineSnapshotTree;
import com.vmware.vim25.VirtualMachineStorageInfo;
import com.vmware.vim25.VirtualMachineUsageOnDatastore;
import org.groundwork.cloudhub.connectors.vmware2.PropertyCollectorSpec;
import org.groundwork.cloudhub.metrics.BaseMetric;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Instant;
import org.springframework.stereotype.Service;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

/**
 * The Snapshot Service calculates snapshot metrics for the following cases:

 1. the number of snapshots is beyond a user-defined threshold (per instance)
 2. set a threshold on snapshot longevity in days
 3. set a threshold on the size of snapshot storage
 4. size related used cases with synthetics in relation to total storage

 The following new metrics will be a part of the VmWare connector profile:

 snapshots.rootCount = number of root snapshots on the VM
 snapshots.childCount = Number of child snapshots on the VM
 snapshots.count - sum of the above.
 snapshots.oldestDays - oldest snapshot in days computed over 24hour period
 snapshots.fullDiskUsage - the total amount of raw disk space used by all sna

 */
@Service
public class SnapshotService {

    // GW snapshot calculated metrics. These are not synthetics, but they are calculated by this service
    public static final String SNAPSHOT_METRIC_COUNT = "snapshots.count";
    public static final String SNAPSHOT_METRIC_ROOT_COUNT = "snapshots.rootCount";
    public static final String SNAPSHOT_METRIC_CHILD_COUNT = "snapshots.childCount";
    public static final String SNAPSHOT_METRIC_OLDEST = "snapshots.oldestInDays";
    public static final String SNAPSHOT_METRIC_DISKUSAGE = "snapshots.fullDiskUsage";
    public static final String SNAPSHOTS_PREFIX = "snapshots";

    // VMWare dynamic property type constants
    public static final String DP_TYPE_SNAPSHOT = "snapshot";
    public static final String DP_TYPE_SNAPSHOT_STORAGE = "storage";

    /**
     * Holds per VM state for snapshot counts and age calculations
     */
    public class SnapshotInfo {

        private String vm;
        private int count = 0;
        private int rootCount = 0;
        private int childCount = 0;
        private int oldest = 0; // in days

        public SnapshotInfo(String vm) {
            this.vm = vm;
        }

        public String getVm() {
            return vm;
        }

        public int getCount() {
            return count;
        }

        public int getRootCount() {
            return rootCount;
        }

        public int getChildCount() {
            return childCount;
        }

        public int getOldest() {
            return oldest;
        }

        public int incrementRootCount() {
            rootCount = rootCount + 1;
            count = count + 1;
            return rootCount;
        }

        public int incrementChildCount() {
            childCount = childCount + 1;
            count = count + 1;
            return childCount;
        }

        public int calculateOldest(GregorianCalendar date) {
            DateTime dt = new DateTime(date);
            int oldest = Days.daysBetween(dt, new Instant()).getDays();
            if (oldest > this.oldest)  {
                this.oldest = oldest;
            }
            return oldest;
        }

    }

    /**
     *  Holds per VM state for snapshot storage information
     */
    public class SnapshotStorageInfo {

        private String vm;
        private long fullDiskUsage = 0;

        public SnapshotStorageInfo(String vm) {
            this.vm = vm;
        }

        public String getVm() {
            return vm;
        }

        public long getFullDiskUsage() {
            return fullDiskUsage;
        }

        public long incrementStorage(long usage) {
            fullDiskUsage = fullDiskUsage + usage;
            return fullDiskUsage;
        }
    }

    /**
     * Configure the VM property spec to retrieve snapshot information only if the query pool has one of the snapshot properties enabled
     * Works with VMwareConnector (legacy)
     *
     * @param vmQueryPool
     * @param pSpecVM this spec is modified, having queries added to it
     */
    public void configurePropertySpec(Map<String, BaseQuery> vmQueryPool, PropertySpec pSpecVM) {
        if (vmQueryPool.containsKey(SNAPSHOT_METRIC_COUNT) || vmQueryPool.containsKey(SNAPSHOT_METRIC_OLDEST) ||
            vmQueryPool.containsKey(SNAPSHOT_METRIC_ROOT_COUNT) || vmQueryPool.containsKey(SNAPSHOT_METRIC_CHILD_COUNT)) {
            pSpecVM.getPathSet().add(DP_TYPE_SNAPSHOT);
        }
        if (vmQueryPool.containsKey(SNAPSHOT_METRIC_DISKUSAGE)) {
            pSpecVM.getPathSet().add(DP_TYPE_SNAPSHOT_STORAGE);
        }
    }

    /**
     * Configure the VM collector spec to retrieve snapshot information only if the query pool has one of the snapshot properties enabled
     * Works with VMwareConnector (legacy)
     *
     * @param vmQueryPool
     * @param vmSpec this spec is modified, having queries added to it
     */
    public void configureCollectorSpec(Map<String, BaseQuery> vmQueryPool, PropertyCollectorSpec vmSpec) {
        if (vmQueryPool.containsKey(SNAPSHOT_METRIC_COUNT) || vmQueryPool.containsKey(SNAPSHOT_METRIC_OLDEST) ||
                vmQueryPool.containsKey(SNAPSHOT_METRIC_ROOT_COUNT) || vmQueryPool.containsKey(SNAPSHOT_METRIC_CHILD_COUNT)) {
            vmSpec.getMetrics().add(DP_TYPE_SNAPSHOT);
        }
        if (vmQueryPool.containsKey(SNAPSHOT_METRIC_DISKUSAGE)) {
            vmSpec.getMetrics().add(DP_TYPE_SNAPSHOT_STORAGE);
        }
    }

    /**
     * For a given VM, calculates snapshot counts and oldest snapshot in days
     *
     * @param vm
     * @param vmsi
     * @return
     */
    public SnapshotInfo calculateSnapshots(String vm, VirtualMachineSnapshotInfo vmsi) {
        SnapshotInfo traversalInfo = new SnapshotInfo(vm);
        for (VirtualMachineSnapshotTree root : vmsi.getRootSnapshotList()) {
            traversalInfo.incrementRootCount();
            traversalInfo.calculateOldest(root.getCreateTime().toGregorianCalendar());
            traverseSnapshotTree(traversalInfo, root.getChildSnapshotList());
        }
        return traversalInfo;
    }

    /**
     * Encapsulates the logic for updating of snapshot metrics into the VM's metric pool results
     *
     * @param vmQueryPool
     * @param customVmNames
     * @param vmSnapshot
     * @param vm
     */
    public void updateSnapshotMetrics(Map<String,BaseQuery> vmQueryPool, Map<String,String> customVmNames,
                                      SnapshotInfo vmSnapshot, VMwareVM vm) {
        String path = SnapshotService.SNAPSHOT_METRIC_COUNT;
        BaseQuery vbq = vmQueryPool.get(path);
        if (vbq != null) {
            BaseMetric vbm = new BaseMetric(
                    path,
                    vbq.getWarning(),
                    vbq.getCritical(),
                    vbq.isGraphed(),
                    vbq.isMonitored(),
                    customVmNames.get(path)
            );
            vbm.setValue(Integer.toString(vmSnapshot.getCount()));
            vm.putMetric(path, vbm);
        }
        path = SnapshotService.SNAPSHOT_METRIC_ROOT_COUNT;
        vbq = vmQueryPool.get(path);
        if (vbq != null) {
            BaseMetric vbm = new BaseMetric(
                    path,
                    vbq.getWarning(),
                    vbq.getCritical(),
                    vbq.isGraphed(),
                    vbq.isMonitored(),
                    customVmNames.get(path)
            );
            vbm.setValue(Integer.toString(vmSnapshot.getRootCount()));
            vm.putMetric(path, vbm);
        }
        path = SnapshotService.SNAPSHOT_METRIC_CHILD_COUNT;
        vbq = vmQueryPool.get(path);
        if (vbq != null) {
            BaseMetric vbm = new BaseMetric(
                    path,
                    vbq.getWarning(),
                    vbq.getCritical(),
                    vbq.isGraphed(),
                    vbq.isMonitored(),
                    customVmNames.get(path)
            );
            vbm.setValue(Integer.toString(vmSnapshot.getChildCount()));
            vm.putMetric(path, vbm);
        }
        path = SnapshotService.SNAPSHOT_METRIC_OLDEST;
        vbq = vmQueryPool.get(path);
        if (vbq != null) {
            BaseMetric vbm = new BaseMetric(
                    path,
                    vbq.getWarning(),
                    vbq.getCritical(),
                    vbq.isGraphed(),
                    vbq.isMonitored(),
                    customVmNames.get(path)
            );
            vbm.setValue(Integer.toString(vmSnapshot.getOldest()));
            vm.putMetric(path, vbm);
        }
    }

    /**
     * For a given VM, calculates snapshot storage usage
     *
     * @param vm
     * @param storageInfo
     * @return
     */
    public SnapshotStorageInfo calculateSnapshotStorage(String vm, VirtualMachineStorageInfo storageInfo) {
        SnapshotStorageInfo snapshotStorageInfo = new SnapshotStorageInfo(vm);
        if (storageInfo != null) {
            List<VirtualMachineUsageOnDatastore> s = storageInfo.getPerDatastoreUsage();
            for (VirtualMachineUsageOnDatastore vmu : s) {
                long mbValue = vmu.getCommitted() / 1000000L;
                snapshotStorageInfo.incrementStorage(mbValue); // + vmu.getUncommitted()
            }
        }
        return snapshotStorageInfo;
    }

    /**
     * Encapsulates the logic for updating of snapshot storage metrics into the VM's metric pool results
     *
     * @param vmQueryPool
     * @param customVmNames
     * @param vmSnapshot
     * @param vm
     */
    public void updateSnapshotStorageMetrics(Map<String,BaseQuery> vmQueryPool, Map<String,String> customVmNames,
                                      SnapshotStorageInfo vmSnapshot, VMwareVM vm) {
        String path = SnapshotService.SNAPSHOT_METRIC_DISKUSAGE;
        BaseQuery vbq = vmQueryPool.get(path);
        if (vbq != null) {
            BaseMetric vbm = new BaseMetric(
                    path,
                    vbq.getWarning(),
                    vbq.getCritical(),
                    vbq.isGraphed(),
                    vbq.isMonitored(),
                    customVmNames.get(path)
            );
            vbm.setValue(Long.toString(vmSnapshot.getFullDiskUsage()));
            vm.putMetric(path, vbm);
        }
    }

    private void traverseSnapshotTree(SnapshotInfo traversalInfo, List<VirtualMachineSnapshotTree> snapshotTrees) {
        for (VirtualMachineSnapshotTree child : snapshotTrees) {
            traversalInfo.incrementChildCount();
            traversalInfo.calculateOldest(child.getCreateTime().toGregorianCalendar());
            traverseSnapshotTree(traversalInfo, child.getChildSnapshotList());
        }
    }

}
