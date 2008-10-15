package org.genedb.query.bool;

import org.genedb.query.BasicQueryI;
import org.genedb.query.Detailer;
import org.genedb.query.NumberedQueryI;
import org.genedb.query.QueryI;
import org.genedb.query.Result;

public class BooleanQueryNode implements NumberedQueryI {

    private int index;
    private BasicQueryI query;
    
    public BooleanQueryNode(BasicQueryI query) {
        this.query = query;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getSimpleDescription() {
        return this.query.getSimpleDescription();
    }

    public boolean isComplete() {
        return this.query.isComplete();
    }

    public Result process() {
        return this.query.process();
    }

    public String getName() {
        return this.query.getName();
    }

}
