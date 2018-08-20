package org.groundwork.rs.conversion;

import com.groundwork.collage.biz.model.RTMMService;
import com.groundwork.collage.model.Comment;
import com.groundwork.collage.model.ServiceStatus;
import org.apache.commons.lang3.StringUtils;
import org.groundwork.rs.dto.DtoComment;
import org.groundwork.rs.dto.DtoDepthType;
import org.groundwork.rs.dto.DtoService;
import org.groundwork.rs.dto.PropertiesSupport;

import java.util.List;
import java.util.Set;

public class ServiceConverter {

    private static String [] SYNC_DEPTH_PROPERTIES = new String[]{"Notes"};

    public final static DtoService convert(ServiceStatus service, DtoDepthType depthType) {
        DtoService dto = new DtoService();
        if (depthType == DtoDepthType.Sync) {
            dto.setId(service.getServiceStatusId());
            dto.setAppType(service.getApplicationType().getName());
            dto.setDescription(service.getServiceDescription());
            dto.setProperties(PropertiesSupport.createDtoPropertyMap(service.getProperties(true),
                    SYNC_DEPTH_PROPERTIES));
            if (dto.getProperties().isEmpty()) {
                dto.setProperties(null);
            }
            dto.setMonitorServer(null);
            dto.setAgentId(service.getAgentId());
        } else {
            dto.setId(service.getServiceStatusId());
            dto.setAppType(service.getApplicationType().getName());
            dto.setAppTypeDisplayName(service.getApplicationType().getDisplayName());
            dto.setDescription(service.getServiceDescription());
            dto.setAgentId(service.getAgentId());
            if (depthType != DtoDepthType.Simple) {
                if (service.getHost() != null) {
                    dto.setHostName(service.getHost().getHostName());
                    if (service.getHost().getDevice() != null) {
                        dto.setDeviceIdentification(service.getHost().getDevice().getIdentification());
                    }
                }
                if (service.getMonitorStatus() != null)
                    dto.setMonitorStatus(service.getMonitorStatus().getName());
                dto.setLastCheckTime(service.getLastCheckTime());
                dto.setNextCheckTime(service.getNextCheckTime());
                dto.setLastStateChange(service.getLastStateChange());
                dto.setMetricType(service.getMetricType());
                dto.setDomain(service.getDomain());
                if (service.getLastHardState() != null)
                    dto.setLastHardState(service.getLastHardState().getName());
                if (service.getCheckType() != null)
                    dto.setCheckType(service.getCheckType().getName());
                if (service.getStateType() != null)
                    dto.setStateType(service.getStateType().getName());
                dto.setLastPlugInOutput(HostStatusSupport.buildLastPluginOutputStringForService(service));
                dto.setProperties(PropertiesSupport.createDtoPropertyMap(service.getProperties(true)));
            }

            if ((depthType == DtoDepthType.Deep) || (depthType == DtoDepthType.Full)) {
                if (StringUtils.equalsIgnoreCase(dto.getAppType(), "NAGIOS")) {
                    // Ensure that the nagios-style comments embedded in the comments property are returned as part of the comments structure
                    List<DtoComment> comments = NagiosSupport.parseNagiosComments(dto.getProperty("Comments"));
                    if ((comments != null) && (comments.size() > 0)) dto.setComments(comments);
                } else {
                    Set<Comment> comments = (Set<Comment>) service.getComments();
                    for (Comment comment : comments) {
                        dto.addComment(new DtoComment(comment.getCommentId(), comment.getNotes(), comment.getAuthor(), comment.getCreatedOn()));
                    }
                }
            }
        }
        return dto;
    }

    /**
     * Convert RTMM service to DTO service instance.
     *
     * @param service RTMM service
     * @return DTO host instance
     */
    public final static DtoService convert(RTMMService service) {
        DtoService dto = new DtoService();
        dto.setId(service.getId());
        dto.setDescription(service.getDescription());
        dto.setMonitorStatus(service.getMonitorStatus());
        dto.setAppType(service.getAppTypeName());
        dto.setAppTypeDisplayName(service.getAppTypeDisplayName());
        dto.setLastCheckTime(service.getLastCheckTime());
        dto.setNextCheckTime(service.getNextCheckTime());
        dto.setLastStateChange(service.getLastStateChange());
        if (service.getIsProblemAcknowledged() != null) {
            dto.getProperties().put("isProblemAcknowledged", service.getIsProblemAcknowledged().toString());
        }
        dto.setLastPlugInOutput(HostStatusSupport.buildLastPluginOutputStringForService(service));
        return dto;
    }
}

