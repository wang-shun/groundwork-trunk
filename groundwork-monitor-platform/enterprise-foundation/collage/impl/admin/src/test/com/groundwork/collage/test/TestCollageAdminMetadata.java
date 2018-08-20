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

import com.groundwork.collage.CollageAdminMetadata;
import com.groundwork.collage.exception.CollageException;
import com.groundwork.collage.model.ApplicationType;
import com.groundwork.collage.model.Category;
import com.groundwork.collage.model.CheckType;
import com.groundwork.collage.model.Component;
import com.groundwork.collage.model.PropertyType;
import com.groundwork.collage.model.ServiceStatus;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.util.Arrays;
import java.util.Map;


/**
 * Tests the Service facade used to manipulate metadata in the system
 *
 * @author  <a href="mailto:pparavicini@itgroundwork.com">Philippe Paravicini</a>
 * @version $Revision: 8692 $ - $Date: 2007-10-15 13:49:04 -0700 (Mon, 15 Oct 2007) $
 *
 * @see CollageAdminMetadata
 */
public class TestCollageAdminMetadata extends AbstractTestAdminBase
{
	private static final String APP_TYPE_NAGIOS = "NAGIOS";
	private static final int NUM_APP_TYPES      = 26;
	private static final int NUM_ENTITY_TYPES   = 26;
	private static final int NUM_PROPERTY_TYPES = 63;

	public TestCollageAdminMetadata(String x) 
	{
		super(x);
	}


	/** define the tests to be run in this class */
	public static Test suite()
	{
		TestSuite suite = new TestSuite(TestCollageAdminMetadata.class);
        executeScript(false, "testdata/monitor-data.sql");

		// run all tests

		// or a subset thereoff
		//suite.addTest(new TestCollageAdminMetadata("testGetApplicationTypeNames"));
		//suite.addTest(new TestCollageAdminMetadata("testGetExtensibleEntityNames"));
		//suite.addTest(new TestCollageAdminMetadata("testGetPropertyTypeNames"));
		//suite.addTest(new TestCollageAdminMetadata("testCreateApplicationType"));
		//suite.addTest(new TestCollageAdminMetadata("testAssignUnassignPropertyType"));
		//suite.addTest(new TestCollageAdminMetadata("testCreateDeletePropertyTypeDate"));
		//suite.addTest(new TestCollageAdminMetadata("testCreateDeletePropertyTypeBoolean"));
		//suite.addTest(new TestCollageAdminMetadata("testCreateDeletePropertyTypeString"));
		//suite.addTest(new TestCollageAdminMetadata("testCreateDeletePropertyTypeInteger"));
		//suite.addTest(new TestCollageAdminMetadata("testCreateDeletePropertyTypeLong"));
		//suite.addTest(new TestCollageAdminMetadata("testCreateDeletePropertyTypeDouble"));
		//suite.addTest(new TestCollageAdminMetadata("testDeletePropertyType"));

		return suite;

		// or
		// TestSetup wrapper= new SomeWrapper(suite);
		// return wrapper;
	}

