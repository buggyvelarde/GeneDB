package org.genedb.querying.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public class QueryFactory {

    @Autowired
    private ApplicationContext applicationContext;

    public Query retrieveQuery(String queryName) {
    	String fullName = queryName + "Query";
    	if (!applicationContext.containsBean(fullName)) {
    		return null;
    	}
        Query ret = (Query) applicationContext.getBean(fullName, Query.class);
        return ret;
    }




}
