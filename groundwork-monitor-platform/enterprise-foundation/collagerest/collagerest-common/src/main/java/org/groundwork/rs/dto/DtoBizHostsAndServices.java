/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2014  GroundWork Open Source Solutions info@groundworkopensource.com

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
import java.util.List;

/**
 * DtoBizHostsAndServices
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@XmlRootElement(name = "bizHostsAndServices")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoBizHostsAndServices {

    @XmlElementWrapper(name="hostNames")
    @XmlElement(name="hostName")
    private List<String> hostNames;
    @XmlElementWrapper(name="serviceDescriptions")
    @XmlElement(name="serviceDescription")
    private List<String> serviceDescriptions;
    @XmlElementWrapper(name="hostGroupNames")
    @XmlElement(name="hostGroupName")
    private List<String> hostGroupNames;
    @XmlElementWrapper(name="serviceGroupCategoryNames")
    @XmlElement(name="serviceGroupCategoryName")
    private List<String> serviceGroupCategoryNames;
    @XmlAttribute
    private boolean setHosts;
    @XmlAttribute
    private boolean setServices;

    /**
     * Full BizHostsAndServices constructor.
     *
     * @param hostNames host names or '*' wildcard to put into downtime
     * @param serviceDescriptions service descriptions or '*' wildcard to put into downtime
     * @param hostGroupNames host group names to put into downtime
     * @param serviceGroupCategoryNames service group category names to put into downtime
     * @param setHosts set host downtime properties
     * @param setServices set service downtime properties
     */
    public DtoBizHostsAndServices(List<String> hostNames, List<String> serviceDescriptions, List<String> hostGroupNames,
                                  List<String> serviceGroupCategoryNames, boolean setHosts, boolean setServices) {
        this.hostNames = hostNames;
        this.serviceDescriptions = serviceDescriptions;
        this.hostGroupNames = hostGroupNames;
        this.serviceGroupCategoryNames = serviceGroupCategoryNames;
        this.setHosts = setHosts;
        this.setServices = setServices;
    }

    /**
     * Default BizHostsAndServices constructor.
     */
    public DtoBizHostsAndServices() {
    }

    /**
     * toString Object protocol implementation.
     *
     * @return BizHostsAndServices as String
     */
    @Override
    public String toString() {
        return String.format(getClass().getName()+"@%x[hostNames=%s,serviceDescriptions=%s,hostGroupNames=%s,serviceGroupCategoryNames=%s]",
                System.identityHashCode(this), hostNames, serviceDescriptions, hostGroupNames, serviceGroupCategoryNames);
    }

    public List<String> getHostNames() {
        return hostNames;
    }

    public void setHostNames(List<String> hostNames) {
        this.hostNames = hostNames;
    }

    public List<String> getServiceDescriptions() {
        return serviceDescriptions;
    }

    public void setServiceDescriptions(List<String> serviceDescriptions) {
        this.serviceDescriptions = serviceDescriptions;
    }

    public List<String> getHostGroupNames() {
        return hostGroupNames;
    }

    public void setHostGroupNames(List<String> hostGroupNames) {
        this.hostGroupNames = hostGroupNames;
    }

    public List<String> getServiceGroupCategoryNames() {
        return serviceGroupCategoryNames;
    }

    public void setServiceGroupCategoryNames(List<String> serviceGroupCategoryNames) {
        this.serviceGroupCategoryNames = serviceGroupCategoryNames;
    }

    public boolean isSetHosts() {
        return setHosts;
    }

    public void setSetHosts(boolean setHosts) {
        this.setHosts = setHosts;
    }

    public boolean isSetServices() {
        return setServices;
    }

    public void setSetServices(boolean setServices) {
        this.setServices = setServices;
    }
}
