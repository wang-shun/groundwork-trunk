/*
 * 
 * Copyright 2007 GroundWork Open Source, Inc. ("GroundWork") All rights
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
package com.groundworkopensource.portal.licensing;

import java.util.Collection;
import org.apache.log4j.Logger;
import org.picocontainer.Startable;


/**
 * This Groundwork license service loads instal info and creates the validator.
 * 
 * @author Arul Shanmugam
 */
public class LicenseServiceImpl implements Startable {

	/** logger. */
	private static final Logger LOGGER = Logger
			.getLogger(LicenseServiceImpl.class.getName());

	/**
	 * License service
	 */
	public LicenseServiceImpl() {

	}

	public void start() {
		LOGGER.info("Starting GroundWork Licensing Service...");
		InstallInfoBean installInfo = new InstallInfoBean();
		installInfo.init();
		LOGGER.info("Install GUID " + installInfo.getInstallGUID());
		LicenseKeyBean licenseKey = new LicenseKeyBean();
		licenseKey.init();
		LicenseValidator validator = new LicenseValidator();
		validator.setInstallInfo(installInfo);
		validator.setLicenseKey(licenseKey);
		LicenseManager.getInstance().addValidator(validator);
		//this.synchExtendedRoles();
	}

	public void stop() {

	}
}
