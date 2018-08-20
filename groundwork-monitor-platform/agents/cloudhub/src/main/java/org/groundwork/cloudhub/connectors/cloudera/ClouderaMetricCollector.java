package org.groundwork.cloudhub.connectors.cloudera;

import com.cloudera.api.model.ApiTimeSeries;
import com.cloudera.api.model.ApiTimeSeriesMetadata;
import com.cloudera.api.model.ApiTimeSeriesResponse;
import com.cloudera.api.model.ApiTimeSeriesResponseList;
import com.cloudera.api.v11.TimeSeriesResourceV11;
import com.cloudera.api.v14.RootResourceV14;
import org.apache.log4j.Logger;
import org.groundwork.agents.utils.StringUtils;
import org.groundwork.cloudhub.connectors.MetricViewDefinitions;
import org.groundwork.cloudhub.exceptions.CloudHubException;
import org.groundwork.cloudhub.metrics.BaseMetric;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.groundwork.cloudhub.metrics.ComputeType;
import org.groundwork.cloudhub.metrics.MetricCollectionState;
import org.groundwork.cloudhub.metrics.MetricProvider;
import org.groundwork.cloudhub.metrics.MetricsPostProcessor;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dtaylor on 5/16/17.
 */
public class ClouderaMetricCollector {

    public static final String CLOUDERA_METRIC_NOT_FOUND = "Cloudera Metric not found: ";
    public static final String ZERO_TIME_SERIES_DATA_FOUND_FOR = "Zero Time Series data found for ";
    public static final String SERVICE_IS_NOT_PROVIDING_ANY_METRICS_AT_THIS_TIME = "Service is not providing any metrics at this time";
    public static final String SERVICE_HEALTH_CHECK_NOT_OPERATIONAL = "Health Check is not operational at this time";
    private static Logger log = Logger.getLogger(ClouderaMetricCollector.class);
    
    private RootResourceV14 rootResource;
    private MetricsPostProcessor postProcessor;
    public static boolean optimized = true;
    private DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
    private boolean enableServiceNameByRoleType = false;

    public static final String METADATA_CATEGORY = "category";
    public static final String METADATA_CATEGORY_HOST = "HOST";
    public static final String METADATA_CATEGORY_ROLE = "ROLE";
    public static final String METADATA_CATEGORY_CLUSTER = "CLUSTER";
    public static final String METADATA_CATEGORY_SERVICE = "SERVICE";
    public static final String METADATA_CATEGORY_RACK = "RACK";
    public static final String METADATA_SERVICE_TYPE = "serviceType";
    public static final String METADATA_ROLE_TYPE = "roleType";
    public static final String METADATA_SERVICE_NAME = "serviceName";
    public static final String METADATA_CLUSTER_NAME = "clusterName";
    public static final String SERVICE_DELIMITER = "-";


    public ClouderaMetricCollector(RootResourceV14 rootResource, MetricsPostProcessor postProcessor, boolean enableServiceNameByRoleType) {
        this.rootResource = rootResource;
        this.postProcessor = postProcessor;
        this.enableServiceNameByRoleType = enableServiceNameByRoleType;
    }

    public void collect(MetricCollectionState state, MetricProvider host, MetricViewDefinitions metricDefinitions,
                                 String typeName, String typeValue) {

         collect(state, host, metricDefinitions, typeName, typeValue, null, null);
    }

    public void collect(MetricCollectionState state, MetricProvider host, MetricViewDefinitions metricDefinitions,
                                 String typeName, String typeValue, String typeName2, String typeValue2) {
        if (optimized) {
            collectOptimized(state, host, metricDefinitions, typeName, typeValue, null, null);
        }
        else {
            collectWithMultipleQueries(state, host, metricDefinitions, typeName, typeValue, null, null);
        }
    }

