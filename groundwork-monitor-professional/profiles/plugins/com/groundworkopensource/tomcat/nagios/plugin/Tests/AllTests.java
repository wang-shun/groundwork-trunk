package com.groundworkopensource.tomcat.nagios.plugin.Tests;

import junit.framework.TestCase;
import com.groundworkopensource.tomcat.nagios.plugin.*;

public class AllTests extends TestCase {

	
	///////////////////////////////////////////////////////////////////////////////////////////////
	// All subsequent tests are to 172.28.112.234, which is protected by password authentication
	//////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Test when a bad password is provided to a protected host
	 *
	 */
	public void testBadPasswordToProtectedHost() {
		String [] args = new String[9];
		System.out.println("=====> testBadPasswordToProtectedHost()");
		try {
			args[0] = "-dump"; 
			args[1] = "-server";
			args[2] = "172.28.112.234";
			args[3] = "-port";
			args[4] = "9004";
			args[5] = "-user";
			args[6] = "monitorRole";
			args[7] = "-password";
			args[8] = "badPassword";
			Monitor mon = new Monitor(args);
			mon.getRC();
			fail("Bad password to protected host, should have failed");
		} catch (Exception e) {
			String rs = e.toString().substring(e.toString().indexOf(":")+2);
			assertEquals(rs,"Monitor failed - bad username/password.");
		}				
	}
	
	/**
	 * Test when no user/password is provided to a protected host
	 *
	 */
	public void testNoPasswordToProtectedHost() {
		String [] args = new String[5];
		System.out.println("=====> testNoPasswordToProtectedHost()");
		try {
			args[0] = "-dump"; 
			args[1] = "-server";
			args[2] = "172.28.112.234";
			args[3] = "-port";
			args[4] = "9004";
			Monitor mon = new Monitor(args);
			mon.getRC();
			fail("No password to protected host, should have failed");
		} catch (Exception e) {
			String rs = e.toString().substring(e.toString().indexOf(":")+2);
			assertEquals(rs,"Monitor failed - bad username/password.");
		}				
	}
	
	/**
	 * Test when a good username/password is passed to a protected host
	 *
	 */
	public void testGoodPasswordToProtectedHost() {
		String [] args = new String[9];
		System.out.println("=====> testNoPasswordToProtectedHost()");
		try {
			args[0] = "-dump"; 
			args[1] = "-server";
			args[2] = "172.28.112.234";
			args[3] = "-port";
			args[4] = "9004";
			args[5] = "-user";
			args[6] = "monitorRole";
			args[7] = "-password";
			args[8] = "gwrk";
			Monitor mon = new Monitor(args);
			mon.getRC();
		} catch (Exception e) {
			fail("Password to protected host, should have worked but didn't");
		}				
	}
	
	////////////////////////////////////////////////////////////////////////////////
	// All subsequent tests are to localhost, which is unprotected
	////////////////////////////////////////////////////////////////////////////////
	/**
	 * Test shell's main
	 *
	 */
	public void testMain() {
		System.out.println("=====> testMain()");
		try {
			String [] args = new String[4];
			args[0] = "-m";
			args[1] = "Catalina:type=Loader,path=/host-manager,host=localhost";
			args[2] = "-a";
			args[3] = "!";
			Shell.main(args);
		} catch (Exception error) {
			error.printStackTrace();
			fail(error.toString());
		} 
	}
	
	/**
	 * Test shell's main using -f option (load args from file)
	 *
	 */
	public void testMainWithFile() {
		System.out.println("=====> testMainWithFile()");
		try {
			String [] args = new String[2];
			args[0] = "-f"; args[1] = "data/test.txt";
			Shell.main(args);
		} catch (Exception error) {
			error.printStackTrace();
			fail(error.toString());
		} 
	}
	
	/**
	 * Test a valid mbean with wildcard attributes
	 */
	public void testGetValidMbeanWithWildCard() {
		String [] args = new String[4];
		System.out.println("=====> testGetValidMbeanWithWildCard()");
		try {
			args[0] = "-m"; args[1] = "java.lang:type=OperatingSystem";;
			args[2] = "-a"; args[3] = "!";
			Monitor mon = new Monitor(args);
			mon.getRC();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}				
	}
	
	/**
	 * Test a valid mbean fully specified.
	 * @throws Exception
	 */
	public void testGetValidMbeanNoWildCard()  {
		String [] args = new String[4];
		System.out.println("=====> testGetValidMbeanNoWildCard()");
		try {
			args[0] = "-m"; args[1] = "Catalina:type=Loader,path=/host-manager,host=localhost";
			args[2] = "-a"; args[3] = "reloadable";
			Monitor mon = new Monitor(args);
			mon.getRC();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}				
	}
	
