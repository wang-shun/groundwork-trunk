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
import java.util.List;

/**
 * DtoBizAuthorizedServiceHostMapEntry
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoBizAuthorizedServiceHostMapEntry {

    @XmlAttribute
    private String serviceName;
    @XmlElementWrapper(name="hostNames")
    @XmlElement(name="hostName")
    private List<String> hostNames;

    /**
     * Default constructor.
     */
    public DtoBizAuthorizedServiceHostMapEntry() {
    }

    /**
     * Full constructor.
     *
     * @param serviceName service name
     * @param hostNames host names
     */
    public DtoBizAuthorizedServiceHostMapEntry(String serviceName, List<String> hostNames) {
        this.serviceName = serviceName;
        this.hostNames = hostNames;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public List<String> getHostNames() {
        return hostNames;
    }

    public void setHostNames(List<String> hostNames) {
        this.hostNames = hostNames;
    }
}
