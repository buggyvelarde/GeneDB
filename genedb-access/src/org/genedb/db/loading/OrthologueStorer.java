package org.genedb.db.loading;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;


public interface OrthologueStorer {

	void afterPropertiesSet();

	void process(File[] files);

	@Transactional
	void writeToDb();
	
	
	
}
