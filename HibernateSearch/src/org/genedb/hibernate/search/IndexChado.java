package org.genedb.hibernate.search;

import java.io.File;
import java.util.List;

import org.genedb.db.dao.CvDao;
import org.genedb.db.dao.SequenceDao;
import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.sequence.Feature;
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
	
	private SequenceDao sequenceDao;
	
	private CvDao cvDao;
	
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
		//cfg.setProperty("hibernate.search.default.directory_provider", "org.hibernate.search.store.FSDirectoryProvider");
		//cfg.setProperty("hibernate.search.worker.batch_size", "2");
		//cfg.setProperty("hibernate.search.default.indexBase", "/Users/cp2/hibernate/search/indexes");
		//FullTextIndexEventListener ft = new FullTextIndexEventListener();
		//cfg.setListener("post-insert", ft);
		//cfg.setListener("post-update", ft);
		//cfg.setListener("post-delete",ft);
        SessionFactory sf = cfg.buildSessionFactory();
        Session session = sf.openSession();
        FullTextSession fullTextSession = Search.createFullTextSession(session);
		Transaction tx = fullTextSession.beginTransaction();
		Query q = session.createQuery("from Feature f where f.uniqueName like 'MAL13%'");
		System.err.println("query ran successfully...");
		
		List<Feature> features = q.list();
		System.err.println("Name of Features is " + features.size());
		/*for (CvTerm feature : features) {
			fullTextSession.index(feature);
		}*/
		tx.commit();
	}

	public SequenceDao getSequenceDao() {
		return sequenceDao;
	}

	public void setSequenceDao(SequenceDao sequenceDao) {
		this.sequenceDao = sequenceDao;
	}

	public CvDao getCvDao() {
		return cvDao;
	}

	public void setCvDao(CvDao cvDao) {
		this.cvDao = cvDao;
	}
}
