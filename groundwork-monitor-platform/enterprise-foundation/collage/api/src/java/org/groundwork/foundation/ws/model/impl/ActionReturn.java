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

public class ActionReturn 
{
	private int actionID;
	private String returnCode;
	private String returnValue;
	 	
	public ActionReturn () {}
	 
	public ActionReturn (int actionID, String returnCode, String returnValue)
	{
		 this.actionID = actionID;
		 this.returnCode = returnCode;		 
		 this.returnValue = returnValue;
	}
		 
	/**
	 * Gets the actionID value for this ActionPerform.
	 * 
	 * @return actionID
	*/
	public int getActionID() {
	    return actionID;
	}	
	
	/**
	 * Sets the actionID value for this ActionPerform.
	 * 
	 * @param deviceID
	 */
	public void setActionID(int actionID) {
	    this.actionID = actionID;
	}

	public String getReturnCode() {
		return returnCode;
	}
	
	public void setReturnCode(String returnCode) {
		this.returnCode = returnCode;
	}
	
	public String getReturnValue() {
		return returnValue;
	}
	
	public void setReturnValue(String returnValue) {
		this.returnValue = returnValue;
	}
	 
	private java.lang.Object __equalsCalc = null;
	public synchronized boolean equals(java.lang.Object obj) {
	    if (!(obj instanceof Device)) return false;
	    ActionReturn other = (ActionReturn) obj;
	    if (obj == null) return false;
	    if (this == obj) return true;
	    if (__equalsCalc != null) {
	        return (__equalsCalc == obj);
	    }
	    __equalsCalc = obj;
	    boolean _equals;
	    _equals = true && 
	        this.actionID == other.getActionID() &&
            ((this.returnCode==null && other.getReturnCode()==null) || 
                    (this.returnCode!=null &&
                     this.returnCode.equals(other.getReturnCode()))) &&
            ((this.returnValue==null && other.getReturnValue()==null) || 
             (this.returnValue!=null &&
              this.returnValue.equals(other.getReturnValue())));
	
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
	    _hashCode += getActionID();
        if (getReturnCode() != null) {
            _hashCode += getReturnCode().hashCode();
        } 
        if (getReturnValue() != null) {
            _hashCode += getReturnValue().hashCode();
        } 
	    __hashCodeCalc = false;
	    return _hashCode;
	 }
	
	 // Type metadata
	 private static org.apache.axis.description.TypeDesc typeDesc =
	    new org.apache.axis.description.TypeDesc(ActionReturn.class, true);
	
	 static {
	    typeDesc.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "ActionReturn"));
	    org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
	    elemField.setFieldName("actionID");
	    elemField.setXmlName(new javax.xml.namespace.QName("", "ActionID"));
	    elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
	    elemField.setNillable(false);
	    typeDesc.addFieldDesc(elemField);
	    elemField = new org.apache.axis.description.ElementDesc();
	    elemField.setFieldName("returnCode");
	    elemField.setXmlName(new javax.xml.namespace.QName("", "ReturnCode"));
	    elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
	    elemField.setNillable(false);
	    typeDesc.addFieldDesc(elemField);
	    elemField = new org.apache.axis.description.ElementDesc();
	    elemField.setFieldName("returnValue");
	    elemField.setXmlName(new javax.xml.namespace.QName("", "ReturnValue"));
	    elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
	    elemField.setNillable(false);
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


}