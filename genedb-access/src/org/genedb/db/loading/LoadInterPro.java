package org.genedb.db.loading;

import java.io.File;
import java.util.Date;
import java.util.Properties;

import org.genedb.db.dao.SequenceDao;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.HibernateTransactionManager;

public class LoadInterPro {

	
	private static SequenceDao sequenceDao;
	
	private static HibernateTransactionManager hibernateTransactionManager;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		

        if (args.length == 0) {
        	System.err.println("No input files specified");
        	System.exit(-1);
        }
		Session session = hibernateTransactionManager.getSessionFactory().openSession();
        Properties overrideProps = new Properties();
        PropertyOverrideHolder.setProperties("dataSourceMunging", overrideProps);
        
        ApplicationContext ctx = new ClassPathXmlApplicationContext(
                new String[] {"NewRunner.xml"});

        InterProParser runner = (InterProParser) ctx.getBean("ipparser", InterProParser.class);
        
        runner.afterPropertiesSet();
        
        String[] filePaths = args;
		
		long start = new Date().getTime();

        for (int i = 0; i < filePaths.length; i++) {
        	Transaction transaction = session.beginTransaction();
            runner.Parse(sequenceDao,filePaths[i]);
            transaction.commit();
		}
        session.close();
        long stop = new Date().getTime();
        
        System.err.println("Total time taken - " + (stop - start)/60000 + " min" );
        
    }
	

	public void afterPropertiesSet() {

		
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
