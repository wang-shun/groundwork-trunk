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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.device.DeviceService;
import org.groundwork.foundation.bs.host.HostService;
import org.groundwork.foundation.bs.logmessage.LogMessageService;
import org.groundwork.foundation.bs.monitorserver.MonitorServerService;
import org.groundwork.foundation.bs.status.StatusService;

import com.groundwork.collage.model.Device;
import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.HostStatus;
import com.groundwork.collage.model.LogMessage;
import com.groundwork.collage.model.MonitorServer;
import com.groundwork.collage.model.ServiceStatus;
import com.groundwork.collage.util.DateTime;
import com.groundwork.collage.util.Nagios;


/**
 * 
 * testInterfaces
 * @author <a href="mailto:rruttimann@itgroundwork.com">Roger Ruttimann</a>
 * @author <a href="mailto:philippe.paravicini@eCommerceStudio.com">Philippe Paravicini</a>
 * @version $Id: TestAdminLogMessage.java,v 1.5 2006/01/12 22:28:43 rogerrut Exp $
 */
public class TestAdminLogMessage extends AbstractTestAdminBase
{
	private LogMessageService 		logMsgService;
	private MonitorServerService 	monitorService;
	private DeviceService        	deviceService;
	private HostService          	hostService;
	private StatusService          	statusService;

	private static final String MONITOR       = "groundwork-monitor-new";
	private static final String DEVICE        = "app-svr-2";
	private static final String HOST          = "app-svr-new-host";
	private static final String SERVICE       = "network_users";
	private static final String SEVERITY      = "OK";
	private static final String MON_STATUS    = "UP";
	private static final String TEXT_MSG      = "Life is good for this device";
	private static final String REPORT_DATE   = "2005-03-08 22:11:47";
	private static final String LAST_INSERT   = "2005-03-08 11:25:39";
	private static final String FIRST_INSERT  = "2005-01-01 12:00:00";
	private static final String SUB_COMPONENT = "API";
	private static final String ERROR_TYPE    = "N/A";
	private static final String LOGGER_NAME   = "jura logger";
	private static final String APP_NAME      = "jura app name";
	private static final String DEVICE_NEW        = "new-test-device-for-admin";
	
    protected Log log = LogFactory.getLog(this.getClass());
    
	public TestAdminLogMessage(String x) 
	{ 
		super(x); 


	}

	/** define the tests to be run in this class */
	public static Test suite()
	{
		TestSuite suite = new TestSuite();
		
		executeScript(false, "testdata/monitor-data.sql");

		// run all tests
		//suite = new TestSuite(TestAdminLogMessage.class);

		// or a subset thereoff
		//suite.addTest(new TestAdminLogMessage("testUpdateLogMessageNewDevice"));
		suite.addTest(new TestAdminLogMessage("testUpdateLogMessageExistingService"));

		return suite;
	}

    public void setUp()
    {
        super.setUp();

        logMsgService = collage.getLogMessageService();
        monitorService = collage.getMonitorServerService();
        deviceService  = collage.getDeviceService();
        hostService    = collage.getHostService();
        statusService  = collage.getStatusService();
    }


