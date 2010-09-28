package org.genedb.querying.tmpquery;

import java.util.List;

public class GeneDetail extends GeneSummary {
	
	private List<String> synonyms;
	private String type;
	private String location;
	
	private String name;
	
	private int featureId;
	
	public GeneDetail(
			String systematicId, 
			String taxonDisplayName, 
			String product, 
			String topLevelFeatureName, 
			String location,
			List<String> synonyms,
			String type,
			String featureId, 
			String name
		) {
		
		
		setSystematicId(systematicId);
        setProduct(product);
		
		
		this.taxonDisplayName = taxonDisplayName;
		
		this.topLevelFeatureName = topLevelFeatureName;
		
		this.location = location;
		this.synonyms = synonyms;
		this.type = type;
		
		this.featureId = Integer.parseInt(featureId);
		
		this.name = name;
		
	}
	
	public String getLocation() {
		return location;
	}
	
	
	public List<String> getSynonyms() {
		return synonyms;
	}
	
	public String getType() {
		return type;
	}
		
	public int getFeatureId() {
		return featureId;
	}
	
	public String getPrimaryName() {
		return name;
	}
	
}
