package org.groundwork.cloudhub.configuration;

import org.groundwork.agents.monitor.MonitorConnectionConfig;
import org.groundwork.agents.monitor.VirtualSystem;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = {"common", "gwos"})
public abstract class ConnectionConfiguration implements MonitorConnectionConfig {

	@Valid
    private CommonConfiguration common;
	@Valid
    private GWOSConfiguration gwos;

    public ConnectionConfiguration() {
        common = new CommonConfiguration();
        gwos = new GWOSConfiguration();
    }

    public ConnectionConfiguration(VirtualSystem virtualSystem) {
        common = new CommonConfiguration(virtualSystem);
        gwos = new GWOSConfiguration();
    }

    public CommonConfiguration getCommon() {
        return common;
    }

    public void setCommon(CommonConfiguration common) {
        this.common = common;
        if (this instanceof ClouderaConfiguration) {
            ClouderaConfiguration clouderaConfiguration = (ClouderaConfiguration)this;
            clouderaConfiguration.getConnection().setPrefixServiceNames(common.getPrefixServiceNames());
        }
    }

    public GWOSConfiguration getGwos() {
        return gwos;
    }

    public void setGwos(GWOSConfiguration gwos) {
        this.gwos = gwos;
    }

    public abstract MonitorConnection getConnection();

    /**
     * Construct host key based on merge hosts configuration. Requires
     * that merge hosts configuration is set.
     *
     * @param host GWOS host name
     * @return host key
     */
    public String makeHostKey(String host) {
        return (getGwos().isMergeHosts() ? host.toLowerCase() : host);
    }

    public static final String makePath(ConnectionConfiguration configuration) {
        StringBuilder filePath = new StringBuilder();
        filePath.append(configuration.getCommon().getPathToConfigurationFile());
        if (!configuration.getCommon().getPathToConfigurationFile().endsWith("/"))
            filePath.append("/");
        filePath.append(configuration.getCommon().getConfigurationFile());
        return filePath.toString();
    }

}

