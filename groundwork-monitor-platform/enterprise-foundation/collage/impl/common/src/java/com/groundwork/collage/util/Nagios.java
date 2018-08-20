/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com

 *	 This program is free software; you can redistribute it and/or modify
 *	 it under the terms of version 2 of the GNU General Public License
 *	 as published by the Free Software Foundation.

 *	 This program is distributed in the hope that it will be useful,
 *	 but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	 GNU General Public License for more details.

 *	 You should have received a copy of the GNU General Public License
 *	 along with this program; if not, write to the Free Software
 *	 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.groundwork.collage.util;

import java.util.Properties;

import com.groundwork.collage.exception.CollageException;
import com.groundwork.collage.model.HostStatus;
import com.groundwork.collage.model.ServiceStatus;
import com.groundwork.collage.model.LogMessage;

/**
 * This class simply holds an enumeration of static constants that can be used
 * to refer to properties and metrics defined for NAGIOS application types in
 * the metadata; it also contains utility classes to create suitable Properties
 * maps for different entities
 * 
 * @author  <a href="mailto:philippe.paravicini@eCommerceStudio.com">Philippe Paravicini</a>
 * @version $Revision: 19298 $ - $Date: 2012-08-10 11:27:28 -0700 (Fri, 10 Aug 2012) $
 */
public class Nagios
{
	public final static String APPLICATION_TYPE              = "NAGIOS";
	public final static String LOG_TYPE                      = "NAGIOS";
	public final static String LAST_PLUGIN_OUTPUT            = "LastPluginOutput";
	public final static String LAST_STATE_CHANGE             = "LastStateChange";
	public final static String RETRY_NUMBER                  = "RetryNumber";
	public final static String IS_CHECKS_ENABLED             = "isChecksEnabled";
	public final static String IS_ACCEPT_PASSIVE_CHECKS      = "isAcceptPassiveChecks";
	public final static String IS_EVENT_HANDLERS_ENABLED     = "isEventHandlersEnabled";
	public final static String IS_ACKNOWLEDGED               = "isAcknowledged";
	public final static String IS_PROBLEM_ACKNOWLEDGED       = "isProblemAcknowledged";
	public final static String TIME_OK                       = "TimeOK";
	public final static String TIME_UNKNOWN                  = "TimeUnknown";
	public final static String TIME_WARNING                  = "TimeWarning";
	public final static String TIME_CRITICAL                 = "TimeCritical";
	public final static String TIME_UP                       = "TimeUp";
	public final static String TIME_DOWN                     = "TimeDown";
	public final static String TIME_UNREACHABLE              = "TimeUnreachable";
	public final static String LAST_NOTIFICATION_TIME        = "LastNotificationTime";
	public final static String CURRENT_NOTIFICATION_NUMBER   = "CurrentNotificationNumber";
	public final static String IS_NOTIFICATIONS_ENABLED      = "isNotificationsEnabled";
	public final static String LATENCY                       = "Latency";
	public final static String EXECUTION_TIME                = "ExecutionTime";
	public final static String IS_FLAP_DETECTION_ENABLED     = "isFlapDetectionEnabled";
	public final static String IS_HOST_FLAPPING              = "isHostFlapping";
	public final static String IS_SERVICE_FLAPPING           = "isServiceFlapping";
	public final static String PERCENT_STATE_CHANGE          = "PercentStateChange";
	public final static String SCHEDULED_DOWNTIME_DEPTH      = "ScheduledDowntimeDepth";
	public final static String IS_FAILURE_PREDICTION_ENABLED = "isFailurePredictionEnabled";
	public final static String IS_PROCESS_PERFORMANCE_DATA   = "isProcessPerformanceData";
	public final static String IS_OBSESS_OVER_SERVICE        = "isObsessOverService";
	public final static String THIRTY_DAY_MOVING_AVG         = "30DayMovingAvg";
	public final static String SUB_COMPONENT                 = "SubComponent";
	public final static String ERROR_TYPE                    = "ErrorType";
	public final static String SERVICE_DESCRIPTION           = "ServiceDescription";
	public final static String LOGGER_NAME                   = "LoggerName";
	public final static String APP_NAME                      = "ApplicationName";
	public final static String APP_CODE                      = "ApplicationCode";
	public final static String IS_PASSIVE_CHECKS_ENABLED 	= "isPassiveChecksEnabled";
	public final static String PERFORMANCE_DATA          	= "PerformanceData";


