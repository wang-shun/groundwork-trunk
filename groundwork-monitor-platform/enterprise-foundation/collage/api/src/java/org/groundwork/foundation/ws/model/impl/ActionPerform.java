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

public class ActionPerform 
{
	 private int actionID;
	 private StringProperty[] parameters;
	 	
	 public ActionPerform () {}
	 
	 public ActionPerform (int actionID, StringProperty[] parameters)
	 {
		 this.actionID = actionID;
		 this.parameters = parameters;		 
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
	    
	 /**
	 * Gets the parameters value for this ActionPerform.
	 * 
	 * @return stringProperty
	 */
	 public StringProperty[] getParameters() {
	    return parameters;
	 }
		
	 /**
	  * Sets the parameters value for this ActionPerform.
	  * 
	  * @param stringProperty
	  */
	 public void setParameters(StringProperty[] parameters) {
	    this.parameters = parameters;
	 }
	
	 public StringProperty getParameter(int i) 
	 {
		if (this.parameters == null)
			throw new IndexOutOfBoundsException("Invalid parameter index - " + i);
		
	    return this.parameters[i];
	 }
	
	 public StringProperty getParameter(String propName) 
	 {	
		if (this.parameters == null)
			return null;
		
		for (int i = 0; i < this.parameters.length; i++)
		{
			if (this.parameters[i].getName().equalsIgnoreCase(propName))
				return this.parameters[i];
		}
	
		return null;
	 }
	
	 public void setParameter(int i, StringProperty _value) 
	 {
		if (this.parameters == null)
			throw new IndexOutOfBoundsException("Invalid parameter index - " + i);
			
	    this.parameters[i] = _value;
	 }	 	 
	 
	 private java.lang.Object __equalsCalc = null;
	 public synchronized boolean equals(java.lang.Object obj) {
	    if (!(obj instanceof Device)) return false;
	    ActionPerform other = (ActionPerform) obj;
	    if (obj == null) return false;
	    if (this == obj) return true;
	    if (__equalsCalc != null) {
	        return (__equalsCalc == obj);
	    }
	    __equalsCalc = obj;
	    boolean _equals;
	    _equals = true && 
	        this.actionID == other.getActionID() &&
	        ((this.parameters==null && other.getParameters()==null) || 
	                (this.parameters!=null &&
	                 java.util.Arrays.equals(this.parameters, other.getParameters())));         
	
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
	    if (getParameters() != null) {
	        for (int i=0;
	             i<java.lang.reflect.Array.getLength(getParameters());
	             i++) {
	            java.lang.Object obj = java.lang.reflect.Array.get(getParameters(), i);
	            if (obj != null &&
	                !obj.getClass().isArray()) {
	                _hashCode += obj.hashCode();
	            }
	        }
	    }    
	    __hashCodeCalc = false;
	    return _hashCode;
	 }
	
	 // Type metadata
	 private static org.apache.axis.description.TypeDesc typeDesc =
	    new org.apache.axis.description.TypeDesc(ActionPerform.class, true);
	
	 static {
	    typeDesc.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "ActionPerform"));
	    org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
	    elemField.setFieldName("actionID");
	    elemField.setXmlName(new javax.xml.namespace.QName("", "ActionID"));
	    elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
	    elemField.setNillable(false);
	    typeDesc.addFieldDesc(elemField);
	    elemField = new org.apache.axis.description.ElementDesc();
	    elemField.setFieldName("parameters");
	    elemField.setXmlName(new javax.xml.namespace.QName("", "Parameters"));
	    elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "StringProperty"));
	    elemField.setMinOccurs(0);
	    elemField.setNillable(false);
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