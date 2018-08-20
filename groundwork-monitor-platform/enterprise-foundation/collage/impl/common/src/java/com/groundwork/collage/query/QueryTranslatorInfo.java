package com.groundwork.collage.query;

public interface QueryTranslatorInfo {

    /**
     * Translate a shorthand basic property name to full property name
     * @return the translated for full property name suitable for HQL consumption or null if not found
     */
    String translateName(String basicPropertyName);

    /**
     * Get the model name for this info record such as host or hostGroup
     * @return the model name
     */
    String getModelName();

    /**
     * Get the property data member path for accessing dynamic properties
     * @return the path for dynamic properties
     */
    String getPropertiesPath();

    /**
     * The translated SELECT clause plus any aliases.
     * @return Returns the SELECT clause
     */
    String getSelectClause();

    /**
     * The translated FROM clause
     * @return the FROM clause of the query statement
     */
    String getFromClause();

    /**
     * Return the base count query statement common for all queries
     * @return the count query string
     */
    String getCountQuery();


    /**
     * Return the alias used for property subqueries
     * @return the alias name for properties
     */
    String getPropertiesAlias();

}
