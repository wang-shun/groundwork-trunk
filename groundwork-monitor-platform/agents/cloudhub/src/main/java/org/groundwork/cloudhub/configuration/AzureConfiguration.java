package org.groundwork.cloudhub.configuration;

import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.cloudhub.connectors.azure.AzureConfigurationProvider;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

@XmlRootElement(name = "vema")
@XmlType(propOrder = { "connection", "views" })
public class AzureConfiguration extends ConnectionConfiguration implements SupportsExtendedViews {

    @Valid
    private  AzureConnection connection;

    private List<ConfigurationView> views;

    public AzureConfiguration() {
        super(VirtualSystem.AZURE);
        connection = new AzureConnection();
        setViews(AzureConfigurationProvider.createDefaultViews());
    }

    @XmlElement(name = "azure")
    public AzureConnection getConnection() {
        return connection;
    }

    public void setConnection(AzureConnection connection) {
        this.connection = connection;
    }

    @XmlElement(name="view", type=ConfigurationView.class)
    @XmlElementWrapper(name="views")
    public List<ConfigurationView> getViews() {
        return views;
    }

    public ConfigurationView getView(String viewName) {
        for (ConfigurationView view : views) {
            if (view.getName().equals(viewName)) {
                return view;
            }
        }
        return null;
    }

    public void setViews(List<ConfigurationView> views) {
        this.views = views;
    }
}
