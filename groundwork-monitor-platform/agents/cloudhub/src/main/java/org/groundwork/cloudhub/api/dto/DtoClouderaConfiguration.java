package org.groundwork.cloudhub.api.dto;

import org.groundwork.cloudhub.configuration.ClouderaConfiguration;
import org.groundwork.cloudhub.configuration.ClouderaConnection;
import org.groundwork.cloudhub.configuration.CommonConfiguration;
import org.groundwork.cloudhub.configuration.ConfigurationView;
import org.groundwork.cloudhub.configuration.GWOSConfiguration;

import java.util.List;

/**
 * Created by dtaylor on 5/31/17.
 */
public class DtoClouderaConfiguration {

    private DtoConfigurationState state;
    private ClouderaConfiguration configuration;

    public DtoClouderaConfiguration() {
        this.state = new DtoConfigurationState();
        this.configuration = new ClouderaConfiguration();
    }

    public DtoClouderaConfiguration(ClouderaConfiguration configuration, Boolean isConnected) {
        this.state = new DtoConfigurationState(isConnected);
        this.configuration = configuration;
    }

    public DtoConfigurationState getState() {
        return state;
    }

    public void setState(DtoConfigurationState state) {
        this.state = state;
    }

    public CommonConfiguration getCommon() {
        return configuration.getCommon();
    }

    public void setCommon(CommonConfiguration common) {
        configuration.setCommon(common);
    }

    public GWOSConfiguration getGwos() {
        return configuration.getGwos();
    }

    public void setGwos(GWOSConfiguration gwos) {
        configuration.setGwos(gwos);
    }

    public ClouderaConnection getConnection() {
        return configuration.getConnection();
    }

    public void setConnection(ClouderaConnection connection) {
        configuration.setConnection(connection);
    }

    public List<ConfigurationView> getViews() {
        return configuration.getViews();
    }

    public void setViews(List<ConfigurationView> views) {
        configuration.setViews(views);
    }
    
}
