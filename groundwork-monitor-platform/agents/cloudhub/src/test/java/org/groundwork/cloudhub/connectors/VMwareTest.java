package org.groundwork.cloudhub.connectors;

import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.AbstractAgentTest;
import org.groundwork.cloudhub.ServerConfigurator;
import org.groundwork.cloudhub.configuration.ProfileServiceTest;
import org.groundwork.cloudhub.configuration.VmwareConfiguration;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.InventoryContainerNode;
import org.groundwork.cloudhub.inventory.InventoryNode;
import org.groundwork.cloudhub.inventory.VirtualMachineNode;
import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.BaseVM;
import org.groundwork.cloudhub.metrics.MonitoringState;
import org.groundwork.cloudhub.monitor.MonitorAgentConfiguration;
import org.groundwork.cloudhub.profile.ProfileMetrics;
import org.groundwork.rs.dto.profiles.Metric;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MonitorAgentConfiguration.class})
public class VMwareTest extends AbstractAgentTest {

    @Test
    public void VMwareInventoryTest() throws Exception {
        VmwareConfiguration config = null;
        ManagementConnector managementConnector = null;
        try {
            config = new VmwareConfiguration();
            ServerConfigurator.setupVmwareVermontConnection(config.getConnection());
            ServerConfigurator.setupLocalGroundworkServer(config.getGwos());
            configurationService.saveConfiguration(config);
            managementConnector = connectorFactory.getManagementConnector(config);
            long start = System.currentTimeMillis();
            managementConnector.openConnection(config.getConnection());
            System.out.println("-- connection time: " + (System.currentTimeMillis() - start));
            managementConnector.setCollectionMode(new CollectionMode(true, true, true, true, true, false, false));
            start = System.currentTimeMillis();
            DataCenterInventory inventory = managementConnector.gatherInventory();
            System.out.println("-- inventory gather time: " + (System.currentTimeMillis() - start));

            inventory.debug();

            // Hypervisors
            assert inventory.getHypervisors().size() == 5;
            InventoryContainerNode hypervisor = inventory.getHypervisors().get("zurich.groundwork.groundworkopensource.com");
            assert hypervisor.getName().equals("zurich.groundwork.groundworkopensource.com");
            assert hypervisor.getVms().size() >= 25;
            // zurich VMs
            assert hypervisor.getVms().get("bern") != null;
//            assert hypervisor.getVms().get("qa-testsuite") != null;
            assert hypervisor.getVms().get("linuxchild") != null;

            // look for known VMs
            assert inventory.getVirtualMachines().size() > 100;
            VirtualMachineNode fedora = inventory.getVirtualMachines().get("eng-rh6-64");
            assert fedora != null;
            assert fedora.getName().equals("eng-rh6-64");
            assert fedora.getSystemName() != null;
            assert fedora.getName() != null;
            VirtualMachineNode win = inventory.getVirtualMachines().get("qa-testsuite");
            assert win != null;
            assert win.getName().equals("qa-testsuite");
            assert win.getSystemName() != null;
            assert win.getName() != null;
            VirtualMachineNode redhat = inventory.getVirtualMachines().get("eng-rhev-m-1");
            assert redhat != null;
            assert redhat.getName().equals("eng-rhev-m-1");
            assert redhat.getSystemName() != null;
            assert redhat.getName() != null;
            VirtualMachineNode centos = inventory.getVirtualMachines().get("eng-centos6-dev1");
            assert centos != null;
            assert centos.getName().equals("eng-centos6-dev1");
            assert centos.getSystemName() != null;
            assert centos.getName() != null;

            // look for known networks
            assert inventory.getNetworks().size() > 6;
            InventoryNode net = inventory.getNetworks().get("VLAN 112");
            assert net != null;
            assert net.getName().equals("VLAN 112");
            assert net.getName() != null;
            net = inventory.getNetworks().get("VM Network");
            assert net != null;
            assert net.getName().equals("VM Network");
            assert net.getName() != null;

            // look for known data stores
            assert inventory.getDatastores().size() > 12;
            InventoryNode ds = inventory.getDatastores().get("zurich-ds");
            assert ds != null;
            assert ds.getName().equals("zurich-ds");
            assert ds.getName() != null;
            ds = inventory.getDatastores().get("morges-ds");
            assert ds != null;
            assert ds.getName().equals("morges-ds");
            assert ds.getName() != null;

            // look for known resource pools
            assert inventory.getResourcePools().size() > 8;
            InventoryNode rp = inventory.getResourcePools().get("QA-Resources");
            assert rp != null;
            assert rp.getName().equals("QA-Resources");
            assert rp.getName() != null;
            rp = inventory.getResourcePools().get("Engineering");
            assert rp != null;
            assert rp.getName().equals("Engineering");
            assert rp.getName() != null;

            System.out.println("-- total inventory gather time: " + (System.currentTimeMillis() - start));

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            if (config != null) {
                configurationService.deleteConfiguration(config);
            }
            managementConnector.closeConnection();
        }
    }

