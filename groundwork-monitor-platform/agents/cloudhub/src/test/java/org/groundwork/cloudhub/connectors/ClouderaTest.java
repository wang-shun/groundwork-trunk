package org.groundwork.cloudhub.connectors;

import org.apache.commons.collections.map.HashedMap;
import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.AbstractAgentTest;
import org.groundwork.cloudhub.ServerConfigurator;
import org.groundwork.cloudhub.configuration.ClouderaConfiguration;
import org.groundwork.cloudhub.configuration.ProfileServiceTest;
import org.groundwork.cloudhub.configuration.SupportsExtendedViews;
import org.groundwork.cloudhub.connectors.cloudera.ClouderaConfigurationProvider;
import org.groundwork.cloudhub.connectors.cloudera.ClouderaConnector;
import org.groundwork.cloudhub.connectors.cloudera.ClouderaMetricCollector;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.BaseMetric;
import org.groundwork.cloudhub.metrics.BaseVM;
import org.groundwork.cloudhub.metrics.MonitoringState;
import org.groundwork.cloudhub.monitor.MonitorAgentConfiguration;
import org.groundwork.cloudhub.profile.ProfileMetrics;
import org.groundwork.cloudhub.synthetics.SyntheticContext;
import org.groundwork.cloudhub.synthetics.Synthetics;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MonitorAgentConfiguration.class})
public class ClouderaTest extends AbstractAgentTest {

    private static Logger log = Logger.getLogger(ClouderaTest.class);

    @Autowired
    protected Synthetics synthetics;

    @Test
    public void ClouderaInventoryTest() throws Exception {
        ClouderaConfiguration config = null;
        ManagementConnector managementConnector = null;
        try {
            config = new ClouderaConfiguration();
            ServerConfigurator.setupClouderaConnection(config.getConnection());
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
    public void ClouderaMetricsTest() throws Exception {
        ClouderaConfiguration config = null;
        MonitoringConnector connector = null;
        try {
            config = new ClouderaConfiguration();
            ServerConfigurator.setupClouderaConnection(config.getConnection());
            ServerConfigurator.setupLocalGroundworkServer(config.getGwos());
            configurationService.saveConfiguration(config);
            connector = connectorFactory.getMonitoringConnector(config);
            connector.connect(config.getConnection());
            CollectionMode mode = new CollectionMode(true, true, false, false, false, false, false);
            connector.setCollectionMode(mode);
            mode.setViews(((SupportsExtendedViews)config).getViews());
            ProfileServiceTest.copyTestProfile(VirtualSystem.CLOUDERA, ProfileServiceTest.TEST_AGENT);
            ProfileMetrics metrics = profileService.readMetrics(config.getCommon().getVirtualSystem(), ProfileServiceTest.TEST_AGENT);

            MonitoringState notOptimized = null;
            MonitoringState optimized = null;
            ClouderaMetricCollector.optimized = false;
            for (int ix = 0; ix < 1; ix++) {
                notOptimized = connector.collectMetrics(notOptimized, getPrimaryMetrics(metrics), getSecondaryMetrics(metrics), getCustomMetrics(metrics));
                // Hosts and Cluster Metrics
                assert notOptimized.hosts().size() > 0;
            }
            ClouderaMetricCollector.optimized = true;
            for (int ix = 0; ix < 1; ix++) {
                optimized = connector.collectMetrics(optimized, getPrimaryMetrics(metrics), getSecondaryMetrics(metrics), getCustomMetrics(metrics));
                // Hosts and Cluster Metrics
                assert optimized.hosts().size() > 0;
            }

            assert optimized.hosts().size() == notOptimized.hosts().size();

            for (BaseHost host1 : notOptimized.hosts().values()) {
                BaseHost host2 = optimized.hosts().get(host1.getHostName());

                assert host2.getVMPool().size() == host1.getVMPool().size();
                assert host2.getMetricPool().size() == host1.getMetricPool().size();

                // validate metrics
                for (String path : host1.getMetricPool().keySet()) {
                    BaseMetric metric1 = host1.getMetric(path);
                    BaseMetric metric2 = host2.getMetric(path);
                    ConnectorTestUtils.assertMetricsEqual("host = " + host1.getHostName(), metric1, metric2);
                    log.debug("-- metric " + metric1.getQuerySpec() + ": " + metric1.getCurrValue() + " === " + metric2.getCurrValue());
                }

                for (BaseVM vmFromPool2 : host2.getVMPool().values()) {
                    assertTrue("VM 1 key not found", host1.getVMPool().get(vmFromPool2.getVMName()) != null);
                }
                for (BaseVM vm1 : host1.getVMPool().values()) {
                    BaseVM vm2 = host2.getVMPool().get(vm1.getVMName());
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
                        ConnectorTestUtils.assertMetricsEqual("vm = " + vm1.getVMName(), metric1, metric2);
                        log.debug("-- metric " + metric1.getQuerySpec() + ": " + metric1.getCurrValue() + " === " + metric2.getCurrValue());
                    }


                }
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
            profileService.removeProfile(VirtualSystem.CLOUDERA, ProfileServiceTest.TEST_AGENT);
        }
    }

    @Test
    public void ClouderaTestConnectionTest() throws Exception {
        ClouderaConfiguration config = null;
        MonitoringConnector connector = null;
        try {
            config = new ClouderaConfiguration();
            ServerConfigurator.setupClouderaConnection(config.getConnection());
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
            profileService.removeProfile(VirtualSystem.CLOUDERA, ProfileServiceTest.TEST_AGENT);
        }
    }

    @Test
    public void ClouderaMetricsNameTest() throws Exception {
        ClouderaConfiguration config = null;
        ClouderaConnector connector = null;
        try {
            config = new ClouderaConfiguration();
            ServerConfigurator.setupClouderaConnection(config.getConnection());
            ServerConfigurator.setupLocalGroundworkServer(config.getGwos());
            configurationService.saveConfiguration(config);
            connector = (ClouderaConnector) connectorFactory.getMonitoringConnector(config);
            connector.testConnection(config.getConnection());
            List<String> names = connector.listMetricNames(ClouderaConfigurationProvider.CLOUDERA_SERVICE_HBASE, config);
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
            profileService.removeProfile(VirtualSystem.CLOUDERA, ProfileServiceTest.TEST_AGENT);
        }
    }

    @Test
    public void ClouderaEvaluateTest() throws Exception {
        Map<String,Object> contextObjects = new HashedMap();
        contextObjects.put("fd_open", 75.0);
        contextObjects.put("fd_max", 100.0);
        SyntheticContext context = synthetics.createContext(contextObjects);
        Number syntheticValue = synthetics.evaluate(context, "(fd_open / fd_max) * 100.0" );
        String result = synthetics.format(syntheticValue, "%.2f%%");
        System.out.println("-- synthetics evaluated to [" + result + "]");
        contextObjects.clear();
        contextObjects.put("physical_memory_used", 4750000000.0);
        context = synthetics.createContext(contextObjects);
        syntheticValue = synthetics.evaluate(context, "GW:GB2(physical_memory_used)" );
        result = synthetics.format(syntheticValue, "%.2f");
        System.out.println("-- synthetics evaluated to [" + result + "]");
    }

}

