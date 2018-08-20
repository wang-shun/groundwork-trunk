/*
 * Collage - The ultimate data integration framework. Copyright (C) 2004-2010
 * GroundWork Open Source Inc. support@gwos.com
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published by the
 * Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 */

package com.groundwork.collage;

import com.groundwork.collage.exception.CollageException;
import com.groundwork.collage.model.ApplicationType;
import com.groundwork.collage.model.Category;
import com.groundwork.collage.model.Device;
import com.groundwork.collage.model.EntityType;
import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.HostGroup;
import com.groundwork.collage.model.LogMessage;
import com.groundwork.collage.model.MonitorStatus;
import com.groundwork.collage.model.PropertyExtensible;
import com.groundwork.collage.model.PropertyType;
import com.groundwork.collage.model.ServiceStatus;
import com.groundwork.collage.model.Severity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * 
 * Adds or updates data in the Collage database. This is designed to be accessed
 * from the Collage feeders (collector/normalizers) to create or update state
 * information.
 * 
 * @author <a href=String mailto:rruttimann@itgroundwork.comString > Roger
 *         Ruttimann</a>
 * @version $Id: CollageAdminInfrastructure.java,v 1.19 2006/01/24 21:56:17
 *          rogerrut Exp $
 * 
 */
public interface CollageAdminInfrastructure {
    // entity properties
    public static final String PROP_HOST_NAME = "Host";
    public static final String PROP_HOST_ID = "HostId";
    public static final String PROP_SERVICE_DESC = "ServiceDescription";
    public static final String PROP_SERVICE_ID = "ServiceId";

    public static final String PROP_HOSTGROUP_ID = "HostGroupId";
    public static final String PROP_HOSTGROUP_NAME = "HostGroup";

    public static final String PROP_HOSTGROUP_ALIAS = "Alias";
    public static final String PROP_AGENT_ID = "AgentId";
    public static final String PROP_HOSTGROUP_DESCRIPTION = "Description";

    public static final String PROP_SERVICEGROUP_ID = "ServiceGroupId";
    public static final String PROP_SERVICEGROUP_NAME = "ServiceGroup";
    public static final String PROP_SERVICEGROUP_DESCRIPTION = "Description";

    public static final String PROP_DEVICE_IDENTIFICATION = "Device";
    public static final String PROP_DEVICE_ID = "DeviceId";

    public static final String PROP_MONITOR_SERVER = "MonitorServerName";

    public static final String PROP_CONSOLIDATION_NAME = "CriteriaName";
    public static final String PROP_CONSOLIDATION_ID = "CriteriaId";
    public static final String PROP_CRITERIA = "Criteria";

    public static final String PROP_LOG_MESSAGE_ID = "LogMessageId";
    public static final String PROP_OPERATION_STATUS = "OperationStatus";

    public static final String PROP_MONITOR_STATUS = "MonitorStatus";
    public static final String PROP_DESCRIPTION = "Description";
    public static final String PROP_DISPLAY_NAME = "DisplayName";
    public static final String PROP_APPLICATION_TYPE_NAME = "ApplicationType";
    public static final String PROP_SEVERITY = "Severity";
    public static final String PROP_TEXT_MESSAGE = "TextMessage";
    public static final String PROP_SCHEDULED_DOWNTIME_DEPTH = "ScheduledDowntimeDepth";

    public static final String PROP_STATE_TYPE = "StateType";
    public static final String PROP_CHECK_TYPE = "CheckType";
    public static final String PROP_LAST_HARD_STATE = "LastHardState";
    public static final String PROP_LAST_STATE_CHANGE = "LastStateChange";
    public static final String PROP_SERVICE_DOMAIN = "ServiceDomain";
    public static final String PROP_SERVICE_METRIC_TYPE = "ServiceMetricType";
    public static final String PROP_SERVICE_LAST_CHECK_TIME = "ServiceLastCheckTime";
    public static final String PROP_SERVICE_NEXT_CHECK_TIME = "ServiceNextCheckTime";
    public static final String PROP_SERVICE_LAST_STATE_CHANGE = "LastStateChange";
    public static final String PROP_IS_ACKNOWLEDGED = "isAcknowledged";
    public static final String PROP_IS_PROB_ACKNOWLEDGED = "isProblemAcknowledged";
    public static final String PROP_ACKNOWLEDGED_BY = "AcknowledgedBy";
    public static final String PROP_ACKNOWLEDGE_COMMENT = "AcknowledgeComment";
    public static final String PROP_PERFORMANCE_DATA = "PerformanceData";
    public static final String PROP_LAST_PLUGIN_OUTPUT = "LastPluginOutput";

