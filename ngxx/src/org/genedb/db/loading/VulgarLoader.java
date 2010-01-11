package org.genedb.db.loading;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Load EST matches from an Exonerate Vulgar file.
 *
 * @author rh11
 */
@Configurable
public class VulgarLoader {
    /*
     * Implementation note: the first version of this code used Hibernate, but
     * was heavily CPU-bound and unacceptably slow. Thus it has been rewritten
     * to use JDBC, and is now thoroughly database-bound and reasonably fast.
     */

    private static final Logger logger = Logger.getLogger(VulgarLoader.class);

    @Autowired
    private SimpleJdbcTemplate simpleJdbcTemplate;

    private PlatformTransactionManager transactionManager;

    // Configurable parameters
    private int organismId;

    /**
     * Set the organism into which to load data.
     *
     * @param organismCommonName the common name of the organism
     */
    public void setOrganismCommonName(String organismCommonName) {
        this.organismId = getOrganismIdFromCommonName(organismCommonName);
    }

    private int getOrganismIdFromCommonName(String organismCommonName) {
        Map<String,Object> idMap = simpleJdbcTemplate.queryForMap(
            "select organism_id from organism where common_name = ?",
            organismCommonName);
        int organismId =  (Integer) idMap.get("organism_id");
        logger.trace(String.format("Organism '%s' has ID %d", organismCommonName, organismId));
        return organismId;
    }

    private String matchType = "EST_match";

    private static Set<String> permittedMatchTypes = new HashSet<String>();
    static {
        permittedMatchTypes.add("nucleotide_match");
        permittedMatchTypes.add("cDNA_match");
        permittedMatchTypes.add("EST_match");
    }

    public void setMatchType(String matchType) {
        this.matchType = matchType;
    }

    private TransactionStatus transactionStatus;
    private TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
    public synchronized void load(VulgarFile file) throws ParsingException, IOException {
        /*
         * This class does not use the declarative @Transactional annotation,
         * because we wish to split the load into a number of consecutive
         * transactions.
         */

        transactionStatus = transactionManager.getTransaction(transactionDefinition);
        init();

        try {
            int i = 0;
            // The iteration implicit in the for(:) loop might cause a VulgarFileException
            for (VulgarMapping mapping: file) {
                loadMapping(mapping);
                if (++i % 100 == 0) {
                    logger.info(String.format("Loaded %d mappings", i));
                    logger.debug("Committing transaction.");
                    transactionManager.commit(transactionStatus);
                    transactionStatus = transactionManager.getTransaction(transactionDefinition);
                }
            }
        } catch (VulgarFileException e) {
            logger.trace("Exception caught. Rolling back.");
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
            logger.trace("Exception caught. Rolling back.");
            transactionManager.rollback(transactionStatus);
            throw e;
        }
    }


    private int matchTypeId;
    private int matchPartTypeId;
    private int partOfTypeId;
    private void init() {
        matchTypeId     = getCvTermId("sequence", matchType);
        matchPartTypeId = getCvTermId("sequence", "match_part");
        partOfTypeId    = getCvTermId("relationship", "part_of");
    }

    private int getCvTermId(String cvName, String cvTermName) {
        return (Integer) simpleJdbcTemplate.queryForMap(
            "select cvterm_id "+
            "from cvterm "+
            "join cv using (cv_id) "+
            "where cv.name = ? "+
            "and cvterm.name = ?",
            cvName, cvTermName
        ).get("cvterm_id");
    }

    private void loadMapping(VulgarMapping mapping) throws VulgarFileException, DataError {

        String matchUniqueName = insertMatch(mapping);
        int matchId = currentFeatureId();
        insertFeatureLoc(matchId, mapping.getQuery(),  mapping.getQMin(), mapping.getQMax(), mapping.getQStrand(), 0);
        insertFeatureLoc(matchId, mapping.getTarget(), mapping.getTMin(), mapping.getTMax(), mapping.getTStrand(), 1);

        int sourcePos = 0;
        int targetPos = 0;
        int partIndex = 0;
        for (VulgarMapping.Match match: mapping.getMatches()) {
            switch (match.getType()) {
            case MATCH:
                createMatchPart(matchId, matchUniqueName, partIndex++,
                    sourcePos, match.getQueryLength(),
                    targetPos, match.getTargetLength());
                /*FALL THROUGH*/
            default:
                sourcePos += match.getQueryLength();
                targetPos += match.getTargetLength();
            }
        }
    }

    private int currentFeatureId() {
        long featureId = (Long) simpleJdbcTemplate.queryForMap(
            "select currval('feature_feature_id_seq'::regclass) as feature_id")
            .get("feature_id");

        return (int) featureId;
    }

