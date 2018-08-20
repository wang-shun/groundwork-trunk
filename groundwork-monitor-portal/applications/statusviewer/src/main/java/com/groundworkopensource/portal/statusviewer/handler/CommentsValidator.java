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

package com.groundworkopensource.portal.statusviewer.handler;

import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import com.groundworkopensource.portal.common.ResourceUtils;

/**
 * Validator class for Comments.
 * 
 * @author mridu_narang
 * 
 */
public class CommentsValidator implements Validator, Serializable {

    /**
     * Maximum allowable length of comment.
     */
    private static final int COMMENT_MAX_ALLOWABLE_LENGTH = 500;

    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = 1L;

    /**
     * (non-Javadoc)
     * 
     * @see javax.faces.validator.Validator#validate(javax.faces.context.FacesContext
     *      , javax.faces.component.UIComponent, java.lang.Object)
     */
    public void validate(FacesContext context, UIComponent component,
            Object value) {

        /* Retrieve String value of field */
        String comment = (String) value;

        // TODO Remove
        // Validate length - REQUIRED
        if (comment == null || comment.trim().length() == 0 || value == null) {
            ((UIInput) component).setValid(false);
            showMessage("Comment is mandatory field.",
                    "Length of comment cannot be zero.");
            return;
        }

        // Validate length - MAX LENGTH = 500 chars
        if (comment.length() > COMMENT_MAX_ALLOWABLE_LENGTH) {
            ((UIInput) component).setValid(false);
            showMessage(
                    ResourceUtils
                            .getLocalizedMessage("com_groundwork_portal_statusviewer_commentsPortlet_lenghtExceeds_500"),
                    ResourceUtils
                            .getLocalizedMessage("com_groundwork_portal_statusviewer_commentsPortlet_lenghtExceeds_500"));
            return;
        }

    }

    /**
     * 
     * Method to set detail & summary message, severity fields for Faces
     * Message.
     * 
     * @param detailMessage
     * @param summaryMessage
     */
    private void showMessage(String detailMessage, String summaryMessage) {

        // Custom faces message
        FacesMessage message = new FacesMessage();

        // Set message details
        message.setDetail(detailMessage);
        message.setSummary(summaryMessage);

        // Set severity
        message.setSeverity(FacesMessage.SEVERITY_ERROR);

        // Throw validator exception with custom message
        throw new ValidatorException(message);
    }
}
