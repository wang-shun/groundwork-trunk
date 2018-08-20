package org.groundwork.cloudhub.connectors.openstack;

import org.groundwork.cloudhub.gwos.GwosStatus;

public enum HypervisorState {

    up, //// OpenStack Hypervisor States are in lowercase
    down,
    unknown;

    public static HypervisorState mapToState(String state) {
        try {
            return HypervisorState.valueOf(state.toLowerCase());
        }
        catch (Exception e) {
            return unknown;
        }
    }

    public static String convertToGroundworkStatus(HypervisorState state) {
        String gwosStatus = GwosStatus.DOWN.status;
        switch (state) {
            case up:
                gwosStatus = GwosStatus.UP.status;
                break;
            case down:
                gwosStatus = GwosStatus.UNSCHEDULED_DOWN.status;
                break;
            case unknown:
            default:
                gwosStatus = GwosStatus.UNREACHABLE.status;
                break;
        }
        return gwosStatus;
    }




}
