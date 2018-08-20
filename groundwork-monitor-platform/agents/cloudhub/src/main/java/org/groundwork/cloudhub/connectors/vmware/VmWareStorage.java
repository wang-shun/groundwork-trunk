package org.groundwork.cloudhub.connectors.vmware;

import org.apache.log4j.Logger;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.groundwork.cloudhub.metrics.BaseSynthetic;
import org.groundwork.cloudhub.metrics.ComputeType;
import org.groundwork.cloudhub.metrics.DefaultMetricProvider;
import org.groundwork.cloudhub.metrics.SourceType;

public class VmWareStorage extends VMwareHost implements DefaultMetricProvider {

    private static Logger log = Logger.getLogger(VmWareStorage.class);

    public VmWareStorage(String vmName) {
        super(vmName);
    }

    // keep these for backward compatibility with v1 connector until removal
    private static final BaseQuery[] storageMetricList =
    {
        new BaseQuery("summary.capacity", -1, -1, false, true, SourceType.storage, ComputeType.query),
        new BaseQuery("summary.freeSpace", -1, -1, true, true, SourceType.storage, ComputeType.query),
        new BaseQuery("summary.uncommitted", -1, -1, false, true, SourceType.storage, ComputeType.query)
    };

    private static final BaseQuery[] storageConfigList =
    {
        new BaseQuery("name", 0, 0, false, false, SourceType.storage, ComputeType.info),
        new BaseQuery("summary.url", 0, 0, false, false, SourceType.storage, ComputeType.info),
        new BaseQuery("summary.type", 0, 0, false, false, SourceType.storage, ComputeType.info),
        new BaseQuery("overallStatus", 0, 0, false, false, SourceType.storage, ComputeType.info),
        new BaseQuery("summary.accessible", 0, 0, false, false, SourceType.storage, ComputeType.info)
    };

    // keep these for backward compatibility with v1 connector until removal
    private static final BaseQuery[] storageSyntheticList =
    {
        new BaseQuery("syn.storage.percent.used", -1, -1, true, true, SourceType.storage, ComputeType.synthetic),
    };

    // keep these for backward compatibility with v1 connector until removal
    private static final BaseSynthetic[] storageSyntheticMaster =
    {
        new BaseSynthetic("syn.storage.percent.used",
                "summary.freeSpace", 1.0,
                "summary.capacity", true, true),
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

    @Override
    public String getMonitorState() {
        return getMonitorStateByStatus();
    }

    public boolean isMetricCollected(BaseQuery query) {
        return (query.getComputeType() == null || !query.getComputeType().equals(ComputeType.synthetic));
    }


    public boolean isMetricPoolable(BaseQuery query) {
        if (query.getSourceType().equals(SourceType.storage)) {
            return true;
        }
        if (query.getServiceType() == null) {
            return false;
        }
        return query.getServiceType().equals(SourceType.storage.name());
    }

    public boolean isMetricMonitored(BaseQuery query) {
        return query.getComputeType().equals(ComputeType.query);
        //return query.getQuery().startsWith("summary");
    }
}
