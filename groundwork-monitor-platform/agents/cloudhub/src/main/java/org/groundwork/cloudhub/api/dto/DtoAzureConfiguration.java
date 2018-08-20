package org.groundwork.cloudhub.api.dto;

import org.groundwork.cloudhub.configuration.*;

import java.util.List;

/**
 * Created by dtaylor on 5/31/17.
 */
public class DtoAzureConfiguration {

    private DtoConfigurationState state;
    private AzureConfiguration configuration;

    public DtoAzureConfiguration() {
        this.state = new DtoConfigurationState();
        this.configuration = new AzureConfiguration();
    }

    public DtoAzureConfiguration(AzureConfiguration configuration, Boolean isConnected) {
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

    public AzureConnection getConnection() {
        return configuration.getConnection();
    }

    public void setConnection(AzureConnection connection) {
        configuration.setConnection(connection);
    }

    public List<ConfigurationView> getViews() {
        return configuration.getViews();
    }

    public void setViews(List<ConfigurationView> views) {
        configuration.setViews(views);
    }
    
}
