package org.genedb.querying.core;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

/**
 * A factory for returning queries, with the option of filtering them by name or by {@link QueryVisibility}.
 *
 * @author art
 * @author gv1
 *
 */
public class QueryFactory {

    private static Logger logger = Logger.getLogger(QueryFactory.class);

    @Autowired
    private ApplicationContext applicationContext;

    Map<QueryVisibility, List<String>> queryNameMap;

    Map<String, Query> queryMap;

    public void setQueryNameMap(Map<QueryVisibility, List<String>> queryNameMap) throws IllegalArgumentException {
        this.queryNameMap = queryNameMap;
        this.queryMap = Maps.newHashMapWithExpectedSize(queryNameMap.size());
        for (List<String> names : queryNameMap.values()) {
            for (String queryName : names) {
                Query query = applicationContext.getBean(queryName, Query.class);
                if (query == null) {
                    throw new IllegalArgumentException("Could not find query with name '" + queryName + "'");
                }
                queryMap.put(queryName, query);
            }
        }
    }

    /**
     * Retrieves a query with of a certain name.
     *
     * @param queryName
     * @return a Query
     */
    // FIXME Overrides prototype!
    public Query retrieveQuery(String queryName) {
        logger.debug(queryName);
        String fullName = queryName + "Query";
        if (queryMap.containsKey(fullName)) {
            logger.debug(queryName + " -- " + queryMap.get(fullName));
            return queryMap.get(fullName);
        }
        return null;
    }

    /**
     *
     * @return a map of all the queries
     */
    public Map<String, Query> listQueries() {
        return Collections.unmodifiableMap(queryMap);
    }

    /**
     * Lists all available queries.
     *
     * @param filterName
     * @return a filtered map of queries
     */
    public Map<String, Query> listQueries(String filterName) {
        if (StringUtils.hasLength(filterName)) {
            return listQueries();
        }
        return filterByName(queryMap, filterName);
    }

    /**
     *
     * Filters the available queries by visibility.
     *
     * @param visibility
     * @return
     */
    public Map<String, Query> listQueries(QueryVisibility visibility) {
        Map<String, Query> filteredMap = new HashMap<String, Query>();
        for (QueryVisibility queryVisibility : queryNameMap.keySet()) {
            if (queryVisibility.compareTo(visibility) != -1) {
                for (String queryName : queryNameMap.get(queryVisibility)) {
                    logger.debug(queryName + " -- " + queryMap.get(queryName));
                    filteredMap.put(queryName, queryMap.get(queryName));
                }
            }
        }
        return filteredMap;
    }

    /**
     *
     * Lists queries that are of equal or greater than visibility than the supplied visibility, filtered by name.
     *
     * @author gv1
     *
     * @param filterName
     * @param visibility
     * @return a map of queries
     */
    public Map<String, Query> listQueries(String filterName, QueryVisibility visibility) {
        Map<String, Query> visibleQueries = listQueries(visibility);
        if ((filterName == null) || (filterName.length() == 0)) {
            return visibleQueries;
        }
        return filterByName(visibleQueries, filterName);
    }

    private Map<String, Query> filterByName(Map<String, Query> inMap, String filterName) {
        Map<String, Query> filteredMap = new HashMap<String, Query>();
        for (String queryName : inMap.keySet()) {
            if (queryName.contains(filterName)) {
                filteredMap.put(queryName, queryMap.get(queryName));
            }
        }
        return filteredMap;
    }

}
