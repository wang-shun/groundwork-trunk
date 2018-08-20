package org.groundwork.foundation.jmx;

import java.util.Hashtable;
import java.util.Iterator;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.remote.JMXConnector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author Vong Tran (vong.tran@gmail.com)
 *
 */

public class QueryMBEAN extends IncommingMessageListenerThread{
	
	public static final String PREFIX_ADAPTER = "<Adapter Session=\"1\" AdapterType=\"SystemAdmin\">";
	public static final String PREFIX_ADAPTER_S0 = "<Adapter Session=\"0\" AdapterType=\"SystemAdmin\">";
	public static final String SURFIX_ADAPTER = "</Adapter>";
	public static final String PREFIX_COMMAND_ADD = "<Command Action=\"ADD\" ApplicationType=\"JMX\">";
	public static final String PREFIX_COMMAND_MODIFY = "<Command Action=\"MODIFY\" ApplicationType=\"JMX\">";
	public static final String SURFIX_COMMAND = "</Command>";
	
	private Log log = LogFactory.getLog(this.getClass());
	private MsgProducer producer;
	private boolean _isQuery = true;
	private static final long RETRY_TIMEOUT = 3000; // 30 seconds
	public QueryMBEAN(){
		super();
	}
	
	public void run(){
		while(_isQuery){
			while((XMLProcessing.htHosts.size() < 1) && (XMLProcessing.listServices.size() < 1)){
				try {
					Thread.sleep(RETRY_TIMEOUT);
					if((XMLProcessing.htHosts.size() > 0) && (XMLProcessing.listServices.size() > 0)){
						break;
					}
				}
				catch (Exception ex)
				{
					log.error(ex);
				}				
			}
			
			String message = "";
			Iterator<Service> ite = XMLProcessing.listServices.iterator();
			while(ite.hasNext()){
				Service service = ite.next();
				if(service.isQuery()){
					String hostName = service.getHostName();
					String objName = service.getMBEAN();
					String attribute = service.getAttribute();
					String key = service.getKey();
					//String serviceDescription = service.get(XMLProcessing.SERVICE_DESCRIPTION);
	//				System.out.println("............Quering with host:" + hostName + " and service: " + serviceDescription);
					if(XMLProcessing.htHosts.containsKey(hostName)){
						if(!XMLProcessing.htHosts.get(hostName).hasConnection()){
							XMLProcessing.htHosts.get(hostName).createConnnection();
						}
						Host h = XMLProcessing.htHosts.get(hostName);
						JMXConnector con = h.getConnection();
						if(con != null){
							try{
								MBeanServerConnection jmxMBeanServer = con.getMBeanServerConnection();
								ObjectName oName = new ObjectName(objName);
								Object oA = jmxMBeanServer.getAttribute(oName, attribute);
								String result = "";
								if(oA instanceof CompositeDataSupport){
									if((key != null) && key.trim().length() > 0){
										CompositeDataSupport cds = (CompositeDataSupport)oA;
										result = cds.get(key).toString();
									}else{
										log.error("Must specify the key for this type of attribute");
									}
								}else{
									result = oA.toString();
								}
								
								service.updateValue(result);
								String rm = "";
								rm = service.getResultMessage();
								message = message + rm;
		
							}catch(Exception e){
								log.error("Could not query MBEAN: " + objName + " at host: " + hostName + " " + e.getMessage() + e.getCause());
								e.printStackTrace();
							}
						}else{
							log.warn("Could not create JMX connection to host: " + hostName);
						}
					}
				}
			}
			if(message.trim().length() > 0){
				message = PREFIX_ADAPTER + PREFIX_COMMAND_MODIFY + message + SURFIX_COMMAND + SURFIX_ADAPTER;
				try{
					producer = new MsgProducer(message);
					producer.start();
					producer.join();
					producer.unInitialize();
				}catch(Exception e){
					log.error("Error while sending result message to JMS queue", e);
				}
				
			}
			
			try{
				Thread.sleep(60000);
			}catch(Exception e){
				log.warn(e);
			}
		}
	}
	
	public void unInitialize(){
		_isQuery = false;
	}

}
