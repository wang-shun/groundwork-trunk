package org.groundwork.rs.utils;

import net.padlocksoftware.license.License;
import net.padlocksoftware.license.LicenseIO;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class PadlockReader {

    public static final String SOFT_LIMIT_DEVICES_PARAM_NAME = "param_5";
    public static final String SOFT_LIMIT_EXPIRATION_DATE_PARAM_NAME = "param_7";

    private static final Logger log = Logger.getLogger(PadlockReader.class.getName());

    /* License values Obfuscation variables */
    /** The xchars. */
    private static char[] xchars = { 0xA5, 0xD2, 0x69, 0xB4, 0x5A, 0x2D, 0x96,
            0x4B, 0 };

    // Hex encoding.
    // Why use 0123456789ABCDEF when lots more entropy is available?
    /** The echars. */
    private static char[] echars = { 'n', 'b', 'T', 'F', 'm', 'H', 's', 'a',
            'L', 'd', 'J', 'i', 'Y', 'V', 'R', 'w' };

    private static char[] rev = new char[256];

    public LicenseInfo readLicense(String path) throws IOException {
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

        License importLicense = LicenseIO.importLicense(new File(path));
        LicenseInfo license = new LicenseInfo();
        license.setPubKey(importLicense.getProperty("param_10"));
        license.setVersion(decrypt(importLicense.getProperty("param_1")));
        license.setSku(decrypt(importLicense.getProperty("param_2")));
        license.setNetworkServiceReqd(decrypt(importLicense.getProperty("param_3")));
        license.setProductName(decrypt(importLicense.getProperty("param_4")));
        license.setInstallGUID(decrypt(importLicense.getProperty("param_9")));
        license.setSoftLimitDevices(decrypt(importLicense.getProperty(SOFT_LIMIT_DEVICES_PARAM_NAME)));
        license.setHardLimitDevices(decrypt(importLicense.getProperty("param_6")));
        license.setSoftLimitExpirationDate(decrypt(importLicense.getProperty(SOFT_LIMIT_EXPIRATION_DATE_PARAM_NAME)));
        license.setHardLimitExpirationDate(decrypt(importLicense.getProperty("param_8")));
        license.setValidationRules(decrypt(importLicense.getProperty("param_11")));
        license.setStartDate(decrypt(importLicense.getProperty("param_12")));
        license.setOrderID(importLicense.getProperty("orderID"));
        return license;
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
        if (log.isDebugEnabled()) {
            log.debug("String to decrypt: " + hex);

        }

        int buflen = hex.length();
        int len = buflen / 2;
        if (len * 2 != buflen) {
            log.error(
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
                log.error(
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

}
