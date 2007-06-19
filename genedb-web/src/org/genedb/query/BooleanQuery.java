package org.genedb.query;

import java.util.ArrayList;
import java.util.List;

public class BooleanQuery implements Query {
	
	private BooleanQueryMode mode;
	private Query left;
	private Query right;

	public BooleanQuery(BooleanQueryMode mode, Query left, Query right) {
		this.mode = mode;
		this.left = left;
		this.right = right;
	}

	public Object getResults() {
		List<String> results = new ArrayList(left.getResults());
		List<String> right = new ArrayList(right.getResults());
		switch (mode) {
		case INTERSECT:
			results.retainAll(right);
			return results;
		case SUBTRACT:
			results.removeAll(right);
			return results;
		case UNION:
			results.addAll(right);
			return results;
		}
		return null;
	}

}
