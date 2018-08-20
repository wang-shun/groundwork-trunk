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

import java.util.HashSet;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.groundwork.collage.CollageAdminInfrastructure;
import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.util.Nagios;

/**
 ***************************************************************************************************
 *
 *
 * @author  <a href="mailto:philippe.paravicini@eCommerceStudio.com">Philippe Paravicini</a>
 * @version $Revision: 19301 $ - $Date: 2012-08-10 16:47:46 -0700 (Fri, 10 Aug 2012) $
 *
 * @see
 ***************************************************************************************************
 */
public class ProfileAdmin extends AbstractTestCaseWithTransactionSupport
{
	private CollageFactory collage    = null;
	private CollageAdminInfrastructure   admin      = null;

	public ProfileAdmin(String x) { 
		super(x); 
		collage    = CollageFactory.getInstance();
		admin      = (CollageAdminInfrastructure)collage.getAPIObject(CollageFactory.ADMIN_SERVICE);
	}

	/** define the tests to be run in this class */
	public static Test suite()
	{
		TestSuite suite = new TestSuite();

		// run all tests
		suite = new TestSuite(ProfileAdmin.class);

		// or a subset thereoff
		//suite.addTest(new ProfileAdmin("testUpdateHostStatus"));
		//suite.addTest(new ProfileAdmin("testUpdateHostStatusNagios"));
		//suite.addTest(new ProfileAdmin("testUpdateServiceStatus"));
		//suite.addTest(new ProfileAdmin("testUpdateServiceStatusInBulk"));
		//suite.addTest(new ProfileAdmin("testUpdateLogMessage"));

		return suite;

		// or
		// TestSetup wrapper= new SomeWrapper(suite);
		// return wrapper;
	}

	/** executed prior to each test */
	protected void setUp() { 
		//beginTransaction();
	}

	/** executed after each test */
	protected void tearDown() { 
		//rollbackTransaction();
	}

	public void testUpdateHostStatus()
	{
		admin.updateHostStatus("INIT_MONITOR",Nagios.APPLICATION_TYPE,"localhost","127.0.0.1", Nagios.createHostStatusProps("PING OK - Packet loss = 0%, RTA = 0.06 ms ","UP", "2005-03-09 22:11:47","2005-03-08 11:25:39","0","85120","6720", "0","0","0","1","1","1","1","0","0.00","0","1","1","ACTIVE","0","0","0","1.0"));
		admin.updateHostStatus("MONTREUX",Nagios.APPLICATION_TYPE,"VEVEY","192.168.2.49",Nagios.createHostStatusProps("PING OK - Packet loss = 0%, RTA = 0.06 ms ","UP", "2005-03-09 22:11:47","2005-03-08 11:25:39","0","85120","6720", "0","0","0","1","1","1","1","0","0.00","0","1","1","ACTIVE","0","0","0","1.0"));
		admin.updateHostStatus("EVIAN",Nagios.APPLICATION_TYPE,"AIX","192.168.2.50",Nagios.createHostStatusProps("PING OK - Packet loss = 0%, RTA = 0.06 ms ","UP", "2005-03-09 22:11:47","2005-03-08 11:25:39","0","85120","6720", "0","0","0","1","1","1","1","0","0.00","0","1","1","ACTIVE","0","0","0","1.0"));
		admin.updateHostStatus("EVIAN",Nagios.APPLICATION_TYPE,"AIX2","192.168.2.50",Nagios.createHostStatusProps("PING OK - Packet loss = 0%, RTA = 0.06 ms ","UP", "2005-03-09 22:11:47","2005-03-08 11:25:39","0","85120","6720", "0","0","0","1","1","1","1","0","0.00","0","1","1","ACTIVE","0","0","0","1.0"));
		admin.updateHostStatus("MONTREUX",Nagios.APPLICATION_TYPE,"VEVEY","192.168.2.49",Nagios.createHostStatusProps("PING OK - Packet loss = 0%, RTA = 0.06 ms ","UP", "2005-03-09 22:16:47","2005-03-08 11:25:39","0","85120","6720", "0","0","0","1","1","1","1","0","0.00","0","1","1","ACTIVE","0","0","0","1.0"));
		admin.updateHostStatus("EVIAN",Nagios.APPLICATION_TYPE,"AIX","192.168.2.50",Nagios.createHostStatusProps("PING OK - Packet loss = 0%, RTA = 0.06 ms ","UP", "2005-03-09 22:16:47","2005-03-08 11:25:39","0","85120","6720", "0","0","0","1","1","1","1","0","0.00","0","1","1","ACTIVE","0","0","0","1.0"));
		admin.updateHostStatus("EVIAN",Nagios.APPLICATION_TYPE,"AIX2","192.168.2.50",Nagios.createHostStatusProps("PING OK - Packet loss = 0%, RTA = 0.06 ms ","UP", "2005-03-09 22:16:47","2005-03-08 11:25:39","0","85120","6720", "0","0","0","1","1","1","1","0","0.00","0","1","1","ACTIVE","0","0","0","1.0"));
	}