    /**
     * This method records the state of a service at given point in time; the
     * name of the service, as well as the lastCheckTime, is contained in the
     * 'properties' argument passed; for more on the format of the map, read the
     * parameter docs below.
     *
     * @param monitorServerName
     *            the unique name of the monitor that is performing the check
     *
     * @param applicationType
     *            the unique name/code for the type of application that is being
     *            monitored, currently one of 'NAGIOS', 'SYSLOG', or
     *            'JMX_SAMPLE'
     *
     * @param hostName
     *            the name of the logical host or application that is being
     *            monitored
     *
     * @param deviceIdent
     *            the unique name/IP/MAC of the physical device on which the
     *            host resides
     *
     * @param properties
     *            map containing String-PrimitiveObject pairs where:
     *
     *            <ul>
     *            <li>
     *            The values are java primitive wrapper classes such as String,
     *            Date, Boolean, Integer, Long and Double, as defined in the
     *            corresponding {@link PropertyType} object</li>
     *            <li>
     *            <p>
     *            The keys of the map are Strings describing some
     *            property/attribute/metric for the ServiceStatus
     *            (PropertyExtensible entity) that is being monitored (e.g.
     *            ServiceDescription, LastStateChange, etc...);
     *            </p>
     *            <p>
     *            These strings are defined (pre-configured) in the
     *            <code>Metadata</code> singleton loaded from the current
     *            implementation of the {@link CollageAccessor} factory
     *            </p>
     *            <p>
     *            In the current implementation, the metadata is stored in a
     *            database and is accessible via the {@link ApplicationType} and
     *            its associated {@link EntityType} and {@link PropertyType}
     *            objects, but this may change in the future, as the metadata
     *            could easily be stored elsewhere, such as an xml file, LDAP
     *            directory, windows application, remote web service, etc...
     *            </p>
     *            </li>
     *            </ul>
     *
     * @see PropertyExtensible
     * @see ApplicationType
     * @see EntityType
     * @see PropertyType
     */
    ServiceStatus updateServiceStatus(String monitorServerName,
                                             String applicationType, String hostName, String deviceIdent,
                                             Map properties);

        /**
         * This method records the state of a service at given point in time; the
         * name of the service, as well as the lastCheckTime, is contained in the
         * 'properties' argument passed; for more on the format of the map, read the
         * parameter docs below.
         *
         * @param monitorServerName
         *            the unique name of the monitor that is performing the check
         *
         * @param applicationType
         *            the unique name/code for the type of application that is being
         *            monitored, currently one of 'NAGIOS', 'SYSLOG', or
         *            'JMX_SAMPLE'
         *
         * @param hostName
         *            the name of the logical host or application that is being
         *            monitored
         *
         * @param deviceIdent
         *            the unique name/IP/MAC of the physical device on which the
         *            host resides
         *
         * @param agentId
         *            the name of the Agent Id from a remote monitor agent
         *
         * @param properties
         *            map containing String-PrimitiveObject pairs where:
         *
         *            <ul>
         *            <li>
         *            The values are java primitive wrapper classes such as String,
         *            Date, Boolean, Integer, Long and Double, as defined in the
         *            corresponding {@link PropertyType} object</li>
         *            <li>
         *            <p>
         *            The keys of the map are Strings describing some
         *            property/attribute/metric for the ServiceStatus
         *            (PropertyExtensible entity) that is being monitored (e.g.
         *            ServiceDescription, LastStateChange, etc...);
         *            </p>
         *            <p>
         *            These strings are defined (pre-configured) in the
         *            <code>Metadata</code> singleton loaded from the current
         *            implementation of the {@link CollageAccessor} factory
         *            </p>
         *            <p>
         *            In the current implementation, the metadata is stored in a
         *            database and is accessible via the {@link ApplicationType} and
         *            its associated {@link EntityType} and {@link PropertyType}
         *            objects, but this may change in the future, as the metadata
         *            could easily be stored elsewhere, such as an xml file, LDAP
         *            directory, windows application, remote web service, etc...
         *            </p>
         *            </li>
         *            </ul>
         *
         * @see PropertyExtensible
         * @see ApplicationType
         * @see EntityType
         * @see PropertyType
         */
    public ServiceStatus updateServiceStatus(String monitorServerName,
            String applicationType, String hostName, String deviceIdent,
            String agentId,
            Map properties) throws CollageException;

    /**
     * This method records the state of a service at given point in time, see
     * {@link #updateServiceStatus(String, String, String, String, String, java.util.Map)}.
     * Provides arguments for batch service, host, and device in the event they are not
     * available by query yet.
     */
    public ServiceStatus updateServiceStatus(ServiceStatus serviceStatus, String monitorServerName,
                                             String applicationType, String hostName, String deviceIdent,
                                             String agentId, Host host, Device device, boolean mergeHosts,
                                             Map properties) throws CollageException;

