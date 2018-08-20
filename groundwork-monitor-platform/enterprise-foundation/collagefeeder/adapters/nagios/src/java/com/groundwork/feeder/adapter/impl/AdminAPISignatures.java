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

package com.groundwork.feeder.adapter.impl;

/**
 * 
 * AdminAPISignatures
 * @author <a href="mailto:rruttimann@itgroundwork.com"> Roger Ruttimann</a>
 * @version $Id: AdminAPISignatures.java 19298 2012-08-10 18:27:28Z rruttimann $
 * 
 * Signatures of API calls into CollageAdmin
 */
public class AdminAPISignatures {

    static String[] updateLogMessage	= new String[] {"LogType",
																						"MonitorServerName", 
																						"Host",
																						"Device",
																						"Severity",
																						"MonitorStatus",
																						"TextMessage",
																						"ReportDate",
																						"LastInsertDate",
																						"SubComponent",
																						"ErrorType",
																						"ServiceDescription",		// lookup the ServiceStatus
																						"ServiceStatus",
																						"LoggerName",
																						"ApplicationName",
																						"FirstInsertDate"
};
    static String[] updateHostStatus		= new String[] {"MonitorServerName", 
																						"Host",
																						"Device",
																						"LastPluginOutput",
																						"MonitorStatus",
																						"LastCheckTime",
																						"LastStateChange",
																						"isAcknowledged",	    
																						"TimeUp",
																						"TimeDown",
																						"TimeUnreachable",
																						"LastNotificationTime",
																						"CurrentNotificationNumber",
																						"isNotificationsEnabled",  
																						"isChecksEnabled",
																						"isEventHandlersEnabled",
																						"isFlapDetectionEnabled",
																						"isHostIsFlapping",
																						"PercentStateChange",
																						"ScheduledDowntimeDepth",
																						"isFailurePredictionEnabled",
																						"isProcessPerformanceData",
																						"CheckType",
																						"Latency",
																			            "ExecutionTime",
																			            "isPassiveChecksEnabled",
																			            "PerformanceData"
	};
    static String[] updateServiceStatus	= new String[] {"MonitorServerName",
																			            "Host",
																			            "Device", 
																			            "ServiceDescription",
																			            "LastPluginOutput",
																			            "MonitorStatus",
																			            "RetryNumber",
																			            "StateType",
																			            "LastCheckTime",
																			            "NextCheckTime",
																			            "CheckType",
																			            "isChecksEnabled",
																			            "isAcceptPassiveChecks",
																			            "isEventHandlersEnabled",
																			            "LastStateChange",
																			            "isProblemAcknowledged",
																			            "LastHardState",
																			            "TimeOK",
																			            "TimeUnknown",
																			            "TimeWarning",
																			            "TimeCritical",
																			            "LastNotificationTime",
																			            "CurrentNotificationNumber",
																			            "isNotificationsEnabled",
																			            "Latency",
																			            "ExecutionTime",
																			            "isFlapDetectionEnabled",
																			            "isServiceFlapping",
																			            "PercentStateChange",
																			            "ScheduledDowntimeDepth",
																			            "isFailurePredictionEnabled",
																			            "isProcessPerformanceData",
																			            "isObsessOverService",
																			            "PerformanceData"
            };
    
    /**
     * ServiceStatus signature for bulk insert
     */
    static String[] updateServiceStatusBulk	= new String[] {"MonitorServerName",
																					            "Host",
																					            "Device"
    };
    
    static String[] createServiceStatus = new String[] {"ServiceDescription",
																			            "LastPluginOutput",
																			            "MonitorStatus",
																			            "RetryNumber",
																			            "StateType",
																			            "LastCheckTime",
																			            "NextCheckTime",
																			            "CheckType",
																			            "isChecksEnabled",
																			            "isAcceptPassiveChecks",
																			            "isEventHandlersEnabled",
																			            "LastStateChange",
																			            "isProblemAcknowledged",
																			            "LastHardState",
																			            "TimeOK",
																			            "TimeUnknown",
																			            "TimeWarning",
																			            "TimeCritical",
																			            "LastNotificationTime",
																			            "CurrentNotificationNumber",
																			            "isNotificationsEnabled",
																			            "Latency",
																			            "ExecutionTime",
																			            "isFlapDetectionEnabled",
																			            "isServiceFlapping",
																			            "PercentStateChange",
																			            "ScheduledDowntimeDepth",
																			            "isFailurePredictionEnabled",
																			            "isProcessPerformanceData",
																			            "isObsessOverService"
};
    
