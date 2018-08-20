package com.groundworkopensource.portal.statusviewer.common;

/**
 * This class defines all the constants used in Nagios Monitoring Statistics
 * Portlet
 * 
 * @author shivangi_walvekar
 * 
 */
public class NagiosStatisticsConstants {
    /**
     * Protected Constructor - Rationale: Instantiating utility classes does not
     * make sense. Hence the constructors should either be private or (if you
     * want to allow sub-classing) protected. <br>
     * 
     * Refer to "HideUtilityClassConstructor" section in
     * http://checkstyle.sourceforge.net/config_design.html.
     */
    protected NagiosStatisticsConstants() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }

    /**
     * String constant for isPassiveChecksEnabled property value
     */
    public static final String IS_PASSIVECHECKS_ENABLED_PROPERTY = "isPassiveChecksEnabled";

    /**
     * String constant for isAcceptPassiveChecks property value for Services.
     * Maps to isPassiveChecksEnabled property of Host.
     */
    public static final String IS_ACCEPT_PASSIVECHECKS_PROPERTY = "isAcceptPassiveChecks";

    /**
     * String constant for isChecksEnabled property value
     */
    public static final String IS_ACTIVECHECKS_ENABLED_PROPERTY = "isChecksEnabled";

    /**
     * String constant for isNotificationsEnabled property value
     */
    public static final String IS_NOTIFICATIONS_ENABLED_PROPERTY = "isNotificationsEnabled";

    /**
     * String constant for isEventHandlersEnabled property value
     */
    public static final String IS_EVENT_HANDLERS_ENABLED_PROPERTY = "isEventHandlersEnabled";

    /**
     * String constant for isObsessedOverHost property value
     */
    public static final String IS_OBSESSED_OVER_HOST_PROPERTY = "isObsessOverHost";

    /**
     * String constant for isObsessOverService property value
     */
    public static final String IS_OBSESSED_OVER_SERVICE_PROPERTY = "isObsessOverService";

    /**
     * String constant for isFlapDetectionEnabled property value
     */
    public static final String IS_FLAP_DETECTION_ENABLED_PROPERTY = "isFlapDetectionEnabled";

    /**
     * String Constant for "nagiosStatisticsHandler"
     */
    public static final String NAGIOS_STATISTICS_HANDLER = "nagiosStatisticsHandler";

    /**
     * String constant for the component with id
     * 'nagiosPortlet_linkPassiveChecksHosts'
     */
    public static final String COMPONENT_CMD_PASSIVE_CHECKS_HOSTS = "nagiosPortlet_linkPassiveChecksHosts";

    /**
     * String constant for the component with id
     * 'stackedNagiosPortlet_linkPassiveChecksHosts'
     */
    public static final String COMPONENT_CMD_PASSIVE_CHECKS_HOSTS_STACKED = "stackedNagiosPortlet_linkPassiveChecksHosts";

    /**
     * String constant for the component with id
     * 'nagiosPortlet_linkActiveChecksHosts'
     */
    public static final String COMPONENT_CMD_ACTIVE_CHECKS_HOSTS = "nagiosPortlet_linkActiveChecksHosts";

    /**
     * String constant for the component with id
     * 'stackedNagiosPortlet_linkActiveChecksHosts'
     */
    public static final String COMPONENT_CMD_ACTIVE_CHECKS_HOSTS_STACKED = "stackedNagiosPortlet_linkActiveChecksHosts";

    /**
     * String constant for the component with id
     * 'nagiosPortlet_linkNotificationsHosts'
     */
    public static final String COMPONENT_CMD_NOTIFICATIONS_HOSTS = "nagiosPortlet_linkNotificationsHosts";

    /**
     * String constant for the component with id
     * 'stackedNagiosPortlet_linkNotificationsHosts'
     */
    public static final String COMPONENT_CMD_NOTIFICATIONS_HOSTS_STACKED = "stackedNagiosPortlet_linkNotificationsHosts";

    /**
     * String constant for the component with id
     * 'nagiosPortlet_linkFlapDetectionHosts'
     */
    public static final String COMPONENT_CMD_FLAP_DETECTION_HOSTS = "nagiosPortlet_linkFlapDetectionHosts";

    /**
     * String constant for the component with id
     * 'stackedNagiosPortlet_linkFlapDetectionHosts'
     */
    public static final String COMPONENT_CMD_FLAP_DETECTION_HOSTS_STACKED = "stackedNagiosPortlet_linkFlapDetectionHosts";

    /**
     * String constant for the component with id
     * 'nagiosPortlet_linkEventHandlersHosts'
     */
    public static final String COMPONENT_CMD_EVENT_HANDLERS_HOSTS = "nagiosPortlet_linkEventHandlersHosts";

    /**
     * String constant for the component with id
     * 'stackedNagiosPortlet_linkEventHandlersHosts'
     */
    public static final String COMPONENT_CMD_EVENT_HANDLERS_HOSTS_STACKED = "stackedNagiosPortlet_linkEventHandlersHosts";

    /**
     * String constant for the component with id
     * 'nagiosPortlet_linkActiveChecksServices'
     */
    public static final String COMPONENT_CMD_ACTIVE_CHECKS_SERVICES = "nagiosPortlet_linkActiveChecksServices";

    /**
     * String constant for the component with id
     * 'stackedNagiosPortlet_linkActiveChecksServices'
     */
    public static final String COMPONENT_CMD_ACTIVE_CHECKS_SERVICES_STACKED = "stackedNagiosPortlet_linkActiveChecksServices";

    /**
     * String constant for the component with id
     * 'nagiosPortlet_linPassiveChecksServices'
     */
    public static final String COMPONENT_CMD_PASSIVE_CHECKS_SERVICES = "nagiosPortlet_linkPassiveChecksServices";

    /**
     * String constant for the component with id
     * 'stackedNagiosPortlet_linPassiveChecksServices'
     */
    public static final String COMPONENT_CMD_PASSIVE_CHECKS_SERVICES_STACKED = "stackedNagiosPortlet_linkPassiveChecksServices";

    /**
     * String constant for the component with id
     * 'nagiosPortlet_linkNotificationsServices'
     */
    public static final String COMPONENT_CMD_NOTIFICATIONS_SERIVCES = "nagiosPortlet_linkNotificationsServices";

    /**
     * String constant for the component with id
     * 'stackedNagiosPortlet_linkNotificationsServices'
     */
    public static final String COMPONENT_CMD_NOTIFICATIONS_SERIVCES_STACKED = "stackedNagiosPortlet_linkNotificationsServices";

    /**
     * String constant for the component with id
     * 'nagiosPortlet_linkFlapDetectionServices'
     */
    public static final String COMPONENT_CMD_FLAP_DETECTION_SERIVCES = "nagiosPortlet_linkFlapDetectionServices";

    /**
     * String constant for the component with id
     * 'stackedNagiosPortlet_linkFlapDetectionServices'
     */
    public static final String COMPONENT_CMD_FLAP_DETECTION_SERIVCES_STACKED = "stackedNagiosPortlet_linkFlapDetectionServices";

    /**
     * String constant for the component with id
     * 'nagiosPortlet_linkEventHandlersServices'
     */
    public static final String COMPONENT_CMD_EVENT_HANDLERS_SERIVCES = "nagiosPortlet_linkEventHandlersServices";

    /**
     * String constant for the component with id
     * 'stackedNagiosPortlet_linkEventHandlersServices'
     */
    public static final String COMPONENT_CMD_EVENT_HANDLERS_SERIVCES_STACKED = "stackedNagiosPortlet_linkEventHandlersServices";

    /**
     * Constant String NAGIOS_STATISTICS_MANAGED_BEAN
     */
    public static final String NAGIOS_STATISTICS_MANAGED_BEAN = "nagiosStatisticsBean";

    /**
     * Constant String NAGIOS_STATISTICS_PORTLET_TITLE
     */
    public static final String NAGIOS_STATISTICS_PORTLET_TITLE = "Nagios Monitoring Statistics";

    /**
     * Constant String for image path "/images/nagios-host-green.gif"
     */
    public static final String IMAGE_PATH_HOST_GREEN = "/images/host-green.gif";

    /**
     * Constant String for image path "/images/nagios-host-green.gif"
     */
    public static final String IMAGE_PATH_HOST_RED = "/images/host-red.gif";

}
