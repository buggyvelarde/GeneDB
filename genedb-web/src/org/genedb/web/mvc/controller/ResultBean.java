package org.genedb.web.mvc.controller;

import java.util.List;

import org.genedb.db.jpa.Feature;

public class ResultBean {

	private List<String> results;
	private String result;

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public List<String> getResults() {
		return results;
	}

	public void setResults(List<String> results) {
		this.results = results;
	}
}
