package org.genedb.db.loading;

public class ParsingException extends RuntimeException {
    
    ParsingException() {
	super();
    }
    
    ParsingException(Exception exp) {
	super(exp);
    }

}
