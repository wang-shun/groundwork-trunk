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

import java.io.File;
import java.io.IOException;

import net.padlocksoftware.license.ImportException;
import net.padlocksoftware.license.License;
import net.padlocksoftware.license.LicenseIO;
import net.padlocksoftware.license.Validator;

import org.apache.log4j.Logger;

/**
 * This is simple bean which contains the encrypted license key data.
 * 
 * @author Arul Shanmugam
 */
public class LicenseKeyBean {

    /** The Constant LICENSE_KEY_PATH. */
    private static final String LICENSE_KEY_PATH = "/usr/local/groundwork/config/groundwork.lic";

    /** logger. */
    private static final Logger LOGGER = Logger.getLogger(LicenseKeyBean.class
            .getName());

    /* License values Obfuscation variables */
    /** The xchars. */
    private static char[] xchars = { 0xA5, 0xD2, 0x69, 0xB4, 0x5A, 0x2D, 0x96,
            0x4B, 0 };

    // Hex encoding.
    // Why use 0123456789ABCDEF when lots more entropy is available?
    /** The echars. */
    private static char[] echars = { 'n', 'b', 'T', 'F', 'm', 'H', 's', 'a',
            'L', 'd', 'J', 'i', 'Y', 'V', 'R', 'w' };
    // mapping to reverse hex encoding
    /** The rev. */
    private static char[] rev = new char[256];

    /** The order id. */
    private String orderID = null;

    /** The install guid. */
    private String installGUID = null;

    /** The product name. */
    private String productName = null;

    /** The version. */
    private String version = null;

    /** The soft limit devices. */
    private String softLimitDevices = null;

    /** The hard limit devices. */
    private String hardLimitDevices = null;

    /** The start date. */
    private String startDate = null;

    /** The soft limit expiration date. */
    private String softLimitExpirationDate = null;

    /** The hard limit expiration date. */
    private String hardLimitExpirationDate = null;

    /** The validation rules. */
    private String validationRules = null;

    /** The network service reqd. */
    private String networkServiceReqd = null;

    /** The pub key. */
    private String pubKey = null;

    /** The validator. */
    private Validator validator = null;

    /** The sku. */
    private String sku = null;

    /**
     * Instantiates a new license key bean.
     */
    public LicenseKeyBean() {
        init();
    }

    /**
     * Inits the.
     */
    public void init() {
    	 License license = null;
        try {
            /* Initialize arrays for encryption */
            int i;
            for (i = 256; --i >= 0;) {
                // First, fill the entire array with invalid values,
                // to detect any inappropriate incoming values.
                rev[i] = 0x00ff;
            }
            for (i = echars.length; --i >= 0;) {
                // Then populate the positions we actually care about.
                rev[echars[i]] = (char) i;
            }

            license = LicenseIO
                    .importLicense(new File(LICENSE_KEY_PATH));
            pubKey = license.getProperty("param_10");
            version = this.decrypt(license.getProperty("param_1"));
            sku = this.decrypt(license.getProperty("param_2"));
            networkServiceReqd = this.decrypt(license.getProperty("param_3"));
            productName = this.decrypt(license.getProperty("param_4"));
            installGUID = this.decrypt(license.getProperty("param_9"));
            softLimitDevices = this.decrypt(license.getProperty("param_5"));
            hardLimitDevices = this.decrypt(license.getProperty("param_6"));
            softLimitExpirationDate = this.decrypt(license
                    .getProperty("param_7"));
            hardLimitExpirationDate = this.decrypt(license
                    .getProperty("param_8"));
            validationRules = this.decrypt(license.getProperty("param_11"));
            startDate = this.decrypt(license.getProperty("param_12"));
            orderID = license.getProperty("orderID");
            
        } catch (IOException ex) {
            LOGGER.error("No License file found.");
            this.reset();
        } catch (ImportException ex) {
            LOGGER.error("Invalid License file found.");
            this.reset();
        } finally
        {
        	if (license != null && pubKey!= null)
        		validator = new Validator(license, pubKey);
        }
    }

    /**
     * Resets the values to null.
     */
    private void reset() {

        installGUID = null;
        productName = null;
        version = null;
        softLimitDevices = null;
        hardLimitDevices = null;
        softLimitExpirationDate = null;
        hardLimitExpirationDate = null;
        validationRules = null;
        networkServiceReqd = null;
        //pubKey = null;
        validator = null;
        startDate = null;
        sku = null;
        orderID = null;
    }

    /**
     * Gets the install guid.
     * 
     * @return the install guid
     */
    public String getInstallGUID() {
        return installGUID;
    }

    /**
     * Sets the install guid.
     * 
     * @param installGUID
     *            the new install guid
     */
    public void setInstallGUID(String installGUID) {
        this.installGUID = installGUID;
    }

    /**
     * Gets the product name.
     * 
     * @return the product name
     */
    public String getProductName() {
        return productName;
    }

    /**
     * Sets the product name.
     * 
     * @param productName
     *            the new product name
     */
    public void setProductName(String productName) {
        this.productName = productName;
    }

    /**
     * Gets the soft limit devices.
     * 
     * @return the soft limit devices
     */
    public String getSoftLimitDevices() {
        return softLimitDevices;
    }

