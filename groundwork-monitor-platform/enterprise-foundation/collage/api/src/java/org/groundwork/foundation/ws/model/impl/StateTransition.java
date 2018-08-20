package org.groundwork.foundation.ws.model.impl;

import java.io.Serializable;
import java.util.Date;


public class StateTransition implements Serializable {

	private String hostName = null;
	private String serviceDescription=null; 
	private MonitorStatus fromStatus = null;
	private Date fromTransitionDate = null;
	private MonitorStatus toStatus = null;
	private Date toTransitionDate = null;
	private Date endTransitionDate = null;
	private Long durationInState;

	public Long getDurationInState() {
		return durationInState;
	}

	public void setDurationInState(Long durationInState) {
		this.durationInState = durationInState;
	}
	
	public StateTransition() {}

	public StateTransition(String hostName, MonitorStatus fromStatus,
			Date fromTransitionDate, MonitorStatus toStatus,
			Date toTransitionDate, Date endTransitionDate, Long durationInState) {
		this.hostName = hostName;
		this.fromStatus = fromStatus;
		this.fromTransitionDate = fromTransitionDate;
		this.toStatus = toStatus;
		this.toTransitionDate = toTransitionDate;
		this.endTransitionDate = endTransitionDate;
		this.durationInState = durationInState;
	}
	
	public StateTransition(String hostName,String serviceDescription, MonitorStatus fromStatus,
			Date fromTransitionDate, MonitorStatus toStatus,
			Date toTransitionDate, Date endTransitionDate, Long durationInState) {
		this.hostName = hostName;
		this.serviceDescription = serviceDescription;
		this.fromStatus = fromStatus;
		this.fromTransitionDate = fromTransitionDate;
		this.toStatus = toStatus;
		this.toTransitionDate = toTransitionDate;
		this.endTransitionDate = endTransitionDate;
		this.durationInState = durationInState;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public MonitorStatus getFromStatus() {
		return fromStatus;
	}

	public void setFromStatus(MonitorStatus fromStatus) {
		this.fromStatus = fromStatus;
	}

	public Date getFromTransitionDate() {
		return fromTransitionDate;
	}

	public void setFromTransitionDate(Date fromTransitionDate) {
		this.fromTransitionDate = fromTransitionDate;
	}

	public MonitorStatus getToStatus() {
		return toStatus;
	}

	public void setToStatus(MonitorStatus toStatus) {
		this.toStatus = toStatus;
	}

	public Date getToTransitionDate() {
		return toTransitionDate;
	}

	public void setToTransitionDate(Date toTransitionDate) {
		this.toTransitionDate = toTransitionDate;
	}

	public Date getEndTransitionDate() {
		return endTransitionDate;
	}

	public void setEndTransitionDate(Date endTransitionDate) {
		this.endTransitionDate = endTransitionDate;
	}

	private java.lang.Object __equalsCalc = null;

	public synchronized boolean equals(java.lang.Object obj) {
		StateTransition other = (StateTransition) obj;
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		if (__equalsCalc != null) {
			return (__equalsCalc == obj);
		}
		__equalsCalc = obj;
		boolean _equals;
		_equals = true
				&& ((this.hostName == null && other.getHostName() == null) || (this.hostName != null && this.hostName
						.equals(other.getHostName())))
				&& ((this.serviceDescription == null && other.getServiceDescription() == null) || (this.serviceDescription != null && this.serviceDescription
						.equals(other.getServiceDescription())))		
				&& ((this.fromStatus == null && other.getFromStatus() == null) || (this.fromStatus != null && this.fromStatus
						.equals(other.getFromStatus())))
				&& ((this.fromTransitionDate == null && other
						.getFromTransitionDate() == null) || (this.fromTransitionDate != null && this.fromTransitionDate
						.equals(other.getFromTransitionDate())))
				&& ((this.toStatus == null && other.getToStatus() == null) || (this.toStatus != null && this.toStatus
						.equals(other.getToStatus())))
				&& ((this.toTransitionDate == null && other
						.getToTransitionDate() == null) || (this.toTransitionDate != null && this.toTransitionDate
						.equals(other.getToTransitionDate())))
				&& ((this.endTransitionDate == null && other
						.getEndTransitionDate() == null) || (this.endTransitionDate != null && this.endTransitionDate
						.equals(other.getEndTransitionDate())))
				&& ((this.durationInState == null && other
						.getDurationInState() == null) || (this.durationInState != null && this.durationInState
						.equals(other.getDurationInState())));
		__equalsCalc = null;
		return _equals;
	}

	private boolean __hashCodeCalc = false;

	public synchronized int hashCode() {
		if (__hashCodeCalc) {
			return 0;
		}
		__hashCodeCalc = true;
		int _hashCode = 1;
		_hashCode += getHostName().hashCode();
		if (getServiceDescription() != null) {
			_hashCode += getServiceDescription().hashCode();
		}
		if (getFromStatus() != null) {
			_hashCode += getFromStatus().hashCode();
		}
		if (getFromTransitionDate() != null) {
			_hashCode += getFromTransitionDate().hashCode();
		}
		if (getToStatus() != null) {
			_hashCode += getToStatus().hashCode();
		}
		if (getToTransitionDate() != null) {
			_hashCode += getToTransitionDate().hashCode();
		}
		if (getEndTransitionDate() != null) {
			_hashCode += getEndTransitionDate().hashCode();
		}
		if (getDurationInState() != null) {
			_hashCode += getDurationInState().hashCode();
		}

		__hashCodeCalc = false;
		return _hashCode;
	}

	// Type metadata
	private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
			StateTransition.class, true);

