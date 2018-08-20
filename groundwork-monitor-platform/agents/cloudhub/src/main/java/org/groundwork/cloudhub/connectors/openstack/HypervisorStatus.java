package org.groundwork.cloudhub.connectors.openstack;

public enum HypervisorStatus {

    enabled, //// OpenStack Hypervisor Statuses are in lowercase
    disabled,
    unknown;

    public static HypervisorStatus mapToStatus(String status) {
        try {
            return HypervisorStatus.valueOf(status.toLowerCase());
        }
        catch (Exception e) {
            return unknown;
        }
    }


}
