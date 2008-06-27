package org.genedb.medusa.changelog.messages;

import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.sequence.Feature;

/**
 * Represents changes to a FeatureProp, understanding the 
 * conventional location for the property on a gene hierarchy 
 * 
 * @author art
 */
public class GenePropMsg extends FeaturePropMessage {

	@Override
	public void accept() {
		String[] cvParts = propertyName.split("::");
		CvTerm cvTerm = cvService.findCvTermByCvAndName(cvParts[0], cvParts[1]);
		CvTerm featureType = featureService.findConventionalFeatureForProperty(cvTerm);
		Feature f = featureService.findGenePart(systematicId, featureType);

		modify(f, cvTerm);
	}
	
	
}
