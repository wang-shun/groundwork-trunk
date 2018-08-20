package org.groundwork.cloudhub.connectors.azure;

import com.google.common.collect.Lists;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.monitor.*;
import com.microsoft.azure.management.resources.GenericResource;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.groundwork.cloudhub.connectors.MetricViewDefinitions;
import org.groundwork.cloudhub.exceptions.CloudHubException;
import org.groundwork.cloudhub.gwos.GwosStatus;
import org.groundwork.cloudhub.metrics.BaseMetric;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.groundwork.cloudhub.metrics.MetricCollectionState;
import org.groundwork.cloudhub.metrics.MetricProvider;
import org.groundwork.cloudhub.metrics.MetricsPostProcessor;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by justinchen on 2/15/18.
 */
public class AzureMetricCollector {

    public static final String METRIC_NOT_FOUND_OR_NO_TIME_SERIES_DATA_FOR_METRIC = "Metric not found or no time series data for metric: ";
    public static final String NO_METRICS_FOUND_FOR_BATCH = "No metrics found for batch starting with: ";
    public static final String NO_TIME_SERIES_DATA_FOR_METRIC = "No time series data for metric: ";
    private static Logger log = Logger.getLogger(AzureMetricCollector.class);

    public static final String SERVICE_IS_NOT_PROVIDING_ANY_METRICS_AT_THIS_TIME = "Service is not providing any metrics at this time";
    public static final String NULL_TIME_SERIES_DATA_FOUND_FOR = "Value for Time Series data is NULL for ";
    public static final int AZURE_METRICS_BATCH_THROTTLE = 5;

    private Azure azure;
    private MetricsPostProcessor postProcessor;

    public AzureMetricCollector(MetricsPostProcessor postProcessor) {
        this.postProcessor = postProcessor;
    }

    public void collectOptimized(MetricDefinitions azureMetricDefs, GenericResource azureResource, MetricProvider metricProvider,
                                 MetricCollectionState state, MetricViewDefinitions metricDefinitions) {
        DateTime startMetricCollectDateTime = DateTime.now();

        // build batches and group by damn Azure metrics throttle limit
        List<BatchedMetric> all = new ArrayList<>();
        for (MetricDefinition azureMetricDef : azureMetricDefs.listByResource(azureResource.id())) {
            BaseQuery query = metricDefinitions.getQueryMap().get(azureMetricDef.name().value());
            if (query != null) {
                all.add(new BatchedMetric(query, azureMetricDef, azureMetricDef.name().value()));
            }
        }
        List<List<BatchedMetric>> batches = Lists.partition(all, AZURE_METRICS_BATCH_THROTTLE);

        // process batches
        for (List<BatchedMetric> batch : batches) {
            try {
                long startQueryTime = System.currentTimeMillis();
                String multiQuery = StringUtils.join(reduceNames(batch), ",");
                String multiAggregations = StringUtils.join(reduceAggregations(batch), ",");
                MetricCollection metricCollection = batch.get(0).getDefinition().defineQuery(multiQuery, multiAggregations)
                        .startingFrom(startMetricCollectDateTime.minusMinutes(2))
                        .endsBefore(startMetricCollectDateTime)
                        .withResultType(ResultType.DATA)
                        .execute();
                if (log.isDebugEnabled()) {
                    log.debug("\t\tQuery time for batched metrics : " + batch.size() + " is " + (System.currentTimeMillis() - startQueryTime) + " ms");
                }
                if (hasMetrics(metricCollection)) {
                    int index = 0;
                    for (BatchedMetric batchedMetric : batch) {
                        boolean isValid = false;
                        BaseMetric baseMetric = batchedMetric.getBaseMetric();
                        BaseQuery query = batchedMetric.getQuery();
                        if (hasTimeSeries(metricCollection, index)) {
                            MetricValue metricValue = metricCollection.metrics().get(index).timeseries().get(0).data().get(0);
                            Number tsValue = getTimeSeriesValue(batchedMetric.getDefinition().primaryAggregationType(), metricValue);
                            isValid = buildMetric(query, baseMetric, tsValue, metricProvider);
                        } else {
                            if (log.isDebugEnabled()) {
                                log.debug(NO_TIME_SERIES_DATA_FOR_METRIC + metricProvider.getName() + " - " + query.getQuery());
                            }
                            Number tsValue = getTimeSeriesValueDefault(batchedMetric.getDefinition().primaryAggregationType());
                            isValid = buildMetric(query, baseMetric, tsValue, metricProvider);
                        }
                        if (!isValid) {
                            failedMetric(batchedMetric);
                        }
                        if (!query.isMonitored()) {
                            baseMetric.setConfigFlag(true);
                        }
                        configExtraBaseMetricStates(baseMetric, metricProvider, query);
                        metricProvider.putMetric(query.getServiceName(), baseMetric);
                        index = index + 1;
                    }
                } else {
                    log.error(NO_METRICS_FOUND_FOR_BATCH + metricProvider.getName() + batch.get(0).getQuery().getQuery());
                    badBatch(batch, metricProvider);
                }
            } catch (Exception e) {
                log.error("Error in collecting metric, " + e.getMessage());
                BaseQuery query = batch.get(0).getQuery();
                BaseMetric baseMetric = batch.get(0).getBaseMetric();
                if (baseMetric != null && query != null) {
                    CloudHubException che = (e instanceof CloudHubException) ? (CloudHubException) e : new CloudHubException(e.getMessage(), e);
                    postProcessor.handleSyntheticException(baseMetric, che, metricProvider, query.getServiceName(), state);
                } else {
                    log.error("ERROR!!! more issue to investigate in collecting metric, null BaseQuery or null BaseMetric? ");
                }
            }
        }
    }

