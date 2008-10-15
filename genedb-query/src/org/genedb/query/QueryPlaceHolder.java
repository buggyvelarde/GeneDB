package org.genedb.query;

public class QueryPlaceHolder implements NumberedQueryI {

    private String name;
    private int index;
    
    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSimpleDescription() {
    // TODO Auto-generated method stub
    return null;
    }

    public boolean isComplete() {
    // TODO Auto-generated method stub
    return false;
    }

    public Result process() {
    // TODO Auto-generated method stub
    return null;
    }


}
