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

package com.groundwork.collage.model;

/**
 * 
 * MessageFilterInterface
 * @author <a href="mailto:rruttimann@itgroundwork.com"> Roger Ruttimann</a>
 * @version $Id: MessageFilter.java 6397 2007-04-02 21:27:40Z glee $
 */
public interface MessageFilter {
    Integer getMessageFilterId();

    void setMessageFilterId(Integer messageFilterId);

    String getName();

    void setName(String name);

    String getRegExpresion();

    void setRegExpresion(String regExpresion);

    Byte getIsChangeSeverityToStatistic();

    void setIsChangeSeverityToStatistic(Byte isChangeSeverityToStatistic);
}