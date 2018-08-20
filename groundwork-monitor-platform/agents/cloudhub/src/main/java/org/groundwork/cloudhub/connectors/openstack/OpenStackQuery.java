package org.groundwork.cloudhub.connectors.openstack;

import org.groundwork.cloudhub.metrics.BaseQuery;
import org.groundwork.cloudhub.metrics.ComputeType;
import org.groundwork.cloudhub.metrics.SourceType;

public class OpenStackQuery extends BaseQuery {

    private boolean isCeilometer = false;
    private boolean isRegex = false;

    public OpenStackQuery(String query, long warning, long critical,
                          boolean isGraphed, boolean isMonitored, boolean isCeilometer, boolean isRegex, String customName, String expression, String format) {
        super(query, warning, critical, isGraphed, isMonitored, false, SourceType.diagnostics, ComputeType.query, customName, expression, format, null);
        this.isCeilometer = isCeilometer;
        this.isRegex = isRegex;
    }

    public boolean isCeilometer() {
        return isCeilometer;
    }

    public void setCeilometer(boolean isCeilometer) {
        this.isCeilometer = isCeilometer;
    }

    public boolean isRegex() {
        return isRegex;
    }

    public void setRegex(boolean isRegex) {
        this.isRegex = isRegex;
    }
}

