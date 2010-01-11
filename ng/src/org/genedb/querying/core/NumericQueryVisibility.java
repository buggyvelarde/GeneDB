package org.genedb.querying.core;

/**
 * A rank of visibilities for each query, with the most visible being at the bottom (i.e., has a higher index).
 *
 * @author gv1
 *
 */
public enum NumericQueryVisibility implements QueryVisibility {

    PRIVATE(0), PUBLIC(1);

    Integer index;

    NumericQueryVisibility(int index) {
        this.index = index;
    }

    /* (non-Javadoc)
     * @see org.genedb.querying.core.QueryVisibility#includesVisibility(org.genedb.querying.core.NumericQueryVisibility)
     */
    public boolean includesVisibility(QueryVisibility v) {
        NumericQueryVisibility nqv;
        if (v instanceof QueryVisibility) {
            nqv = (NumericQueryVisibility) v;
        } else {
            throw new IllegalArgumentException(String.format("Attempt to compare NumericQueryVisibility with '%s'", v.getClass()));
        }
        return this.index.compareTo(nqv.index) >= 0;
    }
}