	/** tests backward compatibility with methods where status values were enumerated */
	public void testUpdateHostStatusNagios()
	{
		admin.updateHostStatus("INIT_MONITOR","INIT_HOST","192.168.2.1","PING OK - Packet loss = 0%, RTA = 0.06 ms ","UP", "2005-03-09 22:11:47","2005-03-08 11:25:39","0","85120","6720", "0","0","0","1","1","1","1","0","0.00","0","1","1","ACTIVE","0","0","0","1.0");
		admin.updateHostStatus("MONTREUX","VEVEY","192.168.2.49","PING OK - Packet loss = 0%, RTA = 0.06 ms ","UP", "2005-03-09 22:11:47","2005-03-08 11:25:39","0","85120","6720", "0","0","0","1","1","1","1","0","0.00","0","1","1","ACTIVE","0","0","0","1.0");
		admin.updateHostStatus("EVIAN","AIX","192.168.2.50","PING OK - Packet loss = 0%, RTA = 0.06 ms ","UP", "2005-03-09 22:11:47","2005-03-08 11:25:39","0","85120","6720", "0","0","0","1","1","1","1","0","0.00","0","1","1","ACTIVE","0","0","0","1.0");
		admin.updateHostStatus("EVIAN","AIX2","192.168.2.50","PING OK - Packet loss = 0%, RTA = 0.06 ms ","UP", "2005-03-09 22:11:47","2005-03-08 11:25:39","0","85120","6720", "0","0","0","1","1","1","1","0","0.00","0","1","1","ACTIVE","0","0","0","1.0");
		admin.updateHostStatus("MONTREUX","VEVEY","192.168.2.49","PING OK - Packet loss = 0%, RTA = 0.06 ms ","UP", "2005-03-09 22:16:47","2005-03-08 11:25:39","0","85120","6720", "0","0","0","1","1","1","1","0","0.00","0","1","1","ACTIVE","0","0","0","1.0");
		admin.updateHostStatus("EVIAN","AIX","192.168.2.50","PING OK - Packet loss = 0%, RTA = 0.06 ms ","UP", "2005-03-09 22:16:47","2005-03-08 11:25:39","0","85120","6720", "0","0","0","1","1","1","1","0","0.00","0","1","1","ACTIVE","0","0","0","1.0");
		admin.updateHostStatus("EVIAN","AIX2","192.168.2.50","PING OK - Packet loss = 0%, RTA = 0.06 ms ","UP", "2005-03-09 22:16:47","2005-03-08 11:25:39","0","85120","6720", "0","0","0","1","1","1","1","0","0.00","0","1","1","ACTIVE","0","0","0","1.0");
	}

