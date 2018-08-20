//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.02.07 at 03:07:31 PM PST 
//


package org.groundwork.cloudhub.connectors.rhev.restapi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for PowerManagementStates complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PowerManagementStates">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="power_management_state" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PowerManagementStates", propOrder = {
    "powerManagementStates"
})
public class PowerManagementStates {

    @XmlElement(name = "power_management_state")
    protected List<String> powerManagementStates;

    /**
     * Gets the value of the powerManagementStates property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the powerManagementStates property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPowerManagementStates().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getPowerManagementStates() {
        if (powerManagementStates == null) {
            powerManagementStates = new ArrayList<String>();
        }
        return this.powerManagementStates;
    }

    public boolean isSetPowerManagementStates() {
        return ((this.powerManagementStates!= null)&&(!this.powerManagementStates.isEmpty()));
    }

    public void unsetPowerManagementStates() {
        this.powerManagementStates = null;
    }

}
