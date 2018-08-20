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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * DtoHostIdentity
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@XmlRootElement(name = "hostIdentity")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoHostIdentity {

    @XmlAttribute
    private UUID hostIdentityId;
    @XmlAttribute
    private String hostName;
    @XmlElementWrapper(name="hostNames")
    @XmlElement(name="hostName")
    private List<String> hostNames;
    @XmlAttribute
    private Boolean host;

    /**
     * HostIdentity constructor.
     *
     * @param hostName host name
     */
    public DtoHostIdentity(String hostName) {
        this(null, hostName, null, null);
    }

    /**
     * HostIdentity constructor.
     *
     * @param hostName host name
     * @param hostNames additional host names.
     */
    public DtoHostIdentity(String hostName, Collection<String> hostNames) {
        this(null, hostName, hostNames, null);
    }

    /**
     * HostIdentity constructor.
     *
     * @param hostIdentityId externally specified id
     * @param hostName host name
     */
    public DtoHostIdentity(UUID hostIdentityId, String hostName) {
        this(hostIdentityId, hostName, null, null);
    }

    /**
     * HostIdentity constructor.
     *
     * @param hostIdentityId externally specified id
     * @param hostName host name
     * @param hostNames additional host names.
     */
    public DtoHostIdentity(UUID hostIdentityId, String hostName, Collection<String> hostNames) {
        this(hostIdentityId, hostName, hostNames, null);
    }

    /**
     * HostIdentity constructor.
     *
     * @param hostIdentityId externally specified id
     * @param hostName host name
     * @param hostNames additional host names.
     * @param host has associated Host boolean flag
     */
    public DtoHostIdentity(UUID hostIdentityId, String hostName, Collection<String> hostNames, Boolean host) {
        if (hostName == null) {
            throw new IllegalArgumentException("hostName must be specified");
        }
        this.hostIdentityId = hostIdentityId;
        this.hostName = hostName;
        this.hostNames = ((hostNames != null) ? new ArrayList<String>(hostNames) : new ArrayList<String>());
        boolean hostNameInHostNames = false;
        for (String name : this.hostNames) {
            if (this.hostName.equalsIgnoreCase(name)) {
                hostNameInHostNames = true;
                break;
            }
        }
        if (!hostNameInHostNames) {
            this.hostNames.add(this.hostName);
        }
        this.host = host;
    }

    /**
     * Default HostIdentity constructor.
     */
    public DtoHostIdentity() {
    }

    /**
     * toString Object protocol implementation.
     *
     * @return HostIdentity as String
     */
    @Override
    public String toString() {
        return String.format(getClass().getName()+"@%x[hostIdentityId=%d,hostName=%s,hostNames=%s]",
                System.identityHashCode(this), hostIdentityId, hostName, hostNames);
    }

    public UUID getHostIdentityId() {
        return hostIdentityId;
    }

    public void setHostIdentityId(UUID hostIdentityId) {
        this.hostIdentityId = hostIdentityId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public List<String> getHostNames() {
        return hostNames;
    }

    public void setHostNames(List<String> hostNames) {
        this.hostNames = hostNames;
    }

    public Boolean getHost() {
        return host;
    }

    public void setHost(Boolean host) {
        this.host = host;
    }
}
