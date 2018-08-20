package org.groundwork.cloudhub.cloudera;

import com.cloudera.api.ApiRootResource;
import com.cloudera.api.ClouderaManagerClientBuilder;
import com.cloudera.api.DataView;
import com.cloudera.api.model.ApiCluster;
import com.cloudera.api.model.ApiClusterList;
import com.cloudera.api.model.ApiConfig;
import com.cloudera.api.model.ApiConfigList;
import com.cloudera.api.model.ApiHealthCheck;
import com.cloudera.api.model.ApiHost;
import com.cloudera.api.model.ApiHostList;
import com.cloudera.api.model.ApiMetricSchema;
import com.cloudera.api.model.ApiMetricSchemaList;
import com.cloudera.api.model.ApiRole;
import com.cloudera.api.model.ApiService;
import com.cloudera.api.model.ApiServiceList;
import com.cloudera.api.model.ApiTimeSeries;
import com.cloudera.api.model.ApiTimeSeriesData;
import com.cloudera.api.model.ApiTimeSeriesMetadata;
import com.cloudera.api.model.ApiTimeSeriesResponse;
import com.cloudera.api.model.ApiTimeSeriesResponseList;
import com.cloudera.api.v1.ClouderaManagerResource;
import com.cloudera.api.v1.RolesResource;
import com.cloudera.api.v1.RootResourceV1;
import com.cloudera.api.v11.TimeSeriesResourceV11;
import com.cloudera.api.v4.TimeSeriesResource;
import org.groundwork.cloudhub.connectors.cloudera.ClouderaConfigurationProvider;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by dtaylor on 4/17/17.
 */
public class ClouderaManagerTest {

    public static final String CLOUDERA_LOCAL_SERVER = "localhost";
    public static final Integer CLOUDERA_LOCAL_PORT = 32769;
    public static final String CLOUDERA_LOCAL_USERNAME = "cloudera";
    public static final String CLOUDERA_LOCAL_PASSWORD = "cloudera";

    public static final String CLOUDERA_DEV_AWS_SERVER = "dev-cloudera";
    public static final Integer CLOUDERA_DEV_AWS_PORT = 7180;
    public static final String CLOUDERA_DEV_AWS_USERNAME = "admin";
    public static final String CLOUDERA_DEV__AWS_PASSWORD = "d3vcloudera!";
    public static final String CLOUDERA_AWS_SERVER = "172.28.111.205";
    public static final Integer CLOUDERA_AWS_PORT = 7180;
    public static final String CLOUDERA_AWS_USERNAME = "admin";
    public static final String CLOUDERA_AWS_PASSWORD = "admin";

    //@Test
    public void testLocalConnection() throws Exception {
        ClouderaManagerClientBuilder builder =
                new ClouderaManagerClientBuilder()
//                        .withBaseURL(new URL("http://localhost:32770"))
                        .withHost(CLOUDERA_LOCAL_SERVER)
                        .withPort(CLOUDERA_LOCAL_PORT)
                        .withUsernamePassword(CLOUDERA_LOCAL_USERNAME, CLOUDERA_LOCAL_PASSWORD)
                        .withConnectionTimeout(1000L, TimeUnit.MILLISECONDS);
        testAPIs(builder);
    }

    @Test
    public void testAWSConnection() throws Exception {
        ClouderaManagerClientBuilder builder =
                new ClouderaManagerClientBuilder()
                        .withHost(CLOUDERA_AWS_SERVER)
                        .withPort(CLOUDERA_AWS_PORT)
                        .withUsernamePassword(CLOUDERA_AWS_USERNAME, CLOUDERA_AWS_PASSWORD)
                        .withConnectionTimeout(4000L, TimeUnit.MILLISECONDS);
        ApiRootResource apiRootResource = builder.build();
        RootResourceV1 rootResource = apiRootResource.getRootV14();

        System.out.println("currentVersion=" + apiRootResource.getCurrentVersion());
        // make sure connection is valid
        ApiClusterList clusters = rootResource.getClustersResource().readClusters(DataView.SUMMARY);
        for (ApiCluster cluster : clusters) {
            System.out.println("cluster = " + cluster.getName()) ;
        }

        testAPIs(builder);
    }

