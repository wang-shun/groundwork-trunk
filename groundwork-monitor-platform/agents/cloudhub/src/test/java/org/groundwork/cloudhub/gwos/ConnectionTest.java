package org.groundwork.cloudhub.gwos;

import org.groundwork.agents.monitor.MonitorAgentCollector;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.ServerConfigurator;
import org.groundwork.cloudhub.configuration.ConfigurationService;
import org.groundwork.cloudhub.configuration.VmwareConfiguration;
import org.groundwork.cloudhub.monitor.MonitorAgentCollectorService;
import org.groundwork.cloudhub.monitor.MonitorAgentConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MonitorAgentConfiguration.class})
public class ConnectionTest {

    @Resource(name = GwosServiceFactory.NAME)
    protected GwosServiceFactory factory;

    @Resource(name = ConfigurationService.NAME)
    protected ConfigurationService configurationService;

    @Resource(name = MonitorAgentCollector.NAME)
    protected MonitorAgentCollectorService collectorService;

    /**
     * WARNING: This test requires a connection to eng-rh6-dev1 to run
     */
    //@Test
    public void test702Connection() {
        VmwareConfiguration vmware = configurationService.createConfiguration(VirtualSystem.VMWARE);
        vmware.getCommon().setDisplayName("Vmware Hypervisor 200");
        ServerConfigurator.setupGWOS702Configuration(ServerConfigurator.GWOS_702_SERVER, vmware.getGwos());
        GwosService gwosService = factory.getGwosServicePrototype(vmware, collectorService.createMonitorAgentInfo(vmware));
        assert gwosService.testConnection(vmware);
    }

    @Test
    public void test710Connection() {
        VmwareConfiguration vmware = configurationService.createConfiguration(VirtualSystem.VMWARE);
        vmware.getCommon().setDisplayName("Vmware Hypervisor 201");
        ServerConfigurator.setupLocalGroundworkServer(vmware.getGwos());
        GwosService gwosService = factory.getGwosServicePrototype(vmware, collectorService.createMonitorAgentInfo(vmware));
        assert gwosService.testConnection(vmware);
    }

}
