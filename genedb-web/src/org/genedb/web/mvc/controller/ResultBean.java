package org.genedb.web.mvc.controller;

import java.util.List;

import org.gmod.schema.organism.Organism;

public class ResultBean {

	private List<Organism> results;
	private String result;

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public List<Organism> getResults() {
		return results;
	}

	public void setResults(List<Organism> results) {
		this.results = results;
	}
}
