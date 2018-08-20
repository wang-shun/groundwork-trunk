package org.groundwork.foundation.profiling.messages;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.xml.namespace.QName;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.profiling.IWorkloadMessage;
import org.groundwork.foundation.profiling.MessageSocketInfo;
import org.groundwork.foundation.profiling.WorkloadMessage;
import org.groundwork.foundation.profiling.WorkloadMgr;
import org.groundwork.foundation.profiling.WorkloadMgr.CaptureMetrics;
import org.groundwork.foundation.profiling.exceptions.ProfilerException;
import org.groundwork.foundation.ws.model.impl.CheckType;
import org.groundwork.foundation.ws.model.impl.Component;
import org.groundwork.foundation.ws.model.impl.DateProperty;
import org.groundwork.foundation.ws.model.impl.Device;
import org.groundwork.foundation.ws.model.impl.DoubleProperty;
import org.groundwork.foundation.ws.model.impl.ExceptionType;
import org.groundwork.foundation.ws.model.impl.Host;
import org.groundwork.foundation.ws.model.impl.HostGroup;
import org.groundwork.foundation.ws.model.impl.HostStatus;
import org.groundwork.foundation.ws.model.impl.IntegerProperty;
import org.groundwork.foundation.ws.model.impl.LongProperty;
import org.groundwork.foundation.ws.model.impl.MonitorStatus;
import org.groundwork.foundation.ws.model.impl.OperationStatus;
import org.groundwork.foundation.ws.model.impl.Priority;
import org.groundwork.foundation.ws.model.impl.PropertyTypeBinding;
import org.groundwork.foundation.ws.model.impl.ServiceStatus;
import org.groundwork.foundation.ws.model.impl.Severity;
import org.groundwork.foundation.ws.model.impl.StateType;
import org.groundwork.foundation.ws.model.impl.StatisticProperty;
import org.groundwork.foundation.ws.model.impl.StringProperty;
import org.groundwork.foundation.ws.model.impl.TimeProperty;
import org.groundwork.foundation.ws.model.impl.TypeRule;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class HttpRequest extends WorkloadMessage 
{   	
	// Number concurrent http requests to perform
	private int _numRequests = 1;
	
	// Http request URL
	private String _url = null;
	
	private List<HttpRequestRunnable>_requests = null;
	
	// Log
	protected static Log log = LogFactory.getLog(HttpRequest.class);
	
	protected HttpRequest (int workloadId,
			int batchCount,
			String name, 
			long threshold,
			MessageSocketInfo messageSocketInfo,
			Connection dbProfilerConnection,
			Connection dbSourceConnection,
			long deltaTime)
	{
		super(workloadId, batchCount, 0, name, threshold, messageSocketInfo, dbProfilerConnection, dbSourceConnection, deltaTime);		
	}
	
	public HttpRequest (Node messageNode, NamedNodeMap attributes) throws ProfilerException
	{
		super(messageNode, attributes);
		
		if (messageNode == null)
		{
			throw new IllegalArgumentException("Invald null Node parameter.");
		}
		
		if (attributes == null)
		{
			throw new IllegalArgumentException("Invald null attribute NamedNodeMap.");
		}				
			
		// Parse out common attributes
        Node node = attributes.getNamedItem("numRequests");
        if (node != null)
        {
	        String value = node.getNodeValue();        
	        try {
	        	_numRequests = Integer.parseInt(value);
	        }
	        catch (Exception e)
	        {
	        	log.warn("Invalid numRequests value.  Should be integer value > 0.  Defaulting to 1");
	        }   	       
        }
        else {
        	log.warn("Invalid numRequests value.  Should be integer value > 0.  Defaulting to 1");
        }
        
        node = attributes.getNamedItem("url");
        if (node == null) {
			throw new ProfilerException("Missing url attribute value.");
		}
        
        _url = node.getNodeValue();
	}	
	
	private HttpRequest(int workloadId, int batchCount,
			String name, long threshold, MessageSocketInfo messageSocketInfo,
			Connection dbProfilerConnection, Connection dbSourceConnection,
			long deltaTime, int numRequests, String url)
	{
		super(workloadId, batchCount, 0, name, threshold, messageSocketInfo,
				dbProfilerConnection, dbSourceConnection, deltaTime);

		_numRequests = numRequests;
		_url = url;
	}
	
	@Override
	protected MessageSocketInfo getMessageSocketInfo()
	{
		// TODO Auto-generated method stub
		return super.getMessageSocketInfo();
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return super.getName();
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}	
	
	public void run() 
	{
		try 
		{			
			// Create Thread Pool
			ThreadPoolExecutor executor = new ThreadPoolExecutor(_numRequests, 
					_numRequests,
					5, 
					TimeUnit.SECONDS,
					new ArrayBlockingQueue<Runnable>(1));									
			
			// We are starting batch time after setup is complete
			// For now we are recording each individual call which is different from the Collector messages
			// For the collector messages we capture the batch time
			// long batchStartTime = System.currentTimeMillis();
						
			log.info(_name + " Http Request being called concurrently - Num Calls=" + _numRequests);
			
			// Create Array of Runnables which call the web method
			_requests = new ArrayList<HttpRequestRunnable>(_numRequests);									
			HttpRequestRunnable runnable = null;
			for (int i = 0; i < _numRequests; i++)
			{
				runnable = new HttpRequestRunnable(i, log, _url);				
				_requests.add(runnable);
				
				// Run on separate thread
				if (log.isDebugEnabled()) {
					log.debug("Executing http request - " + _url);
				}
				
				executor.execute(runnable);
			}			
			
			long startUpdateCheck = System.currentTimeMillis();
			long timeWaitingForUpdate = 0;
			
			// Wait for all calls to complete			
			while (isUpdateComplete() == false) {
				
			//	if (log.isDebugEnabled())
					log.info("Checking Http Request Batch Completion - Message: " + _name + 
							", Http Request: " + _url +
							", Workload: " + _workloadId + ", Batch: " + _batchCount + 
							", Wait Time (ms)=" + timeWaitingForUpdate);
				
				// We give 1 hour to update or else we timeout
				if (timeWaitingForUpdate >= 3600000)
				{
					log.error("Workload Message Batch timeout - Workload Message: " + toString());
					
					WorkloadMgr.batchFailed(this);
					return;
				}
				
				// Let the message be processed.
				Thread.sleep(1000);
			
				timeWaitingForUpdate = System.currentTimeMillis() - startUpdateCheck;
			}									
			
			// Set latency to be the time of the longest message
			_latency = getMaxCallTime();
			
			// Output duration for each call in order
			captureCallTimes();				

			// Shutdown executor
			executor.shutdown();
		}
		catch (Exception e)
		{
			log.error("Error occurred running Workload HttpRequest.", e);
		}
	}

	public String buildMessage() throws ProfilerException {
		// NOT USED for Web Service Call
		return null;
	}

	public Timestamp captureMetrics() throws ProfilerException 
	{
		// Not used in WS Call
		return null;		
	}

	public IWorkloadMessage getRunnableInstance(int workloadId, int batchCount, MessageSocketInfo messageSocketInfo, Connection dbProfilerConnection, Connection dbSourceConnection, long deltaTime,
			int msgCount) 
	{
		return new HttpRequest(workloadId, batchCount,
				_name, _threshold, messageSocketInfo, dbProfilerConnection, dbSourceConnection,
				deltaTime, _numRequests, _url);
	}

	public boolean isUpdateComplete() throws ProfilerException 
	{
		if (_requests == null || _requests.size() == 0) {
			return true;
		}
				
		// Check to see if all call times are greater than -1
		HttpRequestRunnable runnable = null;
		Iterator<HttpRequestRunnable> it = _requests.iterator();
		while (it.hasNext())
		{
			runnable = it.next();
			if (runnable.getException() == null && runnable.getTimeToFirstByte() < 0)
			{					
				return false;
			}
		}
			
		return true;
	}	
	
	public long getMaxCallTime ()
	{
		if (_requests == null || _requests.size() == 0) {
			return -1;
		}
		
		// Check to see if all call times are greater than -1
		Iterator<HttpRequestRunnable> it = _requests.iterator();
		HttpRequestRunnable call = null;
		long maxTime = -1;
		while (it.hasNext())
		{
			 call = it.next();
			if (call.getTimeToFirstByte() < 0) {
				return -1;
			} else if (call.getTimeToFirstByte() > maxTime) {
				maxTime = call.getTimeToFirstByte();
			}				
		}		
		
		// Batch time is actually the time 
		return maxTime;
	}
	
	public int getCheckCount()
	{
		return _numRequests;
	}
	
	private void captureCallTimes ()
	{
		if (_requests == null || _requests.size() == 0) {
			return;
		}

		if (WorkloadMgr.isCapturingMetrics() == CaptureMetrics.OFF) {
			return;
		}
		
		boolean bBatchAdded = false;
		int numErrors = 0;
		for (int i = 0; i < _requests.size(); i++)
		{
			HttpRequestRunnable request = _requests.get(i);
			
			if (request.getException() != null) {
				log.error("Exception occurred with HttpRequest - " + request.getException().toString());
				numErrors++;
				continue;
			}
			
			long timeToFirstByte = request.getTimeToFirstByte();
			
			log.info("Http Request #: " + i + ", Time To First Byte (ms): " + timeToFirstByte);
									
			if (timeToFirstByte > _threshold)
			{					
				log.error(String.format("Http Request Excedes Threshold - Request #: %1$d, Threshold=%2$d,  Call Time = %3$d, Difference=%4$d (ms)", i,  _threshold, timeToFirstByte, (timeToFirstByte - _threshold)));
				
				// Only add the batch once if a single message excedes the threshold.  This is different behavior from the other types of messages
				// b/c we are only measuring them in batch time (time to update all messages).
				if (bBatchAdded == false)
				{
					bBatchAdded = true;					
					WorkloadMgr.batchFailed(this);
				}
			}
			
			/* We capture each individual call time */
			if (WorkloadMgr.isCapturingMetrics() == CaptureMetrics.ALL)
			{		
				// Create workload batch for all messages combined				
				// Add total time to batch start time to calculate time for the whole batch
				try {
					createWorkloadBatch(_workloadId, _batchCount, _name + ", Request #: " + i, _threshold,  request.getStartTime(), request.getEndTime(), new Timestamp(request.getEndTime()));
				}
				catch (Exception e)
				{
					log.error("Error occurred saving Workload Batch for individual http request.", e);
				}
			}		
		}
		
		log.info(String.format("There %1$d http requests that returned with errors.", numErrors));
	}
	
	public class HttpRequestRunnable implements Runnable
	{
		private int _requestNum = -1;
		private String _url = null;
		private long _startTime = -1;
		private long _endTime = -1;
		Log _log = null;
		private Exception _exception = null;
		
		public HttpRequestRunnable(int requestNum, Log log, String url)
		{
			_requestNum = requestNum;
			_url = url;
			_log = log;			
		}

		public void run() 
		{
			HttpURLConnection connection = null;
			InputStream inputStream = null;
			
			try {
				URL url = new URL(_url);

				// NOTE:  We are not including the time to open connection
				//connection = (HttpURLConnection)url.openConnection();				
				//inputStream = connection.getInputStream();
				
				_startTime = System.currentTimeMillis();
				
				connection = (HttpURLConnection)url.openConnection();
				
				connection.setReadTimeout(0);
				connection.setConnectTimeout(0);
				
				inputStream = connection.getInputStream();				
				
				// Output time to first byte
				inputStream.read();
				
				// Capture time to first byte
				_endTime = System.currentTimeMillis();
				
				log.info(String.format("Request # %1$d - Time to first byte (ms): %2$d",  
										_requestNum, getTimeToFirstByte()));
				
				// Read all bytes
				byte[] ba = new byte[2048];
				int totalCount = 0;
				int count;
								
				while ((count = inputStream.read(ba, 0, 2048)) > 0) 
				{
					totalCount += count;
					
					if (log.isDebugEnabled()) {
						log.debug(new String(ba));
					}
				}		
				
				if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
					throw new ProfilerException("Http Request Response Code: " + connection.getResponseCode());
				} else {
					log.info(String.format("Request # %1$d - Response Code: %2$d", 
							_requestNum, connection.getResponseCode()));
				}
													
				_log.info(String.format(
							"Request # %1$d - Time to read %2$d bytes in response for http request (ms): %3$d", 
								_requestNum, totalCount, (System.currentTimeMillis() - _startTime)));							

			}
			catch (Exception e)
			{
				_exception = e;
				
				_log.error("Error invoking http request.", e);		
				
			}
			finally {
				if (inputStream != null)
				{
					try 
					{				
						inputStream.close();
					}
					catch (Exception e)
					{
						_log.error("Error closing input stream - " + e.toString());
					}
				}
								
				if (connection != null) {
					connection.disconnect();
				}
			}
		}
		
		public long getTimeToFirstByte ()
		{
			if (_startTime < 0 || _endTime < 0) {
				return -1;
			}
			
			return _endTime - _startTime;
		}
		
		public long getStartTime ()
		{
			return  _startTime;
		}
		
		public long getEndTime ()
		{
			return  _endTime;
		}				
		
		public Exception getException()
		{
			return _exception;
		}
	}
	
}