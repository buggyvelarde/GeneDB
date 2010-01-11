package org.genedb.querying.core;

public class QueryDetails {
    private String realName;
    private String queryName;
    private String queryDescription;

    public QueryDetails(String realName, String queryName, String queryDescription) {
        this.realName = realName;
        this.queryName = queryName;
        this.queryDescription = queryDescription;
    }

    public String getRealName() {
        return realName;
    }

    public String getQueryName() {
        return queryName;
    }

    public String getQueryDescription() {
        return queryDescription;
    }

}