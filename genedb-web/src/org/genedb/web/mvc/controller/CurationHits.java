package org.genedb.web.mvc.controller;

public class CurationHits {
	
	private String value;
	
	private String cvTerm;
	
	private String feature;
	
	private String organism;

	public String getCvTerm() {
		return cvTerm;
	}

	public void setCvTerm(String cvTerm) {
		this.cvTerm = cvTerm;
	}

	public String getFeature() {
		return feature;
	}

	public void setFeature(String feature) {
		this.feature = feature;
	}

	public String getOrganism() {
		return organism;
	}

	public void setOrganism(String organism) {
		this.organism = organism;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
