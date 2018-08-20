package org.groundwork.cloudhub.connectors.docker.client;

import org.groundwork.cloudhub.gwos.GwosStatus;

import java.util.ArrayList;
import java.util.List;

public class DockerEngineInfo {

    public String name;
    public String status;
    public List<String> containers = new ArrayList<String>();

    public DockerEngineInfo(String name) {
        this.name = name;
        this.status = GwosStatus.UP.status;
    }
}
