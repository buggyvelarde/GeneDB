package org.genedb.query;

import java.util.List;

public interface Query {

	List<String> getResults() throws QueryException;
    
    String getParseableDescription();

}
