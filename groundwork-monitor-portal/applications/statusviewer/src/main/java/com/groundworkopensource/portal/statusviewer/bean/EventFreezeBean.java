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

import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.common.ResourceUtils;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.icesoft.faces.component.ext.HtmlCommandButton;

/**
 * This class is responsible to stop and start JMS connection and display
 * appropriate label on UI
 * 
 * @author manish_kjain
 * 
 */
public class EventFreezeBean {

    /**
     * boolean freeze
     */
    private boolean freeze;

    /**
     * Button Text
     */
    private String freezeButtonText;

    /**
     * logger
     */
    private static Logger logger = Logger.getLogger(EventFreezeBean.class
            .getName());
    /**
     * PAUSE_BUTTON_LABEL
     */
    private static final String PAUSE_BUTTON_LABEL = ResourceUtils
            .getLocalizedMessage(Constant.EVENT_CONTENT_PAUSE_EVENTS);
    /**
     * PAUSE_BUTTON_LABEL
     */
    private static final String RESUME_BUTTON_LABEL = ResourceUtils
            .getLocalizedMessage(Constant.EVENT_CONTENT_RESUME_EVENTS);

    /**
     * constructor
     */
    public EventFreezeBean() {
        freezeButtonText = PAUSE_BUTTON_LABEL;
    }

    /**
     * This method toggle freeze variable on button click.
     * 
     * @param event
     */
    public void toggleButton(ActionEvent event) {
        String linkText = (String) ((HtmlCommandButton) event.getComponent())
                .getValue();

        if (linkText.equalsIgnoreCase(PAUSE_BUTTON_LABEL)) {

            freeze = true;

        } else {

            freeze = false;

        }
        freeze(freeze);
    }

    /**
     * reset button
     */
    public void reset() {
        freeze = !freeze;
        if (freeze) {
            freezeButtonText = RESUME_BUTTON_LABEL;
        } else {
            freezeButtonText = PAUSE_BUTTON_LABEL;
        }
        freeze(freeze);
    }

    /**
     * This method start and stop jms connection depending on value argument
     * 
     * @param value
     */
    public void freeze(boolean value) {

        logger.debug("Is Event portlet freezed =" + value);
        EventListBean eventListBean = (EventListBean) FacesUtils
                .getManagedBean(Constant.EVENT_LIST_BEAN);
        if (value) {
            eventListBean.stopTopicConnection();

            freezeButtonText = RESUME_BUTTON_LABEL;
        } else {
            eventListBean.startTopicConnection();

            freezeButtonText = PAUSE_BUTTON_LABEL;
        } // end if

    }

    /**
     * Returns the freezeButtonText.
     * 
     * @return the freezeButtonText
     */
    public String getFreezeButtonText() {
        return freezeButtonText;
    }

    /**
     * Sets the freezeButtonText.
     * 
     * @param freezeButtonText
     *            the freezeButtonText to set
     */
    public void setFreezeButtonText(String freezeButtonText) {
        this.freezeButtonText = freezeButtonText;
    }

}
