/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2006  GroundWork Open Source Solutions info@groundworkopensource.com

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
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.profiling.exceptions.ConfigFileParseException;
import org.groundwork.foundation.profiling.exceptions.InvalidConfigFileException;
import org.groundwork.foundation.profiling.exceptions.ProfilerException;
import org.groundwork.foundation.profiling.exceptions.UnknownWorkloadMessage;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class WorkloadMgr 
{	
	public enum CaptureMetrics	
	{
		OFF,
		LOG,
		ALL
	}
	
	// SQL Statements
	private static final String PSTMT_INSERT_SESSION = "INSERT INTO WorkloadSessions (Name, StartTime, LStartTime) VALUES(?, ?, ?)";
	private static final String PSTMT_UPDATE_SESSION_END_TIME = "UPDATE WorkloadSessions SET EndTime = ?, LEndTime = ? WHERE SessionID = ?";

	private static final String STMT_GET_CURRENT_TIME = "SELECT Now()";

	// Configuration environment variable
	private static final String KEY_CONFIG_FILE = "profilerConfig";

	private static final String DEFAULT_CONFIG_FILE = "foundation-profiler.xml";

	private static final String SESSION_NAME_FORMAT = "Session - %1$s";
	
	protected static Log log = LogFactory.getLog(WorkloadMgr.class);
	
	// Static member that get updated with all batches that have exceeded threshold and timed out.  Used for acceptence testing
	// Note:  This is a static member therefore multiple WorkloadMgr should not be instantiated
	private static ArrayList<IWorkloadMessage>_failedBatches = new ArrayList<IWorkloadMessage>(5);
	
	// Single instance - Multiple instances are not allowed.
	private static WorkloadMgr _instance = null;

	private String _configFileName = null;
	
	private String _sessionName = null;
	
	// Flag indicating whether we should capture metrics or its just a QA Acceptence test and all we do is output batches that don't meet threshold requirements
	private static CaptureMetrics _captureMetrics = CaptureMetrics.ALL;

	private DBConnectionInfo _profilerDB = null;

	private DBConnectionInfo _sourceDB = null;

	private MessageSocketInfo _messageSocket = null;

	private List<Workload> _workloads = null;

	// Private constructor - Client should use WorkloadMgr.startWorkloads since we only allow one instance to be created
	private WorkloadMgr(String configFileName, String sessionName, Object tg)
			throws InvalidConfigFileException, ConfigFileParseException,
			ProfilerException
	{
		// If no configuration file is defined we look for a system environment
		// variable
		if (configFileName == null || configFileName.length() == 0) {
			configFileName = System.getProperty(KEY_CONFIG_FILE,
					DEFAULT_CONFIG_FILE);
		}

		_configFileName = configFileName;
		_sessionName = sessionName;

		loadConfiguration(_configFileName);
		log.debug("WorkloadMgr:_messageSocket>="+_messageSocket);
		if (_messageSocket != null )
		{   
			String error = "ok";
			String server = _messageSocket.getServer();
			int port = _messageSocket.getPort();
			try
			{
				Socket echoSocket = new Socket( server, port);
			}
			catch (UnknownHostException e){ error = "unknownHost";}
			catch (IOException  e){ error = "ioError";}
			catch (SecurityException e){ error = "securityException";}
			catch (Exception e){ error = "unknownException";}
			if (error.equals("ok")) {
				runWorkloads(tg);
			} else {
				throw new ProfilerException("Foundation Port is not listening (Foundation is down).");
			}
		}
	}
	
	/**
	 * Starts workloads by first parsing configuration file and then running workloads.  There is only
	 * one instance of the WorkloadMgr running at given anytime.
	 * @param configFileName
	 * @param sessionName
	 * @return
	 * @throws InvalidConfigFileException
	 * @throws ConfigFileParseException
	 * @throws ProfilerException
	 */
	public static WorkloadMgr startWorkloads(String configFileName, String sessionName, Object tg)
	throws InvalidConfigFileException, ConfigFileParseException, ProfilerException
	{
		if (_instance == null)
		{
			return new WorkloadMgr(configFileName, sessionName, tg);
		}
		
		throw new ProfilerException("WorkloadMgr already running.");
	}

	/**
	 * Adds a message to the failed batch list
	 * @param message
	 */
	public static void batchFailed(IWorkloadMessage message)
	{
		if (message == null)
		{
			throw new IllegalArgumentException("batchFailed - Invalid null IWorkloadMessage parameter.");
		}
		
		// Add to failed batches
		_failedBatches.add(message);
	}
	
	/**
	 * Returns boolean indicating whether metrics are and or should be captured.
	 * @return
	 */
	public static CaptureMetrics isCapturingMetrics ()
	{
		return _captureMetrics;
	}
	
	private DBConnectionInfo parseDatabaseConnection(Document doc,
			String elementName) throws ConfigFileParseException {
		
		if (doc == null) {
			throw new IllegalArgumentException(
					"Invalid null Document parameter.");
		}

		if (elementName == null || elementName.length() == 0) {
			throw new IllegalArgumentException(
					"Invalid null / emtpy element name parameter.");
		}

		NodeList nodeList = doc.getElementsByTagName(elementName);
		if (nodeList == null || nodeList.getLength() < 1) {
			throw new ConfigFileParseException(_configFileName,
					"Missing element, " + elementName);
		}

		if (log.isInfoEnabled() == true) {
			log.info("Parsing database connection - " + elementName);
		}

		Node dbNode = nodeList.item(0);
		NamedNodeMap nodemap = dbNode.getAttributes();

		Node nameNode = nodemap.getNamedItem("driver");
		String driver = nameNode.getNodeValue();

		nameNode = nodemap.getNamedItem("url");
		String url = nameNode.getNodeValue();

		nameNode = nodemap.getNamedItem("login");
		String login = nameNode.getNodeValue();

		nameNode = nodemap.getNamedItem("password");
		String password = nameNode.getNodeValue();

		return new DBConnectionInfo(driver, url, login, password);
	}

	private MessageSocketInfo parseMessageSocket(Document doc,
			String elementName) throws ConfigFileParseException {
		if (doc == null) {
			throw new IllegalArgumentException(
					"Invalid null Document parameter.");
		}

		if (elementName == null || elementName.length() == 0) {
			throw new IllegalArgumentException(
					"Invalid null / emtpy element name parameter.");
		}

		if (log.isInfoEnabled() == true) {
			log.info("Parsing message socket - " + elementName);
		}

		NodeList nodeList = doc.getElementsByTagName(elementName);
		if (nodeList == null || nodeList.getLength() < 1) {
			throw new ConfigFileParseException(_configFileName,
					"Missing element, " + elementName);
		}

		Node socketNode = nodeList.item(0);
		NamedNodeMap nodemap = socketNode.getAttributes();

		Node nameNode = nodemap.getNamedItem("server");
		String server = nameNode.getNodeValue();

		nameNode = nodemap.getNamedItem("port");
		int port = 0;
		try {
			port = Integer.parseInt(nameNode.getNodeValue());
		} catch (NumberFormatException e) {
			throw new ConfigFileParseException(_configFileName,
					"Invalid port specified for " + elementName);
		}

		return new MessageSocketInfo(server, port);
	}

	private IWorkloadMessage parseMessage(Node messageNode)
			throws UnknownWorkloadMessage, ProfilerException {
		if (log.isDebugEnabled()) log.debug("WorkloadMgr.parseMessage(Node messageNode)");
		if (messageNode == null
				|| "message".equalsIgnoreCase(messageNode.getNodeName()) == false) {
			throw new IllegalArgumentException(
					"Invalid null message Node parameter.");
		}

		// Parse type attribute in order to instantiate message
		NamedNodeMap nodemap = messageNode.getAttributes();
		Node typeNode = nodemap.getNamedItem("type");
		String type = typeNode.getNodeValue();

		if (type == null || type.length() == 0) {
			throw new UnknownWorkloadMessage("null / empty");
		}

		IWorkloadMessage message = null;
		try {
			Class messageClass = Class.forName(type);
			Class[] parameterTypes = new Class[] { Node.class, NamedNodeMap.class };
			// Use NamedNodeMap Constructor
			Constructor ctor = messageClass.getConstructor(parameterTypes);
			message = (IWorkloadMessage) ctor.newInstance(new Object[] {messageNode, nodemap });
		} catch (Exception e) { log.debug(e);
			//throw new UnknownWorkloadMessage(type);
		}

		return message;
	}

	private List<Workload> parseWorkloads(Document doc, String elementName)
			throws ConfigFileParseException {
		
		if (doc == null) {
			throw new IllegalArgumentException(
					"Invalid null Document parameter.");
		}

		if (elementName == null || elementName.length() == 0) {
			throw new IllegalArgumentException(
					"Invalid null / emtpy element name parameter.");
		}

		if (log.isInfoEnabled() == true) {
			log.info("Parsing workloads - " + elementName);
		}

		NodeList nodeList = doc.getElementsByTagName(elementName);
		if (nodeList == null || nodeList.getLength() < 1) {
			throw new ConfigFileParseException(_configFileName,
					"Missing element, " + elementName);
		}

		Node workloadsNode = nodeList.item(0);

		NodeList childNodes = workloadsNode.getChildNodes();
		int numChildNodes = childNodes.getLength();

		List<Workload> workloadList = new ArrayList<Workload>(numChildNodes);

		Node childNode = null;
		NamedNodeMap nodemap = null;
		Node attrNode = null;
		String name = null;
		String numBatches = null;
		String interval = null;
		String quantity = null;
		String distribution = null;
		String enabled = null;
		Workload workload = null;
		NodeList workloadChildren = null;

		// Iterate through workloads
		for (int i = 0; i < numChildNodes; i++) {
			childNode = childNodes.item(i);

			// Only process workload children
			if ("workload".equalsIgnoreCase(childNode.getNodeName())) {
				nodemap = childNode.getAttributes();

				attrNode = nodemap.getNamedItem("name");
				if (attrNode == null) {
					throw new ConfigFileParseException(_configFileName,
							"Missing workload attribute, name");
				}
				name = attrNode.getNodeValue();

				attrNode = nodemap.getNamedItem("numBatches");
				if (attrNode == null) {
					throw new ConfigFileParseException(_configFileName,
							"Missing workload attribute, numBatches");
				}
				numBatches = attrNode.getNodeValue();

				attrNode = nodemap.getNamedItem("interval");
				if (attrNode == null) {
					throw new ConfigFileParseException(_configFileName,
							"Missing workload attribute, interval");
				}
				interval = attrNode.getNodeValue();

				attrNode = nodemap.getNamedItem("quantity");
				if (attrNode == null) {
					throw new ConfigFileParseException(_configFileName,
							"Missing workload attribute, quantity");
				}
				quantity = attrNode.getNodeValue();

				attrNode = nodemap.getNamedItem("distribution");
				if (attrNode == null) {
					throw new ConfigFileParseException(_configFileName,
							"Missing workload attribute, distribution");
				}
				distribution = attrNode.getNodeValue();
	
				attrNode = nodemap.getNamedItem("enabled");
				if (attrNode == null) {
					throw new ConfigFileParseException(_configFileName,
							"Missing workload attribute, enabled");
				}
				enabled = attrNode.getNodeValue();

				try {
					Integer.parseInt(numBatches);															
					Integer.parseInt(interval);
					Integer.parseInt(quantity);
					MessageDistribution.fromString(distribution.toLowerCase());
					Boolean.parseBoolean(enabled);
					workload = new Workload(name, Integer.parseInt(numBatches),
							Integer.parseInt(interval), 
							Integer.parseInt(quantity), 
							MessageDistribution.fromString(distribution.toLowerCase()), 
							Boolean.parseBoolean(enabled));
				} catch (Exception e) {
					throw new ConfigFileParseException(_configFileName,
							"Error parsing workloads.", e);
				}
				// Parse messages
				workloadChildren = childNode.getChildNodes();
				for (int j = 0; j < workloadChildren.getLength(); j++) {
					Node workloadChildNode = workloadChildren.item(j);
					if ("messages".equalsIgnoreCase(workloadChildNode
							.getNodeName())) {
						NodeList messageNodes = workloadChildNode.getChildNodes();
						for (int k = 0; k < messageNodes.getLength(); k++) {
							Node messagesChildNode = messageNodes.item(k);
							if ("message".equalsIgnoreCase(messagesChildNode
									.getNodeName())) {
								try {
									IWorkloadMessage message = parseMessage(messagesChildNode);
									// Add message to workload
									workload.getMessages().add(message);
								} catch (Exception e) {
									throw new ConfigFileParseException(
											"Error occurred parsing workload messages.",
											e);
								}
							}
						}
					}
				}
				// Add Workload
				workloadList.add(workload);
			}
		}

		return workloadList;
	}

	private void loadConfiguration(String configFileName)
			throws InvalidConfigFileException, ConfigFileParseException {
		if (configFileName == null || configFileName.length() == 0) {
			throw new IllegalArgumentException(
					"Invalid null / empty config file name parameter.");
		}

		InputStream inStream = null;

		try {
			// Load Meta Data for the specfic method
			DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();

			inStream = new FileInputStream(configFileName);
			if (inStream == null) {
				throw new InvalidConfigFileException(configFileName);
			}

			Document doc = docBuilder.parse(inStream);

			// Parse foundation-profiler root node first			
			NodeList nodeList = doc.getElementsByTagName("foundation-profiler");
			if (nodeList == null || nodeList.getLength() != 1) {
				throw new ConfigFileParseException(_configFileName,
						"Missing element or too many <foundation-profiler> elements");
			}

			Node profilerNode = nodeList.item(0);
			NamedNodeMap nodemap = profilerNode.getAttributes();
			Node attrNode = nodemap.getNamedItem("captureMetrics");
			if (attrNode != null)
			{
				String captureMetrics = null;
				try {
					captureMetrics = attrNode.getNodeValue();
					_captureMetrics = Enum.valueOf(WorkloadMgr.CaptureMetrics.class, captureMetrics);	
				}
				catch (Exception e)
				{
					log.warn(String.format("Invalid captureMetrics attribute value - %1$s.  Default to true.", captureMetrics));
				}
			}
			// Parse Database Connections - if necessary
			if (_captureMetrics == CaptureMetrics.ALL) {
				this._profilerDB = parseDatabaseConnection(doc, "profilerDB");
			}

			this._sourceDB = parseDatabaseConnection(doc, "foundationDB");

			// Parse Message Socket
			this._messageSocket = parseMessageSocket(doc, "messageSocket");

			// Parse Workloads
			this._workloads = parseWorkloads(doc, "workloads");
			/*
			if (log.isInfoEnabled() == true) {

				log.info("Profiler Database: " + _profilerDB);
				log.info("Foundation Database: " + _sourceDB);
				log.info("Message Socket: " + _messageSocket);
				log.info("Workloads: " + _workloads);
			}
			*/
		} catch (InvalidConfigFileException e) {
			throw e;
		} catch (Exception e) {
			throw new ConfigFileParseException(configFileName, e);
		} finally {
			if (inStream != null) {
				try {
					inStream.close();
				} catch (Exception e) {
					log.warn(e);
				}
			}
		}
	}

	private void runWorkloads(Object tg) throws ProfilerException 
	{

		if (_workloads == null || _workloads.size() == 0)
		{
			log.error("No Workloads Defined.");
			return;
		}

		// Loop through workloads and make sure there is an enabled workload
		Iterator<Workload> it = _workloads.iterator();
		Workload workload = null;
		boolean bHasEnabledWorkload = false;

		while (it.hasNext()) {
			workload = it.next();
			if (workload.getEnabled() == true) {
				bHasEnabledWorkload = true;
				break;
			}
		}

		if (bHasEnabledWorkload == false)
		{
			log.error("No Workloads Enabled.");
			return;			
		}

		//Query Source DB time in order to synch profiler and foundation clocks
		// specifically for log message reported date fields
		long deltaTime = 0;
		try {
			deltaTime = getSourceDBDelta();
			log.info("System time delta (ms) = " + deltaTime);
			
		} catch (Exception e) {
			log.error("Unable to retrieve source db system time.  Defaulting to zero.", e);
		}

		// Create Workload Session
		int sessionId = 0;
		try {
			long now = System.currentTimeMillis();

			// If session name is not present, we generate it with the current date/time
			if (_sessionName == null || _sessionName.length() == 0)
			{
				_sessionName = String.format(SESSION_NAME_FORMAT, new Date(now).toString());
			}		
			sessionId = createWorkloadSession(_sessionName, System.currentTimeMillis());
			if (log.isInfoEnabled() == true) {
				log.info("Session Created: " + _sessionName + ", ID: " + sessionId);
			}
		} catch (Exception e) {
			log.error("Error creating workload session.  Exiting workloads.", e);
			return;
		}

		// Run each workload on its on thread
		it = _workloads.iterator();		
		// First Initialize each workload		
		while (it.hasNext()) {
			workload = it.next();
			if (workload.getEnabled() == true) {
				workload.initialize(sessionId, _messageSocket, _profilerDB,
						_sourceDB, deltaTime, tg);
			}
		}

		// Loop through each workload and start them on a separate thread
		it = _workloads.iterator();
		while (it.hasNext()) {
			workload = it.next();
			if (workload.getEnabled() == true) {
				Thread thread = new Thread(workload);
				thread.run();
			}
		}
		// Monitor workloads to
		while (true) {
			try {
				// Let workloads go
				Thread.sleep(2000);
				// Check each workload - Once each workload is completed then we
				// quit
				boolean bComplete = true;
				for (int i = 0; i < _workloads.size(); i++) {
					workload = _workloads.get(i);
					if (workload.getEnabled() == true && workload.getComplete() == false) {
						bComplete = false;
					} else { // workload completed
						
						if (workload.getEnabled() == true) {
							workload.uninitialize();
						}

						// remove from workload list since they are done
						_workloads.remove(i);
						i--;
					}
				}

				if (bComplete == true) 
				{
					updateWorkloadSessionEndTime(sessionId, System.currentTimeMillis());
					
					// Output Failed (threshold exceeded) Workload Message Batches
					outputResults();
					break;
				}
			} catch (Exception e) {
				log.error(e);
			}
		}
	}
	
	private void outputResults ()
	{
		if (_failedBatches == null || _failedBatches.size() == 0)
		{
			log.info("SUCCESSFUL WORKLOADS - All batches met threshold requirements");
		}
		else 
		{
			StringBuilder sb = new StringBuilder(_failedBatches.size() * 64);
			
			sb.append("WORKLOAD BATCHES WHICH EXCEDE THRESHOLD:\n");			
			
			Iterator<IWorkloadMessage> iterator = _failedBatches.iterator();
			while (iterator.hasNext())
			{
				IWorkloadMessage msg = iterator.next();
				
				sb.append(msg.toString());
				sb.append("\n");				
			}
			
			log.info(sb.toString());
		}
	}

	private int createWorkloadSession(String sessionName, long startTime) throws SQLException, ProfilerException 
	{
		// If we are not capturing metrics then no need to create workload session or return an ID
		if (_captureMetrics != CaptureMetrics.ALL)
		{
			return -1;
		}
		
		Connection dbConn = _profilerDB.createConnection();

		PreparedStatement pstmt = dbConn.prepareStatement(PSTMT_INSERT_SESSION);
		pstmt.setString(1, sessionName);		
		pstmt.setTimestamp(2, new Timestamp(startTime));
		pstmt.setLong(3, startTime);
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

	private void updateWorkloadSessionEndTime(int sessionId, long endTime)
			throws SQLException, ProfilerException 
	{
		// If we are not capturing metrics then no need to update session end time
		if (_captureMetrics != CaptureMetrics.ALL)
		{
			return;
		}
		
		Connection dbConn = _profilerDB.createConnection();

		PreparedStatement pstmt = dbConn.prepareStatement(PSTMT_UPDATE_SESSION_END_TIME);
		pstmt.setTimestamp(1, new Timestamp(endTime));
		pstmt.setLong(2, endTime);
		pstmt.setInt(3, sessionId);

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

	/**
	 * Returns the delta between the source db system time and the profiler's
	 * system time. The value returned should be subtracted for the profiler's
	 * system time to get the time for the source database.
	 * 
	 * @return
	 * @throws SQLException
	 * @throws ProfilerException
	 */
	private long getSourceDBDelta() throws SQLException, ProfilerException
	{
		// We are not capturing metrics so no need to get delta
		if (_captureMetrics == CaptureMetrics.OFF) {
			return 0;
		}
		
		// If the both databases servers are on the same server the delta is zero		
		if (_sourceDB != null && _profilerDB != null &&
			_sourceDB.getServer().equalsIgnoreCase(_profilerDB.getServer())) {
			return 0;
		}
		
		Connection dbConn = _sourceDB.createConnection();

		Statement stmt = dbConn.createStatement();

		ResultSet rs = stmt.executeQuery(STMT_GET_CURRENT_TIME);
		
		// We get system time here to get as close a possible to when the source time was captured.
		long now = System.currentTimeMillis();

		long delta = 0;
		if (rs.next() == true) 
		{
			Timestamp ts = rs.getTimestamp(1);
			
			delta = now - ts.getTime();

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
				log.error("Error closing stmt.", e);
			}
			stmt = null;
		}
		
		return delta;
	}		
}
