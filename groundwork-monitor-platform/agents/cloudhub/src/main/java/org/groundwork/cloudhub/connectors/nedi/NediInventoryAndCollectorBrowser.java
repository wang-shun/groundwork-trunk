package org.groundwork.cloudhub.connectors.nedi;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.log4j.Logger;
import org.groundwork.agents.utils.StringUtils;
import org.groundwork.cloudhub.gwos.GWOSHost;
import org.groundwork.cloudhub.inventory.DataCenterInventory;
import org.groundwork.cloudhub.inventory.InventoryBrowser;
import org.groundwork.cloudhub.inventory.InventoryOptions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by justinchen on 3/3/18.
 */
@Deprecated
public class NediInventoryAndCollectorBrowser implements InventoryBrowser {

    private static Logger log = Logger.getLogger(NediInventoryAndCollectorBrowser.class);

    public static final String TRAFFIC_PREFIX = "Traffic_";

    static String COLUMN_DEVICE = "device";
    static String COLUMN_DEVIP = "devip";
    static String COLUMN_LASTDIS = "lastdis";
    static String COLUMN_CPU = "cpu";
    static String COLUMN_MEMCPU = "memcpu";
    static String COLUMN_TEMP = "temp";
    static String COLUMN_DESCRIPTION = "description";
    static String COLUMN_DEVOPTS = "devopts";
    static String COLUMN_ID = "id";
    static String COLUMN_STATUS = "status";
    static String COLUMN_CLASS = "class";
    static String COLUMN_TARGET = "target";
    static String COLUMN_INFO = "info";
    static String COLUMN_TYPE = "type";

    private HikariDataSource dataSource;
    private Map<String, Map<String, String>> deviceMetrics = new HashMap<>();
    private String policyHost;

    public NediInventoryAndCollectorBrowser(HikariDataSource dataSource, String policyHost) {
        this.dataSource = dataSource;
        this.policyHost = policyHost;
    }

    @Override
    public DataCenterInventory gatherInventory(InventoryOptions options) {
        DataCenterInventory inventory = new DataCenterInventory(options);

        // gather Device inventory
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = createDeviceStatement(connection);
             ResultSet rs = ps.executeQuery()) {

            while(rs.next()){
                Map<String, String> metrics = getDeviceMetric(rs);
                String deviceId = metrics.get(COLUMN_DEVICE);
                deviceMetrics.put(deviceId, metrics);
                GWOSHost devHost = new GWOSHost(deviceId, "NEDI", "Nedi");
                inventory.getAllHosts().put(deviceId, devHost);
            }

        } catch (SQLException se) {
            log.error("SQL Exception on getting devices records: " + se.getMessage());
        }

        // gather Policies inventory
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = createPolicyStatement(connection);
             ResultSet rs = ps.executeQuery()) {

            while(rs.next()){
                String infoColumn = rs.getString(COLUMN_INFO);
                StringBuffer serviceName = new StringBuffer(TRAFFIC_PREFIX);
                serviceName.append(StringUtils.isEmpty(infoColumn) ? "" : infoColumn.replace(' ', '_'));
                serviceName.append("_");
                serviceName.append(rs.getInt(COLUMN_ID));

//                my $pev = db::Selesct( 'events', '', 'id', "time>$now-300 and class='sptr' and source='$p->[0]'" );
//                UPsrv( 'localhost', '127.0.0.1', $pev ? 'CRITICAL' : 'OK', $prefix . $svde, $p->[6] );
//                ADDevent( 'localhost', 'CRITICAL', 'SERIOUS', $prefix . 'Traffic' . $p->[0], $svde ) if $pev;

                Map<String, String> metrics = getDeviceMetric(rs);
                String deviceId = metrics.get("device");
                deviceMetrics.put(deviceId, metrics);
                GWOSHost devHost = new GWOSHost(deviceId, "NEDI", "Nedi");
                inventory.getAllHosts().put(deviceId, devHost);
            }
            GWOSHost polHost = new GWOSHost(policyHost, "NEDI", "Nedi");
            inventory.getAllHosts().put(policyHost, polHost);

        } catch (SQLException se) {
            log.error("SQL Exception on getting polices records: " + se.getMessage());
        }

        return inventory;
    }

    public Map<String, String> getNediMetrics(String deviceId) {
        return deviceMetrics.get(deviceId);
    }

    private PreparedStatement createDeviceStatement(Connection connection) throws SQLException {
        String sql = "SELECT device, devip, lastdis, cpu, memcpu, temp, description, devopts FROM devices";
        PreparedStatement ps = connection.prepareStatement(sql);
        return ps;
    }

    private Map<String, String> getDeviceMetric(ResultSet rs) {
        Map<String, String> metrics = new HashMap<>();
        if (rs != null) {
            try {
                metrics.put(COLUMN_DEVICE, rs.getString(COLUMN_DEVICE));
                metrics.put(COLUMN_DEVIP, String.valueOf(rs.getLong(COLUMN_DEVIP)));
                metrics.put(COLUMN_LASTDIS, String.valueOf(rs.getLong(COLUMN_LASTDIS)));
                metrics.put(COLUMN_CPU, String.valueOf(rs.getInt(COLUMN_CPU)));
                metrics.put(COLUMN_MEMCPU, String.valueOf(rs.getLong(COLUMN_MEMCPU)));
                metrics.put(COLUMN_TEMP, String.valueOf(rs.getInt(COLUMN_TEMP)));
                metrics.put(COLUMN_DESCRIPTION, rs.getString(COLUMN_DESCRIPTION));
                metrics.put(COLUMN_DEVOPTS, rs.getString(COLUMN_DEVOPTS));

            } catch (SQLException e) {
                log.error("Fail to get value : " + e.getMessage());
            }
         }
        return metrics;
    }

    private PreparedStatement createPolicyStatement(Connection connection) throws SQLException {
        String sql = "SELECT id, status, class, target, device, type, alert, info, respolicy, username, time FROM policies";
        PreparedStatement ps = connection.prepareStatement(sql);
        return ps;
    }

    private PreparedStatement createEventsStatement(Connection connection) throws SQLException {
        String sql = "SELECT id, level, time, source, info, class, device  FROM events";
        PreparedStatement ps = connection.prepareStatement(sql);
        return ps;
    }

}
