/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com

 *	 This program is free software; you can redistribute it and/or modify
 *	 it under the terms of version 2 of the GNU General Public License
 *	 as published by the Free Software Foundation.

 *	 This program is distributed in the hope that it will be useful,
 *	 but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	 GNU General Public License for more details.

 *	 You should have received a copy of the GNU General Public License
 *	 along with this program; if not, write to the Free Software
 *	 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.groundwork.collage.test;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.groundwork.foundation.bs.device.DeviceService;
import org.groundwork.foundation.bs.host.HostService;
import org.groundwork.foundation.bs.monitorserver.MonitorServerService;
import org.groundwork.foundation.bs.status.StatusService;

import com.groundwork.collage.model.Device;
import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.MonitorServer;
import com.groundwork.collage.model.ServiceStatus;
import com.groundwork.collage.util.DateTime;
import com.groundwork.collage.util.Nagios;

/**
 * Tests the methods of CollageAdminInfrastructure that act on ServiceStatus  
 *
 * @author <a href="mailto:philippe.paravicini@eCommerceStudio.com">Philippe Paravicini</a>
 * @version $Id: TestAdminServiceStatus.java 19301 2012-08-10 23:47:46Z rruttimann $
 */
public class TestAdminServiceStatus extends AbstractTestAdminBase
{
	private static final String   MONITOR       = "groundwork-monitor1";
	private static final String   DEVICE        = "192.168.1.103";
	private static final String   DEVICE_NEW        = "192.168.1.111";
	private static final String   HOST          = "VALAIS";
	private static final String   SERVICE1      = "local-disk";
	private static final String   SERVICE2      = "local-procs";
	private static final String   SERVICE3      = "local-users";
	private static final String[] SERVICES      = {SERVICE1, SERVICE2, SERVICE3};
	private static final String[] OUTPUT        = {"PING OK - Packet loss = 0%, RTA = 0.06 ms","PING OK - Packet loss = 5%, RTA = 0.08 ms"};
	private static final String[] MON_STATUS    = {"UP","PENDING"};
	private static final String[] RETRY_NUM     = {"2","5"};
	private static final String[] STATE_TYPE    = {"SOFT","HARD"};
	private static final String[] LAST_CHECK    = {"2005-03-08 22:11:47","2005-03-08 22:16:39"};
	private static final String[] NEXT_CHECK    = {"2005-03-08 22:16:46","2005-03-08 22:21:38"};
	private static final String[] CHECK_TYPE    = {"ACTIVE","PASSIVE"};
	private static final String[] IS_CHECK      = {"1","0"};
	private static final String[] IS_ACP_PASV   = {"1","0"};
	private static final String[] IS_EVENT      = {"1","0"};
	private static final String[] LAST_CHANGE   = {"2005-03-08 11:25:39","2005-03-08 11:30:39"};
	private static final String[] IS_PB_ACKED   = {"0","1"};
	private static final String[] LAST_HARD_ST  = {"UNKNOWN","DOWN"};
	private static final String[] TIME_OK       = {"85130","86730"};
	private static final String[] TIME_UNKNOWN  = {"85120","86720"};
	private static final String[] TIME_WARNING  = {"6720","7123"};
	private static final String[] TIME_CRITICAL = {"720","123"};
	private static final String[] LAST_NOTIF    = {"0","2005-03-08 22:16:38"};
	private static final String[] NOTIF_NUM     = {"4","5"};
	private static final String[] IS_NOTIF      = {"1","0"};
	private static final String[] LATENCY       = {"9","7"};
	private static final String[] EXEC_TIME     = {"24","18"};
	private static final String[] IS_FLAP       = {"0","1"};
	private static final String[] IS_SERV_FLAP  = {"0","1"};
	private static final String[] PERCENT       = {"12.99","15.99"};
	private static final String[] SCHED_DOWN    = {"0","5"};
	private static final String[] IS_PREDICT    = {"1","0"};
	private static final String[] IS_PERF       = {"1","0"};
	private static final String[] IS_OBSESS     = {"1","0"};
	private static final String[] PERFORMANCE_DATA= {"1","0"};
	
