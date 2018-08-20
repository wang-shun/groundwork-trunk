package org.groundwork.cloudhub.connectors;

import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.AbstractAgentTest;
import org.groundwork.cloudhub.ServerConfigurator;
import org.groundwork.cloudhub.configuration.AzureConfiguration;
import org.groundwork.cloudhub.configuration.ProfileServiceTest;
import org.groundwork.cloudhub.configuration.SupportsExtendedViews;
import org.groundwork.cloudhub.connectors.azure.AzureConfigurationProvider;
import org.groundwork.cloudhub.connectors.azure.AzureConnector;
import org.groundwork.cloudhub.connectors.azure.AzureHost;
import org.groundwork.cloudhub.connectors.azure.AzureVM;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.BaseMetric;
import org.groundwork.cloudhub.metrics.MonitoringState;
import org.groundwork.cloudhub.monitor.MonitorAgentConfiguration;
import org.groundwork.cloudhub.profile.ProfileMetrics;
import org.groundwork.cloudhub.synthetics.Synthetics;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Set;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MonitorAgentConfiguration.class})
public class AzureTest extends AbstractAgentTest {

    private static Logger log = Logger.getLogger(AzureTest.class);

    @Autowired
    protected Synthetics synthetics;

    @Test
    public void AzureInventoryTest() throws Exception {
        AzureConfiguration config = null;
        ManagementConnector managementConnector = null;
        try {
            config = new AzureConfiguration();
            ServerConfigurator.setupAzureConnection(config.getConnection());
            ServerConfigurator.setupLocalGroundworkServer(config.getGwos());
            configurationService.saveConfiguration(config);
            managementConnector = connectorFactory.getManagementConnector(config);
            long start = System.currentTimeMillis();
            managementConnector.openConnection(config.getConnection());
            System.out.println("-- connection time: " + (System.currentTimeMillis() - start));
            managementConnector.setCollectionMode(new CollectionMode(true, true, false, false, false, false, false));
            start = System.currentTimeMillis();
            DataCenterInventory inventory = managementConnector.gatherInventory();
            System.out.println("-- inventory gather time: " + (System.currentTimeMillis() - start));

            inventory.debug();
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
    public void AzureMetricsTest() throws Exception {
        AzureConfiguration config = null;
        MonitoringConnector connector = null;
        try {
            config = new AzureConfiguration();
            ServerConfigurator.setupAzureConnection(config.getConnection());
            ServerConfigurator.setupLocalGroundworkServer(config.getGwos());
            configurationService.saveConfiguration(config);
            connector = connectorFactory.getMonitoringConnector(config);
            connector.connect(config.getConnection());
            CollectionMode mode = new CollectionMode(true, true, false, false, false, false, false);
            connector.setCollectionMode(mode);
            mode.setViews(((SupportsExtendedViews)config).getViews());
            ProfileServiceTest.copyTestProfile(VirtualSystem.AZURE, ProfileServiceTest.TEST_AGENT);
            ProfileMetrics metrics = profileService.readMetrics(config.getCommon().getVirtualSystem(), ProfileServiceTest.TEST_AGENT);

            AzureConnector connector1 = (AzureConnector)connector;
            Set<String> services = connector1.listServices(config.getConnection());
            MonitoringState monitoringState = null;
            for (int ix = 0; ix < 1; ix++) {
                monitoringState = connector.collectMetrics(monitoringState, getPrimaryMetrics(metrics), getSecondaryMetrics(metrics), getCustomMetrics(metrics));
                // Hosts and Cluster Metrics
                assert monitoringState.hosts().size() > 0;

                String hostNameKey = "groundwork-dev";
                AzureHost host = (AzureHost)monitoringState.hosts().get(hostNameKey);
                assert host != null;

                //checkCosmoDbMetrics(host, "gwos-mongodb");
                checkCosmoDbMetrics(host, "gw-mongo");

                //checkVirtualMachineMetrics(host, "JCUbuntuVM");
                checkVirtualMachineMetrics(host, "ubuntu2");

//                checkStorageAccountMetrics(host, "gwazurediag285");
                //checkStorageAccountMetrics(host, "gwos01storageaccount");

                //checkSqlServerMetrics(host, "gwos02sqldb");

                //checkSqlDatabaseMetrics(host, "gwos02sqldb/gwos-testSQLDb");
                checkSqlDatabaseMetrics(host, "gw-sqlserver/gw-database1");

                //checkSitesMetrics(host, "gwosfuncapp");
                checkSitesMetrics(host, "gw-functiion-app1");

             //   checkSitesMetrics(host, "testgwoswebapp");
                checkSitesMetrics(host, "gw-webapp1");

            }

            for (BaseHost host1 : monitoringState.hosts().values()) {
                // TODO: implement
                //ConnectorTestUtils.assertMetricsEqual();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            if (connector != null) {
                connector.disconnect();
            }
            if (config != null)
                configurationService.deleteConfiguration(config);
            profileService.removeProfile(VirtualSystem.AZURE, ProfileServiceTest.TEST_AGENT);
        }
    }

    @Test
    public void AzureTestConnectionTest() throws Exception {
        AzureConfiguration config = null;
        MonitoringConnector connector = null;
        try {
            config = new AzureConfiguration();
            ServerConfigurator.setupAzureConnection(config.getConnection());
            ServerConfigurator.setupLocalGroundworkServer(config.getGwos());
            configurationService.saveConfiguration(config);
            connector = connectorFactory.getMonitoringConnector(config);
            connector.testConnection(config.getConnection());
            config.getConnection().setServer("localhost2");
            Exception expected = null;
            try {
                connector.testConnection(config.getConnection());
                Assert.assertTrue("should have thrown an exception", false);
            }
            catch (Exception ex) {
                expected = ex;
            }
            Assert.assertNotNull(expected);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            if (connector != null) {
                connector.disconnect();
            }
            if (config != null)
                configurationService.deleteConfiguration(config);
            profileService.removeProfile(VirtualSystem.AZURE, ProfileServiceTest.TEST_AGENT);
        }
    }

    @Test
    public void AzureMetricsNameTest() throws Exception {
        AzureConfiguration config = null;
        AzureConnector connector = null;
        try {
            config = new AzureConfiguration();
            ServerConfigurator.setupAzureConnection(config.getConnection());
            ServerConfigurator.setupLocalGroundworkServer(config.getGwos());
            configurationService.saveConfiguration(config);
            connector = (AzureConnector) connectorFactory.getMonitoringConnector(config);
            connector.testConnection(config.getConnection());
            List<String> names = connector.listMetricNames(AzureConfigurationProvider.AZURE_VIRTUAL_MACHINES, config);
            assert names.size() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            if (connector != null) {
                connector.disconnect();
            }
            if (config != null)
                configurationService.deleteConfiguration(config);
            profileService.removeProfile(VirtualSystem.AZURE, ProfileServiceTest.TEST_AGENT);
        }
    }

    @Test
    public void AzureMetricsPerformanceTest() throws Exception {
        AzureConfiguration config = null;
        MonitoringConnector connector = null;
        try {
            config = new AzureConfiguration();
            ServerConfigurator.setupAzureConnection(config.getConnection());
            ServerConfigurator.setupLocalGroundworkServer(config.getGwos());
            configurationService.saveConfiguration(config);
            connector = connectorFactory.getMonitoringConnector(config);
            connector.connect(config.getConnection());
            CollectionMode mode = new CollectionMode(true, true, false, false, false, false, false);
            connector.setCollectionMode(mode);
            mode.setViews(((SupportsExtendedViews)config).getViews());
            ProfileServiceTest.copyTestProfile(VirtualSystem.AZURE, ProfileServiceTest.TEST_AGENT);
            ProfileMetrics metrics = profileService.readMetrics(config.getCommon().getVirtualSystem(), ProfileServiceTest.TEST_AGENT);

            MonitoringState monitoringState = null;
            for (int ix = 0; ix < 5; ix++) {
                System.out.println("===== Performance Test : Batch " + ix);
                monitoringState = connector.collectMetrics(monitoringState, getPrimaryMetrics(metrics), getSecondaryMetrics(metrics), getCustomMetrics(metrics));

                // Hosts and Cluster Metrics
                assert monitoringState.hosts().size() > 0;

                String hostNameKey = "groundwork-dev";
                AzureHost host = (AzureHost)monitoringState.hosts().get(hostNameKey);
                assert host != null;

            }

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            if (connector != null) {
                connector.disconnect();
            }
            if (config != null)
                configurationService.deleteConfiguration(config);
            profileService.removeProfile(VirtualSystem.AZURE, ProfileServiceTest.TEST_AGENT);
        }
    }

    private void checkVirtualMachineMetrics(AzureHost host, String vmKey) {
        //vmKey = "JCUbuntuVM";
        AzureVM azureVM = (AzureVM)host.getVMPool().get(vmKey);
        assert azureVM != null;

        BaseMetric vmMetric = azureVM.getMetric("percentage_cpu");
        assert vmMetric != null;
        System.out.println("Service Name: " + vmMetric.getServiceName() + " - Percentage CPU Metric: " + vmMetric.getCurrValue());

        vmMetric = azureVM.getMetric("network_in");
        assert vmMetric != null;
        System.out.println("Service Name: " + vmMetric.getServiceName() + " - Network In Metric: " + vmMetric.getCurrValue());

        vmMetric = azureVM.getMetric("disk_read_bytes");
        assert vmMetric != null;
        System.out.println("Service Name: " + vmMetric.getServiceName() + " - Disk Read Bytes Metric: " + vmMetric.getCurrValue());

    }

    private void checkCosmoDbMetrics(AzureHost host, String vmKey) {
        //String vmKey = "gwos-mongodb";
        AzureVM azureVM = (AzureVM)host.getVMPool().get(vmKey);
        assert azureVM != null;

        BaseMetric mongoRequests = azureVM.getMetric("MongoRequests");
        assert mongoRequests != null;
        System.out.println("Service Name: " + mongoRequests.getServiceName() + " - Mongo Requests Metric: " + mongoRequests.getCurrValue());

        mongoRequests = azureVM.getMetric("TotalRequests");
        assert mongoRequests != null;
        System.out.println("Service Name: " + mongoRequests.getServiceName() + " - Total Requests Metric: " + mongoRequests.getCurrValue());

        mongoRequests = azureVM.getMetric("TotalRequestUnits");
        assert mongoRequests != null;
        System.out.println("Service Name: " + mongoRequests.getServiceName() + " - Total Requests Metric: " + mongoRequests.getCurrValue());

    }

    private void checkStorageAccountMetrics(AzureHost host, String vmKey) {
        AzureVM azureVM = (AzureVM)host.getVMPool().get(vmKey);
        assert azureVM != null;

        BaseMetric usedCapacity = azureVM.getMetric("UsedCapacity");
        assert usedCapacity != null;
        System.out.println("Service Name: " + usedCapacity.getServiceName() + " - Account used capacity: " + usedCapacity.getCurrValue());

    }

    private void checkSqlServerMetrics(AzureHost host, String vmKey) {
        AzureVM azureVM = (AzureVM)host.getVMPool().get(vmKey);
        assert azureVM != null;

        // TODO: Query metric failed currently...  Hence, dtuConsumptionPercent is null
        BaseMetric dtuConsumptionPercent = azureVM.getMetric("dtu_consumption_percent");
        assert dtuConsumptionPercent == null;
        //System.out.println("Service Name: " + dtuConsumptionPercent.getServiceName() + " - DTU Consumption Percent: " + dtuConsumptionPercent.getCurrValue());

    }

    private void checkSqlDatabaseMetrics(AzureHost host, String vmKey) {
        AzureVM azureVM = (AzureVM)host.getVMPool().get(vmKey);
        assert azureVM != null;

        BaseMetric cpuPercentage = azureVM.getMetric("cpu_percent");
        assert cpuPercentage != null;
        System.out.println("Service Name: " + cpuPercentage.getServiceName() + " - CPU Percentage: " + cpuPercentage.getCurrValue());

        BaseMetric connectionSucceed = azureVM.getMetric("connection_successful");
        assert connectionSucceed != null;
        System.out.println("Service Name: " + connectionSucceed.getServiceName() + " - Successful Connections: " + connectionSucceed.getCurrValue());
    }

    private void checkSitesMetrics(AzureHost host, String vmKey) {
        AzureVM azureVM = (AzureVM)host.getVMPool().get(vmKey);
        assert azureVM != null;

        BaseMetric bytesReceived = azureVM.getMetric("BytesReceived");
        assert bytesReceived != null;
        System.out.println("Service Name: " + bytesReceived.getServiceName() + " - Bytes received: " + bytesReceived.getCurrValue());

    }
}

