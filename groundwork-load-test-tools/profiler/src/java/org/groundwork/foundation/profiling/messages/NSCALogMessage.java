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
import org.groundwork.foundation.profiling.PassiveChecks;
import org.groundwork.foundation.profiling.WorkloadMessage;
import org.groundwork.foundation.profiling.exceptions.ProfilerException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class NSCALogMessage extends WorkloadMessage
{
	// SQL Statements
	//private static final String PSTMT_CHECK_UPDATED =
	//	"SELECT MAX(ReportDate), Count(*) As NumUpdated FROM LogMessage WHERE TextMessage=?";
	private static final String PSTMT_CHECK_UPDATED ="select Count(*)  from ServiceStatus t1, Host t2, MonitorStatus t3 where t1.HostID = t2.HostID and t1.MonitorStatusID = t3.MonitorStatusID and t1.ServiceDescription like ";
	private static final String PSTMT_CHECK_UPDATED_CONSOLIDATED =
		"SELECT Count(*) As NumUpdated,  MAX(ReportDate)  FROM LogMessage lm INNER JOIN Device d ON lm.DeviceID = d.DeviceId ";
		
	//private static final String MSG_TEXT_MESSAGE = "Profiler SNMP Message, Workload Id=%1$d, BatchCount=%2$d";
	private static final String MSG_TEXT_MESSAGE = "Profiler NSCALogMessage, H=%1$s, S=%2$s, WId=%3$d, B=%4$d, M=%5$d, LIDate=%6$s, MS=%7$s";
	private static String HOST_NAME_FORMAT = "%1$s_%2$d%3$d";
	private static String HOST_NAME_FORMAT2 = "%1$s_%2$d";
	private static final String SERVICE_NAME_FORMAT = "%1$s_%2$d%3$d";
	private static final String SERVICE_NAME_FORMAT2 = "%1$s_%2$d";
	// Number of devices to create and receive event messages
	private int _numDevices = 10;
	
	// Time updated has been completed.
	private Timestamp _updateTime = null;
	
	// Last Insert Date of the message we sent.  We store in order to verify update
	private String _lastInsertDate = null;

	// Where clause built up from number of devices
	private String _whereClause = null;
	private String _hostName = "HTest";
	private String _serviceName = "STest";
	private String _ipAddress = "100.100.100.";
	private String _monitorStatus = null;
	private int _numHosts = 10;
	private int _numServices = 10;
	
	private static final String STATUS_OK = "OK";
	private static final String STATUS_CRITICAL = "CRITICAL";
	// Log	
	protected static Log log = LogFactory.getLog(NSCALogMessage.class);
	
	/**
	 * Factory constructor for "runnable" instances
	 * @param workloadId
	 * @param numDevices
	 * @param name
	 * @param messageSocket
	 * @param dbProfilerConnection
	 * @param dbSourceConnection
	 */
	private NSCALogMessage (int workloadId,
									 int batchCount,
									 String name, 
									 int numHosts,
									 int numServices,
									 String hostName,
									 String serviceName,
									 long threshold,
									 MessageSocketInfo messageSocketInfo,
									 Connection dbProfilerConnection,
									 Connection dbSourceConnection,
									 long deltaTime,
									 int msgCount)
	{
		super(workloadId, batchCount, msgCount, name, threshold, messageSocketInfo, dbProfilerConnection, dbSourceConnection, deltaTime);

		_numHosts = numHosts;
		_numServices = numServices;
		_numDevices = numServices;
		_hostName = hostName;
		_serviceName = serviceName;
		// Build up where clause
		StringBuilder sb = new StringBuilder("WHERE ");		
		String deviceIdClause = "d.Identification = '";
		for (int i = 1; i <= _numHosts; i++)
		{
			
			if (i > 1) {
				sb.append(OR);
			}
			
			sb.append(deviceIdClause);
			sb.append(buildHostName(i));
			sb.append(SINGLE_QUOTE);
		}
		
		//sb.append(CLOSE_PAREN);
		
		_whereClause = sb.toString();		
	}
	
	/**
	 * Default constructor is required for all WorkloadMessages with the NamedNodeMap parameter
	 * @param attributes
	 * @throws ProfilerException
	 */
	public NSCALogMessage(Node messageNode, NamedNodeMap attributes) throws ProfilerException
	{     
		super(messageNode, attributes);
		   		
		Node numHostsNode = attributes.getNamedItem("numHosts");
		if (numHostsNode == null) {
			throw new ProfilerException(
					"numHosts attribute is missing from NSCALogMessage.");
		}
		String numHosts = numHostsNode.getNodeValue();
		try {
			_numHosts = Integer.parseInt(numHosts);
		} catch (Exception e) {
			throw new ProfilerException(
					"NSCALogMessage.ctor - Invalid numHosts setting."
							+ numHosts);
		}
        
		Node numServicesNode = attributes.getNamedItem("numServices");
		if (numServicesNode == null) {
			throw new ProfilerException(
					"numServices attribute is missing from NSCALogMessage.");
		}
		String numServices = numServicesNode.getNodeValue();
		try {
			_numServices = Integer.parseInt(numServices);
		} catch (Exception e) {
			throw new ProfilerException(
					"NSCALogMessage.ctor - Invalid numServices setting."
							+ numServices);
		}
        
        Node hostNameNode = attributes.getNamedItem("hostName");  
        if (hostNameNode == null)
        {
        	throw new ProfilerException("hostName attribute is missing from NSCALogMessage.");
        }
        
        _hostName = hostNameNode.getNodeValue();
        
        Node serviceNameNode = attributes.getNamedItem("serviceName");  
        if (serviceNameNode == null)
        {
        	throw new ProfilerException("serviceName attribute is missing from NSCALogMessage.");
        }
        
        _serviceName = serviceNameNode.getNodeValue();
        
        Node ipAddressNode = attributes.getNamedItem("ipAddress");  
        if (ipAddressNode == null)
        {
        	throw new ProfilerException("ipAddress attribute is missing from NSCALogMessage.");
        }
        
        _ipAddress = ipAddressNode.getNodeValue();
        
	}

	
	public String toString()
	{
		StringBuilder sb = new StringBuilder(32);
		sb.append("Num Hosts: ");
		sb.append(_numHosts);
		sb.append(", ");

		
		sb.append(super.toString());
		
		return sb.toString();
	}
	
	
	/*
	public String buildMessage () throws ProfilerException
	{
		StringBuilder sb = new StringBuilder(MSG_TEXT_MESSAGE.length() * _numHosts * _numServices);
		
		String nscaMsg = null;

		_lastInsertDate = SQL_DATE_FORMAT.format(new Date(System.currentTimeMillis() - _deltaTime)); // Adjust to source db time
		
		// Send bulk message for number of devices
		
		// ********************************************************************************************
		if (log.isDebugEnabled()) log.debug("NSCALogMessage____________PassiveChecks.configureProperties (log4jNSCA.properties)");
		//PassiveChecks.configureProperties("log4jNSCA.properties");
		if (_batchCount % 2 == 0) _monitorStatus = STATUS_CRITICAL;
		else  _monitorStatus = STATUS_OK;
		for (int j = 1; j <= _numHosts; j++)
		{
			for (int i = 1; i <= _numServices; i++)
			{
				String hostName = buildHostName(j);
				String serviceName = buildServiceName(i);
				nscaMsg = buildTextMessage(hostName, serviceName);
				log.debug("NSCALogMessage:buildMessage() _monitorStatus=["+_monitorStatus+"]");
				PassiveChecks.sendNSCAServiceStatus(hostName, serviceName, nscaMsg, _monitorStatus);
				log.debug("NSCALogMessage:buildMessage() after sendNSCA");
				sb.append(nscaMsg);	
			}
		}
		//PassiveChecks.configureProperties("log4j.properties");
		if (log.isDebugEnabled()) log.debug("NSCALogMessage____________PassiveChecks.configureProperties (log4j.properties)");
		// ********************************************************************************************
		return sb.toString();		
	}
	
	*/
	
	public String buildMessage () throws ProfilerException
	{
		StringBuilder sb = new StringBuilder(MSG_TEXT_MESSAGE.length() * _numHosts * _numServices);
		
		String nscaMsg = null;

		_lastInsertDate = SQL_DATE_FORMAT.format(new Date(System.currentTimeMillis() - _deltaTime)); // Adjust to source db time
		
		// Send bulk message for number of devices
		
		// ********************************************************************************************
		if (log.isDebugEnabled()) log.debug("NSCALogMessage____________PassiveChecks.sendNSCAServiceStatus [begin]");
		//PassiveChecks.configureProperties("log4jNSCA.properties");
		if (_msgCount % 3 != 0) _monitorStatus = STATUS_CRITICAL;
		else  _monitorStatus = STATUS_OK;
		
		for (int j = 1; j <= _numHosts; j++)
		{
			for (int i = 1; i <= _numServices; i++)
			{
				String hostName = buildHostName(j);
				String serviceName = buildServiceName(i);
				nscaMsg = buildTextMessage(hostName, serviceName);
				//log.debug("NSCALogMessage:buildMessage() _monitorStatus=["+_monitorStatus+"]");
				//PassiveChecks.sendNSCAServiceStatus(hostName, serviceName, nscaMsg, _monitorStatus);
				//log.debug("NSCALogMessage:buildMessage() after sendNSCA");
				sb.append(nscaMsg);	
			}
		}
		
		//PassiveChecks.configureProperties("log4j.properties");
		
		PassiveChecks.sendNSCAServiceStatus(_numHosts, _numServices, buildHostName(), buildServiceName(), _workloadId, _batchCount, _msgCount, _lastInsertDate, _monitorStatus);
		
		if (log.isDebugEnabled()) log.debug("NSCALogMessage____________PassiveChecks.sendNSCAServiceStatus [end]");
		// ********************************************************************************************
		return sb.toString();		
	}
	
	
	public boolean isUpdateComplete() throws ProfilerException
	{
		String textMessage = buildTextMessage(_hostName, _serviceName);
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			// Query log message Count with the text message that we set in build message
			//pstmt = _dbSourceConnection.prepareStatement(PSTMT_CHECK_UPDATED);
			//pstmt.setString(1, textMessage);
			
			if (_batchCount % 2 == 0)
			{
				log.debug("NSCALogMessage.isUpdateComplete 1: "+PSTMT_CHECK_UPDATED + "'"+buildServiceName()+"%' and t3.Name='OK'");
				pstmt = _dbSourceConnection.prepareStatement(PSTMT_CHECK_UPDATED + "'"+buildServiceName()+"%'");
			}
			else
			{
				log.debug("NSCALogMessage.isUpdateComplete 2: "+PSTMT_CHECK_UPDATED_CONSOLIDATED + _whereClause);
				pstmt = _dbSourceConnection.prepareStatement(PSTMT_CHECK_UPDATED_CONSOLIDATED + _whereClause);
			}
			//pstmt.setString(1, _lastInsertDate);

			rs = pstmt.executeQuery();
			
			if (rs.next())
			{
				int count = rs.getInt(1);
				log.debug("NSCALogMessage.isUpdateComplete: rs.getInt(1)="+rs.getInt(1));
				log.debug("NSCALogMessage.isUpdateComplete: x _numDevices="+_numDevices);
				log.debug("NSCALogMessage.isUpdateComplete: x _numHosts="+_numHosts);
				log.debug("NSCALogMessage.isUpdateComplete: x _numServices="+_numServices);
				int x = _numHosts * _numServices;
				log.debug("NSCALogMessage.isUpdateComplete: x = "+x);
				if (count == _numHosts * _numServices)
				{
					_updateTime = new Timestamp(System.currentTimeMillis());
					if (_batchCount % 2 == 0)
					{
					// Capture completion time
					_updateTime = rs.getTimestamp(2);
					
					// Add delta time between the profiler and source db.  If the profiler is on the same machine this delta should be zero
					_updateTime = new Timestamp(_updateTime.getTime() + _deltaTime);
					log.debug("NSCALogMessage.isUpdateComplete: _updateTime="+_updateTime);
					}
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
		return new NSCALogMessage(workloadId, 
										   	batchCount, 
										   	_name, 
										   	_numHosts,
											_numServices,
											_hostName,
											_serviceName,
											_threshold,
										   	socketInfo, 
										   	dbProfilerConnection, 
										   	dbSourceConnection,
										   	deltaTime,
										   	msgCount
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
			// cpora - no milliseconds
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
		return _numHosts;
	}
	
	private String buildTextMessage(String hostName, String serviceName) {
		// MSG_TEXT_MESSAGE = "Profiler NSCAInit Message, Host=%1$s, Workload Id=%2$d, BatchCount=%3$d, MsgCount=%4$d";
		return String.format(MSG_TEXT_MESSAGE, hostName, serviceName, _workloadId, _batchCount, _msgCount,  _lastInsertDate, _monitorStatus);
	}
	
	private String buildHostName(int i)
	{
		return String.format(HOST_NAME_FORMAT, _hostName, _workloadId, i);
	}
	
	private String buildHostName()
	{
		return String.format(HOST_NAME_FORMAT2, _hostName, _workloadId);
	}
	
	
	private String buildServiceName(int i)
	{
		return String.format(SERVICE_NAME_FORMAT, _serviceName, _workloadId, i);
	}
	
	private String buildServiceName()
	{
		return String.format(SERVICE_NAME_FORMAT2, _serviceName, _workloadId);
	}
	
	
}