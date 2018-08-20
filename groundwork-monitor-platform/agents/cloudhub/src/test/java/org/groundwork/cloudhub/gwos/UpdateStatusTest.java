package org.groundwork.cloudhub.gwos;

import com.groundwork.collage.util.MonitorStatusBubbleUp;
import org.groundwork.agents.monitor.MonitorAgentCollector;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.ServerConfigurator;
import org.groundwork.cloudhub.configuration.ConfigurationService;
import org.groundwork.cloudhub.configuration.DockerConfiguration;
import org.groundwork.cloudhub.gwos.messages.UnreachableStatusMessages;
import org.groundwork.cloudhub.monitor.CloudhubAgentInfo;
import org.groundwork.cloudhub.monitor.MonitorAgentCollectorService;
import org.groundwork.cloudhub.monitor.MonitorAgentConfiguration;
import org.groundwork.cloudhub.profile.MetricType;
import org.groundwork.rs.client.HostClient;
import org.groundwork.rs.client.ServiceClient;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MonitorAgentConfiguration.class})
public class UpdateStatusTest extends AbstractGwosServiceTest {

    @Resource(name = GwosServiceFactory.NAME)
    protected GwosServiceFactory factory;

    @Resource(name = ConfigurationService.NAME)
    protected ConfigurationService configurationService;

    @Resource(name = MonitorAgentCollector.NAME)
    protected MonitorAgentCollectorService collectorService;

    public static final String AGENT_ID = "updateStatusUnreachableTest";
    public static final String DOCKER_HOST = "dst14-hostDockerTest";
    public static final String[] DOCKER_SERVICES = {
            "syn.cpu.usage.system",
            "syn.memory.usage",
            "memory.usage",
            "cpu.load"
    };

    @Test
    public void updateStatusUnreachableTest() {
        DockerConfiguration dockerConfig = configurationService.createConfiguration(VirtualSystem.DOCKER);
        dockerConfig.getCommon().setAgentId(AGENT_ID);
        String connectionString = null;
        try {
            dockerConfig.getCommon().setDisplayName(DOCKER_HOST);
            ServerConfigurator.setupLocalGroundworkServer(dockerConfig.getGwos());
            connectionString = BaseGwosService.buildRsConnectionString(dockerConfig.getGwos());
            CloudhubAgentInfo agentInfo = collectorService.createMonitorAgentInfo(dockerConfig);
            GwosService gwosService = factory.getGwosServicePrototype(dockerConfig, agentInfo);
            createHostAndServices(dockerConfig, connectionString, DOCKER_HOST, DOCKER_SERVICES, MetricType.hypervisor);
            ServiceClient serviceClient = new ServiceClient(connectionString);
            for (String serviceName : DOCKER_SERVICES) {
                assert serviceClient.lookup(serviceName, DOCKER_HOST) != null;
            }
            // Update all hosts to unreachable for this Agent
            gwosService.updateAllHypervisorsStatus(agentInfo, MonitorStatusBubbleUp.UNREACHABLE, MonitorStatusBubbleUp.UNKNOWN, new UnreachableStatusMessages());

            HostClient hostClient = new HostClient(connectionString);
            DtoHost host = hostClient.lookup(DOCKER_HOST);
            assert host != null;
            assert host.getAgentId().equals(AGENT_ID);
            assert host.getHostName().equals(DOCKER_HOST);
            assert host.getMonitorStatus().equals(MonitorStatusBubbleUp.UNREACHABLE);

            for (String serviceName : DOCKER_SERVICES) {
                DtoService service = serviceClient.lookup(serviceName, DOCKER_HOST);
                assert service.getAgentId().equals(AGENT_ID);
                assert service.getHostName().equals(DOCKER_HOST);
                assert service.getMonitorStatus().equals(MonitorStatusBubbleUp.UNKNOWN);
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
