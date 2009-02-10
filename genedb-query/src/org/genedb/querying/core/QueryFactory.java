package org.genedb.querying.core;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.Map;

public class QueryFactory {

    private static Logger logger = Logger.getLogger(QueryFactory.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    Map<String, Query> queryMap;



    public Query retrieveQuery(String queryName) {
        String fullName = queryName + "Query";
        if (!applicationContext.containsBean(fullName)) {
            return null;
        }
        Query ret = applicationContext.getBean(fullName, Query.class);
        return ret;
    }

    public Map<String, Query> listQueries(String filterName) {
        for (Map.Entry<String, Query> entry : queryMap.entrySet()) {
            logger.error(entry.getKey()+" : "+entry.getValue());
        }
        return queryMap;
    }


}
