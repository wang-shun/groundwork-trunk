package org.groundwork.cloudhub.connectors.openstack;


import org.groundwork.cloudhub.metrics.BaseQuery;
import org.groundwork.cloudhub.metrics.BaseVM;
import org.groundwork.cloudhub.metrics.ComputeType;
import org.groundwork.cloudhub.metrics.ExtendedSynthetic;
import org.groundwork.cloudhub.metrics.SourceType;

public class OpenStackVM extends BaseVM {

    private static final BaseQuery[] baseMetricList =
    {
        // Ceilometer
        new BaseQuery( "disk.read.bytes",       -1,        -1,  true,  true, SourceType.ceilometer, ComputeType.query ),
        new BaseQuery( "disk.read.requests",    -1,        -1,  true,  true, SourceType.ceilometer, ComputeType.query ),
        new BaseQuery( "disk.write.bytes",       -1,        -1,  true,  true, SourceType.ceilometer, ComputeType.query ),
        new BaseQuery( "disk.write.requests",    -1,        -1,  true,  true, SourceType.ceilometer, ComputeType.query ),
        new BaseQuery( "cpu_util",              -1,        -1,  true,  true, SourceType.ceilometer, ComputeType.query ),

        new BaseQuery( "network.outgoing.bytes",       -1,        -1,  true,  true, SourceType.ceilometer, ComputeType.query ),
        new BaseQuery( "network.outgoing.packets",    -1,        -1,  true,  true, SourceType.ceilometer, ComputeType.query ),
        new BaseQuery( "network.outgoing.bytes",       -1,        -1,  true,  true, SourceType.ceilometer, ComputeType.query ),
        new BaseQuery( "network.outgoing.packets",    -1,        -1,  true,  true, SourceType.ceilometer, ComputeType.query ),

        // Diagnostics
        new BaseQuery( "memory",             -1,        -1,  true,  true, SourceType.diagnostics, ComputeType.query),
        new BaseQuery( "memory-actual",      -1,        -1,  true,  true, SourceType.diagnostics, ComputeType.query),
        //new BaseQuery( "memory-available",   -1,        -1,  true,  true, SourceType.diagnostics, ComputeType.query ),
        //new BaseQuery( "memory-unused",      -1,        -1,  true,  true, SourceType.diagnostics, ComputeType.query ),
        new BaseQuery( "memory-rss",         -1,        -1,  true,  true, SourceType.diagnostics, ComputeType.query ),
        //new BaseQuery( "memory-swap_out",    -1,        -1,  true,  true, SourceType.diagnostics, ComputeType.query ),
        //new BaseQuery( "memory-swap_in",     -1,        -1,  true,  true, SourceType.diagnostics, ComputeType.query ),
        //new BaseQuery( "memory-minor_fault", -1,        -1,  true,  true, SourceType.diagnostics, ComputeType.query ),
        //new BaseQuery( "memory-major_fault", -1,        -1,  true,  true, SourceType.diagnostics, ComputeType.query ),

        new BaseQuery( "cpu(.)_time",        -1,        -1,  false,  false, SourceType.diagnostics, ComputeType.regex ),

        new BaseQuery( "tap(.+)_rx",         -1,        -1,  true,  true, SourceType.diagnostics, ComputeType.regex ),
        new BaseQuery( "tap(.+)_rx_packets", -1,        -1,  true,  true, SourceType.diagnostics, ComputeType.regex  ),
        new BaseQuery( "tap(.+)_rx_errors",  -1,        -1,  true,  true, SourceType.diagnostics, ComputeType.regex  ),
        new BaseQuery( "tap(.+)_rx_drop",    -1,        -1,  true,  true, SourceType.diagnostics, ComputeType.regex  ),

        new BaseQuery( "tap(.+)_tx",         -1,        -1,  true,  true, SourceType.diagnostics, ComputeType.regex  ),
        new BaseQuery( "tap(.+)_tx_packets", -1,        -1,  true,  true, SourceType.diagnostics, ComputeType.regex  ),
        new BaseQuery( "tap(.+)_tx_errors",  -1,        -1,  true,  true, SourceType.diagnostics, ComputeType.regex  ),
        new BaseQuery( "tap(.+)_tx_drop",    -1,        -1,  true,  true, SourceType.diagnostics, ComputeType.regex  ),

        new BaseQuery( "vd(.)_read",         -1,        -1,  true,  true, SourceType.diagnostics, ComputeType.regex  ),
        new BaseQuery( "vd(.)_write",        -1,        -1,  true,  true, SourceType.diagnostics, ComputeType.regex  ),
        new BaseQuery( "vd(.)_read_req",     -1,        -1,  true,  true, SourceType.diagnostics, ComputeType.regex  ),
        new BaseQuery( "vd(.)_write_req",    -1,        -1,  true,  true, SourceType.diagnostics, ComputeType.regex  ),

    };

    private static final BaseQuery[] baseConfigList = {
    };

    private static final BaseQuery[] baseSyntheticList =
    {
            new BaseQuery("syn.cpu(.)_time", 2000, 4000, true, true, SourceType.diagnostics, ComputeType.regex)
    };

    private static ExtendedSynthetic[] syntheticDefinitions = {
            new ExtendedSynthetic("syn.cpu(.)_time",
                    "cpu(.)_time",
                    ExtendedSynthetic.SyntheticOperation.divide,
                    1048576)
    };

    private OpenStackVM() {}

    public OpenStackVM(String vmName) {
        super(vmName);
    }

    public static BaseQuery[] getDefaultMetrics() {
        return baseMetricList;
    }

    public static BaseQuery[] getDefaultConfigs() {
        return baseConfigList;
    }

    public static BaseQuery[] getSynthetics() {
        return baseSyntheticList;
    }

    public static ExtendedSynthetic[] getSyntheticDefinitions() {
        return syntheticDefinitions;
    }

}
