package org.groundwork.cloudhub.monitor;

import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.ServerConfigurator;
import org.groundwork.cloudhub.configuration.VmwareConfiguration;
import org.groundwork.cloudhub.gwos.GwosService;
import org.groundwork.cloudhub.metrics.MonitoringState;
import org.groundwork.cloudhub.profile.ProfileService;
import org.groundwork.cloudhub.statistics.MonitoringStatistics;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

/*
 *
 */
/**
 * To be used for backward compatibility whenever core changes are made to the VmWare Connector
 * or any changes made to the Inventory or Synchronizer components
 * These tests are tied to the Groundwork Vermont vSphere server
 *
 * Due to the fact that our VSphere environment is live, we cannot depend on integration tests to run consistently
 *
 * Recommend running this command to reset database before running the load test:
 *
 * cd enterprise-foundation/collage/database
 * mvn -P production -DskipTests=false test
 *
 * The test will clean up after itself, deleting all hosts for the current agent and cascade deleting services, events etc
 * 
 * This test must be run with the tomcat maven profile (-P tomcat)
 *
 * mvn test -P tomcat -DskipTests=false -Dtest=MonitoringRegressionTest
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MonitorAgentConfiguration.class})
public class MonitoringRegressionTest extends BaseMonitoringTest {

    private static Logger log = Logger.getLogger(MonitoringRegressionTest.class);

    private static final int CYCLES = 2;

    @Resource(name = ProfileService.NAME)
    private ProfileService profileService;

    @Test
    public void regressionTest() throws Exception {
        log.info("Running Vermont/Regression Load Test ....");
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
                log.info("Running monitoring agent cycle " + config);
                monitoredState = runAgentClientCycle(String.format("cycle-%d", cycle), agentClient, monitoredState);
            }

            // reset data
            log.info("Deleting all data for agent ...");
            GwosService service = factory.getGwosServicePrototype(config, (CloudhubAgentInfo) agentClient.getAgentInfo());
            service.deleteByAgent(config, 1);
            agentClient.disconnect();

            statisticsService.save();
            log.info("... running with Biz Service completed.");

            MonitoringStatistics baseline = statisticsService.readCSV("./src/test/testdata/statistics/baseline-2017-02-21.csv", 2);
            assert baseline != null;
            MonitoringStatistics lastRun = statisticsService.lookup("cycle-2");
            assert lastRun != null;
            compareQueries(baseline, lastRun);


        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            configurationService.deleteConfiguration(config);
            if (agentClient != null) {
                profileService.removeProfile(VirtualSystem.VMWARE, agentClient.getConfiguration().getCommon().getAgentId());
            }
        }
    }

    //@Test
    public void readCSVTest() throws Exception {
        MonitoringStatistics statistics = statisticsService.readCSV("./src/test/testdata/statistics/baseline.csv", 2);
        assert statistics != null;
    }

    protected void compareQueries(MonitoringStatistics stats1, MonitoringStatistics stats2) {
        assertEquals(stats1.getHostQueries().getHosts(), stats2.getHostQueries().getHosts());
        assertEquals( stats1.getHostQueries().getHostStatuses(),  stats2.getHostQueries().getHostStatuses());
        assertEquals( stats1.getHostQueries().getHostStatusProperty(),  stats2.getHostQueries().getHostStatusProperty());
        assertEquals( stats1.getHostQueries().getHostStatusProperty1(),  stats2.getHostQueries().getHostStatusProperty1());
        assertEquals( stats1.getHostQueries().getHostStatusProperty2(),  stats2.getHostQueries().getHostStatusProperty2());
        assertEquals( stats1.getHostQueries().getHostStatusProperty3(),  stats2.getHostQueries().getHostStatusProperty3());
        assertEquals( stats1.getHostQueries().getHostGroups(),  stats2.getHostQueries().getHostGroups());

        assertEquals( stats1.getServiceQueries().getServices(),  stats2.getServiceQueries().getServices());
        assertEquals( stats1.getServiceQueries().getServicesCPU(),  stats2.getServiceQueries().getServicesCPU());
        assertEquals( stats1.getServiceQueries().getServicesCPUToMax(),  stats2.getServiceQueries().getServicesCPUToMax());
        assertEquals( stats1.getServiceQueries().getServicesFreeSpace(),  stats2.getServiceQueries().getServicesFreeSpace());
        assertEquals( stats1.getServiceQueries().getServicesSwappedMemSize(),  stats2.getServiceQueries().getServicesSwappedMemSize());
        assertEquals( stats1.getServiceQueries().getServiceStatusProperty(),  stats2.getServiceQueries().getServiceStatusProperty());
        assertEquals( stats1.getServiceQueries().getServiceStatusProperty1(),  stats2.getServiceQueries().getServiceStatusProperty1());
        assertEquals( stats1.getServiceQueries().getServiceStatusProperty53(),  stats2.getServiceQueries().getServiceStatusProperty53());

        assert Math.abs(stats1.getEventQueries().getEvents() - stats2.getEventQueries().getEvents()) <= 1;
        assert Math.abs(stats1.getEventQueries().getHostEvents() - stats2.getEventQueries().getHostEvents()) <= 1;
        assert Math.abs(stats1.getEventQueries().getServiceEvents() - stats2.getEventQueries().getServiceEvents()) <= 1;
        assert Math.abs(stats1.getEventQueries().getSetupEvents() - stats2.getEventQueries().getSetupEvents()) <= 1;
    }

}