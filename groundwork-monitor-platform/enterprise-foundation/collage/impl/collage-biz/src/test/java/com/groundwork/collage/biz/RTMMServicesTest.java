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
import com.groundwork.collage.biz.model.RTMMService;
import com.groundwork.collage.biz.model.RTMMServiceGroup;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;

/**
 * RTMMServicesTest
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class RTMMServicesTest extends AbstractTestBizBase {

    private static final Log log = LogFactory.getLog(RTMMServicesTest.class);

    private static final boolean USE_TEST_DATA = Boolean.parseBoolean(System.getProperty("useTestData", "true"));

    public RTMMServicesTest(String x) {
        super(x);
    }

    public static Test suite()
    {
        if (USE_TEST_DATA) {
            executeScript(false, "../common/testdata/monitor-data.sql");
        }

        TestSuite suite = new TestSuite();
        suite.addTest(new RTMMServicesTest("testGetHosts"));
        suite.addTest(new RTMMServicesTest("testGetHostGroups"));
        suite.addTest(new RTMMServicesTest("testGetServiceGroups"));
        suite.addTest(new RTMMServicesTest("testGetCustomGroups"));
        return suite;
    }

    /**
     * Test RTMM services host access.
     *
     * @throws Exception
     */
    public void testGetHosts() throws Exception {
        RTMMServices rtmm = (RTMMServices) collage.getAPIObject(RTMMServices.SERVICE);
        assert rtmm != null;

        long start = System.currentTimeMillis();
        Collection<RTMMHost> hosts = rtmm.getHosts();
        long stop = System.currentTimeMillis();
        assert hosts != null;
        log.info("Loaded " + hosts.size() + " hosts in " + (stop - start) + "ms.");

        if (USE_TEST_DATA) {
            log.debug("Hosts:");
            for (RTMMHost host : hosts) {
                log.debug(host);
            }
            assert hosts.size() == 16;
            RTMMHost nagiosHost = null;
            for (RTMMHost host : hosts) {
                if ("nagios".equals(host.getHostName())) {
                    nagiosHost = host;
                    break;
                }
            }
            assert nagiosHost != null;
            assert nagiosHost.getServices().size() == 4;
            for (RTMMService service : nagiosHost.getServices()) {
                if ("local_disk".equals(service.getDescription())) {
                    assert "NAGIOS".equals(service.getAppTypeName());
                    assert "NAGIOS".equals(service.getAppTypeDisplayName());
                    assert "OK".equals(service.getMonitorStatus());
                    assert service.getLastCheckTime() != null;
                    assert service.getNextCheckTime() != null;
                    assert Boolean.TRUE.equals(service.getIsProblemAcknowledged());
                    assert service.getLastStateChange() != null;
                    assert service.getLastPluginOutput() != null;
                } else if ("local_procs".equals(service.getDescription())) {
                    assert "NAGIOS".equals(service.getAppTypeName());
                    assert "NAGIOS".equals(service.getAppTypeDisplayName());
                    assert "WARNING".equals(service.getMonitorStatus());
                    assert service.getLastCheckTime() != null;
                    assert service.getNextCheckTime() != null;
                    assert Boolean.TRUE.equals(service.getIsProblemAcknowledged());
                    assert service.getLastStateChange() != null;
                    assert service.getLastPluginOutput() != null;
                } else if ("local_users".equals(service.getDescription())) {
                    assert "NAGIOS".equals(service.getAppTypeName());
                    assert "NAGIOS".equals(service.getAppTypeDisplayName());
                    assert "OK".equals(service.getMonitorStatus());
                    assert service.getLastCheckTime() != null;
                    assert service.getNextCheckTime() != null;
                    assert Boolean.TRUE.equals(service.getIsProblemAcknowledged());
                    assert service.getLastStateChange() != null;
                    assert service.getLastPluginOutput() != null;
                } else if ("network_users".equals(service.getDescription())) {
                    assert "NAGIOS".equals(service.getAppTypeName());
                    assert "NAGIOS".equals(service.getAppTypeDisplayName());
                    assert "OK".equals(service.getMonitorStatus());
                    assert service.getLastCheckTime() != null;
                    assert service.getNextCheckTime() != null;
                    assert service.getIsProblemAcknowledged() == null;
                    assert service.getLastStateChange() != null;
                    assert service.getLastPluginOutput() == null;
                }
            }
            assert "nagios".equals(nagiosHost.getHostName());
            assert "NAGIOS".equals(nagiosHost.getAppTypeName());
            assert "NAGIOS".equals(nagiosHost.getAppTypeDisplayName());
            assert "UP".equals(nagiosHost.getMonitorStatus());
            assert nagiosHost.getLastCheckTime() != null;
            assert nagiosHost.getLastStateChange() != null;
        }

        if (!hosts.isEmpty()) {
            RTMMHost host = hosts.iterator().next();
            start = System.currentTimeMillis();
            host = rtmm.getHost(host.getId());
            stop = System.currentTimeMillis();
            assert host != null;
            log.info("Loaded host in " + (stop - start) + "ms.");

            if (USE_TEST_DATA) {
                log.debug("Host:");
                log.debug(host);
            }
        }
    }

    /**
     * Test RTMM services host group access.
     *
     * @throws Exception
     */
    public void testGetHostGroups() throws Exception {
        RTMMServices rtmm = (RTMMServices) collage.getAPIObject(RTMMServices.SERVICE);
        assert rtmm != null;

        long start = System.currentTimeMillis();
        Collection<RTMMHostGroup> hostGroups = rtmm.getHostGroups();
        long stop = System.currentTimeMillis();
        assert hostGroups != null;
        log.info("Loaded " + hostGroups.size() + " host groups in " + (stop - start) + "ms.");

        if (USE_TEST_DATA) {
            log.debug("Host groups:");
            for (RTMMHostGroup hostGroup : hostGroups) {
                log.debug(hostGroup);
            }
            assert hostGroups.size() == 14;
            RTMMHostGroup demoSystemHostGroup = null;
            for (RTMMHostGroup hostGroup : hostGroups) {
                if ("demo-system".equals(hostGroup.getName())) {
                    demoSystemHostGroup = hostGroup;
                    break;
                }
            }
            assert demoSystemHostGroup != null;
            assert "demo-system".equals(demoSystemHostGroup.getName());
            assert "NAGIOS".equals(demoSystemHostGroup.getAppTypeName());
            assert demoSystemHostGroup.getHostIds().size() == 4;
        }

        if (!hostGroups.isEmpty()) {
            RTMMHostGroup hostGroup = hostGroups.iterator().next();
            start = System.currentTimeMillis();
            hostGroup = rtmm.getHostGroup(hostGroup.getId());
            stop = System.currentTimeMillis();
            assert hostGroup != null;
            log.info("Loaded host group in " + (stop - start) + "ms.");

            if (USE_TEST_DATA) {
                log.debug("Host group:");
                log.debug(hostGroup);
            }
        }
    }

    /**
     * Test RTMM services service group access.
     *
     * @throws Exception
     */
    public void testGetServiceGroups() throws Exception {
        RTMMServices rtmm = (RTMMServices) collage.getAPIObject(RTMMServices.SERVICE);
        assert rtmm != null;

        long start = System.currentTimeMillis();
        Collection<RTMMServiceGroup> serviceGroups = rtmm.getServiceGroups();
        long stop = System.currentTimeMillis();
        assert serviceGroups != null;
        log.info("Loaded " + serviceGroups.size() + " service groups in " + (stop - start) + "ms.");

        if (USE_TEST_DATA) {
            log.debug("Service groups:");
            for (RTMMServiceGroup serviceGroup : serviceGroups) {
                log.debug(serviceGroup);
            }
            assert serviceGroups.size() == 2;
            RTMMServiceGroup sg1ServiceGroup = null;
            for (RTMMServiceGroup serviceGroup : serviceGroups) {
                if ("SG1".equals(serviceGroup.getName())) {
                    sg1ServiceGroup = serviceGroup;
                    break;
                }
            }
            assert sg1ServiceGroup != null;
            assert "SG1".equals(sg1ServiceGroup.getName());
            assert "NAGIOS".equals(sg1ServiceGroup.getAppTypeName());
            assert sg1ServiceGroup.getServiceIds().size() == 2;
        }

        if (!serviceGroups.isEmpty()) {
            RTMMServiceGroup serviceGroup = serviceGroups.iterator().next();
            start = System.currentTimeMillis();
            serviceGroup = rtmm.getServiceGroup(serviceGroup.getId());
            stop = System.currentTimeMillis();
            assert serviceGroup != null;
            log.info("Loaded service group in " + (stop - start) + "ms.");

            if (USE_TEST_DATA) {
                log.debug("Service group:");
                log.debug(serviceGroup);
            }
        }
    }

    /**
     * Test RTMM services custom group access.
     *
     * @throws Exception
     */
    public void testGetCustomGroups() throws Exception {
        RTMMServices rtmm = (RTMMServices) collage.getAPIObject(RTMMServices.SERVICE);
        assert rtmm != null;

        long start = System.currentTimeMillis();
        Collection<RTMMCustomGroup> customGroups = rtmm.getCustomGroups();
        long stop = System.currentTimeMillis();
        assert customGroups != null;
        log.info("Loaded " + customGroups.size() + " custom groups in " + (stop - start) + "ms.");

        if (USE_TEST_DATA) {
            log.debug("Custom groups:");
            for (RTMMCustomGroup customGroup : customGroups) {
                log.debug(customGroup);
            }
            assert customGroups.size() == 3;
            RTMMCustomGroup cg1CustomGroup = null;
            RTMMCustomGroup cg2CustomGroup = null;
            RTMMCustomGroup cg3CustomGroup = null;
            for (RTMMCustomGroup customGroup : customGroups) {
                if ("CG1".equals(customGroup.getName())) {
                    cg1CustomGroup = customGroup;
                } else if ("CG2".equals(customGroup.getName())) {
                    cg2CustomGroup = customGroup;
                } else if ("CG3".equals(customGroup.getName())) {
                    cg3CustomGroup = customGroup;
                }
            }
            assert cg1CustomGroup != null;
            assert "CG1".equals(cg1CustomGroup.getName());
            assert cg1CustomGroup.getIsRoot() == true;
            assert cg1CustomGroup.getHostGroupIds().isEmpty();
            assert cg1CustomGroup.getServiceGroupIds().isEmpty();
            assert cg1CustomGroup.getChildIds().size() == 2;
            assert cg2CustomGroup != null;
            assert "CG2".equals(cg2CustomGroup.getName());
            assert cg2CustomGroup.getIsRoot() == false;
            assert cg2CustomGroup.getHostGroupIds().size() == 2;
            assert cg2CustomGroup.getServiceGroupIds().isEmpty();
            assert cg2CustomGroup.getChildIds().isEmpty();
            assert cg3CustomGroup != null;
            assert "CG3".equals(cg3CustomGroup.getName());
            assert cg3CustomGroup.getIsRoot() == false;
            assert cg3CustomGroup.getHostGroupIds().isEmpty();
            assert cg3CustomGroup.getServiceGroupIds().size() == 1;
            assert cg3CustomGroup.getChildIds().isEmpty();
        }

        if (!customGroups.isEmpty()) {
            RTMMCustomGroup customGroup = customGroups.iterator().next();
            start = System.currentTimeMillis();
            customGroup = rtmm.getCustomGroup(customGroup.getId());
            stop = System.currentTimeMillis();
            assert customGroup != null;
            log.info("Loaded custom group in " + (stop - start) + "ms.");

            if (USE_TEST_DATA) {
                log.debug("Custom group:");
                log.debug(customGroup);
            }
        }
    }
}
