package com.groundwork.collage.query;

import java.util.Map;

public class CategoryQueryTranslatorInfo extends AbstractQueryTranslatorInfo implements QueryTranslatorInfo {

    public CategoryQueryTranslatorInfo() {
        super("Category", "select distinct c", " from Category c ", "select distinct count(*) from Category c ", null, null);
    }

    protected void createBasicProperties(Map properties) {
        properties.put("id".toLowerCase(), "categoryId");
        properties.put("entityTypeName".toLowerCase(), "c.entityType.name");
        properties.put("appType".toLowerCase(), "c.applicationType.name");
        properties.put("applicationType".toLowerCase(), "c.applicationType.name");
        //properties.put("entities".toLowerCase(), "c.categoryEntities");
    }
    
}
