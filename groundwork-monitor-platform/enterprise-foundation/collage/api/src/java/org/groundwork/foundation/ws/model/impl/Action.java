package org.groundwork.foundation.ws.model.impl;

public class Action {
    private int actionID;

    private java.lang.String name;

    private java.lang.String description;

    private ApplicationType[] applicationTypes;
    
    public Action() {
    }

    public Action(
           int actionID,
           java.lang.String name,
           java.lang.String description,
           ApplicationType[] appTypes)
    {
       this.actionID = actionID;
       this.name = name;
       this.description = description;
       this.applicationTypes = appTypes;
    }


    /**
     * Gets the actionID value for this Action.
     * 
     * @return actionID
     */
    public int getActionID() {
        return actionID;
    }


    /**
     * Sets the actionID value for this Action.
     * 
     * @param deviceID
     */
    public void setActionID(int actionID) {
        this.actionID = actionID;
    }


    /**
     * Gets the name value for this Action.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this Action.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the description value for this Action.
     * 
     * @return description
     */
    public java.lang.String getDescription() {
        return description;
    }


    /**
     * Sets the description value for this Action.
     * 
     * @param description
     */
    public void setDescription(java.lang.String description) {
        this.description = description;
    }

    /**
     * Gets the hosts value for this Action.
     * 
     * @return hosts
     */
    public ApplicationType[] getApplicationTypes() {
        return applicationTypes;
    }

    /**
     * Sets the application types value for this Action.
     * 
     * @param appTypes
     */
    public void setApplicationTypes(ApplicationType[] appTypes) {
        this.applicationTypes = appTypes;
    }

    public ApplicationType getApplicationTypes(int i) {
        return this.applicationTypes[i];
    }

    public void setApplicationTypes(int i, ApplicationType _value) {
        this.applicationTypes[i] = _value;
    }
    
    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Device)) return false;
        Action other = (Action) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.actionID == other.getActionID() &&
            ((this.name==null && other.getName()==null) || 
             (this.name!=null &&
              this.name.equals(other.getName()))) &&
            ((this.description==null && other.getDescription()==null) || 
             (this.description!=null &&
              this.description.equals(other.getDescription()))) ||
            ((this.applicationTypes==null && other.getApplicationTypes()==null) || 
             (this.applicationTypes!=null &&
              java.util.Arrays.equals(this.applicationTypes, other.getApplicationTypes())));              

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
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getDescription() != null) {
            _hashCode += getDescription().hashCode();
        }
        if (getApplicationTypes() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getApplicationTypes());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getApplicationTypes(), i);
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
        new org.apache.axis.description.TypeDesc(Action.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "Action"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("actionID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "ActionID"));
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
        elemField.setFieldName("description");
        elemField.setXmlName(new javax.xml.namespace.QName("", "Description"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("applicationTypes");
        elemField.setXmlName(new javax.xml.namespace.QName("", "ApplicationTypes"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://model.ws.foundation.groundwork.org", "ApplicationType"));
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
