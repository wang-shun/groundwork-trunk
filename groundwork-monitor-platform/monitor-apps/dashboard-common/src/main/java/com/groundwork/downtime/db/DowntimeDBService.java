package com.groundwork.downtime.db;

import com.groundwork.downtime.DowntimeContext;
import com.groundwork.downtime.DowntimeException;
import com.groundwork.downtime.DowntimeMaintenanceWindow;
import com.groundwork.downtime.DowntimeService;
import com.groundwork.downtime.DtoDowntime;
import com.groundwork.downtime.DtoRepeatingDowntime;
import com.groundwork.downtime.http.TransitionWindowCalculator;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * This service retrieves downtimes by querying directly to the Downtime tables in the slareport database
 */
public class DowntimeDBService implements DowntimeService {

    private static final String DB_PROPERTIES = "/usr/local/groundwork/config/db.properties";
    private static final String FIELDS = "iddowntimeschedule, fixed, host, service, hostgroup, servicegroup, author, description, start, \"end\", duration, apptype";
    private static final String POSTGRES_RANGE_QUERY = "select host,service, start, \"end\" from downtimeschedule where start between now() - interval '%s hours' and now() + interval '%s hours'  or \"end\" between now() - interval '%s hours' and now() + interval '%s hours'";

    private DowntimeContext downtimeContext = null;
    private HikariConfig config;
    private HikariDataSource ds;

    @Override
    public DowntimeContext login(String groundworkServer, String _username, String _password) throws DowntimeException {
        try {
            DowntimeContext context = new DowntimeContext();
            Properties properties = new Properties();
            properties.load(new FileInputStream(DB_PROPERTIES));
            String url = ((String) properties.get("collage.url")).replace("gwcollagedb", "slareport");
            String username = (String)properties.get("slareport.username");
            String password = (String)properties.get("slareport.password");

            config = new HikariConfig();
            config.setJdbcUrl(url);
            config.setUsername(username);
            config.setPassword(password);
            config.setDriverClassName((String)properties.get("connection.driver_class"));
            // TODO: remove this property when using a JDBC4 driver
            config.setConnectionTestQuery(properties.getProperty("slareport.connectionTestQuery", "select version();"));
            config.addDataSourceProperty("connectionTimeout", properties.getProperty("slareport.connectionTimeout", "30"));
            config.addDataSourceProperty("idleTimeout", properties.getProperty("slareport.idleTimeout","600000"));
            config.addDataSourceProperty("maxLifetime", properties.getProperty("slareport.maxLifetime","1800000"));
            config.addDataSourceProperty("maximumPoolSize", properties.getProperty("slareport.maximumPoolSize","10"));
            ds = new HikariDataSource(config);

            context.setLoggedOn(true);
            downtimeContext = context;
            return context;
        }
        catch (Exception e) {
            throw new DowntimeException(e);
        }
    }



    @Override
    public void logout(DowntimeContext context) throws DowntimeException {
        context.setLoggedOn(false);
    }

    @Override
    public List<DtoDowntime> list(DowntimeContext context) throws DowntimeException {
        if (downtimeContext == null) {
            login("", "", "");
        }
        return executeQuery("select " + FIELDS + " from downtimeschedule");
    }

    @Override
    public boolean ping(DowntimeContext context) {
        return true;
    }

    @Override
    public DowntimeContext relogin(DowntimeContext context) throws DowntimeException {
        return null;
    }

    @Override
    public Map<String, List<DowntimeMaintenanceWindow>> range(DowntimeContext context, Date startRange, Date endRange) throws DowntimeException {
        if (downtimeContext == null) {
            login("", "", "");
        }
        Map<Long, DtoRepeatingDowntime> repeating = queryRepeatingDowntime();
        List<DtoDowntime> downtimes = executeQuery("select " + FIELDS + " from downtimeschedule");
        // filter programmatically, need to check for NULL end date
        Map<String, List<DowntimeMaintenanceWindow>> transitions = new HashMap<>();
        for (DtoDowntime downtime : downtimes) {
            if (downtime.getFixed()) {
                DtoRepeatingDowntime repeat = repeating.get(downtime.getId());
                if (downtime.getHost() == null) {
                    continue; // must have a host
                }
                if (downtime.getService() == null) {
                    // TODO: generate services???
                    continue;
                }
                downtime.getDuration();
            }
            TransitionWindowCalculator.calculateTransitionWindow(transitions, downtime, startRange, endRange); // new window is added to transitions
        }
        Map<String, List<DowntimeMaintenanceWindow>> transitionsWithGaps = new HashMap<>();
        for (Map.Entry<String,List<DowntimeMaintenanceWindow>> entry : transitions.entrySet()) {
            List<DowntimeMaintenanceWindow> withGaps = TransitionWindowCalculator.addGapsToWindowList(entry.getValue(), startRange, endRange);
            transitionsWithGaps.put(entry.getKey(), withGaps);
        }
        return transitionsWithGaps;
    }

