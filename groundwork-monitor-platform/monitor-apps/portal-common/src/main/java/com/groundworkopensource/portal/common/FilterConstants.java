package com.groundworkopensource.portal.common;

/**
 * This class defines all the constants used for creating/applying filters.
 * 
 * @author shivangi_walvekar
 * 
 */
public class FilterConstants {
    /**
     * Protected Constructor - Rationale: Instantiating utility classes does not
     * make sense. Hence the constructors should either be private or (if you
     * want to allow sub-classing) protected. <br>
     * 
     * Refer to "HideUtilityClassConstructor" section in
     * http://checkstyle.sourceforge.net/config_design.html.
     */
    protected FilterConstants() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }

    /**
     * Filter criterion for Boolean property propertyValues.valueBoolean
     */
    public static final String SERVICE_STATUS_PROPERTYVALUES_VALUEBOOLEAN = "propertyValues.valueBoolean";
    /**
     * Filter criterion for Long property propertyValues.valueLong
     */
    public static final String SERVICE_STATUS_PROPERTYVALUES_VALUELONG = "propertyValues.valueLong";
    /**
     * Filter criterion for String property propertyValues.name
     */
    public static final String SERVICE_STATUS_PROPERTYVALUES_NAME = "propertyValues.name";
    /**
     * Filter monitor status String property for Host.
     */
    public static final String HOST_STATUS_HOST_MONITOR_STATUS_NAME = "hostStatus.hostMonitorStatus.name";
    /**
     * Filter monitor status String property for service.
     */
    public static final String MONITOR_STATUS_NAME = "monitorStatus.name";
    /**
     * Filter criterion for String property hostStatus.propertyValues.name
     */
    public static final String HOST_STATUS_PROPERTYVALUES_NAME = "hostStatus.propertyValues.name";

    /**
     * Constant defining query string for property value of host group
     */
    public static final String HOSTGROUP_PROPERTYVALUES_NAME = "hosts.hostStatus.propertyValues.name";

    /**
     * Constant defining long property for host group
     */
    public static final String HOSTGROUP_STATUS_PROPERTYVALUES_VALUELONG = "hosts.hostStatus.propertyValues.valueLong";
    /**
     * Constant defining Date property for host group
     */
    public static final String HOSTGROUP_STATUS_PROPERTYVALUES_VALUE_DATE = "hosts.hostStatus.propertyValues.valueDate";

    /**
     * Constant defining boolean property for host group
     */
    public static final String HOSTGROUP_STATUS_PROPERTYVALUES_VALUEBOOLEAN = "hosts.hostStatus.propertyValues.valueBoolean";

    /**
     * Filter criterion for Boolean property hostStatus.propertyValues.name
     */
    public static final String HOST_STATUS_PROPERTYVALUES_VALUEBOOLEAN = "hostStatus.propertyValues.valueBoolean";
    /**
     * Filter criterion for Long property hostStatus.propertyValues.name
     */
    public static final String HOST_STATUS_PROPERTYVALUES_VALUELONG = "hostStatus.propertyValues.valueLong";

    /**
     * Filter criterion for Date property hostStatus.propertyValues.name
     */
    public static final String HOST_STATUS_PROPERTY_VALUES_VALUE_DATE = "hostStatus.propertyValues.valueDate";
    /**
     * Service group Constant
     */
    public static final String SERVICE_GROUP = "SERVICE_GROUP";
    /**
     * category entity name String property
     */
    public static final String CATEGORY_ENTITIES_ENTITY_TYPE_NAME = "categoryEntities.entityType.name";
    /**
     * category entity object id String property
     */
    public static final String CATEGORY_ENTITIES_OBJECT_I_D = "categoryEntities.objectID";

    /**
     * SERVICE_STATUS_ID for fetching Services under service group
     */
    public static final String SERVICE_STATUS_ID = "serviceStatusId";

    /**
     * Service group Constant
     */
    public static final String HOST_GROUP_ID = "hostGroups.hostGroupId";

    /**
     * Host ID identifier
     */
    public static final String HOST_HOSTID = "host.hostId";

    /**
     * Host ID identifier
     */
    public static final String HOST_HOSTNAME = "host.hostName";

    /**
     * String property for get events which are closed or Open etc.
     */
    public static final String OPERATION_STATUS_NAME = "operationStatus.name";

    /**
     * get host group monitor filter string property.
     */
    public static final String HOSTGROUP_MONITORSTATUS_NAME = "hosts.hostStatus.hostMonitorStatus.name";

    /**
     * 
     */
    public static final String ACTION_PARAM_USER_NAME = "UserName";
    /**
     * 
     */
    public static final String ACTION_PARAM_SEND_NOTIFY = "SendNotification";
    /**
     * 
     */
    public static final String PROP_NAGIOS_SEND_NOTIFY = "nagios_send_notification";
    /**
     * 
     */
    public static final String ACTION_PARAM_PERSIST_COMMENT = "PersistentComment";
    /**
     * 
     */
    public static final String PROP_NAGIOS_PERSIST_COMMENT = "nagios_persistent_comment";

    /**
     * For SNMPTRAP, SYSLOG params
     */
    public static final String ACTION_PARAM_NSCA_HOST = "nsca_host";

    /**
     * User name contant
     */
    public static final String ACTION_PARAM_USER = "user";
    /**
     * For SNMPTRAP, SYSLOG params
     */
    public static final String ACTION_PARAM_NSCA_COMMENT = "comment";
    /**
     * For SNMPTRAP, SYSLOG params
     */
    public static final String ACTION_PARAM_HOST = "host";

    /**
     * For SNMPTRAP, SYSLOG params
     */
    public static final String ACTION_PARAM_SERVICE = "service";
    /**
     * For SNMPTRAP, SYSLOG params
     */
    public static final String ACTION_PARAM_STATE = "state";
    /**
     * For SNMPTRAP, SYSLOG params
     */
    public static final String ACTION_PARAM_COMMENT = "Comment";
    /**
     * For SNMPTRAP, SYSLOG params
     */
    public static final String ACTION_PARAM_VALUE_COMMENT_PREFIX = "Acknowledged from console at ";
    /**
     * For SNMPTRAP, SYSLOG params
     */
    public static final String DEFAULT_NSCA_HOST = "localhost";
    /**
     * For SNMPTRAP, SYSLOG params
     */
    public static final String SUBMIT_PASSIVE_RESET_COMMENT = "Manual_reset_by_";
    /**
     * For SNMPTRAP, SYSLOG params
     */
    public static final String SERVICE_SNMPTRAP_LAST = "snmptraps_last";
    /**
     * For SNMPTRAP, SYSLOG params
     */
    public static final String SERVICE_SYSLOG_LAST = "syslog_last";
    /**
     * For SNMPTRAP, SYSLOG params
     */
    public static final String DEFAULT_NSCA_STATE = "0";
    /**
     * For SNMPTRAP, SYSLOG params
     */
    public static final String APP_TYPE_SNMPTRAP = "SNMPTRAP";
    /**
     * For SNMPTRAP, SYSLOG params
     */
    public static final String ACTION_PARAM_LOG_MESS_IDS = "LogMessageIds";

    /**
     * Event String parameter for host group
     */
    public static final String DEVICE_HOSTS_HOST_GROUPS_HOST_GROUP_ID = "device.hosts.hostGroups.hostGroupId";

    /**
     * String property for get service under service group and service
     */
    public static final String SERVICE_STATUS_SERVICE_STATUS_ID = "serviceStatus.serviceStatusId";
    /**
     * Integer property to get event for host
     */
    public static final String DEVICE_HOSTS_HOST_ID = "device.hosts.hostId";

    /**
     * String property for host group ID
     */
    public static final String SERVICES_BY_HOST_GROUP_ID_STRING_PROPERTY = "host.hostGroups.hostGroupId";

    /**
     * String property for host group Name
     */
    public static final String SERVICES_BY_HOST_GROUP_NAME_STRING_PROPERTY = "host.hostGroups.name";

    /**
     * String property to get host name.
     */
    public static final String HOST_NAME = "hosts.hostName";

    /**
     * Filter property to get service description
     */
    public static final String SERVICE_DESC = "serviceDescription";

    /**
     * Constant defining isAcknowledged property field of Host - Status
     */
    public static final String IS_ACKNOWLEDGED = "isAcknowledged";

    /**
     * Constant defining isProblemAcknowledged property field of Service -
     * Status
     */
    public static final String IS_PROBLEM_ACKNOWLEDGED = "isProblemAcknowledged";

    /**
     * Host ID Constant
     */
    public static final String HOST_ID = "hostId";

    /**
     * Host group Constant
     */
    public static final String HOST_GROUP_NAME = "hostGroups.name";

    /**
     * Query String to retrieve events belonging to a service with given monitor
     * status
     */
    public static final String SERVICE_STATUS_MONITOR_STATUS_NAME = "serviceStatus.monitorStatus.name";

    /**
     * Query String to retrieve events belonging to a service with given
     * property value name
     */
    public static final String EVENT_SERVICE_STATUS_PROPERTYVALUES_NAME = "serviceStatus.propertyValues.name";
    /**
     * SERVICE_STATUS_LAST_STATE_CHANGE
     */
    public static final String SERVICE_STATUS_LAST_STATE_CHANGE = "serviceStatus.lastStateChange";

    /**
     * Query String to retrieve events belonging to a service with given boolean
     * property value
     */
    public static final String EVENT_SERVICE_STATUS_PROPERTYVALUES_VALUEBOOLEAN = "serviceStatus.propertyValues.valueBoolean";

    /**
     * Query String to retrieve events belonging to a service with given long
     * property value
     */
    public static final String EVENT_SERVICE_STATUS_PROPERTYVALUES_VALUELONG = "serviceStatus.propertyValues.valueLong";

    /**
     * Query String to retrieve events belonging to a host
     */
    public static final String EVENT_DEVICE_HOSTS_HOSTSTATUS_MONITORSTATUS = "device.hosts.hostStatus.hostMonitorStatus.name";

    /**
     * Query String to retrieve events belonging to a host with given property
     * value name
     */
    public static final String EVENT_DEVICE_HOSTS_HOSTSTATUS_PROPERTYVALUES_NAME = "device.hosts.hostStatus.propertyValues.name";

    /**
     * Query String to retrieve events belonging to a host with given long
     * property value
     */
    public static final String EVENT_DEVICE_HOSTS_HOSTSTATUS_PROPERTYVALUES_VALUELONG = "device.hosts.hostStatus.propertyValues.valueLong";
    /**
     * Query String to retrieve events belonging to a host with given Date
     * property value
     */
    public static final String EVENT_DEVICE_HOSTS_HOSTSTATUS_PROPERTYVALUES_VALUEDATE = "device.hosts.hostStatus.propertyValues.valueDate";
    /**
     * Query String to retrieve events belonging to a host with given boolean
     * property value
     */
    public static final String EVENT_DEVICE_HOSTS_HOSTSTATUS_PROPERTYVALUES_VALUEBOOLEAN = "device.hosts.hostStatus.propertyValues.valueBoolean";
    /**
     * String property to get all event under host group
     */
    public static final String DEVICE_HOSTS_HOST_GROUPS_NAME = "device.hosts.hostGroups.name";

    /**
     * String property to get all host under host group
     */
    public static final String HOST_GROUPS_NAME = "hostGroups.name";

    /**
     * String property to get all event under host
     */
    public static final String DEVICE_HOSTS_HOST_NAME = "device.hosts.hostName";
    /**
     * Entity type name String constant
     */
    public static final String ENTITY_TYPE_NAME = "entityType.name";

    /**
     * host status id String property
     */
    public static final String HOST_HOST_ID = "host.hostId";

    /**
     * ENTITY_TYPE_ENTITY_TYPE_ID
     */
    public static final String ENTITY_TYPE_ENTITY_TYPE_ID = "entityType.entityTypeId";

    /**
     * SERVICE_MONITOR_STATUS
     */
    public static final String SERVICE_MONITOR_STATUS = "monitorStatus";

    /**
     * HOST_MONITOR_STATUS
     */
    public static final String HOST_MONITOR_STATUS = "hostStatus.hostMonitorStatus";

}
