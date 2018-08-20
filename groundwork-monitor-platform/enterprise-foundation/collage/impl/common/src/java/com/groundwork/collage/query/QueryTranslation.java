package com.groundwork.collage.query;

import java.util.ArrayList;
import java.util.List;

public class QueryTranslation {

    private final String originalQuery;
    private String hql;
    private String countHql;
    private List<QuerySubstitution> substitutions;

    public QueryTranslation(String originalQuery) {
        this.originalQuery =originalQuery;
        substitutions = new ArrayList<QuerySubstitution>();
    }

    public String getOriginalQuery() {
        return originalQuery;
    }

    public String getHql() {
        return hql;
    }

    public void setHql(String hql) {
        this.hql = hql;
    }

    public QuerySubstitution add(QuerySubstitution.QuerySubstitutionType substitutionType, String value) {
        String placeholder = String.format("$%d", substitutions.size() + 1);
        QuerySubstitution substitution =
                new  QuerySubstitution(substitutionType, placeholder, value);
        substitutions.add(substitution);
        return substitution;
    }

    public List<QuerySubstitution> getSubstitutions() {
        return substitutions;
    }

    public boolean hasSubstitutions() {
        return (substitutions.size() > 0);
    }

    public String getCountHql() {
        return countHql;
    }

    public void setCountHql(String countHql) {
        this.countHql = countHql;
    }
}
