package org.groundwork.cloudhub.connectors.amazon;

import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.DimensionFilter;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
import com.amazonaws.services.cloudwatch.model.ListMetricsRequest;
import com.amazonaws.services.cloudwatch.model.ListMetricsResult;
import com.amazonaws.services.cloudwatch.model.Metric;
import org.groundwork.agents.utils.StringUtils;
import org.groundwork.cloudhub.gwos.GwosStatus;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.InventoryOptions;
import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.BaseMetric;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.groundwork.cloudhub.metrics.BaseVM;
import org.groundwork.cloudhub.metrics.MonitoringState;
import org.groundwork.cloudhub.metrics.SourceType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class AWSItem {

    protected final static String SIMPLE_DATE_FORMAT = "yyyy-MM-dd HH:mm";

    private final static List<String> awsMetricStatList = Arrays.asList(new String[]{"Average", "SampleCount"});

    protected final static SimpleDateFormat dateFormatter = new SimpleDateFormat(SIMPLE_DATE_FORMAT);

    private boolean inUse = false;

    // 2016-06-30: DST: removing info retrieval as its not used
    private boolean enableInfoRetrieval = true;
    
    public abstract String getDisplayName();
    
    public void resolveRelationState(AWSInventory awsInventory) {}
    
    public void populateHostGroupDataCenterInventory(DataCenterInventory inventory,
            InventoryOptions options, AWSInventory awsInventory) {}
    public void populateHostDataCenterInventory(DataCenterInventory inventory,
            InventoryOptions options, AWSInventory awsInventory) {}

    public void collectHostGroupMetrics(MonitoringState currentState, MonitoringState priorState,
            List<BaseQuery> awsQueries, List<BaseQuery> gwQueries,
            AWSInventory awsInventory, AWSConnection connection, Map<String, BaseVM> priorVMMap) {}
    public void collectHostMetrics(MonitoringState currentState, MonitoringState priorState,
            List<BaseQuery> awsQueries, List<BaseQuery> gwQueries, List<BaseQuery> customQueries,
            AWSInventory awsInventory, AWSConnection connection, Map<String, BaseVM> priorVMMap) {}
    
    protected abstract String getStatus();

    protected void markInUse() {
        inUse = true;
    }
    
    protected boolean isInUse() {
        return inUse;
    }

    protected void addMetric(Object current, Object prior, BaseQuery query, String value) {
        addMetric(current, prior, query, value, null, null, null);
    }

    protected void addMetric(Object current, Object prior, BaseQuery query, String value, SourceType sourceType) {
        addMetric(current, prior, query, value, sourceType, null, null);
    }

    protected void addMetric(Object current, Object prior, BaseQuery query, String value, SourceType sourceType, String extraDimensionValue,  Metric awsMetric) {
        String metricName, customName = null;
        if (query.getQuery().startsWith(AmazonConfigurationProvider.METRIC_PREFIX_EBS)) {
            metricName = AmazonConfigurationProvider.METRIC_PREFIX_EBS + extraDimensionValue + "-" + awsMetric.getMetricName();
            if (!StringUtils.isEmpty(query.getCustomName())) {
                customName = AmazonConfigurationProvider.METRIC_PREFIX_EBS + extraDimensionValue + "-" + query.getCustomName();
            }
        }
        else {
            metricName = query.getQuery();
            // @since 7.1.1: override service name
            if (!StringUtils.isEmpty(query.getCustomName())) {
                String[] substrings = metricName.split("\\.");
                customName = (substrings.length == 2) ?
                        substrings[0] + "." + query.getCustomName() : query.getCustomName();
            }
        }
        BaseMetric metric = new BaseMetric(query, metricName);
        if (prior != null) {
            BaseMetric priorMetric = null;
            if (prior instanceof BaseHost) {
                priorMetric = ((BaseHost)prior).getMetric(query.getQuery());
            } else {
                priorMetric = ((BaseVM)prior).getMetric(query.getQuery());
            }
            if (priorMetric != null) {
                metric.setValue(priorMetric.getCurrValue());
                metric.setLastState(priorMetric.getCurrState());
            }
        }
        // @since 7.1.1: override service name
        if (!StringUtils.isEmpty(query.getCustomName())) {
            metric.setCustomName(customName);
        }
        metric.setValue(value);
        if (sourceType != null) {
            metric.setMetricType(sourceType.name());
        }
        if (!getStatus().equals(GwosStatus.UP.status)) {
            metric.setCurrState(getMetricStatus(getStatus()));
        }
        if (current instanceof BaseHost) {
            ((BaseHost)current).putMetric(metricName, metric);
        } else {
            ((BaseVM)current).putMetric(metricName, metric);
        }
    }
    
    protected void populateAWSMetrics(List<BaseQuery> awsQueries, Object target, Object priorNode,
            AWSConnection connection, String dimensionName, String dimensionValue, SourceType sourceType) {

        Map<String, Metric> availableMetrics = getAvailableAWSMetrics(dimensionName,
                dimensionValue, connection);

        GetMetricStatisticsRequest getMetricStatisticsRequest = new GetMetricStatisticsRequest();
        getMetricStatisticsRequest.setStatistics(awsMetricStatList);

        for (BaseQuery awsQuery : awsQueries) {
            Metric metric = availableMetrics.get(awsQuery.getQuery());
            if (metric != null) {
                populateAWSMetric(target, priorNode, awsQuery, metric,
                        connection, getMetricStatisticsRequest, sourceType, dimensionValue);
            }
        }
    }
    
    private Map<String, Metric> getAvailableAWSMetrics(String dimensionName, String dimensionValue,
            AWSConnection connection) {

        HashMap<String, Metric> availableMetrics = new HashMap<String, Metric>();
        
        DimensionFilter dimFilter = new DimensionFilter();
        dimFilter.setName(dimensionName);
        dimFilter.setValue(dimensionValue);
        ArrayList<DimensionFilter> dimFilterList = new ArrayList<DimensionFilter>(1);
        dimFilterList.add(dimFilter);
        
        ListMetricsRequest listMetricsRequest = new ListMetricsRequest();
        listMetricsRequest.setDimensions(dimFilterList);
        for (;;) {
            ListMetricsResult metrics = connection.getMetricsClient().listMetrics(listMetricsRequest);
            for (Metric metric : metrics.getMetrics()) {
                String prefix;
                if (metric.getNamespace().startsWith("AWS")) {
                    if (metric.getNamespace().indexOf('/') >= 0) {
                        prefix = metric.getNamespace().split("/")[1];
                    } else {
                        prefix = metric.getNamespace();
                    }
                }
                else {
                    prefix = metric.getNamespace();
                }
                availableMetrics.put(prefix + "." + metric.getMetricName(), metric);
            }
            
            String nextBatchId = metrics.getNextToken();
            if (nextBatchId == null) {
                break;
            }
            listMetricsRequest.setNextToken(nextBatchId);
        }
        
        return availableMetrics;
    }

    private void populateAWSMetric(Object target, Object prior, BaseQuery awsQuery, Metric metric,
                                   AWSConnection connection, GetMetricStatisticsRequest getMetricStatisticsRequest, SourceType sourceType) {
        populateAWSMetric(target, prior, awsQuery, metric, connection, getMetricStatisticsRequest, sourceType, null);
    }

    private void populateAWSMetric(Object target, Object prior, BaseQuery awsQuery, Metric metric,
            AWSConnection connection, GetMetricStatisticsRequest getMetricStatisticsRequest, SourceType sourceType, String extraDimensionValue) {
        
        getMetricStatisticsRequest.setNamespace(metric.getNamespace());
        getMetricStatisticsRequest.setDimensions(metric.getDimensions());
        getMetricStatisticsRequest.setMetricName(metric.getMetricName());
        
        final int SAMPLING_RANGE_MINUTES = 15;
        Date end = new Date();
        end.setTime(end.getTime() - (end.getTime() % (60L * 1000L)));    // Round down to whole minute.
        Date start = new Date(end.getTime() - ((long)SAMPLING_RANGE_MINUTES * 60L * 1000L));
        getMetricStatisticsRequest.setEndTime(end);
        getMetricStatisticsRequest.setStartTime(start);
        getMetricStatisticsRequest.setPeriod(60);  // Period must be multiple of 60 (seconds)
        
        GetMetricStatisticsResult stats = connection.getMetricsClient().getMetricStatistics(getMetricStatisticsRequest);
        List<Datapoint> datapoints = stats.getDatapoints();
        if (datapoints.isEmpty()) {
            addMetric(target, prior, awsQuery, "0", sourceType, extraDimensionValue, metric);
            return;
        }
        Datapoint latestDatapoint = null;
        for (Datapoint datapoint : datapoints) {
            if (latestDatapoint == null) {
                latestDatapoint = datapoint;
            } else if (latestDatapoint.getTimestamp().compareTo(datapoint.getTimestamp()) < 0) {
                latestDatapoint = datapoint;
            }
        }
        String result = Long.toString(latestDatapoint.getAverage().longValue());
        addMetric(target, prior, awsQuery, result, sourceType, extraDimensionValue, metric);
    }
    
    String getMetricStatus(String hostStatus) {
        if (GwosStatus.UP.status.equals(hostStatus)) {
            return BaseMetric.sOK;
        } else if (GwosStatus.UNSCHEDULED_DOWN.status.equals(hostStatus)) {
            return BaseMetric.sCritical;
        } else if (GwosStatus.UNREACHABLE.status.equals(hostStatus)) {
            return BaseMetric.sUnknown;
        } else if (GwosStatus.SCHEDULED_DOWN.status.equals(hostStatus)) {
            return BaseMetric.sScheduledDown;
        } else if (GwosStatus.UNSCHEDULED_DOWN.status.equals(hostStatus)) {
            return BaseMetric.sScheduledDown;
        } else if (GwosStatus.PENDING.status.equals(hostStatus)) {
            return BaseMetric.sPending;
        } else if (GwosStatus.DOWN.status.equals(hostStatus)) {
            return BaseMetric.sPoweredDown;
        }
        return BaseMetric.sWarning;
    }

    public boolean isEnableInfoRetrieval() {
        return enableInfoRetrieval;
    }

    public void setEnableInfoRetrieval(boolean enableInfoRetrieval) {
        this.enableInfoRetrieval = enableInfoRetrieval;
    }
}
