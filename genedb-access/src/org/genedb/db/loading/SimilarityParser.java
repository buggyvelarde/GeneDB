package org.genedb.db.loading;

import java.util.ArrayList;
import java.util.List;

import org.biojava.bio.Annotation;
import org.gmod.schema.analysis.Analysis;

public class SimilarityParser {

	public List<SimilarityInstance> getAllSimilarityInstance(Annotation an){
		List<SimilarityInstance> ret = new ArrayList<SimilarityInstance>();
		
		List<String> similarities = MiningUtils.getProperties("similarity", an);
		
		for (String similarity : similarities) {
			SimilarityInstance si = new SimilarityInstance();
			
			String sections[] = similarity.split(";");
			if (sections[0] != ""){
				si.setAlgorithm(sections[0]);
			}
			
			if (sections[1] != ""){
				String values[] = sections[1].split(" ");
				si.setPriDatabase(values[0]);
				
				values[1].replaceFirst("(", "");
				values[1].replaceFirst(")", "");
				si.setSecDatabase(values[1]);
			}
			
			if (sections[2] != ""){
				si.setOrganism(sections[2]);
			}
			
			if (sections[3] != ""){
				si.setProduct(sections[3]);
			}
			
			if (sections[4] != ""){
				si.setGene(sections[4]);
			}
			
			if (sections[5] != ""){
				String values[] = sections[5].split(" ");
				si.setLength(values[1]);
			}
			
			if (sections[6] != ""){
				String values[] = sections[6].split("=");
				si.setId(values[1]);
			}
			
			if (sections[7] != ""){
				String values[] = sections[7].split("=");
				si.setUngappedId(values[1]);
			}
			
			if (sections[8] != ""){
				String values[] = sections[8].split("=");
				si.setEvalue(values[1]);
			}
			
			if (sections[9] != ""){
				String values[] = sections[9].split("=");
				si.setScore(values[1]);
			}
			
			if (sections[10] != ""){
				si.setOverlap(sections[10]);
			}
			
			if (sections[11] != ""){
				String values[] = sections[11].split(" ");
				si.setQuery(values[1]);
			}
			
			if (sections[12] != ""){
				String values[] = sections[12].split(" ");
				si.setSubject(values[1]);
				
			}
			ret.add(si);
		}
		return ret;
	}
}
