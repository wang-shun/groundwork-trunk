package org.groundwork.cloudhub.connectors;

import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.AbstractAgentTest;
import org.groundwork.cloudhub.ServerConfigurator;
import org.groundwork.cloudhub.configuration.DockerConfiguration;
import org.groundwork.cloudhub.configuration.ProfileServiceTest;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.InventoryContainerNode;
import org.groundwork.cloudhub.inventory.VirtualMachineNode;
import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.BaseVM;
import org.groundwork.cloudhub.metrics.MonitoringState;
import org.groundwork.cloudhub.monitor.MonitorAgentConfiguration;
import org.groundwork.cloudhub.profile.ProfileMetrics;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MonitorAgentConfiguration.class})
/**
 *
 * Running CAdvisor locally and exposing port 9292
 *
 sudo docker run \
 --volume=/:/rootfs:ro \
 --volume=/var/run:/var/run:rw \
 --volume=/sys:/sys:ro \
 --volume=/var/lib/docker/:/var/lib/docker:ro \
 --publish=9292:8080 \
 --detach=true \
 --name=cadvisor \
 google/cadvisor:latest
 */
public class DockerTest extends AbstractAgentTest {

    @Test
    public void dockerInventoryTest() throws Exception {
        DockerConfiguration doc = null;
        try {
            doc = new DockerConfiguration();
            ServerConfigurator.setupDockerConnection(doc.getConnection());
            //doc.getConnection().setServer("dstmachine:9292");
            ServerConfigurator.setupLocalGroundworkServer(doc.getGwos());
            configurationService.saveConfiguration(doc);
            MonitoringConnector connector = connectorFactory.getMonitoringConnector(doc);
            ManagementConnector management = connectorFactory.getManagementConnector(doc);
            connector.connect(doc.getConnection());
            management.openConnection(doc.getConnection());
            DataCenterInventory inventory = management.gatherInventory();
            assert inventory.getHypervisors().size() == 1;
            InventoryContainerNode hypervisor = inventory.getHypervisors().get(ServerConfigurator.DOCKER_SERVER_WITHOUT_PORT);
            assert hypervisor.getName().equals(ServerConfigurator.DOCKER_SERVER_WITHOUT_PORT);
            assert inventory.getVirtualMachines().size() >= 1;
            // look for BoxSpy
            VirtualMachineNode cAdvisor = inventory.getVirtualMachines().get("dev1-cadvisor");
            assert cAdvisor.getSystemName().equals("cadvisor");
            assert cAdvisor.getName().equals("dev1-cadvisor");

            VirtualMachineNode nginx = inventory.getVirtualMachines().get("dev1-hello-world-nginx");
            assert nginx.getSystemName().equals("hello-world-nginx");
            assert nginx.getName().equals("dev1-hello-world-nginx");

            VirtualMachineNode jetmysql = inventory.getVirtualMachines().get("dev1-jetmysql");
            assert jetmysql.getSystemName().equals("jetmysql");
            assert jetmysql.getName().equals("dev1-jetmysql");

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            if (doc != null)
                configurationService.deleteConfiguration(doc);
        }
    }

    @Test
    public void dockerMetricsTest() throws Exception {
        DockerConfiguration doc = null;
        try {
            doc = new DockerConfiguration();
            ServerConfigurator.setupDockerConnection(doc.getConnection());
            ServerConfigurator.setupLocalGroundworkServer(doc.getGwos());
            configurationService.saveConfiguration(doc);
            MonitoringConnector connector = connectorFactory.getMonitoringConnector(doc);
            connector.connect(doc.getConnection());
            ProfileServiceTest.copyTestProfile(VirtualSystem.DOCKER, ProfileServiceTest.TEST_AGENT);
            ProfileMetrics metrics = profileService.readMetrics(doc.getCommon().getVirtualSystem(), ProfileServiceTest.TEST_AGENT);
            MonitoringState results =
                    connector.collectMetrics(null, getPrimaryMetrics(metrics), getSecondaryMetrics(metrics), getCustomMetrics(metrics));
            assert results.hosts().size() == 1;
            BaseHost host = results.hosts().get(ServerConfigurator.DOCKER_SERVER_WITHOUT_PORT);
            assert host != null;
            assert host.getHostName().equals(ServerConfigurator.DOCKER_SERVER_WITHOUT_PORT);
            assert host.getMetric("syn.memory.usage") != null;
            assert host.getMetric("cpu.usage.total") != null;
            BaseVM vm = host.getVMPool().get("dev1-cadvisor");
            assert vm != null;
            assert vm.getVMName().equals("dev1-cadvisor");
            assert vm.getSystemName().equals("cadvisor");
            assert vm.getMetric("cpu.usage.total") != null;
            assert vm.getMetric("syn.diskio.ratio") != null;
            assert vm.getMetric("syn.memory.usage") != null;
            assert vm.getMetric("syn.memory.working_set") != null;
            assert vm.getMetric("network.rx_packets") != null;
            assert vm.getMetric("network.tx_packets") != null;

            BaseVM vm2 = host.getVMPool().get("dev1-jetmysql");
            BaseVM bash = host.getVMPool().get("dev1-bash");

            double dockerUsage = Double.parseDouble(host.getMetric("cpu.usage.total").getCurrValue());
            double cadvisorUsage = Double.parseDouble(vm.getMetric("cpu.usage.total").getCurrValue());
            double sqlUsage = Double.parseDouble(vm2.getMetric("cpu.usage.total").getCurrValue());
            double bashUsage = Double.parseDouble(bash.getMetric("cpu.usage.total").getCurrValue());

            //System.out.println("usage(user) cadvisor = " + (userAdvisor / userDocker));
            System.out.println("usage(total) docker = " + dockerUsage);
            System.out.println("usage(total) cadvisor = " + cadvisorUsage);
            System.out.println("usage(total) sql = " + sqlUsage);
            System.out.println("usage(total) bash = " + bashUsage);
            
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            if (doc != null)
                configurationService.deleteConfiguration(doc);
            profileService.removeProfile(VirtualSystem.DOCKER, ProfileServiceTest.TEST_AGENT);
        }
    }

}
