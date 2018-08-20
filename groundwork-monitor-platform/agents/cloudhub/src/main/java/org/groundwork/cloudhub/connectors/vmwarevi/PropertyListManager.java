package org.groundwork.cloudhub.connectors.vmwarevi;

import org.groundwork.cloudhub.connectors.ConnectorConstants;
import org.groundwork.cloudhub.metrics.DefaultMetricProvider;
import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class PropertyListManager {

    public static final String PROP_NAME = "name";
    // Host
    public static final String PROP_VM = "vm";
    public static final String PROP_HOST_MODEL = "summary.hardware.model";
    public static final String PROP_HOST_UPTIME = "summary.quickStats.uptime";

    // Virtual Machine
    public static final String PROP_GUEST_NETWORK = "guest.net";
    public static final String PROP_RUNTIME_HOST = "summary.runtime.host";
    public static final String PROP_GUEST_STATE = "guest.guestState";
    public static final String PROP_IP_ADDRESS  = "guest.ipAddress";
    // Host and Virtual Machine
    public static final String PROP_BOOTTIME = "summary.runtime.bootTime";
    public static final String PROP_UPTIME = "summary.quickStats.uptimeSeconds";

    @Autowired
    private VISnapshotService snapshotService;

    /**
     * Returns a set of unique VMWare property names to query on
     *
     * @param queryPool this map gets values added to it
     * @param baseQueries
     * @return unique set of property names
     */
    public Set<String> createPropertyNames(Map<String, BaseQuery> queryPool, List<BaseQuery> baseQueries, DefaultMetricProvider metricProvider) {
        Set<String> queryNames = new HashSet();

        for (BaseQuery query : metricProvider.getDefaultMetricList()) {
            queryPool.put(query.getQuery(), query);
            if (!query.getQuery().startsWith(ConnectorConstants.SYNTHETIC_PREFIX)) {
                queryNames.add(query.getQuery());
            }
        }
        for (BaseQuery query : metricProvider.getDefaultConfigList()) {
            queryPool.put(query.getQuery(), query);
            if (!query.getQuery().startsWith(ConnectorConstants.SYNTHETIC_PREFIX)) {
                queryNames.add(query.getQuery());
            }
        }
        for (BaseQuery query : metricProvider.getDefaultSyntheticList()) {
            queryPool.put(query.getQuery(), query);
            if (!query.getQuery().startsWith(ConnectorConstants.SYNTHETIC_PREFIX)) {
                queryNames.add(query.getQuery());
            }
        }
        // this is LAST in order to OVERRIDE the above defaults
        for (BaseQuery query : baseQueries) {
            if (metricProvider.isMetricPoolable(query)) {
                queryPool.put(query.getQuery(), query);
                if (metricProvider.isMetricCollected(query)) {
                    queryNames.add(query.getQuery());
                }
            }
        }
        return queryNames;
    }

    public void crushMetrics(Map<String, ? extends BaseHost> hostMap, Map<String, BaseQuery> queries) {
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

}