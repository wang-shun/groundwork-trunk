package org.groundwork.cloudhub.connectors;

import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.AbstractAgentTest;
import org.groundwork.cloudhub.ServerConfigurator;
import org.groundwork.cloudhub.configuration.NetAppConfiguration;
import org.groundwork.cloudhub.configuration.ProfileServiceTest;
import org.groundwork.cloudhub.gwos.GwosStatus;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.InventoryContainerNode;
import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.BaseMetric;
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
public class NetAppTest extends AbstractAgentTest {

    @Test
    public void NetAppInventoryTest() throws Exception {
        NetAppConfiguration config = null;
        try {
            config = new NetAppConfiguration();
            ServerConfigurator.setupNetAppConnection(config.getConnection());
            ServerConfigurator.setupLocalGroundworkServer(config.getGwos());
            configurationService.saveConfiguration(config);
            ManagementConnector managementConnector = connectorFactory.getManagementConnector(config);
            managementConnector.openConnection(config.getConnection());
            managementConnector.setCollectionMode(new CollectionMode(true, true, false, false, false, false, false));
            DataCenterInventory inventory = managementConnector.gatherInventory();

            // Verify Controller nodes
            assert inventory.getHypervisors().size() == 3;
            InventoryContainerNode colo1 = inventory.getHypervisors().get("gwos-netapp-colo-01");
            assert colo1.getName().equals("gwos-netapp-colo-01");
            assert colo1.getStatus().equals(BaseMetric.sUP);
            assert colo1.getVms().size() == 3;

            InventoryContainerNode colo2 = inventory.getHypervisors().get("gwos-netapp-colo-02");
            assert colo2.getName().equals("gwos-netapp-colo-02");
            assert colo2.getStatus().equals(BaseMetric.sUP);
            assert colo2.getVms().size() == 2;

            InventoryContainerNode vs1 = inventory.getHypervisors().get("vs1");
            assert vs1.getName().equals("vs1");
            assert vs1.getStatus().equals(BaseMetric.sUP);
            assert vs1.getVms().size() == 6;

            assert inventory.getVirtualMachines().size() == 10;

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            if (config != null)
                configurationService.deleteConfiguration(config);
        }
    }

    @Test
    public void NetAppMetricsTest() throws Exception {
        NetAppConfiguration config = null;
        try {
            config = new NetAppConfiguration();
            ServerConfigurator.setupNetAppConnection(config.getConnection());
            ServerConfigurator.setupLocalGroundworkServer(config.getGwos());
            configurationService.saveConfiguration(config);
            MonitoringConnector connector = connectorFactory.getMonitoringConnector(config);
            connector.connect(config.getConnection());
            connector.setCollectionMode(new CollectionMode(true, true, false, false, false, false, false));
            ProfileServiceTest.copyTestProfile(VirtualSystem.NETAPP, ProfileServiceTest.TEST_AGENT);
            ProfileMetrics metrics = profileService.readMetrics(config.getCommon().getVirtualSystem(), ProfileServiceTest.TEST_AGENT);
            MonitoringState results = connector.collectMetrics(null, getPrimaryMetrics(metrics), getSecondaryMetrics(metrics), getCustomMetrics(metrics));

            assert results.hosts().size() == 3;
            BaseHost host = results.hosts().get("vs1");
            assert host != null;
            assert host.getHostName().equals("vs1");
            assert host.getRunState().equals(GwosStatus.UP.status);

            assert host.getVMPool().size() == 6;
            BaseVM vm = host.getVMPool().get("netapp_eng_ds");
            assert vm != null;
            assert vm.getVMName().equals("netapp_eng_ds");
            assert vm.getMetricPool() != null;
            assert vm.getMetricPool().size() == 6;
            for (String query : TEST_VOLUME_QUERIES) {
                BaseMetric metric1 = vm.getMetricPool().get(query);
                assert metric1 != null;
                assert metric1.getCurrState().equals(BaseMetric.sOK);
                assert metric1.getCurrValue() != null;
                assert !metric1.getCurrValue().equals("0");
            }

            host = results.hosts().get("gwos-netapp-colo-01");
            assert host != null;
            assert host.getHostName().equals("gwos-netapp-colo-01");
            assert host.getRunState().equals(GwosStatus.UP.status);

            assert host.getVMPool().size() == 3;
            vm = host.getVMPool().get("sas_data_1");
            assert vm != null;
            assert vm.getVMName().equals("sas_data_1");
            assert vm.getMetricPool() != null;
            assert vm.getMetricPool().size() == 5;
            for (String query : TEST_AGGREGATE_QUERIES) {
                BaseMetric metric1 = vm.getMetricPool().get(query);
                assert metric1 != null;
                assert metric1.getCurrState().equals(BaseMetric.sOK);
                assert metric1.getCurrValue() != null;
                assert !metric1.getCurrValue().equals("0");
            }


        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            if (config != null)
                configurationService.deleteConfiguration(config);
            profileService.removeProfile(VirtualSystem.NETAPP, ProfileServiceTest.TEST_AGENT);
        }
    }

    private final static String[] TEST_VOLUME_QUERIES = {
            "volume-inode-attributes.files-used",
            "syn.volume.gb.used",
            "syn.volume.percent.files.used",
            "syn.volume.percent.bytes.used",
            "volume-space-attributes.percentage-size-used",
            "syn.volume.percent.bytes.used"
    };

    private final static String[] TEST_AGGREGATE_QUERIES = {
            "aggr-raid-attributes.disk-count",
            "aggr-volume-count-attributes.flexvol-count",
            "syn.aggregate.gb.used",
            "syn.aggregate.gb.available",
            "aggr-space-attributes.percent-used-capacity"
    };
}
