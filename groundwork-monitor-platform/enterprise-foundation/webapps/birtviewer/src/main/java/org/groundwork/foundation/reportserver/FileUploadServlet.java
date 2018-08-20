/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com

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

/*Created on: Mar 28, 2006 */

package org.groundwork.foundation.reportserver;

import org.groundwork.foundation.reportserver.pagebeans.PageBean;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.servlet.*;
import org.apache.commons.fileupload.disk.*;

/**
 * 
 * FileUpload servlet is used to upload report design files to the server.
 *
 */
public class FileUploadServlet extends HttpServlet
{
	// String constants
	private final String MSG_ERROR_NOT_MULTIPART = "Form posted must be multipart.";
	private final String MSG_ERROR_UPLOAD = "Error occurred uploading file(s).";
	private final String MSG_ERROR_INVALID_EXT = "Upload rejected because file has an extension that is not a supported - ";
	private final String MSG_ERROR_MISSING_REDIRECT = "Missing redirectURL form field.";
	
	private final String WINDOWS_PATH_SEPARATOR = "\\";	
	private final String COMMA = ",";
	private final String DOT = ".";
	private final String EXT_RPTDESIGN = "rptdesign";
	
	// Init Parameters
	private final String PARAM_MAX_THRESHOLD = "maxThreshold";
	private final String PARAM_MAX_SIZE = "maxFileSize";
	private final String PARAM_UPLOAD_FILE_EXTS = "uploadFileExtensions";
	
	// Form Fields
	private final String FORM_REDIRECT_URL = "redirectURL";
	private final String FORM_RELATIVE_DIR = "relativeDir";
	
	// Max Threshold after which file are written directly to disk in bytes
	private int DEFAULT_MAX_THRESHOLD = 2048;
	
	// Max file size in bytes
	private int DEFAULT_MAX_FILE_SIZE = 2048000; 
	
    private Log log = LogFactory.getLog(this.getClass());  
    
    private String uploadDirectory = null;
    private int maxFileSize = DEFAULT_MAX_FILE_SIZE;
    private int maxThreshold = DEFAULT_MAX_THRESHOLD;    
    private List<String> fileExtensions = new ArrayList<String>(1);
    
    /**
     * Intialize Servlet.
     */
    public final void init( ServletConfig config ) throws ServletException
    {
    	super.init(config);
    	    	
    	if (log.isInfoEnabled())
    		log.info("FileUploadServlet: Init Servlet.");
    	
    	// Get MaxThreshold initialize parameter
    	String strThreshold = config.getInitParameter(PARAM_MAX_THRESHOLD);
    	if (strThreshold != null && strThreshold.length() > 0)
    	{
    		this.maxThreshold = Integer.parseInt(strThreshold);
    	}
    	
    	// Get Maximum file size initialize parameter
    	String strFileSize = config.getInitParameter(PARAM_MAX_SIZE);
    	if (strFileSize != null && strFileSize.length() > 0)
    	{
    		this.maxFileSize = Integer.parseInt(strFileSize);
    	}
    	
    	// Get File extensions allowed during upload
    	String strFileExtensions = config.getInitParameter(PARAM_UPLOAD_FILE_EXTS);
    	if (strFileExtensions != null && strFileExtensions.length() > 0)
    	{
    		String[] exts = strFileExtensions.split(COMMA);    		
    		for (int i = 0; i < exts.length; i++)
    		{
    			this.fileExtensions.add(exts[i]);
    		}
    	}
    	
    	// Get the upload directory for the init parameters
    	this.uploadDirectory = new PageBean(config.getServletContext()).getReportDirectory();
    	
    	if (log.isInfoEnabled()) {
    		log.info("File Upload Directory = [" + this.uploadDirectory + "]");    	
    		log.info("Max File Size = [" + this.maxFileSize + "]");
    		log.info("Max Threshold = [" + this.maxThreshold + "]");
    	}
    }

    /**
     * Initializes the services which need <code>RunData</code> to initialize
     * themselves (post startup).
     * 
     * @param data
     *            The first <code>GET</code> request.
     */
    public final void init( HttpServletRequest request, HttpServletResponse response )
    {
    }

    // -------------------------------------------------------------------
    // R E Q U E S T P R O C E S S I N G
    // -------------------------------------------------------------------

