package org.groundwork.cloudhub.connectors.netapp;

import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.groundwork.cloudhub.metrics.ExtendedSynthetic;

public class NetAppHost extends BaseHost {

    private static final BaseQuery[] baseMetricList =
    {
        new BaseQuery( "cpu-busytime",   -1,  -1,  false, false ),
        new BaseQuery( "env-failed-fan-count",   -1,  -1,  false, false ),
        new BaseQuery( "env-failed-power-supply-count",   -1,  -1,  false, false ),
        new BaseQuery( "env-over-temperature",   -1,  -1,  false, false ),
        new BaseQuery( "node-uptime",   -1,  -1,  false, false ),
        new BaseQuery( "nvram-battery-status",   -1,  -1,  false, false ),
        new BaseQuery( "product-version",   -1,  -1,  false, false ),


    };

    private static final BaseQuery[] baseConfigList = {
    };

    private static final BaseQuery[] baseSyntheticList = {
        new BaseQuery("syn.cpu-controller-usage", -1, -1, true, true),
    };

    private static ExtendedSynthetic[] syntheticDefinitions = {
        new ExtendedSynthetic("syn.cpu-controller-usage",
                "cpu-busytime", 1.0,
                "node-uptime", false, true)
    };

    private NetAppHost() {}

    public NetAppHost(String hostName) {
        super(hostName);
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

    public ExtendedSynthetic getSynthetic(String handle) {
        for (ExtendedSynthetic v : getSyntheticDefinitions()) {
            if (v.getHandle().equals(handle))
                return v;
        }
        return null;
    }

}
