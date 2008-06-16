package org.genedb.db.loading.interpro;

import java.sql.SQLException;
import java.util.Collection;

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
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;


public class InterProLoader {
    private static final Logger logger = Logger.getLogger(InterProLoader.class);

    private SequenceDao sequenceDao;
    private FeatureUtils featureUtils;
    private HibernateTransactionManager hibernateTransactionManager;

    public void load(InterProFile interProFile) {
        SessionFactory sessionFactory = hibernateTransactionManager.getSessionFactory();
        Session session = SessionFactoryUtils.doGetSession(sessionFactory, true);
        TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
        Transaction transaction = session.getTransaction();

        for (String gene: interProFile.genes()) {
            transaction.begin();
            logger.debug(String.format("Processing gene '%s'", gene));
            loadGene(interProFile, gene);
            transaction.commit();
        }

        TransactionSynchronizationManager.unbindResource(sessionFactory);
        SessionFactoryUtils.closeSession(session);
    }

    private void loadGene(InterProFile interProFile, String gene) {
        Polypeptide polypeptide = getPolypeptideForGene(gene);
        if (polypeptide == null)
            return;
        for (InterProAcc acc: interProFile.accsForGene(gene)) {
            logger.debug(String.format("Processing '%s'", acc.getId()));
            loadGroup(interProFile, gene, acc, polypeptide);
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

    private void loadGroup(InterProFile interProFile, String gene, InterProAcc acc, Polypeptide polypeptide) {
        logger.debug("In loadGroup()");
        DbXRef interproDbxref = null;
        if (acc != InterProAcc.NULL) {
            logger.debug(String.format("Creating InterPro dbxref for '%s'", acc.getId()));
            interproDbxref = featureUtils.findOrCreateDbXRefFromDbAndAccession("InterPro", acc.getId());
            interproDbxref.setDescription(acc.getDescription());
        }

        for (InterProRow row: interProFile.rows(gene, acc)) {
            logger.debug(row);

            // Insert polypeptide_domain, link to Interpro dbxref if applicable
            DbXRef dbxref = featureUtils.findOrCreateDbXRefFromDbAndAccession(row.db, row.nativeAcc);
            PolypeptideDomain polypeptideDomain = sequenceDao.createPolypeptideDomain(polypeptide, row.nativeAcc, row.score, row.desc, row.fmin, row.fmax, dbxref);

            if (interproDbxref != null) {
                FeatureDbXRef featureDbXRef = new FeatureDbXRef(interproDbxref, polypeptide, true);
                polypeptideDomain.addFeatureDbXRef(featureDbXRef);
            }

            // Insert GO terms, if there are any
            for (GoInstance goTerm: row.goTerms) {
                try {
                    featureUtils.createGoEntries(polypeptide, goTerm);
                }
                catch (DataIntegrityViolationException exception) {
                    logger.error(String.format("Failed to create GO term '%s' for domain '%s'", goTerm.getId(), polypeptideDomain.getUniqueName()));
                    Throwable cause = exception.getMostSpecificCause();
                    logger.error(cause.getMessage());
                    if (cause instanceof SQLException) {
                        Throwable ultimateCause = null;
                        for (Throwable chainedException: (SQLException) cause)
                            ultimateCause = chainedException;

                        logger.error(ultimateCause);
                        System.exit(1); // TODO remove this line
                    }
                }
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

