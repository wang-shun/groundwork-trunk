package org.groundwork.cloudhub.connectors.cloudera;

import org.groundwork.cloudhub.metrics.BaseVM;
import org.groundwork.cloudhub.metrics.MetricProvider;

public class ClouderaVM extends BaseVM  implements MetricProvider {

    public ClouderaVM(String vmName) {
        super(vmName);
    }
}
