package org.genedb.querying.core;

import java.util.List;

public interface Query {

	List<String> getResults() throws QueryException;
    
    String getParseableDescription();

}
