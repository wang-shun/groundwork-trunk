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

/**
 * This class holds the state transition data for host/services.
 * 
 * @author shivangi_walvekar
 * 
 */
public class StateTransitionBean {

    /**
     * Monitor status
     */
    private String toState;

    /**
     * Duration the host/service was in a particular monitor state.
     */
    private Long timeInState;

    /**
     * Host name or service name
     */
    private String entityName;

    /**
     * @return entityName
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * @param entityName
     */
    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    /**
     * @return timeInState
     */
    public Long getTimeInState() {
        return timeInState;
    }

    /**
     * @param timeInState
     */
    public void setTimeInState(Long timeInState) {
        this.timeInState = timeInState;
    }

    /**
     * @return toState
     */
    public String getToState() {
        return toState;
    }

    /**
     * @param toState
     */
    public void setToState(String toState) {
        this.toState = toState;
    }

}
