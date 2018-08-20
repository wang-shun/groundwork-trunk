package org.groundwork.cloudhub.configuration;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.groundwork.agents.monitor.VirtualSystem;

/**
 * AmazonConfiguration class stores all the configuration parameters required in
 * the configuration part for Amazon
 */
@XmlRootElement(name = "vema")
@XmlType(propOrder = { "connection" })
public class AmazonConfiguration extends ConnectionConfiguration {

    @Valid
    private AmazonConnection connection;

    public AmazonConfiguration() {

        super(VirtualSystem.AMAZON);
        connection = new AmazonConnection();
    }

    @XmlElement(name = "amazon")
    public AmazonConnection getConnection() {
        return connection;
    }

    public void setConnection(AmazonConnection connection) {
        this.connection = connection;
    }
}