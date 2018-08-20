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

public class SysLogMessage extends WorkloadMessage
{
	// SQL Statements
	private static final String PSTMT_CHECK_UPDATED =
		"SELECT MAX(ReportDate), Count(*) As NumUpdated FROM LogMessage WHERE TextMessage=?";
	
	private static final String PSTMT_CHECK_UPDATED_CONSOLIDATED =
		"SELECT MAX(ReportDate), Count(*) As NumUpdated FROM LogMessage lm INNER JOIN Device d ON lm.DeviceID = d.DeviceId ";
		
	private static final String MSG_TEXT_MESSAGE = "Profiler SYSLOG Message, Workload Id=%1$d, BatchCount=%2$d";
	
	// Log Message Message Format
	// 1 = Host Name / Device Id, 2 = Text Message, 3 = LastInsertDate (Current System Time)
	// NOTE:  The ReportDate set actually becomes LastInsertDate in Foundation and LastInsertDate becomes 
	// the foundation system time.
	// SEE com.groundwork.collage.impl.admin.CollageAdminImpl.java updateLogMessage() method for implementation details
	// Also note, we have removed consolidation (consolidation='SYSLOG') for these messages in order to verify update based on
	// text message of the event.  If consolidation was on then the text message does not change
	// 
	private static final String MSG_SYSLOG_MESSAGE_NO_CONSOLIDATION =
		"<SYSLOG MonitorServerName=\"localhost\"" +
		" Host=\"%1$s\" Device=\"%1$s\" Severity=\"OK\"" +
		" MonitorStatus=\"UP\" TextMessage=\"%2$s\"" +
		" ReportDate=\"%3$s\" LastInsertDate=\"%3$s\"" +
		" SubComponent=\"UNDEFINED\" ErrorType=\"HOST ALERT\"" +
		" ipaddress=\"%1$s\" />";	

	// Since we are using consolidation we have to check if the last check time is updated to the one in the message we sent
	private static final String MSG_SYSLOG_MESSAGE_WITH_CONSOLIDATION =
		"<SYSLOG consolidation='SYSLOG' MonitorServerName=\"localhost\"" +
		" Host=\"%1$s\" Device=\"%1$s\" Severity=\"OK\"" +
		" MonitorStatus=\"UP\" TextMessage=\"Profiler SYSLOG Message - Consolidation\"" +
		" ReportDate=\"%2$s\" LastInsertDate=\"%2$s\"" +
		" SubComponent=\"UNDEFINED\" ErrorType=\"HOST ALERT\"" +
		" ipaddress=\"%1$s\" />";	
	
	private static final String DEVICE_NAME_FORMAT = "SYSLOG_DVC_%1$s_%2$d_%3$d";
	
	// Number of devices to create and receive event messages
	private int _numDevices = 10;
	
	// Use consolidation based on the boolean value
	private boolean _consolidation = false;
	
	// Time updated has been completed.
	private Timestamp _updateTime = null;
	
	// Last Insert Date of the message we sent.  We store in order to verify update
	private String _lastInsertDate = null;

	// Where clause built up from number of devices
	private String _whereClause = null;
	
	// Log	
	protected static Log log = LogFactory.getLog(SysLogMessage.class);
	
	/**
	 * Factory constructor for "runnable" instances
	 * @param workloadId
	 * @param numDevices
	 * @param name
	 * @param messageSocket
	 * @param dbProfilerConnection
	 * @param dbSourceConnection
	 */
	private SysLogMessage (int workloadId,
									 int batchCount,
									 int numDevices, 
									 boolean consolidation,
									 String name, 
									 long threshold,
									 MessageSocketInfo messageSocketInfo,
									 Connection dbProfilerConnection,
									 Connection dbSourceConnection,
									 long deltaTime)
	{
		super(workloadId, batchCount, 0, name, threshold, messageSocketInfo, dbProfilerConnection, dbSourceConnection, deltaTime);
		
		_numDevices = numDevices;
		_consolidation = consolidation;
		
		// Build up where clause
		StringBuilder sb = new StringBuilder("WHERE LastInsertDate = ? AND (");		
		String deviceIdClause = "d.Identification = '";
		for (int i = 0; i < _numDevices; i++)
		{
			
			if (i > 0) {
				sb.append(OR);
			}
			
			sb.append(deviceIdClause);
			sb.append(buildDeviceName(_name, _workloadId, i));
			sb.append(SINGLE_QUOTE);
		}
		
		sb.append(CLOSE_PAREN);
		
		_whereClause = sb.toString();		
	}
	
	/**
	 * Default constructor is required for all WorkloadMessages with the NamedNodeMap parameter
	 * @param attributes
	 * @throws ProfilerException
	 */
	public SysLogMessage(Node messageNode, NamedNodeMap attributes) throws ProfilerException
	{     
		super(messageNode, attributes);
		
		// Parse out specific attributes
        Node numDevicesNode = attributes.getNamedItem("numDevices");  
        if (numDevicesNode == null)
        {
        	throw new ProfilerException("numDevices attribute is missing from SYSLOGMessage.");
        }
        
        String numDevices = numDevicesNode.getNodeValue();     
        try {
        	_numDevices = Integer.parseInt(numDevices);
        }
        catch (Exception e)
        {
        	throw new ProfilerException("SYSLOGMessage.ctor - Invalid numDevices setting." 
        								+ numDevices);
        }   		
        
        Node consolidationNode = attributes.getNamedItem("consolidation");
        
        if (consolidationNode != null)
        {
	        String consolidation = consolidationNode.getNodeValue();
	        
	        try {
	        	_consolidation = Boolean.parseBoolean(consolidation);
	        }
	        catch (Exception e)
	        {
	        	log.warn("Invalid consolidation value.  Should be true or false. Using default consolidation value.");
	        }   	
        }
	}
	
