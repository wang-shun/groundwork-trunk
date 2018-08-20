package org.groundwork.cloudhub.connectors.vmware;


import org.apache.log4j.Logger;
import org.groundwork.cloudhub.connectors.vmware2.MetricsUtils;
import org.groundwork.cloudhub.metrics.*;

public class VMwareVM extends BaseVM implements DefaultMetricProvider {
    private static Logger log = Logger.getLogger(VMwareVM.class);

    public VMwareVM(String vmName) {
        super(vmName);
    }

    // keep these for backward compatibility with v1 connector until removal
    private static final BaseQuery[] baseMetricList =
    {
        new BaseQuery("summary.quickStats.balloonedMemory", 1000, 2000, true, false, SourceType.compute, ComputeType.query),
        new BaseQuery("summary.quickStats.compressedMemory", 1000, 2000, true, false, SourceType.compute, ComputeType.query),
        new BaseQuery("summary.quickStats.consumedOverheadMemory", 1000, 2000, true, false, SourceType.compute, ComputeType.query),
        new BaseQuery("summary.quickStats.guestMemoryUsage", 3000, 5000, true, false, SourceType.compute, ComputeType.query),
        new BaseQuery("summary.quickStats.hostMemoryUsage", 4000, 5000, true, false, SourceType.compute, ComputeType.query),
        new BaseQuery("summary.quickStats.overallCpuDemand", 2000, 3000, true, false, SourceType.compute, ComputeType.query),
        new BaseQuery("summary.quickStats.overallCpuUsage", 1000, 3000, true, false, SourceType.compute, ComputeType.query),
        new BaseQuery("summary.quickStats.privateMemory", 1000, 2000, true, false, SourceType.compute, ComputeType.query),
        new BaseQuery("summary.quickStats.sharedMemory", 1000, 2000, true, false, SourceType.compute, ComputeType.query),
        new BaseQuery( "summary.quickStats.ssdSwappedMemory",       1000,    2000, true, false, SourceType.compute, ComputeType.query ),
        new BaseQuery("summary.quickStats.swappedMemory", 1000, 2000, true, false, SourceType.compute, ComputeType.query),
        new BaseQuery("summary.quickStats.uptimeSeconds", 3197400, 6394800, false, false, SourceType.compute, ComputeType.query),

        new BaseQuery("summary.runtime.memoryOverhead", 0, 0, true, false, SourceType.diagnostics, ComputeType.query),
        new BaseQuery("summary.runtime.maxCpuUsage", 0, 0, true, false, SourceType.diagnostics, ComputeType.query),
        new BaseQuery("summary.runtime.maxMemoryUsage", 0, 0, true, false, SourceType.diagnostics, ComputeType.query),

        new BaseQuery("summary.storage.committed", 0, 0, true, false, SourceType.compute, ComputeType.query),
        new BaseQuery("summary.storage.uncommitted", 0, 0, true, false, SourceType.compute, ComputeType.query),

        // These should be moved into Configs, but doing so breaks old connector
        new BaseQuery("summary.runtime.bootTime", 0, 0, false, false, SourceType.diagnostics, ComputeType.query),
        new BaseQuery("summary.runtime.host", 0, 0, false, false, SourceType.diagnostics, ComputeType.query),
        new BaseQuery("summary.runtime.powerState", 0, 0, false, false, SourceType.diagnostics, ComputeType.query),
        new BaseQuery("summary.runtime.connectionState", 0, 0, false, false, SourceType.diagnostics, ComputeType.query),

    };

    private static final BaseQuery[] baseConfigList =
    {
        new BaseQuery("name", 0, 0, false, false, SourceType.diagnostics, ComputeType.info),
        new BaseQuery("guest.ipAddress", 0, 0, false, false, SourceType.diagnostics, ComputeType.info),
        new BaseQuery("guest.net", 0, 0, false, false, SourceType.diagnostics, ComputeType.info),
        new BaseQuery("guest.guestState", 0, 0, false, false, SourceType.diagnostics, ComputeType.info),
        new BaseQuery("summary.config.memorySizeMB", 0, 0, false, false, SourceType.diagnostics, ComputeType.info),
        new BaseQuery("summary.config.name", 0, 0, false, false, SourceType.diagnostics, ComputeType.info),
        new BaseQuery("summary.config.numCpu", 0, 0, false, false, SourceType.diagnostics, ComputeType.info),
        new BaseQuery("summary.config.numEthernetCards", 0, 0, false, false, SourceType.diagnostics, ComputeType.info),
        new BaseQuery("summary.config.numVirtualDisks", 0, 0, false, false, SourceType.diagnostics, ComputeType.info),
    };

