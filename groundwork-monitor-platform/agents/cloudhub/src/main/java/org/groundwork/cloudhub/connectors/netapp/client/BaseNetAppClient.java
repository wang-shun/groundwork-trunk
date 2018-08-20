package org.groundwork.cloudhub.connectors.netapp.client;

import netapp.manage.NaElement;
import netapp.manage.NaServer;
import org.groundwork.cloudhub.connectors.base.BaseConnectorClient;
import org.groundwork.cloudhub.gwos.GwosStatus;

public abstract class BaseNetAppClient extends BaseConnectorClient {

    protected NaServer server = null;

    public BaseNetAppClient(NaServer server) {
        this.server = server;
    }

    public String determineControllerStatus(NaElement controller) {
        String healthy = controller.getChildContent("is-node-healthy");
        if (healthy != null && !healthy.equals("true")) {
            return GwosStatus.UNSCHEDULED_DOWN.status;
        }
        String tempOver = controller.getChildContent("env-over-temperature");
        if (tempOver != null && !tempOver.equals("false")) {
            return GwosStatus.WARNING.status;
        }
        String batteryStatus = controller.getChildContent("nvram-battery-status");
        if (batteryStatus != null && !batteryStatus.equals("battery_ok")) {
            return GwosStatus.WARNING.status;
        }
        return GwosStatus.UP.status;
    }

    private final static String[][] VOLUME_STATES = {
            {"is-invalid", GwosStatus.UNSCHEDULED_DOWN.status},
            {"in-nvfailed-state", GwosStatus.UNSCHEDULED_DOWN.status},

            {"is-inconsistent", GwosStatus.WARNING.status},
            {"is-unrecoverable", GwosStatus.UNSCHEDULED_DOWN.status}
    };

    public String determineVolumeStatus(NaElement volume) {
        String online = volume.getChildContent("online");
        if (online != null && !online.equals("true")) {
            return GwosStatus.SCHEDULED_DOWN.status;
        }
        for (String[] state : VOLUME_STATES) {
            String check = volume.getChildContent(state[0]);
            if (check != null && !check.equalsIgnoreCase("false")) {
                return state[1];
            }
        }
        return GwosStatus.UP.status;
    }

    public String determineAggregateStatus(NaElement aggregate) {
        String online = aggregate.getChildContent("state");
        if (online != null && !online.equals("online")) {
            return GwosStatus.SCHEDULED_DOWN.status;
        }
        String raidStatus = aggregate.getChildContent("raid-status");
        if (raidStatus != null && !raidStatus.contains("normal")) {
            return GwosStatus.WARNING.status;
        }
        return GwosStatus.UP.status;
    }

    public String determineServerStatus(NaElement server) {
        String state = server.getChildContent("state");
        if (state != null && !state.equals("running")) {
            return GwosStatus.SCHEDULED_DOWN.status;
        }
        return GwosStatus.UP.status;
    }

}