    /** Performs 'bulk' update/insert of ServiceStatus for a host */
    void updateServiceStatus(String monitorServerName, String applicationType,
            String hostName, String deviceIdent, Collection<Map> serviceStatuses)
            throws CollageException;

    /**
     * Updates the Collage ServiceStatus table. If there is no entry for the
     * ServiceStatus, a new one will be created. If the device or host don't
     * exist, they will be added as well.
     * 
     * @param monitorServerName
     *            Name of the MonitorServer
     * @param host
     *            Name of the host
     * @param deviceIdent
     * @param serviceDescription
     *            Name or description of the service
     * @param lastPluginOutput
     *            Last output received
     * @param monitorStatus
     *            MonitorStatus ID. see {@link MonitorStatus} for possible
     *            values.
     * @param retryNumber
     *            Number of times an attempt has been made to contact the
     *            service.
     * @param stateType
     * @param lastCheckTime
     *            The time that the service was checked last.
     * @param nextCheckTime
     *            The time at which the service will be checked next.
     * @param checkType
     * @param isChecksEnabled
     *            Are checks enabled? true/false
     * @param isAcceptPassiveChecks
     *            Are Passive Checks accepted? true/false
     * @param isEventHandlersEnabled
     *            Is the event handler enabled? true/false
     * @param lastStateChange
     *            The time of the last state change
     * @param isProblemAcknowledged
     *            Has the problem been acknowledged? true/false
     * @param lastHardState
     * @param timeOK
     *            The amount of time that the service has been "OK".
     * @param timeUnknown
     *            The amount of time that the service has had a status of
     *            "UNKNOWN".
     * @param timeWarning
     *            The amount of time that the service has had a status of
     *            "WARNING".
     * @param timeCritical
     *            The amount of time that the service has had a status of
     *            "CRITICAL".
     * @param lastNotificationTime
     *            The time that a notification was last sent
     * @param currentNotificationNumber
     *            The count of notifications
     * @param isNotificationsEnabled
     *            Are notifications enabled? true/false
     * @param latency
     * @param executionTime
     * @param isFlapDetectionEnabled
     * @param isServiceFlapping
     * @param percentStateChange
     * @param scheduledDowntimeDepth
     * @param isFailurePredictionEnabled
     * @param isProcessPerformanceData
     * @param isObsessOverService
     * @throws CollageException
     */
    void updateServiceStatus(String monitorServerName, String host,
            String deviceIdent, String serviceDescription,
            String lastPluginOutput, String monitorStatus, String retryNumber,
            String stateType, String lastCheckTime, String nextCheckTime,
            String checkType, String isChecksEnabled,
            String isAcceptPassiveChecks, String isEventHandlersEnabled,
            String lastStateChange, String isProblemAcknowledged,
            String lastHardState, String timeOK, String timeUnknown,
            String timeWarning, String timeCritical,
            String lastNotificationTime, String currentNotificationNumber,
            String isNotificationsEnabled, String latency,
            String executionTime, String isFlapDetectionEnabled,
            String isServiceFlapping, String percentStateChange,
            String scheduledDowntimeDepth, String isFailurePredictionEnabled,
            String isProcessPerformanceData, String isObsessOverService, String PerformanceData)
            throws CollageException;

    /**
     * This method records the state of a logical Host (xyz.domain.com) or
     * possibly an application (xyz.domain.com/webapp) at a given point in time;
     * see the parameter docs of
     * {@link #updateServiceStatus(String,String,String,String,Map)} for more
     */
    void updateHostStatus(String monitorServerName, String applicationType,
            String hostName, String deviceIdent, Map properties);

