package org.genedb.db.loading.interpro;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.genedb.db.dao.SequenceDao;
import org.genedb.db.loading.FeatureUtils;
import org.genedb.db.loading.GoInstance;
import org.gmod.schema.general.DbXRef;
import org.gmod.schema.sequence.FeatureDbXRef;
import org.gmod.schema.sequence.feature.AbstractGene;
import org.gmod.schema.sequence.feature.Polypeptide;
import org.gmod.schema.sequence.feature.PolypeptideDomain;
import org.gmod.schema.sequence.feature.ProductiveTranscript;
import org.gmod.schema.sequence.feature.Transcript;
import org.hibernate.EmptyInterceptor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;


public class InterProLoader {
    private static final Logger logger = Logger.getLogger(InterProLoader.class);

    private SequenceDao sequenceDao;
    private FeatureUtils featureUtils;
    private HibernateTransactionManager hibernateTransactionManager;
    private DbXRefManager dbxrefManager;

    public void load(InterProFile interProFile) {
        SessionFactory sessionFactory = hibernateTransactionManager.getSessionFactory();
        this.dbxrefManager = new DbXRefManager();
        Session session = SessionFactoryUtils.getSession(sessionFactory, dbxrefManager, null);
        TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
        Transaction transaction = session.getTransaction();

        Collection<String> genes = interProFile.genes();
        int n=1;
        for (String gene: genes) {
            transaction.begin();
            logger.info(String.format("Processing gene '%s' [%d/%d]", gene, n++, genes.size()));
            loadGene(interProFile, gene);
            transaction.commit();
            if (n % 50 == 1) {
                logger.info("Clearing session");
                session.clear();
            }
        }

        TransactionSynchronizationManager.unbindResource(sessionFactory);
        SessionFactoryUtils.releaseSession(session, sessionFactory);
    }

    /**
     * Maintains a cache of the DbXRefs that have been created but
     * not yet flushed, so we can look them up even though they're
     * not in the database yet. The cache is cleared whenever
     * Hibernate flushes.
     *
     * @author rh11
     */
    private class DbXRefManager extends EmptyInterceptor {
        private Map<String,Map<String,DbXRef>> dbxrefsByAccByDb
            = new HashMap<String,Map<String,DbXRef>>();

        @Override
        @SuppressWarnings("unchecked")
        public void postFlush(@SuppressWarnings("unused") Iterator entities) {
            logger.debug("Flushing dbxrefs");
            dbxrefsByAccByDb = new HashMap<String,Map<String,DbXRef>>();
        }

        /**
         * Get or create a DbXRef. Works even if the DbXRef has been
         * created but not yet flushed, unlike
         * {@link FeatureUtils#findOrCreateDbXRefFromDbAndAccession(String,String)}
         *
         * @param identifier A string of the form <code>db:accession</code>
         * @return the existing or newly-created DbXRef
         */
        public DbXRef get(String identifier) {
            int colonIndex = identifier.indexOf(':');
            if (colonIndex == -1)
                throw new IllegalArgumentException(String.format(
                    "Failed to parse dbxref identifier '%s'", identifier));
            return get(identifier.substring(0, colonIndex), identifier.substring(colonIndex + 1));
        }

        /**
         * Get or create a DbXRef. Works even if the DbXRef has been
         * created but not yet flushed, unlike
         * {@link FeatureUtils#findOrCreateDbXRefFromDbAndAccession(String,String)}
         *
         * @param dbName The database name
         * @param accession The database-specific identifier
         * @return the existing or newly-created DbXRef
         */
        public DbXRef get(String dbName, String accession) {
            logger.debug(String.format("Getting DbXRef '%s'/'%s'", dbName, accession));
            if (!dbxrefsByAccByDb.containsKey(dbName))
                dbxrefsByAccByDb.put(dbName, new HashMap<String,DbXRef>());

            if (dbxrefsByAccByDb.get(dbName).containsKey(accession))
                return dbxrefsByAccByDb.get(dbName).get(accession);

            DbXRef dbxref = featureUtils.findOrCreateDbXRefFromDbAndAccession(dbName, accession);

            /* The above statement can trigger a flush, hence the apparent
             * repetition here. */
            if (!dbxrefsByAccByDb.containsKey(dbName))
                dbxrefsByAccByDb.put(dbName, new HashMap<String,DbXRef>());

            dbxrefsByAccByDb.get(dbName).put(accession, dbxref);
            return dbxref;
        }

        /**
         * Get or create a DbXRef, and set the description if it's not already set.
         * Works even if the DbXRef has been created but not yet flushed, unlike
         * {@link FeatureUtils#findOrCreateDbXRefFromDbAndAccession(String,String)}
         *
         * @param dbName The database name
         * @param accession The database-specific identifier
         * @param description The description to use
         * @return the existing or newly-created DbXRef
         */
        public DbXRef get(String dbName, String accession, String description) {
            DbXRef dbxref = get(dbName, accession);
            if (dbxref.getDescription() == null)
                dbxref.setDescription(description);
            return dbxref;
        }
    }

