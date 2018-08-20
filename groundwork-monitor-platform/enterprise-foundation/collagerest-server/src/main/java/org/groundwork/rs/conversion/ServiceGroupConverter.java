package org.groundwork.rs.conversion;

import com.groundwork.collage.biz.model.RTMMServiceGroup;
import com.groundwork.collage.model.Category;
import com.groundwork.collage.model.ServiceStatus;
import org.groundwork.rs.dto.DtoDepthType;
import org.groundwork.rs.dto.DtoService;
import org.groundwork.rs.dto.DtoServiceGroup;

import java.util.List;

public class ServiceGroupConverter {

    public static DtoServiceGroup convert(Category category, DtoDepthType depth) {
        return convert(category, null, depth);
    }

    public static DtoServiceGroup convert(Category category, List<ServiceStatus> services, DtoDepthType depth) {
        DtoServiceGroup dto = new DtoServiceGroup();
        dto.setId(category.getCategoryId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setAgentId(category.getAgentId());
        if (category.getApplicationType() != null) {
            dto.setAppType(category.getApplicationType().getName());
            dto.setAppTypeDisplayName(category.getApplicationType().getDisplayName());
        }
        if (services != null) {
            for (ServiceStatus service : services) {
                dto.addService(ServiceConverter.convert(service, depth));
            }
        }
        dto.setBubbleUpStatus(ServiceGroupStatusSupport.calculateBubbleUpStatus(dto));
        return dto;
    }

    /**
     * Convert RTMM service group to DTO service group instance.
     *
     * @param serviceGroup RTMM service group
     * @return DTO service group instance
     */
    public static DtoServiceGroup convert(RTMMServiceGroup serviceGroup) {
        DtoServiceGroup dto = new DtoServiceGroup();
        dto.setId(serviceGroup.getId());
        for (Integer serviceId : serviceGroup.getServiceIds()) {
            DtoService dtoService = new DtoService();
            dtoService.setId(serviceId);
            dto.addService(dtoService);
        }
        dto.setName(serviceGroup.getName());
        dto.setAppType(serviceGroup.getAppTypeName());
        // TODO: consider adding bubble up status for RTMM path
        return dto;
    }
}


