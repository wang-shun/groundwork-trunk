package org.groundwork.cloudhub.connectors.opendaylight.client;

public class VmInfo extends ServerInfo {

    public String hypervisor;

    public VmInfo(String name, String hypervisor)
    {
        super(name);
        this.hypervisor = hypervisor;
    }
}
