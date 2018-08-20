package org.groundwork.rs.conversion;

import com.groundwork.collage.util.MonitorStatusBubbleUp;
import org.apache.commons.lang3.StringUtils;
import org.groundwork.rs.dto.DtoCustomGroup;
import org.groundwork.rs.dto.DtoHostGroup;
import org.groundwork.rs.dto.DtoServiceGroup;

class CustomGroupStatusSupport {

    private static final MonitorStatusBubbleUp.MonitorStatusExtractor<DtoCustomGroup> CHILD_BUBBLE_UP_EXTRACTOR =
            new MonitorStatusBubbleUp.MonitorStatusExtractor<DtoCustomGroup>() {
                @Override
                public String extractMonitorStatus(DtoCustomGroup obj) {
                    return obj.getBubbleUpStatus();
                }
            };

    private static final MonitorStatusBubbleUp.MonitorStatusExtractor<DtoHostGroup> HOSTGROUP_BUBBLE_UP_EXTRACTOR =
            new MonitorStatusBubbleUp.MonitorStatusExtractor<DtoHostGroup>() {
                @Override
                public String extractMonitorStatus(DtoHostGroup obj) {
                    return obj.getBubbleUpStatus();
                }
            };

    private static final MonitorStatusBubbleUp.MonitorStatusExtractor<DtoServiceGroup> SERVICEGROUP_BUBBLE_UP_EXTRACTOR =
            new MonitorStatusBubbleUp.MonitorStatusExtractor<DtoServiceGroup>() {
                @Override
                public String extractMonitorStatus(DtoServiceGroup obj) {
                    return obj.getBubbleUpStatus();
                }
            };

    /**
     * Calculate bubble up status.
     *
     * @param dto dtoCustomGroup
     * @return bubble up status
     */
    static String calculateBubbleUpStatus(DtoCustomGroup dto) {
        if (dto == null) return null;
        String customGroupStatus = MonitorStatusBubbleUp.computeHostGroupMonitorStatusBubbleUp(dto.getChildren(), CHILD_BUBBLE_UP_EXTRACTOR);
        String hostGroupStatus = MonitorStatusBubbleUp.computeHostGroupMonitorStatusBubbleUp(dto.getHostGroups(), HOSTGROUP_BUBBLE_UP_EXTRACTOR);
        String serviceGroupStatus = MonitorStatusBubbleUp.SERVICE_TO_HOST_MONITOR_STATUS_TRANSLATOR
                .get(MonitorStatusBubbleUp.computeServiceGroupMonitorStatusBubbleUp(dto.getServiceGroups(), SERVICEGROUP_BUBBLE_UP_EXTRACTOR));
        return combineStatus(customGroupStatus, hostGroupStatus, serviceGroupStatus);
    }

    private static String combineStatus(String... statuses) {
        int minimumStatus = Integer.MAX_VALUE;
        for (String status : statuses) {
            if (StringUtils.isNotBlank(status) && MonitorStatusBubbleUp.HOST_MONITOR_STATUS_DICTIONARY.contains(status)) {
                minimumStatus = Math.min(minimumStatus, MonitorStatusBubbleUp.HOST_MONITOR_STATUS_DICTIONARY.indexOf(status));
            }
        }
        return (minimumStatus == Integer.MAX_VALUE ? null : MonitorStatusBubbleUp.HOST_MONITOR_STATUS_DICTIONARY.get(minimumStatus));
    }

}
