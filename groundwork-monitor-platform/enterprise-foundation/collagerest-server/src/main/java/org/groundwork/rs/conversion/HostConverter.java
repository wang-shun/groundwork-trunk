package org.groundwork.rs.conversion;

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.biz.model.RTMMHost;
import com.groundwork.collage.biz.model.RTMMService;
import com.groundwork.collage.model.*;
import com.groundwork.collage.model.impl.StateStatistics;
import com.groundwork.collage.model.impl.StatisticProperty;
import org.apache.commons.lang3.StringUtils;
import org.groundwork.foundation.bs.statistics.StatisticsService;
import org.groundwork.rs.dto.DtoComment;
import org.groundwork.rs.dto.DtoDepthType;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.PropertiesSupport;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class HostConverter {

    private static String [] SYNC_DEPTH_PROPERTIES = new String[]{"Alias", "Notes", "Parent"};

    public final static DtoHost convert(Host host, DtoDepthType depthType) {
        DtoHost dto = new DtoHost();
        if (depthType == DtoDepthType.Sync) {
            dto.setId(host.getHostId());
            dto.setHostName(host.getHostName());
            if (host.getApplicationType() != null) {
                dto.setAppType(host.getApplicationType().getName());
            }
            dto.setDeviceIdentification(host.getDevice().getIdentification());
            if (host.getHostStatus() != null) {
                dto.setProperties(PropertiesSupport.createDtoPropertyMap(host.getHostStatus().getProperties(true),
                        SYNC_DEPTH_PROPERTIES));
                if (dto.getProperties().isEmpty()) {
                    dto.setProperties(null);
                }
            }
            Set<ServiceStatus> statusSet = host.getServiceStatuses();
            for (ServiceStatus status : statusSet) {
                dto.addService(ServiceConverter.convert(status, DtoDepthType.Sync));
            }
            dto.setServiceCount(null);
            dto.setAcknowledged(null);
        } else {
            dto.setId(host.getHostId());
            dto.setHostName(host.getHostName());
            if (host.getApplicationType() != null) {
                dto.setAppType(host.getApplicationType().getName());
                dto.setAppTypeDisplayName(host.getApplicationType().getDisplayName());
            }
            dto.setAgentId(host.getAgentId());
            dto.setDescription(host.getDescription());
            if (depthType != DtoDepthType.Simple) {
                dto.setMonitorStatus(lookupLastMonitorStatus(host));
                dto.setDeviceIdentification(host.getDevice().getIdentification());
                dto.setDeviceDisplayName(host.getDevice().getDisplayName());
                if (host.getHostStatus() != null) {
                    if (host.getHostStatus().getLastCheckTime() != null) {
                        dto.setLastCheckTime(host.getHostStatus().getLastCheckTime());
                    }
                    if (host.getHostStatus().getNextCheckTime() != null) {
                        dto.setNextCheckTime(host.getHostStatus().getNextCheckTime());
                    }
                    if (host.getHostStatus().getStateType() != null) {
                        dto.setStateType(host.getHostStatus().getStateType().getName());
                    }
                    if (host.getHostStatus().getCheckType() != null) {
                        dto.setCheckType(host.getHostStatus().getCheckType().getName());
                    }
                    Object acknowledgedObj = host.getHostStatus().getProperty("isAcknowledged");
                    if (acknowledgedObj != null) {
                        dto.setAcknowledged(((Boolean) acknowledgedObj).booleanValue());
                    }
                }
                dto.setBubbleUpStatus(HostStatusSupport.calculateBubbleUpStatus(host, dto.getMonitorStatus()));
                DecimalFormat formatter = new DecimalFormat("##.##");
                dto.setServiceAvailability(formatter.format(HostStatusSupport.calculateServiceAvailability(host)));
                Set<ServiceStatus> statusSet = host.getServiceStatuses();
                dto.setServiceCount(statusSet.size());
                if (host.getHostStatus() != null) {
                    dto.setProperties(PropertiesSupport.createDtoPropertyMap(host.getHostStatus().getProperties(true)));
                    dto.setAlias((String) host.getHostStatus().getProperty("Alias"));
                    dto.setLastStateChange((Date) host.getHostStatus().getProperty("LastStateChange"));
                    dto.setLastPlugInOutput(HostStatusSupport.buildLastPluginOutputStringForHost(host.getHostStatus()));
                }
                if ((depthType == DtoDepthType.Deep) || (depthType == DtoDepthType.Full)) {
                    dto.setDevice(DeviceConverter.convert(host.getDevice(), DtoDepthType.Shallow));
                    if (host.getHostStatus() != null) {
                        dto.setHostStatus(HostStatusConverter.convert(host.getHostStatus()));
                    }

                    for (ServiceStatus status : statusSet) {
                        dto.addService(ServiceConverter.convert(status, depthType)); // DtoDepthType.Shallow));
                    }

                    Set<HostGroup> hostGroupsSet = host.getHostGroups();
                    for (HostGroup hostGroup : hostGroupsSet) {
                        dto.addHostGroup(HostGroupConverter.convert(hostGroup, DtoDepthType.Simple));
                    }

                    dto.setApplicationType(ApplicationTypeConverter.convert(host.getApplicationType(), DtoDepthType.Shallow));

                    if (StringUtils.equalsIgnoreCase(dto.getApplicationType().getName(), "NAGIOS")) {
                        // Ensure that the nagios-style comments embedded in the comments property are returned as part of the comments structure
                        List<DtoComment> comments = NagiosSupport.parseNagiosComments(dto.getProperty("Comments"));
                        if ((comments != null) && (comments.size() > 0)) dto.setComments(comments);
                    } else {
                        // Return foundation comments for non-nagios hosts
                        Set<Comment> comments = (Set<Comment>) host.getComments();
                        for (Comment comment : comments) {
                            dto.addComment(new DtoComment(comment.getCommentId(), comment.getNotes(), comment.getAuthor(), comment.getCreatedOn()));
                        }
                    }

                    if (depthType == DtoDepthType.Full) {
                        StatisticsService statisticService = CollageFactory.getInstance().getStatisticsService();
                        StateStatistics stateStatistics = statisticService.getServiceStatisticByHostName(dto.getHostName());
                        if (stateStatistics != null) {
                            List<StatisticProperty> stats = stateStatistics.getStatisticProperties();
                            for (StatisticProperty stat : stats) {
                                dto.addStatistic(StatisticConverter.convert(stat));
                            }
                        }
                    }
                }
            }
        }
        return dto;
    }

    protected final static String lookupLastMonitorStatus(Host host) {
        HostStatus status = host.getHostStatus();
        if (status == null)
            return null;
        MonitorStatus monStatus = status.getHostMonitorStatus();
        if (monStatus == null)
            return null;
        return monStatus.getName();
    }

    /**
     * Convert RTMM host to DTO host instance. Includes nested
     * RTMM service to DTO service conversion.
     *
     * @param host RTMM host
     * @return DTO host instance
     */
    public final static DtoHost convert(RTMMHost host) {
        DtoHost dto = new DtoHost();
        dto.setId(host.getId());
        for (RTMMService service : host.getServices()) {
            dto.addService(ServiceConverter.convert(service));
        }
        dto.setHostName(host.getHostName());
        dto.setMonitorStatus(host.getMonitorStatus());
        dto.setAlias(host.getAlias());
        dto.setAppType(host.getAppTypeName());
        dto.setAppTypeDisplayName(host.getAppTypeDisplayName());
        dto.setLastCheckTime(host.getLastCheckTime());
        dto.setLastStateChange(host.getLastStateChange());
        if (host.getIsAcknowledged() != null) {
            dto.setAcknowledged(host.getIsAcknowledged());
        }
        if (host.getIsProblemAcknowledged() != null) {
            dto.getProperties().put("isProblemAcknowledged", host.getIsProblemAcknowledged().toString());
        }
        if (host.getScheduledDowntimeDepth() != null) {
            dto.getProperties().put("ScheduledDowntimeDepth", host.getScheduledDowntimeDepth().toString());
        }
        dto.setBubbleUpStatus(HostStatusSupport.calculateBubbleUpStatus(host, dto.getMonitorStatus()));
        DecimalFormat formatter = new DecimalFormat("##.##");
        dto.setServiceAvailability(formatter.format(HostStatusSupport.calculateServiceAvailability(host)));
        dto.setLastPlugInOutput(HostStatusSupport.buildLastPluginOutputStringForHost(host));
        return dto;
    }
}
