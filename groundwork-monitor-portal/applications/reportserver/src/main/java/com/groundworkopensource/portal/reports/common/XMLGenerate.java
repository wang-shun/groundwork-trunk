/*
 * Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork) All rights
 * reserved. Use is subject to GroundWork commercial license terms.
 */

package com.groundworkopensource.portal.reports.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.groundworkopensource.portal.common.ApplicationType;
import com.groundworkopensource.portal.common.PropertyUtils;

/**
 * This class is used to generate XML structure from the report directory.
 * 
 * @author manish_kjain
 */
public class XMLGenerate {

    /**
     * blank space SPACE
     */
    private static final String SPACE = " ";
    /**
     * GW constant
     */
    private static final String GW = "gw ";
    /**
     * UNDER_SCORE
     */
    private static final String UNDER_SCORE = "_";
    /**
     * BAK
     */
    private static final String BAK = ".bak";
    /**
     * DOT
     */
    private static final String DOT = ".";

    /**
     * DASH
     */
    private static final String DASH = "-";

    /**
     * Directory name constant.
     */
    public static final String DIR_NAME = "Dirname";

    /**
     * create object of Document.
     */
    private Document xmldoc = null;

    /**
     * create the element object.
     */
    private Element root = null;

    /**
     * XML constant.
     */
    private static final String XML = "XML";

    /**
     * Encoding constant.
     */
    private static final String ISO_8859_1 = "ISO-8859-1";

    /**
     * constant DISPLAY_NAME.
     */
    private static final String DISPLAY_NAME = "display-name";

    /**
     * Report File Extension.
     */
    private static final String RPTDESIGN = ".rptdesign";

    /**
     * variable of Element class.
     */
    private Element element = null;

    /**
     * create the logger.
     */

    /**
     * Logger.
     */
    private static Logger logger = Logger
            .getLogger(XMLGenerate.class.getName());

    /**
     * method is used to create the XML file.
     * 
     * @param uploadDir
     * 
     * @param fileName
     * @param String
     * @return boolean
     */

