package org.groundwork.cloudhub.connectors.azure.health;

import com.google.common.collect.ImmutableMap;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.sql.SqlDatabase;
import com.microsoft.azure.management.sql.SqlServer;
import org.groundwork.cloudhub.gwos.GwosStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by justinchen on 2/26/18.
 */
public class SqlDatabaseHealthHandler implements AzureHealthHandler {

    // SqlDatabase Status is just a string, not enum, from Azure
    static final String ONLINE = "Online";

    private static final Map<String, HealthInfo> AZURE_SQLDB_STATUS_TO_GW_STATE = ImmutableMap.<String, HealthInfo> builder()
            .put(ONLINE, new HealthInfo(GwosStatus.UP.status))
            .build();

    @Override
    public HealthInfo healthCheck(Object resource) {
        SqlDatabase sqlDatabase = (SqlDatabase)resource;
        HealthInfo info = AZURE_SQLDB_STATUS_TO_GW_STATE.get(sqlDatabase.status());
        if (info == null) {
            return new HealthInfo(GwosStatus.UNREACHABLE.status);
        }
        return info;
    }

    @Override
    public Map<String, Object> buildResources(Azure azure) {
        Map<String,Object> resourceMap = new HashMap<>();
        // TODO: review, to aggregate all SqlDatabase from all SqlServers?
        for (SqlServer sqlServer : azure.sqlServers().list()) {
            for (SqlDatabase sqlDatabase : sqlServer.databases().list()) {
                resourceMap.put(sqlDatabase.id().toLowerCase(), sqlDatabase);
            }
        }
        return resourceMap;
    }
}
