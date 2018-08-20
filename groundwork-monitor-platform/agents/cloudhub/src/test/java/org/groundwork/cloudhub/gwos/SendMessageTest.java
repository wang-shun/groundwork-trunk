package org.groundwork.cloudhub.gwos;

import org.groundwork.agents.monitor.MonitorAgentCollector;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.ServerConfigurator;
import org.groundwork.cloudhub.configuration.ConfigurationService;
import org.groundwork.cloudhub.configuration.VmwareConfiguration;
import org.groundwork.cloudhub.monitor.MonitorAgentCollectorService;
import org.groundwork.cloudhub.monitor.MonitorAgentConfiguration;
import org.groundwork.cloudhub.profile.MetricType;
import org.groundwork.rs.client.EventClient;
import org.groundwork.rs.client.ServiceClient;
import org.groundwork.rs.dto.DtoEvent;
import org.groundwork.rs.dto.DtoServiceList;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MonitorAgentConfiguration.class})
public class SendMessageTest extends AbstractGwosServiceTest {

    protected static final String SEND_MESSAGE_TEST = "send-message-test";
    @Resource(name = ConfigurationService.NAME)
    protected ConfigurationService configurationService;
    @Resource(name = GwosServiceFactory.NAME)
    protected GwosServiceFactory factory;
    @Resource(name = MonitorAgentCollector.NAME)
    protected MonitorAgentCollectorService collectorService;

    @Test
    public void sendMessageTest() {
        VmwareConfiguration vmware = null;
        try {
            vmware = configurationService.createConfiguration(VirtualSystem.VMWARE);
            ServerConfigurator.setupLocalGroundworkServer(vmware.getGwos());
            ServerConfigurator.setupVmwareVermontConnection(vmware.getConnection());
            GwosService gwosService = factory.getGwosServicePrototype(vmware, collectorService.createMonitorAgentInfo(vmware));
            String connectionString = BaseGwosService.buildRsConnectionString(vmware.getGwos());

            ServiceClient serviceClient = new ServiceClient(connectionString);
            DtoServiceList services = new DtoServiceList();
            services.add(buildServiceUpdate(vmware, SEND_MESSAGE_TEST, "localhost", MetricType.hypervisor));
            serviceClient.post(services);

            gwosService.sendEventMessage("localhost", "localhost", SEND_MESSAGE_TEST, "DOWN", "HIGH", "TESTING!!!!", "VEMA");

            EventClient eventClient = new EventClient(connectionString);
            List<DtoEvent> events = eventClient.query("host = 'localhost' and service = 'send-message-test'");
            assert events.size() == 1;
            DtoEvent event = events.get(0);
            eventClient.delete(Integer.toString(event.getId()));
            serviceClient.delete(SEND_MESSAGE_TEST, "localhost");
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

}
