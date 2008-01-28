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
		

        if (args.length == 0) {
        	System.err.println("No input files specified");
        	System.exit(-1);
        }

        ApplicationContext ctx = new ClassPathXmlApplicationContext(
                new String[] {"NewRunner.xml"});

        LoadInterPro runner = (LoadInterPro) ctx.getBean("iploader", LoadInterPro.class);
        runner.afterPropertiesSet();
        runner.process(args);

        
    }
	

	private void afterPropertiesSet() {

		
	}


	private void process(String[] args) {
		String[] filePaths = args;
		
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