	/**
	 * this method generates a map of key-value pairs suitable for updating
	 * the HostStatus properties of a Host monitored by Nagios (NAGIOS application type)
	 */
	public static Properties createHostStatusProps(
			String LastPluginOutput,
			String MonitorStatus, String LastCheckTime, String LastStateChange,
			String isAcknowledged, String TimeUp, String TimeDown,
			String TimeUnreachable, String LastNotificationTime,
			String CurrentNotificationNumber, String isNotificationsEnabled,
			String isChecksEnabled, String isEventHandlersEnabled,
			String isFlapDetectionEnabled, String isHostFlapping,
			String PercentStateChange, String ScheduledDowntimeDepth,
			String isFailurePredictionEnabled, String isProcessPerformanceData,
			String CheckType , String Latency ,String ExecutionTime, String isPassiveChecksEnabled, String PerformanceData) 
	{
		Properties props = new Properties();

		props.setProperty( LAST_PLUGIN_OUTPUT,             LastPluginOutput);
		props.setProperty( HostStatus.EP_MONITOR_STATUS_NAME,  MonitorStatus);
		props.setProperty( HostStatus.EP_LAST_CHECK_TIME, LastCheckTime);
		props.setProperty( LAST_STATE_CHANGE,              LastStateChange);
		props.setProperty( IS_ACKNOWLEDGED,                isAcknowledged);
		props.setProperty( TIME_UP,                        TimeUp);
		props.setProperty( TIME_DOWN,                      TimeDown);
		props.setProperty( TIME_UNREACHABLE,               TimeUnreachable);
		props.setProperty( LAST_NOTIFICATION_TIME,         LastNotificationTime);
		props.setProperty( CURRENT_NOTIFICATION_NUMBER,    CurrentNotificationNumber);
		props.setProperty( IS_NOTIFICATIONS_ENABLED,       isNotificationsEnabled);
		props.setProperty( IS_CHECKS_ENABLED,              isChecksEnabled);
		props.setProperty( IS_EVENT_HANDLERS_ENABLED,      isEventHandlersEnabled);
		props.setProperty( IS_FLAP_DETECTION_ENABLED,      isFlapDetectionEnabled);
		props.setProperty( IS_HOST_FLAPPING,               isHostFlapping);
		props.setProperty( PERCENT_STATE_CHANGE,           PercentStateChange);
		props.setProperty( SCHEDULED_DOWNTIME_DEPTH,       ScheduledDowntimeDepth);
		props.setProperty( IS_FAILURE_PREDICTION_ENABLED,  isFailurePredictionEnabled);
		props.setProperty( IS_PROCESS_PERFORMANCE_DATA,    isProcessPerformanceData);
		props.setProperty( PERFORMANCE_DATA,			   PerformanceData);
		
		
		// New for Nagios 2.0 -- Any of these values can be null
		if (CheckType != null)
			props.setProperty( HostStatus.EP_CHECK_TYPE_NAME,      CheckType);
		else
			props.setProperty( HostStatus.EP_CHECK_TYPE_NAME,      "0");	
		
		if (Latency != null)
			props.setProperty( LATENCY,                               Latency);
		else
			props.setProperty( LATENCY,                               "0");
		
		if (ExecutionTime != null)
			props.setProperty( EXECUTION_TIME,                        ExecutionTime);
		else
			props.setProperty( EXECUTION_TIME,                        "0");
		
		if (isPassiveChecksEnabled != null)
			props.setProperty( IS_PASSIVE_CHECKS_ENABLED,  isPassiveChecksEnabled);
		else
			props.setProperty( IS_PASSIVE_CHECKS_ENABLED,  "0");
		
		
		return props;
	}


	/** 
	 * minimal paramaters to perform a HostStatus update 
	 * - mostly intended for testing purposes
	 */
	public static Properties createHostStatusPropsMinimal(
			String MonitorStatus, 
			String LastCheckTime)
	{
		Properties props = new Properties();

		props.setProperty( HostStatus.EP_MONITOR_STATUS_NAME,  MonitorStatus);
		props.setProperty( HostStatus.EP_CHECK_TYPE_NAME, LastCheckTime);

		return props;
	}


