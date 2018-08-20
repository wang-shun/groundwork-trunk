package org.groundwork.cloudhub.monitor;

import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.AbstractAgentTest;
import org.groundwork.cloudhub.ServerConfigurator;
import org.groundwork.cloudhub.configuration.ConfigurationProvider;
import org.groundwork.cloudhub.configuration.VmwareConfiguration;
import org.groundwork.cloudhub.gwos.GWOSHostGroup;
import org.groundwork.cloudhub.gwos.GwosService;
import org.groundwork.cloudhub.gwos.GwosServiceStatus;
import org.groundwork.cloudhub.gwos.GwosStatus;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MonitorAgentConfiguration.class})
public class ConnectorMonitorTest extends AbstractAgentTest {

    private static Logger log = Logger.getLogger(ConnectorMonitorTest.class);

    @Resource(name="ConnectorMonitor")
    private ConnectorMonitor connectorMonitor;

    @Test
    public void connectorMonitorTest() throws Exception {
        VmwareConfiguration vmware = configurationService.createConfiguration(VirtualSystem.VMWARE);
        try {
            vmware.getCommon().setDisplayName("Vermont 2");
            ServerConfigurator.setupLocalGroundworkServer(vmware.getGwos());
            ServerConfigurator.setupVmwareVermontConnection(vmware.getConnection());
            ServerConfigurator.enableAllViews(vmware.getCommon());
            configurationService.saveConfiguration(vmware);
            ConfigurationProvider provider = connectorFactory.getConfigurationProvider(VirtualSystem.VMWARE);

            CloudhubAgentInfo agentInfo = new CloudhubAgentInfo(provider.getHypervisorDisplayName(),
                    provider.getCloudhubMonitorAgentBeanName(), provider.getConnectorName(),
                    provider.getManagementServerDisplayName(), provider.getApplicationType(),
                    vmware.getCommon().getVirtualSystem(), vmware.getCommon().getConnectionRetries(),
                    vmware.getCommon().getAgentId());
            agentInfo.setName(vmware.getCommon().getConfigurationFile());
            GwosService gwosService = factory.getGwosServicePrototype(vmware, agentInfo);

            ConnectorMonitorState state = new ConnectorMonitorState(GwosStatus.UP.status, GwosServiceStatus.OK.status);
            connectorMonitor.updateGroundworkConnector(gwosService, 0, state);
            connectorMonitor.updateGroundworkConnector(gwosService, 0, state);

            assertNotNull(gwosService.lookupHostGroup("Connectors"));
            assertNotNull(gwosService.lookupHost("localhost"));

            vmware.getGwos().setGwosServer("yourcompany.com");

            connectorMonitor.updateGroundworkConnector(gwosService, 0, state);
            connectorMonitor.updateGroundworkConnector(gwosService, 0, state);

            assertNotNull(gwosService.lookupHostGroup("Connectors"));
            assertNotNull(gwosService.lookupHost("yourcompany.com"));

            gwosService.deleteHostGroup(new GWOSHostGroup("Connectors", null, null, null));
            gwosService.deleteByAgent(vmware, 1);

            assertNull(gwosService.lookupHostGroup("Connectors"));
            assertNull(gwosService.lookupHost("yourcompany.com"));
            assertNotNull(gwosService.lookupHost("localhost"));

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
