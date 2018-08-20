/*
 * Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork) All rights
 * reserved. This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * dname.pl - This is a utility script to clean up the foundation database for
 * device display names and identification fields that were inconsistently fed
 * into the database. This can cause some issues in the display for the event
 * console, especially when upgrading an older database. Use in consultation
 * with GroundWork Support!
 */

package com.groundworkopensource.portal.statusviewer.bean.networkservice;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

/**
 * @author swapnil_gujrathi
 * 
 */
public class NetworkServiceDatabase extends TimerTask {

    /**
     * READ_NOTIFICATIONS_TABLE_INTERVAL - 6 Hours = 21600000 milliseconds
     */
    private static final int READ_NOTIFICATIONS_TABLE_INTERVAL = 21600000; // 120000;

    /**
     * NetworkServiceDatabase Singleton Instance
     */
    private static NetworkServiceDatabase networkServiceDatabaseInstance;

    /**
     * Private constructor
     */
    private NetworkServiceDatabase() {
        // read notifications data from database
        readNotificationsFromDatabase();

        // read installation information from file - one time activity
        readInstallationInformation();
    }

    /**
     * @return networkServiceDatabaseInstance
     */
    public static synchronized NetworkServiceDatabase getInstance() {
        if (networkServiceDatabaseInstance == null) {
            networkServiceDatabaseInstance = new NetworkServiceDatabase();

            // initialize TimerTask
            Timer timer = new Timer();
            timer.schedule(networkServiceDatabaseInstance,
                    READ_NOTIFICATIONS_TABLE_INTERVAL,
                    READ_NOTIFICATIONS_TABLE_INTERVAL);
        }
        return networkServiceDatabaseInstance;
    }

    /**
     * LOGGER
     */
    private static final Logger LOGGER = Logger
            .getLogger(NetworkServiceDatabase.class.getName());

    /**
     * Groundwork Installation Information string
     */
    private String installationInformation;

    /**
     * db_connection
     */
    private Connection dbConnection;

    /**
     * un-read notifications
     */
    private ArrayList<Notification> unReadNotifications = new ArrayList<Notification>();

    /**
     * read notifications
     */
    private ArrayList<Notification> allNotifications = new ArrayList<Notification>();

    /**
     * last checked date string
     */
    private String lastChecked = "unknown";

    /**
     * Reads and sets the installation information.
     */
    private void readInstallationInformation() {
        String installInfo = "";
        Properties properties = NetworkServiceHelpers
                .readPropertiesFromFile(NetworkServiceConfig
                        .get("ns.files.agent_config"));

        installInfo += "<p>";
        if (properties == null) {
            installInfo += NetworkServiceConfig
                    .get("ns.msg.errors.agent_config_reading");
        } else {
            installInfo += "<b>application_guid:</b> "
                    + properties.getProperty("application_guid") + "<br/>";
            installInfo += "<b>installation_guid:</b> "
                    + properties.getProperty("installation_guid") + "<br/>";
        }
        installInfo += "</p>";

        String info = NetworkServiceHelpers
                .readTextFileToHtml(NetworkServiceConfig.get("ns.files.info"));
        installInfo += "<p>";
        if (info == null) {
            installInfo += NetworkServiceConfig
                    .get("ns.msg.errors.info_reading");
        } else {
            installInfo += info;
        }
        installInfo += "</p>";

        // set installation information
        setInstallationInformation(installInfo);
    }

