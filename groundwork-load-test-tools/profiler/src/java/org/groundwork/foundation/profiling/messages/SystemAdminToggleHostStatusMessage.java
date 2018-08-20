/**
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2008  GroundWork Open Source Solutions info@itgroundwork.com

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
import org.groundwork.foundation.profiling.SystemAdminUtil;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class SystemAdminToggleHostStatusMessage extends WorkloadMessage {
	// SQL Statements
	private static final String PSTMT_CHECK_UPDATED = "SELECT Count(*) As NumUpdated from Host h INNER JOIN HostStatus hs ON h.HostID = hs.HostStatusID INNER JOIN MonitorStatus ms ON hs.MonitorStatusID = ms.MonitorStatusID ";

	// Host Status Message Format
	// We use string format to replace the host name, device, last check time and
	// monitor status values
	private static final String MSG_HOST_STATUS = "<HOST_STATUS MonitorServerName=\"localhost\" " +
	"Host=\"%1$s\" Device=\"%1$s\" CheckTypeID=\"0\" CurrentNotificationNumber=\"0\" LastCheckTime=\"%2$s\" " +
	"LastNotificationTime=\"0\" LastPluginOutput=\"\" LastStateChange=\"0000-00-00 00:00:00\" MonitorStatus=\"%3$s\" " +
	"PercentStateChange=\"0.00\" ScheduledDowntimeDepth=\"0\" TimeDown=\"0\" TimeUnreachable=\"0\" TimeUp=\"0\" " +
	"isAcknowledged=\"0\" isChecksEnabled=\"0\" isEventHandlersEnabled=\"0\" isFailurePredictionEnabled=\"0\" " +
	"isFlapDetectionEnabled=\"0\" isHostIsFlapping=\"0\" isNotificationsEnabled=\"0\" isPassiveChecksEnabled=\"0\" " +
	"isProcessPerformanceData=\"0\" />";
	private static String DEVICE_NAME_FORMAT = "HST_DVC_%1$s_%2$d_";
	//private static final String HOST_NAME_FORMAT = "THSM_HST_%1$s_%2$d_%3$d";
	private static final String HOST_NAME_FORMAT = DEVICE_NAME_FORMAT;
	private static final String SERVICE_DESCRIPTION_FORMAT = "TSSM_SVC_%1$s_%2$d_";
	private static final String HOST_NAME_FORMAT2 = "HST_DVC_%1$s_%2$d_%3$d";


	
	private static final String STATUS_UP = "UP";

	private static final String STATUS_DOWN = "DOWN";

	// Number of hosts to create and receive status messages
	private int _numHosts = 10;
	
	private int _monitorUpPercentage = 100;
	// Time updated has been completed.
	private long _updateTime = 0;

	// Where clause built up from number of hosts
	private String _whereClause = null;

	// Last check time used to confirm update
	private String _lastCheckTime = null;

	// Monitor status used to confirm update
	private String _monitorStatus = null;

	// Log
	protected static Log log = LogFactory.getLog(SystemAdminToggleHostStatusMessage.class);

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
	private SystemAdminToggleHostStatusMessage(int workloadId, 
												int batchCount,
												int numHosts, 
												int monitorUpPercentage,
												String name, 
												long threshold, 
												MessageSocketInfo messageSocketInfo,
												Connection dbProfilerConnection, 
												Connection dbSourceConnection,
												long deltaTime,
												int msgCount) 
	{
		super(workloadId, batchCount, msgCount, name, threshold, messageSocketInfo, dbProfilerConnection, dbSourceConnection, deltaTime);

		_numHosts = numHosts;
		_monitorUpPercentage = monitorUpPercentage;
		String hostName = String.format(HOST_NAME_FORMAT, "test", _workloadId);
		int nameLength = hostName.length()+1;
		// Build up where clause
		//StringBuilder sb = new StringBuilder("WHERE ms.Name = ? AND hs.LastCheckTime = ? AND (");
		StringBuilder sb = new StringBuilder("WHERE (ms.Name = 'UP' OR ms.Name = 'PENDING' OR ms.Name = 'OK' OR "+
				" ms.Name = 'CRITICAL' OR ms.Name = 'WARNING' OR ms.Name='DOWN' OR ms.Name='UNREACHABLE' OR "+
				" ms.Name='MAINTENANCE' OR ms.Name='UNKNOWN') AND (hs.LastCheckTime = ? or hs.LastCheckTime is NULL) AND (");
		
		sb.append("substr(h.HostName, ");
		sb.append(nameLength);
		sb.append(") between 1 and ");
		sb.append(_numHosts);
		/*
		String hostNameClause = "h.HostName = '";
		for (int i = 0; i < _numHosts; i++) 
		{
			if (i > 0) {
				sb.append(OR);
			}

			sb.append(hostNameClause);
			sb.append(buildHostName(_name, _workloadId, i));
			sb.append(SINGLE_QUOTE);
		}
		 */
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
	public SystemAdminToggleHostStatusMessage(Node messageNode, NamedNodeMap attributes)
			throws ProfilerException 
	{
		super(messageNode, attributes);

		// Parse out specific attributes
		Node numHostsNode = attributes.getNamedItem("numHosts");
		if (numHostsNode == null) {
			throw new ProfilerException(
					"numHosts attribute is missing from SystemAdminToggleHostStatusMessage.");
		}

		String numHosts = numHostsNode.getNodeValue();
		try {
			_numHosts = Integer.parseInt(numHosts);
		} catch (Exception e) {
			throw new ProfilerException(
					"SystemAdminToggleHostStatusMessage.ctor - Invalid numHosts setting."
							+ numHosts);
		}
		
        Node monitorUpPercentageNode = attributes.getNamedItem("monitorUpPercentage");  
        if (monitorUpPercentageNode == null)
        {
        	throw new ProfilerException("monitorUpPercentage attribute is missing from SystemAdminToggleHostStatusMessage.");
        }
        
        String monitorUpPercentage = monitorUpPercentageNode.getNodeValue();     
        try {
        	_monitorUpPercentage = Integer.parseInt(monitorUpPercentage);
        }
        catch (Exception e)
        {
        	throw new ProfilerException("SystemAdminToggleHostStatusMessage.ctor - Invalid monitorUpPercentage setting." 
        								+ monitorUpPercentage);
        }   		
		
	}

	public int getMonitorUpPercentage() {
		return _monitorUpPercentage;
	}

	public int getNumHosts() {
		return _numHosts;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(32);
		sb.append("Num Hosts: ");
		sb.append(_numHosts);
		sb.append(", ");

		sb.append(super.toString());

		return sb.toString();
	}

	public String buildMessage() throws ProfilerException {
		SystemAdminUtil sysadmin = new SystemAdminUtil();
		String hostName = null;
		// cpora add milliseconds to the date
		_lastCheckTime = SQL_DATE_FORMAT.format(new Date());
		hostName = buildHostName("test", _workloadId);
		String deviceName = hostName;

		String outPut = "";
		try
		{
			String session = String.format(SESSION_NAME_FORMAT, _workloadId, _batchCount, _msgCount);
			outPut = sysadmin.getHostStatusCommand (session, "SystemAdmin", "NAGIOS",hostName, deviceName, _lastCheckTime, _numHosts, _monitorUpPercentage);
		}
		catch (javax.xml.parsers.ParserConfigurationException e){log.debug("buildMessage() error: ", e);}
		return outPut;
		
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
			//pstmt.setString(1, _monitorStatus);

			//_lastCheckTime = SQL_DATE_FORMAT.format(new Date());
			pstmt.setString(1, _lastCheckTime);
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
	public IWorkloadMessage getRunnableInstance(int workloadId, 
												int batchCount,
												MessageSocketInfo messageSocketInfo, 
												Connection dbProfilerConnection,
												Connection dbSourceConnection, 
												long deltaTime,
												int msgCount) 
	{

		return new SystemAdminToggleHostStatusMessage(workloadId, 
														batchCount, 
														_numHosts,
														_monitorUpPercentage,
														_name, 
														_threshold, 
														messageSocketInfo, 
														dbProfilerConnection, 
														dbSourceConnection,
														deltaTime,
														msgCount);
	}
	
	public int getCheckCount() 
	{
		return _numHosts;
	}
	
	private String buildDeviceName (String name, int workloadId)
	{
		return String.format(DEVICE_NAME_FORMAT, name, workloadId);
	}
	
	private String buildHostName(String name, int workloadId)
	{
		return String.format(HOST_NAME_FORMAT, name, workloadId);
	}

	private String buildHostName(String name, int workloadId, int i)
	{
		return String.format(HOST_NAME_FORMAT2, name, workloadId, i);
	}
	private String buildServiceDescription(String name, int workloadId) {
		return String.format(SERVICE_DESCRIPTION_FORMAT, name, workloadId);
	}
}
