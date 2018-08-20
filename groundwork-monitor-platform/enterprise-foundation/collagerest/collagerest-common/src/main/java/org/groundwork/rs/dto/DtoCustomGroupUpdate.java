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
 * DtoCustomGroupUpdate
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@XmlRootElement(name = "customGroup")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoCustomGroupUpdate {

    // Base Attributes

    @XmlAttribute
    private String name;

    @XmlAttribute
    private String description;

    @XmlAttribute
    private String appType;

    @XmlAttribute
    private String agentId;

    // Deep Attributes

    @XmlElementWrapper(name="hostGroupNames")
    @XmlElement(name="hostGroupName")
    private List<String> hostGroupNames;

    @XmlElementWrapper(name="serviceGroupNames")
    @XmlElement(name="serviceGroupName")
    private List<String> serviceGroupNames;

    public DtoCustomGroupUpdate() {
        super();
    }

    public DtoCustomGroupUpdate(String customGroupName) {
        super();
        this.name = customGroupName;
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

    public List<String> getHostGroupNames() {
        return hostGroupNames;
    }

    public void setHostGroupNames(List<String> hostGroupNames) {
        this.hostGroupNames = hostGroupNames;
    }

    public List<String> getServiceGroupNames() {
        return serviceGroupNames;
    }

    public void setServiceGroupNames(List<String> serviceGroupNames) {
        this.serviceGroupNames = serviceGroupNames;
    }

    public void addHostGroupNames(String hostGroupName) {
        if (getHostGroupNames() == null) {
            hostGroupNames = new ArrayList<String>();
        }
        hostGroupNames.add(hostGroupName);
    }

    public void addServiceGroupNames(String serviceGroupName) {
        if (getServiceGroupNames() == null) {
            serviceGroupNames = new ArrayList<String>();
        }
        serviceGroupNames.add(serviceGroupName);
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }
}