    // keep these for backward compatibility with v1 connector until removal
    private static final BaseQuery[] baseSyntheticList =
    {
        new BaseQuery("syn.vm.mem.balloonToConfigMemSize.used", 10, 20, true, false, SourceType.compute, ComputeType.synthetic),
        new BaseQuery("syn.vm.mem.compressedToConfigMemSize.used", 10, 20, true, false, SourceType.compute, ComputeType.synthetic),
        new BaseQuery("syn.vm.mem.sharedToConfigMemSize.used", 25, 75, true, false, SourceType.compute, ComputeType.synthetic),
        new BaseQuery("syn.vm.mem.swappedToConfigMemSize.used", 10, 20, true, false, SourceType.compute, ComputeType.synthetic),
        new BaseQuery("syn.vm.mem.guestToConfigMemSize.used", 70, 85, true, false, SourceType.compute, ComputeType.synthetic),
        new BaseQuery("syn.vm.cpu.cpuToMax.used", 75, 95, true, false, SourceType.compute, ComputeType.synthetic),

        //new BaseQuery( "syn.vm.mem.balloonToConfigMemSize.unused",    90,       80, true, false ),
        //new BaseQuery( "syn.vm.mem.compressedToConfigMemSize.unused", 90,       80, true, false ),
        //new BaseQuery( "syn.vm.mem.sharedToConfigMemSize.unused",     75,       25, true, false ),
        //new BaseQuery( "syn.vm.mem.swappedToConfigMemSize.unused",    90,       80, true, false ),
        //new BaseQuery( "syn.vm.mem.guestToConfigMemSize.unused",      30,       15, true, false ),
        //new BaseQuery( "syn.vm.cpu.cpuToMax.unused",                  25,        5, true, false ),
    };

    private static final BaseSynthetic[] baseSyntheticMaster =
    {
        new BaseSynthetic("syn.vm.mem.balloonToConfigMemSize.used",
                "summary.quickStats.balloonedMemory", 1.0,
                "summary.config.memorySizeMB", false, true),

        new BaseSynthetic("syn.vm.mem.balloonToConfigMemSize.unused",
                "summary.quickStats.balloonedMemory", 1.0,
                "summary.config.memorySizeMB", true, true),

        new BaseSynthetic("syn.vm.mem.compressedToConfigMemSize.used",
                "summary.quickStats.compressedMemory", 1.0,
                "summary.config.memorySizeMB", false, true),

        new BaseSynthetic("syn.vm.mem.compressedToConfigMemSize.unused",
                "summary.quickStats.compressedMemory", 1.0,
                "summary.config.memorySizeMB", true, true),

        new BaseSynthetic("syn.vm.mem.swappedToConfigMemSize.used",
                "summary.quickStats.swappedMemory", 1.0,
                "summary.config.memorySizeMB", false, true),

        new BaseSynthetic("syn.vm.mem.swappedToConfigMemSize.unused",
                "summary.quickStats.swappedMemory", 1.0,
                "summary.config.memorySizeMB", true, true),

        new BaseSynthetic("syn.vm.mem.sharedToConfigMemSize.used",
                "summary.quickStats.sharedMemory", 1.0,
                "summary.config.memorySizeMB", false, true),

        new BaseSynthetic("syn.vm.mem.sharedToConfigMemSize.unused",
                "summary.quickStats.sharedMemory", 1.0,
                "summary.config.memorySizeMB", true, true),

        new BaseSynthetic("syn.vm.mem.guestToConfigMemSize.used",
                "summary.quickStats.guestMemoryUsage", 1.0,
                "summary.config.memorySizeMB", false, true),

        new BaseSynthetic("syn.vm.mem.guestToConfigMemSize.unused",
                "summary.quickStats.guestMemoryUsage", 1.0,
                "summary.config.memorySizeMB", true, true),

        new BaseSynthetic("syn.vm.cpu.cpuToMax.used",
                "summary.quickStats.overallCpuUsage", 1.0,
                "summary.runtime.maxCpuUsage", false, true),

        new BaseSynthetic("syn.vm.cpu.cpuToMax.unused",
                "summary.quickStats.overallCpuUsage", 1.0,
                "summary.runtime.maxCpuUsage", true, true),
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
        for (BaseSynthetic v : baseSyntheticMaster)
            if (v.getHandle().equals(handle))
                return v;
        return null;
    }

