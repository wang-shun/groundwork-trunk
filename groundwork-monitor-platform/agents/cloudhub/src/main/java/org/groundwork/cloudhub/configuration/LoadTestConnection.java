/*
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

package org.groundwork.cloudhub.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * LoadTestConnection
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
@XmlRootElement(name = "loadtest")
@XmlType(propOrder = {"hosts", "hostGroups", "services", "hostsDownPercent", "servicesCriticalPercent"})
@XmlAccessorType(XmlAccessType.FIELD)
public class LoadTestConnection implements MonitorConnection {

    private static final String LOAD_TEST_MGMT_SERVER = "loadtest-management-server";

    private static final int DEFAULT_HOSTS = 100;
    private static final int DEFAULT_HOST_GROUPS = 10;
    private static final int DEFAULT_SERVICES = 10;
    private static final float DEFAULT_HOSTS_DOWN_PERCENT = 1.0F;
    private static final float DEFAULT_SERVICES_CRITICAL_PERCENT = 1.0F;

    private int hosts = DEFAULT_HOSTS;
    private int hostGroups = DEFAULT_HOST_GROUPS;
    private int services = DEFAULT_SERVICES;
    private float hostsDownPercent = DEFAULT_HOSTS_DOWN_PERCENT;
    private float servicesCriticalPercent = DEFAULT_SERVICES_CRITICAL_PERCENT;

    @Override
    public String getServer() {
        return LOAD_TEST_MGMT_SERVER;
    }

    @Override
    public void setServer(String server) {
        throw new RuntimeException("Load test management server not configurable.");
    }

    @Override
    public String getHostName() {
        return LOAD_TEST_MGMT_SERVER;
    }

    public int getHosts() {
        return hosts;
    }

    public void setHosts(int hosts) {
        this.hosts = hosts;
    }

    public int getServices() {
        return services;
    }

    public void setServices(int services) {
        this.services = services;
    }

    public int getHostGroups() {
        return hostGroups;
    }

    public void setHostGroups(int hostGroups) {
        this.hostGroups = hostGroups;
    }

    public float getHostsDownPercent() {
        return hostsDownPercent;
    }

    public void setHostsDownPercent(float hostsDownPercent) {
        this.hostsDownPercent = hostsDownPercent;
    }

    public float getServicesCriticalPercent() {
        return servicesCriticalPercent;
    }

    public void setServicesCriticalPercent(float servicesCriticalPercent) {
        this.servicesCriticalPercent = servicesCriticalPercent;
    }
}
