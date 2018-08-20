package org.groundwork.foundation.profiling.messages;

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

public class WebServiceCall extends WorkloadMessage 
{   
	private final String NODE_PARAMETER = "parameter";
	
	private final String MSG_TOTAL_TIME = "Total Time For All Concurrent WS Calls";
	
	// Number concurrent web services call to perform
	private int _numConcurrent = 1;
	
	private String _endPoint = null;
	private String _operation = null;
	private List<Parameter> _parameters = null;
	
	private List<WSCallRunnable>_calls = null;
	
	// Log
	protected static Log log = LogFactory.getLog(WebServiceCall.class);
	
	protected WebServiceCall (int workloadId,
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
	
	public WebServiceCall (Node messageNode, NamedNodeMap attributes) throws ProfilerException
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
        Node node = attributes.getNamedItem("endpoint");
        if (node == null)
        {
        	throw new ProfilerException("endpoint attribute is missing from message.");
        }
        
        _endPoint = node.getNodeValue();		
        
        node = attributes.getNamedItem("operation");
        if (node == null)
        {
        	throw new ProfilerException("operation attribute is missing from message.");
        }
        
        _operation = node.getNodeValue();		
        
        node = attributes.getNamedItem("numConcurrent");
        if (node == null)
        {
        	throw new ProfilerException("numConcurrent attribute is missing from message.");
        }
        
        String value = node.getNodeValue();        
        try {
        	_numConcurrent = Integer.parseInt(value);
        }
        catch (Exception e)
        {
        	log.warn("Invalid numConcurrent value.  Should be integer value > 0.  Defaulting to 1");
        }   	       
        
        // Parse Parameters
        _parameters = parseParameters(messageNode);
	}	
	
	private WebServiceCall(int workloadId, int batchCount,
			String name, long threshold, MessageSocketInfo messageSocketInfo,
			Connection dbProfilerConnection, Connection dbSourceConnection,
			long deltaTime, int numConcurrent, String endPoint, String operation, List<Parameter> parameters)
	{
		super(workloadId, batchCount, 0, name, threshold, messageSocketInfo,
				dbProfilerConnection, dbSourceConnection, deltaTime);

		_numConcurrent = numConcurrent;
		_endPoint = endPoint;
		_operation = operation;
		_parameters = parameters;
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
	
	private void registerTypeMappings (Call call)
	{				
        java.lang.Class cls;
        javax.xml.namespace.QName qName;        
        java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
        java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
        java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
        java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
        java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
        java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
        java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
        java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
        java.lang.Class simplelistsf = org.apache.axis.encoding.ser.SimpleListSerializerFactory.class;
        java.lang.Class simplelistdf = org.apache.axis.encoding.ser.SimpleListDeserializerFactory.class;
        
        qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "WSFoundationCollection");
        cls = WSFoundationCollection.class;
        call.registerTypeMapping(cls, qName, beansf, beandf, false);
        
        qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "WSFoundationException");        
        cls = org.groundwork.foundation.ws.model.impl.WSFoundationException.class;
        call.registerTypeMapping(cls, qName, beansf, beandf, false);        
        
