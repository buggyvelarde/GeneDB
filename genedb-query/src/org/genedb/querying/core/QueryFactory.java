package org.genedb.querying.core;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryFactory {

    private static Logger logger = Logger.getLogger(QueryFactory.class);

    @Autowired
    private ApplicationContext applicationContext;

    Map<QueryVisibility, List<String>> queryNameMap;
    Map<String, Query> queryMap;
    
    public void setQueryNameMap(Map<QueryVisibility, List<String>> queryNameMap) throws Exception
    {
    	this.queryNameMap = queryNameMap;
    	this.queryMap = new HashMap<String, Query>();
    	for (QueryVisibility visibility : queryNameMap.keySet())
    	{
    		for (String queryName : queryNameMap.get(visibility))
    		{
    			Query query = applicationContext.getBean(queryName, Query.class);
    			if (query == null)
    			{
    				String message = "Could not find query with name : " + queryName + " !";
    				logger.error(message);
    				throw new Exception(message);
    			}
    			queryMap.put(queryName, query);
    		}
    	}
    }
    
    /**
     * Retrieves a query with of a certain name.
     * 
     * @param queryName
     * @return
     */
    public Query retrieveQuery(String queryName) 
    {
    	logger.debug(queryName);
    	String fullName = queryName + "Query";
    	if (queryMap.containsKey(fullName))
    	{
    		logger.debug(queryName + " -- " + queryMap.get(fullName));
    		return queryMap.get(fullName);
    	}
    	return null;
    }
        
    
    /**
     * Lists all available queries.
     * 
     * @param filterName
     * @return
     */
    public Map<String, Query> listQueries(String filterName) 
    {
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
    	
    	for (QueryVisibility queryVisibility : queryNameMap.keySet())
    	{
    		if (queryVisibility.compareTo(visibility) != -1)
    		{
    			for (String queryName : queryNameMap.get(queryVisibility))
    			{
    				filteredMap.put(queryName, queryMap.get(queryName));
    				logger.debug(queryName + " -- " + queryMap.get(queryName));
    			}
    		}
    	}
        return filteredMap;
    }
    


}