    /**
     * @return database connection
     */
    private Connection connect() {
        String dbUrl = "";
        String dbUsername = "";
        String dbPassword = "";
        Properties configProperties = NetworkServiceConfig.getProperties();
        if (null == configProperties) {
            return null;
        }

        try {
            String dbConfigFile = configProperties
                    .getProperty("ns.db.config_file");

            Properties dbProperties = NetworkServiceHelpers
                    .readPropertiesFromFile(dbConfigFile);
            if (dbProperties != null) {
                for (Enumeration<Object> dbEnumeration = dbProperties.keys(); dbEnumeration
                        .hasMoreElements();) {
                    String key = (String) dbEnumeration.nextElement();
                    configProperties.setProperty(key, dbProperties
                            .getProperty(key));
                }
            }

            dbUrl = configProperties.getProperty(configProperties
                    .getProperty("ns.db.url_key"));
            if (dbUrl == null) {
                String dbPort = configProperties.getProperty(configProperties
                        .getProperty("ns.db.port_key"));
                if (dbPort != null) {
                    dbPort = ":" + dbPort;
                } else {
                    dbPort = "";
                }
                String dbConnectionDriver = configProperties
                        .getProperty(configProperties
                                .getProperty("ns.db.connection_driver_key"));
                String dbHost = configProperties.getProperty(configProperties
                        .getProperty("ns.db.dbhost_key"));
                String dbDatabase = configProperties
                        .getProperty(configProperties
                                .getProperty("ns.db.database_key"));
                dbUrl = "jdbc:" + dbConnectionDriver + "://" + dbHost + dbPort
                        + "/" + dbDatabase;
            }

            String dbConnectionClass = configProperties
                    .getProperty(configProperties
                            .getProperty("ns.db.connection_driver_class_key"));
            dbUsername = configProperties.getProperty(configProperties
                    .getProperty("ns.db.username_key"));
            dbPassword = configProperties.getProperty(configProperties
                    .getProperty("ns.db.password_key"));

            Class.forName(dbConnectionClass);
            dbConnection = DriverManager.getConnection(dbUrl, dbUsername,
                    dbPassword);

            return dbConnection;
        } catch (Exception e) {
            LOGGER
                    .error("Exception while connecting to db in Network Service Portlet. ## url:"
                            + dbUrl
                            + "## user:"
                            + dbUsername
                            + "\nActual Exception : " + e);

        }
        return null;
    }

    /**
     * disconnects the database connection
     */
    private void disconnect() {
        if (null != dbConnection) {

            try {
                dbConnection.close();
            } catch (SQLException e) {
                LOGGER
                        .error("SQL Exception while closing the connection. Actual Exception : "
                                + e);
            }

        }
    }

    /**
     * @param query
     * @return
     * @throws SQLException
     */
    private ResultSet query(String query) throws SQLException {
        if (null != dbConnection) {
            Statement queryStatement = dbConnection.createStatement();
            return (queryStatement.executeQuery(query));
        }
        return null;
    }

    /**
     * @param query
     * @return
     */
    private Integer update(String query) {
        try {
            // connect to the database
            connect();

            if (null != dbConnection) {
                Statement updateStatement = dbConnection.createStatement();
                return updateStatement.executeUpdate(query);
            }
        } catch (SQLException e) {
            LOGGER.error("SQLException while updating datbase. Query [ "
                    + query + "] Actual exception : " + e);
        } finally {
            // disconnect
            disconnect();
        }
        return -1;
    }

    /**
     * Returns notifications as per the condition passed.
     * 
     * @param conditions
     * @return list of notifications.
     */
    public ArrayList<Notification> findNotifications(String conditions) {
        if (conditions.equalsIgnoreCase("unread")) {
            return unReadNotifications;
        } else {
            return allNotifications;
        }
    }

    /**
     * @param id
     */
    public void markNotificationAsRead(Integer id) {
        Integer update = update("UPDATE network_service_notifications set is_read = 1 where id = "
                + id + ";");
        if (update == 1) {
            // get the notification from "unread" notifications list
            Notification notificationById = getNotificationById(
                    unReadNotifications, id);
            // remove from unread
            unReadNotifications.remove(notificationById);
            // mark in all notifications list
            markNotificationsReadStatus(true, id);
        }
    }

    /**
     * @param id
     */
    public void markNotificationAsUnread(Integer id) {
        Integer update = update("UPDATE network_service_notifications set is_read = 0 where id = "
                + id + ";");
        if (update == 1) {
            // get the notification from "unread" notifications list
            Notification notificationById = getNotificationById(
                    allNotifications, id);
            // add to unread
            unReadNotifications.add(notificationById);
            // mark in all notifications list
            markNotificationsReadStatus(false, id);
        }
    }

    /**
     * @param id
     */
    public void markNotificationAsArchived(Integer id) {
        Integer update = update("UPDATE network_service_notifications set is_archived = 1 where id = "
                + id + ";");
        if (update == 1) {
            markNotificationsArchieval(true, id);
        }
    }

    /**
     * @param id
     */
    public void markNotificationAsNotArchived(Integer id) {
        Integer update = update("UPDATE network_service_notifications set is_archived = 0 where id = "
                + id + ";");
        if (update == 1) {
            markNotificationsArchieval(false, id);
        }
    }

