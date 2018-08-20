/*
 * Copyright (C) 2017 GroundWork Open Source, Inc. (GroundWork) All rights
 * reserved. This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package com.groundwork.dashboard.portlets.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NocBoardServiceGroup {

    protected String serviceGroupName;

    protected List<NocBoardService> services = new ArrayList<>();

    public String getServiceGroupName() {
        return serviceGroupName;
    }

    public void setServiceGroupName(String serviceGroupName) {
        this.serviceGroupName = serviceGroupName;
    }

    public List<NocBoardService> getServices() {
        return services;
    }

    public void setServices(List<NocBoardService> services) {
        this.services = services;
    }

    public void addService(NocBoardService service) {
        services.add(service);
    }

    public int getServiceCount() {
        return services.size();
    }

    public NocBoardServiceGroup(String serviceGroupName, List<NocBoardService> services) {
        this.serviceGroupName = serviceGroupName;
        this.services = services;
    }
}
