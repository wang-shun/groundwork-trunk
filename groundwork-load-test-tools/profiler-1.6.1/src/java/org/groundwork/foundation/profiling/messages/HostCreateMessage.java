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
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.profiling.IWorkloadMessage;
import org.groundwork.foundation.profiling.MessageSocketInfo;
import org.groundwork.foundation.profiling.WorkloadMessage;
import org.groundwork.foundation.profiling.exceptions.ProfilerException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class HostCreateMessage extends WorkloadMessage
{
	
	
	// SQL Statements
	private static final String PSTMT_CHECK_UPDATED = "SELECT Count(*) As NumUpdated from Host h ";

	// Host and Device Create Message Format
	// We use string format to replace the host name, description, identification 
	private static String MSG_HOST_CREATE = 
		"<HOST HostName='%1$s' Description='Profiler Host Create - %1$s' Identification='%2$s' DeviceDisplay='' />";

	private static final String MSG_HOST_CREATE_4_5 = 
		"<HOST_STATUS MonitorServerName=\"localhost\" Host=\"%1$s\" Identification=\"%2$s\" " +
		"CheckTypeID=\"0\" CurrentNotificationNumber=\"0\" LastCheckTime=\"2006-10-09 00:00:00\" " +
		"LastNotificationTime=\"0\" LastPluginOutput=\"\" LastStateChange=\"0000-00-00 00:00:00\" " +
		"MonitorStatus=\"UP\" PercentStateChange=\"0.00\" ScheduledDowntimeDepth=\"0\" " +
		"TimeDown=\"0\" TimeUnreachable=\"0\" TimeUp=\"0\" isAcknowledged=\"0\" " +
		"isChecksEnabled=\"0\" isEventHandlersEnabled=\"0\" isFailurePredictionEnabled=\"0\" " +
		"isFlapDetectionEnabled=\"0\" isHostIsFlapping=\"0\" isNotificationsEnabled=\"0\" " +
		"isPassiveChecksEnabled=\"0\" isProcessPerformanceData=\"0\" />";

	// NOTE:  There must be a space before the ">" in the beginning tag for parsing reasons in the ProcessFeederData class
	private static String SystemConfigBegin = "<SYSTEM_CONFIG >";
	private static String SystemConfigEnd = "</SYSTEM_CONFIG>";
	
	private static final String HOST_NAME_FORMAT = "HCM_HST_%1$s_%2$d_%3$d_%4$d";
	private static final String DEVICE_NAME_FORMAT = "HCM_DVC_%1$s_%2$d_%3$d_%4$d";

	// Number of hosts to create and receive status messages
	private int _numHosts = 10;

	// Time updated has been completed.
	private long _updateTime = 0;
	
	// Boolean flag indicating whether message should be 45 or 50 version.  
	// TODO:  Allow for more versions to be supported
	private boolean _bUse45Version = false;
	
	// Where clause built up from number of hosts
	private String _whereClause = null;

	// Log
	protected static Log log = LogFactory.getLog(HostCreateMessage.class);

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
	private HostCreateMessage(int workloadId, int batchCount,
			int numHosts, String name, long threshold, MessageSocketInfo messageSocketInfo,
			Connection dbProfilerConnection, Connection dbSourceConnection,
			long deltaTime, boolean bUse45Version) 
	{
		super(workloadId, batchCount, name, threshold, messageSocketInfo,
				dbProfilerConnection, dbSourceConnection, deltaTime);

		_numHosts = numHosts;
		_bUse45Version = bUse45Version;
		
		_whereClause = buildWhereClause();
	}

	/**
	 * Default constructor is required for all WorkloadMessages with the
	 * NamedNodeMap parameter
	 * 
	 * @param attributes
	 * @throws ProfilerException
	 */
	public HostCreateMessage(Node messageNode, NamedNodeMap attributes)
			throws ProfilerException {
		super(messageNode, attributes);

		// Parse out specific attributes
		Node numHostsNode = attributes.getNamedItem("numHosts");
		if (numHostsNode == null) {
			throw new ProfilerException(
					"numHosts attribute is missing from HostCreateMessage.");
		}

		String numHosts = numHostsNode.getNodeValue();
		try {
			_numHosts = Integer.parseInt(numHosts);
		} catch (Exception e) {
			throw new ProfilerException(
					"HostCreateMessage.ctor - Invalid numHosts setting."
							+ numHosts);
		}
		
		Node versionNode = attributes.getNamedItem("version45");
		if (versionNode != null) {
			String use45Version = versionNode.getNodeValue();		
			try {
				_bUse45Version = Boolean.parseBoolean(use45Version);
			} catch (Exception e) {
				log.error("Invalid boolean value for version45 - Defaulting to false and using 5.0 version of HOST create message.");
			}
		}
	}

	public int getNumHosts() {
		return _numHosts;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(32);
		sb.append("Num Hosts: ");
		sb.append(_numHosts);
		sb.append(",  version45: ");
		sb.append(_bUse45Version);
		sb.append(", ");
		
		sb.append(super.toString());

		return sb.toString();
	}

	public String buildMessage() throws ProfilerException {
		StringBuilder sb = new StringBuilder((MSG_HOST_CREATE.length() * _numHosts) + (SystemConfigEnd.length() * 2)); 

		String hostName = null;
		String deviceName = null;
		
		// Send bulk message for number of hosts
		String msg = null;
		
		if (_bUse45Version == false)
			sb.append(SystemConfigBegin);
		
		for (int i = 0; i < _numHosts; i++) {
			hostName = buildHostName(_name, _workloadId, _batchCount, i);
			deviceName = buildDeviceName(_name, _workloadId, _batchCount, i);
			
			if (_bUse45Version == true)
				msg = String.format(MSG_HOST_CREATE_4_5, hostName, deviceName);
			else 
				msg = String.format(MSG_HOST_CREATE, hostName, deviceName);

			sb.append(msg);
		}
		
		if (_bUse45Version == false)
			sb.append(SystemConfigEnd);

		return sb.toString();
	}

	public boolean isUpdateComplete() throws ProfilerException {
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			// Query Hosts Status Count with the monitor status that we set in
			// build message
			// NOTE: There may be an issue with another message batch is sent
			// toggle status
			// before we record the status change.
		   stmt = _dbSourceConnection.createStatement();
		   
		   rs = stmt.executeQuery(PSTMT_CHECK_UPDATED + _whereClause);

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
			
			if (stmt != null)
			{
				try {
					stmt.close();
				}
				catch (Exception e)
				{
					log.error("Error closing prepared stmt.", e);
				}
				stmt = null;
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
		return new HostCreateMessage(workloadId, batchCount, _numHosts,
				_name, _threshold, messageSocketInfo, dbProfilerConnection, dbSourceConnection,
				deltaTime, _bUse45Version);
	}

	private String buildHostName(String name, int workloadId, int batchCount, int hostNum)
	{
		return String.format(HOST_NAME_FORMAT, name, workloadId, batchCount, hostNum);
	}
	
	private String buildDeviceName(String name, int workloadId, int batchCount, int hostNum)
	{
		return String.format(DEVICE_NAME_FORMAT, name, workloadId, batchCount, hostNum);
	}
	
	private String buildWhereClause ()
	{
		// Build up where clause
		StringBuilder sb = new StringBuilder("WHERE (");
		String hostNameClause = "h.HostName = '";
		for (int i = 0; i < _numHosts; i++) {

			if (i > 0)
				sb.append(OR);

			sb.append(hostNameClause);
			sb.append(buildHostName(_name, _workloadId, _batchCount,  i));
			sb.append(SINGLE_QUOTE);
		}

		sb.append(CLOSE_PAREN);

		return sb.toString();
	}
}