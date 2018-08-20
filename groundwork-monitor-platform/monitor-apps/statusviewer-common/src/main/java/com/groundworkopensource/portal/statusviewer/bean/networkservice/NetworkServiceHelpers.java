/*
 * Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork) All rights
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
 * 
 * dname.pl - This is a utility script to clean up the foundation database for
 * device display names and identification fields that were inconsistently fed
 * into the database. This can cause some issues in the display for the event
 * console, especially when upgrading an older database. Use in consultation
 * with GroundWork Support!
 */

package com.groundworkopensource.portal.statusviewer.bean.networkservice;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Helper class containing (static) utility methods for file I/O operations.
 */
public class NetworkServiceHelpers {

    /**
     * LOGGER
     */
	private static final Logger LOGGER = Logger.getLogger(NetworkServiceHelpers.class.getName());

    /**
     * @param filePath
     * @return HTML text from file read
     */
    public static String readTextFileToHtml(String filePath) {
        String result = "";

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(filePath));
            String line = null;

            while ((line = bufferedReader.readLine()) != null) {
                result += line + "<br/>";
            }

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return null;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                return null;
            }
        }

        return result;
    }

    /**
     * @param filePath
     * @param text
     * @return -1 in case of exception, 0 for successfully writing text to file
     */
    public static Integer writeFile(String filePath, String text) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(filePath);
            writer.write(text);
        } catch (Exception e) {
            return -1;
        } finally {
            try {
                if (null != writer) {
                    writer.close();
                }
            } catch (IOException e) {
                return -1;
            }
        }
        return 0;
    }

    /**
     * @param filePath
     * @return Properties
     */
    public static Properties readPropertiesFromFile(String filePath) {
        Properties properties = new Properties();
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(filePath);
            properties.load(fileInputStream);
        } catch (IOException e) {
            return null;
        } finally {
            if (null != fileInputStream) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    LOGGER
                            .debug("IOException while closing fileInputStream. File path : "
                                    + filePath);
                }
            }
        }

        return properties;
    }

    /**
     * Protected Constructor - Rationale: Instantiating utility classes does not
     * make sense. Hence the constructors should either be private or (if you
     * want to allow sub-classing) protected. <br>
     * 
     * Refer to "HideUtilityClassConstructor" section in
     * http://checkstyle.sourceforge.net/config_design.html.
     */
    protected NetworkServiceHelpers() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }
}