    /**
     * Updates the Host identified by the Host name provided; if the a record
     * for the Host does not already; likewise, records are created for the
     * Device on which the Host is hosted and for the MonitorServer monitoring
     * the Device/Host, if such records do not already exist.
     * 
     * @param MonitorServerName
     *            Name of the MonitorServer
     * @param Host
     *            Name of the Host
     * @param DeviceIdent
     * @param LastPluginOutput
     *            Last output received
     * @param MonitorStatus
     *            MonitorStatus ID - @see {@link MonitorStatus} for possible
     *            values.
     * @param LastCheckTime
     *            The time that the host was checked last.
     * @param LastStateChange
     *            The time of the last state change
     * @param isAcknowledged
     *            Has the current state been acknowledged? true/false
     * @param TimeUp
     *            The amount of time the host has been UP
     * @param TimeDown
     *            The amount of time the host has been DOWN
     * @param TimeUnreachable
     *            The amount of time the host has been UNREACHABLE
     * @param LastNotificationTime
     *            The time of the last notification
     * @param CurrentNotificationNumber
     * @param isNotificationsEnabled
     * @param isChecksEnabled
     * @param isEventHandlersEnabled
     * @param isFlapDetectionEnabled
     * @param isHostIsFlapping
     * @param PercentStateChange
     * @param ScheduledDowntimeDepth
     * @param isFailurePredictionEnabled
     * @param isProcessPerformanceData
     * @param CheckType
     * @param Latency
     * @param ExecutionTime
     * @throws CollageException
     */
    void updateHostStatus(String MonitorServerName, String Host,
            String DeviceIdent, String LastPluginOutput, String MonitorStatus,
            String LastCheckTime, String LastStateChange,
            String isAcknowledged, String TimeUp, String TimeDown,
            String TimeUnreachable, String LastNotificationTime,
            String CurrentNotificationNumber, String isNotificationsEnabled,
            String isChecksEnabled, String isEventHandlersEnabled,
            String isFlapDetectionEnabled, String isHostIsFlapping,
            String PercentStateChange, String ScheduledDowntimeDepth,
            String isFailurePredictionEnabled, String isProcessPerformanceData,
            String CheckType, String Latency, String ExecutionTime,
            String isPassiveChecksEnabled, String PerformanceData) throws CollageException;

    /**
     * Reset host status message, (LastPluginOutput), property.
     *
     * @param hostName host name
     */
    void resetHostStatusMessage(String hostName) throws CollageException;

    /**
     * This method records a LogMessage for a Device; see the parameter docs of
     * {@link #updateServiceStatus(String,String,String,String,Map)} for more
     */
    LogMessage updateLogMessage(String monitorServerName,
            String applicationType, String deviceIdent, String severity,
            String textMessage, Map properties);

    /**
     * This method records a LogMessage for a Device; see the parameter docs of
     * {@link #updateServiceStatus(String,String,String,String,Map)} for more.
     * Provides arguments for batch device, host, and service status in the event
     * these are not available by query yet.
     */
    LogMessage updateLogMessage(String monitorServerName,
                                String applicationType, String deviceIdent, String severity,
                                String textMessage, Device device, Host host, ServiceStatus serviceStatus,
                                Map properties);
    /**
     * Removes all message caching for a given host/service
     *
     * @param host
     * @param service
     * @return
     */
    void clearMessageCache(String host, String service);

    /**
     * Update the operation status for the specified log message
     * 
     * @param logMessageId
     *            Comma separated list
     * @param opStatus
     * @throws CollageException
     */
    LogMessage updateLogMessageOperationStatus(String logMessageId,
            String opStatus) throws CollageException;

    /**
     * Update the collage LogMessage table. If the device, host or ServiceStatus
     * don't exist, they will be added.
     * 
     * @param ConsolidationCriteria
     * @param LogType
     * @param MonitorServerName
     *            Name of the MonitorServer
     * @param Host
     *            Name of the Host
     * @param DeviceIdent
     *            Identification of the Device
     * @param Severity
     *            Severity of the entry - see {@link Severity}
     * @param MonitorStatus
     *            MonitorStatus ID - see {@link MonitorStatus}
     * @param TextMessage
     *            Text of the entry
     * @param ReportDate
     *            The date of the entry
     * @param LastInsertDate
     *            Last time the log was updated
     * @param SubComponent
     * @param ErrorType
     * @param ServiceDescription
     *            Name or description of the {@link ServiceStatus}
     * @param ServiceStatus
     *            ServiceStatus ID
     * @param LoggerName
     * @param ApplicationName
     * @throws CollageException
     */
    LogMessage updateLogMessage(
            String ConsolidationCriteria, // Can be null. If defined the message
                                          // will be matched with existing
                                          // entries
            String LogType, // NAGIOS, COLLAGE, SYSLOG)
            String MonitorServerName, String Host, String DeviceIdent,
            String Severity, String MonitorStatus, String TextMessage,
            String ReportDate, String LastInsertDate, String SubComponent,
            String ErrorType,
            String ServiceDescription, // lookup the ServiceStatus
            String ServiceStatus, String LoggerName, String ApplicationName,
            String FirstInsertDate) throws CollageException;

    /**
     * Adds Hosts to a HostGroup.
     * 
     * @param HostGroupName
     *            Name of the HostGroup
     * @param hostList
     *            List of names of Hosts to be added.
     * @throws CollageException
     */
    
    public HostGroup addHostsToHostGroup(String applicationType,
            String HostGroupName, List<String> hostList, String description, String alias)
            throws CollageException;
    
