package org.groundwork.foundation.jmx;

import java.rmi.registry.LocateRegistry;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

/**
 * 
 * @author Vong Tran (vong.tran@gmail.com)
 *
 */

public class JMXConnectionManager {
	private MsgProducer producer = null;
	public JMXConnectionManager(){

	}
	
	public JMXConnector createConnnection(String hostName, String port, String authentication, String user, String password) throws JMXConfigurationException{
		JMXServiceURL jmxUrl = null;
		JMXConnector connector = null;
		String url = "service:jmx:rmi://localhost/jndi/rmi://localhost:9004/jmxrmi";
		url = url.replace("localhost",hostName); 
		url = url.replace("9004",port);			
		try{
			jmxUrl = new JMXServiceURL(url);
			System.out.println("URL:" + url);
			//Connection using ssl.
			if(authentication == "ssl"){
				SslRMIClientSocketFactory csf = new SslRMIClientSocketFactory();
				SslRMIServerSocketFactory ssf = new SslRMIServerSocketFactory();
				LocateRegistry.createRegistry(new Integer(port).intValue(), csf, ssf);
				Map<String, Object> env = new HashMap<String, Object>();
				env.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, csf);
				env.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, ssf);
				env.put("com.sun.jndi.rmi.factory.socket", csf);
				connector = JMXConnectorFactory.connect(jmxUrl, env);
			}else{
				//Connection with authentication is true and with username and password 
				if(user != ""){
					String[] creds = {user, password};
					Map<String, String[]> env = new HashMap<String, String[]>();
					env.put(JMXConnector.CREDENTIALS, creds);
					connector = JMXConnectorFactory.connect(jmxUrl, env);
				}else{
					//Connection with authentication is false and not use username and password
					connector = JMXConnectorFactory.connect(jmxUrl);
				}
			}
		}catch(Exception e){
			//publish event message to the JMS server if we can not establish connection to MBEAN server.
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    	Date currentDate = new Date();
	    	String time = format.format(currentDate).toString();
			String eventMessage = "";
			eventMessage = "<LogMessage MonitorServerName=\"localhost\" Device=\"" + hostName + 
									"\" TextMessage=\"Could not connect to host\" ReportDate=\"" + 
									time + "\" Severity=\"FATAL\" MonitorStatus=\"FAILED\" ErrorType=\"SERVICE ALERT\" Host=\"" +
									hostName + "\"/>";
			eventMessage = JMXService.PREFIX_ADAPTER + JMXService.PREFIX_COMMAND_MODIFY + eventMessage + JMXService.SURFIX_COMMAND + JMXService.SURFIX_ADAPTER;
			//jmsWriter.publishMessage(eventMessage);
			throw new JMXConfigurationException("Error while create jmx connection for host: " + hostName + e);
			//e.printStackTrace();
		}
		System.out.println("Susscess create connection to host with URL:" + url);
		return connector;
	}
	
	public void createConnections(Hashtable<String, Host> listHosts) throws JMXConfigurationException{
		if(listHosts != null && listHosts.size() > 0){
			Collection<Host> cHosts = listHosts.values();
			Iterator<Host> ite = cHosts.iterator();
			while(ite.hasNext()){
				Host host = ite.next();
				try{
					if(host != null){
						String hostName = host.getHostName();
						String port = host.getPort();
						String authentication = host.getAuthentication();
						String user = host.getUserName();
						String password = host.getPassword();
						if(authentication == null){
							authentication = "";
						}
						//String ttt = ("Host:" + hostName + "; port:" + port + "; user:" + user + "; password:" + password + "; authentication: " + authentication);
					
						JMXConnector con = createConnnection(hostName, port, authentication, user, password);
						if(con != null){
						System.out.println("Succesful created connection on ");
						System.out.println(host.getHostName() + " " + host.getDevice());
						XMLProcessing.htHosts.get(hostName).setConnection(con);
						System.out.println("Success add connection to host");
						}
						//XMLProcessing.htHosts.put(hostName, host);
						//XMLProcessing.htHosts.get(hostName).setConnection(con);
						//connections.put(hostName, con);
						//hHosts.put(hostName, oHost);
						//hostDeviceMap.put(hostName, device);
					}
				}catch(Exception e){
					throw new JMXConfigurationException("Error while create connections " + e);
				}
			}
		}
	}
	
	
}
