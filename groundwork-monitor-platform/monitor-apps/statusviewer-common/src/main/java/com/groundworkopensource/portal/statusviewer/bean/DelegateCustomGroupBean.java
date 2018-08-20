/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2015  GroundWork Open Source Solutions info@groundworkopensource.com

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

package com.groundworkopensource.portal.statusviewer.bean;

import com.groundworkopensource.portal.model.CustomGroup;

import javax.faces.model.SelectItem;
import java.util.List;

/**
 * DelegateCustomGroupBean
 *
 * @author <a href="mailto:randy@bluesunrise.com">Randy Watler</a>
 * @version $Id:$
 */
public interface DelegateCustomGroupBean {

    /**
     * Cancel action.
     *
     * @return action
     */
    String cancel();

    /**
     * Delete action.
     *
     * @return action
     */
    String deleteCustomGroup();

    /**
     * Edit action.
     *
     * @return action
     */
    String editCustomGroup();

    /**
     * Publish action.
     *
     * @return action
     */
    String publish();

    /**
     * Save action.
     *
     * @return action
     */
    String save();

    /**
     * Update and publish action.
     *
     * @return action
     */
    String updateAndPublish();

    /**
     * Update and save action.
     *
     * @return action
     */
    String updateAndSave();

    /**
     * Get all custom groups,
     *
     * @return list of custom groups
     */
    List<CustomGroup> getCustomGroups();

    /**
     * Get custom group selections.
     *
     * @return dual list of custom group selections
     */
    DualList getCustomGroupSelectItems();

    /**
     * Get entity type selections.
     *
     * @return list of entity type selections
     */
    List<SelectItem> getEntityTypes();

    /**
     * Get host group selections.
     *
     * @return dual list of host group selections
     */
    DualList getHostGroups();

    /**
     * Get message.
     *
     * @return message
     */
    String getMessage();

    /**
     * Get service group selections.
     *
     * @return dual list of service group selections
     */
    DualList getServiceGroups();
}
