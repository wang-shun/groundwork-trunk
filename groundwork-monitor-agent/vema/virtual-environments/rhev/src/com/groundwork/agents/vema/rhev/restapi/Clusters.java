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
 * <p>Java class for Clusters complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Clusters">
 *   &lt;complexContent>
 *     &lt;extension base="{}BaseResources">
 *       &lt;sequence>
 *         &lt;element ref="{}cluster" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Clusters", propOrder = {
    "clusters"
})
public class Clusters
    extends BaseResources
{

    @XmlElement(name = "cluster")
    protected List<Cluster> clusters;

    /**
     * Gets the value of the clusters property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the clusters property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getClusters().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Cluster }
     * 
     * 
     */
    public List<Cluster> getClusters() {
        if (clusters == null) {
            clusters = new ArrayList<Cluster>();
        }
        return this.clusters;
    }

    public boolean isSetClusters() {
        return ((this.clusters!= null)&&(!this.clusters.isEmpty()));
    }

    public void unsetClusters() {
        this.clusters = null;
    }

}