    /**
     * Sets the soft limit devices.
     * 
     * @param softLimitDevices
     *            the new soft limit devices
     */
    public void setSoftLimitDevices(String softLimitDevices) {
        this.softLimitDevices = softLimitDevices;
    }

    /**
     * Gets the hard limit devices.
     * 
     * @return the hard limit devices
     */
    public String getHardLimitDevices() {
        return hardLimitDevices;
    }

    /**
     * Sets the hard limit devices.
     * 
     * @param hardLimitDevices
     *            the new hard limit devices
     */
    public void setHardLimitDevices(String hardLimitDevices) {
        this.hardLimitDevices = hardLimitDevices;
    }

    /**
     * Gets the soft limit expiration date.
     * 
     * @return the soft limit expiration date
     */
    public String getSoftLimitExpirationDate() {
        return softLimitExpirationDate;
    }

    /**
     * Sets the soft limit expiration date.
     * 
     * @param softLimitExpirationDate
     *            the new soft limit expiration date
     */
    public void setSoftLimitExpirationDate(String softLimitExpirationDate) {
        this.softLimitExpirationDate = softLimitExpirationDate;
    }

    /**
     * Gets the hard limit expiration date.
     * 
     * @return the hard limit expiration date
     */
    public String getHardLimitExpirationDate() {
        return hardLimitExpirationDate;
    }

    /**
     * Sets the hard limit expiration date.
     * 
     * @param hardLimitExpirationDate
     *            the new hard limit expiration date
     */
    public void setHardLimitExpirationDate(String hardLimitExpirationDate) {
        this.hardLimitExpirationDate = hardLimitExpirationDate;
    }

    /**
     * Gets the validation rules.
     * 
     * @return the validation rules
     */
    public String getValidationRules() {
        return validationRules;
    }

    /**
     * Sets the validation rules.
     * 
     * @param validationRules
     *            the new validation rules
     */
    public void setValidationRules(String validationRules) {
        this.validationRules = validationRules;
    }

    /**
     * Gets the network service reqd.
     * 
     * @return the network service reqd
     */
    public String getNetworkServiceReqd() {
        return networkServiceReqd;
    }

    /**
     * Sets the network service reqd.
     * 
     * @param networkServiceReqd
     *            the new network service reqd
     */
    public void setNetworkServiceReqd(String networkServiceReqd) {
        this.networkServiceReqd = networkServiceReqd;
    }

    /**
     * Gets the pub key.
     * 
     * @return the pub key
     */
    public String getPubKey() {
        return pubKey;
    }

    /**
     * Sets the pub key.
     * 
     * @param pubKey
     *            the new pub key
     */
    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    /**
     * Gets the start date.
     * 
     * @return the start date
     */
    public String getStartDate() {
        return startDate;
    }

    /**
     * Sets the start date.
     * 
     * @param startDate
     *            the new start date
     */
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    /**
     * Gets the validator.
     * 
     * @return the validator
     */
    public Validator getValidator() {
        return validator;
    }

    /**
     * Sets the validator.
     * 
     * @param validator
     *            the new validator
     */
    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    /**
     * Decrypts the string.
     * 
     * @param hex
     *            the hex
     * 
     * @return the string
     * 
     * @throws Exception
     *             the exception
     */
    private String decrypt(String hex) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("String to decrypt: " + hex);
           
        }

        int buflen = hex.length();
        int len = buflen / 2;
        if (len * 2 != buflen) {
            LOGGER.error(
                    "Decrypt string failed. Not a valid character used!");
            return "CANNOT_DECRYPT";
        }

        char[] buf = hex.toCharArray();
        char[] str = new char[len];
        int s;
        int l = len;
        int h = buflen;
        for (s = len; --s >= 0;) {
            char hi = rev[buf[--h]];
            char lo = rev[buf[--l]];
            if (hi == 0x00ff || lo == 0x00ff) {
            	 LOGGER.error(
                        "Decrypt string failed. Not a valid character used!");
            	  return "CANNOT_DECRYPT";
            }
            str[s] = (char) ((hi << 4) + lo);
        }

        for (s = len - 1; --s >= 0;) {
            str[s + 1] -= str[s];
        }
        for (s = 1; s < len; ++s) {
            str[s - 1] -= str[s];
        }

        int x = 0;
        for (s = 0; s < len; ++s) {
            str[s] ^= xchars[x++];
            str[s] -= str[s] << 4;
            str[s] &= 0x00ff;
            if (xchars[x] == 0) {
                x = 0;
            }
        }
        return new String(str);

    }

    /**
     * Gets the version.
     * 
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the version.
     * 
     * @param version
     *            the new version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Gets the sku.
     * 
     * @return the sku
     */
    public String getSku() {
        return sku;
    }

    /**
     * Sets the sku.
     * 
     * @param sku
     *            the new sku
     */
    public void setSku(String sku) {
        this.sku = sku;
    }

    /**
     * Gets the order id.
     * 
     * @return the order id
     */
    public String getOrderID() {
        return orderID;
    }

    /**
     * Sets the order id.
     * 
     * @param orderID
     *            the new order id
     */
    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }
}
