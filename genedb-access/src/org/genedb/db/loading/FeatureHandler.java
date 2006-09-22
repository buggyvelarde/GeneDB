package org.genedb.db.loading;

import org.gmod.schema.sequence.Feature;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojava.utils.ChangeVetoException;

import java.io.File;
import java.util.Map;

public interface FeatureHandler extends FeatureProcessor {
    
    public abstract Feature process(File file, Sequence seq)
	    throws ChangeVetoException, BioException;
    
    public abstract void setOptions(Map<String, String> options);

}