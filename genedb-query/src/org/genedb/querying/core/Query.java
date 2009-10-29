package org.genedb.querying.core;

import org.springframework.core.Ordered;
import org.springframework.validation.Validator;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface Query extends Ordered, Validator, Serializable {

    String getQueryDescription();

    List getResults() throws QueryException;

    String getParseableDescription();

    public Map<String, Object> prepareModelData();
    
    public boolean isMaxResultsReached();
    
    String getQueryName();
    
}
