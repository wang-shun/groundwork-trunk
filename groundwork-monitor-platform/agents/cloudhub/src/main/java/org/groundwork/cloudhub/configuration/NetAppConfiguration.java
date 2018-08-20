package org.groundwork.cloudhub.configuration;

import org.groundwork.agents.monitor.VirtualSystem;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * NetappConfiguration class stores all the configuration parameters required in
 * the configuration part for NetApp
 */
@XmlRootElement(name = "vema")
@XmlType(propOrder = { "connection" })
public class NetAppConfiguration extends ConnectionConfiguration {

    @Valid
    private NetAppConnection connection;

    public NetAppConfiguration() {

        super(VirtualSystem.NETAPP);
        connection = new NetAppConnection();
    }

    @XmlElement(name = "netapp")
    public NetAppConnection getConnection() {
        return connection;
    }

    public void setConnection(NetAppConnection connection) {
        this.connection = connection;
    }

}
