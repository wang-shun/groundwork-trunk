/*
 * Copyright 2012 GroundWork , Inc. ("GroundWork")
 * All rights reserved.
*/
package org.groundwork.cloudhub.connectors.base;

/**
 * Created by dtaylor on 5/12/15.
 */
public class MetricFaultInfo {

    String query;
    String host;
    boolean isContainer;

    public MetricFaultInfo(String query, String host) {
        this.query = query;
        this.host = host;
        this.isContainer = false;
    }

    public MetricFaultInfo(String query, String host, boolean isContainer) {
        this.query = query;
        this.host = host;
        this.isContainer = isContainer;
    }

    public String getQuery() {
        return query;
    }

    public String getHost() {
        return host;
    }

    public boolean isContainer() {
        return isContainer;
    }
}
