package org.groundwork.cloudhub.connectors.openstack;

import org.groundwork.cloudhub.metrics.BaseHost;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.groundwork.cloudhub.metrics.BaseSynthetic;
import org.groundwork.cloudhub.metrics.ExtendedSynthetic;

public class OpenStackHost extends BaseHost {

    private static final BaseQuery[] baseMetricList =
    {
        new BaseQuery( "running_vms",          8,        1,  true,  true ),
        new BaseQuery( "free_ram_mb",          2,        1,  true,  true ),
        new BaseQuery( "free_disk_gb",         5,        1,  true,  true )
    };

    private static final BaseQuery[] baseConfigList = {
    };

    private static final BaseQuery[] baseSyntheticList = {
    };

    private static ExtendedSynthetic[] syntheticDefinitions = {
    };

    private OpenStackHost() {}

    public OpenStackHost(String hostName) {
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

    public BaseSynthetic getSynthetic(String handle) {
        for (BaseSynthetic v : getSyntheticDefinitions()) {
            if (v.getHandle().equals(handle))
                return v;
        }
        return null;
    }

}
