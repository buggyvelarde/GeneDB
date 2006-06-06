package org.genedb.db.loading;

public class BasePart implements Part {

    private int size;
    private int offSet;
    
    public int getOffSet() {
        return this.offSet;
    }
    public void setOffSet(int offSet) {
        this.offSet = offSet;
    }
    public int getSize() {
        return this.size;
    }
    public void setSize(int size) {
        this.size = size;
    }
    
    

}
