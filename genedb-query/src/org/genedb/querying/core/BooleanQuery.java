package org.genedb.querying.core;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class BooleanQuery implements Query {

    private BooleanQueryMode mode;
    private Query left;
    private Query right;

    public BooleanQuery(BooleanQueryMode mode, Query left, Query right) {
        this.mode = mode;
        this.left = left;
        this.right = right;
    }

    @SuppressWarnings("unchecked")
    public List<String> getResults() throws QueryException {
        List<String> results = new ArrayList(left.getResults());
        List<String> rightResults = new ArrayList(right.getResults());
        switch (mode) {
        case INTERSECT:
            results.retainAll(rightResults);
            return results;
        case SUBTRACT:
            results.removeAll(rightResults);
            return results;
        case UNION:
            results.addAll(rightResults);
            return results;
        }
        return null;
    }

    public String getParseableDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        sb.append(left.getParseableDescription());
        sb.append(' ');
        sb.append(mode);
        sb.append(' ');
        sb.append(right.getParseableDescription());
        sb.append(')');
        return sb.toString();
    }

    public Map<String, Object> prepareModelData() {
        return Collections.emptyMap();
    }

    public int getOrder() {
        // Not used for boolean queries
        return 0;
    }
}
