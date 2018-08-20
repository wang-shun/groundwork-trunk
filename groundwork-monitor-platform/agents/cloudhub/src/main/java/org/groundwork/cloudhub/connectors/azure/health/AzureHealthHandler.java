package org.groundwork.cloudhub.connectors.azure.health;

import com.microsoft.azure.management.Azure;

import java.util.Map;

public interface AzureHealthHandler {

    HealthInfo healthCheck(Object resource);
    Map<String,Object> buildResources(Azure azure);

}
