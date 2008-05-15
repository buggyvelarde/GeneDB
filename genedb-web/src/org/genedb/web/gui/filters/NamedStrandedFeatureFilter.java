package org.genedb.web.gui.filters;

import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.StrandedFeature;


/**
 *
 *
 * @author <a href="mailto:art@sanger.ac.uk">Adrian Tivey</a>
*/
public class NamedStrandedFeatureFilter implements FeatureFilter {

    private String type;
    private String id;
    private char strand;


    public NamedStrandedFeatureFilter(String type, char strand) {
        this.type = type;
        this.strand = strand;
    }

    public boolean accept(Feature f) {
        if ( !(f instanceof StrandedFeature) ) {
            return false;
        }
        if ( !type.equals(f.getType())) {
            return false;
        }
        StrandedFeature rna = (StrandedFeature) f;
        if ( strand != rna.getStrand().getToken()) {
            return false;
        }
        if (rna.getAnnotation().getProperty("systematic_id").equals(id)) {
            return true;
        }
        return false;
    }

    public void setId(String id) {
        this.id = id;
    }

}
