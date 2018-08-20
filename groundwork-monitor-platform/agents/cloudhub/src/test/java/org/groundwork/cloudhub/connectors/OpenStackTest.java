package org.groundwork.cloudhub.connectors;

import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.AbstractAgentTest;
import org.groundwork.cloudhub.ServerConfigurator;
import org.groundwork.cloudhub.configuration.OpenStackConfiguration;
import org.groundwork.cloudhub.configuration.ProfileServiceTest;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.InventoryContainerNode;
import org.groundwork.cloudhub.inventory.VirtualMachineNode;
import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.BaseMetric;
import org.groundwork.cloudhub.metrics.BaseVM;
import org.groundwork.cloudhub.metrics.MonitoringState;
import org.groundwork.cloudhub.monitor.MonitorAgentConfiguration;
import org.groundwork.cloudhub.profile.ProfileMetrics;
import org.groundwork.rs.dto.profiles.Metric;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MonitorAgentConfiguration.class})
public class OpenStackTest extends AbstractAgentTest {

    private ServerConfigurator.OpenStackTestType openStackTestType = ServerConfigurator.OpenStackTestType.kilo;

    @Test
    public void openStackInventoryTest() throws Exception {
        OpenStackConfiguration config = null;
        try {
            config = new OpenStackConfiguration();
            switch(openStackTestType) {
                case mirantis:
                    ServerConfigurator.setupOpenStackMirantisConnection(config.getConnection());
                    break;
                case juno:
                    ServerConfigurator.setupOpenStackJunoConnection(config.getConnection());
                    break;
                case icehouse:
                    ServerConfigurator.setupOpenStackIceHouseConnection(config.getConnection());
                    break;
                case liberty:
                    ServerConfigurator.setupOpenStackLibertyConnection(config.getConnection());
                    break;
                case kilo:
                    ServerConfigurator.setupOpenStackKiloConnection(config.getConnection());
                    break;
            }
            ServerConfigurator.setupLocalGroundworkServer(config.getGwos());
            configurationService.saveConfiguration(config);
            ManagementConnector management = connectorFactory.getManagementConnector(config);
            management.openConnection(config.getConnection());
            DataCenterInventory inventory = management.gatherInventory();
            switch(openStackTestType) {
                case mirantis: {
                    InventoryContainerNode hypervisor = inventory.getHypervisors().get(ServerConfigurator.MIRANTIS_OPENSTACK_SERVER_NAME);
                    assert hypervisor != null;
                    assert hypervisor.getName().equals(ServerConfigurator.MIRANTIS_OPENSTACK_SERVER_NAME);
                    assert inventory.getVirtualMachines().size() >= 1;
                    VirtualMachineNode gwos = inventory.getVirtualMachines().get("rhel7-gwos");
                    assert gwos != null;
                    assert gwos.getName().equals("rhel7-gwos");
                    assert gwos.getSystemName() != null;
                    assert gwos.getStatus().equals("UP");
                    VirtualMachineNode docker = inventory.getVirtualMachines().get("rhel7-docker");
                    assert docker != null;
                    assert docker.getName().equals("rhel7-docker");
                    assert docker.getSystemName() != null;
                    assert docker.getStatus().equals("UP");
                    VirtualMachineNode performance = inventory.getVirtualMachines().get("rhel7-performance-data");
                    assert performance != null;
                    assert performance.getName().equals("rhel7-performance-data");
                    assert performance.getSystemName() != null;
                    assert performance.getStatus().equals("UP");
                    break;
                }
                case juno: {
                    InventoryContainerNode hypervisor = inventory.getHypervisors().get(ServerConfigurator.JUNO_OPENSTACK_SERVER_NAME);
                    assert hypervisor != null;
                    assert hypervisor.getName().equals(ServerConfigurator.JUNO_OPENSTACK_SERVER_NAME);
                    assert inventory.getVirtualMachines().size() >= 1;
                    VirtualMachineNode centos7 = inventory.getVirtualMachines().get("centos7-docker");
                    assert centos7 != null;
                    assert centos7.getName().equals("centos7-docker");
                    assert centos7.getSystemName() != null;
                    assert centos7.getStatus().equals("UP");
                    VirtualMachineNode centos6 = inventory.getVirtualMachines().get("centos6-1");
                    assert centos6 != null;
                    assert centos6.getName().equals("centos6-1");
                    assert centos6.getSystemName() != null;
                    assert centos6.getStatus().equals("UP");
                    VirtualMachineNode fedora20 = inventory.getVirtualMachines().get("fedora20-1");
                    assert fedora20 != null;
                    assert fedora20.getName().equals("fedora20-1");
                    assert fedora20.getSystemName() != null;
                    assert fedora20.getStatus().equals("UP");
                    VirtualMachineNode fedora19 = inventory.getVirtualMachines().get("fedora19-1");
                    assert fedora19 != null;
                    assert fedora19.getName().equals("fedora19-1");
                    assert fedora19.getSystemName() != null;
                    assert fedora19.getStatus().equals("UP");
                    break;
                }
                case icehouse: {
                    InventoryContainerNode hypervisor = inventory.getHypervisors().get(ServerConfigurator.ICEHOUSE_OPENSTACK_SERVER_NAME);
                    assert hypervisor != null;
                    assert hypervisor.getName().equals(ServerConfigurator.ICEHOUSE_OPENSTACK_SERVER_NAME);
                    assert inventory.getVirtualMachines().size() >= 1;

                    break;
                }
            }
            management.closeConnection();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            if (config != null)
                configurationService.deleteConfiguration(config);
        }
    }

