package org.gmod.schema.utils;

import org.biojava.bio.seq.StrandedFeature.Strand;

public class LocationUtils {
    
    public static StrandedLocation make(org.biojava.bio.symbol.Location loc, Strand strand) {
        StrandedLocation ret;
        if (!loc.isContiguous()) {
            ret = new CompoundLocation(loc, strand);
        } else {
            ret = new SingleLocation(loc, strand);
        }
        return ret;
    }

}
