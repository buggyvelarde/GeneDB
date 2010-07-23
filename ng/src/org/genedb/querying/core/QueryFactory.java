package org.genedb.querying.core;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * A factory for returning queries, with the option of filtering them by name or by {@link NumericQueryVisibility}.
 *
 * @author art
 * @author gv1
 *
 */
public class QueryFactory<T extends QueryVisibility> {

    private static Logger logger = Logger.getLogger(QueryFactory.class);

    @Autowired
    private ApplicationContext applicationContext;

    private Map<T, List<QueryDetails>> visibilityQueryDetails;

    private Map<String, T> nameVisibility;

    public void setQueryNameMap(LinkedHashMap<T, Map<String, Query>> queryNameMap) {

        this.visibilityQueryDetails = Maps.newHashMapWithExpectedSize(queryNameMap.size());
        this.nameVisibility = Maps.newHashMap();

        for (T visibility : queryNameMap.keySet()) {
            List<QueryDetails> queryDetails = Lists.newArrayList();
            visibilityQueryDetails.put(visibility, queryDetails);
            for (Map.Entry<String, Query> entry : queryNameMap.get(visibility).entrySet()) {
                String realName = entry.getKey();
                Query q = entry.getValue();
                QueryDetails qd = new QueryDetails(realName, q.getQueryName(), q.getQueryDescription());
                queryDetails.add(qd);
                nameVisibility.put(realName, visibility);
            }
        }
    }

    /**
     * Retrieves a query with of a certain name.
     *
     * @param queryName
     * @return a Query
     * @throws IllegalAccessException
     */
    public Query retrieveQuery(String queryName, T visibility) {
        T v = nameVisibility.get(queryName);
        if (v == null || !v.includesVisibility(visibility)) {
            logger.error(String.format("Can't access query '%s' as it's visibility '%s' is below required '%s'", queryName, v, visibility));
            return null;
        }
        return applicationContext.getBean(queryName, Query.class);
    }

    /**
     *
     * Filters the available queries by visibility.
     *
     * @param visibility
     * @return
     */
    private List<QueryDetails> listQueries(T visibility) {
        List<QueryDetails> ret = Lists.newArrayList();
        for (Map.Entry<T, List<QueryDetails>> entry : visibilityQueryDetails.entrySet()) {
            if (entry.getKey().includesVisibility(visibility)) {
                ret.addAll(entry.getValue());
            }
        }
        return ret;
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
    public List<QueryDetails> listQueries(String filterName, T visibility) {
        List<QueryDetails> visibleQueries = listQueries(visibility);
        if (!StringUtils.hasLength(filterName)) {
            return visibleQueries;
        }
        return filterByName(visibleQueries, filterName);
    }

    private List<QueryDetails> filterByName(List<QueryDetails> in, String filterName) {
        List<QueryDetails> ret = Lists.newArrayList();
        for (QueryDetails qd : in) {
            if (qd.getRealName().contains(filterName)) {
                ret.add(qd);
            }
        }
        return ret;
    }

}
