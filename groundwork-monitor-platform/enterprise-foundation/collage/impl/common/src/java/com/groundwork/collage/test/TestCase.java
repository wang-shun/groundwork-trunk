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

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.HibernateUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class TestCase extends junit.framework.TestCase 
{
	private static final String MSG_ELAPSED_TIME = "%1$s , Elapsed Time (ms): %2$d";
	private static final String MSG_DEFAULT_ELAPSED_TIME = "Test Execution";

	private static Log classlog = LogFactory.getLog(TestCase.class);

	protected Log log = LogFactory.getLog(this.getClass());
	  
	private long _startTime = 0;
	private long _endTime = 0;


    protected CollageFactory collage = null;
			
    public TestCase(String s) {
        super(s);        
    }

    protected void runTest() throws Throwable {
        try {
            if (log.isInfoEnabled()) log.info("Running test...");
            super.runTest();
        } catch (Throwable e) {
            HibernateUtil.rollbackTransaction();
            throw e;
        } finally{
            HibernateUtil.closeSession();
        }
    }

    protected void setUp() throws Exception {
        super.setUp();

        // Make sure spring assembly is loaded
        collage = CollageFactory.getInstance();
        assertNotNull(collage);
        collage.loadSpringAssembly("META-INF/test-common-model-assembly.xml");
    }

    /**
     * Executes a MySql script to create a test database with static data that
     * can be used across all tests.  
     * @param verbose - if you want more output about result of running a script
     * @param scriptName - the name of the script to run
     * @return integer value indicating success or failure - NOTE: we can expand
     * on this if we want...  For now, 0=success, 1=errors
     */
    public static int executeScript (boolean verbose, String scriptName) 
    {
    	try {
    		String[] cmd = new String[]{"psql",
    				"-f",
    				scriptName, "gwtest",
    				"postgres"
    		};
    		classlog.debug(cmd[0] + " " + cmd[1] + " " +
    				cmd[2] + " " + cmd[3] + " " + cmd[4]);
    		Process proc = Runtime.getRuntime().exec(cmd);
    		if (verbose) {
				// read the output
				InputStream inputstream = proc.getInputStream();
				InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
				BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
				classlog.debug("Output from script:");
				String line;
				while ((line = bufferedreader.readLine()) != null) {
					classlog.debug(line);
				}
			}

			// read the error output and report
			InputStream errorStream = proc.getErrorStream();
			InputStreamReader errorInputStreamReader = new InputStreamReader(errorStream);
			BufferedReader bufferedErrorReader = new BufferedReader(errorInputStreamReader);
			String errorLine = bufferedErrorReader.readLine();
			if (errorLine != null) {
				classlog.error("Error output from script:");
				classlog.error(errorLine);
				while ((errorLine = bufferedErrorReader.readLine()) != null) {
					classlog.error(errorLine);
				}
			}

			// check for failure
			try {
				if (proc.waitFor() != 0) {
					classlog.error("exit value = " +
							proc.exitValue());
				}
			}
			catch (InterruptedException e) {
				classlog.error(e, e);
				return 1;
			}
    	} catch (Exception e) {
			classlog.error(e, e);
    		return 1;
    	}
    	return 0;
    }
    
    
    protected void tearDown() throws Exception {
        super.tearDown();
        if (collage != null) {
           collage.unloadSpringAssembly();
        }
    }

    protected long startTime ()
    {
    	_startTime = System.currentTimeMillis();
    	
    	return _startTime;
    }
    
    protected long endTime ()
    {
    	_endTime = System.currentTimeMillis();
    	
    	return _endTime;
    }    
    
    protected long elapsedTime ()
    {
    	if (_startTime != 0 && _endTime != 0)
    		return _endTime - _startTime;
    	
    	log.error("Start Time and End Time must be set before calling elapsedTime.");
    	
    	return -1;
    }
    
    protected void outputElapsedTime (String msg)
    {
    	// Capture end time
    	endTime();
    	
    	if (msg == null)
    		msg = MSG_DEFAULT_ELAPSED_TIME;
    	
    	String output = String.format(MSG_ELAPSED_TIME, msg, elapsedTime());
    	
		if (log.isInfoEnabled()) log.info(output);
    }
}
