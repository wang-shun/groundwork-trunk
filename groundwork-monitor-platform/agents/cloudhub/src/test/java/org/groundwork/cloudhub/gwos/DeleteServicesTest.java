package org.groundwork.cloudhub.gwos;

import org.groundwork.agents.monitor.DeleteServiceInfo;
import org.groundwork.agents.monitor.MonitorAgentCollector;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.ServerConfigurator;
import org.groundwork.cloudhub.configuration.ConfigurationService;
import org.groundwork.cloudhub.configuration.DockerConfiguration;
import org.groundwork.cloudhub.connectors.docker.DockerConfigurationProvider;
import org.groundwork.cloudhub.monitor.MonitorAgentCollectorService;
import org.groundwork.cloudhub.monitor.MonitorAgentConfiguration;
import org.groundwork.cloudhub.profile.MetricType;
import org.groundwork.rs.client.HostClient;
import org.groundwork.rs.client.ServiceClient;
import org.groundwork.rs.dto.DtoOperationResults;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MonitorAgentConfiguration.class})
public class DeleteServicesTest extends AbstractGwosServiceTest {

    @Resource(name = GwosServiceFactory.NAME)
    protected GwosServiceFactory factory;

    @Resource(name = ConfigurationService.NAME)
    protected ConfigurationService configurationService;

    @Resource(name = MonitorAgentCollector.NAME)
    protected MonitorAgentCollectorService collectorService;

    public static final String AGENT_ID = "testAgent";
    public static final String DOCKER_HOST = "dst13-hostDockerTest";
    public static final String[] DOCKER_SERVICES = {
            "syn.cpu.usage.system",
            "syn.memory.usage",
            "memory.usage",
            "cpu.load"
    };

    @Test
    public void deleteDockerServicesTest() {
        DockerConfiguration dockerConfig = configurationService.createConfiguration(VirtualSystem.DOCKER);
        dockerConfig.getCommon().setAgentId(AGENT_ID);
        String connectionString = null;
        try {
            dockerConfig.getCommon().setDisplayName(DOCKER_HOST);
            ServerConfigurator.setupLocalGroundworkServer(dockerConfig.getGwos());
            connectionString = BaseGwosService.buildRsConnectionString(dockerConfig.getGwos());
            GwosService service = factory.getGwosServicePrototype(dockerConfig, collectorService.createMonitorAgentInfo(dockerConfig));
            createHostAndServices(dockerConfig, connectionString, DOCKER_HOST, DOCKER_SERVICES, MetricType.hypervisor);
            List<DeleteServiceInfo> serviceInfos = new ArrayList<>();
            List<String> serviceNames = Arrays.asList(DOCKER_SERVICES);
            for (String serviceName : serviceNames) {
                serviceInfos.add(new DeleteServiceInfo(serviceName));
            }
            DtoOperationResults results = service.deleteServices(serviceInfos,
                    DockerConfigurationProvider.APPLICATIONTYPE_DOCKER, MetricType.vm, AGENT_ID);
            assert results.getFailed() == 0;
            assert results.getSuccessful() > 0;
            ServiceClient serviceClient = new ServiceClient(connectionString);
            for (String serviceName2 : DOCKER_SERVICES) {
                assert serviceClient.lookup(serviceName2, DOCKER_HOST) == null;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            assert connectionString != null;
            HostClient hostClient = new HostClient(connectionString);
            hostClient.delete(DOCKER_HOST);
            assert hostClient.lookup(DOCKER_HOST) == null;
        }
    }

}
