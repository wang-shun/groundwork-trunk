package org.groundwork.rs.client;

import org.groundwork.rs.dto.DtoHostNotification;
import org.groundwork.rs.dto.DtoHostNotificationList;
import org.groundwork.rs.dto.DtoOperationResult;
import org.groundwork.rs.dto.DtoOperationResults;
import org.groundwork.rs.dto.DtoServiceNotification;
import org.groundwork.rs.dto.DtoServiceNotificationList;
import org.junit.Test;

public class NotificationClientTest extends AbstractClientTest {

    @Test
    public void testHostNotification() throws Exception {
        if (serverDown) return;
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
        NotificationClient client = new NotificationClient(getDeploymentURL());
        DtoOperationResults results = client.notifyHosts(notifications);
        assert 1 == results.getCount();
        for (DtoOperationResult result : results.getResults()) {
            assert(result.getStatus().equals(DtoOperationResult.SUCCESS));
        }
    }

    @Test
    public void testServiceNotification() throws Exception {
        if (serverDown) return;
        DtoServiceNotificationList notifications = new DtoServiceNotificationList();
        DtoServiceNotification notification = new DtoServiceNotification();
        notification.setHostName("localhost");
        notification.setHostAddress("127.0.0.1");
        notification.setHostGroupNames("myGroup");
        notification.setServiceDescription("local_cpu_nagios");
        notification.setServiceOutput("Status of Service is UP");
        notification.setServiceState("GOOD");
        notification.setNotificationComment("some comment");
        notification.setNotificationRecipients("admin@gwos.com");
        notification.setNotificationType("PROBLEM");
        notification.setCheckDateTime("3/30/14");
        notification.setNotificationAuthOrAlias("alias");
        notification.setServiceGroupNames("serviceGroup1");
        notification.setHostAlias("hostalias");
        notification.setServiceNotificationId("34");
        notifications.add(notification);
        NotificationClient client = new NotificationClient(getDeploymentURL());
        DtoOperationResults results = client.notifyServices(notifications);
        assert 1 == results.getCount();
        for (DtoOperationResult result : results.getResults()) {
            assert(result.getStatus().equals(DtoOperationResult.SUCCESS));
        }
    }

}
