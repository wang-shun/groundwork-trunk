package org.groundwork.cloudhub.connectors.nedi;

import org.groundwork.cloudhub.metrics.BaseVM;
import org.groundwork.cloudhub.metrics.MetricProvider;

public class NediVM extends BaseVM  implements MetricProvider {

    public NediVM(String vmName) {
        super(vmName);
    }
}