	public void testUpdateServiceStatus()
	{
		admin.updateServiceStatus("INIT_MONITOR",Nagios.APPLICATION_TYPE,"INIT_HOST","192.168.2.1",Nagios.createServiceStatusProps("init_test_serv","LastPlugin out", "OK","1","HARD","2005-05-05 12:00:00", "2005-05-05 12:01:00", "ACTIVE", "1", "1","1","2005-01-01 12:00:00", "0", "OK", "111", "0","0","0",  "2005-01-01 12:12:12", "1","1","1","5","0","0","0.0","1","1","1","0","1.0"));
		admin.updateServiceStatus("NAGIOS-MAIN",Nagios.APPLICATION_TYPE,"VEVEY","192.168.2.49",Nagios.createServiceStatusProps("vevey_serv_1","LastPlugin out", "OK","1","HARD","2005-05-05 12:00:00", "2005-05-05 12:01:00", "ACTIVE", "1", "1","1","2005-01-01 12:00:00", "0", "OK", "111", "0","0","0",  "2005-01-01 12:12:13", "1","1","1","5","0","0","0.0","1","1","1","0","1.0"));
		admin.updateServiceStatus("NAGIOS-MAIN",Nagios.APPLICATION_TYPE,"AIX","192.168.2.50",Nagios.createServiceStatusProps("aix_serv_1","LastPlugin out", "OK","1","HARD","2005-05-05 12:00:00", "2005-05-05 12:01:00", "ACTIVE", "1", "1","1","2005-01-01 12:00:00", "0", "OK", "111", "0","0","0",  "2005-01-01 12:12:14", "1","1","1","5","0","0","0.0","1","1","1","0","1.0"));
		admin.updateServiceStatus("NAGIOS-MAIN",Nagios.APPLICATION_TYPE,"VEVEY","192.168.2.49",Nagios.createServiceStatusProps("vevey_serv_2","LastPlugin out", "OK","1","HARD","2005-05-05 12:00:00", "2005-05-05 12:01:00", "ACTIVE", "1", "1","1","2005-01-01 12:00:00", "0", "OK", "111", "0","0","0",  "2005-01-01 12:12:15", "1","1","1","5","0","0","0.0","1","1","1","0","1.0"));
		admin.updateServiceStatus("NAGIOS-MAIN",Nagios.APPLICATION_TYPE,"AIX","192.168.2.50",Nagios.createServiceStatusProps("aix_serv_2","LastPlugin out", "OK","1","HARD","2005-05-05 12:00:00", "2005-05-05 12:01:00", "ACTIVE", "1", "1","1","2005-01-01 12:00:00", "0", "OK", "111", "0","0","0",  "2005-01-01 12:12:16", "1","1","1","5","0","0","0.0","1","1","1","0","1.0"));
		admin.updateServiceStatus("NAGIOS-MAIN",Nagios.APPLICATION_TYPE,"VEVEY","192.168.2.49",Nagios.createServiceStatusProps("vevey_serv_1","LastPlugin out", "OK","1","HARD","2005-05-05 12:01:00", "2005-05-05 12:02:00", "ACTIVE", "1", "1","1","2005-01-01 12:00:00", "0", "OK", "111", "0","0","0",  "2005-01-01 12:12:17", "1","1","1","5","0","0","0.0","1","1","1","0","1.0"));
		admin.updateServiceStatus("NAGIOS-MAIN",Nagios.APPLICATION_TYPE,"VEVEY","192.168.2.49",Nagios.createServiceStatusProps("vevey_serv_1","LastPlugin out", "OK","1","HARD","2005-05-05 12:02:00", "2005-05-05 12:03:00", "ACTIVE", "1", "1","1","2005-01-01 12:00:00", "0", "OK", "111", "0","0","0",  "2005-01-01 12:12:18", "1","1","1","5","0","0","0.0","1","1","1","0","1.0"));
		admin.updateServiceStatus("NAGIOS-MAIN",Nagios.APPLICATION_TYPE,"AIX","192.168.2.50",Nagios.createServiceStatusProps("aix_serv_1","LastPlugin out", "OK","1","HARD","2005-05-05 12:01:00", "2005-05-05 12:02:00", "ACTIVE", "1", "1","1","2005-01-01 12:00:00", "0", "OK", "111", "0","0","0",  "2005-01-01 12:12:19", "1","1","1","5","0","0","0.0","1","1","1","0","1.0"));
		admin.updateServiceStatus("NAGIOS-MAIN",Nagios.APPLICATION_TYPE,"VEVEY","192.168.2.49",Nagios.createServiceStatusProps("vevey_serv_1","LastPlugin out", "OK","1","HARD","2005-05-05 12:02:00", "2005-05-05 12:03:00", "ACTIVE", "1", "1","1","2005-01-01 12:00:00", "0", "OK", "111", "0","0","0",  "2005-01-01 12:12:20", "1","1","1","5","0","0","0.0","1","1","1","0","1.0"));
		admin.updateServiceStatus("NAGIOS-MAIN",Nagios.APPLICATION_TYPE,"AIX","192.168.2.50",Nagios.createServiceStatusProps("aix_serv_1","LastPlugin out", "OK","1","HARD","2005-05-05 12:01:00", "2005-05-05 12:02:00", "ACTIVE", "1", "1","1","2005-01-01 12:00:00", "0", "OK", "111", "0","0","0",  "2005-01-01 12:12:21", "1","1","1","5","0","0","0.0","1","1","1","0","1.0"));
		admin.updateServiceStatus("NAGIOS-MAIN",Nagios.APPLICATION_TYPE,"VEVEY","192.168.2.49",Nagios.createServiceStatusProps("vevey_serv_1","LastPlugin out", "OK","1","HARD","2005-05-05 12:02:00", "2005-05-05 12:03:00", "ACTIVE", "1", "1","1","2005-01-01 12:00:00", "0", "OK", "111", "0","0","0",  "2005-01-01 12:12:21", "1","1","1","5","0","0","0.0","1","1","1","0","1.0"));
	}


