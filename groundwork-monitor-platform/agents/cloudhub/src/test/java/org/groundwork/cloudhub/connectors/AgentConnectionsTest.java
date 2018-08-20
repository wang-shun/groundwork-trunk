package org.groundwork.cloudhub.connectors;

import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.AbstractAgentTest;
import org.groundwork.cloudhub.ServerConfigurator;
import org.groundwork.cloudhub.configuration.*;
import org.groundwork.cloudhub.monitor.MonitorAgentConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MonitorAgentConfiguration.class})
public class AgentConnectionsTest extends AbstractAgentTest {

    private static Logger log = Logger.getLogger(AgentConnectionsTest.class);

    @Test
    public void vmWareConnectionTest() {
        VmwareConfiguration vmware = ServerConfigurator.createVmwareVermontServer(configurationService);
        MonitoringConnector connector = connectorFactory.getMonitoringConnector(vmware);
        try {
            long start = System.currentTimeMillis();
            log.info("Connecting to Vmware Vermont Server ...");
            connector.connect(vmware.getConnection());
            connector.disconnect();
            log.info("... Vmware connection test completed, total time ms: " + (System.currentTimeMillis() - start));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            configurationService.deleteConfiguration(vmware);
        }
    }

    // @Test -- DISABLED until we can get RHEV server running
    public void rhevConnectionTest() {
        RedhatConfiguration rhev = ServerConfigurator.createRedhatServer(configurationService);
        MonitoringConnector connector = connectorFactory.getMonitoringConnector(rhev);
        try {
            long start = System.currentTimeMillis();
            log.info("Connecting to Redhat ...");
            connector.connect(rhev.getConnection());
            connector.disconnect();
            log.info("... Redhat connection test completed, total time ms: " + (System.currentTimeMillis() - start));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            configurationService.deleteConfiguration(rhev);
        }
    }

    // @Test - This server is not always available and reliable
    public void thunTest() {
        VmwareConfiguration vmware = configurationService.createConfiguration(VirtualSystem.VMWARE);
        vmware.getCommon().setDisplayName("Thun");
        vmware.getConnection().setUsername("vmware-dev");
        vmware.getConnection().setPassword("m3t30r1t3");
        vmware.getConnection().setServer("thun.groundwork.groundworkopensource.com");
        vmware.getConnection().setSslEnabled(true);
        vmware.getConnection().setUri("sdk");
        ServerConfigurator.enableAllViews(vmware.getCommon());
        configurationService.saveConfiguration(vmware);
        MonitoringConnector connector = connectorFactory.getMonitoringConnector(vmware);
        try {
            long start = System.currentTimeMillis();
            log.info("Connecting to Vmware Thun Server ...");
            connector.connect(vmware.getConnection());
            connector.disconnect();
            log.info("... Vmware Thun connection test completed, total time ms: " + (System.currentTimeMillis() - start));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            configurationService.deleteConfiguration(vmware);
        }
    }

    @Test
    public void amazonConnectionTest() {
        AmazonConfiguration amazon = ServerConfigurator.createAmazonServer(configurationService);
        MonitoringConnector connector = connectorFactory.getMonitoringConnector(amazon);
        try {
            long start = System.currentTimeMillis();
            log.info("Connecting to Amazon Server ...");
            connector.connect(amazon.getConnection());
            connector.disconnect();
            log.info("... Amazon connection test completed, total time ms: " + (System.currentTimeMillis() - start));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            configurationService.deleteConfiguration(amazon);
        }
    }

    @Test
    public void netAppConnectionTest() {
        NetAppConfiguration netapp = ServerConfigurator.createNetAppServer(configurationService);
        MonitoringConnector connector = connectorFactory.getMonitoringConnector(netapp);
        try {
            long start = System.currentTimeMillis();
            log.info("Connecting to NetApp Cluster Manager ...");
            connector.connect(netapp.getConnection());
            connector.disconnect();
            log.info("... NetApp connection test completed, total time ms: " + (System.currentTimeMillis() - start));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            configurationService.deleteConfiguration(netapp);
        }
    }

    @Test
    public void clouderaConnectionTest() {
        ClouderaConfiguration cloudera = ServerConfigurator.createClouderaServer(configurationService);
        MonitoringConnector connector = connectorFactory.getMonitoringConnector(cloudera);
        try {
            long start = System.currentTimeMillis();
            log.info("Connecting to Cloudera Manager ...");
            connector.connect(cloudera.getConnection());
            connector.disconnect();
            log.info("... Cloudera connection test completed, total time ms: " + (System.currentTimeMillis() - start));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            configurationService.deleteConfiguration(cloudera);
        }
    }


    protected static final String AWS_EC2 = "AWS/EC2";

    @Test
    public void splitTest() {
        String metric = AWS_EC2;
        if (metric.indexOf('/') >= 0) {
            String prefix = metric.split("/")[1];
            assert prefix.equals("EC2");
        }
        else {
            String prefix = metric;
        }
        metric = "info";
        if (metric.indexOf('/') >= 0) {
            String prefix = metric.split("/")[1];
            assert prefix.equals("EC2");
        }
        else {
            String prefix = metric;
            assert prefix.equals("info");
        }

    }
}
