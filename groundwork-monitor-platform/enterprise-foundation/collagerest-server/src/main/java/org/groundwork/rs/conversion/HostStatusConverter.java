package org.groundwork.rs.conversion;

import com.groundwork.collage.model.HostStatus;
import org.groundwork.rs.dto.*;

public class HostStatusConverter {

    public final static DtoHostStatus convert(HostStatus status) {
        DtoHostStatus dto = new DtoHostStatus();
        dto.setHostStatusId(status.getHostStatusId());
        dto.setLastCheckTime(status.getLastCheckTime());
        dto.setNextCheckTime(status.getNextCheckTime());
        dto.setHostMonitorStatus(MonitorStatusConverter.convert(status.getHostMonitorStatus()));
        dto.setCheckType(CheckTypeConverter.convert(status.getCheckType()));
        dto.setStateType(StateTypeConverter.convert(status.getStateType()));
        return dto;
    }

}
