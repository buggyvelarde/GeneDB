package org.genedb.db.loading;

public class FilePart extends BasePart implements Part {

    private String name;
    private boolean reparent;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isReparent() {
        return this.reparent;
    }

    public void setReparent(boolean reparent) {
        this.reparent = reparent;
    }

}
