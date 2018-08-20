package org.groundwork.foundation.profiling;

import java.io.StringWriter;
import java.util.Random;

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
import org.w3c.dom.NodeList;

public class SystemAdminUtil {
	public SystemAdminUtil() {

	}
	// Log
	protected static Log log = LogFactory.getLog(SystemAdminUtil.class);
	
	public String getXML(Document dom) {
		String xmlString = "";
		try {
			// Output the XML

			// set up a transformer
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

		} catch (Exception e) {
			System.out.println("" + e);
		}
		return xmlString;
	}

	// ===========================================================================================================================
	// ============================================ ServiceStatus
	// ================================================================
	public void serviceStatusCommand(Document dom, Element cmdEle,
			String hostName, String deviceName, String sDescription,
			String checkType, String time, int numOfHosts,
			int numOfReqServices, int h, int s, int p) {
		String monitorStatus = "UP";
		if (h > numOfHosts) {
			h = 1;
		}
		if (s <= numOfReqServices) {

			if (isThis(p, numOfReqServices, s)) {
				monitorStatus = "UP";
			} else {
				monitorStatus = "DOWN";
			}
			Element subEle = dom.createElement("Service");
			subEle.setAttribute("MonitorServerName", "localhost");
			subEle.setAttribute("Host", hostName + h);
			subEle.setAttribute("Device", deviceName + h);
			subEle.setAttribute("ServiceDescription", sDescription + s);
			subEle.setAttribute("CheckType", checkType);
			subEle.setAttribute("CurrentNotificationNumber", "0");
			subEle.setAttribute("ExecutionTime", "6");
			subEle.setAttribute("LastCheckTime", time);
			subEle.setAttribute("LastHardState", monitorStatus);
			subEle.setAttribute("LastNotificationTime", "0");
			subEle.setAttribute("LastPluginOutput",
					"Profiler Service Status Toggle");
			subEle.setAttribute("LastStateChange", time);
			subEle.setAttribute("Latency", "321");
			subEle.setAttribute("MonitorStatus", monitorStatus);
			subEle.setAttribute("NextCheckTime", time);
			subEle.setAttribute("PercentStateChange", "0.00");
			subEle.setAttribute("RetryNumber", "1");
			subEle.setAttribute("ScheduledDowntimeDepth", "0");
			subEle.setAttribute("StateType", "HARD");
			subEle.setAttribute("TimeCritical", "0");
			subEle.setAttribute("TimeOK", "0");
			subEle.setAttribute("TimeUnknown", "0");
			subEle.setAttribute("TimeWarning", "0");
			subEle.setAttribute("isAcceptPassiveChecks", "1");
			subEle.setAttribute("isChecksEnabled", "1");
			subEle.setAttribute("isEventHandlersEnabled", "1");
			subEle.setAttribute("isFailurePredictionEnabled", "1");
			subEle.setAttribute("isFlapDetectionEnabled", "1");
			subEle.setAttribute("isNotificationsEnabled", "1");
			subEle.setAttribute("isObsessOverService", "1");
			subEle.setAttribute("isProblemAcknowledged", "0");
			subEle.setAttribute("isProcessPerformanceData", "1");
			subEle.setAttribute("isServiceFlapping", "0");
			cmdEle.appendChild(subEle);

			serviceStatusCommand(dom, cmdEle, hostName, deviceName,
					sDescription, checkType, time, numOfHosts,
					numOfReqServices, h + 1, s + 1, p);
		}
		;
	}

