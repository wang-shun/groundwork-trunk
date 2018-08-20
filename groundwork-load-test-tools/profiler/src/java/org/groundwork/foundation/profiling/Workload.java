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

import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.profiling.WorkloadMgr.CaptureMetrics;
import org.groundwork.foundation.profiling.exceptions.ProfilerException;
import org.groundwork.foundation.profiling.messages.SystemAdminInitMessage;
import org.groundwork.foundation.profiling.messages.NSCAInitMessage;
public class Workload implements Runnable
{
	// SQL Statements
	private static final String PSTMT_INSERT_WORKLOAD = "INSERT INTO Workloads (SessionID, Name, StartTime, LStartTime) VALUES(?, ?, ?, ?)";
	private static final String PSTMT_UPDATE_WORKLOAD_END_TIME = "UPDATE Workloads SET EndTime = ?, LEndTime = ? WHERE WorkloadID = ?";
	
	// String Format Constants
	private static final String MSG_EXECUTING_MESSAGE = "%1$s Workload is about to send messages.  Time (ms) since last run = %2$d";
	
	// Unique name of workload
	private String _name = null;
	
	// Num of batches to run
	private long _numBatches = 1;
	
	// Time interval of message in milliseconds
	private int _interval = 60000;
	
	// Number of messages to send each time interval
	private int _quantity = 1;
		
	// Message distribution defining how message quantit
	private MessageDistribution _distribution = MessageDistribution.EVEN;
	
	// Boolean indicating whether the workload is enabled
	private boolean _isEnabled = true;
	private Object _tg;
	// List of messages
	private List<IWorkloadMessage> _messages = new ArrayList<IWorkloadMessage>(5);
	
	// Boolean indicating the workload has been initialized
	private boolean _isInitialized = false;
		
	// Boolean value indicating the workload is finished sending messages and processing results
	private boolean _isComplete = false;
	
	// Start time of work load
	//private final long _startTime = 0;
	private long _startTime = 0;
	
	// Last run of messages
	private long _lastRun = 0;
	
	// Message Socket Info
	protected MessageSocketInfo  _messageSocketInfo = null;
	
	// Profiler Database Connection
	protected Connection _dbProfilerConnection = null;
	
	// Source Database Connection - Foundation
	protected Connection _dbSourceConnection = null;
	
	// Workload Sessoin Id
	private int _sessionId = -1;
	
	// Delta between profiler system time and source db system time
	private long _deltaTime = 0;
	
	// Random Generator
	private Random rnd = new Random();
	
	// Log
	protected static Log log = LogFactory.getLog(Workload.class);
	
	public Workload (String name,
			int numBatches, 
			int interval, 
			int quantity, 
			MessageDistribution distribution,
			boolean enabled)
	{
		if (name == null || name.length() == 0)
		{
			throw new IllegalArgumentException("Invalid null / empty workload name.");
		}
		
		if (numBatches < 1) {
			throw new IllegalArgumentException("Workload numBatches must be greater than 0.");
		}		
		
		if (interval < 1) 
		{
			throw new IllegalArgumentException("Workload interval must be greater than 0.");
		}
				
		_name = name;
		_numBatches = numBatches;
		_interval = interval * 1000;  // Convert to milliseconds
		_quantity = quantity;
		_distribution = distribution;
		_isEnabled = enabled;
	}
	
	public void initialize (int sessionId,
							MessageSocketInfo messageSocketInfo, 
							DBConnectionInfo profilerDBInfo,
							DBConnectionInfo sourceDBInfo,
							long deltaTime, Object tg)
	throws ProfilerException
	{
		log.debug("Workload:Workload.initialize ---- messageSocketInfo"+ messageSocketInfo+"  profilerDBInfo"+profilerDBInfo+"   sourceDBInfo"+sourceDBInfo+"   deltaTime"+deltaTime);
		if (_messages == null)
		{
			return;
		}
		_tg = tg;
		_sessionId = sessionId;
		_deltaTime = deltaTime;
		
		// Create Profiler Database Connection
		if (WorkloadMgr.isCapturingMetrics() == CaptureMetrics.ALL) {
			_dbProfilerConnection = profilerDBInfo.createConnection();
		}
		
		_dbSourceConnection = sourceDBInfo.createConnection();
		_messageSocketInfo = messageSocketInfo;
		
		_isInitialized = true;
	}
	
