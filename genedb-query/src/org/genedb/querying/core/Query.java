package org.genedb.querying.core;

import org.springframework.core.Ordered;

import java.util.List;
import java.util.Map;

public interface Query extends Ordered {

    List<String> getResults() throws QueryException;

    String getParseableDescription();

    public Map<String, Object> prepareModelData();

}
