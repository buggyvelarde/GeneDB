package org.genedb.web.mvc.controller;


public class Taxon {

    private TaxonFeature taxonFeature;
    private int taxonId;
    private int parentTaxonId;
    private boolean inDatabase;
    private boolean childInDatabase;
    private String origNickname;
    private String arcturusName;
    private String nickname; // TODO needed?
    private String fullName;
    private String displayName;
    
    
    public String getHomepageViewName() {
        return "withNews";
    }


    public TaxonFeature getTaxonFeature() {
        if (taxonFeature == null && isChildInDatabase()) {
            return TaxonFeature.getBasicTaxonFeature();
        }
        return taxonFeature;
    }


    public void setTaxonFeature(TaxonFeature feature) {
        this.taxonFeature = feature;
    }


    public boolean isChildInDatabase() {
        return childInDatabase;
    }


    public void setChildInDatabase(boolean childInDatabase) {
        this.childInDatabase = childInDatabase;
    }

}
