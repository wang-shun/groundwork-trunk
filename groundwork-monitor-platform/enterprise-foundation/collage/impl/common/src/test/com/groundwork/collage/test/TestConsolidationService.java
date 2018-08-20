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

import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.groundwork.foundation.bs.logmessage.ConsolidationService;

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.model.ConsolidationCriteria;

/**
 * @author rdandridge
 *
 */
public class TestConsolidationService extends AbstractTestCaseWithTransactionSupport 
{
	/* the following constants should reflect the state of test data */
	ConsolidationService consolidationService = null;
	
	public TestConsolidationService(String x) {
		super(x);
	}

	/** define the tests to be run in this class */
	public static Test suite()
	{
		TestSuite suite = new TestSuite();

		executeScript(false, "testdata/monitor-data.sql");

		// run all tests
		//suite = new TestSuite(TestConsolidationService.class);

		// or a subset thereoff
		suite.addTest(new TestConsolidationService("testGetConsolidationCriterias"));
		suite.addTest(new TestConsolidationService("testGetConsolidationCriteriaById"));
		suite.addTest(new TestConsolidationService("testGetConsolidationCriteriaByName"));
		suite.addTest(new TestConsolidationService("testCreateConsolidationCriteria"));
		suite.addTest(new TestConsolidationService("testSaveConsolidationCriteria"));
		suite.addTest(new TestConsolidationService("testDeleteConsolidationCriteriaById"));
		suite.addTest(new TestConsolidationService("testDeleteConsolidationCriteriaByName"));

		return suite;
	}

    public void setUp() throws Exception
    {
        super.setUp();
		
		// Retrieve logmessage business service
		consolidationService = collage.getConsolidationService();		
		assertNotNull(consolidationService);
	}

	public void testGetConsolidationCriterias()
	{
		startTime();
		Collection<ConsolidationCriteria> criterias = consolidationService.getConsolidationCriterias(null,null);
		outputElapsedTime("consolidationService.getConsolidationCriterias(null,null)");
		assertNotNull(criterias);
		assertEquals(criterias.size(), 2);
	}
	
	public void testGetConsolidationCriteriaById()
	{
		startTime();
		ConsolidationCriteria criteriaByName = consolidationService.getConsolidationCriteriaByName("SYSTEM");
		assertNotNull(criteriaByName);

		Integer criteriaId = criteriaByName.getConsolidationCriteriaId();
		startTime();
		ConsolidationCriteria criteriaById = consolidationService.getConsolidationCriteriaById(criteriaId.intValue());
		outputElapsedTime("consolidationService.getConsolidationCriteriaById(criteriaId.intValue())");
		assertNotNull(criteriaById);
		assertEquals(criteriaByName.getName(), criteriaById.getName());
		assertEquals(criteriaByName.getConsolidationCriteriaId(), criteriaById.getConsolidationCriteriaId());
	}
	
	public void testGetConsolidationCriteriaByName()
	{
		startTime();
		ConsolidationCriteria criteria = consolidationService.getConsolidationCriteriaByName("SYSTEM");
		outputElapsedTime("consolidationService.getConsolidationCriteriaByName(\"SYSTEM\")");
		assertNotNull(criteria);
		assertEquals(criteria.getName(),"SYSTEM");
	}
	
	public void testDeleteConsolidationCriteriaById()
	{
		startTime();
		ConsolidationCriteria criteria = consolidationService.getConsolidationCriteriaByName("SYSTEM");
		outputElapsedTime("consolidationService.getConsolidationCriteriaByName(\"SYSTEM\")");
		if (criteria == null) {
			criteria = consolidationService.createConsolidationCriteria("SYSTEM", "OperationStatus,Device,MonitorStatus,ApplicationType,TextMessage");
			consolidationService.saveConsolidationCriteria(criteria);
		}
		startTime();
		consolidationService.deleteConsolidationCriteriaById(criteria.getConsolidationCriteriaId());
		outputElapsedTime("consolidationService.deleteConsolidationCriteriaById(criteria.getConsolidationCriteriaId())");
		criteria = null;
		criteria = consolidationService.getConsolidationCriteriaByName("SYSTEM");
		assertNull(criteria);
	}
	
	public void testDeleteConsolidationCriteriaByName()
	{
		ConsolidationCriteria criteria = consolidationService.getConsolidationCriteriaByName("SYSTEM");
		if (criteria == null) {
			criteria = consolidationService.createConsolidationCriteria("SYSTEM", "OperationStatus,Device,MonitorStatus,ApplicationType,TextMessage");
			consolidationService.saveConsolidationCriteria(criteria);
		}
		consolidationService.deleteConsolidationCriteriaByName(criteria.getName());
		criteria = null;
		criteria = consolidationService.getConsolidationCriteriaByName("SYSTEM");
		assertNull(criteria);
	}
	
	public void testCreateConsolidationCriteria()
	{
		ConsolidationCriteria criteria = consolidationService.createConsolidationCriteria("TEST", "Severity,Priority,Device");
		assertTrue("Failed Creating Consolidation TEST", criteria instanceof ConsolidationCriteria);
		assertEquals("Consolidation name for  TEST", "TEST", criteria.getName());
	}
	
	public void testSaveConsolidationCriteria()
	{
		ConsolidationCriteria criteria = consolidationService.createConsolidationCriteria("TEST2", "Severity,Priority,Device");
		assertTrue("Failed Creating Consolidation TEST2", criteria instanceof ConsolidationCriteria);
		consolidationService.saveConsolidationCriteria(criteria);
		ConsolidationCriteria newCriteria = consolidationService.getConsolidationCriteriaByName(criteria.getName());
		assertNotNull(newCriteria);
		assertEquals(newCriteria.getName(), criteria.getName());
		
		consolidationService.saveConsolidationCriteria("TEST3", "OperationStatus,Severity,ApplicationType");
		criteria = null;
		criteria = consolidationService.getConsolidationCriteriaByName("TEST3");
		assertNotNull(criteria);
		assertEquals(criteria.getName(), "TEST3");
	}
}
	
