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
package org.groundwork.foundation.profiling;
import java.io.*;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.profiling.WorkloadMgr.CaptureMetrics;
import org.groundwork.foundation.profiling.exceptions.ProfilerException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public abstract class WorkloadMessage implements IWorkloadMessage
{
	// SQL Statements
	private static final String PSTMT_INSERT_MESSAGE_BATCH = 
		"INSERT INTO MessageBatches (WorkloadID, WorkloadBatchId, MessageName, Threshold, BatchStartTime, BatchEndTime, BStartTimeString, BEndTimeString,TimeRecorded, Latency, NumberOfChecks) " +
		"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";	
	
	// String Constants
	protected static final String SINGLE_QUOTE = "'";
	protected static final String OPEN_PAREN = "(";
	protected static final String CLOSE_PAREN = ")";
	protected static final String OR = " OR ";
	
	// One Minute Default Threshold
	private static long DEFAULT_THRESHOLD = 36000;
	
	// Workload Id of which this instance is related
	protected int _workloadId = -1;
	
	// The workload batch count for this workload message
	protected int _batchCount;
	protected int _msgCount = 0;
	// sessionID = sessionId, workloadBatch, messageCount
	public  String SESSION_NAME_FORMAT = "%1$d_%2$d_%3$d";
	// Unique name for workload message
	protected String _name = null;		

	// Message Socket Info
	protected MessageSocketInfo _messageSocketInfo = null;
	
	// Profiler Database Connection
	protected Connection _dbProfilerConnection = null;
	
	// Source Database Connection - Foundation
	protected Connection _dbSourceConnection = null;
		
	// Delta between profiler system time and source db system time
	protected long _deltaTime = 0;
	
	// Threshold in milliseconds used for acceptence output
	protected long _threshold = DEFAULT_THRESHOLD;
	
	// Latency metric to process this message - A -1 indicates the metric has not been captured.
	protected long _latency = -1;
	protected Object _tg;
		
	protected SimpleDateFormat SQL_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	protected SimpleDateFormat SQL_DATE_FORMAT2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSSS");
	// Log
	private static Log log = LogFactory.getLog(WorkloadMessage.class);
		
	
	/**
	 * Constructor used to create "runnable" version
	 * @param workloadId
	 * @param name
	 * @param messageSocket
	 * @param dbProfilerConnection
	 * @param dbSourceConnection
	 */
	protected WorkloadMessage (int workloadId,
			int batchCount,
			int msgCount, 
			String name, 
			long threshold,
			MessageSocketInfo messageSocketInfo,
			Connection dbProfilerConnection,
			Connection dbSourceConnection,
			long deltaTime)
	{
		_workloadId = workloadId;
		_batchCount = batchCount;
		_msgCount = msgCount;
		_name = name;
		_messageSocketInfo = messageSocketInfo;
		_dbProfilerConnection = dbProfilerConnection;
		_dbSourceConnection = dbSourceConnection;
		_deltaTime = deltaTime;
		_threshold = threshold;
	}
	
	/**
	 * Default Constructor
	 *
	 */
	public WorkloadMessage (Node messageNode, NamedNodeMap attributes) throws ProfilerException
	{
		
		if (attributes == null)
		{
			throw new IllegalArgumentException("Invald null attribute NamedNodeMap.");
		}		

		// Parse out common attributes
        Node nameNode = attributes.getNamedItem("name");
        if (nameNode == null)
        {
        	throw new ProfilerException("name attribute is missing from message.");
        }
        
        _name = nameNode.getNodeValue();		
        
        Node thresholdNode = attributes.getNamedItem("threshold");
        if (thresholdNode != null)
        {
        	String threshold = null;
    		try {
    			 threshold =  thresholdNode.getNodeValue();
    			
    			_threshold = Integer.parseInt(threshold);
    			
    			_threshold *= 1000; // Convert to milliseconds
    		}
    		catch (Exception e) 
    		{
    			log.warn(	String.format("WorkloadMessage.ctor - Invalid threshold value defaulting to %1$d - [%2$d].", DEFAULT_THRESHOLD, threshold));
    		}        	
        }

	}
	
	public String getName ()
	{
		return _name;
	}
	
	public void setTg(Object tg)
	{
		_tg = tg;
	}
	protected MessageSocketInfo getMessageSocketInfo ()
	{
		return _messageSocketInfo;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(32);
		sb.append("Workload ID: ");
		sb.append(_workloadId);			
		sb.append(", Name: ");
		sb.append(_name);			
		sb.append(", Threshold (ms):  ");
		sb.append(_threshold);		
		sb.append(", Batch Count: ");
		sb.append(_batchCount);		
		
		if (_latency < 0)
		{
			sb.append(", Message Latency (ms):  Not  captured.");
		}
		else {			
			sb.append(" - Message Latency (ms): ");
			sb.append(_latency);
		}
		
		return sb.toString();
	}
	
	public void run() 
	{
		if (log.isDebugEnabled()) log.debug("WorkloadMessage:WorkloadMessage.run() -------");
		try 
		{
			if (log.isDebugEnabled()) {
				log.debug(_name + " Workload Message being posted.");
			}
			
			long batchStartTime = System.currentTimeMillis();
			String msg = buildMessage();

			if (!_name.equals("NSCALogMessage"))
			{
				sendMessage(msg);
			}
			// We are not capturing any metrics - Profiler used for simulating messages only
			if (WorkloadMgr.isCapturingMetrics() == CaptureMetrics.OFF) {
				return;
			}
			if (log.isDebugEnabled()) {
				log.debug(_name + " Workload Message Metrics being captured.");
			}
			long startUpdateCheck = System.currentTimeMillis();
			long timeWaitingForUpdate = 0;

			// Let the message be processed.  This may effect metrics if we are querying the source database
			// in order to determine the time to update, 
			Thread.sleep(100);

			while (isUpdateComplete() == false) {
				if (log.isDebugEnabled()) {
					log.debug("WorkloadMessage.run() :Checking Message Completion - Message: [" + _name + 
							"], Workload: [" + _workloadId + "], Batch: [" + _batchCount + 
							"], Wait Time (ms)=[" + timeWaitingForUpdate+"]");
				}

				// We give 1 hour to update or else we timeout
				if (timeWaitingForUpdate >= 3600000)
				{
					log.error("WorkloadMessage.run(): Batch timeout - Workload Message: " + toString());
					WorkloadMgr.batchFailed(this);
					return;
				}

				// Let the message be processed.  This may effect metrics if we are querying the source database
				// in order to determine the time to update.  If time for update increase we sleep for a longer period
				// since proportionally it will not effect the update metric captured.
				if (timeWaitingForUpdate > 600000) {
					Thread.sleep(1000);
				} else {
					Thread.sleep(100);
				}
				timeWaitingForUpdate = System.currentTimeMillis() - startUpdateCheck;
			}
			Timestamp timeRecorded = captureMetrics();

			// Update latency value for this message
			_latency =  timeRecorded.getTime() - batchStartTime;

			if (_latency > _threshold)
			{
				WorkloadMgr.batchFailed(this);
				log.error(String.format("Workload Message Excedes Threshold - Threshold=%1$d,  Latency= %2$d, Difference=%3$d (ms)", _threshold, _latency, (_latency - _threshold)));
			}
			
			// We don't add the message until we know it successfully completed			
			// Don't do anthing if we are not capturing metrics
			if (WorkloadMgr.isCapturingMetrics() == CaptureMetrics.ALL)
			{	
				createWorkloadBatch(_workloadId, _batchCount, _name, _threshold, batchStartTime, System.currentTimeMillis(), timeRecorded);
			}	
		}
		catch (Exception e)
		{
			log.error("Error occurred running Workload Message.", e);
		}
	}
	
	private void sendMessage (String msg) throws IOException, ProfilerException
	{
		if (log.isDebugEnabled()) log.debug("WorkloadMessage:WorkloadMessage.sendMessage(msg)");
		if (msg == null || msg.length() == 0)
		{
			log.warn("Not posting message because message string is null or empty.");
			return;
		}
		
		if (_messageSocketInfo == null) {
			log.warn("Trying to send message, but message socket info is null.");
			return;
		}
		
		// Write message to socket
		if (log.isDebugEnabled()) {
			log.debug("WorkloadMessage:Message: [" + msg + "]");
		}
		
		Socket socket = _messageSocketInfo.createSocket();

		OutputStream outStream = socket.getOutputStream();
		outStream.write(msg.getBytes());
		outStream.write("<SERVICE-MAINTENANCE command='close' />".getBytes());
		outStream.close();
		socket.close();		
	}
	
	protected int createWorkloadBatch (int workloadId, 
														   int workloadBatchId, 
														   String name, 
														   long threshold, 
														   long startTime, 
														   long endTime, 
														   Timestamp timeRecorded) 
	throws SQLException
	{
		if (log.isDebugEnabled()) log.debug("WorkloadMessage:WorkloadMessage.createWorkloadBatch ----------------");
		
		// Don't do anthing if we are not capturing metrics
		if (WorkloadMgr.isCapturingMetrics() == CaptureMetrics.OFF)
		{		
			return -1;
		}

		PreparedStatement pstmt = _dbProfilerConnection.prepareStatement(PSTMT_INSERT_MESSAGE_BATCH);
		pstmt.setInt(1, workloadId);
		pstmt.setInt(2, workloadBatchId);
		pstmt.setString(3, name);
		pstmt.setLong(4, threshold);
		pstmt.setTimestamp(5, new Timestamp(startTime));
		pstmt.setTimestamp(6, new Timestamp(endTime));
		pstmt.setString(7, new Long(startTime).toString());
		pstmt.setString(8, new Long(endTime).toString());
		pstmt.setTimestamp(9, timeRecorded);
		pstmt.setLong(10, _latency);
		pstmt.setLong(11, getCheckCount());
		pstmt.execute();
		
		ResultSet rs = pstmt.getGeneratedKeys();
		
		int id = -1;
		
		if (rs.next() == true) {
			id = rs.getInt(1);
		}
		
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
		
		return id;
	}
}