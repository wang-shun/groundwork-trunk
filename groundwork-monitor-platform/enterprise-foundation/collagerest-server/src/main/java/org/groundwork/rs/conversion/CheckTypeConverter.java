package org.groundwork.rs.conversion;

import com.groundwork.collage.model.CheckType;
import org.groundwork.rs.dto.DtoCheckType;

public class CheckTypeConverter {

    public final static DtoCheckType convert(CheckType checkType) {
        DtoCheckType dto = new DtoCheckType();
        if (checkType != null) {
            dto.setCheckTypeId(checkType.getCheckTypeId());
            dto.setName(checkType.getName());
            dto.setDescription(checkType.getDescription());
        }
        return dto;
    }

}
