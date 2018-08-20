package org.groundwork.cloudhub.connectors.vmware;

import org.apache.log4j.Logger;
import org.groundwork.cloudhub.connectors.vmware2.MetricsUtils;
import org.groundwork.cloudhub.metrics.*;

public class VMwareHost extends BaseHost implements DefaultMetricProvider {
    private static Logger log = Logger.getLogger(VMwareVM.class);
    
    public VMwareHost(String hostName) {
        super(hostName);
    }

    // keep these for backward compatibility with v1 connector until removal
    private static final BaseQuery[] baseMetricList =
            {
                    new BaseQuery("summary.quickStats.overallCpuUsage", 2000, 3000, true, false, SourceType.compute, ComputeType.query),
                    new BaseQuery("summary.quickStats.overallMemoryUsage", 2000, 3000, true, false, SourceType.compute, ComputeType.query),
                    new BaseQuery("summary.quickStats.uptime", 3197400, 6000000, false, false, SourceType.compute, ComputeType.query),
                    new BaseQuery("summary.runtime.bootTime", 0, 0, false, false, SourceType.compute, ComputeType.query),

                    // These should be moved into Configs, but doing so breaks old connector
                    new BaseQuery("summary.runtime.connectionState", 0, 0, false, false, SourceType.compute, ComputeType.query),
                    new BaseQuery("summary.runtime.powerState", 0, 0, false, false, SourceType.compute, ComputeType.query),
            };

    private static final BaseQuery[] baseConfigList =
            {
                    new BaseQuery("name", 0, 0, false, false, SourceType.diagnostics, ComputeType.info),
                    new BaseQuery("summary.hardware.cpuMhz", 0, 0, false, false, SourceType.diagnostics, ComputeType.info),
                    //   new BaseQuery("summary.hardware.cpuMhz.scaled", 0, 0, false, false, SourceType.diagnostics, ComputeType.info),
                    new BaseQuery("summary.hardware.numCpuCores", 0, 0, false, false, SourceType.diagnostics, ComputeType.info),
                    new BaseQuery("summary.hardware.memorySize", 0, 0, false, false, SourceType.diagnostics, ComputeType.info),
                    new BaseQuery("summary.hardware.model", 0, 0, false, false, SourceType.diagnostics, ComputeType.info),
                    new BaseQuery("vm", 0, 0, false, false, SourceType.diagnostics, ComputeType.info),
            };

    // keep these for backward compatibility with v1 connector until removal
    private static final BaseQuery[] baseSyntheticList =
            {
                    new BaseQuery("syn.host.cpu.used", 75, 90, false, false, SourceType.compute, ComputeType.synthetic),
                    new BaseQuery("syn.host.mem.used", 80, 95, false, false, SourceType.compute, ComputeType.synthetic),
            };

    private static BaseSynthetic[] baseSyntheticMaster =
            {
                    new BaseSynthetic("syn.host.cpu.used",
                            "summary.quickStats.overallCpuUsage", 1.0,
                            "summary.hardware.cpuMhz", false, true),
                            //"summary.hardware.cpuMhz.scaled", false, true),

                    new BaseSynthetic("syn.host.cpu.unused",
                            "summary.quickStats.overallCpuUsage", 1.0,
                            "summary.hardware.cpuMhz", true, true),
                            //"summary.hardware.cpuMhz.scaled", true, true),

                    new BaseSynthetic("syn.host.mem.used",
                            "summary.quickStats.overallMemoryUsage", 1024.0 * 1024.0,
                            "summary.hardware.memorySize", false, true),

                    new BaseSynthetic("syn.host.mem.unused",
                            "summary.quickStats.overallMemoryUsage", 1024.0 * 1024.0,
                            "summary.hardware.memorySize", true, true),
            };

    public BaseQuery[] getDefaultSyntheticList() {
        return baseSyntheticList;
    }

    public BaseQuery[] getDefaultMetricList() {
        return baseMetricList;
    }

    public BaseQuery[] getDefaultConfigList() {
        return baseConfigList;
    }

    public BaseSynthetic[] getSyntheticMaster() {
        return baseSyntheticMaster;
    }

    public BaseSynthetic getSynthetic(String handle) {
        for (BaseSynthetic v : getSyntheticMaster()) {
            if (v.getHandle().equals(handle))
                return v;
        }
        return null;
    }