    private Boolean buildMetric(BaseQuery query, BaseMetric baseMetric, Number tsValue, MetricProvider metricProvider) {
        if (tsValue != null) {
            String value = (query.getFormat() != null) ? postProcessor.format(tsValue, query.getFormat()) : tsValue.toString();
            baseMetric.setValueOnly(value);
            baseMetric.setCurrentState();
            return true;
        } else {
            if (metricProvider instanceof AzureVM) {
                AzureVM azureVM = (AzureVM)metricProvider;
                if (azureVM.getRunState() != null && !azureVM.getRunState().equals(GwosStatus.UP.status)) {
                    return true;
                }
            }
            log.info(NULL_TIME_SERIES_DATA_FOUND_FOR + metricProvider.getName() + " : " + query.getQuery());
            // metric value is not provided, i.e., have time series data entries but value is null
            return false;
        }
    }

    private BaseMetric failedMetric(BatchedMetric batchedMetric) {
        BaseMetric baseMetric = batchedMetric.getBaseMetric();
        baseMetric.setValueOnly("");
        baseMetric.setCurrState(BaseMetric.sWarning);
        baseMetric.setExplanation(SERVICE_IS_NOT_PROVIDING_ANY_METRICS_AT_THIS_TIME);
        return baseMetric;
    }

    private void badBatch(List<BatchedMetric> batch, MetricProvider metricProvider) {
        for (BatchedMetric batchedMetric : batch) {
            if (!batchedMetric.getQuery().isMonitored()) {
                batchedMetric.getBaseMetric().setConfigFlag(true);
            }
            failedMetric(batchedMetric);
            configExtraBaseMetricStates(batchedMetric.getBaseMetric(), metricProvider, batchedMetric.getQuery());
            metricProvider.putMetric(batchedMetric.getQuery().getServiceName(), batchedMetric.getBaseMetric());
        }
    }

