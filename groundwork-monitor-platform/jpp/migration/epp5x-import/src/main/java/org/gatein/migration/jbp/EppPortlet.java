package org.gatein.migration.jbp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EppPortlet {
    private String              applicationRef;
    private String              portletRef;
    private String              title;
    private List<String>        accessPermissions    = new ArrayList<String>();
    private boolean             showInfoBar;
    private boolean             showApplicationState;
    private boolean             showApplicationMode;
    private String              description;
    private String              icon;
    private int                 priority;
    private String              location;
    private String              region;
    private Map<String, String> preferences          = new HashMap<String, String>();

    public void addPermission(String permissionTo) {
        if (accessPermissions == null) {
            accessPermissions = new ArrayList<String>();
        }

        accessPermissions.add(permissionTo);
    }

    public boolean getHasPreferences() {
        return preferences.size() > 0;
    }

    public void addPreference(String key, String value) {
        preferences.put(key, value);
    }

    public String getApplicationRef() {
        return applicationRef;
    }

    public void setApplicationRef(String applicationRef) {
        this.applicationRef = applicationRef;
    }

    public String getPortletRef() {
        return portletRef;
    }

    public void setPortletRef(String portletRef) {
        this.portletRef = portletRef;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getAccessPermissions() {
        return accessPermissions;
    }

    public void setAccessPermissions(List<String> accessPermissions) {
        this.accessPermissions = accessPermissions;
    }

    public boolean isShowInfoBar() {
        return showInfoBar;
    }

    public void setShowInfoBar(boolean showInfoBar) {
        this.showInfoBar = showInfoBar;
    }

    public boolean isShowApplicationState() {
        return showApplicationState;
    }

    public void setShowApplicationState(boolean showApplicationState) {
        this.showApplicationState = showApplicationState;
    }

    public boolean isShowApplicationMode() {
        return showApplicationMode;
    }

    public void setShowApplicationMode(boolean showApplicationMode) {
        this.showApplicationMode = showApplicationMode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Map<String, String> getPreferences() {
        return preferences;
    }
}
