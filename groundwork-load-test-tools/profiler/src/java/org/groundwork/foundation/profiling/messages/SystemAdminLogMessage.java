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

public class SystemAdminLogMessage extends WorkloadMessage
{
	// SQL Statements
	private static final String PSTMT_CHECK_UPDATED =
		"SELECT MAX(ReportDate), Count(*) As NumUpdated FROM LogMessage WHERE TextMessage=?";
	
	private static final String PSTMT_CHECK_UPDATED_CONSOLIDATED =
		"SELECT MAX(ReportDate), Count(*) As NumUpdated FROM LogMessage lm INNER JOIN Device d ON lm.DeviceID = d.DeviceId ";
		
	//private static final String MSG_TEXT_MESSAGE = "Profiler Log Message, Workload Id=%1$d, BatchCount=%2$d, Foundation=%3$s <";
	private static final String MSG_TEXT_MESSAGE = "Profiler Log Message, Workload Id=%1$d, BatchCount=%2$d";

	private static final String DEVICE_NAME_FORMAT = "HST_DVC_%1$s_%2$d_";
	private static final String DEVICE_NAME_FORMAT2 = "HST_DVC_%1$s_%2$d_%3$d";
	private static final String HOST_NAME_FORMAT = DEVICE_NAME_FORMAT;
	private static final String SERVICE_DESCRIPTION_FORMAT = "TSSM_SVC_%1$s_%2$d_";
	// Number of devices to create and receive event messages
	private int _numDevices = 10;
	private int _numHosts = 10;

	// Use consolidation based on the boolean value
	private boolean _consolidation = false;
	
	// Time updated has been completed.
	private Timestamp _updateTime = null;
	
	// Last Insert Date of the message we sent.  We store in order to verify update
	private String _lastInsertDate = null;

	// Where clause built up from number of devices
	private String _whereClause = null;
	
	// Log	
	protected static Log log = LogFactory.getLog(SystemAdminLogMessage.class);
	

	// h - HOSTS percentage; w = WARNING percentage; u = UP percentage; c = CRITICAL percentage; d = DOWN percentaqe;
	// cs - consolidation percentage
	
