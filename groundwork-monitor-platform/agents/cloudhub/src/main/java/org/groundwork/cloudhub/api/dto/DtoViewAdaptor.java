package org.groundwork.cloudhub.api.dto;

import org.groundwork.cloudhub.configuration.ConfigurationView;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;

import java.util.List;

public abstract class DtoViewAdaptor {

    public static final String STORAGE_VIEW = "storageView";
    public static final String NETWORK_VIEW = "networkView";
    public static final String RESOURCE_POOL_VIEW = "resourcePoolView";
    public static final String CUSTOM_VIEW = "customView";

    protected ConnectionConfiguration configuration;


    public abstract List<ConfigurationView> getViews();

    public void setViews(List<ConfigurationView> views) {
        for (ConfigurationView view : views) {
            if (view.getName().equals(STORAGE_VIEW)) {
                configuration.getCommon().setStorageView(view.isEnabled());
            }
            else if (view.getName().equals(NETWORK_VIEW)) {
                configuration.getCommon().setNetworkView(view.isEnabled());
            }
            else if (view.getName().equals(RESOURCE_POOL_VIEW)) {
                configuration.getCommon().setResourcePoolView(view.isEnabled());
            }
            else if (view.getName().equals(CUSTOM_VIEW)) {
                configuration.getCommon().setCustomView(view.isEnabled());
            }
        }
    }


}
