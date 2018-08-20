package com.groundworkopensource.portal.statusviewer.common.actions;

/**
 * This class defines constants for action commands displayed as menuItems on
 * actions portlet.
 * 
 * @author shivangi_walvekar
 * 
 */
public class ActionCommandsConstants {
    /**
     * Protected Constructor - Rationale: Instantiating utility classes does not
     * make sense. Hence the constructors should either be private or (if you
     * want to allow sub-classing) protected. <br>
     * 
     * Refer to "HideUtilityClassConstructor" section in
     * http://checkstyle.sourceforge.net/config_design.html.
     */
    protected ActionCommandsConstants() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }

    // Constants for parent menu 'Acknowledge' .
    /**
     * Constant for "Acknowledge This Host Problem"
     */
    public static final String ACK_HOST_PROB = "Acknowledge This Host Problem";

    /**
     * Constant for "Acknowledge This Host Problem"
     */
    public static final String ACK_SERVICES_TOO = "Acknowledge This Host Problem - services too";

    /**
     * Constant for "Remove Acknowledgment of Problem"
     */
    public static final String REMOVE_HOST_ACKNOWLEDGEMENT = "Remove Acknowledgment of Problem";
    /**
     * Constant for "Acknowledge This service Problem"
     */
    public static final String ACKNOWLEDGE_SVC_PROBLEM = "Acknowledge Problem";
    /**
     * Constant for "Acknowledge This service Problem"
     */
    public static final String REMOVE_SVC_ACKNOWLEDGEMENT = "Remove Problem Acknowledgment";

    // Constants for parent menu 'Notifications'.
    /**
     * Constant for "Enable notifications for all Hosts"
     */
    public static final String ENABLE_HOSTGROUP_HOST_NOTIFICATIONS = "Enable notifications for all Hosts";
    /**
     * Constant for "Disable notifications for all Hosts"
     */
    public static final String DISABLE_HOSTGROUP_HOST_NOTIFICATIONS = "Disable notifications for all Hosts";
    /**
     * Constant for "Enable notifications for all Services"
     */
    public static final String ENABLE_HOSTGROUP_SVC_NOTIFICATIONS = "Enable notifications for all Services";
    /**
     * Constant for "Disable notifications for all Services"
     */
    public static final String DISABLE_HOSTGROUP_SVC_NOTIFICATIONS = "Disable notifications for all Services";
    /**
     * Constant for "Enable Notifications"
     */
    public static final String ENABLE_HOST_NOTIFICATIONS = "Enable Notifications";

    /**
     * Constant for "Delay next Notification"
     */
    public static final String DELAY_HOST_NOTIFICATION = "Delay next Notification";
    /**
     * Constant for "Disable Notifications for All Services on Host"
     */
    public static final String DISABLE_HOST_SVC_NOTIFICATIONS = "Disable Notifications for All Services on Host";

    /**
     * Constant for "Disable Notifications"
     */
    public static final String DISABLE_HOST_NOTIFICATIONS = "Disable Notifications";

    /**
     * Constant for "Enable Notifications for All Services on Host"
     */
    public static final String ENABLE_HOST_SVC_NOTIFICATIONS = "Enable Notifications for All Services on Host";

    /**
     * Constant for "Enable notifications for all Services"
     */
    public static final String ENABLE_SERVICEGROUP_SVC_NOTIFICATIONS = "Enable notifications for all Services";

    /**
     * Constant for "Disable notifications for all Services"
     */
    public static final String DISABLE_SERVICEGROUP_SVC_NOTIFICATIONS = "Disable notifications for all Services";

    /**
     * Constant for "Disable Notifications" for service
     */
    public static final String DISABLE_SVC_NOTIFICATIONS = "Disable Notifications";

    /**
     * Constant for "Delay Next Notification" for service
     */
    public static final String DELAY_SVC_NOTIFICATION = "Delay Next Notification";

    /**
     * Constant for "Enable Notifications" for service
     */
    public static final String ENABLE_SVC_NOTIFICATIONS = "Enable Notifications";

    // Constants for parent menu 'Downtime'.

    /**
     * Constant for "Schedule Downtime For All Hosts
     */
    public static final String SCHEDULE_HOSTGROUP_HOST_DOWNTIME = "Schedule Downtime For All Hosts";

    /**
     * Constant for "Schedule Downtime For All Services"
     */
    public static final String SCHEDULE_HOSTGROUP_SVC_DOWNTIME = "Schedule Downtime For All Services";

    /**
     * Constant for "Schedule Downtime" for host
     */
    public static final String SCHEDULE_HOST_DOWNTIME = "Schedule Downtime";

    /**
     * Constant for "Schedule Downtime For All Services"
     */
    public static final String SCHEDULE_SERVICEGROUP_SVC_DOWNTIME = "Schedule Downtime For All Services";

    /**
     * Constant for "Schedule Downtime For This Service"
     */
    public static final String SCHEDULE_SVC_DOWNTIME = "Schedule Downtime For This Service";

    // Constants for parent menu 'Configuration/Settings'.

    /**
     * Constant for "Enable Active Checks for all Services" in host group
     */
    public static final String ENABLE_HOSTGROUP_SVC_CHECKS = "Enable Active Checks for all Services";

    /**
     * Constant for "Enable Active Check for all hosts in hostgroup"
     */
    public static final String ENABLE_HOSTGROUP_HOST_CHECKS = "Enable Active Checks for all hosts in hostgroup";

    /**
     * Constant for "Disable checks for all Services"
     */
    public static final String DISABLE_HOSTGROUP_SVC_CHECKS = "Disable Active Checks for all Services";

    /**
     * Constant for "Disable Active Check for all hosts"
     */
    public static final String DISABLE_HOSTGROUP_HOST_CHECKS = "Disable Active Checks for all services - for hosts too";

    /**
     * Constant for "Disable Checks On This Host"
     */
    public static final String DISABLE_HOST_CHECK = "Disable Active Checks on Host";

    /**
     * Constant for "Enable Active Checks On This Host"
     */
    public static final String ENABLE_HOST_CHECK = "Enable Active Checks on Host";

    /**
     * Constant for "Enable Passive Checks"
     */
    public static final String ENABLE_PASSIVE_HOST_CHECKS = "Enable Passive Checks";

    /**
     * Constant for "Disable Passive Checks"
     */
    public static final String DISABLE_PASSIVE_HOST_CHECKS = "Disable Passive Checks";

    /**
     * Constant for "Disable Active Check for All Services on Host"
     */
    public static final String DISABLE_HOST_SVC_CHECKS = "Disable Active Checks for All Services on Host";

    /**
     * Constant for "Enable Active Checks for All Services on Host"
     */
    public static final String ENABLE_HOST_SVC_CHECKS = "Enable Active Checks for All Services on Host";

    // /**
    // * Constant for "Start Obsessing Over This Host"
    // */
    // public static final String START_OBSESSING_OVER_HOST =
    // "Start Obsessing Over This Host";
    //
    // /**
    // * Constant for "Stop Obsessing Over This Host"
    // */
    // public static final String STOP_OBSESSING_OVER_HOST =
    // "Stop Obsessing Over This Host";

    /**
     * Constant for "Enable Flap Detection" for host
     */
    public static final String ENABLE_HOST_FLAP_DETECTION = "Enable Flap Detection";
    /**
     * Constant for "Disable Flap Detection"
     */
    public static final String DISABLE_HOST_FLAP_DETECTION = "Disable Flap Detection";
    /**
     * Constant for "Enable Active Check for all Service"
     */
    public static final String ENABLE_SERVICEGROUP_SVC_CHECKS = "Enable Active Checks for all Services";
    /**
     * Constant for "Disable Active Check for all Services"
     */
    public static final String DISABLE_SERVICEGROUP_SVC_CHECKS = "Disable Active Checks for all Services";
    /**
     * Constant for "Disable Active Check On This Service"
     */
    public static final String DISABLE_SVC_CHECK = "Disable Active Checks on Service";
    /**
     * Constant for "Enable Active Check on Service"
     */
    public static final String ENABLE_SVC_CHECK = "Enable Active Checks on Service";
    /**
     * Constant for "Disable Passive Checks" for service
     */
    public static final String DISABLE_PASSIVE_SVC_CHECKS = "Disable Passive Checks";
    /**
     * Constant for "Enable Passive Checks" for service
     */
    public static final String ENABLE_PASSIVE_SVC_CHECKS = "Enable Passive Checks";
    /**
     * Constant for "Disable Flap Detection" for service
     */
    public static final String DISABLE_SVC_FLAP_DETECTION = "Disable Flap Detection";
    /**
     * Constant for "Enable Flap Detection" for service
     */
    public static final String ENABLE_SVC_FLAP_DETECTION = "Enable Flap Detection";

    // Constants for parent menu 'Event Handler'
    /**
     * Constant for "Enable Event Handler" for host
     */
    public static final String ENABLE_HOST_EVENT_HANDLER = "Enable Event Handler";

    /**
     * Constant for "Disable Event Handler" for host
     */
    public static final String DISABLE_HOST_EVENT_HANDLER = "Disable Event Handler";

    /**
     * Constant for "Disable Event Handler" for service
     */
    public static final String DISABLE_SVC_EVENT_HANDLER = "Disable Event Handler";

    /**
     * Constant for "Enable Event Handler" for service
     */
    public static final String ENABLE_SVC_EVENT_HANDLER = "Enable Event Handler";

    // Constants for parent menu 'Submit Checks'
    /**
     * Constant for "Re-Schedule the Next Check" for host
     */
    public static final String SCHEDULE_HOST_CHECK = "Re-Schedule the Next Check";

    /**
     * Constant for "Re-Schedule the Next Check-Forced" for host
     */
    public static final String SCHEDULE_FORCED_HOST_CHECK = "Re-Schedule the Next Check - Forced";

    /**
     * Constant for "Schedule Check For All Services Of This Host"
     */
    public static final String SCHEDULE_HOST_SVC_CHECKS = "Schedule Check For All Services Of This Host";

    /**
     * Constant for "Schedule Check For All Services Of This Host - Forced"
     */
    public static final String SCHEDULE_FORCED_HOST_SVC_CHECKS = "Schedule Check For All Services Of This Host-Forced";

    /**
     * Constant for "Submit Passive Check Result" for host
     */
    public static final String PROCESS_HOST_CHECK_RESULT = "Submit Passive Check Result";

    /**
     * Constant for "Submit Passive Check Result" for service
     */
    public static final String PROCESS_SERVICE_CHECK_RESULT = "Submit Passive Check Result";

    /**
     * Constant for "Reschedule Next Check" for service
     */
    public static final String SCHEDULE_SVC_CHECK = "Reschedule Next Check";

    /**
     * Constant for "Reschedule Next Check-Forced" for service
     */
    public static final String SCHEDULE_FORCED_SVC_CHECK = "Reschedule Next Check-Forced";

    /**
     * Constant for "schedule triggered downtime for child hosts." option for
     * 'schedule downtime' action command.
     */
    public static final String SCHEDULE_AND_PROPAGATE_TRIGGERED_HOST_DOWNTIME = "Schedule triggered downtime for all child hosts";

    /**
     * Constant for "Schedule non-triggered downtime for all child hosts" option
     * for 'schedule downtime' action command.
     */
    public static final String SCHEDULE_AND_PROPAGATE_HOST_DOWNTIME = "Schedule non-triggered downtime for all child hosts";

    /**
     * Constant for "Do nothing with child hosts" option for 'schedule downtime'
     * action command.
     */
    public static final String DO_NOTHING_WITH_CHILD_HOSTS = "Do nothing with child hosts";

}