    public final static String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    public void testAPIs(ClouderaManagerClientBuilder builder) throws Exception {
        ApiRootResource apiRoot = builder.build();
        RootResourceV1 v14 = apiRoot.getRootV14();
        System.out.println("current version = " + apiRoot.getCurrentVersion());
        ClouderaManagerResource manager = v14.getClouderaManagerResource();
        System.out.println("manager version = " + manager.getVersion());

        TimeSeriesResource timeSeriesResource = apiRoot.getRootV14().getTimeSeriesResource();

        ApiConfigList apiConfigList = manager.getConfig(DataView.FULL);
        for (ApiConfig config : apiConfigList.getConfigs()) {
            System.out.println("config = " + config.toString());
        }
        for (ApiRole role : manager.getMgmtServiceResource().getRolesResource().readRoles()) {
            System.out.println("role " + role.getName() + ", " + role.getHealthSummary());
            for (ApiHealthCheck check : role.getHealthChecks()) {
                // again, no explanation available on readRoles
                System.out.println("\t\tcheck: " + check.getName() + ": " + check.getSummary());
            }
        }

        ApiClusterList clusters = v14.getClustersResource().readClusters(DataView.FULL_WITH_HEALTH_CHECK_EXPLANATION);
        for (ApiCluster cluster : clusters) {
            System.out.println("cluster " + cluster.getName());
            ApiServiceList services = v14.getClustersResource().getServicesResource(cluster.getName()).readServices(DataView.FULL_WITH_HEALTH_CHECK_EXPLANATION);
            Object o = cluster.getServices();
            for (ApiService service : services) {
                System.out.println("service : " + service.getName());
                if (service.getHealthChecks().size() == 0) {
                    // this is where we could build our own health explanation by aggegating status
                }
                for (ApiHealthCheck healthCheck : service.getHealthChecks()) {
                    System.out.println("-- healthCheck : " + healthCheck.getSummary().toString());
                    if (healthCheck.getExplanation() != null) {
                        System.out.println("-- explanation : " + healthCheck.getExplanation());
                    }
                }
//                for (ApiRoleTypeConfig rtc : service.getConfig().getRoleTypeConfigs()) {
//                    System.out.println("==== " + rtc.getRoleType());
//                }
//                for (ApiRole role : service.getRoles()) {
//                    for (ApiHealthCheck rhc : role.getHealthChecks()) {
//                        System.out.println("-- healthCheck : " + rhc.getSummary().toString());
//                        if (rhc.getExplanation() != null) {
//                            System.out.println("-- explanation : " + rhc.getExplanation());
//                        }
//                    }
//                }

//                ApiRoleTypeList roleTypeList = v14.getClustersResource().getServicesResource(cluster.getName()).listRoleTypes(service.getName());
//                for (String rt : roleTypeList) {
//                    System.out.println(" ++++ role type: " + rt);
                    RolesResource rr = v14.getClustersResource().getServicesResource(cluster.getName()).getRolesResource(service.getName());
                    for (ApiRole role : rr.readRoles() ) {
                        System.out.println("\tinstance: " + role.getName() + " : " + role.getHealthSummary().toString());
                        for (ApiHealthCheck check : role.getHealthChecks()) {
                            System.out.println("\t\tcheck: " + check.getName() + ": " + check.getSummary());
                        }
                    }
//                }
            }
        }

        ApiHostList hosts = v14.getHostsResource().readHosts(DataView.FULL);

        String hostName = "";
        for (ApiHost host : hosts) {
            System.out.println("host: " + host.toString());
            System.out.println("health: " + host.getHealthSummary().toString());
            //v12.getHostsResource().getMetrics()
        }

        System.out.println("===============");
        // submitQuery(timeSeriesResource, "select swap_used, physical_memory_used, physical_memory_total, physical_memory_cached, physical_memory_buffers where entityName=cb9f72f1-3616-40a2-8309-1380857b3413");
        // submitQuery(timeSeriesResource, "select total_cpu_user where roleType=DATANODE");
        // submitQuery(timeSeriesResource, "select jvm_heap_used_mb/1024 as jvm_heap_used_gb");
        //submitQuery(timeSeriesResource, "SELECT total_write_bytes_rate_across_disks WHERE entityName = cb9f72f1-3616-40a2-8309-1380857b3413");

        submitQuery(timeSeriesResource, "SELECT total_bytes_receive_rate_across_network_interfaces WHERE entityName = cb9f72f1-3616-40a2-8309-1380857b3413");

        //submitQuery(timeSeriesResource, "SELECT write_bytes_rate_across_disks WHERE entityName = cb9f72f1-3616-40a2-8309-1380857b3413");
        submitQuery(timeSeriesResource, "SELECT bytes_receive_rate_across_network_interfaces WHERE entityName = cb9f72f1-3616-40a2-8309-1380857b3413");
        submitQuery(timeSeriesResource, "SELECT cpu_user_rate / getHostFact(numCores, 1) * 100 as cpu-rate-user WHERE entityName = cb9f72f1-3616-40a2-8309-1380857b3413");

        // HBASE QUERIES - returns 2 JVMs: master and region
        submitQuery(timeSeriesResource, "SELECT jvm_heap_used_mb/1024 as jvm_heap_used_gb WHERE serviceName = 'hbase'");
        submitQuery(timeSeriesResource, "SELECT total_read_requests_rate_across_regionservers as jvm_heap_used_gb WHERE serviceName = 'hbase'");
        submitQuery(timeSeriesResource, "SELECT jvm_total_threads WHERE serviceName = 'hbase'");


        // submitQuery(timeSeriesResource, "select total_read_bytes_rate_across_disks, total_write_bytes_rate_across_disks");

        System.out.println("===============");
        //ApiTimeSeriesResponseList result = timeSeriesResource.queryTimeSeries("select total_cpu_user where roleType=DATANODE", null, null);
        //ApiTimeSeriesResponseList result = timeSeriesResource.queryTimeSeries("select bytes_read, bytes_written where roleType=DATANODE", null, null);

//        select total_read_bytes_rate_across_disks, total_write_bytes_rate_across_disks where category = CLUSTER
//        select total_bytes_receive_rate_across_network_interfaces, total_bytes_transmit_rate_across_network_interfaces where category = CLUSTER

    }