	public Element serviceStatusCommands(Document dom, String applicationType,
			String hostName, String deviceName, String sDescription,
			String checkType, String time, int numOfHosts,
			int numOfReqServices, int p) {
		Element cmdEle = null;
		Element subEle = null;
		cmdEle = dom.createElement("Command");
		cmdEle.setAttribute("Action", "MODIFY");
		cmdEle.setAttribute("ApplicationType", applicationType);

		serviceStatusCommand(dom, cmdEle, hostName, deviceName, sDescription,
				checkType, time, numOfHosts, numOfReqServices, 1, 1, p);

		// ===========================================================================
		/*
		 * for (int j = 1; j<=numOfServices; j++) { for (int i = 1; i<=numOfHosts;i++) {
		 * if (k <= numOfReqServices) { if (isThis(p, numOfServices, i))
		 * monitorStatus = "UP"; else monitorStatus = "DOWN";
		 * serviceStatusCommand (dom, cmdEle, hostName+i, deviceName+i,
		 * sDescription+j, monitorStatus, time); k++; } } }
		 */
		// ===========================================================================
		// root.appendChild(cmdEle);
		return cmdEle;
	}

	public Document setServiceStatusCommand(String applicationType,
			String session, String adapterType, String hostName,
			String deviceName, String sDescription, String checkType,
			String time, int numOfHosts, int numOfReqServices, int p)
			throws javax.xml.parsers.ParserConfigurationException {
		DocumentBuilder docBuilder = null;

		docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document dom = docBuilder.newDocument();
		Element root = dom.createElement("Adapter");

		root.setAttribute("Session", session);
		root.setAttribute("AdapterType", adapterType);
		dom.appendChild(root);
		root.appendChild(serviceStatusCommands(dom, applicationType, hostName,
				deviceName, sDescription, checkType, time, numOfHosts,
				numOfReqServices, p));
		return dom;
	}

	public String getServiceStatusCommand(String session, String adapterType,
			String applicationType, String hostName, String deviceName,
			String sDescription, String checkType, String time, int numOfHosts,
			int numOfReqServices, int p)
			throws javax.xml.parsers.ParserConfigurationException {
		return getXML(setServiceStatusCommand(applicationType, session,
				adapterType, hostName, deviceName, sDescription, checkType,
				time, numOfHosts, numOfReqServices, p));
	}

	// ===========================================================================================================================
	// =================================================== HostStatus
	// ============================================================

	public Element hostStatusCommand(Document dom, String applicationType,
			String hostName, String deviceName, String time1, int numOfHosts,
			int p) {
		String monitorStatus = "UP";
		Element cmdEle = null;
		Element subEle = null;
		cmdEle = dom.createElement("Command");
		cmdEle.setAttribute("Action", "MODIFY");
		cmdEle.setAttribute("ApplicationType", applicationType);
		for (int i = 1; i <= numOfHosts; i++) {
			if (isThis(p, numOfHosts, i)) {
				monitorStatus = "UP";
			} else {
				monitorStatus = "DOWN";
			}
			subEle = dom.createElement("HOST");

			subEle.setAttribute("MonitorServerName", "localhost");
			subEle.setAttribute("Host", hostName + i);
			subEle.setAttribute("Device", deviceName + i);
			subEle.setAttribute("CheckType", "ACTIVE");
			subEle.setAttribute("CurrentNotificationNumber", "0");
			subEle.setAttribute("ExecutionTime", "0.069000");
			subEle.setAttribute("LastCheckTime", time1);
			subEle.setAttribute("Latency", "0.000000");
			subEle.setAttribute("LastNotificationTime", "0");
			subEle.setAttribute("LastPluginOutput", "OK - 127.0.0.1:");
			subEle.setAttribute("LastStateChange", time1);
			subEle.setAttribute("MonitorStatus", monitorStatus);
			subEle.setAttribute("PercentStateChange", "0.000000");
			subEle.setAttribute("ScheduledDowntimeDepth", "0");
			subEle.setAttribute("TimeDown", "0");
			subEle.setAttribute("TimeUnreachable", "0");
			subEle.setAttribute("TimeUp", "1201052522");
			subEle.setAttribute("isAcknowledged", "0");
			subEle.setAttribute("isChecksEnabled", "1");
			subEle.setAttribute("isEventHandlersEnabled", "1");
			subEle.setAttribute("isFailurePredictionEnabled", "1");
			subEle.setAttribute("isFlapDetectionEnabled", "1");
			subEle.setAttribute("isHostFlapping", "0");
			subEle.setAttribute("isNotificationsEnabled", "1");
			subEle.setAttribute("isPassiveChecksEnabled", "1");
			subEle.setAttribute("isProcessPerformanceData", "1");
			cmdEle.appendChild(subEle);
		}
		// root.appendChild(cmdEle);
		return cmdEle;
	}

