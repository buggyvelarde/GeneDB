package org.genedb.web.mvc.controller;

public class GoLookup {
    private String lookup; // The name to lookup, using * for wildcards
    private int start = 0; // Which number to start at if paging
    private int pageSize; // How many results per page, if paging


    public String getLookup() {
        return this.lookup;
    }
    
    public void setLookup(String lookup) {
        this.lookup = lookup;
    }
   
    public int getPageSize() {
        return this.pageSize;
    }
    
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
    
    public int getStart() {
        return this.start;
    }
    
    public void setStart(int start) {
        this.start = start;
    }
    


}
