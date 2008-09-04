package org.genedb.medusa;

import org.gmod.schema.mapped.CvTerm;
import org.gmod.schema.mapped.Feature;

public interface FeatureService {

    Feature findFeature(String systematicId);

    CvTerm findConventionalFeatureForProperty(CvTerm cvTerm);

    Feature findGenePart(String systematicId, CvTerm featureType);

    String findTypeNameForSystematicId(String systematicId);

}
