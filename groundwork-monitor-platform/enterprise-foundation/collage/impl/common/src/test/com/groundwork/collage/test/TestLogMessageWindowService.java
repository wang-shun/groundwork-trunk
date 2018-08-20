package com.groundwork.collage.test;

import com.groundwork.collage.model.*;
import com.groundwork.collage.model.impl.StateTransition;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.device.DeviceService;
import org.groundwork.foundation.bs.host.HostService;
import org.groundwork.foundation.bs.logmessage.LogMessageService;
import org.groundwork.foundation.bs.logmessage.LogMessageWindowServiceImpl;
import org.groundwork.foundation.bs.metadata.MetadataService;
import org.groundwork.foundation.bs.status.StatusService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * TestLogMessageWindowService
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class TestLogMessageWindowService extends AbstractTestCaseWithTransactionSupport {

    private static Log log = LogFactory.getLog(TestLogMessageWindowService.class);

    private static final DateFormat DATE_FORMAT_US = new SimpleDateFormat("MM/dd/yyyy H:mm:ss");

    private static final long SEC_MILLIS = 1000L;
    private static final long HOUR_MILLIS = 3600000L;
    private static final long DAY_MILLIS = 86400000L;

    private LogMessageService logMessageService;
    private DeviceService deviceService;
    private HostService hostService;
    private StatusService statusService;
    private MetadataService metadataService;
    private LogMessageWindowServiceImpl logMessageWindowService;

    /**
     * Create test.
     *
     * @param name
     */
    public TestLogMessageWindowService(String name) {
        super(name);
    }

    /**
     * Declare tests to be run.
     *
     * @return test suite
     */
    public static Test suite() {
        // initialize test database once per suite
        executeScript(false, "testdata/monitor-data.sql");

        // run all tests
        return new TestSuite(TestLogMessageWindowService.class);
    }

    /**
     * Setup test.
     *
     * @throws Exception
     */
    public void setUp() throws Exception {
        // setup test
        super.setUp();

        // initialize services
        logMessageService = collage.getLogMessageService();
        deviceService = collage.getDeviceService();
        hostService = collage.getHostService();
        statusService = collage.getStatusService();
        metadataService = collage.getMetadataService();
        logMessageWindowService = (LogMessageWindowServiceImpl)collage.getLogMessageWindowService();

        // shutdown log message window service service and enable before tests
        logMessageWindowService.uninitialize();
        logMessageWindowService.setWindowEnabled(true);
    }

    public void testLogMessageWindowService() throws Exception {
        // get test hosts and services
        Host nagiosHost = hostService.getHostByHostName("nagios");
        ServiceStatus nagiosLocalDiskService = statusService.getServiceByDescription("local_disk", "nagios");
        ServiceStatus nagiosLocalProcsService = statusService.getServiceByDescription("local_procs", "nagios");
        ServiceStatus nagiosLocalUsersService = statusService.getServiceByDescription("local_users", "nagios");
        ServiceStatus nagiosNetworkUsersService = statusService.getServiceByDescription("network_users", "nagios");
        Host exchangeHost = hostService.getHostByHostName("exchange");
        ServiceStatus exchangeNetworkUsersService = statusService.getServiceByDescription("network_users", "exchange");
        Host appSvrTomcatHost = hostService.getHostByHostName("app-svr-tomcat");
        ServiceStatus appSvrTomcatNetworkUsersService = statusService.getServiceByDescription("network_users",
                "app-svr-tomcat");

        // get monitor status
        MonitorStatus pending = metadataService.getMonitorStatusByName("PENDING");
        MonitorStatus hostUp = metadataService.getMonitorStatusByName("UP");
        MonitorStatus hostDown = metadataService.getMonitorStatusByName("DOWN");
        MonitorStatus hostScheduledDown = metadataService.getMonitorStatusByName("SCHEDULED DOWN");
        MonitorStatus hostUnscheduledDown = metadataService.getMonitorStatusByName("UNSCHEDULED DOWN");
        MonitorStatus serviceOk = metadataService.getMonitorStatusByName("OK");
        MonitorStatus serviceCritical = metadataService.getMonitorStatusByName("CRITICAL");
        MonitorStatus serviceScheduledCritical = metadataService.getMonitorStatusByName("SCHEDULED CRITICAL");
        MonitorStatus serviceUnscheduledCritical = metadataService.getMonitorStatusByName("UNSCHEDULED CRITICAL");
        MonitorStatus startDowntime = metadataService.getMonitorStatusByName("START DOWNTIME");
        MonitorStatus endDowntime = metadataService.getMonitorStatusByName("END DOWNTIME");

        // populate log messages
        long now = System.currentTimeMillis();
        long epoch = now-2*DAY_MILLIS-HOUR_MILLIS;
        long start = now-2*DAY_MILLIS+HOUR_MILLIS;
        long window = now-DAY_MILLIS+HOUR_MILLIS;
        long startEvent = now-4*HOUR_MILLIS;
        long event1 = now-3*HOUR_MILLIS;
        long event2 = now-2*HOUR_MILLIS;
        long endEvent = now-HOUR_MILLIS;
        createLogMessage(nagiosHost, null, pending, epoch);
        createLogMessage(nagiosHost, nagiosLocalDiskService, pending, epoch);
        createLogMessage(nagiosHost, nagiosLocalProcsService, pending, epoch);
        createLogMessage(nagiosHost, nagiosLocalUsersService, pending, epoch);
        createLogMessage(nagiosHost, nagiosNetworkUsersService, pending, epoch);
        createLogMessage(exchangeHost, null, pending, epoch);
        createLogMessage(exchangeHost, exchangeNetworkUsersService, pending, epoch);
        createLogMessage(appSvrTomcatHost, null, pending, epoch);
        createLogMessage(appSvrTomcatHost, appSvrTomcatNetworkUsersService, pending, epoch);
        createLogMessage(nagiosHost, null, hostUp, start);
        createLogMessage(nagiosHost, nagiosLocalDiskService, serviceOk, start);
        createLogMessage(nagiosHost, nagiosLocalProcsService, serviceOk, start);
        createLogMessage(nagiosHost, nagiosLocalProcsService, serviceCritical, event1);
        createLogMessage(nagiosHost, nagiosLocalProcsService, serviceOk, endEvent);
        createLogMessage(nagiosHost, nagiosLocalUsersService, serviceOk, start);
        createLogMessage(exchangeHost, null, hostUp, start);
        createLogMessage(exchangeHost, null, hostDown, window);
        createLogMessage(exchangeHost, exchangeNetworkUsersService, serviceOk, start);
        createLogMessage(exchangeHost, exchangeNetworkUsersService, serviceCritical, window);
        createLogMessage(appSvrTomcatHost, null, hostUp, start);
        createLogMessage(appSvrTomcatHost, null, startDowntime, startEvent);
        createLogMessage(appSvrTomcatHost, null, hostDown, event1);
        createLogMessage(appSvrTomcatHost, null, hostUp, event2);
        createLogMessage(appSvrTomcatHost, null, endDowntime, endEvent);
        createLogMessage(appSvrTomcatHost, appSvrTomcatNetworkUsersService, serviceOk, start);
        createLogMessage(appSvrTomcatHost, appSvrTomcatNetworkUsersService, serviceCritical, startEvent);
        createLogMessage(appSvrTomcatHost, appSvrTomcatNetworkUsersService, startDowntime, event1);
        createLogMessage(appSvrTomcatHost, appSvrTomcatNetworkUsersService, endDowntime, event2);
        createLogMessage(appSvrTomcatHost, appSvrTomcatNetworkUsersService, serviceOk, endEvent);

        // restart log message window service
        assertFalse(logMessageWindowService.isWindowInitialized());
        logMessageWindowService.initialize();
        long waitStart = System.currentTimeMillis();
        do {
            Thread.sleep(100);
        } while (!logMessageWindowService.isWindowInitialized() && System.currentTimeMillis()-waitStart < 5*SEC_MILLIS);
        assertTrue(logMessageWindowService.isWindowInitialized());

        // validate window operation
        String startDate = toDateString(now-DAY_MILLIS);
        String endDate = toDateString(now);
        List<StateTransition> nagiosHostStateTransitions =
                logMessageWindowService.getHostStateTransitions(nagiosHost.getHostName(), startDate, endDate);
        assertNotNull(nagiosHostStateTransitions);
        assertEquals(1, nagiosHostStateTransitions.size());
        assertEquals(nagiosHost, null, hostUp, start, nagiosHostStateTransitions.get(0));
        assertEquals(logMessageService.getHostStateTransitions(nagiosHost.getHostName(), startDate, endDate, false),
                nagiosHostStateTransitions);

        List<StateTransition> nagiosLocalDiskServiceStateTransitions =
                logMessageWindowService.getServiceStateTransitions(nagiosHost.getHostName(),
                        nagiosLocalDiskService.getServiceDescription(), startDate, endDate);
        assertNotNull(nagiosLocalDiskServiceStateTransitions);
        assertEquals(1, nagiosLocalDiskServiceStateTransitions.size());
        assertEquals(nagiosHost, nagiosLocalDiskService, serviceOk, start,
                nagiosLocalDiskServiceStateTransitions.get(0));
        assertEquals(logMessageService.getServiceStateTransitions(nagiosHost.getHostName(),
                nagiosLocalDiskService.getServiceDescription(), startDate, endDate, false),
                nagiosLocalDiskServiceStateTransitions);

        List<StateTransition> nagiosLocalProcsServiceStateTransitions =
                logMessageWindowService.getServiceStateTransitions(nagiosHost.getHostName(),
                        nagiosLocalProcsService.getServiceDescription(), startDate, endDate);
        assertNotNull(nagiosLocalProcsServiceStateTransitions);
        assertEquals(2, nagiosLocalProcsServiceStateTransitions.size());
        assertEquals(nagiosHost, nagiosLocalProcsService, serviceOk, start, serviceUnscheduledCritical, event1,
                nagiosLocalProcsServiceStateTransitions.get(0));
        assertEquals(nagiosHost, nagiosLocalProcsService, serviceUnscheduledCritical, event1, serviceOk, endEvent,
                nagiosLocalProcsServiceStateTransitions.get(1));
        assertEquals(logMessageService.getServiceStateTransitions(nagiosHost.getHostName(),
                nagiosLocalProcsService.getServiceDescription(), startDate, endDate, false),
                nagiosLocalProcsServiceStateTransitions);

        List<StateTransition> nagiosLocalUsersServiceStateTransitions =
                logMessageWindowService.getServiceStateTransitions(nagiosHost.getHostName(),
                        nagiosLocalUsersService.getServiceDescription(), startDate, endDate);
        assertNotNull(nagiosLocalUsersServiceStateTransitions);
        assertEquals(1, nagiosLocalUsersServiceStateTransitions.size());
        assertEquals(nagiosHost, nagiosLocalUsersService, serviceOk, start,
                nagiosLocalUsersServiceStateTransitions.get(0));
        assertEquals(logMessageService.getServiceStateTransitions(nagiosHost.getHostName(),
                nagiosLocalUsersService.getServiceDescription(), startDate, endDate, false),
                nagiosLocalUsersServiceStateTransitions);

        List<StateTransition> nagiosNetworkUsersServiceStateTransitions =
                logMessageWindowService.getServiceStateTransitions(nagiosHost.getHostName(),
                        nagiosNetworkUsersService.getServiceDescription(), startDate, endDate);
        assertNotNull(nagiosNetworkUsersServiceStateTransitions);
        assertEquals(1, nagiosNetworkUsersServiceStateTransitions.size());
        assertEquals(nagiosHost, nagiosNetworkUsersService, pending, epoch,
                nagiosNetworkUsersServiceStateTransitions.get(0));
        assertEquals(logMessageService.getServiceStateTransitions(nagiosHost.getHostName(),
                nagiosNetworkUsersService.getServiceDescription(), startDate, endDate, false),
                nagiosNetworkUsersServiceStateTransitions);

        List<StateTransition> exchangeHostStateTransitions =
                logMessageWindowService.getHostStateTransitions(exchangeHost.getHostName(), startDate, endDate);
        assertNotNull(exchangeHostStateTransitions);
        assertEquals(1, exchangeHostStateTransitions.size());
        assertEquals(exchangeHost, null, hostUp, start, hostUnscheduledDown, window,
                exchangeHostStateTransitions.get(0));
        assertEquals(logMessageService.getHostStateTransitions(exchangeHost.getHostName(), startDate, endDate, false),
                exchangeHostStateTransitions);

        List<StateTransition> exchangeNetworkUsersServiceStateTransitions =
                logMessageWindowService.getServiceStateTransitions(exchangeHost.getHostName(),
                        exchangeNetworkUsersService.getServiceDescription(), startDate, endDate);
        assertNotNull(exchangeNetworkUsersServiceStateTransitions);
        assertEquals(1, exchangeNetworkUsersServiceStateTransitions.size());
        assertEquals(exchangeHost, exchangeNetworkUsersService, serviceOk, start, serviceUnscheduledCritical, window,
                exchangeNetworkUsersServiceStateTransitions.get(0));
        assertEquals(logMessageService.getServiceStateTransitions(exchangeHost.getHostName(),
                exchangeNetworkUsersService.getServiceDescription(), startDate, endDate, false),
                exchangeNetworkUsersServiceStateTransitions);

        List<StateTransition> appSvrTomcatHostStateTransitions =
                logMessageWindowService.getHostStateTransitions(appSvrTomcatHost.getHostName(), startDate, endDate);
        assertNotNull(appSvrTomcatHostStateTransitions);
        assertEquals(2, appSvrTomcatHostStateTransitions.size());
        assertEquals(appSvrTomcatHost, null, hostUp, start, hostScheduledDown, event1,
                appSvrTomcatHostStateTransitions.get(0));
        assertEquals(appSvrTomcatHost, null, hostScheduledDown, event1, hostUp, event2,
                appSvrTomcatHostStateTransitions.get(1));
        assertNotSame(logMessageService.getHostStateTransitions(appSvrTomcatHost.getHostName(), startDate, endDate, false),
                appSvrTomcatHostStateTransitions);

        List<StateTransition> appSvrTomcatNetworkUsersServiceStateTransitions =
                logMessageWindowService.getServiceStateTransitions(appSvrTomcatHost.getHostName(),
                        appSvrTomcatNetworkUsersService.getServiceDescription(), startDate, endDate);
        assertNotNull(appSvrTomcatNetworkUsersServiceStateTransitions);
        assertEquals(4, appSvrTomcatNetworkUsersServiceStateTransitions.size());
        assertEquals(appSvrTomcatHost, appSvrTomcatNetworkUsersService, serviceOk, start, serviceUnscheduledCritical,
                startEvent, appSvrTomcatNetworkUsersServiceStateTransitions.get(0));
        assertEquals(appSvrTomcatHost, appSvrTomcatNetworkUsersService, serviceUnscheduledCritical, startEvent,
                serviceScheduledCritical, event1, appSvrTomcatNetworkUsersServiceStateTransitions.get(1));
        assertEquals(appSvrTomcatHost, appSvrTomcatNetworkUsersService, serviceScheduledCritical, event1,
                serviceUnscheduledCritical, event2, appSvrTomcatNetworkUsersServiceStateTransitions.get(2));
        assertEquals(appSvrTomcatHost, appSvrTomcatNetworkUsersService, serviceUnscheduledCritical, event2,
                serviceOk, endEvent, appSvrTomcatNetworkUsersServiceStateTransitions.get(3));
        assertNotSame(logMessageService.getServiceStateTransitions(appSvrTomcatHost.getHostName(),
                appSvrTomcatNetworkUsersService.getServiceDescription(), startDate, endDate, false),
                appSvrTomcatNetworkUsersServiceStateTransitions);

        try {
            // create test host and service after current window
            Thread.sleep(500L);
            Device testDevice = deviceService.createDevice("test-device", "test-device");
            deviceService.saveDevice(testDevice);
            Host testHost = hostService.createHost("test-host", testDevice);
            hostService.saveHost(testHost);
            HostStatus testHostStatus = hostService.createHostStatus("NAGIOS", testHost);
            testHostStatus.setHostMonitorStatus(pending);
            hostService.saveHostStatus(testHostStatus);
            ServiceStatus testService = statusService.createService("test-service", "NAGIOS", testHost);
            testService.setMonitorStatus(pending);
            testService.setLastHardState(pending);
            testService.setStateType(metadataService.getStateTypeByName("UNKNOWN"));
            testService.setCheckType(metadataService.getCheckTypeByName("ACTIVE"));
            statusService.saveService(testService);
            long create = System.currentTimeMillis();
            createLogMessage(testHost, null, pending, create);
            createLogMessage(testHost, testService, pending, create);

            // add log messages after current window
            Thread.sleep(500L);
            long nextEvent = System.currentTimeMillis();
            createLogMessage(exchangeHost, null, hostUp, nextEvent);
            createLogMessage(exchangeHost, exchangeNetworkUsersService, serviceOk, nextEvent);
            createLogMessage(nagiosHost, nagiosNetworkUsersService, serviceOk, nextEvent);
            createLogMessage(testHost, null, hostUp, nextEvent);
            createLogMessage(testHost, testService, serviceOk, nextEvent);

            // wait for window update
            Thread.sleep(4500L);

            // validate window update operation
            now = System.currentTimeMillis();
            startDate = toDateString(now - DAY_MILLIS);
            endDate = toDateString(now);
            nagiosNetworkUsersServiceStateTransitions =
                    logMessageWindowService.getServiceStateTransitions(nagiosHost.getHostName(),
                            nagiosNetworkUsersService.getServiceDescription(), startDate, endDate);
            assertNotNull(nagiosNetworkUsersServiceStateTransitions);
            assertEquals(1, nagiosNetworkUsersServiceStateTransitions.size());
            assertEquals(nagiosHost, nagiosNetworkUsersService, pending, epoch, serviceOk, nextEvent,
                    nagiosNetworkUsersServiceStateTransitions.get(0));
            assertEquals(logMessageService.getServiceStateTransitions(nagiosHost.getHostName(),
                    nagiosNetworkUsersService.getServiceDescription(), startDate, endDate, false),
                    nagiosNetworkUsersServiceStateTransitions);

            exchangeHostStateTransitions =
                    logMessageWindowService.getHostStateTransitions(exchangeHost.getHostName(), startDate, endDate);
            assertNotNull(exchangeHostStateTransitions);
            assertEquals(2, exchangeHostStateTransitions.size());
            assertEquals(exchangeHost, null, hostUp, start, hostUnscheduledDown, window,
                    exchangeHostStateTransitions.get(0));
            assertEquals(exchangeHost, null, hostUnscheduledDown, window, hostUp, nextEvent,
                    exchangeHostStateTransitions.get(1));
            assertEquals(logMessageService.getHostStateTransitions(exchangeHost.getHostName(), startDate, endDate, false),
                    exchangeHostStateTransitions);

            exchangeNetworkUsersServiceStateTransitions =
                    logMessageWindowService.getServiceStateTransitions(exchangeHost.getHostName(),
                            exchangeNetworkUsersService.getServiceDescription(), startDate, endDate);
            assertNotNull(exchangeNetworkUsersServiceStateTransitions);
            assertEquals(2, exchangeNetworkUsersServiceStateTransitions.size());
            assertEquals(exchangeHost, exchangeNetworkUsersService, serviceOk, start, serviceUnscheduledCritical, window,
                    exchangeNetworkUsersServiceStateTransitions.get(0));
            assertEquals(exchangeHost, exchangeNetworkUsersService, serviceUnscheduledCritical, window, serviceOk, nextEvent,
                    exchangeNetworkUsersServiceStateTransitions.get(1));
            assertEquals(logMessageService.getServiceStateTransitions(exchangeHost.getHostName(),
                    exchangeNetworkUsersService.getServiceDescription(), startDate, endDate, false),
                    exchangeNetworkUsersServiceStateTransitions);

            List<StateTransition> testHostStateTransitions =
                    logMessageWindowService.getHostStateTransitions(testHost.getHostName(), startDate, endDate);
            assertNotNull(testHostStateTransitions);
            assertEquals(1, testHostStateTransitions.size());
            assertEquals(testHost, null, pending, create, hostUp, nextEvent,
                    testHostStateTransitions.get(0));
            assertEquals(logMessageService.getHostStateTransitions(testHost.getHostName(), startDate, endDate, false),
                    testHostStateTransitions);

            List<StateTransition> testServiceStateTransitions =
                    logMessageWindowService.getServiceStateTransitions(testHost.getHostName(),
                            testService.getServiceDescription(), startDate, endDate);
            assertNotNull(testServiceStateTransitions);
            assertEquals(1, testServiceStateTransitions.size());
            assertEquals(testHost, testService, pending, create, serviceOk, nextEvent,
                    testServiceStateTransitions.get(0));
            assertEquals(logMessageService.getServiceStateTransitions(testHost.getHostName(),
                    testService.getServiceDescription(), startDate, endDate, false),
                    testServiceStateTransitions);
        } finally {
            // delete test device, host, service, and log messages
            deviceService.deleteDeviceByIdentification("test-device");
        }
    }

    private void createLogMessage(Host host, ServiceStatus service, MonitorStatus monitorStatus,
                                  long firstInsertMillis) {
        LogMessage logMessage = logMessageService.createLogMessage();
        logMessage.setHostStatus(host.getHostStatus());
        logMessage.setDevice(host.getDevice());
        logMessage.setServiceStatus(service);
        logMessage.setFirstInsertDate(new Date(firstInsertMillis));
        logMessage.setLastInsertDate(new Date(firstInsertMillis));
        logMessage.setMonitorStatus(monitorStatus);
        logMessage.setApplicationType(metadataService.getApplicationTypeByName("NAGIOS"));
        logMessage.setSeverity(metadataService.getSeverityByName("ACKNOWLEDGEMENT (UNKNOWN)"));
        logMessage.setApplicationSeverity(logMessage.getSeverity());
        logMessage.setTextMessage(monitorStatus.getName());
        logMessage.setReportDate(logMessage.getFirstInsertDate());
        logMessage.setPriority(metadataService.getPriorityByName("5"));
        logMessage.setTypeRule(metadataService.getTypeRuleByName("UNDEFINED"));
        logMessage.setComponent(metadataService.getComponentByName("UNDEFINED"));
        logMessage.setOperationStatus(metadataService.getOperationStatusByName("OPEN"));
        logMessageService.saveLogMessage(logMessage);
    }

    private static String toDateString(long dateMillis) {
        return DATE_FORMAT_US.format(new Date(dateMillis));
    }

    private static void assertEquals(Host host, ServiceStatus service, MonitorStatus fromState, long fromTime,
                                     MonitorStatus toState, long toTime, StateTransition stateTransition) {
        assertEquals(host.getHostName(), stateTransition.getHostName());
        if (service != null) {
            assertEquals(service.getServiceDescription(), stateTransition.getServiceDescription());
        } else {
            assertNull(stateTransition.getServiceDescription());
        }
        assertEquals(fromState.getName(), stateTransition.getFromStatus().getName());
        assertEquals(fromTime, stateTransition.getFromTransitionDate().getTime());
        assertEquals(toState.getName(), stateTransition.getToStatus().getName());
        assertEquals(toTime, stateTransition.getToTransitionDate().getTime());
        assertEquals(toTime-fromTime, stateTransition.getDurationInState().longValue());
    }

    private static void assertEquals(Host host, ServiceStatus service, MonitorStatus toState, long toTime,
                                     StateTransition stateTransition) {
        assertEquals(host.getHostName(), stateTransition.getHostName());
        if (service != null) {
            assertEquals(service.getServiceDescription(), stateTransition.getServiceDescription());
        } else {
            assertNull(stateTransition.getServiceDescription());
        }
        assertNull(stateTransition.getFromStatus());
        assertNull(stateTransition.getFromTransitionDate());
        assertEquals(toState.getName(), stateTransition.getToStatus().getName());
        assertEquals(toTime, stateTransition.getToTransitionDate().getTime());
        assertNull(stateTransition.getDurationInState());
    }

    private static void assertEquals(List<StateTransition> expected, List<StateTransition> check) {
        if (expected == null) {
            assertNull(check);
        } else {
            assertNotNull(check);
            assertEquals(expected.size(), check.size());
            for (int i = 0, limit = expected.size(); i < limit; i++) {
                assertEquals(expected.get(i), check.get(i));
            }
        }
    }

    private static void assertEquals(StateTransition expected, StateTransition check) {
        if (expected == null) {
            assertNull(check);
        } else {
            assertNotNull(check);
            assertEquals(expected.getHostName(), check.getHostName());
            if (expected.getServiceDescription() != null) {
                assertNotNull(check.getServiceDescription());
                assertEquals(expected.getServiceDescription(), check.getServiceDescription());
            } else {
                assertNull(check.getServiceDescription());
            }
            if (expected.getFromStatus() != null) {
                assertNotNull(check.getFromStatus());
                assertEqualsMonitorStatusName(expected.getFromStatus().getName(), check.getFromStatus().getName());
            } else {
                assertNull(check.getFromStatus());
            }
            if (expected.getFromTransitionDate() != null) {
                assertNotNull(check.getFromTransitionDate());
                assertEquals(expected.getFromTransitionDate().getTime(), check.getFromTransitionDate().getTime());
            } else {
                assertNull(check.getFromTransitionDate());
            }
            assertNotNull(expected.getToStatus());
            assertNotNull(check.getToStatus());
            assertEqualsMonitorStatusName(expected.getToStatus().getName(), check.getToStatus().getName());
            assertNotNull(expected.getToTransitionDate());
            assertNotNull(check.getToTransitionDate());
            assertEquals(expected.getToTransitionDate().getTime(), check.getToTransitionDate().getTime());
            if (expected.getDurationInState() != null) {
                assertNotNull(check.getDurationInState());
                assertEquals(expected.getDurationInState(), check.getDurationInState());
            } else {
                assertNull(check.getDurationInState());
            }
        }
    }

    private static void assertEqualsMonitorStatusName(String expected, String check) {
        if (expected.contains("CRITICAL") && check.contains("CRITICAL")) {
            return;
        }
        if (expected.contains("DOWN") && check.contains("DOWN")) {
            return;
        }
        assertEquals(expected, check);
    }
}