    @Test
    public void openStackMetricsTest() throws Exception {
        OpenStackConfiguration config = null;
        try {
            config = new OpenStackConfiguration();
            switch(openStackTestType) {
                case mirantis:
                    ServerConfigurator.setupOpenStackMirantisConnection(config.getConnection());
                    break;
                case juno:
                    ServerConfigurator.setupOpenStackJunoConnection(config.getConnection());
                    break;
                case icehouse:
                    ServerConfigurator.setupOpenStackIceHouseConnection(config.getConnection());
                    break;
                case kilo:
                    ServerConfigurator.setupOpenStackKiloConnection(config.getConnection());
                    break;
            }
            ServerConfigurator.setupLocalGroundworkServer(config.getGwos());
            configurationService.saveConfiguration(config);
            MonitoringConnector connector = connectorFactory.getMonitoringConnector(config);
            connector.connect(config.getConnection());
            ProfileServiceTest.copyTestProfile(VirtualSystem.OPENSTACK, ProfileServiceTest.TEST_AGENT);
            ProfileMetrics metrics = profileService.readMetrics(config.getCommon().getVirtualSystem(), ProfileServiceTest.TEST_AGENT);
            Metric m1 = metrics.getPrimary().get(0);
            m1.setCustomName("vms"); // running_vms
            Metric m2 = metrics.getSecondary().get(0);
            m2.setCustomName("diskBytes"); // disk.read.bytes
            MonitoringState results = connector.collectMetrics(null, getPrimaryMetrics(metrics), getSecondaryMetrics(metrics), getCustomMetrics(metrics));
            assert results.hosts().size() == 1;
            switch(openStackTestType) {
                case kilo:
                    BaseHost node4 = results.hosts().get("node-4.gwos.com");
                    assert node4 != null;
                    BaseMetric vmsMetric = node4.getMetric("running_vms");
                    assert vmsMetric.getCustomName().equals("vms");
                    assert vmsMetric.getServiceName().equals("vms");
                    BaseVM micro = node4.getVM("gwos-m1-micro");
                    assert micro != null;
                    BaseMetric diskMetric = micro.getMetric("disk.read.bytes");
                    assert diskMetric.getCustomName().equals("diskBytes");
                    assert diskMetric.getServiceName().equals("diskBytes");
                    break;
                case mirantis: {
                    BaseHost host = results.hosts().get(ServerConfigurator.MIRANTIS_OPENSTACK_SERVER_NAME);
                    assert host != null;
                    assert host.getHostName().equals(ServerConfigurator.MIRANTIS_OPENSTACK_SERVER_NAME);
                    assert host.getMetric("free_disk_gb") != null;
                    assert host.getMetric("running_vms") != null;
                    assert host.getMetric("free_ram_mb") != null;
                    BaseVM vm = host.getVMPool().get("rhel7-gwos");
                    assert vm != null;
                    assert vm.getVMName().equals("rhel7-gwos");
                    //assert vm.getMetric("disk.read.bytes") != null;
                    assert vm.getMetric("cpu_util") != null;
                    vm = host.getVMPool().get("rhel7-docker");
                    assert vm != null;
                    assert vm.getVMName().equals("rhel7-docker");
                    //assert vm.getMetric("disk.read.bytes") != null;
                    assert vm.getMetric("cpu_util") != null;
                    break;
                }
                case juno: {
                    BaseHost host = results.hosts().get(ServerConfigurator.JUNO_OPENSTACK_SERVER_NAME);
                    assert host != null;
                    assert host.getHostName().equals(ServerConfigurator.JUNO_OPENSTACK_SERVER_NAME);
                    assert host.getMetric("free_disk_gb") != null;
                    assert host.getMetric("running_vms") != null;
                    assert host.getMetric("free_ram_mb") != null;
                    BaseVM vm = host.getVMPool().get("centos6-1");
                    assert vm != null;
                    assert vm.getVMName().equals("centos6-1");
                    assert vm.getMetric("disk.read.bytes") != null;
                    assert vm.getMetric("cpu_util") != null;
                    assert vm.getMetric("memory") != null;
                    assert vm.getMetric("memory-rss") != null;
                    assert vm.getMetric("memory-actual") != null;
                    assert vm.getMetric("cpu0_time") != null;
                    assert vm.getMetric("tap58ef77e1-f8_rx") != null;
                    assert vm.getMetric("vda_read") != null;
                    assert vm.getMetric("vda_write") != null;
                    assert vm.getMetric("vda_write_req") != null;
                    break;
                }
                case icehouse: {
                    BaseHost host = results.hosts().get(ServerConfigurator.ICEHOUSE_OPENSTACK_SERVER_NAME);
                    assert host != null;
                    assert host.getHostName().equals(ServerConfigurator.ICEHOUSE_OPENSTACK_SERVER_NAME);
                    assert host.getMetric("free_disk_gb") != null;
                    assert host.getMetric("running_vms") != null;
                    assert host.getMetric("free_ram_mb") != null;
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            if (config != null)
                configurationService.deleteConfiguration(config);
            profileService.removeProfile(VirtualSystem.OPENSTACK, ProfileServiceTest.TEST_AGENT);
        }
    }

    @Test
    public void testPatternReplaceGroup() {
        String result = makeConcreteQueryName("syn.cpu(.)_time", "4");
        assert result.equals("syn.cpu4_time");
    }

    public String makeConcreteQueryName(String query, String sequence) {
        Pattern pattern = Pattern.compile("^.*(\\(.+\\)).*");
        Matcher m = pattern.matcher(query);
        if (m.matches()) {
            String full = m.group(0);
            String text = m.group(1);
            return query.replace(text, sequence);
        }
        return null;
    }

}
