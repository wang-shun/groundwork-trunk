//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.02.07 at 03:07:31 PM PST 
//


package com.groundwork.agents.vema.rhev.restapi;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CdRoms complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CdRoms">
 *   &lt;complexContent>
 *     &lt;extension base="{}BaseDevices">
 *       &lt;sequence>
 *         &lt;element ref="{}cdrom" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CdRoms", propOrder = {
    "cdRoms"
})
public class CdRoms
    extends BaseDevices
{

    @XmlElement(name = "cdrom")
    protected List<CdRom> cdRoms;

    /**
     * Gets the value of the cdRoms property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the cdRoms property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCdRoms().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CdRom }
     * 
     * 
     */
    public List<CdRom> getCdRoms() {
        if (cdRoms == null) {
            cdRoms = new ArrayList<CdRom>();
        }
        return this.cdRoms;
    }

    public boolean isSetCdRoms() {
        return ((this.cdRoms!= null)&&(!this.cdRoms.isEmpty()));
    }

    public void unsetCdRoms() {
        this.cdRoms = null;
    }

}
