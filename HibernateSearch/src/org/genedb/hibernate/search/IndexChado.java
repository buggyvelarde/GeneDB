package org.genedb.hibernate.search;

import java.io.Console;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.feature.AbstractGene;
import org.gmod.schema.sequence.feature.Transcript;
import org.gmod.schema.sequence.feature.UTR;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;
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
     * The number of features to be processed in a single batch. If it's set too
     * high, we run out of heap space.
     */
    private static final int BATCH_SIZE = 10;

    private String databaseUrl;
    private String databaseUsername;
    private String databasePassword;
    private String indexBaseDirectory;

    /**
     * Create a new instance configured with database connection information taken from
     * system properties. If no database password is supplied, the user is prompted for
     * one on the console.
     *
     * @param batchSize
     */
    public static IndexChado configuredWithSystemProperties() {
        String databaseUrl = System.getProperty("database.url");
        if (databaseUrl == null)
            die("The property database.url must be supplied, "
                    + "e.g. -Ddatabase.url=jdbc:postgres://localhost:10101/malaria_workshop");
        String databaseUsername = System.getProperty("database.user");
        if (databaseUsername == null)
            die("The property database.user must be supplied, " + "e.g. -Ddatabase.user=pathdb");

        String databasePassword = System.getProperty("database.password");
        if (databasePassword == null)
            databasePassword = promptForPassword(databaseUrl, databaseUsername);

        String indexBaseDirectory = System.getProperty("index.base");
        if (indexBaseDirectory == null)
            die("The property index.base must be supplied, "
                    + "e.g. -Dindex.base=/software/pathogen/genedb/indexes");

        return new IndexChado(databaseUrl, databaseUsername, databasePassword, indexBaseDirectory);
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

    /**
     * Create a new instance configured with the specified database connection details.
     *
     * @param databaseUrl
     * @param databaseUsername
     * @param databasePassword
     * @param indexBaseDirectory
     */
    private IndexChado(String databaseUrl, String databaseUsername, String databasePassword,
            String indexBaseDirectory) {
        this.databaseUrl = databaseUrl;
        this.databaseUsername = databaseUsername;
        this.databasePassword = databasePassword;
        this.indexBaseDirectory = indexBaseDirectory;
    }

    private Map<Integer,SessionFactory> sessionFactoryByBatchSize = new HashMap<Integer,SessionFactory>();

    /**
     * Get a session factory configured with the database connection information
     * for this instance, and the supplied batch size.
     *
     * @param batchSize
     * @return
     */
    private SessionFactory getSessionFactory(int batchSize) {
        if (sessionFactoryByBatchSize.containsKey(batchSize))
            return sessionFactoryByBatchSize.get(batchSize);

        Configuration cfg = new AnnotationConfiguration();
        cfg.configure();
        cfg.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        cfg.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
        cfg.setProperty("hibernate.connection.username", databaseUsername);
        cfg.setProperty("hibernate.connection.password", databasePassword);
        cfg.setProperty("hibernate.connection.url", databaseUrl);
        cfg.setProperty("hibernate.search.default.directory_provider",
            "org.hibernate.search.store.FSDirectoryProvider");
        cfg.setProperty("hibernate.search.worker.batch_size", String.valueOf(batchSize));
        cfg.setProperty("hibernate.search.default.indexBase", indexBaseDirectory);

        FullTextIndexEventListener ft = new FullTextIndexEventListener();
        cfg.setListener("post-insert", ft);
        cfg.setListener("post-update", ft);
        cfg.setListener("post-delete", ft);

        SessionFactory sessionFactory = cfg.buildSessionFactory();
        sessionFactoryByBatchSize.put(batchSize, sessionFactory);
        return sessionFactory;
    }

    /**
     * Create a new session, configured with the supplied batch size.
     *
     * @param batchSize
     * @return
     */
    private FullTextSession newSession(int batchSize) {
        SessionFactory sessionFactory = getSessionFactory(batchSize);
        FullTextSession session = Search.createFullTextSession(sessionFactory.openSession());
        session.setFlushMode(FlushMode.MANUAL);
        session.setCacheMode(CacheMode.IGNORE);
        return session;
    }

    /**
     * Index features of the specified class. First of all indexes the features
     * in batches, and then retries the failures one-by-one.
     *
     * @param featureClass
     * @param numBatches
     */
    public void indexFeatures(Class<? extends Feature> featureClass, int numBatches) {
        FullTextSession session = newSession(BATCH_SIZE);
        Transaction transaction = session.beginTransaction();
        Set<Integer> failed = batchIndexFeatures(featureClass, numBatches, session);
        transaction.commit();
        session.close();

        if (failed.size() > 0)
            reindexFailedFeatures(failed);
    }

    /**
     * Attempt to index features in batches. Returns identifiers of the features
     * that failed to be indexed. (An exception processing a feature will cause
     * the whole batch to fail, so it's worth trying to reindex failed features
     * one-by-one.)
     *
     * @param featureClass the class of features to index
     * @param numBatches the number of batches to process. If zero or negative,
     *                process all
     * @param session
     * @return a set of featureIds of the features that failed to be indexed
     */
    private Set<Integer> batchIndexFeatures(Class<? extends Feature> featureClass,
            int numBatches, FullTextSession session) {

        Set<Integer> failedToLoad = new HashSet<Integer>();
        Criteria criteria = session.createCriteria(featureClass);
        criteria.add(Restrictions.eq("obsolete", false)); // Do not index obsolete features
        if (numBatches > 0)
            criteria.setMaxResults(numBatches * BATCH_SIZE);

        ScrollableResults results = criteria.scroll(ScrollMode.FORWARD_ONLY);

        logger.info(String.format("Indexing %s", featureClass));
        int thisBatchCount = 0;
        Set<Integer> thisBatch = new HashSet<Integer>();

        for (int i = 1; results.next(); i++) {
            Feature feature = (Feature) results.get(0);
            thisBatch.add(feature.getFeatureId());

            boolean failed = false;
            try {
                logger.debug(String.format("Indexing '%s' (%s)", feature.getUniqueName(),
                    feature.getClass()));
                session.index(feature);
            } catch (Exception e) {
                logger.error("Batch failed", e);
                failed = true;
            }

            if (failed || ++thisBatchCount == BATCH_SIZE) {
                logger.info(String.format("Indexed %d of %s", i, featureClass));
                session.clear();
                thisBatchCount = 0;
                if (failed)
                    failedToLoad.addAll(thisBatch);
                thisBatch = new HashSet<Integer>();
            }
        }
        results.close();
        return failedToLoad;
    }

    /**
     * Attempt to index the provided features individually
     * (i.e. in batches of one). Used to reindex failures
     * from a batch indexing run.
     *
     * @param failed a set of features to reindex
     */
    private void reindexFailedFeatures(Set<Integer> failed) {
        logger.info("Attempting to reindex failed features");
        FullTextSession session = newSession(1);
        Transaction transaction = session.beginTransaction();
        for (int featureId : failed) {
            logger.debug(String.format("Attempting to index feature %d", featureId));
            Feature feature = (Feature) session.load(Feature.class, featureId);
            logger.debug(String.format("Loaded feature '%s'", feature.getUniqueName()));
            try {
                session.index(feature);
                logger.debug("Feature successfully indexed");
            } catch (Exception e) {
                logger.info(String.format("Failed to index feature '%s' on the second attempt",
                    feature.getUniqueName()), e);
            }
            session.clear();
        }
        transaction.commit();
        session.close();
    }


    public static void main(String[] args) {
        // The numBatches argument is only useful for quick-and-dirty testing
        int numBatches = -1;
        if (args.length == 1)
            numBatches = Integer.parseInt(args[0]);
        else if (args.length != 0)
            throw new IllegalArgumentException("Unexpected command-line arguments");

        IndexChado indexer = IndexChado.configuredWithSystemProperties();
        indexer.indexFeatures(AbstractGene.class, numBatches);
        indexer.indexFeatures(Transcript.class, numBatches);
        indexer.indexFeatures(UTR.class, numBatches);
    }
}
