package org.genedb.query.core;

import java.util.List;

public interface Query {

	List<String> getResults() throws QueryException;
    
    String getParseableDescription();

}
