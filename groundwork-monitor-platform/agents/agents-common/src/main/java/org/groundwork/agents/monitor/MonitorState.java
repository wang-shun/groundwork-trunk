package org.groundwork.agents.monitor;

import java.util.concurrent.atomic.AtomicBoolean;

public class MonitorState {

    private AtomicBoolean running = new AtomicBoolean(false);
    private AtomicBoolean suspended = new AtomicBoolean(false);
    private AtomicBoolean configurationUpdated = new AtomicBoolean(true);
    private AtomicBoolean forceShutdown = new AtomicBoolean(false);
    private AtomicBoolean forceDelete = new AtomicBoolean(false);
    private AtomicBoolean forceRename = new AtomicBoolean(false);
    private AtomicBoolean forceSuspend = new AtomicBoolean(false);
    private AtomicBoolean forceDeleteServices = new AtomicBoolean(false);
    private AtomicBoolean forceDeleteView = new AtomicBoolean(false);
    private AtomicBoolean forceDeleteConnectorHost = new AtomicBoolean(false);

    private String renameAgentId = null;
    private String renameOldPrefix = null;
    private String renameNewPrefix = null;
    private MonitorChangeState servicesChangeState = null;
    private MonitorChangeState viewChangeState = null;

    public boolean isRunning() {
        return running.get();
    }

    public void setRunning(boolean value) {
        running.set(value);
    }

    public boolean isSuspended() {
        return suspended.get();
    }

    public void setSuspended(boolean value) {
        suspended.set(value);
    }

    public boolean isConfigurationUpdated() {
        return configurationUpdated.get();
    }

    public void setConfigurationUpdated(boolean value) {
        configurationUpdated.set(value);
    }

    public boolean isForceShutdown() {
        return forceShutdown.get();
    }

    public void setForceShutdown(boolean force) {
        forceShutdown.set(force);
    }

    public boolean isForceDelete() {
        return forceDelete.get();
    }

    public void setForceDelete(boolean forceDelete) {
        this.forceDelete.set(forceDelete);
    }

    public boolean isForceSuspend() {
        return forceSuspend.get();
    }

    public void setForceSuspend(boolean forceSuspend) {
        this.forceSuspend.set(forceSuspend);
    }

    public boolean isForceRename() {
        return forceRename.get();
    }

    public void setForceRename(boolean force) {
         forceRename.set(force);
    }

    public boolean isForceDeleteServices() {
        return forceDeleteServices.get();
    }

    public void setForceDeleteServices(boolean force) {
        forceDeleteServices.set(force);
    }

    public boolean isForceDeleteView() {
        return forceDeleteView.get();
    }

    public void setForceDeleteView(boolean force) {
        forceDeleteView.set(force);
    }

    public boolean getForceDeleteConnectorHost() {
        return forceDeleteConnectorHost.get();
    }

    public void setForceDeleteConnectorHost(boolean force) {
        forceDeleteConnectorHost.set(force);
    }


    public synchronized void startRename(String agentId, String oldPrefix, String newPrefix) {
        this.renameAgentId = agentId;
        this.renameOldPrefix = oldPrefix;
        this.renameNewPrefix = newPrefix;
        this.forceRename.set(true);
    }

    public synchronized void completeRename() {
        this.renameAgentId = null;
        this.renameOldPrefix = null;
        this.renameNewPrefix = null;
        this.forceRename.set(false);
    }

    public synchronized void startDeleteServices(MonitorChangeState changeState) {
        this.servicesChangeState = changeState;
        this.forceDeleteServices.set(true);
    }

    public synchronized void completeDeleteServices() {
        this.servicesChangeState = null;
        this.forceDeleteServices.set(false);
    }

    public synchronized void startDeleteView(MonitorChangeState changeState) {
        this.viewChangeState = changeState;
        this.forceDeleteView.set(true);
    }

    public synchronized void completeDeleteView() {
        this.viewChangeState = null;
        this.forceDeleteView.set(false);
    }

    public synchronized void startDeleteConnectorHost(MonitorChangeState changeState) {
        this.viewChangeState = changeState;
        this.forceDeleteConnectorHost.set(true);
    }

    public synchronized void completeDeleteConnectorHost() {
        this.viewChangeState = null;
        this.forceDeleteConnectorHost.set(false);
    }

    public String getRenameAgentId() {
        return renameAgentId;
    }

    public void setRenameAgentId(String renameAgentId) {
        this.renameAgentId = renameAgentId;
    }

    public String getRenameOldPrefix() {
        return renameOldPrefix;
    }

    public void setRenameOldPrefix(String renameOldPrefix) {
        this.renameOldPrefix = renameOldPrefix;
    }

    public String getRenameNewPrefix() {
        return renameNewPrefix;
    }

    public void setRenameNewPrefix(String renameNewPrefix) {
        this.renameNewPrefix = renameNewPrefix;
    }

    public MonitorChangeState getServicesChangeState() {
        return servicesChangeState;
    }

    public MonitorChangeState getViewChangeState() {
        return viewChangeState;
    }


}

