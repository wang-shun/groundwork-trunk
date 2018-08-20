package com.groundwork.collage.query;

import java.util.Map;

public class EntityTypeQueryTranslatorInfo extends AbstractQueryTranslatorInfo implements QueryTranslatorInfo {

    public EntityTypeQueryTranslatorInfo() {
        super("EntityType", "select distinct t", " from EntityType t ", "select distinct count(*) from EntityType t ", null, null);
    }

    protected void createBasicProperties(Map properties) {
        properties.put("id".toLowerCase(), "entityTypeId");
    }
}
