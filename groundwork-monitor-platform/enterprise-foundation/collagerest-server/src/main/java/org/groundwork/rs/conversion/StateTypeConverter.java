package org.groundwork.rs.conversion;

import com.groundwork.collage.model.StateType;
import org.groundwork.rs.dto.DtoStateType;

public class StateTypeConverter {

    public final static DtoStateType convert(StateType stateType) {
        DtoStateType dto = new DtoStateType();
        if (stateType != null) {
            dto.setStateTypeId(stateType.getStateTypeId());
            dto.setName(stateType.getName());
            dto.setDescription(stateType.getDescription());
        }
        return dto;
    }

}
