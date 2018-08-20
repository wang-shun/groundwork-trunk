package org.groundwork.cloudhub.connectors.opendaylight;


import org.groundwork.cloudhub.metrics.BaseQuery;
import org.groundwork.cloudhub.metrics.BaseVM;
import org.groundwork.cloudhub.metrics.ExtendedSynthetic;

public class OpenDaylightVM extends BaseVM {

    private static final BaseQuery[] baseMetricList =
    {
        new BaseQuery( "receiveBytes",    20000000,  70000000,  true, true ),
        new BaseQuery( "transmitBytes",   20000000,  70000000,  true, true ),
        new BaseQuery( "receiveErrors",   50,        100,       false, true ),
        new BaseQuery( "transmitErrors",   100,      500,       false, true ),
    };

    private static final BaseQuery[] baseConfigList = {
    };

    private static final BaseQuery[] baseSyntheticList = {
    };

    private static ExtendedSynthetic[] syntheticDefinitions = {
    };

    private OpenDaylightVM() {}

    public OpenDaylightVM(String vmName) {
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
