package org.genedb.web.mvc.controller;

import java.util.List;

import org.genedb.db.jpa.Feature;

public class ResultBean {

	private List<Feature> results;

	public List<Feature> getResults() {
		return results;
	}

	public void setResults(List<Feature> results) {
		this.results = results;
	}
}
