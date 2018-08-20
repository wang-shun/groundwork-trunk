package org.groundwork.cloudhub.inventory;

import org.apache.log4j.Logger;

public class DataCenterSyncResult {

    private static Logger log = Logger.getLogger(DataCenterSyncResult.class);

    private int networksAdded = 0;
    private int networksDeleted = 0;
    private int networksModified = 0;
    private int datastoresAdded = 0;
    private int datastoresDeleted = 0;
    private int datastoresModified = 0;
    private int resourcePoolsAdded = 0;
    private int resourcePoolsDeleted = 0;
    private int resourcePoolsModified = 0;
    private int taggedGroupsAdded = 0;
    private int taggedGroupsDeleted = 0;
    private int taggedGroupsModified = 0;

    private int hypervisorsAdded = 0;
    private int hypervisorsDeleted = 0;
    private int hypervisorsModified = 0;
    private int vmsAdded = 0;
    private int vmsDeleted = 0;
    private int vmsModified = 0;


    private DataCenterInventory monitoringInventory;
    private DataCenterInventory gwosInventory;

    public DataCenterSyncResult() {}

    public int getHypervisorsAdded() {
        return hypervisorsAdded;
    }

    public void setHypervisorsAdded(int hypervisorsAdded) {
        this.hypervisorsAdded = hypervisorsAdded;
    }

    public int getHypervisorsDeleted() {
        return hypervisorsDeleted;
    }

    public void setHypervisorsDeleted(int hypervisorsDeleted) {
        this.hypervisorsDeleted = hypervisorsDeleted;
    }

    public int getHypervisorsModified() {
        return hypervisorsModified;
    }

    public void setHypervisorsModified(int hypervisorsModified) {
        this.hypervisorsModified = hypervisorsModified;
    }

    public int getVmsAdded() {
        return vmsAdded;
    }

    public void setVmsAdded(int vmsAdded) {
        this.vmsAdded = vmsAdded;
    }

    public int getVmsDeleted() {
        return vmsDeleted;
    }

    public void setVmsDeleted(int vmsDeleted) {
        this.vmsDeleted = vmsDeleted;
    }

    public int getVmsModified() {
        return vmsModified;
    }

    public void setVmsModified(int vmsModified) {
        this.vmsModified = vmsModified;
    }

    public int getNetworksAdded() {
        return networksAdded;
    }

    public int getNetworksDeleted() {
        return networksDeleted;
    }

    public int getNetworksModified() {
        return networksModified;
    }

    public int getDatastoresAdded() {
        return datastoresAdded;
    }

    public int getDatastoresDeleted() {
        return datastoresDeleted;
    }

    public int getDatastoresModified() {
        return datastoresModified;
    }

    public int getResourcePoolsAdded() {
        return resourcePoolsAdded;
    }

    public int getResourcePoolsDeleted() {
        return resourcePoolsDeleted;
    }

    public int getResourcePoolsModified() {
        return resourcePoolsModified;
    }

    public void incrementHypervisorsAdded() {
        this.hypervisorsAdded++;
    }

    public void incrementHypervisorsDeleted() {
        this.hypervisorsDeleted++;
    }

    public void incrementHypervisorsModified() {
        this.hypervisorsModified++;
    }

    public void incrementVmsAdded() {
        this.vmsAdded++;
    }

    public void incrementVmsDeleted() {
        this.vmsDeleted++;
    }

    public void incrementVmsModified() {
        this.vmsModified++;
    }

    public void incrementNetworksAdded() {
        this.networksAdded++;
    }

    public void incrementNetworksDeleted() {
        this.networksDeleted++;
    }

    public void incrementNetworksModified() {
        this.networksModified++;
    }

    public void incrementDatastoresAdded() {
        this.datastoresAdded++;
    }

    public void incrementDatastoresDeleted() {
        this.datastoresDeleted++;
    }

    public void incrementDatastoresModified() {
        this.datastoresModified++;
    }

    public void incrementResourcePoolsAdded() {
        this.resourcePoolsAdded++;
    }

    public void incrementResourcePoolsDeleted() {
        this.resourcePoolsDeleted++;
    }

    public void incrementResourcePoolsModified() {
        this.resourcePoolsModified++;
    }

