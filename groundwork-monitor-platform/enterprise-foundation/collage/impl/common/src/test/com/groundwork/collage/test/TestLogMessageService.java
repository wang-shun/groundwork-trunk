/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com

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

import com.groundwork.collage.model.Host;
import com.groundwork.collage.model.LogMessage;
import com.groundwork.collage.model.PropertyType;
import com.groundwork.collage.model.ServiceStatus;
import com.groundwork.collage.model.Severity;
import com.groundwork.collage.model.impl.StateTransition;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.groundwork.foundation.bs.host.HostService;
import org.groundwork.foundation.bs.logmessage.LogMessageService;
import org.groundwork.foundation.bs.metadata.MetadataService;
import org.groundwork.foundation.bs.status.StatusService;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationQueryList;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author rdandridge
 * 
 */
public class TestLogMessageService extends
		AbstractTestCaseWithTransactionSupport {
	/* the following constants should reflect the state of test data */
	LogMessageService logMessageService = null;
	HostService hostService = null;
	StatusService statusService = null;
    MetadataService metadataService = null;

	public static final String HOSTGROUPNAME_1 = "demo-system";
	public static final String HOSTNAME_1 = "nagios";
	public static final String HOSTNAME_2 = "db-svr";
	public static final String HOSTNAME_3 = "exchange";
	public static final String SERVICEDESCRIP_1 = "local_users";

	public TestLogMessageService(String x) {
		super(x);
	}

	/** define the tests to be run in this class */
	public static Test suite() {
		TestSuite suite = new TestSuite();

		executeScript(false, "testdata/monitor-data.sql");

		// run all tests
		// suite = new TestSuite(TestLogMessageService.class);

		// or a subset thereoff
		suite.addTest(new TestLogMessageService("testGetLogMessages"));
		suite.addTest(new TestLogMessageService(
				"testGetLogMessagesByApplicationType"));
		suite.addTest(new TestLogMessageService(
				"testGetLogMessagesByDeviceIdentification"));
		suite.addTest(new TestLogMessageService(
				"testGetLogMessagesByDeviceIdentifications"));
		suite
				.addTest(new TestLogMessageService(
						"testGetLogMessagesByDeviceId"));
		suite
				.addTest(new TestLogMessageService(
						"testGetLogMessagesByDeviceIds"));
		suite
				.addTest(new TestLogMessageService(
						"testGetLogMessagesByHostName"));
		suite
				.addTest(new TestLogMessageService(
						"testGetLogMessagesByHostNames"));
		suite.addTest(new TestLogMessageService("testGetLogMessagesByHostId"));
		suite.addTest(new TestLogMessageService("testGetLogMessagesByHostIds"));
		suite.addTest(new TestLogMessageService("testGetLogMessagesByService"));
		suite.addTest(new TestLogMessageService(
				"testGetLogMessagesByServiceStatusId"));
		suite.addTest(new TestLogMessageService(
				"testGetLogMessagesByHostGroupName"));
		/*suite
				.addTest(new TestLogMessageService(
						"testUnlinkLogMessagesFromHost"));*/
		suite.addTest(new TestLogMessageService("testHostStateTransitions"));
		/*suite
				.addTest(new TestLogMessageService(
						"testGetLogMessagesByCriteria"));*/
		// suite.addTest(new
		// TestLogMessageService("testGetLogMessagesByHostGroupNames"));
		// suite.addTest(new
		// TestLogMessageService("testGetLogMessagesByHostGroupId"));
		// suite.addTest(new
		// TestLogMessageService("testGetLogMessagesByHostGroupIds"));
		// suite.addTest(new TestLogMessageService("testGetLogMessageById"));
		// suite.addTest(new
		// TestLogMessageService("testUnlinkLogMessagesFromService"));
		// suite.addTest(new
		// TestLogMessageService("testDeleteLogMessagesForDevice"));
		// suite.addTest(new
		// TestLogMessageService("testGetLogMessageForConsolidationCriteria"));
		// suite.addTest(new TestLogMessageService("testSetIsStateChanged"));

        suite.addTest(new TestLogMessageService("testSetDynamicProperty"));

		return suite;
	}

    public void setUp() throws Exception
    {
        super.setUp();

		// Retrieve logmessage business service
		logMessageService = collage.getLogMessageService();

		assertNotNull(logMessageService);

		hostService = collage.getHostService();
		assertNotNull(hostService);

		statusService = collage.getStatusService();
		assertNotNull(statusService);

        metadataService = collage.getMetadataService();
        assertNotNull(metadataService);
	}

	public void testGetLogMessages() {
		FoundationQueryList logMessages = logMessageService.getLogMessages(
				null, null, null, null, 0, 1000);
		assertNotNull(logMessages);
		assertEquals("Number of log messages", 11, logMessages.size());

		logMessages = null;
		logMessages = logMessageService.getLogMessages(null, null, null, null,
				500, 1000);
		assertNotNull(logMessages);
		assertEquals("Number of log messages", 0, logMessages.size());

		logMessages = null;
		logMessages = logMessageService.getLogMessages(null, null, null, null,
				-1, -1);
		assertNotNull(logMessages);
		assertEquals("Number of log messages", 11, logMessages.size());

		logMessages = null;
		String startDate = "2006-11-16 00:00:01";
		String endDate = "2006-11-18 23:59:59";
		logMessages = logMessageService.getLogMessages(startDate, endDate,
				null, null, -1, -1);
		assertNotNull(logMessages);
		assertEquals("Number of log messages", 10, logMessages.size());
		// create a start date that's out of range
		startDate = "2007-9-17 23:59:59";
		logMessages = null;
		logMessages = logMessageService.getLogMessages(startDate, endDate,
				null, null, -1, -1);
		assertNotNull(logMessages);
		assertEquals("Number of log messages", 0, logMessages.size());

		logMessages = null;
		FilterCriteria filter = FilterCriteria.eq("device.deviceId", 1);
		logMessages = logMessageService.getLogMessages(null, null, filter,
				null, -1, -1);
		assertNotNull(logMessages);
		assertEquals("Number of log messages", 8, logMessages.size());
	}

	public void testGetLogMessagesByApplicationType() {
		FoundationQueryList logMessages = logMessageService
				.getLogMessagesByApplicationTypeName("NAGIOS", null, null,
						null, null, -1, -1);
		assertNotNull(logMessages);
		assertEquals("Number of log messages", 11, logMessages.size());

		logMessages = null;
		logMessages = logMessageService.getLogMessagesByApplicationTypeName(
				"SYSTEM", null, null, null, null, -1, -1);
		assertNotNull(logMessages);
		assertEquals("Number of log messages", 0, logMessages.size());
	}

	public void testGetLogMessagesByDeviceIdentification() {
		FoundationQueryList logMessages = logMessageService
				.getLogMessagesByDeviceIdentification("192.168.1.100", null,
						null, null, null, -1, -1);
		assertNotNull(logMessages);
		assertEquals("Number of log messages", 8, logMessages.size());
	}

	public void testGetLogMessagesByDeviceIdentifications() {
		String[] idList = { "192.168.1.100", "192.168.1.101", "192.168.1.102" };
		FoundationQueryList logMessages = logMessageService
				.getLogMessagesByDeviceIdentifications(idList, null, null,
						null, null, -1, -1);
		assertNotNull(logMessages);
		assertEquals("Number of log messages", 11, logMessages.size());
	}

	public void testGetLogMessagesByDeviceId() {
		FoundationQueryList logMessages = logMessageService
				.getLogMessagesByDeviceId(1, null, null, null, null, -1, -1);
		assertNotNull(logMessages);
		assertEquals("Number of log messages", 8, logMessages.size());
	}

	public void testGetLogMessagesByDeviceIds() {
		int[] idList = { 1, 2, 3 };
		FoundationQueryList logMessages = logMessageService
				.getLogMessagesByDeviceIds(idList, null, null, null, null, -1,
						-1);
		assertNotNull(logMessages);
		assertEquals("Number of log messages", 11, logMessages.size());
	}

	public void testGetLogMessagesByHostName() {
		FoundationQueryList logMessages = logMessageService
				.getLogMessagesByHostName(HOSTNAME_1, null, null, null, null,
						-1, -1);
		assertNotNull(logMessages);
		assertEquals("Number of log messages", 8, logMessages.size());
	}

	public void testGetLogMessagesByHostNames() {
		String[] hostNames = { HOSTNAME_1, HOSTNAME_2, HOSTNAME_3 };
		FoundationQueryList logMessages = logMessageService
				.getLogMessagesByHostNames(hostNames, null, null, null, null,
						-1, -1);
		assertNotNull(logMessages);
		assertEquals("Number of log messages", 11, logMessages.size());
	}

	public void testGetLogMessagesByHostId() {
		Host host = hostService.getHostByHostName(HOSTNAME_1);
		assertNotNull(host);
		FoundationQueryList logMessages = logMessageService
				.getLogMessagesByHostId(host.getHostId(), null, null, null,
						null, -1, -1);
		assertNotNull(logMessages);
		assertEquals("Number of log messages", 8, logMessages.size());
	}

	public void testGetLogMessagesByHostIds() {
		FoundationQueryList hosts = hostService.getHosts(null, null, -1, -1);
		assertNotNull(hosts);
		int[] hostIds = new int[hosts.size()];
		for (int i = 0; i < hosts.size(); i++) {
			Host host = (Host) hosts.get(i);
			hostIds[i] = host.getHostId().intValue();
		}
		FoundationQueryList logMessages = logMessageService
				.getLogMessagesByHostIds(hostIds, null, null, null, null, -1,
						-1);
		assertNotNull(logMessages);
		assertEquals(logMessages.size(), 11);
	}

	public void testGetLogMessagesByService() {
		FoundationQueryList logMessages = logMessageService
				.getLogMessagesByService(HOSTNAME_1, SERVICEDESCRIP_1, null,
						null, null, null, -1, -1);
		assertNotNull(logMessages);
		assertEquals("Number of log messages", 3, logMessages.size());
	}

	public void testGetLogMessagesByServiceStatusId() {
		ServiceStatus service = statusService.getServiceByDescription(
				SERVICEDESCRIP_1, HOSTNAME_1);
		assertNotNull(service);
		FoundationQueryList logMessages = logMessageService
				.getLogMessagesByServiceStatusId(service.getServiceStatusId()
						.intValue(), null, null, null, null, -1, -1);
		assertNotNull(logMessages);
		assertEquals("Number of log messages", 3, logMessages.size());
	}

	public void testGetLogMessagesByHostGroupName() {
		FoundationQueryList logMessages = logMessageService
				.getLogMessagesByHostGroupName(HOSTGROUPNAME_1, null, null,
						null, null, -1, -1);
		assertNotNull(logMessages);
		assertEquals("Number of log messages", 11, logMessages.size());
	}

	public void testGetLogMessagesByHostGroupNames() {
		// TODO: implementation
	}

	public void testGetLogMessagesByHostGroupId() {
		// TODO: implementation
	}

	public void testGetLogMessagesByHostGroupIds() {
		// TODO: implementation
	}

	public void testGetLogMessageById() {
		// TODO: implementation
	}

	// TODO: move this method to the Service tests
	public void testUnlinkLogMessagesFromService() {

	}

	// TODO: move this method to the Host tests
	public void doNottestUnlinkLogMessagesFromHost() {
		int numAffected = logMessageService
				.unlinkLogMessagesFromHost(HOSTNAME_1);
		assertEquals("messages unlinked from host", 8, numAffected);

		// FoundationQueryList messages =
		// logMessageService.getLogMessagesByHostName("nagios", null, null,
		// null, null, -1, -1);
		// assertEquals("No messages for this host", 0, messages.size());
	}

	public void testDeleteLogMessagesForDevice() {
		// TODO: implementation
	}

	public void testGetLogMessageForConsolidationCriteria() {
		// TODO: implementation
	}

	public void testSetIsStateChanged() {
		// TODO: implementation
	}

    /**
     * Unit tests for Hoststate transitions.
     */
    public void testHostStateTransitions() {
        Date currDate = Calendar.getInstance().getTime();
        SimpleDateFormat sdfStart = new SimpleDateFormat("MM/dd/yyyy 00:00:00");
        String startDate = sdfStart.format(currDate);
        SimpleDateFormat sdfEnd = new SimpleDateFormat("MM/dd/yyyy 23:59:59");
        String endDate = sdfEnd.format(currDate);
        List<StateTransition> transitionList = logMessageService.getHostStateTransitions("nagios", startDate, endDate, false);
        assertNotNull(transitionList);
        assertEquals("Number of state transitions", 0, transitionList.size());
        // Set date to yesterday 10am
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        cal.set(Calendar.HOUR_OF_DAY, 10);
        cal.set(Calendar.MINUTE, 00);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date firstinsertdate = cal.getTime();
        // SCENARIO : 1
        this.createTestMessage("exchange", null, "MINOR", "NAGIOS", "Unit test message 1", "OPEN", "PENDING", firstinsertdate);
        transitionList = logMessageService.getHostStateTransitions("exchange", startDate, endDate, false);
        assertEquals("Number of state transitions", 1, transitionList.size());
        if (transitionList != null) {
            for (StateTransition stateTransition : transitionList) {
                assertNull("From status :", stateTransition.getFromStatus());
                assertEquals("To status :", "PENDING", stateTransition.getToStatus().getName());
            }
        } else {
            assertNotNull(transitionList);
        }
        // First state change yesterday
        cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        cal.set(Calendar.HOUR_OF_DAY, 22);
        cal.set(Calendar.MINUTE, 00);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        firstinsertdate = cal.getTime();
        // SCENARIO : 2
        LogMessage msg = this.createTestMessage("exchange", null, "MAJOR", "NAGIOS", "Unit test message 2", "OPEN", "UNSCHEDULED CRITICAL", firstinsertdate);
        transitionList = logMessageService.getHostStateTransitions("exchange", startDate, endDate, false);
        assertEquals("Number of state transitions", 1, transitionList.size());
        if (transitionList != null) {

            cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 00);
            cal.set(Calendar.MINUTE, 00);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long sinceMidnight = System.currentTimeMillis() - cal.getTime().getTime();
            // TODO: Enable this test after availability refactoring for 7.1.0.
            //assertEquals("Duration in state : ",sinceMidnight, transitionList.get(0).getDurationInState().longValue());

        } else {
            assertNotNull(transitionList);
        }
        // SCENARIO : 3
        // First state change today at 3am
        cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 3);
        cal.set(Calendar.MINUTE, 00);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        firstinsertdate = cal.getTime();
        msg.setFirstInsertDate(firstinsertdate);
        logMessageService.saveLogMessage(msg);
        transitionList = logMessageService.getHostStateTransitions("exchange", startDate, endDate, false);
        if (transitionList != null) {
            // TODO: Enable this test after availability refactoring for 7.1.0.
            //assertEquals("Duration in state : ",sincelaststatechange, transitionList.get(0).getDurationInState().longValue());

        } else {
            assertNotNull(transitionList);
        }

        // SCENARIO : 4
        // First state change today at 3am
        cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        cal.set(Calendar.HOUR_OF_DAY, 22);
        cal.set(Calendar.MINUTE, 00);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        firstinsertdate = cal.getTime();
        msg = this.createTestMessage("exchange", null, "MAJOR", "NAGIOS", "Unit test message 3", "OPEN", "UP", firstinsertdate);
        transitionList = logMessageService.getHostStateTransitions("exchange", startDate, endDate, false);
        if (transitionList != null) {
            long sincefirststatechange = System.currentTimeMillis() - cal.getTime().getTime();
            // TODO: Enable this test after availability refactoring for 7.1.0.
            //assertEquals("Duration in state : ",5 * 3600 * 1000, transitionList.get(0).getDurationInState().longValue());
            //assertEquals("Duration in state : ",sincefirststatechange, transitionList.get(1).getDurationInState().longValue());

        } else {
            assertNotNull(transitionList);
        }


    }

    /**
     * Unit tests for Hoststate transitions.
     */
    public void testServiceStateTransitions() {
        Date currDate = Calendar.getInstance().getTime();
        SimpleDateFormat sdfStart = new SimpleDateFormat("MM/dd/yyyy 00:00:00");
        String startDate = sdfStart.format(currDate);
        SimpleDateFormat sdfEnd = new SimpleDateFormat("MM/dd/yyyy 23:59:59");
        String endDate = sdfEnd.format(currDate);
        List<StateTransition> transitionList = logMessageService.getServiceStateTransitions("nagios","network_users", startDate, endDate, false);
        assertNotNull(transitionList);
        assertEquals("Number of state transitions", 0, transitionList.size());
        // Set date to yesterday 10am
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        cal.set(Calendar.HOUR_OF_DAY, 10);
        cal.set(Calendar.MINUTE, 00);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date firstinsertdate = cal.getTime();
        // SCENARIO : 1
        this.createTestMessage("exchange", null, "MINOR", "NAGIOS", "Unit test message 1", "OPEN", "PENDING", firstinsertdate);
        transitionList = logMessageService.getServiceStateTransitions("nagios","network_users", startDate, endDate, false);
        assertEquals("Number of state transitions", 1, transitionList.size());
        if (transitionList != null) {
            for (StateTransition stateTransition : transitionList) {
                assertEquals("From status :", "PENDING", stateTransition.getFromStatus().getName());
                assertEquals("To status :", "PENDING", stateTransition.getToStatus().getName());
            }
        } else {
            assertNotNull(transitionList);
        }
        // First state change yesterday
        cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        cal.set(Calendar.HOUR_OF_DAY, 22);
        cal.set(Calendar.MINUTE, 00);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        firstinsertdate = cal.getTime();
        // SCENARIO : 2
        LogMessage msg = this.createTestMessage("exchange", null, "MAJOR", "NAGIOS", "Unit test message 2", "OPEN", "UNSCHEDULED CRITICAL", firstinsertdate);
        transitionList = logMessageService.getServiceStateTransitions("nagios","network_users", startDate, endDate, false);
        assertEquals("Number of state transitions", 1, transitionList.size());
        if (transitionList != null) {

            cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 00);
            cal.set(Calendar.MINUTE, 00);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long sinceMidnight = System.currentTimeMillis() - cal.getTime().getTime();
            // TODO: Enable this test after availability refactoring for 7.1.0.
            //assertEquals("Duration in state : ",sinceMidnight, transitionList.get(0).getDurationInState().longValue());

        } else {
            assertNotNull(transitionList);
        }
        // SCENARIO : 3
        // First state change today at 3am
        cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 3);
        cal.set(Calendar.MINUTE, 00);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        firstinsertdate = cal.getTime();
        msg.setFirstInsertDate(firstinsertdate);
        logMessageService.saveLogMessage(msg);
        transitionList = logMessageService.getServiceStateTransitions("nagios","network_users", startDate, endDate, false);
        if (transitionList != null) {
            // TODO: Enable this test after availability refactoring for 7.1.0.
            //assertEquals("Duration in state : ",sincelaststatechange, transitionList.get(0).getDurationInState().longValue());

        } else {
            assertNotNull(transitionList);
        }

        // SCENARIO : 4
        // First state change today at 3am
        cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        cal.set(Calendar.HOUR_OF_DAY, 22);
        cal.set(Calendar.MINUTE, 00);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        firstinsertdate = cal.getTime();
        msg = this.createTestMessage("exchange", null, "MAJOR", "NAGIOS", "Unit test message 3", "OPEN", "UP", firstinsertdate);
        transitionList = logMessageService.getServiceStateTransitions("nagios","network_users", startDate, endDate, false);
        if (transitionList != null) {
            long sincefirststatechange = System.currentTimeMillis() - cal.getTime().getTime();
            // TODO: Enable this test after availability refactoring for 7.1.0.
            //assertEquals("Duration in state : ",5 * 3600 * 1000, transitionList.get(0).getDurationInState().longValue());
            //assertEquals("Duration in state : ",sincefirststatechange, transitionList.get(1).getDurationInState().longValue());

        } else {
            assertNotNull(transitionList);
        }
    }

    private LogMessage createTestMessage(String hostname, String service, String severity, String applicationtype, String text, String opstatus, String monitorstatus, Date firstinsertdate) {
        Date currDate = Calendar.getInstance().getTime();
        LogMessage msg = logMessageService.createLogMessage();
        Host host = hostService.getHostByHostName(hostname);
        if (service != null && !service.equalsIgnoreCase("") && host != null) {
            com.groundwork.collage.model.ServiceStatus servicestatus = statusService.getServiceByDescription(service, host.getHostName());
            msg.setServiceStatus(servicestatus);
        }

        msg.setDevice(host.getDevice());
        msg.setHostStatus(host.getHostStatus());
        // Calculate the hash

        // Consolidation Hash values
        msg.setConsolidationHash(0);
        msg.setStatelessHash(0);

        Severity severityObj = metadataService
                .getSeverityByName(severity);

        msg.setApplicationType(metadataService
                .getApplicationTypeByName(applicationtype));
        msg.setTextMessage(text);
        msg.setSeverity(severityObj);
        msg.setApplicationSeverity(severityObj);

        msg.setLastInsertDate(currDate);
        msg.setFirstInsertDate(firstinsertdate);
        /* Set Reporting Date to current date */
        msg.setReportDate(currDate);


        msg.setComponent(metadataService.getComponentByName("UNDEFINED"));
        msg.setTypeRule(metadataService.getTypeRuleByName("UNDEFINED"));
        msg.setPriority(metadataService.getPriorityByName("1"));
        msg.setOperationStatus(metadataService
                .getOperationStatusByName(opstatus));
        msg.setMonitorStatus(metadataService.getMonitorStatusByName(monitorstatus));
        logMessageService.saveLogMessage(msg);
        return msg;

    }

	public void testGetLogMessagesByCriteria() {
		Map<String, String> builtInProperties = new HashMap<String, String>();
		builtInProperties.put(LogMessage.EP_MONITOR_STATUS_NAME, "CRITICAL");
		builtInProperties.put(LogMessage.EP_DEVICE_IDENTIFICATION,
				"192.168.1.100");
		builtInProperties.put(LogMessage.EP_APP_SEVERITY_NAME,
				"CRITICAL");
		builtInProperties.put(LogMessage.EP_TEXT_MESSAGE, "VMS_message_1");
		builtInProperties.put(LogMessage.EP_OPERATION_STATUS_NAME,
				"OPEN");
		builtInProperties.put(LogMessage.EP_REPORT_DATE, "2011-03-23 13:00:01");
		Map<String, String> dynaProperties = new HashMap<String, String>();
		dynaProperties.put("VMSEventID", "1234");
		dynaProperties.put("ObjectClass",	"CMS");
		dynaProperties.put("AdditionalInfo", "test");
		String preProcessFields = "Device,ObjectClass,AdditionalInfo";
		FilterCriteria criteria = this.buildCriteria(preProcessFields, builtInProperties, dynaProperties);
		List<LogMessage> logMessages = logMessageService.getLogMessagesByCriteria(criteria);
		assertEquals("Number of log messages", 1, logMessages.size());
	}

	/**
	 * Builds the criteria for the preProcessEventsByAppType
	 * 
	 * @param preProcessFields
	 * @param builtInProperties
	 * @param dynaProperties
	 * @return
	 */
	private FilterCriteria buildCriteria(String preProcessFields,
			Map<String, String> builtInProperties,
			Map<String, String> dynaProperties) {
		FilterCriteria criteria = null;

		// EntityProperty to Hibernate Property mappings
		Map<String, String> hibMap = new HashMap<String, String>();
		hibMap.put(LogMessage.EP_DEVICE_IDENTIFICATION,
				LogMessage.HP_DEVICE_IDENTIFICATION);
		hibMap.put(LogMessage.EP_APP_SEVERITY_NAME,
				LogMessage.HP_APP_SEVERITY_NAME);
		hibMap.put(LogMessage.EP_TEXT_MESSAGE, LogMessage.HP_TEXT_MESSAGE);
		hibMap.put(LogMessage.EP_OPERATION_STATUS_NAME,
				LogMessage.HP_OPERATION_STATUS_NAME);
		hibMap.put(LogMessage.EP_REPORT_DATE, LogMessage.HP_REPORT_DATE);
		hibMap.put(LogMessage.EP_MONITOR_STATUS_NAME,
				LogMessage.HP_MONITOR_STATUS_NAME);

		StringTokenizer stkn = new StringTokenizer(preProcessFields, ",");
		int criteriaCount = stkn.countTokens();
		int matchCount = 0;
		int dynamicPropCount = 0;
		while (stkn.hasMoreTokens()) {
			String field = stkn.nextToken();
			if (builtInProperties != null
					&& builtInProperties.containsKey(field)) {
				matchCount++;
				if (criteria == null)
					criteria = FilterCriteria.eq(hibMap.get(field),
							builtInProperties.get(field));
				else
					criteria.and(FilterCriteria.eq(hibMap.get(field),
							builtInProperties.get(field)));
			} // end if
			if (dynaProperties != null && dynaProperties.containsKey(field)) {
				matchCount++;
				// An exception for monitorstatus name as this field comes as
				// dynamic properties.
				if (field.equals(LogMessage.EP_MONITOR_STATUS_NAME)) {
					if (criteria == null)
						criteria = FilterCriteria.eq(field, builtInProperties
								.get(field));
					else
						criteria.and(FilterCriteria.eq(field, builtInProperties
								.get(field)));
				} // end if
				else {
					if (criteria == null) {
						criteria = FilterCriteria.eq("propertyValues.name"+ "_" + dynamicPropCount,
								field);
						criteria.and(FilterCriteria.eq(
								"propertyValues.valueString"+ "_" + dynamicPropCount, dynaProperties
										.get(field)));
					} else {
						criteria.and(FilterCriteria.eq(
								"propertyValues.name"+ "_" + dynamicPropCount, field));
						criteria.and(FilterCriteria.eq(
								"propertyValues.valueString"+ "_" + dynamicPropCount, dynaProperties
										.get(field)));
					} // end if
				} // end if
				dynamicPropCount++;
			} // end if
		} // end while
		if (matchCount == criteriaCount) {
			// if all the fields matches then append the open criteria
			criteria.and(FilterCriteria.eq(LogMessage.HP_OPERATION_STATUS_NAME,
					"OPEN"));
			return criteria;
		} // end if
		return null;
	}

    public void testSetDynamicProperty() {
        beginTransaction();
        try {
            // define property type
            metadataService.savePropertyType("TEST_PROPERTY", "testSetDynamicProperty", PropertyType.STRING);
            // create test message
            LogMessage logMessage = createTestMessage("exchange", null, "MAJOR", "NAGIOS", "testSetDynamicProperty",
                    "OPEN", "UP", new Date(System.currentTimeMillis() - 1000L));
            assertNotNull(logMessage);
            assertNull(logMessage.getProperty("TEST_PROPERTY"));
            // set dynamic property
            logMessage.setProperty("TEST_PROPERTY", "TEST_PROPERTY_VALUE");
            logMessageService.saveLogMessage(logMessage);
            // validate dynamic property
            flushAndClearSession();
            logMessage = logMessageService.getLogMessageById(logMessage.getLogMessageId());
            assertNotNull(logMessage);
            assertEquals("TEST_PROPERTY_VALUE", logMessage.getProperty("TEST_PROPERTY"));
            // remove dynamic property
            logMessage.setProperty("TEST_PROPERTY", null);
            assertNull(logMessage.getProperty("TEST_PROPERTY"));
            for (Map.Entry<String, Object> property : logMessage.getProperties(true).entrySet()) {
                assertFalse(property.getKey().equalsIgnoreCase("TEST_PROPERTY"));
            }
            logMessageService.saveLogMessage(logMessage);
            // validate dynamic property
            flushAndClearSession();
            logMessage = logMessageService.getLogMessageById(logMessage.getLogMessageId());
            assertNotNull(logMessage);
            assertNull(logMessage.getProperty("TEST_PROPERTY"));
            for (Map.Entry<String, Object> property : logMessage.getProperties(true).entrySet()) {
                assertFalse(property.getKey().equalsIgnoreCase("TEST_PROPERTY"));
            }
            // remove test message
            logMessageService.removeLogMessage(logMessage.getLogMessageId());
        } finally {
			metadataService.deletePropertyTypeByName("TEST_PROPERTY"); // clean out of metadata cache
            rollbackTransaction();
        }
    }
}
