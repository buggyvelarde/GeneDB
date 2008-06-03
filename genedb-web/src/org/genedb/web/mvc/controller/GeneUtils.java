package org.genedb.web.mvc.controller;

import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureRelationship;
import org.gmod.schema.sequence.feature.MRNA;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class GeneUtils {

	private static String[] geneNameParts = {"gene", "mRNA", "polypeptide", "intron", "exon"};
	private static Set<String> geneParts = new HashSet<String>();
	
	static {
		geneParts.addAll(Arrays.asList(geneNameParts));
	}
	
	// FIXME - This is just it's possible, not certain
	public static boolean isPartOfGene(Feature feature) {
		if (geneParts.contains(feature.getCvTerm().getName())) {
			//System.err.println("Returning true for "+feature.getUniqueName());
			return true;
		}
		//System.err.println("Returning false for "+feature.getUniqueName());
		return false;
	}

	public static Feature getGeneFromPart(Feature feature) {
		if ("polypeptide".equals(feature.getCvTerm().getName())) {
			//System.err.println("Looking for gene for "+feature.getUniqueName());
			Feature gene = null;
			Feature mRNA = null;
			Collection<FeatureRelationship> frs = feature.getFeatureRelationshipsForSubjectId();
			for (FeatureRelationship relationship : frs) {
				//System.err.println("FeatureRealtionship for mRNA is "+relationship.getFeatureByObjectId().getUniqueName());
				if(relationship.getCvTerm().getName().equals("derives_from")){
				    mRNA = relationship.getFeatureByObjectId();
				}
			}
			if (mRNA != null) {
				frs = mRNA.getFeatureRelationshipsForSubjectId();
				for (FeatureRelationship relationship : frs) {
					if(relationship.getCvTerm().getName().equals("part_of")) {
					    gene = relationship.getFeatureByObjectId();
					}
				    //Feature feat = relationship.getFeatureByObjectId();
					
				}
			}
			return gene;
		}
		return feature;
	}

	

}