	/** 
	 * this method generates a map of key-value pairs suitable for updating
	 * the ServiceStatus of a Service monitored by Nagios (NAGIOS application type)
	 */
	public static Properties createServiceStatusProps(
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
			String isProcessPerformanceData, String isObsessOverService, String PerformanceData) 
	{
		Properties props = createServiceStatusPropsMinimal(ServiceDescription,
										MonitorStatus, 
										StateType, 
										CheckType, 										 
										LastHardState);
			
		if (LastPluginOutput != null && LastPluginOutput.length() > 0)
			props.setProperty( LAST_PLUGIN_OUTPUT,                    LastPluginOutput);

		if (RetryNumber != null && RetryNumber.length() > 0)
			props.setProperty( RETRY_NUMBER,                          RetryNumber);
				
		if (LastCheckTime != null && LastCheckTime.length() > 0)
			props.setProperty( ServiceStatus.EP_LAST_CHECK_TIME,     LastCheckTime);
		
		if (NextCheckTime != null && NextCheckTime.length() > 0)
			props.setProperty( ServiceStatus.EP_NEXT_CHECK_TIME,     NextCheckTime);
		
		if (isChecksEnabled != null && isChecksEnabled.length() > 0)
			props.setProperty( IS_CHECKS_ENABLED,                     isChecksEnabled);

		if (isAcceptPassiveChecks != null && isAcceptPassiveChecks.length() > 0)
			props.setProperty( IS_ACCEPT_PASSIVE_CHECKS,              isAcceptPassiveChecks);
		
		if (isEventHandlersEnabled != null && isEventHandlersEnabled.length() > 0)
			props.setProperty( IS_EVENT_HANDLERS_ENABLED,             isEventHandlersEnabled);
		
		if (LastStateChange != null && LastStateChange.length() > 0)
			props.setProperty( ServiceStatus.EP_LAST_STATE_CHANGE,   LastStateChange);
		
		if (isProblemAcknowledged != null && isProblemAcknowledged.length() > 0)
			props.setProperty( IS_PROBLEM_ACKNOWLEDGED,               isProblemAcknowledged);

		if (TimeOK != null && TimeOK.length() > 0)
			props.setProperty( TIME_OK,                               TimeOK);
		
		if (TimeUnknown != null && TimeUnknown.length() > 0)
			props.setProperty( TIME_UNKNOWN,                          TimeUnknown);
		
		if (TimeWarning != null && TimeWarning.length() > 0)
			props.setProperty( TIME_WARNING,                          TimeWarning);
		
		if (TimeCritical != null && TimeCritical.length() > 0)
			props.setProperty( TIME_CRITICAL,                         TimeCritical);
		
		if (LastNotificationTime != null && LastNotificationTime.length() > 0)
			props.setProperty( LAST_NOTIFICATION_TIME,                LastNotificationTime);
		
		if (CurrentNotificationNumber != null && CurrentNotificationNumber.length() > 0)
			props.setProperty( CURRENT_NOTIFICATION_NUMBER,           CurrentNotificationNumber);
		
		if (isNotificationsEnabled != null && isNotificationsEnabled.length() > 0)
			props.setProperty( IS_NOTIFICATIONS_ENABLED,              isNotificationsEnabled);
		
		if (Latency != null && Latency.length() > 0)
			props.setProperty( LATENCY,                               Latency);
		
		if (ExecutionTime != null && ExecutionTime.length() > 0)
			props.setProperty( EXECUTION_TIME,                        ExecutionTime);
		
		if (isFlapDetectionEnabled != null && isFlapDetectionEnabled.length() > 0)
			props.setProperty( IS_FLAP_DETECTION_ENABLED,             isFlapDetectionEnabled);
		
		if (isServiceFlapping != null && isServiceFlapping.length() > 0)
			props.setProperty( IS_SERVICE_FLAPPING,                   isServiceFlapping);
		
		if (PercentStateChange != null && PercentStateChange.length() > 0)
			props.setProperty( PERCENT_STATE_CHANGE,                  PercentStateChange);
		
		if (ScheduledDowntimeDepth != null && ScheduledDowntimeDepth.length() > 0)
			props.setProperty( SCHEDULED_DOWNTIME_DEPTH,              ScheduledDowntimeDepth);
		
		if (isFailurePredictionEnabled != null && isFailurePredictionEnabled.length() > 0)
			props.setProperty( IS_FAILURE_PREDICTION_ENABLED,         isFailurePredictionEnabled);
		
		if (isProcessPerformanceData != null && isProcessPerformanceData.length() > 0)
			props.setProperty( IS_PROCESS_PERFORMANCE_DATA,           isProcessPerformanceData);
		
		if (isObsessOverService != null && isObsessOverService.length() > 0)
			props.setProperty( IS_OBSESS_OVER_SERVICE,                isObsessOverService);
		
		if (PerformanceData != null && PerformanceData.length() > 0)
			props.setProperty(PERFORMANCE_DATA, PerformanceData);

		return props;
	}


