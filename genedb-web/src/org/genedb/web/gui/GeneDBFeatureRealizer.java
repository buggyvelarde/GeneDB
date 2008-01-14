package org.genedb.web.gui;

import org.biojava.bio.BioError;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.FeatureRealizer;
import org.biojava.bio.seq.SimpleFeatureRealizer;
import org.biojava.bio.seq.impl.FeatureImpl;

import java.io.Serializable;
//import org.biojava.bio.seq.io.*;
//import org.biojava.bio.symbol.*;

public class GeneDBFeatureRealizer  implements Serializable {

    private static SimpleFeatureRealizer fr;

    private GeneDBFeatureRealizer() {
        // Deliberately empty - constructor private
    }

    public static FeatureRealizer getInstance() {
        if ( fr == null) {
            fr = new SimpleFeatureRealizer(FeatureImpl.DEFAULT);
        }
        return fr;
    }

}
