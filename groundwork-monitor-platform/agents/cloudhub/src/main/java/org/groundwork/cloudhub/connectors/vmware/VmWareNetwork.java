package org.groundwork.cloudhub.connectors.vmware;

import org.apache.log4j.Logger;
import org.groundwork.cloudhub.metrics.BaseQuery;
import org.groundwork.cloudhub.metrics.BaseSynthetic;
import org.groundwork.cloudhub.metrics.ComputeType;
import org.groundwork.cloudhub.metrics.DefaultMetricProvider;
import org.groundwork.cloudhub.metrics.SourceType;

public class VmWareNetwork extends VMwareHost implements DefaultMetricProvider {

    private static Logger log = Logger.getLogger(VmWareNetwork.class);

    public VmWareNetwork(String vmName) {
        super(vmName);
    }

    private static final BaseQuery[] networkMetricList =
    {
    };

    private static final BaseQuery[] networkConfigList =
    {
        new BaseQuery("name", 0, 0, false, false, SourceType.storage, ComputeType.info),
        new BaseQuery("summary.ipPoolName", 0, 0, false, false,  SourceType.storage, ComputeType.info),
        new BaseQuery("overallStatus", 0, 0, false, false,  SourceType.storage, ComputeType.info),
        new BaseQuery("summary.accessible", 0, -1, false, true,  SourceType.storage, ComputeType.query)
    };

    private static final BaseQuery[] networkSyntheticList =
    {
    };

    private static final BaseSynthetic[] networkSyntheticMaster =
    {
    };

    @Override
    public BaseQuery[] getDefaultSyntheticList() {
        return networkSyntheticList;
    }

    @Override
    public BaseQuery[] getDefaultMetricList() {
        return networkMetricList;
    }

    @Override
    public BaseQuery[] getDefaultConfigList() {
        return networkConfigList;
    }

    @Override
    public BaseSynthetic[] getSyntheticMaster() {
        return networkSyntheticMaster;
    }

    @Override
    public String getMonitorState() {
        return getMonitorStateByStatus();
    }

    public boolean isMetricCollected(BaseQuery query) {
        return (query.getComputeType() == null || !query.getComputeType().equals(ComputeType.synthetic));
    }

    public boolean isMetricPoolable(BaseQuery query) {
        if (query.getSourceType().equals(SourceType.network)) {
            return true;
        }
        if (query.getServiceType() == null) {
            return false;
        }
        return query.getServiceType().equals(SourceType.network.name());
    }

    public boolean isMetricMonitored(BaseQuery query) {
        return query.getComputeType().equals(ComputeType.query);
        //return query.getQuery().startsWith("summary");
    }
}