	public void testUpdateServiceStatusInBulk()
	{
		HashSet services;

		// initialize the framework
		admin.updateServiceStatus("INIT_MONITOR",Nagios.APPLICATION_TYPE,"INIT_HOST","192.168.2.1",Nagios.createServiceStatusProps("init_test_serv","LastPlugin out", "OK","1","HARD","2005-05-05 12:00:00", "2005-05-05 12:01:00", "ACTIVE", "1", "1","1","2005-01-01 12:00:00", "0", "OK", "111", "0","0","0",  "2005-01-01 12:12:12", "1","1","1","5","0","0","0.0","1","1","1","0","1.0"));

		// add new vevey services
		services = new HashSet();
		services.add(Nagios.createServiceStatusProps("vevey_serv_1","LastPlugin out", "OK","1","HARD","2005-05-05 12:00:00", "2005-05-05 12:01:00", "ACTIVE", "1", "1","1","2005-01-01 12:00:00", "0", "OK", "111", "0","0","0",  "2005-01-01 12:12:12", "1","1","1","5","0","0","0.0","1","1","1","0","1.0"));
		services.add(Nagios.createServiceStatusProps("vevey_serv_2","LastPlugin out", "DOWN","1","HARD","2005-05-05 12:00:00", "2005-05-05 12:01:00", "ACTIVE", "1", "1","1","2005-01-01 12:00:00", "0", "OK", "111", "0","0","0",  "2005-01-01 12:12:12", "1","1","1","5","0","0","0.0","1","1","1","0","1.0"));
		services.add(Nagios.createServiceStatusProps("vevey_serv_3","LastPlugin out", "WARNING","1","HARD","2005-05-05 12:00:00", "2005-05-05 12:01:00", "ACTIVE", "1", "1","1","2005-01-01 12:00:00", "0", "OK", "111", "0","0","0",  "2005-01-01 12:12:12", "1","1","1","5","0","0","0.0","1","1","1","0","1.0"));
		services.add(Nagios.createServiceStatusProps("vevey_serv_4","LastPlugin out", "UP","1","HARD","2005-05-05 12:00:00", "2005-05-05 12:01:00", "ACTIVE", "1", "1","1","2005-01-01 12:00:00", "0", "OK", "111", "0","0","0",  "2005-01-01 12:12:12", "1","1","1","5","0","0","0.0","1","1","1","0","1.0"));
		services.add(Nagios.createServiceStatusProps("vevey_serv_5","LastPlugin out", "PENDING","1","HARD","2005-05-05 12:00:00", "2005-05-05 12:01:00", "ACTIVE", "1", "1","1","2005-01-01 12:00:00", "0", "OK", "111", "0","0","0",  "2005-01-01 12:12:12", "1","1","1","5","0","0","0.0","1","1","1","0","1.0"));

		admin.updateServiceStatus("NAGIOS-MAIN",Nagios.APPLICATION_TYPE, "VEVEY","192.168.2.49", services);


		// add new aix services
		services = new HashSet();
		services.add(Nagios.createServiceStatusProps("aix_serv_1","LastPlugin out", "OK","1","HARD","2005-05-05 12:00:00", "2005-05-05 12:01:00", "ACTIVE", "1", "1","1","2005-01-01 12:00:00", "0", "OK", "111", "0","0","0",  "2005-01-01 12:12:12", "1","1","1","5","0","0","0.0","1","1","1","0","1.0"));
		services.add(Nagios.createServiceStatusProps("aix_serv_2","LastPlugin out", "UP","1","HARD","2005-05-05 12:00:00", "2005-05-05 12:01:00", "ACTIVE", "1", "1","1","2005-01-01 12:00:00", "0", "OK", "111", "0","0","0",  "2005-01-01 12:12:12", "1","1","1","5","0","0","0.0","1","1","1","0","1.0"));
		services.add(Nagios.createServiceStatusProps("aix_serv_3","LastPlugin out", "OK","1","HARD","2005-05-05 12:00:00", "2005-05-05 12:01:00", "ACTIVE", "1", "1","1","2005-01-01 12:00:00", "0", "OK", "111", "0","0","0",  "2005-01-01 12:12:12", "1","1","1","5","0","0","0.0","1","1","1","0","1.0"));
		services.add(Nagios.createServiceStatusProps("aix_serv_4","LastPlugin out", "DOWN","1","HARD","2005-05-05 12:00:00", "2005-05-05 12:01:00", "ACTIVE", "1", "1","1","2005-01-01 12:00:00", "0", "OK", "111", "0","0","0",  "2005-01-01 12:12:12", "1","1","1","5","0","0","0.0","1","1","1","0","1.0"));
		services.add(Nagios.createServiceStatusProps("aix_serv_5","LastPlugin out", "WARNING","1","HARD","2005-05-05 12:00:00", "2005-05-05 12:01:00", "ACTIVE", "1", "1","1","2005-01-01 12:00:00", "0", "OK", "111", "0","0","0",  "2005-01-01 12:12:12", "1","1","1","5","0","0","0.0","1","1","1","0","1.0"));
		services.add(Nagios.createServiceStatusProps("aix_serv_6","LastPlugin out", "OK","1","HARD","2005-05-05 12:00:00", "2005-05-05 12:01:00", "ACTIVE", "1", "1","1","2005-01-01 12:00:00", "0", "OK", "111", "0","0","0",  "2005-01-01 12:12:12", "1","1","1","5","0","0","0.0","1","1","1","0","1.0"));
		services.add(Nagios.createServiceStatusProps("aix_serv_7","LastPlugin out", "UP","1","HARD","2005-05-05 12:00:00", "2005-05-05 12:01:00", "ACTIVE", "1", "1","1","2005-01-01 12:00:00", "0", "OK", "111", "0","0","0",  "2005-01-01 12:12:12", "1","1","1","5","0","0","0.0","1","1","1","0","1.0"));
		services.add(Nagios.createServiceStatusProps("aix_serv_8","LastPlugin out", "PENDING","1","HARD","2005-05-05 12:00:00", "2005-05-05 12:01:00", "ACTIVE", "1", "1","1","2005-01-01 12:00:00", "0", "OK", "111", "0","0","0",  "2005-01-01 12:12:12", "1","1","1","5","0","0","0.0","1","1","1","0","1.0"));
		services.add(Nagios.createServiceStatusProps("aix_serv_9","LastPlugin out", "OK","1","HARD","2005-05-05 12:00:00", "2005-05-05 12:01:00", "ACTIVE", "1", "1","1","2005-01-01 12:00:00", "0", "OK", "111", "0","0","0",  "2005-01-01 12:12:12", "1","1","1","5","0","0","0.0","1","1","1","0","1.0"));
		services.add(Nagios.createServiceStatusProps("aix_serv_10","LastPlugin out", "OK","1","HARD","2005-05-05 12:00:00", "2005-05-05 12:01:00", "ACTIVE", "1", "1","1","2005-01-01 12:00:00", "0", "OK", "111", "0","0","0",  "2005-01-01 12:12:12", "1","1","1","5","0","0","0.0","1","1","1","0","1.0"));

		admin.updateServiceStatus("NAGIOS-MAIN",Nagios.APPLICATION_TYPE,"AIX","192.168.2.50", services);


		//// now update existing services

		// update vevey services
		services = new HashSet();
		services.add(Nagios.createServiceStatusProps("vevey_serv_1","LastPlugin out2", "UP","1","HARD","2005-05-05 12:01:00", "2005-05-05 12:02:00", "ACTIVE", "1", "1","1","2005-01-01 12:00:00", "0", "OK", "111", "0","0","0",  "2005-01-01 12:12:12", "1","1","1","5","0","0","0.0","1","1","1","0","1.0"));
		services.add(Nagios.createServiceStatusProps("vevey_serv_2","LastPlugin out2", "WARNING","1","HARD","2005-05-05 12:01:00", "2005-05-05 12:02:00", "ACTIVE", "1", "1","1","2005-01-01 12:00:00", "0", "OK", "111", "0","0","0",  "2005-01-01 12:12:12", "1","1","1","5","0","0","0.0","1","1","1","0","1.0"));
		services.add(Nagios.createServiceStatusProps("vevey_serv_3","LastPlugin out2", "OK","1","HARD","2005-05-05 12:01:00", "2005-05-05 12:02:00", "ACTIVE", "1", "1","1","2005-01-01 12:00:00", "0", "OK", "111", "0","0","0",  "2005-01-01 12:12:12", "1","1","1","5","0","0","0.0","1","1","1","0","1.0"));
		services.add(Nagios.createServiceStatusProps("vevey_serv_4","LastPlugin out2", "PENDING","1","HARD","2005-05-05 12:01:00", "2005-05-05 12:02:00", "ACTIVE", "1", "1","1","2005-01-01 12:00:00", "0", "OK", "111", "0","0","0",  "2005-01-01 12:12:12", "1","1","1","5","0","0","0.0","1","1","1","0","1.0"));
		services.add(Nagios.createServiceStatusProps("vevey_serv_5","LastPlugin out2", "OK","1","HARD","2005-05-05 12:01:00", "2005-05-05 12:02:00", "ACTIVE", "1", "1","1","2005-01-01 12:00:00", "0", "OK", "111", "0","0","0",  "2005-01-01 12:12:12", "1","1","1","5","0","0","0.0","1","1","1","0","1.0"));

		admin.updateServiceStatus("NAGIOS-MAIN",Nagios.APPLICATION_TYPE,"VEVEY","192.168.2.49", services);


		// update aix services
		services = new HashSet();
		services.add(Nagios.createServiceStatusProps("aix_serv_1","LastPlugin out", "OK","1","HARD","2005-05-05 12:01:00", "2005-05-05 12:02:00", "ACTIVE", "1", "1","1","2005-01-01 12:00:00", "0", "OK", "111", "0","0","0",  "2005-01-01 12:12:12", "1","1","1","5","0","0","0.0","1","1","1","0","1.0"));
		services.add(Nagios.createServiceStatusProps("aix_serv_2","LastPlugin out", "UP","1","HARD","2005-05-05 12:01:00", "2005-05-05 12:02:00", "ACTIVE", "1", "1","1","2005-01-01 12:00:00", "0", "OK", "111", "0","0","0",  "2005-01-01 12:12:12", "1","1","1","5","0","0","0.0","1","1","1","0","1.0"));
		services.add(Nagios.createServiceStatusProps("aix_serv_3","LastPlugin out", "OK","1","HARD","2005-05-05 12:01:00", "2005-05-05 12:02:00", "ACTIVE", "1", "1","1","2005-01-01 12:00:00", "0", "OK", "111", "0","0","0",  "2005-01-01 12:12:12", "1","1","1","5","0","0","0.0","1","1","1","0","1.0"));
		services.add(Nagios.createServiceStatusProps("aix_serv_4","LastPlugin out", "WARNING","1","HARD","2005-05-05 12:01:00", "2005-05-05 12:02:00", "ACTIVE", "1", "1","1","2005-01-01 12:00:00", "0", "OK", "111", "0","0","0",  "2005-01-01 12:12:12", "1","1","1","5","0","0","0.0","1","1","1","0","1.0"));
		services.add(Nagios.createServiceStatusProps("aix_serv_5","LastPlugin out", "PENDING","1","HARD","2005-05-05 12:01:00", "2005-05-05 12:02:00", "ACTIVE", "1", "1","1","2005-01-01 12:00:00", "0", "OK", "111", "0","0","0",  "2005-01-01 12:12:12", "1","1","1","5","0","0","0.0","1","1","1","0","1.0"));
		services.add(Nagios.createServiceStatusProps("aix_serv_6","LastPlugin out", "UP","1","HARD","2005-05-05 12:01:00", "2005-05-05 12:02:00", "ACTIVE", "1", "1","1","2005-01-01 12:00:00", "0", "OK", "111", "0","0","0",  "2005-01-01 12:12:12", "1","1","1","5","0","0","0.0","1","1","1","0","1.0"));
		services.add(Nagios.createServiceStatusProps("aix_serv_7","LastPlugin out", "PENDING","1","HARD","2005-05-05 12:01:00", "2005-05-05 12:02:00", "ACTIVE", "1", "1","1","2005-01-01 12:00:00", "0", "OK", "111", "0","0","0",  "2005-01-01 12:12:12", "1","1","1","5","0","0","0.0","1","1","1","0","1.0"));
		services.add(Nagios.createServiceStatusProps("aix_serv_8","LastPlugin out", "OK","1","HARD","2005-05-05 12:01:00", "2005-05-05 12:02:00", "ACTIVE", "1", "1","1","2005-01-01 12:00:00", "0", "OK", "111", "0","0","0",  "2005-01-01 12:12:12", "1","1","1","5","0","0","0.0","1","1","1","0","1.0"));
		services.add(Nagios.createServiceStatusProps("aix_serv_9","LastPlugin out", "OK","1","HARD","2005-05-05 12:01:00", "2005-05-05 12:02:00", "ACTIVE", "1", "1","1","2005-01-01 12:00:00", "0", "OK", "111", "0","0","0",  "2005-01-01 12:12:12", "1","1","1","5","0","0","0.0","1","1","1","0","1.0"));
		services.add(Nagios.createServiceStatusProps("aix_serv_10","LastPlugin out","OK","1","HARD","2005-05-05 12:01:00", "2005-05-05 12:02:00", "ACTIVE", "1", "1","1","2005-01-01 12:00:00", "0", "OK", "111", "0","0","0",  "2005-01-01 12:12:12", "1","1","1","5","0","0","9.9","1","1","1","0","1.0"));

		admin.updateServiceStatus("NAGIOS-MAIN",Nagios.APPLICATION_TYPE,"AIX","192.168.2.50", services);
	}


