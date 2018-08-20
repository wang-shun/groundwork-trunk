package org.groundwork.cloudhub.connectors;

import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.AbstractAgentTest;
import org.groundwork.cloudhub.ServerConfigurator;
import org.groundwork.cloudhub.configuration.AmazonConfiguration;
import org.groundwork.cloudhub.configuration.ProfileServiceTest;
import org.groundwork.cloudhub.connectors.amazon.AmazonConfigurationProvider;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.InventoryContainerNode;
import org.groundwork.cloudhub.inventory.InventoryNode;
import org.groundwork.cloudhub.inventory.VirtualMachineNode;
import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.BaseVM;
import org.groundwork.cloudhub.metrics.ComputeType;
import org.groundwork.cloudhub.metrics.MonitoringState;
import org.groundwork.cloudhub.metrics.SourceType;
import org.groundwork.cloudhub.monitor.MonitorAgentConfiguration;
import org.groundwork.cloudhub.profile.ProfileMetrics;
import org.groundwork.rs.dto.profiles.Metric;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MonitorAgentConfiguration.class})
public class AmazonTest extends AbstractAgentTest {

    @Test
    public void AmazonInventoryTest() throws Exception {
        AmazonConfiguration config = null;
        try {
            config = new AmazonConfiguration();
            ServerConfigurator.setupAmazonConnection2(config.getConnection());
            ServerConfigurator.setupLocalGroundworkServer(config.getGwos());
            configurationService.saveConfiguration(config);
            ManagementConnector managementConnector = connectorFactory.getManagementConnector(config);
            managementConnector.openConnection(config.getConnection());
            managementConnector.setCollectionMode(new CollectionMode(true, true, true, true, true, false, false, true, "GWHostGroup", false, false));
            DataCenterInventory inventory = managementConnector.gatherInventory();

            // Availability Zones
            assert inventory.getHypervisors().size() == 3;
            InventoryContainerNode hypervisor = inventory.getHypervisors().get("us-west-2c");
            assert hypervisor.getName().equals("us-west-2c");
            assert hypervisor.getVms().size() >= 5;

            // EC2 instances
            int numFound = 0;
            for (VirtualMachineNode vm : hypervisor.getVms().values()) {
                if (vm.getName().endsWith("Windows-2008-IIS-SQL") ||
                        vm.getName().endsWith("SLES-12-01") ||
                        vm.getName().endsWith("groundwork-rds-instance")) {
                    ++numFound;
                }
            }
            assert numFound == 3;

            // Look for known instances
            assert inventory.getVirtualMachines().size() > 6;
            numFound = 0;
            for (VirtualMachineNode vm : inventory.getVirtualMachines().values()) {
                if (vm.getName().endsWith("Windows-2008-IIS-SQL") ||
                        vm.getName().endsWith("SLES-12-01") ||
                        vm.getName().endsWith("groundwork-rds-instance")) {
                    ++numFound;
                }
            }
            assert numFound == 3;

            // look for known networks
            assert inventory.getNetworks().size() > 0;
            InventoryNode net = inventory.getNetworks().get("vpc-471b9e22");
            assert net != null;
            assert net.getName().equals("vpc-471b9e22");

            assert inventory.getDatastores().size() >= 1;
            InventoryNode ds = inventory.getDatastores().get(AmazonConfigurationProvider.RDS_HOST_GROUP);
            assert ds != null;
            VirtualMachineNode rds = inventory.getDatastores().get(AmazonConfigurationProvider.RDS_HOST_GROUP).getVms().get("groundwork-rds-instance");
            assert rds != null;

            // look for known Tagged Groups
            assert inventory.getTaggedGroups().size() >= 1;
            InventoryContainerNode tg = inventory.getTaggedGroups().get("webservers");
            assert tg != null;
            assert tg.getName().equals("webservers");
            assert tg.getVms().size() >= 2;


        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            if (config != null)
                configurationService.deleteConfiguration(config);
        }
    }

    @Test
    public void AmazonMetricsTest() throws Exception {
        AmazonConfiguration config = null;
        try {
            config = new AmazonConfiguration();
            ServerConfigurator.setupAmazonConnection(config.getConnection());
            ServerConfigurator.setupLocalGroundworkServer(config.getGwos());
            configurationService.saveConfiguration(config);
            MonitoringConnector connector = connectorFactory.getMonitoringConnector(config);
            connector.connect(config.getConnection());
            connector.setCollectionMode(new CollectionMode(true, true, true, true, true, false, false, true, "GWHostGroup", true, false));
            ProfileServiceTest.copyTestProfile(VirtualSystem.AMAZON, ProfileServiceTest.TEST_AGENT);
            ProfileMetrics metrics = profileService.readMetrics(config.getCommon().getVirtualSystem(), ProfileServiceTest.TEST_AGENT);
            Metric metric = metrics.getPrimary().get(12);
            metric.setCustomName("override");
            System.out.println("-- metric Name: " + metric.getName() + ", " + metric.getServiceName());
            MonitoringState results = connector.collectMetrics(null, getPrimaryMetrics(metrics), getSecondaryMetrics(metrics), getCustomMetrics(metrics));

            // EBS DataStore (HostGroup) Metrics
            assert results.hosts().size() > 1;
            BaseHost host = results.hosts().get("us-west-2a");
            assert host != null;
            BaseVM ebs = host.getVM("Micro1");
            assert ebs.getVMName().equals("Micro1");
            assert ebs.getMetric("EBS.vol-d4bb4cc4-VolumeIdleTime") != null;  // NOTE: EBS volume must be in-use to generate these metrics.
            assert ebs.getMetric("EBS.vol-d4bb4cc4-VolumeQueueLength") != null;
            assert ebs.getRunState() != null;


            // EC2 Metrics
            int numFound = 0;
            for (BaseVM vm : host.getVMPool().values()) {
                if (vm.getVMName().endsWith("Micro1")) {
                    ++numFound;
                    assert vm.getMetric("EC2.NetworkIn") != null;  // NOTE: EC2 instance must be running to generate this metric.
                }
            }
            assert numFound == 1;

            // RDS Metrics
            host = results.hosts().get("us-west-2c");
            assert host != null;
            BaseVM vm = host.getVM("groundwork-rds-instance");
            assert vm != null;
            assert vm.getMetric("RDS.FreeStorageSpace") != null;  // NOTE: RDS instance must be running to generate this metric.

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            if (config != null)
                configurationService.deleteConfiguration(config);
            profileService.removeProfile(VirtualSystem.AMAZON, ProfileServiceTest.TEST_AGENT);
        }
    }

