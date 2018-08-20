package com.groundwork.collage.query;

public class QuerySubstitution {

    public enum QuerySubstitutionType {
        CATEGORY_EQUAL,
        CATEGORY_IN
    };

    private QuerySubstitutionType substitutionType;
    private String placeHolder;
    private String value;

    public QuerySubstitution(QuerySubstitutionType substitutionType, String placeHolder, String value) {
        this.substitutionType = substitutionType;
        this.placeHolder = placeHolder;
        this.value = value;
    }

    public QuerySubstitutionType getSubstitutionType() {
        return substitutionType;
    }

    public String getPlaceHolder() {
        return placeHolder;
    }

    public String getValue() {
        return value;
    }
}
