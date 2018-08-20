package org.groundwork.cloudhub.connectors.nedi;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.groundwork.collage.CollageSeverity;
import org.apache.log4j.Logger;
import org.groundwork.agents.utils.StringUtils;
import org.groundwork.cloudhub.exceptions.ConnectorException;
import org.groundwork.cloudhub.gwos.GwosServiceStatus;
import org.groundwork.cloudhub.gwos.GwosStatus;
import org.groundwork.cloudhub.inventory.InventoryContainerNode;
import org.groundwork.rs.dto.DtoEvent;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service(NediDatabaseAccess.NAME)
public class NediDatabaseAccess {

    public static final String NAME = "NediDatabaseAccess";

    private static Logger log = Logger.getLogger(NediDatabaseAccess.class);
    public static final String TRAFFIC_PREFIX = "Traffic_";

    // Devices table columns
    static final String COLUMN_DEVICE = "device";
    static final String COLUMN_DEVIP = "devip";
    static final String COLUMN_LASTDIS = "lastdis";
    static final String COLUMN_LASTOK = "lastok";
    static final String COLUMN_CPU = "cpu";
    static final String COLUMN_MEMCPU = "memcpu";
    static final String COLUMN_TEMP = "temp";
    static final String COLUMN_DESCRIPTION = "description";
    static final String COLUMN_DEVOPTS = "devopts";
    // Monitoring table columns
    static final String COLUMN_LATENCY = "latency";
    static final String COLUMN_LATMAX = "latmax";
    static final String COLUMN_LATAVG = "latavg";
    // Event table columns
    static final String COLUMN_ID = "id";
    static final String COLUMN_STATUS = "status";
    static final String COLUMN_CLASS = "class";
    static final String COLUMN_TARGET = "target";
    static final String COLUMN_INFO = "info";
    static final String COLUMN_TYPE = "type";

    private final Cache<String, DtoEvent> eventCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build();

