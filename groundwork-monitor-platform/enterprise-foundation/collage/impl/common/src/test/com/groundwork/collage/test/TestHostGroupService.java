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


import com.groundwork.collage.model.HostGroup;
import com.groundwork.collage.util.Autocomplete;
import com.groundwork.collage.util.AutocompleteName;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.groundwork.foundation.bs.hostgroup.HostGroupService;
import org.groundwork.foundation.dao.FoundationQueryList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @author rdandridge
 *
 */
public class TestHostGroupService extends AbstractTestCaseWithTransactionSupport 
{
	private HostGroupService hostGroupService = null;
    private Autocomplete hostGroupAutocompleteService = null;

    /* the following constants should reflect the state of test data */
	public static final String HOSTGROUPNAME_1 = "demo-system";
	public static final String HOSTGROUPNAME_2 = "Email";
	public static final String HOSTGROUPNAME_3 = "Storage";
	public static final String HOSTGROUPNAME_4 = "Email_Atlanta";
	public static final String HOSTGROUPNAME_5 = "Email_New_York";
	public static final String HOSTGROUPNAME_6 = "Email_Miami";
    public static final String HOSTGROUPNAME_7 = "Email_Minneapolis";

	public TestHostGroupService(String x) {
		super(x);
	}

	/** define the tests to be run in this class */
	public static Test suite()
	{
		TestSuite suite = new TestSuite();

		executeScript(false, "testdata/monitor-data.sql");

		// run all tests
		//suite = new TestSuite(TestHostGroupService.class);

		// or a subset thereoff
		suite.addTest(new TestHostGroupService("testGetHostGroups"));
		suite.addTest(new TestHostGroupService("testGetHostGroupByName"));
		suite.addTest(new TestHostGroupService("testGetHostGroupById"));
		suite.addTest(new TestHostGroupService("testDeleteHostGroupByName"));
		suite.addTest(new TestHostGroupService("testDeleteHostGroupById"));
		suite.addTest(new TestHostGroupService("testDeleteHostGroup"));
		suite.addTest(new TestHostGroupService("testCreateHostGroup"));
		suite.addTest(new TestHostGroupService("testSaveHostGroup"));
        suite.addTest(new TestHostGroupService("testAutocomplete"));

		return suite;
	}

    public void setUp() throws Exception
    {
        super.setUp();
		
		// Retrieve host group business service
		hostGroupService = collage.getHostGroupService();		
		assertNotNull(hostGroupService);

        hostGroupAutocompleteService = collage.getHostGroupAutocompleteService();
        assertNotNull(hostGroupAutocompleteService);
	}
	
	public void testGetHostGroups()
	{
		FoundationQueryList hostgroups = hostGroupService.getHostGroups(null, null, -1, -1);
		assertNotNull(hostgroups);
		assertEquals("Count of Hostgroups", 14, hostgroups.size());
		
		// TODO: more complete testing of getHostGroups
	}
	
	public void testGetHostGroupByName()
	{
		HostGroup hostGroup = hostGroupService.getHostGroupByName(HOSTGROUPNAME_1);
		assertNotNull(hostGroup);
		assertEquals("Hostgroup name passed = retrieved",HOSTGROUPNAME_1, hostGroup.getName());
	}
	
	public void testGetHostGroupById()
	{
		HostGroup hostGroupByName = hostGroupService.getHostGroupByName(HOSTGROUPNAME_1);
		assertNotNull(hostGroupByName);
		HostGroup hostGroupById = hostGroupService.getHostGroupById(hostGroupByName.getHostGroupId().intValue());
		assertNotNull(hostGroupById);
		assertEquals("Hostgroup retrieved = hostgroup asked for:",hostGroupById.getHostGroupId().intValue(), hostGroupByName.getHostGroupId().intValue());
	}
	
	public void testDeleteHostGroupByName()
	{
		hostGroupService.deleteHostGroupByName(HOSTGROUPNAME_1);
		HostGroup hostGroupByName = hostGroupService.getHostGroupByName(HOSTGROUPNAME_1);
		assertNull(hostGroupByName);
	}
	public void testDeleteHostGroupById()
	{
		HostGroup hostGroupByName = hostGroupService.getHostGroupByName(HOSTGROUPNAME_2);
		assertNotNull(hostGroupByName);
		hostGroupService.deleteHostGroupById(hostGroupByName.getHostGroupId().intValue());
		hostGroupByName = null;
		hostGroupByName = hostGroupService.getHostGroupByName(HOSTGROUPNAME_2);
		assertNull(hostGroupByName);
	}
	
