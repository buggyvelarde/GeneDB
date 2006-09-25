package org.genedb.db.loading;

public class ExpectedLookupMissing extends RuntimeException {

	public ExpectedLookupMissing() {}

	public ExpectedLookupMissing(String message) {
		super(message);
	}

}
