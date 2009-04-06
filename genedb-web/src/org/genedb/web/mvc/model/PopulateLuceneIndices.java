package org.genedb.web.mvc.model;

import org.genedb.db.audit.ChangeSet;
import org.genedb.db.dao.SequenceDao;

import org.gmod.schema.cfg.ChadoAnnotationConfiguration;
import org.gmod.schema.feature.AbstractGene;
import org.gmod.schema.feature.Gap;
import org.gmod.schema.feature.Gene;
import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.Transcript;
import org.gmod.schema.mapped.Feature;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.event.FullTextIndexEventListener;
import org.hibernate.search.reader.ReaderProvider;
import org.hibernate.search.store.DirectoryProvider;
import org.postgresql.ds.PGSimpleDataSource;

import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;
import uk.co.flamingpenguin.jewel.cli.Cli;
import uk.co.flamingpenguin.jewel.cli.CliFactory;
import uk.co.flamingpenguin.jewel.cli.Option;
import uk.co.flamingpenguin.jewel.cli.Unparsed;

import java.io.Console;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


/**
 * Create Lucene indices.
 * <p>
 * The way it works is as follows:
 * A list of feature_ids is generated that must be deleted and/or updated. This can be done by:
 *    (i)  Indexing by type. Each indexed feature type is treated in turn; currently
 *         the classes <code>AbstractGene</code>, <code>Transcript</code> and
 *         <code>Gap</code> are indexed (in that order). For each type, all features of that type
 *         are listed
 *    (ii) The first option can be limited by a given organism
 *   (iii) A list of features can be provided through a <code>ChangeSet</code>
 *
 * Given this list, the features are loaded and indexed in batches of 10.
 * If an exception is thrown while indexing a particular feature, the exception is caught and
 * the whole batch will fail.
 * The members of the failed batch are then put into a queue. When all batches of the relevant type
 * have been processed, the queued members of failed batches are indexed individually. If a feature
 * fails this time, that means it cannot be indexed (due to bad data, or a bug in the code).
 * An error is logged.
 *
 * @author rh11
 */
public class PopulateLuceneIndices implements IndexUpdater {
    private static Logger logger = Logger.getLogger(PopulateLuceneIndices.class);

    /**
     * The number of features to be processed in a single batch. If it's set too
     * high, we run out of heap space.
     */
    private static final int BATCH_SIZE = 10;

    /**
     * Which types of feature to index.
     */
    private static final Collection<Class<? extends Feature>> INDEXED_CLASSES
    = new ArrayList<Class<? extends Feature>>();
    static {
        INDEXED_CLASSES.add(AbstractGene.class);
        INDEXED_CLASSES.add(Transcript.class);
        INDEXED_CLASSES.add(Polypeptide.class);
        INDEXED_CLASSES.add(Gap.class);
        // Add feature types here, if a new type of feature should be indexed.
        // Don't forget to update the class doc comment!
    }

    private boolean failFast = false;

    private DataSource dataSource;

    private SequenceDao sequenceDao;

    private Map<Integer,SessionFactory> sessionFactoryByBatchSize = new HashMap<Integer,SessionFactory>();

    private String indexBaseDirectory;
    private String organism;
    private int numBatches = -1;


    public PopulateLuceneIndices() {
        // Default constructor
    }

    /**
     * Create a new instance configured with the specified database connection details.
     *
     * @param databaseUrl
     * @param databaseUsername
     * @param databasePassword
     * @param indexBaseDirectory
     */
    private PopulateLuceneIndices(String host, int port, String userName, String dbName, String password,
            String indexBaseDirectory) {
        this.indexBaseDirectory = indexBaseDirectory;
        makeDataSource(host, port, userName, dbName, password);
    }


    private void makeDataSource(String host, int port, String userName, String dbName, String password) {
        if (dataSource != null) {
            logger.warn("A new datasource is being created, although one is already defined");
        }

        PGSimpleDataSource sds = new PGSimpleDataSource();

        sds.setServerName(host);
        sds.setPortNumber(port);
        sds.setDatabaseName(dbName);
        sds.setUser(userName);
        sds.setPassword(password);

        this.dataSource = sds;
    }


