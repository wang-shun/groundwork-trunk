/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com

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
package org.groundwork.foundation.ws.model.impl;

public class StateStatistics implements java.io.Serializable {
	private long totalHosts;

	private long totalServices;

	private String name;

	private StatisticProperty[] statisticProperties;

	private String bubbleUpStatus;

	public StateStatistics() {
	}

	public StateStatistics(String name, String bubbleUpStatus, long totalHosts,
			long totalServices, StatisticProperty[] statisticProperties) {
		this.name = name;
		this.totalHosts = totalHosts;
		this.totalServices = totalServices;
		this.statisticProperties = statisticProperties;
	}

	/* Setters and Getters */

	/**
	 * @return Returns the statisticProperties.
	 */
	public StatisticProperty[] getStatisticProperties() {
		return statisticProperties;
	}

	/**
	 * @param statisticProperties
	 *            The statisticProperties to set.
	 */
	public void setStatisticProperties(StatisticProperty[] statisticProperties) {
		this.statisticProperties = statisticProperties;
	}

	/**
	 * @return Returns the totalHosts.
	 */
	public long getTotalHosts() {
		return totalHosts;
	}

	/**
	 * @param totalHosts
	 *            The totalHosts to set.
	 */
	public void setTotalHosts(long totalHosts) {
		this.totalHosts = totalHosts;
	}

	/**
	 * @return Returns the totalServices.
	 */
	public long getTotalServices() {
		return totalServices;
	}

	/**
	 * @param totalServices
	 *            The totalServices to set.
	 */
	public void setTotalServices(long totalServices) {
		this.totalServices = totalServices;
	}

	/**
	 * @return Returns the hostGroupName.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param hostGroupName
	 *            The hostGroupName to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	private java.lang.Object __equalsCalc = null;

	public synchronized boolean equals(java.lang.Object obj) {
		if (!(obj instanceof StateStatistics))
			return false;
		StateStatistics other = (StateStatistics) obj;
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
				&& this.totalHosts == other.getTotalHosts()
				&& this.totalServices == other.getTotalServices()
				&& ((this.bubbleUpStatus == null && other.getBubbleUpStatus() == null) || (this.bubbleUpStatus != null && this.bubbleUpStatus
						.equals(other.getBubbleUpStatus())))
				&& ((this.name == null && other.getName() == null) || (this.name != null && this.name
						.equals(other.getName())))
				&& ((this.statisticProperties == null && other
						.getStatisticProperties() == null) || (this.statisticProperties != null && java.util.Arrays
						.equals(this.getStatisticProperties(), other
								.getStatisticProperties())));
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
		_hashCode += getTotalHosts();
		_hashCode += getTotalServices();
		if (getStatisticProperties() != null) {
			for (int i = 0; i < java.lang.reflect.Array
					.getLength(getStatisticProperties()); i++) {
				java.lang.Object obj = java.lang.reflect.Array.get(
						getStatisticProperties(), i);
				if (obj != null && !obj.getClass().isArray()) {
					_hashCode += obj.hashCode();
				}
			}
		}
		if (getBubbleUpStatus() != null) {
			_hashCode += getBubbleUpStatus().hashCode();
		}
		if (getName() != null) {
			_hashCode += getName().hashCode();
		}
		__hashCodeCalc = false;
		return _hashCode;
	}

	// Type metadata
	private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
			StateStatistics.class, true);

	static {
		typeDesc
				.setXmlType(new javax.xml.namespace.QName(
						"http://model.ws.foundation.groundwork.org",
						"StateStatistics"));
		org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("name");
		elemField.setXmlName(new javax.xml.namespace.QName("", "Name"));
		elemField.setXmlType(new javax.xml.namespace.QName(
				"http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);

		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("bubbleUpStatus");
		elemField
				.setXmlName(new javax.xml.namespace.QName("", "BubbleUpStatus"));
		elemField.setXmlType(new javax.xml.namespace.QName(
				"http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(true);
		typeDesc.addFieldDesc(elemField);
		
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("totalServices");
		elemField
				.setXmlName(new javax.xml.namespace.QName("", "TotalServices"));
		elemField.setXmlType(new javax.xml.namespace.QName(
				"http://www.w3.org/2001/XMLSchema", "long"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		
		
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("totalHosts");
		elemField.setXmlName(new javax.xml.namespace.QName("", "TotalHosts"));
		elemField.setXmlType(new javax.xml.namespace.QName(
				"http://www.w3.org/2001/XMLSchema", "long"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		
		
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("statisticProperties");
		elemField.setXmlName(new javax.xml.namespace.QName("", "Statistic"));
		elemField.setXmlType(new javax.xml.namespace.QName(
				"http://model.ws.foundation.groundwork.org",
				"StatisticProperty"));
		elemField.setMinOccurs(0);
		elemField.setNillable(true);
		elemField.setMaxOccursUnbounded(true);
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

	/**
	 * Bubbleup status is calculated here
	 * @return
	 */
	public String getBubbleUpStatus() {
		if (statisticProperties != null && statisticProperties.length > 0) {
			String UNSCHEDULED_DOWN = "UNSCHEDULED DOWN";
			String WARNING = "WARNING";
			String PENDING = "PENDING";
			String UNREACHABLE = "UNREACHABLE";
			String SCHEDULED_DOWN = "SCHEDULED DOWN";
			String DOWN = "DOWN";
			String UP = "UP";
			
			// Ranking is done in the following order for host
			String[] ranking = { UNSCHEDULED_DOWN,DOWN, UNREACHABLE, SCHEDULED_DOWN, PENDING,
					  UP };
			for (int i = 0; i < ranking.length; i++) {
				if (checkStatus(ranking[i])) {
					bubbleUpStatus = ranking[i];
					break;
				} // end if
			} // end if
			if (bubbleUpStatus == null) {
				String UNSCHEDULED_CRITICAL = "UNSCHEDULED CRITICAL";
				String SCHEDULED_CRITICAL = "SCHEDULED CRITICAL";
				String CRITICAL = "CRITICAL";
				String UNKNOWN = "UNKNOWN";
				String OK = "OK";
				// Ranking is done in the following order for services
				String[] rankingServices = { UNSCHEDULED_CRITICAL, CRITICAL, WARNING,
						SCHEDULED_CRITICAL, 
						UNKNOWN, OK };
				for (int i = 0; i < rankingServices.length; i++) {
					if (checkStatus(rankingServices[i])) {
						bubbleUpStatus = rankingServices[i];
						break;
					} // end if
				} // end if
			} // end if
		}
		return bubbleUpStatus;
	}

	private boolean checkStatus(String monitorStatus) {
		boolean result = false;
		for (int i = 0; i < statisticProperties.length; i++) {
			StatisticProperty prop = statisticProperties[i];
			if (prop.getName() != null
					&& (prop.getName().equalsIgnoreCase(monitorStatus))
					&& prop.getCount() > 0) {
				result = true;
				break;
			}
		}
		return result;
	}

	public void setBubbleUpStatus(String bubbleUpStatus) {
		this.bubbleUpStatus = bubbleUpStatus;
	}

}
