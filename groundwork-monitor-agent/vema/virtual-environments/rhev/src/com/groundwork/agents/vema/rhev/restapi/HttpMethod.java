//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.02.07 at 03:07:31 PM PST 
//


package com.groundwork.agents.vema.rhev.restapi;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for HttpMethod.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="HttpMethod">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="GET"/>
 *     &lt;enumeration value="POST"/>
 *     &lt;enumeration value="PUT"/>
 *     &lt;enumeration value="DELETE"/>
 *     &lt;enumeration value="OPTIONS"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "HttpMethod")
@XmlEnum
public enum HttpMethod {

    GET,
    POST,
    PUT,
    DELETE,
    OPTIONS;

    public String value() {
        return name();
    }

    public static HttpMethod fromValue(String v) {
        return valueOf(v);
    }

}