    /**
     * Returns Last checked date string.
     * 
     * @return Last checked date string.
     */
    public String lastChecked() {
        return lastChecked;
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.util.TimerTask#run()
     */
    @Override
    public void run() {
        // read notifications and last updated date from database
        readNotificationsFromDatabase();
    }

    /**
     * Reads notifications and last updated date from database.
     * readNotificationsFromDatabase
     */
    private void readNotificationsFromDatabase() {
        LOGGER
                .debug("Network Service Portlet: Reading Notifications From Database !!!!!");
        ResultSet resultSet;
        String unreadNotificationsQueryString = "SELECT * FROM network_service_notifications where is_read = 0;";
        String readNotificationsQueryString = "SELECT * FROM network_service_notifications;";
        String lastCheckedQueryString = "SELECT last_checked FROM network_service_status order by id desc limit 1;";

        // connect to the database
        connect();

        ArrayList<Notification> notifications = new ArrayList<Notification>();
        // fetch all unread notifications
        try {
            resultSet = query(unreadNotificationsQueryString);
            if (null != resultSet) {
                while (resultSet.next()) {
                    notifications.add(new Notification(resultSet));
                }
                unReadNotifications = notifications;
                resultSet.close();
            }
        } catch (Exception e) {
            LOGGER
                    .error("######### Exception while fetching un-read notifications. Query [ "
                            + unreadNotificationsQueryString
                            + "] Actual exception :" + e);
        }

        // fetch all read notifications
        try {
            notifications = new ArrayList<Notification>();
            resultSet = query(readNotificationsQueryString);
            if (null != resultSet) {
                while (resultSet.next()) {
                    notifications.add(new Notification(resultSet));
                }
                allNotifications = notifications;
                resultSet.close();
            }
        } catch (Exception e) {
            LOGGER
                    .error("######### Exception while fetching read notifications. Query [ "
                            + readNotificationsQueryString
                            + "] Actual exception :" + e);
        }

        // get the last checked date-string

        try {
            resultSet = query(lastCheckedQueryString);
            if (null != resultSet) {
                if (resultSet.next()) {
                    lastChecked = resultSet.getString("last_checked");
                }
                resultSet.close();
            }
        } catch (Exception e) {
            LOGGER
                    .error("######### Exception while fetching last checked date string. Query [ "
                            + readNotificationsQueryString
                            + "] Actual exception :" + e);
        }

        // disconnect the database connection
        disconnect();
    }

    /**
     * Sets the installationInformation.
     * 
     * @param installationInformation
     *            the installationInformation to set
     */
    public void setInstallationInformation(String installationInformation) {
        this.installationInformation = installationInformation;
    }

    /**
     * Returns the installationInformation.
     * 
     * @return the installationInformation
     */
    public String getInstallationInformation() {
        return installationInformation;
    }

    /**
     * Returns the unreadNotifications.
     * 
     * @return the unreadNotifications
     */
    public ArrayList<Notification> getUnreadNotifications() {
        return unReadNotifications;
    }

    /**
     * Returns all Notifications.
     * 
     * @return all Notifications
     */
    public ArrayList<Notification> getAllNotifications() {
        return allNotifications;
    }

    /**
     * @param markArchieved
     * @param notificationId
     */
    private void markNotificationsArchieval(boolean markArchieved,
            Integer notificationId) {
        Iterator<Notification> readNotificationsIterator = allNotifications
                .iterator();
        while (readNotificationsIterator.hasNext()) {
            Notification notification = readNotificationsIterator.next();
            if (notificationId.equals(notification.getId())) {
                notification.setIsArchived(markArchieved);
            }
        }

        Iterator<Notification> unReadNotificationsIterator = unReadNotifications
                .iterator();
        while (unReadNotificationsIterator.hasNext()) {
            Notification notification = unReadNotificationsIterator.next();
            if (notificationId == notification.getId()) {
                notification.setIsArchived(markArchieved);
            }
        }
    }

    /**
     * @param markRead
     * @param notificationId
     */
    private void markNotificationsReadStatus(boolean markRead,
            Integer notificationId) {
        Iterator<Notification> readNotificationsIterator = allNotifications
                .iterator();
        while (readNotificationsIterator.hasNext()) {
            Notification notification = readNotificationsIterator.next();
            if (notificationId.equals(notification.getId())) {
                notification.setIsRead(markRead);
            }
        }
    }

    /**
     * @param notifications
     * @param notificationId
     * @return Notification with passed Id
     */
    private Notification getNotificationById(
            ArrayList<Notification> notifications, Integer notificationId) {
        Iterator<Notification> notificationsIterator = notifications.iterator();
        while (notificationsIterator.hasNext()) {
            Notification notification = notificationsIterator.next();
            if (notificationId.equals(notification.getId())) {
                return notification;
            }
        }
        return null;
    }
}