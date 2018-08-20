package org.groundwork.cloudhub.api;

import org.groundwork.cloudhub.configuration.ConnectionConfiguration;

/**
 * Created by dtaylor on 6/7/17.
 */
public class SaveConfigurationResult {

    private boolean isNew = false;
    private ConnectionConfiguration oldConfig;
    private ConnectionConfiguration newConfig;
    private String message = "";
    private boolean success = false;

    public SaveConfigurationResult(boolean isNew, ConnectionConfiguration oldConfig, ConnectionConfiguration newConfig, String message) {
        this.isNew = isNew;
        this.oldConfig = oldConfig;
        this.newConfig = newConfig;
        this.message = message;
        this.success = (message == null || message.isEmpty());
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public ConnectionConfiguration getOldConfig() {
        return oldConfig;
    }

    public void setOldConfig(ConnectionConfiguration oldConfig) {
        this.oldConfig = oldConfig;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
        this.success = (message == null || message.isEmpty());
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public ConnectionConfiguration getNewConfig() {
        return newConfig;
    }

    public void setNewConfig(ConnectionConfiguration newConfig) {
        this.newConfig = newConfig;
    }
}
