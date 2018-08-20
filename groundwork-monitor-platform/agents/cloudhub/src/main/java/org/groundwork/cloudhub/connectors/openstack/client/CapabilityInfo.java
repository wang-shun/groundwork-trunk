package org.groundwork.cloudhub.connectors.openstack.client;

import java.util.HashMap;
import java.util.Map;

public class CapabilityInfo {

    private Map<String, Boolean> apiCapabilities = new HashMap<>();
    private Map<String, Boolean> storageCapabilities = new HashMap<>();
    private Map<String, Boolean> alarmStorageCapabilities = new HashMap<>();

    public Map<String, Boolean> getApiCapabilities() {
        return apiCapabilities;
    }

    public void setApiCapabilities(Map<String, Boolean> apiCapabilities) {
        this.apiCapabilities = apiCapabilities;
    }

    public Map<String, Boolean> getStorageCapabilities() {
        return storageCapabilities;
    }

    public void setStorageCapabilities(Map<String, Boolean> storageCapabilities) {
        this.storageCapabilities = storageCapabilities;
    }

    public Map<String, Boolean> getAlarmStorageCapabilities() {
        return alarmStorageCapabilities;
    }

    public void setAlarmStorageCapabilities(Map<String, Boolean> alarmStorageCapabilities) {
        this.alarmStorageCapabilities = alarmStorageCapabilities;
    }
}