	public void uninitialize ()
	{
		if (_messages == null)
		{
			return;
		}		
		
		if (_dbProfilerConnection != null)
		{
			try {
				_dbProfilerConnection.close();
				_dbProfilerConnection = null;
			}
			catch (Exception e)
			{
				log.error("Error occurred closing profiler db connection.", e);
			}
		}

		if (_dbSourceConnection != null)
		{
			try {
				_dbSourceConnection.close();
				_dbSourceConnection = null;
			}
			catch (Exception e)
			{
				log.error("Error occurred closing source db connection.", e);
			}
		}
		
		_isInitialized = false;
	}
	
	public String getName ()
	{
		return _name;
	}

	public long getNumBatches ()
	{
		return _numBatches;
	}
	
	public int getInterval ()
	{
		return _interval;
	}	
	
	public int getQuantity ()
	{
		return _quantity;
	}	

	public MessageDistribution getDistribution ()
	{
		return _distribution;		
	}
		
	public boolean getEnabled ()
	{
		return _isEnabled;
	}
	
	/**
	 * Note, For now, we are returning the internal reference which means clients can modify the 
	 * list.
	 * @return
	 */
	public List<IWorkloadMessage> getMessages ()
	{
		return _messages;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(64);
		sb.append("Name: ");
		sb.append(_name);
		sb.append(", ");
		
		sb.append("Num Batches: ");
		sb.append(_numBatches);
		sb.append(", ");
		
		sb.append("Interval: " );
		sb.append(_interval);
		sb.append(", ");
		
		sb.append("Quantity: " );
		sb.append(_quantity);
		sb.append(", ");
		
		sb.append("Distribution: " );
		sb.append(_distribution);	
		sb.append(", ");
		
		sb.append("IsEnabled: ");
		sb.append(_isEnabled);
		sb.append("\n\tWorkload Messages:\n");
		
		// Output each message
		Iterator<IWorkloadMessage> it = _messages.iterator();
		while (it.hasNext())
		{
			sb.append(it.next().toString());
		}
		
		return sb.toString();
	}
	
