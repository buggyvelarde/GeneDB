package org.genedb.web.gui.filters;

import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.StrandedFeature;


/**
 *
 *
 * @author <a href="mailto:art@sanger.ac.uk">Adrian Tivey</a>
*/
public class ComboFeatureFilter implements FeatureFilter {

    private char strand;
    private int status;


    public ComboFeatureFilter(char strand, int status) {
        this.strand = strand;
        this.status = status;
    }

    public boolean accept(Feature f) {
        StrandedFeature g = (StrandedFeature) f;
        if ( strand != g.getStrand().getToken()) {
              return false;
        }
        int s = Integer.parseInt(g.getAnnotation().getProperty("colour").toString());
        if ( s== status) {
            return true;
        }
        return false;
    }

}