	/** 
	 * minimal paramaters to perform a ServiceStatus update 
	 * - mostly intended for testing purposes
	 */
	public static Properties createServiceStatusPropsMinimal(
			String ServiceDescription,
			String MonitorStatus, 
			String StateType, 
			String CheckType, 
			String LastHardState)
	{
		if (ServiceDescription == null || ServiceDescription.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty Service Description.");

		if (MonitorStatus == null || MonitorStatus.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty MonitorStatus.");

		if (StateType == null || StateType.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty StateType.");

		if (CheckType == null || CheckType.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty CheckType.");

		if (LastHardState == null || LastHardState.length() == 0)
			throw new IllegalArgumentException("Invalid null / empty LastHardState.");

		Properties props = new Properties();
		
		props.setProperty( ServiceStatus.EP_SERVICE_DESCRIPTION, ServiceDescription);
		props.setProperty( ServiceStatus.EP_MONITOR_STATUS_NAME,      MonitorStatus);
		props.setProperty( ServiceStatus.EP_STATE_TYPE_NAME,          StateType);
		props.setProperty( ServiceStatus.EP_CHECK_TYPE_NAME,          CheckType);
		props.setProperty( ServiceStatus.EP_LAST_HARD_STATE_NAME,     LastHardState);

		return props;
	}


	/** 
	 * this method generates a map of key-value pairs suitable for entering
	 * a LogMessage for a Device monitored by Nagios (NAGIOS application type);
	 */
	public static Properties createLogMessageProps(
            String hostName, String monitorStatus,
            String reportDate, String lastInsertDate,
            String subComponent, String errorType, String serviceDescr,
            String loggerName, String applicationName, String firstInsertDate, String txtMsg) 
    {
		Properties props = new Properties();
		
		// Required
		try
		{
		props.setProperty( LogMessage.EP_HOST_NAME,           hostName);
		props.setProperty( LogMessage.EP_MONITOR_STATUS_NAME,      monitorStatus);
		props.setProperty( LogMessage.EP_REPORT_DATE,         reportDate);
		}
		catch (Exception e)
		{
			throw new CollageException("Required properties not defined. HostName[" + hostName + "], MonitorStatus[" + monitorStatus +"] and ReportDate[" + reportDate + "] needs to be specified");
		}
		
		//	Optional
		if (firstInsertDate != null && firstInsertDate.length() > 0)
			props.setProperty( LogMessage.EP_FIRST_INSERT_DATE,    firstInsertDate);
		if (lastInsertDate != null && lastInsertDate.length() > 0)
			props.setProperty( LogMessage.EP_LAST_INSERT_DATE,    lastInsertDate);
		if (serviceDescr != null && serviceDescr.length() > 0)
			props.setProperty( LogMessage.EP_SERVICE_STATUS_DESCRIPTION, serviceDescr);
		if (subComponent != null && subComponent.length() > 0)
			props.setProperty( SUB_COMPONENT,                      subComponent);
		if (errorType != null && errorType.length() > 0)
			props.setProperty( ERROR_TYPE,                         errorType);
		if (loggerName != null && loggerName.length() > 0)
			props.setProperty( LOGGER_NAME,                        loggerName);
		if (applicationName != null && applicationName.length() > 0)
			props.setProperty( APP_NAME,                           applicationName);
		if (txtMsg != null && txtMsg.length() > 0)
			props.setProperty( "TextMessage",  txtMsg);
		
		return props;
    }
}
