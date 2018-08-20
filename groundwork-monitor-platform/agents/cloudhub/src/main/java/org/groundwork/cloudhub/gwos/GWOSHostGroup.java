/*
 * Copyright 2012 GroundWork , Inc. ("GroundWork") 
 * All rights reserved. 
*/
package org.groundwork.cloudhub.gwos;

public class GWOSHostGroup {

    private String hostGroup = null;
    private String alias = null;
    private String description = null;
    private String applicationType = null;
    private String agentId = null;

    /**
     * Default constructor requires Host information
     *
     * @param hostGroup
     * @param description
     * @param alias
     * @param applicationType
     */
    public GWOSHostGroup(String hostGroup, String description, String alias, String applicationType) {
        this.hostGroup = hostGroup;
        this.description = description;
        this.alias = alias;
        this.applicationType = applicationType;
    }

	public GWOSHostGroup(String hostGroup, String description, String alias, String applicationType, String agentId) {
		this.hostGroup = hostGroup;
		this.description = description;
		this.alias = alias;
		this.applicationType = applicationType;
		this.agentId = agentId;
	}

    public String getHostGroupName() {
        return hostGroup;
    }

	public String getHostGroup() {
		return hostGroup;
	}

	public void setHostGroup(String hostGroup) {
		this.hostGroup = hostGroup;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getApplicationType() {
		return applicationType;
	}

	public void setApplicationType(String applicationType) {
		this.applicationType = applicationType;
	}

	public String getAgentId() {
		return agentId;
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}
}
