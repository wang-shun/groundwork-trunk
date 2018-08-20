package org.groundwork.cloudhub.connectors.azure.health;

import com.google.common.collect.ImmutableMap;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.sql.SqlDatabase;
import com.microsoft.azure.management.sql.SqlServer;
import org.groundwork.cloudhub.gwos.GwosStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by justinchen on 2/26/18.
 */
public class SqlServerHealthHandler implements AzureHealthHandler {

    // No SqlServer status from Azure.  The followings are defined in in gwos
    // If all SqlDatabase under a SqlServer are 'Online', it is 'Available'
    // Else if any SqlDatabase under a SqlServer is NOT 'Online', make it a warning
    // else none of the SqlDatabase under the SqlServer is 'Online', make it scheduled down
    private static final String AVAILABLE = "Available";
    private static final String WARNING = "Warning";
    private static final String UNAVAILABLE = "Unavailable";

    private static final Map<String, HealthInfo> AZURE_SQL_SERVER_TO_GW_STATE = ImmutableMap.<String, HealthInfo> builder()
            .put(AVAILABLE, new HealthInfo(GwosStatus.UP.status))
            .put(WARNING, new HealthInfo(GwosStatus.WARNING.status))
            .put(UNAVAILABLE, new HealthInfo(GwosStatus.SCHEDULED_DOWN.status))
            .build();

    @Override
    public HealthInfo healthCheck(Object resource) {
        SqlServer sqlServer = (SqlServer) resource;

        SqlDbStatus sqlDbStatus = aggregateSqlDbStatus(sqlServer);
        String serverStatus = UNAVAILABLE;
        if (sqlDbStatus.onlineDbCount >= sqlDbStatus.totalDbCount) {
            serverStatus = AVAILABLE;
        } else if (sqlDbStatus.onlineDbCount < sqlDbStatus.totalDbCount && sqlDbStatus.onlineDbCount > 0) {
            serverStatus = WARNING;
        } // fallback to UNAVAILABLE

        HealthInfo info  = AZURE_SQL_SERVER_TO_GW_STATE.get(serverStatus);
        if (WARNING.equals(serverStatus)) {
            String extraMsg = "Warning! %d Sql databases are online out of %d in total";
            info.runStateExtra = String.format(extraMsg, sqlDbStatus.onlineDbCount, sqlDbStatus.totalDbCount);
        }
        if (info == null) {
            return new HealthInfo(GwosStatus.UNREACHABLE.status);
        }
        return info;
    }

    @Override
    public Map<String, Object> buildResources(Azure azure) {
        Map<String,Object> resourceMap = new HashMap<>();
        for (SqlServer sqlServer : azure.sqlServers().list()) {
            resourceMap.put(sqlServer.id().toLowerCase(), sqlServer);
        }
        return resourceMap;
    }

    private SqlDbStatus aggregateSqlDbStatus(SqlServer sqlServer) {
        SqlDbStatus allDbStatus = new SqlDbStatus();
        if (sqlServer != null && sqlServer.databases() != null) {
            List<SqlDatabase> sqlDbs = sqlServer.databases().list();
            allDbStatus.totalDbCount = sqlDbs.size();
            allDbStatus.onlineDbCount = 0;
            for (SqlDatabase sqlDb : sqlDbs) {
                if (SqlDatabaseHealthHandler.ONLINE.equals(sqlDb.status())) {
                    allDbStatus.onlineDbCount++;
                }
            }
        }
        return allDbStatus;
    }

    private static class SqlDbStatus {
        int totalDbCount = 0;
        int onlineDbCount = 0;
    }
}
