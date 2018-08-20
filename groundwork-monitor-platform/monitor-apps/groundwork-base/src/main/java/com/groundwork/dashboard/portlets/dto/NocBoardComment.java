/*
 * Copyright (C) 2017 GroundWork Open Source, Inc. (GroundWork) All rights
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

package com.groundwork.dashboard.portlets.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NocBoardComment {

    protected String commentID = "";

    protected String commentDate = "";

    protected String commentUser = "";

    protected String commentText = "";

    /**
     * Delimiter for comment fields
     */
    private static final String FIELD_LEVEL_DELIMITER = ";::;";
    private static final String COMMENT_LEVEL_DELIMITER = "#!#";

    /**
     * Allowable comment parameters
     */
    private static final int NO_OF_COMMENT_PARAMETERS = 4;

    public String getCommentID() {
        return commentID;
    }

    public void setCommentID(String commentID) {
        this.commentID = commentID;
    }

    public String getCommentDate() {
        return commentDate;
    }

    public void setCommentDate(String commentDate) {
        this.commentDate = commentDate;
    }

    public String getCommentUser() {
        return commentUser;
    }

    public void setCommentUser(String commentUser) {
        this.commentUser = commentUser;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public String getFieldDelimiter(){ return  FIELD_LEVEL_DELIMITER; }

    public String getCommentLevelDelimiter() { return COMMENT_LEVEL_DELIMITER; }

    public NocBoardComment() {
    }
    
    public NocBoardComment(String commentID, String commentDate, String commentUser, String commentText) {
        this.commentID = commentID;
        this.commentDate = commentDate;
        this.commentUser = commentUser;
        this.commentText = commentText;
    }

    public NocBoardComment(String commentString) {
        //commentString is of the format id;::;date;::;user;::;'commentText'

        StringBuilder stringBuilder;
        // Index fields
        int startIndex, endIndex;

        // parse all fields in the comment
        String[] commentFields = commentString.split(FIELD_LEVEL_DELIMITER);
        // Fixed number of parameters in comments = 4
        if (commentFields.length != NO_OF_COMMENT_PARAMETERS) {
            // Skip and process next comment
            return;
        }
        // Here received complete comment. Hence Sequentially retrieve
        // fields / parameters in the comment.
        this.commentID = commentFields[0];
        this.commentDate = commentFields[1];
        this.commentUser = commentFields[2];
        String extendedCommentText = commentFields[3];

        // Process Comment Text
        // Get string without single quotes
        stringBuilder = new StringBuilder(extendedCommentText);
        startIndex = stringBuilder.indexOf("'");
        endIndex = stringBuilder.lastIndexOf("'");
        // Remove single quotes
        this.commentText = extendedCommentText.substring(startIndex + 1, endIndex);
    }
}
