package org.genedb.query.params;


import java.util.List;


/**
 * Interface indicating a Param which can only take 
 * values from a specified list.
 * 
 * @author art
 */
public interface ListConstraint {
    
    public List<String> getAllAcceptableValues();
    
    public List<String> getAcceptableValues(String partName, boolean mustBePrefix);
    
    public boolean isValid(String value);
    
}
