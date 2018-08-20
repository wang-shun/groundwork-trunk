package org.groundwork.downtime;

import org.junit.Test;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

public class DowntimeDatabaseTest {

    private static final String DB_PROPERTIES = "/usr/local/groundwork/config/db.properties";
    //private static final String FIELDS = "iddowntimeschedule, fixed, host, service, hostgroup, servicegroup, author, description, start, \"end\", duration, apptype";
    private static final String FIELDS = "iddowntimeschedule, fixed, host, service, hostgroup, servicegroup, author, description, start, duration, apptype";


    @Test
    public void jdbcTest() throws Exception {


        Connection connection = null;
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(DB_PROPERTIES));
            Class.forName((String)properties.get("connection.driver_class"));
            String url = ((String)properties.get("collage.url")).replace("gwcollagedb", "slareport");
            connection = DriverManager.getConnection(url, (String)properties.get("collage.username"), (String)properties.get("collage.password"));
            //connection = DriverManager.getConnection(url, "postgres", "postgres");
            if (connection != null) {
                System.out.println("Connected to the database");
            }
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("select " + FIELDS + " from downtimeschedule");
            while (rs.next()) {
                String host = rs.getString(3);
                String service = rs.getString(4);
                System.out.println(host + ":" + service);
            }

        } catch (ClassNotFoundException ex) {
            System.out.println("Could not find database driver class");
            ex.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (connection != null) {
                connection.close();
            }
        }
    }
}
