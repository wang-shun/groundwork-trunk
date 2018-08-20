package org.groundwork.cloudhub.api.dto;

import org.groundwork.cloudhub.configuration.CommonConfiguration;
import org.groundwork.cloudhub.configuration.DockerConfiguration;
import org.groundwork.cloudhub.configuration.DockerConnection;
import org.groundwork.cloudhub.configuration.GWOSConfiguration;

/**
 * Created by dtaylor on 5/31/17.
 */
public class DtoDockerConfiguration {

    private DtoConfigurationState state;
    private DockerConfiguration configuration;

    public DtoDockerConfiguration() {
        this.state = new DtoConfigurationState();
        this.configuration = new DockerConfiguration();
    }

    public DtoDockerConfiguration(DockerConfiguration configuration, Boolean isConnected) {
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

    public DockerConnection getConnection() {
        return configuration.getConnection();
    }

    public void setConnection(DockerConnection connection) {
        configuration.setConnection(connection);
    }


}
