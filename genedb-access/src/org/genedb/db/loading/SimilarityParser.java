package org.genedb.db.loading;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biojava.bio.Annotation;

public class SimilarityParser {
	
	protected static final Log logger = LogFactory.getLog(SimilarityParser.class);
	
	public List<SimilarityInstance> getAllSimilarityInstance(Annotation an){
		List<SimilarityInstance> ret = new ArrayList<SimilarityInstance>();
		
		List<String> similarities = MiningUtils.getProperties("similarity", an);
		
		if (similarities != null) {
			for (String similarity : similarities) {
				SimilarityInstance si = new SimilarityInstance();
				
				String sections[] = similarity.split(";");
				int count = 0;
				for (String value : sections) {
					count++;
					
					switch (count) {
					case 1:
						if (value.length() > 1){
							si.setAlgorithm(value);
						}
						break;
					case 2:
						if (value.length() > 1){
							String temp = value.replaceAll("^\\s+", "");
							temp = temp.replaceAll("\\s+$", "");
							String values[] = temp.split(" ");
							si.setPriDatabase(values[0]);
							
							temp = values[1].replaceAll("^\\W", "");
							temp = temp.replaceAll("\\W$", "");
							si.setSecDatabase(temp);
						}
						break;
					case 3:
						if (value.length() > 1){
							String temp = value.replaceAll("^\\s+", "");
							temp = temp.replaceAll("\\s+$", "");
							si.setOrganism(temp);
						}
						break;
					case 4:
						if (value.length() > 1){
							String temp = value.replaceAll("^\\s+", "");
							temp = temp.replaceAll("\\s+$", "");
							si.setProduct(temp);
						}
						break;
					case 5:
						if (value.length() > 1){
							String temp = value.replaceAll("^\\s+", "");
							temp = temp.replaceAll("\\s+$", "");
							si.setGene(temp);
						}
						break;
					case 6:
						if (value.length() > 1){
							String temp = value.replaceAll("^\\s+", "");
							String values[] = temp.split(" ");
							if(values.length != 3){
								count--;
							} else {
								si.setLength(values[1]);
							}
						}
						break;
					case 7:
						if (value.length() > 1){
							if (value.contains("=")){
								String values[] = value.split("=");
								si.setId(values[1].replaceAll("\\W$", ""));
							}
						}
						break;
					case 8:
						if (value.length() > 1){
							if (value.contains("=")){
								String values[] = value.split("=");
								si.setUngappedId(values[1].replaceAll("\\W$", ""));
							}
						}
						break;
					case 9:
						if (value.length() > 1){
							if (value.contains("=")){
								String values[] = value.split("=");
								si.setEvalue(values[1].replaceAll("\\W$", ""));
							}
						}
						break;
					case 10:
						if (value.length() > 1){
							if (value.contains("=")){
								String values[] = value.split("=");
								si.setScore(values[1]);
							}
						}
						break;
					case 11:
						if (value.length() > 1){
							si.setOverlap(value);
						}
						break;
					case 12:
						if (value.length() > 1){
							String temp = value.replaceAll("^\\s+", "");
							String values[] = temp.split(" ");
							if(values[1].equals("aa")){
								count++;
							}
							si.setQuery(values[1]);
						} else {
							count--;
						}
						break;
					case 13:
						if (value.length() > 1){
							String temp = value.replaceAll("^\\s+", "");
							String values[] = temp.split(" ");
							if(values[1].equals("aa")){
								count++;
							}
							si.setSubject(values[1]);
							
						} else {
							count--;
						}
						break;
					}
				}
				if (count != 13) {
					logger.error("THIS QUALIFIER IS NOT CURATED PROPERLY: " + an.getProperty("systematic_id"));
					System.out.println("THIS QUALIFIER IS NOT CURATED PROPERLY: " + an.getProperty("systematic_id"));
				} else {
					ret.add(si);
				}
				
			}
		} 
		return ret;
	}
}
