package org.genedb.querying.core;

import org.springframework.core.Ordered;
import org.springframework.validation.Validator;

import java.util.List;
import java.util.Map;

public interface Query extends Ordered, Validator {

    String getQueryDescription();

    List<String> getResults() throws QueryException;

    String getParseableDescription();

    public Map<String, Object> prepareModelData();

}
