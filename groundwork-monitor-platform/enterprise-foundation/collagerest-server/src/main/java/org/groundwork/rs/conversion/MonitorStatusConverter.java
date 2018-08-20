package org.groundwork.rs.conversion;

import com.groundwork.collage.model.MonitorStatus;
import org.groundwork.rs.dto.DtoMonitorStatus;

public class MonitorStatusConverter {

    public final static DtoMonitorStatus convert(MonitorStatus status) {
        DtoMonitorStatus dto = new DtoMonitorStatus();
        dto.setMonitorStatusId(status.getMonitorStatusId());
        dto.setName(status.getName());
        dto.setDescription(status.getDescription());
        return dto;
    }

}