    public boolean isMetric(String path) {
        for (BaseQuery query : getDefaultMetricList()) {
            if (path.equals(query.getQuery()))
                return true;
        }
        return false;
    }

    public boolean isConfig(String path) {
        for (BaseQuery query : getDefaultConfigList()) {
            if (path.equals(query.getQuery()))
                return true;
        }
        return false;
    }


    /**
     * getMonitorState()
     * <p>
     * ALERT: needs to be run after other variables are set!
     * <p>
     * <p/>
     * turns a number of retrieved VIM25 properties into a 'runtime state'
     * of the virtual machine.  Kind of defensive code, that could be made
     * more trim, and less suspicious.
     * <p/>
     * MODES:
     * powerState    connectionState     result    extra
     * ----------------------------------------------------------
     * null                  x       UNREACHABLE   powerState not set
     * x               null       UNREACHABLE   connectionState not set
     * ----------------------------------------------------------
     * powered_on        connected      UP
     * powered_on        suspended      SUSPENDED
     * powered_on              ?        DOWN      {connectionState}
     * ----------------------------------------------------------
     * powered_off             ?        DOWN
     * ?                 ?        UNREACHABLE   {powerState}
     * ----------------------------------------------------------
     *
     * @return
     */
    public static final String UP = "UP";
    public static final String WARNING = "WARNING";
    public static final String SUSPENDED = "SUSPENDED";
    public static final String UNSCHEDULED_DOWN = "UNSCHEDULED DOWN";
    public static final String SCHEDULED_DOWN = "SCHEDULED DOWN";
    public static final String UNREACHABLE = "UNREACHABLE";

    public String getMonitorState() {
        String connectionState = null;
        String powerState = null;
        BaseMetric metric = null;
        String monitorState = null;
        String extraState = null;

        if ((metric = getMetric("summary.runtime.connectionState")) != null) {
            connectionState = metric.getCurrValue();
            connectionState = connectionState.toUpperCase();
        }

        if ((metric = getMetric("summary.runtime.powerState")) != null) {
            powerState = metric.getCurrValue();
            if (powerState.equals("poweredOn")) {
                powerState = "POWERED_ON";
            } else if (powerState.equals("poweredOff")) {
                powerState = "POWERED_OFF";
            } else {
                powerState = powerState.toUpperCase();
            }
        }

        if (powerState == null)
            monitorState = UNREACHABLE;
        else if (connectionState == null)
            monitorState = UNREACHABLE;
        else if (powerState.equalsIgnoreCase("powered_on")) {
            if (connectionState.equalsIgnoreCase("connected"))
                monitorState = UP;
            else if (connectionState.equalsIgnoreCase("suspended"))
                monitorState = SUSPENDED;
            else
                monitorState = UNSCHEDULED_DOWN;
        } else if (powerState.equalsIgnoreCase("powered_off"))
            monitorState = SCHEDULED_DOWN;
        else
            monitorState = UNREACHABLE;

        extraState = "pwr=" + powerState + " "
                + "con=" + connectionState + " ";

        this.setRunExtra(extraState == null ? "" : extraState);

        return monitorState;
    }

    public String getMonitorStateByStatus() {
        BaseMetric metric = getConfig("overallStatus");
        if (metric == null)
            return UNREACHABLE;
        String state = metric.getCurrValue();
        if (state != null) {
            state = state.toUpperCase();
            if (state.equals("GREEN"))
                return UP;
            /*WARNING is not a valid Host state and therefore YELLOW needs to be mapped to UP */
            else if (state.equals("YELLOW"))
                return UP;
            else if (state.equals("ORANGE"))
                return WARNING;
            else if (state.equals("RED"))
                return UNREACHABLE;
        }
        return UNREACHABLE;
    }

    public boolean isMetricCollected(BaseQuery query) {
        return query.getComputeType() == null || !query.getComputeType().equals(ComputeType.synthetic);
    }

    public boolean isMetricPoolable(BaseQuery query) {
        return query.getSourceType().equals(SourceType.diagnostics) || query.getSourceType().equals(SourceType.compute);
    }

    public boolean isMetricMonitored(BaseQuery query) {
        return query.getComputeType().equals(ComputeType.query);
        //return query.getQuery().startsWith("summary.quickStats") || query.getQuery().startsWith("summary.runtime");
    }

    public boolean isRunning(MetricCollectionState collector) {
        return MetricsUtils.isRunning(collector);
    }

}
