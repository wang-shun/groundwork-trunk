package org.groundwork.cloudhub.api.dto;

import org.groundwork.cloudhub.configuration.CommonConfiguration;
import org.groundwork.cloudhub.configuration.ConfigurationView;
import org.groundwork.cloudhub.configuration.GWOSConfiguration;
import org.groundwork.cloudhub.configuration.VmwareConfiguration;
import org.groundwork.cloudhub.configuration.VmwareConnection;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by dtaylor on 5/31/17.
 */
public class DtoVmwareConfiguration extends DtoViewAdaptor {


    private DtoConfigurationState state;

    public DtoVmwareConfiguration() {
        this.state = new DtoConfigurationState();
        this.configuration = new VmwareConfiguration();
    }

    public DtoVmwareConfiguration(VmwareConfiguration configuration, Boolean isConnected) {
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

    public VmwareConnection getConnection() {
        return (VmwareConnection)configuration.getConnection();
    }

    public void setConnection(VmwareConnection connection) {
        ((VmwareConfiguration)configuration).setConnection(connection);
    }

    public List<ConfigurationView> getViews() {
        List<ConfigurationView> views = new LinkedList<>();
        views.add(new ConfigurationView(STORAGE_VIEW, configuration.getCommon().isStorageView(), false));
        views.add(new ConfigurationView(NETWORK_VIEW, configuration.getCommon().isNetworkView(), false));
        views.add(new ConfigurationView(RESOURCE_POOL_VIEW, configuration.getCommon().isResourcePoolView(), false));
        return views;
    }

}
