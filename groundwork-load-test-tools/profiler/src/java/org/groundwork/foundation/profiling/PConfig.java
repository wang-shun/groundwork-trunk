package org.groundwork.foundation.profiling;

import java.util.*;
import java.io.*;
import java.io.StringWriter;
import java.util.Random;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

//import java.sql.*;
import java.text.SimpleDateFormat;

//import org.groundwork.foundation.profiling.*;
import org.groundwork.foundation.profiling.exceptions.*;
import org.groundwork.foundation.profiling.messages.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PConfig
{
	public PConfig () {}
	private String pdurl = "jdbc:mysql://localhost/GWProfilerDB";
	private String fdurl = "jdbc:mysql://localhost/GWCollageDB";
	private String fdlogin = "root";
	private String fdpassword = "";
	private String pdlogin = "root";
	private String pdpassword = "";
	private String skserver = "localhost";
	private String skport = "4913";
	private String ngserver = "localhost";
	private String ngport = "5667";
	private String xmlConfigDir = "profilersa/xml/";
	private Log log = LogFactory.getLog(this.getClass());
	private Object tg;
	public String workloadType = "name=&numBatches=1&interval=1&quantity=1&distribution=EVEN&distribution=BURST&distribution=RANDOM&enabled=TRUE&enabled=FALSE";


	public String messageType01 = "type=org.groundwork.foundation.profiling.messages.SystemAdminInitMessage&name=SystemAdminInitMessage&numDevices=10&numHosts=10&numServices=20&threshold=5";
	public String messageType02 = "type=org.groundwork.foundation.profiling.messages.SystemAdminToggleHostStatusMessage&name=SystemAdminToggleHostStatusMessage&numHosts=10&monitorUpPercentage=70&threshold=5";
	public String messageType03 = "type=org.groundwork.foundation.profiling.messages.SystemAdminServiceStatusMessage&name=SystemAdminToggleServiceStatusMessage&numHosts=10&numServices=20&monitorUpPercentage=80&threshold=5";
	public String messageType04 = "type=org.groundwork.foundation.profiling.messages.SystemAdminLogMessage&name=SystemAdminLogMessage&numHosts=10&csPercent=30&threshold=5";

	public String messageType05 = "type=org.groundwork.foundation.profiling.messages.EventMessage&name=EventMessage&numDevices=&threshold=5&consolidation=true";
	public String messageType06 = "type=org.groundwork.foundation.profiling.messages.SNMPMessage&name=SNMPMessage&numDevices=10&threshold=5&consolidation=false";
	public String messageType07 = "type=org.groundwork.foundation.profiling.messages.SysLogMessage&name=SysLogMessage&numDevices=10&threshold=5&consolidation=true";
	public String messageType08 = "type=org.groundwork.foundation.profiling.messages.HostCreateMessage&name=HostCreateMessage&numHosts=10&threshold=5&version45=false";
	public String messageType09 = "type=org.groundwork.foundation.profiling.messages.ToggleServiceStatusMessage&name=ToggleServiceStatusMessage&threshold=5&numServices=20";
	public String messageType10 = "type=org.groundwork.foundation.profiling.messages.ToggleHostStatusMessage&name=ToggleHostStatusMessage&threshold=5&numHosts=10";
	public String messageType11 = "type=org.groundwork.foundation.profiling.messages.HttpRequest&name=HttpRequest&url=http://172.28.113.232/monitor/index.php&numRequests=10";

	public String messageType12 = "type=org.groundwork.foundation.profiling.messages.NSCAInitMessage&name=NSCAInitMessage&numHosts=5&numServices=2&hostName=hany&serviceName=sany&ipAddress=300.100.100.&numBatches=10&interval=20&nscaStart=y&percentage=70&threshold=5";
	public String messageType13 = "type=org.groundwork.foundation.profiling.messages.NSCALogMessage&name=NSCALogMessage&numHosts=5&numServices=2&hostName=hany&serviceName=sany&ipAddress=300.100.100.&percentage=70&threshold=5";
	public String messageChoices = "type=NSCAInitMessage&type=NSCALogMessage&type=SystemAdminInitMessage&type=SystemAdminToggleHostStatusMessage&type=SystemAdminServiceStatusMessage&type=SystemAdminLogMessage&type=EventMessage&type=SNMPMessage&type=SysLogMessage&type=HostCreateMessage&type=ToggleServiceStatusMessage&type=ToggleHostStatusMessage&type=HttpRequest";


	public String fP = "fP.captureMetrics=ALL&fP.captureMetrics=LOG&fP.captureMetrics=OFF";
	public String fd = "fd.driver=com.mysql.jdbc.Driver&fd.url="+fdurl+"&fd.login=root&fd.password=";
	public String pd = "pd.driver=com.mysql.jdbc.Driver&pd.url="+pdurl+"&pd.login=root&pd.password=";
	public String sk = "sk.server="+skserver+"&sk.port="+skport;
	public String ng = "ng.server="+ngserver+"&ng.port="+ngport;
	
	public Map<String, Map<String, Map<String,String>>> workloads = new HashMap<String, Map<String, Map<String,String>>>();

	public Map<String,String> messageTypes = new HashMap<String,String> ();


	public PConfig (String pdurl, String pdlogin, String pdpassword, String fdurl, String fdlogin, String fdpassword, String skserver, String skport)
	{
		// ===================================================================
		this.pdurl = pdurl;
		this.pdlogin = pdlogin;
		this.pdpassword = pdpassword;
		this.fdurl = fdurl;
		this.fdlogin = fdlogin;
		this.fdpassword = fdpassword;
		this.skserver = skserver;
		this.skport = skport;
		
		messageTypes.put("EventMessage", messageType05);

		messageTypes.put("HostCreateMessage", messageType08);
		messageTypes.put("HttpRequest", messageType11);
		messageTypes.put("SNMPMessage", messageType06);
		messageTypes.put("SysLogMessage", messageType07);
		messageTypes.put("SystemAdminInitMessage", messageType01);
		messageTypes.put("SystemAdminToggleHostStatusMessage", messageType02);
		messageTypes.put("SystemAdminServiceStatusMessage", messageType03);
		messageTypes.put("SystemAdminLogMessage", messageType04);

		messageTypes.put("ToggleServiceStatusMessage", messageType09);
		messageTypes.put("ToggleHostStatusMessage", messageType10);
		messageTypes.put("NSCAInitMessage", messageType12);
		messageTypes.put("NSCALogMessage", messageType13);
		// ===================================================================

	}

	public PConfig (String pdurl, String pdlogin, String pdpassword, 
						String fdurl, String fdlogin, String fdpassword, 
								String skserver, String skport, String ngserver, String ngport)
	{
		this(pdurl, pdlogin, pdpassword, fdurl, fdlogin, fdpassword, skserver, skport);
		this.ngserver = ngserver;
		this.ngport = ngport;

	}
	
	
	public Connection getConnect (String DB) 
	{
		String url = "jdbc:mysql://localhost/"+DB+"?user=root&password=";
		if (DB.equals("GWCollageDB")) url = fdurl+"?user="+fdlogin+"&password="+fdpassword;
		if (DB.equals("GWProfilerDB")) url = pdurl+"?user="+pdlogin+"&password="+pdpassword;
		Connection conn = null;
		try 
		{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(url);
		}
		catch (ClassNotFoundException ex) {log.error("PConfig.ClassNotFoundException: " + ex.getMessage());}
		catch (InstantiationException ex) {log.error("PConfig.InstantiationException: " + ex.getMessage());}
		catch (IllegalAccessException ex) {log.error("PConfig.IllegalAccessException: " + ex.getMessage());}
		catch (SQLException ex) {log.error("PConfig.SQLException: " + ex.getMessage());}
		return conn;

	}
	public void cleanDB ()
	{
	    Connection conn = getConnect ("GWProfilerDB");
	    if (conn != null)
	    {
		Statement stmt = null;

		try 
		{
			stmt = conn.createStatement();
			if (!stmt.execute("DELETE FROM WorkloadSessions")) {
				log.error("PConfig.cleanDB didn't succeed");
			}
			close (stmt);
			close (conn);
			conn = getConnect ("GWCollageDB");
			stmt = conn.createStatement();
			if (!stmt.execute("DELETE FROM Device")) {
				log.error("PConfig.cleanDB didn't succeed");
			}
		}
		catch (SQLException ex) {log.error("PConfig.SQLException: " + ex.getMessage());}
		finally 
		{
			close (stmt);
			close (conn);
		}
	    }
	}
	
	public String navigationHeading = "<table bgcolor=\"black\" width=\"710\">"+
	"<tr><td align=\"middle\" style=\"background-color:#DC143C;color:#FFFFFF\">"+
	"<a href=\"index.html\" style=\"font-weight:bold;text-decoration:none;color:#FFFFFF\" target=\"_self\">About</a></td>"+
	"<td align=\"middle\" bgcolor=\"#FFD700\">"+
	"<a href=\"index.jsp?cmd=simplesa\" style=\"font-weight:bold;text-decoration:none;\" target=\"_self\">SystemAdmin</a></td>"+
	"<td align=\"middle\" bgcolor=\"#FFA500\">"+
	"<a href=\"helpSystemAdmin.html\" style=\"font-weight:bold;text-decoration:none;\" target=\"_self\">Help SystemAdmin</a></td>"+
	"<td align=\"middle\" bgcolor=\"#FFD700\">"+
	"<a href=\"index.jsp?cmd=newconfig\" style=\"font-weight:bold;text-decoration:none;\" target=\"_self\">Foundation 1.5</a></td>"+
	"<td align=\"middle\" bgcolor=\"#FFA500\">"+
	"<a href=\"helpLegacy.html\" style=\"font-weight:bold;text-decoration:none;\" target=\"_self\">Help Foundation 1.5</a></td>"+
	"<td align=\"middle\" bgcolor=\"#FFD700\">"+
	"<a href=\"index.jsp?cmd=nscaext\" style=\"font-weight:bold;text-decoration:none;\" target=\"_self\">NSCA Extension</a></td>"+
	"<td align=\"middle\" bgcolor=\"#FFA500\">"+
	"<a href=\"helpNSCAext.html\" style=\"font-weight:bold;text-decoration:none;\" target=\"_self\">Help NSCA Extension</a></td>"+
	"</tr></table>";
		
		
	public String getNavigationHeading ()
	{
		return navigationHeading;
	}
	
	public String getRows ()
	{
		String outp = "<div class=\"header1\">"+navigationHeading+
	    "<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><br><br>";
	    
	    Connection conn = getConnect ("GWProfilerDB");
	    java.text.SimpleDateFormat SQL_DATE_FORMAT = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSSS");
	    if (conn != null)
	    {
		Statement stmt = null;
		ResultSet rs = null;
		int prevWkld = -1;
		int currWkld = 1;
		int prevSsn = -1;
		int currSsn = 1;
		try 
		{
    			stmt = conn.createStatement();
    			String theStatement = "select "+
						"t1.WorkloadID as Workload, t3.SessionID as SessionID, t1.MessageName as Message, " +
						"t1.BStartTimeString as MessageStartTime, t1.Latency as Latency," +
							"t1.BEndTimeString as MessageEndTime, t1.TimeRecorded as TimeRecorded, " +
							"t2.Name as WorkloadName, t2.LStartTime as WorkloadStartTime, " +
							"t2.LEndTime as WorkloadEndTime, t3.Name as SessionName, t1.MessageBatchID, t1.WorkloadBatchID, " +
							"t3.LStartTime as SessionStartTime, t3.LEndTime as SessionEndTime, t1.NumberOfChecks as Checks " +
							"from " +
							"MessageBatches t1, Workloads t2, WorkloadSessions t3 "+
							"where " +
							"t3.SessionID = t2.SessionID and t2.WorkloadID = t1.WorkloadID order By " +
							"t1.BatchStartTime";
    			if (stmt.execute(theStatement)) 
    			{
        			rs = stmt.getResultSet();
        			int msgsCount = 1;
        			while (rs.next()) 
        			{

        				if (rs.getRow() == 1)
        				{
        					long startTime = rs.getLong("SessionStartTime");
        					long endTime = rs.getLong("SessionEndTime");
        					long latency = endTime - startTime;
        					outp = outp +   "<tr><td colspan=\"2\">&#160;</td><td  class=\"hdg0\">Start Time</td><td  class=\"hdg0\">End Time</td><td  class=\"hdg0\">Latency</td><td  class=\"hdg0\">Checks</td><td  class=\"hdg0\">Latency/Check</td><td class=\"hdg0\">LogMessageCount</td></tr>";

        				}
        				currSsn = rs.getInt(2);
        				if (prevSsn != currSsn)
        				{
        					long startTime = rs.getLong("SessionStartTime");
        					long endTime = rs.getLong("SessionEndTime");
        					long latency = endTime - startTime;
        					if (rs.getRow()!=1) outp = outp + "<tr><td colspan=\"6\">&#160;<br></td></tr>";

        					outp = outp +  "<tr><td class=\"hdgss\">Session Name</td><td class=\"hdgs\">"+
									  (String) rs.getString("SessionName")+"</td><td class=\"start\">"+
 									  SQL_DATE_FORMAT.format(new Date( startTime))+"</td><td class=\"end\">"+
 									  SQL_DATE_FORMAT.format(new Date( endTime)) +"</td><td class=\"latency\">"+latency+"</td><td colspan=\"3\">&#160;</td></tr>";
        					// outp = outp + "<tr><td
        					// colspan=\"5\">&#160;<br></td></tr>";
        				}
        				prevSsn = currSsn;

        				currWkld = rs.getInt(1);
        				if (prevWkld != currWkld)
        				{
        					long startTime = rs.getLong("WorkloadStartTime");
        					long endTime = rs.getLong("WorkloadEndTime");
        					long latency = endTime - startTime;
        					String workloadName = (String) rs.getString("WorkloadName");
        					String wkStartTime = SQL_DATE_FORMAT.format(new Date(startTime));
        					String wkEndTime = SQL_DATE_FORMAT.format(new Date(endTime));
        					// outp = outp + "<tr><td
        					// colspan=\"5\">&#160;<br></td></tr>";
        					outp = outp +  	"<tr><td class=\"hdgWW\">Workload Name</td><td class=\"hdgw\">"+workloadName+
										    "</td><td class=\"start\">"+wkStartTime+"</td><td class=\"end\">"+ wkEndTime+
										    "</td><td class=\"latency\">"+latency+"</td><td colspan=\"3\">&#160;</td></tr>";
        					long checks = Long.parseLong((String)rs.getString("Checks"));
        					latency = rs.getLong("Latency");
        					String message = (String) rs.getString("Message").trim();
        					log.debug("1. PConfig:getRows message ["+message+"]");
        					String msgStartTime = SQL_DATE_FORMAT.format(new Date(Long.valueOf((String) rs.getString("MessageStartTime"))));
        					String msgEndTime = SQL_DATE_FORMAT.format(new Date(Long.valueOf((String) rs.getString("MessageEndTime"))));
        					int msgBatchID = rs.getInt("MessageBatchID");
        					int batchcount = rs.getInt("WorkloadBatchID");
        					int wkid = rs.getInt("Workload");
        					log.debug("PConfig:getRows batchcount ["+batchcount+"]");
							log.debug("PConfig:getRows wikid ["+wkid+"]");
        					String sessionName = (String)rs.getString("SessionName");

        					int rows = getRows(batchcount, wkid);
        					String srows = new Integer(rows).toString();
        					log.debug("PConfig:getRows srows="+srows);
        					String outMessage = "<a href=\"index.jsp?cmd=logmsg&pd.url="+pdurl+
        					"&pd.login="+pdlogin+"&pd.password="+pdpassword+"&fd.url="+fdurl+
        					"&fd.login="+fdlogin+"&fd.password="+fdpassword+"&sk.server="+skserver+"&sk.port="+skport+"&ng.server="+ngserver+"&ng.port="+ngport+
        					"&message="+message+"&wkid="+wkid+"&batchcount="+batchcount+
							"&wkname="+workloadName+"&ssname="+sessionName+"&msgbatchid="+msgBatchID+"\">" + message +"</a>";
        					if (!message.equals("SystemAdminLogMessage") || !message.equals("NSCALogMessage"))
        					{
        						log.debug("1. PConfig:getRows nnnnnnnnn");
        						srows = "";
        						outMessage = message;
        					}
        					log.debug("PConfig:getRows msgsCount ["+msgsCount+"]");
        					outp = outp + "<tr><td class=\"hdg1\">Message Name</td><td class=\"hdg2\">"+outMessage+"</td><td class=\"start\">" +
																msgStartTime +"</td><td class=\"end\">" +
																msgEndTime +"</td><td class=\"latency\">"+
																latency+"</td><td  class=\"latency\">"+
																checks+"</td><td  class=\"latency\">"+ 
																latency/checks +"</td><td class=\"end\" align=\"right\">"+
																srows+"</td></tr>";
        					msgsCount = 0;
        				}
        				else
        				{
        					long checks = Long.parseLong((String)rs.getString("Checks"));
        					long latency = rs.getLong("Latency");
        					String message = (String) rs.getString("Message").trim();
        					log.debug("2. PConfig:getRows message ["+message+"]");
        					String msgStartTime = SQL_DATE_FORMAT.format(new Date(Long.valueOf((String) rs.getString("MessageStartTime"))));
        					String msgEndTime = SQL_DATE_FORMAT.format(new Date(Long.valueOf((String) rs.getString("MessageEndTime"))));
							int msgBatchID = rs.getInt("MessageBatchID");
							int batchcount = rs.getInt("WorkloadBatchID");
							int wkid = rs.getInt("Workload");
							log.debug("PConfig:getRows batchcount ["+batchcount+"]");
							log.debug("PConfig:getRows wikid ["+wkid+"]");
        					String sessionName = (String)rs.getString("SessionName");
        					String workloadName = (String) rs.getString("WorkloadName");
        					int rows = getRows(batchcount, wkid);
        					String srows = new Integer(rows).toString();
        					log.debug("PConfig:getRows srows ["+srows+"]");
        					String outMessage = "<a href=\"index.jsp?cmd=logmsg&pd.url="+pdurl+
        					"&pd.login="+pdlogin+"&pd.password="+pdpassword+"&fd.url="+fdurl+
        					"&fd.login="+fdlogin+"&fd.password="+fdpassword+"&sk.server="+skserver+"&sk.port="+skport+"&ng.server="+ngserver+"&ng.port="+ngport+
        					"&message="+message+"&wkid="+wkid+"&batchcount="+batchcount+
							"&wkname="+workloadName+"&ssname="+sessionName+"&msgbatchid="+msgBatchID+"\">" + message +"</a>";
        					if (!message.equals("SystemAdminLogMessage") || !message.equals("NSCALogMessage"))
        					{
        						log.debug("2. PConfig:getRows nnnnnnnnn");
        						srows = "";
        						outMessage = message;
        					}
        					log.debug("PConfig:getRows msgsCount ["+msgsCount+"]");
        					outp = outp + "<tr><td>"+msgsCount+".</td><td class=\"hdg2\">"+outMessage+"</td><td class=\"start\">" +
																msgStartTime +"</td><td class=\"end\">" +
																msgEndTime +"</td><td class=\"latency\">"+
																latency+"</td><td  class=\"latency\">"+
																checks+"</td><td  class=\"latency\">"+ 
																latency/checks +"</td><td  class=\"end\" align=\"right\">"+
																srows+"</td></tr>";
        				}
        				prevWkld = currWkld;
        				msgsCount++;

        			}
    			}
			}
			catch (SQLException ex) {log.error("PConfig.SQLException: " + ex.getMessage());}
			finally 
			{
				close (stmt);
				close (rs);
				close (conn);
			}
	     }
	     return outp +"</table></div>";
	}



	
	public String getRows (String sessionName, String workloadName, String message, String batchcount, String wkid)
	{
		String outp = "<div class=\"header1\">"+navigationHeading+
	    "<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><br><br>";
	    
	    Connection conn = getConnect ("GWCollageDB");
	    java.text.SimpleDateFormat SQL_DATE_FORMAT = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    if (conn != null)
	    {
	    	Statement stmt = null;
	    	ResultSet rs = null;
	    	try 
	    	{
    			stmt = conn.createStatement();
    			String theStatement = "select "+
						"LogMessageID, DeviceID, TextMessage, FirstInsertDate,LastInsertDate, ReportDate "+
						"from LogMessage "+
						"where TextMessage like 'Profiler Log Message, Workload Id="+wkid+", BatchCount=%'";
    			if (stmt.execute(theStatement)) 
    			{   				
        			rs = stmt.getResultSet();
        			int msgsCount = 1;
        			outp = outp +   "<tr><td>&#160;</td><td  class=\"hdg0\">Session Name: "+sessionName+"</td><td  class=\"hdg0\">Workload Name: "+workloadName+"</td><td  colspan=\"2\" class=\"hdg0\">Message Name: "+message+"</td></tr>";
        			outp = outp +   "<tr><td>&#160;</td><td  class=\"hdg0\">TextMessage</td><td  class=\"hdg1\">FirstInsertDate</td><td  class=\"hdg1\">LastInsertDate</td><td  class=\"end\">ReportDate</td></tr>";
        			while (rs.next()) 
        			{
        					outp = outp +  "<tr><td>"+msgsCount+".</td><td class=\"hdgs\">"+(String) rs.getString("TextMessage")+"</td><td class=\"start\">"+
        					rs.getTimestamp("FirstInsertDate")+"</td><td class=\"start\">"+
        					rs.getTimestamp("LastInsertDate")+"</td><td class=\"end\">"+
        					rs.getTimestamp("ReportDate")+"</td></tr>";
        					msgsCount++;
        			}
    			}
	    	}
	    	catch (SQLException ex) {log.error("PConfig.SQLException: " + ex.getMessage());}
	    	finally 
	    	{
	    		close (stmt);
	    		close (rs);
	    		close (conn);
	    	}
	     }
	     return outp +"</table></div>";
	}


	
	public int getRows (int batchcount, int wkid)
	{
		int rows = 0;
	    Connection conn = getConnect ("GWCollageDB");
	    if (conn != null)
	    {
	    	Statement stmt = null;
	    	ResultSet rs = null;
	    	try 
	    	{
    			stmt = conn.createStatement();
    			String theStatement = "select count(*) "+
						"from LogMessage "+
						//"where TextMessage like 'Profiler Log Message, Workload Id="+wkid+", BatchCount="+batchcount+"'";
    					"where TextMessage like 'Profiler NSCALogMessage, %WId="+wkid+", B="+batchcount+"%'";
    			if (stmt.execute(theStatement)) 
    			{   				
        			rs = stmt.getResultSet();
        			while (rs.next()) 
        			{
        				rows = rs.getInt(1);
        			}
    			}
	    	}
	    	catch (SQLException ex) {log.error("PConfig.SQLException: " + ex.getMessage());}
	    	finally 
	    	{
	    		close (stmt);
	    		close (rs);
	    		close (conn);
	    	}
	     }
	     return rows;
	}


   public void close(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        }
        catch (Exception e) {
            // ignore
            e.printStackTrace();
        }
    }


    public void close(Statement stmt) {
        try {
            if (stmt != null) {
                stmt.close();
            }
        }
        catch (Exception e) {
            // ignore
            e.printStackTrace();
        }
    }


    public void close(PreparedStatement ps) {
        try {
            if (ps != null) {
                ps.close();
            }
        }
        catch (Exception e) {
            // ignore
            e.printStackTrace();
        }
    }


    public void close(Connection conn) {
        try {
            if (conn != null) {
                conn.close();
            }
        }
        catch (Exception e) {
            // ignore
            e.printStackTrace();
        }
    }



	public void setXmlConfigDir(String xmlConfigDir)
	{
		this.xmlConfigDir = xmlConfigDir;
	}

	public String getXmlConfigDir()
	{
		return this.xmlConfigDir;
	}



	public String  preFix (String qstring, String prefix, String idx)
	{
		String result = "";

		StringTokenizer token = new StringTokenizer(qstring, "&"); 
		String nullString ="";
		while (token.hasMoreElements()) 
		{ 

			String elemnt = (String)token.nextElement(); 
			if (!elemnt.startsWith("cmd")) 
			{ 

				String [] name_value = elemnt.split("=");
				if (name_value.length==1) {
					name_value = new String []{name_value[0],nullString};
				}
				if (result.equals("")) {
					result = prefix + "."+idx + "."+name_value[0]+"="+name_value[1];
				} else {
					result = result + "&" + prefix + "."+idx + "."+name_value[0]+"="+name_value[1];
				}	
			} 
		} 
		return result;
	}

	public void  setWkMsg (Map<String, String> mapTemp, String qstring, String filter, String prefix, int idx)
	{
		StringTokenizer token = new StringTokenizer(qstring, "&"); 
		String nullString ="";
		while (token.hasMoreElements()) 
		{ 

			String elemnt = (String)token.nextElement(); 
			if (!elemnt.startsWith("cmd") && elemnt.startsWith(filter)) 
			{ 

				String [] name_value = elemnt.split("=");
				if (name_value.length==1) {
					name_value = new String []{name_value[0],nullString};
				}

				String tmp = name_value[0];
				String [] leftS = tmp.split("\\.");

				if (leftS[0].equals(filter)) 
				{
					String messg = prefix +"."+leftS[idx];
					setWkMsg (mapTemp, messg, elemnt);
				}									
			} 
		} 
	}


	public void  setHeaders (Map<String, String> mapTemp, String qstring, String prefix)
	{
		StringTokenizer token = new StringTokenizer(qstring, "&"); 
		String nullString ="";
		while (token.hasMoreElements()) 
		{ 

			String elemnt = (String)token.nextElement(); 
			if (!elemnt.startsWith("cmd") && elemnt.startsWith(prefix)) 
			{ 

				String [] name_value = elemnt.split("=");
				if (name_value.length==1) {
					name_value = new String []{name_value[0],nullString};
				}

				String tmp = name_value[0];
				String [] leftS = tmp.split("\\.");

				if (leftS[0].equals(prefix)) 
				{
					String value = mapTemp.get(prefix);
					if (value == null) {
						mapTemp.put(prefix, elemnt);
					} else {
						mapTemp.put(prefix, value+"&"+elemnt);
					}
				}									
			} 
		} 
	}






	public Map<String, String>  setWkMsg (String qstring, String filter, String prefix, int idx)
	{
		Map<String, String> mapTemp = new HashMap<String, String>();
		setWkMsg (mapTemp, qstring, filter, prefix, idx);
		return mapTemp;
	}




	public void addNewWkld (Map<String,Map<String,String>> wkmapmsTemp, Map<String,String> workLoadTemp, String wkQueryStr)
	{
		int wkidx = workLoadTemp.size()+1;
		workLoadTemp.put("wk."+wkidx, preFix(wkQueryStr, "wk", new Integer(wkidx).toString()));
		wkmapmsTemp.put("wk."+wkidx, new HashMap<String, String>());	
	}



	public void addNewMsg (Map<String,Map<String,String>> wkmapmsTemp, String msgQueryStr, String wkidx)
	{
		Map<String,String> msgTable = wkmapmsTemp.get("wk."+wkidx);
		if (msgTable == null) {msgTable = new HashMap<String, String>();wkmapmsTemp.put("wk."+wkidx, msgTable);}
		int msidx = msgTable.size()+1;
		msgTable.put("ms."+msidx,preFix(msgQueryStr, "ms."+msidx, wkidx));	
		
	}


	public void  setWkMsg (Map<String, String> wkmsTemp, String wk_ms, String elemnt)
	{
					String wkmsgT = wkmsTemp.get(wk_ms);
					if (wkmsgT == null) {
						wkmsTemp.put(wk_ms, elemnt);
					} else {
						wkmsTemp.put(wk_ms, wkmsgT + "&" + elemnt);
					}
	}




	public int  getFrequency (String noun, String[]  args)
	{
		Map<String, Integer> m = new HashMap<String, Integer>();

        	// Initialize frequency table from command line
        	for (String a : args) 
		{
            		Integer freq = m.get(a);
            		m.put(a, freq == null ? 1 : freq + 1);
        	}
		return m.get(noun);
	}


	public List <String[]>  getList (String  args)
	{
		// list of pairs: L=({a,b}, {c,d}, ...)
		List <String[]> list = new ArrayList<String[]>();
		StringTokenizer token = new StringTokenizer(args, "&"); 
		String nullString ="";
		while (token.hasMoreElements()) 
		{ 

			String elemnt = (String)token.nextElement(); 
			if (!elemnt.startsWith("cmd")) 
			{ 

				String [] name_value = elemnt.split("=");
				if (name_value.length==2) {
					list.add(name_value);
				} else {
					list.add(new String []{name_value[0],nullString});
				}
			} 
		} 	
		return list;
	}



	public Map<String, List <String>>  getMap (String  args)
	{
		// list of pairs: L=({a,b}, {c,d}, ...)
		
		Map<String, List <String>> htable = new HashMap<String, List <String>> ();
		StringTokenizer token = new StringTokenizer(args, "&"); 
		String nullString ="";
		while (token.hasMoreElements()) 
		{ 
			String elemnt = (String)token.nextElement(); 
			if (!elemnt.startsWith("cmd")) 
			{ 
				String [] name_value = elemnt.split("=");
				if (name_value.length==1) {
					name_value = new String []{name_value[0],nullString};
				}

				List <String> values = htable.get(name_value[0]);
				if (values != null) {
					values.add(name_value[1]);
				} else
				{ 
					List <String> list = new ArrayList<String>();
					list.add(name_value[1]);
					htable.put(name_value[0], list);	
				}
			} 
		} 	
		return htable;
	}



	public String getXML (Document dom)
	{
		String xmlString = "";
	    try
	    {

            	TransformerFactory transfac = TransformerFactory.newInstance();
            	Transformer trans = transfac.newTransformer();
            	trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            	trans.setOutputProperty(OutputKeys.INDENT, "no");

            	// create string from xml tree
            	StringWriter sw = new StringWriter();
            	StreamResult result = new StreamResult(sw);
            	DOMSource source = new DOMSource(dom);
            	trans.transform(source, result);
            	xmlString = sw.toString();

	    }
	    catch (Exception e) {log.error("PConfig."+e);}
	    return xmlString;
	}

	public Element createLinkElement(Document dom, Map<String, List <String>> params, String link, String theText)
	{
		Element anEle = dom.createElement("a");
		if (params != null)
		{
			for (Map.Entry<String, List <String>> e : params.entrySet())
			{
				anEle.setAttribute(e.getKey(), e.getValue().get(0));
			}
		}
		anEle.setAttribute("href", link);
		anEle.appendChild(dom.createTextNode(theText));
		return anEle;
	}


	public Element createAnElement(Document dom, Map<String, List <String>> params, String elName)
	{
		Element anEle = dom.createElement(elName);
		if (params != null)
		{
			for (Map.Entry<String, List <String>> e : params.entrySet())
			{
				anEle.setAttribute(e.getKey(), e.getValue().get(0));
			}
		}
		return anEle;
	}


	public Element createAnInputElement(Document dom, String name, String  value)
	{
		Element inputEle = dom.createElement("input");
		inputEle.setAttribute("type", "text");
		inputEle.setAttribute("class", "in");
		inputEle.setAttribute("name", name);
		inputEle.setAttribute("value", value);
		return inputEle;
	}

	public Element createRadioButtonElement(Document dom, String name, String value)
	{
		Element inputEle = dom.createElement("input");
		inputEle.setAttribute("type", "radio");
		inputEle.setAttribute("style", "width:10px;float:left");
		inputEle.setAttribute("class", "in");
		inputEle.setAttribute("name", name);
		inputEle.setAttribute("value", value);
		return inputEle;
	}


	public Element createCheckBoxElement(Document dom, String name, String value)
	{
		Element inputEle = dom.createElement("input");
		inputEle.setAttribute("type", "checkbox");
		inputEle.setAttribute("style", "width:15px;");
		inputEle.setAttribute("class", "in");
		inputEle.setAttribute("name", name);
		inputEle.setAttribute("value", value);
		return inputEle;
	}

	public Element createSimpleSelectElement (Document dom, String choices, String name)
	{
		Element isEle = null;
		Map<String, List <String>> types = getMap(choices);
		for (Map.Entry<String, List <String>> e : types.entrySet())
		{
			String nv0 = e.getKey();
			isEle = createSelectElement (dom, e, name);
		}
		return isEle;
	}

	public Element createAWorkloadElement(Document dom, Map<String, List <String>> params,  String  title, int i)
	{
		Element divEle = createAnElement(dom, getMap ("class=header2"), "div");
		Element tableEle = createAnElement(dom, null, "table");
		divEle.appendChild(tableEle);
		// =======================================================================================
		Element trEle = createAnElement(dom, null, "tr");
		tableEle.appendChild(trEle);
		Element tdEle = createAnElement(dom, getMap ("align=left"), "td");
		trEle.appendChild(tdEle);
		tdEle.appendChild(createCheckBoxElement(dom, "addms", "addms."+new Integer(i).toString()));
		tdEle = createAnElement(dom, getMap ("align=left"), "td");
		trEle.appendChild(tdEle);
		tdEle.appendChild(createSimpleSelectElement (dom, messageChoices, "addms."+new Integer(i).toString()));
		tdEle = createAnElement(dom, getMap ("align=left"), "td");
		trEle.appendChild(tdEle);
		org.w3c.dom.Text txtEle = dom.createTextNode("Choose a message type when adding another message ");
		tdEle.appendChild(txtEle);
		// =======================================================================================
		trEle = createAnElement(dom, null, "tr");
		tableEle.appendChild(trEle);
		tdEle = createAnElement(dom, getMap ("align=middle"), "td");
		trEle.appendChild(tdEle);
		
		tdEle = createAnElement(dom, getMap ("align=left&colspan=2"), "td");
		trEle.appendChild(tdEle);
		Element inputEle = createAnElement(dom, getMap("name=cmd&type=submit&class=btn&value=Delete"),"input");
		inputEle.setAttribute("onmouseover","this.className=\'btn btnhov\'");
		inputEle.setAttribute("onmouseout","this.className=\'btn\'");
		tdEle.appendChild(inputEle);
		tdEle.appendChild(dom.createTextNode(" this "));
		tdEle.appendChild(createCheckBoxElement(dom, "delEl", "wk."+new Integer(i).toString()));

		tdEle.appendChild(dom.createTextNode(" Workload"));
		// =======================================================================================
		divEle.appendChild(createTableRows(dom, params, title+" "+new Integer(i).toString()));
		return divEle;
	}


	public Element createAMessageElement(Document dom, String messageType, String  title, String ms)
	{
		Element divEle = createAnElement(dom, getMap ("class=header3"), "div");
		// =======================================================================================
		Element tableEle = createAnElement(dom, null, "table");
		divEle.appendChild(tableEle);
		Element trEle = createAnElement(dom, null, "tr");
		tableEle.appendChild(trEle);
		Element tdEle = createAnElement(dom, getMap ("align=middle"), "td");
		trEle.appendChild(tdEle);
		

		
		tdEle = createAnElement(dom, getMap ("align=middle"), "td");
		trEle.appendChild(tdEle);

		Element inputEle = createAnElement(dom, getMap("name=cmd&type=submit&class=btn&value=Delete"),"input");
		inputEle.setAttribute("onmouseover","this.className=\'btn btnhov\'");
		inputEle.setAttribute("onmouseout","this.className=\'btn\'");
		tdEle.appendChild(inputEle);
		tdEle.appendChild(dom.createTextNode(" this "));

		tdEle.appendChild(createCheckBoxElement(dom, "delEl", ms));
		
		
		org.w3c.dom.Text txtEle = dom.createTextNode(" Message ");
		tdEle.appendChild(txtEle);

		// =======================================================================================
		divEle.appendChild(createTableRows(dom, getMap (messageType), title));
		return divEle;
	}

	public Element createTableRows(Document dom, Map<String, List <String>> params, String message)
	{
		Element tableEle = createAnElement(dom, null, "table");
		createSubTableRows(dom, tableEle, params, message);
		return tableEle;
	}

	public Element createNavigationHeadingTable(Document dom)
	{
		
		Element tableEle = createAnElement(dom, null, "table");
		tableEle.setAttribute("bgcolor", "black");
		tableEle.setAttribute("width", "710");
		Element trEle = createAnElement(dom, null, "tr");
		tableEle.appendChild(trEle);
		Element tdEle = createAnElement(dom, null, "td");
		tdEle.setAttribute("align", "middle");
		tdEle.setAttribute("style", "background-color:#DC143C;color:#FFFFFF");
		trEle.appendChild(tdEle);
		tdEle.appendChild(createLinkElement(dom, getMap("target=_self&style=font-weight:bold;text-decoration:none;color:#FFFFFF"), "index.html", "About"));
		
		tdEle = createAnElement(dom, null, "td");
		tdEle.setAttribute("align", "middle");
		tdEle.setAttribute("bgcolor", "#FFD700");
		trEle.appendChild(tdEle);
		tdEle.appendChild(createLinkElement(dom, getMap("target=_self&style=font-weight:bold;text-decoration:none;"), "index.jsp?cmd=simplesa", "SystemAdmin"));
		
		tdEle = createAnElement(dom, null, "td");
		tdEle.setAttribute("align", "middle");
		tdEle.setAttribute("bgcolor", "#FFA500");
		trEle.appendChild(tdEle);
		tdEle.appendChild(createLinkElement(dom, getMap("target=_self&style=font-weight:bold;text-decoration:none;"), "helpSystemAdmin.html", "Help SystemAdmin"));

		tdEle = createAnElement(dom, null, "td");
		tdEle.setAttribute("align", "middle");
		tdEle.setAttribute("bgcolor", "#FFD700");
		trEle.appendChild(tdEle);
		tdEle.appendChild(createLinkElement(dom, getMap("target=_self&style=font-weight:bold;text-decoration:none;"), "index.jsp?cmd=newconfig", "Foundation 1.5"));

		tdEle = createAnElement(dom, null, "td");
		tdEle.setAttribute("align", "middle");
		tdEle.setAttribute("bgcolor", "#FFA500");
		trEle.appendChild(tdEle);
		tdEle.appendChild(createLinkElement(dom, getMap("target=_self&style=font-weight:bold;text-decoration:none;"), "helpLegacy.html", "Help Foundation 1.5"));

		// NSCA - Extension

		tdEle = createAnElement(dom, null, "td");
		tdEle.setAttribute("align", "middle");
		tdEle.setAttribute("bgcolor", "#FFD700");
		trEle.appendChild(tdEle);
		tdEle.appendChild(createLinkElement(dom, getMap("target=_self&style=font-weight:bold;text-decoration:none;"), "index.jsp?cmd=nscaext", "NSCA Extension"));

		tdEle = createAnElement(dom, null, "td");
		tdEle.setAttribute("align", "middle");
		tdEle.setAttribute("bgcolor", "#FFA500");
		trEle.appendChild(tdEle);
		tdEle.appendChild(createLinkElement(dom, getMap("target=_self&style=font-weight:bold;text-decoration:none;"), "helpNSCAext.html", "Help NSCA Extension"));
		
		return tableEle;
	}

	public Element createTableHeadRows(Document dom,Map<String,String> tableHead, String cmd, String fileName)
	{
		log.debug("createTableHeadRows================begin");
		Element divEle = createAnElement(dom, getMap("class=header1"), "div");
		Element tableEle = createNavigationHeadingTable(dom);
		
		divEle.appendChild(tableEle);
		divEle.appendChild(createAnElement(dom, null, "br"));
		divEle.appendChild(createAnElement(dom, null, "br"));
		// Element inputEle = createAnElement(dom,
		// getMap("name=cmd&type=reset&class=btn"),"input");
		Element inputEle = null;
		// inputEle.setAttribute("onmouseover","this.className=\'btn btnhov\'");
		// inputEle.setAttribute("onmouseout","this.className=\'btn\'");
		// divEle.appendChild(inputEle);
		divEle.appendChild(createAnElement(dom, null, "br"));
		// divEle.appendChild(createAnElement(dom, null, "br"));
		// divEle.appendChild(createAnElement(dom, null, "br"));
		// ==========================================================================================
		Element fieldsetEle = createAnElement(dom, null, "fieldset");
		divEle.appendChild(fieldsetEle);
		Element legendEle = createAnElement(dom, null, "legend");
		legendEle.setAttribute("style", "font-size:18px");
		legendEle.appendChild(dom.createTextNode("Test System"));
		fieldsetEle.appendChild(legendEle);
		tableEle = createAnElement(dom, null, "table");
		fieldsetEle.appendChild(tableEle);
		Element trEle = createAnElement(dom, null, "tr");
		Element tdEle = createAnElement(dom, null, "td");
		tableEle.appendChild(trEle);
		trEle.appendChild(tdEle);
		tdEle.setAttribute("align", "left");
		tdEle.setAttribute("colspan", "2");
		File dir = new File(xmlConfigDir);
		File parent = dir.getAbsoluteFile();
		
		File fparent = new File (parent.getParent());
		File fprevParent = new File (fparent.getParent());

		String absoluteFileDir = fprevParent.getAbsolutePath();
		
		
		// String absoluteFileDir = parent.getAbsolutePath();
		tdEle.appendChild(dom.createTextNode("Directory to store your configurations:"+absoluteFileDir+"/"));

		
		tdEle.appendChild(createAnElement(dom, getMap("name=xmlConfigDir&style=font-size: small;width: 260px;&value=profilersa/xml&class=in&type=text"), "input"));
		Element hiddenEle = createAnElement(dom, getMap("name=configFilesDir&value="+absoluteFileDir+"/profilersa&type=hidden"),"input");
		tdEle.appendChild(hiddenEle);
		trEle = createAnElement(dom, null, "tr");
		tableEle.appendChild(trEle);
		tdEle = createAnElement(dom, null, "td");
		tdEle.setAttribute("align", "left");

		trEle.appendChild(tdEle);
		tdEle.appendChild(dom.createTextNode("After defining the workloads create a "));
		inputEle = createAnElement(dom, getMap("name=cmd&type=submit&class=btn&value=new"),"input");
		inputEle.setAttribute("onmouseover","this.className=\'btn btnhov\'");
		inputEle.setAttribute("onmouseout","this.className=\'btn\'");
		tdEle.appendChild(inputEle);
		tdEle.appendChild(dom.createTextNode(" configuration: "));
		tdEle.appendChild(createAnElement(dom, getMap("name=configxml&type=text&class=in&alt=Specify a file name"), "input"));
		
		trEle = createAnElement(dom, null, "tr");
		tableEle.appendChild(trEle);

		tdEle = createAnElement(dom, null, "td");
		tdEle.setAttribute("align", "left");
		trEle.appendChild(tdEle);
		tdEle.appendChild(dom.createTextNode("Choose this"));

		Element selectEle = createAnElement(dom, null, "select");
		tdEle.appendChild(selectEle);
		selectEle.setAttribute("name", "config");
		selectEle.setAttribute("class", "in");
		Element optionEle = null;
		String [] files = getXMLFiles(xmlConfigDir);
		
		if (files != null)
		{
			for (int i=0;i<files.length;i++)
			{
				optionEle = createAnElement(dom, null, "option");
				optionEle.setAttribute("value", files[i]);
				optionEle.setAttribute("class", "in");
				if (files[i].equals(fileName)) optionEle.setAttribute("selected", "true");
				optionEle.appendChild(dom.createTextNode(files[i]));
				selectEle.appendChild(optionEle);
			}
		}
		tdEle.appendChild(dom.createTextNode("configuration to"));

		inputEle = createAnElement(dom, getMap("name=cmd&type=submit&class=btn&value=execute"),"input");
		inputEle.setAttribute("onmouseover","this.className=\'btn btnhov\'");
		inputEle.setAttribute("onmouseout","this.className=\'btn\'");
		tdEle.appendChild(inputEle);
		Element spanEle = createAnElement(dom, null, "span");
		spanEle.setAttribute("style","font-size:26px;font-weight:bold;color:black");
		spanEle.appendChild(dom.createTextNode("/"));
		tdEle.appendChild(spanEle);
		
		inputEle = createAnElement(dom, getMap("name=cmd&type=submit&class=btn&value=change"),"input");
		inputEle.setAttribute("onmouseover","this.className=\'btn btnhov\'");
		inputEle.setAttribute("onmouseout","this.className=\'btn\'");
		tdEle.appendChild(inputEle);
		spanEle = createAnElement(dom, null, "span");
		spanEle.setAttribute("style","font-size:26px;font-weight:bold;color:black");
		spanEle.appendChild(dom.createTextNode("/"));
		tdEle.appendChild(spanEle);
		
		inputEle = createAnElement(dom, getMap("name=cmd&type=submit&class=btn&value=delete"),"input");
		inputEle.setAttribute("onmouseover","this.className=\'btn btnhov\'");
		inputEle.setAttribute("onmouseout","this.className=\'btn\'");
		tdEle.appendChild(inputEle);
		spanEle = createAnElement(dom, null, "span");
		spanEle.setAttribute("style","font-size:26px;font-weight:bold;color:black");
		spanEle.appendChild(dom.createTextNode("/"));
		tdEle.appendChild(spanEle);
		
		inputEle = createAnElement(dom, getMap("name=cmd&type=submit&class=btn&value=save"),"input");
		inputEle.setAttribute("onmouseover","this.className=\'btn btnhov\'");
		inputEle.setAttribute("onmouseout","this.className=\'btn\'");
		tdEle.appendChild(inputEle);
		spanEle = createAnElement(dom, null, "span");
		spanEle.setAttribute("style","font-size:26px;font-weight:bold;color:black");
		spanEle.appendChild(dom.createTextNode("/"));
		tdEle.appendChild(spanEle);
		
		
		// ====================================================Foundation Profiler=================================================
		Element tableEle1_2 = createAnElement(dom, null, "table");
		tdEle.appendChild(tableEle1_2);
		Element trEle1 = createAnElement(dom, null, "tr");
		tableEle1_2.appendChild(trEle1);
		Element tdEle1 = createAnElement(dom, getMap("align=middle&colspan=2"), "td");
		trEle1.appendChild(tdEle1);
		Element tableFPEle = createAnElement(dom, null, "table");
		createSubTableRows(dom, tableFPEle, getMap (tableHead.get("fP")), "Foundation Profiler");
		tdEle1.appendChild(tableFPEle);
		/*
		 * tdEle1.appendChild(createAnElement(dom,
		 * "id=fP.captureMetrics","for")); tdEle1 = createAnElement(dom, null,
		 * "td"); trEle1.appendChild(tdEle1); selectEle = createAnElement(dom,
		 * getMap("id=fP.captureMetrics&name=fP.captureMetrics&multiple=true&class=at"),
		 * "select"); optionEle = createAnElement(dom, getMap("value=ALL"),
		 * "option"); optionEle.appendChild(dom.createTextNode("ALL"));
		 * selectEle.appendChild(optionEle); optionEle = createAnElement(dom,
		 * getMap("value=LOG"), "option");
		 * optionEle.appendChild(dom.createTextNode("LOG"));
		 * selectEle.appendChild(optionEle); optionEle = createAnElement(dom,
		 * getMap("value=OFF"), "option");
		 * optionEle.appendChild(dom.createTextNode("OFF"));
		 * selectEle.appendChild(optionEle); tdEle1.appendChild(selectEle);
		 */
		tdEle1 = createAnElement(dom, null, "td");
		trEle1.appendChild(tdEle1);
		tdEle1.appendChild(dom.createTextNode("ALL – Metrics are captured and outputted to both the log and db"));
		tdEle1.appendChild(createAnElement(dom, null, "br"));
		tdEle1.appendChild(dom.createTextNode("LOG – Metrics are captured and outputted to the log only"));
		tdEle1.appendChild(createAnElement(dom, null, "br"));
		tdEle1.appendChild(dom.createTextNode("OFF – NO metrics are captured"));
		// ==================================================Foundation Profiler===================================================
		
		
/*
		if (!cmd.equals("simplesa"))
		{
			spanEle = createAnElement(dom, null, "span");
			spanEle.setAttribute("style","font-size:26px;font-weight:bold;color:black");
			spanEle.appendChild(dom.createTextNode("/"));
			tdEle.appendChild(spanEle);
		
			inputEle = createAnElement(dom, getMap("name=cmd&type=submit&class=btn&value=save"),"input");
			inputEle.setAttribute("onmouseover","this.className=\'btn btnhov\'");
			inputEle.setAttribute("onmouseout","this.className=\'btn\'");
			tdEle.appendChild(inputEle);
		}
*/
		// =====================================================================================================
		tdEle = createAnElement(dom, null, "td");
		trEle.appendChild(tdEle);
		Element fieldsetEle1 = createAnElement(dom, null, "fieldset");
		tdEle.appendChild(fieldsetEle1);
		Element tableEle1 = createAnElement(dom, null, "table");
		createSubTableRows(dom, tableEle1, getMap (tableHead.get("pd")), "Profiler DB");
		fieldsetEle1.appendChild(tableEle1);
		
		inputEle = createAnElement(dom, getMap("name=cmd&type=submit&class=btn&value=cleanDB"),"input");
		inputEle.setAttribute("onmouseover","this.className=\'btn btnhov\'");
		inputEle.setAttribute("onmouseout","this.className=\'btn\'");
		fieldsetEle1.appendChild(inputEle);
		
		inputEle = createAnElement(dom, getMap("name=cmd&type=submit&class=btn&value=Report"),"input");
		inputEle.setAttribute("onmouseover","this.className=\'btn btnhov\'");
		inputEle.setAttribute("onmouseout","this.className=\'btn\'");
		fieldsetEle1.appendChild(inputEle);
		// =====================================================================================================
		//trEle = createAnElement(dom, null, "tr");
		//tableEle.appendChild(trEle);
		//tdEle = createAnElement(dom, null, "td");
		//trEle.appendChild(tdEle);
		// ====================================================Foundation Profiler=================================================

		
		
		
		divEle.appendChild(createAnElement(dom, null, "br"));
		divEle.appendChild(createAnElement(dom, null, "br"));
		// ====================================================================================================================
		fieldsetEle = createAnElement(dom, null, "fieldset");
		divEle.appendChild(fieldsetEle);
		divEle.appendChild(createAnElement(dom, null, "br"));
		legendEle = createAnElement(dom, null, "legend");
		legendEle.setAttribute("style", "font-size:18px");
		legendEle.appendChild(dom.createTextNode("Target Groundwork Server"));
		fieldsetEle.appendChild(legendEle);
		tableEle = createAnElement(dom, null, "table");
		tableEle.setAttribute("width","500");
		fieldsetEle.appendChild(tableEle);
		trEle = createAnElement(dom, null, "tr");
		tableEle.appendChild(trEle);
		
		tdEle = createAnElement(dom, null, "td");
		tdEle.setAttribute("valign","top");
		trEle.appendChild(tdEle);
		Element tableEle2 = createAnElement(dom, null, "table");
		createSubTableRows(dom, tableEle2, getMap (tableHead.get("sk")), "Message Socket");
		tdEle.appendChild(tableEle2);
		

		if (cmd.equals("nscaext"))
		{
			log.debug ("PConfig.call to Nagios Socket create table begin");
			log.debug("PConfig.1 >> ");
			tdEle = createAnElement(dom, null, "td");
			tdEle.setAttribute("valign","top");
			log.debug("PConfig.2 >> ");
			trEle.appendChild(tdEle);
			log.debug("PConfig.3 >> ");
			tableEle2 = createAnElement(dom, null, "table");
			log.debug("PConfig.4 >> ");
			log.debug("PConfig.4-bis >> "+tableHead.get("ng"));
			createSubTableRows(dom, tableEle2, getMap (tableHead.get("ng")), "Nagios Socket");
			log.debug("PConfig.5 >> ");
			tdEle.appendChild(tableEle2);
			log.debug("PConfig.6 >> ");
			log.debug ("PConfig.call to Nagios Socket create table end");
		}

		tdEle = createAnElement(dom, null, "td");
		trEle.appendChild(tdEle);
		Element tableEle3 = createAnElement(dom, null, "table");
		log.debug("PConfig.------7 >> "+tableHead.get("fd"));
		createSubTableRows(dom, tableEle3, getMap (tableHead.get("fd")), "Foundation DB");
		tdEle.appendChild(tableEle3);

		tdEle = createAnElement(dom, null, "td");
		trEle.appendChild(tdEle); 
		log.debug("PConfig.createTableHeadRows================end");
		return divEle;
	}


	public Element createSelectElement (Document dom, Map.Entry<String, List <String>> e, String nv0, String name)
	{
		Element  isEle = null;	
    		if (e.getValue().size()==1) {
				isEle = createAnInputElement(dom, nv0, e.getValue().get(0));
			} else
		{
			isEle = createAnElement(dom, getMap ("id="+nv0+"&name="+name+"&class=in"), "select");
			for (String val : e.getValue()) 
			{
				Element optionEle = createAnElement(dom, getMap ("value="+val+"&class=in"), "option");
				org.w3c.dom.Text txtEle = dom.createTextNode(val);
				optionEle.appendChild(txtEle);
				isEle.appendChild(optionEle);
			}
		}
		return isEle;
	}


	public Element createSelectElement (Document dom, Map.Entry<String, List <String>> e, String nv0)
	{
		Element  isEle = null;	
    		if (e.getValue().size()==1) {
				isEle = createAnInputElement(dom, nv0, e.getValue().get(0));
				if (nv0.startsWith("sk.port")) isEle.setAttribute("style", "width:52px;text-align:right;");
				if (nv0.startsWith("sk.server")) isEle.setAttribute("style", "width:72px;text-align:right;");
				if (nv0.startsWith("ng.port")) isEle.setAttribute("style", "width:52px;text-align:right;");
				if (nv0.startsWith("ng.server")) isEle.setAttribute("style", "width:72px;text-align:right;");
				if (nv0.startsWith("fP.captureMetrics")) isEle.setAttribute("style", "width:32px;text-align:right;");
				if (nv0.endsWith("url")) isEle.setAttribute("style", "width:254px;text-align:right;");
				if (nv0.endsWith("login")) isEle.setAttribute("style", "width:52px;text-align:right;");
				if (nv0.endsWith("password")) isEle.setAttribute("style", "width:52px;text-align:right;");
			} else
		{
			isEle = createAnElement(dom, getMap ("id="+nv0+"&name="+nv0), "select");
			isEle.setAttribute("class","in");
			if (nv0.startsWith("fP.captureMetrics")) isEle.setAttribute("multiple","true");
			int i = 1;
			Element optionEle = null; 
			for (String val : e.getValue()) 
			{
				if (i == 1) optionEle =	createAnElement(dom, getMap ("value="+val+"&class=in&selected=true"), "option");
				else
				optionEle =	createAnElement(dom, getMap ("value="+val+"&class=in"), "option");
				org.w3c.dom.Text txtEle = dom.createTextNode(val);
				optionEle.appendChild(txtEle);
				isEle.appendChild(optionEle);
				i++;
			}
		}
		return isEle;
	}


	public Element createSubTableRows(Document dom, Element tableEle, Map<String, List <String>> params, String message)
	{
		Element trEle = createAnElement(dom, null, "tr");
		tableEle.appendChild(trEle);
		Element tdEle = createAnElement(dom, null, "td");
		tdEle.setAttribute("align", "middle");
		tdEle.setAttribute("valign", "top");
		tdEle.setAttribute("colspan", "2");
		trEle.appendChild(tdEle);
		org.w3c.dom.Text txtEle = dom.createTextNode(message);
		tdEle.appendChild(txtEle);

		for (Map.Entry<String, List <String>> e : params.entrySet())
		{
			String nv0 = e.getKey();
			trEle = createAnElement(dom, null, "tr");
			tableEle.appendChild(trEle);
			tdEle = createAnElement(dom, null, "td");
			trEle.appendChild(tdEle);
			Element forEle = createAnElement(dom, getMap ("id="+nv0), "for");

			String placer = "";

			if (nv0.startsWith("ms.")) {String[] forTag = nv0.split("\\.");placer = forTag[3];}
			else
			if (nv0.startsWith("wk.")) {String[] forTag = nv0.split("\\.");placer = forTag[2];}
			else
			if (nv0.startsWith("fP.")|| 
			    nv0.startsWith("fd.")|| 
			    nv0.startsWith("pd.")|| 
			    nv0.startsWith("ng.")|| 
			    nv0.startsWith("sk.")) {String[] forTag = nv0.split("\\.");placer = forTag[1];} else {
				placer = nv0;
			}
			txtEle = dom.createTextNode(placer);
			forEle.appendChild(txtEle);
			tdEle.appendChild(forEle);

			tdEle.setAttribute("width", "150");
			tdEle = createAnElement(dom, null, "td");
			trEle.appendChild(tdEle);
			tdEle.appendChild(createSelectElement (dom, e, nv0));
			
		}
		return tableEle;
	}



	public Element createSubTableRows(Document dom, Element topEle,  Map<String, String> workloadTemp, 
										Map<String,Map<String,String>>  wkmapmsTemp)
	{
		Element workloadEle = null;
		int i = 0;
		for (Map.Entry<String, Map<String,String>> e : wkmapmsTemp.entrySet())
		{
			i++;
    			String wkld = e.getKey();
			int j = 0;
			workloadEle = createAWorkloadElement(dom, getMap(workloadTemp.get(wkld)),  "Workload", i);
			for (Map.Entry<String, String> m : e.getValue().entrySet())
			{
				j++;

				workloadEle.appendChild(createAMessageElement(dom, m.getValue(), "Message "+j, m.getKey() + "."+ new Integer(i).toString()));

			}
			topEle.appendChild(workloadEle);
		}
		return topEle;
	}




	public void showWkMs(Map<String, String> workloadTemp, Map<String,Map<String,String>>  wkmapmsTemp)
	{

		int i = 0;
		for (Map.Entry<String, Map<String,String>> e : wkmapmsTemp.entrySet())
		{
			i++;
    			String wkld = e.getKey();
			int j = 0;
			log.debug("PConfig.Workload "+ i + "     " +getMap(workloadTemp.get(wkld)));
			for (Map.Entry<String, String> m : e.getValue().entrySet())
			{
				j++;
				log.debug("PConfig.Message "+j+"    " + m.getValue());

			}
		}
	}
	
	public Element createHiddenSimpleSAElements(Document dom, Element divEle)
	{
	
		divEle.appendChild(createAnElement(dom, getMap("name=wk.1.name&type=hidden&value=SimpleSystemAdmin"), "input"));
		divEle.appendChild(createAnElement(dom, getMap("name=wk.1.enabled&type=hidden&value=true"), "input"));
		divEle.appendChild(createAnElement(dom, getMap("name=wk.1.interval&type=hidden&value=1"), "input"));
		divEle.appendChild(createAnElement(dom, getMap("name=wk.1.numBatches&type=hidden&value=1"), "input"));
		divEle.appendChild(createAnElement(dom, getMap("name=wk.1.distribution&type=hidden&value=even"), "input"));
		divEle.appendChild(createAnElement(dom, getMap("name=wk.1.quantity&type=hidden&value=1"), "input"));
		divEle.appendChild(createAnElement(dom, getMap("name=ms.1.1.type&type=hidden&value=org.groundwork.foundation.profiling.messages.SystemAdminInitMessage"), "input"));
		divEle.appendChild(createAnElement(dom, getMap("name=ms.1.1.name&type=hidden&value=SystemAdminInitMessage"), "input"));
		divEle.appendChild(createAnElement(dom, getMap("name=ms.3.1.numHosts&type=hidden&value=10"), "input"));
		divEle.appendChild(createAnElement(dom, getMap("name=ms.3.1.type&type=hidden&value=org.groundwork.foundation.profiling.messages.SystemAdminServiceStatusMessage"), "input"));
		divEle.appendChild(createAnElement(dom, getMap("name=ms.3.1.numServices&type=hidden&value=20"), "input"));
		divEle.appendChild(createAnElement(dom, getMap("name=ms.3.1.name&type=hidden&value=SystemAdminToggleServiceStatusMessage"), "input"));
		divEle.appendChild(createAnElement(dom, getMap("name=ms.4.1.type&type=hidden&value=org.groundwork.foundation.profiling.messages.SystemAdminLogMessage"), "input"));
		divEle.appendChild(createAnElement(dom, getMap("name=ms.4.1.name&type=hidden&value=SystemAdminLogMessage"), "input"));
		divEle.appendChild(createAnElement(dom, getMap("name=ms.4.1.numHosts&type=hidden&value=10"), "input"));
		
		divEle.appendChild(createAnElement(dom, getMap("name=ms.2.1.type&type=hidden&value=org.groundwork.foundation.profiling.messages.SystemAdminToggleHostStatusMessage"), "input"));
		divEle.appendChild(createAnElement(dom, getMap("name=ms.2.1.name&type=hidden&value=SystemAdminToggleHostStatusMessage"), "input"));
		divEle.appendChild(createAnElement(dom, getMap("name=ms.2.1.numHosts&type=hidden&value=10"), "input"));
		
		return divEle;
	}
	
	
	
	public Element createSimpleSATable(Document dom, Element topEle)
	{
		Element divEle = createAnElement(dom, null, "div");
		topEle.appendChild(divEle);
		Element tableEle = createAnElement(dom, null, "table");
		divEle.appendChild(tableEle);
		Element trEle = createAnElement(dom, null, "tr");
		tableEle.appendChild(trEle);
		Element thEle = createAnElement(dom, null, "th");
		trEle.appendChild(thEle);
		thEle.setAttribute("colspan", "6");
		thEle.appendChild(dom.createTextNode("Initialize the system with following number of Hosts and Services:"));

		trEle = createAnElement(dom, null, "tr");
		tableEle.appendChild(trEle);
		Element tdEle = createAnElement(dom, null, "td");
		trEle.appendChild(tdEle);
		tdEle.setAttribute("align", "right");
		tdEle.appendChild(dom.createTextNode("Number of Hosts: "));
		tdEle = createAnElement(dom, null, "td");
		trEle.appendChild(tdEle);
		tdEle.appendChild(createAnElement(dom, getMap("style=width:42px;&name=ms.1.1.numHosts&type=text&class=in&value=10"), "input"));
		
		tdEle = createAnElement(dom, null, "td");
		trEle.appendChild(tdEle);
		tdEle.setAttribute("align", "right");
		tdEle.appendChild(dom.createTextNode("Toggle the Service Status in percentage:"));
		tdEle = createAnElement(dom, null, "td");
		trEle.appendChild(tdEle);
		tdEle.appendChild(createAnElement(dom, getMap("style=width:42px;&class=in&name=ms.3.1.monitorUpPercentage&type=text&value=80"), "input"));
		
		
		tdEle = createAnElement(dom, null, "td");
		trEle.appendChild(tdEle);
		tdEle.setAttribute("align", "right");
		tdEle.appendChild(dom.createTextNode("Interval between batches:"));
		tdEle = createAnElement(dom, null, "td");
		trEle.appendChild(tdEle);
		tdEle.appendChild(createAnElement(dom, getMap("style=width:42px;&name=wk.1.interval&type=text&class=in&value=30"), "input"));
		

		trEle = createAnElement(dom, null, "tr");
		tableEle.appendChild(trEle);
		tdEle = createAnElement(dom, null, "td");
		trEle.appendChild(tdEle);
		tdEle.setAttribute("align", "right");
		tdEle.appendChild(dom.createTextNode("Number of Services: "));
		tdEle = createAnElement(dom, null, "td");
		trEle.appendChild(tdEle);
		tdEle.appendChild(createAnElement(dom, getMap("style=width:42px;&name=ms.1.1.numServices&type=text&class=in&value=20"), "input"));
		
		
		tdEle = createAnElement(dom, null, "td");
		trEle.appendChild(tdEle);
		tdEle.setAttribute("align", "right");
		tdEle.appendChild(dom.createTextNode("Toggle the Host Status in percentage:"));
		tdEle = createAnElement(dom, null, "td");
		trEle.appendChild(tdEle);
		tdEle.appendChild(createAnElement(dom, getMap("style=width:42px;&name=ms.2.1.monitorUpPercentage&type=text&class=in&value=70"), "input"));

		
		tdEle = createAnElement(dom, null, "td");
		trEle.appendChild(tdEle);
		tdEle.setAttribute("align", "right");
		tdEle.appendChild(dom.createTextNode("Number of batches:"));
		tdEle = createAnElement(dom, null, "td");
		trEle.appendChild(tdEle);
		tdEle.appendChild(createAnElement(dom, getMap("style=width:42px;&name=wk.1.numBatches&type=text&class=in&value=1"), "input"));

		

		trEle = createAnElement(dom, null, "tr");
		tableEle.appendChild(trEle);
		tdEle = createAnElement(dom, null, "td");
		trEle.appendChild(tdEle);
		tdEle.setAttribute("align", "right");
		tdEle.appendChild(dom.createTextNode("Threshold: "));
		tdEle = createAnElement(dom, null, "td");
		trEle.appendChild(tdEle);
		tdEle.appendChild(createAnElement(dom, getMap("style=width:42px;&name=ms.1.1.threshold&type=text&class=in&value=5"), "input"));


		
		tdEle = createAnElement(dom, null, "td");
		trEle.appendChild(tdEle);
		tdEle.setAttribute("align", "right");
		tdEle.appendChild(dom.createTextNode("Specify in percentage the consolidation rate:"));
		tdEle = createAnElement(dom, null, "td");
		trEle.appendChild(tdEle);
		tdEle.appendChild(createAnElement(dom, getMap("style=width:42px;&name=ms.4.1.csPercent&type=text&class=in&value=30"), "input"));

		tdEle = createAnElement(dom, null, "td");
		trEle.appendChild(tdEle);
		// tdEle.setAttribute("align", "right");
		// tdEle.appendChild(dom.createTextNode("Interval between batches:"));
		tdEle = createAnElement(dom, null, "td");
		trEle.appendChild(tdEle);
		// tdEle.appendChild(createAnElement(dom,
		// getMap("style=width:42px;&name=wk.1.quantity&type=text&class=in&value=1"),
		// "input"));

		
		
		return topEle;
	}

	
	

	public Element createNSCAExtTable(Document dom, Element topEle)
	{
		log.debug ("PConfig.createNSCAExtTable begin");
		Element divEle = createAnElement(dom, null, "div");
		topEle.appendChild(divEle);
		Element tableEle = createAnElement(dom, null, "table");
		divEle.appendChild(tableEle);
		Element trEle = createAnElement(dom, null, "tr");
		tableEle.appendChild(trEle);
		Element thEle = createAnElement(dom, null, "th");
		trEle.appendChild(thEle);
		thEle.setAttribute("colspan", "6");
		thEle.appendChild(dom.createTextNode("Initialize the system:"));

		trEle = createAnElement(dom, null, "tr");
		tableEle.appendChild(trEle);
		Element tdEle = createAnElement(dom, null, "td");
		trEle.appendChild(tdEle);
		tdEle.setAttribute("align", "right");
		tdEle.appendChild(dom.createTextNode("Number of Hosts: "));
		tdEle = createAnElement(dom, null, "td");
		trEle.appendChild(tdEle);
		tdEle.appendChild(createAnElement(dom, getMap("style=width:42px;&name=ms.1.1.numHosts&type=text&class=in&value=10"), "input"));
		
		tdEle = createAnElement(dom, null, "td");
		trEle.appendChild(tdEle);
		tdEle.setAttribute("align", "right");
		tdEle.appendChild(dom.createTextNode("Host name:"));
		tdEle = createAnElement(dom, null, "td");
		trEle.appendChild(tdEle);
		tdEle.appendChild(createAnElement(dom, getMap("style=width:42px;&class=in&name=ms.1.1.hostName&type=text&value=hany"), "input"));
		
		
		tdEle = createAnElement(dom, null, "td");
		trEle.appendChild(tdEle);
		tdEle.setAttribute("align", "right");
		tdEle.appendChild(dom.createTextNode("Interval between batches:"));
		tdEle = createAnElement(dom, null, "td");
		trEle.appendChild(tdEle);
		tdEle.appendChild(createAnElement(dom, getMap("style=width:42px;&name=wk.1.interval&type=text&class=in&value=30"), "input"));
		

		tdEle = createAnElement(dom, null, "td");
		trEle.appendChild(tdEle);
		tdEle.setAttribute("align", "right");
		tdEle.appendChild(dom.createTextNode("NSCA Start:"));
		tdEle = createAnElement(dom, null, "td");
		trEle.appendChild(tdEle);
		tdEle.appendChild(createAnElement(dom, getMap("style=width:42px;&name=ms.1.1.nscaStart&type=text&class=in&value=y"), "input"));
		
		
		
		
		trEle = createAnElement(dom, null, "tr");
		tableEle.appendChild(trEle);
		tdEle = createAnElement(dom, null, "td");
		trEle.appendChild(tdEle);
		tdEle.setAttribute("align", "right");
		tdEle.appendChild(dom.createTextNode("Number of Services: "));
		tdEle = createAnElement(dom, null, "td");
		trEle.appendChild(tdEle);
		tdEle.appendChild(createAnElement(dom, getMap("style=width:42px;&name=ms.1.1.numServices&type=text&class=in&value=20"), "input"));
		
		
		tdEle = createAnElement(dom, null, "td");
		trEle.appendChild(tdEle);
		tdEle.setAttribute("align", "right");
		tdEle.appendChild(dom.createTextNode("Service Name:"));
		tdEle = createAnElement(dom, null, "td");
		trEle.appendChild(tdEle);
		tdEle.appendChild(createAnElement(dom, getMap("style=width:42px;&name=ms.1.1.serviceName&type=text&class=in&value=sany"), "input"));

		
		tdEle = createAnElement(dom, null, "td");
		trEle.appendChild(tdEle);
		tdEle.setAttribute("align", "right");
		tdEle.appendChild(dom.createTextNode("Number of batches:"));
		tdEle = createAnElement(dom, null, "td");
		trEle.appendChild(tdEle);
		tdEle.appendChild(createAnElement(dom, getMap("style=width:42px;&name=wk.1.numBatches&type=text&class=in&value=1"), "input"));

		
		
		tdEle = createAnElement(dom, null, "td");
		trEle.appendChild(tdEle);
		tdEle.setAttribute("align", "right");
		tdEle.appendChild(dom.createTextNode("&#160;"));

		
		
		

		trEle = createAnElement(dom, null, "tr");
		tableEle.appendChild(trEle);
		tdEle = createAnElement(dom, null, "td");
		trEle.appendChild(tdEle);
		tdEle.setAttribute("align", "right");
		tdEle.appendChild(dom.createTextNode("IP Address: "));
		tdEle = createAnElement(dom, null, "td");
		trEle.appendChild(tdEle);
		tdEle.appendChild(createAnElement(dom, getMap("style=width:102px;&name=ms.1.1.ipAddress&type=text&class=in&value=100.100.100."), "input"));


		
		tdEle = createAnElement(dom, null, "td");
		trEle.appendChild(tdEle);
		tdEle.setAttribute("align", "right");
		tdEle.appendChild(dom.createTextNode("Toggle the Service Status in % (OK):"));
		tdEle = createAnElement(dom, null, "td");
		trEle.appendChild(tdEle);
		tdEle.appendChild(createAnElement(dom, getMap("style=width:42px;&class=in&name=ms.1.1.percentage&type=text&value=70"), "input"));


		tdEle = createAnElement(dom, null, "td");
		trEle.appendChild(tdEle);
		tdEle.setAttribute("align", "right");
		tdEle.appendChild(dom.createTextNode("Threshold: "));
		tdEle = createAnElement(dom, null, "td");
		trEle.appendChild(tdEle);
		tdEle.appendChild(createAnElement(dom, getMap("style=width:42px;&name=ms.1.1.threshold&type=text&class=in&value=5"), "input"));

		tdEle = createAnElement(dom, null, "td");
		trEle.appendChild(tdEle);
		tdEle.setAttribute("align", "right");
		tdEle.appendChild(dom.createTextNode("&#160;"));
		
		
		log.debug ("PConfig.createNSCAExtTable end");
		
		return topEle;
	}

	
	
	
	public Document createConfiguration (Map<String,String> tableHead, 
							Map<String,String> workLoadTemp, Map<String,Map<String,String>> wkmapmsTemp, String cmd, String fileName)
	throws javax.xml.parsers.ParserConfigurationException
	{
		log.debug("PConfig.createConfiguration============begin");
		DocumentBuilder docBuilder = null;
	    	docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document dom = docBuilder.newDocument();
		try
		{
			// Element root = createAnElement(dom, null, "html");
			// Element headEle = createAnElement(dom, null, "head");
			// root.appendChild(headEle);
			// --Element styleEle = createAnElement(dom, null, "style");
			// --org.w3c.dom.Text txtEle = dom.createTextNode(style);
			// --styleEle.appendChild(txtEle);
			// --headEle.appendChild(styleEle);
			// Element body = createAnElement(dom, null, "body");
			// root.appendChild(body);

			Element formEle = dom.createElement("form");
			formEle.setAttribute("method", "post");
			formEle.setAttribute("action", "index.jsp");
			Element topEle = createTableHeadRows(dom, tableHead, cmd, fileName);
			Element fieldsetEle = createAnElement(dom, null, "fieldset");
			topEle.appendChild(fieldsetEle);
			Element legendEle = createAnElement(dom, getMap("style=font-size:18px;"), "legend");
			fieldsetEle.appendChild(legendEle);
			legendEle.appendChild(dom.createTextNode("Workloads"));
			createSubTableRows(dom, fieldsetEle,  workLoadTemp, wkmapmsTemp);
			if (cmd.equals("simplesa")) createSimpleSATable(dom, fieldsetEle);
			else
			if (cmd.equals("nscaext")) createNSCAExtTable(dom, fieldsetEle);
			Element tableEle = createAnElement(dom, null, "table");
			tableEle.setAttribute("width", "100%");
			tableEle.setAttribute("align", "middle");
			Element trEle = createAnElement(dom, null, "tr");
			tableEle.appendChild(trEle);
			Element tdEle = createAnElement(dom, null, "td");
			tdEle.setAttribute("align", "middle");
			trEle.appendChild(tdEle);
			// tdEle.appendChild(createAnElement(dom,
			// getMap("id=cmd&type=reset&class=btn"), "input"));
			/*
			 * Element inputEle = createAnElement(dom,
			 * getMap("name=cmd&type=reset&class=btn"),"input");
			 * inputEle.setAttribute("onmouseover","this.className=\'btn
			 * btnhov\'");
			 * inputEle.setAttribute("onmouseout","this.className=\'btn\'");
			 * tdEle.appendChild(inputEle);
			 */
			
			tdEle = createAnElement(dom, null, "td");
			tdEle.setAttribute("align", "middle");
			trEle.appendChild(tdEle);
			// tdEle.appendChild(createAnElement(dom,
			// getMap("type=submit&name=cmd&class=btn&value=cleanDB"),
			// "input"));
			/*
			 * inputEle = createAnElement(dom,
			 * getMap("name=cmd&type=submit&class=btn&value=cleanDB"),"input");
			 * inputEle.setAttribute("onmouseover","this.className=\'btn
			 * btnhov\'");
			 * inputEle.setAttribute("onmouseout","this.className=\'btn\'");
			 * tdEle.appendChild(inputEle);
			 */
			
			// tdEle = createAnElement(dom, null, "td");
			// tdEle.setAttribute("align", "middle");
			// trEle.appendChild(tdEle);
			// tdEle.appendChild(createAnElement(dom,
			// getMap("class=btn&type=submit&name=cmd&value=execute"),
			// "input"));
			// tdEle = createAnElement(dom, null, "td");
			// tdEle.setAttribute("align", "middle");
			// trEle.appendChild(tdEle);
			// tdEle.appendChild(createAnElement(dom,
			// getMap("id=cmd&type=submit&name=cmd&value=change"), "input"));
			// tdEle = createAnElement(dom, null, "td");
			// tdEle.setAttribute("align", "middle");
			// trEle.appendChild(tdEle);
			// tdEle.appendChild(createAnElement(dom,
			// getMap("class=btn&type=submit&name=cmd&value=delete"), "input"));
			if (!cmd.equals("simplesa") && !cmd.equals("nscaext"))
			{
				tdEle = createAnElement(dom, null, "td");
				tdEle.setAttribute("align", "middle");
				trEle.appendChild(tdEle);
				// tdEle.appendChild(createAnElement(dom,
				// getMap("type=submit&class=btn&name=cmd&value=addwk"),
				// "input"));
				Element inputEle = createAnElement(dom, getMap("name=cmd&type=submit&class=btn&value=addwk"),"input");
				inputEle.setAttribute("onmouseover","this.className=\'btn btnhov\'");
				inputEle.setAttribute("onmouseout","this.className=\'btn\'");
				tdEle.appendChild(inputEle);
				
			
				tdEle = createAnElement(dom, null, "td");
				tdEle.setAttribute("align", "middle");
				trEle.appendChild(tdEle);
				// tdEle.appendChild(createAnElement(dom,
				// getMap("type=submit&class=btn&name=cmd&value=addmsg"),
				// "input"));

				inputEle = createAnElement(dom, getMap("name=cmd&type=submit&class=btn&value=addmsg"),"input");
				inputEle.setAttribute("onmouseover","this.className=\'btn btnhov\'");
				inputEle.setAttribute("onmouseout","this.className=\'btn\'");
				tdEle.appendChild(inputEle);
			}
			// tdEle = createAnElement(dom, null, "td");
			// tdEle.setAttribute("align", "middle");
			// trEle.appendChild(tdEle);
			// tdEle.appendChild(createAnElement(dom,
			// getMap("id=cmd&type=submit&name=cmd&value=new"), "input"));
			fieldsetEle.appendChild(tableEle);
			formEle.appendChild(topEle);
			// body.appendChild(formEle);
			// dom.appendChild(root);
			// if (cmd.equals("simplesa")) createHiddenSimpleSAElements(dom,
			// formEle);
			if (cmd.equals("simplesa")) formEle.appendChild(createAnElement(dom, getMap("name=cmdtype&type=hidden&value=simplesa"), "input"));
			if (cmd.equals("nscaext")) formEle.appendChild(createAnElement(dom, getMap("name=cmdtype&type=hidden&value=nscaext"), "input"));
			dom.appendChild(formEle);
		}
		catch (Exception e) {log.error("createConfiguration="+e);log.debug("createConfiguration="+e);} 
		log.debug("PConfig.createConfiguration============end");
		return dom;
	}

	// ========================================================================================
	
	
	public Document createConfiguration (String hosts, String services, String hostName, String ipAddress, String serviceName, String nscaStart)
	throws javax.xml.parsers.ParserConfigurationException
	{
		DocumentBuilder docBuilder = null;
		docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document dom = docBuilder.newDocument();
		try
		{
			Element formEle = dom.createElement("form");
			formEle.setAttribute("method", "post");
			formEle.setAttribute("action", "index.jsp");
			//Element topEle = createTableHeadRows(dom, tableHead, cmd, fileName);
			
			Element divEle = createAnElement(dom, getMap("class=header1"), "div");
			Element tableEle = createNavigationHeadingTable(dom);
			divEle.appendChild(tableEle);
			divEle.appendChild(createAnElement(dom, null, "br"));
			divEle.appendChild(createAnElement(dom, null, "br"));
			Element fieldsetEle = createAnElement(dom, null, "fieldset");
			formEle.appendChild(divEle);
			
			divEle.appendChild(fieldsetEle);
			Element legendEle = createAnElement(dom, null, "legend");
			legendEle.setAttribute("style", "font-size:18px");
			legendEle.appendChild(dom.createTextNode("Test NSCA Extension"));
			fieldsetEle.appendChild(legendEle);
			tableEle = createAnElement(dom, null, "table");
			fieldsetEle.appendChild(tableEle);
			
			Element trEle = createAnElement(dom, null, "tr");
			tableEle.appendChild(trEle);
			Element tdEle = createAnElement(dom, null, "td");
			trEle.appendChild(tdEle);
			tdEle.appendChild(dom.createTextNode("Hosts"));
			tdEle = createAnElement(dom, null, "td");
			trEle.appendChild(tdEle);
			tdEle.appendChild(dom.createTextNode("Services"));
			tdEle = createAnElement(dom, null, "td");
			trEle.appendChild(tdEle);
			tdEle.appendChild(dom.createTextNode("Host Name"));
			tdEle = createAnElement(dom, null, "td");
			trEle.appendChild(tdEle);
			tdEle.appendChild(dom.createTextNode("IP Address"));
			tdEle = createAnElement(dom, null, "td");
			trEle.appendChild(tdEle);
			tdEle.appendChild(dom.createTextNode("Service Name"));
			tdEle = createAnElement(dom, null, "td");
			trEle.appendChild(tdEle);
			tdEle.appendChild(dom.createTextNode("Reload Nagios Config"));
			
			trEle = createAnElement(dom, null, "tr");
			tableEle.appendChild(trEle);
			tdEle = createAnElement(dom, null, "td");
			trEle.appendChild(tdEle);
			tdEle.appendChild(createAnElement(dom, getMap("name=hosts&value="+hosts+"&type=text&class=in&style=font-size: small;width: 52px;"),"input"));
			tdEle = createAnElement(dom, null, "td");
			trEle.appendChild(tdEle);
			tdEle.appendChild(createAnElement(dom, getMap("name=services&value="+services+"&type=text&class=in&style=font-size: small;width: 48px;"),"input"));
			tdEle = createAnElement(dom, null, "td");
			trEle.appendChild(tdEle);
			tdEle.appendChild(createAnElement(dom, getMap("name=hostName&value="+hostName+"&type=text&class=in&style=font-size: small;width: 160px;"),"input"));
			tdEle = createAnElement(dom, null, "td");
			trEle.appendChild(tdEle);
			tdEle.appendChild(createAnElement(dom, getMap("name=ipAddress&value="+ipAddress+"&type=text&class=in&style=font-size: small;width: 90px;"),"input"));
			tdEle = createAnElement(dom, null, "td");
			trEle.appendChild(tdEle);
			tdEle.appendChild(createAnElement(dom, getMap("name=serviceName&value="+serviceName+"&type=text&class=in&style=font-size: small;width: 160px;"),"input"));
			tdEle = createAnElement(dom, null, "td");
			trEle.appendChild(tdEle);
			tdEle.appendChild(createAnElement(dom, getMap("name=nscaStart&value="+nscaStart+"&type=text&class=in&value=y&style=font-size: small;width: 160px;"),"input"));
			trEle = createAnElement(dom, null, "tr");
			tableEle.appendChild(trEle);
			tdEle = createAnElement(dom, null, "td");
			trEle.appendChild(tdEle);
			tdEle.setAttribute("colspan","5");
			tdEle.setAttribute("align","center");
			
			Element inputEle = createAnElement(dom, getMap("name=cmd&type=submit&class=btn&value=TestNSCA"),"input");
			inputEle.setAttribute("onmouseover","this.className=\'btn btnhov\'");
			inputEle.setAttribute("onmouseout","this.className=\'btn\'");
			inputEle.setAttribute("style","width:68px");
			
			tdEle.appendChild(inputEle);
			dom.appendChild(formEle);
		}
		catch (Exception e) {log.error("PConfig.createConfiguration2="+e);} 
		return dom;
	}
	// =========================================================================================
	public String getConfiguration (String hosts, String services, String hostName, String ipAddress, String serviceName, String nscaStart)
	{
		try {return getXML (createConfiguration (hosts, services, hostName, ipAddress, serviceName, nscaStart));} catch (javax.xml.parsers.ParserConfigurationException e){log.debug("getConfiguration parsing error", e);}
		return "error: getConfiguration nsca-extension";
	}

	// =========================================================================================
	public String getConfiguration (String [] args, List <String[]> list, Object tg)
	throws javax.xml.parsers.ParserConfigurationException
	{
		this.tg = tg;
		return getConfiguration (args, list);
	}
	public String getConfiguration (String [] args, List <String[]> list)
	throws javax.xml.parsers.ParserConfigurationException
	{
		File fxmlDir = new File (xmlConfigDir);
		if (!fxmlDir.exists()) {
			fxmlDir.mkdirs();
		}
		// ====================================================================================
		Map<String,String> workLoadTemp = new HashMap<String,String>();
		Map<String,String> wkmsTemp = new HashMap<String,String>();
		Map<String,Map<String,String>> wkmapmsTemp = new HashMap<String,Map<String,String>>();
		Map<String,String> tableHead = new HashMap<String,String> ();
		// ======================================================================================================
		// String qstring =fP+"&"+fd+"&"+pd+"&"+sk+"&"+ng+"&"+
		// wk1+"&"+wk2+"&"+wk3+"&"+ms_1_1+"&"+ms_2_1+"&"+ms_3_1+"&"+ms_4_1+"&"+ms_1_2+"&"+ms_3_2+"&"+ms_2_3+"&"+ms_4_3;
		String qstring = "";

		String cmd = args[1];
		if (cmd.equals("nscaext"))
			ng = "ng.server="+ngserver+"&ng.port=5667";
		if (cmd.equals("newconfig") ||cmd.equals("simplesa") ||cmd.equals("nscaext")) {
			qstring =fP+"&"+fd+"&"+pd+"&"+sk+"&"+ng;
		} else {
			qstring = args[0];
		}
		// wk.p = workload number p
		// ms.n.m = message number n belonging to workload number m
		setHeaders (tableHead, qstring, "fP");
		setHeaders (tableHead, qstring, "fd");
		setHeaders (tableHead, qstring, "pd");
		setHeaders (tableHead, qstring, "sk");
		setHeaders (tableHead, qstring, "ng");
		// ==========================================
		workLoadTemp = setWkMsg (qstring, "wk", "wk", 1);
		wkmsTemp = setWkMsg (qstring, "ms", "wk", 2);

		for (Map.Entry<String, String> e : wkmsTemp.entrySet())
		{
			String wkl = e.getKey();
			String wklMsg = e.getValue();
			wkmapmsTemp.put(wkl, setWkMsg(wklMsg, "ms", "ms", 1)); 
		}

		// ===========================================================================
		if (cmd.equals("new") ||cmd.equals("save")) 
		{
			getXmlConfiguration (tableHead,workLoadTemp, wkmapmsTemp, xmlConfigDir+"/"+args[2]);
			return getXML (createConfiguration (tableHead, workLoadTemp, wkmapmsTemp, cmd, args[2]));
		}
		else
		if (cmd.equals("execute")) 
		{
			String xmlConfig = args[2];
			String config = "-config";
			String [] parameters = new String []{config, xmlConfigDir + "/" + xmlConfig};
			// args[0] = config;
			// args[1] = xmlConf;
			String error = "ok";
			try
			{
				error = org.groundwork.foundation.profiling.ProfileFoundation.startProfiler(parameters, tg);
			}
			catch (Exception e){ error = "error " +e.getMessage();}
			if (error.equals("ok"))
			{
				// Thread thisThread = Thread.currentThread();
				// try {thisThread.sleep(2000);} catch(Exception e) { error =
				// "errorInSleepingThread" + e.getMessage();}
					return "submitted";
			}
			return error;
		}
		else
		{
			if (cmd.equals("addwk")) {
				addNewWkld (wkmapmsTemp, workLoadTemp, workloadType);
			} else
			if (cmd.equals("addmsg"))
			{

				ListIterator<String[]>lit = list.listIterator();
				while (lit.hasNext())
				{
					String [] nv = lit.next();
					String wkIdx = nv[1];
					String messageType = messageTypes.get(nv[0]);
					addNewMsg (wkmapmsTemp, messageType, wkIdx);				
				}
			}
			return getXML (createConfiguration (tableHead, workLoadTemp, wkmapmsTemp, cmd, args[2]));
		}

	}


	public Document createXmlConfiguration (Map<String,String> tableHead, 
							Map<String,String> workLoadTemp, Map<String,Map<String,String>> wkmapmsTemp)
	throws javax.xml.parsers.ParserConfigurationException
	{
		DocumentBuilder docBuilder = null;
	    	docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document dom = docBuilder.newDocument();
		try
		{
			
			Element root = createAnElement(dom, getMap (tableHead.get("fP").replaceAll("fP.","")), "foundation-profiler");
			Element foundationDBEle = createAnElement(dom, getMap (tableHead.get("fd").replaceAll("fd.","")), "foundationDB");
			root.appendChild(foundationDBEle);
			Element profilerDBEle = createAnElement(dom, getMap (tableHead.get("pd").replaceAll("pd.","")), "profilerDB");
			root.appendChild(profilerDBEle);
			Element messageSocketEle = createAnElement(dom, getMap (tableHead.get("sk").replaceAll("sk.","")), "messageSocket");
			root.appendChild(messageSocketEle);

			Element workloadsEle = createAnElement(dom, null, "workloads");
			root.appendChild(workloadsEle);
			// for each workload add it to workloads
			// for each wkload in workloads get all messages.

			for (Map.Entry<String, Map<String,String>> e : wkmapmsTemp.entrySet())
			{
    				String wkld = e.getKey();
				String [] wkldPair = wkld.split("\\.");
				Element workloadEle = createAnElement(dom, getMap(workLoadTemp.get(wkld).replaceAll(wkld+".", "")), "workload");
				workloadsEle.appendChild(workloadEle);
				Element messagesEle = createAnElement(dom, null, "messages");
				workloadEle.appendChild(messagesEle);
				for (Map.Entry<String, String> m : e.getValue().entrySet())
				{
					Element messageEle = createAnElement(dom, getMap(m.getValue().replaceAll(m.getKey()+"."+wkldPair[1]+".", "")), "message");
					messagesEle.appendChild(messageEle);
				}
			}

			dom.appendChild(root);
		}
		catch (Exception e) {log.error("PConfig.createXmlConfiguration="+e);} 
		return dom;
	}



	public void getXmlConfiguration (Map<String,String> tableHead, 
							Map<String,String> workLoadTemp, Map<String,Map<String,String>> wkmapmsTemp, String xmlConfig)
	throws javax.xml.parsers.ParserConfigurationException
	{
		// String xmlConfig =
		// "/home/cpora/eclipse2/workspace/groundwork-professional/load-test-tools/profiler/deploy/mytest.xml";
		String xmlString = getXML (createXmlConfiguration (tableHead, workLoadTemp, wkmapmsTemp));
		writeToFile(xmlConfig, xmlString);

	}


	public void writeToFile(String outPutFile, String outPutString)
	{
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(outPutFile));
			out.write(outPutString);
			out.close();
		} catch (IOException e) { log.error("PConfig.Error writing to file: " + e);}
	} 

	// =======================================================================================



	public String deleteConfigElement(String [] deleteList, String qstring)
	{
		for (int i = 0; i<deleteList.length;i++)
		{
			String deleteElement = deleteList[i];
			// if deleteElement startsWith "wk" then delete all messages
			// belonging to that workload.
			// Example: wk.i ----> delete ms.x.i where x can be 1..n
			// This is the same as saying: delete all ms.x.i (all messages that
			// end with i).
			if (deleteElement.startsWith("wk"))
			{
				String [] nv = deleteElement.split("=");
				String [] elmnts = deleteElement.split("\\.");
				String suffix = elmnts[1];
				qstring = filterString (qstring, deleteElement);
				qstring = filterStringOnSuffix(qstring, suffix);
			} else {
				qstring = filterString (qstring, deleteElement);
			}
		 	
		}
		return qstring;
	}




	public String filterString (String qstring, String deleteElement)
	{
		String result = "";
		StringTokenizer token = new StringTokenizer(qstring, "&");
		while (token.hasMoreElements()) 
		{ 
			String elemnt = (String)token.nextElement(); 
			if (!elemnt.startsWith(deleteElement))
			{
				if (result.equals("")) {
					result = elemnt;
				} else {
					result = result + "&" + elemnt;
				}	
			} 
		} 
		return result;
	}

	public String filterStringOnSuffix (String qstring, String suffix)
	{
		String result = "";
		StringTokenizer token = new StringTokenizer(qstring, "&");
		while (token.hasMoreElements()) 
		{ 
			String elemnt = (String)token.nextElement();
			if (elemnt.startsWith("ms."))
			{
				String [] nv = elemnt.split("=");
				String [] elemnts = nv[0].split("\\.");
				if (!elemnts[2].equals(suffix))
				{
					if (result.equals("")) {
						result = elemnt;
					} else {
						result = result + "&" + elemnt;
					}	
				}
			}
			else
			{
				if (result.equals("")) {
					result = elemnt;
				} else {
					result = result + "&" + elemnt;
				}	
			} 
		} 
		return result;
	}

	public static String [] getXMLFiles(String directoryName)
	{
		File dir = new File(directoryName);

    		// It is also possible to filter the list of returned files.
    		// This example does not return any files that start with `.'.
    		FilenameFilter filter = new FilenameFilter() 
					{
        					public boolean accept(File dir, String name) 
						{
            						return name.endsWith("xml");
        					}
    					};
    		return dir.list(filter);
    	}
	// =======================================================================================

}
