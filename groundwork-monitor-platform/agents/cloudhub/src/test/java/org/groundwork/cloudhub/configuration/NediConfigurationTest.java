package org.groundwork.cloudhub.configuration;

import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.AbstractAgentTest;
import org.groundwork.cloudhub.ServerConfigurator;
import org.groundwork.cloudhub.monitor.MonitorAgentConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MonitorAgentConfiguration.class})
public class NediConfigurationTest extends AbstractAgentTest {

    @Test
    public void testNediCreate() {

        NediConfiguration cc = ServerConfigurator.createNediServer(configurationService);
        try {
            ServerConfigurator.setupLocalGroundworkServer(cc.getGwos());
            configurationService.saveConfiguration(cc);

            NediConfiguration configuration = (NediConfiguration)configurationService.readConfiguration(
                    cc.getCommon().getPathToConfigurationFile() + cc.getCommon().getConfigurationFile());


            // assert connection default values are stored and re-read
            NediConnection connection = configuration.getConnection();
            assertEquals(ServerConfigurator.NEDI_SERVER, connection.getServer());
            assertEquals(ServerConfigurator.NEDI_USERNAME, connection.getUsername());
            assertEquals(ServerConfigurator.NEDI_PASSWORD, connection.getPassword());
            assertEquals(ServerConfigurator.NEDI_DATABASE, connection.getDatabase());
            assertEquals(ServerConfigurator.NEDI_POLICY_HOST, connection.getPolicyHost());

            // assert GWOS connection default values are stored and re-read
            GWOSConfiguration gwos = configuration.getGwos();
            assertEquals("localhost", gwos.getGwosServer());
            assertEquals("RESTAPIACCESS", gwos.getWsUsername());
            //assertEquals("RESTAPIACCESSPASSWORD", gwos.getWsPassword());
            assertEquals("/api", gwos.getRsEndPoint());
            assertEquals("80", gwos.getWsPortNumber());

            // assert Common default values are stored and re-read
            CommonConfiguration common = configuration.getCommon();
            assertEquals(VirtualSystem.NEDI.name(), common.getVirtualSystem().name());
            assertEquals("NeDi Connector", common.getDisplayName());
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
