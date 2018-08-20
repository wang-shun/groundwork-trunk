/**
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2006  GroundWork Open Source Solutions info@itgroundwork.com

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
package org.groundwork.foundation.profiling.messages;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.profiling.IWorkloadMessage;
import org.groundwork.foundation.profiling.MessageSocketInfo;
import org.groundwork.foundation.profiling.WorkloadMessage;
import org.groundwork.foundation.profiling.exceptions.ProfilerException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class ToggleServiceStatusMessage extends WorkloadMessage {
	// SQL Statements
	private static final String PSTMT_CHECK_UPDATED = "SELECT Count(*) As NumUpdated from ServiceStatus ss INNER JOIN MonitorStatus ms ON ss.MonitorStatusID = ms.MonitorStatusID ";

	// Service Status Message Format
	// We use string format to replace the service description, LastCheckTime,
	// LastHardState, LastStateChange, MonitorStatus, NextCheckTime
	// 1 = Host / Device ID, 2 = Service Description, 3=Current System Time,
	// 4=Monitor Status
	private static final String MSG_SERVICE_STATUS = "<SERVICE_STATUS MonitorServerName=\"localhost\" Host=\"%1$s\""
			+ " Device=\"%1$s\" ServiceDescription=\"%2$s\""
			+ " CheckType=\"ACTIVE\" CurrentNotificationNumber=\"0\" ExecutionTime=\"6\""
			+ " LastCheckTime=\"%3$s\" LastHardState=\"%4$s\""
			+ " LastNotificationTime=\"0\" LastPluginOutput=\"Profiler Service Status Toggle\""
			+ " LastStateChange=\"%3$s\" Latency=\"321\""
			+ " MonitorStatus=\"%4$s\" NextCheckTime=\"%3$s\""
			+ " PercentStateChange=\"0.00\" RetryNumber=\"1\" ScheduledDowntimeDepth=\"0\""
			+ " StateType=\"HARD\" TimeCritical=\"0\" TimeOK=\"0\" TimeUnknown=\"0\""
			+ " TimeWarning=\"0\" isAcceptPassiveChecks=\"1\" isChecksEnabled=\"1\""
			+ " isEventHandlersEnabled=\"1\" isFailurePredictionEnabled=\"1\""
			+ " isFlapDetectionEnabled=\"1\" isNotificationsEnabled=\"1\""
			+ " isObsessOverService=\"1\" isProblemAcknowledged=\"0\""
			+ " isProcessPerformanceData=\"1\" isServiceFlapping=\"0\" />";

	private static final String SERVICE_DESCRIPTION_FORMAT = "TSSM_SVC_%1$s_%2$d_%3$d";

	private static final String HOST_DEVICE_FORMAT = "TSSM_DEV_%1$s_%2$d_%3$d";

	private static final String STATUS_OK = "OK";

	private static final String STATUS_CRITICAL = "CRITICAL";

	// Number of services to create and receive status messages
	private int _numServices = 10;

	// Time updated has been completed.
	private long _updateTime = 0;

	// Where clause built up from number of hosts
	private String _whereClause = null;

	// Last check time used to confirm update
	private String _lastCheckTime = null;

	// Monitor status used to confirm update
	private String _monitorStatus = null;

	// Log
	protected static Log log = LogFactory
			.getLog(ToggleServiceStatusMessage.class);

	/**
	 * Factory constructor for "runnable" instances
	 * 
	 * @param workloadId
	 * @param numHosts
	 * @param name
	 * @param messageSocket
	 * @param dbProfilerConnection
	 * @param dbSourceConnection
	 */
	private ToggleServiceStatusMessage(int workloadId, int batchCount,
			int numServices, String name, long threshold, MessageSocketInfo messageSocketInfo,
			Connection dbProfilerConnection, Connection dbSourceConnection,
			long deltaTime) {
		super(workloadId, batchCount, 0, name, threshold, messageSocketInfo,
				dbProfilerConnection, dbSourceConnection, deltaTime);

		_numServices = numServices;

		// Build up where clause
		StringBuilder sb = new StringBuilder(
				"WHERE ms.Name = ? AND ss.LastCheckTime = ? AND (");
		String ssDescClause = "ss.ServiceDescription = '";
		for (int i = 0; i < _numServices; i++) {
			if (i > 0) {
				sb.append(OR);
			}

			sb.append(ssDescClause);
			sb.append(buildServiceDescription(_name, _workloadId, i));
			sb.append(SINGLE_QUOTE);
		}

		sb.append(CLOSE_PAREN);

		_whereClause = sb.toString();
	}

	/**
	 * Default constructor is required for all WorkloadMessages with the
	 * NamedNodeMap parameter
	 * 
	 * @param attributes
	 * @throws ProfilerException
	 */
	public ToggleServiceStatusMessage(Node messageNode, NamedNodeMap attributes)
			throws ProfilerException {
		super(messageNode, attributes);

		// Parse out specific attributes
		Node numServicesNode = attributes.getNamedItem("numServices");
		if (numServicesNode == null) {
			throw new ProfilerException(
					"numHosts attribute is missing from ToggleHostStatusMessage.");
		}

		String numServices = numServicesNode.getNodeValue();
		try {
			_numServices = Integer.parseInt(numServices);
		} catch (Exception e) {
			throw new ProfilerException(
					"ToggleServiceStatusMessage.ctor - Invalid numServices setting."
							+ numServices);
		}
	}

	public int getNumServices() {
		return _numServices;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(32);
		sb.append("Num Services: ");
		sb.append(_numServices);
		sb.append(", ");

		sb.append(super.toString());

		return sb.toString();
	}

	public String buildMessage() throws ProfilerException {
		StringBuilder sb = new StringBuilder(MSG_SERVICE_STATUS.length()
				* _numServices);

		String serviceName = null;
		String hostName = null;
		// cpora - milliseconds are important for display
		_lastCheckTime = SQL_DATE_FORMAT2.format(new Date());
		_monitorStatus = STATUS_OK;

		// Toggle status based on batch count
		if (_batchCount % 2 == 0) {
			_monitorStatus = STATUS_CRITICAL;
		}

		// Send bulk message for number of services
		String msg = null;
		for (int i = 0; i < _numServices; i++) {
			
			// We change host and device every 7 services.  This will avoid queuing of all batch matches
			// b/c of a device and / or host match
			if (i % 7 == 0)
			{
				// Setup host / device id to use
				hostName = String.format(HOST_DEVICE_FORMAT, _name, _workloadId, i); 
			}
			
			serviceName = buildServiceDescription(_name, _workloadId, i);
			msg = String.format(MSG_SERVICE_STATUS, hostName, serviceName,
					_lastCheckTime, _monitorStatus);

			sb.append(msg);
		}

		return sb.toString();
	}

	public boolean isUpdateComplete() throws ProfilerException {
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			// Query Service Status Count with the monitor status that we set in
			// build message
			// NOTE: There may be an issue with another message batch is sent
			// toggle status
			// before we record the status change.
			pstmt = _dbSourceConnection.prepareStatement(PSTMT_CHECK_UPDATED + _whereClause);
			pstmt.setString(1, _monitorStatus);
			// cpora - no milliseconds are necessary
			_lastCheckTime = SQL_DATE_FORMAT.format(new Date());
			pstmt.setString(2, _lastCheckTime);

			rs = pstmt.executeQuery();

			if (rs.next()) {
				int count = rs.getInt(1);

				if (count == _numServices) {
					_updateTime = System.currentTimeMillis();
					return true;
				}

				return false;
			} else {
				throw new ProfilerException(
						"Unable to check if update is complete.");
			}
		} catch (Exception e) {
			throw new ProfilerException(
					"Error checking for whether update is complete.", e);
		}
		finally {
			if (rs != null) 
			{
				try {
					rs.close();
				} catch (Exception e) {
					log.error("Error closing result set", e);
				}
				rs = null;
			}
			
			if (pstmt != null)
			{
				try {
					pstmt.close();
				}
				catch (Exception e)
				{
					log.error("Error closing prepared stmt.", e);
				}
				pstmt = null;
			}
		}		
	}

	public Timestamp captureMetrics() throws ProfilerException {
		return new Timestamp(_updateTime);
	}

	/**
	 * Clones this instance allowing it to run on a separate thread. Note - It
	 * is sharing message socket and database connections
	 */
	public IWorkloadMessage getRunnableInstance(int workloadId, int batchCount,
			MessageSocketInfo messageSocketInfo, Connection dbProfilerConnection,
			Connection dbSourceConnection, long deltaTime,
			int msgCount) {
		return new ToggleServiceStatusMessage(workloadId, batchCount,
				_numServices, _name, _threshold, messageSocketInfo, dbProfilerConnection,
				dbSourceConnection, deltaTime);
	}

	public int getCheckCount()
	{
		return _numServices;
	}
	
	private String buildServiceDescription(String name, int workloadId, int i) {
		return String.format(SERVICE_DESCRIPTION_FORMAT, name, workloadId, i);
	}
}