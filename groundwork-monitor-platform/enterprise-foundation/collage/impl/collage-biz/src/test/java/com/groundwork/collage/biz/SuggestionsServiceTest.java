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

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.biz.model.SuggestionEntityType;
import com.groundwork.collage.biz.model.Suggestions;
import com.groundwork.collage.model.ApplicationType;
import com.groundwork.collage.model.CheckType;
import com.groundwork.collage.model.Device;
import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.HostIdentity;
import com.groundwork.collage.model.MonitorStatus;
import com.groundwork.collage.model.ServiceStatus;
import com.groundwork.collage.model.StateType;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.device.DeviceService;
import org.groundwork.foundation.bs.host.HostService;
import org.groundwork.foundation.bs.hostidentity.HostIdentityService;
import org.groundwork.foundation.bs.status.StatusService;
import org.hibernate.FlushMode;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Suggestions
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class SuggestionsServiceTest extends AbstractTestBizBase {

    private static final Log log = LogFactory.getLog(SuggestionsServiceTest.class);

    private static final Set<SuggestionEntityType> ALL_ENTITY_TYPES =
            new HashSet<SuggestionEntityType>(Arrays.asList(SuggestionEntityType.values()));

    public SuggestionsServiceTest(String x) {
        super(x);
    }

    public static Test suite()
    {
        executeScript(false, "../common/testdata/monitor-data.sql");

        TestSuite suite = new TestSuite();
        suite.addTest(new SuggestionsServiceTest("testSuggestionsService"));
        suite.addTest(new SuggestionsServiceTest("testSuggestionsServicePerformance"));
        return suite;
    }

    /**
     * Test SuggestionsService.
     *
     * @throws Exception
     */
    public void testSuggestionsService() throws Exception {
        SuggestionsService suggestionsService = (SuggestionsService) collage.getAPIObject(SuggestionsService.SERVICE);
        assert suggestionsService != null;

        log.debug("Test suggestions queries...");
        Suggestions hostSuggestions = suggestionsService.querySuggestions(null, -1, SuggestionEntityType.HOST);
        assert hostSuggestions != null;
        assert hostSuggestions.getCount() > 0;
        assert hostSuggestions.getSuggestions() != null;
        assert !hostSuggestions.getSuggestions().isEmpty();
        hostSuggestions = suggestionsService.querySuggestions(null, 5, SuggestionEntityType.HOST);
        assert hostSuggestions != null;
        assert hostSuggestions.getCount() > 0;
        assert hostSuggestions.getSuggestions() != null;
        assert !hostSuggestions.getSuggestions().isEmpty();
        hostSuggestions = suggestionsService.querySuggestions("gwrk-*", -1, SuggestionEntityType.HOST);
        assert hostSuggestions != null;
        assert hostSuggestions.getCount() > 0;
        assert hostSuggestions.getSuggestions() != null;
        assert !hostSuggestions.getSuggestions().isEmpty();
        hostSuggestions = suggestionsService.querySuggestions("gwrk-*", 2, SuggestionEntityType.HOST);
        assert hostSuggestions != null;
        assert hostSuggestions.getCount() > 0;
        assert hostSuggestions.getSuggestions() != null;
        assert !hostSuggestions.getSuggestions().isEmpty();

        Suggestions serviceSuggestions = suggestionsService.querySuggestions(null, -1, SuggestionEntityType.SERVICE);
        assert serviceSuggestions != null;
        assert serviceSuggestions.getCount() > 0;
        assert serviceSuggestions.getSuggestions() != null;
        assert !serviceSuggestions.getSuggestions().isEmpty();
        serviceSuggestions = suggestionsService.querySuggestions(null, 3, SuggestionEntityType.SERVICE);
        assert serviceSuggestions != null;
        assert serviceSuggestions.getCount() > 0;
        assert serviceSuggestions.getSuggestions() != null;
        assert !serviceSuggestions.getSuggestions().isEmpty();
        serviceSuggestions = suggestionsService.querySuggestions("*local*", -1, SuggestionEntityType.SERVICE);
        assert serviceSuggestions != null;
        assert serviceSuggestions.getCount() > 0;
        assert serviceSuggestions.getSuggestions() != null;
        assert !serviceSuggestions.getSuggestions().isEmpty();
        serviceSuggestions = suggestionsService.querySuggestions("*local*", 1, SuggestionEntityType.SERVICE);
        assert serviceSuggestions != null;
        assert serviceSuggestions.getCount() > 0;
        assert serviceSuggestions.getSuggestions() != null;
        assert !serviceSuggestions.getSuggestions().isEmpty();

        Suggestions hostGroupSuggestions = suggestionsService.querySuggestions(null, -1, SuggestionEntityType.HOSTGROUP);
        assert hostGroupSuggestions != null;
        assert hostGroupSuggestions.getCount() > 0;
        assert hostGroupSuggestions.getSuggestions() != null;
        assert !hostGroupSuggestions.getSuggestions().isEmpty();
        hostGroupSuggestions = suggestionsService.querySuggestions(null, 3, SuggestionEntityType.HOSTGROUP);
        assert hostGroupSuggestions != null;
        assert hostGroupSuggestions.getCount() > 0;
        assert hostGroupSuggestions.getSuggestions() != null;
        assert !hostGroupSuggestions.getSuggestions().isEmpty();
        hostGroupSuggestions = suggestionsService.querySuggestions("Email_*", -1, SuggestionEntityType.HOSTGROUP);
        assert hostGroupSuggestions != null;
        assert hostGroupSuggestions.getCount() > 0;
        assert hostGroupSuggestions.getSuggestions() != null;
        assert !hostGroupSuggestions.getSuggestions().isEmpty();
        hostGroupSuggestions = suggestionsService.querySuggestions("Email_*", 1, SuggestionEntityType.HOSTGROUP);
        assert hostGroupSuggestions != null;
        assert hostGroupSuggestions.getCount() > 0;
        assert hostGroupSuggestions.getSuggestions() != null;
        assert !hostGroupSuggestions.getSuggestions().isEmpty();

        Suggestions serviceGroupSuggestions = suggestionsService.querySuggestions(null, -1, SuggestionEntityType.SERVICE_GROUP);
        assert serviceGroupSuggestions != null;
        assert serviceGroupSuggestions.getCount() > 0;
        assert serviceGroupSuggestions.getSuggestions() != null;
        assert !serviceGroupSuggestions.getSuggestions().isEmpty();
        serviceGroupSuggestions = suggestionsService.querySuggestions(null, 1, SuggestionEntityType.SERVICE_GROUP);
        assert serviceGroupSuggestions != null;
        assert serviceGroupSuggestions.getCount() > 0;
        assert serviceGroupSuggestions.getSuggestions() != null;
        assert !serviceGroupSuggestions.getSuggestions().isEmpty();
        serviceGroupSuggestions = suggestionsService.querySuggestions("*2", -1, SuggestionEntityType.SERVICE_GROUP);
        assert serviceGroupSuggestions != null;
        assert serviceGroupSuggestions.getCount() > 0;
        assert serviceGroupSuggestions.getSuggestions() != null;
        assert !serviceGroupSuggestions.getSuggestions().isEmpty();
        serviceGroupSuggestions = suggestionsService.querySuggestions("SG*", 1, SuggestionEntityType.SERVICE_GROUP);
        assert serviceGroupSuggestions != null;
        assert serviceGroupSuggestions.getCount() > 0;
        assert serviceGroupSuggestions.getSuggestions() != null;
        assert !serviceGroupSuggestions.getSuggestions().isEmpty();

        Suggestions customGroupSuggestions = suggestionsService.querySuggestions(null, -1, SuggestionEntityType.CUSTOM_GROUP);
        assert customGroupSuggestions != null;
        assert customGroupSuggestions.getCount() > 0;
        assert customGroupSuggestions.getSuggestions() != null;
        assert !customGroupSuggestions.getSuggestions().isEmpty();
        customGroupSuggestions = suggestionsService.querySuggestions(null, 1, SuggestionEntityType.CUSTOM_GROUP);
        assert customGroupSuggestions != null;
        assert customGroupSuggestions.getCount() > 0;
        assert customGroupSuggestions.getSuggestions() != null;
        assert !customGroupSuggestions.getSuggestions().isEmpty();
        customGroupSuggestions = suggestionsService.querySuggestions("*2", -1, SuggestionEntityType.CUSTOM_GROUP);
        assert customGroupSuggestions != null;
        assert customGroupSuggestions.getCount() > 0;
        assert customGroupSuggestions.getSuggestions() != null;
        assert !customGroupSuggestions.getSuggestions().isEmpty();
        customGroupSuggestions = suggestionsService.querySuggestions("CG*", 1, SuggestionEntityType.CUSTOM_GROUP);
        assert customGroupSuggestions != null;
        assert customGroupSuggestions.getCount() > 0;
        assert customGroupSuggestions.getSuggestions() != null;
        assert !customGroupSuggestions.getSuggestions().isEmpty();

        Suggestions allSuggestions = suggestionsService.querySuggestions(null, -1, ALL_ENTITY_TYPES);
        assert allSuggestions != null;
        assert allSuggestions.getCount() > 0;
        assert allSuggestions.getSuggestions() != null;
        assert !allSuggestions.getSuggestions().isEmpty();
        allSuggestions = suggestionsService.querySuggestions(null, 5, ALL_ENTITY_TYPES);
        assert allSuggestions != null;
        assert allSuggestions.getCount() > 0;
        assert allSuggestions.getSuggestions() != null;
        assert !allSuggestions.getSuggestions().isEmpty();
        allSuggestions = suggestionsService.querySuggestions("*gw*", -1, ALL_ENTITY_TYPES);
        assert allSuggestions != null;
        assert allSuggestions.getCount() > 0;
        assert allSuggestions.getSuggestions() != null;
        assert !allSuggestions.getSuggestions().isEmpty();
        allSuggestions = suggestionsService.querySuggestions("*gw*", 2, ALL_ENTITY_TYPES);
        assert allSuggestions != null;
        assert allSuggestions.getCount() > 0;
        assert allSuggestions.getSuggestions() != null;
        assert !allSuggestions.getSuggestions().isEmpty();

        // create test hosts and services
        log.debug("Creating test hosts and services...");
        Set<String> deviceIdentifications = new HashSet<String>();
        Set<String> hostNames = new HashSet<String>();
        createTestHostsAndServices(2, 5, deviceIdentifications, hostNames);

        log.debug("Test suggestions hosts/services queries...");
        List<String> hostServices = suggestionsService.hostServiceDescriptions("bulk-biz-host-0");
        assert hostServices != null;
        assert hostServices.size() == 5;
        assert hostServices.contains("bulk-biz-service-0");
        hostServices = suggestionsService.hostServiceDescriptions("bulk-biz-host-0-alias");
        assert hostServices != null;
        assert hostServices.size() == 5;
        assert hostServices.contains("bulk-biz-service-0");
        Map<String,String> serviceHosts = suggestionsService.serviceHostNames("bulk-biz-service-0");
        assert serviceHosts != null;
        assert serviceHosts.size() == 4;
        assert serviceHosts.containsKey("bulk-biz-host-0");
        assert serviceHosts.get("bulk-biz-host-0").equals("bulk-biz-host-0");
        assert serviceHosts.containsKey("bulk-biz-host-0-alias");
        assert serviceHosts.get("bulk-biz-host-0-alias").equals("bulk-biz-host-0");

        // cleanup
        log.debug("Cleanup test hosts and services...");
        cleanupTestHostsAndServices(deviceIdentifications, hostNames);
    }

    /**
     * Test SuggestionsService performance.
     *
     * @throws Exception
     */
    public void testSuggestionsServicePerformance() throws Exception {
        // get services
        SuggestionsService suggestionsService = (SuggestionsService) collage.getAPIObject(SuggestionsService.SERVICE);
        assert suggestionsService != null;

        // create test hosts and services
        log.debug("Creating test hosts and services...");
        Set<String> deviceIdentifications = new HashSet<String>();
        Set<String> hostNames = new HashSet<String>();
        createTestHostsAndServices(5000, 10, deviceIdentifications, hostNames);

        // force db vacuum/full/analyze
        log.debug("DB vacuum/analyze...");
        dbVacuumFullAnalyze();

        // test suggestions queries
        log.debug("Test suggestions queries performance...");
        long start = System.currentTimeMillis();
        Suggestions hostSuggestions = suggestionsService.querySuggestions(null, -1, SuggestionEntityType.HOST);
        assert hostSuggestions != null;
        assert hostSuggestions.getCount() >= 10000;
        assert hostSuggestions.getSuggestions() != null;
        assert hostSuggestions.getSuggestions().size() >= 1000;
        log.debug("All host suggestions query time: "+(System.currentTimeMillis()-start)+"ms");

        start = System.currentTimeMillis();
        hostSuggestions = suggestionsService.querySuggestions(null, 5, SuggestionEntityType.HOST);
        assert hostSuggestions != null;
        assert hostSuggestions.getCount() >= 10000;
        assert hostSuggestions.getSuggestions() != null;
        assert hostSuggestions.getSuggestions().size() == 5;
        log.debug("Limit 5 host suggestions query time: "+(System.currentTimeMillis()-start)+"ms");

        start = System.currentTimeMillis();
        hostSuggestions = suggestionsService.querySuggestions("bulk-biz-host-3???", -1, SuggestionEntityType.HOST);
        assert hostSuggestions != null;
        assert hostSuggestions.getCount() == 1000;
        assert hostSuggestions.getSuggestions() != null;
        assert hostSuggestions.getSuggestions().size() == 1000;
        log.debug("'bulk-biz-host-3???' host suggestions query time: "+(System.currentTimeMillis()-start)+"ms");

        start = System.currentTimeMillis();
        hostSuggestions = suggestionsService.querySuggestions("bulk-biz-host-3???", 5, SuggestionEntityType.HOST);
        assert hostSuggestions != null;
        assert hostSuggestions.getCount() == 1000;
        assert hostSuggestions.getSuggestions() != null;
        assert hostSuggestions.getSuggestions().size() == 5;
        log.debug("Limit 5 'bulk-biz-host-3???' host suggestions query time: "+(System.currentTimeMillis()-start)+"ms");

        start = System.currentTimeMillis();
        Suggestions serviceSuggestions = suggestionsService.querySuggestions(null, -1, SuggestionEntityType.SERVICE);
        assert serviceSuggestions != null;
        assert serviceSuggestions.getCount() >= 10;
        assert serviceSuggestions.getSuggestions() != null;
        assert serviceSuggestions.getSuggestions().size() > 10;
        log.debug("All service suggestions query time: "+(System.currentTimeMillis()-start)+"ms");

        start = System.currentTimeMillis();
        serviceSuggestions = suggestionsService.querySuggestions(null, 5, SuggestionEntityType.SERVICE);
        assert serviceSuggestions != null;
        assert serviceSuggestions.getCount() >= 10;
        assert serviceSuggestions.getSuggestions() != null;
        assert serviceSuggestions.getSuggestions().size() == 5;
        log.debug("Limit 5 service suggestions query time: "+(System.currentTimeMillis()-start)+"ms");

        start = System.currentTimeMillis();
        serviceSuggestions = suggestionsService.querySuggestions("bulk-biz-service-*", -1, SuggestionEntityType.SERVICE);
        assert serviceSuggestions != null;
        assert serviceSuggestions.getCount() == 10;
        assert serviceSuggestions.getSuggestions() != null;
        assert serviceSuggestions.getSuggestions().size() == 10;
        log.debug("'bulk-biz-service-*' service suggestions query time: "+(System.currentTimeMillis()-start)+"ms");

        start = System.currentTimeMillis();
        serviceSuggestions = suggestionsService.querySuggestions("bulk-biz-service-*", 5, SuggestionEntityType.SERVICE);
        assert serviceSuggestions != null;
        assert serviceSuggestions.getCount() == 10;
        assert serviceSuggestions.getSuggestions() != null;
        assert serviceSuggestions.getSuggestions().size() == 5;
        log.debug("Limit 5 'bulk-biz-service-*' service suggestions query time: "+(System.currentTimeMillis()-start)+"ms");

        // cleanup
        log.debug("Cleanup test hosts and services...");
        cleanupTestHostsAndServices(deviceIdentifications, hostNames);
    }

    /**
     * Create test hosts and services.
     *
     * @param numHosts number of hosts to create
     * @param numServicesPerHost number of services per host
     * @param deviceIdentifications created device identifications
     * @param hostNames created host names
     */
    private void createTestHostsAndServices(int numHosts, int numServicesPerHost, Set<String> deviceIdentifications, Set<String> hostNames) {
        DeviceService deviceService = collage.getDeviceService();
        assert deviceService != null;
        HostService hostService = collage.getHostService();
        assert hostService != null;
        StatusService statusService = collage.getStatusService();
        assert statusService != null;
        HostIdentityService hostIdentityService = collage.getHostIdentityService();
        assert hostIdentityService != null;

        beginTransaction();
        getSession().setFlushMode(FlushMode.COMMIT);
        ApplicationType nagios = metadataService.getApplicationTypeByName("NAGIOS");
        MonitorStatus pending = metadataService.getMonitorStatusByName("PENDING");
        StateType unknown = metadataService.getStateTypeByName("UNKNOWN");
        CheckType active = metadataService.getCheckTypeByName("ACTIVE");
        Map<String,Device> devices = new HashMap<String,Device>();
        for (int h = 0; (h < numHosts); h++) {
            String deviceIdentification = "bulk-biz-device-" + (h % (numHosts / 2));
            Device device = devices.get(deviceIdentification);
            if (device == null) {
                device = deviceService.createDevice(deviceIdentification, deviceIdentification);
                device.setApplicationType(nagios);
                deviceService.saveDevice(device);
                devices.put(deviceIdentification, device);
                deviceIdentifications.add(deviceIdentification);
            }
            String hostName = "bulk-biz-host-" + h;
            hostNames.add(hostName);
            Host host = hostService.createHost(hostName, device);
            host.setApplicationType(nagios);
            host.setAgentId("suggestions");
            host.setLastMonitorStatus("PENDING");
            for (int s = 0; (s < numServicesPerHost); s++) {
                ServiceStatus service = statusService.createService();
                service.setHost(host);
                service.setServiceDescription("bulk-biz-service-" + s);
                service.setApplicationType(nagios);
                service.setAgentId("suggestions");
                service.setMonitorStatus(pending);
                service.setLastHardState(pending);
                service.setStateType(unknown);
                service.setCheckType(active);
                service.setLastMonitorStatus("PENDING");
                statusService.saveService(service);
            }
            HostIdentity hostIdentity = hostIdentityService.createHostIdentity(host, Arrays.asList(new String[]{hostName + "-alias"}));
            hostIdentityService.saveHostIdentity(hostIdentity);
        }
        commitTransaction();
    }

    /**
     * Perform DB vacuum/full/analyze to prevent from interfering with
     * performance measurement.
     *
     * @throws Exception
     */
    private void dbVacuumFullAnalyze() throws Exception {
        SessionFactory sessionFactory = (SessionFactory) CollageFactory.getInstance().getAPIObject(CollageFactory.HIBERNATE_SESSION_FACTORY);
        StatelessSession session = null;
        Statement statement = null;
        try {
            session = sessionFactory.openStatelessSession();
            statement = session.connection().createStatement();
            statement.execute("vacuum full analyze");
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (session != null) {
                session.close();
            }
        }
    }

    /**
     * Cleanup test hosts and services.
     *
     * @param deviceIdentifications created device identifications
     * @param hostNames created host names
     */
    private void cleanupTestHostsAndServices(Set<String> deviceIdentifications, Set<String> hostNames) {
        DeviceService deviceService = collage.getDeviceService();
        assert deviceService != null;
        HostIdentityService hostIdentityService = collage.getHostIdentityService();
        assert hostIdentityService != null;

        beginTransaction();
        getSession().setFlushMode(FlushMode.COMMIT);
        // get host identities and devices
        List<HostIdentity> hostIdentities = new ArrayList<>();
        for (String hostName : hostNames) {
            hostIdentities.add(hostIdentityService.getHostIdentityByIdOrHostName(hostName));
        }
        List<Device> devices = new ArrayList<>();
        for (String deviceIdentification : deviceIdentifications) {
            devices.add(deviceService.getDeviceByIdentification(deviceIdentification));
        }
        // delete host identities and devices
        for (HostIdentity hostIdentity : hostIdentities) {
            hostIdentity.setHost(null);
        }
        hostIdentityService.saveHostIdentities(hostIdentities);
        hostIdentityService.deleteHostIdentities(hostIdentities);
        deviceService.deleteDevices(devices);
        commitTransaction();
    }
}