    /**
     * Host/Service Availability
     * 
     */
    static String[] updateHostAvailability	= new String[] {"Host",
            "StartTime",
            "EndTime",
            "PERCENT_KNOWN_TIME_UP_UNSCHEDULED",
            "TOTAL_TIME_UNDETERMINED",
            "PERCENT_TIME_DOWN_UNSCHEDULED",
            "PERCENT_KNOWN_TIME_UNREACHABLE",
            "PERCENT_TOTAL_TIME_UNREACHABLE",
            "PERCENT_TIME_DOWN_SCHEDULED",
            "TOTAL_TIME_DOWN",
            "PERCENT_KNOWN_TIME_DOWN_UNSCHEDULED",
            "PERCENT_TOTAL_TIME_UNDETERMINED",
            "PERCENT_KNOWN_TIME_UNREACHABLE_SCHEDULED",
            "TIME_UP_SCHEDULED",
            "PERCENT_TIME_UP_UNSCHEDULED",
            "TIME_DOWN_UNSCHEDULED",
            "PERCENT_KNOWN_TIME_UP_SCHEDULED",
            "PERCENT_TIME_UNREACHABLE_SCHEDULED",
            "PERCENT_KNOWN_TIME_UP",
            "PERCENT_KNOWN_TIME_DOWN_SCHEDULED",
            "PERCENT_KNOWN_TIME_UNREACHABLE_UNSCHEDULED",
            "PERCENT_TIME_UNREACHABLE_UNSCHEDULED",
            "PERCENT_TOTAL_TIME_DOWN",
            "PERCENT_TIME_UP_SCHEDULED",
            "TIME_UP_UNSCHEDULED",
            "PERCENT_TOTAL_TIME_UP",
            "TIME_UNDETERMINED_NO_DATA",
            "TOTAL_TIME_UP",
            "PERCENT_TIME_UNDETERMINED_NOT_RUNNING",
            "TIME_DOWN_SCHEDULED",
            "TIME_UNREACHABLE_UNSCHEDULED",
            "TIME_UNREACHABLE_SCHEDULED",
            "PERCENT_TIME_UNDETERMINED_NO_DATA",
            "TOTAL_TIME_UNREACHABLE",
            "PERCENT_KNOWN_TIME_DOWN",
            "TIME_UNDETERMINED_NOT_RUNNING"
    };
    
    static String[] updateServiceAvailability	= new String[] {"Host",
            "SERVICE_DESCRIPTION",
            "StartTime",
            "EndTime",
            "TOTAL_TIME_OK",
            "PERCENT_KNOWN_TIME_OK_UNSCHEDULED",
            "TOTAL_TIME_UNDETERMINED",
            "TOTAL_TIME_UNKNOWN",
            "TOTAL_TIME_CRITICAL",
            "PERCENT_KNOWN_TIME_UNKNOWN",
            "PERCENT_KNOWN_TIME_WARNING",
            "PERCENT_TOTAL_TIME_UNDETERMINED",
            "PERCENT_TIME_UNKNOWN_UNSCHEDULED",
            "TIME_UNKNOWN_SCHEDULED",
            "PERCENT_TIME_CRITICAL_SCHEDULED",
            "PERCENT_TIME_WARNING_SCHEDULED",
            "TIME_UNKNOWN_UNSCHEDULED",
            "PERCENT_TIME_OK_SCHEDULED",
            "PERCENT_TOTAL_TIME_WARNING",
            "TIME_OK_UNSCHEDULED",
            "TOTAL_TIME_WARNING",
            "TIME_CRITICAL_SCHEDULED",
            "PERCENT_TIME_UNKNOWN_SCHEDULED",
            "PERCENT_KNOWN_TIME_CRITICAL_UNSCHEDULED",
            "PERCENT_TIME_WARNING_UNSCHEDULED",
            "PERCENT_KNOWN_TIME_CRITICAL_SCHEDULED",
            "TIME_CRITICAL_UNSCHEDULED",
            "TIME_UNDETERMINED_NO_DATA",
            "PERCENT_TOTAL_TIME_CRITICAL",
            "PERCENT_TIME_UNDETERMINED_NOT_RUNNING",
            "TIME_WARNING_UNSCHEDULED",
            "PERCENT_KNOWN_TIME_WARNING_UNSCHEDULED",
            "PERCENT_TOTAL_TIME_UNKNOWN",
            "PERCENT_TIME_CRITICAL_UNSCHEDULED",
            "PERCENT_KNOWN_TIME_WARNING_SCHEDULED",
            "PERCENT_TOTAL_TIME_OK",
            "PERCENT_KNOWN_TIME_OK_SCHEDULED",
            "PERCENT_KNOWN_TIME_UNKNOWN_UNSCHEDULED",
            "PERCENT_KNOWN_TIME_OK",
            "TIME_OK_SCHEDULED",
            "PERCENT_TIME_UNDETERMINED_NO_DATA",
            "PERCENT_KNOWN_TIME_CRITICAL",
            "PERCENT_TIME_OK_UNSCHEDULED",
            "PERCENT_KNOWN_TIME_UNKNOWN_SCHEDULED",
            "TIME_WARNING_SCHEDULED",
            "TIME_UNDETERMINED_NOT_RUNNING"
      };
    