    /**
     * Collect with multiple queries by any Type name or Entity name
     * This collection algorithm submits one query per metric. It is roughly 3x slower than collectOptimize
     *
     * @param state
     * @param host
     * @param metricDefinitions
     * @param typeName
     * @param typeValue
     * @param typeName2
     * @param typeValue2
     */
    protected void collectWithMultipleQueries(MetricCollectionState state, MetricProvider host, MetricViewDefinitions metricDefinitions,
                              String typeName, String typeValue, String typeName2, String typeValue2) {
        TimeSeriesResourceV11 timeSeriesResource = rootResource.getTimeSeriesResource();
        DateTime dt = new DateTime();
        String fromDate = fmt.print(dt.minusMinutes(5));
        String toDate = fmt.print(dt);
        for (BaseQuery query : metricDefinitions.getQueries()) {
            String metricName = query.getServiceName();
            // check for synthetic or health check amd skip
            if (skip(query)) {
                continue;
            }
            String tsQuery = (typeValue2 == null) ?
                    String.format("SELECT %s WHERE %s = %s", query.getQuery(), typeName, typeValue)
                    : String.format("SELECT %s WHERE %s = %s and %s = %s", query.getQuery(), typeName, typeValue, typeName2, typeValue2);
            String serviceName = null;
            ApiTimeSeriesResponseList responseList = timeSeriesResource.queryTimeSeries(tsQuery, fromDate, toDate);
            for (ApiTimeSeriesResponse response : responseList) {
                for (ApiTimeSeries timeSeries : response.getTimeSeries()) {
                    serviceName = buildServiceName(timeSeries.getMetadata());
                    if (timeSeries.getData().size() > 0) {
                        createMetric(state , host, query, timeSeries, serviceName, metricName);
                    }
                    else {
                        createMissingMetric(state, host, query, timeSeries, serviceName, metricName);   
                    }
                }
            }
        }
    }

    private boolean createMetric(MetricCollectionState state , MetricProvider host, BaseQuery query, ApiTimeSeries timeSeries, String serviceName, String metricName) {
        boolean found = false;
        BaseMetric metric = new BaseMetric(
                serviceName,
                query.getWarning(),
                query.getCritical(),
                query.isGraphed(),
                query.isMonitored(),
                query.getCustomName()
        );
        metric.setMetricType(query.getServiceType());
        if (!query.isMonitored()) {
            metric.setConfigFlag(true);
        }
        try {
            Double tsValue = timeSeries.getData().get(timeSeries.getData().size()-1).getValue();
            String value = (query.getFormat() != null) ? postProcessor.format(tsValue, query.getFormat()) : tsValue.toString();
            metric.setValueOnly(value);
            // TODO: metric.setNumericValue(tsValue);
            found = true;
        }
        catch(CloudHubException e) {
            postProcessor.handleSyntheticException(metric, e, host, metricName, state);
            found = true;
        }
        BaseMetric priorMetric = host.getMetricPool().get(serviceName);
        if (priorMetric != null) {
            metric.setLastState(priorMetric.getCurrState());
        }
        host.putMetric(serviceName, metric);

        if (!found) {
            if (serviceName == null) {
                serviceName = query.getServiceName();
            }
            priorMetric = host.getMetricPool().get(serviceName);
            String message = CLOUDERA_METRIC_NOT_FOUND + metricName;
            if (priorMetric != null) {
                postProcessor.handleSyntheticException(priorMetric, new CloudHubException(message), host, metricName, state);
            }
            else {
                message += ", host: " + host.getName();
                log.error(message);
            }
        }
        return found;
    }

    private boolean createMissingMetric(MetricCollectionState state , MetricProvider host, BaseQuery query, ApiTimeSeries timeSeries, String serviceName, String metricName) {
        if (log.isInfoEnabled()) {
            log.info(ZERO_TIME_SERIES_DATA_FOUND_FOR + host.getName() + " : " + timeSeries.getMetadata().getMetricName());
        }
        BaseMetric metric = new BaseMetric(
                serviceName,
                query.getWarning(),
                query.getCritical(),
                query.isGraphed(),
                query.isMonitored(),
                query.getCustomName()
        );
        metric.setMetricType(query.getServiceType());
        if (!query.isMonitored()) {
            metric.setConfigFlag(true);
        }
        metric.setValueOnly("");
        metric.setCurrState(BaseMetric.sCritical);
        metric.setExplanation(SERVICE_IS_NOT_PROVIDING_ANY_METRICS_AT_THIS_TIME);
        BaseMetric priorMetric = host.getMetricPool().get(serviceName);
        if (priorMetric != null) {
            metric.setLastState(priorMetric.getCurrState());
        }
        host.putMetric(serviceName, metric);
        return true;
    }
    