    public void submitQuery(TimeSeriesResource timeSeriesResource, String query) {
        System.out.println("-- query: " + query);
        DateTime now = new DateTime();
        DateTime minuteAgo = now.minusMinutes(1);
        String nowFormatted = DateTimeFormat.forPattern(DATE_FORMAT).print(now);
        String minuteAgoFormatted = DateTimeFormat.forPattern(DATE_FORMAT).print(minuteAgo);
        ApiTimeSeriesResponseList result = timeSeriesResource.queryTimeSeries(query, minuteAgoFormatted, nowFormatted);
        for (ApiTimeSeriesResponse response : result) {
            for (ApiTimeSeries series : response.getTimeSeries()) {
                ApiTimeSeriesMetadata metadata = series.getMetadata();
                String category = metadata.getAttributes().get(METADATA_CATEGORY);
                String host = null;
                String serviceType = null;
                switch (category) {
                    case METADATA_CATEGORY_HOST:
                        host = metadata.getEntityName();
                        break;
                    case METADATA_CATEGORY_ROLE:
                        serviceType = metadata.getAttributes().get(METADATA_SERVICE_TYPE);
                        host = metadata.getAttributes().get(METADATA_SERVICE_NAME);
                        break;
                    case METADATA_CATEGORY_CLUSTER:
                        host = metadata.getAttributes().get(METADATA_CLUSTER_NAME);
                        break;
                    default:
                        break;
                }
                if (host == null) {
                    System.out.println("skipping ts for category " + category);
                    continue;
                }
                String metricName = (metadata.getAlias() != null) ? metadata.getAlias() : metadata.getMetricName();
                System.out.println("host: " + host);
                List<ApiTimeSeriesData> seriesData = series.getData();
                if (seriesData != null && seriesData.size() > 0) {
                    System.out.println(metricName + " :  " + seriesData.get(0).getValue());
                }
            }
            System.out.println("-- end query ");
        }
    }

    public static final String METADATA_CATEGORY = "category";
    public static final String METADATA_CATEGORY_HOST = "HOST";
    public static final String METADATA_CATEGORY_ROLE = "ROLE";
    public static final String METADATA_CATEGORY_CLUSTER = "CLUSTER";
    public static final String METADATA_CATEGORY_RACK = "RACK";
    public static final String METADATA_SERVICE_TYPE = "serviceType";
    public static final String METADATA_SERVICE_NAME = "serviceName";
    public static final String METADATA_CLUSTER_NAME = "clusterName";


    class QueryContext {
        private String metricName;
        private String alias;
        private String category;
        private String role;
        private String serviceType;
        private String service;

        public QueryContext(ApiTimeSeriesMetadata metadata) {
            category = metadata.getAttributes().get("");
        }
    }

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

    @Test
    public void testMetricsSchema() throws Exception {
        ClouderaManagerClientBuilder builder =
                new ClouderaManagerClientBuilder()
                        .withHost(CLOUDERA_AWS_SERVER)
                        .withPort(CLOUDERA_AWS_PORT)
                        .withUsernamePassword(CLOUDERA_AWS_USERNAME, CLOUDERA_AWS_PASSWORD)
                        .withConnectionTimeout(1000L, TimeUnit.MILLISECONDS);

        ApiRootResource apiRoot = builder.build();
        RootResourceV1 v14 = apiRoot.getRootV14();
        System.out.println("current version = " + apiRoot.getCurrentVersion());
        ClouderaManagerResource manager = v14.getClouderaManagerResource();
        System.out.println("manager version = " + manager.getVersion());
        TimeSeriesResourceV11 timeSeriesResource = apiRoot.getRootV14().getTimeSeriesResource();
        long start = System.currentTimeMillis();
        ApiMetricSchemaList list = timeSeriesResource.getMetricSchema();
        System.out.println("execution getMetricSchema = " + (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        for (ApiMetricSchema schema : list.getSchemas()) {
            for (String source : schema.getSources().keySet()) {
                if (source.equals("CLUSTER") && schema.getSources().size() > 1) {
                    continue;
                }
                List<String> metricsPerSourceType = metricsPerView.get(source);
                if (metricsPerSourceType == null) {
                    System.out.println("-- unsupported " + source);
                    continue;
                }
                metricsPerSourceType.add(schema.getName());
            }
        }
        System.out.println("build getMetricSchema = " + (System.currentTimeMillis() - start));

        for (List<String> metricsList : metricsPerView.values()) {
            Collections.sort(metricsList);
        }
        System.out.println("-- done");
    }

}
