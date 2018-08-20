package com.groundwork.collage.query;

import java.util.Map;

public class ApplicationTypeQueryTranslatorInfo extends AbstractQueryTranslatorInfo implements QueryTranslatorInfo {

    public ApplicationTypeQueryTranslatorInfo() {
        super("ApplicationType", "select distinct t", " from ApplicationType t ", "select distinct count(*) from ApplicationType t ", null, null);
    }

    protected void createBasicProperties(Map properties) {
        properties.put("id".toLowerCase(), "applicationTypeId");
    }
}
