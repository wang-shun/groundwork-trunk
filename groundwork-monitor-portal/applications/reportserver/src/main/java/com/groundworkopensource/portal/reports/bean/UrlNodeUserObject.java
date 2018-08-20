/*
 *  Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork)
 *  All rights reserved. Use is subject to GroundWork commercial license terms.
 */

package com.groundworkopensource.portal.reports.bean;

import javax.swing.tree.DefaultMutableTreeNode;

import com.icesoft.faces.component.tree.IceUserObject;

/**
 * The UrlNodeUserObject object is responsible for storing extra data for a url.
 * The url along with text is bound to a ice:commanLink object which will launch
 * a new browser window pointed to the url.
 * 
 * @author rashmi_tambe
 */
public class UrlNodeUserObject extends IceUserObject {

    /**
     * object ID.
     */
    private String objectId;

    /**
     * @return String
     */
    public String getObjectId() {
        return objectId;
    }

    /**
     * @param objectID
     */
    public void setObjectId(String objectID) {
        this.objectId = objectID;
    }

    /**
     * @param wrapper
     */
    public UrlNodeUserObject(final DefaultMutableTreeNode wrapper) {
        super(wrapper);
    }
}
