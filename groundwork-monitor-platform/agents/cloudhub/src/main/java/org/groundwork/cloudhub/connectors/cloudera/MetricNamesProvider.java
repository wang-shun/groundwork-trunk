package org.groundwork.cloudhub.connectors.cloudera;

import com.cloudera.api.model.ApiMetricSchema;
import com.cloudera.api.model.ApiMetricSchemaList;
import com.cloudera.api.v4.TimeSeriesResource;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class MetricNamesProvider {

    private static Logger log = Logger.getLogger(org.groundwork.cloudhub.connectors.cloudera.MetricNamesProvider.class);

    /**
     * expire the name cache after NAME_CACHE_EXPIRATION_HOURS hours.
     * The purpose is to limit excessive metric name cache generation computations when the end user
     * is building a metric configuration in the UI
     */
    private DateTime lastCacheLoad = null;
    public static final int NAME_CACHE_EXPIRATION_HOURS = 12;

    private Map<String, List<String>> metricsPerView = new HashMap<String, List<String>>() {
        {
            put(ClouderaConfigurationProvider.CLOUDERA_CLUSTER, new ArrayList<String>());
            put(ClouderaConfigurationProvider.CLOUDERA_HOST, new ArrayList<String>());
            put(ClouderaConfigurationProvider.CLOUDERA_SERVICE_HBASE, new ArrayList<String>());
            put(ClouderaConfigurationProvider.CLOUDERA_SERVICE_HDFS, new ArrayList<String>());
            put(ClouderaConfigurationProvider.CLOUDERA_SERVICE_HIVE, new ArrayList<String>());
            put(ClouderaConfigurationProvider.CLOUDERA_SERVICE_HUE, new ArrayList<String>());
            put(ClouderaConfigurationProvider.CLOUDERA_SERVICE_IMPALA, new ArrayList<String>());
            put(ClouderaConfigurationProvider.CLOUDERA_SERVICE_KAFKA, new ArrayList<String>());
            put(ClouderaConfigurationProvider.CLOUDERA_SERVICE_KSINDEXER, new ArrayList<String>());
            put(ClouderaConfigurationProvider.CLOUDERA_SERVICE_OOZIE, new ArrayList<String>());
            put(ClouderaConfigurationProvider.CLOUDERA_SERVICE_SOLR, new ArrayList<String>());
            put(ClouderaConfigurationProvider.CLOUDERA_SERVICE_SPARK, new ArrayList<String>());
            put(ClouderaConfigurationProvider.CLOUDERA_SERVICE_YARN, new ArrayList<String>());
            put(ClouderaConfigurationProvider.CLOUDERA_SERVICE_ZOOKEEPER, new ArrayList<String>());
        }
    };

    /**
     * Health Checks as of version v14
     * updated 2017-09-11
     */
    private Map<String, List<String>> healthChecksPerView = new HashMap<String, List<String>>() {
        {
            put(ClouderaConfigurationProvider.CLOUDERA_CLUSTER, new ArrayList<String>() {
            });
            put(ClouderaConfigurationProvider.CLOUDERA_HOST, new ArrayList<String>() {
                {
                    add("HOST_AGENT_LOG_DIRECTORY_FREE_SPACE");
                }

                {
                    add("HOST_AGENT_PARCEL_DIRECTORY_FREE_SPACE");
                }

                {
                    add("HOST_AGENT_PROCESS_DIRECTORY_FREE_SPACE");
                }

                {
                    add("HOST_CLOCK_OFFSET");
                }

                {
                    add("HOST_DNS_RESOLUTION");
                }

                {
                    add("HOST_MEMORY_SWAPPING");
                }

                {
                    add("HOST_NETWORK_FRAME_ERRORS");
                }

                {
                    add("HOST_NETWORK_INTERFACES_SLOW_MODE");
                }

                {
                    add("HOST_SCM_HEALTH");
                }
            });
            put(ClouderaConfigurationProvider.CLOUDERA_SERVICE_HBASE, new ArrayList<String>() {
                {
                    add("HBASE_MASTER_HEALTH");
                }

                {
                    add("HBASE_REGION_SERVERS_HEALTHY");
                }
            });
            put(ClouderaConfigurationProvider.CLOUDERA_SERVICE_HDFS, new ArrayList<String>() {
                {
                    add("HDFS_BLOCKS_WITH_CORRUPT_REPLICAS");
                }

                {
                    add("HDFS_CANARY_HEALTH");
                }

                {
                    add("HDFS_DATA_NODES_HEALTHY");
                }

                {
                    add("HDFS_FREE_SPACE_REMAINING");
                }

                {
                    add("HDFS_HA_NAMENODE_HEALTH");
                }

                {
                    add("HDFS_MISSING_BLOCKS");
                }

                {
                    add("HDFS_UNDER_REPLICATED_BLOCKS");
                }
            });
            put(ClouderaConfigurationProvider.CLOUDERA_SERVICE_HIVE, new ArrayList<String>() {
                {
                    add("HIVE_HIVEMETASTORES_HEALTHY");
                }

                {
                    add("HIVE_HIVESERVER2S_HEALTHY");
                }
            });
            put(ClouderaConfigurationProvider.CLOUDERA_SERVICE_HUE, new ArrayList<String>() {
                {
                    add("HUE_HUE_SERVERS_HEALTHY");
                }
            });
            put(ClouderaConfigurationProvider.CLOUDERA_SERVICE_IMPALA, new ArrayList<String>() {
                {
                    add("IMPALA_ASSIGNMENT_LOCALITY");
                }

                {
                    add("IMPALA_CATALOGSERVER_HEALTH");
                }

                {
                    add("IMPALA_IMPALADS_HEALTHY");
                }

                {
                    add("IMPALA_STATESTORE_HEALTH");
                }
            });
            put(ClouderaConfigurationProvider.CLOUDERA_SERVICE_KAFKA, new ArrayList<String>() {
                {
                    add("KAFKA_KAFKA_BROKER_SCM_HEALTH");
                }
                {
                    add("KAFKA_KAFKA_MIRROR_MAKER_SCM_HEALTH");
                }
                {
                    add("KAFKA_KAFKA_GATEWAY_SCM_HEALTH");
                }

            });
            put(ClouderaConfigurationProvider.CLOUDERA_SERVICE_KSINDEXER, new ArrayList<String>() {
                {
                    add("KS_INDEXER_HBASE_INDEXERS_HEALTHY");
                }
            });
            put(ClouderaConfigurationProvider.CLOUDERA_SERVICE_OOZIE, new ArrayList<String>() {
                {
                    add("OOZIE_OOZIE_SERVERS_HEALTHY");
                }
            });
            put(ClouderaConfigurationProvider.CLOUDERA_SERVICE_SOLR, new ArrayList<String>() {
                {
                    add("SOLR_SOLR_SERVERS_HEALTHY");
                }
            });
            put(ClouderaConfigurationProvider.CLOUDERA_SERVICE_SPARK, new ArrayList<String>() {
            });
            put(ClouderaConfigurationProvider.CLOUDERA_SERVICE_YARN, new ArrayList<String>() {
                {
                    add("YARN_JOBHISTORY_HEALTH");
                }

                {
                    add("YARN_NODE_MANAGERS_HEALTHY");
                }

                {
                    add("YARN_RESOURCEMANAGERS_HEALTH");
                }

                {
                    add("YARN_USAGE_AGGREGATION_HEALTH");
                }
            });
            put(ClouderaConfigurationProvider.CLOUDERA_SERVICE_ZOOKEEPER, new ArrayList<String>() {
                {
                    add("ZOOKEEPER_CANARY_HEALTH");
                }

                {
                    add("ZOOKEEPER_SERVERS_HEALTHY");
                }
            });
        }
    };


    public int build(TimeSeriesResource timeSeriesResource) {

        // cache the metric name cache over NAME_CACHE_EXPIRATION_HOURS hours
        if (lastCacheLoad != null) {
            DateTime now = new DateTime();
            DateTime expire = lastCacheLoad.plusHours(NAME_CACHE_EXPIRATION_HOURS);
            if (now.isBefore(expire)) {
                int count = 0;
                for (List<String> list : metricsPerView.values()) {
                    count = count + list.size();
                }
                if (count > 0) {
                    Duration duration = new Duration(now, expire);
                    if (log.isInfoEnabled()) {
                        log.info("Metric name cache not expired. Loading " + count + " metrics from cache. Cache will expire in " + duration.getStandardMinutes() + " minutes");
                    }
                    return count;
                }
            }
        }
        long start = System.currentTimeMillis();
        int count = 0;
        ApiMetricSchemaList list = timeSeriesResource.getMetricSchema();
        if (list.size() > 0) {
            clear();
        }
        synchronized (metricsPerView) {
            for (ApiMetricSchema schema : list.getSchemas()) {
                for (String source : schema.getSources().keySet()) {
                    if (source.equals(ClouderaConfigurationProvider.CLOUDERA_CLUSTER) && schema.getSources().size() > 1) {
                        continue;
                    }
                    List<String> metricsPerSourceType = metricsPerView.get(source);
                    if (metricsPerSourceType == null) {
                        continue;
                    }
                    metricsPerSourceType.add(schema.getName());
                    count++;
                }
            }
            lastCacheLoad = new DateTime();
        }
        for (List<String> metricList : metricsPerView.values()) {
            Collections.sort(metricList);
        }
        if (log.isInfoEnabled()) {
            log.info("Rebuilt cloudera metric name cache in " + (System.currentTimeMillis() - start) + " ms");
        }
        return count;
    }

    public List<String> getListByServiceType(String serviceType, TimeSeriesResource timeSeriesResource) {
        List<String> list = metricsPerView.get(serviceType);
        if (list == null) {
            log.error("getListByServiceType: requested an unsupported service type: " + serviceType);
            return new ArrayList<>();
        }
        if (list.size() == 0) {
            build(timeSeriesResource);
            return metricsPerView.get(serviceType);
        }
        return list;
    }

    public static final String ALL_PREFIX = "";
    public static final String WILDCARD_PREFIX = "*";
    public static final int DEFAULT_NAMES_LIMIT = 10;

    // INFO:  not completed, we decided to do autocomplete on client side
    // INFO: to complete, would need to store this.metricsPerView elements as MetricName objects, not Strings
    public List<MetricName> autocomplete(String serviceType, String prefix, TimeSeriesResource timeSeriesResource) {
        int limit = DEFAULT_NAMES_LIMIT;
        List<String> list = metricsPerView.get(serviceType);
        if (list == null) {
            log.error("getListByServiceType: requested an unsupported service type: " + serviceType);
            return new ArrayList<>();
        }
        if (list.size() == 0) {
            build(timeSeriesResource);
        }
        prefix = ((prefix != null) ? prefix.toLowerCase().trim() : ALL_PREFIX);
        if (prefix.equals(WILDCARD_PREFIX)) {
            prefix = ALL_PREFIX;
        }
        if (limit == 0) {
            return Collections.EMPTY_LIST;
        }
        // snapshot sorted search names
        List<MetricName> searchNames = createMetricNameList(list);
        // binary search across names for prefix
        int searchIndex = Collections.binarySearch(searchNames, new MetricName(prefix));
        searchIndex = ((searchIndex >= 0) ? searchIndex : -searchIndex - 1);
        // return limited prefix matching names
        List<MetricName> names = new ArrayList<MetricName>();
        if (searchIndex < searchNames.size()) {
            if ((limit > 0) && (searchNames.get(searchIndex).getCanonicalName() != null)) {
                // return limited search canonical names
                Set<String> uniqueCanonicalNames = new HashSet<String>();
                for (int i = searchIndex; (i < searchNames.size()); i++) {
                    MetricName searchName = searchNames.get(i);
                    if (uniqueCanonicalNames.add(searchName.getCanonicalName()) && (uniqueCanonicalNames.size() > limit)) {
                        break;
                    }
                    if (searchName.getLowerCaseName().startsWith(prefix)) {
                        names.add(searchName);
                    } else {
                        break;
                    }
                }
            } else {
                // return limited search names
                limit = ((limit > 0) ? Math.min(searchIndex + limit, searchNames.size()) : searchNames.size());
                for (int i = searchIndex; (i < limit); i++) {
                    MetricName searchName = searchNames.get(i);
                    if (searchName.getLowerCaseName().startsWith(prefix)) {
                        names.add(searchName);
                    } else {
                        break;
                    }
                }
            }
        }
        return names;
    }

    protected List<MetricName> createMetricNameList(List<String> names) {
        List<MetricName> metricNames = new ArrayList<>();
        for (String name : names) {
            metricNames.add(new MetricName(name));
        }
        return metricNames;
    }

    public void clear() {
        synchronized (metricsPerView) {
            if (log.isInfoEnabled()) {
                log.info("Clearing cloudera metric name cache ");
            }
            for (List<String> names : metricsPerView.values()) {
                names.clear();
            }
        }
    }

    public List<String> getHealthChecksByServiceType(String serviceType) {
        List<String> list = healthChecksPerView.get(serviceType);
        if (list == null) {
            log.error("healthChecksByServiceType: requested an unsupported service type: " + serviceType);
            return new ArrayList<>();
        }
        return list;
    }
}
