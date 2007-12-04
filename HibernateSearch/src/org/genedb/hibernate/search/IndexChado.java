package org.genedb.hibernate.search;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureCvTerm;
import org.gmod.schema.sequence.FeatureProp;
import org.gmod.schema.sequence.FeatureRelationship;
import org.gmod.schema.sequence.Gene;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.hibernate.search.event.*;

public class IndexChado {
	
	private HibernateTransactionManager hibernateTransactionManager;
	
	public HibernateTransactionManager getHibernateTransactionManager() {
		return hibernateTransactionManager;
	}

	public void setHibernateTransactionManager(
			HibernateTransactionManager hibernateTransactionManager) {
		this.hibernateTransactionManager = hibernateTransactionManager;
	}

	public static void main(String[] args) {
        
        Configuration cfg = new Configuration();
        File f = new File("resources/");
        cfg.addDirectory(f);
        cfg.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
		cfg.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
		cfg.setProperty("hibernate.connection.username", "pathdb");
		cfg.setProperty("hibernate.connection.password", "Pyrate_1");
		cfg.setProperty("hibernate.connection.url", "jdbc:postgresql://pathdbsrv1a:10101/malaria_workshop");
		cfg.setProperty("hibernate.search.default.directory_provider", "org.hibernate.search.store.FSDirectoryProvider");
		//cfg.setProperty("hibernate.search.worker.batch_size", "1");
		cfg.setProperty("hibernate.search.default.indexBase", "/Users/cp2/hibernate/search/indexes");
		FullTextIndexEventListener ft = new FullTextIndexEventListener();
		cfg.setListener("post-insert", ft);
		cfg.setListener("post-update", ft);
		cfg.setListener("post-delete",ft);
        SessionFactory sf = cfg.buildSessionFactory();
        Session session = sf.openSession();
        FullTextSession fullTextSession = Search.createFullTextSession(session);
		Transaction tx = fullTextSession.beginTransaction();
		Query q = session.createQuery("from Gene g");
		q.setMaxResults(50);
		//System.err.println("query ran successfully...");
		
		List<Gene> features = q.list();
		//System.err.println("Name of Features is " + features.size());
		long start = new Date().getTime();
		for (Gene feature : features) {
			//System.err.println(feature.getFeatureId());
			fullTextSession.index(feature);
		}
		tx.commit();
		fullTextSession.close();
		long end = new Date().getTime();
		long duration = (end - start) / 1000;
		System.err.println("Processing completed: "+duration / 60 +" min "+duration  % 60+ " sec.");
	}
}
