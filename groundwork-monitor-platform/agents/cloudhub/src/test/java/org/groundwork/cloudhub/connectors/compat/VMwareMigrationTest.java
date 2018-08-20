package org.groundwork.cloudhub.connectors.compat;

import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.AbstractAgentTest;
import org.groundwork.cloudhub.ServerConfigurator;
import org.groundwork.cloudhub.configuration.ProfileServiceTest;
import org.groundwork.cloudhub.configuration.VmwareConfiguration;
import org.groundwork.cloudhub.connectors.CollectionMode;
import org.groundwork.cloudhub.connectors.ConnectorConstants;
import org.groundwork.cloudhub.connectors.ManagementConnector;
import org.groundwork.cloudhub.connectors.MonitoringConnector;
import org.groundwork.cloudhub.connectors.vmware.VMwareConnector;
import org.groundwork.cloudhub.connectors.vmware.VMwareHost;
import org.groundwork.cloudhub.connectors.vmware.VMwareVM;
import org.groundwork.cloudhub.connectors.vmware.VmWareNetwork;
import org.groundwork.cloudhub.connectors.vmware.VmWareStorage;
import org.groundwork.cloudhub.connectors.vmware2.VmWareConnector2;
import org.groundwork.cloudhub.connectors.vmwarevi.VIConnector;
import org.groundwork.cloudhub.gwos.GWOSHost;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.InventoryContainerNode;
import org.groundwork.cloudhub.inventory.VirtualMachineNode;
import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.BaseMetric;
import org.groundwork.cloudhub.metrics.BaseVM;
import org.groundwork.cloudhub.metrics.MonitoringState;
import org.groundwork.cloudhub.monitor.MonitorAgentConfiguration;
import org.groundwork.cloudhub.profile.ProfileMetrics;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.SimpleDateFormat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * To be used for backward compatibility whenever core changes are made to the VmWare Connector
 * This test was initially used to test the migration to the VI and V2 connectors from the V1 connector
 * The VI connector was abandoned, but is mostly completed except for previousState processing. It is not ready for production
 * and has been abandoned due to DoubleCloud announcing discontinued support for the Open Source driver
 *
 * Recommend running these tests prior to checking any change to the behavior of the VM2 connector
 * Beware that tests sometimes fail due to metrics being updating on the VMSphere after running of the V1 cycle, but
 * prior to running the V2 cycle. Usually just re-running the test will pass
 *
 * Due to the fact that our VSphere environment is live, we cannot depend on integration tests to run consistently
 *
 * Also recommend running the MonitoringRegressionTest wheneveer changing any behavior to inventory or synchronizer components
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MonitorAgentConfiguration.class})
public class VMwareMigrationTest extends AbstractAgentTest {

    public int REPEAT_COMPARISONS = 1;
    private static Logger log = Logger.getLogger(VMwareMigrationTest.class);

    @Test
    public void compareInventory_VM1_VI() throws Exception {
        compareInventory(VMwareConnector.NAME, VIConnector.NAME);
    }

    @Test
    public void compareInventory_VM2_VI() throws Exception {
        compareInventory(VmWareConnector2.NAME, VIConnector.NAME);
    }

