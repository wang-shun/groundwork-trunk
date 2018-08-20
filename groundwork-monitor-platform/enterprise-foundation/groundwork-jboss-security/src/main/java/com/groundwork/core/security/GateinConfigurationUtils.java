/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2014  GroundWork Open Source Solutions info@groundworkopensource.com

 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.

 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.

 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.groundwork.core.security;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.LocatorImpl;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

/**
 * GateinConfigurationUtils
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public class GateinConfigurationUtils {

    public static final File GROUNDWORK_INSTALL_DIR = new File("/usr/local/groundwork");
    public static final File GATEIN_PROPERTIES_FILE = new File(GROUNDWORK_INSTALL_DIR, "config/gatein.properties");
    public static final String SUPER_USER_PROPERTY = "super.user";
    public static final File PORTAL_WAR_FILE = new File(GROUNDWORK_INSTALL_DIR, "jpp/gatein/gatein.ear/portal.war");
    public static final String PORTAL_CONFIGURATION_FILEPATH = "WEB-INF/conf/portal/portal-configuration.xml";

    /**
     * Update JBoss portal UserACL super.user init param in portal configuration
     * if changed. Retrieves super.user setting from gatein.properties file. Silently
     * returns if setting not in properties file.
     *
     * @return returns true if updated or false if not updated or super.user setting
     * not in gatein.properties.
     */
    public static boolean updateUserACLSuperUserInitParam() {
        return updateUserACLSuperUserInitParam(null);
    }

    /**
     * Update JBoss portal UserACL super.user init param in portal configuration
     * if changed. If the super user is not specified, the super.user setting
     * is retrieved from the gatein.properties file. Silently returns if super user
     * is not specified and setting not in properties file.
     *
     * @param superUser super user setting or null to use getein.properties
     * @return returns true if updated or false if not updated or super.user setting
     * not in gatein.properties and not specified.
     */
    public static boolean updateUserACLSuperUserInitParam(String superUser) {

        // get super user name from gatein properties if not specified
        if (superUser == null) {
            if (!GATEIN_PROPERTIES_FILE.isFile() || !GATEIN_PROPERTIES_FILE.canRead()) {
                throw new RuntimeException("Cannot read gatein.properties file: "+GATEIN_PROPERTIES_FILE);
            }
            FileReader gateinPropertiesReader = null;
            try {
                gateinPropertiesReader = new FileReader(GATEIN_PROPERTIES_FILE);
                Properties gateinProperties = new Properties();
                gateinProperties.load(gateinPropertiesReader);
                superUser = gateinProperties.getProperty(SUPER_USER_PROPERTY);
                if (superUser == null) {
                    return false;
                }
            } catch (IOException ioe) {
                throw new RuntimeException("Error reading gatein.properties file: "+GATEIN_PROPERTIES_FILE);
            } finally {
                if (gateinPropertiesReader != null) {
                    try {
                        gateinPropertiesReader.close();
                    } catch (IOException ioe) {
                    }
                }
            }
        }

        // update super user in portal war configuration
        if (!PORTAL_WAR_FILE.isFile() || !PORTAL_WAR_FILE.canRead() || !PORTAL_WAR_FILE.canWrite()) {
            throw new RuntimeException("Cannot read or update portal war file: "+PORTAL_WAR_FILE);
        }
        JarFile portalWarFile = null;
        File updatedPortalWarFile = null;
        JarOutputStream updatedPortalWarFileStream = null;
        try {
            // test portal configuration portal war file entry for super user update
            portalWarFile = new JarFile(PORTAL_WAR_FILE);
            JarEntry portalWarFileEntry = portalWarFile.getJarEntry(PORTAL_CONFIGURATION_FILEPATH);
            if (portalWarFileEntry == null) {
                throw new RuntimeException("Cannot find portal configuration in portal war file: "+PORTAL_WAR_FILE+":"+PORTAL_CONFIGURATION_FILEPATH);
            }
            InputStream portalConfigurationEntryInputStream = portalWarFile.getInputStream(portalWarFileEntry);
            try {
                if (!updatePortalConfiguration(superUser, portalConfigurationEntryInputStream, null)) {
                    return false;
                }
            } finally {
                portalConfigurationEntryInputStream.close();
            }

            // copy portal war file entries into a temporary updated war file, updating
            // portal configuration in the process
            updatedPortalWarFile = File.createTempFile("updated-portal-war", ".tmp", GROUNDWORK_INSTALL_DIR);
            updatedPortalWarFileStream = new JarOutputStream(new FileOutputStream(updatedPortalWarFile));
            Enumeration<JarEntry> portalWarFileEntries = portalWarFile.entries();
            while (portalWarFileEntries.hasMoreElements()) {
                portalWarFileEntry = portalWarFileEntries.nextElement();
                if (portalWarFileEntry.getName().equals(PORTAL_CONFIGURATION_FILEPATH)) {
                    // update portal configuration portal war file entry
                    updatedPortalWarFileStream.putNextEntry(new JarEntry(portalWarFileEntry.getName()));
                    portalConfigurationEntryInputStream = portalWarFile.getInputStream(portalWarFileEntry);
                    try {
                        if (!updatePortalConfiguration(superUser, portalConfigurationEntryInputStream, updatedPortalWarFileStream)) {
                            throw new RuntimeException("Configuration in portal war file not updated: "+PORTAL_WAR_FILE+":"+PORTAL_CONFIGURATION_FILEPATH);
                        }
                    } finally {
                        portalConfigurationEntryInputStream.close();
                        updatedPortalWarFileStream.flush();
                    }
                } else {
                    // copy portal war file entry
                    updatedPortalWarFileStream.putNextEntry(portalWarFileEntry);
                    InputStream copyEntryInputStream = portalWarFile.getInputStream(portalWarFileEntry);
                    try {
                        readFully(copyEntryInputStream, updatedPortalWarFileStream);
                    } finally {
                        copyEntryInputStream.close();
                        updatedPortalWarFileStream.flush();
                    }
                }
            }
            updatedPortalWarFileStream.close();
            updatedPortalWarFileStream = null;
            portalWarFile.close();
            portalWarFile = null;
            if (!updatedPortalWarFile.renameTo(PORTAL_WAR_FILE)) {
                throw new RuntimeException("Unable to replace portal war file: "+PORTAL_WAR_FILE);
            }
            updatedPortalWarFile = null;
            return true;
        } catch (IOException ioe) {
            throw new RuntimeException("Error updating portal war file: "+PORTAL_WAR_FILE);
        } finally {
            if (portalWarFile != null) {
                try {
                    portalWarFile.close();
                } catch (IOException ioe) {
                }
            }
            if (updatedPortalWarFileStream != null) {
                try {
                    updatedPortalWarFileStream.close();
                } catch (IOException ioe) {
                }
            }
            if (updatedPortalWarFile != null) {
                updatedPortalWarFile.delete();
            }
        }
    }

    /**
     * Update super user setting in portal configuration file. Can be dryrun to
     * test super user setting by not specifying a writer for the updated file.
     *
     * @param superUser super user setting
     * @param portalConfigurationInputStream portal configuration input stream
     * @param portalConfigurationOutputStream portal configuration output stream or null for dryrun
     * @return update/updated flag
     * @throws IOException
     */
    private static boolean updatePortalConfiguration(String superUser, InputStream portalConfigurationInputStream, OutputStream portalConfigurationOutputStream) throws IOException {
        // buffer portal configuration
        ByteArrayOutputStream bufferedPortalConfigurationOutputStream = new ByteArrayOutputStream();
        readFully(portalConfigurationInputStream, bufferedPortalConfigurationOutputStream);
        byte [] bufferedPortalConfigurationBytes = bufferedPortalConfigurationOutputStream.toByteArray();

        // parse portal configuration
        final Locator [] userACLSuperUserValueLocations = new Locator[2];
        final String [] userACLSuperUserValue = new String[1];
        final SAXException stopException = new SAXException("stop");
        try {
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            saxParserFactory.setNamespaceAware(true);
            SAXParser saxParser = saxParserFactory.newSAXParser();
            XMLReader xmlReader = saxParser.getXMLReader();
            xmlReader.setContentHandler(new DefaultHandler() {
                private Locator locator;
                private StringBuilder characters = new StringBuilder();
                private boolean component;
                private boolean userACLComponent;
                private boolean initParams;
                private boolean valueParam;
                private boolean userACLSuperUserValueParam;
                private boolean captureUserACLSuperUserValue;

                @Override
                public void setDocumentLocator(Locator locator) {
                    this.locator = locator;
                }

                @Override
                public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
                    characters.setLength(0);
                    if (localName.equals("component")) {
                        component = true;
                    } else if (userACLComponent) {
                        if (component && localName.equals("init-params")) {
                            initParams = true;
                        } else if (initParams && localName.equals("value-param")) {
                            valueParam = true;
                        } else if (userACLSuperUserValueParam && localName.equals("value")) {
                            userACLSuperUserValueLocations[0] = new LocatorImpl(locator);
                            userACLSuperUserValueLocations[1] = new LocatorImpl(locator);
                            captureUserACLSuperUserValue = true;
                        }
                    }
                }

                @Override
                public void endElement(String uri, String localName, String qName) throws SAXException {
                    if (localName.equals("component")) {
                        component = false;
                        userACLComponent = false;
                    } else if (component && localName.equals("key") && characters.toString().trim().equals("org.exoplatform.portal.config.UserACL")) {
                        userACLComponent = true;
                    } else if (userACLComponent) {
                        if (initParams && localName.equals("init-params")) {
                            initParams = false;
                        } else if (valueParam && localName.equals("value-param")) {
                            valueParam = false;
                            userACLSuperUserValueParam = false;
                        } else if (valueParam && localName.equals("name") && characters.toString().trim().equals("super.user")) {
                            userACLSuperUserValueParam = true;
                        } else if (userACLSuperUserValueParam && localName.equals("value")) {
                            userACLSuperUserValue[0] = characters.toString().trim();
                            throw stopException;
                        }
                    }
                    characters.setLength(0);
                }

                @Override
                public void characters(char[] ch, int start, int length) throws SAXException {
                    characters.append(ch, start, length);
                    if (captureUserACLSuperUserValue) {
                        userACLSuperUserValueLocations[1] = new LocatorImpl(locator);
                    }
                }

                @Override
                public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
                    if (captureUserACLSuperUserValue) {
                        userACLSuperUserValueLocations[1] = new LocatorImpl(locator);
                    }
                }
            });
            xmlReader.parse(new InputSource(new ByteArrayInputStream(bufferedPortalConfigurationBytes)));
        } catch (IOException ioe) {
            throw ioe;
        } catch (Exception e) {
            if (e != stopException) {
                throw new IOException("Unexpected error parsing portal configuration file: " + e, e);
            }
        }
        if (userACLSuperUserValue[0] == null) {
            throw new RuntimeException("Cannot update portal configuration without existing super user declaration");
        }

        // return after copying if portal configuration does not need update or if dryrun
        boolean update = ((userACLSuperUserValue[0] == null) || !userACLSuperUserValue[0].equals(superUser));
        if (portalConfigurationOutputStream == null) {
            return update;
        }
        if (!update) {
            readFully(new ByteArrayInputStream(bufferedPortalConfigurationBytes), portalConfigurationOutputStream);
            return update;
        }

        // update portal configuration
        BufferedReader portalConfigurationReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bufferedPortalConfigurationBytes)));
        PrintWriter portalConfigurationWriter = new PrintWriter(new OutputStreamWriter(portalConfigurationOutputStream));
        boolean updated = false;
        int lineNumber = 1;
        for (String line = null; ((line = portalConfigurationReader.readLine()) != null); lineNumber++) {
            if (!updated && (lineNumber >= userACLSuperUserValueLocations[0].getLineNumber())) {
                int replaceStart = ((lineNumber == userACLSuperUserValueLocations[0].getLineNumber()) ? userACLSuperUserValueLocations[0].getColumnNumber()-1 : 0);
                int replaceEnd = ((lineNumber == userACLSuperUserValueLocations[1].getLineNumber()) ? userACLSuperUserValueLocations[1].getColumnNumber()-1 : line.length());
                int replaceIndex = line.indexOf(userACLSuperUserValue[0], replaceStart);
                if ((replaceIndex != -1) && (replaceIndex <= replaceEnd-userACLSuperUserValue[0].length())) {
                    line = line.substring(0, replaceIndex)+superUser+line.substring(replaceIndex+userACLSuperUserValue[0].length());
                    updated = true;
                }
                if (!updated && (lineNumber >= userACLSuperUserValueLocations[1].getLineNumber())) {
                    throw new RuntimeException("Failed to update portal configuration");
                }
            }
            portalConfigurationWriter.println(line);
        }
        portalConfigurationWriter.flush();
        return updated;
    }

    /**
     * Read input stream fully into output stream.
     *
     * @param inputStream source input stream
     * @param outputStream sink output stream
     * @throws IOException
     */
    private static void readFully(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] copyBuffer = new byte[4096];
        for (int read = -1; ((read = inputStream.read(copyBuffer)) != -1); ) {
            outputStream.write(copyBuffer, 0, read);
        }
    }

    /**
     * Main entry point for running the {@link #updateUserACLSuperUserInitParam(java.lang.String)}
     * utility. Usage:
     *
     * java -cp /usr/local/groundwork/jpp/modules/com/groundwork/security/main/groundwork-jboss-security-7.1.0.jar com.groundwork.core.security.GateinConfigurationUtils [-superuser <name>]
     *
     * @param args command line args
     */
    public static void main(String [] args) {
        // parse args
        String superUser = null;
        for (int i = 0; (i < args.length); i++) {
            if (args[i].equalsIgnoreCase("-superuser") && (i+1 < args.length)) {
                superUser = args[++i];
            } else {
                System.err.println("Unrecognized option or option syntax: "+args[i]);
                System.exit(1);
            }
        }

        // update super user
        try {
            if (updateUserACLSuperUserInitParam(superUser)) {
                System.out.println("Updated super user in portal war file portal configuration");
            } else {
                System.out.println("Super user already set in portal war file portal configuration");
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
