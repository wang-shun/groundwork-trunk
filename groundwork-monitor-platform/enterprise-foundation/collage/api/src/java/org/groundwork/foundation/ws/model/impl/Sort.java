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

import java.util.Arrays;

public class Sort  implements java.io.Serializable
{
    private SortItem[] sortItem;

    public Sort() {
    }

    public Sort(
           SortItem[] sortItem) {
           this.sortItem = sortItem;
    }
    
    public Sort ( boolean sortAscending,
           java.lang.String propertyName)
    {
    	if (propertyName == null || propertyName.length() == 0)
    		throw new IllegalArgumentException("Invalid null / empty property name.");
    	
    	sortItem = new SortItem[] {new SortItem(sortAscending, propertyName)};
    }

    /**
     * Gets the sortItem value for this Sort.
     * 
     * @return sortItem
     */
    public SortItem[] getSortItem() {
        return sortItem;
    }

    /**
     * Sets the sortItem value for this Sort.
     * 
     * @param sortItem
     */
    public void setSortItem(SortItem[] sortItem) {
        this.sortItem = sortItem;
    }

    public SortItem getSortItem(int i) {
        return this.sortItem[i];
    }

    public void setSortItem(int i, SortItem _value) {
        this.sortItem[i] = _value;
    }
    
    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Sort)) return false;
        Sort other = (Sort) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.sortItem==null && other.getSortItem()==null) || 
             (this.sortItem!=null &&
              java.util.Arrays.equals(this.sortItem, other.getSortItem())));
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
        if (getSortItem() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getSortItem());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getSortItem(), i);
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
        new org.apache.axis.description.TypeDesc(Sort.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "Sort"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("sortItem");
        elemField.setXmlName(new javax.xml.namespace.QName("", "SortItem"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "SortItem"));
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
