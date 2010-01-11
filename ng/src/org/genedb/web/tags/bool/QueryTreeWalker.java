package org.genedb.web.tags.bool;

import org.genedb.query.NumberedQueryI;
import org.genedb.query.bool.BooleanQuery;

public class QueryTreeWalker {

    private int index;
    private NumberedQueryI query;



    public QueryTreeWalker(NumberedQueryI query, int index) {
        this.query = query;
        this.index = index;
    }

    public void go() {
        setNode(query);
    }

    private void setNode(NumberedQueryI node) {
        node.setIndex(index);
        index++;
        if (node instanceof BooleanQuery) {
            BooleanQuery bool = (BooleanQuery) node;
            setNode(bool.getFirstQuery());
            setNode(bool.getSecondQuery());
        }
    }


}