    public HostGroup addHostsToHostGroup(String applicationType,
            String HostGroupName, List<String> hostList, String description)
            throws CollageException;
    
    public HostGroup addHostsToHostGroup(String applicationType,
            String HostGroupName, List<String> hostList)
            throws CollageException;

    /**
     * Adds Services to a ServiceGroup.
     * 
     * @param ServiceGroupName
     *            Name of the ServiceGroup
     * @param serviceList
     *            List of names of Services to be added.
     * @throws CollageException
     */
    // public void addServicesToServiceGroup(String applicationType, String
    // serviceGroupName, List<String> serviceList) throws CollageException;
    /**
     * Adds Devices to a Parent Device.
     * 
     * @param parentDevice
     *            Name of the Parent Device
     * @param deviceList
     *            List of names of Devices to be added.
     * @throws CollageException
     */
    public void addDevicesToParentDevice(String parentDevice,
            List<String> deviceList) throws CollageException;

    /**
     * Adds Devices to a Child Device.
     * 
     * @param childDevice
     *            Name of the Child Device.
     * @param deviceList
     *            List of names of Devices to be added.
     * @throws CollageException
     */
    public void addDevicesToChildDevice(String childDevice,
            List<String> deviceList) throws CollageException;

    /**
     * Adds Devices to a MonitorServer; if a MonitorServer with the name
     * provided does not exist, it is created.
     * 
     * @param monitorServer
     *            Name of the MonitorServer
     * @param deviceList
     *            List of names of Devices to be added.
     * @throws CollageException
     */
    public void addDevicesToMonitorServer(String monitorServer,
            List<String> deviceList) throws CollageException;

    /**
     * Removes Hosts from a HostGroup
     * 
     * @param HostGroupName
     *            Name of the HostGroup
     * @param hostList
     *            List of names of Hosts to remove
     * @throws CollageException
     */
    public void removeHostsFromHostGroup(String HostGroupName,
            List<String> hostList) throws CollageException;

    /**
     * Removes Devices from a Parent Device.
     * 
     * @param parentDevice
     *            Name of Parent Device
     * @param deviceList
     *            List of names of Devices to remove
     * @throws CollageException
     */
    public void removeDevicesFromParentDevice(String parentDevice,
            List<String> deviceList) throws CollageException;

    /**
     * Removes Devices from a Child Device
     * 
     * @param childDevice
     *            Name of Child Device
     * @param deviceList
     *            List of names of Devices to remove.
     * @throws CollageException
     */
    public void removeDevicesFromChildDevice(String childDevice,
            List<String> deviceList) throws CollageException;

    /**
     * Removes Devices from a MonitorServer
     * 
     * @param monitorServer
     *            Name of MonitorServer
     * @param deviceList
     *            List of names of Devices to remove.
     * @throws CollageException
     */
    public void removeDevicesFromMonitorServer(String monitorServer,
            List<String> deviceList) throws CollageException;

    /**
     * deletes ServiceStatus records with the ServiceDescription provided, and
     * de-associates from that service all LogMessages that were associated with
     * that Service
     * 
     * @param hostName
     *            Name of Host containing the Service to be removed
     * @param serviceDescr
     *            Name of Service to remove
     * @throws CollageException
     */
    public Integer removeService(String hostName, String serviceDescr)
            throws CollageException;

    /**
     * deletes ServiceStatus records with the service id provided, and
     * de-associates from that service all LogMessages that were associated with
     * that Service
     *
     * @param serviceId
     *            Name of Service to remove
     * @throws CollageException
     */
    public void removeService(int serviceId) throws CollageException;

    /**
     * deletes the Host with the name provided, and the related HostStatus, and
     * ServiceStatus - unlinks (but does not delete) all LogMessages that were
     * previously attached to this Host
     * 
     * @param hostName
     * @throws CollageException
     */
    public Integer removeHost(String hostName) throws CollageException;

    /**
     * deletes the Host with the id provided, and the related HostStatus, and
     * ServiceStatus - unlinks (but does not delete) all LogMessages that were
     * previously attached to this Host
     * 
     * @param hostId
     * @throws CollageException
     */
    public Integer removeHost(int hostId) throws CollageException;

    /**
     * Deletes the HostGroup with the name provided, but does not affect any of
     * the Hosts within that HostGroup
     * 
     * @param hostGroupName
     * @throws CollageException
     */
    public Integer removeHostGroup(String hostGroupName)
            throws CollageException;

    /**
     * Deletes the HostGroup with the id provided, but does not affect any of
     * the Hosts within that HostGroup
     * 
     * @param hostGroupId
     * @throws CollageException
     */
    public Integer removeHostGroup(int hostGroupId) throws CollageException;

