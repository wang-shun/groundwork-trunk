package org.groundwork.cloudhub.connectors.amazon;

import com.amazonaws.services.cloudwatch.model.ListMetricsRequest;
import com.amazonaws.services.cloudwatch.model.ListMetricsResult;
import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.ConnectionState;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.agents.utils.StringUtils;
import org.groundwork.cloudhub.configuration.AmazonConnection;
import org.groundwork.cloudhub.configuration.ConfigurationView;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.configuration.MonitorConnection;
import org.groundwork.cloudhub.connectors.CollectionMode;
import org.groundwork.cloudhub.connectors.ConnectorConstants;
import org.groundwork.cloudhub.connectors.ManagementConnector;
import org.groundwork.cloudhub.connectors.MetricViewDefinitions;
import org.groundwork.cloudhub.connectors.MonitoringConnector;
import org.groundwork.cloudhub.connectors.base.BaseConnector;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.cloudhub.exceptions.RateExceededException;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.InventoryOptions;
import org.groundwork.cloudhub.metrics.*;
import org.groundwork.cloudhub.profile.ProfileService;
import org.groundwork.rs.dto.profiles.CloudHubProfile;
import org.groundwork.rs.dto.profiles.Metric;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * AmazonConnector for connecting with the Amazon EC2
 */
