package org.groundwork.cloudhub.connectors.openstack.client;

import org.groundwork.cloudhub.connectors.openstack.HypervisorState;
import org.groundwork.cloudhub.connectors.openstack.HypervisorStatus;

public class HypervisorInfo {

    public String id;
    public String name;
    public HypervisorState state = HypervisorState.unknown;
    public HypervisorStatus status = HypervisorStatus.unknown;

    public HypervisorInfo(String id, String name, String state, String status) {
        this.id = id;
        this.name = name;
        this.state = HypervisorState.mapToState(state);
        this.status = HypervisorStatus.mapToStatus(status);
    }

    public HypervisorInfo(String id, String name, HypervisorState state, HypervisorStatus status) {
        this.id = id;
        this.name = name;
        this.state = state;
        this.status = status;
    }

}
