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

package org.groundwork.rs.conversion;

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.biz.model.RTMMCustomGroup;
import com.groundwork.collage.model.Category;
import com.groundwork.collage.model.HostGroup;
import com.groundwork.collage.model.ServiceStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.category.CategoryService;
import org.groundwork.foundation.bs.hostgroup.HostGroupService;
import org.groundwork.foundation.bs.status.StatusService;
import org.groundwork.rs.dto.DtoCustomGroup;
import org.groundwork.rs.dto.DtoDepthType;
import org.groundwork.rs.dto.DtoHostGroup;
import org.groundwork.rs.dto.DtoServiceGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CustomGroupConverter {

    protected static Log log = LogFactory.getLog(CustomGroupConverter.class);

    /**
     * Convert custom group category to DTO custom group instance.
     *
     * @param category custom group category
     * @return DTO custom group instance
     */
    public static DtoCustomGroup convert(Category category, DtoDepthType depth) {
        CategoryService categoryService = CollageFactory.getInstance().getCategoryService();
        List<DtoHostGroup> hostGroups = new ArrayList<>();
        List<DtoServiceGroup> serviceGroups = null;
        if ((category.getCategoryEntities() != null) && !category.getCategoryEntities().isEmpty()) {
            HostGroupService hostGroupService = CollageFactory.getInstance().getHostGroupService();
            for (HostGroup hostGroup : hostGroupService.getHostGroupsByCategoryId(category.getCategoryId())) {
                DtoHostGroup dtoHostGroup;
                // Ensure that we don't return hostgroups with hosts unless the depth is DEEP or greater
                if (hostGroup != null && depth.ordinal() >= DtoDepthType.Deep.ordinal()) {
                    dtoHostGroup = HostGroupConverter.convert(hostGroup, depth);
                } else {
                    dtoHostGroup = HostGroupConverter.convert(hostGroup, DtoDepthType.Simple);
                }
                if (dtoHostGroup != null) hostGroups.add(dtoHostGroup);
            }

            StatusService statusService = CollageFactory.getInstance().getStatusService();
            List<Category> serviceGroupCategories = categoryService.getCategoriesByCategoryId(category.getCategoryId(),
                    CategoryService.ENTITY_TYPE_CODE_SERVICEGROUP);
            serviceGroups = new ArrayList<>();
            for (Category serviceGroupCategory : serviceGroupCategories) {
                if (depth.ordinal() >= DtoDepthType.Deep.ordinal()) {
                    List<ServiceStatus> serviceStatuses = statusService.getServicesByCategoryId(serviceGroupCategory.getCategoryId());
                    if ((serviceStatuses != null) && (serviceStatuses.size() > 0)) {
                        serviceGroups.add(ServiceGroupConverter.convert(serviceGroupCategory, serviceStatuses, depth));
                    }
                } else {
                    serviceGroups.add(ServiceGroupConverter.convert(serviceGroupCategory, null, depth));
                }
            }
        }
        return convert(category, hostGroups, serviceGroups, category.getChildren(), depth);
    }

    /**
     * Convert custom group category to DTO custom group instance.
     *
     * @param category custom group category
     * @param hostGroups custom group host groups
     * @param serviceGroups custom group service groups
     * @param children custom group children
     * @return DTO custom group instance
     */
    private static DtoCustomGroup convert(Category category, List<DtoHostGroup> hostGroups,
                                          List<DtoServiceGroup> serviceGroups, Collection<Category> children,
                                          DtoDepthType depth) {
        DtoCustomGroup dto = new DtoCustomGroup();
        dto.setId(category.getCategoryId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setAgentId(category.getAgentId());
        if (category.getApplicationType() != null) {
            dto.setAppType(category.getApplicationType().getName());
            dto.setAppTypeDisplayName(category.getApplicationType().getDisplayName());
        }
        dto.setRoot(category.isRoot());
        if (hostGroups != null) {
            dto.setHostGroups(hostGroups);
            /*
            for (HostGroup hostGroup : hostGroups) {
                dto.addHostGroup(HostGroupConverter.convert(hostGroup, depth));
            }
            */
        }
        if (serviceGroups != null) {
            dto.setServiceGroups(serviceGroups);
        }
        if (children != null) {
            for (Category child : children) {
                dto.addChild(convert(child, depth));
            }
        }
        if (depth.ordinal() >= DtoDepthType.Deep.ordinal()) dto.setBubbleUpStatus(CustomGroupStatusSupport.calculateBubbleUpStatus(dto));
        return dto;
    }

    /**
     * Convert RTMM custom group to DTO custom group instance.
     *
     * @param customGroup RTMM custom group
     * @return DTO custom group instance
     */
    public static DtoCustomGroup convert(RTMMCustomGroup customGroup) {
        DtoCustomGroup dto = new DtoCustomGroup();
        dto.setId(customGroup.getId());
        for (Integer hostGroupId : customGroup.getHostGroupIds()) {
            DtoHostGroup dtoHostGroup = new DtoHostGroup();
            dtoHostGroup.setId(hostGroupId);
            dto.addHostGroup(dtoHostGroup);
        }
        for (Integer serviceGroupId : customGroup.getServiceGroupIds()) {
            DtoServiceGroup dtoServiceGroup = new DtoServiceGroup();
            dtoServiceGroup.setId(serviceGroupId);
            dto.addServiceGroup(dtoServiceGroup);
        }
        for (Integer childId : customGroup.getChildIds()) {
            DtoCustomGroup dtoCustomGroup = new DtoCustomGroup();
            dtoCustomGroup.setId(childId);
            dto.addChild(dtoCustomGroup);
        }
        dto.setName(customGroup.getName());
        dto.setRoot(customGroup.getIsRoot());
        // TODO: consider adding bubble up status for RTMM path
        return dto;
    }
}