	public Document setHostStatusCommand(String applicationType,
			String session, String adapterType, String hostName,
			String deviceName, String time1, int numOfHosts, int p)
			throws javax.xml.parsers.ParserConfigurationException {
		DocumentBuilder docBuilder = null;

		docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document dom = docBuilder.newDocument();
		Element root = dom.createElement("Adapter");

		root.setAttribute("Session", session);
		root.setAttribute("AdapterType", adapterType);
		dom.appendChild(root);
		root.appendChild(hostStatusCommand(dom, applicationType, hostName,
				deviceName, time1, numOfHosts, p));
		return dom;
	}

	public String getHostStatusCommand(String session, String adapterType,
			String applicationType, String hostName, String deviceName,
			String time1, int numOfHosts, int p)
			throws javax.xml.parsers.ParserConfigurationException {
		return getXML(setHostStatusCommand(applicationType, session,
				adapterType, hostName, deviceName, time1, numOfHosts, p));
	}

	// ===========================================================================================================================
	/*
	 * public boolean isThis(int p, int n, int i) { int q = p*n/100; int r = 0;
	 * 
	 * boolean highProcentage = n/2 < q; if (highProcentage) r = n / (n - q);
	 * else r = n/q;
	 * 
	 * if (highProcentage) { if (i%r!=0)return true; } else { if (i%r==0)return
	 * true; } return false; }
	 */
	public boolean isThis(int p, int n, int i) {
		double pd = p;
		double nd = n;
		double id = i;
		double q = pd * nd / 100;
		double r = 0.00;

		boolean highProcentage = nd / 2 < q;

		if (highProcentage) {
			r = nd / (nd - pd * nd / 100);
		} else {
			r = 100 / pd;
		}
		int f = new Double(id % r).intValue();

		if (highProcentage) {
			if (f != 0) {
				return true;
			}
		} else {
			if (f == 0) {
				return true;
			}
		}
		return false;
	}

