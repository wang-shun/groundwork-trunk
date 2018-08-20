package org.groundwork.rs.conversion;

import com.groundwork.collage.model.PropertyType;
import org.groundwork.rs.dto.DtoPropertyDataType;
import org.groundwork.rs.dto.DtoPropertyType;

public class PropertyTypeConverter {

    public final static DtoPropertyType convert(PropertyType propertyType) {
        DtoPropertyType dto = new DtoPropertyType();
        if (propertyType != null) {
            dto.setId(propertyType.getPropertyTypeId());
            dto.setName(propertyType.getName());
            dto.setDescription(propertyType.getDescription());
            dto.setDataType(DtoPropertyDataType.DtoPropertyDataTypeWrapper.valueOf(propertyType.getPrimitiveType()).getType());
        }
        return dto;
    }

}
