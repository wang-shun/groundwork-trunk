package org.groundwork.foundation.jmx;

/**
 * 
 * @author Vong Tran (vong.tran@gmail.com)
 *
 */

public class Service {
	//Attributes for ServiceCheck
	//private boolean active;
	private String hostName;
	private String device;
	private String serviceDescription;
	private String mBean;
	private String attribute;
	private String key;
	private String warningThresold;
	private String criticalThresold;
	private String status = "";
	private String value;
	private String resultMessage = "";
	private int interval = 1;
	private int query = 1;
	
	private MsgProducer producer;
	private static final String OK_LOG_MESSAGE = "OK - JMX Connection established on host: ";
	private static final String WARNING_LOG_MESSAGE = "WARNING - Fail to establish JMX Connection on host: ";
	private static final String CRITICAL_LOG_MESSAGE = "CRITICAL - JMX Connection established on host: ";
	private static final String UNKNOWN_LOG_MESSAGE = "UNKNOWN - Could not query value for service: ";
	
	
	/**
	 * Constructor for ServiceCheck
	 */
	public Service(){
		
	}
	
	/**
	 * Constructor for ServiceCheck with active, mBean, attribute, key, warningThresold, criticalThresold
	 * @param active
	 * @param mBean
	 * @param attribute
	 * @param key
	 * @param warningThresold
	 * @param criticalThresold
	 */
	public Service(String hostName, String serviceDescription, String mBean, String attribute, String key, String warningThresold, String criticalThresold, int interval){
		if(mBean == null){
			throw new NullPointerException("mBean requires not null");
		}else if(mBean.trim().length() < 1){
			throw new IllegalArgumentException("mBean requires not empty string");
		}
		if(attribute == null){
			throw new NullPointerException("attribute requires not null");
		}else if(attribute.trim().length() < 1){
			throw new IllegalArgumentException("attribute requires not empty string");
		}
		this.hostName = hostName;
		this.serviceDescription = serviceDescription;
		this.mBean = mBean;
		this.attribute = attribute;
		this.key = key;
		this.warningThresold = warningThresold;
		this.criticalThresold = criticalThresold;
		this.interval = interval;
	}
	
	/**
	 * Set active attribute for ServiceCheck
	 * @param active
	 */
//	public void setActive(boolean active){
//		this.active = active;
//	}
	
	/**
	 * Set MBEAN value for ServiceCheck
	 * @param mBean
	 */
	public void setMBEAN(String mBean){
		if(mBean == null){
			throw new NullPointerException("mBean requires not null");
		}else if(mBean.trim().length() < 1){
			throw new IllegalArgumentException("mBean requires not empty string");
		}
		this.mBean = mBean;
	}
	
	/**
	 * Set attribute attribute for ServiceCheck
	 * @param attribute
	 */
	public void setAttribute(String attribute){
		if(attribute == null){
			throw new NullPointerException("attribute requires not null");
		}else if(attribute.trim().length() < 1){
			throw new IllegalArgumentException("attribute requires not empty string");
		}
		this.attribute = attribute;
	}
	
	/**
	 * Set key attribute for ServiceCheck
	 * @param key
	 */
	public void setKey(String key){
		this.key = key;
	}
	
	/**
	 * Set warningThresold attribute for ServiceCheck
	 * @param warningThresold
	 */
	public void setWarningThresold(String warningThresold){
		this.warningThresold = warningThresold;
	}
	
	/**
	 * Set criticalThresold attribute for ServiceCheck
	 * @param criticalThresold
	 */
	public void setCriticalThresold(String criticalThresold){
		this.criticalThresold = criticalThresold;
	}
	
	/**
	 * Get active value
	 * @return
	 */
//	public boolean getActive(){
//		return this.active;
//	}
	
	/**
	 * Check active status
	 * @return
	 */
//	public boolean isActive(){
//		return this.active;
//	}
	
	public String getHostName(){
		return this.hostName;
	}
	/**
	 * Get MBean value
	 * @return
	 */
	public String getMBEAN(){
		return this.mBean;
	}
	
	/**
	 * Get Attribute value
	 * @return
	 */
	public String getAttribute(){
		return this.attribute;
	}
	
	/**
	 * Get key value
	 * @return
	 */
	public String getKey(){
		return this.key;
	}
	
	/**
	 * Get warning thresold
	 * @return
	 */
	public String getWarningThresold(){
		return this.warningThresold;
	}
	
