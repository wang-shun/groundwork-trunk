package org.groundwork.cloudhub.monitor;

import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.AbstractAgentTest;
import org.groundwork.cloudhub.ServerConfigurator;
import org.groundwork.cloudhub.configuration.VmwareConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static junit.framework.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MonitorAgentConfiguration.class})
public class CloudhubMonitorAgentTest extends AbstractAgentTest {

    private static Logger log = Logger.getLogger(CloudhubMonitorAgentTest.class);

    @Test
    public void monitorCollectorTest() throws Exception {
        assertNotNull(configurationService);
        assertNotNull(collectorService);
        VmwareConfiguration vmware = configurationService.createConfiguration(VirtualSystem.VMWARE);
        try {
            vmware.getCommon().setDisplayName("Vermont");
            vmware.getCommon().setDisplayName("Vmware Hypervisor 101");
            ServerConfigurator.setupLocalGroundworkServer(vmware.getGwos());
            ServerConfigurator.setupVmwareVermontConnection(vmware.getConnection());
            ServerConfigurator.enableAllViews(vmware.getCommon());
            configurationService.saveConfiguration(vmware);
            collectorService.startMonitoringConnection(vmware);
            String vmwareAgentName = vmware.getCommon().getConfigurationFile();
            CloudhubMonitorAgent vmwareAgent = collectorService.lookup(vmwareAgentName);
            assertNotNull(vmwareAgent);
            int count = 0;
            for (CloudhubMonitorAgent agent : collectorService.list()) {
                log.debug(agent.getAgentInfo());
                assert agent.isSuspended();
                assert agent.getAgentInfo().getAgentId().equals(vmware.getCommon().getAgentId());
                assert agent.getAgentInfo().getName().equals(vmwareAgentName);
                count++;
            }
            assert count == 1;
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
        finally {
            if (vmware != null)
                configurationService.deleteConfiguration(vmware);
        }
    }

}
