package org.groundwork.cloudhub.configuration;

import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.AbstractAgentTest;
import org.groundwork.cloudhub.ServerConfigurator;
import org.groundwork.cloudhub.connectors.azure.AzureConfigurationProvider;
import org.groundwork.cloudhub.monitor.MonitorAgentConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MonitorAgentConfiguration.class})
public class AzureConfigurationTest extends AbstractAgentTest {

    @Test
    public void testAzureCreate() {

        AzureConfiguration cc = ServerConfigurator.createAzureServer(configurationService);
        try {
            ServerConfigurator.setupLocalGroundworkServer(cc.getGwos());
            configurationService.saveConfiguration(cc);

            AzureConfiguration configuration = (AzureConfiguration)configurationService.readConfiguration(
                    cc.getCommon().getPathToConfigurationFile() + cc.getCommon().getConfigurationFile());

            // assert configuration default values are stored and re-read
            List<ConfigurationView> views = configuration.getViews();
            assertTrue(views.size() >= 4);
            assertEquals(false, configuration.getView(AzureConfigurationProvider.AZURE_CLUSTER).isService());
            assertEquals(false, configuration.getView(AzureConfigurationProvider.AZURE_HOST).isService());
            assertEquals(true, configuration.getView(AzureConfigurationProvider.AZURE_VIRTUAL_MACHINES).isService());
            assertEquals(true, configuration.getView(AzureConfigurationProvider.AZURE_COSMOS_DBS).isService());

            // assert connection default values are stored and re-read
            AzureConnection connection = configuration.getConnection();
            assertEquals(ServerConfigurator.AZURE_CREDENTIALS_FILE, connection.getCredentialsFile());
            assertEquals(new Long(6000L), connection.getTimeoutMs());

            // assert GWOS connection default values are stored and re-read
            GWOSConfiguration gwos = configuration.getGwos();
            assertEquals("localhost", gwos.getGwosServer());
            assertEquals("RESTAPIACCESS", gwos.getWsUsername());
            //assertEquals("RESTAPIACCESSPASSWORD", gwos.getWsPassword());
            assertEquals("/api", gwos.getRsEndPoint());
            assertEquals("80", gwos.getWsPortNumber());

            // assert Common default values are stored and re-read
            CommonConfiguration common = configuration.getCommon();
            assertEquals(VirtualSystem.AZURE.name(), common.getVirtualSystem().name());
            assertEquals("Azure Dev Instance", common.getDisplayName());
            assertEquals(5, common.getCheckIntervalMinutes());
            assertEquals(2, common.getSyncIntervalMinutes());

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            configurationService.deleteConfiguration(cc);
            assert configurationService.doesConfigurationExist(cc.getCommon().getPathToConfigurationFile() +
                    cc.getCommon().getConfigurationFile()) == false;
        }
    }

}
