package com.groundworkopensource.portal.statusviewer.common.actions;

/**
 * This class defines all the constants for the command description to be
 * displayed on all the intermediate pop-ups for actions portlet.
 * 
 * @author shivangi_walvekar
 * 
 */
public class CommandDescriptionConstants {
    /**
     * Protected Constructor - Rationale: Instantiating utility classes does not
     * make sense. Hence the constructors should either be private or (if you
     * want to allow sub-classing) protected. <br>
     * 
     * Refer to "HideUtilityClassConstructor" section in
     * http://checkstyle.sourceforge.net/config_design.html.
     */
    protected CommandDescriptionConstants() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }

    /**
     * String constant for the description for the nagios command 'Acknowledge
     * This Host Problem'
     */
    public static final String ACK_HOST_PROB = "This command is used to acknowledge a host problem. When a host problem is acknowledged, future notifications about problems are temporarily disabled until the host changes state (i.e. recovers). Contacts for this host will receive a notification about the acknowledgement, so they are aware that someone is working on the problem. Additionally, a comment will also be added to the host. Make sure to enter your name and fill in a brief description of what you are doing in the comment field. If you would like the host comment to remain once the acknowledgement is removed Or in between restarts of Nagios, check the 'Persistent' checkbox. If you do not want an acknowledgement notification sent out to the appropriate contacts, uncheck the 'Send Notification' checkbox.";

    /**
     * String constant for the description for the command 'Acknowledge All
     * Services', which is available at some places (e. g. Seurat view)
     */
    public static final String ACK_ALL_SERVICES_PROB = "This command is used to acknowledge problems of all services associated with a host. When a service problem is acknowledged, future notifications about problems are temporarily disabled until the service changes state (i.e. recovers). Contacts for these services will receive a notification about the acknowledgement, so they are aware that someone is working on the problem. Additionally, a comment will also be added to the service. Make sure to enter your name and fill in a brief description of what you are doing in the comment field. If you would like the service comment to remain once the acknowledgement is removed Or in between restarts of Nagios, check the 'Persistent' checkbox. If you do not want an acknowledgement notification sent out to the appropriate contacts, uncheck the 'Send Notification' checkbox.";
    /**
     * String constant for the description for the nagios command 'Remove
     * Acknowledgment of Problem' for host
     */
    public static final String REM_ACK_PROB = "This command is used to remove an acknowledgement for a particular host problem. Once the acknowledgement is removed, notifications may start being sent out about the host problem.";
    /**
     * String constant for the description for the nagios command 'Enable
     * Notifications' for host
     */
    public static final String ENABLE_HOST_NOTIFICATIONS = "This command is used to enable notifications for the specified host. Notifications will only be sent out for the host state types you defined in your host definition. Note that this command does not enable notifications for services associated with this host.";
    /**
     * String constant for the description for the nagios command 'Delay next
     * Notification' for host
     */
    public static final String DELAY_HOST_NOTIFICATION = "This command is used to delay the next problem notification that is sent out for the specified host. The notification delay will be disregarded if the host changes state before the next notification is scheduled to be sent out. This command has no effect if the host is currently UP.";
    /**
     * String constant for the description for the nagios command 'Disable
     * Notifications for All Services on Host'
     */
    public static final String DISABLE_HOST_SVC_NOTIFICATIONS = "This command is used to prevent notifications from being sent out for all services on the specified host. You will have to re-enable notifications for all services associated with this host before any alerts can be sent out in the future. This does not prevent notifications from being sent out about the host unless you check the 'Disable for host too' option.";
    /**
     * String constant for the description for the nagios command 'Disable
     * Notifications' for this Host
     */
    public static final String DISABLE_HOST_NOTIFICATIONS = "This command is used to prevent notifications from being sent out for the specified host. You will have to re-enable notifications for this host before any alerts can be sent out in the future. Note that this command does not disable notifications for services associated with this host.";
    /**
     * String constant for the description for the nagios command 'Enable
     * Notifications for All Services on Host'
     */
    public static final String ENABLE_HOST_SVC_NOTIFICATIONS = "This command is used to enable notifications for all services on the specified host. Notifications will only be sent out for the service state types you defined in your service definition. This does not enable notifications for the host unless you check the 'Enable for host too' option.";

    /**
     * String constant for the description for the nagios command 'Disable
     * Checks On This Host'
     */
    public static final String DISABLE_HOST_CHECK = "This command is used to temporarily prevent Nagios from actively checking the status of a particular host. If Nagios needs to check the status of this host, it will assume that it is in the same state that it was in before checks were disabled.";
    /**
     * String constant for the description for the nagios command 'Enable Checks
     * On This Host'
     */
    public static final String ENABLE_HOST_CHECK = "This command is used to enable active checks of this host.";
    /**
     * String constant for the description for the nagios command 'Enable
     * Passive Checks' for this host
     */
    public static final String ENABLE_PASSIVE_HOST_CHECKS = "This command is used to allow Nagios to accept passive host check results that it finds in the external command file for a particular host.";
    /**
     * String constant for the description for the nagios command 'Disable
     * Passive Checks' for this host
     */
    public static final String DISABLE_PASSIVE_HOST_CHECKS = "This command is used to stop Nagios from accepting passive host check results that it finds in the external command file for a particular host. All passive check results that are found for this host will be ignored.";
    /**
     * String constant for the description for the nagios command 'Disable
     * Active Checks for All Services on Host'
     */
    public static final String DISABLE_HOST_SVC_CHECKS = "This command is used to disable active checks of all services associated with the specified host. When a service is disabled Nagios will not monitor the service. Doing this will prevent any notifications being sent out for the specified service while it is disabled. In order to have Nagios check the service in the future you will have to re-enable the service. Note that disabling service checks may not necessarily prevent notifications from being sent out about the host which those services are associated with. This does not disable checks of the host unless you check the 'Disable for host too' option.";
    /**
     * String constant for the description for the nagios command 'Enable Active
     * Checks for All Services on Host'
     */
    public static final String ENABLE_HOST_SVC_CHECKS = "This command is used to enable active checks of all services associated with the specified host. This does not enable checks of the host unless you check the 'Enable for host too' option.";

    // /**
    // * String constant for the description for the nagios command 'Start
    // * Obsessing Over This Host'
    // */
    // public static final String START_OBSESSING_OVER_HOST =
    // "This command is used to have Nagios start obsessing over a particular host."
    // ;
    // /**
    // * String constant for the description for the nagios command 'Stop
    // * Obsessing Over This Host'
    // */
    // public static final String STOP_OBSESSING_OVER_HOST =
    // "This command is used to stop Nagios from obsessing over a particular host."
    // ;

    /**
     * String constant for the description for the nagios command 'Enable Flap
     * Detection' for this Host'
     */
    public static final String ENABLE_HOST_FLAP_DETECTION = "This command is used to enable flap detection for a specific host. If flap detection is disabled on a program-wide basis, this will have no effect.";
    /**
     * String constant for the description for the nagios command 'Disable Flap
     * Detection' for this Host'
     */
    public static final String DISABLE_HOST_FLAP_DETECTION = "This command is used to disable flap detection for a specific host.";
    /**
     * String constant for the description for the nagios command 'Enable Event
     * Handler' for this Host'
     */
    public static final String ENABLE_HOST_EVENT_HANDLER = "This command is used to allow Nagios to run the host event handler for a particular service when necessary (if one is defined).";
    /**
     * String constant for the description for the nagios command 'Disable Event
     * Handler' for this Host'
     */
    public static final String DISABLE_HOST_EVENT_HANDLER = "This command is used to temporarily prevent Nagios from running the host event handler for a particular host.";
    /**
     * String constant for the description for the nagios command 'Re-Schedule
     * the Next Check' for this Host
     */
    public static final String SCHEDULE_HOST_CHECK = "This command is used to schedule the next check of a particular host. Nagios will re-queue the host to be checked at the time you specify. If you select the force check option, Nagios will force a check of the host regardless of both what time the scheduled check occurs and whether or not checks are enabled for the host.";
    /**
     * String constant for the description for the nagios command 'Schedule
     * Check For All Services Of This Host'
     */
    public static final String SCHEDULE_HOST_SVC_CHECKS = "This command is used to scheduled the next check of all services on the specified host. If you select the force check option, Nagios will force a check of all services on the host regardless of both what time the scheduled checks occur and whether or not checks are enabled for those services.";
    /**
     * String constant for the description for the nagios command 'Submit
     * Passive Check Result' for this host
     */
    public static final String PROCESS_HOST_CHECK_RESULT = "This command is used to submit a passive check result for a particular host.";
    /**
     * String constant for the description for the nagios command 'Schedule
     * Downtime' for this host
     */
    public static final String SCHEDULE_HOST_DOWNTIME = "This command is used to schedule downtime for a particular host. During the specified downtime, Nagios will not send notifications out about the host. When the scheduled downtime expires, Nagios will send out notifications for this host as it normally would. Scheduled downtimes are preserved across program shutdowns and restarts. Both the start and end times should be specified in the following format: mm/dd/yyyy hh:mm:ss. If you select the fixed option, the downtime will be in effect between the start and end times you specify. If you do not select the fixed option, Nagios will treat this as 'flexible' downtime. Flexible downtime starts when the host goes down or becomes unreachable (sometime between the start and end times you specified) and lasts as long as the duration of time you enter. The duration fields do not apply for fixed downtime.";

    /**
     * String constant for the description for the nagios command 'Acknowledge
     * Problem' for this service
     */
    public static final String ACK_SVC_PROB = "This command is used to acknowledge a service problem. When a service problem is acknowledged, future notifications about problems are temporarily disabled until the service changes state (i.e. recovers). Contacts for this service will receive a notification about the acknowledgement, so they are aware that someone is working on the problem. Additionally, a comment will also be added to the service. Make sure to enter your name and fill in a brief description of what you are doing in the comment field. If you would like the service comment to remain once the acknowledgement is removed Or in between restarts of Nagios, check the 'Persistent' checkbox. If you do not want an acknowledgement notification sent out to the appropriate contacts, uncheck the 'Send Notification' checkbox.";
    /**
     * String constant for the description for the nagios command 'Remove
     * Problem Acknowledgment' for this service
     */
    public static final String REM_SVC_ACK = "This command is used to remove an acknowledgement for a particular service problem. Once the acknowledgement is removed, notifications may start being sent out about the service problem.";
    /**
     * String constant for the description for the nagios command 'Schedule
     * Downtime For This Service'
     */
    public static final String SCHEDULE_SVC_DOWNTIME = "This command is used to schedule downtime for a particular service. During the specified downtime, Nagios will not send notifications out about the service. When the scheduled downtime expires, Nagios will send out notifications for this service as it normally would. Scheduled downtimes are preserved across program shutdowns and restarts. Both the start and end times should be specified in the following format: mm/dd/yyyy hh:mm:ss. option, Nagios will treat this as 'flexible' downtime. Flexible downtime starts when the service enters a non-OK state (sometime between the start and end times you specified) and lasts as long as the duration of time you enter. The duration fields do not apply for fixed downtime.";
    /**
     * String constant for the description for the nagios command 'Disable
     * Notifications For This Service'
     */
    public static final String DISABLE_SVC_NOTIFICATIONS = "This command is used to prevent notifications from being sent out for the specified service. You will have to re-enable notifications for this service before any alerts can be sent out in the future.";
    /**
     * String constant for the description for the nagios command 'Delay Next
     * Notification For This Service'
     */
    public static final String DELAY_SVC_NOTIFICATION = "This command is used to delay the next problem notification that is sent out for the specified service. The notification delay will be disregarded if the service changes state before the next notification is scheduled to be sent out. This command has no effect if the service is currently in an OK state.";
    /**
     * String constant for the description for the nagios command 'Enable
     * Notifications For This Service'
     */
    public static final String ENABLE_SVC_NOTIFICATIONS = "This command is used to enable notifications for the specified service. Notifications will only be sent out for the service state types you defined in your service definition.";
    /**
     * String constant for the description for the nagios command 'Disable
     * Checks On This Service'
     */
    public static final String DISABLE_SVC_CHECK = "This command is used to disable active checks of a service.";
    /**
     * String constant for the description for the nagios command 'Disable
     * Checks On This Service'
     */
    public static final String ENABLE_SVC_CHECK = "This command is used to disable active checks of a service.";
    /**
     * String constant for the description for the nagios command 'Disable
     * Passive Checks for This Service'
     */
    public static final String DISABLE_PASSIVE_SVC_CHECKS = "This command is used to stop Nagios accepting passive service check results that it finds in the external command file for this particular service. All passive check results that are found for this service will be ignored.";

    /**
     * String constant for the description for the nagios command 'Enable
     * Passive Checks for this Service'
     */
    public static final String ENABLE_PASSIVE_SVC_CHECKS = "This command is used to allow Nagios to accept passive service check results that it finds in the external command file for this particular service.";

    /**
     * String constant for the description for the nagios command 'Disable Flap
     * Detection for this Service'
     */
    public static final String DISABLE_SVC_FLAP_DETECTION = "This command is used to disable flap detection for a specific service.";

    /**
     * String constant for the description for the nagios command 'Enable Flap
     * Detection for this Service'
     */
    public static final String ENABLE_SVC_FLAP_DETECTION = "This command is used to enable flap detection for a specific service. If flap detection is disabled on a program-wide basis, this will have no effect.";
    /**
     * String constant for the description for the nagios command 'Disable Event
     * Handler for this Service'
     */
    public static final String DISABLE_SVC_EVENT_HANDLER = "This command is used to temporarily prevent Nagios from running the service event handler for a particular service.";

    /**
     * String constant for the description for the nagios command 'Enable Event
     * Handler for this Service'
     */
    public static final String ENABLE_SVC_EVENT_HANDLER = "This command is used to allow Nagios to run the service event handler for a particular service when necessary (if one is defined).";
    /**
     * String constant for the description for the nagios command 'Submit
     * Passive Check Result for this Service'
     */
    public static final String PROCESS_SERVICE_CHECK_RESULT = "This command is used to submit a passive check result for a particular service. It is particularly useful for resetting security-related services to OK states once they have been dealt with.";
    /**
     * String constant for the description for the nagios command 'Reschedule
     * Next Check for this Service'
     */
    public static final String SCHEDULE_SVC_CHECK = "This command is used to schedule the next check of a particular service. Nagios will re-queue the service to be checked at the time you specify. If you select the force check option, Nagios will force a check of the service regardless of both what time the scheduled check occurs and whether or not checks are enabled for the service.";
    /**
     * String constant for the description for the nagios command 'Schedule
     * Downtime For All Hosts' in this host group
     */
    public static final String SCHEDULE_HOSTGROUP_HOST_DOWNTIME = "This command is used to schedule downtime for all hosts in a particular hostgroup. During the specified downtime, Nagios will not send notifications out about the hosts. When the scheduled downtime expires, Nagios will send out notifications for the hosts as it normally would. Scheduled downtimes are preserved across program shutdowns and restarts. Both the start and end times should be specified in the following format: mm/dd/yyyy hh:mm:ss. If you select the fixed option, the downtime will be in effect between the start and end times you specify. If you do not select the fixed option, Nagios will treat this as 'flexible' downtime. Flexible downtime starts when a host goes down or becomes unreachable (sometime between the start and end times you specified) and lasts as long as the duration of time you enter. The duration fields do not apply for fixed dowtime.";
    /**
     * String constant for the description for the nagios command 'Schedule
     * Downtime For All Services' in this host group'
     */
    public static final String SCHEDULE_HOSTGROUP_SVC_DOWNTIME = "This command is used to schedule downtime for all services in a particular hostgroup. During the specified downtime, Nagios will not send notifications out about the services. When the scheduled downtime expires, Nagios will send out notifications for the services as it normally would. Scheduled downtimes are preserved across program shutdowns and restarts. Both the start and end times should be specified in the following format: mm/dd/yyyy hh:mm:ss. If you select the fixed option, the downtime will be in effect between the start and end times you specify. If you do not select the fixed option, Nagios will treat this as 'flexible' downtime. Flexible downtime starts when a service enters a non-OK state (sometime between the start and end times you specified) and lasts as long as the duration of time you enter. The duration fields do not apply for fixed dowtime. Note that scheduling downtime for services does not automatically schedule downtime for the hosts those services are associated with. If you want to also schedule downtime for all hosts in the hostgroup, check the 'Schedule downtime for hosts too' option.";
    /**
     * String constant for the description for the nagios command 'Enable
     * notifications for all Hosts' in this host group
     */
    public static final String ENABLE_HOSTGROUP_HOST_NOTIFICATIONS = "This command is used to enable notifications for all hosts in the specified hostgroup. Notifications will only be sent out for the host state types you defined in your host definitions.";
    /**
     * String constant for the description for the nagios command 'Disable
     * notifications for all Hosts' in this host group
     */
    public static final String DISABLE_HOSTGROUP_HOST_NOTIFICATIONS = "This command is used to prevent notifications from being sent out for all hosts in the specified hostgroup. You will have to re-enable notifications for all hosts in this hostgroup before any alerts can be sent out in the future.";
    /**
     * String constant for the description for the nagios command 'Enable
     * notifications for all Services' in this host group
     */
    public static final String ENABLE_HOSTGROUP_SVC_NOTIFICATIONS = "This command is used to enable notifications for all services in the specified hostgroup. Notifications will only be sent out for the service state types you defined in your service definitions. This does not enable notifications for the hosts in this hostgroup unless you check the 'Enable for hosts too' option.";

    /**
     * String constant for the description for the nagios command 'Disable
     * notifications for all Services' in this host group
     */
    public static final String DISABLE_HOSTGROUP_SVC_NOTIFICATIONS = "This command is used to prevent notifications from being sent out for all services in the specified hostgroup. You will have to re-enable notifications for all services in this hostgroup before any alerts can be sent out in the future. This does not prevent notifications from being sent out about the hosts in this hostgroup unless you check the 'Disable for hosts too' option.";

    /**
     * String constant for the description for the nagios command 'Enable checks
     * for all Services' in this host group
     */
    public static final String ENABLE_HOSTGROUP_SVC_CHECKS = "This command is used to enable active checks of all services in the specified hostgroup. This does not enable active checks of the hosts in the hostgroup unless you check the 'Enable for hosts too' option.";
    /**
     * String constant for the description for the nagios command 'Disable
     * checks for all Services' in this host group
     */
    public static final String DISABLE_HOSTGROUP_SVC_CHECKS = "This command is used to disable active checks of all services in the specified hostgroup. This does not disable checks of the hosts in the hostgroup unless you check the 'Disable for hosts too' option.";

    /**
     * String constant for the description for the nagios command 'Enable
     * notifications for all Services' in this service group
     */
    public static final String ENABLE_SERVICEGROUP_SVC_NOTIFICATIONS = "This command is used to enable notifications for all service in the specified servicegroup. Notifications will only be sent out for the service state types you defined in your service definitions.";
    /**
     * String constant for the description for the nagios command 'Disable
     * notifications for all Services' in this service group
     */
    public static final String DISABLE_SERVICEGROUP_SVC_NOTIFICATIONS = "This command is used to prevent notifications from being sent out for all service in the specified servicegroup. You will have to re-enable notifications for all services in this servicegroup before any alerts can be sent out in the future.";
    /**
     * String constant for the description for the nagios command 'Schedule
     * Downtime For All Services' in this service group
     */
    public static final String SCHEDULE_SERVICEGROUP_SVC_DOWNTIME = "This command is used to schedule downtime for all services in a particular serivcegroup. During the specified downtime, Nagios will not send notifications out about the services. When the scheduled downtime expires, Nagios will send out notifications for the services as it normally would. Scheduled downtimes are preserved across program shutdowns and restarts. Both the start and end times should be specified in the following format: mm/dd/yyyy hh:mm:ss. If you select the fixed option, the downtime will be in effect between the start and end times you specify. If you do not select the fixed option, Nagios will treat this as 'flexible' downtime. Flexible downtime starts when a service goes down or becomes unreachable (sometime between the start and end times you specified) and lasts as long as the duration of time you enter. The duration fields do not apply for fixed dowtime.";
    /**
     * String constant for the description for the nagios command 'Enable checks
     * for all Service' in this service group
     */
    public static final String ENABLE_SERVICEGROUP_SVC_CHECKS = "This command is used to enable active checks of all services in the specified servicegroup.";
    /**
     * String constant for the description for the nagios command
     * 'ADD_HOST_COMMENT'
     */
    public static final String ADD_HOST_COMMENT = "Adds a comment to a particular host. If the 'persistent' box is left unchecked, the comment will be deleted the next time Nagios is restarted. Otherwise, the comment will persist across program restarts until it is deleted manually.";
    /**
     * String constant for the description for the nagios command
     * 'ADD_SVC_COMMENT'
     */
    public static final String ADD_SVC_COMMENT = "Adds a comment to a particular service. If the 'persistent' box is left unchecked, the comment will be deleted the next time Nagios is restarted. Otherwise, the comment will persist across program restarts until it is deleted manually.";
    /**
     * String constant for the description for the nagios command
     * 'DEL_HOST_COMMENT'
     */
    public static final String DEL_HOST_COMMENT = "Deletes a host comment.";
    /**
     * String constant for the description for the nagios command
     * 'DEL_SVC_COMMENT'
     */
    public static final String DEL_SVC_COMMENT = "Deletes a service comment.";
    /**
     * String constant for the description for the nagios command 'Disable
     * checks for all Service' in this service group
     */
    public static final String DISABLE_SERVICEGROUP_SVC_CHECKS = "This command is used to disable active checks of all services in the specified servicegroup.";

}
