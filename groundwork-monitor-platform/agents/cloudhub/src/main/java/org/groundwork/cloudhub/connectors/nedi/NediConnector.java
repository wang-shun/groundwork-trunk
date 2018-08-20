package org.groundwork.cloudhub.connectors.nedi;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.ConnectionState;
import org.groundwork.cloudhub.configuration.ConfigurationView;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.configuration.MonitorConnection;
import org.groundwork.cloudhub.configuration.NediConnection;
import org.groundwork.cloudhub.connectors.MetricViewDefinitions;
import org.groundwork.cloudhub.connectors.base.BaseConnector;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.cloudhub.gwos.GwosStatus;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.InventoryOptions;
import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.BaseMetric;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.groundwork.cloudhub.metrics.ComputeType;
import org.groundwork.cloudhub.metrics.MetricsPostProcessor;
import org.groundwork.cloudhub.metrics.MonitoringState;
import org.groundwork.rs.dto.DtoEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(NediConnector.NAME)
@Scope("prototype")
public class NediConnector extends BaseConnector {
    public static final String NAME = "NediConnector";

    public final static String DEFAULT_POLICY_HOST = "localhost";
    public final static String SERVICE_TYPE_DEVICE = "Nedi";
    public final static String SERVICE_TYPE_POLICY = "NediPolicy";

    private static final String JDBC_URL_TMPL = "jdbc:postgresql://%s:%s/%s?prepareThreshold=1";
    private static long FIVE_MINUTES = 300;   // in seconds

    private static Logger log = Logger.getLogger(NediConnector.class);
    private HikariDataSource dataSource;
    private ConnectionState connectionState = ConnectionState.NASCENT;
    private DataCenterInventory inventory;
    private NediInventoryBrowser inventoryBrowser;

    @Autowired
    private NediDatabaseAccess nediDatabaseAccess;

    private long nediInterval = FIVE_MINUTES; // in seconds, 5 minutes
    private String policyHost = DEFAULT_POLICY_HOST;
    private Boolean monitorDevices = true;
    private Boolean monitorPolicies = true;
    private Boolean monitorEvents = false;

    @Autowired
    protected MetricsPostProcessor postProcessor;

    @Override
    public DataCenterInventory gatherInventory() throws ConnectorException {
        if (!ConnectionState.CONNECTED.equals(connectionState) || !isDataSourceValid(dataSource)) {
            throw new ConnectorException("Nedi connector in invalid state to get inventory");
        }

        if (inventoryBrowser == null) {
            inventoryBrowser = new NediInventoryBrowser(dataSource, policyHost);
        }

        InventoryOptions options = new InventoryOptions(true, false, false, false, false, "");
        inventory = inventoryBrowser.gatherInventory(options);

        return inventory;
    }

    @Override
    public void openConnection(MonitorConnection monitorConnection) throws ConnectorException {
        if (log.isDebugEnabled()) log.debug("Opening connection to " + monitorConnection.getServer());
        if (connectionState != ConnectionState.CONNECTED) {
            connect(monitorConnection);
        }
    }

    @Override
    public void closeConnection() throws ConnectorException {
        if (log.isDebugEnabled()) log.debug("closing connection");
        if (connectionState == ConnectionState.CONNECTED) {
            disconnect();
        }
    }

    @Override
    public void connect(MonitorConnection monitorConnection) throws ConnectorException {
        disconnect();
        long startTime = System.currentTimeMillis();
        connectionState = ConnectionState.CONNECTING;
        NediConnection nediConnection = (NediConnection) monitorConnection;
        nediInterval = nediConnection.getNediInterval();
        policyHost = nediConnection.getPolicyHost();
        dataSource = configDataSource(nediConnection);
        monitorDevices = nediConnection.getMonitorDevices();
        monitorPolicies = nediConnection.getMonitorPolicies();
        monitorEvents = nediConnection.getMonitorEvents();
        if (dataSource != null && !dataSource.isClosed()) {
            try (Connection connection = dataSource.getConnection();
                 Statement ps = connection.createStatement();
                 ResultSet rs = ps.executeQuery("select * from devices")) {
                connectionState = ConnectionState.CONNECTED;
            } catch (Exception e) {
                connectionState = ConnectionState.FAILED;
                throw new ConnectorException(e);
            }
        } else {
            connectionState = ConnectionState.CONNECTED;
        }
        if (log.isDebugEnabled())
            log.debug("Nedi data source is ready in " + (System.currentTimeMillis() - startTime) + " ms");
    }

