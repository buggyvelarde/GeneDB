package org.genedb.web.gui.filters;

import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.StrandedFeature;

public class RNAFilter implements FeatureFilter {

    private char strand;
    private final String MISC_RNA = "misc_RNA";
    private final String SN_RNA = "snRNA";
    private final String SNO_RNA = "snoRNA";
    private final String T_RNA = "tRNA";
    private final String R_RNA = "rRNA";
    private final String S_RNA = "sRNA";

    private final String[] ALL_RNA = {MISC_RNA, S_RNA, SN_RNA, SNO_RNA, T_RNA, R_RNA};


    public RNAFilter(char strand) {
        this.strand = strand;
    }

    public boolean accept(Feature f) {
        if ( !(f instanceof StrandedFeature) ) {
            return false;
        }
        StrandedFeature g = (StrandedFeature) f;
        if ( strand != g.getStrand().getToken()) {
            return false;
        }
        for (int i=0; i < ALL_RNA.length ; i++) {
            if ( ALL_RNA[i].equals(g.getType())) {
                return true;
            }
        }
        return false;
    }
}
