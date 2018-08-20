/*
 *  Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork)
 *  All rights reserved. Use is subject to GroundWork commercial license terms.
 */

package com.groundworkopensource.portal.reports.bean;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.groundworkopensource.portal.common.ApplicationType;
import com.groundworkopensource.portal.common.PropertyUtils;
import com.groundworkopensource.portal.common.ResourceUtils;
import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.reports.common.FacesUtils;
import com.groundworkopensource.portal.reports.common.IPCUtils;
import com.groundworkopensource.portal.reports.common.ReportConstants;
import com.groundworkopensource.portal.reports.common.XMLGenerate;
import com.icesoft.faces.async.render.SessionRenderer;
import com.icesoft.faces.component.inputfile.InputFile;

/**
 * The back-end bean for the publish report function.
 * 
 * @author nitin_jadhaav
 */

public class InputFileController {

    /**
     * PUBLISH_REPORT_ERROR_FILE_EXTENSION_ERROR
     */
    private static final String PUBLISH_REPORT_ERROR_FILE_EXTENSION_ERROR = "publish_report.error.file_extension_error";

    /**
     * PUBLISH_REPORT_ERROR_FILE_UPLOAD_ERROR
     */
    private static final String PUBLISH_REPORT_ERROR_FILE_UPLOAD_ERROR = "publish_report.error.file_upload_error";

    /**
     * PUBLISH_REPORT_MESSAGE_FILE_UPLOAD_SUCCESS
     */
    private static final String PUBLISH_REPORT_MESSAGE_FILE_UPLOAD_SUCCESS = "publish_report.message.file_upload_success";

    /**
     * Logger.
     */
    public static final Logger LOGGER = Logger
            .getLogger(InputFileController.class);

    /**
     * File Path retrieved from context, of actual report directory on disk.
     */
    private String fileUploadContextPath;

    /**
     * Name of renderGroup to be rendered, for AJAX PUSH.
     */
    private String groupName;

    /**
     * The session name of variable.
     */
    private static final String CURRENT_FILE_LIST_KEY = "org.iceface.current.file_list";

    /**
     * The session name of variable.
     */
    private static final String CURRENT_FILE_UPLOAD_DIR_KEY = "org.iceface.current.upload_dir";

    /**
     * constructor.
     * 
     * @throws GWPortalException
     */
    public InputFileController() throws GWPortalException {

        /* get file upload directory path from context */
        fileUploadContextPath = PropertyUtils
                .getProperty(ApplicationType.REPORT_VIEWER,
                        ReportConstants.REPORTS_NAME_XML);

        setRelativeFileUploadDirPath("");
        setFileListBean(new FileListBean());

    }

