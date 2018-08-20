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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Properties;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.Arrays;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import net.padlocksoftware.license.LicenseStatus;
import net.padlocksoftware.license.Validator;

import org.apache.log4j.Logger;
import org.groundwork.foundation.ws.api.WSDevice;
import org.groundwork.foundation.ws.impl.WSDeviceServiceLocator;
import org.groundwork.foundation.ws.model.impl.Filter;
import org.groundwork.foundation.ws.model.impl.FilterOperator;
import org.groundwork.foundation.ws.model.impl.WSFoundationCollection;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ApplicationScoped;
import javax.faces.event.ActionEvent;

/**
 * This class validates the license for the enterprise portlets.
 * 
 * @author Arul Shanmugam
 */
@ManagedBean(name = "licenseValidator")
@ApplicationScoped
public class LicenseValidator implements EnterpriseLicenseValidator {

	/**
	 * one kilobyte = 1024 bytes
	 */
	private static final int ONE_KB = 1024;

	/** logger. */
	private static final Logger LOGGER = Logger
			.getLogger(LicenseValidator.class.getName());

	/** The Constant LICENSE_KEY_PATH. */
	private static final String LICENSE_KEY_PATH = "/usr/local/groundwork/config/groundwork.lic";
	
	/** The Constant LICENSE_KEY_PATH. */
	private static final String GATEIN_CONFIG_PATH = "/usr/local/groundwork/config/configuration.properties";

	/** The Constant Foundation properties path. */
	private static final String FOUNDATION_CONFIG_PATH = "/usr/local/groundwork/config/foundation.properties";
	
	/** The Constant SV properties path. */
	private static final String SV_PROP_PATH = "/usr/local/groundwork/config/status-viewer.properties";

	/** The license key. */
	private LicenseKeyBean licenseKey = null;

	/** The license key string. */
	private String licenseKeyString = null;

	/** The install info. */
	private InstallInfoBean installInfo = null;

	/** The validation rules. */
	private String validationRules = null;

	/** The server time. */
	private String serverTime = null;

	/** The error message */
	private String errorMessage = null;
	
	private String softLimitMessage = null;
	private String softlimitbgcolor = null;
	private String softlimittxtcolor = null;

	/** Set to true if admin user */
	private boolean adminUser = false;

	/**
	 * Instantiates a new license validator.
	 */
	public LicenseValidator() {
		LOGGER.info("contruct from jar...");
		licenseKeyString = readFileAsString(LICENSE_KEY_PATH);
	}
	
	@PostConstruct
    public void postContruct() {
		LOGGER.info("postContruct from jar...");
        installInfo = new InstallInfoBean();
        licenseKey = new LicenseKeyBean();
    }
	
	