    @Test
    public void AmazonListCustomMetricsTest() throws Exception {
        AmazonConfiguration config = null;
        try {
            config = new AmazonConfiguration();
            ServerConfigurator.setupAmazonConnection(config.getConnection());
            ServerConfigurator.setupLocalGroundworkServer(config.getGwos());
            configurationService.saveConfiguration(config);
            MonitoringConnector connector = connectorFactory.getMonitoringConnector(config);
            connector.connect(config.getConnection());
            connector.setCollectionMode(new CollectionMode(true, true, true, true, true, false, false, true, "GWHostGroup", false, false));
            ProfileServiceTest.copyTestProfile(VirtualSystem.AMAZON, ProfileServiceTest.TEST_AGENT);
            //ProfileMetrics metrics = profileService.readMetrics(config.getCommon().getVirtualSystem(), ProfileServiceTest.TEST_AGENT);
            List<Metric> customMetrics = connector.retrieveCustomMetrics();

            assert (customMetrics.size() > 0);
            for (Metric custom : customMetrics) {
                assert custom.getSourceType().equals(SourceType.custom.name());
                assert custom.getComputeType().equals(ComputeType.query.name());
                assert !custom.getName().startsWith("AWS");
                System.out.println("**** custom: " + custom.getName());
            }

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            if (config != null)
                configurationService.deleteConfiguration(config);
            profileService.removeProfile(VirtualSystem.AMAZON, ProfileServiceTest.TEST_AGENT);
        }
    }

    @Test
    public void AmazonELBInventoryTest() throws Exception {
        AmazonConfiguration config = null;
        try {
            config = new AmazonConfiguration();
            ServerConfigurator.setupAmazonConnection2(config.getConnection());
            ServerConfigurator.setupLocalGroundworkServer(config.getGwos());
            configurationService.saveConfiguration(config);
            ManagementConnector managementConnector = connectorFactory.getManagementConnector(config);
            managementConnector.openConnection(config.getConnection());
            managementConnector.setCollectionMode(new CollectionMode(true, true, true, true, true, false, false, true, "GWHostGroup", false, false));
            DataCenterInventory inventory = managementConnector.gatherInventory();

            // ELB
            VirtualMachineNode elb = inventory.getVirtualMachines().get("elb-test");
            assert elb != null;
            assert inventory.getHypervisors().get("us-west-2b").getVms().get("elb-test") != null;
            assert inventory.getHypervisors().get("us-west-2a").getVms().get("elb-test") == null;

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            if (config != null)
                configurationService.deleteConfiguration(config);
        }
    }

    @Test
    public void AmazonELBMetricsTest() throws Exception {
        AmazonConfiguration config = null;
        try {
            config = new AmazonConfiguration();
            ServerConfigurator.setupAmazonConnection2(config.getConnection());
            ServerConfigurator.setupLocalGroundworkServer(config.getGwos());
            configurationService.saveConfiguration(config);
            MonitoringConnector connector = connectorFactory.getMonitoringConnector(config);
            connector.connect(config.getConnection());
            connector.setCollectionMode(new CollectionMode(true, true, true, true, true, false, false, true, "GWHostGroup", true, false));
            ProfileServiceTest.copyTestProfile(VirtualSystem.AMAZON, ProfileServiceTest.TEST_AGENT);
            ProfileMetrics metrics = profileService.readMetrics(config.getCommon().getVirtualSystem(), ProfileServiceTest.TEST_AGENT);
            MonitoringState results = connector.collectMetrics(null, getPrimaryMetrics(metrics), getSecondaryMetrics(metrics), getCustomMetrics(metrics));
            BaseHost host = results.hosts().get("us-west-2b");
            assert host != null;
            BaseVM elb = host.getVM("elb-test");
            assert elb.getVMName().equals("elb-test");
            assert elb.getMetric("ELB.HealthyHostCount") != null;
            assert elb.getMetric("ELB.RequestCount") != null;
            assert elb.getRunState() != null;

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            if (config != null)
                configurationService.deleteConfiguration(config);
            profileService.removeProfile(VirtualSystem.AMAZON, ProfileServiceTest.TEST_AGENT);
        }
    }
}