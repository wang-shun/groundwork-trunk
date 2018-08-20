package org.groundwork.foundation.jmx;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author Vong Tran (vong.tran@gmail.com)
 *
 */

public class XMLProcessing {
	public static final String SYNC = "SYNC";
	public static final String ACTION = "Action";
	public static final String ADAPTER = "Adapter";
	public static final String SESSION = "Session";
	public static final String ADAPTER_TYPE = "AdapterType";
	public static final String COMMAND = "Command";
	public static final String APPLICATION_TYPE = "ApplicationType";
	public static final String HOST = "Host";
	public static final String PORT = "JMXPort";
	public static final String AUTHENTICATION = "Authentication";
	public static final String USER = "User";
	public static final String PASS = "Password";
	public static final String DESCRIPTION = "Description";
	public static final String DEVICE = "Device";
	public static final String DISPLAY_NAME = "DisplayName";
	public static final String LAST_STATE_CHANGE = "LastStateChange";
	public static final String SERVICE = "Service";
	public static final String SERVICE_DESCRIPTION = "ServiceDescription";
	public static final String CHECK_TYPE = "CheckType";
	public static final String MONITOR_STATUS = "MonitorStatus";
	public static final String TEXT_MESSAGE = "TextMessage";
	public static final String MBEAN = "MBEAN";
	public static final String THRESOLD_WARN = "ThresoldWarn";
	public static final String THRESOLD_CRITICAL = "ThresoldCritical";
	public static final String ATTRIBUTE = "Attribute";
	public static final String KEY = "Key";
	public static final String INTERVAL = "Interval";
	
	public static final String OK_STATUS = "OK";
	public static final String WARNING_STATUS = "WARNING";
	public static final String CRITICAL_STATUS = "CRITICAL";
	public static final String UNKNOWN_STATUS = "UNKNOWN";
	public static final String UP_STATUS = "UP";
	public static final String DOWN_STATUS = "DOWN";
	
	public static final String NAGIOS_LOG_FILE_PATH = "/usr/local/groundwork/nagios/eventhandlers/service_perfdata.log";
	public static final String PREFIX_ADAPTER = "<Adapter Session=\"1\" AdapterType=\"SystemAdmin\">";
	public static final String PREFIX_ADAPTER_S0 = "<Adapter Session=\"0\" AdapterType=\"SystemAdmin\">";
	public static final String SURFIX_ADAPTER = "</Adapter>";
	public static final String PREFIX_COMMAND_ADD = "<Command Action=\"ADD\" ApplicationType=\"JMX\">";
	public static final String PREFIX_COMMAND_MODIFY = "<Command Action=\"MODIFY\" ApplicationType=\"JMX\">";
	public static final String SURFIX_COMMAND = "</Command>";
	
//	public static List<Hashtable<String, String>> listHosts = new Vector<Hashtable<String,String>>();
//	public static List<Hashtable<String, String>> listServices = new Vector<Hashtable<String,String>>();
	public static Hashtable<String, Host> htHosts = new Hashtable<String, Host>();
	public static List<Service> listServices = new Vector<Service>();
	
	public XMLProcessing(){
		
	}
	
	public static String convertToString(Element element) {
		StringWriter sw = null;
		try{
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer trans = tf.newTransformer();
			sw = new StringWriter();
			trans.transform(new DOMSource(element), new StreamResult(sw));
		}catch(Exception e){
			//throw new Exception("Could not convert element to String");
		}
		return sw.toString();
	}
	
	public static Element filterAttributes(Element element, List<String> removeAttributes){
		Iterator<String> it = removeAttributes.iterator();
		while(it.hasNext()){
			String removeAtt = it.next();
			if(element.hasAttribute(removeAtt)){
				element.removeAttribute(removeAtt);
			}
		}
		
		if(element.hasChildNodes()){
			NodeList children = element.getChildNodes();
			for(int i = 0; i < children.getLength(); i++){
				Node child = children.item(i);
				if(child.getNodeType() == Node.ELEMENT_NODE){
					Element childElement = (Element)child;
					filterAttributes(childElement, removeAttributes);
				}
			}
		}
		return element;
	}
	
	public static String addSyncToMessage(String message){
		String rs = "<" + SYNC + " " + ACTION + "='start'/>" + message + "<" + SYNC + " " + ACTION + "='stop'/>";
		return rs;
	}
	