@Service(AmazonConnector.NAME)
@Scope("prototype")
public class AmazonConnector extends BaseConnector implements
        MonitoringConnector, ManagementConnector {

    public static final String NAME = "AmazonConnector";
    public static final String RATE_EXCEEDED_PATTERN = "rate exceeded";
    public static final String RATE_EXCEEDED_EVENT_MESSAGE = "Amazon connector detected rate exceeded event...";

    private static Logger log = Logger.getLogger(AmazonConnector.class);

    private static final int MAXRETRIES = 2; // internal, arbitrary
    private static final long RETRYGAP = 5L * 1000L; // 5 secs...

    private int amazonRetries = 0;
    private ConnectionState connectionState = ConnectionState.NASCENT;

    private AWSConnection awsConnection = null;
    private AWSInventory awsInventory = null;

    @Autowired
    protected MetricsPostProcessor postProcessor;

    @Resource(name = ProfileService.NAME)
    protected ProfileService profileService;

    @Override
    public MonitoringState collectMetrics(MonitoringState priorState,
            List<BaseQuery> hostQueries, List<BaseQuery> vmQueries, List<BaseQuery> customQueries)
            throws ConnectorException {

        try {
            long startTime = System.currentTimeMillis();

            if (priorState == null) {
                priorState = new MonitoringState();
            }
            if (connectionState != ConnectionState.CONNECTED) {
                log.error("collectMetrics(): not connected");
                return priorState;
            }

            awsInventory = new AWSInventory(awsConnection, getCollectionMode(), true);

            MonitoringState monitoringState = new MonitoringState();
            awsInventory.collectMetrics(monitoringState, priorState, hostQueries, vmQueries, customQueries, awsConnection);

            // process synthetics
            List<BaseQuery> ec2Queries = new ArrayList<>();
            List<BaseQuery> rdsQueries = new ArrayList<>();
            List<BaseQuery> elbQueries = new ArrayList<>();
            int count = filterQueries(hostQueries, ec2Queries, rdsQueries, elbQueries);
            if (count > 0) {
                MetricViewDefinitions ec2View = new MetricViewDefinitions(new ConfigurationView("EC2", true, false), ec2Queries, true, true);
                MetricViewDefinitions rdsView = new MetricViewDefinitions(new ConfigurationView("RDS", true, false), rdsQueries, true, true);
                MetricViewDefinitions elbView = new MetricViewDefinitions(new ConfigurationView("ELB", true, false), elbQueries, true, true);
                for (BaseHost host : monitoringState.hosts().values()) {
                    for (BaseVM vm : host.getVMPool().values()) {
                        if (vm.getMetricPool().size() > 0) {
                            AmazonVM amazonVM = (AmazonVM)vm;
                            MetricViewDefinitions view;
                            if (amazonVM.getNodeType().equals(AmazonVM.AmazonNodeType.EC2)) {
                                view = ec2View;
                            }
                            else if (amazonVM.getNodeType().equals(AmazonVM.AmazonNodeType.ELB)) {
                                view = elbView;
                            }
                            else {
                                view = rdsView;
                            }
                            if (view.getQueries().size() > 0) {
                                postProcessor.processSynthetics(vm, view, monitoringState.getState());
                            }
                        }
                    }
                }
            }

            // remove un-monitored metrics
            for (BaseHost host : monitoringState.hosts().values()) {
                for (BaseVM vm : host.getVMPool().values()) {
                    List<String> deletes = new LinkedList<>();
                    for (Map.Entry<String,BaseMetric> metricEntry : vm.getMetricPool().entrySet()) {
                        if (!metricEntry.getValue().isMonitored()) {
                            deletes.add(metricEntry.getKey());
                        }
                    }
                    for (String key : deletes) {
                        vm.getMetricPool().remove(key);
                    }
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("Amazon metrics collection completed in " + (startTime - System.currentTimeMillis()) + " ms");
            }
            return monitoringState;
        }
        catch (ConnectorException ce) {
            String message = ce.getMessage();
            if (message.toLowerCase().contains(RATE_EXCEEDED_PATTERN)) {
                log.error(RATE_EXCEEDED_EVENT_MESSAGE);
                throw new RateExceededException(ce.getMessage(), ce);
            }
            throw ce;
        }
    }

    @Override
    public DataCenterInventory gatherInventory() throws ConnectorException {

        try {
            if (connectionState != ConnectionState.CONNECTED) {
                log.error("gatherInventory(): not connected");
                return null;
            }

            CollectionMode mode = getCollectionMode();
            InventoryOptions options = new InventoryOptions(mode.isDoHosts(), mode.isDoStorageDomains(),
                    mode.isDoNetworks(), mode.isDoResourcePools(), mode.isDoTaggedGroups(), mode.getGroupTag());
            if (awsInventory == null) {
                awsInventory = new AWSInventory(awsConnection, getCollectionMode(), false);
            }
            DataCenterInventory inventory = awsInventory.gatherInventory(options);

            awsInventory = null;
            return inventory;
        }
        catch (ConnectorException ce) {
            String message = ce.getMessage();
            if (message.toLowerCase().contains(RATE_EXCEEDED_PATTERN)) {
                log.error(RATE_EXCEEDED_EVENT_MESSAGE);
                throw new RateExceededException(ce.getMessage(), ce);
            }
            throw ce;
        }
    }

    @Override
    public void connect(MonitorConnection monitorConnection)
            throws ConnectorException {
        try {
            connectionState = ConnectionState.CONNECTING;
            AmazonConnection connection = (AmazonConnection) monitorConnection;
            connect(connection.getUsername(), connection.getPassword(), connection.getServer(), connection.isSslEnabled(), connection.getEnableIAMRoles());
        }
        catch (ConnectorException ce) {
            String message = ce.getMessage();
            if (message.toLowerCase().contains(RATE_EXCEEDED_PATTERN)) {
                log.error(RATE_EXCEEDED_EVENT_MESSAGE);
                throw new RateExceededException(ce.getMessage(), ce);
            }
            throw ce;
        }
    }

    @Override
    public void disconnect() throws ConnectorException {
        connectionState = ConnectionState.DISCONNECTED;

    }

    @Override
    public ConnectionState getConnectionState() {
        return connectionState;
    }

    @Override
    public void openConnection(MonitorConnection monitorConnection)
            throws ConnectorException {
        if (connectionState != ConnectionState.CONNECTED) {
            connect(monitorConnection);
        }

    }

    @Override
    public void closeConnection() throws ConnectorException {
        if (connectionState == ConnectionState.CONNECTED) {
            disconnect();
        }
    }

    private void connect(String accessKey, String secretKey, String endpoint, boolean useSSL, boolean enableIAMRoles) {
        log.debug("Attempting Amazon connection");

        String es = "";

        if (accessKey == null || secretKey.isEmpty())
            es += "{accessKey} needed\n";

        if (secretKey == null || secretKey.isEmpty())
            es += "{secretKey} needed\n";

        if (getConnectionState() == ConnectionState.CONNECTED)
            return;

        if (!es.isEmpty())
            throw new ConnectorException(es);

        awsConnection = new AWSConnection(accessKey, secretKey, endpoint, useSSL, enableIAMRoles);
        String lastException = null;
        Exception exception = null;
        for (amazonRetries = 0; amazonRetries < MAXRETRIES; amazonRetries++) {
            try {
                connectionState = ConnectionState.CONNECTING;
                awsConnection.testConnection();
                connectionState = ConnectionState.CONNECTED;
                lastException = null;
                exception = null;
                break;
            } catch (Exception e) {
                connectionState = ConnectionState.TIMEDOUT;
                lastException = e.getMessage();
                exception = e;
                if (MAXRETRIES > 1) {
                    try {
                        Thread.sleep(RETRYGAP);
                    } catch (Exception ee) {
                    }
                }
            }
        }

        if (lastException != null)
            log.error("Amazon Connect - last exception (" + lastException + ")");

        if (connectionState != ConnectionState.CONNECTED && exception != null) {
            throw new ConnectorException(lastException, exception);
        }
    }

    public AWSConnection getAwsConnection() {
        return awsConnection;
    }

    /***
     * Retrieve the unique custom metrics list (not metric samples)
     * @return
     */
    @Override
    public List<Metric> retrieveCustomMetrics() throws ConnectorException {
        try {
            Set<String> metricMap = new HashSet<>();
            List<Metric> availableMetrics = new ArrayList<>();
            ListMetricsRequest listMetricsRequest = new ListMetricsRequest();
            for (; ; ) {
                ListMetricsResult metrics = getAwsConnection().getMetricsClient().listMetrics(listMetricsRequest);
                for (com.amazonaws.services.cloudwatch.model.Metric metric : metrics.getMetrics()) {
                    if (!metric.getNamespace().startsWith("AWS")) {
                        String metricName = metric.getNamespace() + "." + metric.getMetricName();
                        Metric customMetric = new Metric(metricName,
                                "" /* description */, false /* monitored */, false /* graphed */,
                                -1 /*warningThreshold*/, -1 /* criticalThreshold*/,
                                SourceType.custom.name(), ComputeType.query.name(), null, null, null, null);
                        if (!metricMap.contains(metricName)) {
                            metricMap.add(metricName);
                            availableMetrics.add(customMetric);
                        }
                    }
                }
                String nextBatchId = metrics.getNextToken();
                if (nextBatchId == null) {
                    break;
                }
                listMetricsRequest.setNextToken(nextBatchId);
            }
            return availableMetrics;
        }
        catch (ConnectorException ce) {
            String message = ce.getMessage();
            if (message.toLowerCase().contains(RATE_EXCEEDED_PATTERN)) {
                log.error(RATE_EXCEEDED_EVENT_MESSAGE);
                throw new RateExceededException(ce.getMessage(), ce);
            }
            throw ce;
        }
    }

    @Override
    public List<String> listMetricNames(String serviceType, ConnectionConfiguration configuration) {
        CloudHubProfile profile = (CloudHubProfile) profileService.readProfileTemplate(VirtualSystem.AMAZON);
        List<String> names = new LinkedList<>();
        if (serviceType.equals(AmazonConfigurationProvider.EC2)) {
            for (Metric metric : profile.getHypervisor().getMetrics()) {
                if (StringUtils.isEmpty(metric.getSourceType()) && (metric.getComputeType() == null || !metric.getComputeType().equals(ComputeType.synthetic))) {
                    names.add(metric.getName());
                }
            }
        } else if (serviceType.equals(AmazonConfigurationProvider.STORAGE)) {
            for (Metric metric : profile.getHypervisor().getMetrics()) {
                if (!StringUtils.isEmpty(metric.getSourceType()) && metric.getSourceType().equals(AmazonConfigurationProvider.STORAGE)) {
                    if (metric.getComputeType() == null || !metric.getComputeType().equals(ComputeType.synthetic)) {
                        names.add(metric.getName());
                    }
                }
            }
        } else if (serviceType.equals(AmazonConfigurationProvider.NETWORK)) {
            for (Metric metric : profile.getHypervisor().getMetrics()) {
                if (!StringUtils.isEmpty(metric.getSourceType()) && metric.getSourceType().equals(AmazonConfigurationProvider.NETWORK)) {
                    if (metric.getComputeType() == null || !metric.getComputeType().equals(ComputeType.synthetic)) {
                        names.add(metric.getName());
                    }
                }
            }
        }
        else if (serviceType.equals(AmazonConfigurationProvider.CUSTOM)) {
            for (Metric metric : profile.getCustom().getMetrics()) {
                if (!StringUtils.isEmpty(metric.getSourceType()) && metric.getSourceType().equals(AmazonConfigurationProvider.CUSTOM)) {
                    if (metric.getComputeType() == null || !metric.getComputeType().equals(ComputeType.synthetic)) {
                        names.add(metric.getName());
                    }
                }
            }
        }
        return names;
    }

    protected int filterQueries (List<BaseQuery> hostQueries, List<BaseQuery> ec2Queries, List<BaseQuery> rdsQueries, List<BaseQuery> elbQueries) {
        int syntheticCount = 0;
        for (BaseQuery query : hostQueries) {
            boolean isSynthetic = false;
            if (query.getComputeType() != null && query.getComputeType().equals(ComputeType.synthetic)) {
                syntheticCount = syntheticCount + 1;
                isSynthetic = true;
            }
            if (query.getServiceType() == null || query.getServiceType().equalsIgnoreCase(ConnectorConstants.ENTITY_HYPERVISOR)) {
                ec2Queries.add(query);
            } else if (query.getServiceType().equalsIgnoreCase(AmazonConfigurationProvider.STORAGE)) {
                if (isSynthetic) {
                    if (query.getExpression() == null || query.getExpression().contains(AmazonConfigurationProvider.METRIC_PREFIX_RDS)) {
                        rdsQueries.add(query);
                    } else {
                        ec2Queries.add(query);
                    }
                } else {
                    if (query.getQuery().startsWith(AmazonConfigurationProvider.METRIC_PREFIX_RDS)) {
                        rdsQueries.add(query);
                    } else {
                        ec2Queries.add(query);
                    }
                }
            } else if (query.getServiceType().equalsIgnoreCase(AmazonConfigurationProvider.NETWORK)) {
                elbQueries.add(query);
            } else if (query.getServiceType().equalsIgnoreCase(AmazonConfigurationProvider.CUSTOM)) {
                ec2Queries.add(query);
            }
        }
        return syntheticCount;
    }

}
