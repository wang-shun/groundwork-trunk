package org.groundwork.cloudhub.metrics;

import com.groundwork.collage.util.MonitorStatusBubbleUp;
import org.apache.log4j.Logger;
import org.groundwork.agents.utils.StringUtils;

import java.util.Date;

public class BaseMetric extends BaseProperties {
    public static final String sPoweredDown = MonitorStatusBubbleUp.SCHEDULED_CRITICAL;
    public static final String sScheduledDown = MonitorStatusBubbleUp.SCHEDULED_CRITICAL;
    public static final String sUnknown = MonitorStatusBubbleUp.UNKNOWN;
    public static final String sCritical = MonitorStatusBubbleUp.UNSCHEDULED_CRITICAL;
    public static final String sAllCritical = MonitorStatusBubbleUp.CRITICAL;
    public static final String sWarning = MonitorStatusBubbleUp.WARNING;
    public static final String sPending = MonitorStatusBubbleUp.PENDING;
    public static final String sOK = MonitorStatusBubbleUp.OK;
    public static final String sUP = MonitorStatusBubbleUp.UP;

    private static final Date nullDate = new Date();

    private String querySpec = null;
    private String queryRegex = null;
    private String customName = null;

    private String currValue = null;
    private String lastValue = null;
    private String currState = null;
    // 2017-02-07
    // removing pending default state for backward compatibility
    // when cloudhub is restarted, the state shouldn't go to PENDING
    private String lastState = null; // sPending
    private String currStateExtra = null;
    private String explanation;

    private boolean useLessThanLogic = false;
    private long thresholdWarning = 0;
    private long thresholdCritical = 0;

    private boolean monitorFlag = false;
    private boolean graphFlag = false;
    private boolean traceFlag = false;   // for diagnostic output
    private boolean configFlag = false;   // marks that this metric is not to be written to GWOS

    private String metricType;

    private static Logger log = Logger.getLogger(BaseMetric.class);

    public BaseMetric(String query, long warning, long critical, boolean isGraphed, boolean isMonitored, String customName) {
        this.querySpec = query;
        this.thresholdWarning = warning;
        this.thresholdCritical = critical;
        this.graphFlag = isGraphed;
        this.monitorFlag = isMonitored;
        this.customName = customName;
        useLessThanLogic = (warning > critical);
    }

    public BaseMetric(BaseQuery query) {
        this.querySpec = query.getQuery();
        this.thresholdWarning = query.getWarning();
        this.thresholdCritical = query.getCritical();
        this.graphFlag = query.isGraphed();
        this.monitorFlag = query.isMonitored();
        useLessThanLogic = (query.getWarning() > query.getCritical());
    }

    public BaseMetric(BaseQuery query, String translatedName) {
        this.querySpec = translatedName;
        if (query.isRegex()) {
            this.queryRegex = query.getQuery();
        }
        this.thresholdWarning = query.getWarning();
        this.thresholdCritical = query.getCritical();
        this.graphFlag = query.isGraphed();
        this.monitorFlag = query.isMonitored();
        useLessThanLogic = (query.getWarning() > query.getCritical());
    }

    public void setThresholds(long warning, long critical) {
        this.thresholdWarning = warning;
        this.thresholdCritical = critical;

        useLessThanLogic = (warning > critical);
    }

    public void setIsGraphed(boolean isGraphed) {
        this.graphFlag = isGraphed;
    }

    public void setIsMonitored(boolean isMonitored) {
        this.monitorFlag = isMonitored;
    }

    public void setTrace() {
        this.traceFlag = true;
    }

    protected boolean isOverThreshold(long thresholdValue) {
        if (currValue == null)
            return false;

        if (thresholdValue == -1)
            return false;

        long compvalue = 0;
        try {
            if (currValue.contains("%")) {
                compvalue = Long.parseLong(currValue.substring(0, currValue.indexOf("%")));
            }
            else if (currValue.contains(".")) {
                Double doubleValue = Double.parseDouble(currValue);
                doubleValue = Math.abs(doubleValue);
                compvalue = doubleValue.longValue();
            }
            else {
                compvalue = Long.parseLong(currValue);
            }
        } catch (Exception e) {
            compvalue = 0;
        }

        return useLessThanLogic
                ? compvalue <= thresholdValue
                : compvalue >= thresholdValue;
    }

    public boolean isCritical() {
        return isOverThreshold(thresholdCritical);
    }

    public boolean isWarning() {
        return isOverThreshold(thresholdWarning);
    }

    public boolean isGraphed() {
        return this.graphFlag;
    }

    public boolean isMonitored() {
        return this.monitorFlag;
    }


    private void printTrace(String header, BaseMetric vbm) {
        StringBuilder s = new StringBuilder();

        s.append(String.format("metric %s:\n", header));
        s.append(toString(vbm));

        log.info(s.toString());
    }

    public String toString() {
        return toString(this);
    }

