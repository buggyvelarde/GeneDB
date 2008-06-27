package org.genedb.medusa;

import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.sequence.Feature;

public interface FeatureService {

	Feature findFeature(String systematicId);

	CvTerm findConventionalFeatureForProperty(CvTerm cvTerm);

	Feature findGenePart(String systematicId, CvTerm featureType);

}
