package org.groundwork.cloudhub.profile;

import org.groundwork.rs.dto.profiles.Metric;

import java.util.ArrayList;
import java.util.List;

public class BaseProfileWrapper {

    private String agent;
    private String profileType;

    public BaseProfileWrapper() {}

    public BaseProfileWrapper(String profileType, String agent) {
        this.profileType = profileType;
        this.agent = agent;
    }

    public String getAgent() {
        return this.agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public String getProfileType() {
        return profileType;
    }

    public void setProfileType(String profileType) {
        this.profileType = profileType;
    }

    public void mergeMetrics(List<UIMetric> metrics, List<Metric> profileMetrics) {
        if (metrics != null) {
            List<Metric> additional = new ArrayList<Metric>();
            for (UIMetric uiMetric : metrics) {
                boolean found = false;
                for (Metric metric : profileMetrics) {
                    if (uiMetric.getName().equals(metric.getName())) {
                        mergeMetric(metric, uiMetric);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    additional.add(createMetric(uiMetric));
                }
            }
            for (Metric metric : additional) {
                profileMetrics.add(metric);
            }
        }

    }

    protected void mergeMetric(Metric metric, UIMetric uiMetric) {
        mergeMetric(metric, uiMetric, false);
    }

        /**
         * Merge back in a UIMetric into a Metric
         *
         * @param metric
         * @param uiMetric
         */
    protected void mergeMetric(Metric metric, UIMetric uiMetric, Boolean mergeDescription) {
        metric.setWarningThreshold(Double.parseDouble(uiMetric.getUiWarningThreshold()));
        metric.setCriticalThreshold(Double.parseDouble(uiMetric.getUiCriticalThreshold()));
        metric.setMonitored(uiMetric.isMonitored());
        metric.setGraphed(uiMetric.isGraphed());
        metric.setCustomName(uiMetric.getCustomName());
        metric.setExpression(uiMetric.getExpression());
        metric.setFormat(uiMetric.getFormat());
        metric.setServiceType(uiMetric.getServiceType());
        metric.setSourceType(uiMetric.getSourceType());
        if (mergeDescription) {
            metric.setDescription(uiMetric.getDescription());
        }
    }

    protected Metric createMetric(UIMetric uiMetric) {
        return new Metric(uiMetric.getName(), uiMetric.getDescription(), uiMetric.isMonitored(),
                uiMetric.isGraphed(), Double.parseDouble(uiMetric.getUiWarningThreshold()),
                Double.parseDouble(uiMetric.getUiCriticalThreshold()),
                uiMetric.getSourceType(), uiMetric.getComputeType(), uiMetric.getCustomName(),
                uiMetric.getExpression(), uiMetric.getFormat(), uiMetric.getServiceType());
    }
}
