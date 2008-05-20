package org.genedb.hibernate.search;

import java.io.Console;
import java.io.File;

import org.apache.log4j.Logger;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.Gene;
import org.gmod.schema.sequence.Mrna;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.event.FullTextIndexEventListener;

public class IndexChado {
    private static Logger logger = Logger.getLogger(IndexChado.class);

    private static void die(String message) {
        System.err.println(message);
        System.err.println();
        System.exit(1);
    }
    
    /**
     * The number of features to be processed in a single batch.
     * If it's set too high, we run out of heap space.
     * 
     * Also, if indexing of a feature fails then the whole batch will
     * fail to be indexed. At present (2008-05-20) there are some
     * failures on <code>Mrna</code> caused by transcripts having no associated
     * gene. To avoid collateral damage, we set BATCH_SIZE to 1
     * until this problem is resolved, which unfortunately makes
     * indexing very slow.
     */
    private static final int BATCH_SIZE = 1;

    private static SessionFactory getSessionFactory(String databaseUrl, String databaseUsername,
            String databasePassword, String indexBaseDirectory) {
        Configuration cfg = new Configuration();
        cfg.addDirectory(new File("../genedb-db/input"));
        cfg.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        cfg.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
        cfg.setProperty("hibernate.connection.username", databaseUsername);
        cfg.setProperty("hibernate.connection.password", databasePassword);
        cfg.setProperty("hibernate.connection.url",      databaseUrl);
        cfg.setProperty("hibernate.search.default.directory_provider",
            "org.hibernate.search.store.FSDirectoryProvider");
        cfg.setProperty("hibernate.search.worker.batch_size", String.valueOf(BATCH_SIZE));
        cfg.setProperty("hibernate.search.default.indexBase", indexBaseDirectory);
        
        FullTextIndexEventListener ft = new FullTextIndexEventListener();
        cfg.setListener("post-insert", ft);
        cfg.setListener("post-update", ft);
        cfg.setListener("post-delete", ft);
        
        return cfg.buildSessionFactory();
    }
    
    private static String promptForPassword(String databaseUrl, String databaseUsername) {
        Console console = System.console();
        if (console == null)
            die("No password has been supplied, and no console found");
        
        char[] password = null;
        while (password == null)
            password = console.readPassword("Password for %s@%s: ", databaseUsername, databaseUrl);
        return new String(password);
    }
    
    private static SessionFactory getSessionFactory() {
        String databaseUrl = System.getProperty("database.url");
        if (databaseUrl == null)
            die("The property database.url must be supplied, "+
                "e.g. -Ddatabase.url=jdbc:postgres://localhost:10101/malaria_workshop");
        String databaseUsername = System.getProperty("database.user");
        if (databaseUsername == null)
            die("The property database.user must be supplied, "+
                "e.g. -Ddatabase.user=pathdb");
        
        String databasePassword = System.getProperty("database.password");
        if (databasePassword == null)
            databasePassword = promptForPassword(databaseUrl, databaseUsername);
        
        String indexBaseDirectory = System.getProperty("index.base");
        if (indexBaseDirectory == null)
            die("The property index.base must be supplied, "+
                "e.g. -Dindex.base=/software/pathogen/genedb/indexes");
        
        return getSessionFactory(databaseUrl, databaseUsername, databasePassword, indexBaseDirectory);
    }

    public static void main(String[] args) {
        // The numBatches argument is only useful for quick-and-dirty testing
        int numBatches = -1;
        if (args.length == 1)
            numBatches = Integer.parseInt(args[0]);
        else if (args.length != 0)
            throw new IllegalArgumentException("Unexpected command-line arguments");

        SessionFactory sf = getSessionFactory();
        FullTextSession session = Search.createFullTextSession(sf.openSession());

        session.setFlushMode(FlushMode.MANUAL);
        session.setCacheMode(CacheMode.IGNORE);
        
        Transaction transaction;
        
        transaction = session.beginTransaction();
        indexFeatures(Gene.class, numBatches, session);
        transaction.commit();

        transaction = session.beginTransaction();
        indexFeatures(Mrna.class, numBatches, session);
        transaction.commit();
    }
    
    private static void indexFeatures(Class<? extends Feature> featureClass, int numBatches, FullTextSession session) {
       Criteria criterion = session.createCriteria(featureClass);
       if (numBatches > 0)
           criterion.setMaxResults(numBatches * BATCH_SIZE);
            
       ScrollableResults results = criterion.scroll(
            ScrollMode.FORWARD_ONLY);

        logger.info(String.format("Indexing %s", featureClass));
        for (int i = 1; results.next(); i++) {
            Feature feature = (Feature) results.get(0);
            try {
                logger.debug(String.format("Indexing '%s' (%s)", feature.getUniqueName(), featureClass));
                session.index(feature);
            }
            catch (Exception e) {
                logger.error(String.format("Failed while indexing feature '%s' (%s)", feature.getUniqueName(), featureClass), e);
            }
            if (i % BATCH_SIZE == 0) {
                logger.info(String.format("Indexed %d of %s", i, featureClass));
                session.clear();
            }
        }
        results.close();
    }
}
