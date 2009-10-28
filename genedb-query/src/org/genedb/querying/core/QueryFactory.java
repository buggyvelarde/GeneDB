package org.genedb.querying.core;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
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
    
    /**
     * Lists all available queries.
     * 
     * @param filterName
     * @return
     */
    public Map<String, Query> listQueries(String filterName) {
        for (Map.Entry<String, Query> entry : queryMap.entrySet()) {
            logger.error(entry.getKey()+" : "+entry.getValue());
        }
        return queryMap;
    }
    
    /**
     * 
     * Lists queries that are of equal or greater than visibility than the supplied visibility. 
     * 
     * @author gv1
     * 
     * @param filterName
     * @param visibility
     * @return
     */
    public Map<String, Query> listQueries(String filterName, QueryVisibility visibility) 
    {
    	Map<String, Query> filteredMap = new HashMap<String, Query>();
    	for (String key : queryMap.keySet())
    	{
    		Query query = queryMap.get(key);    		
    		// if this Query object's visibility is equal or greater than that of the filter
    		// then it should be included
    		if (query.getVisibility().compareTo(visibility) != -1)
    		{
    			filteredMap.put(key, query);
    		}
    	}
        return filteredMap;
    }
    
    


}