	private int _csPercent = 100;  
	//private int _hostsPercent = 100; 
	//private int _warningPercent = 100; 
	//private int _upPercent = 100; 
	//private int _criticalPercent = 100; 
	//private int _downPercent = 100;
	/**
	 * Factory constructor for "runnable" instances
	 * @param workloadId
	 * @param numDevices
	 * @param name
	 * @param messageSocket
	 * @param dbProfilerConnection
	 * @param dbSourceConnection
	 */
	private SystemAdminLogMessage (int workloadId,
									 int batchCount,
									 int numHosts, 
									 int csPercent,  
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
		_csPercent = csPercent;  

		// Build up where clause
		StringBuilder sb = new StringBuilder("WHERE LastInsertDate = ? AND (");		
		String deviceIdClause = "d.Identification = '";
		for (int i = 0; i < _numHosts; i++)
		{
			
			if (i > 0) {
				sb.append(OR);
			}
			
			sb.append(deviceIdClause); 
			sb.append(buildDeviceName("test", _workloadId, i));
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
	public SystemAdminLogMessage(Node messageNode, NamedNodeMap attributes) throws ProfilerException
	{     
		super(messageNode, attributes);
		
		// Parse out specific attributes
        Node numHostsNode = attributes.getNamedItem("numHosts");  
        if (numHostsNode == null)
        {
        	throw new ProfilerException("numHosts attribute is missing from SystemAdminLogMessage.");
        }
        
        String numHosts = numHostsNode.getNodeValue();     
        try {
        	_numHosts = Integer.parseInt(numHosts);
        }
        catch (Exception e)
        {
        	throw new ProfilerException("SystemAdminLogMessage.ctor - Invalid numHosts setting." 
        								+ numHosts);
        }   	
        
        
        
        // =========================================================================================
		// Parse out specific attributes
        Node csPercentNode = attributes.getNamedItem("csPercent");  
        if (csPercentNode == null)
        {
        	throw new ProfilerException("csPercent attribute is missing from SystemAdminLogMessage.");
        }
        
        String csPercent = csPercentNode.getNodeValue();     
        try {
        	_csPercent = Integer.parseInt(csPercent);
        }
        catch (Exception e)
        {
        	throw new ProfilerException("SystemAdminLogMessage.ctor - Invalid csPercent setting." 
        								+ csPercent);
        }   	

	}
	public int getnumHosts ()
	{
		return _numHosts;
	}
	
	public int getnumDevices ()
	{
		return _numDevices;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(32);
		sb.append("Num Devices: ");
		sb.append(_numDevices);
		sb.append(", ");
		sb.append("Num Hosts: ");
		sb.append(_numHosts);
		sb.append(", ");
		sb.append("Consolidation: ");
		sb.append(_consolidation);
		sb.append(", ");
		
		sb.append(super.toString());
		
		return sb.toString();
	}
	
	public String buildMessage () throws ProfilerException
	{
		SystemAdminUtil sysadmin = new SystemAdminUtil();
		String deviceName = buildDeviceName("test", _workloadId);		
		String textMessage = String.format(MSG_TEXT_MESSAGE, _workloadId, _batchCount);

		_lastInsertDate = SQL_DATE_FORMAT.format(new Date(System.currentTimeMillis() - _deltaTime)); // Adjust to source db time
		String hostName = deviceName;
		String sDescription = buildServiceDescription("test", _workloadId);

		String outPut = "";
		try
		{
			String session = String.format(SESSION_NAME_FORMAT, _workloadId, _batchCount, _msgCount);
			outPut = sysadmin.getLogMessageCommand (session, "SystemAdmin",  "NAGIOS",hostName, deviceName, 
					sDescription, 
					textMessage, 
					_lastInsertDate, 
					_csPercent, 
					_numHosts 
			);
		}
		catch (javax.xml.parsers.ParserConfigurationException e){log.debug(e);}
		return outPut;
	}
	
	
	public int getCheckCount() 
	{
		return _numHosts;
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

		return new SystemAdminLogMessage(workloadId, 
										   batchCount, 
										   _numHosts, 
										   _csPercent,  
										   _name, 
										   _threshold,
										   socketInfo, 
										   dbProfilerConnection, 
										   dbSourceConnection,
										   deltaTime,
										   msgCount);
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

				if (count == _numHosts)
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
					log.error("SystemAdminLogMessage: Error closing result set", e);
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
					log.error("SystemAdminLogMessage: Error closing prepared stmt.", e);
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
			_lastInsertDate = SQL_DATE_FORMAT.format(new Date(System.currentTimeMillis() - _deltaTime)); 
			pstmt.setString(1, _lastInsertDate);

			rs = pstmt.executeQuery();
			
			if (rs.next())
			{
				int count = rs.getInt(2);

				if (count == _numHosts)
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
					log.error("SystemAdminLogMessage: Error closing result set", e);
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
					log.error("SystemAdminLogMessage: Error closing prepared stmt.", e);
				}
				pstmt = null;
			}
		}		
	}
	
	
	private String buildDeviceName (String name, int workloadId)
	{
		return String.format(DEVICE_NAME_FORMAT, name, workloadId);
	}
	
	

	private String buildDeviceName (String name, int workloadId, int i)
	{
		return String.format(DEVICE_NAME_FORMAT2, name, workloadId, i);
	}
	
	private String buildHostName(String name, int workloadId)
	{
		return String.format(HOST_NAME_FORMAT, name, workloadId);
	}
	
	private String buildServiceDescription(String name, int workloadId) {
		return String.format(SERVICE_DESCRIPTION_FORMAT, name, workloadId);
	}
}