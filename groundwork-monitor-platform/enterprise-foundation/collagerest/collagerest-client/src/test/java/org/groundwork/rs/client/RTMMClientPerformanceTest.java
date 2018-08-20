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
import org.groundwork.rs.dto.DtoDepthType;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostGroup;
import org.groundwork.rs.dto.DtoServiceGroup;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.List;

/**
 * RTMMClientPerformanceTest
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class RTMMClientPerformanceTest extends AbstractClientTest {

    @Test
    public void performanceTest() {
        if (serverDown) return;

        // get clients
        LicenseClient licenseClient = new LicenseClient(getDeploymentURL(), MediaType.APPLICATION_JSON_TYPE);
        RTMMClient client = new RTMMClient(getDeploymentURL(), MediaType.APPLICATION_JSON_TYPE);
        HostClient hostClient = new HostClient(getDeploymentURL(), MediaType.APPLICATION_JSON_TYPE);
        HostGroupClient hostGroupClient = new HostGroupClient(getDeploymentURL(), MediaType.APPLICATION_JSON_TYPE);
        ServiceGroupClient serviceGroupClient = new ServiceGroupClient(getDeploymentURL(), MediaType.APPLICATION_JSON_TYPE);
        CustomGroupClient customGroupClient = new CustomGroupClient(getDeploymentURL(), MediaType.APPLICATION_JSON_TYPE);

        // open client connection
        licenseClient.check(0);

        // test list hosts
        long start = System.currentTimeMillis();
        List<DtoHost> hosts = client.listHosts();
        long end = System.currentTimeMillis();
        assert hosts != null;
        log.info(String.format("Elapsed time for %d RTMM list hosts: %d", hosts.size(), (end-start)));
        start = System.currentTimeMillis();
        hosts = hostClient.list(DtoDepthType.Deep);
        end = System.currentTimeMillis();
        assert hosts != null;
        log.info(String.format("Elapsed time for %d list hosts: %d", hosts.size(), (end-start)));

        // test lookup host
        if (!hosts.isEmpty()) {
            int id = hosts.get(0).getId();
            start = System.currentTimeMillis();
            DtoHost host = client.lookupHost(id);
            end = System.currentTimeMillis();
            assert host != null;
            log.info(String.format("Elapsed time for RTMM lookup host: %d", (end-start)));
            start = System.currentTimeMillis();
            List<DtoHost> queryHosts = hostClient.query("id = "+id, DtoDepthType.Deep);
            end = System.currentTimeMillis();
            assert queryHosts != null;
            assert !queryHosts.isEmpty();
            host = queryHosts.get(0);
            assert host != null;
            log.info(String.format("Elapsed time for lookup host: %d", (end-start)));

            // test lookup hosts
            if (hosts.size() >= 2) {
                List<Integer> ids = Arrays.asList(hosts.get(0).getId(), hosts.get(1).getId());
                start = System.currentTimeMillis();
                queryHosts = client.lookupHosts(ids);
                assert queryHosts != null;
                assert queryHosts.size() == 2;
                end = System.currentTimeMillis();
                log.info(String.format("Elapsed time for RTMM lookup hosts: %d", (end - start)));
                start = System.currentTimeMillis();
                queryHosts = hostClient.query("id in ( " + ids.get(0) + ", " + ids.get(1) + ")", DtoDepthType.Deep);
                end = System.currentTimeMillis();
                assert queryHosts != null;
                assert queryHosts.size() == 2;
                log.info(String.format("Elapsed time for lookup hosts: %d", (end - start)));
            }
        }

        // test list host groups
        start = System.currentTimeMillis();
        List<DtoHostGroup> hostGroups = client.listHostGroups();
        end = System.currentTimeMillis();
        assert hostGroups != null;
        log.info(String.format("Elapsed time for %d RTMM list host groups: %d", hostGroups.size(), (end-start)));
        start = System.currentTimeMillis();
        hostGroups = hostGroupClient.list();
        end = System.currentTimeMillis();
        assert hostGroups != null;
        log.info(String.format("Elapsed time for %d list host groups: %d", hostGroups.size(), (end-start)));

        // test lookup host group
        if (!hostGroups.isEmpty()) {
            int id = hostGroups.get(0).getId();
            start = System.currentTimeMillis();
            DtoHostGroup hostGroup = client.lookupHostGroup(id);
            end = System.currentTimeMillis();
            assert hostGroup != null;
            log.info(String.format("Elapsed time for RTMM lookup host group: %d", (end-start)));
            start = System.currentTimeMillis();
            List<DtoHostGroup> queryHostGroups = hostGroupClient.query("id = "+id);
            end = System.currentTimeMillis();
            assert queryHostGroups != null;
            assert !queryHostGroups.isEmpty();
            hostGroup = queryHostGroups.get(0);
            assert hostGroup != null;
            log.info(String.format("Elapsed time for lookup host group: %d", (end-start)));

            // test lookup host groups
            if (hostGroups.size() >= 2) {
                List<Integer> ids = Arrays.asList(hostGroups.get(0).getId(), hostGroups.get(1).getId());
                start = System.currentTimeMillis();
                queryHostGroups = client.lookupHostGroups(ids);
                assert queryHostGroups != null;
                assert queryHostGroups.size() == 2;
                end = System.currentTimeMillis();
                log.info(String.format("Elapsed time for RTMM lookup host groups: %d", (end - start)));
                start = System.currentTimeMillis();
                queryHostGroups = hostGroupClient.query("id in ( " + ids.get(0) + ", " + ids.get(1) + ")");
                end = System.currentTimeMillis();
                assert queryHostGroups != null;
                assert queryHostGroups.size() == 2;
                log.info(String.format("Elapsed time for lookup host groups: %d", (end - start)));
            }
        }

        // test list service groups
        start = System.currentTimeMillis();
        List<DtoServiceGroup> serviceGroups = client.listServiceGroups();
        end = System.currentTimeMillis();
        assert serviceGroups != null;
        log.info(String.format("Elapsed time for %d RTMM list service groups: %d", serviceGroups.size(), (end-start)));
        start = System.currentTimeMillis();
        serviceGroups = serviceGroupClient.list();
        end = System.currentTimeMillis();
        assert serviceGroups != null;
        log.info(String.format("Elapsed time for %d list service groups: %d", serviceGroups.size(), (end-start)));

        // test lookup service group
        if (!serviceGroups.isEmpty()) {
            int id = serviceGroups.get(0).getId();
            start = System.currentTimeMillis();
            DtoServiceGroup serviceGroup = client.lookupServiceGroup(id);
            end = System.currentTimeMillis();
            assert serviceGroup != null;
            log.info(String.format("Elapsed time for RTMM lookup service group: %d", (end-start)));
            start = System.currentTimeMillis();
            List<DtoServiceGroup> queryServiceGroups = serviceGroupClient.query("id = "+id);
            end = System.currentTimeMillis();
            assert queryServiceGroups != null;
            assert !queryServiceGroups.isEmpty();
            serviceGroup = queryServiceGroups.get(0);
            assert serviceGroup != null;
            log.info(String.format("Elapsed time for lookup service group: %d", (end-start)));

            // test lookup service groups
            if (serviceGroups.size() >= 2) {
                List<Integer> ids = Arrays.asList(serviceGroups.get(0).getId(), serviceGroups.get(1).getId());
                start = System.currentTimeMillis();
                queryServiceGroups = client.lookupServiceGroups(ids);
                assert queryServiceGroups != null;
                assert queryServiceGroups.size() == 2;
                end = System.currentTimeMillis();
                log.info(String.format("Elapsed time for RTMM lookup service groups: %d", (end - start)));
                start = System.currentTimeMillis();
                queryServiceGroups = serviceGroupClient.query("id in ( " + ids.get(0) + ", " + ids.get(1) + ")");
                end = System.currentTimeMillis();
                assert queryServiceGroups != null;
                assert queryServiceGroups.size() == 2;
                log.info(String.format("Elapsed time for lookup service groups: %d", (end - start)));
            }
        }

        // test list custom groups
        start = System.currentTimeMillis();
        List<DtoCustomGroup> customGroups = client.listCustomGroups();
        end = System.currentTimeMillis();
        assert customGroups != null;
        log.info(String.format("Elapsed time for %d RTMM list custom groups: %d", customGroups.size(), (end-start)));
        start = System.currentTimeMillis();
        customGroups = customGroupClient.list();
        end = System.currentTimeMillis();
        assert customGroups != null;
        log.info(String.format("Elapsed time for %d list custom groups: %d", customGroups.size(), (end-start)));

        // test lookup custom group
        if (!customGroups.isEmpty()) {
            int id = customGroups.get(0).getId();
            start = System.currentTimeMillis();
            DtoCustomGroup customGroup = client.lookupCustomGroup(id);
            end = System.currentTimeMillis();
            assert customGroup != null;
            log.info(String.format("Elapsed time for RTMM lookup custom group: %d", (end-start)));
            start = System.currentTimeMillis();
            List<DtoCustomGroup> queryCustomGroups = customGroupClient.query("id = "+id);
            end = System.currentTimeMillis();
            assert queryCustomGroups != null;
            assert !queryCustomGroups.isEmpty();
            customGroup = queryCustomGroups.get(0);
            assert customGroup != null;
            log.info(String.format("Elapsed time for lookup custom group: %d", (end-start)));

            // test lookup custom groups
            if (customGroups.size() >= 2) {
                List<Integer> ids = Arrays.asList(customGroups.get(0).getId(), customGroups.get(1).getId());
                start = System.currentTimeMillis();
                queryCustomGroups = client.lookupCustomGroups(ids);
                assert queryCustomGroups != null;
                assert queryCustomGroups.size() == 2;
                end = System.currentTimeMillis();
                log.info(String.format("Elapsed time for RTMM lookup custom groups: %d", (end - start)));
                start = System.currentTimeMillis();
                queryCustomGroups = customGroupClient.query("id in ( " + ids.get(0) + ", " + ids.get(1) + ")");
                end = System.currentTimeMillis();
                assert queryCustomGroups != null;
                assert queryCustomGroups.size() == 2;
                log.info(String.format("Elapsed time for lookup custom groups: %d", (end - start)));
            }
        }
    }
}