    /**
     * Get a session factory configured with the database connection information
     * for this instance, and the supplied batch size.
     *
     * @param batchSize
     * @return
     */
    private SessionFactory getSessionFactory(Integer batchSize) {
        if (sessionFactoryByBatchSize.containsKey(batchSize)) {
            return sessionFactoryByBatchSize.get(batchSize);
        }

        ChadoAnnotationConfiguration cfg = new ChadoAnnotationConfiguration();
        cfg.setDataSource(getDataSource());

        cfg.addPackage("org.gmod.schema.mapped");
        cfg.addPackage("org.gmod.schema.feature");

        cfg.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        cfg.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
        //cfg.setProperty("hibernate.connection.username", databaseUsername);
        //cfg.setProperty("hibernate.connection.password", databasePassword);
        //cfg.setProperty("hibernate.connection.url", databaseUrl);

        cfg.setProperty("hibernate.search.default.directory_provider",
        "org.hibernate.search.store.FSDirectoryProvider");
        cfg.setProperty("hibernate.search.worker.batch_size", String.valueOf(batchSize));
        cfg.setProperty("hibernate.search.default.indexBase", indexBaseDirectory);

        FullTextIndexEventListener ft = new FullTextIndexEventListener();
        cfg.setListener("post-insert", ft);
        cfg.setListener("post-update", ft);
        cfg.setListener("post-delete", ft);

        //cfg.configure();

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
    private FullTextSession newSession(Integer batchSize) {
        SessionFactory sessionFactory = getSessionFactory(batchSize);
        FullTextSession session = Search.createFullTextSession(sessionFactory.openSession());
        session.setFlushMode(FlushMode.MANUAL);
        session.setCacheMode(CacheMode.IGNORE);
        return session;
    }


    public void indexFeatures(List<Integer> featureIds) {
        FullTextSession session = newSession(BATCH_SIZE);
        Transaction transaction = session.beginTransaction();
        Set<Integer> failed = batchIndexFeatures(featureIds, session);
        transaction.commit();
        session.close();

        if (failed.size() > 0) {
            reindexFailedFeatures(failed);
        }
    }


    /**
     * Index features of the specified class. First of all indexes the features
     * in batches, and then retries the failures one-by-one.
     *
     * @param featureClass
     * @param numBatches
     */
    public void indexFeatures(Class<? extends Feature> featureClass) {
        FullTextSession session = newSession(BATCH_SIZE);
        Transaction transaction = session.beginTransaction();
        Set<Integer> failed = batchIndexFeatures(featureClass, session);
        transaction.commit();
        session.close();

        if (failed.size() > 0) {
            reindexFailedFeatures(failed);
        }
    }

    public void indexFeatures() {
        for (Class<? extends Feature> featureClass: INDEXED_CLASSES) {
            indexFeatures(featureClass);
        }
    }

    public boolean updateAllCaches(ChangeSet changeSet) {
        // Ignore changes to top level feature

        try {
            // Let's process deletes first
            Set<Integer> deletedIds = Sets.newHashSet();
            deletedIds.addAll(changeSet.deletedFeatureIds(Gene.class));
            deletedIds.addAll(changeSet.deletedFeatureIds(Transcript.class));
            deletedIds.addAll(changeSet.deletedFeatureIds(Polypeptide.class));
            deletedIds.addAll(changeSet.deletedFeatureIds(Gap.class));
            deleteFromIndex(deletedIds);


            // Now adds and updates
            Set<Integer> alteredIds = Sets.newHashSet();
            alteredIds.addAll(changeSet.newFeatureIds(Gene.class));
            alteredIds.addAll(changeSet.changedFeatureIds(Gene.class));
            alteredIds.addAll(changeSet.newFeatureIds(Transcript.class));
            alteredIds.addAll(changeSet.changedFeatureIds(Transcript.class));
            alteredIds.addAll(changeSet.newFeatureIds(Polypeptide.class));
            alteredIds.addAll(changeSet.changedFeatureIds(Polypeptide.class));
            alteredIds.addAll(changeSet.newFeatureIds(Gap.class));
            alteredIds.addAll(changeSet.changedFeatureIds(Gap.class));

            FullTextSession session = newSession(BATCH_SIZE);
            Transaction transaction = session.beginTransaction();

            Set<Integer> failed = batchIndexFeatures(alteredIds, session);
            transaction.commit();
            session.close();

            if (failed.size() > 0) {
                reindexFailedFeatures(failed);
            }
        }
        catch (IOException exp) {
            logger.error("Failed to update Lucene indices", exp);
            return false;
        }
        return true;
    }

    /**
     * Delete all the given ids from the index
     *
     * @param ids the list of feature ids
     * @throws IOException
     */
    private void deleteFromIndex(Collection<Integer> ids) throws IOException {
        FullTextSession session = newSession(BATCH_SIZE);
        SearchFactory searchFactory = session.getSearchFactory();
        ReaderProvider rp = searchFactory.getReaderProvider();
        DirectoryProvider[] directoryProviders = searchFactory.getDirectoryProviders(Feature.class);
        if (directoryProviders ==  null || directoryProviders.length != 1) {
            throw new RuntimeException("Unable to open a directory provider");
        }
        IndexReader reader = rp.openReader(directoryProviders[0]);

        for (Integer id : ids) {
            reader.deleteDocuments(new Term("featureId", Integer.toString(id)));
        }


        rp.closeReader(reader);
        session.close();
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
    private Set<Integer> batchIndexFeatures(Collection<Integer> featureIds,
            FullTextSession session) {

        Set<Integer> failedToLoad = new HashSet<Integer>();

        int thisBatchCount = 0;
        Set<Integer> thisBatch = new HashSet<Integer>();

        int i = 0;
        for (Integer featureId : featureIds) {
            Feature feature = sequenceDao.getFeatureById(i);
            thisBatch.add(featureId);

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
                logger.info(String.format("Indexed %d of %d", i, featureIds.size()));
                session.clear();
                thisBatchCount = 0;
                if (failed) {
                    failedToLoad.addAll(thisBatch);
                }
                thisBatch = new HashSet<Integer>();
            }
            i++;
        }
        return failedToLoad;
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
            FullTextSession session) {

        Criteria criteria = session.createCriteria(featureClass);
        criteria.add(Restrictions.eq("obsolete", Boolean.FALSE)); // Do not index obsolete features
        if (organism != null) {
            criteria.createCriteria("organism")
            .add( Restrictions.eq("commonName", organism));
        }
        if (numBatches > 0) {
            criteria.setMaxResults(numBatches * BATCH_SIZE);
        }

        ScrollableResults results = criteria.scroll(ScrollMode.FORWARD_ONLY);

        logger.info(String.format("Indexing %s", featureClass));
        List<Integer> featureIds = Lists.newArrayList();

        for (int i = 1; results.next(); i++) {
            Feature feature = (Feature) results.get(0);
            featureIds.add(feature.getFeatureId());
        }

        results.close();
        return batchIndexFeatures(featureIds, session);
    }

    /**
     * Attempt to index the provided features individually
     * (i.e. in batches of one). Used to reindex failures
     * from a batch indexing run.
     *
     * @param failed a set of features to reindex
     * @throws Exception
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
            } catch (Exception exp) {
                String msg = String.format("Failed to index feature '%s' on the second attempt", feature.getUniqueName());
                if (failFast) {
                    throw new RuntimeException(msg, exp);
                }
                logger.info(msg, exp);
            }
            session.clear();
        }
        transaction.commit();
        session.close();
    }


    /* Accessors */

    public DataSource getDataSource() {
        if (dataSource == null) {
            throw new NullPointerException("Datasource hasn't been injected, or created from connection properties");
        }
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
    }

    public int getNumBatches() {
        return numBatches;
    }

    public void setNumBatches(int numBatches) {
        this.numBatches = numBatches;
    }

    private void setOrganism(String organism) {
        this.organism = organism;
    }


    /* Static methods */


    public static String promptForPassword(String databaseUrl, String databaseUsername) {
        Console console = System.console();
        if (console == null) {
            System.err.println("No password has been supplied, and no console found\n");
            System.exit(1);
            return ""; // Dummy to prevent null warning
        }

        char[] password = null;
        while (password == null) {
            password = console.readPassword("Password for %s@%s: ", databaseUsername, databaseUrl);
        }
        return new String(password);
    }


    public static void main(String[] args) {

        Cli<PopulateLuceneIndicesArgs> cli = CliFactory.createCli(PopulateLuceneIndicesArgs.class);
        PopulateLuceneIndicesArgs iga = null;
        try {
            iga = cli.parseArguments(args);
        }
        catch(ArgumentValidationException exp) {
            System.err.println("Unable to run:");
            System.err.println(cli.getHelpMessage());
            exp.printStackTrace();
            return;
        }

        String user = System.getenv("USER");
        if ( iga.isUserName()) {
            user = iga.getUserName();
        }

        String password;
        if ( ! iga.isPassword()) {
            password = promptForPassword(iga.getDbName(), iga.getUserName());
        } else {
            password = iga.getPassword();
        }

        PopulateLuceneIndices indexer = new PopulateLuceneIndices(iga.getHost(), iga.getPort(),
                user, iga.getDbName(), password, iga.getIndexDirectory());

        if (iga.isOrganism()) {
            indexer.setOrganism(iga.getOrganism());
        }

        indexer.setFailFast(iga.getFailFast());

        if  (iga.isNumBatches()) {
            indexer.setNumBatches(iga.getNumBatches());
        }

        indexer.indexFeatures();
    }



    interface PopulateLuceneIndicesArgs {

        /* Testing */

        @Option(shortName="n", description="Number of batches - only useful for quick-and-dirty testing")
        int getNumBatches();
        void setNumBatches(int numBatches);
        boolean isNumBatches();

        @Option(shortName="f", longName="failFast", description="Fail on second try if there's a problem")
        boolean getFailFast();
        void setFailFast(boolean failFast);
        boolean isFailFast();

        /* What exactly to index */

        @Option(shortName="o", description="Only index this organism")
        String getOrganism();
        void setOrganism(String organism);
        boolean isOrganism();


        /* Db connection info */

        @Option(shortName="h", longName="host", description="Host for db server", defaultValue="localhost")
        String getHost();

        @Option(shortName="p", longName="port", description="Port number of db server", defaultValue="5432")
        int getPort();


        @Option(shortName="U", longName="userName", description="User to log in as")
        String getUserName();
        boolean isUserName();

        @Option(shortName="P", longName="password", description="")
        String getPassword();
        boolean isPassword();

        @Unparsed
        String getDbName();

        /* Index location */

        @Option(shortName="i", longName="index", description="Directory where the indices are stored")
        String getIndexDirectory();

    }

}