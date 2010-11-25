package org.genedb.web.mvc.model;

import org.genedb.db.audit.ChangeSet;

import org.gmod.schema.feature.AbstractGene;
import org.gmod.schema.feature.Gap;
import org.gmod.schema.feature.Gene;
import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.ProductiveTranscript;
import org.gmod.schema.feature.Transcript;
import org.gmod.schema.mapped.Feature;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.reader.ReaderProvider;
import org.hibernate.search.store.DirectoryProvider;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;
import uk.co.flamingpenguin.jewel.cli.Cli;
import uk.co.flamingpenguin.jewel.cli.CliFactory;
import uk.co.flamingpenguin.jewel.cli.Option;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    @Override
    public int updateTranscriptCache(ChangeSet changeSet) throws Exception {
        // TODO Auto-generated method stub
        return 0;
    }

    private static Logger logger = Logger.getLogger(PopulateLuceneIndices.class);

    /**
     * The number of features to be processed in a single batch. If it's set too
     * high, we run out of heap space.
     */
    private static final int DEFAULT_BATCH_SIZE = 10;

    private int batchSize = DEFAULT_BATCH_SIZE;

    /**
     * Which types of feature to index.
     */
    private static final Collection<Class<? extends Feature>> INDEXED_CLASSES
    = new ArrayList<Class<? extends Feature>>();
    static {
        INDEXED_CLASSES.add(Gap.class);
        // Add feature types here, if a new type of feature should be indexed.
        // Don't forget to update the class doc comment!
    }

    private boolean failFast = false;

    private ConfigurableGeneDBSessionFactoryBean configurableGeneDBSessionFactoryBean;

    public ConfigurableGeneDBSessionFactoryBean getConfigurableGeneDBSessionFactoryBean() {
        return configurableGeneDBSessionFactoryBean;
    }

    public void setConfigurableGeneDBSessionFactoryBean(
            ConfigurableGeneDBSessionFactoryBean configurableGeneDBSessionFactoryBean) {
        this.configurableGeneDBSessionFactoryBean = configurableGeneDBSessionFactoryBean;
    }

    private String indexBaseDirectory;
    private String organism;
    private int numBatches = -1;

    private int featureStart = -1;
    private int featureEnd;

    private String hibernateDialect = "org.hibernate.dialect.PostgreSQLDialect";
    private String hibernateDriverClass = "org.postgresql.Driver";



    public PopulateLuceneIndices() {
        // Default constructor
    }

    /**
     * Create a new FullTextSession, configured with the supplied sessionFactory.
     *
     * @param batchSize
     * @return
     * @throws Exception
     */
    private FullTextSession newSession(int batchSize) {
        SessionFactory sessionFactory = null;
        try {
            sessionFactory = configurableGeneDBSessionFactoryBean.createFullTextSessionFactory(indexBaseDirectory, batchSize);
        } catch (Exception exp) {
            exp.printStackTrace();
            System.exit(65);
        }
        logger.info("sessionFactory is "+sessionFactory);
        Session basicSession = sessionFactory.openSession();
        FullTextSession session = Search.getFullTextSession(basicSession);
        session.setFlushMode(FlushMode.MANUAL);
        session.setCacheMode(CacheMode.IGNORE);
        logger.info(String.format("Just made. The value of session is '%s' and it is '%s'", session, session.isConnected()));

        return session;
    }

    /**
     * Index features of the specified class. First of all indexes the features
     * in batches, and then retries the failures one-by-one.
     *
     * @param featureClass
     * @param numBatches
     */
    public void indexFeatures(Class<? extends Feature> featureClass, int numBatches, FullTextSession session) {
        //Transaction transaction = session.beginTransaction();
        Set<Integer> failed = batchIndexFeatures(featureClass, numBatches, session);
        //transaction.commit();
        //session.close();

        if (failed.size() > 0) {
            reindexFailedFeatures(failed);
        }
        logger.trace("Leaving indexFeatures ("+featureClass+")");
    }

    public void indexFeatures() {
        FullTextSession session = newSession(batchSize);

        indexGenes(session);

        for (Class<? extends Feature> featureClass: INDEXED_CLASSES) {
            indexFeatures(featureClass, numBatches, session);
        }
        session.close();
        logger.trace("Leaving indexFeatures");
    }

    private Set<Integer> indexGenes(FullTextSession session) {

        Set<Integer> failedToLoad = new HashSet<Integer>();

        String hql = "select featureId from AbstractGene where obsolete=false";

        if (featureStart > 0) {
            hql += " and featureId >= "+featureStart;
        }
        if (featureEnd > 0) {
            hql += " and featureId < "+featureEnd;
        }

        if (organism != null) {
            hql += " and organism.commonName = '"+organism +"'";
        }
        Query idQuery = session.createQuery(hql);


        logger.info("Indexing AbstractGenes");

        @SuppressWarnings("unchecked") List<Integer> allIds = idQuery.list();

        int batchCount = 0;
        int start = 0;
        int end = start + batchSize;
        //long startTime = new Date().getTime();

        while (start < allIds.size()) {
            if (end > allIds.size()) {
                end = allIds.size();
            }

            List<Integer> thisBatch = allIds.subList(start, end);

            String ids = StringUtils.collectionToCommaDelimitedString(thisBatch);
            logger.debug(String.format("The list of ids being looked up is '%s'", ids));

            Query featureQuery = session.createQuery("from Feature where featureId in ("+ids+")");

            if (numBatches > 0) {
                featureQuery.setMaxResults(numBatches * batchSize);
            }
            //featureQuery.setMaxResults(BATCH_SIZE);

            @SuppressWarnings("unchecked") List<AbstractGene> genes = featureQuery.list();

            boolean failed = false;
            int i=0;

            for (AbstractGene gene : genes) {
                i++;
                try {
                    logger.info(String.format("Indexing '%s' (%s)", gene.getUniqueName(), gene.getClass()));
                    session.index(gene);

                    for (Transcript transcript : gene.getTranscripts()) {
                        logger.info(String.format("-Indexing '%s' (%s)", transcript.getUniqueName(), transcript.getClass()));
                        session.index(transcript);
                        if (transcript instanceof ProductiveTranscript) {
                            ProductiveTranscript productiveTranscript = (ProductiveTranscript) transcript;
                            Polypeptide protein = productiveTranscript.getProtein();
                            if (protein != null) {
                                logger.info(String.format("--Indexing '%s' (%s)", protein.getUniqueName(), protein.getClass()));
                                session.index(protein);
                            }
                        }

                    }

                } catch (Exception exp) {
                    logger.error("Batch failed", exp);
                    failed = true;
                }
                if ((i % 10) == 0) {
                    logger.debug('.');
                }
             }

            batchCount++;
            //logger.info(String.format("Indexed '%d'pc ('%d' of '%d'), %d hours, %d mins left", (batchCount*batchSize)*100/allIds.size(),batchCount*batchSize, allIds.size(), hours, mins));
            logger.debug(String.format("Indexed '%d'pc ('%d' of '%d')", (batchCount*batchSize)*100/allIds.size(),batchCount*batchSize, allIds.size()));
            if (failed) {
                failedToLoad.addAll(thisBatch);
            } else {
                try {
                    session.flushToIndexes();
                }
                catch (Exception exp) {
                    exp.printStackTrace(System.err);
                    System.exit(1);
                }
            }
            session.clear();

            start = end;
            end = start + batchSize;
        }

        logger.trace("Leaving batchIndexFeatures");
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
    @Transactional
    private Set<Integer> batchIndexFeatures(Class<? extends Feature> featureClass,
            int numBatches, FullTextSession session) {

        Set<Integer> failedToLoad = new HashSet<Integer>();

        String hql = "select featureId from "+featureClass.getName()+" where obsolete=false";

        if (organism != null) {
            hql += " and organism.commonName = '"+organism +"'";
        }
        Query idQuery = session.createQuery(hql);

        //if (numBatches > 0) {
        //    q.setMaxResults(numBatches * BATCH_SIZE);
        //}
        //q.setMaxResults(BATCH_SIZE);

        logger.info(String.format("Indexing %s", featureClass));

        @SuppressWarnings("unchecked") List<Integer> allIds = idQuery.list();

        int batchCount = 0;
        int start = 0;
        int end = start + batchSize;

        while (start < allIds.size()) {
            if (end > allIds.size()) {
                end = allIds.size();
            }

            List<Integer> thisBatch = allIds.subList(start, end);

            String ids = StringUtils.collectionToCommaDelimitedString(thisBatch);

            Query featureQuery = session.createQuery("from "+featureClass.getName()+" where featureId in ("+ids+")");

            @SuppressWarnings("unchecked") List<Feature> features = featureQuery.list();

            boolean failed = false;
            int i=0;
            for (Feature feature : features) {
                i++;
                try {
                    logger.debug(String.format("Indexing '%s' (%s)", feature.getUniqueName(), feature.getClass()));
                    session.index(feature);
                    batchCount++;
                    logger.debug(String.format("Indexed '%d' ('%d' of '%d') of type '%s'", (batchCount*batchSize)*100/allIds.size(),batchCount*batchSize, allIds.size(), featureClass));
                } catch (Exception exp) {
                    //System.err.println(exp);
                    logger.error("Batch failed", exp);
                    failed = true;
                }
                if ((i % 10) == 0) {
                    logger.warn('.');
                }
             }

            if (failed) {
                logger.warn("Adding failed to batch");
                failedToLoad.addAll(thisBatch);
            } else {
                logger.debug("About to flush to indices");
                try {
                    session.flushToIndexes();
                }
                catch (Exception exp) {
                    exp.printStackTrace(System.err);
                    System.exit(1);
                }
                logger.debug("Just flushed to indices");
            }
            logger.debug("About to clear session");
            session.clear();
            logger.debug("Just cleared session");

            start = end;
            end = start + batchSize;
        }

        logger.trace("Leaving batchIndexFeatures");
        return failedToLoad;
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









    public void indexFeatures(List<Integer> featureIds) {
        FullTextSession session = newSession(batchSize);
        //Transaction transaction = session.beginTransaction();
        Set<Integer> failed = batchIndexFeatures(featureIds, session);
        //transaction.commit();
        session.close();

        if (failed.size() > 0) {
            reindexFailedFeatures(failed);
        }
        logger.trace("Leaving indexFeatures");
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

            FullTextSession session = newSession(batchSize);
            //Transaction transaction = session.beginTransaction();

            Set<Integer> failed = batchIndexFeatures(alteredIds, session);
            //transaction.commit();
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
        FullTextSession session = newSession(10);
        SearchFactory searchFactory = session.getSearchFactory();
        ReaderProvider rp = searchFactory.getReaderProvider();
        DirectoryProvider<?>[] directoryProviders = searchFactory.getDirectoryProviders(Feature.class);
        if (directoryProviders ==  null || directoryProviders.length < 1) {
            throw new RuntimeException("Unable to open a directory provider");
        }
        IndexReader reader = rp.openReader(directoryProviders);

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
    @Transactional
    private Set<Integer> batchIndexFeatures(Collection<Integer> featureIds,
            FullTextSession session) {

        logger.info(String.format("C. The value of session is '%s' and it is '%s'", session, session.isConnected()));
        Set<Integer> failedToLoad = new HashSet<Integer>();

        int thisBatchCount = 0;
        Set<Integer> thisBatch = new HashSet<Integer>();

        int i = 0;
        for (Integer featureId : featureIds) {
            Feature feature = (Feature) session.load(Feature.class, featureId);
            thisBatch.add(featureId);

            boolean failed = false;
            try {
                logger.debug(String.format("Indexing '%s' (%s)", feature.getUniqueName(),
                        feature.getClass()));
                session.index(feature);
            } catch (Exception exp) {
                logger.error("Batch failed", exp);
                failed = true;
            }

            if (failed || ++thisBatchCount == batchSize) {
                logger.debug(String.format("Indexed %d of %d", i, featureIds.size()));
                session.clear();
                thisBatchCount = 0;
                if (failed) {
                    failedToLoad.addAll(thisBatch);
                }
                thisBatch = new HashSet<Integer>();
            }
            i++;
        }
        logger.info(String.format("C. The value of session is '%s' and it is '%s'", session, session.isConnected()));
        return failedToLoad;
    }


    /* Accessors */

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

        /* Index location */
        @Option(shortName="i", longName="index", description="Directory where the indices are stored")
        String getIndexDirectory();

        /* Batch size */
        @Option(shortName="b", description="(Optional) batch size")
        int getBatchSize();
        void setBatchSize(int batchSize);
        boolean isBatchSize();

        /* Feature start */
        @Option(shortName="s", description="(Optional) featureId start")
        int getFeatureStart();
        void setFeatureStart(int featureStart);
        boolean isFeatureStart();

        /* Feature end */
        @Option(shortName="e", description="(Optional) featureId end")
        int getFeatureEnd();
        void setFeatureEnd(int featureEnd);
        boolean isFeatureEnd();
    }


    public String getIndexBaseDirectory() {
        return indexBaseDirectory;
    }

    public void setIndexBaseDirectory(String indexBaseDirectory) {
        this.indexBaseDirectory = indexBaseDirectory;
    }

    public String getHibernateDialect() {
        return hibernateDialect;
    }

    public void setHibernateDialect(String hibernateDialect) {
        this.hibernateDialect = hibernateDialect;
    }

    public String getHibernateDriverClass() {
        return hibernateDriverClass;
    }

    public void setHibernateDriverClass(String hibernateDriverClass) {
        this.hibernateDriverClass = hibernateDriverClass;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public static void main(String[] args) {

        
    	PropertyConfigurator.configure("resources/classpath/log4j.index.properties");
    	
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

        ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext(
                new String[] {"classpath:applicationContext.xml"});
        PopulateLuceneIndices indexer = ctx.getBean("populateLuceneIndices", PopulateLuceneIndices.class);

        if (iga.isOrganism()) {
            indexer.setOrganism(iga.getOrganism());
        }

        indexer.setFailFast(iga.getFailFast());

        if  (iga.isNumBatches()) {
            indexer.setNumBatches(iga.getNumBatches());
        }

        if (iga.isBatchSize()) {
            indexer.setBatchSize(iga.getBatchSize());
        }

        if (iga.isFeatureStart()) {
            indexer.setFeatureStart(iga.getFeatureStart());
        }

        if (iga.isFeatureEnd()) {
            indexer.setFeatureEnd(iga.getFeatureEnd());
        }

        indexer.setIndexBaseDirectory(iga.getIndexDirectory());

        indexer.indexFeatures();
        logger.trace("Leaving main");
        System.exit(0);
    }

    public void setFeatureStart(int featureStart) {
        this.featureStart = featureStart;
    }

    public void setFeatureEnd(int featureEnd) {
        this.featureEnd = featureEnd;
    }

}