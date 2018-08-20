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

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * DtoHostIdentityList
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@XmlRootElement(name="hostIdentities")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoHostIdentityList {

    @XmlElement(name="hostIdentity")
    @JsonProperty("hostIdentities")
    private List<DtoHostIdentity> hostIdentities = new ArrayList<DtoHostIdentity>();

    /**
     * Shallow copy constructor.
     *
     * @param hostIdentities HostIdentities instances to copy.
     */
    public DtoHostIdentityList(Collection<DtoHostIdentity> hostIdentities) {
        this.hostIdentities.addAll(hostIdentities);
    }

    /**
     * Default constructor.
     */
    public DtoHostIdentityList() {
    }

    /**
     * Add host identity instance to host identities list.
     *
     * @param hostIdentity host identity instance to add
     */
    public void add(DtoHostIdentity hostIdentity) {
        hostIdentities.add(hostIdentity);
    }

    /**
     * Get host identities list size.
     *
     * @return size of host identities list
     */
    public int size()
    {
        return hostIdentities.size();
    }

    public List<DtoHostIdentity> getHostIdentities() {
        return hostIdentities;
    }
}
