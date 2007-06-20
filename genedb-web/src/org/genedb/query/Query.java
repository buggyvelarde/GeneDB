package org.genedb.query;

public interface Query {

	Object getResults() throws QueryException;
    
    String getParseableDescription();

}
