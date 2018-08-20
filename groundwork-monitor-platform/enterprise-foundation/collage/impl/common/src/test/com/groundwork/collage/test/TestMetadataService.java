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

import com.groundwork.collage.model.ApplicationType;
import com.groundwork.collage.model.CheckType;
import com.groundwork.collage.model.Component;
import com.groundwork.collage.model.EntityType;
import com.groundwork.collage.model.LogMessage;
import com.groundwork.collage.model.MonitorStatus;
import com.groundwork.collage.model.OperationStatus;
import com.groundwork.collage.model.Priority;
import com.groundwork.collage.model.PropertyType;
import com.groundwork.collage.model.Severity;
import com.groundwork.collage.model.StateType;
import com.groundwork.collage.model.TypeRule;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.groundwork.foundation.bs.exception.BusinessServiceException;
import org.groundwork.foundation.bs.metadata.MetadataService;
import org.groundwork.foundation.dao.FilterCriteria;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.dao.MatchType;
import org.groundwork.foundation.dao.SortCriteria;

import java.util.Collection;

/**
 * @author glee
 *
 */
public class TestMetadataService extends AbstractTestCaseWithTransactionSupport
{
	private static final String NEW_APP_TYPE_NAME = "NewApplicationType";
	private static final String NEW_APP_TYPE_DESCRIPTION = "NewApplicationType Description";
	
	private MetadataService metadataService = null;
	
	public TestMetadataService(String x) {
		super(x);
	}
	
	/** define the tests to be run in this class */
	public static Test suite()
	{
		TestSuite suite = new TestSuite();

		executeScript(false, "testdata/monitor-data.sql");

		// run all tests
		suite = new TestSuite(TestMetadataService.class);

		// or a subset thereoff
		//suite.addTest(new TestMetadataService(""));
    
		return suite;
	}

    public void setUp() throws Exception
    {
        super.setUp();
		
		// Retrieve business service
		metadataService = collage.getMetadataService();		
		assertNotNull(metadataService);		
	}
	
	public void testCreateApplicationType() throws BusinessServiceException
	{
		// Create application type
		ApplicationType appType = metadataService.createApplicationType();
		assertNotNull(appType);
		
		appType.setName(NEW_APP_TYPE_NAME);
		appType.setDescription(NEW_APP_TYPE_DESCRIPTION);
		
		// Save Application Type
		metadataService.saveApplicationType(appType);
		
		// Get newly created Application Type
		appType = metadataService.getApplicationTypeByName(NEW_APP_TYPE_NAME);
		assertNotNull(appType);
		assertEquals(NEW_APP_TYPE_DESCRIPTION, appType.getDescription());
		
		// Delete Application Type
		metadataService.deleteApplicationType(appType);
		
		// Make sure application type has been deleted
		appType = metadataService.getApplicationTypeByName(NEW_APP_TYPE_NAME);
		assertNull(appType);
		
		///////////////////////////////////////////////////////////////////////
		// Create Application Type (String name, String Description)
		///////////////////////////////////////////////////////////////////////
		
		appType = metadataService.createApplicationType(NEW_APP_TYPE_NAME, NEW_APP_TYPE_DESCRIPTION);
		assertNotNull(appType);
				
		// Save Application Type
		metadataService.saveApplicationType(appType);
		
		// Get newly created Application Type
		appType = metadataService.getApplicationTypeById(appType.getApplicationTypeId().intValue());
		assertNotNull(appType);
		assertEquals(NEW_APP_TYPE_NAME, appType.getName());
		
		// Delete Application Type by id
		int appTypeId = appType.getApplicationTypeId().intValue();
		metadataService.deleteApplicationTypeById(appTypeId);
		
		// Make sure application type has been deleted
		appType = metadataService.getApplicationTypeById(appTypeId);
		assertNull(appType);	
		
		///////////////////////////////////////////////////////////////////////
		// Save Application Type (String name, String Description)
		///////////////////////////////////////////////////////////////////////
		
		appType = metadataService.saveApplicationType(NEW_APP_TYPE_NAME, NEW_APP_TYPE_DESCRIPTION);
		assertNotNull(appType);
		
		// Get newly created Application Type
		appType = metadataService.getApplicationTypeById(appType.getApplicationTypeId().intValue());
		assertNotNull(appType);
		assertEquals(NEW_APP_TYPE_NAME, appType.getName());
		
		// Delete Application Type by id
		appTypeId = appType.getApplicationTypeId().intValue();
		metadataService.deleteApplicationTypeById(appTypeId);
		
		// Make sure application type has been deleted
		appType = metadataService.getApplicationTypeById(appTypeId);
		assertNull(appType);

        // Attempt to delete existing utilized application type
        assertNotNull("NAGIOS exists", metadataService.getApplicationTypeByName("NAGIOS"));
        // disable hibernate log4j logging
        disableHibernateLogging();
        try {
            metadataService.deleteApplicationTypeByName("NAGIOS");
            fail("NAGIOS delete succeeded when it should have failed on constraints");
        } catch (Exception e) {
        }
        // enable hibernate log4j logging
        reenableHibernateLogging();
        assertNotNull("NAGIOS should exist after failed delete", metadataService.getApplicationTypeByName("NAGIOS"));
	}