	private MonitorServerService 	monitorService;
	private DeviceService        	deviceService;
	private HostService          	hostService;
	private StatusService    		statusService;
	
	public TestAdminServiceStatus(String x) 
	{ 
		super(x);
	}

    public void setUp()
    {
        super.setUp();
        monitorService = collage.getMonitorServerService();
        deviceService  = collage.getDeviceService();
        hostService    = collage.getHostService();
        statusService = collage.getStatusService();
    }

	/** define the tests to be run in this class */
	public static Test suite()
	{
		TestSuite suite = new TestSuite();
		
		executeScript(false, "testdata/monitor-data.sql");

		// run all tests
		//suite = new TestSuite(TestAdminServiceStatus.class);

		// or a subset thereoff
		//suite.addTest(new TestAdminServiceStatus("testUpdateServiceStatusNagios"));
		//suite.addTest(new TestAdminServiceStatus("testUpdateServiceStatusNagiosBulk"));

		return suite;
	}

	public void notestUpdateServiceStatusNagios() throws Exception
	{
		MonitorServer monitor;
		Device        device;
		Host          host;
		ServiceStatus service;


		//// check that none of the entities exist
		//monitor = monitorService.getMonitorServerByName(MONITOR);
		//assertNull(MONITOR + " does not exist", monitor);

		device = deviceService.getDeviceByIdentification(DEVICE_NEW);
		assertNull(DEVICE + " does not exist", device);

		host = hostService.getHostByHostName(HOST);
		assertNull(HOST + " does not exist", host);

		service = statusService.getServiceByDescription(SERVICE1, HOST);
		assertNull(SERVICE1 + " does not exist", service);


		//// update a new ServiceStatus
		admin.updateServiceStatus( 
						MONITOR, Nagios.APPLICATION_TYPE, HOST, DEVICE_NEW,
						Nagios.createServiceStatusProps( 
								SERVICE1, OUTPUT[0], MON_STATUS[0], RETRY_NUM[0], STATE_TYPE[0], 
								LAST_CHECK[0], NEXT_CHECK[0], CHECK_TYPE[0], IS_CHECK[0], IS_ACP_PASV[0],
								IS_EVENT[0], LAST_CHANGE[0], IS_PB_ACKED[0], LAST_HARD_ST[0],
								TIME_OK[0], TIME_UNKNOWN[0], TIME_WARNING[0], TIME_CRITICAL[0],
								LAST_NOTIF[0], NOTIF_NUM[0], IS_NOTIF[0], LATENCY[0], EXEC_TIME[0],
								IS_FLAP[0], IS_SERV_FLAP[0], PERCENT[0], SCHED_DOWN[0], 
								IS_PREDICT[0], IS_PERF[0], IS_OBSESS[0],PERFORMANCE_DATA[0]));


		// verify that all the appropriate entities were created
		monitor = monitorService.getMonitorServerByName(MONITOR);
		assertNotNull(MONITOR + " created", monitor);

		device = deviceService.getDeviceByIdentification(DEVICE_NEW);
		assertNotNull(DEVICE + " created", device);

		host = hostService.getHostByHostName(HOST);
		assertNotNull(HOST + " created", host);

		service = statusService.getServiceByDescription(SERVICE1, HOST);
		assertNotNull(SERVICE1 + " created", service);

		// verify the values retrieved from the db
//		assertEquals("output-0",         OUTPUT[1],                                    service.get(Nagios.LAST_PLUGIN_OUTPUT));
		assertEquals("mon status-0",     metadataService.getMonitorStatusByName(MON_STATUS[0]),     service.getMonitorStatus());
		assertEquals("retry num-0",      new Integer(RETRY_NUM[0]),                    service.get(Nagios.RETRY_NUMBER));
		assertEquals("state type-0",     metadataService.getStateTypeByName(STATE_TYPE[0]),         service.getStateType());
		assertEquals("last check-0",     DateTime.parse(LAST_CHECK[0]),                service.getLastCheckTime());
		assertEquals("next check-0",     DateTime.parse(NEXT_CHECK[0]),                service.getNextCheckTime());
		assertEquals("check type-0",     metadataService.getCheckTypeByName(CHECK_TYPE[0]),         service.getCheckType());
		assertEquals("is check-0",       Boolean.valueOf(IS_CHECK[0].equals("1")),     service.get(Nagios.IS_CHECKS_ENABLED));
		assertEquals("is acp pasv-0",    Boolean.valueOf(IS_ACP_PASV[0].equals("1")),  service.get(Nagios.IS_ACCEPT_PASSIVE_CHECKS));
		assertEquals("is event-0",       Boolean.valueOf(IS_EVENT[0].equals("1")),     service.get(Nagios.IS_EVENT_HANDLERS_ENABLED));
		assertEquals("last change-0",    DateTime.parse(LAST_CHANGE[0]),               service.getLastStateChange());
		assertEquals("is pk acked-0",    Boolean.valueOf(IS_PB_ACKED[0].equals("1")) , service.get(Nagios.IS_PROBLEM_ACKNOWLEDGED));
		assertEquals("last hard st-0",   metadataService.getMonitorStatusByName(LAST_HARD_ST[0]),   service.getLastHardState());
		assertEquals("time ok-0",        new Long(TIME_OK[0]),                         service.get(Nagios.TIME_OK));
		assertEquals("time unknown-0",   new Long(TIME_UNKNOWN[0]),                    service.get(Nagios.TIME_UNKNOWN));
		assertEquals("time warning-0",   new Long(TIME_WARNING[0]),                    service.get(Nagios.TIME_WARNING));
		assertEquals("time critical-0",  new Long(TIME_CRITICAL[0]),                   service.get(Nagios.TIME_CRITICAL));
		assertEquals("last notif-0",     DateTime.parse(LAST_NOTIF[0]),                service.get(Nagios.LAST_NOTIFICATION_TIME));
		assertEquals("notif num-0",      new Integer(NOTIF_NUM[0]),                    service.get(Nagios.CURRENT_NOTIFICATION_NUMBER));
		assertEquals("is notif-0",       Boolean.valueOf(IS_NOTIF[0].equals("1")),     service.get(Nagios.IS_NOTIFICATIONS_ENABLED));
		assertEquals("latency-0",        new Double(LATENCY[0]),                         service.get(Nagios.LATENCY));
		assertEquals("exec time-0",      new Double(EXEC_TIME[0]),                       service.get(Nagios.EXECUTION_TIME));
		assertEquals("is flap-0",        Boolean.valueOf(IS_FLAP[0].equals("1")),      service.get(Nagios.IS_FLAP_DETECTION_ENABLED));
		assertEquals("is serv flap-0",   Boolean.valueOf(IS_SERV_FLAP[0].equals("1")), service.get(Nagios.IS_SERVICE_FLAPPING));
		assertEquals("percent change-0", new Double(PERCENT[0]),                       service.get(Nagios.PERCENT_STATE_CHANGE));
		assertEquals("sched down -0",    new Integer(SCHED_DOWN[0]),                   service.get(Nagios.SCHEDULED_DOWNTIME_DEPTH));
		assertEquals("is predict-0",     Boolean.valueOf(IS_PREDICT[0].equals("1")),   service.get(Nagios.IS_FAILURE_PREDICTION_ENABLED));
		assertEquals("is perf-0",        Boolean.valueOf(IS_PERF[0].equals("1")),      service.get(Nagios.IS_PROCESS_PERFORMANCE_DATA));
		assertEquals("is obsess-0",      Boolean.valueOf(IS_OBSESS[0].equals("1")),    service.get(Nagios.IS_OBSESS_OVER_SERVICE));


		//// update an existing HostStatus using the Nagios interface
		/*admin.updateServiceStatus( 
				MONITOR, HOST, DEVICE_NEW, SERVICE1, 
				OUTPUT[1], MON_STATUS[1], RETRY_NUM[1], STATE_TYPE[1], 
				LAST_CHECK[1], NEXT_CHECK[1], CHECK_TYPE[1], IS_CHECK[1], IS_ACP_PASV[1],
				IS_EVENT[1], LAST_CHANGE[1], IS_PB_ACKED[1], LAST_HARD_ST[1],
				TIME_OK[1], TIME_UNKNOWN[1], TIME_WARNING[1], TIME_CRITICAL[1],
				LAST_NOTIF[1], NOTIF_NUM[1], IS_NOTIF[1], LATENCY[1], EXEC_TIME[1],
				IS_FLAP[1], IS_SERV_FLAP[1], PERCENT[1], SCHED_DOWN[1], 
				IS_PREDICT[1], IS_PERF[1], IS_OBSESS[1]);
*/

		// verify the values retrieved from the db
		host = hostService.getHostByHostName(HOST);
		assertNotNull(HOST + " retrieved", host);

		service = statusService.getServiceByDescription(SERVICE1, HOST);
		assertNotNull(SERVICE1 + " retrieved", service);

/*		assertEquals("output-1",         OUTPUT[1],                                    service.get(Nagios.LAST_PLUGIN_OUTPUT));
		assertEquals("mon status-1",     metadataService.getMonitorStatusByName(MON_STATUS[1]),     service.getMonitorStatus());
		assertEquals("retry num-1",      new Integer(RETRY_NUM[1]),                    service.get(Nagios.RETRY_NUMBER));
		assertEquals("state type-1",     metadataService.getStateTypeByName(STATE_TYPE[1]),         service.getStateType());
		assertEquals("last check-1",     DateTime.parse(LAST_CHECK[1]),                service.getLastCheckTime());
		assertEquals("next check-1",     DateTime.parse(NEXT_CHECK[1]),                service.getNextCheckTime());
		assertEquals("check type-1",     metadataService.getCheckTypeByName(CHECK_TYPE[1]),         service.getCheckType());
		assertEquals("is check-1",       Boolean.valueOf(IS_CHECK[1].equals("1")),     service.get(Nagios.IS_CHECKS_ENABLED));
		assertEquals("is acp pasv-1",    Boolean.valueOf(IS_ACP_PASV[1].equals("1")),  service.get(Nagios.IS_ACCEPT_PASSIVE_CHECKS));
		assertEquals("is event-1",       Boolean.valueOf(IS_EVENT[1].equals("1")),     service.get(Nagios.IS_EVENT_HANDLERS_ENABLED));
		assertEquals("last change-1",    DateTime.parse(LAST_CHANGE[1]),               service.getLastStateChange());
		assertEquals("is pk acked-1",    Boolean.valueOf(IS_PB_ACKED[1].equals("1")) , service.get(Nagios.IS_PROBLEM_ACKNOWLEDGED));
		assertEquals("last hard st-1",   metadataService.getMonitorStatusByName(LAST_HARD_ST[1]),   service.getLastHardState());
		assertEquals("time ok-1",        new Long(TIME_OK[1]),                         service.get(Nagios.TIME_OK));
		assertEquals("time unknown-1",   new Long(TIME_UNKNOWN[1]),                    service.get(Nagios.TIME_UNKNOWN));
		assertEquals("time warning-1",   new Long(TIME_WARNING[1]),                    service.get(Nagios.TIME_WARNING));
		assertEquals("time critical-1",  new Long(TIME_CRITICAL[1]),                   service.get(Nagios.TIME_CRITICAL));
		assertEquals("last notif-1",     DateTime.parse(LAST_NOTIF[1]),                service.get(Nagios.LAST_NOTIFICATION_TIME));
		assertEquals("notif num-1",      new Integer(NOTIF_NUM[1]),                    service.get(Nagios.CURRENT_NOTIFICATION_NUMBER));
		assertEquals("is notif-1",       Boolean.valueOf(IS_NOTIF[1].equals("1")),     service.get(Nagios.IS_NOTIFICATIONS_ENABLED));
		assertEquals("latency-1",        new Double(LATENCY[1]),                       service.get(Nagios.LATENCY));
		assertEquals("exec time-1",      new Double(EXEC_TIME[1]),                     service.get(Nagios.EXECUTION_TIME));
		assertEquals("is flap-1",        Boolean.valueOf(IS_FLAP[1].equals("1")),      service.get(Nagios.IS_FLAP_DETECTION_ENABLED));
		assertEquals("is serv flap-1",   Boolean.valueOf(IS_SERV_FLAP[1].equals("1")), service.get(Nagios.IS_SERVICE_FLAPPING));
		assertEquals("percent change-1", new Double(PERCENT[1]),                       service.get(Nagios.PERCENT_STATE_CHANGE));
		assertEquals("sched down -1",    new Integer(SCHED_DOWN[1]),                   service.get(Nagios.SCHEDULED_DOWNTIME_DEPTH));
		assertEquals("is predict-1",     Boolean.valueOf(IS_PREDICT[1].equals("1")),   service.get(Nagios.IS_FAILURE_PREDICTION_ENABLED));
		assertEquals("is perf-1",        Boolean.valueOf(IS_PERF[1].equals("1")),      service.get(Nagios.IS_PROCESS_PERFORMANCE_DATA));
		assertEquals("is obsess-1",      Boolean.valueOf(IS_OBSESS[1].equals("1")),    service.get(Nagios.IS_OBSESS_OVER_SERVICE));

*/
		// teardown by removing all the new entities
		monitorService.deleteMonitorServer(monitor);
		deviceService.deleteDevice(device);

		monitor = monitorService.getMonitorServerByName(MONITOR);
		assertNull(MONITOR + " was deleted", monitor);

		device = deviceService.getDeviceByIdentification(DEVICE_NEW);
		assertNull(DEVICE_NEW + " was deleted", device);

		host = hostService.getHostByHostName(HOST);
		assertNull(HOST + " was deleted", host);

		service = statusService.getServiceByDescription(SERVICE1, HOST);
		assertNull(SERVICE1 + " was deleted", service);
	}


