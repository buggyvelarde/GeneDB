package org.genedb.pipeline.domain;

public enum GoCategory {
	MOLECULAR_FUNCTION('F'), CELLULAR_COMPONENT('C'), BIOLOGICAL_PROCESS('P');
	
	private GoCategory(char abbreviation) {
		this.abbreviation = abbreviation;
	}
	
	private char abbreviation;
	
	
	public char getAbbreviation() {
		return abbreviation;
	}
}