    /**
         * Optimized single query instead of submitting one query per metric like collectWithMultipleQueries
         * This collection algorithm submits one query per entity Type, with a select clause of multiple queries.
         * It is roughly 3x faster than collectWithMultipleQueries
     *
     * @param state
     * @param host
     * @param metricDefinitions
     * @param typeName
     * @param typeValue
     * @param typeName2
     * @param typeValue2
     */
    protected void collectOptimized(MetricCollectionState state, MetricProvider host, MetricViewDefinitions metricDefinitions,
                              String typeName, String typeValue, String typeName2, String typeValue2) {
        TimeSeriesResourceV11 timeSeriesResource = rootResource.getTimeSeriesResource();
        DateTime dt = new DateTime();
        String fromDate = fmt.print(dt.minusMinutes(5));
        String toDate = fmt.print(dt);
        StringBuffer tsQuery = new StringBuffer("SELECT ");
        boolean first = true;
        Map<String,BaseQuery> queryMap = new HashMap<>();
        for (BaseQuery query : metricDefinitions.getQueries()) {
            if (skip(query)) {
                continue;
            }
            if (!first) {
                tsQuery.append(", ");
            }
            tsQuery.append(query.getQuery());
            queryMap.put(query.getQuery(), query);
            first = false;
        }
        if (first) {
            return; // no metrics processed
        }
        String where = (typeValue2 == null) ?
                String.format(" WHERE %s = %s", typeName, typeValue)
                : String.format(" WHERE %s = %s and %s = %s", typeName, typeValue, typeName2, typeValue2);
        tsQuery.append(where);
        String serviceName = null;
        ApiTimeSeriesResponseList responseList = timeSeriesResource.queryTimeSeries(tsQuery.toString(), fromDate, toDate);
        for (ApiTimeSeriesResponse response : responseList) {
            for (ApiTimeSeries timeSeries : response.getTimeSeries()) {
                BaseQuery query = queryMap.get(buildAliasedName(timeSeries.getMetadata()));
                if (query == null) {
                    log.error("Query not found: " + timeSeries.getMetadata().getMetricName());
                    continue;
                }
                String metricName = query.getServiceName();
                serviceName = buildServiceName(timeSeries.getMetadata());
                if (timeSeries.getData().size() > 0) {
                    createMetric(state, host, query, timeSeries, serviceName, metricName);
                }
                else {
                    createMissingMetric(state, host, query, timeSeries, serviceName, metricName);
                }
            }
        }
    }

    protected String buildAliasedName(ApiTimeSeriesMetadata metadata) {
        StringBuffer name = new StringBuffer(metadata.getMetricName());
        if (metadata.getAlias() != null) {
            name.append(" as ");
            name.append(metadata.getAlias());
        }
        return name.toString();
    }

    protected String buildServiceName(ApiTimeSeriesMetadata metadata) {
        String category = metadata.getAttributes().get(METADATA_CATEGORY);
        String metricName = (metadata.getAlias() != null) ? metadata.getAlias() : metadata.getMetricName();
        String serviceName;
        switch (category) {
            case METADATA_CATEGORY_ROLE:
                String roleType = metadata.getAttributes().get(METADATA_ROLE_TYPE);
                serviceName = (roleType == null) ? metricName : roleType + SERVICE_DELIMITER + metricName;
                break;
            case METADATA_CATEGORY_HOST:
            case METADATA_CATEGORY_CLUSTER:
            case METADATA_CATEGORY_SERVICE:
            default:
                serviceName = metricName;
                break;
        }
        /**
         * Configured to optionally use Cloudera Category Roles (metadata roleType attribute) in building a service name
         *
         */
        if (enableServiceNameByRoleType == false) {
            serviceName = metricName;
        }
        return serviceName;
    }

    protected String buildHostName(ApiTimeSeriesMetadata metadata) {
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
        return host;
    }

    protected boolean skip(BaseQuery query) {
        if (query.getComputeType() != null) {
            if (query.getComputeType().equals(ComputeType.health) || query.getComputeType().equals(ComputeType.synthetic) || !StringUtils.isEmpty(query.getExpression())) {
                return true;
            }
        }
        return false;
    }

}
