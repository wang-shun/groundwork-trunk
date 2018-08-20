/*
 *  Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork)
 *  All rights reserved. Use is subject to GroundWork commercial license terms.
 */

package com.groundworkopensource.portal.reports.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The back-end bean for the file list for publish report function.
 * 
 * this component is a workaround for incorrect ajax push functionality, where
 * the list of files in publish report screen is updated on clicking on folder
 * in tree.
 * 
 * @author nitin_jadhav
 */

public class FileListBean {

	/**
	 * filelist that contains current directory files.
	 */
	private List<FileObject> fileList = Collections
			.synchronizedList(new ArrayList<FileObject>());

	/**
	 * hidden field to be shown in report tree portlet.
	 */

	private String hiddenField = " ";

	/**
	 * @param fileList
	 */
	public void setFileList(List<FileObject> fileList) {
		this.fileList = fileList;
	}

	/**
	 * @return List
	 */
	public List<FileObject> getFileList() {
		return fileList;
	}

	/**
	 * @param hiddenField
	 */
	public void setHiddenField(String hiddenField) {
		this.hiddenField = hiddenField;
	}

	/**
	 * @return String
	 */
	public String getHiddenField() {
		return hiddenField;
	}

}
