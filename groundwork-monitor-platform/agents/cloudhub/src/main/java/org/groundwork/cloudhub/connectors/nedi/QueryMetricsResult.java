package org.groundwork.cloudhub.connectors.nedi;

import org.groundwork.cloudhub.gwos.GwosServiceStatus;

public class QueryMetricsResult {
    private String name;
    private String value;
    private String extra;
    private GwosServiceStatus state = GwosServiceStatus.OK;

    public QueryMetricsResult() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public GwosServiceStatus getState() {
        return state;
    }

    public void setState(GwosServiceStatus state) {
        this.state = state;
    }
}
