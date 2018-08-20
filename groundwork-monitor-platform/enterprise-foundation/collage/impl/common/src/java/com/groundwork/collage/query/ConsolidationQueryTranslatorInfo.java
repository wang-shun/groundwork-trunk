package com.groundwork.collage.query;

import java.util.Map;

public class ConsolidationQueryTranslatorInfo extends AbstractQueryTranslatorInfo implements QueryTranslatorInfo {

    public ConsolidationQueryTranslatorInfo() {
        super("ConsolidationCriteria", "select distinct c", " from ConsolidationCriteria c ", "select distinct count(*) from ConsolidationCriteria c ", null, null);
    }

    protected void createBasicProperties(Map properties) {
        properties.put("id".toLowerCase(), "consolidationCriteriaId");
    }
}