    public String toString(BaseMetric o) {
        StringBuffer s = new StringBuffer(1000);

        s.append(String.format("%-40s: '%s'\n", "querySpec", o.querySpec));
        s.append(String.format("%-40s: '%s'\n", "currValue", o.currValue));
        s.append(String.format("%-40s: '%s'\n", "lastValue", o.lastValue));
        s.append(String.format("%-40s: '%s'\n", "currState", o.currState));
        s.append(String.format("%-40s: '%s'\n", "useLessThanLogic", o.useLessThanLogic ? "true" : "false"));
        s.append(String.format("%-40s: '%d'\n", "thresholdWarning", o.thresholdWarning));
        s.append(String.format("%-40s: '%d'\n", "thresholdCritical", o.thresholdCritical));
        s.append(String.format("%-40s: '%s'\n", "graphFlag", o.graphFlag ? "true" : "false"));
        s.append(String.format("%-40s: '%s'\n", "monitorFlag", o.monitorFlag ? "true" : "false"));
        s.append(String.format("%-40s: '%s'\n", "traceFlag", o.traceFlag ? "true" : "false"));

        return s.toString();
    }

    @Deprecated // used by old VMWare and Redhat connectors only
    public void mergeInNew(BaseMetric update) {
        if (this.traceFlag || update.traceFlag) {
            printTrace("0: update  object: ", update);
            printTrace("1: base    object: ", this);
        }

        if (this.querySpec == null || update.querySpec != null)
            this.querySpec = update.querySpec;

        this.useLessThanLogic = update.useLessThanLogic;
        this.thresholdWarning = update.thresholdWarning;
        this.thresholdCritical = update.thresholdCritical;
        this.graphFlag = update.graphFlag;
        this.monitorFlag = update.monitorFlag;
        this.traceFlag = update.traceFlag || this.traceFlag;  //latching
        this.setCustomName(update.getCustomName());
        setValue(update.currValue);

        if (this.traceFlag || update.traceFlag)
            printTrace("2: updated object: ", this);
    }

    @Deprecated  // this has the side effect of only allowing runState to be set once, overriding lastState
    private void setCurrState()
    {
        //  fix this, this is very problematic ... disallows setting state on a metric more than once
        if (currState != null) {
            lastState = currState;
        }
        setCurrentState();
    }

    /**
     * Alternative to setCurrState(), which side-effects lastState
     * Goal is to not use setCurrState because of side-effect
     *
     */
    public void setCurrentState() 
    {
        String state;
        String x;
        if (currValue == null) {
            state = sUnknown;
            x = "No Value";
        } else if (isCritical()) {
            state = sCritical;
            x = "";
        } else if (isWarning()) {
            state = sWarning;
            x = "";
        } else if (lastState == sPending) {
            state = sOK;
            x = "";
        } else if (lastState == null) {
            state = sOK;
            x = "";
        } else {
            state = sOK;
            x = "";
        }
        currState = state;
        currStateExtra = x + " (" + querySpec + ")";
    }

    protected boolean isChange(String current, String last) {
        if (current != null) {
            return (last != null) ? current.equals(last) : true;
        }
        else {
            return (last != null);
        }
    }

    public boolean isStateChange() {
        return isChange(currState, lastState);
    }

    public boolean isValueChange() {
        return isChange(currValue, lastValue);
    }

    public void setValueOnly(String value) {
        currValue = value;
        setCurrentState();
    }

    // This has a side-effect of setting lastValue, and limiting this call to only once per metrics gathering
    // we should look into using setValueOnly as connectors are updated
    public void setValue(String value) {
        if (currValue != null) {
            lastValue = currValue;
        }
        currValue = value;
        setCurrState();              // very important: CHAINED computation
    }

    public void setLastValue(String lastValue) {
        this.lastValue = lastValue;

    }

    public String getCurrState() {
        return this.currState;
    }

    public String getCurrStateExtra() {
        return this.currStateExtra;
    }

    public String getCurrValue() {
        return this.currValue;
    }

    public String getLastState() {
        return this.lastState;
    }

    public void setLastState(String lastState) {
        this.lastState = lastState;
    }
    public void setCurrState(String currentState) {
        this.currState = currentState;
    }

    public String getLastValue() {
        return this.lastValue;
    }

    public String getQuerySpec() {
        return this.querySpec;
    }

    public long getThresholdWarning() {
        return this.thresholdWarning;
    }

    public long getThresholdCritical() {
        return this.thresholdCritical;
    }

    public String getQueryRegex() {
        return queryRegex;
    }

    public void setQueryRegex(String queryRegex) {
        this.queryRegex = queryRegex;
    }

    public String getMetricType() {
        return metricType;
    }

    public void setMetricType(String metricType) {
        this.metricType = metricType;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public String getServiceName() {
        if (!StringUtils.isEmpty(customName)) {
            return customName;
        }
        return querySpec;
    }

    public boolean isConfigFlag() {
        return configFlag;
    }

    public void setConfigFlag(boolean configFlag) {
        this.configFlag = configFlag;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
