/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com

 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.

 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.

 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.groundwork.collage.model.impl;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
public class MessageFilter implements Serializable, com.groundwork.collage.model.MessageFilter
{
    private static final long serialVersionUID = 1;

    /** identifier field */
    private Integer messageFilterId;

    /** persistent field */
    private String name;

    /** persistent field */
    private String regExpresion;

    /** nullable persistent field */
    private Byte isChangeSeverityToStatistic;

    /** full constructor */
    public MessageFilter(Integer messageFilterId, String name,
            String regExpresion, Byte isChangeSeverityToStatistic) {
        this.messageFilterId = messageFilterId;
        this.name = name;
        this.regExpresion = regExpresion;
        this.isChangeSeverityToStatistic = isChangeSeverityToStatistic;
    }

    /** default constructor */
    public MessageFilter() {
    }

    /** minimal constructor */
    public MessageFilter(Integer messageFilterId, String name,
            String regExpresion) {
        this.messageFilterId = messageFilterId;
        this.name = name;
        this.regExpresion = regExpresion;
    }

    public Integer getMessageFilterId() {
        return this.messageFilterId;
    }

    public void setMessageFilterId(Integer messageFilterId) {
        this.messageFilterId = messageFilterId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegExpresion() {
        return this.regExpresion;
    }

    public void setRegExpresion(String regExpresion) {
        this.regExpresion = regExpresion;
    }

    public Byte getIsChangeSeverityToStatistic() {
        return this.isChangeSeverityToStatistic;
    }

    public void setIsChangeSeverityToStatistic(Byte isChangeSeverityToStatistic) {
        this.isChangeSeverityToStatistic = isChangeSeverityToStatistic;
    }

    public String toString() {
        return new ToStringBuilder(this).append("messageFilterId",
                getMessageFilterId()).toString();
    }

}