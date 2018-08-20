package org.groundwork.cloudhub.connectors;

import org.groundwork.cloudhub.metrics.BaseMetric;

import static org.junit.Assert.assertEquals;

public class ConnectorTestUtils {

    public static void assertMetricsEqual(String baseMessage, BaseMetric metric1, BaseMetric metric2) throws Exception {
        String message = baseMessage + " " + metric1.getQuerySpec();
        assertEquals(message + ": currState: ", metric1.getCurrState(), metric2.getCurrState());
        assertEquals(message + ": state extra: ", metric1.getCurrStateExtra(), metric2.getCurrStateExtra());
        assertEquals(message + ": currentValue: ", metric1.getCurrValue(), metric2.getCurrValue());
        assertEquals(message + ": customName: ", metric1.getCustomName(), metric2.getCustomName());
        assertEquals(message + ": lastState: ", metric1.getLastState(), metric2.getLastState());
        assertEquals(message + ": lastValue: ", metric1.getLastValue(), metric2.getLastValue());
        assertEquals(message + ": querySpec: ", metric1.getQuerySpec(), metric2.getQuerySpec());
        assertEquals(message + ": isCritical: ", metric1.isCritical(), metric2.isCritical());
        assertEquals(message + ": isWarning: ", metric1.isWarning(), metric2.isWarning());
        assertEquals(message + ": isMonitored: ", metric1.isMonitored(), metric2.isMonitored());
        assertEquals(message + ": isGraphed: ", metric1.isGraphed(), metric2.isGraphed());
        assertEquals(message + ": isStateChange: ", metric1.isStateChange(), metric2.isStateChange());
        assertEquals(message + ": isValueChange: ", metric1.isValueChange(), metric2.isValueChange());
        assertEquals(message + ": threshold critical: ", metric1.getThresholdCritical(), metric2.getThresholdCritical());
        assertEquals(message + ": threshold warning: ", metric1.getThresholdWarning(), metric2.getThresholdWarning());
        assertEquals(message + ": metricType: ", metric1.getMetricType(), metric2.getMetricType());
        assertEquals(message + ": serviceName: ", metric1.getServiceName(), metric2.getServiceName());
    }

}
