package org.groundwork.cloudhub.connectors.azure;

import org.groundwork.cloudhub.metrics.BaseVM;
import org.groundwork.cloudhub.metrics.MetricProvider;

public class AzureVM extends BaseVM  implements MetricProvider {

    public AzureVM(String vmName) {
        super(vmName);
    }
}