    /**
     * Deletes the Device with the name provided, including all its Hosts,
     * Services and LogMessages
     * 
     * @param identification
     * @throws CollageException
     */
    public void removeDevice(String identification) throws CollageException;

    /**
     * Deletes the Device with the id provided, including all its Hosts,
     * Services and LogMessages
     * 
     * @param deviceId
     * @throws CollageException
     */
    public void removeDevice(int deviceId) throws CollageException;

    /**
     * Creates a datastructure for Nagios properties that is backwards
     * compatible with Collage 1.0 Data Model.
     * 
     * @param ServiceDescription
     * @param LastPluginOutput
     * @param MonitorStatus
     * @param RetryNumber
     * @param StateType
     * @param LastCheckTime
     * @param NextCheckTime
     * @param CheckType
     * @param isChecksEnabled
     * @param isAcceptPassiveChecks
     * @param isEventHandlersEnabled
     * @param LastStateChange
     * @param isProblemAcknowledged
     * @param LastHardState
     * @param TimeOK
     * @param TimeUnknown
     * @param TimeWarning
     * @param TimeCritical
     * @param LastNotificationTime
     * @param CurrentNotificationNumber
     * @param isNotificationsEnabled
     * @param Latency
     * @param ExecutionTime
     * @param isFlapDetectionEnabled
     * @param isServiceFlapping
     * @param PercentStateChange
     * @param ScheduledDowntimeDepth
     * @param isFailurePredictionEnabled
     * @param isProcessPerformanceData
     * @param isObsessOverService
     * @return Properties map that can be passed into the Admin API
     */
    public Properties createNagiosServiceStatusProps(String ServiceDescription,
            String LastPluginOutput, String MonitorStatus, String RetryNumber,
            String StateType, String LastCheckTime, String NextCheckTime,
            String CheckType, String isChecksEnabled,
            String isAcceptPassiveChecks, String isEventHandlersEnabled,
            String LastStateChange, String isProblemAcknowledged,
            String LastHardState, String TimeOK, String TimeUnknown,
            String TimeWarning, String TimeCritical,
            String LastNotificationTime, String CurrentNotificationNumber,
            String isNotificationsEnabled, String Latency,
            String ExecutionTime, String isFlapDetectionEnabled,
            String isServiceFlapping, String PercentStateChange,
            String ScheduledDowntimeDepth, String isFailurePredictionEnabled,
            String isProcessPerformanceData, String isObsessOverService);

    /**
     * API to manage Categories which are nothing more than nested groups. The
     * methods are called from the adapter so that third party applications are
     * able to do inserts and updates.
     */

    /**
     * 
     * @param categoryName
     * @param entityType
     * @param entityEntityType
     * @param entityObjectID
     * @return CategoryID of the created Category
     * @throws CollageException
     * 
     *             Note if ObjectID is not null a CategoryEntity for that Object
     *             type will be created. If ObjectID is null only a
     *             CategoryEntry will be created. If the category already exists
     *             only the CategoryEntity will be added.
     */
    public Category addCategoryEntity(String categoryName, String entityType, String entityEntityType,
            String entityObjectID) throws CollageException;

    /**
     * Another flavor of add category API which allows to define the description field for the Category object
     * @param categoryName
     * @param entityType
     * @param entityEntityType
     * @param entityObjectID
     * @param description
     * @return Category
     * @throws CollageException
     *
     * Note if ObjectID is not null a CategoryEntity for that Object type will be created. If ObjectID is null
      * only a CategoryEntry will be created. If the category already exists only the CategoryEntity will be added.
     */
    public Category addCategoryEntity(String categoryName, String entityType, String entityEntityType,
                                      String entityObjectID, String description) throws CollageException;

    /**
     * 
     * @param categoryName
     * @param entityType
     * @return removed category
     * @throws CollageException
     */
    public Category removeCategory(String categoryName, String entityType) throws CollageException;

    /**
     * 
     * @param categoryID
     * @return removed category
     * @throws CollageException
     */
    public Category removeCategory(Integer categoryID) throws CollageException;

    /**
     * 
     * @param categoryName
     * @param entityType
     * @param entityEntityType
     * @param entityObjectID
     * @throws CollageException
     */
    public Category removeCategoryEntity(String categoryName, String entityType,
            String entityEntityType, String entityObjectID) throws CollageException;

    /**
     * 
     * @param EntityType
     * @param ObjectID
     * @throws CollageException
     */
    public Collection<Category> removeCategoryEntity(String EntityType,
            int ObjectID) throws CollageException;

    /**
     * 
     * @param parentCategoryName
     * @param categoryName
     * @param entityType
     * @throws CollageException
     */
    public Category addCategoryToParent(String parentCategoryName,
            String categoryName, String entityType) throws CollageException;

