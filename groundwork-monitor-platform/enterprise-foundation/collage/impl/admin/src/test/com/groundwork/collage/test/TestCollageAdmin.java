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

package com.groundwork.collage.test;

import com.groundwork.collage.CollageAdminInfrastructure;
import com.groundwork.collage.model.Device;
import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.HostGroup;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.groundwork.foundation.bs.device.DeviceService;
import org.groundwork.foundation.bs.host.HostService;
import org.groundwork.foundation.bs.hostgroup.HostGroupService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * TestCollageAdmin
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class TestCollageAdmin extends AbstractTestAdminBase {

    public TestCollageAdmin(String x) {
        super(x);
    }

    /**
     * define the tests to be run in this class
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(TestCollageAdmin.class);

        //suite.addTest(new TestCollageAdmin("testHostGroupAdmin"));

        return suite;
    }

    public void testHostGroupAdmin() {

        HostService hostService = collage.getHostService();
        HostGroupService hostGroupService = collage.getHostGroupService();

        try {
            // create test host
            Map<String, String> hostProperties0 = new HashMap<String, String>();
            hostProperties0.put(CollageAdminInfrastructure.PROP_HOST_NAME, "test-host-0");
            hostProperties0.put(CollageAdminInfrastructure.PROP_DEVICE_IDENTIFICATION, "test-device-0");
            admin.addOrUpdateHost(hostProperties0);
            Map<String, String> hostProperties1 = new HashMap<String, String>();
            hostProperties1.put(CollageAdminInfrastructure.PROP_HOST_NAME, "test-host-1");
            hostProperties1.put(CollageAdminInfrastructure.PROP_DEVICE_IDENTIFICATION, "test-device-1");
            admin.addOrUpdateHost(hostProperties1);

            // add host to host group
            beginTransaction();
            admin.addHostsToHostGroup("NAGIOS", "test-host-group-0", Arrays.asList(new String[]{"test-host-0"}));
            Host host0 = hostService.getHostByHostName("test-host-0");
            HostGroup hostGroup0 = hostGroupService.getHostGroupByName("test-host-group-0");
            assertNotNull(host0);
            assertNotNull(hostGroup0);
            assertNotNull(host0.getHostGroups());
            assertEquals(1, host0.getHostGroups().size());
            assertTrue(host0.getHostGroups().contains(hostGroup0));
            // known issue: addHostsToHostGroup() does not leave object model in consistent state since
            // it only updates one side of the Host <-> HostGroup bidirectional relationship for
            // performance reasons. See CollageAdminImpl.java.
            assertNotNull(hostGroup0.getHosts());
            assertEquals(0, hostGroup0.getHosts().size());
            // commit and refetch hostGroup0 to pick up addHostsToHostGroup() change from database
            commitTransaction();
            beginTransaction();
            hostGroup0 = hostGroupService.getHostGroupByName("test-host-group-0");
            assertNotNull(hostGroup0.getHosts());
            assertEquals(1, hostGroup0.getHosts().size());
            assertTrue(hostGroup0.getHosts().contains(host0));
            commitTransaction();

            // update host group
            beginTransaction();
            admin.updateHostGroup("NAGIOS", "test-host-group-0", Arrays.asList(new String[]{"test-host-1"}));
            host0 = hostService.getHostByHostName("test-host-0");
            hostGroup0 = hostGroupService.getHostGroupByName("test-host-group-0");
            Host host1 = hostService.getHostByHostName("test-host-1");
            assertNotNull(host0);
            assertNotNull(hostGroup0);
            assertNotNull(host1);
            assertNotNull(host0.getHostGroups());
            assertEquals(1, host0.getHostGroups().size());
            assertNotNull(host1.getHostGroups());
            assertEquals(1, host1.getHostGroups().size());
            assertTrue(host1.getHostGroups().contains(hostGroup0));
            assertNotNull(hostGroup0.getHosts());
            assertEquals(2, hostGroup0.getHosts().size());
            assertTrue(hostGroup0.getHosts().contains(host0));
            assertTrue(hostGroup0.getHosts().contains(host1));
            commitTransaction();

        } finally {
            // remove test host and host groups
            try {
                rollbackTransaction();
            } catch (Exception e) {
            }
            try {
                admin.removeHostGroup("test-host-group-0");
            } catch (Exception e) {
            }
            try {
                admin.removeHost("test-host-0");
            } catch (Exception e) {
            }
            try {
                admin.removeDevice("test-device-0");
            } catch (Exception e) {
            }
            try {
                admin.removeHost("test-host-1");
            } catch (Exception e) {
            }
            try {
                admin.removeDevice("test-device-1");
            } catch (Exception e) {
            }
        }
    }

    public static final String HOST_NAME = "docker1-host-1";
    public static final String HOST_DESCRIPTION = "docker1-host-1-desc";
    public static final String NEW_HOST_NAME = "docker2-host-1";
    public static final String NEW_HOST_DESCRIPTION = "docker2-host-1-desc";
    public static final String DEVICE_NAME = HOST_NAME;
    public static final String NEW_DEVICE_NAME = NEW_HOST_NAME;

    public void testRenameHost() {
        HostService hostService = collage.getHostService();
        DeviceService deviceService = collage.getDeviceService();
        try {
            Map<String, String> properties = new HashMap<String, String>();
            properties.put(CollageAdminInfrastructure.PROP_HOST_NAME, HOST_NAME);
            properties.put(CollageAdminInfrastructure.PROP_DESCRIPTION, HOST_DESCRIPTION);
            properties.put(CollageAdminInfrastructure.PROP_DEVICE_IDENTIFICATION, DEVICE_NAME);
            properties.put(CollageAdminInfrastructure.PROP_DISPLAY_NAME, DEVICE_NAME);
            admin.addOrUpdateHost(properties);

            Host host = hostService.getHostByHostName(HOST_NAME);
            assert host.getHostName().equals(HOST_NAME);
            assert host.getDescription().equals(HOST_DESCRIPTION);
            assert host.getDevice().getIdentification().equals(DEVICE_NAME);

            Host renamedHost = admin.renameHost(HOST_NAME, NEW_HOST_NAME, NEW_HOST_DESCRIPTION, NEW_DEVICE_NAME);
            assert renamedHost != null;
            assert renamedHost.getHostName().equals(NEW_HOST_NAME);
            assert renamedHost.getDescription().equals(NEW_HOST_DESCRIPTION);
            assert renamedHost.getDevice().getIdentification().equals(NEW_DEVICE_NAME);

            assert hostService.getHostByHostName(HOST_NAME) == null;

            Host host2 = hostService.getHostByHostName(NEW_HOST_NAME);
            assert host2 != null;
            assert host2.getHostName().equals(NEW_HOST_NAME);
            assert host2.getDescription().equals(NEW_HOST_DESCRIPTION);
            assert host2.getDevice().getIdentification().equals(NEW_DEVICE_NAME);

            assert deviceService.getDeviceByIdentification(DEVICE_NAME) == null;

            Device device = deviceService.getDeviceByIdentification(NEW_DEVICE_NAME);
            assert device != null;
            assert device.getIdentification().equals(NEW_DEVICE_NAME);
            assert device.getDisplayName().equals(NEW_DEVICE_NAME);

        } finally {
            admin.removeDevice(NEW_DEVICE_NAME);
            assert hostService.getHostByHostName(NEW_HOST_NAME) == null;
            assert deviceService.getDeviceByIdentification(NEW_DEVICE_NAME) == null;
        }
    }
}
