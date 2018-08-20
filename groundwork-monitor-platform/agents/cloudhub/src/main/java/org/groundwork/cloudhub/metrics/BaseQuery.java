package org.groundwork.cloudhub.metrics;

import org.apache.log4j.Logger;
import org.groundwork.agents.utils.StringUtils;
import org.groundwork.rs.dto.profiles.Metric;

public class BaseQuery {
    private static Logger log = Logger.getLogger(BaseQuery.class);

    private String queryString;
    private long thresholdWarning;
    private long thresholdCritical;
    private boolean graphFlag;
    private boolean monitorFlag;
    private boolean traceFlag;
    private SourceType sourceType;
    private ComputeType computeType;
    private String customName = "";
    private String expression;
    private String format;
    private String serviceType;

    // Most statics
    public BaseQuery(String query, long warning, long critical, boolean isGraphed, boolean isMonitored) {
        this(query, warning, critical, isGraphed, isMonitored, false, SourceType.diagnostics, ComputeType.query, null, null, null, null);
    }

    // OpenStack static
    public BaseQuery(String query, long warning, long critical, boolean isGraphed, boolean isMonitored, SourceType sourceType, ComputeType computeType) {
        this(query, warning, critical, isGraphed, isMonitored, false, sourceType, computeType, null, null, null, null);
    }
    
    public BaseQuery(String query, long warning, long critical, boolean isGraphed, boolean isMonitored, boolean isTraced,
                     SourceType sourceType, ComputeType computeType, String customName, String expression, String format, String serviceType) {
        this.queryString = query;
        this.thresholdWarning = warning;
        this.thresholdCritical = critical;
        this.graphFlag = isGraphed;
        this.monitorFlag = isMonitored;
        this.traceFlag = isTraced;
        this.sourceType = sourceType;
        this.computeType = computeType;
        this.customName = customName;
        this.expression = expression;
        this.format = format;
        this.serviceType = serviceType;
    }

    public BaseQuery(Metric metric) {
        this.queryString = metric.getName();
        this.thresholdWarning = (long)metric.getWarningThreshold();
        this.thresholdCritical = (long)metric.getCriticalThreshold();
        this.graphFlag = metric.isGraphed();
        this.monitorFlag = metric.isMonitored();
        this.traceFlag = false;
        this.computeType = toComputeType(metric.getComputeType());
        this.sourceType = toSourceType(metric.getSourceType());
        this.customName = metric.getCustomName();
        this.expression = metric.getExpression();
        this.format = metric.getFormat();
        this.serviceType = metric.getServiceType();
    }

    public String getQuery() {
        return this.queryString;
    }

    public long getWarning() {
        return this.thresholdWarning;
    }

    public long getCritical() {
        return this.thresholdCritical;
    }

    public boolean isGraphed() {
        return this.graphFlag;
    }

    public boolean isMonitored() {
        return this.monitorFlag;
    }

    public boolean isTraced() {
        return this.traceFlag;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public void setWarning(long warning) {
        this.thresholdWarning = warning;
    }

    public void setCritical(long critical) {
        this.thresholdCritical = critical;
    }

    public void setGraphed(boolean graphFlag) {
        this.graphFlag = graphFlag;
    }

    public void setMonitored(boolean monitorFlag) {
        this.monitorFlag = monitorFlag;
    }

    public void setTraced(boolean traceFlag) {
        this.traceFlag = traceFlag;
    }

    public ComputeType getComputeType() {
        return computeType;
    }

    public void setComputeType(ComputeType computeType) {
        this.computeType = computeType;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public boolean isCeilometer() {
        return sourceType == SourceType.ceilometer;
    }

    public boolean isRegex() {
        return computeType == ComputeType.regex;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public static SourceType toSourceType(String source) {
        if (source != null && source.trim().length() > 0) {
            try {
                return SourceType.valueOf(source);
            } catch (IllegalArgumentException e) {
                log.error("illegal source type: " + source);
            }
        }
        return SourceType.diagnostics;
    }

    public static ComputeType toComputeType(String compute) {
        if (compute != null && compute.trim().length() > 0) {
            try {
                return ComputeType.valueOf(compute);
            } catch (IllegalArgumentException e) {
                log.error("illegal compute type: " + compute);
            }
        }
        return ComputeType.query;
    }

    public String getServiceName() {
        if (!StringUtils.isEmpty(customName)) {
            return customName;
        }
        return queryString;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }
}

