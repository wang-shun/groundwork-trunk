package org.groundwork.rs.examples;

/**
 * Collage Rest API Example Code
 *
 * NOTE: this App is dependent on having this file installed and configured
 * with the Collage Web Services credentials
 *
 * "/usr/local/groundwork/config/ws_client.properties";
 */
public class App 
{
    protected static final String DEPLOYMENT_URL = "http://localhost:8080/foundation-webapp/api";
    private static boolean enableAsserts = true;

    public static void main( String[] args )
    {
        FoundationConnection connection = new FoundationConnection(DEPLOYMENT_URL, enableAsserts);

        HostExamples hostExamples = new HostExamples(connection);
        hostExamples.listHosts();
        hostExamples.queryHosts();
        hostExamples.lookupHostDeep();
        hostExamples.hostMaintenance();

        HostGroupExamples hostGroupExamples = new HostGroupExamples(connection);
        hostGroupExamples.listHostGroups();
        hostGroupExamples.queryHostGroups();
        hostGroupExamples.lookupHostGroupDeep();
        hostGroupExamples.hostGroupMaintenance();

        EventExamples eventExamples = new EventExamples(connection);
        eventExamples.createEvents();

        NotificationExamples notificationExamples = new NotificationExamples(connection);
        notificationExamples.createHostNotification();
        notificationExamples.createServiceNotification();

        PerfDataExamples perfDataExamples = new PerfDataExamples(connection);
        perfDataExamples.createPerformanceData();
    }

}