    public void testUpdateLogMessageNewDevice() throws Exception
	{
		MonitorServer monitor;
		Device        device;
		Host          host;
		
		//// check that none of the entities exist
		monitor = monitorService.getMonitorServerByName(MONITOR);
		assertNull(MONITOR + " does not exist", monitor);

		device = deviceService.getDeviceByIdentification(DEVICE);
		assertNull(DEVICE + " does not exist", device);

		host = hostService.getHostByHostName(HOST);
		assertNull(HOST + " does not exist", host);

		//// update a LogMessage with inexistent Monitor/Device/Host
		admin.updateLogMessage( 
				MONITOR, Nagios.APPLICATION_TYPE, DEVICE, SEVERITY, TEXT_MSG,
				Nagios.createLogMessageProps( 
					HOST, MON_STATUS, REPORT_DATE, LAST_INSERT, SUB_COMPONENT, 
					ERROR_TYPE, SERVICE, LOGGER_NAME, APP_NAME, FIRST_INSERT,TEXT_MSG));


		// verify that MonitorServer and Device were created...
		monitor = monitorService.getMonitorServerByName(MONITOR);
		assertNotNull(MONITOR + " created", monitor);

		device = deviceService.getDeviceByIdentification(DEVICE);
		assertNotNull(DEVICE + " created", device);

		// but not Host
		host = hostService.getHostByHostName(HOST);
		assertNull(HOST + " not created", host);

		LogMessage msg = (LogMessage)logMsgService.getLogMessagesByDeviceIdentification(DEVICE, null, null, null, null, -1, 100).iterator().next();

		// verify the values retrieved from the db
		assertEquals("appType",       Nagios.APPLICATION_TYPE,               msg.getApplicationType().getName());
		assertEquals("device",        device,                                msg.getDevice());
		assertEquals("severity",      metadataService.getSeverityByName(SEVERITY),        msg.getSeverity());
		assertEquals("appSeverity",   metadataService.getSeverityByName(SEVERITY),        msg.getApplicationSeverity());
		assertEquals("mon status",    metadataService.getMonitorStatusByName(MON_STATUS), msg.getMonitorStatus());
		assertEquals("text msg",      TEXT_MSG,                              msg.getTextMessage());
		assertEquals("first insert",  DateTime.parse(FIRST_INSERT),          msg.getFirstInsertDate()); // yes, this how this is currently implemented
		assertEquals("last insert",   DateTime.parse(LAST_INSERT),           msg.getLastInsertDate());  // yes, this how this is currently implemented
		assertNotNull("report date",  msg.getReportDate());                                             // current implementation sets reportDate to "new Date()";
		assertEquals("msg count",     new Integer(1),                        msg.getMsgCount());
		assertEquals("component",     metadataService.getComponentByName("UNDEFINED"),    msg.getComponent());
		assertEquals("priority",      metadataService.getPriorityByName("1"),             msg.getPriority());
		assertEquals("op status",     metadataService.getOperationStatusByName("OPEN"),   msg.getOperationStatus());
		assertEquals("type rule",     metadataService.getTypeRuleByName("UNDEFINED"),     msg.getTypeRule());
		assertEquals("sub component", SUB_COMPONENT,                         msg.get(Nagios.SUB_COMPONENT));
		assertEquals("error type",    ERROR_TYPE,                            msg.get(Nagios.ERROR_TYPE));
		assertEquals("logger name",   LOGGER_NAME,                           msg.get(Nagios.LOGGER_NAME));
		assertEquals("app name",      APP_NAME,                              msg.get(Nagios.APP_NAME));

		// teardown by removing all the new entities
		monitorService.deleteMonitorServer(monitor);
		deviceService.deleteDevice(device);

		monitor = monitorService.getMonitorServerByName(MONITOR);
		assertNull(MONITOR + " was deleted", monitor);

		device = deviceService.getDeviceByIdentification(DEVICE_NEW);
		assertNull(DEVICE_NEW + " was deleted", device);

		assertEquals("no LogMessages for device " + DEVICE, 
				0, logMsgService.getLogMessagesByDeviceIdentification(DEVICE, null, null, null, null, -1, 100).size());
	}


