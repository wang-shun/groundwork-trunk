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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * DtoBizAuthorization
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@XmlRootElement(name = "bizAuthorization")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoBizAuthorization {

    @XmlElementWrapper(name="hostGroupNames")
    @XmlElement(name="hostGroupName")
    private List<String> hostGroupNames;
    @XmlElementWrapper(name="serviceGroupNames")
    @XmlElement(name="serviceGroupName")
    private List<String> serviceGroupNames;

    /**
     * Default constructor.
     */
    public DtoBizAuthorization() {
    }

    /**
     * Full constructor.
     *
     * @param hostGroupNames authorized host group names
     * @param serviceGroupNames authorized service group names
     */
    public DtoBizAuthorization(List<String> hostGroupNames, List<String> serviceGroupNames) {
        this.hostGroupNames = hostGroupNames;
        this.serviceGroupNames = serviceGroupNames;
    }

    /**
     * toString Object protocol implementation.
     *
     * @return BizAuthorization as String
     */
    @Override
    public String toString() {
        return String.format(getClass().getName()+"@%x[hostGroupNames=%s,serviceGroupNames=%s]",
                System.identityHashCode(this), hostGroupNames, serviceGroupNames);
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
}
