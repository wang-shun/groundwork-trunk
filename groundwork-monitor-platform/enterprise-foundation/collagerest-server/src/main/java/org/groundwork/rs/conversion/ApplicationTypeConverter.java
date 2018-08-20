package org.groundwork.rs.conversion;

import com.groundwork.collage.CollageFactory;
import com.groundwork.collage.model.ApplicationEntityProperty;
import com.groundwork.collage.model.ApplicationType;
import com.groundwork.collage.model.EntityType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.metadata.MetadataService;
import org.groundwork.foundation.dao.FoundationQueryList;
import org.groundwork.foundation.dao.SortCriteria;
import org.groundwork.rs.dto.DtoApplicationType;
import org.groundwork.rs.dto.DtoDepthType;
import org.groundwork.rs.dto.DtoEntityProperty;
import org.groundwork.rs.dto.PropertiesSupport;

import java.util.Collection;
import java.util.List;

public class ApplicationTypeConverter {

    protected static Log log = LogFactory.getLog(ApplicationTypeConverter.class);

    public final static DtoApplicationType convert(ApplicationType applicationType, DtoDepthType depthType) {
        DtoApplicationType dto = new DtoApplicationType();
        if (applicationType != null) {
            dto.setId(applicationType.getApplicationTypeId());
            dto.setName(applicationType.getName());
            dto.setDisplayName(applicationType.getDisplayName());
            dto.setDescription(applicationType.getDescription());
            dto.setStateTransitionCriteria(applicationType.getStateTransitionCriteria());
            if (depthType == DtoDepthType.Deep) {
                MetadataService metadataService =  CollageFactory.getInstance().getMetadataService();
                  for(EntityType entityType : listEntityTypes(metadataService)) {
                    try {
                        List<ApplicationEntityProperty> propertyList = metadataService.getApplicationEntityProperties(entityType.getName(), applicationType.getName(), true);
                        if (propertyList.size() > 0) {
                            dto.addEntityType(EntityTypeConverter.convert(entityType));
                            for (ApplicationEntityProperty aep : propertyList) {
                                DtoEntityProperty entityProperty = EntityPropertyConverter.convert(aep);
                                dto.addEntityProperty(entityProperty);
                            }
                        }
                    }
                    catch (Exception e) {
                        log.error("Failed to convert ApplicationType's Entity or ApplicationEntityProperty", e);
                    }
                }
                //Set set = applicationType.getApplicationEntityProperties();
                dto.setProperties(PropertiesSupport.createDtoPropertyMap(applicationType.getProperties(false)));
            }
        }
        return dto;
    }

    public final static Collection<EntityType> listEntityTypes(MetadataService metadataService){
        FoundationQueryList list = metadataService.getEntityTypes(
                null, SortCriteria.asc(MetadataService.PROP_NAME), -1, -1);
        return list.getResults();
    }
}


