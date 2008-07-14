package org.genedb.db.loading.featureProcessors;

import org.biojava.bio.seq.StrandedFeature;
import org.genedb.db.loading.FeatureProcessor;
import org.genedb.db.loading.ProcessingPhase;

import org.gmod.schema.mapped.Feature;

public class Gap_Processor extends BaseFeatureProcessor implements FeatureProcessor {

	@Override
	public void processStrandedFeature(Feature parent, StrandedFeature f, int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
    public ProcessingPhase getProcessingPhase() {
		// TODO Auto-generated method stub
		return null;
	}

}
