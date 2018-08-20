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
package com.groundworkopensource.portal.statusviewer.common.eventbroker;

import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

import org.apache.log4j.Logger;

import com.groundworkopensource.portal.common.ApplicationType;
import com.groundworkopensource.portal.common.CommonConstants;
import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.common.PropertyUtils;
import com.groundworkopensource.portal.common.ResourceUtils;
import com.groundworkopensource.portal.common.exception.GWPortalGenericException;
import com.groundworkopensource.portal.statusviewer.common.Constant;

/**
 * This class encrypts the data using DES algorithm. Algorithm - DES, Mode -
 * CFB8, Padding - no
 * 
 * @author shivangi_walvekar
 * 
 */
public class Encryptor {

    /**
     * String constant for message "File created at "
     */
    public static final String FILE_CREATED_INFO_MSG = "File created at ";
    /**
     * String constant for "DES/CFB8/NoPadding"
     */
    public static final String DES_CFB8_NOPADDING = "DES/CFB8/NoPadding";

    /**
     * String constant for "txt"
     */
    public static final String FILE_EXT_TXT = ".txt";

    /**
     * String constant for "txt"
     */
    public static final String ENCRYPTED_FILE_NAME = "encryptedData";

    /**
     * String constant for "txt"
     */
    public static final String DECRYPTED_FILE_NAME = "decrypted";

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(Encryptor.class
            .getName());

    /**
     * key used for encrypting nagios commands to be sent to the event broker
     * from actions portlet
     */
    private static String encryptionKey;

    /**
     * Static initializer that reads the encryption related configuration
     * parameters (encryption algorithm,encryption key)
     */
    static {
        try {
            // read application type from web.xml
            String appType = FacesUtils
                    .getContextParam(CommonConstants.APPLICATION_TYPE_CONTEXT_PARAM_NAME);
            ApplicationType applicationType = ApplicationType
                    .getApplicationType(appType);
            // Read encryption key from application specific properties file
            encryptionKey = PropertyUtils.getProperty(applicationType,
                    CommonConstants.EVENT_BROKER_ENCRYPTION_KEY);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * This method encrypts the input with 'DES/CFB8/NoPadding'.
     * 
     * @param input
     * @param ivData
     * @return enc
     * @throws GWPortalGenericException
     */
    public byte[] encrypt(String input, byte[] ivData)
            throws GWPortalGenericException {
        DESKeySpec desKeySpec;
        // Create IvParameterSpec
        IvParameterSpec ivSpec = new IvParameterSpec(ivData);
        if ((encryptionKey == null) || (input == null)
                || (Constant.EMPTY_STRING.equals(input.trim()))) {
            throw new GWPortalGenericException(
                    ResourceUtils
                            .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_socketError"));
        }

        // Creating salt which will be used to generate DES key.
        byte[] salt = encryptionKey.getBytes();

        byte[] enc = input.getBytes();

        try {
            // Create instance of Cipher.
            Cipher cipher = Cipher.getInstance(DES_CFB8_NOPADDING);

            SecretKeyFactory secretKeyFactory = SecretKeyFactory
                    .getInstance(Constant.DES);

            // Create DES Key specification
            desKeySpec = new DESKeySpec(salt);

            // Generate secretKey using DES key Spec.
            SecretKey secretKey = secretKeyFactory.generateSecret(desKeySpec);

            // Initialize Cipher.
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

            byte[] bytes = input.getBytes();
            // encrypt
            enc = cipher.doFinal(bytes);
        } catch (GeneralSecurityException e) {
            LOGGER.error(e.getMessage());
            throw new GWPortalGenericException(
                    ResourceUtils
                            .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_socketError"));
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            throw new GWPortalGenericException(
                    ResourceUtils
                            .getLocalizedMessage("com_groundwork_portal_statusviewer_actionsPortlet_socketError"));
        }
        // LOGGER.debug("Encryption successful !");
        return enc;
    }
}
