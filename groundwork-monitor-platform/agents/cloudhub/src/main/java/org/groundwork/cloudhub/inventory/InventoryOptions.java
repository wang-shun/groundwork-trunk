package org.groundwork.cloudhub.inventory;

public class InventoryOptions {

    private boolean viewHypervisors = false;
    private boolean viewDatastores = false;
    private boolean viewNetworks = false;
    private boolean viewResourcePools = false;
    private boolean viewTaggedGroups = false;
    private String groupTag;

    public InventoryOptions(boolean viewHypervisors, boolean viewDatastores, boolean viewNetworks, boolean viewResourcePools) {
        this(viewHypervisors, viewDatastores, viewNetworks, viewResourcePools, false, "");
    }

    public InventoryOptions(boolean viewHypervisors, boolean viewDatastores, boolean viewNetworks, boolean viewResourcePools,
                            boolean viewTaggedGroups, String groupTag) {
        this.viewHypervisors = viewHypervisors;
        this.viewDatastores = viewDatastores;
        this.viewNetworks = viewNetworks;
        this.viewResourcePools = viewResourcePools;
        this.viewTaggedGroups = viewTaggedGroups;
        this.groupTag = groupTag;
    }

    public boolean isViewHypervisors() {
        return viewHypervisors;
    }

    public void setViewHypervisors(boolean viewHypervisors) {
        this.viewHypervisors = viewHypervisors;
    }

    public boolean isViewDatastores() {
        return viewDatastores;
    }

    public void setViewDatastores(boolean viewDatastores) {
        this.viewDatastores = viewDatastores;
    }

    public boolean isViewNetworks() {
        return viewNetworks;
    }

    public void setViewNetworks(boolean viewNetworks) {
        this.viewNetworks = viewNetworks;
    }

    public boolean isViewResourcePools() {
        return viewResourcePools;
    }

    public void setViewResourcePools(boolean viewResourcePools) {
        this.viewResourcePools = viewResourcePools;
    }

    public boolean isViewTaggedGroups() {
        return viewTaggedGroups;
    }

    public void setViewTaggedGroups(boolean viewTaggedGroups) {
        this.viewTaggedGroups = viewTaggedGroups;
    }

    public String getGroupTag() {
        return groupTag;
    }

    public void setGroupTag(String groupTag) {
        this.groupTag = groupTag;
    }
}
