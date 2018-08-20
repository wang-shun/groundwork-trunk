package org.groundwork.cloudhub.connectors.azure.health;

import com.google.common.collect.ImmutableMap;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.cosmosdb.CosmosDBAccount;
import org.groundwork.cloudhub.gwos.GwosStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by justinchen on 2/26/18.
 */
public class DatabaseAccountHealthHandler implements AzureHealthHandler {

    // SqlDatabase Status is just a string, not enum, from Azure
    static final String ONLINE = "Online";

    private static final Map<String, HealthInfo> AZURE_DATABASE_ACCOUNT_TO_GW_STATE = ImmutableMap.<String, HealthInfo> builder()
            .put(ONLINE, new HealthInfo(GwosStatus.UP.status))
            .build();

    @Override
    public HealthInfo healthCheck(Object resource) {
        // Make it always Online for now
        return AZURE_DATABASE_ACCOUNT_TO_GW_STATE.get(ONLINE);
    }

    @Override
    public Map<String, Object> buildResources(Azure azure) {
        Map<String, Object> resourceMap = new HashMap<>();
        for (CosmosDBAccount cosmosDBAccount : azure.cosmosDBAccounts().list()) {
            resourceMap.put(cosmosDBAccount.id().toLowerCase(), cosmosDBAccount);
        }
        return resourceMap;
    }
}
