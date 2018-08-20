/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com

 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.

 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.

 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.groundwork.collage;

import java.util.Collection;
import com.groundwork.collage.exception.CollageException;
import com.groundwork.collage.model.ServiceStatus;
import com.groundwork.collage.model.Severity;

/**
 * 
 * Adds or updates data in the Collage database.  This is designed to be accessed
 * from the Collage feeders (collector/normalizers) to create or update state
 * information.
 * 
 * @author <a href=String mailto:rruttimann@itgroundwork.comString > Roger Ruttimann</a>
 * @version $Id: CollageAdmin.java 19298 2012-08-10 18:27:28Z rruttimann $
 * 
 */
public interface CollageAdmin {

   /**
    * Updates the Collage ServiceStatus table;  If there is no entry for the 
    * ServiceStatus, a new one will be created;  If the Monitor Server, Device or Host don't 
    * exist, they will be created as well.
    * 
    * @param MonitorServerName Name of the MonitorServer
    * @param Host Name of the host
    * @param DeviceIdent 
    * @param ServiceDescription Name or description of the service
    * @param LastPluginOutput Last output received 
    * @param MonitorStatus a code identifying a MonitorStatus value, an
    * enumerated list 
    * @param RetryNumber Number of times an attempt has been made to
    * contact the service.
    * @param StateType
    * @param LastCheckTime The time that the service was checked last.
    * @param NextCheckTime The time at which the service will be checked next.
    * @param CheckType
    * @param isChecksEnabled Are checks enabled? true/false
    * @param isAcceptPassiveChecks Are Passive Checks accepted? true/false
    * @param isEventHandlersEnabled Is the event handler enabled? true/false
    * @param LastStateChange The time of the last state change
    * @param isProblemAcknowledged Has the problem been acknowledged? true/false
    * @param LastHardState
    * @param TimeOK The amount of time that the service has been "OK".
    * @param TimeUnknown The amount of time that the service has had a status of
    * "UNKNOWN".
    * @param TimeWarning The amount of time that the service has had a status of
    * "WARNING".
    * @param TimeCritical The amount of time that the service has had a status of
    * "CRITICAL".
    * @param LastNotificationTime The time that a notification was last sent
    * @param CurrentNotificationNumber The count of notifications
    * @param isNotificationsEnabled Are notifications enabled? true/false
    * @param Latency
    * @param ExecutionTime
    * @param isFlapDetectionEnabled 
    * @param isServiceFlapping
    * @param PercentStateChange
    * @param ScheduledDowntimeDepth
    * @param isFailurePredictionEnabled
    * @param isProcessPerformanceData
    * @param isObsessOverService
    * @throws CollageException
    */
    void updateServiceStatus(  
        String MonitorServerName, 
        String Host,
        String DeviceIdent,
        String ServiceDescription, 
        String LastPluginOutput,
        String MonitorStatus,
        String RetryNumber,
        String StateType,
        String LastCheckTime,
        String NextCheckTime,
        String CheckType,
        String isChecksEnabled,
        String isAcceptPassiveChecks,
        String isEventHandlersEnabled,
        String LastStateChange,
        String isProblemAcknowledged,
        String LastHardState,
        String TimeOK,
        String TimeUnknown,
        String TimeWarning,
        String TimeCritical,
        String LastNotificationTime,
        String CurrentNotificationNumber,
        String isNotificationsEnabled,
        String Latency,
        String ExecutionTime,
        String isFlapDetectionEnabled,
        String isServiceFlapping,
        String PercentStateChange,
        String ScheduledDowntimeDepth,
        String isFailurePredictionEnabled,
        String isProcessPerformanceData,
        String isObsessOverService,
        String PerformanceData)
    throws CollageException;


    /** Performs 'bulk' update/insert of ServiceStatus for a host */
    void updateServiceStatus(
            String MonitorServerName, String Host, String DeviceIdent, Collection serviceStatuses)
        throws CollageException;


