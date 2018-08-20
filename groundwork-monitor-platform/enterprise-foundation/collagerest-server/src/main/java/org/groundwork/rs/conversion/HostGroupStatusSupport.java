package org.groundwork.rs.conversion;


import com.groundwork.collage.util.MonitorStatusBubbleUp;
import org.groundwork.rs.dto.DtoHost;
import org.groundwork.rs.dto.DtoHostGroup;

class HostGroupStatusSupport {

    private static final MonitorStatusBubbleUp.MonitorStatusExtractor<DtoHost> BUBBLE_UP_EXTRACTOR =
            new MonitorStatusBubbleUp.MonitorStatusExtractor<DtoHost>() {
                @Override
                public String extractMonitorStatus(DtoHost obj) {
                    return obj.getBubbleUpStatus();
                }
            };

    /**
     * Calculate bubble up status.
     *
     * @param dto dtoHostGroup
     * @return bubble up status
     */
    static String calculateBubbleUpStatus(DtoHostGroup dto) {
        return (dto == null ? null : MonitorStatusBubbleUp.computeHostGroupMonitorStatusBubbleUp(dto.getHosts(), BUBBLE_UP_EXTRACTOR));
    }

}