	public void testUpdateLogMessageExistingService() throws Exception
	{
		MonitorServer monitor;
		Device        device;
		Host          host;
		HostStatus    hostStatus;
		ServiceStatus service;

		final String STATE_TYPE = "HARD";
		final String CHECK_TYPE = "ACTIVE";		
		final String LAST_CHECK = LAST_INSERT;
		final String LAST_HARD_STATE = "OK";
	
		log.info("********** testUpdateLogMessageExistingService STARTED ***********");		
		admin.updateHostStatus(MONITOR, Nagios.APPLICATION_TYPE, HOST, DEVICE, 
				Nagios.createHostStatusPropsMinimal(MON_STATUS, LAST_CHECK));
		
		log.info("********** testUpdateLogMessageExistingService2 STARTED ***********");		
		admin.updateServiceStatus(MONITOR, Nagios.APPLICATION_TYPE, HOST, DEVICE, 
				Nagios.createServiceStatusPropsMinimal(
					SERVICE, MON_STATUS, STATE_TYPE, CHECK_TYPE, LAST_HARD_STATE));
		
		//// check that the entities were created
		monitor = monitorService.getMonitorServerByName(MONITOR);
		assertNotNull(MONITOR + " monitor does not exist", monitor);

		device = deviceService.getDeviceByIdentification(DEVICE);
		assertNotNull(DEVICE_NEW + " device does not exist", device);
		
		host = hostService.getHostByHostName(HOST);
		assertNotNull(HOST + " host does not exist", host);

		hostStatus = host.getHostStatus();
		assertNotNull(HOST + " host status does not exist", hostStatus);

		service = statusService.getServiceByDescription(SERVICE, HOST); 
		assertNotNull(SERVICE + " service status does not exist", service);
/*
		//// update a LogMessage with existing Monitor/Device/Host/Service
		admin.updateLogMessage( 
				MONITOR, Nagios.APPLICATION_TYPE, DEVICE_NEW, SEVERITY, TEXT_MSG,
				Nagios.createLogMessageProps( 
					HOST, MON_STATUS, REPORT_DATE, LAST_INSERT, SUB_COMPONENT, 
					ERROR_TYPE, SERVICE, LOGGER_NAME, APP_NAME, FIRST_INSERT));

		LogMessage msg = (LogMessage)logMsgService.getLogMessagesByDeviceIdentification(DEVICE,
																null, 
																null, 
																null, 
																null, 
																-1,
																100).iterator().next();

		// verify the values retrieved from the db
		assertEquals("appType",       Nagios.APPLICATION_TYPE,               msg.getApplicationType().getName());
		assertEquals("device",        device,                                msg.getDevice());
		assertEquals("host status",   hostStatus,                            msg.getHostStatus());
		assertEquals("service",       service,                               msg.getServiceStatus());
		assertEquals("severity",      metadataService.getSeverityByName(SEVERITY),        msg.getSeverity());
		assertEquals("appSeverity",   metadataService.getSeverityByName(SEVERITY),        msg.getApplicationSeverity());
		assertEquals("mon status",    metadataService.getMonitorStatusByName(MON_STATUS), msg.getMonitorStatus());
		assertEquals("text msg",      TEXT_MSG,                              msg.getTextMessage());
		assertEquals("first insert",  DateTime.parse(FIRST_INSERT),           msg.getFirstInsertDate()); // yes, this how this is currently implemented
//		assertEquals("last insert",   DateTime.parse(REPORT_DATE),           msg.getLastInsertDate());  // yes, this how this is currently implemented
		assertNotNull("report date",  msg.getReportDate());                                             // current implementation sets reportDate to "new Date()";
		assertEquals("msg count",     new Integer(1),                        msg.getMsgCount());
		assertEquals("component",     metadataService.getComponentByName("UNDEFINED"),    msg.getComponent());
		assertEquals("priority",      metadataService.getPriorityByName("1"),             msg.getPriority());
		assertEquals("op status",     metadataService.getOperationStatusByName("OPEN"),   msg.getOperationStatus());
		assertEquals("type rule",     metadataService.getTypeRuleByName("UNDEFINED"),     msg.getTypeRule());
		assertEquals("sub component", SUB_COMPONENT,                         msg.get(Nagios.SUB_COMPONENT));
		assertEquals("error type",    ERROR_TYPE,                            msg.get(Nagios.ERROR_TYPE));
		assertEquals("logger name",   LOGGER_NAME,                           msg.get(Nagios.LOGGER_NAME));
		assertEquals("app name",      APP_NAME,                              msg.get(Nagios.APP_NAME));

		// teardown by removing all the new entities
		monitorService.deleteMonitorServer(monitor);
		deviceService.deleteDevice(device);

		monitor = monitorService.getMonitorServerByName(MONITOR);
		assertNull(MONITOR + " was deleted", monitor);

		device = deviceService.getDeviceByIdentification(DEVICE_NEW);
		assertNull(DEVICE + " was deleted", device);

		assertEquals("no LogMessages for device " + DEVICE_NEW, 
				0, logMsgService.getLogMessagesByDeviceIdentification(DEVICE_NEW, null, null, null, null, -1, 100).size());
						*/
	}

}


