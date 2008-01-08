package org.genedb.db.domain.serviceImpls;

import java.util.HashSet;
import java.util.Set;

import org.genedb.db.domain.services.LockStatus;
import org.genedb.db.domain.services.LockAndNotificationService;

public class LockAndNotificationServiceImpl implements LockAndNotificationService {

	private Set<String> locks = new HashSet<String>();
	
	public LockStatus lockGene(String systematicId) {
		if (!(locks.contains(systematicId))) {
			locks.add(systematicId);
			return new LockStatus(true);
		}
		return null;
	}

	public void unlockGene(String systematicId) {
		locks.remove(systematicId);
	}

}
