package org.genedb.query.bool;

import org.genedb.query.BasicQueryI;
import org.genedb.query.Detailer;
import org.genedb.query.QueryI;
import org.genedb.query.Result;

public class BooleanQuery extends BooleanQueryNode {

    
    private BooleanOp op;
    private int index;
    private BooleanQueryNode query1;
    private BooleanQueryNode query2;
    
    
    
    public BooleanQuery(BooleanOp op, BasicQueryI query1, BasicQueryI query2) {
        super(null);
        this.op = op;
        this.query1 = new BooleanQueryNode(query1);
        this.query2 = new BooleanQueryNode(query2);
    }

    public BooleanOp getOp() {
        return op;
    }
    
    public void setOp(BooleanOp op) {
        this.op = op;
    }
    
    public BooleanQueryNode getFirstQuery() {
        return query1;
    }
    
    public void setFirstQuery(BooleanQueryNode query1) {
        this.query1 = query1;
    }
    
    public BooleanQueryNode getSecondQuery() {
        return query2;
    }
    
    public void setSecondQuery(BooleanQueryNode query2) {
        this.query2 = query2;
    }

    @Override
    public String getSimpleDescription() {
        // TODO Auto-generated method stub
        return "( " + query1.getSimpleDescription() + "  " + op + "  " + query2.getSimpleDescription() + " )";
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public boolean isComplete() {
        return getFirstQuery().isComplete() && getSecondQuery().isComplete();
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getQueryAsString() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getResultType() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isStoredInHistory() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Result process() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setFineDetailer(Detailer detailer) {
        // TODO Auto-generated method stub
        
    }

    public void setSummaryDetailer(Detailer detailer) {
        // TODO Auto-generated method stub
        
    }
    
    
    
    
}