    public void setNetworksAdded(int networksAdded) {
        this.networksAdded = networksAdded;
    }

    public void setNetworksDeleted(int networksDeleted) {
        this.networksDeleted = networksDeleted;
    }

    public void setNetworksModified(int networksModified) {
        this.networksModified = networksModified;
    }

    public void setDatastoresAdded(int datastoresAdded) {
        this.datastoresAdded = datastoresAdded;
    }

    public void setDatastoresDeleted(int datastoresDeleted) {
        this.datastoresDeleted = datastoresDeleted;
    }

    public void setDatastoresModified(int datastoresModified) {
        this.datastoresModified = datastoresModified;
    }

    public void setResourcePoolsAdded(int resourcePoolsAdded) {
        this.resourcePoolsAdded = resourcePoolsAdded;
    }

    public void setResourcePoolsDeleted(int resourcePoolsDeleted) {
        this.resourcePoolsDeleted = resourcePoolsDeleted;
    }

    public void setResourcePoolsModified(int resourcePoolsModified) {
        this.resourcePoolsModified = resourcePoolsModified;
    }

    public int getTaggedGroupsAdded() {
        return taggedGroupsAdded;
    }

    public void setTaggedGroupsAdded(int taggedGroupsAdded) {
        this.taggedGroupsAdded = taggedGroupsAdded;
    }

    public int getTaggedGroupsDeleted() {
        return taggedGroupsDeleted;
    }

    public void setTaggedGroupsDeleted(int taggedGroupsDeleted) {
        this.taggedGroupsDeleted = taggedGroupsDeleted;
    }

    public int getTaggedGroupsModified() {
        return taggedGroupsModified;
    }

    public void setTaggedGroupsModified(int taggedGroupsModified) {
        this.taggedGroupsModified = taggedGroupsModified;
    }

    public void debug(String agent) {
        if (log.isDebugEnabled()) {
            log.debug("=============== Sync Results " + agent + " ===============");
            log.debug("Hypervisors Added         : " + hypervisorsAdded);
            log.debug("Hypervisors Deleted       : " + hypervisorsDeleted);
            log.debug("Hypervisors Modified      : " + hypervisorsModified);
            if (vmsAdded > 0 || vmsDeleted > 0 || vmsModified > 0) {
                log.debug("VMs Added         : " + vmsAdded);
                log.debug("VMs Deleted       : " + vmsDeleted);
                log.debug("VMs Modified      : " + vmsModified);
            }
            if (networksAdded > 0 || networksDeleted > 0 || networksModified > 0) {
                log.debug("Networks Added         : " + networksAdded);
                log.debug("Networks Deleted       : " + networksDeleted);
                log.debug("Networks Modified      : " + networksModified);
            }
            if (datastoresAdded > 0 || datastoresDeleted > 0 || datastoresModified > 0) {
                log.debug("Datastores Added       : " + datastoresAdded);
                log.debug("Datastores Deleted     : " + datastoresDeleted);
                log.debug("Datastores Modified    : " + datastoresModified);
            }
            if (resourcePoolsAdded > 0 || resourcePoolsDeleted > 0 || resourcePoolsModified > 0) {
                log.debug("Resource Pools Added   : " + resourcePoolsAdded);
                log.debug("Resource Pools Deleted : " + resourcePoolsDeleted);
                log.debug("Resource Pools Modified: " + resourcePoolsModified);
            }
            if (taggedGroupsAdded > 0 || taggedGroupsDeleted > 0 || taggedGroupsModified > 0) {
                log.debug("Tagged Groups Added   : " + taggedGroupsAdded);
                log.debug("Tagged Groups Deleted : " + taggedGroupsDeleted);
                log.debug("Tagged Groups Modified: " + taggedGroupsModified);
            }

        }
    }

    public DataCenterInventory getMonitoringInventory() {
        return monitoringInventory;
    }

    public void setMonitoringInventory(DataCenterInventory monitoringInventory) {
        this.monitoringInventory = monitoringInventory;
    }

    public DataCenterInventory getGwosInventory() {
        return gwosInventory;
    }

    public void setGwosInventory(DataCenterInventory gwosInventory) {
        this.gwosInventory = gwosInventory;
    }

}
