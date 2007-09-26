package org.genedb.db.loading;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;


public interface Goa2GeneDBI {

	void afterPropertiesSet();

	void process(File[] files);

	@Transactional
	void writeToDb();
	
	
	
}
