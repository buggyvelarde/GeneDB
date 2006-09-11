package org.genedb.db.loading;

import org.gmod.schema.sequence.Feature;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojava.utils.ChangeVetoException;

public interface FeatureHandler extends FeatureProcessor {
    
    public abstract Feature processSources(Sequence seq)
	    throws ChangeVetoException, BioException;

}