	//
	//	
	/*
	 * o - okPrecentage, w - warningPrecentage, u - upPercentage, c -
	 * criticalPercentage, d - downPercentage, h - hostsPercentage
	 * 
	 */
	public Element logMessageCommand(Document dom, String applicationType,
			String hostName, String deviceName, String sDescription,
			String txtMessg, String time, int cs, int h, int n, int w, int u,
			int c, int d) {
		Element cmdEle = null;
		msgs = 0;
		cmdEle = dom.createElement("Command");
		cmdEle.setAttribute("Action", "ADD");
		cmdEle.setAttribute("ApplicationType", applicationType);

		if (u > 0) {
			logMCommand(dom, cmdEle, applicationType, hostName, deviceName,
					sDescription, "OK", "UP", txtMessg, time, cs, h, n, u);
		}
		if (w > 0) {
			logMCommand(dom, cmdEle, applicationType, hostName, deviceName,
					sDescription, "WARNING", "WARNING", txtMessg, time, cs, h,
					n, w);
		}
		if (c > 0) {
			logMCommand(dom, cmdEle, applicationType, hostName, deviceName,
					sDescription, "CRITICAL", "CRITICAL", txtMessg, time, cs,
					h, n, c);
		}
		if (d > 0) {
			logMCommand(dom, cmdEle, applicationType, hostName, deviceName,
					sDescription, "DOWN", "DOWN", txtMessg, time, cs, h, n, d);
		}
		/*
		 * for (int i = 1; i<=n; i++) {
		 * 
		 * if (isThis(o, n , i)) {severity = "OK"; monitorStatus = "OK";} else
		 * if (isThis(w, n , i)) {severity = "WARNING"; monitorStatus =
		 * "WARNING";} else if (isThis(u, n , i)) {severity = "OK";
		 * monitorStatus = "UP";} else if (isThis(c, n , i)) {severity =
		 * "CRITICAL"; monitorStatus = "CRITICAL";} else if (isThis(d, n , i))
		 * {severity = "DOWN"; monitorStatus = "DOWN";} else {severity = status;
		 * monitorStatus = status;}
		 * 
		 * subEle = dom.createElement("LogMessage"); // if errorType == HOST
		 * ALERT then status: Severity=OK, MonitorStatus=UP // else // Severity =
		 * MonitorStatus (OK or CRITICAL or WARNING) // if
		 * (errorType.equals("HOST ALERT")) { subEle.setAttribute("Severity",
		 * severity); subEle.setAttribute("MonitorStatus", monitorStatus);
		 * subEle.setAttribute("SubComponent", hostName+i);
		 * subEle.setAttribute("Host", hostName+i);
		 * subEle.setAttribute("Device", deviceName+i); } else //
		 * errorType.equals ("SERVICE ALERT") { subEle.setAttribute("Severity",
		 * severity); subEle.setAttribute("MonitorStatus", monitorStatus);
		 * subEle.setAttribute("ServiceDescription", sDescription+i);
		 * subEle.setAttribute("SubCompornd.nextInt(100)nent",
		 * hostName+":"+sDescription+i); subEle.setAttribute("Host", hostName);
		 * subEle.setAttribute("Device", deviceName); } // if there is no
		 * consolidation do nothing (don't add an attribute consolidation). if
		 * (!consolidation.equals("no")) subEle.setAttribute("consolidation",
		 * consolidation); subEle.setAttribute("ErrorType", errorType);
		 * subEle.setAttribute("LastInsertDate", "");
		 * subEle.setAttribute("ReportDate", time);
		 * subEle.setAttribute("TextMessage", txtMessg);
		 * subEle.setAttribute("MonitorServerName", "localhost");
		 * cmdEle.appendChild(subEle); }
		 */
		// root.appendChild(cmdEle);
		if (msgs < n) {
			int diff = n - msgs;
			
			for (int i = 1; i <= diff; i++) {
				cmdEle.appendChild(mkSubElement(dom, hostName, deviceName,
						sDescription, "OK", "UP", txtMessg, "SERVICE ALERT",
						time, h, msgs + i));
			}
		}

		return cmdEle;
	}

	public Element mkSubElement(Document dom, String hostName,
			String deviceName, String sDescription, String severity,
			String monitorStatus, String txtMessg, String errorType,
			String time, int h, int i) {
		Random rnd = new Random();
		Element subEle = dom.createElement("LogMessage");
		// if errorType == HOST ALERT then status: Severity=OK,
		// MonitorStatus=UP
		// else
		// Severity = MonitorStatus (OK or CRITICAL or WARNING)
		//
		
		if (errorType.equals("HOST ALERT")) {
			subEle.setAttribute("Severity", severity);
			subEle.setAttribute("MonitorStatus", monitorStatus);
			subEle.setAttribute("SubComponent", hostName + i);
			subEle.setAttribute("Host", hostName + i);
			subEle.setAttribute("Device", deviceName + i);
			subEle.setAttribute("ErrorType", "HOST ALERT");
		} else // errorType.equals ("SERVICE ALERT")
		{
			int rd = rnd.nextInt(h);
			subEle.setAttribute("Severity", severity);
			subEle.setAttribute("MonitorStatus", severity);
			subEle.setAttribute("ServiceDescription", sDescription + rd);
			subEle.setAttribute("ErrorType", "SERVICE ALERT");
			subEle.setAttribute("SubComponent", hostName + ":" + sDescription + rd);
			subEle.setAttribute("Host", hostName + i);
			subEle.setAttribute("Device", deviceName + i);
		}
		// if there is no consolidation do nothing (don't add an
		// attribute consolidation).
		// if (!consolidation.equals("no")) {
		// subEle.setAttribute("consolidation", "NAGIOSEVENT");
		// }

		subEle.setAttribute("LastInsertDate", "");
		subEle.setAttribute("ReportDate", time);
		subEle.setAttribute("TextMessage", txtMessg);
		subEle.setAttribute("MonitorServerName", "localhost");
		return subEle;

	}