    public void compareInventory(String connectorName1, String connectorName2) throws Exception {
        VmwareConfiguration config = null;
        ManagementConnector vmware1 = null, vmware2 = null;
        try {
            config = new VmwareConfiguration();
            ServerConfigurator.setupVmwareVermontConnection(config.getConnection());
            ServerConfigurator.setupLocalGroundworkServer(config.getGwos());
            configurationService.saveConfiguration(config);

            // Gather inventory for legacy connector
            vmware1 = connectorFactory.getManagementConnector(config, connectorName1);
            vmware1.openConnection(config.getConnection());
            vmware1.setCollectionMode(new CollectionMode(true, true, true, true, true, false, false));
            long start = System.currentTimeMillis();
            DataCenterInventory inventory1 = vmware1.gatherInventory();
            System.out.println("-- VMWare1 inventory gather time: " + (System.currentTimeMillis() - start));
            vmware1.closeConnection();

            // Gather inventory for new vmware connector
            vmware2 = connectorFactory.getManagementConnector(config, connectorName2);
            vmware2.openConnection(config.getConnection());
            vmware2.setCollectionMode(new CollectionMode(true, true, true, true, true, false, false));
            start = System.currentTimeMillis();
            DataCenterInventory inventory2 = vmware2.gatherInventory();
            System.out.println("-- VMWare2 inventory gather time: " + (System.currentTimeMillis() - start));
            vmware2.closeConnection();

            // Compare hypervisor inventories
            assertEquals("hypervisors count differ", inventory1.getHypervisors().size(), inventory2.getHypervisors().size());
            for (String key1 : inventory1.getHypervisors().keySet()) {
                assertNotNull("hypervisor keys dont match", inventory2.getHypervisors().get(key1));
            }
            for (InventoryContainerNode node1 : inventory1.getHypervisors().values()) {
                InventoryContainerNode node2 = inventory2.getHypervisors().get(node1.getName());
                assertContainerNodesEqual(node1, node2);
                for (String key : node1.getVms().keySet()) {
                    VirtualMachineNode vm1 = node1.getVms().get(key);
                    VirtualMachineNode vm2 = node2.getVms().get(key);
                    assertVirtualMachineNodesEqual(vm1, vm2);
                }
            }

            // Compare hosts inventories
            assertEquals("hosts count differ", inventory1.getAllHosts().size(), inventory2.getAllHosts().size());
            for (String key1 : inventory1.getAllHosts().keySet()) {
                assertNotNull("host keys dont match", inventory2.getAllHosts().get(key1));
            }
            for (GWOSHost node1 : inventory1.getAllHosts().values()) {
                GWOSHost node2 = inventory2.getAllHosts().get(node1.getHostName());
                assertEquals("host names differ", node1.getHostName(), node2.getHostName());
                assertEquals("agent names differ", node1.getAgentId(), node2.getAgentId());
                assertEquals("app types names differ", node1.getAppType(), node2.getAppType());
            }

            // Compare VM inventories
            assertEquals("VM count differ", inventory1.getVirtualMachines().size(), inventory2.getVirtualMachines().size());
            for (String key1 : inventory1.getVirtualMachines().keySet()) {
                assertNotNull("VM keys dont match", inventory2.getVirtualMachines().get(key1));
            }
            for (VirtualMachineNode node1 : inventory1.getVirtualMachines().values()) {
                VirtualMachineNode node2 = inventory2.getVirtualMachines().get(node1.getName());
                assertVirtualMachineNodesEqual(node1, node2);
            }

            // Compare datastore inventories
            assertEquals("datastores count differ", inventory1.getDatastores().size(), inventory2.getDatastores().size());
            for (String key1 : inventory1.getDatastores().keySet()) {
                assertNotNull("datastore keys dont match", inventory2.getDatastores().get(key1));
            }
            for (InventoryContainerNode node1 : inventory1.getDatastores().values()) {
                InventoryContainerNode node2 = inventory2.getDatastores().get(node1.getName());
                assertContainerNodesEqual(node1, node2);
                for (String key : node1.getVms().keySet()) {
                    VirtualMachineNode vm1 = node1.getVms().get(key);
                    VirtualMachineNode vm2 = node2.getVms().get(key);
                    assertVirtualMachineNodesEqual(vm1, vm2);
                }
            }

            // Compare network inventories
            assertEquals("network count differ", inventory1.getNetworks().size(), inventory2.getNetworks().size());
            for (String key1 : inventory1.getNetworks().keySet()) {
                assertNotNull("networks keys dont match", inventory2.getNetworks().get(key1));
            }
            for (InventoryContainerNode node1 : inventory1.getNetworks().values()) {
                InventoryContainerNode node2 = inventory2.getNetworks().get(node1.getName());
                assertContainerNodesEqual(node1, node2);
                for (String key : node1.getVms().keySet()) {
                    VirtualMachineNode vm1 = node1.getVms().get(key);
                    VirtualMachineNode vm2 = node2.getVms().get(key);
                    assertVirtualMachineNodesEqual(vm1, vm2);
                }
            }

            // Compare resource pools inventories
            assertEquals("resource pools count differ", inventory1.getResourcePools().size(), inventory2.getResourcePools().size());
            for (String key1 : inventory1.getResourcePools().keySet()) {
                assertNotNull("resource pools keys dont match", inventory2.getResourcePools().get(key1));
            }
            for (InventoryContainerNode node1 : inventory1.getResourcePools().values()) {
                InventoryContainerNode node2 = inventory2.getResourcePools().get(node1.getName());
                assertContainerNodesEqual(node1, node2);
                for (String key : node1.getVms().keySet()) {
                    VirtualMachineNode vm1 = node1.getVms().get(key);
                    VirtualMachineNode vm2 = node2.getVms().get(key);
                    assertNotNull("vm2 should not be null: " + node1.getName() + ":" + key, vm2);
                    assertVirtualMachineNodesEqual(vm1, vm2);
                }
            }

            // Compare system name maps
            assertEquals("System name map count differ", inventory1.getSystemNameMap().size(), inventory2.getSystemNameMap().size());
            for (String key1 : inventory1.getSystemNameMap().keySet()) {
                assertEquals("System map values differ", inventory1.getSystemNameMap().get(key1), inventory2.getSystemNameMap().get(key1));
            }

            // Compare tagged groups inventories
            assertEquals("tagged groups count differ", inventory1.getTaggedGroups().size(), inventory2.getTaggedGroups().size());
            for (String key1 : inventory1.getTaggedGroups().keySet()) {
                assertNotNull("tagged groups keys dont match", inventory2.getTaggedGroups().get(key1));
            }
            for (InventoryContainerNode node1 : inventory1.getTaggedGroups().values()) {
                InventoryContainerNode node2 = inventory2.getTaggedGroups().get(node1.getName());
                assertContainerNodesEqual(node1, node2);
                for (String key : node1.getVms().keySet()) {
                    VirtualMachineNode vm1 = node1.getVms().get(key);
                    VirtualMachineNode vm2 = node2.getVms().get(key);
                    assertVirtualMachineNodesEqual(vm1, vm2);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            if (config != null) {
                configurationService.deleteConfiguration(config);
            }
        }
    }

    private void assertContainerNodesEqual(InventoryContainerNode node1, InventoryContainerNode node2) {
        assertNotNull("node2 should not be null: " + node1.getName(), node2);
        assertEquals("node names differ", node1.getName(), node2.getName());
        assertEquals("node prefixed names differ", node1.getPrefixedName(), node2.getPrefixedName());
        assertEquals("node status differ", node1.getStatus(), node2.getStatus());
        assertEquals("node transient states differ", node1.isTransient(), node2.isTransient());
    }

    private void assertVirtualMachineNodesEqual(VirtualMachineNode vm1, VirtualMachineNode vm2) {
        assertNotNull("vm node2 should not be null: " + vm1.getName(), vm2);
        assertEquals("node names differ", vm1.getName(), vm2.getName());
        assertEquals("node prefixed names differ", vm1.getSystemName(), vm2.getSystemName());
        assertEquals("node status differ", vm1.getStatus(), vm2.getStatus());
    }


    @Test
    public void compareMetric_V1_VI() throws Exception {
        // TODO: V1 has not implement previousState and is thus not ready for production
        REPEAT_COMPARISONS = 0;
        compareMetrics(VMwareConnector.NAME, VIConnector.NAME);
    }

    @Test
    public void compareMetrics_VM1_VM2() throws Exception {
        compareMetrics(VMwareConnector.NAME, VmWareConnector2.NAME);
    }


    public void compareMetrics(String connectorName1, String connectorName2) throws Exception {
        VmwareConfiguration config = null;
        MonitoringConnector vmware1 = null, vmware2 = null;
        boolean doStorage = true, doNetworks = true, doResourcePools = false;

        try {
            config = new VmwareConfiguration();
            ServerConfigurator.setupVmwareVermontConnection(config.getConnection());
            ServerConfigurator.setupLocalGroundworkServer(config.getGwos());
            configurationService.saveConfiguration(config);
            vmware1 = connectorFactory.getMonitoringConnector(config, connectorName1);
            long startup = System.currentTimeMillis();
            vmware1.connect(config.getConnection());
            System.out.println("connect time 1 = " + (System.currentTimeMillis() - startup));
            vmware1.setCollectionMode(new CollectionMode(true, true, doStorage, doNetworks, doResourcePools, false, false));
            ProfileServiceTest.copyTestProfile(VirtualSystem.VMWARE, ProfileServiceTest.TEST_AGENT);
            ProfileMetrics metrics = profileService.readMetrics(config.getCommon().getVirtualSystem(), ProfileServiceTest.TEST_AGENT);

            // Gather Metrics for OLD VMware connector
            long start = System.currentTimeMillis();
            MonitoringState results1 = vmware1.collectMetrics(null, getPrimaryMetrics(metrics), getSecondaryMetrics(metrics), getCustomMetrics(metrics));
            System.out.println("-- VMWare1 METRICS gather time: " + (System.currentTimeMillis() - start));

            // Gather Metrics for NEW VMware connector
            vmware2 = connectorFactory.getMonitoringConnector(config, connectorName2);
            startup = System.currentTimeMillis();
            vmware2.connect(config.getConnection());
            System.out.println("connect time 2 = " + (System.currentTimeMillis() - startup));
            vmware2.setCollectionMode(new CollectionMode(true, true, doStorage, doNetworks, doResourcePools, false, false));
            start = System.currentTimeMillis();
            MonitoringState results2 = vmware2.collectMetrics(null, getPrimaryMetrics(metrics), getSecondaryMetrics(metrics), getCustomMetrics(metrics));
            System.out.println("-- VMWare2 METRICS gather time: " + (System.currentTimeMillis() - start));

            compareTwoConnectors(results1, results2);

            // repeat comparisons with prior metrics
            for (int ix = 0; ix < REPEAT_COMPARISONS; ix++) {

                start = System.currentTimeMillis();
                results1 = vmware1.collectMetrics(results1, getPrimaryMetrics(metrics), getSecondaryMetrics(metrics), getCustomMetrics(metrics));
                System.out.println("== VMWare1 METRICS gather time: " + (System.currentTimeMillis() - start));

                start = System.currentTimeMillis();
                results2 = vmware2.collectMetrics(results2, getPrimaryMetrics(metrics), getSecondaryMetrics(metrics), getCustomMetrics(metrics));
                System.out.println("== VMWare2 METRICS gather time: " + (System.currentTimeMillis() - start));

                compareTwoConnectors(results1, results2);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            if (config != null) {
                configurationService.deleteConfiguration(config);
                profileService.removeProfile(VirtualSystem.VMWARE, ProfileServiceTest.TEST_AGENT);
            }
            if (vmware1 != null) {
                vmware1.disconnect();
            }
            if (vmware2 != null) {
                vmware2.disconnect();
            }
        }
    }

    private void compareTwoConnectors(MonitoringState results1, MonitoringState results2) throws Exception {
        // Compare host counts
        assertEquals("Hosts count differ", results1.hosts().size(), results2.hosts().size());
        for (String hostName : results1.hosts().keySet()) {
            assertTrue("host key not found", results2.hosts().get(hostName) != null);
        }
        for (BaseHost host : results1.hosts().values()) {
            VMwareHost host1 = (VMwareHost)host;
            VMwareHost host2 = (VMwareHost)results2.hosts().get(host.getHostName());

            if (host2 instanceof VmWareStorage) {
                assertEquals("storage name", host1.getHostName(), host2.getHostName());
                //assertEquals("storage system name", host1.getSystemName(), host2.getSystemName());
                assertEquals("storage description", host1.getDescription(), host2.getDescription());
                assertEquals("storage current run state: " + host1.getHostName(), host1.getRunState(), host2.getRunState());
                assertEquals("storage extra run state", host1.getRunExtra(), host2.getRunExtra());
            }
            else if (host2 instanceof VmWareNetwork) {
                assertEquals("network name", host1.getHostName(), host2.getHostName());
                //assertEquals("network system name", host1.getSystemName(), host2.getSystemName());
                assertEquals("network description", host1.getDescription(), host2.getDescription());
                assertEquals("network current run state", host1.getRunState(), host2.getRunState());
                assertEquals("network extra run state", host1.getRunExtra(), host2.getRunExtra());
            }
            else {
                assertEquals("host boot date", host1.getBootDate(), host2.getBootDate());
                assertEquals("host boot date ms", host1.getBootDateMillisec(), host2.getBootDateMillisec());
                assertDatesWithinRange("host last update", 10, host1.getLastUpdate(), host2.getLastUpdate());
                assertTimeStampsWithinRange("host last update ms", 10, host1.getLastUpdateMillisec(), host2.getLastUpdateMillisec());

                // assert host attributes
                assertEquals("host name", host1.getHostName(), host2.getHostName());
                assertEquals("host system name", host1.getSystemName(), host2.getSystemName());
                assertEquals("host description", host1.getDescription(), host2.getDescription());
                assertEquals("host current run state", host1.getRunState(), host2.getRunState());
                assertEquals("host extra run state", host1.getRunExtra(), host2.getRunExtra());
                // Due to old merge algorithm not setting initial value to "PENDING", commenting out for now
                assertEquals("host previous run state", host1.getPrevRunState(), host2.getPrevRunState());
            }

            // assert Host Metrics
            assertEquals("Host Metric count differs for " + host1.getHostName(),
                    host1.getMetricPool().size(), host2.getMetricPool().size()) ;
            for (String path : host1.getMetricPool().keySet()) {
                BaseMetric metric1 = host1.getMetric(path);
                BaseMetric metric2 = host2.getMetric(path);
                assertMetricsEqual("host = " + host1.getHostName(), metric1, metric2);
                //log.debug("-- metric " + metric1.getQuerySpec() + ": " + metric1.getCurrValue() + " === " + metric2.getCurrValue());
            }

            // assert Host Configs
            assertEquals("Host Config count differs for " + host1.getHostName(),
                    host1.getConfigPool().size(), host2.getConfigPool().size()) ;

            // Host VMs
            assertEquals("VM count differs for " + host1.getHostName(), host1.getVMPool().size(), host2.getVMPool().size());

            for (BaseVM vmFromPool2 : host2.getVMPool().values()) {
                assertTrue("VM 1 key not found", host1.getVMPool().get(vmFromPool2.getVMName()) != null);
            }
            for (BaseVM vm : host1.getVMPool().values()) {
                VMwareVM vm1 = (VMwareVM)vm;
                VMwareVM vm2 = (VMwareVM)host2.getVMPool().get(vm1.getVMName());
                assertEquals("vm name", vm1.getVMName(), vm2.getVMName());
                // OLD CONNECTOR does not track system name: assertEquals("vm system name", vm1.getSystemName(), vm2.getSystemName());
                assertEquals("vm hypervisor name", vm1.getHypervisor(), vm2.getHypervisor());
                assertEquals("vm mac address", vm1.getMacAddress(), vm2.getMacAddress());
                assertEquals("vm ip address", vm1.getIpAddress(), vm2.getIpAddress());
                assertEquals("vm guest state", vm1.getGuestState(), vm2.getGuestState());
                assertEquals("vm current run state", vm1.getRunState(), vm2.getRunState());
                assertEquals("vm extra run state", vm1.getRunExtra(), vm2.getRunExtra());
                // due to old merge algorithm not setting initial value to "PENDING", commenting out for now
                assertEquals("vm previous run state", vm1.getPrevRunState(), vm2.getPrevRunState());

                assertEquals("VM metric count differs for " + host1.getHostName() + ":" + vm1.getVMName(),
                        vm1.getMetricPool().size(), vm2.getMetricPool().size()) ;
                for (String path : vm1.getMetricPool().keySet()) {
                    BaseMetric metric1 = vm1.getMetric(path);
                    BaseMetric metric2 = vm2.getMetric(path);
                    assertMetricsEqual("vm = " + vm1.getVMName(), metric1, metric2);
                    //log.debug("-- metric " + metric1.getQuerySpec() + ": " + metric1.getCurrValue() + " === " + metric2.getCurrValue());
                }
            }
        }
        // Compare Events
        assertEquals("Events count differ", results1.events().size(), results2.events().size());

    }

    @Test
    public void developMetricsVIConnector() throws Exception {
        developMetricsOverride(VIConnector.NAME);
    }

    @Test
    public void developMetricsConnector2() throws Exception {
        developMetricsOverride(VmWareConnector2.NAME);
    }

    @Test
    public void developMetricsConnector1() throws Exception {
        developMetricsOverride(VMwareConnector.NAME);
    }

    public void developMetricsOverride(String connectorName) throws Exception {
        VmwareConfiguration config = null;
        MonitoringConnector vmware = null;
        try {
            config = new VmwareConfiguration();
            ServerConfigurator.setupVmwareVermontConnection(config.getConnection());
            ServerConfigurator.setupLocalGroundworkServer(config.getGwos());
            configurationService.saveConfiguration(config);
            vmware = connectorFactory.getMonitoringConnector(config, connectorName);
            vmware.connect(config.getConnection());
            vmware.setCollectionMode(new CollectionMode(true, true, true, true, true, false, false));
            ProfileServiceTest.copyTestProfile(VirtualSystem.VMWARE, ProfileServiceTest.TEST_AGENT);
            ProfileMetrics metrics = profileService.readMetrics(config.getCommon().getVirtualSystem(), ProfileServiceTest.TEST_AGENT);
            long start = System.currentTimeMillis();
            MonitoringState results = vmware.collectMetrics(null, getPrimaryMetrics(metrics, false), getSecondaryMetrics(metrics, false), getCustomMetrics(metrics, false));
            System.out.println("-- VMWare METRICS gather time: " + (System.currentTimeMillis() - start));
            vmware.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            if (config != null) {
                configurationService.deleteConfiguration(config);
                profileService.removeProfile(VirtualSystem.VMWARE, ProfileServiceTest.TEST_AGENT);
            }
        }
    }

    private void assertMetricsEqual(String baseMessage, BaseMetric metric1, BaseMetric metric2) throws Exception {
        String message = baseMessage + " " + metric1.getQuerySpec();
        assertEquals(message + ": currState: ", metric1.getCurrState(), metric2.getCurrState());
        assertEquals(message + ": state extra: ", metric1.getCurrStateExtra(), metric2.getCurrStateExtra());
        assertEquals(message + ": currentValue: ", metric1.getCurrValue(), metric2.getCurrValue());
        if (metric1.getCustomName() != null)
            assertEquals(message + ": customName: ", metric1.getCustomName(), metric2.getCustomName());
        else
            assertEquals(message + ": customName: ", metric2.getCustomName(), "");
        //if (metric1.getLastState() == null && metric2.getLastState() != null && metric2.getLastState().equals(sPending))
        assertEquals(message + ": lastState: ", metric1.getLastState(), metric2.getLastState());
        assertEquals(message + ": lastValue: ", metric1.getLastValue(), metric2.getLastValue());
        assertEquals(message + ": querySpec: ", metric1.getQuerySpec(), metric2.getQuerySpec());
        assertEquals(message + ": isCritical: ", metric1.isCritical(), metric2.isCritical());
        assertEquals(message + ": isWarning: ", metric1.isWarning(), metric2.isWarning());
        assertEquals(message + ": isMonitored: ", metric1.isMonitored(), metric2.isMonitored());
        assertEquals(message + ": isGraphed: ", metric1.isGraphed(), metric2.isGraphed());
        assertEquals(message + ": isStateChange: ", metric1.isStateChange(), metric2.isStateChange());
        assertEquals(message + ": isValueChange: ", metric1.isValueChange(), metric2.isValueChange());
        assertEquals(message + ": threshold critical: ", metric1.getThresholdCritical(), metric2.getThresholdCritical());
        assertEquals(message + ": threshold warning: ", metric1.getThresholdWarning(), metric2.getThresholdWarning());
        assertEquals(message + ": metricType: ", metric1.getMetricType(), metric2.getMetricType());
        assertEquals(message + ": serviceName: ", metric1.getServiceName(), metric2.getServiceName());
    }

    private void assertDatesWithinRange(String message, int diffInMinutes, String dt1, String dt2) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(ConnectorConstants.gwosDateFormat);
        Duration duration = new Duration(new DateTime(sdf.parse(dt1)), new DateTime(sdf.parse(dt2)));
        assertTrue(message, duration.getStandardMinutes() < diffInMinutes);
    }

    private void assertTimeStampsWithinRange(String message, int diffInMinutes, long timestamp1, long timestamp2) {
        Duration duration = new Duration(timestamp1, timestamp2);
        assertTrue(message, duration.getStandardMinutes() < diffInMinutes);
    }
}

