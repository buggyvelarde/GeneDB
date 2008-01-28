package org.genedb.db.loading;

import java.io.File;
import java.util.Date;
import java.util.Properties;

import org.genedb.db.dao.SequenceDao;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class LoadInterPro {

	
	private static SequenceDao sequenceDao;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String[] filePaths = args;

        if (filePaths.length == 0) {
        	System.err.println("No input files specified");
        	System.exit(-1);
        }

        ApplicationContext ctx = new ClassPathXmlApplicationContext(
                new String[] {"NewRunner.xml"});

        long start = new Date().getTime();
        for (int i = 0; i < filePaths.length; i++) {
			InterProParser ipp = new InterProParser(sequenceDao,filePaths[i]);
		}
        
        long stop = new Date().getTime();
        
        System.err.println("Total time taken - " + (stop - start)/60000 + " min" );
    }
	

	public SequenceDao getSequenceDao() {
		return sequenceDao;
	}

	public void setSequenceDao(SequenceDao sequenceDao) {
		this.sequenceDao = sequenceDao;
	}
}
