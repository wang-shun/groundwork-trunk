package org.groundwork.cloudhub.connectors.azure.health;

import com.google.common.collect.ImmutableMap;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.storage.AccountStatus;
import com.microsoft.azure.management.storage.StorageAccount;
import org.groundwork.cloudhub.gwos.GwosStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by justinchen on 2/26/18.
 */
public class StorageAccountHealthHandler implements AzureHealthHandler {

    private static final Map<AccountStatus, HealthInfo> AZURE_STORAGEACT_STATE_TO_GW_STATE = ImmutableMap.<AccountStatus, HealthInfo> builder()
            .put(AccountStatus.AVAILABLE, new HealthInfo(GwosStatus.UP.status))
            .put(AccountStatus.UNAVAILABLE, new HealthInfo(GwosStatus.SCHEDULED_DOWN.status))
            .build();

    @Override
    public HealthInfo healthCheck(Object resource) {
        StorageAccount storageAccount = (StorageAccount)resource;

        HealthInfo info = null;
        if (storageAccount.accountStatuses().primary() != null) {
            info = AZURE_STORAGEACT_STATE_TO_GW_STATE.get(storageAccount.accountStatuses().primary());
        } else {
            info = AZURE_STORAGEACT_STATE_TO_GW_STATE.get(storageAccount.accountStatuses().secondary());
        }
        if (info == null) {
            return new HealthInfo(GwosStatus.UNREACHABLE.status);
        }
        return info;
    }

    @Override
    public Map<String, Object> buildResources(Azure azure) {
        Map<String,Object> resourceMap = new HashMap<>();
        for (StorageAccount storageAccount : azure.storageAccounts().list()) {
            resourceMap.put(storageAccount.id().toLowerCase(), storageAccount);
        }
        return resourceMap;
    }

}
