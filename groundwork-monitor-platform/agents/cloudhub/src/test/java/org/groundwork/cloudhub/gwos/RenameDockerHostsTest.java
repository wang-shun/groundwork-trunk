package org.groundwork.cloudhub.gwos;

import org.groundwork.agents.monitor.MonitorAgentCollector;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.ServerConfigurator;
import org.groundwork.cloudhub.configuration.ConfigurationService;
import org.groundwork.cloudhub.configuration.DockerConfiguration;
import org.groundwork.cloudhub.configuration.GWOSConfiguration;
import org.groundwork.cloudhub.monitor.MonitorAgentCollectorService;
import org.groundwork.cloudhub.monitor.MonitorAgentConfiguration;
import org.groundwork.cloudhub.metrics.BaseVM;
import org.groundwork.rs.client.HostClient;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostList;
import org.groundwork.rs.dto.DtoOperationResults;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MonitorAgentConfiguration.class})
public class RenameDockerHostsTest {

    @Resource(name = GwosServiceFactory.NAME)
    protected GwosServiceFactory factory;

    @Resource(name = ConfigurationService.NAME)
    protected ConfigurationService configurationService;

    @Resource(name = MonitorAgentCollector.NAME)
    protected MonitorAgentCollectorService collectorService;

    @Test
    public void renameTest() {
        DockerConfiguration docker = configurationService.createConfiguration(VirtualSystem.DOCKER);
        docker.getCommon().setDisplayName("Docker Hypervisor 200");
        ServerConfigurator.setupLocalGroundworkServer(docker.getGwos());
        docker.getGwos().setGwosVersion(GWOSConfiguration.DEFAULT_VERSION);
        GwosService service = factory.getGwosServicePrototype(docker, collectorService.createMonitorAgentInfo(docker));
        // start test populate data
        List<BaseVM> vms = new ArrayList<>();

        HostClient client = new HostClient(BaseGwosService.buildRsConnectionString(docker.getGwos()));
        DtoHostList updates = new DtoHostList();
        DtoHost host1 = new DtoHost();
        host1.setHostName("docker-host1");
        host1.setDescription("docker-host1");
        host1.setMonitorStatus("OK");
        host1.setDeviceIdentification(host1.getHostName());
        host1.setAgentId(docker.getCommon().getAgentId());
        updates.add(host1);
        DtoOperationResults results = client.post(updates);
        assert results.getFailed() == 0;
        assert results.getSuccessful() == 1;

        // verify vms are added
        DtoHost host = client.lookup(host1.getHostName());
        assert host != null;
        assert host.getHostName().equals("docker-host1");
        assert host.getDescription().equals("docker-host1");

        // rename vms
        service.renamePrefixByAgent(docker.getCommon().getAgentId(), "docker-", "new-");

        // validate rename
        assert client.lookup("docker-host1") == null;
        host = client.lookup("new-host1");
        assert host != null;
        assert host.getHostName().equals("new-host1");
        assert host.getDescription().equals("new-host1");

        // delete data
        client.delete(host.getHostName());
        assert client.lookup("new-host1") == null;

    }

}
