package org.genedb.db.loading.featureProcessors;

import org.biojava.bio.seq.StrandedFeature;
import org.genedb.db.loading.ProcessingPhase;
import org.gmod.schema.sequence.Feature;

public class DummyFeatureProcessor extends BaseFeatureProcessor {

	@Override
	public void processStrandedFeature(Feature parent, StrandedFeature f,
			int offset) {
		// Deliberately empty - it is a dummy class...

	}

	public ProcessingPhase getProcessingPhase() {
		return ProcessingPhase.LAST;
	}

}
