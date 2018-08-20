package org.groundwork.cloudhub.connectors.azure.health;

import com.google.common.collect.ImmutableMap;
import com.microsoft.azure.management.Azure;
import org.groundwork.cloudhub.connectors.azure.AzureConfigurationProvider;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class AzureHealthProcessor {

    private Map<String,AzureHealthHandler> healthHandlerMap = new HashMap<>();
    private AzureHealthHandler defaultHandler = new AzureDefaultHealthHandler();

    private static final Map<String,AzureHealthHandler> HEALTH_HANDLER_MAP = ImmutableMap.<String,AzureHealthHandler> builder()
            .put(AzureConfigurationProvider.AZURE_VIRTUAL_MACHINES, new VirtualMachineHealthHandler())
            .put(AzureConfigurationProvider.AZURE_WEBSITES, new WebAppHealthHandler())
            .put(AzureConfigurationProvider.AZURE_SQL_DATABASES, new SqlDatabaseHealthHandler())
            .put(AzureConfigurationProvider.AZURE_STORAGE_ACCOUNTS, new StorageAccountHealthHandler())
            .put(AzureConfigurationProvider.AZURE_SQL_SERVERS, new SqlServerHealthHandler())
            .put(AzureConfigurationProvider.AZURE_COSMOS_DBS, new DatabaseAccountHealthHandler())
            .build();

    public HealthInfo healthCheck(String resourceType, Object resource) {
        AzureHealthHandler healthHandler = HEALTH_HANDLER_MAP.get(resourceType);
        if (healthHandler == null) {
            healthHandler = defaultHandler;
        }
        return healthHandler.healthCheck(resource);
    }

    public Map<String,Object> buildResources(String resourceType, Azure azure) {
        AzureHealthHandler healthHandler = HEALTH_HANDLER_MAP.get(resourceType);
        if (healthHandler == null) {
            healthHandler = defaultHandler;
        }
        return healthHandler.buildResources(azure);
    }
}