    /**
     * The event handler method to be called on File Upload.
     * 
     * @param event
     * @throws GWPortalException
     */
    public void uploadFile(final ActionEvent event) throws GWPortalException {
        InputFile inputFile = (InputFile) event.getSource();
        FileObject currentFile;
        FileListBean fileListBean = getFileListBean();

        /* error message to be displayed on page if any */
        String errorMessage;

        FacesContext facesContext = FacesContext.getCurrentInstance();

        if (inputFile.getStatus() == InputFile.SAVED) {

            /* File uploaded and saved successfully */

            currentFile = new FileObject(inputFile.getFile());

            // add the file to list if its not already there
            boolean fileAlreadyExists = false;
            List<FileObject> fileList = fileListBean.getFileList();
            for (FileObject fileObject : fileList) {
                if (fileObject.getFileName().equals(currentFile)) {
                    fileAlreadyExists = true;
                }
            }
            /* add file to our file list */
            if (!fileAlreadyExists) {
                fileListBean.getFileList().add(currentFile);
            }

            /* adding message to display on screen */
            errorMessage = ResourceUtils
                    .getLocalizedMessage(PUBLISH_REPORT_MESSAGE_FILE_UPLOAD_SUCCESS);

            if (facesContext != null) {
                facesContext.addMessage(null, new FacesMessage(errorMessage));
            }

            /* regenerate directory XML file */
            new XMLGenerate().addToXML(getRelativeFileUploadDirPath(),
                    inputFile.getFilename());

        } else if (inputFile.getStatus() == InputFile.INVALID) {

            /* File upload failed due to invalid or 0 byte size file file */

            errorMessage = ResourceUtils
                    .getLocalizedMessage(PUBLISH_REPORT_ERROR_FILE_UPLOAD_ERROR);

            LOGGER.error(errorMessage);

            /* adding message to display on screen */
            if (facesContext != null) {
                facesContext.addMessage(null, new FacesMessage(errorMessage));
            }

        } else if (inputFile.getStatus() == InputFile.INVALID_NAME_PATTERN) {

            /*
             * File upload failed due to invalid file extension, non .rptdesign
             * file
             */

            errorMessage = ResourceUtils
                    .getLocalizedMessage(PUBLISH_REPORT_ERROR_FILE_EXTENSION_ERROR);

            /*
             * adding message to display on screen, after cleaning facesContext
             * TODO: can we remove below code?
             */
            /*
             * Iterator<FacesMessage> iterator = facesContext.getMessages();
             * while (iterator.hasNext()) { iterator.remove(); }
             */
            if (facesContext != null) {
                facesContext.addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        ReportConstants.EMPTY_STRING, errorMessage));
            }
        }
        LOGGER.info("File upload status: " + inputFile.getStatus());
        setFileListBean(fileListBean);
    }

    /**
     * It concatenates the base directory path with inner directory path and
     * returns it.
     * 
     * @return String
     * @throws GWPortalException
     */
    public String getUploadPath() throws GWPortalException {
        return fileUploadContextPath + getRelativeFileUploadDirPath();
    }

    /**
     * This Method gets called when some directory in report tree (Publish
     * screen) is clicked.
     * 
     * @param event
     * @throws GWPortalException
     */
    public void showExistingFiles(final ActionEvent event)
            throws GWPortalException {

        /* load files list from session */
        String reportDir;

        /* get corresponding report directory under main directory */

        if (event != null) {
            /* Event is null, that means the call is not from uploadFile method */
            reportDir = FacesUtils
                    .getRequestParameter(ReportConstants.REPORT_DIR);
        } else {

            /* Event is not null, that means the call is from uploadFile method */
            reportDir = getRelativeFileUploadDirPath();
        }

        if (reportDir != null
                && !reportDir.equalsIgnoreCase(ReportConstants.NULL_STRING)) {
            /*
             * read report XML file, get the appropriate directory and make list
             * of files in it to display on page.
             */
            String reportDirName = PropertyUtils.getProperty(
                    ApplicationType.REPORT_VIEWER,
                    ReportConstants.REPORTS_NAME_XML);
            NodeList dirNodeList = gerDirListFromXMLFile(reportDirName);
            Node dirNode = null;

            /*
             * find appropriate directory from list and set relative path in
             * session
             */
            String dirName = null;
            int i;
            for (i = 0; i < dirNodeList.getLength(); i++) {
                dirNode = dirNodeList.item(i);
                dirName = dirNode.getAttributes().item(0).getNodeValue();
                String displayDirName = dirNode.getAttributes().item(1)
                        .getNodeValue();
                if (reportDir.equalsIgnoreCase(displayDirName)) {
                    setRelativeFileUploadDirPath(dirName);
                    break;
                }
            }
            if (i == dirNodeList.getLength()) {
                // directory not found in XML file directory list!!
                // show files in root directory
                setRelativeFileUploadDirPath(ReportConstants.EMPTY_STRING);
                dirName = ReportConstants.EMPTY_STRING;
            }

            /* got report directory, generate list of files */
            FileListBean bean = (FileListBean) FacesUtils
                    .getManagedBean(ReportConstants.FILE_LIST_BEAN);

            File dirFile = new File(reportDirName + dirName);
            if (dirFile.isDirectory()) {
                // must be a directory
                File[] reportList = dirFile.listFiles();
                bean.getFileList().clear();

                for (File file : reportList) {
                    if (file.getName().endsWith(
                            ReportConstants.REPORT_FILE_EXTENSION)) {
                        bean.getFileList().add(new FileObject(file));
                    }
                }
            }
            // put list bean in session
            setFileListBean(bean);
        }

    }

    /**
     * read report XML file, get the appropriate directory and make list of
     * files in it to display on page.
     * 
     * @param reportDirName
     */

    private NodeList gerDirListFromXMLFile(final String reportDirName) {

        // get the report file name from session
        File file = new File(reportDirName + ReportConstants.REPORT_EN_XML);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                .newInstance();
        DocumentBuilder db;

        NodeList dirNodeList = null;
        try {
            db = documentBuilderFactory.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();

            dirNodeList = doc.getElementsByTagName(ReportConstants.REPORT_DIR);

        } catch (ParserConfigurationException e) {
            LOGGER.error(
                    "Error in configuration. Please check report XML file.", e);
        } catch (SAXException e) {
            LOGGER.error("Error in XML file. Please check report XML file.", e);
        } catch (IOException e) {
            LOGGER.error("Error reading report XML file.", e);
        }

        return dirNodeList;
    }

    /**
     * returns file list
     * 
     * @return FileListBean
     * @throws GWPortalException
     */
    public FileListBean getFileListBean() throws GWPortalException {
        return (FileListBean) IPCUtils
                .getApplicationAttribute(CURRENT_FILE_LIST_KEY);
    }

    /**
     * Set file list
     * 
     * @param list
     * @throws GWPortalException
     */
    public void setFileListBean(final FileListBean list)
            throws GWPortalException {
        if (groupName == null) {
            groupName = IPCUtils.getSessionID();
            SessionRenderer.addCurrentSession(groupName);
        }
        IPCUtils.setApplicationAttribute(CURRENT_FILE_LIST_KEY, list);
        SessionRenderer.render(groupName);
    }

    /**
     * Sets relative file upload path on server
     * 
     * @param relativeFileUploadDirPath
     * @throws GWPortalException
     */
    public void setRelativeFileUploadDirPath(
            final String relativeFileUploadDirPath) throws GWPortalException {
        if (groupName == null) {
            groupName = IPCUtils.getSessionID();
            SessionRenderer.addCurrentSession(groupName);
        }
        IPCUtils.setApplicationAttribute(CURRENT_FILE_UPLOAD_DIR_KEY,
                relativeFileUploadDirPath);
        SessionRenderer.render(groupName);
    }

    /**
     * get current file upload path on server from session
     * 
     * @return RelativeFileUploadDirPath
     * @throws GWPortalException
     */
    public String getRelativeFileUploadDirPath() throws GWPortalException {
        return (String) IPCUtils
                .getApplicationAttribute(CURRENT_FILE_UPLOAD_DIR_KEY);
    }

}
