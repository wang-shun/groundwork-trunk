package org.groundwork.cloudhub.connectors.opendaylight.client;

import org.groundwork.cloudhub.gwos.GwosStatus;

public class ServerInfo {

    public String name;
    public String status;

    public ServerInfo(String name) {
        this.name = name;
        this.status = GwosStatus.UP.status;
    }
}
