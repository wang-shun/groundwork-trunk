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
import java.util.Arrays;

import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;

import com.groundworkopensource.portal.common.FacesUtils;
import com.groundworkopensource.portal.common.ResourceUtils;
import com.groundworkopensource.portal.common.exception.GWPortalException;
import com.groundworkopensource.portal.common.exception.WSDataUnavailableException;
import com.groundworkopensource.portal.statusviewer.common.Constant;
import com.icesoft.faces.component.menubar.MenuItem;

/**
 * This class is Responsible to Select All or Reselect messages in Event portlet
 * 
 * @author manish_kjain
 * 
 */
public class EventMessageSelectBean implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -7497072713484493596L;
    /**
     * Event Bean Array to hold All rows
     */
    private EventBean[] allRows;
    /**
     * represent All rows is select or none.
     */
    private boolean selectAll;
    /**
     * Label of select all button on UI
     */
    private String selectAllButtonText;
    /**
     * logger
     */
    public static final Logger LOGGER = Logger
            .getLogger(EventMessageSelectBean.class.getName());

    /**
     * @return boolean
     */
    public boolean isSelectAll() {
        return selectAll;
    }

    /**
     * @param selectAll
     */
    public void setSelectAll(boolean selectAll) {
        this.selectAll = selectAll;
    }

    /**
     * constructor Initialize default button label
     */
    public EventMessageSelectBean() {
        // TODO: resource bundle
        selectAllButtonText = ResourceUtils
                .getLocalizedMessage(Constant.SELECT_ALL_BUTTON_LABEL);
    }

    /**
     * @return EventBean
     */
    public EventBean[] getAllRows() {
        return allRows;
    }

    /**
     * @param allRows
     */
    public void setAllRows(EventBean[] allRows) {
        this.allRows = allRows;
    }

    /**
     * @param event
     */
    public void toggleAllSelected(ActionEvent event) {
        EventListBean eventListBean = (EventListBean) FacesUtils
                .getManagedBean(Constant.EVENT_LIST_BEAN);
        EventDataTableBean dataTableBean = eventListBean.getDataTableBean();

        if (dataTableBean != null) {
            allRows = dataTableBean.getEvents();
        } // end if
        if (allRows != null && allRows.length > 0) {
            selectAll = !selectAll;
            if (selectAll) {
                selectAllButtonText = ResourceUtils
                        .getLocalizedMessage(Constant.DE_SELECT_ALL_BUTTON_LABEL);

            } else {
                selectAllButtonText = ResourceUtils
                        .getLocalizedMessage(Constant.SELECT_ALL_BUTTON_LABEL);
            }

            try {
                setAllSelected(selectAll);
            } catch (WSDataUnavailableException e) {
                if (dataTableBean != null) {
                    dataTableBean.setMessage(true);
                    dataTableBean.setError(true);
                    dataTableBean.setErrorMessage(e.getMessage());
                }
            } catch (GWPortalException e) {
                if (dataTableBean != null) {
                    dataTableBean.setMessage(true);
                    dataTableBean.setError(true);
                    dataTableBean.setErrorMessage(e.getMessage());
                }
            }
            if (dataTableBean != null) {
                dataTableBean.setRowSelected(selectAll);
            }

        } // end if
    }

    /**
     * Selected All display event in data tabel
     * 
     * @param value
     * @throws GWPortalException
     * @throws WSDataUnavailableException
     */
    private void setAllSelected(boolean value)
            throws WSDataUnavailableException, GWPortalException {
        if (allRows != null) {
            for (int i = allRows.length - 1; i >= 0; i--) {
                EventBean row = allRows[i];
                row.setSelected(value);
            }

            EventListBean eventListBean = (EventListBean) FacesUtils
                    .getManagedBean(Constant.EVENT_LIST_BEAN);
            EventDataTableBean dataTableBean = eventListBean.getDataTableBean();
            dataTableBean.setSelectedRows(Arrays.asList(allRows));
            if (!value) {
                EventMenuActionBean eventMenuActionBean = (EventMenuActionBean) FacesUtils
                        .getManagedBean(Constant.EVENT_MENU_ACTION_BEAN);
                eventMenuActionBean.reset();
                this.reset();

            }
            EventMenuActionBean eventMenuActionBean = (EventMenuActionBean) FacesUtils
                    .getManagedBean(Constant.EVENT_MENU_ACTION_BEAN);
            MenuItem menu = eventMenuActionBean.getMenuModel().get(0);
            if (allRows != null && allRows.length >= 1) {
                // for future :-icon to be add in menu bar
                menu.setIcon(Constant.EMPTY_STRING);
                eventMenuActionBean.menuListener();
                dataTableBean.setEnablePopUpMenu(true);
                // check if all selected item is nagios ,if true render nagios
                // menu item in pop up menu item
                if (eventMenuActionBean.isSingleAppType(allRows)) {
                    if (Constant.NAGIOS.equalsIgnoreCase(eventMenuActionBean
                            .getSingleAppType())) {
                        dataTableBean.setIsnagiosAcknowledge(true);
                    }
                    if (Constant.SNMPTRAP.equalsIgnoreCase(eventMenuActionBean
                            .getSingleAppType())
                            || Constant.SYSLOG
                                    .equalsIgnoreCase(eventMenuActionBean
                                            .getSingleAppType())) {
                        dataTableBean.setSyslogORSnmptrapMenu(true);
                    }
                }
                dataTableBean.constructComponent();
                // reset nagios menu item.
                dataTableBean.setIsnagiosAcknowledge(false);
                // reset submit passive checks menu item.
                dataTableBean.setSyslogORSnmptrapMenu(false);
            } else {
                // for future :-icon to be add in menu bar
                menu.setIcon(Constant.EMPTY_STRING);
            } // end if
            EventFreezeBean eventFreezeBean = (EventFreezeBean) FacesUtils
                    .getManagedBean(Constant.EVENT_FREEZE_BEAN);
            if (eventFreezeBean != null) {
                eventFreezeBean.freeze(true);
            }
        }

    }

    /**
     * @return String
     */
    public String getSelectAllButtonText() {
        return selectAllButtonText;
    }

    /**
     * @param selectAllButtonText
     */
    public void setSelectAllButtonText(String selectAllButtonText) {
        this.selectAllButtonText = selectAllButtonText;
    }

    /**
     * reset the select all button
     */
    public void reset() {
        this.allRows = null;
        this.selectAllButtonText = ResourceUtils
                .getLocalizedMessage(Constant.SELECT_ALL_BUTTON_LABEL);

        selectAll = false;
        EventListBean eventListBean = (EventListBean) FacesUtils
                .getManagedBean(Constant.EVENT_LIST_BEAN);
        EventDataTableBean dataTableBean = eventListBean.getDataTableBean();
        dataTableBean.resetEvents();

    }
}
