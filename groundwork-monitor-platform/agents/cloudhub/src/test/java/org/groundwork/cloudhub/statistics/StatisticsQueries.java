package org.groundwork.cloudhub.statistics;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class StatisticsQueries {

    private static final String DB_PROPERTIES = "/usr/local/groundwork/config/db.properties";

    private Connection connection;
    private MonitoringStatistics statistics;

    public StatisticsQueries(MonitoringStatistics statistics) {
        this.statistics = statistics;
    }

    public void connect() throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        Properties properties = loadDatabaseProperties();
        Driver driver =(Driver)Class.forName((String)properties.get("connection.driver_class")).newInstance();
        connection = DriverManager.getConnection((String)properties.get("collage.url"),
                (String)properties.get("collage.username"), (String)properties.get("collage.password"));
    }

    public void disconnect() throws SQLException {
        connection.close();
    }

    /**
     * executes a count query on param table with where clause
     *
     * @param table a valid Collage database table name
     * @param where this OPTIONAL parameter is the where section of the select statement
     * @return the count
     * @throws SQLException
     */
    public int executeCountQuery(String table, String where) throws SQLException {
        int count = 0;
        String queryString = "SELECT COUNT(*) FROM " + table;
        if (where != null) {
            queryString = queryString + " where " + where;
        }
        try (
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(queryString);
        ) {
            while (rs.next()) {
                count = rs.getInt("count");
            }
            return count;
        }
        catch (SQLException e) {
            throw e;
        }
    }

    private Properties loadDatabaseProperties() {
        Properties result = new Properties();
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(DB_PROPERTIES));
            result.put("connection.driver_class", properties.get("connection.driver_class"));
            result.put("collage.url", properties.get("collage.url"));
            result.put("collage.username", properties.get("collage.username"));
            result.put("collage.password", properties.get("collage.password"));
        }
        catch (Exception e) {
            e.printStackTrace();
            result.put("connection.driver_class", "org.postgresql.Driver");
            result.put("collage.url", "jdbc:postgresql://localhost:5432/gwcollagedb");
            result.put("collage.username", "collage");
            result.put("collage.password", "gwrk");
        }
        return result;
    }
}
