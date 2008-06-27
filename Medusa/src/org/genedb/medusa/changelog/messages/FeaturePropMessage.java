package org.genedb.medusa.changelog.messages;

import org.genedb.db.loading.RankableUtils;
import org.genedb.medusa.CvService;
import org.genedb.medusa.FeatureService;
import org.genedb.medusa.changelog.ActionVerb;

import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureProp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FeaturePropMessage extends ChangeLogMessage {
	
	private String newValue;
	private String oldValue;
	protected String propertyName;
	private ActionVerb av;
	protected String systematicId;
	protected FeatureService featureService;
	protected CvService cvService;
	
	
	public void accept() {
		Feature f = featureService.findFeature(systematicId);
		String[] cvParts = propertyName.split("::");
		CvTerm cvTerm = cvService.findCvTermByCvAndName(cvParts[0], cvParts[1]);
		modify(f, cvTerm);
	}
	
	
	/**
	 * Modify a feature property on a feature. The type of modification 
	 * depends upon the ActionVerb 
	 * 
	 * @param feature the feature to apply the change to
	 * @param property the type of property eg name/colour etc to change
	 */
	protected void modify(Feature feature, CvTerm property) {
		Collection<FeatureProp> fps = feature.getFeatureProps();
		List<FeatureProp> filtered = filterFeaturePropsByType(property, fps);

		if (av == ActionVerb.REMOVE || av == ActionVerb.REPLACE) {
			FeatureProp old = searchForGivenFeaturePropValue(filtered, oldValue);
			if (old == null) {
				throw new RuntimeException("can't remove/replace as not there");
			}
			if (av == ActionVerb.REPLACE) {
				old.setValue(newValue);
			} else {
				fps.remove(old);		
				filtered.remove(old);
			}
		}
			
		if (av == ActionVerb.ADD) {
			FeatureProp newFeatureProp = searchForGivenFeaturePropValue(filtered, newValue);
			if (newFeatureProp != null) {
				throw new RuntimeException("Already exists");
			}
			int rank = RankableUtils.getNextRank(filtered);
			FeatureProp fp = new FeatureProp(feature, property, newValue, rank);
			feature.addFeatureProp(fp);
		}
	}


	/**
	 * Search through a list of FeatureProps for one with a given value. It's typically most useful 
	 * if the list has already been filtered for the type
	 * 
	 * @param fps List of FeatureProps
	 * @param value the value to search for
	 * @return the (last) matching FeatureProp, or null if no matches 
	 */
	private FeatureProp searchForGivenFeaturePropValue(
			List<FeatureProp> fps, String value) {
		FeatureProp fp = null;
		for (FeatureProp featureProp : fps) {
			if (featureProp.getValue().equals(value)) {
				fp = featureProp;
			}
		}

		return fp;
	}


	/**
	 * Filter a list of FeatureProp by their type, returning a new list with any matches
	 * 
	 * @param property the type of the FeatureKey to match on
	 * @param fps the list of FeatureProperties to filter
	 * @return a possibly empty list of matches
	 */
	private List<FeatureProp> filterFeaturePropsByType(CvTerm property,
			Collection<FeatureProp> fps) {
		List<FeatureProp> filtered = new ArrayList<FeatureProp>();
		for (FeatureProp featureProp : fps) {
			if (featureProp.cvTerm.equals(property)) {
				filtered.add(featureProp);
			}
		}
		return filtered;
	}
	
}		
		
