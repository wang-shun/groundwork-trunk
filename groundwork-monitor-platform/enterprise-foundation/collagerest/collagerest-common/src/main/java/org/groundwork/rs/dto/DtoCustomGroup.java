/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2015  GroundWork Open Source Solutions info@groundworkopensource.com

 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.

 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.

 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.groundwork.rs.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * DtoCustomGroup
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@XmlRootElement(name = "customGroup")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoCustomGroup {

    // Base Attributes

    @XmlAttribute
    protected Integer id;

    @XmlAttribute
    private String name;

    @XmlAttribute
    private String description;

    @XmlAttribute
    private String appType;

    @XmlAttribute
    private String appTypeDisplayName;

    @XmlAttribute
    private String agentId;

    @XmlAttribute
    private Boolean root;

    // Deep Attributes

    @XmlElementWrapper(name="hostGroups")
    @XmlElement(name="hostGroup")
    private List<DtoHostGroup> hostGroups;

    @XmlElementWrapper(name="serviceGroups")
    @XmlElement(name="serviceGroup")
    private List<DtoServiceGroup> serviceGroups;

    @XmlElementWrapper(name="children")
    @XmlElement(name="category")
    private List<DtoCustomGroup> children;

    @XmlAttribute
    private String bubbleUpStatus;

    public DtoCustomGroup() {
        super();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public String getAppTypeDisplayName() {
        return appTypeDisplayName;
    }

    public void setAppTypeDisplayName(String appTypeDisplayName) {
        this.appTypeDisplayName = appTypeDisplayName;
    }

    public Boolean isRoot() {
        return root;
    }

    public void setRoot(Boolean root) {
        this.root = root;
    }

    public List<DtoHostGroup> getHostGroups() {
        return hostGroups;
    }

    public void setHostGroups(List<DtoHostGroup> hostGroups) {
        this.hostGroups = hostGroups;
    }

    public List<DtoServiceGroup> getServiceGroups() {
        return serviceGroups;
    }

    public void setServiceGroups(List<DtoServiceGroup> serviceGroups) {
        this.serviceGroups = serviceGroups;
    }

    public List<DtoCustomGroup> getChildren() {
        return children;
    }

    public void setChildren(List<DtoCustomGroup> children) {
        this.children = children;
    }

    public void addHostGroup(DtoHostGroup hostGroup) {
        if (getHostGroups() == null) {
            hostGroups = new ArrayList<DtoHostGroup>();
        }
        hostGroups.add(hostGroup);
    }

    public void addServiceGroup(DtoServiceGroup serviceGroup) {
        if (getServiceGroups() == null) {
            serviceGroups = new ArrayList<DtoServiceGroup>();
        }
        serviceGroups.add(serviceGroup);
    }

    public void addChild(DtoCustomGroup child) {
        if (children == null) {
            children = new ArrayList<DtoCustomGroup>();
        }
        children.add(child);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getBubbleUpStatus() {
        return bubbleUpStatus;
    }

    public void setBubbleUpStatus(String bubbleUpStatus) {
        this.bubbleUpStatus = bubbleUpStatus;
    }

}
