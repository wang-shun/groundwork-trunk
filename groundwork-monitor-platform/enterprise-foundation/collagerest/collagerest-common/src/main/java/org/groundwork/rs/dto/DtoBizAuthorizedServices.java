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

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.List;
import java.util.Map;

/**
 * DtoBizAuthorizedServices
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@XmlRootElement(name = "bizAuthorizedServices")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoBizAuthorizedServices {

    @XmlElementWrapper(name="hostNames")
    @XmlElement(name="hostName")
    private List<String> hostNames;
    @XmlElement(name = "serviceHostNames")
    @XmlJavaTypeAdapter(DtoBizAuthorizedServiceHostMapAdapter.class)
    @JsonIgnore
    private Map<String,List<String>> serviceHostNames;

    /**
     * Default constructor.
     */
    public DtoBizAuthorizedServices() {
    }

    /**
     * Full constructor.
     */
    public DtoBizAuthorizedServices(List<String> hostNames, Map<String,List<String>> serviceHostNames) {
        this.hostNames = hostNames;
        this.serviceHostNames = serviceHostNames;
    }

    /**
     * toString Object protocol implementation.
     *
     * @return BizAuthorizedServices as String
     */
    @Override
    public String toString() {
        return String.format(getClass().getName()+"@%x[hostNames=%s,serviceHostNames=%s]",
                System.identityHashCode(this), hostNames, serviceHostNames);
    }

    public List<String> getHostNames() {
        return hostNames;
    }

    public void setHostNames(List<String> hostNames) {
        this.hostNames = hostNames;
    }

    @JsonProperty("serviceHostNames")
    public Map<String, List<String>> getServiceHostNames() {
        return serviceHostNames;
    }

    @JsonProperty("serviceHostNames")
    public void setServiceHostNames(Map<String, List<String>> serviceHostNames) {
        this.serviceHostNames = serviceHostNames;
    }
}