	public void run()
	{
		if (_isInitialized == false)
		{
			log.error("Workload must be initialized to execute - Workload: " + _name);
			return;
		}
		
		// Set start time
		_startTime = System.currentTimeMillis();		
		
		// Create an entry in the profiler database for the new workload, if we are actually capturing metrics
		int workloadId = -1;
		try {
			workloadId = createWorkload(_startTime);
			log.info("Workload Id:  " + workloadId);
		}
		catch (Exception e)
		{
			log.error("Error saving workload to profiler db - Workload: " + _name, e);
			return;
		}
		//=======================================================================================
		// cpora March 20 2008
		// add an SystemAdminMessage initialization process.
		//
		int msgCount = 1;
		//SystemAdminInitMessage sysadminMsg = null;
		
		WorkloadMessage sysadminMsg = null;
		Iterator<IWorkloadMessage> its = _messages.iterator();

		while (its.hasNext())
		{			
			log.debug("Workload:----------workloadId="+workloadId);
			log.debug("Workload:----------_messageSocketInfo="+_messageSocketInfo);
			log.debug("Workload:----------_dbProfilerConnection="+_dbProfilerConnection);
			log.debug("Workload:----------_dbSourceConnection="+_dbSourceConnection);
			log.debug("Workload:----------_deltaTime="+_deltaTime);
			log.debug("Workload:----------msgCount="+msgCount);
			WorkloadMessage message = (WorkloadMessage)its.next().getRunnableInstance(workloadId,
																	 1,//workloadBatch,
																	 _messageSocketInfo, 
																	 _dbProfilerConnection,
																	 _dbSourceConnection,
																	 _deltaTime,
																	 msgCount);
			if (message.getName().equals("SystemAdminInitMessage"))
			{
				sysadminMsg = (SystemAdminInitMessage) message;
				its.remove();
				break;

			}
			else
			if (message.getName().equals("NSCAInitMessage"))
			{
				sysadminMsg = (org.groundwork.foundation.profiling.messages.NSCAInitMessage) message;
				its.remove();
				break;
			}	
			msgCount++;
		}
		if (sysadminMsg != null)
		{
			try
			{
				Thread tSysAdmin = new Thread(sysadminMsg, _name);
				tSysAdmin.start();
				tSysAdmin.join();
			} catch (InterruptedException e)
			{
				log.error("Error failed to Initialize "+_name+" workload to profiler db - Workload: " + _name, e);
				return;
			}
		}
		//=======================================================================================
		// Number of workload batches sent
		int workloadBatch = 0;
		int numMessages = _messages.size();
		//=======================================================================================
		// cpora March 20 2008
		// add an SystemAdminMessage initialization process.
		//

		if (numMessages == 0) {
			try {
				updateWorkloadEndTime(workloadId, System.currentTimeMillis());
			}
			catch (Exception e)
			{
				log.error("Error Updating Workload end time.", e);
			}
			_isComplete = true;
			return;
		}
		msgCount = 0;
		//=======================================================================================
		
		
		// Note:  We are limiting the pool to the number of messages in the workload.  We don't allow
		// multiple round of messages to occur b/c it may stop the previous messages from being
		// able to capture metrics (e.g. Host Status Toggle)
		ThreadPoolExecutor executor = new ThreadPoolExecutor(numMessages, 
				numMessages, 
				5, 
				TimeUnit.SECONDS,
				new ArrayBlockingQueue<Runnable>(100, true));
		//ExecutorService executor = Executors.newCachedThreadPool();
		while (true)
		{
			try {
				// Let calcuations and other workloads execute
				Thread.sleep(1000);
				// We stop all message threads for this workload if we have reached our batch limit
				if (workloadBatch >= _numBatches)
				{
					log.info(_name + " Workload has reached batch limit of " + _numBatches);				

					// We wait until the last round is completed in order to insure metrics were captured.
					while (executor.getActiveCount() != 0)
					{
						Thread.sleep(5000);
					}
					// Shut down executor previously submitted workload message will be completed.
					executor.shutdown();				
					// Update workload end time in profiler database
					try {
						updateWorkloadEndTime(workloadId, System.currentTimeMillis());
					}
					catch (Exception e)
					{
						log.error("Error Updating Workload end time.", e);
					}
					// Set workload to complete state
					_isComplete = true;
					break;
				}
				else   
				{
					// We wait until the last round is completed in order to insure metrics were captured.
					while (executor.getActiveCount() != 0)
					{
						Thread.sleep(200);
					}
					// Run first time or determine if we need to send the messages again
					long delta = isTimeToSendMessages();
					if (delta >= 0)
					{
						// BURST DISTRIBUTION - Send quantity messages each interval milliseconds
						// OR
						// RANDOM DISTRIBUTION - Send messages at least every interval
						if (_distribution == MessageDistribution.BURST || _distribution == MessageDistribution.RANDOM)
						{

							for (int i = 0; i < _quantity; i++)
							{
								// Increment workload batch for each burst
								workloadBatch++;
								log.info(String.format(MSG_EXECUTING_MESSAGE, _name, delta));

								// Send Messages and capture data - Note: Messages like toggle status my have problems
								// capturing data
								Iterator<IWorkloadMessage> it = _messages.iterator();
								while (it.hasNext())
								{			
									WorkloadMessage message = (WorkloadMessage)it.next().getRunnableInstance(workloadId,
																							 workloadBatch,
																							 _messageSocketInfo, 
																							 _dbProfilerConnection,
																							 _dbSourceConnection,
																							 _deltaTime,
																							 msgCount);
									//if (message.getName().equals("NSCAExtensionMessage"))
									//	message.setTg(_tg);
									msgCount++;
									executor.execute(message);
								}									
							}							
						} else {
							
						// EVEN DISTRIBUTION - Send messages each (interval / quantity) milliseconds
							// Increment workload batch
							workloadBatch++;
							log.info(String.format(MSG_EXECUTING_MESSAGE, _name, delta));
							// Send Messages and capture data
							Iterator<IWorkloadMessage> it = _messages.iterator();
							while (it.hasNext())
							{			
								WorkloadMessage message = (WorkloadMessage)it.next().getRunnableInstance(workloadId,
																						 workloadBatch,
																						 _messageSocketInfo, 
																						 _dbProfilerConnection,
																						 _dbSourceConnection,
																						 _deltaTime,
																						 msgCount);	
								if (message.getName().equals("NSCAExtensionMessage"))
									message.setTg(_tg);								
								msgCount++;
								executor.execute(message);
							}															
						}					
						// Update last run time after message workloads have been excecuted.
						_lastRun = System.currentTimeMillis();
					}	
				}	
			}
			catch (Exception e)
			{
				log.error("Workload.run() error: ", e);
			}		
		}
	}	
	
