package org.genedb.db.loading;

import java.io.File;
import java.util.Date;
import java.util.Properties;

import org.genedb.db.dao.SequenceDao;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.HibernateTransactionManager;

public class LoadInterPro {

	
	private static SequenceDao sequenceDao;
	
	private HibernateTransactionManager hibernateTransactionManager;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		

        if (args.length == 0) {
        	System.err.println("No input files specified");
        	System.exit(-1);
        }
        
        Properties overrideProps = new Properties();
        PropertyOverrideHolder.setProperties("dataSourceMunging", overrideProps);
        
        ApplicationContext ctx = new ClassPathXmlApplicationContext(
                new String[] {"NewRunner.xml"});

        InterProParser runner = (InterProParser) ctx.getBean("ipparser", InterProParser.class);
        
        runner.afterPropertiesSet();
        
        String[] filePaths = args;
		
		long start = new Date().getTime();
        for (int i = 0; i < filePaths.length; i++) {
            runner.Parse(sequenceDao,filePaths[i]);
		}
        
        long stop = new Date().getTime();
        
        System.err.println("Total time taken - " + (stop - start)/60000 + " min" );
        
    }
	

	private void process(String[] args) {
		
	}


	public SequenceDao getSequenceDao() {
		return sequenceDao;
	}

	public void setSequenceDao(SequenceDao sequenceDao) {
		this.sequenceDao = sequenceDao;
	}


	public HibernateTransactionManager getHibernateTransactionManager() {
		return hibernateTransactionManager;
	}


	public void setHibernateTransactionManager(
			HibernateTransactionManager hibernateTransactionManager) {
		this.hibernateTransactionManager = hibernateTransactionManager;
	}
}
