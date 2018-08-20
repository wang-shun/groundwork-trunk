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
import java.sql.Statement;
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

public class SystemAdminInitMessage extends WorkloadMessage
{
	// SQL Statements
	private static final String PSTMT_CHECK_UPDATED =
		"SELECT Count(*) As NumUpdated from Host h ";
	

	//private static String MSG_TEXT_MESSAGE = "Profiler SystemAdminInit Message, Workload Id=%1$d, BatchCount=%2$d";
	
	private static String MSG_SYSTEMADMIN_INIT_MESSAGE = "Profiler SystemAdminInit Message, Host=%1$s";

	//private static final String DEVICE_NAME_FORMAT = "EVM_DVC_%1$s_%2$d_%3$d";
	//private static final String HOST_NAME_FORMAT = "HCM_HST_%1$s_%2$d_%3$d_%4$d";

	private static String DEVICE_NAME_FORMAT = "HST_DVC_%1$s_%2$d_";
	private static final String HOST_NAME_FORMAT = DEVICE_NAME_FORMAT;
	private static final String SERVICE_DESCRIPTION_FORMAT = "TSSM_SVC_%1$s_%2$d_";
	private static final String HOSTGROUP_NAME_FORMAT = "HG_"+DEVICE_NAME_FORMAT;
	// Number of devices to create and receive event messages
	private int _numDevices = 10;
	private int _numHosts = 10;
	private int _numServices = 10;

	// Time updated has been completed.
	//private Timestamp _updateTime = null;
	private long _updateTime = 0;
	// Last Insert Date of the message we sent.  We store in order to verify update
	private String _lastInsertDate = null;

	// Where clause built up from number of devices
	private String _whereClause = null;
	
	// Log	
	protected static Log log = LogFactory.getLog(EventMessage.class);
	
	/**
	 * Factory constructor for "runnable" instances
	 * @param workloadId
	 * @param numDevices
	 * @param name
	 * @param messageSocket
	 * @param dbProfilerConnection
	 * @param dbSourceConnection
	 */
	private SystemAdminInitMessage (int workloadId,
									 int batchCount,
									 int numDevices, 
									 int numHosts,
									 int numServices,
									 String name, 
									 long threshold,
									 MessageSocketInfo messageSocketInfo,
									 Connection dbProfilerConnection,
									 Connection dbSourceConnection,
									 long deltaTime,
									 int msgCount)
	{

		super(workloadId, batchCount, msgCount, name, threshold, messageSocketInfo, dbProfilerConnection, dbSourceConnection, deltaTime);

		_numDevices = numDevices;
		_numHosts = numHosts;
		_numServices = numServices;
		_whereClause = buildWhereClause ();		
	}
	
	

	private String buildWhereClause ()
	{
		// where substr(HostName,35) between 3 and 7
		// Build up where clause
		String hostName = String.format(HOST_NAME_FORMAT, "test", _workloadId);
		int nameLength = hostName.length()+1;
		
		StringBuilder sb = new StringBuilder("where substr(h.HostName, ");
		sb.append(nameLength);
		sb.append(") between 1 and ");
		sb.append(_numHosts);
		
		/*StringBuilder sb = new StringBuilder("WHERE (");
		String hostNameClause = "h.HostName = '";
		for (int i = 0; i < _numHosts; i++) {

			if (i > 0) {
				sb.append(OR);
			}

			sb.append(hostNameClause);
			sb.append(buildHostName(_name, _workloadId, _batchCount,  i));
			sb.append(SINGLE_QUOTE);
		}

		sb.append(CLOSE_PAREN);
		*/
		return sb.toString();
	}
	
	
	/**
	 * Default constructor is required for all WorkloadMessages with the NamedNodeMap parameter
	 * @param attributes
	 * @throws ProfilerException
	 */
	public SystemAdminInitMessage(Node messageNode, NamedNodeMap attributes) throws ProfilerException
	{     
		super(messageNode, attributes);
		
		// Parse out specific attributes
        Node numDevicesNode = attributes.getNamedItem("numDevices");  
        if (numDevicesNode == null)
        {
        	throw new ProfilerException("numDevices attribute is missing from SystemAdminInitMessage.");
        }
        
        String numDevices = numDevicesNode.getNodeValue();     
        try {
        	_numDevices = Integer.parseInt(numDevices);
        }
        catch (Exception e)
        {
        	throw new ProfilerException("SystemAdminInitMessage.ctor - Invalid numDevices setting." 
        								+ numDevices);
        }   	
        
		Node numHostsNode = attributes.getNamedItem("numHosts");
		if (numHostsNode == null) {
			throw new ProfilerException(
					"numHosts attribute is missing from SystemAdminInitMessage.");
		}
		String numHosts = numHostsNode.getNodeValue();
		try {
			_numHosts = Integer.parseInt(numHosts);
		} catch (Exception e) {
			throw new ProfilerException(
					"SystemAdminInitMessage.ctor - Invalid numHosts setting."
							+ numHosts);
		}
        
		Node numServicesNode = attributes.getNamedItem("numServices");
		if (numServicesNode == null) {
			throw new ProfilerException(
					"numServices attribute is missing from SystemAdminInitMessage.");
		}
		String numServices = numServicesNode.getNodeValue();
		try {
			_numServices = Integer.parseInt(numServices);
		} catch (Exception e) {
			throw new ProfilerException(
					"SystemAdminInitMessage.ctor - Invalid numServices setting."
							+ numServices);
		}
        
	}
	
