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

package com.groundwork.collage.model;

import java.util.Set;
import java.util.UUID;

/**
 * HostIdentity
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public interface HostIdentity extends PropertyExtensible {

    /** Entity type */
    static final String ENTITY_TYPE_CODE = "HostIdentity";

    /** Spring bean interface id */
    static final String INTERFACE_NAME = "com.groundwork.collage.model.HostIdentity";

    /** Hibernate component name that this entity service using */
    static final String COMPONENT_NAME = "com.groundwork.collage.model.impl.HostIdentity";

    /* Built-In (Non-dynamic) Properties - The Properties which have a description (hibernate property)
     * defined are properties that can be used to query the entity.
     * We distinquish between "filterable" properties and properties that are returned in
     * property maps.
     */
    static final String HP_ID = "hostIdentityId";
    static final String HP_HOST_NAME = "hostName";
    static final String HP_HOST_ID = "host.hostId";
    static final String HP_HOST_HOST_NAME = "host.hostName";

    /** Filter-Only Properties */
    static final String HP_HOST_NAMES = "hostNames";

    /** Entity Property Constants */
    static final String EP_ID = "HostIdentityId";
    static final String EP_HOST_NAME = "HostName";
    static final String EP_HOST_ID = "HostId";
    static final String EP_HOST_HOST_NAME = "HostHostName";
    static final String EP_HOST_NAMES = "HostNames";

    UUID getHostIdentityId();

    String getHostName();
    void setHostName(String hostName);

    Set<String> getHostNames();

    Host getHost();
    void setHost(Host host);
}
