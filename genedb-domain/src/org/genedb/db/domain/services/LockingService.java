package org.genedb.db.domain.services;

public interface LockingService {
	
	//LockStatus findLockStatus(String systematicId);
	
	LockStatus lockGene(String systematicId);
	
	void unlockGene(String systematicId);

}