	/**
	 * Test the help option
	 * @throws Exception
	 */
	public static void testHelp()  {
		String [] args = null;
		System.out.println("=====> testHelp()");
		try {
			Monitor mon = new Monitor(args);
			fail("Null arguments should not be allowed");
		} catch (Exception e) {
			String rs = e.toString().substring(e.toString().indexOf(":")+2);
			assertEquals(rs,"Monitor failed - no arguments");
		}
	}
	
	/**
	 * Test bad arguments
	 */
	public static void testBadArg() {
		System.out.println("=====> testBadArg()");
		String [] args = new String[1];
		try {
			args[0] = "-prt";
			Monitor mon = new Monitor(args);
			fail("Bad arguments should not be allowed");
		} catch (Exception e) {
			String rs = e.toString().substring(e.toString().indexOf(":")+2);
			assertEquals(rs,"Monitor failed unknown argument - -prt");
		}
	}
	
	/**
	 * Test unbalanced arguments
	 */
	public static void testUnbalancedArgs()  {
		String [] args = new String[1];
		System.out.println("=====> testUnbalancedArgs()");
		try {
			args[0] = "-s";
			Monitor mon = new Monitor(args);
			fail("Unbalanced -s arguments should not be allowed");
		} catch (Exception e) {
			String rs = e.toString().substring(e.toString().indexOf(":")+2);
			assertEquals(rs,"Unbalanced argument for -s");
		}
		
		try {
			args[0] = "-p";
			Monitor mon = new Monitor(args);
			fail("Unbalanced -p arguments should not be allowed");
		} catch (Exception e) {
			String rs = e.toString().substring(e.toString().indexOf(":")+2);
			assertEquals(rs,"Unbalanced argument for -p");
		}
		try {
			args[0] = "-u";
			Monitor mon = new Monitor(args);
			fail("Unbalanced -u arguments should not be allowed");
		} catch (Exception e) {
			String rs = e.toString().substring(e.toString().indexOf(":")+2);
			assertEquals(rs,"Unbalanced argument for -u");
		}
		try {
			args[0] = "-password";
			Monitor mon = new Monitor(args);
			fail("Unbalanced -password arguments should not be allowed");
		} catch (Exception e) {
			String rs = e.toString().substring(e.toString().indexOf(":")+2);
			assertEquals(rs,"Unbalanced argument for -password");
		}	
		try {
			args[0] = "-m";
			Monitor mon = new Monitor(args);
			fail("Unbalanced -m arguments should not be allowed");
		} catch (Exception e) {
			String rs = e.toString().substring(e.toString().indexOf(":")+2);
			assertEquals(rs,"Unbalanced argument for -m");
		}	
		try {
			args[0] = "-a";
			Monitor mon = new Monitor(args);
			fail("Unbalanced -a arguments should not be allowed");
		} catch (Exception e) {
			String rs = e.toString().substring(e.toString().indexOf(":")+2);
			assertEquals(rs,"Unbalanced argument for -a");
		}			
	}
	
	/**
	 * Test a bad port option
	 */
	public static void testBadPort() {
		String [] args = new String[2];
		System.out.println("=====> testBadPort()");
		try {
			args[0] = "-p";
			args[1] = "hello";
			Monitor mon = new Monitor(args);
			fail("Null arguments should not be allowed");
		} catch (Exception e) {
			String rs = e.toString().substring(e.toString().indexOf(":")+2);
			assertEquals(rs,"Non-integer port value not allowed: hello");
		}
	}
	
	/**
	 * Test the dump, use defaults for server and port.
	 *
	 */
	public static void testDumpWithDefaults() {
		String [] args = new String[1];
		System.out.println("=====> testDumpWithDefaults()");
		try {
			args[0] = "-dump";
			Monitor mon = new Monitor(args);
			mon.getRC();
		} catch (Exception e) {
			fail("Unexpected: " + e);
		}	 
	}
	
	/**
	 * Test dump, no defaults for server and port but with bad server
	 */
	public static void testDumpBadServerWithNoDefaults()  {
		String [] args = new String[5];
		System.out.println("=====> testDumpBadServerWithNoDefaults()");
		try {
			args[0] = "-server"; args[1] = "locoBadhost";
			args[2] = "-port"; args[3] = "9004";
			args[4] = "-dump";
			Monitor mon = new Monitor(args);
			fail("Shouldn't work with a bad host");
		} catch (Exception e) {
			String rs = e.toString().substring(e.toString().indexOf(":")+2);
			rs = rs.substring(0,rs.indexOf(":"));
			assertEquals(rs,"Failed to retrieve RMIServer stub");	
		}		 
	}
	
	/**
	 * Test getting an invalid mbean
	 *
	 */
	public static void getInvalidMbean()  {
		String [] args = new String[5];
		System.out.println("=====> getInvalidMbean()");
		try {
			args[0] = "-m";
			args[1] = "Catalina:type.path.host";
			Monitor mon = new Monitor(args);
			mon.getRC();
		} catch (Exception e) {
			fail(e.toString());
		}		
	}
}
