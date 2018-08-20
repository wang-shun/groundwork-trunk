/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2009  GroundWork Open Source Solutions info@groundworkopensource.com

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

import java.util.Date;

public class SimpleHost implements java.io.Serializable 
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int hostID;

    private java.lang.String name;
    
    private String monitorStatus;
    
    private Date lastCheckTime;
    
    private String alias;
    
    private String bubbleUpStatus;
    
    private SimpleServiceStatus[] simpleServiceStatus;
    
    private Date lastStateChange;
    
    private double serviceAvailability;
    
    private boolean acknowledged;
    
    private String lastPlugInOutput;
    
    public Date getLastStateChange() {
		return lastStateChange;
	}

	public void setLastStateChange(Date lastStateChange) {
		this.lastStateChange = lastStateChange;
	}

	public SimpleHost() {
    }

    public SimpleHost(
           int hostID,
           java.lang.String name,
           String monitorStatus,
           Date lastCheckTime, String bubbleUpStatus,
           String alias,SimpleServiceStatus[] simpleServiceStatus, Date lastStateChange, double serviceAvailability, boolean acknowledged, String lastPlugInOutput) 
    {
           this.hostID = hostID;
           this.name = name;
           this.monitorStatus = monitorStatus;
           this.lastCheckTime = lastCheckTime;
           this.alias = alias;
           this.bubbleUpStatus = bubbleUpStatus;
           this.simpleServiceStatus = simpleServiceStatus;
           this.lastStateChange = lastStateChange;
           this.serviceAvailability = serviceAvailability;
           this.acknowledged = acknowledged;
           this.lastPlugInOutput = lastPlugInOutput;
    }


    /**
     * Gets the hostID value for this SimpleHost.
     * 
     * @return hostID
     */
    public int getHostID() {
        return hostID;
    }


    /**
     * Sets the hostID value for this SimpleHost.
     * 
     * @param hostID
     */
    public void setHostID(int hostID) {
        this.hostID = hostID;
    }




    /**
     * Gets the name value for this SimpleHost.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this SimpleHost.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }

    /**
     * Gets the monitorStatus value for this HostStatus.
     * 
     * @return monitorStatus
     */
    public String getMonitorStatus() 
    {
        return monitorStatus;
    }


    /**
     * Sets the monitorStatus value for this HostStatus.
     * 
     * @param monitorStatus
     */
    public void setMonitorStatus(String monitorStatus) {
        this.monitorStatus = monitorStatus;
    }
    
    /**
     * Gets the lastCheckTime value for this HostStatus.
     * 
     * @return lastCheckTime
     */
    public Date getLastCheckTime() 
    {
        return lastCheckTime;
    }


    /**
     * Sets the lastCheckTime value for this HostStatus.
     * 
     * @param lastCheckTime
     */
    public void setLastCheckTime(Date lastCheckTime) {
        this.lastCheckTime = lastCheckTime;
    }    
    
    /**
     * Gets the propertyTypeBinding value for this HostStatus.
     * 
     * @return propertyTypeBinding
     */
    public String getAlias() {
        return alias;
    }


    /**
     * Sets the propertyTypeBinding value for this HostStatus.
     * 
     * @param propertyTypeBinding
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }
    
    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SimpleHost)) return false;
        SimpleHost other = (SimpleHost) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.hostID == other.getHostID() &&
            this.serviceAvailability== other.getServiceAvailability() &&
             ((this.name==null && other.getName()==null) || 
             (this.name!=null &&
              this.name.equals(other.getName())) )&&
            ((this.monitorStatus==null && other.getMonitorStatus()==null) || 
             (this.monitorStatus!=null &&
              this.monitorStatus.equals(other.getMonitorStatus())))  &&
            ((this.alias==null && other.getAlias()== null) || 
             (this.alias!=null &&
              this.alias.equals(other.getAlias())) )  &&       
              ((this.bubbleUpStatus==null && other.getBubbleUpStatus()==null) || 
                      (this.bubbleUpStatus!=null &&
                       this.bubbleUpStatus.equals(other.getBubbleUpStatus())))  &&
            ((this.lastCheckTime==null && other.getLastCheckTime()==null) || 
             (this.lastCheckTime!=null && this.lastCheckTime.equals(other.getLastCheckTime()))) && 
             ((this.lastStateChange==null && other.getLastStateChange()==null) || 
                     (this.lastStateChange!=null && this.lastStateChange.equals(other.getLastStateChange()))) && 
                     this.acknowledged==other.isAcknowledged() &&
             ((this.simpleServiceStatus==null && other.getSimpleServiceStatus()==null) || 
                     (this.simpleServiceStatus!=null &&  java.util.Arrays.equals(this.simpleServiceStatus, other.getSimpleServiceStatus()))) && 
                     ((this.lastPlugInOutput==null && other.getLastPlugInOutput()==null) || 
                             (this.lastPlugInOutput!=null && this.lastPlugInOutput.equals(other.getLastPlugInOutput()))) ;                               
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
        _hashCode += getHostID();
        
        _hashCode += getServiceAvailability();
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getMonitorStatus() != null) {
            _hashCode += getMonitorStatus().hashCode();
        }   
        if (getLastCheckTime() != null) {
            _hashCode += getLastCheckTime().hashCode();
        }  
        if (getLastStateChange() != null) {
            _hashCode += getLastStateChange().hashCode();
        }  
        if (getLastPlugInOutput() != null) {
            _hashCode += getLastPlugInOutput().hashCode();
        }  
        if (getAlias() != null) {
            _hashCode += getAlias().hashCode();
        }  
        if (getBubbleUpStatus() != null) {
            _hashCode += getBubbleUpStatus().hashCode();
        }   
        if (getSimpleServiceStatus() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getSimpleServiceStatus());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getSimpleServiceStatus(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        _hashCode += (isAcknowledged() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SimpleHost.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "SimpleHost"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("hostID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "HostID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("name");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("monitorStatus");
        elemField.setXmlName(new javax.xml.namespace.QName("", "MonitorStatus"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);  
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lastCheckTime");
        elemField.setXmlName(new javax.xml.namespace.QName("", "LastCheckTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lastStateChange");
        elemField.setXmlName(new javax.xml.namespace.QName("", "LastStateChange"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("alias");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Alias"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField); 
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("bubbleUpStatus");
        elemField.setXmlName(new javax.xml.namespace.QName("", "BubbleUpStatus"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField); 
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("simpleServiceStatus");
        elemField.setXmlName(new javax.xml.namespace.QName("", "SimpleServiceStatus"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "SimpleServiceStatus"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("serviceAvailability");
        elemField.setXmlName(new javax.xml.namespace.QName("", "ServiceAvailability"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("acknowledged");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Acknowledged"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lastPlugInOutput");
        elemField.setXmlName(new javax.xml.namespace.QName("", "LastPlugInOutput"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

	public SimpleServiceStatus[] getSimpleServiceStatus() {
		return simpleServiceStatus;
	}

	public void setSimpleServiceStatus(SimpleServiceStatus[] simpleServiceStatus) {
		this.simpleServiceStatus = simpleServiceStatus;
	}
	
	public SimpleServiceStatus getSimpleServiceStatus(int i) {
		return this.simpleServiceStatus[i];
	}

	public void setSimpleServiceStatus(int i,SimpleServiceStatus _value) {
		this.simpleServiceStatus[i] = _value;
	}

	public String getBubbleUpStatus() {
		return bubbleUpStatus;
	}

	public void setBubbleUpStatus(String bubbleUpStatus) {
		this.bubbleUpStatus = bubbleUpStatus;
	}

	public double getServiceAvailability() {
		return serviceAvailability;
	}

	public void setServiceAvailability(double serviceAvailability) {
		this.serviceAvailability = serviceAvailability;
	}

	public boolean isAcknowledged() {
		return acknowledged;
	}

	public void setAcknowledged(boolean acknowledged) {
		this.acknowledged = acknowledged;
	}

	public String getLastPlugInOutput() {
		return lastPlugInOutput;
	}

	public void setLastPlugInOutput(String lastPlugInOutput) {
		this.lastPlugInOutput = lastPlugInOutput;
	}

	

}