    public List<DowntimeMaintenanceWindow> lookup(String hostName, String serviceName, Map<String,List<DowntimeMaintenanceWindow>> maintenanceWindows) {
        List<DowntimeMaintenanceWindow> windows = maintenanceWindows.get(TransitionWindowCalculator.makeKey(hostName, serviceName));
        if (windows == null) {
            DowntimeMaintenanceWindow window = new DowntimeMaintenanceWindow(
                    DowntimeMaintenanceWindow.MaintenanceStatus.None, 0.00f, "None Scheduled");
            windows = new LinkedList<>();
            windows.add(window);
        }
        return windows;
    }

    protected List<DtoDowntime> executeQuery(String query) throws DowntimeException {
        try (
                Connection connection = ds.getConnection();
                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(query);
        ) {
            List<DtoDowntime> downtimes = new ArrayList<>();
            while (rs.next()) {
                downtimes.add(convert(rs));
            }
            return downtimes;
        }
        catch (Exception e) {
            throw new DowntimeException(e);
        }
    }

    protected DtoDowntime convert(ResultSet rs) throws SQLException {
        DtoDowntime dt = new DtoDowntime();
        dt.setId(rs.getLong(1));
        dt.setFixed(rs.getBoolean(2));
        dt.setHost(rs.getString(3));
        dt.setService(rs.getString(4));
        dt.setHostGroup(rs.getString(5));
        dt.setServiceGroup(rs.getString(6));
        dt.setAuthor(rs.getString(7));
        dt.setDescription(rs.getString(8));
        dt.setStart(rs.getTimestamp(9));
        dt.setEnd(rs.getTimestamp(10));
        dt.setDuration(rs.getInt(11));
        dt.setAppType(rs.getString(12));
        return dt;
    }

    protected DtoRepeatingDowntime convertRecurring(ResultSet rs) throws SQLException {
        DtoRepeatingDowntime dt = new DtoRepeatingDowntime();
        dt.setId(rs.getLong(1));
        dt.setYear(rs.getString(2));
        dt.setMonth(rs.getString(3));
        dt.setDay(rs.getString(4));
        dt.setWeek(rs.getString(5));
        dt.setWeekday0(rs.getBoolean(6));
        dt.setWeekday1(rs.getBoolean(7));
        dt.setWeekday2(rs.getBoolean(8));
        dt.setWeekday3(rs.getBoolean(9));
        dt.setWeekday4(rs.getBoolean(10));
        dt.setWeekday5(rs.getBoolean(11));
        dt.setWeekday6(rs.getBoolean(12));
        dt.setCount(rs.getInt(13));
        dt.setEndDate(rs.getTimestamp(14));
        dt.setDowntimeId(rs.getLong(15));
        return dt;
    }

    protected Map<Long, DtoRepeatingDowntime> queryRepeatingDowntime() throws DowntimeException {
        try (
                Connection connection = ds.getConnection();
                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery("select iddowntimeschedulerepeat, year, month, day, week, weekday_0, weekday_1, weekday_2, weekday_3, weekday_4, weekday_5, weekday_6, \"count\", enddate, fk_iddowntimeschedule from downtimeschedulerepeat");
        ) {
            Map<Long, DtoRepeatingDowntime> downtimes = new HashMap<>();
            while (rs.next()) {
                DtoRepeatingDowntime downtime = convertRecurring(rs);
                downtimes.put(downtime.getId(), downtime);
            }
            return downtimes;
        }
        catch (Exception e) {
            throw new DowntimeException(e);
        }

    }
}