	public static String filterAndAddSyncToMessage(String message){
		String rs = "<" + SYNC + " " + ACTION + "='start'>" + message + "</" + SYNC + ">";
		
		List<String> rAttributes = new ArrayList<String>();
		rAttributes.add(XMLProcessing.PORT);
		rAttributes.add(XMLProcessing.USER);
		rAttributes.add(XMLProcessing.PASS);
		rAttributes.add(XMLProcessing.AUTHENTICATION);
		rAttributes.add(XMLProcessing.MBEAN);
		rAttributes.add(XMLProcessing.ATTRIBUTE);
		rAttributes.add(XMLProcessing.KEY);
		rAttributes.add(XMLProcessing.THRESOLD_WARN);
		rAttributes.add(XMLProcessing.THRESOLD_CRITICAL);
		Element element = null;
		try{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			byte[] in = rs.getBytes();
			ByteArrayInputStream io = new ByteArrayInputStream(in);
			Document document = db.parse(io);
			element = document.getDocumentElement();
			element = filterAttributes(element, rAttributes);
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
		return convertToString(element);
	}
	
	public static String filterMessage(String message){
		String rs = message;
		
		List<String> rAttributes = new ArrayList<String>();
		rAttributes.add(XMLProcessing.PORT);
		rAttributes.add(XMLProcessing.USER);
		rAttributes.add(XMLProcessing.PASS);
		rAttributes.add(XMLProcessing.AUTHENTICATION);
		rAttributes.add(XMLProcessing.MBEAN);
		rAttributes.add(XMLProcessing.ATTRIBUTE);
		rAttributes.add(XMLProcessing.KEY);
		rAttributes.add(XMLProcessing.THRESOLD_WARN);
		rAttributes.add(XMLProcessing.THRESOLD_CRITICAL);
		rAttributes.add(XMLProcessing.INTERVAL);
		Element element = null;
		try{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			byte[] in = rs.getBytes();
			ByteArrayInputStream io = new ByteArrayInputStream(in);
			Document document = db.parse(io);
			element = document.getDocumentElement();
			element = filterAttributes(element, rAttributes);
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
		return convertToString(element);
	}
	
	public static Element addSyncToElement(Element element){
		
		return null;
	}
	
	public static String toResultMessage(String host, String serviceDescription, String status, String textMessage){
		String rs = "<" + SERVICE + " " + HOST + "='" + host + "' " + SERVICE_DESCRIPTION + "='" + serviceDescription + 
					"' " + MONITOR_STATUS + "='" + status + "' " + TEXT_MESSAGE + "='" + textMessage + "'/>";
//		String rs = "<" + SERVICE + " " + HOST + "='" + host + "' " + SERVICE_DESCRIPTION + "='" + serviceDescription + 
//		"' " + MONITOR_STATUS + "='OK' " + "JVMValue='" + result + "'/>";

		return rs;
	}
	
	public static String toServiceLogMessage(String device, String host, String status, String serviceDescription, String textMessage){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		
		String rs = "<LogMessage MonitorServerName='localhost' " + DEVICE + "='" + device + "' " + HOST + "='" + host + "' " + SERVICE_DESCRIPTION + "='" + serviceDescription + 
					"' " + MONITOR_STATUS + "='" + status + "' " + TEXT_MESSAGE + "='" + textMessage + "' ErrorType='SERVICE ALERT' Severity='" + status + "' ReportDate='" + sdf.format(date) + "' />";

		return rs;
	}
	
	public static String toHostLogMessage(String device, String host, String status, String textMessage){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		
		String rs = "<LogMessage MonitorServerName='localhost' " + DEVICE + "='" + device + "' " + HOST + "='" + host + "' " + 
			MONITOR_STATUS + "='" + status + "' " + TEXT_MESSAGE + "='" + textMessage + "' ErrorType='SERVICE ALERT' Severity='" + status + "' ReportDate='" + sdf.format(date) + "' />";

		return rs;
	}
	
	public static void addServiceHost(Document document){
		
	}
	
	public static void addHosts(Document document){
		//Hashtable<String, String> hosts = new Hashtable<String, String>();
		NodeList lHosts = document.getElementsByTagName(HOST);
		for(int i = 0; i < lHosts.getLength(); i++){
			Node nHost = lHosts.item(i);
			if(nHost.getNodeType() == Node.ELEMENT_NODE){
				Element eHost = (Element)nHost;
				NamedNodeMap attrs = eHost.getAttributes();
				String hostName = attrs.getNamedItem(HOST).getNodeValue();
				String device = attrs.getNamedItem(DEVICE).getNodeValue();
				String port = attrs.getNamedItem(PORT).getNodeValue();
				String user = attrs.getNamedItem(USER).getNodeValue();
				String pass = attrs.getNamedItem(PASS).getNodeValue();
				String authentication = null;
				if(attrs.getNamedItem(AUTHENTICATION)!= null){
					authentication = attrs.getNamedItem(AUTHENTICATION).getNodeValue();
				}
				Host host = new Host(hostName, device, port, authentication, user, pass);
				htHosts.put(hostName, host);
			}
		}
	}
	
	public static void addHosts(String xmlMessage){
		try{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			byte[] in = xmlMessage.getBytes();
			ByteArrayInputStream io = new ByteArrayInputStream(in);
			Document document = db.parse(io);
			XMLProcessing.addHosts(document);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void addServices(Document document){
		//Hashtable<String, String> services = null;
		NodeList listSrvs = document.getElementsByTagName(SERVICE);
		if(listSrvs != null && listSrvs.getLength() > 0){
			//int numHosts = listSrvs.getLength();
			//services = new Hashtable<String, String>(numHosts);
			for(int i = 0; i < listSrvs.getLength(); i++){
				Node srv = listSrvs.item(i);
				if(srv.getNodeType() == Node.ELEMENT_NODE){
					Element eSrv = (Element)srv;
					NamedNodeMap attrs = eSrv.getAttributes();
					String hostName = attrs.getNamedItem(HOST).getNodeValue();
					String serviceDescription = attrs.getNamedItem(SERVICE_DESCRIPTION).getNodeValue();
					String mBean = attrs.getNamedItem(MBEAN).getNodeValue();
					String attribute = attrs.getNamedItem(ATTRIBUTE).getNodeValue();
					String key = null;
					String warningThresold = null;
					String criticalThresold = null;
					int interval = 1;
					if(attrs.getNamedItem(KEY)!= null){
						key = attrs.getNamedItem(KEY).getNodeValue();
					}
					if(attrs.getNamedItem(THRESOLD_WARN)!= null){
						warningThresold = attrs.getNamedItem(THRESOLD_WARN).getNodeValue();
					}
					if(attrs.getNamedItem(THRESOLD_CRITICAL)!= null){
						criticalThresold = attrs.getNamedItem(THRESOLD_CRITICAL).getNodeValue();
					}
					if(attrs.getNamedItem(INTERVAL) != null){
						interval = new Integer(attrs.getNamedItem(INTERVAL).getNodeValue()).intValue();
					}
					Service service = new Service(hostName, serviceDescription, mBean, attribute, key, warningThresold, criticalThresold, interval);
					listServices.add(service);
				}
			}
		}
	}
	
	public static void addServices(String xmlMessage){
		try{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			byte[] in = xmlMessage.getBytes();
			ByteArrayInputStream io = new ByteArrayInputStream(in);
			Document document = db.parse(io);
			XMLProcessing.addServices(document);
		}catch(Exception e){
			
		}
	}
	
//	public static void addHosts(Document document){
//		//Hashtable<String, String> hosts = new Hashtable<String, String>();
//		NodeList lHosts = document.getElementsByTagName(HOST);
//		for(int i = 0; i < lHosts.getLength(); i++){
//			Node nHost = lHosts.item(i);
//			if(nHost.getNodeType() == Node.ELEMENT_NODE){
//				Element eHost = (Element)nHost;
//				NamedNodeMap attrs = eHost.getAttributes();
//				Node attribute = null;
//				String name = null;
//				String value = null;
//				int len = attrs.getLength();
//				if(len > 0){
//					Hashtable<String, String> host = new Hashtable<String, String>(len);
//					for ( int attCount=0; attCount < len;attCount++)
//					{
//					    attribute = attrs.item(attCount);
//					    name = attribute.getNodeName();
//					    value = attribute.getNodeValue();
//					    host.put(name, value);
//					    //Hashtable<String, String> hHosts = new Hashtable<String, String>();
//					    //System.out.println(name + ": " + value);
//					    if (name == null || value == null || name.length() == 0)
//					    {   
//					    	continue;
//					    }
//					    
//				    }
//					listHosts.add(host);
//				}
//			}
//		}
//	}
//	
//	public static void addHosts(String xmlMessage){
//		try{
//			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//			DocumentBuilder db = dbf.newDocumentBuilder();
//			byte[] in = xmlMessage.getBytes();
//			ByteArrayInputStream io = new ByteArrayInputStream(in);
//			Document document = db.parse(io);
//			XMLProcessing.addHosts(document);
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//	}
//	
//	public static void addServices(Document document){
//		Hashtable<String, String> services = null;
//		NodeList listSrvs = document.getElementsByTagName(SERVICE);
//		if(listSrvs != null && listSrvs.getLength() > 0){
//			int numHosts = listSrvs.getLength();
//			services = new Hashtable<String, String>(numHosts);
//			for(int i = 0; i < listSrvs.getLength(); i++){
//				Node srv = listSrvs.item(i);
//				if(srv.getNodeType() == Node.ELEMENT_NODE){
//					Element eSrv = (Element)srv;
//					NamedNodeMap attrs = eSrv.getAttributes();
//					Node attribute = null;
//					String name = null;
//					String value = null;
//					int len = attrs.getLength();
//					if(len > 0){
//						Hashtable<String, String> service = new Hashtable<String, String>(len);
//						for ( int attCount=0; attCount < len;attCount++)
//						{
//						    attribute = attrs.item(attCount);
//						    name = attribute.getNodeName();
//						    value = attribute.getNodeValue();
//						    service.put(name, value);
//						    //Hashtable<String, String> hHosts = new Hashtable<String, String>();
//						    //System.out.println(name + ": " + value);
//						    if (name == null || value == null || name.length() == 0)
//						    {   
//						    	continue;
//						    }
//						    
//					    }
//						listServices.add(service);
//					}
//				}
//			}
//		}
//	}
//	
//	public static void addServices(String xmlMessage){
//		try{
//			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//			DocumentBuilder db = dbf.newDocumentBuilder();
//			byte[] in = xmlMessage.getBytes();
//			ByteArrayInputStream io = new ByteArrayInputStream(in);
//			Document document = db.parse(io);
//			XMLProcessing.addServices(document);
//		}catch(Exception e){
//			
//		}
//	}
	

}
