package org.gmod.schema.sequence;

import java.util.Collection;
import java.util.HashSet;

public class Protein extends Feature {
	
	public Collection<Feature> gene;

	public Collection<Feature> getGene() {
		Collection<Feature> gene = new HashSet<Feature>();
		Collection<FeatureRelationship> objects = this.getFeatureRelationshipsForSubjectId();
		for (FeatureRelationship relationship : objects) {
			Feature mrna =  relationship.getFeatureByObjectId();
			Collection<FeatureRelationship> temps = mrna.getFeatureRelationshipsForSubjectId();
			for (FeatureRelationship relation : temps) {
				Feature tmp = relation.getFeatureByObjectId();
				if (tmp.getCvTerm().getCvTermId() == 792)
					gene.add(tmp);
			}
		}
		return gene;
	}

	public void setGene(Collection<Feature> gene) {
		this.gene = gene;
	}
}
