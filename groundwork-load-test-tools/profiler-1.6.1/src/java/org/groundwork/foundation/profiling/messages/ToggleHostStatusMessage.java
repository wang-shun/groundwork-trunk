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

public class ToggleHostStatusMessage extends WorkloadMessage {
	// SQL Statements
	private static final String PSTMT_CHECK_UPDATED = "SELECT Count(*) As NumUpdated from Host h INNER JOIN HostStatus hs ON h.HostID = hs.HostStatusID INNER JOIN MonitorStatus ms ON hs.MonitorStatusID = ms.MonitorStatusID ";

	// Host Status Message Format
	// We use string format to replace the host name, last check time and
	// monitor status values
	private static final String MSG_HOST_STATUS = "<HOST_STATUS MonitorServerName=\"localhost\" Host=\"%1$s\" Identification=\"127.0.0.1\" CheckTypeID=\"0\" CurrentNotificationNumber=\"0\" LastCheckTime=\"%2$s\" LastNotificationTime=\"0\" LastPluginOutput=\"\" LastStateChange=\"0000-00-00 00:00:00\" MonitorStatus=\"%3$s\" PercentStateChange=\"0.00\" ScheduledDowntimeDepth=\"0\" TimeDown=\"0\" TimeUnreachable=\"0\" TimeUp=\"0\" isAcknowledged=\"0\" isChecksEnabled=\"0\" isEventHandlersEnabled=\"0\" isFailurePredictionEnabled=\"0\" isFlapDetectionEnabled=\"0\" isHostIsFlapping=\"0\" isNotificationsEnabled=\"0\" isPassiveChecksEnabled=\"0\" isProcessPerformanceData=\"0\" />";

	private static final String HOST_NAME_FORMAT = "THSM_HST_%1$s_%2$d_%3$d";

	private static final String STATUS_UP = "UP";

	private static final String STATUS_DOWN = "DOWN";

	// Number of hosts to create and receive status messages
	private int _numHosts = 10;

	// Time updated has been completed.
	private long _updateTime = 0;

	// Where clause built up from number of hosts
	private String _whereClause = null;

	// Last check time used to confirm update
	private String _lastCheckTime = null;

	// Monitor status used to confirm update
	private String _monitorStatus = null;

	// Log
	protected static Log log = LogFactory.getLog(ToggleHostStatusMessage.class);

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
	private ToggleHostStatusMessage(int workloadId, int batchCount,
			int numHosts, String name, long threshold, MessageSocketInfo messageSocketInfo,
			Connection dbProfilerConnection, Connection dbSourceConnection,
			long deltaTime) {
		super(workloadId, batchCount, name, threshold, messageSocketInfo,
				dbProfilerConnection, dbSourceConnection, deltaTime);

		_numHosts = numHosts;

		// Build up where clause
		StringBuilder sb = new StringBuilder(
				"WHERE ms.Name = ? AND hs.LastCheckTime = ? AND (");
		String hostNameClause = "h.HostName = '";
		for (int i = 0; i < _numHosts; i++) {

			if (i > 0)
				sb.append(OR);

			sb.append(hostNameClause);
			sb.append(buildHostName(_name, _workloadId, i));
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
	public ToggleHostStatusMessage(Node messageNode, NamedNodeMap attributes)
			throws ProfilerException {
		super(messageNode, attributes);

		// Parse out specific attributes
		Node numHostsNode = attributes.getNamedItem("numHosts");
		if (numHostsNode == null) {
			throw new ProfilerException(
					"numHosts attribute is missing from ToggleHostStatusMessage.");
		}

		String numHosts = numHostsNode.getNodeValue();
		try {
			_numHosts = Integer.parseInt(numHosts);
		} catch (Exception e) {
			throw new ProfilerException(
					"ToggleHostStatusMessage.ctor - Invalid numHosts setting."
							+ numHosts);
		}
	}

	public int getNumHosts() {
		return _numHosts;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(32);
		sb.append("Num Hosts: ");
		sb.append(_numHosts);
		sb.append(", ");

		sb.append(super.toString());

		return sb.toString();
	}

	public String buildMessage() throws ProfilerException {
		StringBuilder sb = new StringBuilder(MSG_HOST_STATUS.length()
				* _numHosts);

		String hostName = null;
		_monitorStatus = STATUS_UP;
		_lastCheckTime = SQL_DATE_FORMAT.format(new Date());

		// Toggle status based on batch count
		if ((_batchCount % 2) == 0) {
			_monitorStatus = STATUS_DOWN;
		}

		// Send bulk message for number of hosts
		String msg = null;
		for (int i = 0; i < _numHosts; i++) {
			hostName = buildHostName(_name, _workloadId, i);
			msg = String.format(MSG_HOST_STATUS, hostName, _lastCheckTime,
					_monitorStatus);

			sb.append(msg);
		}

		return sb.toString();
	}

	public boolean isUpdateComplete() throws ProfilerException {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			// Query Hosts Status Count with the monitor status that we set in
			// build message
			// NOTE: There may be an issue with another message batch is sent
			// toggle status
			// before we record the status change.
		   pstmt = _dbSourceConnection.prepareStatement(PSTMT_CHECK_UPDATED + _whereClause);
			pstmt.setString(1, _monitorStatus);
			pstmt.setString(2, _lastCheckTime);

			rs = pstmt.executeQuery();

			if (rs.next()) {
				int count = rs.getInt(1);

				if (count == _numHosts) {
					// Capture completion time and subtract time spent waiting
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
			Connection dbSourceConnection, long deltaTime) {
		return new ToggleHostStatusMessage(workloadId, batchCount, _numHosts,
				_name, _threshold, messageSocketInfo, dbProfilerConnection, dbSourceConnection,
				deltaTime);
	}

	private String buildHostName(String name, int workloadId, int i)
	{
		return String.format(HOST_NAME_FORMAT, name, workloadId, i);
	}
}