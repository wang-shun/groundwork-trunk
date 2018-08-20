/*
 * Collage - The ultimate data integration framework. Copyright (C) 2004-2009
 * GroundWork Open Source Solutions info@groundworkopensource.com
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published by the
 * Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 */
package org.groundwork.foundation.ws.model.impl;

import java.util.Date;

// TODO: Auto-generated Javadoc
/**
 * The Class SimpleServiceStatus.
 */
public class SimpleServiceStatus implements java.io.Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The service status id. */
    private int serviceStatusID;

    /** The description. */
    private String description;

    /** The monitor status. */
    private String monitorStatus;

    /** The last check time. */
    private Date lastCheckTime;

    /** The last state change. */
    private Date lastStateChange;
    /** To store the next check time for a service. */
    private Date nextCheckTime;

    /** The acknowledged. */
    private boolean acknowledged;

    /** The host name. */
    private String hostName;

    /** The host id. */
    private int hostId;

    /** The last plug in output. */
    private String lastPlugInOutput;

    /**
     * Instantiates a new simple service status.
     */
    public SimpleServiceStatus() {
    }

    /**
     * Instantiates a new simple service status.
     * 
     * @param serviceStatusID
     *            the service status id
     * @param description
     *            the description
     * @param monitorStatus
     *            the monitor status
     * @param lastCheckTime
     *            the last check time
     * @param nextCheckTime
     *            the next check time
     * @param acknowledged
     *            the acknowledged
     * @param lastStateChange
     *            the last state change
     * @param hostName
     *            the host name
     * @param hostId
     *            the host id
     * @param lastPlugInOutput
     *            the last plug in output
     */
    public SimpleServiceStatus(int serviceStatusID, String description,
            String monitorStatus, Date lastCheckTime, Date nextCheckTime,
            boolean acknowledged, Date lastStateChange, String hostName,
            int hostId, String lastPlugInOutput) {
        this.serviceStatusID = serviceStatusID;

        this.description = description;

        this.monitorStatus = monitorStatus;
        this.lastCheckTime = lastCheckTime;
        this.nextCheckTime = nextCheckTime;
        this.acknowledged = acknowledged;
        this.lastStateChange = lastStateChange;
        this.hostName = hostName;
        this.hostId = hostId;
        this.lastPlugInOutput = lastPlugInOutput;
    }

    /**
     * Gets the serviceStatusID value for this ServiceStatus.
     * 
     * @return serviceStatusID
     */
    public int getServiceStatusID() {
        return serviceStatusID;
    }

    /**
     * Sets the serviceStatusID value for this ServiceStatus.
     * 
     * @param serviceStatusID
     *            the service status id
     */
    public void setServiceStatusID(int serviceStatusID) {
        this.serviceStatusID = serviceStatusID;
    }

    /**
     * Gets the description value for this ServiceStatus.
     * 
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description value for this ServiceStatus.
     * 
     * @param description
     *            the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the monitorStatus value for this ServiceStatus.
     * 
     * @return monitorStatus
     */
    public String getMonitorStatus() {
        return monitorStatus;
    }

    /**
     * Sets the monitorStatus value for this ServiceStatus.
     * 
     * @param monitorStatus
     *            the monitor status
     */
    public void setMonitorStatus(String monitorStatus) {
        this.monitorStatus = monitorStatus;
    }

    /**
     * Gets the lastCheckTime value for this ServiceStatus.
     * 
     * @return lastCheckTime
     */
    public Date getLastCheckTime() {
        return lastCheckTime;
    }

    /**
     * Sets the lastCheckTime value for this ServiceStatus.
     * 
     * @param lastCheckTime
     *            the last check time
     */
    public void setLastCheckTime(Date lastCheckTime) {
        this.lastCheckTime = lastCheckTime;
    }

    /**
     * Gets the nextCheckTime value for this Service.
     * 
     * @return nextCheckTime
     */
    public Date getNextCheckTime() {
        return nextCheckTime;
    }

    /**
     * Sets the nextCheckTime value for this Service.
     * 
     * @param nextCheckTime
     *            the next check time
     */
    public void setNextCheckTime(Date nextCheckTime) {
        this.nextCheckTime = nextCheckTime;
    }

    /** The __equals calc. */
    private java.lang.Object __equalsCalc = null;

    /**
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SimpleServiceStatus))
            return false;
        SimpleServiceStatus other = (SimpleServiceStatus) obj;
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
                && this.serviceStatusID == other.getServiceStatusID()
                && ((this.description == null && other.getDescription() == null) || (this.description != null && this.description
                        .equals(other.getDescription())))
                && this.acknowledged == other.isAcknowledged()
                && ((this.monitorStatus == null && other.getMonitorStatus() == null) || (this.monitorStatus != null && this.monitorStatus
                        .equals(other.getMonitorStatus())))
                && ((this.lastStateChange == null && other.getLastStateChange() == null) || (this.lastStateChange != null && this.lastStateChange
                        .equals(other.getLastStateChange())))
                && ((this.lastCheckTime == null && other.getLastCheckTime() == null) || (this.lastCheckTime != null && this.lastCheckTime
                        .equals(other.getLastCheckTime())))
                && ((this.hostName == null && other.getHostName() == null) || (this.hostName != null && this.hostName
                        .equals(other.getHostName())))
                && this.hostId == other.getHostId()
                && ((this.lastPlugInOutput == null && other
                        .getLastPlugInOutput() == null) || (this.lastPlugInOutput != null && this.lastPlugInOutput
                        .equals(other.getLastPlugInOutput())))
                && ((this.nextCheckTime == null && other.getNextCheckTime() == null) || (this.nextCheckTime != null && this.nextCheckTime
                        .equals(other.getNextCheckTime())));
        __equalsCalc = null;
        return _equals;
    }

    /** The __hash code calc. */
    private boolean __hashCodeCalc = false;

    /**
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        _hashCode += getServiceStatusID();

        if (getDescription() != null) {
            _hashCode += getDescription().hashCode();
        }

        if (getMonitorStatus() != null) {
            _hashCode += getMonitorStatus().hashCode();
        }
        if (getLastCheckTime() != null) {
            _hashCode += getLastCheckTime().hashCode();
        }
        if (getNextCheckTime() != null) {
            _hashCode += getNextCheckTime().hashCode();
        }
        if (getLastPlugInOutput() != null) {
            _hashCode += getLastPlugInOutput().hashCode();
        }
        if (getLastStateChange() != null) {
            _hashCode += getLastStateChange().hashCode();
        }
        if (getHostName() != null) {
            _hashCode += getHostName().hashCode();
        }

        _hashCode += getHostId();

        _hashCode += (isAcknowledged() ? Boolean.TRUE : Boolean.FALSE)
                .hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    /** The type desc. */
    private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
            SimpleServiceStatus.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName(
                "http://model.ws.foundation.groundwork.org",
                "SimpleServiceStatus"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("serviceStatusID");
        elemField.setXmlName(new javax.xml.namespace.QName("",
                "ServiceStatusID"));
        elemField.setXmlType(new javax.xml.namespace.QName(
                "http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);

        typeDesc.addFieldDesc(elemField);

        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("description");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Description"));
        elemField.setXmlType(new javax.xml.namespace.QName(
                "http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);

        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("monitorStatus");
        elemField
                .setXmlName(new javax.xml.namespace.QName("", "MonitorStatus"));
        elemField.setXmlType(new javax.xml.namespace.QName(
                "http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);

        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lastCheckTime");
        elemField
                .setXmlName(new javax.xml.namespace.QName("", "LastCheckTime"));
        elemField.setXmlType(new javax.xml.namespace.QName(
                "http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);

        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("acknowledged");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Acknowledged"));
        elemField.setXmlType(new javax.xml.namespace.QName(
                "http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);

        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lastStateChange");
        elemField.setXmlName(new javax.xml.namespace.QName("",
                "LastStateChange"));
        elemField.setXmlType(new javax.xml.namespace.QName(
                "http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);

        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("hostName");
        elemField.setXmlName(new javax.xml.namespace.QName("", "HostName"));
        elemField.setXmlType(new javax.xml.namespace.QName(
                "http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);

        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("hostId");
        elemField.setXmlName(new javax.xml.namespace.QName("", "HostId"));
        elemField.setXmlType(new javax.xml.namespace.QName(
                "http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);

        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("lastPlugInOutput");
        elemField.setXmlName(new javax.xml.namespace.QName("",
                "LastPlugInOutput"));
        elemField.setXmlType(new javax.xml.namespace.QName(
                "http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);

        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("nextCheckTime");
        elemField
                .setXmlName(new javax.xml.namespace.QName("", "NextCheckTime"));
        elemField.setXmlType(new javax.xml.namespace.QName(
                "http://www.w3.org/2001/XMLSchema", "dateTime"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object.
     * 
     * @return the type desc
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer.
     * 
     * @param mechType
     *            the mech type
     * @param _javaType
     *            the _java type
     * @param _xmlType
     *            the _xml type
     * 
     * @return the serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
            java.lang.String mechType, java.lang.Class _javaType,
            javax.xml.namespace.QName _xmlType) {
        return new org.apache.axis.encoding.ser.BeanSerializer(_javaType,
                _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer.
     * 
     * @param mechType
     *            the mech type
     * @param _javaType
     *            the _java type
     * @param _xmlType
     *            the _xml type
     * 
     * @return the deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
            java.lang.String mechType, java.lang.Class _javaType,
            javax.xml.namespace.QName _xmlType) {
        return new org.apache.axis.encoding.ser.BeanDeserializer(_javaType,
                _xmlType, typeDesc);
    }

    /**
     * Checks if is acknowledged.
     * 
     * @return true, if is acknowledged
     */
    public boolean isAcknowledged() {
        return acknowledged;
    }

    /**
     * Sets the acknowledged.
     * 
     * @param acknowledged
     *            the new acknowledged
     */
    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

    /**
     * Gets the last state change.
     * 
     * @return the last state change
     */
    public Date getLastStateChange() {
        return lastStateChange;
    }

    /**
     * Sets the last state change.
     * 
     * @param lastStateChange
     *            the new last state change
     */
    public void setLastStateChange(Date lastStateChange) {
        this.lastStateChange = lastStateChange;
    }

    /**
     * Gets the host name.
     * 
     * @return the host name
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * Sets the host name.
     * 
     * @param hostName
     *            the new host name
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * Gets the host id.
     * 
     * @return the host id
     */
    public int getHostId() {
        return hostId;
    }

    /**
     * Sets the host id.
     * 
     * @param hostId
     *            the new host id
     */
    public void setHostId(int hostId) {
        this.hostId = hostId;
    }

    /**
     * Gets the last plug in output.
     * 
     * @return the last plug in output
     */
    public String getLastPlugInOutput() {
        return lastPlugInOutput;
    }

    /**
     * Sets the last plug in output.
     * 
     * @param lastPlugInOutput
     *            the new last plug in output
     */
    public void setLastPlugInOutput(String lastPlugInOutput) {
        this.lastPlugInOutput = lastPlugInOutput;
    }

}
