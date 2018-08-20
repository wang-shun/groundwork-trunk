package org.groundwork.cloudhub.connectors;

import org.groundwork.cloudhub.configuration.ConfigurationView;

import java.util.List;

public class CollectionMode {

    private boolean doHosts = true;
    private boolean doVMs = true;
    private boolean doStorageDomains = false;
    private boolean doNetworks = false;
    private boolean doResourcePools = false;
    private boolean doClusters = false;
    private boolean doDataCenters = false;
    private boolean doTaggedGroups = false;
    private String  groupTag;
    private boolean doCustom = false;
    private List<ConfigurationView> views;
    private boolean doPrefixServiceNames = false;

    public CollectionMode(boolean doHosts, boolean doVMs, boolean doStorageDomains, boolean doNetworks,
                          boolean doResourcePools, boolean doClusters, boolean doDataCenters) {
        this(doHosts, doVMs, doStorageDomains, doNetworks, doResourcePools, doClusters, doDataCenters, false, "", false, false);
    }

    public CollectionMode(boolean doHosts, boolean doVMs, boolean doStorageDomains, boolean doNetworks,
                          boolean doResourcePools, boolean doClusters, boolean doDataCenters,
                          boolean doTaggedGroups, String groupTag, boolean doCustom, boolean doPrefixServiceNames) {
        this.doHosts = doHosts;
        this.doVMs = doVMs;
        this.doStorageDomains = doStorageDomains;
        this.doNetworks = doNetworks;
        this.doResourcePools = doResourcePools;
        this.doClusters = doClusters;
        this.doDataCenters = doDataCenters;
        this.doTaggedGroups = doTaggedGroups;
        this.groupTag = groupTag;
        this.doCustom = doCustom;
        this.doPrefixServiceNames = doPrefixServiceNames;
    }

    public boolean isDoHosts() {
        return doHosts;
    }

    public void setDoHosts(boolean doHosts) {
        this.doHosts = doHosts;
    }

    public boolean isDoVMs() {
        return doVMs;
    }

    public void setDoVMs(boolean doVMs) {
        this.doVMs = doVMs;
    }

    public boolean isDoStorageDomains() {
        return doStorageDomains;
    }

    public void setDoStorageDomains(boolean doStorageDomains) {
        this.doStorageDomains = doStorageDomains;
    }

    public boolean isDoNetworks() {
        return doNetworks;
    }

    public void setDoNetworks(boolean doNetworks) {
        this.doNetworks = doNetworks;
    }

    public boolean isDoResourcePools() {
        return doResourcePools;
    }

    public void setDoResourcePools(boolean doResourcePools) {
        this.doResourcePools = doResourcePools;
    }

    public boolean isDoClusters() {
        return doClusters;
    }

    public void setDoClusters(boolean doClusters) {
        this.doClusters = doClusters;
    }

    public boolean isDoDataCenters() {
        return doDataCenters;
    }

    public void setDoDataCenters(boolean doDataCenters) {
        this.doDataCenters = doDataCenters;
    }

    public boolean isDoTaggedGroups() {
        return doTaggedGroups;
    }

    public void setDoTaggedGroups(boolean doTaggedGroups) {
        this.doTaggedGroups = doTaggedGroups;
    }

    public String getGroupTag() {
        return groupTag;
    }

    public void setGroupTag(String groupTag) {
        this.groupTag = groupTag;
    }

    public boolean isDoCustom() {
        return doCustom;
    }

    public void setDoCustom(boolean doCustom) {
        this.doCustom = doCustom;
    }

    public List<ConfigurationView> getViews() {
        return views;
    }

    public void setViews(List<ConfigurationView> views) {
        this.views = views;
    }

    public boolean isDoPrefixServiceNames() {
        return doPrefixServiceNames;
    }

    public void setDoPrefixServiceNames(boolean doPrefixServiceNames) {
        this.doPrefixServiceNames = doPrefixServiceNames;
    }
}