	/**
	 * Get critical thresold
	 * @return
	 */
	public String getCriticalThresold(){
		return this.criticalThresold;
	}
	
	public String getDeviceName(){
		if(this.device == null){
			String device = XMLProcessing.htHosts.get(hostName).getDevice();
			this.device = device;
		}
		return this.device;
	}
	
	public String getResultMessage(){
		return this.resultMessage;
	}
	
	public void updateStatus(String status){
		if(this.status.compareToIgnoreCase(status) != 0){
			this.status = status;
			if(this.status.compareToIgnoreCase("ok") == 0){
				String textMessage = OK_LOG_MESSAGE + this.hostName;
				sendLogMessage(textMessage);
			}
			if(this.status.compareToIgnoreCase("warning") == 0){
				String textMessage = WARNING_LOG_MESSAGE + this.hostName;
				sendLogMessage(textMessage);
			}
			if(this.status.compareToIgnoreCase("critical") == 0){
				String textMessage = CRITICAL_LOG_MESSAGE + this.hostName;
				sendLogMessage(textMessage);
			}
			if(this.status.compareToIgnoreCase("unknow") == 0){
				String textMessage = UNKNOWN_LOG_MESSAGE + this.hostName;
				sendLogMessage(textMessage);
			}
		}
	}
	
	public void sendLogMessage(String textMessage){
		String device = getDeviceName();
		String logMessage = XMLProcessing.toServiceLogMessage(device, hostName, status, serviceDescription, textMessage);
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
	/**
	 * Update result value for service, check status and send Log Message if status changed.
	 * @param value
	 */
	public void updateValue(String value){
		this.value = value;
		String previousStatus = this.status;
		String textMessage = "";
		
		if(value == null){
			this.status = XMLProcessing.UNKNOWN_STATUS;
			textMessage = UNKNOWN_LOG_MESSAGE + this.serviceDescription;
			
		}else{
			if((this.warningThresold != null) || (this.criticalThresold != null)){
				textMessage = this.serviceDescription + ":" + this.value + ";" + this.warningThresold + ";" + this.criticalThresold;
			}else{
				textMessage = this.serviceDescription + ":" + this.value;
			}
			this.status = checkStatus();
		}
		
		this.resultMessage = XMLProcessing.toResultMessage(hostName, serviceDescription, status, textMessage);
		if(previousStatus.compareToIgnoreCase(this.status) !=0){
			sendLogMessage(textMessage);
		}
	}
	
	public String checkStatus(){
		String rStatus = "";
		if((warningThresold == null)&&(criticalThresold == null)){
			rStatus = XMLProcessing.OK_STATUS;
		}else{
			double dValue = (new Double(value)).doubleValue();
			if((warningThresold != null) && (criticalThresold == null)){
				double wThresold = (new Double(warningThresold)).doubleValue();
				if(dValue < wThresold){
					rStatus = XMLProcessing.OK_STATUS;
				}else{
					rStatus = XMLProcessing.WARNING_STATUS;
				}
				
			}
			if((warningThresold == null) && (criticalThresold != null)){
				double cThresold = (new Double(criticalThresold)).doubleValue();
				if(dValue < cThresold){
					rStatus = XMLProcessing.OK_STATUS;
				}else{
					rStatus = XMLProcessing.CRITICAL_STATUS;
				}
				
			}
			if((warningThresold != null) && (criticalThresold != null)){
				double wThresold = (new Double(warningThresold)).doubleValue();
				double cThresold = (new Double(criticalThresold)).doubleValue();
				if(wThresold < cThresold){
					if(dValue < wThresold){
						rStatus = XMLProcessing.OK_STATUS;
					}else{
						if(dValue < cThresold){
							rStatus = XMLProcessing.WARNING_STATUS;
						}else{
							rStatus = XMLProcessing.CRITICAL_STATUS;
						}
					}
				}else{
					if(dValue > wThresold){
						rStatus = XMLProcessing.OK_STATUS;
					}else{
						if(dValue > cThresold){
							rStatus = XMLProcessing.WARNING_STATUS;
						}else{
							rStatus = XMLProcessing.CRITICAL_STATUS;
						}
					}
				}
				
			}
		}
		return rStatus;
	}
	
	public String getStatus(){
		return this.status;
	}
	
	public boolean isQuery(){
		if(query == 1){
			query = interval;
			return true;
		}else{
			query = query - 1;
			return false;
		}
	}

}
