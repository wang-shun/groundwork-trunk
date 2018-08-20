package org.groundwork.cloudhub.connectors.azure.health;

import com.microsoft.azure.Resource;
import com.microsoft.azure.management.Azure;
import org.groundwork.cloudhub.gwos.GwosStatus;

import java.util.HashMap;
import java.util.Map;

public class AzureDefaultHealthHandler implements AzureHealthHandler {

    private static final HealthInfo DEFAULT_UP = new HealthInfo(GwosStatus.UP.status);

    @Override
    public HealthInfo healthCheck(Object resource) {
        return DEFAULT_UP;
    }

    @Override
    public Map<String, Object> buildResources(Azure azure) {
        Map<String,Object> resourceMap = new HashMap<>();
        return resourceMap;
    }

}
