package org.genedb.medusa.changelog;

import org.genedb.medusa.changelog.messages.ChangeLogMessage;

public class ChangeLogProcessor {
	
	public void processCommand(String message) {

	
	}	
	
	public void processChangeLogMessage(ChangeLogMessage clm) {
		
		fillInTaxonId(clm);
		
		validateStoreAndProcess(clm);
		
		
	}

	private void validateStoreAndProcess(ChangeLogMessage clm) {
//		Processor p = findProcessor(clm);
//		p.process(clm);
		store(clm);
	}

	private void store(ChangeLogMessage clm) {
		// TODO Auto-generated method stub
		
	}

	private void fillInTaxonId(ChangeLogMessage clm) {
		// TODO Auto-generated method stub
		
	}
	
	

}
