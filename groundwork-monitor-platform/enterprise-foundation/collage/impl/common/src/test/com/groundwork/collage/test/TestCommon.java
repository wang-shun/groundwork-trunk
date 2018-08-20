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

import com.groundwork.collage.impl.AbstractDAO;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests CommonDAO implementation, which is used to add monitor to the system
 *
 * @author <a href="mailto:rruttimann@itgroundwork.com"> Roger Ruttimann</a>
 * @author <a href="mailto:pparavicini@itgroundwork.com">Philippe Paravicini</a>
 *
 * @version $Id: TestCommon.java 8692 2007-10-15 20:49:04Z glee $
 */
public class TestCommon extends TestCase {

    
    public TestCommon(String x) {
        super(x);
    }


    /** define the tests to be run in this class */
    public static Test suite()
    {
        TestSuite suite = new TestSuite();

		executeScript(false, "testdata/monitor-data.sql");

		// run all tests
        suite = new TestSuite(TestCommon.class);

        // or a subset thereoff
        //suite.addTest(new TestCommon("testCollageModelInterfaces"));
        //suite.addTest(new TestCommon("testIsAutoCreateUnknownProperties"));
        //suite.addTest(new TestCommon("testArrayToInClauseForTextColumn"));

        return suite;
    }
    public void setUp() throws Exception
    {
        super.setUp();
    }


    /** 
     * ensure that we can create implementations of all API entities through
     * the CollageFactory
     */
    public void testCollageModelInterfaces() throws Exception 
    {
        if (log.isInfoEnabled()) log.info("API initialization was successful. Check spring framework");

        String[] interfaces = new String[]{
            "FoundationProperties",
            "com.groundwork.collage.model.CheckType",
            "com.groundwork.collage.model.HostGroup",
            "com.groundwork.collage.model.MonitorStatus",
            "com.groundwork.collage.model.StateType",
            "com.groundwork.collage.model.Component",
            "com.groundwork.collage.model.Device",
            "com.groundwork.collage.model.Host",
            "com.groundwork.collage.model.HostStatus",
            "com.groundwork.collage.model.OperationStatus",
            "com.groundwork.collage.model.Severity",
            "com.groundwork.collage.model.LogMessage",
            "com.groundwork.collage.model.TypeRule",
            "com.groundwork.collage.model.MonitorServer",
            "com.groundwork.collage.model.ApplicationType",
            "com.groundwork.collage.model.EntityType",
            "com.groundwork.collage.model.PropertyType",
            "com.groundwork.collage.model.AuditLog"
        };
  
        Object instance = null;
        
        for (int i=0; i < interfaces.length; i++)
        {
            try
            {
                instance = collage.getAPIObject(interfaces[i]);
                assertTrue("implementation for " + interfaces[i], instance != null);

                if (log.isInfoEnabled()) 
                    log.info("Success. Implementation for interface " + interfaces[i] + " found.");
                
            } catch (Exception e)
            {
                String msg = "No implementation for interface " + interfaces[i] + " found.";
                log.error(msg, e);
                fail(msg);
            }
        }
    }

    public void testIsAutoCreateUnknownProperties()
    {
      assertFalse("AutoCreateUnknownProperties is false", collage.isAutoCreateUnknownProperties());
    }

    public void testArrayToInClauseForTextColumn()
    {
      String[] in = {"a","b","c"};

      assertEquals("In clause for text", 
          "('a','b','c')", AbstractDAO.arrayToInClauseForTextColumn(in));

      in = null;

      assertEquals("In clause for text", 
          "()", AbstractDAO.arrayToInClauseForTextColumn(in));
    }

}
