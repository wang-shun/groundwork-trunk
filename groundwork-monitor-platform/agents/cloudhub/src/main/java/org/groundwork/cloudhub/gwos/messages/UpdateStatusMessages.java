package org.groundwork.cloudhub.gwos.messages;

/**
 * Created by dtaylor on 6/3/15.
 */
public interface UpdateStatusMessages {

    /**
     * Retrieve status message for a host having status updated for a hypervisor
     * @return
     */
    String getHostHypervisorMessage();

    /**
     * Retrieve status message for a host having status updated for a VM
     * @return
     */
    String getHostVmMessage();

    /**
     * Retrieve status message for a monitor host having status updated
     * @return
     */
    String getHostMonitorMessage();

    /**
     * Retrieve status message for a service having status updated for a hypervisor
     * @return
     */
    String getServiceHypervisorMessage();

    /**
     * Retrieve status message for a service having status updated for a VM
     * @return
     */
    String getServiceVmMessage();

    /**
     * Retrieve status message for a monitor service having status updated
     * @return
     */
    String getServiceMonitorMessage();

    /**
     * Comment for notifications
     */
    String getComment();

    /**
     * Comment for monitor notifications
     */
    String getMonitorComment();

    /**
     * Retrieve the notification type for this message
     *
     * @return
     */
    String getNotificationType();

    /**
     * Retrieve the operation status for this message
     *
     * @return
     */
    String getOperationalStatus();


    /**
     * Retrieve the severity for this messages
     */
    String getSeverity();
}
