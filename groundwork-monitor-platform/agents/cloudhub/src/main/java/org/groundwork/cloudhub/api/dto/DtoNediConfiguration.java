package org.groundwork.cloudhub.api.dto;

import org.groundwork.cloudhub.configuration.CommonConfiguration;
import org.groundwork.cloudhub.configuration.GWOSConfiguration;
import org.groundwork.cloudhub.configuration.NediConfiguration;
import org.groundwork.cloudhub.configuration.NediConnection;

/**
 * Created by dtaylor on 5/31/17.
 */
public class DtoNediConfiguration {

    private DtoConfigurationState state;
    private NediConfiguration configuration;

    public DtoNediConfiguration() {
        this.state = new DtoConfigurationState();
        this.configuration = new NediConfiguration();
    }

    public DtoNediConfiguration(NediConfiguration configuration, Boolean isConnected) {
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

    public NediConnection getConnection() {
        return configuration.getConnection();
    }

    public void setConnection(NediConnection connection) {
        configuration.setConnection(connection);
    }


}
