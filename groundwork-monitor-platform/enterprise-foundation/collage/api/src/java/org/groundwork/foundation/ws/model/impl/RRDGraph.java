/**
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

/**
 * Wrapper object for an image file extracted from an RRD graph
 * 
 * @author rruttimann@gwos.com
 *
 */
public class RRDGraph implements java.io.Serializable {
	/**
	 * 
	 */
	public static final String CODE_INTERNAL_ERROR = "Graph Command Failed";
	public static final String CODE_NOTHING_RETURNED = "No graph available";
	public static final String CODE_SUCCESS = "SUCCESS";

	private static final long serialVersionUID = 1L;
	private String rrdLabel = null;
	private byte[] graph = null;
	
	
	

	public RRDGraph ()
	{
	
	}
		
	public RRDGraph (String rrdLabel)
	{
		this.rrdLabel = rrdLabel;
	}
	
	public RRDGraph (String rrdLabel, byte[] graph )
	{		
		this.rrdLabel = rrdLabel;
		this.graph = graph;
		
	}

	
	/**
	 * @return the rrdLabel
	 */
	public String getRrdLabel() {
		return rrdLabel;
	}

	/**
	 * @param rrdLabel the rrdLabel to set
	 */
	public void setRrdLabel(String rrdLabel) {
		this.rrdLabel = rrdLabel;
	}

	/**
	 * @return the graph
	 */
	public byte[] getGraph() {
		return graph;
	}

	/**
	 * @param graph the graph to set
	 */
	public void setGraph(byte[] graph) {
		this.graph = graph;
	}
	
	
    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof RRDGraph)) return false;
        RRDGraph other = (RRDGraph) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
           
            ((this.rrdLabel==null && other.getGraph()==null) || 
             (this.rrdLabel!=null &&
              this.rrdLabel.equals(other.getGraph())) &&
              
          
              ((this.graph==null && other.getGraph()==null) || 
                      (this.graph!=null &&
                       java.util.Arrays.equals(this.graph, other.getGraph()))));
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
       
        if (getRrdLabel() != null) {
            _hashCode += getRrdLabel().hashCode();
        }
        if (getGraph() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getGraph());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getGraph(), i);
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
        new org.apache.axis.description.TypeDesc(RRDGraph.class, true);

    static {
    	typeDesc.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "RRDGraph"));
    	
    	org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();        
        elemField.setFieldName("rrdLabel");
        elemField.setXmlName(new javax.xml.namespace.QName("", "RRDLabel"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("graph");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Graph"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "base64Binary"));
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
    
    public byte getGraph(int i) {
        return this.graph[i];
    }

    public void getGraph(int i, byte _value) {
        this.graph[i] = _value;
    }

}
