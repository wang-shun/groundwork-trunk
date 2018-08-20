package org.groundwork.rs.conversion;

import com.groundwork.collage.util.MonitorStatusBubbleUp;
import org.groundwork.rs.dto.DtoService;
import org.groundwork.rs.dto.DtoServiceGroup;

class ServiceGroupStatusSupport {

    private static final MonitorStatusBubbleUp.MonitorStatusExtractor<DtoService> BUBBLE_UP_EXTRACTOR =
            new MonitorStatusBubbleUp.MonitorStatusExtractor<DtoService>() {
                @Override
                public String extractMonitorStatus(DtoService obj) {
                    return obj.getMonitorStatus();
                }
            };

    /**
     * Calculate bubble up status.
     *
     * @param dto dtoServiceGroup
     * @return bubble up status
     */
    static String calculateBubbleUpStatus(DtoServiceGroup dto) {
        return (dto == null ? null : MonitorStatusBubbleUp.computeServiceGroupMonitorStatusBubbleUp(dto.getServices(), BUBBLE_UP_EXTRACTOR));
    }

}
