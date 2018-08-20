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

import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.statusviewer.bean.FilterBean;
import com.groundworkopensource.portal.statusviewer.common.Constant;

/**
 * This class handles filter selections for hosts and services
 * 
 * @author mridu_narang
 * 
 */

public class FilterHandler implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -7707747442932031534L;

    /**
     * Logger
     */
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Instance of StateController
     */
    private final StateController stateController = new StateController();

    /**
     * Method to relay call to IPCHandler to apply selected filters.
     * 
     * @param e
     *            The action event associated with the method
     */
    public void applyFilters(ActionEvent e) {

        /*
         * Pass the filter key values to StateController. Note that for
         * selection message an empty string is passed as key value.
         */

        FilterBean filterBean = (FilterBean) FacesUtils
                .getManagedBean(Constant.FILTER_BEAN);

        if (filterBean != null) {
            // update state-controller
            stateController.update(filterBean.getSelectedNodeType(), filterBean
                    .getNodeName(), filterBean.getNodeId());
            this.stateController.applyFilter(
                    filterBean.getSelectedHostFilter(), filterBean
                            .getSelectedServiceFilter());
        } else {
            this.logger.debug("Filter Bean instance is null");
        }
    }

    /**
     * Method to relay call to IPCHandler to reset filter lists to their
     * defaults
     * 
     * @param e
     *            The action event associated with the method
     */
    public void resetFilters(ActionEvent e) {

        FilterBean filterBean = (FilterBean) FacesUtils
                .getManagedBean(Constant.FILTER_BEAN);

        /*
         * Empty string will set default option to - display all i.e. no filters
         * applied
         */
        if (filterBean != null) {
            filterBean.setSelectedHostFilter(Constant.EMPTY_STRING);
            filterBean.setSelectedServiceFilter(Constant.EMPTY_STRING);

            // update state-controller
            stateController.update(filterBean.getSelectedNodeType(), filterBean
                    .getNodeName(), filterBean.getNodeId());

            // Call stateController to set empty filters.
            this.stateController.applyFilter(Constant.EMPTY_STRING,
                    Constant.EMPTY_STRING);
        } else {
            this.logger.debug("Filter Bean instance is null");
        }

    }
}