    public boolean addToXML(String uploadDir, String fileName) {

        // get the context parameter value from web.XML
        String reportFileName = PropertyUtils
                .getProperty(ApplicationType.REPORT_VIEWER,
                        ReportConstants.REPORTS_NAME_XML);
        File file = new File(reportFileName + ReportConstants.REPORT_EN_XML);
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
                .newInstance();
        DocumentBuilder documentBuilder;
        Document document = null;
        try {
            documentBuilder = docBuilderFactory.newDocumentBuilder();
            document = documentBuilder.parse(file);
            document.getDocumentElement().normalize();

        } catch (ParserConfigurationException e) {
            logger.error("Error occured while reading XML file in addToXML()");
        } catch (SAXException e) {
            logger.error("Error occured while reading XML file");
        } catch (IOException e) {
            logger.error("Error occured while reading XML file");
        }
        if (document == null) {
            return false;
        }

        NodeList elements = document
                .getElementsByTagName(ReportConstants.REPORT_DIR);

        if (elements != null) {
            for (int i = 0; i < elements.getLength(); i++) {
                Node node = elements.item(i);
                Node attribute = node.getAttributes().getNamedItem(
                        ReportConstants.DIR_NAME);
                if (attribute != null
                        && attribute.getNodeValue().equals(uploadDir)) {
                    Element reportFileElement = document.createElementNS(null,
                            ReportConstants.REPORT_FILE);
                    Element fileElement = document.createElementNS(null,
                            ReportConstants.DISPLAY_NAME);
                    String leafDispalyName;
                    try {
                        leafDispalyName = getFileDisplayName(fileName);
                    } catch (Exception e) {
                        logger
                                .error("Exception while Creating leaf Dispaly Name");
                        leafDispalyName = fileName;
                    }
                    Node n = document.createTextNode(leafDispalyName);
                    fileElement.appendChild(n);
                    Element fileObjectIdElement = document.createElementNS(
                            null, ReportConstants.FILE_OBJECT_ID);
                    Node n1 = document.createTextNode(fileName);
                    fileObjectIdElement.appendChild(n1);
                    reportFileElement.appendChild(fileElement);
                    reportFileElement.appendChild(fileObjectIdElement);
                    node.appendChild(reportFileElement);

                    // Write out to file using the serializer

                    OutputFormat out = new OutputFormat(document);
                    out.setIndenting(true);

                    XMLSerializer xmlSer;
                    try {
                        xmlSer = new XMLSerializer(new FileOutputStream(
                                new File(reportFileName
                                        + ReportConstants.REPORT_EN_XML)), out);
                        xmlSer.serialize(document);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        return false;
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * return the tree leaf display name.
     * 
     * @param name
     * @return String
     */
    private String getFileDisplayName(String name) {
        String displayName;
        displayName = name.substring(0, name.lastIndexOf(DOT));
        displayName = displayName.replaceAll(DASH, SPACE);
        displayName = displayName.replaceAll(UNDER_SCORE, SPACE);
        if (displayName.toLowerCase().startsWith(GW)) {
            displayName = displayName.substring(2);
            displayName = displayName.trim();
        }
        return displayName;
    }

    /**
     * Commented method for future use: PLEASE DO NOT DELETE!!
     * 
     * return the tree directory display name.
     * 
     * @param dname
     * @return String
     */

    // private String getDirDisplayName(String dname) {
    //
    // // TODO: temporary logic!! Please replace with proper one String name =
    // dname.substring(1);
    // char[] charray = dname.toCharArray();
    // int i;
    // for (i = 0; i < charray.length; i++) {
    // if (charray[i] > 'A' && charray[i] < 'Z') {
    // break;
    // }
    // }
    //
    // dname = dname.charAt(0) + dname.substring(1, i + 1) + " "
    // + dname.substring(i + 1);
    //
    // return dname;
    // }
    /**
     * return the tree directory display name.
     * 
     * @param dname
     * @return String
     */
    private static String getDirDisplayName(String dname) {

        // TODO: temporary logic!! Please replace with proper one
        String name = dname.substring(1);
        char[] charray = name.toCharArray();
        int i;
        for (i = 0; i < charray.length; i++) {
            if (charray[i] > 'A' && charray[i] < 'Z') {
                break;
            }
        }

        dname = dname.charAt(0) + dname.substring(1, i + 1) + SPACE
                + dname.substring(i + 1);

        return dname;
    }

    /**
     * method is used to create the XML file.
     */
    public void generateXML() {
        String contextPathXML = null;
        Properties loadPropertiesFromFilePath = PropertyUtils
                .loadPropertiesFromFilePath(ApplicationType.REPORT_VIEWER
                        .getDefaultPropertiesPath());
        if (loadPropertiesFromFilePath != null) {
            contextPathXML = loadPropertiesFromFilePath
                    .getProperty(ReportConstants.REPORTS_NAME_XML);
        }
        if (contextPathXML == null) {
            contextPathXML = ReportConstants.REPORT_XML_PATH;

        }
        logger.debug("contextPathXML:-" + contextPathXML);
        // back up existing Report XML file
        File file = new File(contextPathXML + ReportConstants.REPORT_EN_XML);
        if (file != null && file.exists()) {
            file.renameTo(new File(contextPathXML
                    + ReportConstants.REPORT_EN_XML + BAK));
        }
        xmldoc = new DocumentImpl();
        root = xmldoc.createElement(ReportConstants.REPORT);

        traverse(new File(contextPathXML));
        xmldoc.appendChild(root);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(contextPathXML
                    + ReportConstants.REPORT_EN_XML);

            OutputFormat of = new OutputFormat(XML, ISO_8859_1, true);
            of.setIndent(1);
            of.setIndenting(true);
            // of.setDoctype(null,"reportxml.dtd");
            XMLSerializer serializer = new XMLSerializer(fos, of);
            serializer.asDOMSerializer();
            serializer.serialize(xmldoc.getDocumentElement());
            fos.close();
        } catch (IOException ex) {
            logger.warn("Error Writing XML file to disk. ");
            // back up existing file

            File backupFile = new File(contextPathXML
                    + ReportConstants.REPORT_EN_XML + BAK);
            if (backupFile != null && backupFile.exists()) {
                backupFile.renameTo(new File(contextPathXML
                        + ReportConstants.REPORT_EN_XML));
            }
        }
        // delete backup file
        new File(contextPathXML + ReportConstants.REPORT_EN_XML + BAK).delete();

    }

    /**
     * method is used to traverse the file specified in argument and create the
     * element and node.
     * 
     * @param File
     */
    private void traverse(final File dir) {
        if (dir.isFile()) {
            if (dir.getName().endsWith(RPTDESIGN)) {

                Element reportFileElement = xmldoc.createElementNS(null,
                        ReportConstants.REPORT_FILE);
                Element fileElement = xmldoc
                        .createElementNS(null, DISPLAY_NAME);
                String leafDispalyName;
                try {
                    leafDispalyName = getFileDisplayName(dir.getName());
                } catch (Exception e) {
                    logger.error("Exception while Creating leaf Dispaly Name");
                    leafDispalyName = dir.getName();
                }

                Node n = xmldoc.createTextNode(leafDispalyName);
                fileElement.appendChild(n);
                Element fileObjectIdElement = xmldoc.createElementNS(null,
                        ReportConstants.FILE_OBJECT_ID);
                Node n1 = xmldoc.createTextNode(dir.getName());
                fileObjectIdElement.appendChild(n1);
                reportFileElement.appendChild(fileElement);
                reportFileElement.appendChild(fileObjectIdElement);
                element.appendChild(reportFileElement);
                root.appendChild(element);
            }
        }
        if (dir.isDirectory()) {
            element = xmldoc.createElementNS(null, ReportConstants.REPORT_DIR);
            element.setAttributeNS(null, DIR_NAME, dir.getName());
            element.setAttributeNS(null, DISPLAY_NAME, getDirDisplayName(dir
                    .getName()));
            // e = xmldoc.createElementNS(null, dir.getName());
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                traverse(new File(dir, children[i]));
            }
        }

    }

}
