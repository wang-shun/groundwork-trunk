package org.groundwork.cloudhub.connectors.opendaylight;

import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.groundwork.cloudhub.metrics.ExtendedSynthetic;

public class OpenDaylightHost extends BaseHost {

    private static final BaseQuery[] baseMetricList = {
    };

    private static final BaseQuery[] baseConfigList = {
    };

    private static final BaseQuery[] baseSyntheticList = {
    };

    private static ExtendedSynthetic[] syntheticDefinitions = {
    };

    private OpenDaylightHost() {}

    public OpenDaylightHost(String hostName) {
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

}
