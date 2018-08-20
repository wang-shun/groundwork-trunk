package com.groundwork.collage.query;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractQueryTranslatorInfo implements QueryTranslatorInfo {

    /**
     * basic translation map
     */
    protected Map<String, String> basicProperties = new ConcurrentHashMap<String, String>();

    protected String modelName;
    protected String select;
    protected String from;
    protected String countQuery;
    protected String propertiesPath;
    protected String propertiesAlias;

    public AbstractQueryTranslatorInfo(String modelName, String select, String from, String countQuery, String propertiesPath, String propertiesAlias) {
        this.modelName = modelName;
        this.select = select;
        this.from = from;
        this.countQuery = countQuery;
        this.propertiesPath = propertiesPath;
        this.propertiesAlias = propertiesAlias;
        createBasicProperties(basicProperties);
    }

    /**
     * override this to create properties
     * @param basicProperties
     */
    protected abstract void createBasicProperties(Map basicProperties);


    @Override
    public String translateName(String basicPropertyName) {
        return basicProperties.get(basicPropertyName.toLowerCase());
    }

    @Override
    public String getModelName() {
        return modelName;
    }

    @Override
    public String getSelectClause() {
        return select;
    }

    @Override
    public String getFromClause() {
        return from;
    }

    @Override
    public String getCountQuery() {
        return countQuery;
    }

    @Override
    public String getPropertiesPath() {
        return propertiesPath;
    }

    @Override
    public String getPropertiesAlias() {
        return propertiesAlias;
    }

}
