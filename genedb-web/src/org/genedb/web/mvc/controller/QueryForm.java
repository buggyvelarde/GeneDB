package org.genedb.web.mvc.controller;

import org.genedb.query.NumberedQueryI;

/**
 * <code>MultiActionController</code> that handles all non-form URL's.
 *
 * @author Ken Krebs
 */
public class QueryForm {
    private boolean advanced = true;
    private NumberedQueryI nq;
    
    public boolean isAdvanced() {
        return advanced;
    }
    public void setAdvanced(boolean advanced) {
        this.advanced = advanced;
    }
    public NumberedQueryI getNumberedQueryI() {
        return nq;
    }
    public void setNumberedQuery(NumberedQueryI nq) {
        this.nq = nq;
    }
}