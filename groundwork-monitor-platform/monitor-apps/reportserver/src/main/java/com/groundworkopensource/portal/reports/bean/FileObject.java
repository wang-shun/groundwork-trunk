/*
 *  Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork)
 *  All rights reserved. Use is subject to GroundWork commercial license terms.
 */

package com.groundworkopensource.portal.reports.bean;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The back-end bean for the File that is being uploaded by user.
 * 
 * @author nitin_jadhav
 */

public class FileObject {

	/**
	 * File Object.
	 */
	private File file;

	/**
	 * @param file
	 */
	public void setFile(File file) {
		this.file = file;
	}

	/**
	 * @return File
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @return String
	 */
	public String getLastModified() {
		Date lastModifiedDate = new Date(file.lastModified());
		SimpleDateFormat format = new SimpleDateFormat(
				"d-MM-yyyy 'at' HH:mm:ss z");
		return format.format(lastModifiedDate);
	}

	/**
	 * @return String
	 */
	public String getFileName() {
		return file.getName();
	}

	/**
	 * @param file
	 */
	public FileObject(File file) {
		super();
		this.file = file;
	}

}