    /**
     * Query for the device inventory
     *
     * @param dataSource
     * @param inventory
     * @return
     * @throws ConnectorException
     */
    public Map<String, InventoryContainerNode> queryDeviceInventory(DataSource dataSource, Map<String, InventoryContainerNode> inventory)
            throws ConnectorException {

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement("select device, devip from devices");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                InventoryContainerNode node = new InventoryContainerNode(rs.getString(COLUMN_DEVICE), rs.getString(COLUMN_DEVIP));
                inventory.put(node.getName(), node);
            }

        } catch (SQLException se) {
            log.error("SQL Exception on getting device inventory: " + se.getMessage());
            throw new ConnectorException(se);
        }
        return inventory;
    }

    /**
     * Query for the Policy inventory
     *
     * @param dataSource
     * @param policyHost
     * @param inventory
     * @return
     * @throws ConnectorException
     */
    public Map<String, InventoryContainerNode> queryPolicyInventory(DataSource dataSource, String policyHost, Map<String, InventoryContainerNode> inventory)
            throws ConnectorException {

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement("select id from policies");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                InventoryContainerNode node = new InventoryContainerNode(policyHost, policyHost);
                inventory.put(node.getName(), node);
            }

        } catch (SQLException se) {
            log.error("SQL Exception on getting policy inventory: " + se.getMessage());
            throw new ConnectorException(se);
        }
        return inventory;
    }

    /**
     * Query for device metrics and build a result map of service name to measurement
     *
     * @param dataSource
     * @return
     */
    public List<Map<String,String>> queryDeviceMetrics(DataSource dataSource) {
        List<Map<String,String>> deviceMetrics = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = createDeviceStatement(connection);
             ResultSet rs = ps.executeQuery()) {
            while(rs.next()){
                Map<String, String> metrics = getDeviceMetric(rs);
                deviceMetrics.add(metrics);
            }
            return deviceMetrics;
        } catch (SQLException se) {
            log.error("SQL Exception on getting devices records: " + se.getMessage());
            throw new ConnectorException(se);
        }

    }

    /**
     * Query  for the policy metrics and build a result of metrics
     *
     * @param dataSource
     * @return
     */
    public List<QueryMetricsResult> queryPolicyMetrics(DataSource dataSource) {
        List<QueryMetricsResult> results = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = createPolicyStatement(connection);
             ResultSet rs = ps.executeQuery()) {
            while(rs.next()){
                String infoColumn = rs.getString(COLUMN_INFO);
                StringBuffer serviceName = new StringBuffer(TRAFFIC_PREFIX);
                serviceName.append(StringUtils.isEmpty(infoColumn) ? "" : infoColumn.replace(' ', '_'));
                serviceName.append("_");
                serviceName.append(rs.getLong(COLUMN_ID));
                QueryMetricsResult result = new QueryMetricsResult();
                result.setName(serviceName.toString());
                result.setValue("");
                result.setExtra(rs.getString(COLUMN_TYPE));
                String id = new Long(rs.getLong(COLUMN_ID)).toString();
                LookupEventResult lookup = lookupEvent(dataSource, id);
                if (lookup.getEventId() != -1) {
                    result.setState(GwosServiceStatus.UNSCHEDULED_CRITICAL);
                    result.setExtra(lookup.getInfo());
                }
                else {
                    result.setValue("Health check OK");
                }
                results.add(result);
            }
            return results;
        } catch (SQLException se) {
            log.error("SQL Exception on getting policies metrics: " + se.getMessage());
            throw new ConnectorException(se);
        }
    }

    // my $pev = db::Selesct( 'events', '', 'id', "time>$now-300 and class='sptr' and source='$p->[0]'" );
    public LookupEventResult lookupEvent(DataSource dataSource, String source) {
        long eventId = -1;
        String info = "";
        Long nowMinusFiveMinutes = (System.currentTimeMillis() / 1000) - 300;
        try (Connection connection = dataSource.getConnection(); // and class = 'sptr'
             PreparedStatement ps = connection.prepareStatement("select id, info, time from events where time > ? and source = ? order by time DESC");) {
            ps.setLong(1, nowMinusFiveMinutes);
            ps.setString(2, source);
            try ( ResultSet rs = ps.executeQuery();) {
                if (rs.next()) {
                    eventId = rs.getLong(1);
                    info = rs.getString(2);
                }
            }
            return new LookupEventResult(eventId, info);
        } catch (SQLException se) {
            log.error("SQL Exception on getting policy inventory: " + se.getMessage());
            throw new ConnectorException(se);
        }
    }

    // TODO: should 5 minutes (300 seconds) be configurable
    public List<DtoEvent> findRecentEvents(DataSource dataSource) {
        List<DtoEvent> events = new ArrayList<>();
        Long nowMinusFiveMinutes = (System.currentTimeMillis() / 1000) - 300;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT id, level, time, source, info, class, device FROM events WHERE time > ? and device is not null");) {
            ps.setLong(1, nowMinusFiveMinutes);
            try ( ResultSet rs = ps.executeQuery();) {
                while (rs.next()) {
                    Long eventId = rs.getLong(1);
                    DtoEvent event = eventCache.getIfPresent(eventId);
                    if (event == null) {
                        event = new DtoEvent(rs.getString(COLUMN_DEVICE), "PROBLEM", GwosStatus.WARNING.status, CollageSeverity.LOW.name(), rs.getString(COLUMN_INFO));
                        events.add(event);
                    }
                }
            }
            return events;
        } catch (SQLException se) {
            log.error("SQL Exception on getting recent events: " + se.getMessage());
            throw new ConnectorException(se);
        }
    }

    private Map<String, String> getDeviceMetric(ResultSet rs) {
        Map<String, String> metrics = new HashMap<>();
        if (rs != null) {
            try {
                metrics.put(COLUMN_DEVICE, rs.getString(COLUMN_DEVICE));
                metrics.put(COLUMN_DEVIP, String.valueOf(rs.getLong(COLUMN_DEVIP)));
                metrics.put(COLUMN_LASTOK, String.valueOf(rs.getLong(COLUMN_LASTOK)));
                metrics.put(COLUMN_CPU, String.valueOf(rs.getInt(COLUMN_CPU)));
                metrics.put(COLUMN_MEMCPU, String.valueOf(rs.getLong(COLUMN_MEMCPU)));
                metrics.put(COLUMN_TEMP, String.valueOf(rs.getInt(COLUMN_TEMP)));
                metrics.put(COLUMN_DESCRIPTION, rs.getString(COLUMN_DESCRIPTION));
                metrics.put(COLUMN_DEVOPTS, rs.getString(COLUMN_DEVOPTS));
                metrics.put(COLUMN_LATENCY, rs.getString(COLUMN_LATENCY));
                metrics.put(COLUMN_LATMAX, rs.getString(COLUMN_LATMAX));
                metrics.put(COLUMN_LATAVG, rs.getString(COLUMN_LATAVG));
            } catch (SQLException e) {
                log.error("Fail to get value : " + e.getMessage());
            }
        }
        return metrics;
    }


    private static final String DEVICE_MONITORING_QUERY = "SELECT\n" +
            "    d.device,\n" +
            "    d.devip,\n" +
            "    d.lastdis,\n" +
            "    d.cpu,\n" +
            "    d.memcpu,\n" +
            "    d.temp,\n" +
            "    d.description,\n" +
            "    d.devopts,\n" +
            "    m.lastok,\n" +
            "    m.status,\n" +
            "    m.ok,\n" +
            "    m.latency,\n" +
            "    m.latmax,\n" +
            "    m.latavg,\n" +
            "    m.uptime,\n" +
            "    m.alert\n" +
            "FROM\n" +
            "    monitoring m,\n" +
            "    devices d\n" +
            "WHERE\n" +
            "    m.class = 'dev'\n" +
            "    AND m.device = d.device";

    private PreparedStatement createDeviceStatement(Connection connection) throws SQLException {
        return connection.prepareStatement(DEVICE_MONITORING_QUERY);
    }

    private PreparedStatement createPolicyStatement(Connection connection) throws SQLException {
        String sql = "SELECT id, status, class, target, device, type, alert, info, respolicy, usrname, time FROM policies";
        PreparedStatement ps = connection.prepareStatement(sql);
        return ps;
    }

    public int updateAllDevicesTimeStamp(DataSource dataSource) {
        Long now = System.currentTimeMillis() / 1000;
        String sql = "update monitoring set lastok = " + now;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);) {
            return ps.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    class LookupEventResult {
        private long eventId = -1;
        private String info;

        LookupEventResult(long eventId, String info) {
            this.eventId = eventId;
            this.info = info;
        }

        public long getEventId() {
            return eventId;
        }

        public void setEventId(long eventId) {
            this.eventId = eventId;
        }

        public String getInfo() {
            return info;
        }

        public void setInfo(String info) {
            this.info = info;
        }
    }

}