	public Element logMCommand(Document dom, Element cmdEle,
			String applicationType, String hostName, String deviceName,
			String sDescription, String severity, String monitorStatus,
			String txtMessg, String time, int cs, int h, int n, int p) {
		Random rnd = new Random();
		Element subEle = null;
		for (int i = 1; i <= n; i++) {
			String errorType = "";

			if (isThis(p, n, i)) {
				// int v = i < n - 2 ? i + 2 : i / 2;
				if (isThis(h, n, i)) {
					errorType = "HOST ALERT";
				} else {
					errorType = "SERVICE ALERT";
				}

				subEle = mkSubElement(dom, hostName, deviceName, sDescription,
												severity, monitorStatus, txtMessg, errorType, time, h, i);
				cmdEle.appendChild(subEle);
				msgs++;
			}
		}
		return cmdEle;
	}

	static int msgs = 0;

	/*
	 * public Element logMCommand(Document dom, Element cmdEle, String
	 * applicationType, String hostName, String deviceName, String sDescription,
	 * String severity, String monitorStatus, String txtMessg, String time, int
	 * cs, int h, int n, int p) { Random rnd = new Random(); Element subEle =
	 * null; for (int i = 1; i <= n; i++) { String errorType = ""; String
	 * consolidation = "no";
	 * 
	 * if (isThis(cs, n, i)) { consolidation = "yes"; } else { consolidation =
	 * "no"; }
	 * 
	 * if (isThis(p, n, i)) { int v = i < n - 2 ? i + 2 : i / 2; if (isThis(h,
	 * n, i)) { errorType = "HOST ALERT"; } else { errorType = "SERVICE ALERT"; }
	 * 
	 * 
	 * 
	 * subEle = dom.createElement("LogMessage"); // if errorType == HOST ALERT
	 * then status: Severity=OK, // MonitorStatus=UP // else // Severity =
	 * MonitorStatus (OK or CRITICAL or WARNING) // if (errorType.equals("HOST
	 * ALERT")) { subEle.setAttribute("Severity", severity);
	 * subEle.setAttribute("MonitorStatus", monitorStatus);
	 * subEle.setAttribute("SubComponent", hostName + i);
	 * subEle.setAttribute("Host", hostName + i); subEle.setAttribute("Device",
	 * deviceName + i); subEle.setAttribute("ErrorType", "HOST ALERT"); } else //
	 * errorType.equals ("SERVICE ALERT") { subEle.setAttribute("Severity",
	 * severity); subEle.setAttribute("MonitorStatus", severity);
	 * subEle.setAttribute("ServiceDescription", sDescription + i);
	 * subEle.setAttribute("ErrorType", "SERVICE ALERT");
	 * subEle.setAttribute("SubComponent", hostName + ":" + sDescription + i);
	 * int rd = rnd.nextInt(h); subEle.setAttribute("Host", hostName + rd);
	 * subEle.setAttribute("Device", deviceName + rd); } // if there is no
	 * consolidation do nothing (don't add an // attribute consolidation). if
	 * (!consolidation.equals("no")) { subEle.setAttribute("consolidation",
	 * "NAGIOSEVENT"); }
	 * 
	 * subEle.setAttribute("LastInsertDate", "");
	 * subEle.setAttribute("ReportDate", time);
	 * subEle.setAttribute("TextMessage", txtMessg);
	 * subEle.setAttribute("MonitorServerName", "localhost");
	 * cmdEle.appendChild(subEle); } } return cmdEle; }
	 */
	public Document setLogMessageCommand(String applicationType,
			String session, String adapterType, String hostName,
			String deviceName, String sDescription, String txtMessg,
			String time, int cs, int h, int n, int w, int u, int c, int d)

