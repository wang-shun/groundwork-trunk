package org.groundwork.cloudhub.monitor;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.AbstractAgentTest;
import org.groundwork.cloudhub.ServerConfigurator;
import org.groundwork.cloudhub.configuration.RedhatConfiguration;
import org.groundwork.cloudhub.connectors.MonitoringConnector;
import org.groundwork.cloudhub.inventory.DataCenterSyncResult;
import org.groundwork.cloudhub.metrics.MonitoringState;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static junit.framework.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = {"/cloudhub-test.xml" })
@ContextConfiguration(classes = {MonitorAgentConfiguration.class})
public class RhevLoadTest extends AbstractAgentTest {

    private static Logger log = Logger.getLogger(RhevLoadTest.class);

    // @Test - Disabled until RHEL server is up again
    public void monitorRedHatAgentTest() throws Exception {
        assertNotNull(configurationService);
        assertNotNull(collectorService);
        RedhatConfiguration redhat = configurationService.createConfiguration(VirtualSystem.REDHAT);
        redhat.getCommon().setAgentId("d533fd91-5aa8-472b-bf22-63bf08268e61");
        redhat.getCommon().setDisplayName("Redhat");
        redhat.getCommon().setDisplayName("Redhat Hypervisor 102");
        ServerConfigurator.setupLocalGroundworkServer(redhat.getGwos());
        ServerConfigurator.setupRedhatConnection(redhat.getConnection());
        ServerConfigurator.enableAllViews(redhat.getCommon());
        configurationService.saveConfiguration(redhat);
        MonitoringConnector connector = connectorFactory.getMonitoringConnector(redhat);
        try {
//            assert (PostgresUtils.restoreDatabase());
//            setupHttpClient();
            CloudhubMonitorAgentClient monitorAgentClient = (CloudhubMonitorAgentClient)collectorService.createMonitorAgent(redhat);
            monitorAgentClient.connect();
            DataCenterSyncResult syncResult = monitorAgentClient.synchronizeInventory();
            MonitoringState monitoredState = monitorAgentClient.collect(null);
            monitoredState = monitorAgentClient.filter(monitoredState);
            monitoredState = monitorAgentClient.synchronize(monitoredState, syncResult);
            monitorAgentClient.updateMonitor(monitoredState, syncResult);
            monitorAgentClient.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            configurationService.deleteConfiguration(redhat);
        }
    }

    private void setupHttpClient() {
        HttpClient httpClient = new DefaultHttpClient();
        //DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpParams params = httpClient.getParams();
        HttpConnectionParams.setConnectionTimeout(params, 10000);
        HttpConnectionParams.setSoTimeout(params, 30000);
    }

}
