package org.groundwork.cloudhub.connectors.docker.client;

import org.groundwork.cloudhub.connectors.opendaylight.client.ServerInfo;

public class ContainerInfo extends ServerInfo {

    public String engine;
    public String id;

    public ContainerInfo(String name, String id, String engine)
    {
        super(name);
        this.id = id;
        this.engine = engine;
    }
}