	public int getnumDevices ()
	{
		return _numDevices;
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder(32);
		sb.append("Num Devices: ");
		sb.append(_numDevices);
		sb.append(", ");

		sb.append("Consolidation: ");
		sb.append(_consolidation);
		sb.append(", ");
		
		sb.append(super.toString());
		
		return sb.toString();
	}
	
	public String buildMessage () throws ProfilerException
	{
		StringBuilder sb = new StringBuilder(MSG_SYSLOG_MESSAGE_NO_CONSOLIDATION.length() * _numDevices);
		
		String deviceName = null;		
		String textMessage = String.format(MSG_TEXT_MESSAGE, _workloadId, _batchCount);			
		String snmpMsg = null;
		// cpora - we need milliseconds
		_lastInsertDate = SQL_DATE_FORMAT2.format(new Date(System.currentTimeMillis() - _deltaTime)); // Adjust to source db time
		
		// Send bulk message for number of devices
		for (int i = 0; i < _numDevices; i++)
		{
			deviceName = buildDeviceName(_name, _workloadId, i);
			
			if (_consolidation == true)
			{
				snmpMsg = String.format(MSG_SYSLOG_MESSAGE_WITH_CONSOLIDATION, deviceName, _lastInsertDate);
			}
			else 
			{
				snmpMsg = String.format(MSG_SYSLOG_MESSAGE_NO_CONSOLIDATION, deviceName, textMessage, _lastInsertDate);
			}
			
			sb.append(snmpMsg);		
		}
		
		return sb.toString();		
	}
	
	public boolean isUpdateComplete() throws ProfilerException
	{
		if (_consolidation == true) {
			return checkConsolidatedUpdate();
		} else {
			return checkUnconsolidatedUpdate();
		}
	}
	
	public Timestamp captureMetrics () 
		throws ProfilerException
	{	
		return _updateTime;
	}
	
	/**
	 * Clones this instance allowing it to run on a separate thread.  Note - It is sharing message socket and database connections
	 */
	public IWorkloadMessage getRunnableInstance(int workloadId, 
												int batchCount, 
												MessageSocketInfo socketInfo, 
												Connection dbProfilerConnection, 
												Connection dbSourceConnection,
												long deltaTime,
												int msgCount)
	{
		return new SysLogMessage(workloadId, 
										   batchCount, 
										   _numDevices,
										   _consolidation,
										   _name, 
										   _threshold,
										   socketInfo, 
										   dbProfilerConnection, 
										   dbSourceConnection,
										   deltaTime
										   );
	}
	
	private boolean checkUnconsolidatedUpdate() throws ProfilerException
	{
		String textMessage = String.format(MSG_TEXT_MESSAGE, _workloadId, _batchCount);
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			// Query log message Count with the text message that we set in build message
			pstmt = _dbSourceConnection.prepareStatement(PSTMT_CHECK_UPDATED);
			pstmt.setString(1, textMessage);
			
			rs = pstmt.executeQuery();
			
			if (rs.next())
			{
				int count = rs.getInt(2);
				
				if (count == _numDevices)
				{
					// Capture completion time
					_updateTime = rs.getTimestamp(1);
					
					// Add delta time between the profiler and source db.  If the profiler is on the same machine this delta should be zero
					_updateTime = new Timestamp(_updateTime.getTime() + _deltaTime);
					
					return true;					
				}
				
				return false;
			}
			else {
				throw new ProfilerException("Unable to check if update is complete.");
			}			
		}
		catch (Exception e)
		{
			throw new ProfilerException("Error checking for whether update is complete.", e);
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
	
	private boolean checkConsolidatedUpdate() throws ProfilerException
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
	
		try {
			// Query log message Count with the text message that we set in build message
			pstmt = _dbSourceConnection.prepareStatement(PSTMT_CHECK_UPDATED_CONSOLIDATED + _whereClause);
			// cpora - milliseconds not necessary because we compare to DB date format
			_lastInsertDate = SQL_DATE_FORMAT.format(new Date(System.currentTimeMillis() - _deltaTime)); 
			pstmt.setString(1, _lastInsertDate);

			rs = pstmt.executeQuery();
			
			if (rs.next())
			{
				int count = rs.getInt(2);
				
				if (count == _numDevices)
				{
					// Capture completion time
					_updateTime = rs.getTimestamp(1);
					
					// Add delta time between the profiler and source db.  If the profiler is on the same machine this delta should be zero
					_updateTime = new Timestamp(_updateTime.getTime() + _deltaTime);
					
					return true;					
				}
				
				return false;
			}
			else {
				throw new ProfilerException("Unable to check if update is complete.");
			}			
		}
		catch (Exception e)
		{
			throw new ProfilerException("Error checking for whether update is complete.", e);
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
	
	public int getCheckCount()
	{
		return _numDevices;
	}
	
	private String buildDeviceName (String name, int workloadId, int i)
	{
		return String.format(DEVICE_NAME_FORMAT, name, workloadId, i);
	}
}