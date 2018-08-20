package org.groundwork.cloudhub.monitor;

import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.AbstractAgentTest;
import org.groundwork.cloudhub.configuration.AmazonConfiguration;
import org.groundwork.cloudhub.configuration.ConfigurationProvider;
import org.groundwork.cloudhub.connectors.CollectionMode;
import org.groundwork.cloudhub.connectors.ManagementConnector;
import org.groundwork.cloudhub.connectors.MonitoringConnector;
import org.groundwork.cloudhub.gwos.GwosService;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.DataCenterSyncResult;
import org.groundwork.cloudhub.inventory.InventoryOptions;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static junit.framework.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MonitorAgentConfiguration.class})
public class AmazonTaggedGroupsTest  extends AbstractAgentTest {

   // @Test // requires a running GWOS
    public void amazonTaggedGroupSyncTest() throws Exception {
        assertNotNull(configurationService);
        assertNotNull(collectorService);
        AmazonConfiguration awsConfig = null;
        try {
            awsConfig = (AmazonConfiguration)configurationService.readConfiguration("/usr/local/groundwork/config/cloudhub/cloudhub-amazon-6.xml");
            //configurationService.saveConfiguration(awsConfig);
            MonitoringConnector connector = connectorFactory.getMonitoringConnector(awsConfig);
            ManagementConnector management = connectorFactory.getManagementConnector(awsConfig);
            CollectionMode mode = new CollectionMode(
                    true, // hosts
                    true, // VMs
                    true, // storage
                    false, // network
                    false, // resource pool
                    false, // clusters
                    false, // data centers
                    true,   // tagged resources
                    "GWHostGroup",
                    false,
                    false
            );
            connector.setCollectionMode(mode);
            management.setCollectionMode(mode);
            connector.connect(awsConfig.getConnection());
            DataCenterInventory inventory = management.gatherInventory();
            inventory.debug();
            ConfigurationProvider provider = connectorFactory.getConfigurationProvider(VirtualSystem.AMAZON);
            CloudhubAgentInfo agentInfo = new CloudhubAgentInfo(provider.getHypervisorDisplayName(),
                    provider.getCloudhubMonitorAgentBeanName(), provider.getConnectorName(),
                    provider.getManagementServerDisplayName(), provider.getApplicationType(),
                    awsConfig.getCommon().getVirtualSystem(), awsConfig.getCommon().getConnectionRetries(),
                    awsConfig.getCommon().getAgentId());
            GwosService gwosService = factory.getGwosServicePrototype(awsConfig, agentInfo);
            DataCenterInventory gwosInventory = gwosService.gatherInventory(new InventoryOptions(true, true, true, true));
            DataCenterSyncResult results = synchronizer.synchronizeInventory(inventory, gwosInventory, awsConfig, agentInfo, gwosService);
            assert results != null;
            results.debug(agentInfo.getName());
            connector.disconnect();
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
        finally {
//            if (awsConfig != null)
//                configurationService.deleteConfiguration(awsConfig);
        }
    }
}
