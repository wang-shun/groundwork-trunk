package org.groundwork.cloudhub.api.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dtaylor on 5/31/17.
 */
public class DtoConfigurationState {

    private List<String> versions = new ArrayList();
    private List<String> viewsRemoved = new ArrayList<>();
    private List<String> viewsAdded = new ArrayList<>();
    private Boolean prefixServiceNamesChanged = false;
    private Boolean resourceGroupsChanged = false;
    private Boolean monitorChanged = false;
    private Boolean displayNameChanged = false;
    private Boolean hostPrefixChanged = false;
    private Boolean gwosServerChanged = false;
    private Boolean isConnected = false;

    public DtoConfigurationState() {
        versions.add("7.1");
        versions.add("7.0");
    }

    public DtoConfigurationState(Boolean isConnected) {
        this();
        this.isConnected = isConnected;
    }

    public List<String> getVersions() {
        return versions;
    }

    public void setVersions(List<String> versions) {
        this.versions = versions;
    }

    public List<String> getViewsRemoved() {
        return viewsRemoved;
    }

    public void setViewsRemoved(List<String> viewsRemoved) {
        this.viewsRemoved = viewsRemoved;
    }

    public List<String> getViewsAdded() {
        return viewsAdded;
    }

    public void setViewsAdded(List<String> viewsAdded) {
        this.viewsAdded = viewsAdded;
    }

    public Boolean getPrefixServiceNamesChanged() {
        return prefixServiceNamesChanged;
    }

    public void setPrefixServiceNamesChanged(Boolean prefixServiceNamesChanged) {
        this.prefixServiceNamesChanged = prefixServiceNamesChanged;
    }

    public Boolean getResourceGroupsChanged() {
        return resourceGroupsChanged;
    }

    public void setResourceGroupsChanged(Boolean resourceGroupsChanged) {
        this.resourceGroupsChanged = resourceGroupsChanged;
    }

    public Boolean getConnected() {
        return isConnected;
    }

    public void setConnected(Boolean connected) {
        isConnected = connected;
    }

    public Boolean getDisplayNameChanged() {
        return displayNameChanged;
    }

    public void setDisplayNameChanged(Boolean displayNameChanged) {
        this.displayNameChanged = displayNameChanged;
    }

    public Boolean getMonitorChanged() {
        return monitorChanged;
    }

    public void setMonitorChanged(Boolean monitorChanged) {
        this.monitorChanged = monitorChanged;
    }

    public Boolean getHostPrefixChanged() {
        return hostPrefixChanged;
    }

    public void setHostPrefixChanged(Boolean hostPrefixChanged) {
        this.hostPrefixChanged = hostPrefixChanged;
    }

    public Boolean getGwosServerChanged() {
        return gwosServerChanged;
    }

    public void setGwosServerChanged(Boolean gwosServerChanged) {
        this.gwosServerChanged = gwosServerChanged;
    }
}
