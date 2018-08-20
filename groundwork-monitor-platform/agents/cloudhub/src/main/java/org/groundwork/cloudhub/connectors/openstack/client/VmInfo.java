package org.groundwork.cloudhub.connectors.openstack.client;

import org.groundwork.cloudhub.connectors.openstack.OpenStackStatus;

public class VmInfo {

    public String id;
    public String name;
    public String hypervisor;
    public OpenStackStatus status = OpenStackStatus.UNKNOWN;

    public VmInfo(String id, String name, String hypervisor, String status) {
        this.id = id;
        this.name = name;
        this.hypervisor = hypervisor;
        this.status = OpenStackStatus.mapToStatus(status);
    }

    public VmInfo(String id, String name, String hypervisor, OpenStackStatus status) {
        this.id = id;
        this.name = name;
        this.hypervisor = hypervisor;
        this.status = status;
    }

}