    public void collect(MetricDefinitions azureMetricDefs, GenericResource azureResource, MetricProvider metricProvider, MetricCollectionState state, MetricViewDefinitions metricDefinitions) {

        DateTime startMetricCollectDateTime = DateTime.now();
        for (MetricDefinition azureMetricDef : azureMetricDefs.listByResource(azureResource.id())) {
            BaseQuery query = null;
            BaseMetric baseMetric = null;
            try {
                // MetricDefinition.name() is actually equal to Metric.name() from our test...
                // So, it can be used to query view def map.
                query = metricDefinitions.getQueryMap().get(azureMetricDef.name().value());

                if (query != null) {
                    baseMetric = new BaseMetric(
                            query.getQuery(),
                            query.getWarning(),
                            query.getCritical(),
                            query.isGraphed(),
                            query.isMonitored(),
                            query.getCustomName()
                    );
                    if (!query.isMonitored()) {
                        baseMetric.setConfigFlag(true);
                    }
                    long startQueryTime = System.currentTimeMillis();
                    // query could throw exception but need to continue on to next one
                    MetricCollection metrics = azureMetricDef.defineQuery()
                            // TODO: get 2 since the last one is always null, seems to be a Azure bug.
                            .startingFrom(startMetricCollectDateTime.minusMinutes(2))
                            .endsBefore(startMetricCollectDateTime)
                            .withResultType(ResultType.DATA)
                            .execute();

                    if (log.isDebugEnabled()) {
                        log.debug("\t\tQuery time for metric : " + azureMetricDef.name().value() + " is " + (System.currentTimeMillis() - startQueryTime) + " ms");
                    }
                    boolean isValid = false;
                    if (hasMetrics(metrics) && hasTimeSeries(metrics, 0)) {
                        MetricValue metricValue = metrics.metrics().get(0).timeseries().get(0).data().get(0);
                        Number tsValue = getTimeSeriesValue(azureMetricDef.primaryAggregationType(), metricValue);
                        if (tsValue != null) {
                            String value = (query.getFormat() != null) ? postProcessor.format(tsValue, query.getFormat()) : tsValue.toString();
                            baseMetric.setValueOnly(value);
                            baseMetric.setCurrentState();
                            isValid = true;
                        } else {
                            if (metricProvider instanceof AzureVM) {
                                AzureVM azureVM = (AzureVM)metricProvider;
                                if (azureVM.getRunState() != null && azureVM.getRunState().equals(GwosStatus.UP.status)) {
                                    log.info(NULL_TIME_SERIES_DATA_FOUND_FOR + metricProvider.getName() + " : " + query.getQuery());
                                }
                            }
                            // metric value is not provided, i.e., have time series data entries but value is null
                        }
                    } else {
                        log.error(METRIC_NOT_FOUND_OR_NO_TIME_SERIES_DATA_FOR_METRIC + metricProvider.getName() + query.getQuery());
                    }
                    if (!isValid) {
                        baseMetric.setValueOnly("");
                        baseMetric.setCurrState(BaseMetric.sWarning);
                        baseMetric.setExplanation(SERVICE_IS_NOT_PROVIDING_ANY_METRICS_AT_THIS_TIME);
                    }
                    configExtraBaseMetricStates(baseMetric, metricProvider, query);
                    metricProvider.putMetric(query.getServiceName(), baseMetric);
                }
            } catch (Exception e) {
                log.error("Error in collecting metric, " + e.getMessage());
                if (baseMetric != null && query != null) {
                    CloudHubException che = (e instanceof CloudHubException) ? (CloudHubException) e : new CloudHubException(e.getMessage(), e);
                    postProcessor.handleSyntheticException(baseMetric, che, metricProvider, query.getServiceName(), state);
                } else {
                    log.error("ERROR!!! more issue to investigate in collecting metric, null BaseQuery or null BaseMetric? ");
                }
            }
        }
    }

    private boolean hasMetrics(MetricCollection metrics) {
        return (metrics.metrics() != null && metrics.metrics().size() > 0 && metrics.metrics().get(0) != null);
    }

    private boolean hasTimeSeries(MetricCollection metrics, int index) {
        return metrics.metrics().get(index).timeseries().size() > 0;
    }

    private Number getTimeSeriesValue(AggregationType aggregationType, MetricValue metricValue) {
        Number tsValue = null;
        switch (aggregationType) {
            case COUNT:
                tsValue = metricValue.count();
                break;

            case AVERAGE:
                tsValue = metricValue.average();
                break;

            case TOTAL:
                tsValue = metricValue.total();
                break;

            case MAXIMUM:
                tsValue = metricValue.maximum();
                break;

            case MINIMUM:
                tsValue = metricValue.minimum();
                break;

            case NONE:
                log.error("ERROR!!! Aggregation Type not defined in the metric definition");
                throw new CloudHubException("Unhandled aggregation type when getting metric value");
        }
        return tsValue;
    }

    private Number getTimeSeriesValueDefault(AggregationType aggregationType) {
        Number tsValue = null;
        switch (aggregationType) {
            case COUNT:
                tsValue = 0L;
                break;

            case AVERAGE:
            case TOTAL:
            case MAXIMUM:
            case MINIMUM:
                tsValue = 0.0;
                break;

            case NONE:
                log.error("ERROR!!! Default Aggregation Type not defined in the metric definition");
                throw new CloudHubException("Unhandled aggregation type when getting metric value");
        }
        return tsValue;
    }

    private void configExtraBaseMetricStates(BaseMetric baseMetric, MetricProvider metricProvider, BaseQuery query) {
        baseMetric.setMetricType(query.getServiceType());
        if (!query.isMonitored()) {
            baseMetric.setConfigFlag(true);
        }

        BaseMetric priorMetric = metricProvider.getMetricPool().get(query.getServiceName());
        if (priorMetric != null) {
            baseMetric.setLastState(priorMetric.getCurrState());
        }
    }

    private static List<String> reduceNames(List<BatchedMetric> batch) {
        List<String> reduced = new LinkedList<>();
        for (BatchedMetric metric : batch) {
            reduced.add(metric.getMetricName());
        }
        return reduced;
    }

    private static List<String> reduceAggregations(List<BatchedMetric> batch) {
        List<String> reduced = new LinkedList<>();
        for (BatchedMetric metric : batch) {
            reduced.add(metric.getDefinition().primaryAggregationType().toString());
        }
        return reduced;
    }

}