	public void testUpdateLogMessage()
	{
		admin.updateLogMessage(/*ConsolidationCriteria*/null,"NAGIOS","INIT_MON","INIT_HOST","192.168.2.1","HIGH","UP","Warning while testing","2005-05-03 12:49:36","2005-05-03 12:49:36","API", "Nothing", "check_host", "OK", "loggerName", "ApplicationName","2005-01-01");
		admin.updateLogMessage(/*ConsolidationCriteria*/null,"NAGIOS","MONTREUX","VEVEY","192.168.2.49","LOW","OK","Warning while testing","2005-05-03 12:49:36","2005-05-03 12:49:36","API", "Nothing", "check_host", "OK", "loggerName", "ApplicationName","2005-01-01");
		admin.updateLogMessage(/*ConsolidationCriteria*/null,"NAGIOS","MONTREUX","VEVEY","192.168.2.49","LOW","UP","Warning while testing","2005-05-03 12:49:37","2005-05-03 12:49:37","API", "Nothing", "check_host", "OK", "loggerName", "ApplicationName","2005-01-01");
		admin.updateLogMessage(/*ConsolidationCriteria*/null,"NAGIOS","MONTREUX","VEVEY","192.168.2.49","OK","DOWN","Warning while testing","2005-05-03 12:49:38","2005-05-03 12:49:38","API", "Nothing", "check_host", "OK", "loggerName", "ApplicationName","2005-01-01");
		admin.updateLogMessage(/*ConsolidationCriteria*/null,"NAGIOS","MONTREUX","VEVEY","192.168.2.49","HIGH","WARNING","Warning while testing","2005-05-03 12:49:38","2005-05-03 12:49:38","API", "Nothing", "check_host", "OK", "loggerName", "ApplicationName","2005-01-01");
		admin.updateLogMessage(/*ConsolidationCriteria*/null,"NAGIOS","MONTREUX","VEVEY","192.168.2.49","FATAL","UNREACHABLE", "Warning while testing","2005-05-03 12:49:38","2005-05-03 12:49:38","API", "Nothing", "check_host", "OK", "loggerName", "ApplicationName","2005-01-01");
	}


