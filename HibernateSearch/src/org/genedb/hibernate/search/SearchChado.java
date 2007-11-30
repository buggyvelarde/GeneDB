package org.genedb.hibernate.search;

import java.io.File;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.gmod.schema.cv.CvTerm;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.event.FullTextIndexEventListener;

public class SearchChado {
	
	@SuppressWarnings("unchecked")
	public static void main (String[] args) {
		Configuration cfg = new Configuration();
        File f = new File("resources/");
        cfg.addDirectory(f);
        cfg.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
		cfg.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
		cfg.setProperty("hibernate.connection.username", "pathdb");
		cfg.setProperty("hibernate.connection.password", "Pyrate_1");
		cfg.setProperty("hibernate.connection.url", "jdbc:postgresql://pathdbsrv1a:10101/malaria_workshop");
		//cfg.setProperty("hibernate.search.default.directory_provider", "org.hibernate.search.store.FSDirectoryProvider");
		//cfg.setProperty("hibernate.search.default.indexBase", "/Users/cp2/hibernate/search/indexes");
		FullTextIndexEventListener ft = new FullTextIndexEventListener();
		cfg.setListener("post-insert", ft);
		cfg.setListener("post-update", ft);
		cfg.setListener("post-delete",ft);
        SessionFactory sf = cfg.buildSessionFactory();
        Session session = sf.openSession();
        FullTextSession fullTextSession = Search.createFullTextSession(session);
		Transaction tx = fullTextSession.beginTransaction();
		MultiFieldQueryParser qp = new MultiFieldQueryParser(new String[]{"name"},new StandardAnalyzer());
		 try {
			Query q = qp.parse("gene");
			org.hibernate.Query query = fullTextSession.createFullTextQuery(q, CvTerm.class);
			List<CvTerm> result = query.list();
			for (CvTerm term : result) {
				System.out.println("name is " + term.getName());
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		tx.commit();
	}

}