	throws javax.xml.parsers.ParserConfigurationException {
		DocumentBuilder docBuilder = null;

		docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document dom = docBuilder.newDocument();
		Element root = dom.createElement("Adapter");

		root.setAttribute("Session", session);
		root.setAttribute("AdapterType", adapterType);
		dom.appendChild(root);

		root
				.appendChild(logMessageCommand(dom, applicationType, hostName,
						deviceName, sDescription, txtMessg, time, cs, h, n, w,
						u, c, d));
		return dom;
	}

	public String getLogMessageCommand(String session, String adapterType,
			String applicationType, String hostName, String deviceName,
			String sDescription, String txtMessg, String time, int cs,
			// int h,
			int n
	// , int w, int u, int c, int d
	) throws javax.xml.parsers.ParserConfigurationException {
		Random rnd = new Random();
		int h = rnd.nextInt(100);
		int w = rnd.nextInt(50);
		int u = 50 - w;
		int c = rnd.nextInt(50);
		int d = 50 - c;
		Document logMessagesCMD = setLogMessageCommand(applicationType,
				session, adapterType, hostName, deviceName, sDescription,
				txtMessg, time, cs, h, n, w, u, c, d);
		NodeList logMessages = logMessagesCMD
				.getElementsByTagName("LogMessage");
		int msgLength = logMessages.getLength();
		int cns = cs * msgLength / 100;
		for (int i = 0; i < cns; i++) {
			Element logMsg = (Element) logMessages.item(i);
			logMsg.setAttribute("consolidation", "NAGIOSEVENT");
		}
		return getXML(logMessagesCMD);
	}

