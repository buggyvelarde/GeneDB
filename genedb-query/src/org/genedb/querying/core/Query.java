package org.genedb.querying.core;

import org.springframework.core.Ordered;
import org.springframework.validation.Validator;

import java.util.List;
import java.util.Map;

public interface Query<T> extends Ordered, Validator {

    String getQueryDescription();

    List<T> getResults() throws QueryException;

    String getParseableDescription();

    public Map<String, Object> prepareModelData();

}
