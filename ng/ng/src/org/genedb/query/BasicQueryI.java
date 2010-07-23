package org.genedb.query;


public interface BasicQueryI {
    
    /**
     * Get the unique name representing this query
     * 
     * @return the name
     */
    public String getName();
    
    
    /**
     * A user-friendly description of the query
     * 
     * @return the description
     */
    String getSimpleDescription();
    
    
    /**
     * Check if the query has a set of valid Parameters
     * 
     * @return true, if the query is ready to run
     */
    boolean isComplete();
    
        
        /**
         * Execute this query, or return a cached, Result
         * 
         * @return the result
         */
        public Result process();
    
}
