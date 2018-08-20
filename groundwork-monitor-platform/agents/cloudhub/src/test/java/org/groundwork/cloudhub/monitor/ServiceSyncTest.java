package org.groundwork.cloudhub.monitor;

import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.ServerConfigurator;
import org.groundwork.cloudhub.configuration.ConfigurationProvider;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.configuration.VmwareConfiguration;
import org.groundwork.cloudhub.connectors.amazon.AmazonConfigurationProvider;
import org.groundwork.cloudhub.connectors.openshift.OpenShiftConfigurationProvider;
import org.groundwork.cloudhub.connectors.vmware.VMwareConfigurationProvider;
import org.groundwork.cloudhub.gwos.GwosService;
import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.BaseVM;
import org.groundwork.cloudhub.metrics.MonitoringState;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static junit.framework.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MonitorAgentConfiguration.class})
public class ServiceSyncTest extends BaseMonitoringTest {

    @Resource
    private ServiceSynchronizer serviceSynchronizer;
    @Resource
    private VMwareConfigurationProvider vmwareConfigProvider;
    @Resource
    private OpenShiftConfigurationProvider openShiftConfigurationProvider;
    @Resource
    private AmazonConfigurationProvider amazonConfigurationProvider;

    private static Logger log = Logger.getLogger(ServiceSyncTest.class);

    private static final int CYCLES = 1;

    @Test
    public void syncServiceTest() throws Exception {
        log.info("Running Service Sync Test ....");
        assertNotNull(configurationService);
        assertNotNull(collectorService);
        ConnectionConfiguration config = configurationService.readConfiguration("/usr/local/groundwork/config/cloudhub/cloudhub-vmware-14.xml");
        CloudhubMonitorAgent agentClient = null;
        try {
            MonitoringState monitoredState = null;
            agentClient = collectorService.createMonitorAgent(config);
            agentClient.connect();
            for (int cycle = 1; cycle <= CYCLES; cycle++) {
                monitoredState = runAgentClientCycle2(String.format("cycle-%d-710", cycle), agentClient, monitoredState);
            }

            // simulate deletion
            for (BaseHost host : monitoredState.hosts().values()) {

//                if (host.getName().equals("STOR-oracle")) {
//                    log.info("removing STOR-oracle simulated summary.uncommitted");
//                    host.getMetricPool().remove("summary.uncommitted");
//                    host.getMetricPool().remove("summary.freeSpace");
//                }

                for (BaseVM vm : host.getVMPool().values()) {
                    if (vm.getName().equals("gw-logstash-02")) {
                        log.info("removing gw-logstash-02 simulated snapshots");
                        vm.getMetricPool().remove("snapshots.count");
                        vm.getMetricPool().remove("snapshots.rootCount");
                        vm.getMetricPool().remove("snapshots.childCount");
                        vm.getMetricPool().remove("snapshots.oldestInDays");
                    }
                }
            }

            ConfigurationProvider configurationProvider = connectorFactory.getConfigurationProvider(config.getCommon().getVirtualSystem());
            if (configurationProvider.isSynchronizeServicesEnabled()) {
                GwosService gwosService = factory.getGwosServicePrototype(config, (CloudhubAgentInfo) agentClient.getAgentInfo());
                serviceSynchronizer.sync(gwosService, monitoredState);
            }

            agentClient.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
        }
    }

    //@Test
    public void syncServiceFullTest() throws Exception {
        log.info("Running Service Sync Full Test ....");
        assertNotNull(configurationService);
        assertNotNull(collectorService);
        VmwareConfiguration config = configurationService.createConfiguration(VirtualSystem.VMWARE);
        config.getCommon().setDisplayName("Vermont");
        ServerConfigurator.setupLocalGroundworkServer(config.getGwos());
        ServerConfigurator.enableAllViews(config.getCommon());
        ServerConfigurator.setupVmwareVermontConnection(config.getConnection());
        configurationService.saveConfiguration(config);
        CloudhubMonitorAgent agentClient = null;
        try {
            statisticsService.setEnabled(true);
            MonitoringState monitoredState = null;
            agentClient = collectorService.createMonitorAgent(config);
            agentClient.connect();
            for (int cycle = 1; cycle <= CYCLES; cycle++) {
                monitoredState = runAgentClientCycle(String.format("cycle-%d-710", cycle), agentClient, monitoredState);
            }

            GwosService gwosService = factory.getGwosServicePrototype(config, (CloudhubAgentInfo)agentClient.getAgentInfo());
            ConfigurationProvider configurationProvider = connectorFactory.getConfigurationProvider(config.getCommon().getVirtualSystem());
            if (serviceSynchronizer.isEnabled(configurationProvider)) {
                serviceSynchronizer.sync(gwosService, monitoredState);
            }

            GwosService service = factory.getGwosServicePrototype(config, (CloudhubAgentInfo) agentClient.getAgentInfo());
            service.deleteByAgent(config, 1);

            agentClient.disconnect();

            statisticsService.save();

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            if (agentClient != null) {
                profileService.removeProfile(VirtualSystem.VMWARE, agentClient.getConfiguration().getCommon().getAgentId());
            }
            configurationService.deleteConfiguration(config);
        }
    }

    @Test
    public void initServiceTest() throws Exception {
         assert serviceSynchronizer != null;
         assert vmwareConfigProvider.isSynchronizeServicesEnabled() == true;
         assert openShiftConfigurationProvider.isSynchronizeServicesEnabled() == false;
         assert amazonConfigurationProvider.isSynchronizeServicesEnabled() == false;
    }

}
