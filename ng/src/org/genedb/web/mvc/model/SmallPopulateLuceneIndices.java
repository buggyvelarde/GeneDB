package org.genedb.web.mvc.model;

import org.genedb.db.audit.ChangeSet;

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
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.reader.ReaderProvider;
import org.hibernate.search.store.DirectoryProvider;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;
import uk.co.flamingpenguin.jewel.cli.Cli;
import uk.co.flamingpenguin.jewel.cli.CliFactory;
import uk.co.flamingpenguin.jewel.cli.Option;

import java.io.Console;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

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
public class SmallPopulateLuceneIndices {//implements IndexUpdater {
    private static Logger logger = Logger.getLogger(SmallPopulateLuceneIndices.class);

    /**
     * The number of features to be processed in a single batch. If it's set too
     * high, we run out of heap space.
     */
    private static final int BATCH_SIZE = 10;

    /**
     * Which types of feature to index.
     */
    private static final Collection<Class<? extends Feature>> INDEXED_CLASSES = new ArrayList<Class<? extends Feature>>();
    static {
        INDEXED_CLASSES.add(AbstractGene.class);
        INDEXED_CLASSES.add(Transcript.class);
        INDEXED_CLASSES.add(Polypeptide.class);
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
    private int numBatches = 10;

    private String hibernateDialect = "org.hibernate.dialect.PostgreSQLDialect";
    private String hibernateDriverClass = "org.postgresql.Driver";


    /**
     * Index features of the specified class. First of all indexes the features
     * in batches, and then retries the failures one-by-one.
     *
     * @param featureClass
     * @param numBatches
     */
    public void indexFeatures(Class<? extends Feature> featureClass, FullTextSession session) {
       
//        Transaction transaction = session.beginTransaction();
        logger.info(String.format("A. The value of session is '%s' and it is '%s'", session, session.isConnected()));
        //Set<Integer> failed = batchIndexFeatures(featureClass, -1, session);
        batchIndexFeatures(featureClass, session);
//        transaction.commit();
        //logger.info("About to close session");
        //session.close();
        //logger.info("Session closed");

//        if (failed.size() > 0) {
//            logger.info("calling reindex");
//            reindexFailedFeatures(failed);
//        }
        logger.info("Got to end of indexFeatures(Class)");
    }

    public void indexFeatures() {
     SessionFactory sessionFactory = null;
      try {
          sessionFactory = configurableGeneDBSessionFactoryBean.createFullTextSessionFactory(indexBaseDirectory, 10);
      } catch (Exception exp) {
          exp.printStackTrace();
          System.exit(65);
      }
      logger.info("sessionFactory is "+sessionFactory);
      Session session = sessionFactory.openSession();
      FullTextSession fs = Search.getFullTextSession(session);
      
//      session.setFlushMode(FlushMode.MANUAL);
//      session.setCacheMode(CacheMode.IGNORE);
//      logger.info(String.format("Just made. The value of session is '%s' and it is '%s'", session, session.isConnected()));
//      return session;
        Transaction tx = fs.beginTransaction();
        for (Class<? extends Feature> featureClass: INDEXED_CLASSES) {
            indexFeatures(featureClass, fs);
        }
        //fs.getSearchFactory().optimize(Feature.class);
        tx.commit();
        fs.close();
        logger.info("Got to end of indexFeatures()");
        throw new RuntimeException();
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
    //@Transactional
    private void batchIndexFeatures(Class<? extends Feature> featureClass,
            FullTextSession fullTextSession) {

        fullTextSession.setFlushMode(FlushMode.MANUAL); 
        fullTextSession.setCacheMode(CacheMode.IGNORE); 
        Transaction transaction = fullTextSession.beginTransaction(); 
        //Scrollable results will avoid loading too many objects in memory 
        Set<Integer> failedToLoad = new HashSet<Integer>();
        Criteria criteria = fullTextSession.createCriteria(featureClass);
        criteria.add(Restrictions.eq("obsolete", false)); // Do not index obsolete features
        if (organism != null) {
            criteria.createCriteria("organism")
            .add( Restrictions.eq("commonName", organism));
        }
        if (numBatches > 0) {
            criteria.setMaxResults(numBatches * BATCH_SIZE);
        }
        
        ScrollableResults results = criteria.setFetchSize(BATCH_SIZE).scroll(ScrollMode.FORWARD_ONLY);

        logger.info(String.format("Indexing %s", featureClass));
        
        int thisBatchCount = 0;
        Set<Integer> thisBatch = new HashSet<Integer>();
        int index = 0; 
        while( results.next() ) {            
            Feature feature = (Feature) results.get(0);
            thisBatch.add(feature.getFeatureId());
            boolean failed = false;
            
            
            index++; 

            
            //try {
            //    logger.debug(String.format("Indexing '%s' (%s)", feature.getUniqueName(),
            //        feature.getClass()));
            fullTextSession.index( feature ); //index each element 
            //} catch (Exception e) {
            //    logger.error("Batch failed", e);
            //    failed = true;
            //}
            
//            if (failed || ++thisBatchCount == BATCH_SIZE) {
//                logger.info(String.format("Indexed %d of %s", i, featureClass));
//                session.clear();
//                thisBatchCount = 0;
//                if (failed) {
//                    failedToLoad.addAll(thisBatch);
//                }
//                thisBatch = new HashSet<Integer>();
//            }
            
            
            if (index % BATCH_SIZE == 0) { 
                fullTextSession.flushToIndexes(); //apply changes to indexes 
                fullTextSession.clear(); //clear since the queue is processed 
            } 
        } 
        transaction.commit(); 
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

        ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext(
                new String[] {"classpath:applicationContext.xml"});
        SmallPopulateLuceneIndices indexer = ctx.getBean("smallPopulateLuceneIndices", SmallPopulateLuceneIndices.class);

        if (iga.isOrganism()) {
            indexer.setOrganism(iga.getOrganism());
        }

        indexer.setFailFast(iga.getFailFast());

        if  (iga.isNumBatches()) {
            indexer.setNumBatches(iga.getNumBatches());
        }

        indexer.setIndexBaseDirectory(iga.getIndexDirectory());

        try {
            indexer.indexFeatures();
        }
        catch (RuntimeException exp) {
            exp.printStackTrace();
            System.err.println("About to go into finally block");
        }
        finally {
            System.err.println("Going to try to close context");
            ctx.close();
        }
        System.err.println("All indexing finished");
        Map<Thread, StackTraceElement[]> m = Thread.getAllStackTraces();
        for (Entry<Thread, StackTraceElement[]> entry : m.entrySet()) {
            Thread t = entry.getKey();
            if (t.getName().matches("pool-\\d+-thread-\\d+")) {
                try {
                    t.stop();
                }
                catch (ThreadDeath td) {
                    td.printStackTrace();
                    throw td;
                }
            }
        }
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


}