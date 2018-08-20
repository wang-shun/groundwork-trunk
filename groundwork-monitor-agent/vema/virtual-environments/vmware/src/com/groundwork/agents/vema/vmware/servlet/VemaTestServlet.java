/*
 * Copyright 2012 GroundWork Open Source, Inc. ("GroundWork") All rights
 * reserved. This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package com.groundwork.agents.vema.vmware.servlet;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.*;

import com.groundwork.agents.vema.vmware.deprecated.PrintCounters;
import com.groundwork.agents.vema.vmware.deprecated.SimpleClient;

/**
 * Servlet implementation class VMWareServlet
 */
public class VemaTestServlet extends HttpServlet
{
	private static Logger log = Logger.getLogger(VemaTestServlet.class);
	public  static final long serialVersionUID = 1;

	/**
	 * Default constructor.
	 */
	public VemaTestServlet()
	{
	}

	/**
	 * @see Servlet#init(ServletConfig)
	 */
    private void cannedTestPrintCounters()
    {
        PrintCounters pco = new PrintCounters();
        // ----------------------------------------------------------------------
        // this hack is just to package the testing in our development environment.
        // ----------------------------------------------------------------------
        log.info( "Inside cannedTestPrintCounters" );
        String[] args = 
        {
            "--url",        "https://colorado.groundwork.groundworkopensource.com/sdk",
            "--username",   "vmware-dev",
            "--password",   "M3t30r1t3",
            "--entityname", "lucerne.groundwork.groundworkopensource.com",
            "--entitytype", "hostsystem",
            "--filename",   "/home/rlynch/testprintcounters.xml",
        };
        pco.TestPrintCounters( args );
        log.info( "After  TestPrintCounters" );
    }
    
    private void cannedTestVema()
    {
        SimpleClient simpleclient = new SimpleClient();
        // ----------------------------------------------------------------------
        // this hack is just to package the testing in our development environment.
        // ----------------------------------------------------------------------
        log.info( "Starting cannedTestVema" );
		String[] args = {
//"--multi", "https://172.28.115.98/sdk&vmware-dev&M3t30r1t3&www-3", // hanoi
//"--multi", "https://hanoi/sdk&vmware-dev&M3t30r1t3&www-3", // hanoi
//"--multi", "https://172.28.115.172/sdk&admin&M3t30r1t3&www-3", // hanoi
				"--multi", "https://eng-vsphere4/sdk&administrator&m3t30r1t3&www-3", // hanoi
//"--multi", "https://thun.groundwork.groundworkopensource.com/sdk&vmware-dev&M3t30r1t3&www-3",
//"--multi", "https://colorado.groundwork.groundworkopensource.com/sdk&vmware-dev&M3t30r1t3&www-3",
//"--multi", "https://24.18.196.28/sdk&root&!QAZxsw2&www-3",

		// "--url", "https://colorado.groundwork.groundworkopensource.com/sdk",
		// "--url", "https://thun.groundwork.groundworkopensource.com/sdk",
		// "--url", "https://172.28.115.98/sdk", // hanoi...com //
		// "--username", "vmware-dev",
		// "--password", "M3t30r1t3",
		// "--vmname", "www-3",
		// "--entityname", "thun.groundwork.groundworkopensource.com",
		// "--entitytype", "hostsystem",
		// "--filename", "/home/rlynch/testprintcounters.xml",
		};
        simpleclient.TestSimpleClient( args );
        log.info( "After  TestPrintCounters" );
    }

	public void init(ServletConfig config) throws ServletException
	{
        log.info("\nThis is the INIT() part of VemaTestServlet()\n");
		try
		{
            // this.cannedTestPrintCounters();
            this.cannedTestVema();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
        log.info("\nThis is a test stop point\n\n\n\n\n");
	}
}
