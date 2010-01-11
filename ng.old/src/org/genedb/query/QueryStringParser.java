package org.genedb.query;

/**
 * Generates a tree of queries and params from a String representation
 * 
 * @author art
 */
public interface QueryStringParser {

    public QueryI parseQueryString(String queryString);
    
}
