
package org.groundwork.foundation.jmx;

import java.rmi.registry.LocateRegistry;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

//import com.sun.jmx.remote.util.Service;

/**
 * Host contains information for a host and a list of ServiceCheck 
 * 
 * @author Vong Tran (vong.tran@gmail.com)
 * @version 1.0
 */
public class Host {
	private String hostName;
	private String device;
	private String port;
	private String authentication;
	private String userName;
	private String password;
	private String status="";
	private JMXConnector connection=null;
	private boolean isConnected = false;
	
	private MsgProducer producer;
	private static final String UP_LOG_MESSAGE = "UP - JMX Connection established on host: ";
	private static final String DOWN_LOG_MESSAGE = "DOWN - Fail to establish JMX Connection on host: ";
	/**
	 * Constructor for Host
	 */
	public Host(){
		
	}
	
	/**
	 * Constructor for Host
	 * @param hostName
	 * @param iPAddress
	 * @param port
	 * @param serviceChecks
	 * @param authentication
	 * @param userName
	 * @param password
	 */
	public Host(String hostName, String device, String port, String authentication, String userName, String password){
		if(hostName == null){
			throw new NullPointerException("hostName required not null");
		}else if(hostName.trim().length() < 1){
			throw new IllegalArgumentException("hostName requires not empty string");
		}
		if(device == null){
			throw new NullPointerException("iPAddress requires not null");
		}else if(device.trim().length() < 1){
			throw new IllegalArgumentException("IPAddress required not empty string");
		}
		if(port == null){
			throw new NullPointerException("port requires not null");
		}else if(port.trim().length() < 1){
			throw new IllegalArgumentException("port requires not empty string");
		}
		try{
			Integer iPort = new Integer(port);
		}catch(NumberFormatException e){
			throw new NumberFormatException("port required an integer");
		}
		if(authentication == null){
			this.authentication = "";
		}else{
			this.authentication = authentication;
		}
		this.hostName = hostName;
		this.device = device;
		this.port = port;
		this.userName = userName;
		this.password = password;
	}
	
	/**
	 * Set hostName for Host
	 * @param hostName
	 */
	public void setHostName(String hostName){
		if(hostName == null){
			throw new NullPointerException("hostName required not null");
		}else if(hostName.trim().length() < 1){
			throw new IllegalArgumentException("hostName requires not empty string");
		}
		this.hostName = hostName;
	}
	
	/**
	 * Set iPAddress for Host
	 * @param iPAddress
	 */
	public void setDevice(String device){
		if(device == null){
			throw new NullPointerException("device requires not null");
		}else if(device.trim().length() < 1){
			throw new IllegalArgumentException("device required not empty string");
		}
		this.device = device;
	}
	
	/**
	 * Set port for Host
	 * @param port
	 */
	public void setPort(String port){
		if(port == null){
			throw new NullPointerException("port requires not null");
		}else if(port.trim().length() < 1){
			throw new IllegalArgumentException("port requires not empty string");
		}
		try{
			Integer iPort = new Integer(port);
		}catch(NumberFormatException e){
			throw new NumberFormatException("port required an integer");
		}
		this.port = port;
	}
	
	
	/**
	 * Set authentication
	 * @param authentication
	 */
	public void setAuthentication(String authentication){
		this.authentication = authentication;
	}
	
	/**
	 * Set userName
	 * @param userName
	 */
	public void setUserName(String userName){
		this.userName = userName;
	}
	
	/**
	 * Set password
	 * @param password
	 */
	public void setPassword(String password){
		this.password = password;
	}
	
	public void setConnection(JMXConnector connection){
		this.connection = connection;
		if(connection != null){
			updateStatus("UP");
		}else{
			updateStatus("DOWN");
		}
	}
	
	/**
	 * Get hostName
	 * @return
	 */
	public String getHostName(){
		return this.hostName;
	}
	
	/**
	 * Get IPAddress
	 * @return
	 */
	public String getDevice(){
		return this.device;
	}
	
	/**
	 * Get port
	 * @return
	 */
	public String getPort(){
		return this.port;
	}
	
	/**
	 * Get authentication
	 * @return
	 */
	public String getAuthentication(){
		return this.authentication;
	}
	
	/**
	 * Get UserName
	 * @return
	 */
	public String getUserName(){
		return this.userName;
	}
	
	/**
	 * Get Password
	 * @return
	 */
	public String getPassword(){
		return this.password;
	}
	
	public boolean hasConnection(){
		return this.isConnected;
	}
	
	public JMXConnector getConnection(){
		return this.connection;
	}
	
	public void updateStatus(String status){
		if(this.status.compareToIgnoreCase(status) != 0){
			this.status = status;
			if(this.status.compareToIgnoreCase("up") == 0){
				String textMessage = UP_LOG_MESSAGE + this.hostName;
				sendLogMessage(textMessage);
			}
			if(this.status.compareToIgnoreCase("down") == 0){
				String textMessage = DOWN_LOG_MESSAGE + this.hostName;
				sendLogMessage(textMessage);
			}
		}
	}
	
	public void sendLogMessage(String textMessage){
		String logMessage = XMLProcessing.toHostLogMessage(this.device, this.hostName, this.status, textMessage);
		logMessage = XMLProcessing.PREFIX_ADAPTER_S0 + XMLProcessing.PREFIX_COMMAND_ADD + logMessage + XMLProcessing.SURFIX_COMMAND + XMLProcessing.SURFIX_ADAPTER;
		try{
			producer = new MsgProducer(logMessage);
			producer.start();
			producer.join();
			producer.unInitialize();
		}catch(Exception e){
			System.out.println("Error while sending result message to JMS queue " + e);
		}
		
	}
	
	public void sendUpdateMessage(){
		
	}
	
	public void createConnnection(){
		JMXServiceURL jmxUrl = null;
		//JMXConnector connector = null;
		String url = "service:jmx:rmi://localhost/jndi/rmi://localhost:9004/jmxrmi";
		url = url.replace("localhost",this.hostName); 
		url = url.replace("9004",this.port);			
		try{
			jmxUrl = new JMXServiceURL(url);
			System.out.println("URL:" + url);
			//Connection using ssl.
			if(this.authentication == "ssl"){
				SslRMIClientSocketFactory csf = new SslRMIClientSocketFactory();
				SslRMIServerSocketFactory ssf = new SslRMIServerSocketFactory();
				LocateRegistry.createRegistry(new Integer(port).intValue(), csf, ssf);
				Map<String, Object> env = new HashMap<String, Object>();
				env.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, csf);
				env.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, ssf);
				env.put("com.sun.jndi.rmi.factory.socket", csf);
				this.connection = JMXConnectorFactory.connect(jmxUrl, env);
			}else{
				//Connection with authentication is true and with username and password 
				if(this.userName != ""){
					String[] creds = {userName, password};
					Map<String, String[]> env = new HashMap<String, String[]>();
					env.put(JMXConnector.CREDENTIALS, creds);
					this.connection = JMXConnectorFactory.connect(jmxUrl, env);
				}else{
					//Connection with authentication is false and not use username and password
					this.connection = JMXConnectorFactory.connect(jmxUrl);
				}
			}
			isConnected = true;
			updateStatus("UP");
		}catch(Exception e){
			isConnected = false;
			updateStatus("DOWN");
			
		}
		
	}
}