	static {
		typeDesc
				.setXmlType(new javax.xml.namespace.QName(
						"http://model.ws.foundation.groundwork.org",
						"StateTransition"));
		org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("hostName");
		elemField.setXmlName(new javax.xml.namespace.QName("", "HostName"));
		elemField.setXmlType(new javax.xml.namespace.QName(
				"http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("serviceDescription");
		elemField.setXmlName(new javax.xml.namespace.QName("", "ServiceDescription"));
		elemField.setXmlType(new javax.xml.namespace.QName(
				"http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("fromStatus");
		elemField.setXmlName(new javax.xml.namespace.QName("", "FromStatus"));
		elemField.setXmlType(new javax.xml.namespace.QName(
				"http://model.ws.foundation.groundwork.org", "MonitorStatus"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("fromTransitionDate");
		elemField.setXmlName(new javax.xml.namespace.QName("",
				"FromTransitionDate"));
		elemField.setXmlType(new javax.xml.namespace.QName(
				"http://www.w3.org/2001/XMLSchema", "dateTime"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("toStatus");
		elemField.setXmlName(new javax.xml.namespace.QName("", "ToStatus"));
		elemField.setXmlType(new javax.xml.namespace.QName(
				"http://model.ws.foundation.groundwork.org", "MonitorStatus"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("toTransitionDate");
		elemField.setXmlName(new javax.xml.namespace.QName("",
				"ToTransitionDate"));
		elemField.setXmlType(new javax.xml.namespace.QName(
				"http://www.w3.org/2001/XMLSchema", "dateTime"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("endTransitionDate");
		elemField.setXmlName(new javax.xml.namespace.QName("",
				"EndTransitionDate"));
		elemField.setXmlType(new javax.xml.namespace.QName(
				"http://www.w3.org/2001/XMLSchema", "dateTime"));
		elemField.setMinOccurs(0);
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("durationInState");
		elemField.setXmlName(new javax.xml.namespace.QName("",
				"DurationInState"));
		elemField.setXmlType(new javax.xml.namespace.QName(
				"http://www.w3.org/2001/XMLSchema", "long"));
		elemField.setMinOccurs(0);
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);

	}

	/**
	 * Return type metadata object
	 */
	public static org.apache.axis.description.TypeDesc getTypeDesc() {
		return typeDesc;
	}

	/**
	 * Get Custom Serializer
	 */
	public static org.apache.axis.encoding.Serializer getSerializer(
			java.lang.String mechType, java.lang.Class _javaType,
			javax.xml.namespace.QName _xmlType) {
		return new org.apache.axis.encoding.ser.BeanSerializer(_javaType,
				_xmlType, typeDesc);
	}

	/**
	 * Get Custom Deserializer
	 */
	public static org.apache.axis.encoding.Deserializer getDeserializer(
			java.lang.String mechType, java.lang.Class _javaType,
			javax.xml.namespace.QName _xmlType) {
		return new org.apache.axis.encoding.ser.BeanDeserializer(_javaType,
				_xmlType, typeDesc);
	}

	public String getServiceDescription() {
		return serviceDescription;
	}

	public void setServiceDescription(String serviceDescription) {
		this.serviceDescription = serviceDescription;
	}

}
