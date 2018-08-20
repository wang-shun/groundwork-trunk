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

package com.groundwork.collage.biz;

import com.groundwork.collage.biz.model.RTMMCustomGroup;
import com.groundwork.collage.biz.model.RTMMHost;
import com.groundwork.collage.biz.model.RTMMHostGroup;
import com.groundwork.collage.biz.model.RTMMServiceGroup;

import java.util.Collection;

/**
 * RTMMService
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public interface RTMMServices {

    public final static String SERVICE = "com.groundwork.collage.biz.RTMMServices";

    /**
     * Get all hosts with services. Optimized for RTMM access.
     *
     * See {@link com.groundwork.collage.biz.model.RTMMHost} and
     * {@link com.groundwork.collage.biz.model.RTMMService}.
     *
     * @return collection of optimized RTMM host instances
     */
    Collection<RTMMHost> getHosts();

    /**
     * Get host with services by id. Optimized for RTMM access.
     *
     * See {@link com.groundwork.collage.biz.model.RTMMHost} and
     * {@link com.groundwork.collage.biz.model.RTMMService}.
     *
     * @param hostId host id
     * @return an optimized RTMM host instance or null
     */
    RTMMHost getHost(int hostId);

    /**
     * Get hosts with services by id. Optimized for RTMM access.
     * Hosts are not returned in order of specified ids.
     *
     * See {@link com.groundwork.collage.biz.model.RTMMHost} and
     * {@link com.groundwork.collage.biz.model.RTMMService}.
     *
     * @param hostIds host ids
     * @return collection of optimized RTMM host instances
     */
    Collection<RTMMHost> getHosts(Integer [] hostIds);

    /**
     * Get all host groups. Optimized for RTMM access: references
     * hosts by id.
     *
     * See {@link com.groundwork.collage.biz.model.RTMMHostGroup}.
     *
     * @return collection of optimized RTMM host group instances
     */
    Collection<RTMMHostGroup> getHostGroups();

    /**
     * Get host group by id. Optimized for RTMM access: references
     * hosts by id.
     *
     * See {@link com.groundwork.collage.biz.model.RTMMHostGroup}.
     *
     * @param hostGroupId host group id
     * @return an optimized RTMM host group instance or null
     */
    RTMMHostGroup getHostGroup(int hostGroupId);

    /**
     * Get host groups by id. Optimized for RTMM access: references
     * hosts by id. Host groups are not returned in order of
     * specified ids.
     *
     * See {@link com.groundwork.collage.biz.model.RTMMHostGroup}.
     *
     * @param hostGroupIds host group ids
     * @return collection of optimized RTMM host group instances
     */
    Collection<RTMMHostGroup> getHostGroups(Integer [] hostGroupIds);

    /**
     * Get all service groups. Optimized for RTMM access: references
     * services by id.
     *
     * See {@link com.groundwork.collage.biz.model.RTMMServiceGroup}.
     *
     * @return collection of optimized RTMM service group instances
     */
    Collection<RTMMServiceGroup> getServiceGroups();

    /**
     * Get service group by id. Optimized for RTMM access: references
     * services by id.
     *
     * See {@link com.groundwork.collage.biz.model.RTMMServiceGroup}.
     *
     * @param serviceGroupId service group id
     * @return an optimized RTMM service group instance or null
     */
    RTMMServiceGroup getServiceGroup(int serviceGroupId);

    /**
     * Get service groups by id. Optimized for RTMM access: references
     * services by id. Service groups are not returned in order of
     * specified ids.
     *
     * See {@link com.groundwork.collage.biz.model.RTMMServiceGroup}.
     *
     * @param serviceGroupIds service group ids
     * @return collection of optimized RTMM service group instances
     */
    Collection<RTMMServiceGroup> getServiceGroups(Integer [] serviceGroupIds);

    /**
     * Get all custom groups. Optimized for RTMM access: references
     * other custom groups, host groups, and service groups by id.
     *
     * See {@link com.groundwork.collage.biz.model.RTMMCustomGroup}.
     *
     * @return collection of optimized RTMM custom group instances
     */
    Collection<RTMMCustomGroup> getCustomGroups();

    /**
     * Get custom group by id. Optimized for RTMM access: references
     * other custom groups, host groups, and service groups by id.
     *
     * See {@link com.groundwork.collage.biz.model.RTMMCustomGroup}.
     *
     * @param customGroupId custom group id
     * @return an optimized RTMM custom group instance or null
     */
    RTMMCustomGroup getCustomGroup(int customGroupId);

    /**
     * Get custom groups by id. Optimized for RTMM access: references
     * other custom groups, host groups, and service groups by id.
     * Custom groups are not returned in order of specified ids.
     *
     * See {@link com.groundwork.collage.biz.model.RTMMCustomGroup}.
     *
     * @param customGroupIds custom group ids
     * @return collection of optimized RTMM custom group instances
     */
    Collection<RTMMCustomGroup> getCustomGroups(Integer [] customGroupIds);
}
