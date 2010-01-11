package org.genedb.querying.core;

public interface QueryVisibility {

    public abstract boolean includesVisibility(QueryVisibility qv);

}
