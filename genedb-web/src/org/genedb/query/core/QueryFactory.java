package org.genedb.query.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public class QueryFactory {

    @Autowired
    private ApplicationContext applicationContext;

    public Query retrieveQuery(String queryName) {
        Query ret = (Query) applicationContext.getBean(queryName, Query.class);
        return ret;
    }




}
