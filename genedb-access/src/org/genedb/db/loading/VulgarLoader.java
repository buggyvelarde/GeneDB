package org.genedb.db.loading;

import org.genedb.db.dao.OrganismDao;
import org.genedb.db.dao.SequenceDao;

import org.gmod.schema.feature.EST;
import org.gmod.schema.feature.ESTMatch;
import org.gmod.schema.feature.MatchPart;
import org.gmod.schema.feature.TopLevelFeature;
import org.gmod.schema.mapped.Organism;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.io.IOException;

/**
 * Load EST matches from an Exonerate Vulgar file.
 *
 * @author rh11
 */
@Configurable
public class VulgarLoader {
    private static final Logger logger = Logger.getLogger(VulgarLoader.class);

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private OrganismDao organismDao;

    @Autowired
    private SequenceDao sequenceDao;

    // Configurable parameters
    private Organism organism;

    /**
     * Set the organism into which to load data.
     *
     * @param organismCommonName the common name of the organism
     */
    public void setOrganismCommonName(String organismCommonName) {
        this.organism = organismDao.getOrganismByCommonName(organismCommonName);
        if (organism == null) {
            throw new IllegalArgumentException(String.format("Organism '%s' not found", organismCommonName));
        }
    }

    private Session session;
    private TransactionStatus transactionStatus;
    private TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
    public synchronized void load(VulgarFile file) throws ParsingException, IOException {
        /*
         * This class does not use the declarative @Transactional annotation,
         * because we need to split the load into a number of consecutive
         * transactions if we are not to run out of heap space.
         */

        transactionStatus = transactionManager.getTransaction(transactionDefinition);
        session = SessionFactoryUtils.doGetSession(sessionFactory, false);

        try {
            int i = 0;
            // The iteration in the for(:) loop might cause a VulgarFileException
            for (VulgarMapping mapping: file) {
                loadMapping(mapping);
                if (++i % 10 == 0) {
                    logger.info(String.format("Loaded %d mappings", i));
                    logger.debug("Committing transaction and clearing session");
                    transactionManager.commit(transactionStatus); // Also closes session!
                    transactionStatus = transactionManager.getTransaction(transactionDefinition);
                    session = SessionFactoryUtils.doGetSession(sessionFactory, false);
                }
            }
        } catch (VulgarFileException e) {
            transactionManager.rollback(transactionStatus);
            Throwable t = e.getCause();
            if (t == null) {
                throw new RuntimeException("VulgarFileException of null type", e);
            }
            if (t instanceof IOException) {
                throw (IOException) t;
            }
            if (t instanceof ParsingException) {
                throw (ParsingException) t;
            }
            throw new RuntimeException("VulgarFileException of unknown type", e);
        }
        catch (RuntimeException e) {
            transactionManager.rollback(transactionStatus);
            throw e;
        }
    }

    private void loadMapping(VulgarMapping mapping) throws VulgarFileException, DataError {
        EST est = sequenceDao.getFeatureByUniqueName(mapping.getQuery(), EST.class);
        if (est == null) {
            throw new DataError(String.format("Could not find EST feature '%s'", mapping.getQuery()));
        }
        TopLevelFeature target = sequenceDao.getFeatureByUniqueName(mapping.getTarget(), TopLevelFeature.class);
        if (target == null) {
            throw new DataError(String.format("Could not find target feature '%s'", mapping.getTarget()));
        }

        logger.debug(String.format("Creating ESTMatch '%s'(%d-%d s%d)->'%s'(%d-%d s%d)",
            est.getUniqueName(), mapping.getQMin(), mapping.getQMax(), mapping.getQStrand(),
            target.getUniqueName(), mapping.getTMin(), mapping.getTMax(), mapping.getTStrand()));

        // ESTMatch.create(EST est, TopLevelFeature target,
        //          int sourceFmin, int sourceFmax, int sourceStrand,
        //          int targetFmin, int targetFmax, int targetStrand)
        ESTMatch estMatch = ESTMatch.create(est, target,
            mapping.getQMin(), mapping.getQMax(), mapping.getQStrand(),
            mapping.getTMin(), mapping.getTMax(), mapping.getTStrand());
        session.persist(estMatch);

        int sourcePos = 0;
        int targetPos = 0;
        int partIndex = 0;
        for (VulgarMapping.Match match: mapping.getMatches()) {
            switch (match.getType()) {
            case MATCH:
                String partName = String.format("%s:part%d", estMatch.getUniqueName(), partIndex++);
                logger.debug(String.format("Creating match part '%s'", partName));
                MatchPart matchPart = estMatch.createPart(partName, sourcePos, match.getQueryLength(),
                    targetPos, match.getTargetLength());
                session.persist(matchPart);
                /*FALL THROUGH*/
            default:
                sourcePos += match.getQueryLength();
                targetPos += match.getTargetLength();
            }
        }
    }
}
