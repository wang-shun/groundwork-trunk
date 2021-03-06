//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.02.07 at 03:07:31 PM PST 
//


package org.groundwork.cloudhub.connectors.rhev.restapi;

import javax.xml.bind.annotation.*;


/**
 * <p>Java class for Version complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Version">
 *   &lt;complexContent>
 *     &lt;extension base="{}BaseResource">
 *       &lt;attribute name="major" type="{http://www.w3.org/2001/XMLSchema}unsignedShort" />
 *       &lt;attribute name="minor" type="{http://www.w3.org/2001/XMLSchema}unsignedShort" />
 *       &lt;attribute name="build" type="{http://www.w3.org/2001/XMLSchema}unsignedShort" />
 *       &lt;attribute name="revision" type="{http://www.w3.org/2001/XMLSchema}unsignedShort" />
 *       &lt;attribute name="full_version" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Version")
@XmlSeeAlso({
    VersionCaps.class
})
public class Version
    extends BaseResource
{

    @XmlAttribute
    @XmlSchemaType(name = "unsignedShort")
    protected Integer major;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedShort")
    protected Integer minor;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedShort")
    protected Integer build;
    @XmlAttribute
    @XmlSchemaType(name = "unsignedShort")
    protected Integer revision;
    @XmlAttribute(name = "full_version")
    protected String fullVersion;

    /**
     * Gets the value of the major property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMajor() {
        return major;
    }

    /**
     * Sets the value of the major property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMajor(Integer value) {
        this.major = value;
    }

    /**
     * Gets the value of the minor property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMinor() {
        return minor;
    }

    /**
     * Sets the value of the minor property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMinor(Integer value) {
        this.minor = value;
    }

    /**
     * Gets the value of the build property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getBuild() {
        return build;
    }

    /**
     * Sets the value of the build property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setBuild(Integer value) {
        this.build = value;
    }

    /**
     * Gets the value of the revision property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getRevision() {
        return revision;
    }

    /**
     * Sets the value of the revision property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setRevision(Integer value) {
        this.revision = value;
    }

    /**
     * Gets the value of the fullVersion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFullVersion() {
        return fullVersion;
    }

    /**
     * Sets the value of the fullVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFullVersion(String value) {
        this.fullVersion = value;
    }

}
