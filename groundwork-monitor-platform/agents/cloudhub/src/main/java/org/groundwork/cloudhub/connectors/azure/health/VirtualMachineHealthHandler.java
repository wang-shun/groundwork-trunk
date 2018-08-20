package org.groundwork.cloudhub.connectors.azure.health;

import com.google.common.collect.ImmutableMap;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.PowerState;
import com.microsoft.azure.management.compute.VirtualMachine;
import org.groundwork.cloudhub.gwos.GwosStatus;

import java.util.HashMap;
import java.util.Map;

public class VirtualMachineHealthHandler implements AzureHealthHandler {

    private static final Map<PowerState,HealthInfo> AZURE_POWER_STATE_TO_GW_STATE = ImmutableMap.<PowerState,HealthInfo> builder()
            .put(PowerState.RUNNING, new HealthInfo(GwosStatus.UP.status))
            .put(PowerState.DEALLOCATED, new HealthInfo(GwosStatus.SCHEDULED_DOWN.status))
            .put(PowerState.DEALLOCATING, new HealthInfo(GwosStatus.SCHEDULED_DOWN.status))
            .put(PowerState.STARTING, new HealthInfo(GwosStatus.PENDING.status))
            .put(PowerState.STOPPED, new HealthInfo(GwosStatus.SCHEDULED_DOWN.status))
            .put(PowerState.STOPPING, new HealthInfo(GwosStatus.SCHEDULED_DOWN.status))
            .put(PowerState.UNKNOWN, new HealthInfo(GwosStatus.UNREACHABLE.status))
            .build();

    @Override
    public HealthInfo healthCheck(Object resource) {
        VirtualMachine vm = (VirtualMachine)resource;
        HealthInfo info  = AZURE_POWER_STATE_TO_GW_STATE.get(vm.powerState());
        if (info == null) {
            return new HealthInfo(GwosStatus.UNREACHABLE.status);
        }
        return info;
    }

    @Override
    public Map<String, Object> buildResources(Azure azure) {
        Map<String,Object> resourceMap = new HashMap<>();
        for (VirtualMachine vm : azure.virtualMachines().list()) {
            resourceMap.put(vm.id().toLowerCase(), vm);
        }
        return resourceMap;
    }

}
