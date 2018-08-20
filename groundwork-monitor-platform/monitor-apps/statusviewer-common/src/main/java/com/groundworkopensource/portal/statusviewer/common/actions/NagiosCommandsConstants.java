package com.groundworkopensource.portal.statusviewer.common.actions;

/**
 * This class defines constants for nagios commands templates.
 * 
 * @author shivangi_walvekar
 * 
 */
public class NagiosCommandsConstants {

    /**
     * Protected Constructor - Rationale: Instantiating utility classes does not
     * make sense. Hence the constructors should either be private or (if you
     * want to allow sub-classing) protected. <br>
     * 
     * Refer to "HideUtilityClassConstructor" section in
     * http://checkstyle.sourceforge.net/config_design.html.
     */
    protected NagiosCommandsConstants() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }

    // Constants for nagios command templates for 'Acknowledge' menu.
    /**
     * Command template for ACKNOWLEDGE_HOST_PROBLEM nagios command
     */
    public static final String ACK_HOST_PROB = "<bytesize_cmd>;<user_name>;ACKNOWLEDGE_HOST_PROBLEM;<host_name>;1;<is_send_notification>;<is_persistent_comment>;<comment_author>;<comment_data>\n";

    /**
     * Command template for ACKNOWLEDGE_SVC_PROBLEM nagios command
     */
    public static final String ACK_SERVICES_TOO = "<bytesize_cmd>;<user_name>;ACKNOWLEDGE_SVC_PROBLEM;<host_name>;<svc_description>;1;<is_send_notification>;<is_persistent_comment>;<comment_author>;<comment_data>\n";

    /**
     * Command template for REMOVE_HOST_ACKNOWLEDGEMENT nagios command
     */
    public static final String REMOVE_HOST_ACKNOWLEDGEMENT = "<bytesize_cmd>;<user_name>;REMOVE_HOST_ACKNOWLEDGEMENT;<host_name>\n";

    /**
     * Command template for ACKNOWLEDGE_SVC_PROBLEM nagios command
     */
    public static final String ACKNOWLEDGE_SVC_PROBLEM = "<bytesize_cmd>;<user_name>;ACKNOWLEDGE_SVC_PROBLEM;<host_name>;<svc_description>;1;<is_send_notification>;<is_persistent_comment>;<comment_author>;<comment_data>\n";

    /**
     * Command template for REMOVE_SVC_ACKNOWLEDGEMENT
     */
    public static final String REMOVE_SVC_ACKNOWLEDGEMENT = "<bytesize_cmd>;<user_name>;REMOVE_SVC_ACKNOWLEDGEMENT;<host_name>;<svc_description>\n";

    // Constants for nagios command templates for 'Notifications' menu.
    /**
     * Command template for ENABLE_HOSTGROUP_HOST_NOTIFICATIONS
     */
    public static final String ENABLE_HOSTGROUP_HOST_NOTIFICATIONS = "<bytesize_cmd>;<user_name>;ENABLE_HOSTGROUP_HOST_NOTIFICATIONS;<hostgroup_name>\n";

    /**
     * Command template for ENABLE_HOSTGROUP_HOST_NOTIFICATIONS
     */
    public static final String DISABLE_HOSTGROUP_HOST_NOTIFICATIONS = "<bytesize_cmd>;<user_name>;DISABLE_HOSTGROUP_HOST_NOTIFICATIONS;<hostgroup_name>\n";

    /**
     * Command template for ENABLE_HOSTGROUP_SVC_NOTIFICATIONS
     */
    public static final String ENABLE_HOSTGROUP_SVC_NOTIFICATIONS = "<bytesize_cmd>;<user_name>;ENABLE_HOSTGROUP_SVC_NOTIFICATIONS;<hostgroup_name>\n";

    /**
     * Command template for DISABLE_HOSTGROUP_SVC_NOTIFICATIONS
     */
    public static final String DISABLE_HOSTGROUP_SVC_NOTIFICATIONS = "<bytesize_cmd>;<user_name>;DISABLE_HOSTGROUP_SVC_NOTIFICATIONS;<hostgroup_name>\n";

    /**
     * Command template for ENABLE_HOST_NOTIFICATIONS
     */
    public static final String ENABLE_HOST_NOTIFICATIONS = "<bytesize_cmd>;<user_name>;ENABLE_HOST_NOTIFICATIONS;<host_name>\n";

    /**
     * Command template for DELAY_HOST_NOTIFICATION
     */
    public static final String DELAY_HOST_NOTIFICATION = "<bytesize_cmd>;<user_name>;DELAY_HOST_NOTIFICATION;<host_name>;<notification_time>\n";

    /**
     * Command template for DELAY_HOST_NOTIFICATION
     */
    public static final String DISABLE_HOST_SVC_NOTIFICATIONS = "<bytesize_cmd>;<user_name>;DISABLE_HOST_SVC_NOTIFICATIONS;<host_name>\n";

    /**
     * Command template for DISABLE_HOST_NOTIFICATIONS
     */
    public static final String DISABLE_HOST_NOTIFICATIONS = "<bytesize_cmd>;<user_name>;DISABLE_HOST_NOTIFICATIONS;<host_name>\n";

    /**
     * Command template for ENABLE_HOST_SVC_NOTIFICATIONS
     */
    public static final String ENABLE_HOST_SVC_NOTIFICATIONS = "<bytesize_cmd>;<user_name>;ENABLE_HOST_SVC_NOTIFICATIONS;<host_name>\n";

    /**
     * Command template for ENABLE_SERVICEGROUP_SVC_NOTIFICATIONS
     */
    public static final String ENABLE_SERVICEGROUP_SVC_NOTIFICATIONS = "<bytesize_cmd>;<user_name>;ENABLE_SERVICEGROUP_SVC_NOTIFICATIONS;<servicegroup_name>\n";

    /**
     * Command template for DISABLE_SERVICEGROUP_SVC_NOTIFICATIONS
     */
    public static final String DISABLE_SERVICEGROUP_SVC_NOTIFICATIONS = "<bytesize_cmd>;<user_name>;DISABLE_SERVICEGROUP_SVC_NOTIFICATIONS;<servicegroup_name>\n";

    /**
     * Command template for DISABLE_SVC_NOTIFICATIONS
     */
    public static final String DISABLE_SVC_NOTIFICATIONS = "<bytesize_cmd>;<user_name>;DISABLE_SVC_NOTIFICATIONS;<host_name>;<svc_description>\n";

    /**
     * Command template for DELAY_SVC_NOTIFICATION
     */
    public static final String DELAY_SVC_NOTIFICATION = "<bytesize_cmd>;<user_name>;DELAY_SVC_NOTIFICATION;<host_name>;<svc_description>;<notification_time>\n";
    /**
     * Command template for ENABLE_SVC_NOTIFICATIONS
     */
    public static final String ENABLE_SVC_NOTIFICATIONS = "<bytesize_cmd>;<user_name>;ENABLE_SVC_NOTIFICATIONS;<host_name>;<svc_description>\n";

    // Constants for nagios command templates for 'Downtime' menu.
    /**
     * Command template for SCHEDULE_HOSTGROUP_HOST_DOWNTIME
     */
    public static final String SCHEDULE_HOSTGROUP_HOST_DOWNTIME = "<bytesize_cmd>;<user_name>;SCHEDULE_HOSTGROUP_HOST_DOWNTIME;<hostgroup_name>;<start_time>;<end_time>;<isfixed>;0;<duration>;<comment_author>;<comment_data>\n";

    /**
     * Command template for SCHEDULE_HOSTGROUP_SVC_DOWNTIME
     */
    public static final String SCHEDULE_HOSTGROUP_SVC_DOWNTIME = "<bytesize_cmd>;<user_name>;SCHEDULE_HOSTGROUP_SVC_DOWNTIME;<hostgroup_name>;<start_time>;<end_time>;<isfixed>;0;<duration>;<comment_author>;<comment_data>\n";

    /**
     * Command template for SCHEDULE_HOST_DOWNTIME
     */
    public static final String SCHEDULE_HOST_DOWNTIME = "<bytesize_cmd>;<user_name>;SCHEDULE_HOST_DOWNTIME;<host_name>;<start_time>;<end_time>;<isfixed>;<triggered_by>;<duration>;<comment_author>;<comment_data>\n";

    /**
     * Command template for SCHEDULE_SERVICEGROUP_SVC_DOWNTIME
     */
    public static final String SCHEDULE_SERVICEGROUP_SVC_DOWNTIME = "<bytesize_cmd>;<user_name>;SCHEDULE_SERVICEGROUP_SVC_DOWNTIME;<servicegroup_name>;<start_time>;<end_time>;<isfixed>;0;<duration>;<comment_author>;<comment_data>\n";

    /**
     * Command template for SCHEDULE_SVC_DOWNTIME
     */
    public static final String SCHEDULE_SVC_DOWNTIME = "<bytesize_cmd>;<user_name>;SCHEDULE_SVC_DOWNTIME;<host_name>;<svc_description>;<start_time>;<end_time>;<isfixed>;<triggered_by>;<duration>;<comment_author>;<comment_data>\n";

    // Constants for nagios command templates for 'Configuration/Settings' menu.
    /**
     * Command template for ENABLE_HOSTGROUP_SVC_CHECKS
     */
    public static final String ENABLE_HOSTGROUP_SVC_CHECKS = "<bytesize_cmd>;<user_name>;ENABLE_HOSTGROUP_SVC_CHECKS;<hostgroup_name>\n";

    /**
     * Command template for ENABLE_HOSTGROUP_HOST_CHECKS
     */
    public static final String ENABLE_HOSTGROUP_HOST_CHECKS = "<bytesize_cmd>;<user_name>;ENABLE_HOSTGROUP_HOST_CHECKS;<hostgroup_name>\n";

    /**
     * Command template for DISABLE_HOSTGROUP_SVC_CHECKS
     */
    public static final String DISABLE_HOSTGROUP_SVC_CHECKS = "<bytesize_cmd>;<user_name>;DISABLE_HOSTGROUP_SVC_CHECKS;<hostgroup_name>\n";

    /**
     * Command template for DISABLE_HOSTGROUP_HOST_CHECKS
     */
    public static final String DISABLE_HOSTGROUP_HOST_CHECKS = "<bytesize_cmd>;<user_name>;DISABLE_HOSTGROUP_HOST_CHECKS;<hostgroup_name>\n";

    /**
     * Command template for DISABLE_HOST_CHECK
     */
    public static final String DISABLE_HOST_CHECK = "<bytesize_cmd>;<user_name>;DISABLE_HOST_CHECK;<host_name>\n";
    /**
     * Command template for ENABLE_HOST_CHECK
     */
    public static final String ENABLE_HOST_CHECK = "<bytesize_cmd>;<user_name>;ENABLE_HOST_CHECK;<host_name>\n";
    /**
     * Command template for ENABLE_PASSIVE_HOST_CHECKS
     */
    public static final String ENABLE_PASSIVE_HOST_CHECKS = "<bytesize_cmd>;<user_name>;ENABLE_PASSIVE_HOST_CHECKS;<host_name>\n";
    /**
     * Command template for DISABLE_PASSIVE_HOST_CHECKS
     */
    public static final String DISABLE_PASSIVE_HOST_CHECKS = "<bytesize_cmd>;<user_name>;DISABLE_PASSIVE_HOST_CHECKS;<host_name>\n";
    /**
     * Command template for DISABLE_HOST_SVC_CHECKS
     */
    public static final String DISABLE_HOST_SVC_CHECKS = "<bytesize_cmd>;<user_name>;DISABLE_HOST_SVC_CHECKS;<host_name>\n";
    /**
     * Command template for ENABLE_HOST_SVC_CHECKS
     */
    public static final String ENABLE_HOST_SVC_CHECKS = "<bytesize_cmd>;<user_name>;ENABLE_HOST_SVC_CHECKS;<host_name>\n";

    // /**
    // * Command template for START_OBSESSING_OVER_HOST
    // */
    // public static final String START_OBSESSING_OVER_HOST =
    // "<bytesize_cmd>;<user_name>;START_OBSESSING_OVER_HOST;<host_name>\n";
    // /**
    // * Command template for STOP_OBSESSING_OVER_HOST
    // */
    // public static final String STOP_OBSESSING_OVER_HOST =
    // "<bytesize_cmd>;<user_name>;STOP_OBSESSING_OVER_HOST;<host_name>\n";

    /**
     * Command template for ENABLE_HOST_FLAP_DETECTION
     */
    public static final String ENABLE_HOST_FLAP_DETECTION = "<bytesize_cmd>;<user_name>;ENABLE_HOST_FLAP_DETECTION;<host_name>\n";
    /**
     * Command template for DISABLE_HOST_FLAP_DETECTION
     */
    public static final String DISABLE_HOST_FLAP_DETECTION = "<bytesize_cmd>;<user_name>;DISABLE_HOST_FLAP_DETECTION;<host_name>\n";
    /**
     * Command template for ENABLE_SERVICEGROUP_SVC_CHECKS
     */
    public static final String ENABLE_SERVICEGROUP_SVC_CHECKS = "<bytesize_cmd>;<user_name>;ENABLE_SERVICEGROUP_SVC_CHECKS;<servicegroup_name>\n";
    /**
     * Command template for DISABLE_SERVICEGROUP_SVC_CHECKS
     */
    public static final String DISABLE_SERVICEGROUP_SVC_CHECKS = "<bytesize_cmd>;<user_name>;DISABLE_SERVICEGROUP_SVC_CHECKS;<servicegroup_name>\n";
    /**
     * Command template for DISABLE_SVC_CHECK
     */
    public static final String DISABLE_SVC_CHECK = "<bytesize_cmd>;<user_name>;DISABLE_SVC_CHECK;<host_name>;<svc_description>\n";
    /**
     * Command template for ENABLE_SVC_CHECK
     */
    public static final String ENABLE_SVC_CHECK = "<bytesize_cmd>;<user_name>;ENABLE_SVC_CHECK;<host_name>;<svc_description>\n";
    /**
     * Command template for DISABLE_PASSIVE_SVC_CHECKS
     */
    public static final String DISABLE_PASSIVE_SVC_CHECKS = "<bytesize_cmd>;<user_name>;DISABLE_PASSIVE_SVC_CHECKS;<host_name>;<svc_description>\n";
    /**
     * Command template for ENABLE_PASSIVE_SVC_CHECKS
     */
    public static final String ENABLE_PASSIVE_SVC_CHECKS = "<bytesize_cmd>;<user_name>;ENABLE_PASSIVE_SVC_CHECKS;<host_name>;<svc_description>\n";
    /**
     * Command template for DISABLE_SVC_FLAP_DETECTION
     */
    public static final String DISABLE_SVC_FLAP_DETECTION = "<bytesize_cmd>;<user_name>;DISABLE_SVC_FLAP_DETECTION;<host_name>;<svc_description>\n";
    /**
     * Command template for ENABLE_SVC_FLAP_DETECTION
     */
    public static final String ENABLE_SVC_FLAP_DETECTION = "<bytesize_cmd>;<user_name>;ENABLE_SVC_FLAP_DETECTION;<host_name>;<svc_description>\n";

    // Constants for nagios command templates for 'Event Handler' menu.
    /**
     * Command template for ENABLE_HOST_EVENT_HANDLER
     */
    public static final String ENABLE_HOST_EVENT_HANDLER = "<bytesize_cmd>;<user_name>;ENABLE_HOST_EVENT_HANDLER;<host_name>\n";
    /**
     * Command template for DISABLE_HOST_EVENT_HANDLER
     */
    public static final String DISABLE_HOST_EVENT_HANDLER = "<bytesize_cmd>;<user_name>;DISABLE_HOST_EVENT_HANDLER;<host_name>\n";
    /**
     * Command template for DISABLE_SVC_EVENT_HANDLER
     */
    public static final String DISABLE_SVC_EVENT_HANDLER = "<bytesize_cmd>;<user_name>;DISABLE_SVC_EVENT_HANDLER;<host_name>;<svc_description>\n";
    /**
     * Command template for ENABLE_SVC_EVENT_HANDLER
     */
    public static final String ENABLE_SVC_EVENT_HANDLER = "<bytesize_cmd>;<user_name>;ENABLE_SVC_EVENT_HANDLER;<host_name>;<svc_description>\n";

    // Constants for nagios command templates for 'Submit Checks' menu.
    /**
     * Command template for SCHEDULE_HOST_CHECK
     */
    public static final String SCHEDULE_HOST_CHECK = "<bytesize_cmd>;<user_name>;SCHEDULE_HOST_CHECK;<host_name>;<start_time>\n";
    /**
     * Command template for SCHEDULE_FORCED_HOST_CHECK
     */
    public static final String SCHEDULE_FORCED_HOST_CHECK = "<bytesize_cmd>;<user_name>;SCHEDULE_FORCED_HOST_CHECK;<host_name>;<start_time>\n";
    /**
     * Command template for SCHEDULE_HOST_SVC_CHECKS
     */
    public static final String SCHEDULE_HOST_SVC_CHECKS = "<bytesize_cmd>;<user_name>;SCHEDULE_HOST_SVC_CHECKS;<host_name>;<scheduled_time>\n";
    /**
     * Command template for SCHEDULE_FORCED_HOST_SVC_CHECKS
     */
    public static final String SCHEDULE_FORCED_HOST_SVC_CHECKS = "<bytesize_cmd>;<user_name>;SCHEDULE_FORCED_HOST_SVC_CHECKS;<host_name>;<scheduled_time>\n";
    /**
     * Command template for PROCESS_HOST_CHECK_RESULT
     */
    public static final String PROCESS_HOST_CHECK_RESULT = "<bytesize_cmd>;<user_name>;PROCESS_HOST_CHECK_RESULT;<host_name>;<plugin_state>;<plugin_output>|<Perf_data>\n";
    /**
     * Command template for PROCESS_SERVICE_CHECK_RESULT
     */
    public static final String PROCESS_SERVICE_CHECK_RESULT = "<bytesize_cmd>;<user_name>;PROCESS_SERVICE_CHECK_RESULT;<host_name>;<svc_description>;<plugin_state>;<plugin_output>|<Perf_data>\n";
    /**
     * Command template for SCHEDULE_SVC_CHECK
     */
    public static final String SCHEDULE_SVC_CHECK = "<bytesize_cmd>;<user_name>;SCHEDULE_SVC_CHECK;<host_name>;<svc_description>;<start_time>\n";
    /**
     * Command template for SCHEDULE_FORCED_SVC_CHECK
     */
    public static final String SCHEDULE_FORCED_SVC_CHECK = "<bytesize_cmd>;<user_name>;SCHEDULE_FORCED_SVC_CHECK;<host_name>;<svc_description>;<start_time>\n";

    /**
     * Command template for SCHEDULE_AND_PROPAGATE_TRIGGERED_HOST_DOWNTIME
     */
    public static final String SCHEDULE_AND_PROPAGATE_TRIGGERED_HOST_DOWNTIME = "<bytesize_cmd>;<user_name>;SCHEDULE_AND_PROPAGATE_TRIGGERED_HOST_DOWNTIME;<host_name>;<start_time>;<end_time>;<isfixed>;<triggered_by>;<duration>;<comment_author>;<comment_data>\n";

    /**
     * Command template for SCHEDULE_AND_PROPAGATE_HOST_DOWNTIME
     */
    public static final String SCHEDULE_AND_PROPAGATE_HOST_DOWNTIME = "<bytesize_cmd>;<user_name>;SCHEDULE_AND_PROPAGATE_HOST_DOWNTIME;<host_name>;<start_time>;<end_time>;<isfixed>;<triggered_by>;<duration>;<comment_author>;<comment_data>\n";

    /**
     * Command template for SCHEDULE_AND_PROPAGATE_HOST_DOWNTIME
     */
    public static final String ADD_HOST_COMMENT = "<bytesize_cmd>;<user_name>;ADD_HOST_COMMENT;<host_name>;<persistent>;<author>;<comment>\n";

    /**
     * Command template for SCHEDULE_AND_PROPAGATE_HOST_DOWNTIME
     */
    public static final String ADD_SVC_COMMENT = "<bytesize_cmd>;<user_name>;ADD_SVC_COMMENT;<host_name>;<service_description>;<persistent>;<author>;<comment>\n";

    /**
     * Command template for SCHEDULE_AND_PROPAGATE_HOST_DOWNTIME
     */
    public static final String DEL_HOST_COMMENT = "<bytesize_cmd>;<user_name>;DEL_HOST_COMMENT;<comment_id>\n";

    /**
     * Command template for SCHEDULE_AND_PROPAGATE_HOST_DOWNTIME
     */
    public static final String DEL_SVC_COMMENT = "<bytesize_cmd>;<user_name>;DEL_SVC_COMMENT;<comment_id>\n";
}
