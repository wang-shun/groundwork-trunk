package org.groundwork.cloudhub.configuration;

import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.AbstractAgentTest;
import org.groundwork.cloudhub.ServerConfigurator;
import org.groundwork.cloudhub.connectors.cloudera.ClouderaConfigurationProvider;
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
public class ClouderaConfigurationTest extends AbstractAgentTest {

    @Test
    public void testClouderaCreate() {

        ClouderaConfiguration cc = ServerConfigurator.createClouderaServer(configurationService);
        try {
            ServerConfigurator.setupLocalGroundworkServer(cc.getGwos());
            configurationService.saveConfiguration(cc);

            ClouderaConfiguration configuration = (ClouderaConfiguration)configurationService.readConfiguration(
                    cc.getCommon().getPathToConfigurationFile() + cc.getCommon().getConfigurationFile());

            // assert configuration default values are stored and re-read
            List<ConfigurationView> views = configuration.getViews();
            assertTrue(views.size() >= 14);
            assertEquals(false, configuration.getView(ClouderaConfigurationProvider.CLOUDERA_CLUSTER).isService());
            assertEquals(false, configuration.getView(ClouderaConfigurationProvider.CLOUDERA_HOST).isService());
            assertEquals(true, configuration.getView(ClouderaConfigurationProvider.CLOUDERA_SERVICE_HBASE).isService());
            assertEquals(true, configuration.getView(ClouderaConfigurationProvider.CLOUDERA_SERVICE_HDFS).isService());
            assertEquals(true, configuration.getView(ClouderaConfigurationProvider.CLOUDERA_SERVICE_SPARK).isService());

            // assert connection default values are stored and re-read
            ClouderaConnection connection = configuration.getConnection();
            assertEquals(ServerConfigurator.CLOUDERA_SERVER, connection.getServer());
            assertEquals(ServerConfigurator.CLOUDERA_ADMIN_USER, connection.getUsername());
            assertEquals(ServerConfigurator.CLOUDERA_ADMIN_PASSWORD, connection.getPassword());

            // assert GWOS connection default values are stored and re-read
            GWOSConfiguration gwos = configuration.getGwos();
            assertEquals("localhost", gwos.getGwosServer());
            assertEquals("RESTAPIACCESS", gwos.getWsUsername());
            //assertEquals("RESTAPIACCESSPASSWORD", gwos.getWsPassword());
            assertEquals("/api", gwos.getRsEndPoint());
            assertEquals("80", gwos.getWsPortNumber());

            // assert Common default values are stored and re-read
            CommonConfiguration common = configuration.getCommon();
            assertEquals(VirtualSystem.CLOUDERA.name(), common.getVirtualSystem().name());
            assertEquals("Cloudera Dev Instance", common.getDisplayName());
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
