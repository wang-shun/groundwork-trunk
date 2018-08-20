package org.groundwork.cloudhub.connectors;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.AbstractAgentTest;
import org.groundwork.cloudhub.ServerConfigurator;
import org.groundwork.cloudhub.configuration.NediConfiguration;
import org.groundwork.cloudhub.configuration.ProfileServiceTest;
import org.groundwork.cloudhub.connectors.nedi.NediDatabaseAccess;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.BaseMetric;
import org.groundwork.cloudhub.metrics.MonitoringState;
import org.groundwork.cloudhub.monitor.MonitorAgentConfiguration;
import org.groundwork.cloudhub.profile.ProfileMetrics;
import org.groundwork.cloudhub.synthetics.Synthetics;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

/**
 * Created by justinchen on 3/2/18.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MonitorAgentConfiguration.class})

public class NediTest extends AbstractAgentTest {
    private static Logger log = Logger.getLogger(NediTest.class);

    @Autowired
    protected Synthetics synthetics;

    @Test(expected = HikariPool.PoolInitializationException.class)
    public void NediConnectionTestNegative() throws Exception {
        NediConfiguration config = null;
        MonitoringConnector connector = null;
        try {
            config = new NediConfiguration();
            ServerConfigurator.setupNediConnection(config.getConnection());
            config.getConnection().setServer("localhost");
            config.getConnection().setDatabase("nedi");
            config.getConnection().setUsername("postgresInvalid");
            config.getConnection().setPassword("postgresInvalid");

            ServerConfigurator.setupLocalGroundworkServer(config.getGwos());
            configurationService.saveConfiguration(config);
            connector = connectorFactory.getMonitoringConnector(config);

            connector.testConnection(config.getConnection());

        } finally {
            if (connector != null) {
                connector.disconnect();
            }
            if (config != null) {
                configurationService.deleteConfiguration(config);
            }
            profileService.removeProfile(VirtualSystem.NEDI, ProfileServiceTest.TEST_AGENT);
        }
    }

    @Test
    public void NediMetricsTest() throws Exception {
        NediConfiguration config = null;
        MonitoringConnector connector = null;
        try {
            config = new NediConfiguration();
            ServerConfigurator.setupNediConnection(config.getConnection());
            config.getConnection().setServer("localhost");
            config.getConnection().setDatabase("nedi2");
            config.getConnection().setUsername("postgres");
            config.getConnection().setPassword("postgres");

            ServerConfigurator.setupLocalGroundworkServer(config.getGwos());
            configurationService.saveConfiguration(config);
            connector = connectorFactory.getMonitoringConnector(config);

            connector.connect(config.getConnection());

            CollectionMode mode = new CollectionMode(true, true, false, false, false, false, false);
            connector.setCollectionMode(mode);
            ProfileServiceTest.copyTestProfile(VirtualSystem.NEDI, ProfileServiceTest.TEST_AGENT);
            ProfileMetrics metrics = profileService.readMetrics(config.getCommon().getVirtualSystem(), ProfileServiceTest.TEST_AGENT);

            MonitoringState monitoringState = null;
            monitoringState = connector.collectMetrics(monitoringState, getPrimaryMetrics(metrics), getSecondaryMetrics(metrics), getCustomMetrics(metrics));

            Map<String, BaseHost> hosts = monitoringState.hosts();
            assert hosts.size() > 0;

            for (String hostName : hosts.keySet()) {
                BaseHost host = hosts.get(hostName);

                Map<String, BaseMetric> baseMetrics = host.getMetricPool();
                assert baseMetrics != null;
                assert baseMetrics.size() > 0;

                System.out.println("Device : " + hostName);
                for (String metricKey : baseMetrics.keySet()) {
                    BaseMetric metric = baseMetrics.get(metricKey);
                    assert metric.getCurrValue() != null;
                    if (hostName.equals("localhost")) {
                        assert metric.getExplanation().length() > 0;
                        System.out.println("\tMetric = " + metricKey + ", extra = " + metric.getExplanation());
                    }
                    else {
                        assert metric.getCurrValue().length() > 0;
                        System.out.println("\tMetric = " + metricKey + ", value = " + metric.getCurrValue());
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            if (connector != null) {
                connector.disconnect();
            }
            if (config != null) {
                configurationService.deleteConfiguration(config);
            }
            profileService.removeProfile(VirtualSystem.NEDI, ProfileServiceTest.TEST_AGENT);
        }
    }

    @Test
    public void NediInventoryTest() throws Exception {
        NediConfiguration config = null;
        ManagementConnector connector = null;
        try {
            config = new NediConfiguration();
            ServerConfigurator.setupNediConnection(config.getConnection());
            config.getConnection().setServer("localhost");
            config.getConnection().setDatabase("nedi2");
            config.getConnection().setUsername("postgres");
            config.getConnection().setPassword("postgres");

            ServerConfigurator.setupLocalGroundworkServer(config.getGwos());
            configurationService.saveConfiguration(config);
            connector = connectorFactory.getManagementConnector(config);

            connector.openConnection(config.getConnection());
            DataCenterInventory inventory = connector.gatherInventory();

            assert inventory != null;
            assert inventory.getHypervisors() != null;
            assert inventory.getHypervisors().size() > 0;
            //assert inventory.getAllHosts() != null;
            //assert inventory.getAllHosts().size() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } finally {
            if (connector != null) {
                connector.closeConnection();
            }
            if (config != null) {
                configurationService.deleteConfiguration(config);
            }
            profileService.removeProfile(VirtualSystem.NEDI, ProfileServiceTest.TEST_AGENT);
        }
    }

    @Test
    public void updatePolicyTimes() {
        HikariConfig config = new HikariConfig();
        String jdbcUrl = "jdbc:postgresql://localhost:5432/nedi4?prepareThreshold=1";
        config.setJdbcUrl(jdbcUrl);
        config.setUsername("postgres");
        config.setPassword("postgres");
        config.setDriverClassName("org.postgresql.Driver");

        // TODO: remove this property when using a JDBC4 driver
        config.setConnectionTestQuery("select version();");
        config.setTransactionIsolation("TRANSACTION_NONE");
        DataSource ds = new HikariDataSource(config);

        NediDatabaseAccess databaseAccess = new NediDatabaseAccess();
        int count = databaseAccess.updateAllDevicesTimeStamp(ds);
        assert count > 0;

        List<Map<String,String>> results = databaseAccess.queryDeviceMetrics(ds);
        for (Map<String,String> result : results) {
            Long lastdis = Long.parseLong(result.get("lastok"));
            long interval = 300; // 5 minutes, 300 seconds
            long now = System.currentTimeMillis() / 1000;
            long minutes = (now - lastdis) / 60;
            if ((now - lastdis) < interval) {
                System.out.println("less than: " + (minutes));
            }
            else {
                System.out.println("out of range: " + (minutes));
            }
         }
    }

}
