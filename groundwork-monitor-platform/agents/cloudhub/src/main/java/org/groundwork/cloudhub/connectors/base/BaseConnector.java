package org.groundwork.cloudhub.connectors.base;

import org.apache.log4j.Logger;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.configuration.MonitorConnection;
import org.groundwork.cloudhub.connectors.CollectionMode;
import org.groundwork.cloudhub.connectors.ManagementConnector;
import org.groundwork.cloudhub.connectors.MonitoringConnector;
import org.groundwork.cloudhub.exceptions.CloudHubException;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.BaseMetric;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.groundwork.cloudhub.metrics.BaseVM;
import org.groundwork.cloudhub.metrics.ExtendedSynthetic;
import org.groundwork.cloudhub.metrics.MonitoringState;
import org.groundwork.rs.dto.profiles.CloudHubProfile;
import org.groundwork.rs.dto.profiles.ContainerProfile;
import org.groundwork.rs.dto.profiles.HubProfile;
import org.groundwork.rs.dto.profiles.Metric;
import org.groundwork.rs.dto.profiles.NetHubProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class BaseConnector implements MonitoringConnector, ManagementConnector {

    private static Logger log = Logger.getLogger(BaseConnector.class);

    protected CollectionMode collectionMode = new CollectionMode(true, true, false, false, false, false, false);

    @Override
    public HubProfile readProfile() throws CloudHubException {
        // by default no profile available from connector
        return null;
    }

    @Override
    public CloudHubProfile readCloudProfile() throws CloudHubException {
        return (CloudHubProfile) readProfile();
    }

    @Override
    public NetHubProfile readNetworkProfile() throws CloudHubException {
        return (NetHubProfile) readProfile();
    }

    @Override
    public ContainerProfile readContainerProfile() throws CloudHubException {
        return (ContainerProfile) readProfile();
    }

    @Override
    public List<Metric> retrieveCustomMetrics() throws ConnectorException {
        return null;
    }

    public void setCollectionMode(CollectionMode mode) {
        this.collectionMode = mode;
    }

    public CollectionMode getCollectionMode() {
        return collectionMode;
    }

    public String queryConnectorInfo(String tag) {
        return "not supported";
    }

    protected void computePrimarySynthetics(Map<String, BaseQuery> queryPool,
                                            BaseHost host, ExtendedSynthetic synthetic, BaseHost priorHost) {
        BaseQuery vbq = queryPool.get(synthetic.getHandle());
        if (vbq != null && vbq.isMonitored()) {
            BaseMetric vbm = new BaseMetric(
                    vbq.getQuery(),
                    vbq.getWarning(),
                    vbq.getCritical(),
                    vbq.isGraphed(),
                    vbq.isMonitored(),
                    vbq.getCustomName()
            );
            String value1 = host.getValueByKey(synthetic.getLookup1());
            String value2 = host.getValueByKey(synthetic.getLookup2());
            String result = String.valueOf(synthetic.compute(value1, value2)) + ((synthetic.isPercent()) ? "%" : "");
            vbm.setValue(result);

            if (priorHost != null) {
                BaseMetric priorMetric = priorHost.getMetric(synthetic.getHandle());
                if (priorMetric != null) {
                    vbm.setLastState(priorMetric.getCurrState());
                }
            }

            if (vbq.isTraced())
                vbm.setTrace();

            host.putMetric(vbq.getQuery(), vbm);
        }
    }

    protected void computeSecondarySynthetics(Map<String, ? extends BaseQuery> queryPool,
                                              BaseVM vm, ExtendedSynthetic synthetic, BaseVM priorVM) {
        BaseQuery vbq = queryPool.get(synthetic.getHandle());
        if (vbq != null && vbq.isMonitored()) {
            BaseMetric vbm = new BaseMetric(
                    vbq.getQuery(),
                    vbq.getWarning(),
                    vbq.getCritical(),
                    vbq.isGraphed(),
                    vbq.isMonitored(),
                    vbq.getCustomName()
            );
            String value1 = vm.getValueByKey(synthetic.getLookup1());
            String value2 = vm.getValueByKey(synthetic.getLookup2());
            String result = String.valueOf(synthetic.compute(value1, value2)) + ((synthetic.isPercent()) ? "%" : "");
            vbm.setValue(result);

            if (priorVM != null) {
                BaseMetric priorMetric = priorVM.getMetric(synthetic.getHandle());
                if (priorMetric != null) {
                    vbm.setLastState(priorMetric.getCurrState());
                }
            }

            if (vbq.isTraced())
                vbm.setTrace();

            vm.putMetric(vbq.getQuery(), vbm);
        }
    }

    protected int pushDownMetrics(MonitoringState hostPool,
                                  Map<String, BaseQuery> monitoredHostMetrics,
                                  Map<String, ? extends BaseQuery> monitoredVmMetrics) {
        int count = 0;
        for (BaseHost host : hostPool.hosts().values()) {
            for (BaseMetric metric : host.getMetricPool().values()) {
                String metricBaseName = stripServiceNamePrefix(metric.getQuerySpec());
                if (monitoredHostMetrics.get(metricBaseName) == null) {
                    host.getMetricPool().remove(metric.getQuerySpec());
                    count++;
                }
            }
            for (BaseVM vm : host.getVMPool().values()) {
                for (BaseMetric metric : vm.getMetricPool().values()) {
                    String metricBaseName = stripServiceNamePrefix(metric.getQuerySpec());
                    if (monitoredVmMetrics.get(metricBaseName) == null) {
                        vm.getMetricPool().remove(metric.getQuerySpec());
                        count++;
                    }
                }
            }
        }

        if (log.isDebugEnabled()) {
            for (BaseHost host : hostPool.hosts().values()) {
                for (BaseMetric metric : host.getMetricPool().values()) {
                    if (metric.isMonitored()) {
                        log.debug("hostmetric: " + metric.getQuerySpec());
                    }
                }
                for (BaseVM vm : host.getVMPool().values()) {
                    for (BaseMetric metric : vm.getMetricPool().values()) {
                        if (metric.isMonitored()) {
                            log.debug("vmmetric: " + metric.getQuerySpec());
                        }
                    }
                }
            }
        }
        return count;
    }

    /**
     * Strip off service name prefix and return only metric name
     * Default implementation does nothing
     *
     * @param fullService
     * @return pure metric name with service prefix stripped
     */
    protected String stripServiceNamePrefix(String fullService) {
        return fullService;
    }

    protected void crushMetrics(Map<String, ? extends BaseHost> hostMap, Map<String, BaseQuery> queries) {
        for (BaseHost host : hostMap.values()) {
            for (String metricName : host.getMetricPool().keySet())
                if (!host.getMetric(metricName).isMonitored() || !queries.containsKey(metricName)) {
                    host.getMetricPool().remove(metricName);
                }

            for (String configName : host.getConfigPool().keySet())
                if (!host.getConfig(configName).isMonitored()) {
                    host.getConfigPool().remove(configName);
                }
        }
    }

    public void testConnection(MonitorConnection monitorConnection) throws ConnectorException {
        connect(monitorConnection);
    }

    @Override
    public List<String> listMetricNames(String serviceType, ConnectionConfiguration configuration) {
        return new ArrayList<>();
    }
}