	/**
	 * Validates the license for enterprise portlets.
	 * 
	 * @return true, if validate
	 */
	public boolean validate() {
		boolean valid = false;
		if (licenseKey != null) {
			// Always refresh the licenseKey(like putting in the request scope)
			licenseKey.init();
			Validator validator = licenseKey.getValidator();

			if (validator != null) {
				LicenseStatus licenseStatus = validator.validate();
				if (licenseStatus != LicenseStatus.Valid) {
					errorMessage = "License Key Validation Failed. Bad Signature(File might be corrupted) or License Expired!";
					return false;
				}
				validationRules = licenseKey.getValidationRules();
				StringTokenizer stkn = new StringTokenizer(validationRules, ";");

				while (stkn.hasMoreTokens()) {
					String paramName = stkn.nextToken();
					/*
					 * Verify the version. It checks the beginning of the
					 * version. For example if the keys defines 6.1 the key is
					 * valid for 6.1, 6.1.x or if it defines 6 it's valid for
					 * all minor releases
					 */
					if (paramName != null
							&& paramName.equalsIgnoreCase("param_1")) {
						if (!installInfo.getVersion().startsWith(
								licenseKey.getVersion())) {
							errorMessage = "License Key Validation Failed. Version mismatch!";
							return false;
						}
					}
					if (paramName != null
							&& paramName.equalsIgnoreCase("param_4")) {
						if (!licenseKey.getProductName().equals(
								installInfo.getProductName())) {
							errorMessage = "License Key Validation Failed. ProductName mismatch!";
							return false;
						}
					}
					if (paramName != null
							&& paramName.equalsIgnoreCase("param_9")) {
						if (!licenseKey.getInstallGUID().equals(
								installInfo.getInstallGUID())) {
							errorMessage = "License Key Validation Failed. InstallGUID mismatch!";
							return false;
						}
					}
					if (paramName != null
							&& paramName.equalsIgnoreCase("param_3")) {
						/*
						 * Check for the Network service only if it is required
						 * -- GWMON-8307
						 */
						if (licenseKey.getNetworkServiceReqd()
								.compareToIgnoreCase("1") == 0) {
							if (!licenseKey.getNetworkServiceReqd().equals(
									installInfo.getNetworkServiceReqd())) {
								errorMessage = "License Key Validation Failed. Network Services mismatch!";
								return false;
							}
						}
					}
					if (paramName != null
							&& paramName.equalsIgnoreCase("param_6")) {
						int deviceCount = doDeviceCount();
						if (deviceCount < 0) {
							errorMessage = "License Key Validation Failed. Cannot calculate number of devices!";
							return false;
						}
						if (deviceCount > Integer.parseInt(licenseKey
								.getHardLimitDevices())) {
							errorMessage = "License Key Validation Failed. Devices HardLimit reached!";
							return false;
						}
					}
					/*
					 * Make sure that customer runs in the time range between
					 * startDate EndDate
					 */
					if (paramName != null
							&& paramName.equalsIgnoreCase("param_12")) {
						try {
							Date now = Calendar.getInstance().getTime();
							Date startDate = DateFormat.getDateTimeInstance()
									.parse(licenseKey.getStartDate());

							if (now.before(startDate)) {
								errorMessage = "License Key Validation Failed. Invalid Date range!";
								return false;
							}
						} catch (ParseException exc) {
							LOGGER.error("License validation: Unable to parse Start date");
							return false;
						}
					}
					/*
					 * Hard limit Expiration Date violation should invalidate
					 * License as well
					 */
					if (paramName != null
							&& paramName.equalsIgnoreCase("param_8")) {
						try {
							Date now = Calendar.getInstance().getTime();
							Date hardLimitExpirationDate = DateFormat
									.getDateTimeInstance()
									.parse(licenseKey
											.getHardLimitExpirationDate());

							if (now.after(hardLimitExpirationDate)) {
								errorMessage = "License Key Validation Failed. HardLimit Date Expired!";
								return false;
							}
						} catch (ParseException exc) {
							LOGGER.error("License validation: Unable to parse expiration date");
							return false;
						}
					}
				}
				// reset the message
				valid = true;

			} // end if
		} // end if
		return valid;
	}

	/**
	 * Sets the license key.
	 * 
	 * @param licKey
	 *            the new license key
	 */
	public void setLicenseKey(LicenseKeyBean licKey) {
		this.licenseKey = licKey;
	}

	/**
	 * Gets the license key.
	 * 
	 * @return the license key
	 */
	public LicenseKeyBean getLicenseKey() {
		return this.licenseKey;
	}

