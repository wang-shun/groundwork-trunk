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

package com.groundworkopensource.portal.statusviewer.bean;

import java.io.Serializable;

/**
 * Class denoting the backing bean for the comments portlet
 * 
 * @author mridu_narang
 */

public class CommentsBean implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -7641295858481953547L;

    /**
     * User associated with the comment
     */
    private String user;

    /**
     * Comment Text
     */
    private String comment;

    /**
     * Date the comment was added
     */
    private String date;

    /**
     * Comment ID associated with the comment
     */
    private String commentId;

    /**
     * deletePending to indicate if comment deletion is pending.
     */
    private boolean deletePending;

    /**
     * Index in the list at which this comment will get inserted / will be
     * there.
     */
    private int commentInsertIndex;

    /**
     * Default Constructor
     * 
     * @param comment
     * @param commentId
     * @param date
     * @param user
     * @param deletePending
     * @param commentInsertIndex
     */
    public CommentsBean(String comment, String commentId, String date,
            String user, boolean deletePending, int commentInsertIndex) {
        this.setComment(comment);
        this.setCommentId(commentId);
        this.setDate(date);
        this.setUser(user);
        this.setDeletePending(deletePending);
        this.setCommentInsertIndex(commentInsertIndex);
    }

    // Getters and Setter Methods
    /**
     * 
     * Method to set user/author of the comment
     * 
     * @param user
     *            the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * Method to retrieve user/author of the comment
     * 
     * @return the user
     */
    public String getUser() {
        return this.user;
    }

    /**
     * Method to set comment
     * 
     * @param comment
     *            the comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Method to retrieve comment
     * 
     * @return the comment
     */
    public String getComment() {
        return this.comment;
    }

    /**
     * Method to set date
     * 
     * @param date
     *            the date to set
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * Method to retrieve date
     * 
     * @return the date
     */
    public String getDate() {
        return this.date;
    }

    /**
     * Method to set comment ID
     * 
     * @param commentId
     *            the commentId to set
     */
    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    /**
     * Method to retrieve comment ID
     * 
     * @return the commentId
     */
    public String getCommentId() {
        return this.commentId;
    }

    /**
     * Sets the deletePending.
     * 
     * @param deletePending
     *            the deletePending to set
     */
    public void setDeletePending(boolean deletePending) {
        this.deletePending = deletePending;
    }

    /**
     * Returns the deletePending.
     * 
     * @return the deletePending
     */
    public boolean isDeletePending() {
        return deletePending;
    }

    /**
     * Sets the commentInsertIndex.
     * 
     * @param commentInsertIndex
     *            the commentInsertIndex to set
     */
    public void setCommentInsertIndex(int commentInsertIndex) {
        this.commentInsertIndex = commentInsertIndex;
    }

    /**
     * Returns the commentInsertIndex.
     * 
     * @return the commentInsertIndex
     */
    public int getCommentInsertIndex() {
        return commentInsertIndex;
    }

    /**
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return commentId + "_" + comment + "_" + deletePending + "_Index_"
                + commentInsertIndex;
    }
}