    static String[] updateHostGroupHostAvailability = new String[] {
            "Hostgroup",
            "StartTime",
            "EndTime",
            "PERCENT_KNOWN_TIME_UP_UNSCHEDULED",
            "TOTAL_TIME_UNDETERMINED",
            "PERCENT_TIME_DOWN_UNSCHEDULED",
            "PERCENT_KNOWN_TIME_UNREACHABLE",
            "PERCENT_TOTAL_TIME_UNREACHABLE",
            "PERCENT_TIME_DOWN_SCHEDULED",
            "TOTAL_TIME_DOWN",
            "PERCENT_KNOWN_TIME_DOWN_UNSCHEDULED",
            "PERCENT_TOTAL_TIME_UNDETERMINED",
            "PERCENT_KNOWN_TIME_UNREACHABLE_SCHEDULED",
            "TIME_UP_SCHEDULED",
            "PERCENT_TIME_UP_UNSCHEDULED",
            "TIME_DOWN_UNSCHEDULED",
            "PERCENT_KNOWN_TIME_UP_SCHEDULED",
            "PERCENT_TIME_UNREACHABLE_SCHEDULED",
            "PERCENT_KNOWN_TIME_UP",
            "PERCENT_KNOWN_TIME_DOWN_SCHEDULED",
            "PERCENT_KNOWN_TIME_UNREACHABLE_UNSCHEDULED",
            "PERCENT_TIME_UNREACHABLE_UNSCHEDULED",
            "PERCENT_TOTAL_TIME_DOWN",
            "PERCENT_TIME_UP_SCHEDULED",
            "TIME_UP_UNSCHEDULED",
            "PERCENT_TOTAL_TIME_UP",
            "TIME_UNDETERMINED_NO_DATA",
            "TOTAL_TIME_UP",
            "PERCENT_TIME_UNDETERMINED_NOT_RUNNING",
            "TIME_DOWN_SCHEDULED",
            "TIME_UNREACHABLE_UNSCHEDULED",
            "TIME_UNREACHABLE_SCHEDULED",
            "PERCENT_TIME_UNDETERMINED_NO_DATA",
            "TOTAL_TIME_UNREACHABLE",
            "PERCENT_KNOWN_TIME_DOWN",
            "TIME_UNDETERMINED_NOT_RUNNING"
            
    };
    
    static String[]  updateHostGroupServiceAvailability = new String[] { 
            "Hostgroup",
            "SERVICE_DESCRIPTION",
            "StartTime",
            "EndTime",
            "TOTAL_TIME_OK",
            "PERCENT_KNOWN_TIME_OK_UNSCHEDULED",
            "TOTAL_TIME_UNKNOWN",
            "TOTAL_TIME_UNDETERMINED",
            "TOTAL_TIME_CRITICAL",
            "PERCENT_KNOWN_TIME_UNKNOWN",
            "PERCENT_KNOWN_TIME_WARNING",
            "TIME_UNKNOWN_SCHEDULED",
            "PERCENT_TOTAL_TIME_UNDETERMINED",
            "PERCENT_TIME_UNKNOWN_UNSCHEDULED",
            "PERCENT_TIME_WARNING_SCHEDULED",
            "PERCENT_TIME_CRITICAL_SCHEDULED",
            "TIME_UNKNOWN_UNSCHEDULED",
            "PERCENT_TIME_OK_SCHEDULED",
            "PERCENT_TOTAL_TIME_WARNING",
            "TIME_OK_UNSCHEDULED",
            "TOTAL_TIME_WARNING",
            "TIME_CRITICAL_SCHEDULED",
            "PERCENT_TIME_UNKNOWN_SCHEDULED",
            "PERCENT_KNOWN_TIME_CRITICAL_UNSCHEDULED",
            "PERCENT_KNOWN_TIME_CRITICAL_SCHEDULED",
            "PERCENT_TIME_WARNING_UNSCHEDULED",
            "TIME_CRITICAL_UNSCHEDULED",
            "TIME_UNDETERMINED_NO_DATA",
            "PERCENT_TOTAL_TIME_CRITICAL",
            "TIME_WARNING_UNSCHEDULED",
            "PERCENT_TIME_UNDETERMINED_NOT_RUNNING",
            "PERCENT_KNOWN_TIME_WARNING_UNSCHEDULED",
            "PERCENT_TOTAL_TIME_UNKNOWN",
            "PERCENT_KNOWN_TIME_WARNING_SCHEDULED",
            "PERCENT_TIME_CRITICAL_UNSCHEDULED",
            "PERCENT_TOTAL_TIME_OK",
            "PERCENT_KNOWN_TIME_OK_SCHEDULED",
            "PERCENT_KNOWN_TIME_UNKNOWN_UNSCHEDULED",
            "PERCENT_KNOWN_TIME_OK",
            "TIME_OK_SCHEDULED",
            "PERCENT_TIME_UNDETERMINED_NO_DATA",
            "PERCENT_KNOWN_TIME_CRITICAL",
            "PERCENT_TIME_OK_UNSCHEDULED",
            "PERCENT_KNOWN_TIME_UNKNOWN_SCHEDULED",
            "TIME_WARNING_SCHEDULED",
            "TIME_UNDETERMINED_NOT_RUNNING"    
    };
    
 
}
