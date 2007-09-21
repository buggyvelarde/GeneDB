package org.genedb.db.loading;

import java.io.File;

public interface OrthologueStorer {

	void afterPropertiesSet();

	void process(File input);

	void writeToDb();
	
	
	
}
