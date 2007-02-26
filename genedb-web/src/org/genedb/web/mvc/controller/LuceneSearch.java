package org.genedb.web.mvc.controller;

import org.apache.lucene.search.Query;

public class LuceneSearch {
	
	private String query;
	
	private String field;

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
