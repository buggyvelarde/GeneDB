package org.genedb.query;

import java.util.List;

public interface QueryI extends BasicQueryI {
        
        /**
         * Return the type of object that the query returns eg a SO term or publication
         * 
         * @return the type
         */
        public String getResultType();

        
        /**
         * Should the query's results be stored in the user's history, if one exists
         * 
         * @return true, if it should be stored
         */
        public boolean isStoredInHistory();
        
        //public boolean isCacheable();
        
        /**
         * A string representing the complete query and parameters. Can be parsed back using
         * an appropriate QueryStringParser
         * 
         * @see QueryStringParser
         * @return the string representation
         */
        public String getQueryAsString();
        
        /**
         * Set a detailer to use for displaying summaries of the results of this query, 
         * rather than the default
         * 
         * @param detailer The detailer, or null to reset to use the default
         */
        public void setSummaryDetailer(Detailer detailer);
        
        /**
         * Set a detailer to use for displaying a detailed view of one of the results 
         * of this query, rather than the default
         * 
         * @param detailer The detailer, or null to reset to use the default
         */
        public void setFineDetailer(Detailer detailer);
        
        
        /**
         * Write the config for this bean in Spring XML format
         * 
         * @param pw where to write to
         */
        // FIXME Should probably use XML dom mechanism
        //public void writeSpringBean(PrintWriter pw);

        
        /**
         * Get the list of Params that this query needs
         * 
         * @return a List of Param
         */
        public List<Param> getParameters();
    
    
    int getIndex();
    void setIndex(int index);
    


}