	public void notestUpdateServiceStatusNagiosBulk() throws Exception
	{
		MonitorServer monitor;
		Device        device;
		Host          host;
		ServiceStatus service;
		Set propSet;

		//// check that none of the entities exist
		monitor = monitorService.getMonitorServerByName(MONITOR);
		assertNull(MONITOR + " does not exist", monitor);

		device = deviceService.getDeviceByIdentification(DEVICE_NEW);
		assertNull(DEVICE_NEW + " does not exist", device);

		host = hostService.getHostByHostName(HOST);
		assertNull(HOST + " does not exist", host);

		//// create a Collection of properties to update ServiceStatus in Bulk
		propSet = new HashSet();

		for (int i=0; i < SERVICES.length ; i++)
		{
			propSet.add(
						Nagios.createServiceStatusProps( 
								SERVICES[i], OUTPUT[0], MON_STATUS[0], RETRY_NUM[0], STATE_TYPE[0], 
								LAST_CHECK[0], NEXT_CHECK[0], CHECK_TYPE[0], IS_CHECK[0], IS_ACP_PASV[0],
								IS_EVENT[0], LAST_CHANGE[0], IS_PB_ACKED[0], LAST_HARD_ST[0],
								TIME_OK[0], TIME_UNKNOWN[0], TIME_WARNING[0], TIME_CRITICAL[0],
								LAST_NOTIF[0], NOTIF_NUM[0], IS_NOTIF[0], LATENCY[0], EXEC_TIME[0],
								IS_FLAP[0], IS_SERV_FLAP[0], PERCENT[0], SCHED_DOWN[0], 
								IS_PREDICT[0], IS_PERF[0], IS_OBSESS[0],PERFORMANCE_DATA[0]));
		} // end for


		//// update new ServiceStatuses in Bulk
		admin.updateServiceStatus(MONITOR, Nagios.APPLICATION_TYPE, HOST, DEVICE_NEW, propSet);

		// verify that all the appropriate entities were created
		monitor = monitorService.getMonitorServerByName(MONITOR);
		assertNotNull(MONITOR + " created", monitor);

		device = deviceService.getDeviceByIdentification(DEVICE);
		assertNotNull(DEVICE + " created", device);

		host = hostService.getHostByHostName(HOST);
		assertNotNull(HOST + " created", host);

		for (int i=0; i < SERVICES.length ; i++)
		{
			service = statusService.getServiceByDescription(SERVICES[i], HOST);
			assertNotNull(SERVICES[i] + " created", service);

			// verify the values retrieved from the db
			assertEquals("output-0",         OUTPUT[0],                                    service.get(Nagios.LAST_PLUGIN_OUTPUT));
			assertEquals("mon status-0",     metadataService.getMonitorStatusByName(MON_STATUS[0]),     service.getMonitorStatus());
			assertEquals("retry num-0",      new Integer(RETRY_NUM[0]),                    service.get(Nagios.RETRY_NUMBER));
			assertEquals("state type-0",     metadataService.getStateTypeByName(STATE_TYPE[0]),         service.getStateType());
			assertEquals("last check-0",     DateTime.parse(LAST_CHECK[0]),                service.getLastCheckTime());
			assertEquals("next check-0",     DateTime.parse(NEXT_CHECK[0]),                service.getNextCheckTime());
			assertEquals("check type-0",     metadataService.getCheckTypeByName(CHECK_TYPE[0]),         service.getCheckType());
			assertEquals("is check-0",       Boolean.valueOf(IS_CHECK[0].equals("1")),     service.get(Nagios.IS_CHECKS_ENABLED));
			assertEquals("is acp pasv-0",    Boolean.valueOf(IS_ACP_PASV[0].equals("1")),  service.get(Nagios.IS_ACCEPT_PASSIVE_CHECKS));
			assertEquals("is event-0",       Boolean.valueOf(IS_EVENT[0].equals("1")),     service.get(Nagios.IS_EVENT_HANDLERS_ENABLED));
			assertEquals("last change-0",    DateTime.parse(LAST_CHANGE[0]),               service.getLastStateChange());
			assertEquals("is pk acked-0",    Boolean.valueOf(IS_PB_ACKED[0].equals("1")) , service.get(Nagios.IS_PROBLEM_ACKNOWLEDGED));
			assertEquals("last hard st-0",   metadataService.getMonitorStatusByName(LAST_HARD_ST[0]),   service.getLastHardState());
			assertEquals("time ok-0",        new Long(TIME_OK[0]),                         service.get(Nagios.TIME_OK));
			assertEquals("time unknown-0",   new Long(TIME_UNKNOWN[0]),                    service.get(Nagios.TIME_UNKNOWN));
			assertEquals("time warning-0",   new Long(TIME_WARNING[0]),                    service.get(Nagios.TIME_WARNING));
			assertEquals("time critical-0",  new Long(TIME_CRITICAL[0]),                   service.get(Nagios.TIME_CRITICAL));
			assertEquals("last notif-0",     DateTime.parse(LAST_NOTIF[0]),                service.get(Nagios.LAST_NOTIFICATION_TIME));
			assertEquals("notif num-0",      new Integer(NOTIF_NUM[0]),                    service.get(Nagios.CURRENT_NOTIFICATION_NUMBER));
			assertEquals("is notif-0",       Boolean.valueOf(IS_NOTIF[0].equals("1")),     service.get(Nagios.IS_NOTIFICATIONS_ENABLED));
			assertEquals("latency-0",        new Double(LATENCY[0]),                         service.get(Nagios.LATENCY));
			assertEquals("exec time-0",      new Double(EXEC_TIME[0]),                       service.get(Nagios.EXECUTION_TIME));
			assertEquals("is flap-0",        Boolean.valueOf(IS_FLAP[0].equals("1")),      service.get(Nagios.IS_FLAP_DETECTION_ENABLED));
			assertEquals("is serv flap-0",   Boolean.valueOf(IS_SERV_FLAP[0].equals("1")), service.get(Nagios.IS_SERVICE_FLAPPING));
			assertEquals("percent change-0", new Double(PERCENT[0]),                       service.get(Nagios.PERCENT_STATE_CHANGE));
			assertEquals("sched down -0",    new Integer(SCHED_DOWN[0]),                   service.get(Nagios.SCHEDULED_DOWNTIME_DEPTH));
			assertEquals("is predict-0",     Boolean.valueOf(IS_PREDICT[0].equals("1")),   service.get(Nagios.IS_FAILURE_PREDICTION_ENABLED));
			assertEquals("is perf-0",        Boolean.valueOf(IS_PERF[0].equals("1")),      service.get(Nagios.IS_PROCESS_PERFORMANCE_DATA));
			assertEquals("is obsess-0",      Boolean.valueOf(IS_OBSESS[0].equals("1")),    service.get(Nagios.IS_OBSESS_OVER_SERVICE));

		}


		//// update existing ServiceStatus in Bulk
		propSet = new HashSet();

		for (int i=0; i < SERVICES.length ; i++)
		{
			propSet.add(
						Nagios.createServiceStatusProps( 
								SERVICES[i], OUTPUT[1], MON_STATUS[1], RETRY_NUM[1], STATE_TYPE[1], 
								LAST_CHECK[1], NEXT_CHECK[1], CHECK_TYPE[1], IS_CHECK[1], IS_ACP_PASV[1],
								IS_EVENT[1], LAST_CHANGE[1], IS_PB_ACKED[1], LAST_HARD_ST[1],
								TIME_OK[1], TIME_UNKNOWN[1], TIME_WARNING[1], TIME_CRITICAL[1],
								LAST_NOTIF[1], NOTIF_NUM[1], IS_NOTIF[1], LATENCY[1], EXEC_TIME[1],
								IS_FLAP[1], IS_SERV_FLAP[1], PERCENT[1], SCHED_DOWN[1], 
								IS_PREDICT[1], IS_PERF[1], IS_OBSESS[1],PERFORMANCE_DATA[1]));
		} // end for

		admin.updateServiceStatus(MONITOR, Nagios.APPLICATION_TYPE, HOST, DEVICE, propSet);

		// verify the values retrieved from the db
		host = hostService.getHostByHostName(HOST);
		assertNotNull(HOST + " retrieved", host);

		for (int i=0; i < SERVICES.length ; i++)
		{
			service = statusService.getServiceByDescription(SERVICES[i], HOST);
			assertNotNull(SERVICES[i] + " retrieved", service);

			assertEquals("output-1",         OUTPUT[1],                                    service.get(Nagios.LAST_PLUGIN_OUTPUT));
			assertEquals("mon status-1",     metadataService.getMonitorStatusByName(MON_STATUS[1]),     service.getMonitorStatus());
			assertEquals("retry num-1",      new Integer(RETRY_NUM[1]),                    service.get(Nagios.RETRY_NUMBER));
			assertEquals("state type-1",     metadataService.getStateTypeByName(STATE_TYPE[1]),         service.getStateType());
			assertEquals("last check-1",     DateTime.parse(LAST_CHECK[1]),                service.getLastCheckTime());
			assertEquals("next check-1",     DateTime.parse(NEXT_CHECK[1]),                service.getNextCheckTime());
			assertEquals("check type-1",     metadataService.getCheckTypeByName(CHECK_TYPE[1]),         service.getCheckType());
			assertEquals("is check-1",       Boolean.valueOf(IS_CHECK[1].equals("1")),     service.get(Nagios.IS_CHECKS_ENABLED));
			assertEquals("is acp pasv-1",    Boolean.valueOf(IS_ACP_PASV[1].equals("1")),  service.get(Nagios.IS_ACCEPT_PASSIVE_CHECKS));
			assertEquals("is event-1",       Boolean.valueOf(IS_EVENT[1].equals("1")),     service.get(Nagios.IS_EVENT_HANDLERS_ENABLED));
			assertEquals("last change-1",    DateTime.parse(LAST_CHANGE[1]),               service.getLastStateChange());
			assertEquals("is pk acked-1",    Boolean.valueOf(IS_PB_ACKED[1].equals("1")) , service.get(Nagios.IS_PROBLEM_ACKNOWLEDGED));
			assertEquals("last hard st-1",   metadataService.getMonitorStatusByName(LAST_HARD_ST[1]),   service.getLastHardState());
			assertEquals("time ok-1",        new Long(TIME_OK[1]),                         service.get(Nagios.TIME_OK));
			assertEquals("time unknown-1",   new Long(TIME_UNKNOWN[1]),                    service.get(Nagios.TIME_UNKNOWN));
			assertEquals("time warning-1",   new Long(TIME_WARNING[1]),                    service.get(Nagios.TIME_WARNING));
			assertEquals("time critical-1",  new Long(TIME_CRITICAL[1]),                   service.get(Nagios.TIME_CRITICAL));
			assertEquals("last notif-1",     DateTime.parse(LAST_NOTIF[1]),                service.get(Nagios.LAST_NOTIFICATION_TIME));
			assertEquals("notif num-1",      new Integer(NOTIF_NUM[1]),                    service.get(Nagios.CURRENT_NOTIFICATION_NUMBER));
			assertEquals("is notif-1",       Boolean.valueOf(IS_NOTIF[1].equals("1")),     service.get(Nagios.IS_NOTIFICATIONS_ENABLED));
			assertEquals("latency-1",        new Double(LATENCY[1]),                         service.get(Nagios.LATENCY));
			assertEquals("exec time-1",      new Double(EXEC_TIME[1]),                       service.get(Nagios.EXECUTION_TIME));
			assertEquals("is flap-1",        Boolean.valueOf(IS_FLAP[1].equals("1")),      service.get(Nagios.IS_FLAP_DETECTION_ENABLED));
			assertEquals("is serv flap-1",   Boolean.valueOf(IS_SERV_FLAP[1].equals("1")), service.get(Nagios.IS_SERVICE_FLAPPING));
			assertEquals("percent change-1", new Double(PERCENT[1]),                       service.get(Nagios.PERCENT_STATE_CHANGE));
			assertEquals("sched down -1",    new Integer(SCHED_DOWN[1]),                   service.get(Nagios.SCHEDULED_DOWNTIME_DEPTH));
			assertEquals("is predict-1",     Boolean.valueOf(IS_PREDICT[1].equals("1")),   service.get(Nagios.IS_FAILURE_PREDICTION_ENABLED));
			assertEquals("is perf-1",        Boolean.valueOf(IS_PERF[1].equals("1")),      service.get(Nagios.IS_PROCESS_PERFORMANCE_DATA));
			assertEquals("is obsess-1",      Boolean.valueOf(IS_OBSESS[1].equals("1")),    service.get(Nagios.IS_OBSESS_OVER_SERVICE));
		} // end for


		// teardown by removing all the new entities
		monitorService.deleteMonitorServer(monitor);
		deviceService.deleteDevice(device);
		hostService.deleteHost(host);

		monitor = monitorService.getMonitorServerByName(MONITOR);
		assertNull(MONITOR + " was deleted", monitor);

		device = deviceService.getDeviceByIdentification(DEVICE);
		assertNull(DEVICE + " was deleted", device);

		host = hostService.getHostByHostName(HOST);
		assertNull(HOST + " was deleted", host);

		for (int i=0; i < SERVICES.length ; i++)
		{
			service = statusService.getServiceByDescription(SERVICES[i], HOST);
			assertNull(SERVICES[i] + " was deleted", service);
		} 
	}
}