    private Set<String> matchUniqueNames = new HashSet<String>();
    private String insertMatch(VulgarMapping mapping) {
        String matchUniqueName;
        if (mapping.getTStrand() >= 0) {
            matchUniqueName = String.format("match:%s@%s:%d-%d",
                mapping.getQuery(), mapping.getTarget(), mapping.getTMin(), mapping.getTMax());
        } else {
            matchUniqueName = String.format("match:%s@%s:(%d-%d)",
                mapping.getQuery(), mapping.getTarget(), mapping.getTMin(), mapping.getTMax());
        }

        // It's possible to have more than one match at the same location,
        // so make sure the name is unique;
        String originalMatchUniqueName = matchUniqueName;
        int i = 1;
        while (matchUniqueNames.contains(matchUniqueName)) {
            matchUniqueName = originalMatchUniqueName + ":" + i++;
        }
        matchUniqueNames.add(matchUniqueName);

        simpleJdbcTemplate.update(
            "insert into feature ("+
            " organism_id, uniquename, type_id, is_analysis"+
            ") values ("+
            " ?, ?, ?, true"+
            ")",
            organismId, matchUniqueName, matchTypeId);
        logger.trace(String.format("Inserted ESTMatch feature '%s'", matchUniqueName));
        return matchUniqueName;
    }

    private void insertFeatureLoc(int featureId, String sourceFeature, int fmin, int fmax, int strand, int rank)
            throws DataError {
        int n = simpleJdbcTemplate.update(
            "insert into featureloc ("+
            " feature_id, srcfeature_id, fmin, fmax, strand, locgroup, rank"+
            ") ("+
            "  select ?"+
            "       , feature.feature_id"+
            "       , ?, ?, ?, 0, ?"+
            "  from feature"+
            "  where feature.uniqueName = ?"+
            ")",
            featureId, fmin, fmax, strand, rank, sourceFeature);

        if (n != 1) {
            throw new DataError(String.format("Feature '%s' not found", sourceFeature));
        }
        logger.trace(String.format("Inserted featureLoc (featureId=%d, fmin=%d, fmax=%d, strand=%d, rank=%d) to '%s'",
            featureId, fmin, fmax, strand, rank, sourceFeature));

        n = simpleJdbcTemplate.update(
            "insert into featureloc ("+
            " feature_id, srcfeature_id, fmin, fmax, strand, locgroup, rank"+
            ") ("+
            "  select ? as feature_id"+
            "       , feature.feature_id as srcfeature_id"+
            "       , ? + featureloc.fmin as fmin" +
            "       , ? + featureloc.fmin as fmax" +
            "       , ? as strand" +
            "       , featureloc.locgroup + 1 as locgroup" +
            "       , ? as rank"+
            "  from feature"+
            "  join featureloc using (feature_id)"+
            "  where feature.uniqueName = ?"+
            "  and featureloc.rank = 0"+
            ")",
            featureId, fmin, fmax, strand, rank, sourceFeature);
        logger.trace(String.format("Inserted %d dependent featureLocs for '%s'", n, sourceFeature));
    }

    private void createMatchPart(int matchId, String matchUniqueName, int partIndex,
            int sourcePos, int sourceLength, int targetPos, int targetLength)
    {
        String partUniqueName = String.format("%s:part%d", matchUniqueName, partIndex);
        simpleJdbcTemplate.update(
            "insert into feature ("+
            " organism_id, uniquename, type_id, is_analysis"+
            ") values ("+
            " ?, ?, ?, true"+
            ")",
            organismId, partUniqueName, matchPartTypeId);
        logger.trace(String.format("Inserted MatchPart '%s'", partUniqueName));

        int partId = currentFeatureId();

        simpleJdbcTemplate.update(
            "insert into feature_relationship ("+
            "  subject_id, type_id, object_id"+
            ") values ("+
            "  ?, ?, ?"+
            ")",
            partId, partOfTypeId, matchId);
        logger.trace("Inserted FeatureRelationship");

        createPartLoc(partId, matchId, sourcePos, sourceLength, 0);
        createPartLoc(partId, matchId, targetPos, targetLength, 1);
    }

    private void createPartLoc(int partId, int matchId, int pos, int length, int rank) {
        int n = simpleJdbcTemplate.update(
            "insert into featureloc ("+
            " feature_id, srcfeature_id, fmin, fmax, strand, locgroup, rank"+
            ") ("+
            "  select ? as feature_id"+
            "       , featureloc.srcfeature_id"+
            "       , featureloc.fmin + ?"+
            "       , featureloc.fmin + ?"+
            "       , featureloc.strand"+
            "       , featureloc.locgroup"+
            "       , featureloc.rank"+
            "  from featureloc"+
            "  where featureloc.feature_id = ?"+
            "  and featureloc.rank = ?"+
            ")",
            partId, pos, pos + length, matchId, rank
        );
        logger.trace(String.format("Created %d featurelocs for part ID=%d of match ID=%d at %d, length %d with rank %d",
            n, partId, matchId, pos, length, rank));
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }
}
