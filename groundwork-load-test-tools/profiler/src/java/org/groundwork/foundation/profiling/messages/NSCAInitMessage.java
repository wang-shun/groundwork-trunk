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

import java.io.IOException;
import java.sql.Connection;

import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.profiling.IWorkloadMessage;
import org.groundwork.foundation.profiling.MessageSocketInfo;
import org.groundwork.foundation.profiling.PassiveChecks;
import org.groundwork.foundation.profiling.WorkloadMessage;
import org.groundwork.foundation.profiling.exceptions.ProfilerException;
import org.groundwork.foundation.profiling.SystemAdminUtil;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class NSCAInitMessage extends WorkloadMessage
{
	// SQL Statements
	//private static final String PSTMT_CHECK_UPDATED ="SELECT Count(*) As NumUpdated from Host h ";
	
	private static final String PSTMT_CHECK_UPDATED ="SELECT Count(*) As NumUpdated from (ServiceStatus t1 INNER JOIN  Host t2 ON t1.HostID=t2.HostID) inner join MonitorStatus t3 on t1.MonitorStatusID=t3.MonitorStatusID ";
	//private static String MSG_TEXT_MESSAGE = "Profiler NSCAInit Message, Workload Id=%1$d, BatchCount=%2$d";
	
	private static String MSG_NSCA_INIT_MESSAGE = "Profiler NSCAInit Message, Host=%1$s";
	
	//private static final String DEVICE_NAME_FORMAT = "EVM_DVC_%1$s_%2$d_%3$d";
	//private static final String HOST_NAME_FORMAT = "HCM_HST_%1$s_%2$d_%3$d_%4$d";

	private static String DEVICE_NAME_FORMAT = "%1$s_%2$d";
	//private static final String HOST_NAME_FORMAT = DEVICE_NAME_FORMAT;
	private static final String SERVICE_DESCRIPTION_FORMAT = "%1$s_%2$d";
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
	
	private String _hostName = "HTest";
	private String _serviceName = "STest";
	private String _ipAddress = "100.100.100.";
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
	private NSCAInitMessage (int workloadId,
									 int batchCount,
									 int numDevices, 
									 int numHosts,
									 int numServices,
									 String hostName,
									 String serviceName,
									 String ipAddress,
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
		_hostName = hostName;
		_serviceName = serviceName;
		_ipAddress = ipAddress;
		_whereClause = buildWhereClause ();		
	}
	
	

	private String buildWhereClause ()
	{
		String hostName = buildHostName();
		/*
		int nameLength = hostName.length()+1;
		
		StringBuilder sb = new StringBuilder("where substr(h.HostName, ");
		sb.append(nameLength);
		sb.append(") between 1 and ");
		sb.append(_numHosts);
		return sb.toString();
		*/
		
		StringBuilder sb = new StringBuilder(" where t2.HostName like '");
		sb.append(hostName);
		sb.append("%'");
		sb.append(" and t3.Name='PENDING'");
		return sb.toString();
	}
	
	
	/**
	 * Default constructor is required for all WorkloadMessages with the NamedNodeMap parameter
	 * @param attributes
	 * @throws ProfilerException
	 */
	public NSCAInitMessage(Node messageNode, NamedNodeMap attributes) throws ProfilerException
	{     
		super(messageNode, attributes);
        log.debug("NSCAInitMessage(Node messageNode, NamedNodeMap attributes) ");
		Node numHostsNode = attributes.getNamedItem("numHosts");
		if (numHostsNode == null) {
			throw new ProfilerException(
					"numHosts attribute is missing from NSCAInitMessage.");
		}
		String numHosts = numHostsNode.getNodeValue();
		
        log.debug("NSCAInitMessage numHosts ["+numHosts+"]");
		try {
			_numHosts = Integer.parseInt(numHosts);
		} catch (Exception e) {
			throw new ProfilerException(
					"NSCAInitMessage.ctor - Invalid numHosts setting."
							+ numHosts);
		}
        _numDevices = _numHosts;
        
		Node numServicesNode = attributes.getNamedItem("numServices");
		if (numServicesNode == null) {
			throw new ProfilerException(
					"numServices attribute is missing from NSCAInitMessage.");
		}
		String numServices = numServicesNode.getNodeValue();
		log.debug("NSCAInitMessage numServices ["+numServices+"]");
		try {
			_numServices = Integer.parseInt(numServices);
		} catch (Exception e) {
			throw new ProfilerException(
					"NSCAInitMessage.ctor - Invalid numServices setting."
							+ numServices);
		}
        
        Node hostNameNode = attributes.getNamedItem("hostName");  
        if (hostNameNode == null)
        {
        	throw new ProfilerException("hostName attribute is missing from NSCAInitMessage.");
        }
        
        _hostName = hostNameNode.getNodeValue();
        log.debug("NSCAInitMessage hostName ["+_hostName+"]");
        Node serviceNameNode = attributes.getNamedItem("serviceName");  
        if (serviceNameNode == null)
        {
        	throw new ProfilerException("serviceName attribute is missing from NSCAInitMessage.");
        }
        
        _serviceName = serviceNameNode.getNodeValue();
        log.debug("NSCAInitMessage _serviceName ["+_serviceName+"]");
        Node ipAddressNode = attributes.getNamedItem("ipAddress");  
        if (ipAddressNode == null)
        {
        	throw new ProfilerException("ipAddress attribute is missing from NSCAInitMessage.");
        }
        
        _ipAddress = ipAddressNode.getNodeValue();
        log.debug("NSCAInitMessage _ipAddress ["+_ipAddress+"]");
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
		

		sb.append("Num Services: ");
		sb.append(_numServices);
		sb.append(", ");
		
		sb.append(super.toString());
		
		return sb.toString();
	}
	
	public String buildMessage () throws ProfilerException
	{		
		boolean error = false;
		SystemAdminUtil sysadmin = new SystemAdminUtil();
		String deviceName = buildHostName();
		_lastInsertDate = SQL_DATE_FORMAT.format(new Date());
		String hostName = deviceName;
		String hostGroupName = String.format(HOSTGROUP_NAME_FORMAT, _hostName, _workloadId);
		String description = String.format(MSG_NSCA_INIT_MESSAGE, hostName);
		String serviceName = buildServiceName();
		String outPut = "";
		try
		{
			String session = String.format(SESSION_NAME_FORMAT, _workloadId, _batchCount, _msgCount);
			outPut = sysadmin.getInitMessage (session, "SystemAdmin", "NAGIOS", hostName, description, 
					deviceName, hostName, serviceName, hostGroupName, _numHosts, _numServices, _lastInsertDate); // LastStateChange for service and host
			outPut = "<SYNC action=\'start\'/>" + outPut+"<SYNC action=\'stop\'/>";
			
		}
		catch (javax.xml.parsers.ParserConfigurationException e){log.error(e);error = true;}
		if (error) 
		throw new ProfilerException("NSCAInitMessage.buildMessage: error creating SystemAdmin message!");
		else
		if (configureNagios(hostName, serviceName)) 
		throw new ProfilerException("NSCAInitMessage.configureNagios: error creating Nagios configuration files!");
		else
		return outPut;
		
	}
	
	
	public boolean configureNagios(String hostName, String serviceName)
	{
		boolean error = false;
		// String textMessage = buildTextMessage();
		_lastInsertDate = SQL_DATE_FORMAT.format(new Date(System.currentTimeMillis() - _deltaTime)); // Adjust to source db time
		try
		{
			if (log.isDebugEnabled()) log.debug("NSCAInitMessage____________PassiveChecks.start");
			//PassiveChecks.createConfigurations(_tg, _numHosts, _numServices, hostName, _ipAddress, serviceName);
			PassiveChecks.createConfigurations(_numHosts, _numServices, hostName, _ipAddress, serviceName);
			PassiveChecks.executeCommand("/usr/local/groundwork/monarch/bin/nagios_reload");
			if (log.isDebugEnabled()) log.debug("NSCAInitMessage____________PassiveChecks.end");
		}
		catch (IOException io){log.error("NSCAInitMessage.configureNagios:"+io); error = true;}
		return error;
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
		return new NSCAInitMessage(workloadId, 
										   batchCount, 
										   _numDevices,
										   _numHosts,
										   _numServices,
										   _hostName,
										   _serviceName,
										   _ipAddress,
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
		Statement stmt = null;
		ResultSet rs = null;

		try {
			stmt = _dbSourceConnection.createStatement();
			log.debug("NSCAInitMessage:isUpdateComplete()"+PSTMT_CHECK_UPDATED + _whereClause);
			rs = stmt.executeQuery(PSTMT_CHECK_UPDATED + _whereClause);
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
				log.debug("NSCAInitMessage:isUpdateComplete()");
				throw new ProfilerException("Unable to check if update is complete.");
			}			
		}
		catch (Exception e)
		{
			log.debug("NSCAInitMessage:isUpdateComplete()"+e);
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
	
	private String buildHostName()
	{
		return String.format(DEVICE_NAME_FORMAT, _hostName, _workloadId);
	}
	
	private String buildServiceName()
	{
		return String.format(SERVICE_DESCRIPTION_FORMAT, _serviceName, _workloadId);
	}
	
}