    /**
     * The primary method invoked when the File Upload servlet is executed.
     * 
     * @param req
     *            Servlet request.
     * @param res
     *            Servlet response.
     * @exception IOException
     *                a servlet exception.
     * @exception ServletException
     *                a servlet exception.
     */
    public final void doGet( HttpServletRequest req, HttpServletResponse res ) throws IOException, ServletException
    {
    	// Check that we have a file upload request
    	boolean isMultipart = ServletFileUpload.isMultipartContent(new ServletRequestContext(req));
    	if (isMultipart == false)
    	{
    		throw new ServletException(MSG_ERROR_NOT_MULTIPART);
    	}
    	
    	try {
	    	// Create a factory for disk-based file items
	    	DiskFileItemFactory factory = new DiskFileItemFactory();
	
	    	// Set factory constraints
	    	factory.setSizeThreshold(this.maxThreshold);
	    	//factory.setRepository(yourTempDirectory);
	
	    	// Create a new file upload handler
	    	ServletFileUpload upload = new ServletFileUpload(factory);
	
	    	// Set overall request size constraint
	    	upload.setSizeMax(this.maxFileSize);
	
	    	// Parse the request
	    	List fileItems = upload.parseRequest(req);
	    	
	    	String redirectURL = null;
	    	String relativeDir = null;
	    	Iterator it = fileItems.iterator();
	    	while (it.hasNext())
	    	{
	    		FileItem fileItem = (FileItem)it.next();
	    			    		
	    		// Process Form Fields
	        	if (fileItem.isFormField() == true) {
	        		
	        		// Look for redirect URL
	        	    String name = fileItem.getFieldName();
	        	    if (FORM_REDIRECT_URL.equalsIgnoreCase(name))
	        	    {
	        	    	redirectURL = fileItem.getString();
	        	    	
	        	    	if (log.isInfoEnabled()) {
	        	    		log.info("RedirectURL:  " + redirectURL);    	
	        	    	}
	        	    }
	        	    else if (FORM_RELATIVE_DIR.equalsIgnoreCase(name))
	        	    {
	        	    	relativeDir = fileItem.getString();
	        	    	
	        	    	if (log.isInfoEnabled()) {
	        	    		log.info("RelativeDir:  " + relativeDir);    	
	        	    	}	        	    	
	        	    }
	        	    
	        		continue;
	        	}
	        	
	        	// Write file to directory
	        	
	        	// Strip off the path information.  Some browser provide full path information
	        	String fileName = stripPath(fileItem.getName());
	        	if (fileName == null || fileName.length() == 0 || fileItem.getSize() == 0)
	        	{
	        		// No file specified nothing to do
	        		continue;
	        	}
	        	
	        	if (log.isInfoEnabled())
	    	    	log.info("File Name Being Uploaded: " + fileName);
	    	    
	        	// Only allow uploads of extensions we support
	        	if (validFileExtension(fileName) == true)
	        	{	        	
		    	    String filePath = this.uploadDirectory;	    	    
		    	    if (relativeDir != null && relativeDir.length() > 0)
		    	    {
		    	    	filePath += relativeDir;
		    	    }
		    	    
		    	    filePath += fileName;
		    	    
		    	    File file = new File(filePath);
		    	    
		    	    if (log.isInfoEnabled())
		    	    	log.info("Upload File Path: " + filePath);
		    	    
		    	    fileItem.write(file);    
	        	}
	        	else {
	        		log.error(MSG_ERROR_INVALID_EXT + fileName);
	        	}
	    	}    
	    	
	    	// Redirect to url specified
	    	if (redirectURL == null || redirectURL.length() == 0)
	    	{
	    		throw new ServletException(MSG_ERROR_MISSING_REDIRECT);
	    	}
	    	
	    	res.sendRedirect(res.encodeRedirectURL(redirectURL));
    	}
    	catch (Exception e)
    	{
    		log.error(MSG_ERROR_UPLOAD, e);
    		throw new ServletException(MSG_ERROR_UPLOAD, e);
    	}
     }

    /**
     * In this application doGet and doPost are the same thing.
     * 
     * @param req
     *            Servlet request.
     * @param res
     *            Servlet response.
     * @exception IOException
     *                a servlet exception.
     * @exception ServletException
     *                a servlet exception.
     */
    public final void doPost( HttpServletRequest req, HttpServletResponse res ) throws IOException, ServletException
    {
        doGet(req, res);
    }

    // -------------------------------------------------------------------
    // S E R V L E T S H U T D O W N
    // -------------------------------------------------------------------

    /**
     * The <code>Servlet</code> destroy method. Invokes
     * <code>ServiceBroker</code> tear down method.
     */
    public final void destroy()
    {
    	if (log.isInfoEnabled())
    		log.info("FileUploadServlet. Done shutting down!");
    }    
    
    private boolean validFileExtension (String fileName)
    {
    	// If no file extensions are defined, we accept all
    	if (this.fileExtensions == null || this.fileExtensions.size() == 0)
    	{
    		return true;
    	}
    	
    	if (fileName == null || fileName.length() == 0)
    	{
    		return false;
    	}
    	
    	String ext = null;
    	int pos = fileName.lastIndexOf(DOT);
    	if (pos >= 0)
    	{
    		// Note:  Empty string is returned if the file name ends with a "."
    		ext = fileName.substring(pos + 1);
    	}    	
    	
    	return this.fileExtensions.contains(ext);
    }
    
    private String stripPath (String fileName)
    {
    	if (fileName == null || fileName.length() == 0)
    	{
    		return fileName;
    	}
    	
    	int pos = fileName.lastIndexOf(WINDOWS_PATH_SEPARATOR);
    	if (pos < 0)
    	{
    		pos = fileName.lastIndexOf(File.separatorChar);
    	}
    	    	
    	if (pos >= 0)
    	{
    		return fileName.substring(pos + 1);
    	}    	
    	else {
    		return fileName;
    	}
    }
}