    @Test
    public void VMwareMetricsTest() throws Exception {
        VmwareConfiguration config = null;
        MonitoringConnector connector = null;
        try {
            config = new VmwareConfiguration();
            ServerConfigurator.setupVmwareVermontConnection(config.getConnection());
            ServerConfigurator.setupLocalGroundworkServer(config.getGwos());
            configurationService.saveConfiguration(config);
            connector = connectorFactory.getMonitoringConnector(config);
            connector.connect(config.getConnection());
            connector.setCollectionMode(new CollectionMode(true, true, true, true, true, false, false));
            ProfileServiceTest.copyTestProfile(VirtualSystem.VMWARE, ProfileServiceTest.TEST_AGENT);
            ProfileMetrics metrics = profileService.readMetrics(config.getCommon().getVirtualSystem(), ProfileServiceTest.TEST_AGENT);
            for (Metric m : metrics.getSecondary()) {
                if (m.getName().equals("summary.runtime.memoryOverhead")) {
                    m.setMonitored(true);
                }
            }
            MonitoringState results = connector.collectMetrics(null, getPrimaryMetrics(metrics), getSecondaryMetrics(metrics), getCustomMetrics(metrics));

            // Hypervisor Metrics
            assert results.hosts().size() > 10;
            BaseHost host = results.hosts().get("wil.groundwork.groundworkopensource.com");
            assert host != null;
            assert host.getHostName().equals("wil.groundwork.groundworkopensource.com");
            assert host.getMetric("syn.host.cpu.used") != null;
            assert host.getMetric("syn.host.mem.used") != null;
            assert host.getRunState() != null;

            // VM Metrics
            BaseVM vm = host.getVMPool().get("qa-ubuntu-14-4-64-2");
            assert vm != null;
            assert vm.getRunState() != null;
            assert vm.getVMName().equals("qa-ubuntu-14-4-64-2");
            assert vm.getMetric("syn.vm.mem.balloonToConfigMemSize.used") != null;
            assert vm.getMetric("syn.vm.mem.compressedToConfigMemSize.used") != null;
            assert vm.getMetric("syn.vm.mem.sharedToConfigMemSize.used") != null;
            assert vm.getMetric("syn.vm.mem.swappedToConfigMemSize.used") != null;
            assert vm.getMetric("syn.vm.mem.guestToConfigMemSize.used") != null;
            assert vm.getMetric("syn.vm.cpu.cpuToMax.used") != null;
            vm = host.getVMPool().get("maloja");
            assert vm != null;
            assert vm.getRunState() != null;
            assert vm.getVMName().equals("maloja");
            assert vm.getMetric("syn.vm.mem.balloonToConfigMemSize.used") != null;
            assert vm.getMetric("syn.vm.mem.compressedToConfigMemSize.used") != null;
            assert vm.getMetric("syn.vm.mem.sharedToConfigMemSize.used") != null;
            assert vm.getMetric("syn.vm.mem.swappedToConfigMemSize.used") != null;
            assert vm.getMetric("syn.vm.mem.guestToConfigMemSize.used") != null;
            assert vm.getMetric("syn.vm.cpu.cpuToMax.used") != null;
            // snap shots
//            BaseVM primary = host.getVMPool().get("primary");
//            assert primary != null;
//            assert primary.getRunState() != null;
//            assert primary.getVMName().equals("primary");
//            assert primary.getMetric(VISnapshotService.SNAPSHOT_METRIC_COUNT) != null;
//            assert primary.getMetric(VISnapshotService.SNAPSHOT_METRIC_ROOT_COUNT) != null;
//            assert primary.getMetric(VISnapshotService.SNAPSHOT_METRIC_CHILD_COUNT) != null;
//            assert primary.getMetric(VISnapshotService.SNAPSHOT_METRIC_OLDEST) != null;
//            assert primary.getMetric(SnapshotService.SNAPSHOT_METRIC_DISKUSAGE) != null;

            // Data Store Metrics
            BaseHost storage  = results.hosts().get("STOR-datastore1");
            assert storage != null;
            assert storage.getRunState() != null;
            assert storage.getHostName().equals("STOR-datastore1");
            assert storage.getMetric("summary.uncommitted") != null;
            assert storage.getMetric("syn.storage.percent.used") != null;
            assert storage.getMetric("summary.freeSpace") != null;
            assert storage.getMetric("summary.capacity") != null;


        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            if (connector != null) {
                connector.disconnect();
            }
            if (config != null)
                configurationService.deleteConfiguration(config);
            profileService.removeProfile(VirtualSystem.VMWARE, ProfileServiceTest.TEST_AGENT);
        }
    }

}
