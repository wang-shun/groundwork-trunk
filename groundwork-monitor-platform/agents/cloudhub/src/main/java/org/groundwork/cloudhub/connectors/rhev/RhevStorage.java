package org.groundwork.cloudhub.connectors.rhev;

import org.apache.log4j.Logger;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.groundwork.cloudhub.metrics.BaseSynthetic;

public class RhevStorage extends RhevHost {

    private static Logger log = Logger.getLogger(RhevStorage.class);

    public RhevStorage(String name)
    {
        super(name);
    }


    private static final BaseQuery[] storageMetricList =
    {
        new BaseQuery("used", 0, 0, false, true),
        new BaseQuery("available", 10, 1, true, true),
        new BaseQuery("committed", 0, 0, false, true)
    };

    private static final BaseQuery[] storageConfigList =
    {
        new BaseQuery("name", 0, 0, false, false),
        new BaseQuery("storageFormat", 0, 0, false, false),
        new BaseQuery("type", 0, 0, false, false),
        new BaseQuery("storage.type", 0, 0, false, false),
        new BaseQuery("storage.path", 0, 0, false, false),
    };

    private static final BaseQuery[] storageSyntheticList =
    {
        new BaseQuery("syn.storage.percent.free", 30, 10, true, true),
    };

    private static final BaseSynthetic[] storageSyntheticMaster =
    {
        new BaseSynthetic("syn.storage.percent.free",
                "used", 1.0,
                "available", true, true),
    };

    @Override
    public BaseQuery[] getDefaultSyntheticList() {
        return storageSyntheticList;
    }

    @Override
    public BaseQuery[] getDefaultMetricList() {
        return storageMetricList;
    }

    @Override
    public BaseQuery[] getDefaultConfigList() {
        return storageConfigList;
    }

    @Override
    public BaseSynthetic[] getSyntheticMaster() {
        return storageSyntheticMaster;
    }



}