	/** 
	 * this is here to generate a map of key-value pairs suitable for updating
	 * the ServiceStatus properties of a Nagios application
	 */
	/*
	private Properties createServiceStatusProps(
			String ServiceDescription,
			String LastPluginOutput, String MonitorStatus, String RetryNumber,
			String StateType, String LastCheckTime, String NextCheckTime,
			String CheckType, String isChecksEnabled,
			String isAcceptPassiveChecks, String isEventHandlersEnabled,
			String LastStateChange, String isProblemAcknowledged,
			String LastHardState, String TimeOK, String TimeUnknown,
			String TimeWarning, String TimeCritical,
			String LastNotificationTime, String CurrentNotificationNumber,
			String isNotificationsEnabled, String Latency,
			String ExecutionTime, String isFlapDetectionEnabled,
			String isServiceFlapping, String PercentStateChange,
			String ScheduledDowntimeDepth, String isFailurePredictionEnabled,
			String isProcessPerformanceData, String isObsessOverService) 
	{
		Properties props = new Properties();

		props.setProperty("ServiceDescription",         ServiceDescription);
		props.setProperty("LastPluginOutput",           LastPluginOutput);
		props.setProperty("MonitorStatus",              MonitorStatus);
		props.setProperty("RetryNumber",                RetryNumber);
		props.setProperty("StateType",                  StateType);
		props.setProperty("LastCheckTime",              LastCheckTime);
		props.setProperty("NextCheckTime",              NextCheckTime);
		props.setProperty("CheckType",                  CheckType);
		props.setProperty("isChecksEnabled",            isChecksEnabled);
		props.setProperty("isAcceptPassiveChecks",      isAcceptPassiveChecks);
		props.setProperty("isEventHandlersEnabled",     isEventHandlersEnabled);
		props.setProperty("LastStateChange",            LastStateChange);
		props.setProperty("isProblemAcknowledged",      isProblemAcknowledged);
		props.setProperty("LastHardState",              LastHardState);
		props.setProperty("TimeOK",                     TimeOK);
		props.setProperty("TimeUnknown",                TimeUnknown);
		props.setProperty("TimeWarning",                TimeWarning);
		props.setProperty("TimeCritical",               TimeCritical);
		props.setProperty("LastNotificationTime",       LastNotificationTime);
		props.setProperty("CurrentNotificationNumber",  CurrentNotificationNumber);
		props.setProperty("isNotificationsEnabled",     isNotificationsEnabled);
		props.setProperty("Latency",                    Latency);
		props.setProperty("ExecutionTime",              ExecutionTime);
		props.setProperty("isFlapDetectionEnabled",     isFlapDetectionEnabled);
		props.setProperty("isServiceFlapping",          isServiceFlapping);
		props.setProperty("PercentStateChange",         PercentStateChange);
		props.setProperty("ScheduledDowntimeDepth",     ScheduledDowntimeDepth);
		props.setProperty("isFailurePredictionEnabled", isFailurePredictionEnabled);
		props.setProperty("isProcessPerformanceData",   isProcessPerformanceData);
		props.setProperty("isObsessOverService",        isObsessOverService);

		return props;
	}
	*/

} // end class ProfileAdmin

