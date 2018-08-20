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

package org.groundwork.rs.client;

import org.groundwork.rs.dto.DtoCustomGroup;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostGroup;
import org.groundwork.rs.dto.DtoService;
import org.groundwork.rs.dto.DtoServiceGroup;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.List;

/**
 * RTMMClientTest
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class RTMMClientTest extends AbstractClientTest {

    @Test
    public void testRTMMListAndLookup() {
        if (serverDown) return;

        // get clients
        RTMMClient client = new RTMMClient(getDeploymentURL(), MediaType.APPLICATION_JSON_TYPE);

        // test list hosts
        List<DtoHost> hosts = client.listHosts();
        assert hosts != null;
        assert !hosts.isEmpty();
        DtoHost localhostHost = null;
        for (DtoHost host : hosts) {
            if ("localhost".equals(host.getHostName())) {
                localhostHost = host;
                break;
            }
        }
        assertLocalhostHost(localhostHost);
        // test lookup host
        localhostHost = client.lookupHost(localhostHost.getId());
        assertLocalhostHost(localhostHost);
        // test lookup hosts
        if (hosts.size() >= 2) {
            hosts = client.lookupHosts(Arrays.asList(hosts.get(0).getId(), hosts.get(1).getId()));
            assert hosts != null;
            assert hosts.size() == 2;
        }

        // test list host groups
        List<DtoHostGroup> hostGroups = client.listHostGroups();
        assert hostGroups != null;
        assert !hostGroups.isEmpty();
        DtoHostGroup linuxServersHostGroup = null;
        for (DtoHostGroup hostGroup : hostGroups) {
            if ("Linux Servers".equals(hostGroup.getName())) {
                linuxServersHostGroup = hostGroup;
                break;
            }
        }
        assertLinuxServersHostGroup(linuxServersHostGroup);
        // test lookup host group
        linuxServersHostGroup = client.lookupHostGroup(linuxServersHostGroup.getId());
        assertLinuxServersHostGroup(linuxServersHostGroup);
        // test lookup host groups
        if (hostGroups.size() >= 2) {
            hostGroups = client.lookupHostGroups(Arrays.asList(hostGroups.get(0).getId(), hostGroups.get(1).getId()));
            assert hostGroups != null;
            assert hostGroups.size() == 2;
        }

        // test list service groups
        List<DtoServiceGroup> serviceGroups = client.listServiceGroups();
        assert serviceGroups != null;
        assert !serviceGroups.isEmpty();
        DtoServiceGroup sg1ServiceGroup = null;
        for (DtoServiceGroup serviceGroup : serviceGroups) {
            if ("SG1".equals(serviceGroup.getName())) {
                sg1ServiceGroup = serviceGroup;
                break;
            }
        }
        assertSG1ServiceGroup(sg1ServiceGroup);
        // test lookup service group
        sg1ServiceGroup = client.lookupServiceGroup(sg1ServiceGroup.getId());
        assertSG1ServiceGroup(sg1ServiceGroup);
        // test lookup service groups
        if (serviceGroups.size() >= 2) {
            serviceGroups = client.lookupServiceGroups(Arrays.asList(serviceGroups.get(0).getId(),
                    serviceGroups.get(1).getId()));
            assert serviceGroups != null;
            assert serviceGroups.size() == 2;
        }

        // test list custom groups
        List<DtoCustomGroup> customGroups = client.listCustomGroups();
        assert customGroups != null;
        assert !customGroups.isEmpty();
        DtoCustomGroup cg1CustomGroup = null;
        DtoCustomGroup cg2CustomGroup = null;
        DtoCustomGroup cg3CustomGroup = null;
        for (DtoCustomGroup customGroup : customGroups) {
            if ("CG1".equals(customGroup.getName())) {
                cg1CustomGroup = customGroup;
            } else if ("CG2".equals(customGroup.getName())) {
                cg2CustomGroup = customGroup;
            } else if ("CG3".equals(customGroup.getName())) {
                cg3CustomGroup = customGroup;
            }
        }
        assertCG1CustomGroup(cg1CustomGroup);
        assertCG2CustomGroup(cg2CustomGroup);
        assertCG3CustomGroup(cg3CustomGroup);
        // test lookup custom group
        cg1CustomGroup = client.lookupCustomGroup(cg1CustomGroup.getId());
        assertCG1CustomGroup(cg1CustomGroup);
        cg2CustomGroup = client.lookupCustomGroup(cg2CustomGroup.getId());
        assertCG2CustomGroup(cg2CustomGroup);
        cg3CustomGroup = client.lookupCustomGroup(cg3CustomGroup.getId());
        assertCG3CustomGroup(cg3CustomGroup);
        // test lookup custom groups
        if (customGroups.size() >= 2) {
            customGroups = client.lookupCustomGroups(Arrays.asList(customGroups.get(0).getId(),
                    customGroups.get(1).getId()));
            assert customGroups != null;
            assert customGroups.size() == 2;
        }
    }

    private static void assertLocalhostHost(DtoHost localhostHost) {
        assert localhostHost != null;
        assert localhostHost.getId() != null;
        assert "localhost".equals(localhostHost.getHostName());
        assert "NAGIOS".equals(localhostHost.getAppType());
        assert "UP".equals(localhostHost.getMonitorStatus());
        assert "NAGIOS".equals(localhostHost.getAppTypeDisplayName());
        String serviceAvailability = localhostHost.getServiceAvailability();
        assert serviceAvailability != null;
        assert Double.parseDouble(serviceAvailability) >= 90.0;
        assert localhostHost.getBubbleUpStatus() != null;
        assert localhostHost.getLastPlugInOutput() != null;
        assert "Linux Server #1".equals(localhostHost.getAlias());
        assert localhostHost.getLastCheckTime() != null;
        assert !localhostHost.isAcknowledged();
        assert localhostHost.getLastStateChange() != null;
        assert localhostHost.getProperties().get("isProblemAcknowledged") == null;
        assert "0".equals(localhostHost.getProperties().get("ScheduledDowntimeDepth"));

        assert localhostHost.getServices() != null;
        assert !localhostHost.getServices().isEmpty();
        DtoService localMemJavaService = null;
        for (DtoService service : localhostHost.getServices()) {
            if ("local_mem_java".equals(service.getDescription())) {
                localMemJavaService = service;
                break;
            }
        }
        assert localMemJavaService != null;
        assert localMemJavaService.getId() != null;
        assert "local_mem_java".equals(localMemJavaService.getDescription());
        assert "NAGIOS".equals(localMemJavaService.getAppType());
        assert "NAGIOS".equals(localMemJavaService.getAppTypeDisplayName());
        assert localMemJavaService.getMonitorStatus() != null;
        assert localMemJavaService.getLastCheckTime() != null;
        assert localMemJavaService.getNextCheckTime() != null;
        assert localMemJavaService.getLastStateChange() != null;
        assert "false".equals(localMemJavaService.getProperties().get("isProblemAcknowledged"));
        assert localMemJavaService.getLastPlugInOutput() != null;
    }

    private static void assertLinuxServersHostGroup(DtoHostGroup linuxServersHostGroup) {
        assert linuxServersHostGroup != null;
        assert linuxServersHostGroup.getId() != null;
        assert "Linux Servers".equals(linuxServersHostGroup.getName());
        assert "Linux Servers".equals(linuxServersHostGroup.getAlias());
        assert "NAGIOS".equals(linuxServersHostGroup.getAppType());
        assert linuxServersHostGroup.getHosts() != null;
        assert !linuxServersHostGroup.getHosts().isEmpty();
        for (DtoHost host : linuxServersHostGroup.getHosts()) {
            assert host.getId() != null;
        }
    }

    private static void assertSG1ServiceGroup(DtoServiceGroup sg1ServiceGroup) {
        assert sg1ServiceGroup != null;
        assert sg1ServiceGroup.getId() != null;
        assert "SG1".equals(sg1ServiceGroup.getName());
        assert "NAGIOS".equals(sg1ServiceGroup.getAppType());
        assert sg1ServiceGroup.getServices() != null;
        assert !sg1ServiceGroup.getServices().isEmpty();
        for (DtoService service : sg1ServiceGroup.getServices()) {
            assert service.getId() != null;
        }
    }

    private static void assertCG1CustomGroup(DtoCustomGroup cg1CustomGroup) {
        assert cg1CustomGroup != null;
        assert cg1CustomGroup.getId() != null;
        assert "CG1".equals(cg1CustomGroup.getName());
        assert cg1CustomGroup.isRoot();
        assert cg1CustomGroup.getChildren() != null;
        assert !cg1CustomGroup.getChildren().isEmpty();
        for (DtoCustomGroup customGroup : cg1CustomGroup.getChildren()) {
            assert customGroup.getId() != null;
        }
    }

    private static void assertCG2CustomGroup(DtoCustomGroup cg2CustomGroup) {
        assert cg2CustomGroup != null;
        assert cg2CustomGroup.getId() != null;
        assert "CG2".equals(cg2CustomGroup.getName());
        assert !cg2CustomGroup.isRoot();
        assert cg2CustomGroup.getHostGroups() != null;
        assert !cg2CustomGroup.getHostGroups().isEmpty();
        for (DtoHostGroup hostGroup : cg2CustomGroup.getHostGroups()) {
            assert hostGroup.getId() != null;
        }
    }

    private static void assertCG3CustomGroup(DtoCustomGroup cg3CustomGroup) {
        assert cg3CustomGroup != null;
        assert cg3CustomGroup.getId() != null;
        assert "CG3".equals(cg3CustomGroup.getName());
        assert !cg3CustomGroup.isRoot();
        assert cg3CustomGroup.getServiceGroups() != null;
        assert !cg3CustomGroup.getServiceGroups().isEmpty();
        for (DtoServiceGroup serviceGroup : cg3CustomGroup.getServiceGroups()) {
            assert serviceGroup.getId() != null;
        }
    }
}
