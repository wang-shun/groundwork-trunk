package com.groundworkopensource.tomcat.nagios.plugin;

/**
 * Tomcat monitor.
 */

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class Monitor {
	public static int UNKNOWN_STATUS = 3;
	JMXServiceURL jmxUrl = null;
	JMXConnector jmxConnector = null;
	
	MBeanServerConnection jmxMBeanServer = null;
	String server = "localhost";
	String port = "9004";
	String user = "";
	String password = "";
	boolean verbose = false;
	boolean dump = false;
	int rc = 0;
	HashMap<String,javax.management.ObjectName> managedObjects = new HashMap<String,javax.management.ObjectName>();
	Vector<Tuple> beanList = new Vector<Tuple>();

	/**
	 * Monitor - Check remote instance MBeans
	 * @param args. String []. List of arguments
	 * @throws Exception. If anything goes wrong
	 */
	public Monitor(String [] args) throws Exception {
	/*	String url = "service:jmx:rmi://localhost/jndi/rmi://localhost:9004/jmxrmi";
		parseArgs(args);
		url = url.replace("localhost",server); 
		url = url.replace("9004",port);			
		jmxUrl = new JMXServiceURL(url);
		jmxConnector = JMXConnectorFactory.connect(jmxUrl);
		jmxMBeanServer = jmxConnector.getMBeanServerConnection();
		loadObjects();
		process();*/
		String url = "service:jmx:rmi://localhost/jndi/rmi://localhost:9004/jmxrmi";
		parseArgs(args);
		url = url.replace("localhost",server); 
		url = url.replace("9004",port);			
		jmxUrl = new JMXServiceURL(url);
		try {
			String [] creds = null;
			Map<String,String[]> env = new HashMap<String,String[]>();
			if ((creds = setupCredentials()) != null) {
				env.put(JMXConnector.CREDENTIALS, creds);
				jmxConnector = JMXConnectorFactory.connect(jmxUrl,env);
			}
			else
				jmxConnector = JMXConnectorFactory.connect(jmxUrl);
		} catch (SecurityException error) {
			rc = UNKNOWN_STATUS;
			throw new Exception("Monitor failed - bad username/password.");
		} 
		jmxMBeanServer = jmxConnector.getMBeanServerConnection();
		loadObjects();
		process();	
	}
	
	/**
	 * Setup the credentials if provided
	 * @return. String []. The stuff to put into the environment hashmap
	 */
	String [] setupCredentials() {
		if (user == null)
			return null;
		String[] creds = {user, password};
		return creds;
	}
	
	/**
	 * Dump - Sorted dump of object names.
	 * @throws Exception. If anything goes wrong
	 */
	public void dump()  throws Exception {
		Set syms = managedObjects.keySet();
		Iterator ix = syms.iterator();
		Vector v = new Vector();
		for (int i=0;i<syms.size();i++) {
			String key = (String)ix.next();
			v.add(key);
		}
		String[] stringArray = new String[v.size()];
		for (int i = 0; i < v.size(); i++) {
			Object x = v.get(i);
			stringArray[i] = (String)x;
		}
		Arrays.sort(stringArray);
		for (int i=0;i<stringArray.length;i++) {
			System.out.println(stringArray[i]);
		}
	}
	
	/**
	 * Given an mbean/attribute tuple, print the value.
	 * @param objName. String. Object name in MBean format.
	 * @param attrName. The name of the attribute, or '!' for wildcard
	 */
	public void getMBeanDetails(String objName, String attrName) throws Exception {
		 Attribute attribute = null;
	      try {
	         ObjectName objectName = new ObjectName(objName);
	         ObjectInstance objectInstance = jmxMBeanServer.getObjectInstance(objectName);

	         MBeanInfo mbeanInfo = jmxMBeanServer.getMBeanInfo(objectName);

	         MBeanAttributeInfo[] attributes = mbeanInfo.getAttributes();
	         String[] attributeNames = new String[attributes.length];
	         for (int i = 0; i < attributes.length; i++) {
	            attributeNames[i] = attributes[i].getName();
	         }

	         AttributeList attributeList = jmxMBeanServer.getAttributes(objectName, attributeNames);
	         Iterator it = attributeList.iterator();
	       
	         while (it.hasNext()) {
	            attribute = (Attribute)it.next();
	            if (attrName.equals("!") || attribute.getName().equals(attrName))
	            		System.out.println(objName + "." + attribute.getName() + " = " + attribute.getValue());
	         }
	      } catch (Exception e) {
				rc = UNKNOWN_STATUS;
				throw new Exception("Monitor failed - " + e.toString() +" encountered retrieving " + objName + " / " + attribute.getName());
	      }
	  }
	
	/**
	 * Return the return code (after parsing errors.
	 * @return. Int. The rc.
	 */
	 public int getRC() {
		return rc;
	}
	
	 /**
	  * Load the objects for this manager.
	  * @throws Exception. If anything goes wrong.
	  */
	public void loadObjects()  throws Exception {
		try {
			Iterator mBeans = jmxMBeanServer.queryNames(null, null).iterator(); 
			while (mBeans.hasNext()) {
				javax.management.ObjectName on = (javax.management.ObjectName)mBeans.next();
				String name = on.toString();
				managedObjects.put(name, on);
			}
		} catch (Exception error) {
			error.printStackTrace();
		}
	}
	
	/**
	 * Output the help/about.
	 *
	 */
	public void outputHelp() {
		System.out.println("Nagios/Tomcat Monitor, Version 1.0\nCopyright (c) 2006, by Groundwork Opensource, Inc.\nAll rights reserved.");
	}
	
	/**
	 * Parse the arguments.
	 * -s servername             	| Provide servername, default localhost
	 * -p portnum					| Provide port number, 9004 by default
	 * -u username                  | Provide username, future use
	 * -password password			| Provide password, future use
	 * -d 							| dump all object names in this manager connection
	 * -m beanname					| The object name in mbean ObjectName format
	 * -a attribute 				| The attribute name, as String. Use '!' for wildcard
	 * -v   						| verbose mode. Not implemented yet.
	 * 
	 * @param args. String[]. Arguments to the monitor
	 * @throws Exception. If anything goes wrong.
	 */
	public void parseArgs(String [] args) throws Exception {
		int i = 0;
		boolean matched = false;
		String mbeanName = null;
		String mbeanAttribute = null;
		
		if (args == null) {
			rc = UNKNOWN_STATUS;
			throw new Exception("Monitor failed - no arguments");	
		}

		while(i<args.length) {
			matched = false;
			if (args[i].equalsIgnoreCase("-Server") || args[i].equalsIgnoreCase("-s")) {
				if (args.length < i+2) 
					throw new Exception("Unbalanced argument for " + args[i]);
				server = args[i + 1];
				i+=2;
				matched = true;
			} else if (args[i].equalsIgnoreCase("-Port") || args[i].equalsIgnoreCase("-p")) {
				if (args.length < i+2) 
					throw new Exception("Unbalanced argument for " + args[i]);				
				port = args[i + 1];
				i+=2;
				try {
					int x = Integer.parseInt(port);
				} catch (Exception error) {
					throw new Exception("Non-integer port value not allowed: " + port);
				}
				matched = true;
			} else if (args[i].equalsIgnoreCase("-User") || args[i].equalsIgnoreCase("-u")) {
				if (args.length < i+2) 
					throw new Exception("Unbalanced argument for " + args[i]);				
				user = args[i + 1];
				i+=2;
				matched = true;
			} else if (args[i].equalsIgnoreCase("-Password")) {
				if (args.length < i+2) 
					throw new Exception("Unbalanced argument for " + args[i]);
				password = args[i + 1];
				i+=2;
				matched = true;
			} else if (args[i].equalsIgnoreCase("-MBEAN") || args[i].equalsIgnoreCase("-m")) {
				if (args.length < i+2) 
					throw new Exception("Unbalanced argument for " + args[i]);
				mbeanName = args[i + 1];
				i+=2;
				if (mbeanAttribute != null) {
					Tuple t = new Tuple(mbeanName,mbeanAttribute);
					beanList.add(t);
					mbeanName = null;
					mbeanAttribute = null;
				}
				matched = true;
			} else if (args[i].equalsIgnoreCase("-Attribute") || args[i].equalsIgnoreCase("-a")) {
				if (args.length < i+2) 
					throw new Exception("Unbalanced argument for " + args[i]);
				mbeanAttribute = args[i + 1];
				if (mbeanName != null) {
					Tuple t = new Tuple(mbeanName,mbeanAttribute);
					beanList.add(t);
					mbeanName = null;
					mbeanAttribute = null;
				}
				i+=2;
				matched = true;
			} else if (args[i].equalsIgnoreCase("-Help") || args[i].equalsIgnoreCase("-h")) {
				outputHelp();
				i++;
				matched = true;
			} else if (args[i].equalsIgnoreCase("-Verbose") || args[i].equalsIgnoreCase("-v")) {
				verbose = true;
				matched = true;
			} else if (args[i].equalsIgnoreCase("-Dump") || args[i].equalsIgnoreCase("-d")) {
				dump = true;
				matched = true;
				i++;
			}
			if (!matched) {
				rc = UNKNOWN_STATUS;
				throw new Exception("Monitor failed unknown argument - " + args[i]);
			}
		}	
		
		if (!matched) {
			rc = UNKNOWN_STATUS;
			throw new Exception ("Monitor failed - no arguments");
		}
	}
	
	/**
	 * Do what the user has designated
	 * @throws Exception. If anything goes wrong.
	 */
	public void process() throws Exception  {
		if (dump) {
			dump();
		} else {
			for (int i=0;i<beanList.size();i++) {
				Tuple t = (Tuple)beanList.get(i);
				getMBeanDetails(t.getBeanName(),t.getBeanAttribute());
			}
		}
	}
	
}

/**
 * A class for associating mbeans by objectname, attribute
 *
 */
class Tuple {
	String mbeanName = null;
	String mbeanAttribute = null;
	String mbeanValue = null;

	/**
	 * Create a tuple
	 * @param mBeanName. String. The objectname as a string.
	 * @param mbeanAttribute. String. The attribute.
	 */
	public Tuple(String mBeanName, String mbeanAttribute) {
		this.mbeanName = mBeanName;
		this.mbeanAttribute = mbeanAttribute;
	}
	
	public String getBeanAttribute() {
		return mbeanAttribute;
	}
	
	public String getBeanName() {
		return mbeanName;
	}
}