	// ================================================ Adapter Initialize
	// System (initSystem) =====================================
	//
	// Initially create a given number of Hosts:
	// <Adapter Session="1" AdapterType="SystemAdmin">
	// <Command Action='ADD' ApplicationType='NAGIOS'>
	// <Host Host='localhost' Description='localhost' Device='localhost'
	// DisplayName='localhost' />
	// <Host Host='random_counter_host_00' Description='data044'
	// Device='127.0.0.1' DisplayName='random_counter_host_00' />
	// <Host Host='random_counter_host_01' Description='data045'
	// Device='127.0.0.1' DisplayName='random_counter_host_01' />
	// <Host Host='random_counter_host_02' Description='data046'
	// Device='127.0.0.1' DisplayName='random_counter_host_02' />
	// <Host Host='random_counter_host_03' Description='data047'
	// Device='127.0.0.1' DisplayName='random_counter_host_03' />
	// <Host Host='random_counter_host_04' Description='wxyz010'
	// Device='192.168.2.1' DisplayName='random_counter_host_04' />
	// ....................................................................................................................
	// </Command>
	//
	// </Adapter>
	//
	// for each Host create a given amount of services:
	//
	// <Adapter Session="2" AdapterType="SystemAdmin">
	// <Command Action='ADD' ApplicationType='NAGIOS'>
	// <Service Host='localhost' ServiceDescription='Current Load'
	// CheckType='PASSIVE' StateType='SOFT'
	// MonitorStatus='PENDING' LastHardState='PENDING'/>
	// <Service Host='localhost' ServiceDescription='Current Users'
	// CheckType='PASSIVE' StateType='SOFT'
	// MonitorStatus='PENDING' LastHardState='PENDING'/>
	// <Service Host='localhost' ServiceDescription='icmp_ping'
	// CheckType='PASSIVE' StateType='SOFT'
	// MonitorStatus='PENDING' LastHardState='PENDING'/>
	// <Service Host='localhost' ServiceDescription='nagios_latency'
	// CheckType='PASSIVE' StateType='SOFT'
	// MonitorStatus='PENDING' LastHardState='PENDING'/>
	// <Service Host='localhost' ServiceDescription='Root Partition'
	// CheckType='PASSIVE' StateType='SOFT'
	// MonitorStatus='PENDING' LastHardState='PENDING'/>
	// <Service Host='random_counter_host_00'
	// ServiceDescription='timed_dummy_random_0' CheckType='PASSI, String
	// errorType, String statusVE' StateType='SOFT'
	// MonitorStatus='PENDING' LastHardState='PENDING'/>
	// <Service Host='random_counter_host_00'
	// ServiceDescription='timed_dummy_random_1' CheckType='PASSIVE'
	// StateType='SOFT'
	// MonitorStatus='PENDING' LastHardState='PENDING'/>
	// <Service Host='random_counter_host_00'
	// ServiceDescription='timed_dummy_random_2' CheckType='PASSIVE'
	// StateType='SOFT'
	// MonitorStatus='PENDING' LastHardState='PENDING'/>
	// ....................................................................................................................
	// </Command>
	// </Adapter>
	//
	// input: n - number of hosts
	// m - number of services/host
	// rNH - root name of host -> rNH0001 through rNH000n
	// rNHD - root name of host description -> rNHD0001 through rNHD000n
	// rNHDN - root name of host display name -> rNHDN = rNH
	// rND - root name of device -> rND0001 through rND000n
	// rNSD - root name of ServiceDescription -> rNSD0001 through rNSD000m
	//
	// Formula for considering the values of n: if the test creates x number of
	// workloads, n should be a multiple of x.
	// Thus, our algorithm looks like this:
	// initSystem (sessionID, adapterType, applicationType, n, m, rNH, rNHD,
	// rND, rNSD)
	// begin
	// /* initialize each host */
	// for (int i=1; i<=n;i++)
	// begin
	// setDom();
	// setRoot (sessionID, adapterType);
	// createHostCommand(applicationType, rNH+i, rND+i, rNHD+i, rNH+i);
	// end
	//
	// createHostGroupCommand(applicationType, hostGroupName);
	// createModifyHostGroupCommand(applicationType, hostGroupName, hostName,
	// n);
	// /* for each host initialize m services */
	// for (int i=1; i<=n;i++)
	// begin
	// for (int j=1; j<=m;j++)
	// begin
	// createServiceCommand(applicationType, rNH+j, rNSD+j);
	// end
	// end
	// end
	//	
	//	

	public Element createHostCommand(Document dom, String applicationType,
			String hostName, String hDescription, String device,
			String displayName, int n, String lastStateChange) {
		Element cmdEle = null;
		Element subEle = null;

		cmdEle = dom.createElement("Command");
		cmdEle.setAttribute("Action", "ADD");
		cmdEle.setAttribute("ApplicationType", applicationType);
		for (int i = 1; i <= n; i++) {
			subEle = dom.createElement("Host");
			subEle.setAttribute("Host", hostName + i);
			subEle.setAttribute("Description", hDescription + i);
			subEle.setAttribute("Device", device + i);
			subEle.setAttribute("DisplayName", displayName + i);
			subEle.setAttribute("LastStateChange", lastStateChange);
			cmdEle.appendChild(subEle);
		}
		return cmdEle;
	}

	public Element createHostGroupCommand(Document dom, String applicationType,
			String hostGroupName) {
		Element cmdEle = null;
		Element subEle = null;
		cmdEle = dom.createElement("Command");
		cmdEle.setAttribute("Action", "ADD");
		cmdEle.setAttribute("ApplicationType", applicationType);
		subEle = dom.createElement("HostGroup");
		subEle.setAttribute("HostGroup", hostGroupName);
		cmdEle.appendChild(subEle);
		return cmdEle;
	}