    /**
     * @param parentCategoryName
     * @param categoryName
     * @param entityType
     * @throws CollageException
     */
    public void removeCategoryFromParent(String parentCategoryName,
            String categoryName, String entityType) throws CollageException;

    /**
     * 
     * @param categoryID
     * @param name
     * @param description
     * @return updated category
     * @throws CollageException
     */
    public Category updateCategory(Integer categoryID, String name,
            String description) throws CollageException;

    /**
     * Stores a category into the database
     *
     * @param category
     * @throws CollageException
     */
    public void saveCategory(Category category) throws CollageException;

    /**
     * Propagate created categories stub for AOP injection.
     *
     * @param categories created categories collection
     * @throws CollageException
     */
    public void propagateCreatedCategories(Collection<Category> categories) throws CollageException;

    /**
     * Propagate deleted categories stub for AOP injection.
     *
     * @param categories deleted categories collection
     * @throws CollageException
     */
    public void propagateDeletedCategories(Collection<Category> categories) throws CollageException;

    /**
     * Propagate modified categories stub for AOP injection.
     *
     * @param categories modified categories collection
     * @throws CollageException
     */
    public void propagateModifiedCategories(Collection<Category> categories) throws CollageException;

    /**
     * Manage Consolidation Criterias. Consolidation criterias will be applied
     * to any inserts to LogMessage that identify the consolidation criteria to
     * be applied
     */

    /**
     * 
     * @param name
     * @param consolidationCriteria
     * @throws CollageException
     */
    void addOrUpdateConsolidationCriteria(String name,
            String consolidationCriteria) throws CollageException;

    /**
     * 
     * @param ConsolidationCriteriaID
     * @throws CollageException
     */
    void removeConsolidationCriteria(Integer ConsolidationCriteriaID)
            throws CollageException;

    /**
     * 
     * @param Name
     * @return removed status
     * @throws CollageException
     */
    boolean removeConsolidationCriteria(String Name) throws CollageException;

    /**
     * 
     * @param consolidationCriteriaID
     * @param Name
     * @param ConsolidationCriteria
     * @throws CollageException
     */
    void updateConsolidationCriteria(Integer consolidationCriteriaID,
            String Name, String ConsolidationCriteria) throws CollageException;

    /**
     * 
     * @param ServiceStatusID
     * @param applicationType
     * @param properties
     * @throws CollageException
     */
    void updateServiceStatusByID(Integer ServiceStatusID, String applicationType, Map properties)
            throws CollageException;

    /**
     * 
     * @param HostStatusID
     * @param applicationType
     * @param properties
     * @throws CollageException
     */
    void updateHostStatusByID(Integer HostStatusID, String applicationType, Map properties)
            throws CollageException;

    /**
     * 
     * @param LogMessageID
     * @param properties
     * @throws CollageException
     */
    LogMessage updateLogMessageByID(Integer LogMessageID, Map properties)
            throws CollageException;

    /**
     * acknowledgeEvent Updates an existing event entry by setting the
     * acknowledged by
     * 
     * @param applicationType
     * @param typeRule
     * @param host
     * @param serviceDescription
     * @param acknowledgedBy
     * @param acknowledgeComment
     * @throws CollageException
     */
    public boolean acknowledgeEvent(String applicationType, String typeRule,
            String host, String serviceDescription, String acknowledgedBy,
            String acknowledgeComment) throws CollageException;

    /**
     * triggerAcknowledgeEventAOP Triggers the AOP
     * 
     * @param serviceId
     * @param hostId
     * @throws CollageException
     */
    public ArrayList<Integer> triggerAcknowledgeEventAOP(ArrayList messageIds,
            String hostId, String serviceId);

    /**
     * Insert Performance Data into the system. If the Service or Host or
     * service doesn't exist the value will be lost.
     * 
     * @param hostName
     * @param serviceDescription
     * @param performanceDataLabel
     * @param performanceValue
     * @param checkDate
     * @throws CollageException
     */
    public void insertPerformanceData(final String hostName,
            final String serviceDescription, final String performanceDataLabel,
            double performanceValue, String checkDate) throws CollageException;

    /**
     * Create hosts in bulk - This method will also create devices and monitor
     * servers if necessary. If a host already exists it is updated. Also if the
     * application type is not defined or is not found it defaults to NAGIOS.
     * 
     * @param hosts
     * @throws CollageException
     * @deprecated  will be deprecated in future release
     */
    public void addOrUpdateHosts(List<Hashtable<String, String>> hosts)
            throws CollageException;

