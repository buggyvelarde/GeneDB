package org.gmod.schema.sequence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.Indexed;

@SuppressWarnings("serial")
@Indexed
public class Gene extends Feature {
	
	@SuppressWarnings("unused")
	@ContainedIn
	private Collection<Feature> protein;

	public Collection<Feature> getProtein() {
		Collection<Feature> protein = new HashSet<Feature>();
		Collection<FeatureRelationship> objects = this.getFeatureRelationshipsForObjectId();
		for (FeatureRelationship relationship : objects) {
			Feature mrna =  relationship.getFeatureBySubjectId();
			Collection<FeatureRelationship> temps = mrna.getFeatureRelationshipsForObjectId();
			for (FeatureRelationship relation : temps) {
				Feature tmp = relation.getFeatureBySubjectId();
				if (tmp.getCvTerm().getCvTermId() == 191)
					protein.add(tmp);
			}
		}
		return protein;
	}
	
	public void setProtein(Collection<Feature> protein) {
		this.protein = protein;
	}
}