	public Element createModifyHostGroupCommand(Document dom,
			String applicationType, String hostGroupName, String hostName, int n) {
		Element cmdEle = null;
		Element subEle = null;
		Element subsubEle = null;
		cmdEle = dom.createElement("Command");
		cmdEle.setAttribute("Action", "MODIFY");
		cmdEle.setAttribute("ApplicationType", applicationType);
		subEle = dom.createElement("HostGroup");
		subEle.setAttribute("HostGroup", hostGroupName);
		for (int i = 1; i <= n; i++) {
			subsubEle = dom.createElement("Host");
			subsubEle.setAttribute("Host", hostName + i);
			subEle.appendChild(subsubEle);
		}
		cmdEle.appendChild(subEle);
		return cmdEle;
	}

	public Element createService(Document dom, String hostName,
			String sDescription, String lastStateChange) {
		Element serviceEle = null;
		serviceEle = dom.createElement("Service");
		serviceEle.setAttribute("Host", hostName);
		serviceEle.setAttribute("ServiceDescription", sDescription);
		serviceEle.setAttribute("CheckType", "PASSIVE");
		serviceEle.setAttribute("StateType", "SOFT");
		serviceEle.setAttribute("MonitorStatus", "PENDING");
		serviceEle.setAttribute("LastHardState", "PENDING");
		serviceEle.setAttribute("LastStateChange", lastStateChange);
		return serviceEle;
	}

	public Element createServiceCommand(Document dom, String applicationType,
			String hostName, String sDescription, int numOfHosts,
			int numOfServices, String lastStateChange) {
		Element cmdEle = null;
		cmdEle = dom.createElement("Command");
		cmdEle.setAttribute("Action", "ADD");
		cmdEle.setAttribute("ApplicationType", applicationType);
		for (int j = 1; j <= numOfHosts; j++) {
			for (int i = 1; i <= numOfServices; i++) {
				cmdEle.appendChild(createService(dom, hostName + j,
						sDescription + i, lastStateChange));
			}
		}
		return cmdEle;
	}

	public Document setInitSystem(String session, String adapterType,
			String applicationType, String hostName, String description,
			String deviceName, String displayName, String serviceDescription,
			String hostGroupName, int nHosts, int nServices,
			String lastStateChange)
			throws javax.xml.parsers.ParserConfigurationException {
		DocumentBuilder docBuilder = null;

		docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document dom = docBuilder.newDocument();

		Element adapterEle = dom.createElement("Adapter");
		adapterEle.setAttribute("Session", session);
		adapterEle.setAttribute("AdapterType", adapterType);

		dom.appendChild(adapterEle);
		try {
			adapterEle.appendChild(createHostCommand(dom, applicationType,
					hostName, description, deviceName, hostName, nHosts,
					lastStateChange));
			adapterEle.appendChild(createHostGroupCommand(dom, applicationType,
					hostGroupName));
			adapterEle.appendChild(createModifyHostGroupCommand(dom,
					applicationType, hostGroupName, hostName, nHosts));
			/* for each host initialize m services */

			adapterEle.appendChild(createServiceCommand(dom, applicationType,
					hostName, serviceDescription, nHosts, nServices,
					lastStateChange));

		} catch (Exception e) {
			System.out.println("error1=" + e);
		}
		return dom;
	}

	public String getInitMessage(String session, String adapterType,
			String applicationType, String hostName, String description,
			String deviceName, String displayName, String serviceDescription,
			String hostGroupName, int nHosts, int nServices,
			String lastStateChange)
			throws javax.xml.parsers.ParserConfigurationException {

		return getXML(setInitSystem(session, adapterType, applicationType,
				hostName, description, deviceName, displayName,
				serviceDescription, hostGroupName, nHosts, nServices,
				lastStateChange));
	}

}
