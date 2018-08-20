package org.groundwork.cloudhub.monitor;

import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.AbstractAgentTest;
import org.groundwork.cloudhub.ServerConfigurator;
import org.groundwork.cloudhub.configuration.ConfigurationProvider;
import org.groundwork.cloudhub.configuration.RedhatConfiguration;
import org.groundwork.cloudhub.configuration.VmwareConfiguration;
import org.groundwork.cloudhub.connectors.CollectionMode;
import org.groundwork.cloudhub.connectors.ManagementConnector;
import org.groundwork.cloudhub.connectors.MonitoringConnector;
import org.groundwork.cloudhub.gwos.GwosService;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.DataCenterSyncResult;
import org.groundwork.cloudhub.inventory.InventoryOptions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static junit.framework.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MonitorAgentConfiguration.class})
public class SynchronizeInventoryTest extends AbstractAgentTest {

    @Test
    public void vmWareInventorySyncTest() throws Exception {
        assertNotNull(configurationService);
        assertNotNull(collectorService);
        VmwareConfiguration vmware = null;
        try {
            vmware = configurationService.createConfiguration(VirtualSystem.VMWARE);
            vmware.getCommon().setDisplayName("Vermont");
            ServerConfigurator.setupLocalGroundworkServer(vmware.getGwos());
            ServerConfigurator.setupVmwareVermontConnection(vmware.getConnection());
            configurationService.saveConfiguration(vmware);
            MonitoringConnector connector = connectorFactory.getMonitoringConnector(vmware);
            ManagementConnector management = connectorFactory.getManagementConnector(vmware);
            CollectionMode mode = new CollectionMode(
                    true, // hosts
                    true, // VMs
                    true, // storage
                    true, // network
                    true, // resource pool
                    false, // clusters
                    false  // data centers
            );
            connector.setCollectionMode(mode);
            management.setCollectionMode(mode);
            connector.connect(vmware.getConnection());
            DataCenterInventory inventory = management.gatherInventory();
            //inventory.debug();
            ConfigurationProvider provider = connectorFactory.getConfigurationProvider(VirtualSystem.VMWARE);
            CloudhubAgentInfo agentInfo = new CloudhubAgentInfo(provider.getHypervisorDisplayName(),
                    provider.getCloudhubMonitorAgentBeanName(), provider.getConnectorName(),
                    provider.getManagementServerDisplayName(), provider.getApplicationType(),
                    vmware.getCommon().getVirtualSystem(), vmware.getCommon().getConnectionRetries(),
                    vmware.getCommon().getAgentId());
            GwosService gwosService = factory.getGwosServicePrototype(vmware, agentInfo);
            DataCenterInventory gwosInventory = gwosService.gatherInventory(new InventoryOptions(true, true, true, true));
            DataCenterSyncResult results = synchronizer.synchronizeInventory(inventory, gwosInventory, vmware, agentInfo, gwosService);
            assert results != null;
            results.debug(agentInfo.getName());
            assert results.getHypervisorsAdded() == inventory.getHypervisors().size();
            assert results.getVmsAdded() == inventory.getVirtualMachines().size();
            assert results.getNetworksAdded() == inventory.getNetworks().size();
            assert results.getDatastoresAdded() == inventory.getDatastores().size();
            assert results.getResourcePoolsAdded() == inventory.getResourcePools().size();
            assert results.getHypervisorsDeleted() == 0;
            assert results.getHypervisorsModified() == 0;
            assert results.getVmsDeleted() == 0;
            assert results.getVmsModified() == 0;
            assert results.getNetworksDeleted() == 0;
            assert results.getNetworksModified() == 0;
            assert results.getDatastoresDeleted() == 0;
            assert results.getDatastoresModified() == 0;
            assert results.getResourcePoolsDeleted() == 0;
            assert results.getResourcePoolsModified() == 0;
            connector.disconnect();
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

    // @Test -  Disabled until Redhat Server is up again
    public void redhatInventorySyncTest() throws Exception {
        assertNotNull(configurationService);
        assertNotNull(collectorService);
        RedhatConfiguration redhat = null;
        try {
            redhat = configurationService.createConfiguration(VirtualSystem.REDHAT);
            redhat.getCommon().setDisplayName("Redhat");
            ServerConfigurator.setupLocalGroundworkServer(redhat.getGwos());
            ServerConfigurator.setupRedhatConnection(redhat.getConnection());
            redhat.getCommon().setAgentId("b2b73af8-d36a-4450-8dba-c31ac3b1eb96"); // always use same agent id
            configurationService.saveConfiguration(redhat);
            MonitoringConnector connector = connectorFactory.getMonitoringConnector(redhat);
            ManagementConnector management = connectorFactory.getManagementConnector(redhat);
            CollectionMode mode = new CollectionMode(true, // hosts
                    true, // VMs
                    true, // storage
                    true, // network
                    true, // resource pool
                    false, // clusters
                    false);  // data centers
            connector.setCollectionMode(mode);
            management.setCollectionMode(mode);
            connector.connect(redhat.getConnection());
            DataCenterInventory inventory = management.gatherInventory();
            //inventory.debug();
            ConfigurationProvider provider = connectorFactory.getConfigurationProvider(VirtualSystem.REDHAT);
            CloudhubAgentInfo agentInfo = new CloudhubAgentInfo(provider.getHypervisorDisplayName(),
                    provider.getCloudhubMonitorAgentBeanName(), provider.getConnectorName(),
                    provider.getManagementServerDisplayName(), provider.getApplicationType(),
                    redhat.getCommon().getVirtualSystem(), redhat.getCommon().getConnectionRetries(),
                    redhat.getCommon().getAgentId());
            GwosService gwosService = factory.getGwosServicePrototype(redhat, agentInfo);
            DataCenterInventory gwosInventory = gwosService.gatherInventory(new InventoryOptions(true, true, true, true));
            DataCenterSyncResult results = synchronizer.synchronizeInventory(inventory, gwosInventory, redhat, agentInfo, gwosService);
            results.debug(agentInfo.getName());
            connector.disconnect();
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
        finally {
            if (redhat != null)
                configurationService.deleteConfiguration(redhat);
        }
    }

}