	public boolean getComplete ()
	{
		return _isComplete;
	}
	
	/**
	 * Returns 0 if first run, < 0 if not time to run or the delta in milliseconds since the last run
	 * @return
	 */
	private long isTimeToSendMessages()
	{
		// First Run
		if (_lastRun == 0)
		{
			return 0;
		}
		
		long delta = System.currentTimeMillis() - _lastRun;
		
		if (log.isDebugEnabled()) {
			log.debug("Workload:***** Last Message DELTA : " + delta);
		}
		
		// Even distribution
		if (_distribution == MessageDistribution.EVEN)
		{
			// For even distribution, we send execute the workload message every (interval / quantity) seconds
			if (delta >= _interval / _quantity)
			{							
				return delta;
			}
		}
		else if (_distribution == MessageDistribution.BURST)
		{
			// Every interval we send quantity messages
			if (delta >= _interval)
			{
				return delta;
			}
		}
		else if (_distribution == MessageDistribution.RANDOM)
		{
			// At the very least, we send every interval with Random distribution
			if (delta >= _interval)
			{
				return delta;
			}			
			
			// Randomly determine if messages should be sent
			if (rnd.nextBoolean() == true)
			{
				return delta;
			}			
		}
		
		return -1;
	}
	
	private int createWorkload (long startTime) throws SQLException
	{
		// Don't do anthing if we are not capturing metrics
		if (WorkloadMgr.isCapturingMetrics() != CaptureMetrics.ALL)
		{					
			return new Date().hashCode(); // Return hash date and time  b/c we are not capturing any metrics - Probably QA Acceptence tests
							 // We return a hashcode because some message may use the workload id to key off of (e.g. EventMessage - TextMessage).
							// Date is not a GUID, but it should suffice
		}

		PreparedStatement stmt = _dbProfilerConnection.prepareStatement(PSTMT_INSERT_WORKLOAD);
		stmt.setInt(1, _sessionId);
		stmt.setString(2, _name);
		stmt.setTimestamp(3, new Timestamp(startTime));
		stmt.setLong(4, startTime);
		stmt.execute();
		
		ResultSet rs = stmt.getGeneratedKeys();
		
		int workloadId = -1;
				
		if (rs.next() == true) {
			workloadId = rs.getInt(1);
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
		
		return workloadId;
	}
	
	private void updateWorkloadEndTime (int workloadId, long endTime) throws SQLException
	{
		// Don't do anthing if we are not capturing metrics
		if (WorkloadMgr.isCapturingMetrics() != CaptureMetrics.ALL)
		{		
			return;
		}

		PreparedStatement pstmt = _dbProfilerConnection.prepareStatement(PSTMT_UPDATE_WORKLOAD_END_TIME);
		pstmt.setTimestamp(1, new Timestamp(endTime));
		pstmt.setLong(2, endTime);
		pstmt.setInt(3, workloadId);
		
		pstmt.execute();		
		
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
