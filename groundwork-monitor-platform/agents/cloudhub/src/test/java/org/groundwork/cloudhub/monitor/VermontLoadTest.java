package org.groundwork.cloudhub.monitor;

import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.ServerConfigurator;
import org.groundwork.cloudhub.configuration.VmwareConfiguration;
import org.groundwork.cloudhub.metrics.MonitoringState;
import org.groundwork.cloudhub.gwos.GwosService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static junit.framework.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MonitorAgentConfiguration.class})
public class VermontLoadTest extends BaseMonitoringTest {

    private static Logger log = Logger.getLogger(VermontLoadTest.class);

    private static final int CYCLES = 5;

    /**
     * Recommend running this command to reset database before running the load test:
     *   cd enterprise-foundation/collage/database
     *   mvn -P production -DskipTests=false test
     * @throws Exception
     */
    @Test
    public void vermontLoadTest() throws Exception {
        log.info("Running Vermont Load Test ....");
        assertNotNull(configurationService);
        assertNotNull(collectorService);
        VmwareConfiguration config = configurationService.createConfiguration(VirtualSystem.VMWARE);
        config.getCommon().setDisplayName("Vermont");
        ServerConfigurator.setupLocalGroundworkServer(config.getGwos());
        ServerConfigurator.enableAllViews(config.getCommon());
        ServerConfigurator.setupVmwareVermontConnection(config.getConnection());
        configurationService.saveConfiguration(config);
        try {
            statisticsService.setEnabled(true);
            MonitoringState monitoredState = null;
            // run without Biz Service
            log.info("Running with 710 Service (not biz) ...");
            CloudhubMonitorAgent agentClient = collectorService.createMonitorAgent(config);
            agentClient.connect();
            for (int cycle = 1; cycle <= CYCLES; cycle++) {
                monitoredState = runAgentClientCycle(String.format("cycle-%d-710", cycle), agentClient, monitoredState);
            }
            log.info("... running with 710 Service (not biz) completed.");

            // reset data
            log.info("Deleting all data for agent ...");
            GwosService service = factory.getGwosServicePrototype(config, (CloudhubAgentInfo) agentClient.getAgentInfo());
            service.deleteByAgent(config, 1);
            agentClient.disconnect();
            log.info("... deleting all data for agent completed.");

            // run with Biz Service
            log.info("Running with Biz Services...");
            factory.setUseBiz(true);
            monitoredState = null;
            agentClient = collectorService.createMonitorAgent(config);
            agentClient.connect();
            for (int cycle = 1; cycle <= CYCLES; cycle++) {
                monitoredState = runAgentClientCycle(String.format("cycle-%d-biz", cycle), agentClient, monitoredState);
            }

            service.deleteByAgent(config, 1);

            agentClient.disconnect();

            statisticsService.save();
            log.info("... running with Biz Service completed.");

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            configurationService.deleteConfiguration(config);
        }
    }

}