	/**
	 * Creates a new license key file.
	 * 
	 * @param event
	 *            the event
	 * 
	 * @return the string
	 */
	public void createLicenseKeyFile(ActionEvent event) {
		if (licenseKeyString == null || licenseKeyString.equals("")) {
			LOGGER.error("Invalid License Key");
			FacesContext.getCurrentInstance().addMessage(
					"License Key Validation Failed",
					new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"Please enter a valid license key!", null));
			return;
		}
		Writer output = null;
		try {
			File file = new File(LICENSE_KEY_PATH);
			if (file.exists()) {
				if (!file.delete()) {
					LOGGER.info("Failed to delete License File : "
							+ LICENSE_KEY_PATH);
				}
			}
			output = new BufferedWriter(new FileWriter(file));
			output.write(licenseKeyString.trim());
			LOGGER.info("License File created Successfully");

		} catch (Exception exc) {
			LOGGER.error(exc.getMessage());
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (Exception exc) {
					LOGGER.error(exc.getMessage());
				}
			}
			licenseKey.init();
			installInfo.init();
		}
		if (this.validate()) {
			FacesContext
					.getCurrentInstance()
					.addMessage(
							"License Key Validation Success",
							new FacesMessage(
									FacesMessage.SEVERITY_INFO,
									"Thank you for validating the license. Your license is activated now. Happy Monitoring!",
									null));
			// Redirect to landing page
			try {
				String serverName = PropertyUtils.getPropertyFromFilePath(GATEIN_CONFIG_PATH,"gatein.sso.portal.url");
				LOGGER.debug("Server Name==>" + serverName);
				FacesContext.getCurrentInstance().getExternalContext().redirect(serverName + "/portal/classic");
				// reset the message
				this.errorMessage = null;
			} catch (Exception exc) {
				LOGGER.error(exc.getMessage());
			}
		} else {
			FacesContext.getCurrentInstance().addMessage(
					"License Key Validation Failed",
					new FacesMessage(FacesMessage.SEVERITY_ERROR, errorMessage,
							null));

		}
	}

	/**
	 * Performs a device count.
	 * 
	 * @return the int
	 */
	private int doDeviceCount() {
		int hostSize = -1;
		WSDeviceServiceLocator deviceLocator = new WSDeviceServiceLocator();
		try {
			String foundationURL = PropertyUtils.getProperty(
					ApplicationType.STATUS_VIEWER,
					CommonConstants.FOUNDATION_WS_URL_KEY);
			if (foundationURL == null)
				return hostSize;
			deviceLocator.setEndpointAddress("wsdevice", foundationURL
					+ "wsdevice");
			
			WSDevice wsDevice = deviceLocator.getdevice();
			Filter filter = new Filter("identification", FilterOperator.NE,
					"-1");
			WSFoundationCollection col = wsDevice.getDevicesByCriteria(filter,
					null, -1, -1);
			hostSize = col.getTotalCount();
		} catch (Exception exc) {
			LOGGER.error(exc.getMessage());

		} // end try/catch

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Validation: Device count: " + hostSize);
		}

		return hostSize;
	}	

	/**
	 * Checks if Softlimit exceeded on both devices and the expiration dates.
	 *
	 *
	 * @return true, if checks if is soft limit exceeded
	 */
	public boolean isSoftLimitExceeded() {
		boolean result = false;

		// only do soft license if admin user
		try {
			/**
			 * # GWMON-11052 soft limit license properties:
			 # soft.limit.display.list: list of roles that will see softlimit message
			 # soft.limit.license.message: message for license warn date exceeded
			 # soft.limit.count.message: message for warning count exceeded
			 # soft.limit.bgcolor: back ground color
			 # soft.limit.txtcolor: color of text
			 # color values: red=#FA1A1A, white=#FFFFFF, yellow=#FFFF00, black=#000000
			 soft.limit.display.list=admin
			 soft.limit.license.message=warning period: %s exceeded license Expires: %s.
			 soft.limit.count.message=device soft limit:%d exceeded hard limit at:%d
			 soft.limit.bgcolor=#FFFF00
			 soft.limit.txtcolor=#FFFFFF

			 */
			String licMessage = PropertyUtils.getPropertyFromFilePath(FOUNDATION_CONFIG_PATH,"soft.limit.license.message");
			if (licMessage == null) {
				licMessage = "warning period: %s exceeded license Expires: %s.";
			}

			// GWMON-9697 add url refs to license and dev count FAQ
			String licDateUrl = PropertyUtils.getPropertyFromFilePath(FOUNDATION_CONFIG_PATH,"soft.limit.license.datefaq.url");
			if (licDateUrl == null) {
				licDateUrl = "https://kb.groundworkopensource.com/pages/viewpage.action?pageId=13798193";
			}
			if (!licDateUrl.equalsIgnoreCase("NONE"))
				licMessage = licMessage + "&nbsp;" + "<a href=\"" + licDateUrl + "\"><u>Click here for more info</u></a>";
			String countMessage = PropertyUtils.getPropertyFromFilePath(FOUNDATION_CONFIG_PATH,"soft.limit.count.message");
			if (countMessage == null) {
				countMessage = "device soft limit:%d exceeded hard limit at:%d.";
			}

			// GWMON-9697 add url refs to license and dev count FAQ
			String licCountUrl = PropertyUtils.getPropertyFromFilePath(FOUNDATION_CONFIG_PATH,"soft.limit.license.countfaq.url");
			if (licCountUrl == null) {
				licCountUrl = "https://kb.groundworkopensource.com/pages/viewpage.action?pageId=13797325";
			}
			if (!licCountUrl.equalsIgnoreCase("NONE"))
				countMessage = countMessage + "&nbsp;" + "<a href=\"" + licCountUrl + "\"><u>Click here for more info</u></a>";
			this.softlimitbgcolor = PropertyUtils.getPropertyFromFilePath(FOUNDATION_CONFIG_PATH,"soft.limit.bgcolor");
			if (this.softlimitbgcolor == null) {
				this.softlimitbgcolor = "#FFFF00";
			}
			this.softlimittxtcolor = PropertyUtils.getPropertyFromFilePath(FOUNDATION_CONFIG_PATH,"soft.limit.txtcolor");
			if (this.softlimittxtcolor == null) {
				this.softlimittxtcolor = "#FFFFFF";
			}

			try {
				if (licenseKey != null
						&& licenseKey.getSoftLimitExpirationDate() != null) {

					String warnMessage = "";
					int devSoftLimit = Integer.parseInt(licenseKey.getSoftLimitDevices());
					int devHardLimit = Integer.parseInt(licenseKey.getHardLimitDevices());
					String softLimitDate = licenseKey.getSoftLimitExpirationDate();
					String hardLimitDate = licenseKey.getHardLimitExpirationDate();
					Date now = Calendar.getInstance().getTime();
					Date softLimitExpirationDate = DateFormat.getDateTimeInstance().parse(softLimitDate);
					int hostSize = doDeviceCount();
					if (validationRules != null
							&& validationRules.indexOf("param_5") != -1
							&& hostSize > devSoftLimit) {
						warnMessage = String.format(countMessage, devSoftLimit, devHardLimit);
						result = true;
					}
					if (validationRules != null
							&& validationRules.indexOf("param_7") != -1
							&& now.after(softLimitExpirationDate)) {
						if (warnMessage.length() > 0)
							warnMessage = String.format(licMessage,	softLimitDate,
									hardLimitDate) + "&nbsp;<br>&nbsp;" + warnMessage;
						else
							warnMessage = String.format(licMessage,	softLimitDate, hardLimitDate);

						result = true;
					}

					this.softLimitMessage = warnMessage;
				} // end if
			} catch (ParseException exc) {
				LOGGER.error("Unable to parse the date");
			}
		} catch (IOException ex) {
			LOGGER.error("Unable to get ");
		}
		return result;
	}
	
	/**
	 * Just a flag for softlimit message
	 */
	public String getSoftLimitMessage() {
		return this.softLimitMessage;
	}
	public String getSoftLimitbgColor() { return this.softlimitbgcolor; }
	public String getSoftLimittxtColor() { return this.softlimittxtcolor; }

	/**
	 * Checks if Hardlimit exceeded on both devices and the expiration dates.
	 * 
	 * @return true, if checks if is hard limit exceeded
	 */
	public boolean isHardLimitExceeded() {
		boolean result = false;

		try {
			if (licenseKey != null
					&& licenseKey.getHardLimitExpirationDate() != null) {

				Date now = Calendar.getInstance().getTime();
				Date hardLimitExpirationDate = DateFormat.getDateTimeInstance()
						.parse(licenseKey.getHardLimitExpirationDate());

				int hostSize = doDeviceCount();
				if (validationRules != null
						&& validationRules.indexOf("param_6") != -1
						&& hostSize > Integer.parseInt(licenseKey
								.getHardLimitDevices())) {
					result = true;
				}
				if (validationRules != null
						&& validationRules.indexOf("param_8") != -1
						&& now.after(hardLimitExpirationDate)) {
					result = true;
				}
			} // end if
		} catch (ParseException exc) {
			LOGGER.error("Unable to parse the date");
		}
		return result;
	}

	/**
	 * Gets the license key string.
	 * 
	 * @return the license key string
	 */
	public String getLicenseKeyString() {
		return licenseKeyString;
	}

	/**
	 * Sets the license key string.
	 * 
	 * @param licenseKeyString
	 *            the new license key string
	 */
	public void setLicenseKeyString(String licenseKeyString) {
		this.licenseKeyString = licenseKeyString;
	}
	
	/**
	 * Gets the errorMessage.
	 * 
	 * @return the error message string
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * Sets the errorMessage string.
	 * 
	 * @param errorMessage
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * Gets the install info.
	 * 
	 * @return the install info
	 */
	public InstallInfoBean getInstallInfo() {
		return installInfo;
	}

	/**
	 * Sets the install info.
	 * 
	 * @param installInfo
	 *            the new install info
	 */
	public void setInstallInfo(InstallInfoBean installInfo) {
		this.installInfo = installInfo;
	}

	/**
	 * Gets the server time.
	 * 
	 * @return the server time
	 */
	public String getServerTime() {
		return DateFormat.getDateTimeInstance().format(
				Calendar.getInstance().getTime());
	}

	/**
	 * Sets the server time.
	 * 
	 * @param serverTime
	 *            the new server time
	 */
	public void setServerTime(String serverTime) {
		this.serverTime = serverTime;
	}

	/**
	 * Read file as string.
	 * 
	 * @param filePath
	 *            the name of the file to open.
	 * 
	 * @return the string
	 */
	private String readFileAsString(String filePath) {
		StringBuffer fileData = new StringBuffer(CommonConstants.THOUSAND);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(filePath));
			char[] buf = new char[ONE_KB];
			int numRead = 0;
			while ((numRead = reader.read(buf)) != -1) {
				String readData = String.valueOf(buf, 0, numRead);
				fileData.append(readData);
				buf = new char[ONE_KB];
			} // end while
		} catch (Exception exc) {
			LOGGER.error(exc.getMessage());
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (Exception exc) {
					LOGGER.error(exc.getMessage());
				} // end try/catch
			} // end if
		} // end try/catch/finally
		return fileData.toString();
	}

}