	public void testDeleteHostGroup()
	{
		// test delete on a single hostgroup
		HostGroup hostGroup = hostGroupService.getHostGroupByName(HOSTGROUPNAME_3);
		assertNotNull(hostGroup);
		hostGroupService.deleteHostGroup(hostGroup);
		hostGroup = null;
		hostGroup = hostGroupService.getHostGroupByName(HOSTGROUPNAME_3);
		assertNull(hostGroup);
		
		// test delete on a hostgroup collection
		ArrayList<HostGroup> hostGroupList = new ArrayList<HostGroup>();
		HostGroup hg1 = hostGroupService.getHostGroupByName(HOSTGROUPNAME_4);
		assertNotNull(hg1);
		hostGroupList.add(hg1);
		HostGroup hg2 = hostGroupService.getHostGroupByName(HOSTGROUPNAME_5);
		assertNotNull(hg2);
		hostGroupList.add(hg2);
		HostGroup hg3 = hostGroupService.getHostGroupByName(HOSTGROUPNAME_6);
		assertNotNull(hg3);
		hostGroupList.add(hg3);
		hostGroupService.deleteHostGroup(hostGroupList);
		hg1 = null;
		hg1 = hostGroupService.getHostGroupByName(HOSTGROUPNAME_4);
		assertNull(hg1);
		hg2 = null;
		hg2 = hostGroupService.getHostGroupByName(HOSTGROUPNAME_5);
		assertNull(hg2);
		hg3 = null;
		hg3 = hostGroupService.getHostGroupByName(HOSTGROUPNAME_6);
		assertNull(hg3);
	}
	
	public void testCreateHostGroup()
	{
		HostGroup emptyHostGroup = hostGroupService.createHostGroup();
		assertNotNull(emptyHostGroup);
		assertEquals(emptyHostGroup.getHosts(), new HashSet());
		assertEquals(emptyHostGroup.getName(), null);
		
		HostGroup newHostGroup = hostGroupService.createHostGroup("TestHostGroup");
		assertNotNull(newHostGroup);
		assertEquals(newHostGroup.getName(),"TestHostGroup");
	}
	
	public void testSaveHostGroup()
	{
		HostGroup newHostGroup = hostGroupService.createHostGroup("TestHostGroup");
		assertNotNull(newHostGroup);
		hostGroupService.saveHostGroup(newHostGroup);
		HostGroup hostGroup = hostGroupService.getHostGroupByName("TestHostGroup");
		assertNotNull(hostGroup);
		
		HostGroup emptyHostGroup = hostGroupService.createHostGroup();
		assertNotNull(emptyHostGroup);
		try {
			hostGroupService.saveHostGroup(emptyHostGroup);
			fail("No exception thrown when saving an invalid hostgroup");
		}
		catch(Exception e)
		{
			// expected an exception
		}
		
		// now do it the right way
		/*emptyHostGroup.setName("EmptyHostGroup");
		hostGroupService.saveHostGroup(emptyHostGroup)*/;
		
		ArrayList<HostGroup> hostGroupList = new ArrayList<HostGroup>();
		HostGroup hg1 = hostGroupService.createHostGroup("test1");
		assertNotNull(hg1);
		hostGroupList.add(hg1);
		HostGroup hg2 = hostGroupService.createHostGroup("test2");
		assertNotNull(hg2);
		hostGroupList.add(hg2);
		HostGroup hg3 = hostGroupService.createHostGroup("test3");
		assertNotNull(hg3);
		hostGroupList.add(hg3);
		hostGroupService.saveHostGroup(hostGroupList);
		hg1 = null;
		hg1 = hostGroupService.getHostGroupByName("test1");
		assertNotNull(hg1);
		hg2 = null;
		hg2 = hostGroupService.getHostGroupByName("test2");
		assertNotNull(hg2);
		hg3 = null;
		hg3 = hostGroupService.getHostGroupByName("test3");
		assertNotNull(hg3);
	}

    public void testAutocomplete() throws Exception {
        // wait for initial load
        Thread.sleep(250);
        // test autocomplete names
        List<AutocompleteName> names = hostGroupAutocompleteService.autocomplete(HOSTGROUPNAME_7);
        assertNotNull(names);
        assertEquals(1, names.size());
        assertEquals(this.HOSTGROUPNAME_7, names.get(0).getName());
        // create host group
        try {
            HostGroup hostGroup = hostGroupService.createHostGroup(HOSTGROUPNAME_7+"-2");
            hostGroupService.saveHostGroup(hostGroup);
            // wait for refresh and validate names
            Thread.sleep(250);
            names = hostGroupAutocompleteService.autocomplete(HOSTGROUPNAME_7);
            assertNotNull(names);
            assertEquals(2, names.size());
            assertEquals(HOSTGROUPNAME_7, names.get(0).getName());
            assertEquals(HOSTGROUPNAME_7+"-2", names.get(1).getName());
        } finally {
            // cleanup test objects
            hostGroupService.deleteHostGroupByName(HOSTGROUPNAME_7+"-2");
        }
        // wait for refresh and validate names
        Thread.sleep(250);
        names = hostGroupAutocompleteService.autocomplete(HOSTGROUPNAME_7);
        assertNotNull(names);
        assertEquals(1, names.size());
        assertEquals(this.HOSTGROUPNAME_7, names.get(0).getName());
    }
}