    private void loadGene(InterProFile interProFile, String gene) {
        Set<String> goTermIds = new HashSet<String>();
        Polypeptide polypeptide = getPolypeptideForGene(gene);
        if (polypeptide == null)
            return;
        for (InterProAcc acc: interProFile.accsForGene(gene)) {
            logger.debug(String.format("Processing '%s'", acc.getId()));
            loadGroup(interProFile, gene, acc, polypeptide, goTermIds);
        }
    }

    private Polypeptide getPolypeptideForGene(String geneUniqueName) {
        AbstractGene gene = sequenceDao.getFeatureByUniqueName(geneUniqueName,
            AbstractGene.class);
        if (gene == null) {
            logger.error(String.format("Gene '%s' not found in database", geneUniqueName));
            return null;
        }

        Collection<Transcript> transcripts = gene.getTranscripts();
        if (transcripts.isEmpty()) {
            logger.error(String.format("Gene '%s' has no transcripts", geneUniqueName));
            return null;
        }

        // Select the coding transcript with the least feature_id,
        // logging an error if there's more than one.
        ProductiveTranscript selectedTranscript = null;
        int numberOfProductiveTranscripts = 0;
        for (Transcript transcript : transcripts)
            if (transcript instanceof ProductiveTranscript) {
                ++ numberOfProductiveTranscripts;
                if (selectedTranscript == null
                    || transcript.getFeatureId() < selectedTranscript.getFeatureId())
                {
                    selectedTranscript = (ProductiveTranscript) transcript;
                }
            }

        if (selectedTranscript == null) {
            logger.error("Gene '%s' has no coding transcripts.");
            return null;
        }
        if (numberOfProductiveTranscripts > 1)
            logger.error(String.format("The gene '%s' is alternatively spliced: " +
        		"we don't know to which transcript the domain hits apply.\n" +
        		"We've selected '%s', the first coding transcript by loading" +
        		"order: there's no reason to believe that is right!",
        		geneUniqueName, selectedTranscript.getUniqueName()));

        return selectedTranscript.getProtein();
    }

    private void loadGroup(InterProFile interProFile, String gene, InterProAcc acc,
            Polypeptide polypeptide, Set<String> goTermIds) {
        logger.debug("In loadGroup()");
        DbXRef interproDbxref = null;
        if (acc != InterProAcc.NULL) {
            logger.debug(String.format("Creating InterPro dbxref for '%s' with description '%s'",
                acc.getId(), acc.getDescription()));
            interproDbxref = dbxrefManager.get("InterPro", acc.getId(), acc.getDescription());
        }

        int n = -1;
        for (InterProRow row: interProFile.rows(gene, acc)) {
            n++;
            logger.debug(row);

            // Insert polypeptide_domain
            DbXRef dbxref = dbxrefManager.get(row.db, row.nativeAcc, row.nativeDesc);
            if (dbxref == null) {
                logger.error(String.format("Could not find dbxref '%s'/'%s'", row.db, row.nativeAcc));
                continue;
            }

            String domainUniqueName;
            if (n == 0)
                domainUniqueName = String.format("%s:%s", polypeptide.getUniqueName(), row.nativeAcc);
            else
                domainUniqueName = String.format("%s:%s:%d",
                    polypeptide.getUniqueName(), row.nativeAcc, n);

            PolypeptideDomain polypeptideDomain = sequenceDao.createPolypeptideDomain(
                domainUniqueName, polypeptide, row.score, row.desc, row.fmin, row.fmax, dbxref);

            // link to Interpro dbxref if applicable
            if (interproDbxref != null) {
                FeatureDbXRef featureDbXRef = new FeatureDbXRef(interproDbxref, polypeptideDomain, true);
                sequenceDao.persist(featureDbXRef);
                polypeptideDomain.addFeatureDbXRef(featureDbXRef);
            }

            // Insert GO terms, if there are any
            for (GoInstance goTerm: row.goTerms) {
                if (goTermIds.contains(goTerm.getId())) {
                    logger.debug(String.format("The GO term '%s' has already been added to gene '%s'",
                        goTerm.getId(), gene));
                    continue;
                }
                goTermIds.add(goTerm.getId());

                logger.debug(String.format("Creating GO term '%s' for domain '%s'",
                    goTerm.getId(), polypeptideDomain.getUniqueName()));

                featureUtils.createGoEntries(polypeptide, goTerm,
                    "From Interpro file", dbxrefManager.get(goTerm.getWithFrom()));
            }
        }
    }


    public void setFeatureUtils(FeatureUtils featureUtils) {
        this.featureUtils = featureUtils;
    }

    public void setHibernateTransactionManager(
            HibernateTransactionManager hibernateTransactionManager) {
        this.hibernateTransactionManager = hibernateTransactionManager;
    }

    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }
}

