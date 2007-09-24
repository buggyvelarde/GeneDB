package org.genedb.web.mvc.controller;


public class LuceneSearch {
	
	private String query;
	
	private String field;
	
	private boolean history = false;

	public boolean isHistory() {
		return history;
	}

	public void setHistory(boolean history) {
		this.history = history;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

}