	/** executed prior to each test */
	public void setUp() {
        try {
            super.setUp();
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
    }



	public void testGetApplicationTypeNames()
	{
		String[] names = adminMeta.getApplicationTypeNames();
		Arrays.sort(names);
		assertEquals("number of app types", NUM_APP_TYPES, names.length);
		assertEquals("ARCHIVE", "ARCHIVE", names[0]);
		assertEquals("AUDIT", "AUDIT", names[1]);
		assertEquals("AWS", "AWS", names[2]);
	}


	public void testGetExtensibleEntityNames()
	{
		String[] names = adminMeta.getExtensibleEntityNames();
		Arrays.sort(names);
		assertEquals("number of extensible entities", NUM_ENTITY_TYPES, names.length);
		int i = 0;
		assertEquals(ApplicationType.ENTITY_TYPE_CODE, names[i++]);
		assertEquals(Category.ENTITY_TYPE_CODE, names[i++]);
		assertEquals(CheckType.ENTITY_TYPE_CODE, names[i++]);
		assertEquals(Component.ENTITY_TYPE_CODE, names[i++]);
	}


	public void testGetPropertyTypeNames()
	{
		String[] names = adminMeta.getPropertyTypeNames();
		assertEquals("number of property types", NUM_PROPERTY_TYPES, names.length);
	}


	public void testCreateApplicationType()
	{
		String APP_NAME  = "SEISMIC_MONITOR";
		String APP_DESCR = "Used to track seismic monitoring stations";

		Map allMeta = adminMeta.getAllMetadata();
		assertEquals("num of app types before transaction", NUM_APP_TYPES, allMeta.size());

		adminMeta.createApplicationType(APP_NAME, APP_DESCR);
		allMeta = adminMeta.getAllMetadata();
		assertEquals("num of app types after addition", NUM_APP_TYPES + 1, allMeta.size());

		ApplicationType appType = adminMeta.getApplicationType(APP_NAME);
		assertNotNull(APP_NAME + " app type not null", appType); 
		assertEquals(APP_NAME + " descr", APP_DESCR, appType.getDescription());

		// return the db to its prior state
		metadataService.deleteApplicationType(appType);

		allMeta = adminMeta.getAllMetadata();
		assertEquals("num of app types after rollback", NUM_APP_TYPES, allMeta.size());
	}


	public void testAssignUnassignPropertyType()
	{
		String APP_TYPE  = "NAGIOS";
		String ENT_TYPE  = ServiceStatus.ENTITY_TYPE_CODE;
		String PROP_TYPE = "isAcknowledged";
		PropertyType prop;

		prop = metadataService.getApplicationTypeByName(APP_TYPE).getPropertyType(ENT_TYPE, PROP_TYPE);
		assertNull(PROP_TYPE + " before PropertyType assignment", prop);

		adminMeta.assignPropertyType(APP_TYPE, ENT_TYPE, PROP_TYPE);

		prop = metadataService.getApplicationTypeByName(APP_TYPE).getPropertyType(ENT_TYPE, PROP_TYPE);
		assertNotNull(PROP_TYPE + " after PropertyType assignment", prop);

		// return the db to its prior state
		adminMeta.unassignPropertyType(APP_TYPE, ENT_TYPE, PROP_TYPE);
		prop = metadataService.getApplicationTypeByName(APP_TYPE).getPropertyType(ENT_TYPE, PROP_TYPE);
		assertNull("after rollback", prop);
	}


	public void testCreateDeletePropertyTypeDate()
	{
		String PROP_NAME  = "TestDateProperty";
		String PROP_DESCR = "Sample Date Property";
		String PROP_TYPE  = PropertyType.DATE;

		log.debug("loading property type names 1");
		String[] names = adminMeta.getPropertyTypeNames();
		assertEquals("number of property types before addition", NUM_PROPERTY_TYPES, names.length);

		adminMeta.createPropertyType(PROP_NAME, PROP_DESCR, PROP_TYPE);

		log.debug("loading property type names 2");
		names = adminMeta.getPropertyTypeNames();
		assertEquals("number of property types after addition", NUM_PROPERTY_TYPES + 1, names.length);
		
		PropertyType propType = metadataService.getPropertyTypeByName(PROP_NAME);
		assertNotNull(PROP_NAME + " prop type not null", propType); 
		assertEquals(PROP_NAME + " descr", PROP_DESCR, propType.getDescription());

		boolean safeDelete = true;
		adminMeta.deletePropertyType(PROP_NAME, safeDelete);
		
		log.debug("loading property type names 3");
		names = adminMeta.getPropertyTypeNames();
		assertEquals("number of property types after rollback", NUM_PROPERTY_TYPES, names.length);
	}


	public void testCreateDeletePropertyTypeBoolean()
	{
		String PROP_NAME  = "TestBooleanProperty";
		String PROP_DESCR = "Sample Boolean Property";
		String PROP_TYPE  = PropertyType.BOOLEAN;

		log.debug("loading property type names 1");
		String[] names = adminMeta.getPropertyTypeNames();
		assertEquals("number of property types before addition", NUM_PROPERTY_TYPES, names.length);

		adminMeta.createPropertyType(PROP_NAME, PROP_DESCR, PROP_TYPE);

		log.debug("loading property type names 2");
		names = adminMeta.getPropertyTypeNames();
		assertEquals("number of property types after addition", NUM_PROPERTY_TYPES + 1, names.length);

		PropertyType propType = metadataService.getPropertyTypeByName(PROP_NAME);
		assertNotNull(PROP_NAME + " prop type not null", propType); 
		assertEquals(PROP_NAME + " descr", PROP_DESCR, propType.getDescription());

		boolean safeDelete = true;
		adminMeta.deletePropertyType(PROP_NAME, safeDelete);
		
		log.debug("loading property type names 3");
		names = adminMeta.getPropertyTypeNames();
		assertEquals("number of property types after rollback", NUM_PROPERTY_TYPES, names.length);
	}


	public void testCreateDeletePropertyTypeString()
	{
		String PROP_NAME  = "TestStringProperty";
		String PROP_DESCR = "Sample String Property";
		String PROP_TYPE  = PropertyType.STRING;

		log.debug("loading property type names 1");
		String[] names = adminMeta.getPropertyTypeNames();
		assertEquals("number of property types before addition", NUM_PROPERTY_TYPES, names.length);

		adminMeta.createPropertyType(PROP_NAME, PROP_DESCR, PROP_TYPE);

		log.debug("loading property type names 2");
		names = adminMeta.getPropertyTypeNames();
		assertEquals("number of property types after addition", NUM_PROPERTY_TYPES + 1, names.length);

		PropertyType propType = metadataService.getPropertyTypeByName(PROP_NAME);
		assertNotNull(PROP_NAME + " prop type not null", propType); 
		assertEquals(PROP_NAME + " descr", PROP_DESCR, propType.getDescription());

		boolean safeDelete = true;
		adminMeta.deletePropertyType(PROP_NAME, safeDelete);
		
		log.debug("loading property type names 3");
		names = adminMeta.getPropertyTypeNames();
		assertEquals("number of property types after rollback", NUM_PROPERTY_TYPES, names.length);
	}


	public void testCreateDeletePropertyTypeInteger()
	{
		String PROP_NAME  = "TestIntegerProperty";
		String PROP_DESCR = "Sample Integer Property";
		String PROP_TYPE  = PropertyType.INTEGER;

		log.debug("loading property type names 1");
		String[] names = adminMeta.getPropertyTypeNames();
		assertEquals("number of property types before addition", NUM_PROPERTY_TYPES, names.length);

		adminMeta.createPropertyType(PROP_NAME, PROP_DESCR, PROP_TYPE);

		log.debug("loading property type names 2");
		names = adminMeta.getPropertyTypeNames();
		assertEquals("number of property types after addition", NUM_PROPERTY_TYPES + 1, names.length);

		PropertyType propType = metadataService.getPropertyTypeByName(PROP_NAME);
		assertNotNull(PROP_NAME + " prop type not null", propType); 
		assertEquals(PROP_NAME + " descr", PROP_DESCR, propType.getDescription());

		boolean safeDelete = true;
		adminMeta.deletePropertyType(PROP_NAME, safeDelete);

		log.debug("loading property type names 3");
		names = adminMeta.getPropertyTypeNames();
		assertEquals("number of property types after rollback", NUM_PROPERTY_TYPES, names.length);
	}


	public void testCreateDeletePropertyTypeLong()
	{
		String PROP_NAME  = "TestLongProperty";
		String PROP_DESCR = "Sample Long Property";
		String PROP_TYPE  = PropertyType.LONG;

		log.debug("loading property type names 1");
		String[] names = adminMeta.getPropertyTypeNames();
		assertEquals("number of property types before addition", NUM_PROPERTY_TYPES, names.length);

		adminMeta.createPropertyType(PROP_NAME, PROP_DESCR, PROP_TYPE);

		log.debug("loading property type names 2");
		names = adminMeta.getPropertyTypeNames();
		assertEquals("number of property types after addition", NUM_PROPERTY_TYPES + 1, names.length);

		PropertyType propType = metadataService.getPropertyTypeByName(PROP_NAME);
		assertNotNull(PROP_NAME + " prop type not null", propType); 
		assertEquals(PROP_NAME + " descr", PROP_DESCR, propType.getDescription());

		boolean safeDelete = true;
		adminMeta.deletePropertyType(PROP_NAME, safeDelete);

		log.debug("loading property type names 3");
		names = adminMeta.getPropertyTypeNames();
		assertEquals("number of property types after rollback", NUM_PROPERTY_TYPES, names.length);
	}


	public void testCreateDeletePropertyTypeDouble()
	{
		String PROP_NAME  = "TestDoubleProperty";
		String PROP_DESCR = "Sample Double Property";
		String PROP_TYPE  = PropertyType.DOUBLE;

		log.debug("loading property type names 1");
		String[] names = adminMeta.getPropertyTypeNames();
		assertEquals("number of property types before addition", NUM_PROPERTY_TYPES, names.length);

		adminMeta.createPropertyType(PROP_NAME, PROP_DESCR, PROP_TYPE);

		log.debug("loading property type names 2");
		names = adminMeta.getPropertyTypeNames();
		assertEquals("number of property types after addition", NUM_PROPERTY_TYPES + 1, names.length);

		PropertyType propType = metadataService.getPropertyTypeByName(PROP_NAME);
		assertNotNull(PROP_NAME + " prop type not null", propType); 
		assertEquals(PROP_NAME + " descr", PROP_DESCR, propType.getDescription());

		boolean safeDelete = true;
		adminMeta.deletePropertyType(PROP_NAME, safeDelete);


		log.debug("loading property type names 3");
		names = adminMeta.getPropertyTypeNames();
		assertEquals("number of property types after rollback", NUM_PROPERTY_TYPES, names.length);
	}


	/** 
	 * this tests both the getPropertyTypeAssignment property and the deletion
	 * property, the horror! 
	 * TODO: part of this should be added to the testAssignUnassignPropertyType
	 * test or to its own test
	 */
	public void testDeletePropertyType()
	{
		String PROP_NAME  = "TestDeleteProperty";
		String PROP_DESCR = "Sample Double Property";
		String PROP_TYPE  = PropertyType.DOUBLE;

		String ENT1 = "HOST_STATUS";
		String ENT2 = "SERVICE_STATUS";

		String[] names = adminMeta.getPropertyTypeNames();

		adminMeta.createPropertyType(PROP_NAME, PROP_DESCR, PROP_TYPE);

		adminMeta.assignPropertyType(APP_TYPE_NAGIOS, ENT1, PROP_NAME);
		adminMeta.assignPropertyType(APP_TYPE_NAGIOS, ENT2, PROP_NAME);

		// making the same assignment twice should not break anything
		adminMeta.assignPropertyType(APP_TYPE_NAGIOS, ENT2, PROP_NAME);

		assertEquals(PROP_NAME + " assignment count", 2, adminMeta.getPropertyTypeAssignments(PROP_NAME).length);

		// test deletion per se
		try {
			adminMeta.deletePropertyType(PROP_NAME, true);
			fail("safeDelete did not prevent PropertyType deletion");
		}
		catch (CollageException e) {
			log.info("the failure to delete PropertyType " + PROP_NAME + " was expected");
		}

		assertEquals(PROP_NAME + " assignment count", 2, adminMeta.getPropertyTypeAssignments(PROP_NAME).length);
		names = adminMeta.getPropertyTypeNames();
		assertEquals("number of property types after safe delete", NUM_PROPERTY_TYPES+1, names.length);

		adminMeta.unassignPropertyType(APP_TYPE_NAGIOS, ENT2, PROP_NAME);
		adminMeta.unassignPropertyType(APP_TYPE_NAGIOS, ENT1, PROP_NAME);
		adminMeta.deletePropertyType(PROP_NAME, false);

		assertEquals(PROP_NAME + " assignment count", 0, adminMeta.getPropertyTypeAssignments(PROP_NAME).length);
		names = adminMeta.getPropertyTypeNames();
		assertEquals("number of property types after forced delete", NUM_PROPERTY_TYPES, names.length);
	}

} // end class TestCollageAdminMetadata