    /**
     * getMonitorState()
     * <p/>
     * turns a number of retrieved VIM25 properties into a 'runtime state'
     * of the virtual machine.  Kind of defensive code, that could be made
     * more trim, and less suspicious.
     * <p/>
     * MODES:
     * powerState   guestState   connectionState     result    extra
     * ----------------------------------------------------------------------
     * null           x                  x       UNREACHABLE   no powerstate
     * x        null                  x       UNREACHABLE   no gueststate
     * x           x               null       UNREACHABLE   no connectionstate
     * ----------------------------------------------------------------------
     * powered_on   running          connected      UP
     * powered_on   suspended        connected      SUSPENDED
     * powered_on        ?           connected      UP        {guestState}
     * powered_on        ?           suspended      SUSPENDED
     * powered_on   suspended              ?        SUSPENDED
     * powered_on   notrunning             ?        UNSCHED DOWN
     * powered_on        ?                 ?        UNSCHED DOWN      {guestState}
     * ----------------------------------------------------------------------
     * suspended         ?                 ?        SUSPENDED
     * powered_off       ?                 ?        SCHEDULED_DOWN
     * ?           ?                 ?        UNREACHABLE   {powerState}
     * ----------------------------------------------------------------------
     *
     * @return
     */
    protected static final String sUnreachable = "UNREACHABLE";
    protected static final String sUp = "UP";
    protected static final String sSuspended = "SUSPENDED";
    protected static final String sUnschedDown = "UNSCHEDULED DOWN";
    protected static final String sSchedDown = "SCHEDULED DOWN";

    public String getMonitorState() {
        String connectionState = null; // convenient variables
        String powerState = null; // which make for more readable
        String guestState = null; // code.
        BaseMetric metric = null;
        String r = null; // receives       state information
        String x = null; // receives extra state information

        if ((metric = getMetric("summary.runtime.connectionState")) != null) {
            connectionState = metric.getCurrValue();
            connectionState = connectionState.toUpperCase();
        }

        if ((metric = getMetric("summary.runtime.powerState")) != null) {
            powerState = metric.getCurrValue();
            if (powerState.equals("poweredOn")) {
                powerState = "POWERED_ON";
            }
            else if (powerState.equals("poweredOff")) {
                powerState = "POWERED_OFF";
            }
            else {
                powerState = powerState.toUpperCase();
            }
        }

        if ((metric = getConfig("guest.guestState")) != null) {
            guestState = metric.getCurrValue();
        }

        if (powerState == null) r = sUnreachable;
        else if (guestState == null) r = sUnreachable;
        else if (connectionState == null) r = sUnreachable;
        else if (powerState.equalsIgnoreCase("powered_on"))
            if (connectionState.equalsIgnoreCase("connected"))
                if (guestState.equalsIgnoreCase("running")) r = sUp;
                else if (guestState.equalsIgnoreCase("suspended")) r = sSuspended;
                else if (guestState.equalsIgnoreCase("notrunning")) r = sUnschedDown;
                else r = sUp;
            else if (connectionState.equalsIgnoreCase("suspended")) r = sSuspended;
            else if (connectionState.equalsIgnoreCase("notrunning")) r = sUnschedDown;
            else if (guestState.equalsIgnoreCase("suspended")) r = sSuspended;
            else r = sUnschedDown;
        else if (powerState.equalsIgnoreCase("powered_off")) r = sSchedDown;
        else if (powerState.equalsIgnoreCase("suspended")) r = sSuspended;
        else r = sUnreachable;

        x = "pwr=" + powerState + " "
                + "con=" + connectionState + " "
                + "guest=" + guestState;
        if (guestState.equalsIgnoreCase("notrunning")) {
            x += ", (VMTools not installed or running)";
        }
        this.setRunExtra(x);
        return r;
    }
    
    public boolean isMetricCollected(BaseQuery query) {
        return ((query.getComputeType() == null || !query.getComputeType().equals(ComputeType.synthetic))
                && !query.getQuery().startsWith(SnapshotService.SNAPSHOTS_PREFIX));
    }

    public boolean isMetricPoolable(BaseQuery query) {
        return query.getSourceType().equals(SourceType.diagnostics) || query.getSourceType().equals(SourceType.compute);
    }

    public boolean isMetricMonitored(BaseQuery query) {
        return query.getComputeType().equals(ComputeType.query);
//        return (query.getQuery().startsWith("summary.quickStats")
//                || query.getQuery().startsWith("summary.runtime")
//                || query.getQuery().startsWith("summary.storage")
//                );
    }

    public boolean isRunning(MetricCollectionState collector) {
        return MetricsUtils.isRunning(collector);
    }
    
}

