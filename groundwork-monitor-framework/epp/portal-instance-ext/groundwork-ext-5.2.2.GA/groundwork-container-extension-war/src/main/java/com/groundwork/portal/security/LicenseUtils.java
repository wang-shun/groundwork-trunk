package com.groundwork.portal.security;

import java.io.File;

import org.apache.log4j.Logger;

public class LicenseUtils {
	/**
	 * Logger.
	 */
	private static Logger log = Logger.getLogger(LicenseUtils.class);

	/**
	 * The path to the Archive where the Portal-Console is located.
	 * 
//	 * TODO:  This should not be hardcode.  Perhaps it should be a bean or look in the classpath?
	 */
	private static String PORTAL_CONSOLE_WAR_PATH = "/usr/local/groundwork/foundation/container/webapps/jboss/jboss-portal.sar/portal-console.war";

	/**
	 * This utility method will check the <code>CONSOLE_WAR_PATH</code> for a valid license.
	 * 
	 * @return true, if a valid license of found
	 */
	public static boolean validateWebArchive() {
		/*
		// Get the validation object for validation checks
		EnterpriseLicenseValidator validator = LicenseManager.getInstance().getLicenseValidator();
		
		if (validator != null) {
			// Validation only available in enterprise
			// Check if console war file exists.If exists then it is enterprise
			final File consoleWAR = new File(PORTAL_CONSOLE_WAR_PATH);
			
			if (log.isDebugEnabled() ) {
				log.debug( "Attempting to check the web archive for a valid license: " + consoleWAR.getCanonicalPath() );
			}
			
			return validator.validate();
		} else {
			return false;
		}
		*/
		
		
		// By pass for testing..
		final boolean returnValue = false;
		
		log.warn( "Bypassing license validation:" + returnValue );
		
		return returnValue;
	}

}
