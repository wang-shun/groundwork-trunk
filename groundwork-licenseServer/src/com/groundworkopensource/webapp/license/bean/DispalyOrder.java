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

package com.groundworkopensource.webapp.license.bean;

/**
 * This bean is used to display Order information On UI
 * 
 * @author manish_kjain
 * 
 */
public class DispalyOrder {

    /**
     * StartDate
     */
    private String startDate;

    /**
     * expiryDate
     */
    private String expiryDate;

    /**
     * hardLimitExpiryDate
     */
    private String hardLimitExpiryDate;

    /**
     * softLimitDevice
     */
    private Short softLimitDevice;

    /**
     * hardLimitDevice
     */
    private Short hardLimitDevice;
    /**
     * productVersion
     */
    private String productVersion;

    /**
     * networkServiceRequired
     */
    private String networkServiceRequired;

    /**
     * licenseKey
     */
    private String licenseKey;

    /**
     
     */
    public DispalyOrder() {
        super();
        setStartDate("");
        this.expiryDate = "";
        // this.hardLimitDevice = "";
        this.hardLimitExpiryDate = "";
        this.licenseKey = "";
        this.networkServiceRequired = "";
        this.productVersion = "";
        // this.softLimitDevice = "";
    }

    /**
     * Returns the expiryDate.
     * 
     * @return the expiryDate
     */
    public String getExpiryDate() {
        return expiryDate;
    }

    /**
     * Sets the expiryDate.
     * 
     * @param expiryDate
     *            the expiryDate to set
     */
    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    /**
     * Returns the hardLimitExpiryDate.
     * 
     * @return the hardLimitExpiryDate
     */
    public String getHardLimitExpiryDate() {
        return hardLimitExpiryDate;
    }

    /**
     * Sets the hardLimitExpiryDate.
     * 
     * @param hardLimitExpiryDate
     *            the hardLimitExpiryDate to set
     */
    public void setHardLimitExpiryDate(String hardLimitExpiryDate) {
        this.hardLimitExpiryDate = hardLimitExpiryDate;
    }

    /**
     * Returns the softLimitDevice.
     * 
     * @return the softLimitDevice
     */
    public Short getSoftLimitDevice() {
        return softLimitDevice;
    }

    /**
     * Sets the softLimitDevice.
     * 
     * @param softLimitDevice
     *            the softLimitDevice to set
     */
    public void setSoftLimitDevice(Short softLimitDevice) {
        this.softLimitDevice = softLimitDevice;
    }

    /**
     * Returns the hardLimitDevice.
     * 
     * @return the hardLimitDevice
     */
    public Short getHardLimitDevice() {
        return hardLimitDevice;
    }

    /**
     * Sets the hardLimitDevice.
     * 
     * @param hardLimitDevice
     *            the hardLimitDevice to set
     */
    public void setHardLimitDevice(Short hardLimitDevice) {
        this.hardLimitDevice = hardLimitDevice;
    }

    /**
     * Returns the productVersion.
     * 
     * @return the productVersion
     */
    public String getProductVersion() {
        return productVersion;
    }

    /**
     * Sets the productVersion.
     * 
     * @param productVersion
     *            the productVersion to set
     */
    public void setProductVersion(String productVersion) {
        this.productVersion = productVersion;
    }

    /**
     * Returns the networkServiceRequired.
     * 
     * @return the networkServiceRequired
     */
    public String getNetworkServiceRequired() {
        return networkServiceRequired;
    }

    /**
     * Sets the networkServiceRequired.
     * 
     * @param networkServiceRequired
     *            the networkServiceRequired to set
     */
    public void setNetworkServiceRequired(String networkServiceRequired) {
        this.networkServiceRequired = networkServiceRequired;
    }

    /**
     * Returns the licenseKey.
     * 
     * @return the licenseKey
     */
    public String getLicenseKey() {
        return licenseKey;
    }

    /**
     * Sets the licenseKey.
     * 
     * @param licenseKey
     *            the licenseKey to set
     */
    public void setLicenseKey(String licenseKey) {
        this.licenseKey = licenseKey;
    }

    /**
     * Sets the startDate.
     * 
     * @param startDate
     *            the startDate to set
     */
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    /**
     * Returns the startDate.
     * 
     * @return the startDate
     */
    public String getStartDate() {
        return startDate;
    }

}
