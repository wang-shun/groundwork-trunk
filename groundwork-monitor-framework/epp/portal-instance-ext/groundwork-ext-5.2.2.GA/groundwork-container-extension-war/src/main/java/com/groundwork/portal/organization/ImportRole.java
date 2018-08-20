package com.groundwork.portal.organization;

import java.util.HashSet;
import java.util.Set;

public class ImportRole {
    private final String name;
    private final String displayName;
    private Set<String>  members = new HashSet<String>();

    /**
     * @param  name
     * @param  displayName
     * @param  members
     */
    public ImportRole(String name, String displayName, Set<String> members) {
        this.name        = name;
        this.displayName = displayName;
        this.members     = members;
    }

    /**
     * @param  name
     * @param  displayName
     */
    public ImportRole(String name, String displayName) {
        this.name        = name;
        this.displayName = displayName;
    }

    /** @return */
    public String getName() {
        return name;
    }

    /** @return */
    public String getDisplayName() {
        return displayName;
    }

    /** @return */
    public Set<String> getMembers() {
        return members;
    }

    /** @param  members */
    public void setMembers(Set<String> members) {
        this.members = members;
    }
}