	public int getnumDevices ()
	{
		return _numDevices;
	}
	

	public int getNumHosts() {
		return _numHosts;
	}
	
	public int getNumServices() {
		return _numServices;
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
		
		sb.append(super.toString());
		
		return sb.toString();
	}
	
	public String buildMessage () throws ProfilerException
	{		
		SystemAdminUtil sysadmin = new SystemAdminUtil();
		String deviceName = String.format(DEVICE_NAME_FORMAT, "test", _workloadId);
		_lastInsertDate = SQL_DATE_FORMAT.format(new Date());
		String hostName = deviceName;
		String hostGroupName = String.format(HOSTGROUP_NAME_FORMAT, "test", _workloadId);
		String description = String.format(MSG_SYSTEMADMIN_INIT_MESSAGE, hostName);
		String serviceDescription = buildServiceDescription ("test", _workloadId);
		
		String outPut = "";
		try
		{
			String session = String.format(SESSION_NAME_FORMAT, _workloadId, _batchCount, _msgCount);
			outPut = sysadmin.getInitMessage (session, "SystemAdmin", "NAGIOS", hostName, description, 
					deviceName, hostName, serviceDescription, hostGroupName, _numHosts, _numServices, _lastInsertDate); // LastStateChange for service and host
			outPut = "<SYNC action=\'start\'/>" + outPut+"<SYNC action=\'stop\'/>";
			
		}
		catch (javax.xml.parsers.ParserConfigurationException e){log.debug(e);}
		return outPut;
		
	}
	

	public Timestamp captureMetrics () 
		throws ProfilerException
	{	
		//return _updateTime;
		return new Timestamp(_updateTime);
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
		return new SystemAdminInitMessage(workloadId, 
										   batchCount, 
										   _numDevices,
										   _numHosts,
										   _numServices,
										   _name, 
										   _threshold,
										   socketInfo, 
										   dbProfilerConnection, 
										   dbSourceConnection,
										   deltaTime,
										   msgCount);
	}
	
	public boolean isUpdateComplete() throws ProfilerException
	{
		String deviceName = String.format(DEVICE_NAME_FORMAT, "test", _workloadId);
		String hostName = deviceName;
		String textMessage = String.format(MSG_SYSTEMADMIN_INIT_MESSAGE, hostName);

		Statement stmt = null;
		ResultSet rs = null;
		
		
		try {
			stmt = _dbSourceConnection.createStatement();
			rs = stmt.executeQuery(PSTMT_CHECK_UPDATED);
			if (rs.next())
			{
				int count = rs.getInt(1);

				if (count >= _numHosts)
				{
					_updateTime = System.currentTimeMillis();
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
	
	public int getCheckCount()
	{
		return _numDevices + _numHosts + _numHosts * _numServices + 2;
	}
	
	private String buildHostName(String name, int workloadId, int batchCount)
	{
		return String.format(HOST_NAME_FORMAT, name, workloadId);
	}
	private String buildHostName(String name, int workloadId, int batchCount, int hostNum)
	{
		return String.format(HOST_NAME_FORMAT, name, workloadId, batchCount, hostNum);
	}
	

	
	private String buildDeviceName (String name, int workloadId)
	{
		return String.format(DEVICE_NAME_FORMAT, name, workloadId);
	}
	
	private String buildServiceDescription(String name, int workloadId) {
		return String.format(SERVICE_DESCRIPTION_FORMAT, name, workloadId);
	}
	
}