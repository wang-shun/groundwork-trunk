package org.groundwork.rs.examples;

import org.groundwork.rs.client.NotificationClient;
import org.groundwork.rs.dto.DtoHostNotification;
import org.groundwork.rs.dto.DtoHostNotificationList;
import org.groundwork.rs.dto.DtoOperationResult;
import org.groundwork.rs.dto.DtoOperationResults;
import org.groundwork.rs.dto.DtoServiceNotification;
import org.groundwork.rs.dto.DtoServiceNotificationList;

public class NotificationExamples {

    private final FoundationConnection connection;

    public NotificationExamples(FoundationConnection connection) {
        this.connection = connection;
    }

    public void createServiceNotification() {

        NotificationClient notificationClient = new NotificationClient(connection.getDeploymentUrl());
        DtoServiceNotificationList notifications = new DtoServiceNotificationList();
        DtoServiceNotification notification = new DtoServiceNotification();
        notification.setServiceState("OK");
        notification.setHostName("localhost");
        notification.setHostAddress("127.0.0.1");
        notification.setHostGroupNames("myGroup");
        notification.setServiceDescription("local_cpu_nagios");
        notification.setServiceOutput("Status of Service is UP");
        notification.setNotificationComment("some comment");
        notification.setNotificationRecipients("admin@gwos.com");
        notification.setNotificationType("PROBLEM");
        notification.setCheckDateTime("3/30/14");
        notification.setNotificationAuthOrAlias("alias");
        notification.setServiceGroupNames("serviceGroup1");
        notification.setHostAlias("hostalias");
        notification.setServiceNotificationId("34");
        DtoOperationResults results = notificationClient.notifyServices(notifications);
        if (connection.isEnableAsserts()) {
            assert 1 == results.getCount();
            for (DtoOperationResult result : results.getResults()) {
                assert (result.getStatus().equals(DtoOperationResult.SUCCESS));
            }
        }
    }

    public void createHostNotification() {
        NotificationClient notificationClient = new NotificationClient(connection.getDeploymentUrl());
        DtoHostNotificationList notifications = new DtoHostNotificationList();
        DtoHostNotification notification = new DtoHostNotification();
        notification.setHostName("localhost");
        notification.setHostAddress("127.0.0.1");
        notification.setHostGroupNames("myGroup");
        notification.setHostNotificationId("33");
        notification.setHostOutput("Status is Single and Available");
        notification.setHostState("Stateful");
        notification.setNotificationComment("some comment");
        notification.setNotificationRecipients("admin@gwos.com");
        notification.setNotificationType("PROBLEM");
        notification.setCheckDateTime("3/30/14");
        notification.setNotificationAuthOrAlias("alias");
        notifications.add(notification);
        DtoOperationResults results = notificationClient.notifyHosts(notifications);
        assert 1 == results.getCount();
        for (DtoOperationResult result : results.getResults()) {
            assert(result.getStatus().equals(DtoOperationResult.SUCCESS));
        }
    }
}
