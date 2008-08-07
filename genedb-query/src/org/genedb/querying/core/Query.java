package org.genedb.querying.core;

import java.util.List;
import java.util.Map;

public interface Query {

	List<String> getResults() throws QueryException;

    String getParseableDescription();

	public Map<String, Object> prepareModelData();

}
