package org.groundwork.cloudhub.connectors;

import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.AbstractAgentTest;
import org.groundwork.cloudhub.ServerConfigurator;
import org.groundwork.cloudhub.configuration.OpenDaylightConfiguration;
import org.groundwork.cloudhub.configuration.ProfileServiceTest;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.InventoryContainerNode;
import org.groundwork.cloudhub.inventory.VirtualMachineNode;
import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.BaseVM;
import org.groundwork.cloudhub.metrics.MonitoringState;
import org.groundwork.cloudhub.monitor.MonitorAgentConfiguration;
import org.groundwork.cloudhub.profile.ProfileMetrics;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MonitorAgentConfiguration.class})
public class OpenDayLightTest extends AbstractAgentTest {

    @Test
    public void openDaylightInventoryBrowserTest() throws Exception {
        OpenDaylightConfiguration od = null;
        try {
            od = new OpenDaylightConfiguration();
            ServerConfigurator.setupOpenDaylightConnection(od.getConnection());
            ServerConfigurator.setupLocalGroundworkServer(od.getGwos());
            configurationService.saveConfiguration(od);
            MonitoringConnector connector = connectorFactory.getMonitoringConnector(od);
            ManagementConnector management = connectorFactory.getManagementConnector(od);
            connector.setCollectionMode(new CollectionMode(
                    true, // hosts
                    true, // VMs
                    false, // storage
                    false, // network
                    false, // resource pool
                    false, // clusters
                    false  // data centers
            ));
            connector.connect(od.getConnection());
            DataCenterInventory inventory = management.gatherInventory();
            assert inventory.getHypervisors().size() == 1;
            InventoryContainerNode hypervisor = inventory.getHypervisors().get(ServerConfigurator.OPEN_DAYLIGHT_SERVER_WITHOUT_PORT);
            assert hypervisor.getName().equals(ServerConfigurator.OPEN_DAYLIGHT_SERVER_WITHOUT_PORT);
            assert inventory.getVirtualMachines().size() >= 1;
            // look for known switches
            VirtualMachineNode boxSpy = inventory.getVirtualMachines().get("00:00:00:00:00:00:00:01");
            assert boxSpy.getSystemName().equals("00:00:00:00:00:00:00:01");
            assert boxSpy.getName().equals("00:00:00:00:00:00:00:01");
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
        finally {
            if (od != null)
                configurationService.deleteConfiguration(od);
        }
    }

    @Test
    public void openDaylightMetricsTest() throws Exception {
        OpenDaylightConfiguration config = null;
        try {
            config = new OpenDaylightConfiguration();
            ServerConfigurator.setupOpenDaylightConnection(config.getConnection());
            ServerConfigurator.setupLocalGroundworkServer(config.getGwos());
            configurationService.saveConfiguration(config);
            MonitoringConnector connector = connectorFactory.getMonitoringConnector(config);
            connector.connect(config.getConnection());
            ProfileServiceTest.copyTestProfile(VirtualSystem.OPENDAYLIGHT, ProfileServiceTest.TEST_AGENT);
            ProfileMetrics metrics = profileService.readMetrics(config.getCommon().getVirtualSystem(), ProfileServiceTest.TEST_AGENT);
            MonitoringState results = connector.collectMetrics(null, getPrimaryMetrics(metrics), getSecondaryMetrics(metrics), getCustomMetrics(metrics));
            assert results.hosts().size() == 1;
            BaseHost host = results.hosts().get(ServerConfigurator.OPEN_DAYLIGHT_SERVER_WITHOUT_PORT);
            assert host != null;
            assert host.getHostName().equals(ServerConfigurator.OPEN_DAYLIGHT_SERVER_WITHOUT_PORT);
            BaseVM vm = host.getVMPool().get("00:00:00:00:00:00:00:01");
            assert vm != null;
            assert vm.getVMName().equals("00:00:00:00:00:00:00:01");
            assert vm.getMetric("00:00:00:00:00:00:00:01-0-receiveBytes") != null;
            assert vm.getMetric("00:00:00:00:00:00:00:01-0-transmitBytes") != null;
            assert vm.getMetric("00:00:00:00:00:00:00:01-0-receiveErrors") != null;
            assert vm.getMetric("00:00:00:00:00:00:00:01-0-transmitErrors") != null;
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            if (config != null)
                configurationService.deleteConfiguration(config);
            profileService.removeProfile(VirtualSystem.OPENDAYLIGHT, ProfileServiceTest.TEST_AGENT);
        }
    }

}