	public void testGetApplicationTypes() throws BusinessServiceException
	{
		SortCriteria sortCriteria = SortCriteria.asc(MetadataService.PROP_NAME);
		FoundationQueryList results = metadataService.getApplicationTypes(
				null, 
				sortCriteria, 
				-1, 
				-1);
		assertNotNull(results);
		
		// NOTE:  We are checking for greater than 2 b/c the professional version
		// adds application types in the seed data (syslog, snmp).
		assertTrue((results.size() >= 2));

		// Search for application type which start with NAG
		FilterCriteria filterCriteria = FilterCriteria.like(MetadataService.PROP_NAME,
															"NAG", 
															MatchType.START);
		
		results = metadataService.getApplicationTypes(filterCriteria, 
															null, 
															-1, 
															-1);
		assertNotNull(results);
		assertEquals(1, results.size());
	}

	public void testGetCheckTypeValues() throws BusinessServiceException
	{
		Collection<CheckType> values = metadataService.getCheckTypeValues();
		assertNotNull(values);
		assertEquals(2, values.size());
	}

	public void testGetComponentValues() throws BusinessServiceException
	{
		Collection<Component> values = metadataService.getComponentValues();
		assertNotNull(values);
		assertEquals(4, values.size());
	}

	public void testGetEntityTypes() throws BusinessServiceException
	{
		EntityType entityType = metadataService.getEntityTypeById(1);
		assertNotNull(entityType);
		assertEquals("HOST_STATUS", entityType.getName());
		
		entityType = metadataService.getEntityTypeByName("DEVICE");
		assertNotNull(entityType);
		assertEquals(4, entityType.getEntityTypeId().intValue());	
		
		FilterCriteria filterCriteria = FilterCriteria.ilike(MetadataService.PROP_NAME, 
																"statUs", 
																MatchType.END);
		SortCriteria sortCriteria = SortCriteria.asc(MetadataService.PROP_NAME);
		
		FoundationQueryList results = metadataService.getEntityTypes(filterCriteria, sortCriteria, -1, -1);
		assertNotNull(results);
		assertEquals(4, results.size());
		assertEquals("MONITOR_STATUS", ((EntityType)(results.get(1))).getName());
	}

	public void testGetMonitorStatus() throws BusinessServiceException
	{
		MonitorStatus monitorStatus = metadataService.getMonitorStatusByName("DOWN");
		assertNotNull(monitorStatus);
		assertEquals("DOWN", monitorStatus.getName());
		
		Collection<MonitorStatus> list = metadataService.getMonitorStatusValues();
		assertNotNull(list);
		assertEquals(26, list.size());
	}

	public void testGetOperationStatusValues() throws BusinessServiceException
	{
		Collection<OperationStatus> list = metadataService.getOperationStatusValues();
		assertNotNull(list);
		assertEquals(5, list.size());
	}

	public void testGetPriorityValues() throws BusinessServiceException
	{
		Collection<Priority> list = metadataService.getPriorityValues();
		assertNotNull(list);
		assertEquals(10, list.size());
	}

	public void testGetPropertyType() throws BusinessServiceException
	{
		// Get PropertyType by id - Note, This id is generated and is order dependent
		// and property type are added with seed data for Foundation so we are not checking
		// the actual PropertyType returned just that one is returned.
		PropertyType propertyType = metadataService.getPropertyTypeById(1);
		assertNotNull(propertyType);
		
		// Get Property Type By Name
		propertyType = metadataService.getPropertyTypeByName("isHostFlapping");
		assertNotNull(propertyType);
		assertEquals("isHostFlapping", propertyType.getName());
		
		// Retrieve property type by filter and sort
		FilterCriteria filterCriteria = FilterCriteria.like("name", "Acknowledge", MatchType.START);
		SortCriteria sortCriteria = SortCriteria.asc("name");
		
		FoundationQueryList results = metadataService.getPropertyTypes(filterCriteria, sortCriteria, -1, -1);
		assertNotNull(results);
		assertEquals(2, results.size());
		assertEquals("AcknowledgeComment", ((PropertyType)(results.get(0))).getName());
	}

	public void testGetSeverityValues() throws BusinessServiceException
	{
		Collection<Severity> list = metadataService.getSeverityValues();
		assertNotNull(list);
		assertEquals(26, list.size());
	}

	public void testGetStateTypeValues() throws BusinessServiceException
	{
		Collection<StateType> list = metadataService.getStateTypeValues();
		assertNotNull(list);
		assertEquals(3, list.size());
	}

	public void testGetTypeRuleValues() throws BusinessServiceException
	{
		Collection<TypeRule> list = metadataService.getTypeRuleValues();
		assertNotNull(list);
		assertEquals(9, list.size());
	}

	public void testIsBuiltInProperty(String entityType, String propertyTypeName) throws BusinessServiceException
	{
		boolean bIsBuiltIn = metadataService.isBuiltInProperty(LogMessage.ENTITY_TYPE_CODE, LogMessage.EP_TEXT_MESSAGE);
		assertTrue(bIsBuiltIn);
		
		bIsBuiltIn = metadataService.isBuiltInProperty(LogMessage.ENTITY_TYPE_CODE, LogMessage.KEY_MONITOR_SERVER);
		assertFalse(bIsBuiltIn);		
	}
}
