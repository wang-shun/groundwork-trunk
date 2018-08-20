package com.groundwork.collage.query;

import java.util.Map;

public class PropertyTypeQueryTranslatorInfo extends AbstractQueryTranslatorInfo implements QueryTranslatorInfo {

    public PropertyTypeQueryTranslatorInfo() {
        super("PropertyType", "select distinct t", " from PropertyType t ", "select distinct count(*) from PropertyType t ", null, null);
    }

    protected void createBasicProperties(Map properties) {
        properties.put("id".toLowerCase(), "propertyTypeId");
    }
}
