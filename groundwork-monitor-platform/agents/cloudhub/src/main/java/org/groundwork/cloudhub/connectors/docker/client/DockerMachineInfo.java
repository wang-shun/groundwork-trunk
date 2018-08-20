package org.groundwork.cloudhub.connectors.docker.client;

/**
 * Created by dtaylor on 11/4/14.
 */
public class DockerMachineInfo {

    public int numCores;
    public long memoryCapacity;

    public DockerMachineInfo(int numCores, long memoryCapacity) {
        this.numCores = numCores;
        this.memoryCapacity = memoryCapacity;
    }

}
