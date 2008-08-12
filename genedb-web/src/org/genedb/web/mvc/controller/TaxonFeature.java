package org.genedb.web.mvc.controller;

import java.util.List;

public class TaxonFeature {
    private String getHomePageViewName;
    private List<FastaFile> fastaFiles;
    private boolean motifSearchEnabled;
    private String introText;
    private static final TaxonFeature BASIC_INSTANCE = new TaxonFeature(); // TODO configure
    
    public static TaxonFeature getBasicTaxonFeature() {
        return BASIC_INSTANCE;
    }
    
    
}
