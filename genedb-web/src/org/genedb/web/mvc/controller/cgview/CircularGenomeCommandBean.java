package org.genedb.web.mvc.controller.cgview;  


public class CircularGenomeCommandBean {
    
    private String enzymeName;
    
    private String taxon;
    
    private String uniqueName;

    public String getEnzymeName() {
        return this.enzymeName;
    }

    public void setEnzymeName(String enzymeName) {
        this.enzymeName = enzymeName;
    }

    public String getTaxon() {
        return this.taxon;
    }

    public void setTaxon(String taxon) {
        this.taxon = taxon;
    }

    public String getUniqueName() {
        return this.uniqueName;
    }

    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }
    
    
}