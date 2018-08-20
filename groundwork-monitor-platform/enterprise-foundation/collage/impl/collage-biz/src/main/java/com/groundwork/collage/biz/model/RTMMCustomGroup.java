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

package com.groundwork.collage.biz.model;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * RTMMCustomGroup
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class RTMMCustomGroup {

    private final Integer id;
    private final List<Integer> hostGroupIds = new ArrayList<Integer>();
    private final List<Integer> serviceGroupIds = new ArrayList<Integer>();
    private final List<Integer> childIds = new ArrayList<Integer>();

    private String name;
    private Boolean isRoot;

    public RTMMCustomGroup(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("id", id).
                append("name", name).
                append("isRoot", isRoot).
                append("hostGroupIds", hostGroupIds).
                append("serviceGroupIds", serviceGroupIds).
                append("childIds", childIds).
                toString();
    }

    public Integer getId() {
        return id;
    }

    public List<Integer> getHostGroupIds() {
        return hostGroupIds;
    }

    public List<Integer> getServiceGroupIds() {
        return serviceGroupIds;
    }

    public List<Integer> getChildIds() {
        return childIds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsRoot() {
        return isRoot;
    }

    public void setIsRoot(Boolean isRoot) {
        this.isRoot = isRoot;
    }
}