    /** 
     * returns a ServiceStatus bean that can be used to call
     * updateServiceStatus with a collection of statuses 
     */
    public ServiceStatus createServiceStatus(
            String ServiceDescription,
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
     * Updates the Host identified by the Host name provided; if the a record
     * for the Host does not already; likewise, records are created for the
     * Device on which the Host is hosted and for the MonitorServer monitoring
     * the Device/Host, if such records do not already exist.
     *
     * @param MonitorServerName Name of the MonitorServer
     * @param Host Name of the Host
     * @param DeviceIdent
     * @param LastPluginOutput Last output received 
     * @param MonitorStatus a code identifying a MonitorStatus value, an
     * enumerated list 
     * @param LastCheckTime The time that the host was checked last.
     * @param LastStateChange The time of the last state change
     * @param isAcknowledged Has the current state been acknowledged? true/false
     * @param TimeUp The amount of time the host has been UP
     * @param TimeDown The amount of time the host has been DOWN
     * @param TimeUnreachable The amount of time the host has been UNREACHABLE
     * @param LastNotificationTime The time of the last notification
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
     * @throws CollageException
     */
     void updateHostStatus( 
            String MonitorServerName, 
            String Host,
            String DeviceIdent,
            String LastPluginOutput,
            String MonitorStatus,
            String LastCheckTime,
            String LastStateChange,
            String isAcknowledged,      
            String TimeUp,
            String TimeDown,
            String TimeUnreachable,
            String LastNotificationTime,
            String CurrentNotificationNumber,
            String isNotificationsEnabled,  
            String isChecksEnabled,
            String isEventHandlersEnabled,
            String isFlapDetectionEnabled,
            String isHostIsFlapping,
            String PercentStateChange,
            String ScheduledDowntimeDepth,
            String isFailurePredictionEnabled,
            String isProcessPerformanceData,
            String PerformanceData)
     throws CollageException;
     
     /**
      * Update the collage LogMessage table.  If the device, host or  
      * ServiceStatus don't exist, they will be added.
      * 
      * @param LogType
      * @param MonitorServerName Name of the MonitorServer
      * @param Host Name of the Host
      * @param DeviceIdent Identification of the Device
      * @param Severity Severity of the entry - see {@link Severity}
      * @param MonitorStatus a code identifying a MonitorStatus value, an
      * enumerated list 
      * @param TextMessage Text of the entry
      * @param ReportDate The date of the entry
      * @param LastInsertDate Last time the log was updated
      * @param SubComponent
      * @param ErrorType
      * @param ServiceDescription Name or description of the {@link ServiceStatus}
      * @param ServiceStatus ServiceStatus ID
      * @param LoggerName
      * @param ApplicationName
      * @throws CollageException
      */
     void updateLogMessage(	String LogType,		// NAGIOS, COLLAGE, SYSLOG)
												String MonitorServerName, 
												String Host,
												String DeviceIdent,
												String Severity,
												String MonitorStatus,
												String TextMessage,
												String ReportDate,
												String LastInsertDate,
												String SubComponent,
												String ErrorType,
												String ServiceDescription,		// lookup the ServiceStatus
												String ServiceStatus,
												String LoggerName,
												String ApplicationName )
     throws CollageException;
     
     
          
     /**
      * Adds Hosts to a HostGroup.
      * 
      * @param ApplicationTypeID
      * @param HostGroupName Name of the HostGroup 
      * @param hostList List of names of Hosts to be added.
      * @throws CollageException
      */
     public void addHostsToHostGroup(String applicationType, String HostGroupName, String hostList) throws CollageException;
     
     /**
 	 * Removes a Device from the system, including all its associated Hosts,
 	 * Services, and LogMessages
 	 * @param identification
 	 * @throws CollageException
 	 */
 	public void removeDevice(String id) throws CollageException;

     /**
      * Adds Devices to a Parent Device.
      * 
      * @param parentDevice Name of the Parent Device
      * @param deviceList List of names of Devices to be added.
      * @throws CollageException
      */
     public void addDevicesToParentDevice(String parentDevice, String deviceList) throws CollageException;
     
     /**
      * Adds Devices to a Child Device.
      * 
      * @param childDevice Name of the Child Device.
      * @param deviceList List of names of Devices to be added.
      * @throws CollageException
      */
     public void addDevicesToChildDevice(String childDevice, String deviceList) throws CollageException;
     
     /**
      * Adds Devices to a MonitorServer; if a MonitorServer with the name
      * provided does not exist, it is created.
      * 
      * @param monitorServer Name of the MonitorServer
      * @param deviceList List of names of Devices to be added.
      * @throws CollageException
      */
     public void addDevicesToMonitorServer(String monitorServer, String deviceList) throws CollageException;
     
     /**
      * Removes Hosts from a HostGroup
      * 
      * @param HostGroupName Name of the HostGroup
      * @param hostList List of names of Hosts to remove
      * @throws CollageException
      */
     public void removeHostsFromHostGroup(String HostGroupName, String hostList) throws CollageException;
     
     /**
      * Removes Devices from a Parent Device.
      * 
      * @param parentDevice Name of Parent Device
      * @param deviceList List of names of Devices to remove
      * @throws CollageException
      */
     public void removeDevicesFromParentDevice(String parentDevice, String deviceList) throws CollageException;
     
     /**
      * Removes Devices from a Child Device
      * 
      * @param childDevice Name of Child Device
      * @param deviceList List of names of Devices to remove.
      * @throws CollageException
      */
     public void removeDevicesFromChildDevice(String childDevice, String deviceList) throws CollageException;
     
     /**
      * Removes Devices from a MonitorServer
      * @param monitorServer Name of MonitorServer
      * @param deviceList List of names of Devices to remove.
      * @throws CollageException
      */
     public void removeDevicesFromMonitorServer(String monitorServer, String deviceList) throws CollageException;


     /**
      * deletes ServiceStatus records with the ServiceDescription provided, and
      * de-associates from that service all LogMessages that were associated with
      * that Service
      *
      * @param serviceDescr Name of Service to remove
      * @throws CollageException
      */
     public void removeService(String serviceDescr) throws CollageException;
     
     /**
      * deletes the Host with the name provided, and the related HostStatus,
      * and ServiceStatus - unlinks (but does not delete) all LogMessages that
      * were previously attached to this Host
      *
      * @param hostName
      * @throws CollageException
      */
     public void removeHost(String hostName) throws CollageException;

     /**
      * Deletes the HostGroup with the name provided, but does not affect any
      * of the Hosts within that HostGroup
      *
      * @param hostGroupName
      * @throws CollageException
      */
     public void removeHostGroup(String hostGroupName) throws CollageException;
     

     /**
      * Deletes the Server (Device) with the name provided, including all its
	  * Hosts, Services and LogMessages
      *
      * @param serverIdent The IP or MAC address of the server (Device) to be deleted
      * @throws CollageException
      */
     public void removeServer(String serverIdent) throws CollageException;
     
     /**
      * acknowledgeEvent Updates an existing event entry by setting the acknowledged by
     * @param applicationType
     * @param typeRule
     * @param host
     * @param serviceDescription
     * @param acknowledgedBy
     * @param acknowledgeComment
     * @throws CollageException
     */
    public void acknowledgeEvent(String applicationType, String typeRule, String host, String serviceDescription, String acknowledgedBy, String acknowledgeComment) throws CollageException;
     

}
