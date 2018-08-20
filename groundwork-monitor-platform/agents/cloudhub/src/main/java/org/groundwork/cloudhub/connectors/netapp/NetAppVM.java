package org.groundwork.cloudhub.connectors.netapp;


import org.groundwork.cloudhub.metrics.BaseQuery;
import org.groundwork.cloudhub.metrics.BaseVM;
import org.groundwork.cloudhub.metrics.ExtendedSynthetic;

public class NetAppVM extends BaseVM {

    private NetAppNode.NetAppNodeType nodeType;
    private String controller;
    private String aggregate;

    private static final BaseQuery[] baseMetricList =
    {
        new BaseQuery( "volume-inode-attributes.files-total",   -1,  -1,  false, false ),
        new BaseQuery( "volume-inode-attributes.files-used",   -1,  -1,  true, true ),
        new BaseQuery( "volume-space-attributes.size-total",   -1,  -1,  false, false ),
        new BaseQuery( "volume-space-attributes.size-used",   -1,  -1,  true, true ),

        new BaseQuery( "volume-space-attributes.size-available",   -1,  -1,  false, false ),
        new BaseQuery( "volume-space-attributes.percentage-size-used",   -1,  -1,  true, true ),

        new BaseQuery( "aggr-raid-attributes.disk-count",   -1,  -1,  true, false ),
        new BaseQuery( "aggr-volume-count-attributes.flexvol-count",   -1,  -1,  true, false ),

        new BaseQuery( "aggr-space-attributes.size-total",   -1,  -1,  false, false ),
        new BaseQuery( "aggr-space-attributes.size-used",   -1,  -1,  false, false ),
        new BaseQuery( "aggr-space-attributes.size-available",   -1,  -1,  false, false ),
        new BaseQuery( "aggr-space-attributes.percent-used-capacity",   -1,  -1,  true, true ),

    };

    private static final BaseQuery[] baseConfigList = {
    };


    private static final BaseQuery[] baseSyntheticList =
    {
        new BaseQuery("syn.volume.percent.files.used", -1, -1, true, true),
        new BaseQuery("syn.volume.percent.bytes.used", -1, -1, true, true),
        new BaseQuery("syn.volume.gb.used", -1, -1, true, true),
        new BaseQuery("syn.volume.gb.available", -1, -1, true, true),
        new BaseQuery("syn.aggregate.gb.used", -1, -1, true, true),
        new BaseQuery("syn.aggregate.gb.available", -1, -1, true, true)
    };


    private static ExtendedSynthetic[] syntheticDefinitions =
    {
        new ExtendedSynthetic("syn.volume.percent.files.used",
                "volume-inode-attributes.files-used", 1.0,
                "volume-inode-attributes.files-total", false, true),
        new ExtendedSynthetic("syn.volume.percent.bytes.used",
                "volume-space-attributes.size-used", 1.0,
                "volume-space-attributes.size-total", false, true),
        new ExtendedSynthetic("syn.volume.gb.used",
                "volume-space-attributes.size-used",
                ExtendedSynthetic.SyntheticOperation.divide,
                1000000000.0),
        new ExtendedSynthetic("syn.volume.gb.available",
                "volume-space-attributes.size-available",
                ExtendedSynthetic.SyntheticOperation.divide,
                1000000000.0),
        new ExtendedSynthetic("syn.aggregate.gb.used",
                "aggr-space-attributes.size-used",
                ExtendedSynthetic.SyntheticOperation.divide,
                1000000000.0),
        new ExtendedSynthetic("syn.aggregate.gb.available",
                "aggr-space-attributes.size-available",
                ExtendedSynthetic.SyntheticOperation.divide,
                1000000000.0)
    };

    private NetAppVM() {}

    public NetAppVM(String vmName, NetAppNode.NetAppNodeType netAppType) {
        super(vmName);
        this.nodeType = netAppType;
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

    public NetAppNode.NetAppNodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NetAppNode.NetAppNodeType nodeType) {
        this.nodeType = nodeType;
    }

    public boolean isVolume() {
        return nodeType == NetAppNode.NetAppNodeType.Volume;
    }

    public boolean isAggregate() {
        return nodeType == NetAppNode.NetAppNodeType.Aggregate;
    }

    public String getController() {
        return controller;
    }

    public void setController(String controller) {
        this.controller = controller;
    }

    public String getAggregate() {
        return aggregate;
    }

    public void setAggregate(String aggregate) {
        this.aggregate = aggregate;
    }
}