        /*
        qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "SortCriteria");
        cls = org.groundwork.foundation.ws.model.impl.SortCriteria.class;
        call.registerTypeMapping(cls, qName, beansf, beandf, false);

        qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "BooleanProperty");
        cls = org.groundwork.foundation.ws.model.impl.BooleanProperty.class;
        call.registerTypeMapping(cls, qName, beansf, beandf, false);
        
        qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "CheckType");
        cls = CheckType.class;
        call.registerTypeMapping(cls, qName, beansf, beandf, false);

        qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "Component");
        cls =Component.class;
        call.registerTypeMapping(cls, qName, beansf, beandf, false);
        
        qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "DateProperty");
        cls = DateProperty.class;
        call.registerTypeMapping(cls, qName, beansf, beandf, false);
        
        qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "Device");
        cls = Device.class;
        call.registerTypeMapping(cls, qName, beansf, beandf, false);
        
        qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "DoubleProperty");
        cls = DoubleProperty.class;
        call.registerTypeMapping(cls, qName, beansf, beandf, false);
        
        qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "EventQueryType");        
        cls = org.groundwork.foundation.ws.model.impl.EventQueryType.class;
        call.registerTypeMapping(cls, qName, enumsf, enumdf, false);
        
        qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "HostGroupQueryType");        
        cls = org.groundwork.foundation.ws.model.impl.HostGroupQueryType.class;
        call.registerTypeMapping(cls, qName, enumsf, enumdf, false);
        
        qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "ExceptionType");
        cls = ExceptionType.class;
        call.registerTypeMapping(cls, qName, enumsf, enumdf, false);

        qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "Host");
        cls = Host.class;
        call.registerTypeMapping(cls, qName, beansf, beandf, false);

        qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "HostGroup");
        cls = HostGroup.class;
        call.registerTypeMapping(cls, qName, beansf, beandf, false);

        qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "HostStatus");
        cls = HostStatus.class;
        call.registerTypeMapping(cls, qName, beansf, beandf, false);

        qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "IntegerProperty");
        cls = IntegerProperty.class;
        call.registerTypeMapping(cls, qName, beansf, beandf, false);
        
        qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "LogMessage");
        cls = org.groundwork.foundation.ws.model.impl.LogMessage.class;
        call.registerTypeMapping(cls, qName, beansf, beandf, false);
        
        qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "LongProperty");        
        cls = LongProperty.class;
        call.registerTypeMapping(cls, qName, beansf, beandf, false);
        
        qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "MonitorStatus");
        cls = MonitorStatus.class;
        call.registerTypeMapping(cls, qName, beansf, beandf, false);

        qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "OperationStatus");
        cls = OperationStatus.class;
        call.registerTypeMapping(cls, qName, beansf, beandf, false);

        qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "Priority");
        cls = Priority.class;
        call.registerTypeMapping(cls, qName, beansf, beandf, false);

        qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "PropertyTypeBinding");
        cls = PropertyTypeBinding.class;
        call.registerTypeMapping(cls, qName, beansf, beandf, false);

        qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "ServiceStatus");
        cls = ServiceStatus.class;
        call.registerTypeMapping(cls, qName, beansf, beandf, false);

        qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "Severity");
        cls = Severity.class;
        call.registerTypeMapping(cls, qName, beansf, beandf, false);

        qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "SortCriteria");
        cls = org.groundwork.foundation.ws.model.impl.SortCriteria.class;
        call.registerTypeMapping(cls, qName, beansf, beandf, false);

        qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "StateType");
        cls =StateType.class;
        call.registerTypeMapping(cls, qName, beansf, beandf, false);

        qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "StringProperty");
        cls = StringProperty.class;
        call.registerTypeMapping(cls, qName, beansf, beandf, false);;

        qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "TimeProperty");        
        cls = TimeProperty.class;
        call.registerTypeMapping(cls, qName, beansf, beandf, false);

        qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "TypeRule");
        cls = TypeRule.class;
        call.registerTypeMapping(cls, qName, beansf, beandf, false);

        qName = new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "StatisticProperty");
        cls = StatisticProperty.class;
        call.registerTypeMapping(cls, qName, beansf, beandf, false);
*/                  		
	}
	
	public void run() 
	{
		try 
		{			
			// Extract SOAP Action URI from endpoint
			String uri = _endPoint.substring(_endPoint.indexOf("\\") + 1);
			
			Service service = new Service();
			service.setTypeMappingVersion("1.2");
			
			Parameter param = null;
			Object[] paramValues = new Object[_parameters.size()];
			
			// Build parameter array
			for (int i = 0; i < _parameters.size(); i++)
			{
				param = _parameters.get(i);
									
				paramValues[i] = param.getValue();				
			}
			
			// Build Call Array
			Call call = null;			
			Call[] callArray = new Call[_numConcurrent];			
			for (int i = 0; i < _numConcurrent; i++)
			{
				call = (Call)service.createCall();
	   			
				registerTypeMappings(call);
				
				call.setTargetEndpointAddress( new java.net.URL(_endPoint) );
				call.setOperationName(new QName("urn:fws", _operation));			
				call.setUseSOAPAction(true);
				call.setSOAPActionURI(uri);
				call.setEncodingStyle(null);
				call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
				call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
				call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);		
				
				for (int j = 0; j < _parameters.size(); j++)
				{
					param = _parameters.get(j);
					
					call.addParameter(param.getName(),
							  org.apache.axis.Constants.XSD_STRING,
							  javax.xml.rpc.ParameterMode.IN);
			  
					call.setReturnType( new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "WSFoundationCollection"), WSFoundationCollection.class);					
				}
								
				callArray[i] = call;			
			}
											
			// Create Thread Pool
			ThreadPoolExecutor executor = new ThreadPoolExecutor(_numConcurrent, 
					_numConcurrent,
					5, 
					TimeUnit.SECONDS,
					new ArrayBlockingQueue<Runnable>(1));									
			
			// We are starting batch time after setup is complete
			// For now we are recording each individual call which is different from the Collector messages
			// For the collector messages we capture the batch time
			// long batchStartTime = System.currentTimeMillis();
						
			log.info(_name + " WS Methods being called concurrently - Num Calls=" + _numConcurrent);
			
			// Create Array of Runnables which call the web method
			_calls = new ArrayList<WSCallRunnable>(_numConcurrent);									
			WSCallRunnable runnable = null;
			for (int i = 0; i < _numConcurrent; i++)
			{
				runnable = new WSCallRunnable(log, callArray[i], paramValues);				
				_calls.add(runnable);
				
				// Run on separate thread
				if (log.isDebugEnabled()) {
					log.debug("Executing web method - " + _operation);
				}
				
				executor.execute(runnable);
			}			
			
			long startUpdateCheck = System.currentTimeMillis();
			long timeWaitingForUpdate = 0;
			
			// Wait for all calls to complete			
			while (isUpdateComplete() == false) {
				
			//	if (log.isDebugEnabled())
					log.info("Checking WS Call Batch Completion - Message: " + _name + 
							", WS Operation: " + _operation +
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
			log.error("Error occurred running Workload WebServiceCall Message.", e);
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
		return new WebServiceCall(workloadId, batchCount,
				_name, _threshold, messageSocketInfo, dbProfilerConnection, dbSourceConnection,
				deltaTime, _numConcurrent, _endPoint, _operation, _parameters);
	}

	public boolean isUpdateComplete() throws ProfilerException 
	{
		if (_calls == null || _calls.size() == 0) {
			return true;
		}
		
		// Check to see if all call times are greater than -1
		Iterator<WSCallRunnable> it = _calls.iterator();
		while (it.hasNext())
		{
			if (it.next().getCallDuration() < 0) {
				return false;
			}
		}
			
		return true;
	}	
	
	public long getMaxCallTime ()
	{
		if (_calls == null || _calls.size() == 0) {
			return -1;
		}
		
		// Check to see if all call times are greater than -1
		Iterator<WSCallRunnable> it = _calls.iterator();
		WSCallRunnable call = null;
		long maxTime = -1;
		while (it.hasNext())
		{
			 call = it.next();
			if (call.getCallDuration() < 0) {
				return -1;
			} else if (call.getCallDuration() > maxTime) {
				maxTime = call.getCallDuration();
			}				
		}		
		
		// Batch time is actually the time 
		return maxTime;
	}
	
	public int getCheckCount()
	{
		return _numConcurrent;
	}
	
	private void captureCallTimes ()
	{
		if (_calls == null || _calls.size() == 0) {
			return;
		}

		if (WorkloadMgr.isCapturingMetrics() == CaptureMetrics.OFF) {
			return;
		}
		
		boolean bBatchAdded = false;
		for (int i = 0; i < _calls.size(); i++)
		{
			WSCallRunnable call = _calls.get(i);
			
			long callDuration = call.getCallDuration();
			
			log.info("WS Call Duration:  Call #: " + i + ", Duration: " + callDuration);
									
			if (callDuration > _threshold)
			{					
				log.error(String.format("WS Call Excedes Threshold - Call #: %1$d, Threshold=%2$d,  Call Time = %3$d, Difference=%4$d (ms)", i,  _threshold, callDuration, (callDuration - _threshold)));
				
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
					createWorkloadBatch(_workloadId, _batchCount, _name + ", Call #: " + i, _threshold,  call.getStartTime(), call.getEndTime(), new Timestamp(call.getEndTime()));
				}
				catch (Exception e)
				{
					log.error("Error occurred saving Workload Batch for individual ws calls.", e);
				}
			}		
		}
	}
	
	private List<Parameter> parseParameters (Node messageNode) throws ProfilerException
	{
		List<Parameter>parameters = new ArrayList<Parameter>(3);
        Node childNode = null;
        Node attributeNode = null;
        NamedNodeMap parameterAttributes = null;
        String paramName = null;
        String paramValue = null;
        Parameter param = null;
        
        NodeList childNodes = messageNode.getChildNodes();        
		for (int i = 0; i < childNodes.getLength(); i++) {
			childNode = childNodes.item(i);
			
			if (NODE_PARAMETER.equalsIgnoreCase(childNode.getNodeName())) 
			{
				parameterAttributes = childNode.getAttributes();
				
				attributeNode = parameterAttributes.getNamedItem("name");
		        if (attributeNode == null)
		        {
		        	throw new ProfilerException("name attribute is missing from parameter.");
		        }
		        
		        paramName = attributeNode.getNodeValue();

				attributeNode = parameterAttributes.getNamedItem("value");
		        if (attributeNode == null)
		        {
		        	throw new ProfilerException("name attribute is missing from parameter.");
		        }		   
		        
		        paramValue = attributeNode.getNodeValue();
		        
		        param = new Parameter(paramName, paramValue);		        
		        parameters.add(param);
		        
		        if (log.isDebugEnabled()) {
					log.debug("WSCall Parameter Added: " + param.toString());
				}
			}
		}		
		
		return parameters;
	}
	
	public class Parameter 
	{
		private String _name = null;
		private String _value = null;
		
		public Parameter (String name, String value)
		{
			_name = name;
			_value = value;
		}
		
		public String getName ()
		{
			return _name;			
		}
		
		public String getValue ()
		{
			return _value;
		}
		
		public String toString()
		{
			StringBuilder sb = new StringBuilder(32);
			sb.append("Name: ");
			sb.append(_name);
			sb.append(", Value: ");
			sb.append(_value);
			
			return sb.toString();
		}
	}
	
	public class WSCallRunnable implements Runnable
	{
		private Call _call = null;
		private Object[] _params = null;
		private long _startTime = -1;
		private long _endTime = -1;
		Log _log = null;
		
		public WSCallRunnable(Log log, Call call, Object[] params)
		{
			_call = call;
			_params = params;
			_log = log;			
		}

		public void run() 
		{
			try {								
				_startTime = System.currentTimeMillis();
				_call.invoke(_params);								
				_endTime =  System.currentTimeMillis();
				
				if (log.isDebugEnabled()) {
					_log.debug("Time to execute WS Method (ms): " + getCallDuration());
				}
			}
			catch (Exception e)
			{
				_log.error("Error invoking web service call.", e);
			}
		}
		
		public long getCallDuration ()
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
	}
	
}