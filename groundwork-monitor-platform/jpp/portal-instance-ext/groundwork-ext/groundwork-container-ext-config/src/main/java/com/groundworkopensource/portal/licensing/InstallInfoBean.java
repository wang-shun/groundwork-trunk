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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * This is simple bean which contains bitrock Install Info.
 * 
 * @author Arul Shanmugam
 */
public class InstallInfoBean {

    /** logger. */
    private static final Logger LOGGER = Logger.getLogger(InstallInfoBean.class
            .getName());

    /** The install guid. */
    private String installGUID = null;

    /** The product name. */
    private String productName = null;

    /** The version. */
    private String version = null;

    /** The network service reqd. */
    private String networkServiceReqd = null;

    /** The Constant BITROCK_INFO_TXT. */
    private static final String BITROCK_INFO_TXT = "/usr/local/groundwork/Info.txt";

    /**
     * Install info bean.
     */
    public InstallInfoBean() {

    }

    /**
     * Inits the.
     */
    public void init() {
        Properties infoProperties = new Properties();
        try {
            infoProperties.load(new FileInputStream(BITROCK_INFO_TXT));
            installGUID = infoProperties.getProperty("install_guid");
            productName = infoProperties.getProperty("name");
            version = infoProperties.getProperty("version");
            networkServiceReqd = infoProperties.getProperty("network_service");
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
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

}
