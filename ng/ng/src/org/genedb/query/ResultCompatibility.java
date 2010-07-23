package org.genedb.query;


public interface ResultCompatibility {
    
    /**
     * Compare what two datasets are containing, to see if they are compatible ie can they be merged etc 
     * 
     * @return whether the two data sets are compatible
     */
    public boolean areCompatible(Result one, Result two);
    
}