    @Override
    public void disconnect() throws ConnectorException {
        if (log.isDebugEnabled()) log.debug("Disconnecting from Nedi");
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            dataSource = null;
            inventoryBrowser = null;
        }
        connectionState = ConnectionState.DISCONNECTED;
    }

    @Override
    public ConnectionState getConnectionState() {
        return connectionState;
    }

    private boolean isDataSourceValid(HikariDataSource dataSource) {
        return dataSource != null && !dataSource.isClosed();
    }

    private HikariDataSource configDataSource(NediConnection nediConnection) {
        HikariConfig config = new HikariConfig();

        String jdbcUrl = String.format(JDBC_URL_TMPL, nediConnection.getServer(), nediConnection.getPort(), nediConnection.getDatabase());

        config.setJdbcUrl(jdbcUrl);
        config.setUsername(nediConnection.getUsername());
        config.setPassword(nediConnection.getPassword());
        config.setDriverClassName("org.postgresql.Driver");

        // TODO: remove this property when using a JDBC4 driver
        config.setConnectionTestQuery("select version();");
        config.addDataSourceProperty("connectionTimeout", "30000");
        config.addDataSourceProperty("idleTimeout", "600000");
        config.addDataSourceProperty("maxLifetime", "1800000");
        config.addDataSourceProperty("maximumPoolSize", "10");
        String jboss = System.getProperty("jboss.server.name");
        if (jboss == null) {
            config.setTransactionIsolation("TRANSACTION_NONE");
        }
        return new HikariDataSource(config);
    }

    private GwosStatus getDeviceStatus(String lastOK) {
        GwosStatus hostStatus = GwosStatus.UNREACHABLE;
        try {
            if (lastOK != null && lastOK.length() > 0) {
                long interval = (nediInterval > 0) ? nediInterval : FIVE_MINUTES; // in seconds
                long now = System.currentTimeMillis() / 1000; // now in seconds
                long nediLastOK = Long.parseLong(lastOK); // in seconds
                if ((now - nediLastOK) < interval) {
                    hostStatus = GwosStatus.UP;
                }
            }
        } catch (NumberFormatException nfe) {
            log.error("Fail to convert lastOK value, " + lastOK + ", : " + nfe.getMessage());
        }
        return hostStatus;
    }

    @Override
    public MonitoringState collectMetrics(MonitoringState priorState, List<BaseQuery> hostQueries, List<BaseQuery> vmQueries, List<BaseQuery> customQueries) throws ConnectorException {
        long startTime = System.currentTimeMillis();

        MonitoringState monitoringState = new MonitoringState();
        if (priorState == null) {
            priorState = new MonitoringState();
        }

        Map<String, BaseQuery> policyMetrics = new HashMap<>();

        // collect device metrics
        if (monitorDevices) {
            MetricViewDefinitions view = new MetricViewDefinitions(new ConfigurationView("vm", true, false), vmQueries, true, true);
            List<Map<String, String>> deviceMetrics = nediDatabaseAccess.queryDeviceMetrics(dataSource);
            for (Map<String, String> deviceMap : deviceMetrics) {
                String deviceHostName = deviceMap.get(NediDatabaseAccess.COLUMN_DEVICE);
                NediHost nediHost = new NediHost(deviceHostName);
                BaseHost priorHost = priorState.hosts().get(deviceHostName);
                if (priorHost != null) {
                    nediHost.setPrevRunState(priorHost.getRunState());
                }
                int syntheticCount = 0;
                for (BaseQuery query : vmQueries) {
                    if (query.getServiceType() == null)
                        continue;
                    if (query.getServiceType().equals(SERVICE_TYPE_POLICY)) {
                        policyMetrics.put(query.getQuery(), query);
                        continue;
                    }
                    if (query.getComputeType() != null && query.getComputeType().equals(ComputeType.synthetic)) {
                        syntheticCount = syntheticCount + 1;
                        continue;
                    }
                    BaseMetric metric = new BaseMetric(query.getQuery(),
                            query.getWarning(),
                            query.getCritical(),
                            query.isGraphed(),
                            query.isMonitored(),
                            query.getCustomName());
                    metric.setMetricType(SERVICE_TYPE_DEVICE);
                    String value = deviceMap.get(query.getQuery());

                    if (value != null) {
                        metric.setValueOnly(value);
                        metric.setCurrentState();
                    } else {
                        metric.setValueOnly("");
                        metric.setCurrState(BaseMetric.sWarning);
                        metric.setExplanation("Missing metric from device");
                    }
                    if (priorHost != null) {
                        BaseMetric priorMetric = priorHost.getMetric(query.getQuery());
                        if (priorMetric != null) {
                            metric.setLastState(priorMetric.getCurrState());
                        }
                    }
                    if (!query.isMonitored()) {
                        metric.setConfigFlag(true);
                    }
                    nediHost.putMetric(query.getQuery(), metric);
                }
                GwosStatus deviceStatus = getDeviceStatus(deviceMap.get(NediDatabaseAccess.COLUMN_LASTOK));
                nediHost.setRunningState(deviceStatus.status);
                monitoringState.hosts().put(nediHost.getHostName(), nediHost);
                if (syntheticCount > 0) {
                    postProcessor.processSynthetics(nediHost, view, monitoringState.getState());
                }
            }
        }

        // collect Policies Metrics
        if (monitorPolicies) {
            // TODO: collectPolicyMetricsByConfig(policyMetrics, databaseAccess, monitoringState)
            List<QueryMetricsResult> dbPolicyMetrics = nediDatabaseAccess.queryPolicyMetrics(dataSource);
            NediHost nediHost = new NediHost(policyHost);
            nediHost.setRunningState(GwosStatus.UP.status);
            BaseHost priorHost = priorState.hosts().get(policyHost);
            if (priorHost != null) {
                nediHost.setPrevRunState(priorHost.getRunState());
            }
            for (QueryMetricsResult qmr : dbPolicyMetrics) {
                BaseMetric metric = new BaseMetric(qmr.getName(), -1, -1, false, true, "");
                metric.setValueOnly(qmr.getValue());
                metric.setExplanation(qmr.getExtra());
                metric.setCurrState(qmr.getState().status);
                metric.setMetricType(SERVICE_TYPE_POLICY);
                if (priorHost != null) {
                    BaseMetric priorMetric = priorHost.getMetric(qmr.getName());
                    if (priorMetric != null) {
                        metric.setLastState(priorMetric.getCurrState());
                    }
                }
                nediHost.putMetric(qmr.getName(), metric);
            }
            if (nediHost.getMetricPool().size() > 0) {
                monitoringState.hosts().put(nediHost.getHostName(), nediHost);
            }
        }

        // collect events
        if (monitorEvents) {
            List<DtoEvent> events = nediDatabaseAccess.findRecentEvents(dataSource);
            // TODO: send events
        }

        if (log.isInfoEnabled()) {
            log.info("Nedi collectMetrics completed in " + (System.currentTimeMillis() - startTime) + " ms");
        }
        return monitoringState;
    }

    // TODO: we'll probably want to discover policy metrics dynamically
    private void collectPolicyMetricsByConfig(Map<String, BaseQuery> policyMetrics, NediDatabaseAccess databaseAccess, MonitoringState monitoringState, MonitoringState priorState) {
        if (policyMetrics.size() > 0) {
            List<QueryMetricsResult> dbPolicyMetrics = databaseAccess.queryPolicyMetrics(dataSource);
            NediHost nediHost = new NediHost(policyHost);
            nediHost.setRunningState(GwosStatus.UP.status);
            BaseHost priorHost = priorState.hosts().get(policyHost);
            if (priorHost != null) {
                nediHost.setPrevRunState(priorHost.getRunState());
            }
            for (QueryMetricsResult qmr : dbPolicyMetrics) {
                BaseQuery query = policyMetrics.get(qmr.getName());
                if (query != null) {
                    BaseMetric metric = new BaseMetric(query.getQuery(),
                            query.getWarning(),
                            query.getCritical(),
                            query.isGraphed(),
                            query.isMonitored(),
                            query.getCustomName());
                    metric.setMetricType(SERVICE_TYPE_POLICY);
                    metric.setValueOnly(qmr.getValue());
                    metric.setExplanation(qmr.getExtra());
                    metric.setCurrState(qmr.getState().status);
                    if (priorHost != null) {
                        BaseMetric priorMetric = priorHost.getMetric(qmr.getName());
                        if (priorMetric != null) {
                            metric.setLastState(priorMetric.getCurrState());
                        }
                    }
                    nediHost.putMetric(qmr.getName(), metric);
                }
            }
            if (nediHost.getMetricPool().size() > 0) {
                monitoringState.hosts().put(nediHost.getHostName(), nediHost);
            }
        }

    }

    private static final List<String> OOTB_NEDI_SERVICES = new ArrayList() {
        {
            add("cpu");
            add("memcpu");
            add("temp");
            add("latency");
            add("latmax");
            add("latavg");
        }

    };

    @Override
    public List<String> listMetricNames(String serviceType, ConnectionConfiguration configuration) {

        List<String> services = new ArrayList<>(OOTB_NEDI_SERVICES);
        /*
        NediDatabaseAccess databaseAccess = new NediDatabaseAccess();
        List<QueryMetricsResult> queryResults = databaseAccess.queryPolicyMetrics(dataSource);
        for (QueryMetricsResult metric : queryResults) {
            services.add(metric.getName());
        }
        */
        return services;
    }
}
