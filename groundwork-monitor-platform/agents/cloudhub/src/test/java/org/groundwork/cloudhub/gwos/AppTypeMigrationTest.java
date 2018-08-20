package org.groundwork.cloudhub.gwos;

import org.groundwork.agents.monitor.MonitorAgentCollector;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.ServerConfigurator;
import org.groundwork.cloudhub.configuration.ConfigurationService;
import org.groundwork.cloudhub.configuration.DockerConfiguration;
import org.groundwork.cloudhub.configuration.OpenDaylightConfiguration;
import org.groundwork.cloudhub.connectors.docker.DockerConfigurationProvider;
import org.groundwork.cloudhub.monitor.MonitorAgentCollectorService;
import org.groundwork.cloudhub.monitor.MonitorAgentConfiguration;
import org.groundwork.rs.client.ApplicationTypeClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MonitorAgentConfiguration.class})
public class AppTypeMigrationTest  extends AbstractGwosServiceTest  {

    @Resource(name = GwosServiceFactory.NAME)
    protected GwosServiceFactory factory;

    @Resource(name = ConfigurationService.NAME)
    protected ConfigurationService configurationService;

    @Resource(name = MonitorAgentCollector.NAME)
    protected MonitorAgentCollectorService collectorService;

    @Test
    public void migrationTest() {
        DockerConfiguration docker = configurationService.createConfiguration(VirtualSystem.DOCKER);
        docker.getCommon().setDisplayName("Docker 200");
        ServerConfigurator.setupLocalGroundworkServer(docker.getGwos());
        ApplicationTypeClient applicationTypeClient = new ApplicationTypeClient(BaseGwosService.buildRsConnectionString((docker.getGwos())));

        GwosService service = factory.getGwosServicePrototype(docker, collectorService.createMonitorAgentInfo(docker));
        service.migrateApplicationTypes();
        //configurationService.deleteConfiguration(docker);
        assert applicationTypeClient.lookup(DockerConfigurationProvider.APPLICATIONTYPE_DOCKER) != null;

        OpenDaylightConfiguration odl = configurationService.createConfiguration(VirtualSystem.OPENDAYLIGHT);
        odl.getCommon().setDisplayName("ODL 200");
        ServerConfigurator.setupLocalGroundworkServer(odl.getGwos());
        GwosService service2 = factory.getGwosServicePrototype(odl, collectorService.createMonitorAgentInfo(odl));
        service2.migrateApplicationTypes();
        //configurationService.deleteConfiguration(docker);
        assert applicationTypeClient.lookup(DockerConfigurationProvider.APPLICATIONTYPE_DOCKER) != null;

    }

}