    /**
     * Create hosts in bulk - This method will also create devices and monitor
     * servers if necessary. If a host already exists it is updated. Also if the
     * application type is not defined or is not found it defaults to NAGIOS.
     *
     * @param hosts
     * @throws CollageException
     */
    public List<Host> addOrUpdateHostList(List<Map<String, String>> hosts)
            throws CollageException;


    /**
     * Create host
     * 
     * @param hostAttributes
     */
    public Host addOrUpdateHost(Map<String, String> hostAttributes);

    /**
     * Create host, see {@link #addOrUpdateHost(java.util.Map)},
     * Provides arguments for batch host and device in the event they are not
     * available by query yet.
     */
    public Host addOrUpdateHost(Host host, Device device, boolean mergeHosts, Map<String, String> hostAttributes);

    /**
     * Update Host Group - Create host group if it doesn't exist and updates it
     * to contain only the hosts provided.
     * 
     * @param applicationType
     * @param hostGroupName
     * @param hostList
     * @throws CollageException
     */

    public HostGroup updateHostGroup(String applicationType,
            String hostGroupName, List<String> hostList)
            throws CollageException;

    /**
     * Update Host Group. Create host group if it doesn't exist and updates it
     * to contain only the hosts provided, see {@link #updateHostGroup(String, String, java.util.List)}.
     * Provides argument for batch host group and hosts in the event they are not available by query yet.
     */
    public HostGroup updateHostGroup(String applicationType,
                                     String hostGroupName, List<String> hostList, HostGroup hostGroup,
                                     List<Host> hosts)
            throws CollageException;

    /**
     * Update Host Group - Complete version that takes all the fields. Create
     * host group if it doesn't exist and updates it to contain only the hosts
     * provided.
     * 
     * @param applicationType
     * @param hostGroupName
     * @param hostList
     * @param alias
     * @param description
     * @return HostGroup
     * @throws CollageException
     */

    public HostGroup updateHostGroup(String applicationType,
            String hostGroupName,
            List<String> hostList,
            String alias,
            String description,
            String agentId) throws CollageException;

    /**
     * Execute all commands
     * 
     * @param commandList
     */
    public void executeCommands(List<CollageCommand> commandList);

    /**
     * Propagate host changes to the hostgroup
     * 
     * @param host
     * @return Set<HostGroup>
     */
    public Set<HostGroup> propagateHostChangesToHostGroup(Host host)
            throws CollageException;

    /**
     * Propagate host changes to the hostgroup. Just to stimulate the AOP
     *
     * @param hostGroups
     * @return Set<HostGroup>
     */
    public Set<HostGroup> propagateHostChangesToHostGroup(
            Set<HostGroup> hostGroups) throws CollageException;

    /**
     * Propagate Service changes to the ServiceGroup
     * 
     * @param serviceStatus
     * @return List<Category>
     */
    public Collection<Category> propagateServiceChangesToServiceGroup(
            ServiceStatus serviceStatus) throws CollageException;

    /**
     * Propagate Service changes to the Host
     * 
     * @param serviceStatus
     * @return Host
     */
    public Host propagateServiceChangesToHost(ServiceStatus serviceStatus)
            throws CollageException;

    /**
     * Propagate Service changes to the Host. Just to stimulate the AOP
     * 
     * @param host
     * @return Host
     */
    public Host propagateServiceChangesToHost(Host host)
            throws CollageException;

    /**
     * Bulk Update of multiple events
     *
     * @param opStatus
     * @param updatedBy
     * @param comments
     * @throws CollageException
     */
    public void storeEventOperationalStatus(List<Integer> events, String opStatus, String updatedBy, String comments)
            throws CollageException;

    /**
     * Remove a log message and send notifications
     *
     * @param id
     * @throws CollageException
     */
    public void removeLogMessage(int id) throws CollageException;

    /**
     * Renames a host return true if successful and false on errors
     * Will check HostIdentity to ensure data integrity and uniqueness
     *
     * @param oldHostName the existing name of the host
     * @param newHostName the proposed new name of the host
     * @param description optional parameter to update the host description
     * @param deviceIdentification optional parameter to update device Identification
     * @return Host returns the new Host record if successful
     * @throws CollageException on unexpected failure
     */
    public Host renameHost(String oldHostName, String newHostName, String description, String deviceIdentification)
            throws CollageException;

    /**
     * Helper to translate incoming monitor status.
     *
     * @param serviceProperties service properties
     * @return monitor status
     */
    public String translateNewServiceMonitorStatus(Map<String,String> serviceProperties);

    /**
     * Helper to translate incoming monitor status.
     *
     * @param hostProperties host properties
     * @return monitor status
     */
    public String translateNewHostMonitorStatus(Map<String,String> hostProperties);
}
