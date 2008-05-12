package org.gmod.schema.sequence;

import java.util.Collection;
import java.util.HashSet;

import javax.persistence.Transient;

import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

public class Protein extends Feature {
	

	public Gene gene;

	public Gene getGene() {
		Collection<Feature> gene = new HashSet<Feature>();
		Collection<FeatureRelationship> objects = this.getFeatureRelationshipsForSubjectId();
		for (FeatureRelationship relationship : objects) {
			Feature mrna =  relationship.getFeatureByObjectId();
			Collection<FeatureRelationship> temps = mrna.getFeatureRelationshipsForSubjectId();
			for (FeatureRelationship relation : temps) {
				Feature tmp = relation.getFeatureByObjectId();
				if (tmp.getCvTerm().getCvTermId() == 792)
					return (Gene)tmp;
			}
		}
		return null;
	}

	public void setGene(Gene gene) {
		this.gene = gene;
	}
}
